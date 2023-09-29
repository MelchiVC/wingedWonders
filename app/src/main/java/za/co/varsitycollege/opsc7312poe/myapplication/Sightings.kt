package za.co.varsitycollege.opsc7312poe.myapplication
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.firebase.storage.FirebaseStorage

import za.co.varsitycollege.opsc7312poe.myapplication.databinding.ActivitySightingsBinding
import java.io.ByteArrayOutputStream

class Sightings : AppCompatActivity() {
    private lateinit var binding:ActivitySightingsBinding
    private var imageUri: Uri? = null
    private  var request_gallery=100
    private  var request_carmera=200

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding= ActivitySightingsBinding.inflate(layoutInflater)
        setContentView(binding.root)


//on click listener to open a dialog to let users to decdie using camera or storage to select image
val builder = AlertDialog.Builder(this)
           builder.setTitle("Upload image methods")
           builder.setMessage("Choose a method to upload image")
           builder.setPositiveButton("Gallery") { dialog: DialogInterface, which: Int ->
               dialog.dismiss()
               selectImage()
           }
           builder.setNegativeButton("Camera") { dialog: DialogInterface, which: Int ->
               dialog.dismiss()
               carmeraImage()
           }
val dialog:AlertDialog=builder.create()
        dialog.show()



        binding.saveButton.setOnClickListener{
uploadImage()
        }


    }

    // Function to select image from local storage
private fun selectImage() {
    val selectIntent = Intent()
    selectIntent.type="image/*"
    startActivityForResult(selectIntent,request_gallery)
}
    // function to use carmera to take a photo
    private fun carmeraImage(){
        val cameraIntent= Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        if(cameraIntent.resolveActivity(packageManager) !=null) {
            val permission =ContextCompat.checkSelfPermission(this,android.Manifest.permission.CAMERA)
            if(permission !=PackageManager.PERMISSION_GRANTED){
                ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.CAMERA),1 )
            }
            else{
                startActivityForResult(cameraIntent,request_carmera)
            }
        }

        }

    // Function used after successfully pick image and set image uri
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK && requestCode == request_gallery && data != null) {
             imageUri = data?.data!!
            binding.birdImageView.setImageURI(imageUri)
        }
        else if (resultCode == RESULT_OK && requestCode == request_carmera && data != null){
            val imageBitmap = data.extras?.get("data") as Bitmap

            // Convert Bitmap to Uri
            imageUri = getImageUriFromBitmap(imageBitmap)

            binding.birdImageView.setImageBitmap(imageBitmap)
        }
    }

    private fun getImageUriFromBitmap(bitmap: Bitmap): Uri {
        val byteArrayOutputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream)
        val path = MediaStore.Images.Media.insertImage(contentResolver, bitmap, "Title", null)
        return Uri.parse(path)
    }

    //function to upload image to firebase
    private fun uploadImage(){
        val storage= FirebaseStorage.getInstance().getReference("images/")
        val birdRef = storage.child("images")

        if (imageUri != null) {
            birdRef.putFile(imageUri!!)
                .addOnSuccessListener { taskSnapshot ->
                    // Image uploaded successfully
                    // You can handle the success here, for example, show a Toast message
                    Toast.makeText(this, "Image uploaded successfully", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener { exception ->
                    // Handle unsuccessful uploads
                    // You can also show an error message to the user
                    Toast.makeText(this, "Image upload failed: ${exception.message}", Toast.LENGTH_SHORT).show()
                }
        } else {
            // Handle the case where imageUri is null (user didn't select an image)
            Toast.makeText(this, "No image selected", Toast.LENGTH_SHORT).show()
        }}}