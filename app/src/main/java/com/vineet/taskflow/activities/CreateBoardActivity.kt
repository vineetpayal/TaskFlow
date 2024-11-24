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
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.vineet.taskflow.R
import com.vineet.taskflow.databinding.ActivityCreateBoardBinding
import com.vineet.taskflow.firebase.FirestoreClass
import com.vineet.taskflow.models.Board
import com.vineet.taskflow.utils.Constants
import io.appwrite.Client
import io.appwrite.ID
import io.appwrite.Permission
import io.appwrite.models.InputFile
import io.appwrite.services.Storage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream

class CreateBoardActivity : BaseActivity() {

    private lateinit var binding: ActivityCreateBoardBinding
    private  var mSelectedImageUri: Uri? = null
    private lateinit var mSelectImageFile: File
    private var selectImageUrl: String = ""

    private lateinit var mUsername: String

    companion object {
        const val IMAGE_REQUEST_CODE = 1
        const val READ_IMAGE_PERMISSION_CODE = 2
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCreateBoardBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setUpActionBar()

        if (intent.hasExtra(Constants.NAME)) {
            mUsername = intent.getStringExtra(Constants.NAME)!!
        }

        binding.ivBoardImage.setOnClickListener {

            if (ContextCompat.checkSelfPermission(
                    this, Manifest.permission.READ_MEDIA_IMAGES
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                showImageChooser()
            } else {
                requestPermissions(
                    arrayOf(Manifest.permission.READ_MEDIA_IMAGES), READ_IMAGE_PERMISSION_CODE
                )
            }
        }

        binding.btnCreate.setOnClickListener {
            if (mSelectedImageUri != null) {
                lifecycleScope.launch {
                    withContext(IO) {
                        try {
                            uploadBoardImage()
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                }
            } else {
                showProgressDialog(resources.getString(R.string.please_wait))
                createBoard()
            }
        }
    }

    private fun createBoard() {
        val assignedUsersArrayList: ArrayList<String> = ArrayList()
        assignedUsersArrayList.add(getCurrentUserID())

        var board = Board(
            binding.etBoardName.text.toString(),
            selectImageUrl,
            mUsername,
            assignedUsersArrayList
        )

        FirestoreClass().createBoard(this, board)
    }

    private suspend fun uploadBoardImage() {
        withContext(Dispatchers.Main) {
            showProgressDialog(resources.getString(R.string.please_wait))
        }
        if (mSelectedImageUri != null) {

            val client = Client(this)
                .setEndpoint("https://cloud.appwrite.io/v1")
                .setProject(Constants.PROJECT_ID);

            val storage = Storage(client)

            //upload file
            val imageId = ID.unique()
            val file = storage.createFile(
                Constants.BOARD_IMAGE_BUCKET_ID,
                fileId = imageId,
                file = InputFile.fromFile(mSelectImageFile),
            )

            //get file url
            val fileUrl = storage.getFile(
                bucketId = Constants.BOARD_IMAGE_BUCKET_ID,
                fileId = imageId
            )

            //build url from metadata
            selectImageUrl = Constants.buildUrl(this, fileUrl)
            Log.d("this", "uploadBoardImage: " + selectImageUrl)

            withContext(Dispatchers.Main) {
                Toast.makeText(
                    this@CreateBoardActivity,
                    "Board created successfully!",
                    Toast.LENGTH_SHORT
                )
                    .show()
            }
            createBoard()
        }
    }

    fun boardCreatedSuccessfully() {
        hideProgressDialog()
        setResult(Activity.RESULT_OK)
        finish()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == IMAGE_REQUEST_CODE && resultCode == Activity.RESULT_OK && data!!.data != null) {
            mSelectedImageUri = data.data!!
            val selectedBitmap: Bitmap = getBitmap(contentResolver, mSelectedImageUri)

            mSelectImageFile = File(
                getExternalFilesDir(Environment.DIRECTORY_PICTURES),
                System.currentTimeMillis().toString() + "_selectedImg.jpg"
            )

            Constants.convertBitmapToFile(mSelectImageFile, selectedBitmap)

            try {
                Glide
                    .with(this)
                    .load(mSelectedImageUri)
                    .centerCrop()
                    .placeholder(R.drawable.ic_board_place_holder)
                    .into(binding.ivBoardImage)
            } catch (e: Exception) {
                e.printStackTrace()
            }

        }
    }

    private fun showImageChooser() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent, IMAGE_REQUEST_CODE)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == READ_IMAGE_PERMISSION_CODE && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            showImageChooser()
        } else {
            Toast.makeText(this, "Permission is needed for creating a board!", Toast.LENGTH_SHORT)
                .show()
        }
    }

    private fun setUpActionBar() {
        setSupportActionBar(binding.toolbarCreateBoardActivity)

        val actionBar = supportActionBar

        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeAsUpIndicator(R.drawable.ic_back_arrow)
            actionBar.title = resources.getString(R.string.create_board_title)
        }
    }
}