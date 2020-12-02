package com.mongodb.realm.livedataquickstart.model

import androidx.annotation.MainThread
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import io.realm.RealmModel
import io.realm.RealmObject
import io.realm.RealmObjectChangeListener

/**
 * This class represents a RealmObject wrapped inside a LiveData.
 *
 * The provided RealmObject must be a managed object that exists in a realm on creation.
 *
 * When the enclosing LifecycleOwner is killed, the listener is automatically unsubscribed.
 *
 * Realm keeps the managed RealmObject up-to-date whenever a change occurs on any thread.
 * When the RealmObject changes, LiveRealmObject notifies the observer.
 *
 * LiveRealmObject observes the object until it is invalidated. You can invalidate the RealmObject by
 * deleting it or by closing the realm that owns it.
 *
 * @param obj the RealmModel instance to which we want to subscribe for changes
 * @param T the type of the RealmModel
 */
class LiveRealmObject<T : RealmModel?> @MainThread constructor(
    obj: T? = null
) : MutableLiveData<T>() {

    private val listener = RealmObjectChangeListener<T> { obj, objectChangeSet ->
        if (!objectChangeSet!!.isDeleted) {
            setValue(obj)
        } else { // Because invalidated objects are unsafe to set in LiveData, pass null instead.
            setValue(null)
        }
    }

    init {
        // it is allowed to initialize this LiveData with null - maybe we don't have anything in the DB yet
        value = obj
    }

    /**
     * Starts observing the RealmObject if we have observers and the object is still valid.
     */
    override fun onActive() {
        super.onActive()

        // remove all existing listeners in the current object
        removeListener(this.value)

        val obj = value
        if (obj != null && RealmObject.isValid(obj)) {
            RealmObject.addChangeListener(obj, listener)
        }
    }

    /**
     * Stops observing the RealmObject.
     */
    override fun onInactive() {
        super.onInactive()
        val obj = value
        if (obj != null && RealmObject.isValid(obj)) {
            RealmObject.removeChangeListener(obj, listener)
        }
    }

    /**
     * Sets the value for this LiveData instance. It is necessary to enforce only valid objects are
     * being set so that a RealmObjectChangeListener can be properly added.
     */
    override fun setValue(value: T?) {
        // remove all existing listeners in the current object
        removeListener(this.value)

        // add again for the new value
        if (value != null && RealmObject.isValid(value)) {
            RealmObject.addChangeListener(value, listener)
        }

        // proper assignation
        super.setValue(value)
    }

    private fun removeListener(value: T?) {
        value?.let { currentValue ->
            RealmObject.removeAllChangeListeners(currentValue)
        }
    }
}
