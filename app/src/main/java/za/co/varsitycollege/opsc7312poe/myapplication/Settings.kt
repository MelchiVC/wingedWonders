package za.co.varsitycollege.opsc7312poe.myapplication

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Switch

class Settings : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        val unitAutoCompleteTextView = findViewById<AutoCompleteTextView>(R.id.unitAutoCompleteTextView)

// Define the options for the dropdown
        val unitOptions = arrayOf("Kilometers", "Miles")

// Create an ArrayAdapter to populate the dropdown options
        val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, unitOptions)

// Set the ArrayAdapter as the adapter for the AutoCompleteTextView
        unitAutoCompleteTextView.setAdapter(adapter)

        val notificationSwitch = findViewById<Switch>(R.id.notificationSwitch)

        notificationSwitch.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                // Enable notifications
                // Perform necessary actions when the toggle button is checked
            } else {
                // Disable notifications
                // Perform necessary actions when the toggle button is unchecked
            }
        }
    }
}