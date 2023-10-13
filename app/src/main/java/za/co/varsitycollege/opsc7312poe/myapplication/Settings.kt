package za.co.varsitycollege.opsc7312poe.myapplication

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Spinner
import android.widget.Switch
import android.widget.TextView
import android.widget.Toast
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlin.collections.Map

class Settings : AppCompatActivity() {
    private lateinit var bottomNavigationView: BottomNavigationView
    private lateinit var NameText: TextView
    private lateinit var emailText: TextView
    private lateinit var EditProfile: TextView
    private lateinit var unitSpinner: Spinner
    private lateinit var notificationSwitch: Switch
    private val unitOptions = arrayOf("Miles", "Kilometers")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        unitSpinner = findViewById<Spinner>(R.id.unitSpinner)
        notificationSwitch = findViewById<Switch>(R.id.notificationSwitch)
        NameText= findViewById(R.id.full_name)
        emailText= findViewById(R.id.email)
        EditProfile= findViewById(R.id.edit_profile)
        EditProfile.setOnClickListener {
            // Enable the Spinner and Switch
            unitSpinner.isEnabled = true
            notificationSwitch.isEnabled = true
//For commit
            // Add any other code you need when "Edit Profile" is clicked
        }
        updateUIWithLoggedInUser()
        fetchUserDataAndSettings()

        setupSpinner(unitSpinner, unitOptions)

        notificationSwitch.isChecked = areNotificationsEnabled()

        notificationSwitch.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                // Enable notifications
                enableNotifications()
                Toast.makeText(this, "Notifications enabled", Toast.LENGTH_SHORT).show()
            } else {
                // Disable notifications
                disableNotifications()
            }
        }
    }
    //Gets logged in users details
    private fun updateUIWithLoggedInUser() {
        val loggedInUser = UserDataManager.getInstance().getLoggedInUser()

        // Check if the loggedInUser is not null
        if (loggedInUser != null) {
            // Update the NameText and emailText with the user's data
            NameText.text = loggedInUser.full_name
            emailText.text = loggedInUser.email
        }
    }
    //Spinner method
    private fun setupSpinner(spinner: Spinner, options: Array<String>) {
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, options)
        spinner.adapter = adapter
        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val selectedUnitSystem = options[position]
                showToast("Selected Unit System: $selectedUnitSystem")
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                // Handle case where nothing is selected (optional)
            }
        }
    }
    // Sample method to check if notifications are enabled
    private fun areNotificationsEnabled(): Boolean {
        // Implement your logic to check if notifications are enabled
        // For example, you can check shared preferences or system settings
        return true // Change this based on your actual logic
    }

    // Sample method to enable notifications
    private fun enableNotifications() {
        // Implement code to enable notifications here
    }

    // Sample method to disable notifications
    private fun disableNotifications() {
        // Implement code to disable notifications here
    }
    private fun fetchUserDataAndSettings() {
        // Fetch the UID from UserDataManager
        val firebaseUserId = UserDataManager.getInstance().getLoggedInUser()?.uid

        if (firebaseUserId != null) {
            // Assuming you have a reference to Firebase here, replace "yourFirebaseReference" with your actual reference
            val databaseReference = FirebaseDatabase.getInstance().reference.child("users")
                .child(firebaseUserId)
            val userReference = databaseReference.child(firebaseUserId)

            userReference.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    val userSettings = dataSnapshot.child("Settings")
                    val notificationsEnabled = userSettings.child("Notifications").getValue(Boolean::class.java)
                    val units = userSettings.child("Units").getValue(String::class.java)

                    // Update the UI with the fetched data
                    updateUIWithLoggedInUser(userSettings.child("full_name").getValue(String::class.java), userSettings.child("email").getValue(String::class.java))
                    unitSpinner.setSelection(unitOptions.indexOf(units))
                    notificationSwitch.isChecked = notificationsEnabled ?: false
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    // Handle errors here
                }
            })
        }
    }
    private fun updateUIWithLoggedInUser(fullName: String?, email: String?) {
        // Check if the fullName and email are not null
        if (!fullName.isNullOrBlank() && !email.isNullOrBlank()) {
            // Update the NameText and emailText with the user's data
            NameText.text = fullName
            emailText.text = email
        }
    }



    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}