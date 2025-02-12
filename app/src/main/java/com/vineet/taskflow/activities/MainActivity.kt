package com.vineet.taskflow.activities

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.core.view.GravityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.vineet.taskflow.R
import com.vineet.taskflow.adapters.BoardItemsAdapter
import com.vineet.taskflow.databinding.ActivityMainBinding
import com.vineet.taskflow.firebase.FirestoreClass
import com.vineet.taskflow.models.Board
import com.vineet.taskflow.models.User
import com.vineet.taskflow.utils.Constants

class MainActivity : BaseActivity(), NavigationView.OnNavigationItemSelectedListener {
    private lateinit var binding: ActivityMainBinding
    private lateinit var mUsername: String

    private lateinit var rvBoardsList: RecyclerView
    private lateinit var tvNoBoardsAvailable: TextView

    companion object {
        const val MY_PROFILE_ACTIVITY_REQUEST_CODE: Int = 11
        const val CREATE_BOARD_REQUESR_CODE : Int = 12
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        rvBoardsList = binding.appBarLayout.mainContent.rvBoardsList
        tvNoBoardsAvailable = binding.appBarLayout.mainContent.tvNoBoardsAvailable


        setUpActionBar()
        binding.navView.setNavigationItemSelectedListener(this)

        FirestoreClass().loadUserData(this, true)


        binding.appBarLayout.btnAdd.setOnClickListener {
            val intent = Intent(this, CreateBoardActivity::class.java)
            intent.putExtra(Constants.NAME, mUsername)
            startActivityForResult(intent, CREATE_BOARD_REQUESR_CODE)
        }

    }

    fun populateBoardsList(boardList: ArrayList<Board>) {
        hideProgressDialog()

        if (boardList.size > 0) {
            rvBoardsList.visibility = View.VISIBLE
            tvNoBoardsAvailable.visibility = View.GONE

            rvBoardsList.layoutManager = LinearLayoutManager(this)
            rvBoardsList.setHasFixedSize(true)
            val adapter = BoardItemsAdapter(this, boardList)
            rvBoardsList.adapter = adapter
        } else {
            rvBoardsList.visibility = View.GONE
            tvNoBoardsAvailable.visibility = View.VISIBLE
        }
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
                startActivityForResult(
                    Intent(this, MyProfileActivity::class.java),
                    MY_PROFILE_ACTIVITY_REQUEST_CODE
                )
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == MY_PROFILE_ACTIVITY_REQUEST_CODE) {
                FirestoreClass().loadUserData(this)
            }
            if(requestCode == CREATE_BOARD_REQUESR_CODE){
                FirestoreClass().getBoardsList(this)
            }
        }
    }

    fun updateNavigationUserDetails(loggedInUser: User, readBoardsList: Boolean) {
        mUsername = loggedInUser.name
        Glide
            .with(this)
            .load(loggedInUser.image)
            .centerCrop()
            .placeholder(R.drawable.ic_user_place_holder)
            .into(binding.navView.getHeaderView(0).findViewById(R.id.nav_user_image))

        binding.navView.getHeaderView(0).findViewById<TextView>(R.id.tv_username).text =
            loggedInUser.name

        if (readBoardsList) {
            showProgressDialog(resources.getString(R.string.please_wait))
            FirestoreClass().getBoardsList(this)
        }
    }
}