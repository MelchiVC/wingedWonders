package za.co.varsitycollege.opsc7312poe.myapplication

import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.Toast
import java.lang.ref.WeakReference
object LocationManager {
    var userLocation: UserLocation? = null
}
class GetStarted : AppCompatActivity() {
    private lateinit var getStartedButton: Button
    private lateinit var locationProvider: LocationProvider

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_get_started)

        locationProvider = LocationProvider(WeakReference(this))

        getStartedButton = findViewById(R.id.getStarted)
        getStartedButton.setOnClickListener {
            locationProvider.getLastKnownLocation(
                onLocationReady = { latitude, longitude ->
                    LocationManager.userLocation = UserLocation(latitude, longitude)
                },
                onError = {
                    Toast.makeText(this, "Failed to get location", Toast.LENGTH_SHORT).show()
                }
            )
            val intent = Intent(this, Login::class.java)
            startActivity(intent)
        }
    }
}