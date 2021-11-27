package com.example.applink

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.AppCompatButton
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser


class LoginActivity : AppCompatActivity() {
    private var mAuth: FirebaseAuth? = null
    private var forgotPassword: TextView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        mAuth = FirebaseAuth.getInstance()

        val btn1 = findViewById<Button>(R.id.btnLogReg)
        btn1.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
            overridePendingTransition(R.anim.slide_from_right, R.anim.slide_to_left)
        }
        forgotPassword = findViewById(R.id.Txt_Reset)

        val btnLogin :AppCompatButton= findViewById(R.id.btnLogin2)
        val email = findViewById<EditText>(R.id.EdtEmail1)
        val password = findViewById<EditText>(R.id.EdtPass1)
        btnLogin.setOnClickListener{
            if (TextUtils.isEmpty(email.text.toString().trim())){
                Toast.makeText(this,"Please enter email",Toast.LENGTH_SHORT).show()
            }else if (TextUtils.isEmpty(password.text.toString().trim())) {
                Toast.makeText(this@LoginActivity,"Please enter password",Toast.LENGTH_SHORT).show()
            }else {mAuth!!.signInWithEmailAndPassword(email.text.toString().trim(), password.text.toString().trim())
                .addOnCompleteListener(
                    OnCompleteListener<AuthResult> { task ->
                        if (task.isSuccessful){
                            val firebaseUser : FirebaseUser = task.result!!.user!!
                            Toast.makeText(this@LoginActivity,"You are logged in successfully.",Toast.LENGTH_SHORT).show()
                            val intent =
                                Intent(this@LoginActivity,MainActivity::class.java)
                            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                            startActivity(intent)
                            finish()
                        }else{
                            Toast.makeText(this@LoginActivity,task.exception!!.message.toString(),Toast.LENGTH_SHORT).show()
                        }
                    }
                )}
        }

        forgotPassword!!.setOnClickListener{
            val newIntent = Intent(this,ResetPasswordActivity::class.java)
            startActivity(newIntent)
        }

    }
    override fun onStart() {
        super.onStart()
        val currentUser: FirebaseUser? = FirebaseAuth.getInstance().currentUser
        if (currentUser != null) {
            sendUserToMainActivity()
        }
    }
    private fun sendUserToMainActivity() {
        val mainIntent = Intent(this@LoginActivity, MainActivity::class.java)
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        startActivity(mainIntent)
        finish()
    }




}