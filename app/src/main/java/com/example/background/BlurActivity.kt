package com.example.background

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.lifecycle.ViewModelProviders
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.view.View
import android.widget.*
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.activity_blur.*


class BlurActivity : AppCompatActivity() {

    private lateinit var viewModel: BlurViewModel
    var imgUri:String? = ""

    private  var receiver = object : BroadcastReceiver(){
        override fun onReceive(context: Context?, intent: Intent?) {
            when {
                intent!!.action == PROCESS_RUNNING -> {
                    progress_bar.visibility = View.VISIBLE
                    go_button.visibility = View.GONE
                }
                intent.action == PROCESS_FINISH -> {
                    Toast.makeText(this@BlurActivity,"Done", Toast.LENGTH_SHORT).show()
                    showWorkFinished()
                }
                intent.action == ACTION_IMAGEURI -> {
                    imgUri = intent.getStringExtra("imgUri")
                    Glide.with(context!!).load(imgUri).into(image_view)
                    viewModel.setImageUri(imgUri)
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_blur)

        val filter = IntentFilter()
        filter.addAction(PROCESS_RUNNING)
        filter.addAction(PROCESS_FINISH)
        filter.addAction(ACTION_IMAGEURI)
        registerReceiver(receiver, filter)

        // Get the ViewModel
        viewModel = ViewModelProviders.of(this).get(BlurViewModel::class.java)

        // Image uri should be stored in the ViewModel; put it there then display
        val imageUriExtra = intent.getStringExtra(KEY_IMAGE_URI)
        viewModel.setImageUri(imageUriExtra)
        viewModel.imageUri?.let { imageUri ->
            Glide.with(this).load(imageUri).into(image_view)
        }

        setOnClickListener()
    }

    private fun showWorkInProgress() {
        Toast.makeText(this@BlurActivity,"Burring image...", Toast.LENGTH_SHORT).show()
        progress_bar.visibility = View.VISIBLE
        go_button.visibility = View.GONE
        finish.visibility = View.GONE
        edit.visibility = View.GONE
    }


   private fun setOnClickListener(){
       go_button.setOnClickListener{
           viewModel.applyBlur()
           showWorkInProgress()
       }

       finish.setOnClickListener {
           finish()
       }

       edit.setOnClickListener {
           Toast.makeText(this, "Still in development", Toast.LENGTH_SHORT).show()
       }
   }

   private fun showWorkFinished() {
        progress_bar.visibility = View.GONE
        go_button.visibility = View.VISIBLE
        go_button.text = getText(R.string.again)
        finish.visibility = View.VISIBLE
        edit.visibility = View.VISIBLE
    }


    override fun onDestroy() {
        super.onDestroy()
        /*
        Unregister receiver to clean up memory
         */
        unregisterReceiver(receiver)
    }
}