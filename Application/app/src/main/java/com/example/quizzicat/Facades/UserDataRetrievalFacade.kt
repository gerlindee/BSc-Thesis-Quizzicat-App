package com.example.quizzicat.Facades

import com.example.quizzicat.Model.User
import com.example.quizzicat.Utils.UserDataCallBack
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class UserDataRetrievalFacade(private val firebaseFirestore: FirebaseFirestore, private val uid: String) {
    public fun getUserDetails(callback: UserDataCallBack) {
        firebaseFirestore.collection("Users")
            .whereEqualTo("uid", uid)
            .get()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    var user: User? = null
                    for (document in task.result!!) {
                        val userUID = document.get("uid") as String
                        val userCity = document.get("city") as String
                        val userCountry = document.get("country") as String
                        val userDisplayName = document.get("displayName") as String
                        val userPicture = document.get("profileImageURL") as String
                        user = User(userUID, userDisplayName, userPicture, userCountry, userCity)
                    }
                    callback.onCallback(user!!)
                }
            }
    }
}