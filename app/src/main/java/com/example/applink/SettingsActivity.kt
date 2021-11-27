package com.example.applink

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.squareup.picasso.Picasso
import com.theartofdev.edmodo.cropper.CropImage
import com.theartofdev.edmodo.cropper.CropImageView
import de.hdodenhof.circleimageview.CircleImageView

class SettingsActivity : AppCompatActivity() {
    private var mToolbar: Toolbar? = null
    private var userName: EditText? = null
    private var gender: EditText? = null
    private var fullName: EditText? = null
    private var description: EditText? = null
    private var userProfileImage: CircleImageView? = null
    private var saveSettingsbtn: Button? = null
    private var settingsUserReference: DatabaseReference? = null
    private var mAuth: FirebaseAuth? = null
    var currentUserId: String? = null
    private var userProfileImageRef: StorageReference? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        mAuth = FirebaseAuth.getInstance()
        currentUserId = mAuth!!.currentUser!!.uid
        userProfileImageRef = FirebaseStorage.getInstance().reference.child("Profile Images")
        settingsUserReference = FirebaseDatabase.getInstance().reference.child("Users").child(currentUserId.toString())

        mToolbar = findViewById(R.id.settings_toolbar)
        setSupportActionBar(mToolbar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.setDisplayShowHomeEnabled(true)
        supportActionBar!!.title = "Account Settings"

        userName = findViewById(R.id.settings_username)
        gender = findViewById(R.id.settings_gender)
        fullName = findViewById(R.id.settings_fullname)
        description = findViewById(R.id.settings_description)
        userProfileImage = findViewById(R.id.settings_profile_img)
        saveSettingsbtn = findViewById(R.id.update_account_btn)

        userProfileImage!!.setOnClickListener {
            val galleryIntent = Intent()
            galleryIntent.action = Intent.ACTION_GET_CONTENT
            galleryIntent.type = "image/*"
            startActivityForResult(galleryIntent, Gallery_Pick)
        }

        settingsUserReference!!.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()){
                    val myProfileImage = snapshot.child("profileimage").value.toString()
                    val myusername = snapshot.child("Username").value.toString()
                    val myfullname = snapshot.child("Full name").value.toString()
                    val mygender = snapshot.child("Gender").value.toString()
                    val mydescription = snapshot.child("Description").value.toString()

                    Picasso.with(this@SettingsActivity).load(myProfileImage).placeholder(R.drawable.img) to myProfileImage
                    userName!!.setText(myusername)
                    fullName!!.setText(myfullname)
                    description!!.setText(mydescription)
                    gender!!.setText(mygender)


                }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })

        saveSettingsbtn!!.setOnClickListener{validateAccountInfo()}
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == SetupActivity.Gallery_Pick && resultCode == RESULT_OK && data != null){
            val imageUrl = data.data
            CropImage.activity(imageUrl)
                .setGuidelines(CropImageView.Guidelines.ON)
                .setAspectRatio(1,1)
                .start(this)

        }
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE){
            val result: CropImage.ActivityResult = CropImage.getActivityResult(data)
            if (resultCode == RESULT_OK){
                val resultUri: Uri? = result.uri
                val filePath: StorageReference = userProfileImageRef!!.child(currentUserId+".jpg")
                filePath.putFile(resultUri!!).addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(
                            this@SettingsActivity,
                            "Profile Image stored successfully to Firebase storage...",
                            Toast.LENGTH_SHORT
                        ).show()
                        val downloadUrl: String = task.result!!.storage.downloadUrl.toString()
                        settingsUserReference!!.child("profileimage").setValue(downloadUrl)
                            .addOnCompleteListener { task ->

                                if (task.isSuccessful) {
                                    val selfIntent =
                                        Intent(this@SettingsActivity, SettingsActivity::class.java)
                                    selfIntent.flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
                                    startActivity(selfIntent)
                                    Toast.makeText(
                                        this@SettingsActivity,
                                        "Profile Image stored to Firebase Database Successfully...",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                } else {
                                    val message: String = task.exception!!.message.toString()
                                    Toast.makeText(
                                        this@SettingsActivity,
                                        "Error Occurred: $message",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }
                    }
                }
            }
        }
        else{
            Toast.makeText(this, "Error Occured: Image can not be cropped. Try Again.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun validateAccountInfo() {
        val usernamevalid = userName!!.text.toString()
        val fullnamevalid = fullName!!.text.toString()
        val gendervalid = gender!!.text.toString()
        val descripvalid = description!!.text.toString()

        if (TextUtils.isEmpty(usernamevalid)) {
            Toast.makeText(this, "Please write your username...", Toast.LENGTH_SHORT).show()

        }
        if (TextUtils.isEmpty(fullnamevalid)) {
            Toast.makeText(this, "Please write your full name...", Toast.LENGTH_SHORT).show()

        }
        if (TextUtils.isEmpty(gendervalid)) {
            Toast.makeText(this, "Please write your gender...", Toast.LENGTH_SHORT).show()

        }
        else{
            updateAccountInfo(usernamevalid,fullnamevalid,gendervalid,descripvalid)
        }
    return
    }

    private fun updateAccountInfo(usernamevalid: String, fullnamevalid: String, gendervalid: String, descripvalid: String) {
        val updateMap: HashMap<String,String> = HashMap()
        updateMap["Username"] = usernamevalid
        updateMap["Full name"] = fullnamevalid
        updateMap["Gender"] = gendervalid
        updateMap["Description"] = descripvalid
        settingsUserReference!!.updateChildren(updateMap as Map<String, Any>)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    sendUserToMainActivity()
                    Toast.makeText(this, "Account settings saved successfully", Toast.LENGTH_SHORT)
                        .show()

                } else {
                    Toast.makeText(this, "Error Occurred", Toast.LENGTH_SHORT).show()
                }
            }

    }
    private fun sendUserToMainActivity() {
        val mainIntent = Intent(this@SettingsActivity, MainActivity::class.java)
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        startActivity(mainIntent)
        finish()
    }
    companion object {
        const val Gallery_Pick = 1
    }


}



