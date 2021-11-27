package com.example.applink

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView
import java.util.*


class MainActivity : AppCompatActivity() {
    private var navigationView: NavigationView? = null
    private var mToolbar: Toolbar? = null
    private var postList: RecyclerView? = null
    private var addNewPostButton: ImageButton? = null
    private var actionBarDrawerToggle: ActionBarDrawerToggle? = null
    private var usersRef: DatabaseReference? = null
    private var postsRef: DatabaseReference? = null
    private var mAuth: FirebaseAuth? = null
    private var navProfileImage: CircleImageView? = null
    private var navProfileUserName:TextView? = null
    var currentUserID: String? = null
    private var userProfileImageRef: StorageReference? = null

    override fun onCreate(savedInstanceState: Bundle?) {


        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mAuth = FirebaseAuth.getInstance()
        userProfileImageRef = FirebaseStorage.getInstance().reference.child("Profile Images")
        usersRef = FirebaseDatabase.getInstance().reference.child("Users")
        postsRef = FirebaseDatabase.getInstance().reference.child("Posts")
        mToolbar = findViewById(R.id.main_page_toolbar)
        setSupportActionBar(mToolbar)
        supportActionBar!!.title = "Home"
        currentUserID = mAuth!!.currentUser!!.uid
        addNewPostButton = findViewById(R.id.add_new_post_btn)



        navigationView = findViewById<View>(R.id.navigation_view) as NavigationView
        postList = findViewById<View>(R.id.all_users_post_list) as RecyclerView
        postList!!.setHasFixedSize(true)
        val linearLayoutManager = LinearLayoutManager(this)
        linearLayoutManager.reverseLayout = true
        linearLayoutManager.stackFromEnd = true
        postList!!.layoutManager = linearLayoutManager
        val navView = navigationView!!.inflateHeaderView(R.layout.navigation_header)
        val drawerLayout = findViewById<View>(R.id.drawable_layout) as DrawerLayout
        actionBarDrawerToggle = ActionBarDrawerToggle(
            this@MainActivity, drawerLayout, R.string.drawer_open, R.string.drawer_close
        )
        drawerLayout.addDrawerListener(actionBarDrawerToggle!!)
        actionBarDrawerToggle!!.syncState()
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)

        navigationView!!.setNavigationItemSelectedListener { item ->
            userMenuSelector(item)
            false
        }
        navProfileImage = navView.findViewById(R.id.nav_profile_pic)
        navProfileUserName = navView.findViewById(R.id.nav_user_name)
        addNewPostButton!!.setOnClickListener{sendUserToPostActivity()}

        usersRef!!.child(currentUserID!!).addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()){
                    if (snapshot.hasChild("Username")) {
                        val username: String = snapshot.child("Username").value.toString()
                        navProfileUserName!!.text = username

                    }
                    if (snapshot.hasChild("profileimage")){
//                        val image = userProfileImageRef!!.child("$currentUserID+jpg")
                        Glide.with(this@MainActivity).load("com.google.android.gms.tasks.zzu@57702a2").placeholder(R.drawable.img).into(navProfileImage!!)
                    }
                     else {
                        Toast.makeText(this@MainActivity, "Profile name do not exists...", Toast.LENGTH_SHORT).show()
                    }
                }


            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        })

    }
    private fun displayAllUsersPosts() {
        val options = FirebaseRecyclerOptions.Builder<Posts>()
            .setQuery(postsRef!!,Posts::class.java)
            .setLifecycleOwner(this)
            .build()

        val firebaseRecyclerAdapter = object : FirebaseRecyclerAdapter<Posts, PostsViewHolder>(options){
            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostsViewHolder {
                return PostsViewHolder(LayoutInflater.from(parent.context)
                    .inflate(R.layout.all_posts_layout, parent, false))
            }

            override fun onBindViewHolder(holder: PostsViewHolder, position: Int, model: Posts) {
                holder.setUsername(model.Username)
                holder.setProfileimage(ctx = null, model.profileimage)
                holder.setDate(model.date!!)
                holder.setTime(model.time!!)
                holder.setPostimage(ctx1 = null,model.postimage)
                holder.setDescription(model.description)

            }


        }

        postList!!.adapter = firebaseRecyclerAdapter

    }
    class PostsViewHolder(var mView: View) : RecyclerView.ViewHolder(
        mView
    ){
        fun setUsername(usernam: String?) {
            val username = mView.findViewById<View>(R.id.post_username) as TextView
            username.text = usernam
        }

        fun setProfileimage(ctx: Context?, profileimage: String?) {
            val image: CircleImageView =
                mView.findViewById<View>(R.id.post_profile_image) as CircleImageView
            Picasso.with(ctx).load(profileimage).into(image)
        }

        fun setTime(time: String) {
            val postTime = mView.findViewById<View>(R.id.post_time) as TextView
            postTime.text = "    $time"
        }

        fun setDate(date: String) {
            val postDate = mView.findViewById<View>(R.id.post_date) as TextView
            postDate.text = "    $date"
        }

        fun setDescription(description: String?) {
            val postDescription = mView.findViewById<View>(R.id.post_description) as TextView
            postDescription.text = description
        }

        fun setPostimage(ctx1: Context?, postimage: String?) {
            val postImage = mView.findViewById<View>(R.id.post_image) as ImageView
            Picasso.with(ctx1).load(postimage).into(postImage)
        }
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return if (actionBarDrawerToggle!!.onOptionsItemSelected(item)) {
            true
        } else super.onOptionsItemSelected(item)
    }

    private fun userMenuSelector(item: MenuItem) {
        when (item.itemId) {

            R.id.nav_logout -> {
                mAuth!!.signOut()
                sendUserToLoginActivity()
            }
            R.id.nav_post -> {sendUserToPostActivity()}
            R.id.nav_settings -> {sendUserToSettingsActivity()}
            R.id.nav_explore -> {sendUserToExploreActivity()}
            R.id.nav_profile -> {sendUserToProfileActivity()}


        }
    }

    private fun sendUserToLoginActivity() {
        val loginIntent = Intent(this@MainActivity, LoginActivity::class.java)
        loginIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        startActivity(loginIntent)
        finish()
    }

    override fun onStart() {
        super.onStart()
        val currentUser: FirebaseUser? = mAuth!!.currentUser
        if (currentUser == null) {
            sendUserToLoginActivity()
        } else {
            checkUserExistence()
        }
    }
//
    private fun checkUserExistence() {
        val currentuserid: String = mAuth!!.currentUser!!.uid
        usersRef!!.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (!snapshot.hasChild(currentuserid)){
                    finish()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        })
    }

    private fun sendUserToPostActivity() {
        val addNewPostIntent = Intent(this@MainActivity, PostActivity::class.java)
        startActivity(addNewPostIntent)
    }
    private fun sendUserToSettingsActivity() {
        val setingsIntent = Intent(this@MainActivity, SettingsActivity::class.java)
        startActivity(setingsIntent)
    }
    private fun sendUserToExploreActivity() {
        val setingsIntent = Intent(this@MainActivity, ExploreActivity::class.java)
        startActivity(setingsIntent)
    }
    private fun sendUserToProfileActivity() {
        val setingsIntent = Intent(this@MainActivity, ProfileActivity::class.java)
        startActivity(setingsIntent)
    }


}
