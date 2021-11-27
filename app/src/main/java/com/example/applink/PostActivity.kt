package com.example.applink

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.HashMap

class PostActivity : AppCompatActivity() {
    private var mToolbar: Toolbar? = null
    private var selectPostImage: ImageButton? = null
    private var updatePostButton: Button? = null
    private var imageUri: Uri? = null
    private var postDescription: EditText? = null
    private var description: String? = null
    private var postsImagesRefrence: StorageReference? = null
    private var saveCurrentDate: String? = null
    private var saveCurrentTime: String? = null
    private var postRandomName: String? = null
    private var downloadUrl: String? = null
    private var usersRef: DatabaseReference? = null
    private var postsRef: DatabaseReference? = null
    private var currentuserid: String? = null
    private var mAuth: FirebaseAuth? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_post)


        postsImagesRefrence = FirebaseStorage.getInstance().reference
        usersRef = FirebaseDatabase.getInstance().reference.child("Users")
        postsRef = FirebaseDatabase.getInstance().reference.child("Posts")
        mAuth = FirebaseAuth.getInstance()
        currentuserid = mAuth!!.currentUser!!.uid
        mToolbar = findViewById<View>(R.id.update_post_page_toolbar) as Toolbar
        setSupportActionBar(mToolbar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.setDisplayShowHomeEnabled(true)
        supportActionBar!!.title = "Update Post"
        selectPostImage = findViewById<View>(R.id.select_post_image) as ImageButton
        updatePostButton = findViewById<View>(R.id.update_post_btn) as Button
        postDescription = findViewById<View>(R.id.post_description) as EditText
        selectPostImage!!.setOnClickListener { openGallery() }
        updatePostButton!!.setOnClickListener { validatePostInfo() }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId
        if (id == android.R.id.home) {
            sendUserToMainActivity()
        }
        return super.onOptionsItemSelected(item)
    }
    private fun sendUserToMainActivity() {
        val mainIntent = Intent(this@PostActivity, MainActivity::class.java)
        startActivity(mainIntent)
    }
    private fun storingImageToFirebaseStorage(){
        val calFordDate = Calendar.getInstance()
        val currentDate = SimpleDateFormat("dd-MMMM-yyyy", Locale.ENGLISH)
        saveCurrentDate = currentDate.format(calFordDate.time)
        val calFordTime = Calendar.getInstance()
        val currentTime = SimpleDateFormat("HH:mm", Locale.ENGLISH)
        saveCurrentTime = currentTime.format(calFordTime.time)
        postRandomName = saveCurrentDate + saveCurrentTime
        val filePath: StorageReference = postsImagesRefrence!!.child("Post Images")
            .child(imageUri!!.lastPathSegment + postRandomName + ".jpg")
        filePath.putFile(imageUri!!).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                downloadUrl = task.result!!.storage.downloadUrl.toString()
                Toast.makeText(
                    this@PostActivity,
                    "image uploaded successfully to Storage...", Toast.LENGTH_SHORT).show()
                savingPostInformationToDatabase()

            } else {
                val message = task.exception!!.message
                Toast.makeText(this@PostActivity, "Error occured: $message", Toast.LENGTH_SHORT)
                    .show()
            }
        }
    }
    private fun validatePostInfo() {
        description = postDescription!!.text.toString()
        if (imageUri == null) {
            Toast.makeText(this, "Please select post image...", Toast.LENGTH_SHORT).show()
        } else if (TextUtils.isEmpty(description)) {
            Toast.makeText(this, "Please say something about your image...", Toast.LENGTH_SHORT)
                .show()
        } else {

            storingImageToFirebaseStorage()
        }
    }
    private fun savingPostInformationToDatabase() {
        usersRef!!.child(currentuserid!!).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    val userName: String = dataSnapshot.child("Username").value.toString()
                    val userProfileImage: String = dataSnapshot.child("profileimage").value.toString()
                    val postsMap: HashMap<String,String> = HashMap()
                    postsMap["uid"] = currentuserid!!
                    postsMap["date"] = saveCurrentDate!!
                    postsMap["time"] = saveCurrentTime!!
                    postsMap["description"] = description!!
                    postsMap["postimage"] = downloadUrl!!
                    postsMap["profileimage"] = userProfileImage
                    postsMap["username"] = userName

                    postsRef!!.child(currentuserid + postRandomName).updateChildren(postsMap as Map<String, Any>)
                        .addOnCompleteListener { p0 ->
                            if (p0.isSuccessful) {
                                sendUserToMainActivity()
                                Toast.makeText(
                                    this@PostActivity,
                                    "New Post is updated successfully.",
                                    Toast.LENGTH_SHORT
                                ).show()
                            } else {
                                Toast.makeText(
                                    this@PostActivity,
                                    "Error Occurred while updating your post.",
                                    Toast.LENGTH_SHORT
                                ).show()

                            }
                        }

                }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        })
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == Gallery_Pick && resultCode == RESULT_OK && data != null) {
            imageUri = data.data
            selectPostImage!!.setImageURI(imageUri)
        }
    }
    private fun openGallery() {
        val galleryIntent = Intent()
        galleryIntent.action = Intent.ACTION_GET_CONTENT
        galleryIntent.type = "image/*"
        startActivityForResult(galleryIntent, Gallery_Pick)
    }
    companion object {
        private const val Gallery_Pick = 1
    }
}


