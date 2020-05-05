package com.example.pokemon1

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import com.google.ar.core.AugmentedImageDatabase
import com.google.ar.core.Config
import com.google.ar.core.Session
import com.google.ar.sceneform.FrameTime
import com.google.ar.sceneform.ux.ArFragment
import com.google.ar.sceneform.ux.HandMotionAnimation
import com.google.ar.sceneform.ux.HandMotionView
import java.io.IOException

private const val REQUEST_CODE_CHOOSE_IMAGE = 0
private const val USE_DATABASE = false

class ImageFragment : ArFragment() {

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (!USE_DATABASE) {
            chooseNewImage()
        }
    }

    override fun getSessionConfiguration(session: Session?): Config {
        val config = super.getSessionConfiguration(session)
        config.focusMode = Config.FocusMode.AUTO
        if (USE_DATABASE){
            config.augmentedImageDatabase=
                createAugmentedImageDatabase(session ?:return config)
        }
        return config
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
       planeDiscoveryController.setInstructionView(null)
       planeDiscoveryController.hide()
       arSceneView.planeRenderer.isEnabled = false
    }

    private fun chooseNewImage() {
        Intent(Intent.ACTION_GET_CONTENT).run {  // open the gallery
            type = "image/*"
            startActivityForResult(this, REQUEST_CODE_CHOOSE_IMAGE)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == Activity.RESULT_OK && requestCode == REQUEST_CODE_CHOOSE_IMAGE) {
            val imageUri = data?.data ?: return
            val session = arSceneView.session ?: return
            val config = getSessionConfiguration(session)
            val database = createAugmentedImageDatabaseWithSingleImage(session, imageUri)
            config.augmentedImageDatabase = database
            config.updateMode = Config.UpdateMode.LATEST_CAMERA_IMAGE
            session.configure(config)
        }
    }
     private fun createAugmentedImageDatabase(session: Session): AugmentedImageDatabase? {
         return try {
             val inputStream = resources.openRawResource(R.raw.my_image_database)
             AugmentedImageDatabase.deserialize(session, inputStream)
         } catch(e: IOException) {
             Log.e("ImageArFragment", "IOException while loading augmented image from storage", e)
             null
         }
     }

    private fun createAugmentedImageDatabaseWithSingleImage(
        session: Session,
        imageUri: Uri
    ): AugmentedImageDatabase {
        val database = AugmentedImageDatabase(session)
        val bmp = loadAugmentedImageBitmap(imageUri)
        database.addImage("myImage.jpg", bmp)
        return database
    }

    private fun loadAugmentedImageBitmap(imageUri: Uri): Bitmap? {
        return try {
            context?.contentResolver?.openInputStream(imageUri)?.use {
                BitmapFactory.decodeStream(it)
            }
        } catch (e: IOException) {
            Log.e("ImageArFragment", "IOException while loading augmented image from storage", e)
            null
        }
    }
}