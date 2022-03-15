package com.example.projemanag.activities


import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts


import androidx.core.view.GravityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide

import com.example.projemanag.R
import com.example.projemanag.adapters.BoardItemsAdapter
import com.example.projemanag.firebase.FirestoreClass
import com.example.projemanag.models.User
import com.example.projemanag.databinding.ActivityMainBinding
import com.example.projemanag.databinding.NavHeaderMainBinding
import com.example.projemanag.models.Board
import com.example.projemanag.utils.Constants

import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.iid.FirebaseInstanceIdReceiver
import com.google.firebase.installations.FirebaseInstallations
import com.google.firebase.messaging.FirebaseMessaging

class MainActivity : BaseActivity(), NavigationView.OnNavigationItemSelectedListener {
    private var binding: ActivityMainBinding? = null
    private lateinit var myProfileIntentLauncher: ActivityResultLauncher<Intent>
    private lateinit var myCreateBoardActivityIntentLauncher: ActivityResultLauncher<Intent>
    private lateinit var mUserName: String

    private lateinit var mSharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding?.root)

        setupActionBar()
        binding?.navView?.setNavigationItemSelectedListener(this)

        mSharedPreferences = this.getSharedPreferences(Constants.PROJEMANAG_PREFERENCES, Context.MODE_PRIVATE)

        val tokenUpdated = mSharedPreferences.getBoolean(Constants.FCM_TOKEN_UPDATED, false)

        if (tokenUpdated){
            showProgressDialog(getString(R.string.please_wait))
            FirestoreClass().loadUserData(this, true)
        }else{
            FirebaseMessaging.getInstance().token.addOnSuccessListener(this@MainActivity) {
                token -> updateFCMToken(token)
            }
        }

        FirestoreClass().loadUserData(this,true)
        onActivityResult()

        binding?.mainAppBarLayout?.fabCreateBoard?.setOnClickListener {
            val intent = Intent(this,CreateBoardActivity::class.java)
            intent.putExtra(Constants.NAME,mUserName)
            myCreateBoardActivityIntentLauncher.launch(intent)
        }

    }

    fun populateBoardsListToUI(boardsList: ArrayList<Board>){
        hideProgressDialog()

        if (boardsList.size > 0){
            binding?.mainAppBarLayout?.mainContent?.rvBoardsList?.visibility = View.VISIBLE
            binding?.mainAppBarLayout?.mainContent?.tvNoBoardsAvailable?.visibility = View.GONE

            binding?.mainAppBarLayout?.mainContent?.rvBoardsList?.layoutManager = LinearLayoutManager(this)
            binding?.mainAppBarLayout?.mainContent?.rvBoardsList?.setHasFixedSize(true)

            val adapter = BoardItemsAdapter(this, boardsList)
            binding?.mainAppBarLayout?.mainContent?.rvBoardsList?.adapter = adapter

            adapter.setOnClickListener(object: BoardItemsAdapter.OnClickListener{
                override fun onClick(position: Int, model: Board) {
                    val intent = Intent(this@MainActivity,TaskListActivity::class.java)
                    intent.putExtra(Constants.DOCUMENT_ID, model.documentId)
                    startActivity(intent)
                }
            })
        }else{
            binding?.mainAppBarLayout?.mainContent?.rvBoardsList?.visibility = View.GONE
            binding?.mainAppBarLayout?.mainContent?.tvNoBoardsAvailable?.visibility = View.VISIBLE
        }
    }

    private fun setupActionBar(){
        setSupportActionBar(binding?.mainAppBarLayout?.toolbarMainActivity)
        binding?.mainAppBarLayout?.toolbarMainActivity?.setNavigationIcon(R.drawable.ic_action_navigation_menu)
        binding?.mainAppBarLayout?.toolbarMainActivity?.setNavigationOnClickListener {
            toggleDrawer()
        }
    }


    private fun toggleDrawer(){
        if (binding?.drawerLayout!!.isDrawerOpen(GravityCompat.START)){
            binding?.drawerLayout!!.closeDrawer(GravityCompat.START)
        }else{
            binding?.drawerLayout!!.openDrawer(GravityCompat.START)
        }
    }

    override fun onBackPressed() {
        if (binding?.drawerLayout!!.isDrawerOpen(GravityCompat.START)){
            binding?.drawerLayout!!.closeDrawer(GravityCompat.START)
        }else{
            doubleBackToExit()
        }
    }

    fun updateNavigationUserDetails(user: User, readBoardsList: Boolean){
        hideProgressDialog()
        mUserName = user.name

        val headerView = binding?.navView?.getHeaderView(0)
        val navHeaderMainBinding: NavHeaderMainBinding = NavHeaderMainBinding.bind(headerView!!)

        Glide
            .with(this)
            .load(user.image)
            .centerCrop()
            .placeholder(R.mipmap.ic_user_place_holder)
            .into(navHeaderMainBinding.ivProfileUserImage)
        navHeaderMainBinding.tvUsername.text = user.name
        if (readBoardsList){
            showProgressDialog(getString(R.string.please_wait))
            FirestoreClass().getBoardsList(this)
        }
    }

    private fun onActivityResult(){
        myProfileIntentLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult())
        { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                FirestoreClass().loadUserData(this)
            }else{
                Log.e("Cancelled","Cancelled")
            }
        }
        myCreateBoardActivityIntentLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult())
        { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                FirestoreClass().getBoardsList(this)
            }else{
                Log.e("Cancelled","Cancelled")
            }
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.nav_my_profile -> {
                val myProfileIntent = Intent(this,MyProfileActivity::class.java
                    )
                myProfileIntentLauncher.launch(myProfileIntent)
            }
            R.id.nav_sign_out -> {
                FirebaseAuth.getInstance().signOut()
                mSharedPreferences.edit().clear().apply()
                val intent = Intent(this,IntroActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
                finish()
            }
        }
        binding?.drawerLayout!!.closeDrawer(GravityCompat.START)
        return true
    }

    fun tokenUpdateSuccess(){
        hideProgressDialog()
        val editor : SharedPreferences.Editor = mSharedPreferences.edit()
        editor.putBoolean(Constants.FCM_TOKEN_UPDATED, true)
        editor.apply()
        showProgressDialog(getString(R.string.please_wait))
        FirestoreClass().loadUserData(this, true)
    }

    private fun updateFCMToken(token: String){
        val userHashMap = HashMap<String,Any>()
        userHashMap[Constants.FCM_TOKEN] = token
        showProgressDialog(getString(R.string.please_wait))
        FirestoreClass().updateUserProfileData(this,userHashMap)
    }

    override fun onDestroy() {
        super.onDestroy()
        binding = null
    }
}