package com.example.projemanag.activities

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.WindowInsets
import android.view.WindowManager
import android.widget.Toast
import com.example.projemanag.R
import com.example.projemanag.models.User
import com.example.projemanag.databinding.ActivitySignInBinding
import com.google.firebase.auth.FirebaseAuth

class SignInActivity : BaseActivity() {
    private var binding: ActivitySignInBinding? = null
    private lateinit var auth: FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignInBinding.inflate(layoutInflater)
        setContentView(binding?.root)

        auth = FirebaseAuth.getInstance()

        setupActionBar()
        @Suppress("DEPRECATION")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R){
            window.insetsController?.hide(WindowInsets.Type.statusBars())
        }else{
            window.setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN
            )
        }
        binding?.btnSignIn?.setOnClickListener {
            signInRegisteredUser()
        }
    }

    fun signInSuccess(user:User){
        hideProgressDialog()
        startActivity(Intent(this,MainActivity::class.java))
        finish()
    }

    private fun setupActionBar(){
        setSupportActionBar(binding?.toolbarSignInActivity)

        val actionBar = supportActionBar
        if (actionBar != null){
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeAsUpIndicator(R.drawable.ic_black_color_back_24dp)
        }
        binding?.toolbarSignInActivity?.setNavigationOnClickListener {
            onBackPressed()
        }
    }

    private fun signInRegisteredUser(){
        val email: String = binding?.etEmail?.text.toString().trim { it <= ' ' }
        val password: String = binding?.etPassword?.text.toString().trim { it <= ' ' }

        if (validateForm(email,password)){
            showProgressDialog(getString(R.string.please_wait))
            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    hideProgressDialog()
                    if (task.isSuccessful) {
                        // Sign in success, update UI with the signed-in user's information
                        Log.d("Sign In", "createUserWithEmail:success")
                        val user = auth.currentUser
                        startActivity(Intent(this,MainActivity::class.java))
                    } else {
                        // If sign in fails, display a message to the user.
                        Log.w("Sign In", "createUserWithEmail:failure", task.exception)
                        Toast.makeText(baseContext, "Authentication failed.",
                            Toast.LENGTH_SHORT).show()

                    }
                }
        }
    }

    private fun validateForm(email: String,password:String):Boolean{
        return when{
            TextUtils.isEmpty(email) -> {
                showErrorSnackBar("Please enter an email address")
                false
            }
            TextUtils.isEmpty(password) -> {
                showErrorSnackBar("Please enter a password")
                false
            }else -> {
                true
            }

        }
    }
    override fun onDestroy() {
        super.onDestroy()
        binding = null
    }
}