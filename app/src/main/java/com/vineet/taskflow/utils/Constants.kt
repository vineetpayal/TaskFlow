package com.vineet.taskflow.utils

import android.app.Activity
import android.graphics.Bitmap
import com.vineet.taskflow.activities.CreateBoardActivity
import com.vineet.taskflow.activities.MyProfileActivity
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream

object Constants {

    //firestore constants
    const val USERS: String = "users"
    const val BOARDS: String = "boards"

    //appwrite constants
    const val USER_PROFILE_BUCKET_ID: String = "673ab6d200369492a92b"
    const val BOARD_IMAGE_BUCKET_ID: String = "674168450005735e0517"
    const val PROJECT_ID: String = "673ab5b5000e8c6f1dd6"

    const val IMAGE: String = "image"
    const val NAME: String = "name"
    const val MOBILE: String = "mobile"

    fun buildUrl(activity: Activity, file: io.appwrite.models.File): String {
        when (activity) {
            is MyProfileActivity -> {
                return "https://cloud.appwrite.io/v1/storage/buckets/${Constants.USER_PROFILE_BUCKET_ID}/files/${file.id}/view?project=${Constants.PROJECT_ID}"
            }

            is CreateBoardActivity -> {
                return "https://cloud.appwrite.io/v1/storage/buckets/${Constants.BOARD_IMAGE_BUCKET_ID}/files/${file.id}/view?project=${Constants.PROJECT_ID}"
            }
        }
        return ""
    }

    fun convertBitmapToFile(destinationFile: File, bitmap: Bitmap) {
        //create a file to write bitmap data
        destinationFile.createNewFile()   //Convert bitmap to byte array
        val bos = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 50, bos)
        val bitmapData = bos.toByteArray()   //write the bytes in file
        val fos = FileOutputStream(destinationFile)
        fos.write(bitmapData)
        fos.flush()
        fos.close()
    }
}