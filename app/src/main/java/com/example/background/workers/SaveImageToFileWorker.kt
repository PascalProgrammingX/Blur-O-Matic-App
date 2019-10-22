package com.example.background.workers

import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.provider.MediaStore
import androidx.work.Worker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.example.background.ACTION_IMAGEURI
import com.example.background.KEY_IMAGE_URI
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.*


 class SaveImageToFileWorker(context: Context, workerParams: WorkerParameters) : Worker(context, workerParams) {


    private val TAG by lazy { SaveImageToFileWorker::class.java.simpleName }
    private val Title = "Blurred Image"
    private val dateFormatter = SimpleDateFormat(
            "yyyy.MM.dd 'at' HH:mm:ss z",
            Locale.getDefault()
    )

    override fun doWork(): Result {
        val resolver = applicationContext.contentResolver
        return try {
            val resourceUri = inputData.getString(KEY_IMAGE_URI)
            val bitmap = BitmapFactory.decodeStream(
                    resolver.openInputStream(Uri.parse(resourceUri)))
            val imageUrl = MediaStore.Images.Media.insertImage(
                    resolver, bitmap, Title, dateFormatter.format(Date()))
            if (!imageUrl.isNullOrEmpty()) {
                val output = workDataOf(KEY_IMAGE_URI to imageUrl)

                /*
                Passing saved blurred back to BlurActivity.
                User will choose to blur image again or finish work.
                 */
                val intent = Intent()
                intent.action = ACTION_IMAGEURI
                intent.putExtra("imgUri", imageUrl)
                applicationContext.sendBroadcast(intent)
                Result.success(output)

            } else {
                Timber.e("Writing to MediaStore failed")
                Result.failure()
            }
        } catch (exception: Exception) {
            Timber.e(exception)
            Result.failure()
        }
    }

}