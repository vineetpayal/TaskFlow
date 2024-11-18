package com.vineet.taskflow.activities

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.provider.MediaStore.Images.Media.getBitmap
import android.util.Log
import android.webkit.MimeTypeMap
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.net.toFile
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide

import com.vineet.taskflow.R
import com.vineet.taskflow.databinding.ActivityMyProfileBinding
import com.vineet.taskflow.firebase.FirestoreClass
import com.vineet.taskflow.models.User
import com.vineet.taskflow.utils.Constants
import io.appwrite.Client
import io.appwrite.ID
import io.appwrite.models.InputFile
import io.appwrite.models.UploadProgress
import io.appwrite.services.Storage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.net.URI
import java.util.Timer
import kotlin.math.log

class MyProfileActivity : BaseActivity() {

    companion object {
        private const val READ_STORAGE_PERMISSION_CODE = 1
        private const val PICK_IMAGE_REQUEST_CODE = 2
    }

    private lateinit var binding: ActivityMyProfileBinding
    private lateinit var mUserDetails: User

    private var mSelectImageFileUri: Uri? = null
    private var selectedImageFile: File? = null
    private var profileImageUrl: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMyProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setUpActionBar()
        FirestoreClass().loadUserData(this)

        binding.ivUserImage.setOnClickListener {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES)
                == PackageManager.PERMISSION_GRANTED
            ) {
                showImageChooser()
            } else {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.READ_MEDIA_IMAGES),
                    READ_STORAGE_PERMISSION_CODE
                )
            }
        }

        binding.btnUpdate.setOnClickListener {

            //upload user image
            if (mSelectImageFileUri != null) {
                lifecycleScope.launch {
                    withContext(IO) {
                        try {
                            uploadUserImage();
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }

                    }
                }

            } else {
                showProgressDialog(resources.getString(R.string.please_wait))
                updateUserProfileData()
            }

        }


    }

    //upload image
    private suspend fun uploadUserImage() {

        withContext(Dispatchers.Main) {
            showProgressDialog(resources.getString(R.string.please_wait))
        }
        if (mSelectImageFileUri != null) {

            val client = Client(this)
                .setEndpoint("https://cloud.appwrite.io/v1")
                .setProject(Constants.PROJECT_ID);

            val storage = Storage(client)

            //upload file
            val imageId = ID.unique()
            val file = storage.createFile(
                Constants.USER_PROFILE_BUCKET_ID,
                fileId = imageId,
                file = InputFile.fromFile(selectedImageFile!!),
            )

            //get file url
            val fileUrl = storage.getFile(
                bucketId = Constants.USER_PROFILE_BUCKET_ID,
                fileId = imageId
            )

            //build url from metadata
            profileImageUrl = buildUrl(fileUrl)

            withContext(Dispatchers.Main) {
                Toast.makeText(this@MyProfileActivity, "User Profile Updated", Toast.LENGTH_SHORT)
                    .show()
            }

            updateUserProfileData()

        }
    }

    private fun buildUrl(file: io.appwrite.models.File): String {
        return "https://cloud.appwrite.io/v1/storage/buckets/${Constants.USER_PROFILE_BUCKET_ID}/files/${file.id}/view?project=${Constants.PROJECT_ID}"
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


    //image picker
    private fun showImageChooser() {
        var galleryIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(galleryIntent, PICK_IMAGE_REQUEST_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK
            && requestCode == PICK_IMAGE_REQUEST_CODE
            && data!!.data != null
        ) {
            mSelectImageFileUri = data.data
            val selectedBitmap: Bitmap = getBitmap(contentResolver, mSelectImageFileUri)

            /*We can access getExternalFileDir() without asking any storage permission.*/
            selectedImageFile = File(
                getExternalFilesDir(Environment.DIRECTORY_PICTURES),
                System.currentTimeMillis().toString() + "_selectedImg.jpg"
            )

            convertBitmapToFile(selectedImageFile!!, selectedBitmap)


            try {
                Glide.with(this)
                    .load(mSelectImageFileUri)
                    .centerCrop()
                    .placeholder(R.drawable.ic_user_place_holder)
                    .into(binding.ivUserImage)
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }

    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == READ_STORAGE_PERMISSION_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                showImageChooser()

            }
        } else {
            Toast.makeText(this, "Permissions are required to continue", Toast.LENGTH_SHORT).show()
        }

    }

    private fun updateUserProfileData() {
        val userHashMap = HashMap<String, Any>()

        if (profileImageUrl.isNotEmpty() && profileImageUrl != mUserDetails.image) {
            userHashMap[Constants.IMAGE] = profileImageUrl
        }

        if (binding.etName.text.toString() != mUserDetails.name) {
            userHashMap[Constants.NAME] = binding.etName.text.toString()
        }

        if (binding.etMobile.text.toString() != mUserDetails.mobile.toString()) {
            userHashMap[Constants.MOBILE] = binding.etMobile.text.toString().toLong()
        }

        FirestoreClass().updateUserProfileData(this, userHashMap)
    }

    //setting up toolbar with back button
    private fun setUpActionBar() {
        setSupportActionBar(binding.toolbarMyProfileActivity)
        val actionBar = supportActionBar

        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeAsUpIndicator(R.drawable.ic_back_arrow)
            actionBar.title = resources.getString(R.string.my_profile)
        }
    }

    //fetching user data from firestore
    fun setUserDataInUI(user: User) {

        mUserDetails = user

        Glide.with(this)
            .load(user.image)
            .centerCrop()
            .placeholder(R.drawable.ic_user_place_holder)
            .into(binding.ivUserImage)

        binding.etName.setText(user.name)
        binding.etEmail.setText(user.email)
        if (user.mobile != 0L) {
            binding.etMobile.setText(user.mobile.toString())
        }
    }

    fun profileUpdateSuccess() {
        hideProgressDialog()
        finish()
    }
}