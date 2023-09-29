package za.co.varsitycollege.opsc7312poe.myapplication

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView

class Home : AppCompatActivity() {
    private lateinit var greetingTextView: TextView

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
    }
}