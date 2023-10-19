package za.co.varsitycollege.opsc7312poe.myapplication

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.TextView
import com.google.android.material.bottomnavigation.BottomNavigationView

class Home : AppCompatActivity() {
    private lateinit var greetingTextView: TextView
    private lateinit var bottomNavigationView: BottomNavigationView
    //For commit
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)
        greetingTextView = findViewById(R.id.hello_user)

        // Retrieve the logged-in user's email from UserDataManager
        val loggedInUser = UserDataManager.getInstance().getLoggedInUser()
        val fullName = loggedInUser?.full_name

        // Set the TextView text to "Hello" + user's name
        greetingTextView.text = "Hello ${fullName ?: "Guest"}"

        greetingTextView.setOnClickListener{
            val registrationIntent = Intent(this, Settings::class.java)
            startActivity(registrationIntent)
        }

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
    }

    fun openNextScreen(view: View) {
        // Add code to navigate to the next screen here, e.g., starting a new activity.
        val intent = Intent(this, ListSightingActivity::class.java)
        startActivity(intent)
    }
}