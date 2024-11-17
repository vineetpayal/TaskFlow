package com.vineet.taskflow.activities

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.widget.TextView
import android.widget.Toast
import androidx.core.view.GravityCompat
import com.bumptech.glide.Glide
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.vineet.taskflow.R
import com.vineet.taskflow.databinding.ActivityMainBinding
import com.vineet.taskflow.firebase.FirestoreClass
import com.vineet.taskflow.models.User

class MainActivity : BaseActivity(), NavigationView.OnNavigationItemSelectedListener {
    private lateinit var binding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setUpActionBar()
        binding.navView.setNavigationItemSelectedListener(this)

        FirestoreClass().loadUserData(this)
    }

    private fun setUpActionBar() {
        setSupportActionBar(binding.appBarLayout.toolbarMainActivity)
        val toolbar = binding.appBarLayout.toolbarMainActivity

        toolbar.setNavigationIcon(R.drawable.ic_menu)
        toolbar.setNavigationOnClickListener {
            toggleDrawer()
        }
    }

    private fun toggleDrawer() {
        if (binding.drawerLayout.isDrawerOpen(GravityCompat.START)) {
            binding.drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            binding.drawerLayout.openDrawer(GravityCompat.START)
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        if (binding.drawerLayout.isDrawerOpen(GravityCompat.START)) {
            binding.drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            doubleBackToExit()
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.nav_my_profile -> {
                startActivity(Intent(this, MyProfileActivity::class.java))
            }

            R.id.nav_sign_out -> {
                Toast.makeText(this, "Sign out", Toast.LENGTH_SHORT).show()
                FirebaseAuth.getInstance().signOut()

                val intent = Intent(this, IntroActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
                finish()
            }
        }
        binding.drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }

    fun updateNavigationUserDetails(loggedInUser: User) {
        Glide
            .with(this)
            .load(loggedInUser.image)
            .centerCrop()
            .placeholder(R.drawable.ic_user_place_holder)
            .into(binding.navView.getHeaderView(0).findViewById(R.id.nav_user_image))

        binding.navView.getHeaderView(0).findViewById<TextView>(R.id.tv_username).text = loggedInUser.name
    }
}