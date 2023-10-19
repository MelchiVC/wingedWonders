package za.co.varsitycollege.opsc7312poe.myapplication

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.Spinner
import android.widget.Switch
import android.widget.TextView
import android.widget.Toast
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
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
    private lateinit var logoutButton: Button
    private val unitOptions = arrayOf("Miles", "Kilometers")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        unitSpinner = findViewById<Spinner>(R.id.unitSpinner)
        notificationSwitch = findViewById<Switch>(R.id.notificationSwitch)
        NameText= findViewById(R.id.full_name)
        emailText= findViewById(R.id.email)
        EditProfile= findViewById(R.id.edit_profile)
        logoutButton = findViewById(R.id.logoutBtn)
        EditProfile.setOnClickListener {
            unitSpinner.isEnabled = true
            notificationSwitch.isEnabled = true
        }
        logoutButton.setOnClickListener {
            logout()
            val intent = Intent(this@Settings, Login::class.java)
            startActivity(intent)
            finish()
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

        //BOTTOM NAVIGATION
        bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottomNavigationView)
        bottomNavigationView.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.menu_home -> {
                    // Start the HomeActivity
                    startActivity(Intent(applicationContext, Home::class.java))
                    overridePendingTransition(0, 0)
                    return@setOnNavigationItemSelectedListener true
                }
                R.id.menu_map -> {
                    // Start the SettingsActivity
                    startActivity(Intent(applicationContext, Map::class.java))
                    overridePendingTransition(0, 0)
                    return@setOnNavigationItemSelectedListener true
                }

                R.id.menu_sightings -> {
                    // Start the SightingsActivity
                    startActivity(Intent(applicationContext, Sightings::class.java))
                    overridePendingTransition(0, 0)
                    return@setOnNavigationItemSelectedListener true
                }
                R.id.menu_map -> return@setOnNavigationItemSelectedListener true
            }
            false
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
                showToast("Select a unit")
            }
        }
    }
    private fun areNotificationsEnabled(): Boolean {
        return true
    }

    private fun enableNotifications() {
    }

    private fun disableNotifications() {
    }
    private fun fetchUserDataAndSettings() {
        val firebaseUserId = UserDataManager.getInstance().getLoggedInUser()?.uid

        if (firebaseUserId != null) {
            val databaseReference = FirebaseDatabase.getInstance().reference.child("users")
                .child(firebaseUserId)
            val userReference = databaseReference.child(firebaseUserId)

            userReference.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    val userSettings = dataSnapshot.child("Settings")
                    val notificationsEnabled = userSettings.child("Notifications").getValue(Boolean::class.java)
                    val units = userSettings.child("Units").getValue(String::class.java)

                    updateUIWithLoggedInUser(userSettings.child("full_name").getValue(String::class.java), userSettings.child("email").getValue(String::class.java))
                    unitSpinner.setSelection(unitOptions.indexOf(units))
                    notificationSwitch.isChecked = notificationsEnabled ?: false
                }

                override fun onCancelled(databaseError: DatabaseError) {
                }
            })
        }
    }
    private fun updateUIWithLoggedInUser(fullName: String?, email: String?) {
        if (!fullName.isNullOrBlank() && !email.isNullOrBlank()) {
            NameText.text = fullName
            emailText.text = email
        }
    }
    private fun logout() {
        FirebaseAuth.getInstance().signOut()
        startActivity(Intent(applicationContext, Login::class.java))
        finish()
    }


    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}