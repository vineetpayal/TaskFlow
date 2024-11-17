package com.vineet.taskflow.activities

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import androidx.appcompat.widget.Toolbar
import com.google.firebase.auth.FirebaseAuth
import com.vineet.taskflow.R
import com.vineet.taskflow.databinding.ActivityLoginBinding
import com.vineet.taskflow.firebase.FirestoreClass
import com.vineet.taskflow.models.User

class LoginActivity : BaseActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()

        binding.btnLogin.setOnClickListener {
            loginUser()
        }

        setupActionBar(findViewById(R.id.toolbar_login_activity))
    }

    private fun loginUser() {
        val email = binding.etEmailLogin.text.toString().trim { it <= ' ' }
        val password = binding.etPasswordLogin.text.toString().trim { it <= ' ' }

        if (validateForm(email, password)) {
            showProgressDialog(resources.getString(R.string.please_wait))

            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    hideProgressDialog()
                    if (task.isSuccessful) {
                        FirestoreClass().loadUserData(this)
                    } else {
                        showErrorSnackBar(task.exception!!.message.toString())
                    }
                }
        }
    }

    fun loginSuccess(loggedInUser: User) {
        hideProgressDialog()
        startActivity(Intent(this, MainActivity::class.java))
    }

    private fun validateForm(email: String, password: String): Boolean {
        return when {
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
            actionBar.setHomeAsUpIndicator(R.drawable.baseline_arrow_back_24)
        }

    }


}