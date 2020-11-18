package com.mongodb.realm.livedataquickstart

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import com.mongodb.realm.livedataquickstart.model.Counter
import com.mongodb.realm.livedataquickstart.model.LiveRealmObject
import io.realm.Realm
import io.realm.kotlin.where
import io.realm.mongodb.App
import io.realm.mongodb.AppConfiguration
import io.realm.mongodb.Credentials
import io.realm.mongodb.User
import io.realm.mongodb.sync.SyncConfiguration

/**
 * A simple [Fragment] subclass as the default destination in the navigation.
 */
class CounterFragment : Fragment() {

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // :code-block-start: appid
        // :hide-start:
        val appID = "unfsck-dmrvz" // replace this with your App ID
        // :replace-with:
        // val appID = "YOUR APP ID HERE" // TODO: replace this with your App ID
        // :hide-end:

        val app: App = App(
            AppConfiguration.Builder(appID)
                .build()
        )
        // :code-block-end:

        // log in to the app so we can access realm
        app.loginAsync(Credentials.anonymous()) {
            if (it.isSuccess) {
                Log.v("QUICKSTART", "Successfully authenticated anonymously.")
                val user: User? = app.currentUser()
                val partitionValue: String = "My Project"

                val config = SyncConfiguration.Builder(user!!, partitionValue)
                    // because this application only reads/writes small amounts of data, it's OK to read/write from the UI thread
                    .allowWritesOnUiThread(true)
                    .allowQueriesOnUiThread(true)
                    .build()

                // open a realm, access all counters stored in this realm
                val realm: Realm = Realm.getInstance(config)
                val counterQuery = realm.where<Counter>()
                val counters = counterQuery.findAll()

                // if we haven't created the one counter for this app before (as on first launch), create it now
                if(counters.size == 0) {
                    realm.executeTransaction { transactionRealm ->
                        val counter = Counter()
                        transactionRealm.insert(counter)
                    }
                }

                // there should be one and only one counter at this point -- obtain a reference to it
                val counter = counters[0]

                // create a live object containing the counter
                val liveCounter = LiveRealmObject<Counter>(counter!!)

                liveCounter.observe(requireActivity(), androidx.lifecycle.Observer { obj : Counter? ->
                    view.findViewById<TextView>(R.id.textview).text = obj?.value?.get().toString()
                })

                view.findViewById<Button>(R.id.button).setOnClickListener {
                    realm.executeTransaction {
                        counter.add()
                    }
                }
            } else {
                Log.e("QUICKSTART", "Failed to log in. Error: ${it.error}")
            }
        }

    }
}