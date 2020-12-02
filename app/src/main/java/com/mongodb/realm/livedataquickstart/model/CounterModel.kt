package com.mongodb.realm.livedataquickstart.model

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.map
import io.realm.Realm
import io.realm.RealmConfiguration
import io.realm.kotlin.where

class CounterModel : ViewModel() {

    private var realm: Realm

    private val _counter: LiveRealmObject<Counter>
    val counter: LiveData<String>
        get() = _counter.map { it.value.get().toString() }

    init {
//        // :code-block-start: appid
//        // :hide-start:
//        val appID = "unfsck-dmrvz" // replace this with your App ID
//        // :replace-with:
//        // val appID = "YOUR APP ID HERE" // TODO: replace this with your App ID
//        // :hide-end:
//
//        val app = App(
//            AppConfiguration.Builder(appID)
//                .build()
//        )
//        // :code-block-end:
//
//        // log in to the app so we can access realm
//        app.login(Credentials.anonymous())
//
//        Log.v("QUICKSTART", "Successfully authenticated anonymously.")
//        val user: User? = app.currentUser()
//        val partitionValue = "My Project"
//
//        val config = SyncConfiguration.Builder(user!!, partitionValue)
//            // because this application only reads/writes small amounts of data, it's OK to read/write from the UI thread
//            .allowWritesOnUiThread(true)
//            .allowQueriesOnUiThread(true)
//            .build()

        val config = RealmConfiguration.Builder()
            .allowWritesOnUiThread(true)
            .allowQueriesOnUiThread(true)
            .build()

        // open a realm
        realm = Realm.getInstance(config)

        // access all counters stored in this realm
        val counterQuery = realm.where<Counter>()
        val counters = counterQuery.findAll()

        // if we haven't created the one counter for this app before (as on first launch), create it now
        if (counters.size == 0) {
            realm.executeTransaction { transactionRealm ->
                val counter = Counter()
                transactionRealm.insert(counter)
            }
        }

        // there should be one and only one counter at this point, so we can just grab the 0th index
        this._counter = LiveRealmObject(counters[0])
    }

    fun incrementCounter() {
        realm.executeTransaction {
            checkNotNull(_counter.value).add()
        }
    }

    override fun onCleared() {
        realm.close()
    }
}
