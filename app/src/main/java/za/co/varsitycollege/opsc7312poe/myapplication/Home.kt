package za.co.varsitycollege.opsc7312poe.myapplication

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider

class Home : AppCompatActivity() {
    private lateinit var greetingTextView: TextView
    private lateinit var userDataViewModel: UserDataViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)
        userDataViewModel = ViewModelProvider(this)[UserDataViewModel::class.java]
        // Initialize the TextView
        greetingTextView = findViewById(R.id.hello_user)

        // Observe the userData LiveData
        userDataViewModel.userData.observe(this) { userData ->
            if (userData != null && !userData.email.isNullOrBlank()) {
                greetingTextView.text = "Hello, ${userData.email}"
            } else {
                Toast.makeText(this, "Unsuccessful", Toast.LENGTH_SHORT).show()
            }
        }
    }
}