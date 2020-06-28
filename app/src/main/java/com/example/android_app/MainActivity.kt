package com.example.android_app

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.ContentValues
import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Base64
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.android_app.models.FileEntity
import com.example.android_app.networking.RestClient
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import com.ocaterinca.ocaterinca.utils.ImageUtils
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.io.IOException
import okhttp3.*
import okio.Buffer
import org.json.JSONObject
import java.io.File
import java.util.concurrent.TimeUnit


class MainActivity : AppCompatActivity() {

    var fileUri: Uri? = null
    private var mediaPath: String? = null
    private var postPath: String? = null
    private var mImageFileLocation = ""


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        upload.isEnabled = false
        //listen to gallery button click
        gallery.setOnClickListener {
            pickPhotoFromGallery()
        }

        //listen to take photo button click
        takePhoto.setOnClickListener {
            askCameraPermission()
        }

        upload.setOnClickListener { preupload() }

        val clientWebsoket = OkHttpClient.Builder()
            .readTimeout(3, TimeUnit.SECONDS)
            .build()
        val request = Request.Builder()
            .url("ws://192.168.0.105:3000")
            .build()
        val wsListener = EchoWebSocketListener()
        clientWebsoket.newWebSocket(request, wsListener) // this provide to make 'Open ws connection'

