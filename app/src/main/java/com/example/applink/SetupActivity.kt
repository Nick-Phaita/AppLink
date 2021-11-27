package com.example.applink

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.google.android.gms.tasks.OnSuccessListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.UploadTask
import com.squareup.picasso.MemoryPolicy
import com.squareup.picasso.NetworkPolicy
import com.squareup.picasso.Picasso
import com.theartofdev.edmodo.cropper.CropImage
import com.theartofdev.edmodo.cropper.CropImageView
import de.hdodenhof.circleimageview.CircleImageView

class SetupActivity : AppCompatActivity() {
    private var userName: EditText? = null
    private var gender: EditText? = null
    private var fullName: EditText? = null
    private var description: EditText? = null
    private var email: EditText? = null
    private var profileImage: CircleImageView? = null
    private var saveInformationbuttion: Button? = null
    private var mAuth: FirebaseAuth? = null
    private var usersRef: DatabaseReference? = null
    var currentUserID: String? = null
    private var userProfileImageRef: StorageReference? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_setup)

        mAuth = FirebaseAuth.getInstance()
        currentUserID = mAuth!!.currentUser!!.uid
        usersRef = FirebaseDatabase.getInstance().reference.child("Users").child(currentUserID.toString())
        userProfileImageRef = FirebaseStorage.getInstance().reference.child("Profile Images")

        userName = findViewById(R.id.editT_username)
        fullName = findViewById(R.id.editT_fullname)
        description = findViewById(R.id.editT_description)
        gender = findViewById(R.id.editT_gender)
        email = findViewById(R.id.T_email)
        saveInformationbuttion = findViewById(R.id.btn_save)
        profileImage = findViewById(R.id.profile_pic)
        saveInformationbuttion!!.setOnClickListener { saveAccountSetupInformation() }
        profileImage!!.setOnClickListener {
            val galleryIntent = Intent()
            galleryIntent.action = Intent.ACTION_GET_CONTENT
            galleryIntent.type = "image/*"
            startActivityForResult(galleryIntent, Gallery_Pick)
        }

        usersRef!!.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                if(snapshot.exists()){
                    if (snapshot.hasChild("profileimage")){
                        val image = snapshot.child("profileimage").value.toString()
                        Picasso.with(this@SetupActivity).load(image).memoryPolicy(MemoryPolicy.NO_CACHE).networkPolicy(NetworkPolicy.NO_CACHE).placeholder(R.drawable.img) to (profileImage)
                    }
                    else{
                        Toast.makeText(this@SetupActivity,"Please select profile pic",Toast.LENGTH_SHORT).show()
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })
    }
    private fun saveAccountSetupInformation() {
        val username = userName!!.text.toString()
        val fullname = fullName!!.text.toString()
        val descrip = description!!.text.toString()
        val gender = gender!!.text.toString()
        val email = email!!.text.toString()

        if (TextUtils.isEmpty(username)) {
            Toast.makeText(this, "Please write your username...", Toast.LENGTH_SHORT).show()
            return
        }
        if (TextUtils.isEmpty(fullname)) {
            Toast.makeText(this, "Please write your full name...", Toast.LENGTH_SHORT).show()
            return
        }
        if (TextUtils.isEmpty(gender)) {
            Toast.makeText(this, "Please write your gender...", Toast.LENGTH_SHORT).show()
            return
        }
        if (TextUtils.isEmpty(email)) {
            Toast.makeText(this, "Please write your email...", Toast.LENGTH_SHORT).show()
            return
        }
        else {

            val userMap : HashMap<String,String> = HashMap()
            userMap["Username"] = username
            userMap["Full name"] = fullname
            userMap["Gender"] = gender
            userMap["Description"] = descrip
            userMap["Email"] = email
            usersRef!!.updateChildren(userMap as Map<String, Any>)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(this, "Saved Successfully", Toast.LENGTH_SHORT).show()
                        sendUserToMainActivity()
                    } else {
                        val message: String = task.exception!!.message.toString()
                        Toast.makeText(
                            this@SetupActivity, "Error Occured: $message", Toast.LENGTH_SHORT
                        ).show()
                    }
                }

        }


    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == Gallery_Pick && resultCode == RESULT_OK && data != null){
            val imageUrl = data.data
            CropImage.activity(imageUrl)
                .setGuidelines(CropImageView.Guidelines.ON)
                .setAspectRatio(1,1)
                .start(this)

        }
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE){
            val result: CropImage.ActivityResult =CropImage.getActivityResult(data)
            if (resultCode == RESULT_OK){
                val resultUri: Uri? = result.uri
                val filePath: StorageReference = userProfileImageRef!!.child(currentUserID+".jpg")
                filePath.putFile(resultUri!!).addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(
                            this@SetupActivity,
                            "Profile Image stored successfully to Firebase storage...",
                            Toast.LENGTH_SHORT
                        ).show()
                        val downloadUrl: String = task.result!!.storage.downloadUrl.toString()
                        usersRef!!.child("profileimage").setValue(downloadUrl)
                            .addOnCompleteListener { task ->

                                    if (task.isSuccessful) {
                                        val selfIntent =
                                            Intent(this@SetupActivity, SetupActivity::class.java)
                                        selfIntent.flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
                                        startActivity(selfIntent)
                                        Toast.makeText(
                                            this@SetupActivity,
                                            "Profile Image stored to Firebase Database Successfully...",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    } else {
                                        val message: String = task.exception!!.message.toString()
                                        Toast.makeText(
                                            this@SetupActivity,
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
    private fun sendUserToMainActivity() {
        val mainIntent = Intent(this@SetupActivity, MainActivity::class.java)
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        startActivity(mainIntent)
        finish()
    }
    companion object {
        const val Gallery_Pick = 1
    }

}