package com.example.applink

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth

class ResetPasswordActivity : AppCompatActivity() {
    private var sendPasswordReset: Button? = null
    private var emailPasswordReset: EditText? = null
    private var mToolbar: Toolbar? = null
    private var mAuth: FirebaseAuth? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reset_password)
        mAuth = FirebaseAuth.getInstance()

        mToolbar = findViewById(R.id.forgot_pass_toolbar)
        setSupportActionBar(mToolbar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.setDisplayShowHomeEnabled(true)
        supportActionBar!!.title = "Reset Password"

        sendPasswordReset=findViewById(R.id.send_password_reset)
        emailPasswordReset=findViewById(R.id.Edt_email_reset)

        sendPasswordReset!!.setOnClickListener{
            val userEmail = emailPasswordReset!!.text.toString()
            if (TextUtils.isEmpty(userEmail)) {
                Toast.makeText(this, "Please write your email...", Toast.LENGTH_SHORT).show()

            }
            else{
                mAuth!!.sendPasswordResetEmail(userEmail).addOnCompleteListener(object : OnCompleteListener<Void> {
                    override fun onComplete(p0: Task<Void>) {
                        if (p0.isSuccessful){
                            Toast.makeText(this@ResetPasswordActivity,"Please check your email",Toast.LENGTH_SHORT).show()
                            val newIntent = Intent(this@ResetPasswordActivity,LoginActivity::class.java)
                            startActivity(newIntent)
                        }
                        else{
                            val message: String = p0.exception!!.message.toString()
                            Toast.makeText(
                                this@ResetPasswordActivity,
                                "Error Occurred: $message",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }

                })
            }
        }
    }
}