package com.vineet.taskflow.activities

import android.os.Bundle
import android.text.TextUtils
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.vineet.taskflow.R
import com.vineet.taskflow.databinding.ActivityRegisterBinding
import com.vineet.taskflow.firebase.FirestoreClass
import com.vineet.taskflow.models.User

class RegisterActivity : BaseActivity() {
    private lateinit var binding: ActivityRegisterBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val toolBar: Toolbar = findViewById(R.id.toolbar_register_activity)
        setupActionBar(toolBar)

        binding.btnRegister.setOnClickListener {
            registerUser()
        }
    }

    private fun registerUser() {
        val name: String = binding.etNameRegister.text.toString().trim { it <= ' ' }
        val email: String = binding.etEmailRegister.text.toString().trim { it <= ' ' }
        val password: String = binding.etPasswordRegister.text.toString().trim { it <= ' ' }

        if (validateForm(name, email, password)) {
            showProgressDialog(resources.getString(R.string.please_wait))
            FirebaseAuth.getInstance().createUserWithEmailAndPassword(
                email, password
            ).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val firebaseUser: FirebaseUser = task.result!!.user!!
                    val registeredEmail = firebaseUser.email!!

                    val user = User(firebaseUser.uid,name,registeredEmail)
                    FirestoreClass().registerUser(this,user)
                }else{
                    showErrorSnackBar(task.exception!!.message.toString())
                }
            }
        }

    }

    fun userRegisteredSuccess(){
        hideProgressDialog()
        Toast.makeText(
            this,
            "You have successfully registered",
            Toast.LENGTH_SHORT).show()
        FirebaseAuth.getInstance().signOut()
        finish()
    }

    private fun validateForm(name: String, email: String, password: String): Boolean {
        return when {
            TextUtils.isEmpty(name) -> {
                showErrorSnackBar("Please enter a name")
                false
            }

            TextUtils.isEmpty(email) -> {
                showErrorSnackBar("Please enter an email address")
                false
            }

            TextUtils.isEmpty(password) -> {
                showErrorSnackBar("Please enter a password")
                false
            }

            else -> {
                true
            }
        }

    }

    private fun setupActionBar(toolbar: Toolbar) {
        setSupportActionBar(toolbar)
        val actionBar = supportActionBar

        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeAsUpIndicator(R.drawable.baseline_arrow_back_24);
        }
    }
}