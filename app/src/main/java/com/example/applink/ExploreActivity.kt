package com.example.applink

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.EditText
import android.widget.ImageButton
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class ExploreActivity : AppCompatActivity() {
    private var mToolbar: Toolbar? = null
    private var searchBtn: ImageButton? = null
    private var searchInputText: EditText? =null
    private var searchResultList: RecyclerView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_explore)

        mToolbar = findViewById(R.id.explore_app_bar_layout)
        setSupportActionBar(mToolbar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.setDisplayShowHomeEnabled(true)
        supportActionBar!!.title = "Explore"
        searchResultList = findViewById(R.id.search_result_list)
        searchResultList!!.setHasFixedSize(true)
        searchResultList!!.layoutManager = LinearLayoutManager(this)


        searchBtn = findViewById(R.id.search_followers_btn)
        searchInputText = findViewById(R.id.search_box_input)

        searchBtn!!.setOnClickListener{
            val searchBoxInput = searchInputText!!.text.toString()
            searchFollowers()}
    }

    private fun searchFollowers() {

    }
}