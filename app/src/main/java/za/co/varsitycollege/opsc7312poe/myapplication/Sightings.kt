package za.co.varsitycollege.opsc7312poe.myapplication
import android.app.DatePickerDialog
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.location.Location
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import za.co.varsitycollege.opsc7312poe.myapplication.databinding.ActivitySightingsBinding
import java.io.ByteArrayOutputStream
import java.util.*

class Sightings : AppCompatActivity() {
    private lateinit var binding: ActivitySightingsBinding
    private lateinit var mAuth: FirebaseAuth
    private lateinit var bottomNavigationView: BottomNavigationView
    private lateinit var locationTextview: TextView
    private lateinit var mDatabase: DatabaseReference
    private lateinit var birdData: BirdData
    private lateinit var storageReference: StorageReference
    private var imageUri: Uri? = null
    private  var request_gallery=100
    private  var request_carmera=200

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding= ActivitySightingsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val c= Calendar.getInstance()
        val year=c.get(Calendar.YEAR)
        val month=c.get(Calendar.MONTH)
        val day=c.get(Calendar.DAY_OF_MONTH)
        locationTextview = findViewById(R.id.locationTextView)
        locationTextview.text = UserLocationProvider.getLocationName()

        binding.addPhotoButton.setOnClickListener{
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
        }
        binding.selectDateButton.setOnClickListener{

            val datePickerDialog=DatePickerDialog(this,DatePickerDialog.OnDateSetListener { datePicker, Y, M, D ->
                binding.txtSelectedDate.setText(""+D+"/"+M+"/"+Y
                )
            },year,month,day)
            datePickerDialog.datePicker.maxDate= System.currentTimeMillis()
            datePickerDialog.show()
        }

//on click listener to save all the detailed that is entered
        binding.saveButton.setOnClickListener{
            uploadbird()
            Toast.makeText(this, "Bird uploaded successfully", Toast.LENGTH_SHORT).show()
        }
// on click listener to navigate to bird list page
        binding.viewSightings.setOnClickListener{
            val intent = Intent(this@Sightings, ListSightingActivity::class.java)
            startActivity(intent)
            finish()
        }
        //region Navbar
        bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottomNavigationView)
        bottomNavigationView.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.menu_home -> {
                    // Start the HomeActivity
                    startActivity(Intent(this, Home::class.java))
                    return@setOnNavigationItemSelectedListener true
                }
                R.id.menu_map -> {
                    // Start the MapActivity
                    startActivity(Intent(this, Map::class.java))
                    return@setOnNavigationItemSelectedListener true
                }
                R.id.menu_sightings -> {
                    // Start the SightingsActivity
                    startActivity(Intent(this, Sightings::class.java))
                    return@setOnNavigationItemSelectedListener true
                }
                R.id.menu_settings -> {
                    // Start the SettingsActivity
                    startActivity(Intent(this, Settings::class.java))
                    return@setOnNavigationItemSelectedListener true
                }
            }
            false
        }
        //endregion

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
    //function to convert bitmap to url
    private fun getImageUriFromBitmap(bitmap: Bitmap): Uri {
        val byteArrayOutputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream)
        val path = MediaStore.Images.Media.insertImage(contentResolver, bitmap, "Title", null)
        return Uri.parse(path)
    }

    //function to upload bird object to firebase
    private fun uploadbird() {
        val user = FirebaseAuth.getInstance().currentUser
        mDatabase = FirebaseDatabase.getInstance().reference
        if (user != null) {
            val uid = user.uid
            val birdname = binding.birdNameEditText.text.toString().trim()
            val selectDate = binding.txtSelectedDate.text.toString().trim()
            val location= UserLocationProvider.getLocationName()
            //val Location = userLocation.locationName
            if (birdname.isNotEmpty() && selectDate.isNotEmpty() && imageUri != null && location != null) {
                // Upload image to Firebase Storage
                storageReference = FirebaseStorage.getInstance().reference
                    .child("users/$uid")
                val imageRef=storageReference.child("bird/${System.currentTimeMillis()}.jpg")
                imageRef.putFile(imageUri!!).addOnCompleteListener { storageTask ->
                    if (storageTask.isSuccessful) {
                        // Get download URL for the uploaded image
                        imageRef.downloadUrl.addOnSuccessListener { uri ->
                            val imageUrl = uri.toString()

                            // Create a BirdData object with the image URL
                            val birdData = BirdData(uid, birdname, selectDate, imageUrl,location)

                            // Save bird data to Firebase Realtime Database
                            mDatabase = FirebaseDatabase.getInstance().reference
                                .child("users/$uid")
                            val birdRef=mDatabase.child("bird/$birdname")
                            birdRef.setValue(birdData)
                                .addOnSuccessListener {
                                    Toast.makeText(this, "Bird uploaded successfully", Toast.LENGTH_SHORT).show()
                                }
                                .addOnFailureListener { exception ->
                                    Toast.makeText(this, "Bird upload failed: ${exception.message}", Toast.LENGTH_SHORT).show()
                                }
                        }
                    } else {
                        // Handle storage upload failure
                        Toast.makeText(this, "Image upload failed", Toast.LENGTH_SHORT).show()
                    }
                }
            } else {
                // Invalid input data
                Toast.makeText(this, "Bird name, date, and image are required", Toast.LENGTH_SHORT).show()
            }
        } else {
            // User not authenticated
            Toast.makeText(this, "User not authenticated", Toast.LENGTH_SHORT).show()
        }
    }}