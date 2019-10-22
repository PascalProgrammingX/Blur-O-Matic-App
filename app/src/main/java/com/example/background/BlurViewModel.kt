/*
 * Copyright (C) 2018 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.background

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.work.*
import com.example.background.workers.BlurWorker
import com.example.background.workers.CleanupWorker
import com.example.background.workers.SaveImageToFileWorker


class BlurViewModel(application: Application) : AndroidViewModel(application) {

    internal var imageUri: Uri? = null
    private var outputUri: Uri? = null
    private val workManager = WorkManager.getInstance(application)

    private fun uriOrNull(uriString: String?): Uri? {
        return if (!uriString.isNullOrEmpty()) {
            Uri.parse(uriString)
        } else {
            null
        }
    }

    /*
    Setting uri of image to be blurred.
    Uri is the absolute location or path to the image in storage.
     */
    internal fun setImageUri(uri: String?) {
        imageUri = uriOrNull(uri)
    }

    private fun createInputDataForUri(): Data {
        val builder = Data.Builder()
        imageUri?.let {
            builder.putString(KEY_IMAGE_URI, imageUri.toString())
        }
        return builder.build()
    }



    internal fun applyBlur(){
        /*
        Sometimes you only want one chain of work to run at a time. For example,
        perhaps you have a work chain that syncs your local data with the server -
        you probably want to let the first data sync finish before starting a new one.
        To do this, you would use beginUniqueWork instead of beginWith; and you provide a unique String name.
         This names the entire chain of work requests so that you can refer to and query them together.
       Ensure that your chain of work to blur your file is unique by using beginUniqueWork.
       Pass in IMAGE_MANIPULATION_WORK_NAME as the key. You'll also need to pass in a ExistingWorkPolicy.
        Your options are REPLACE, KEEP or APPEND. You'll use REPLACE because if the user decides to blur another
        image before the current one is finished, we want to stop the current one and start blurring the new image.
         */
        var continuation = workManager
                .beginUniqueWork(
                        IMAGE_MANIPULATION_WORK_NAME,
                        ExistingWorkPolicy.REPLACE,
                        OneTimeWorkRequest.from(CleanupWorker::class.java)
                )
        // Add WorkRequests to blur the image the number of times requested
       /* for (i in 0 until blurLevel) {
            val blurBuilder = OneTimeWorkRequestBuilder<BlurWorker>()
            // Input the Uri if this is the first blur operation
            // After the first blur operation the input will be the output of previous
            // blur operations.
            if (i == 0) {
                blurBuilder.setInputData(createInputDataForUri())
            }
            continuation = continuation.then(blurBuilder.build())
        }*/

        val blurBuilder = OneTimeWorkRequestBuilder<BlurWorker>()
        blurBuilder.setInputData(createInputDataForUri())
        continuation = continuation.then(blurBuilder.build())

        // Add WorkRequest to save the image to the filesystem
        val save = OneTimeWorkRequestBuilder<SaveImageToFileWorker>()
                .build()
        continuation = continuation.then(save)
        // Actually start the work
        continuation.enqueue()
    }



    internal fun setOutputUri(outputImageUri: String?) {
        outputUri = uriOrNull(outputImageUri)
    }
}
