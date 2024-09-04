package com.example.foodie.data

import com.example.foodie.data.model.AppSettings
import com.example.foodie.data.model.Category
import com.example.foodie.data.model.DiscoverySettings
import com.google.android.libraries.places.api.model.Place
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class Repository(private var db: FirebaseFirestore) {
    val foodCategories = loadFoodCategories()

    init {
        db = FirebaseFirestore.getInstance()
    }

    fun getUserRef(userId: String): DocumentReference {
        val userRef = db.collection("users").document(userId)
        return userRef
    }

    suspend fun getRestaurantList(
        userRef: DocumentReference,
        collection: String
    ): MutableList<Place> {
        val collRef = userRef.collection(collection)
        val querySnapshot = withContext(Dispatchers.IO) {
            collRef.get().await()
        }

        val restaurantList = mutableListOf<Place>()
        for (document in querySnapshot.documents) {
            val place = document.toObject(Place::class.java)
            if (place != null) {
                restaurantList.add(place)
            }
        }
        return restaurantList
    }

    suspend fun addNewRestaurant(
        userRef: DocumentReference,
        collection: String,
        restaurantName: String,
        restaurantData: Place
    ) {
        val docRef = userRef.collection(collection).document(restaurantName)
        withContext(Dispatchers.IO) {
            docRef.set(restaurantData).await()
        }
    }

    suspend fun deleteLikedRestaurant(userRef: DocumentReference, restaurantName: String) {
        val docRef = userRef.collection("likes").document(restaurantName)
        withContext(Dispatchers.IO) {
            docRef.delete().await()
        }
    }

    suspend fun setAppSettings(userRef: DocumentReference, appSettings: AppSettings) {
        val docRef = userRef.collection("settings").document("app settings")
        withContext(Dispatchers.IO) {
            docRef.set(appSettings).await()
        }
    }

    suspend fun getAppSettings(userRef: DocumentReference): AppSettings? {
        val docRef = userRef.collection("settings").document("app settings")
        val documentSnapshot = withContext(Dispatchers.IO) {
            docRef.get().await()
        }
        return documentSnapshot.toObject(AppSettings::class.java)
    }

    suspend fun setDiscoverySettings(
        userRef: DocumentReference,
        discoverySettings: DiscoverySettings
    ) {
        val docRef = userRef.collection("settings").document("discovery settings")
        withContext(Dispatchers.IO) {
            docRef.set(discoverySettings).await()
        }
    }

    suspend fun getDiscoverySettings(userRef: DocumentReference): DiscoverySettings? {
        val docRef = userRef.collection("settings").document("discovery settings")
        val documentSnapshot = withContext(Dispatchers.IO) {
            docRef.get().await()
        }
        return documentSnapshot.toObject(DiscoverySettings::class.java)
    }

    private fun loadFoodCategories(): List<Category> {
        return foodCategoryData
    }
}
