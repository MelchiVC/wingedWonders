package za.co.varsitycollege.opsc7312poe.myapplication
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.preference.PreferenceManager.OnActivityResultListener
import android.provider.MediaStore
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.firebase.storage.FirebaseStorage

import za.co.varsitycollege.opsc7312poe.myapplication.databinding.ActivitySightingsBinding

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
         var imagemap=data.extras?.get("data") as Bitmap
            binding.birdImageView.setImageBitmap(imagemap)
        }
    }


    //function to upload image to firebase
  /*  private fun uploadImage(){
        val storage= FirebaseStorage.getInstance().getReference("images/")
        val birdRef=
        storage.putFile(imageUri!!).addOnSuccessListener {
            storage.downloadUrl.addOnSuccessListener { Uri->
                val map= HashMap<String, Any>()
                map["pic"]= Uri.toString()
                taskRef.child("imageUri").setValue(map)
    }
}
        }*/
}