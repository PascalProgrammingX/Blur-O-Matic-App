package com.example.background.workers


import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.work.Worker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import timber.log.Timber
import com.example.background.*


class BlurWorker (ctx: Context, params:WorkerParameters) : Worker(ctx, params){

    private fun sendBroadcastFinish(context:Context){
        /*
        Receiver to tell the blurActivity that the work is finish.
        And should hide some views.
         */
        val intent = Intent()
        intent.action = PROCESS_FINISH
        context.sendBroadcast(intent)
    }

    override fun doWork(): Result {
        val appContext = applicationContext
        val resourceUri = inputData.getString(KEY_IMAGE_URI)
        return try {
            val resolver = appContext.contentResolver
            val picture = BitmapFactory.decodeStream(
                    resolver.openInputStream(Uri.parse(resourceUri))
            )

            // Blurred Image bitmap stored in output.
            val output = blurBitmap(picture, appContext, 25f)

            // Write output bitmap to a temp file
            val outputUri = writeBitmapToFile(appContext, output)

            sendBroadcastFinish(appContext)
            val outputData = workDataOf(KEY_IMAGE_URI to outputUri.toString())
            Result.success(outputData)

        }catch (throwable: Throwable){
            Timber.e(throwable)
            Result.failure()
        }
    }
}