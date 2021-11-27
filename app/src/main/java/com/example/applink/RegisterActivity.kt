package com.example.applink

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils

import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.widget.AppCompatButton
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser


class RegisterActivity : AppCompatActivity() {



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)




        val btn1 = findViewById<Button>(R.id.btnRegLog)
        btn1.setOnClickListener {
            onBackPressed()
        }

        val btnRegi = findViewById<AppCompatButton>(R.id.btn_Register)
        val email = findViewById<EditText>(R.id.EdtEmail2)
        val password = findViewById<EditText>(R.id.EdtPass2)
        val conpass = findViewById<EditText>(R.id.EdtConfirmPass2)
        btnRegi.setOnClickListener {
            if (TextUtils.isEmpty(email.text.toString().trim())) {
                Toast.makeText(this@RegisterActivity,"Please enter email",Toast.LENGTH_SHORT).show()
            }
            else if (TextUtils.isEmpty(password.text.toString().trim())) {
                Toast.makeText(this@RegisterActivity,"Please enter password",Toast.LENGTH_SHORT).show()
            }
            else if (TextUtils.isEmpty(conpass.text.toString().trim())) {
                Toast.makeText(this@RegisterActivity,"Please re-enter password",Toast.LENGTH_SHORT).show()
            }
            else if (password.text.toString().trim() != conpass.text.toString().trim()) {
                Toast.makeText(this@RegisterActivity,"Passwords do not match",Toast.LENGTH_SHORT).show()
            }
            else {
                FirebaseAuth.getInstance().createUserWithEmailAndPassword(email.text.toString().trim(), password.text.toString().trim())
                    .addOnCompleteListener(
                        OnCompleteListener<AuthResult> { task ->
                            if (task.isSuccessful){
                                val firebaseUser : FirebaseUser = task.result!!.user!!
                                Toast.makeText(this@RegisterActivity,"You are registered successfully.",Toast.LENGTH_SHORT).show()
                                val intent =
                                    Intent(this@RegisterActivity,SetupActivity::class.java)
                                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                intent.putExtra("email",email.text.toString())
                                startActivity(intent)
                                finish()
                            }else{
                                Toast.makeText(this@RegisterActivity,task.exception!!.message.toString(),Toast.LENGTH_SHORT).show()

                            }
                        }
                    )}


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
        val mainIntent = Intent(this@RegisterActivity, MainActivity::class.java)
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        startActivity(mainIntent)
        finish()
    }


    override fun onBackPressed() {
        super.onBackPressed()
        overridePendingTransition(R.anim.slide_from_left,R.anim.slide_to_right)
    }
}