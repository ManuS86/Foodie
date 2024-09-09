package com.example.foodie.data

import com.example.foodie.data.local.foodCategoryData
import com.example.foodie.data.model.AppSettings
import com.example.foodie.data.model.Category
import com.example.foodie.data.model.DiscoverySettings
import com.example.foodie.data.model.Id
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class FirestoreRepository(private var db: FirebaseFirestore) {
    val foodCategories = loadFoodCategories()

    init {
        db = FirebaseFirestore.getInstance()
    }

    fun getUserRef(userId: String): DocumentReference {
        val userRef = db.collection("users").document(userId)
        return userRef
    }

    suspend fun getRestaurantIdList(
        userRef: DocumentReference,
        collection: String
    ): MutableList<String> {
        val collRef = userRef.collection(collection)
        val querySnapshot = withContext(Dispatchers.IO) {
            collRef.get().await()
        }

        val restaurantIdList = mutableListOf<String>()
        for (document in querySnapshot.documents) {
            if (document != null) {
                val restaurantId = document.toObject(Id::class.java)
                restaurantIdList.add(restaurantId?.id!!)
            }
        }
        return restaurantIdList
    }

    suspend fun addRestaurant(
        userRef: DocumentReference,
        collection: String,
        restaurantName: String,
        restaurantId: Id
    ) {
        val docRef = userRef.collection(collection).document(restaurantName)
        withContext(Dispatchers.IO) {
            docRef.set(restaurantId).await()
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
