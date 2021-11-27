package com.example.applink

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView

class ProfileActivity : AppCompatActivity() {

    private var userName: TextView? = null
    private var gender: TextView? = null
    private var fullName: TextView? = null
    private var description: TextView? = null
    private var userProfileImage: CircleImageView? = null
    private var profileUserReference: DatabaseReference? = null
    private var mAuth: FirebaseAuth? = null
    var currentUserId: String? = null
    private var mToolbar: Toolbar? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)
        mToolbar = findViewById(R.id.profile_toolbar)
        setSupportActionBar(mToolbar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.setDisplayShowHomeEnabled(true)
        supportActionBar!!.title = "Profile"

        mAuth = FirebaseAuth.getInstance()
        currentUserId = mAuth!!.currentUser!!.uid
        profileUserReference = FirebaseDatabase.getInstance().reference.child("Users").child(currentUserId.toString())

        userName = findViewById(R.id.myProfile_username)
        gender = findViewById(R.id.myProfile_gender)
        fullName = findViewById(R.id.myProfile_fullname)
        description = findViewById(R.id.myProfile_description)
        userProfileImage = findViewById(R.id.myProfile_pic)

        profileUserReference!!.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()){
                    val myProfileImage = snapshot.child("profileimage").value.toString()
                    val myusername = snapshot.child("Username").value.toString()
                    val myfullname = snapshot.child("Full name").value.toString()
                    val mygender = snapshot.child("Gender").value.toString()
                    val mydescription = snapshot.child("Description").value.toString()

                    Picasso.with(this@ProfileActivity).load(myProfileImage).placeholder(R.drawable.img) to myProfileImage
                    userName!!.text = "@$myusername"
                    fullName!!.text = myfullname
                    description!!.text = mydescription
                    gender!!.text = mygender
                }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        })
    }
}