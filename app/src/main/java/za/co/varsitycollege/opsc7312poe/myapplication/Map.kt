package za.co.varsitycollege.opsc7312poe.myapplication


import android.os.Bundle
import android.widget.SeekBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions

class Map: AppCompatActivity(), OnMapReadyCallback {
    private lateinit var mapView: MapView
    //For commit
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_map)

        val distanceSeekBar = findViewById<SeekBar>(R.id.distanceSeekBar)
        val distanceTextView = findViewById<TextView>(R.id.distanceTextView)

// Set the maximum value for the SeekBar (e.g., 100 km)
        val maxDistance = 100
        distanceSeekBar.max = maxDistance

// Set the initial value for the TextView
        distanceTextView.text = "Maximum Distance: 0 km"

// Set a SeekBar listener to update the TextView
        distanceSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                // Update the TextView with the current selected distance
                distanceTextView.text = "Maximum Distance: $progress km"
            }

            override fun onStartTrackingTouch(p0: SeekBar?) {
                TODO("Not yet implemented")
            }

            override fun onStopTrackingTouch(p0: SeekBar?) {
                TODO("Not yet implemented")
            }


        })


        //initializing the mapView
        mapView = findViewById(R.id.mapView)
        mapView.onCreate(savedInstanceState)
        mapView.getMapAsync(this)
    }

    override fun onResume() {
        super.onResume()
        mapView.onResume()
    }

    override fun onPause() {
        super.onPause()
        mapView.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
        mapView.onDestroy()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapView.onLowMemory()
    }

    override fun onMapReady(googleMap: GoogleMap) {
        // Customize the map settings and add markers for birding hotspots here
        // Use the eBird API to obtain the nearby birding hotspots and display them on the map
    }
}
