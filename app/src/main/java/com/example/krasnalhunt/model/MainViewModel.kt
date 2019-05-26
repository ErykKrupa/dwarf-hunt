package com.example.krasnalhunt.model

import android.content.Context
import android.os.AsyncTask
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.android.gms.maps.GoogleMap
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.MetadataChanges
import com.google.firebase.firestore.SetOptions


class MainViewModel(context: Context) : ViewModel() {
    val database = AppDatabase.createInstance(context)
    val items by lazy { database.dwarfItemDao().findItems() }

    val auth: FirebaseAuth = FirebaseAuth.getInstance()
    val user: FirebaseUser? get() = auth.currentUser
    val firestore = FirebaseFirestore.getInstance()
    var firestoreListener: ListenerRegistration? = null

    val locationPermissionGranted = MutableLiveData<Boolean>()
    lateinit var map: GoogleMap

    fun observeFirestore(activity: AppCompatActivity) {
        firestoreListener?.remove()
        AsyncTask.execute {
            database.dwarfItemDao().resetCaught()
            activity.runOnUiThread {
                firestoreListener = firestore.collection("caught-dwarfs")
                    .document(user!!.uid)
                    .addSnapshotListener(MetadataChanges.EXCLUDE) { documentSnapshot, firebaseFirestoreException ->
                        documentSnapshot?.run {
                            AsyncTask.execute {
                                // TODO: simplify
                                data?.forEach { (dwarfIdString, caught) ->
                                    val id = dwarfIdString.toInt()
                                    val dwarf = database.dwarfItemDao().findItem(id)
                                    dwarf.caught = caught as Boolean
                                    database.dwarfItemDao().updateItems(dwarf)
                                }
                            }
                        } ?: run {
                            Log.e("TAG", "Document snapshot unavailable", firebaseFirestoreException)
                        }
                    }
            }
        }
    }

    fun updateCaught(dwarfItem: DwarfItem) {
        AsyncTask.execute {
            val dwarf = database.dwarfItemDao().findItem(dwarfItem.id)
            if (dwarf.caught) {
                firestore.collection("caught-dwarfs")
                    .document(auth.currentUser!!.uid).update(mapOf(dwarf.id.toString() to false))
            } else {
                firestore.collection("caught-dwarfs")
                    .document(auth.currentUser!!.uid).set(mapOf(dwarf.id.toString() to true), SetOptions.merge())
            }
        }
    }

    fun removeFirestoreListener() {
        firestoreListener?.remove()
        firestoreListener = null
    }

    fun init() = Unit
}