        // Trigger shutdown of the dispatcher's executor so this process can exit cleanly.
        clientWebsoket.dispatcher().executorService().shutdown()

    }

    inner class EchoWebSocketListener : WebSocketListener() {
        lateinit var webSocket: WebSocket
        override fun onOpen(webSocket: WebSocket, response: Response) {
            this.webSocket = webSocket
            webSocket.send("Hello, there!")
            webSocket.send("What's up?")
        }

        override fun onMessage(webSocket: WebSocket?, text: String?) {
            GlobalScope.launch(Dispatchers.Main) {

                val s: String = JSONObject(text).getString("status")
                Toast.makeText(this@MainActivity, s, Toast.LENGTH_LONG).show()
            }
        }
    }



    private fun preupload() {
        progressBar.visibility = View.VISIBLE
        takePhoto.visibility = View.GONE
        upload.visibility = View.GONE
        gallery.visibility = View.GONE
        imageView.visibility = View.GONE
        cardView.visibility = View.GONE

        val filePathColumn = arrayOf(MediaStore.Images.Media.DATA)

        val cursor = contentResolver.query(fileUri!!, filePathColumn, null, null, null)
        assert(cursor != null)
        cursor!!.moveToFirst()

        val columnIndex = cursor.getColumnIndex(filePathColumn[0])
        mediaPath = cursor.getString(columnIndex)
        // Set the Image in ImageView for Previewing the Media
        imageView.setImageBitmap(BitmapFactory.decodeFile(mediaPath))
        cursor.close()
        postPath = mediaPath
        var image = ImageUtils.createBitmapFromPath(postPath)!!
        val width = image.width
        val height = image.height
        Log.d("gigi", "$width $height")

        uploadFile()

    }

    //pick a photo from gallery
    private fun pickPhotoFromGallery() {
        val pickImageIntent = Intent(
            Intent.ACTION_PICK,
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        )

        startActivityForResult(pickImageIntent, AppConstants.PICK_PHOTO_REQUEST)
    }

    //launch the camera to take photo via intent
    private fun launchCamera() {
        val values = ContentValues(1)
        values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpg")
        fileUri = contentResolver
            .insert(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                values
            )
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        if (intent.resolveActivity(packageManager) != null) {
            intent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri)
            intent.addFlags(
                Intent.FLAG_GRANT_READ_URI_PERMISSION
                        or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
            )
            startActivityForResult(intent, AppConstants.TAKE_PHOTO_REQUEST)
        }
    }

    //ask for permission to take photo
    fun askCameraPermission() {
        Dexter.withActivity(this)
            .withPermissions(
                Manifest.permission.CAMERA,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ).withListener(object : MultiplePermissionsListener {
                override fun onPermissionsChecked(report: MultiplePermissionsReport) {/* ... */
                    if (report.areAllPermissionsGranted()) {
                        //once permissions are granted, launch the camera
                        launchCamera()
                    } else {
                        Toast.makeText(
                            this@MainActivity,
                            "All permissions need to be granted to take photo",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }

                override fun onPermissionRationaleShouldBeShown(
                    permissions: List<PermissionRequest>,
                    token: PermissionToken
                ) {/* ... */
                    //show alert dialog with permission options
                    AlertDialog.Builder(this@MainActivity)
                        .setTitle(
                            "Permissions Error!"
                        )
                        .setMessage(
                            "Please allow permissions to take photo with camera"
                        )
                        .setNegativeButton(
                            android.R.string.cancel,
                            { dialog, _ ->
                                dialog.dismiss()
                                token?.cancelPermissionRequest()
                            })
                        .setPositiveButton(android.R.string.ok,
                            { dialog, _ ->
                                dialog.dismiss()
                                token?.continuePermissionRequest()//aici fracao
                            })
//                        .setOnDismissListener({
//                            token?.cancelPermissionRequest() })
//                        .show()
                }

            }).check()

    }

    //override function that is called once the photo has been taken
    override fun onActivityResult(
        requestCode: Int, resultCode: Int,
        data: Intent?
    ) {
        if (resultCode == Activity.RESULT_OK
            && requestCode == AppConstants.TAKE_PHOTO_REQUEST
        ) {
            //photo from camera
            //display the photo on the imageview
            imageView.setImageURI(fileUri)
            Toast.makeText(this, "Image taken", Toast.LENGTH_LONG).show()
        } else if (resultCode == Activity.RESULT_OK
            && requestCode == AppConstants.PICK_PHOTO_REQUEST
        ) {
            //photo from gallery
            fileUri = data?.data
            Log.d("gigi", fileUri!!.encodedPath!!)
            imageView.setImageURI(fileUri)
            upload.isEnabled = true
        } else {
            super.onActivityResult(requestCode, resultCode, data)
        }

    }

    @SuppressLint("CheckResult")
    private fun uploadFile() {
//        if (postPath == null || postPath == "") {
//            Toast.makeText(this, "please select an image ", Toast.LENGTH_LONG).show()
//            return
//        } else {

            // Map is used to multipart the file using okhttp3.RequestBody
            val map = HashMap<String, RequestBody>()
            val file = File(postPath!!)

            // Parsing any Media type file
            val requestBody = RequestBody.create(MediaType.parse("*/*"), file)
//            map["file\"; filename=\"" + file.name + "\""] = requestBody
            map["file"] = requestBody
//            map["fileName"] = "horea"
            Log.d("horea", map.toString())
            val baits = ImageUtils.resizeImageKeepAspectRatio(postPath, 2000, 2000).base64
            val fileEntity = FileEntity(
                fileName = "Gigi",
                file = "data:image/jpeg;base64,$baits"
            )
            Log.d("gigi", fileEntity.toString())
            RestClient.service.upload(fileEntity)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ result ->
                    Log.d("gigi", result.toString())
                    progressBar.visibility = View.GONE
                    takePhoto.visibility = View.VISIBLE
                    upload.visibility = View.VISIBLE
                    gallery.visibility = View.VISIBLE
                    imageView.visibility = View.VISIBLE
                    cardView.visibility = View.VISIBLE

                    moveToCodeActivity(result.text, result.googleText)

                }
                    ,
                    { throwable ->
                        Log.d("gigi", "Pl: " + throwable.message!!)
                        progressBar.visibility = View.GONE
                        takePhoto.visibility = View.VISIBLE
                        upload.visibility = View.VISIBLE
                        gallery.visibility = View.VISIBLE
                        imageView.visibility = View.VISIBLE
                        cardView.visibility = View.VISIBLE

                        Toast.makeText(this, throwable.message!!, Toast.LENGTH_LONG).show()
                    })


//        }
    }

    private fun moveToCodeActivity(text: String, googleText: String) {
        val activity = Intent(this, CodeActivity::class.java)
        activity.putExtra("text", text)
        activity.putExtra("googleText", googleText)
        this.startActivity(activity)
    }

        private fun bodyToString(request: RequestBody): String {
            try {
                val buffer = Buffer()
                request.writeTo(buffer)
                return buffer.readUtf8()
            } catch (e: IOException) {
                return "did not work"
            }

        }

        fun convertToBase64(attachment: File): String {
            return Base64.encodeToString(attachment.readBytes(), Base64.NO_WRAP)
        }


    }
