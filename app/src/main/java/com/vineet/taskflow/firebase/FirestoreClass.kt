package com.vineet.taskflow.firebase

import android.app.Activity
import android.util.Log
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.vineet.taskflow.activities.CreateBoardActivity
import com.vineet.taskflow.activities.LoginActivity
import com.vineet.taskflow.activities.MainActivity
import com.vineet.taskflow.activities.MyProfileActivity
import com.vineet.taskflow.activities.RegisterActivity
import com.vineet.taskflow.models.Board
import com.vineet.taskflow.models.User
import com.vineet.taskflow.utils.Constants

class FirestoreClass {

    private val mFireStore = FirebaseFirestore.getInstance()

    fun registerUser(activity: RegisterActivity, userInfo: User) {
        mFireStore.collection(Constants.USERS)
            .document(getCurrentUserId())
            .set(userInfo, SetOptions.merge())
            .addOnSuccessListener {
                activity.userRegisteredSuccess()
            }.addOnFailureListener { e ->
                Log.i(activity.javaClass.simpleName, "registerUser: " + e)

            }
    }

    fun createBoard(activity: CreateBoardActivity, board: Board) {
        mFireStore.collection(Constants.BOARDS)
            .document()
            .set(board, SetOptions.merge())
            .addOnSuccessListener {
                Toast.makeText(activity, "Board created successsfully!", Toast.LENGTH_SHORT).show()
                activity.boardCreatedSuccessfully()
            }.addOnFailureListener { e ->
                Log.i(activity.javaClass.simpleName, "createBoard: " + e)
                activity.hideProgressDialog()
            }
    }

    fun updateUserProfileData(activity: MyProfileActivity, userHashMap: Map<String, Any>) {
        mFireStore.collection(Constants.USERS)
            .document(getCurrentUserId())
            .update(userHashMap)
            .addOnSuccessListener {
                Toast.makeText(activity, "Profile Updated Successfully!", Toast.LENGTH_SHORT).show()
                activity.profileUpdateSuccess()
            }
            .addOnFailureListener { e ->
                Log.i(activity.javaClass.simpleName, "updateUserProfileData: " + e)
                activity.hideProgressDialog()
                Toast.makeText(activity, "Error while updating the profile!", Toast.LENGTH_SHORT)
                    .show()
            }
    }

    fun loadUserData(activity: Activity) {
        mFireStore.collection(Constants.USERS)
            .document(getCurrentUserId())
            .get()
            .addOnSuccessListener { document ->
                val loggedInUser = document.toObject(User::class.java)!!
                when (activity) {
                    is LoginActivity -> {
                        activity.loginSuccess(loggedInUser)
                    }

                    is MainActivity -> {
                        activity.updateNavigationUserDetails(loggedInUser)
                    }

                    is MyProfileActivity -> {
                        activity.setUserDataInUI(loggedInUser)
                    }
                }
            }
    }

    fun getCurrentUserId(): String {
        val currentUser = FirebaseAuth.getInstance().currentUser
        var currentUserId = ""
        if (currentUser != null) {
            currentUserId = currentUser.uid
        }
        return currentUserId
    }
}