package za.co.varsitycollege.opsc7312poe.myapplication

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.mapbox.geojson.Point
import com.mapbox.search.ResponseInfo
import com.mapbox.search.ReverseGeoOptions
import com.mapbox.search.SearchCallback
import com.mapbox.search.SearchEngine
import com.mapbox.search.SearchEngineSettings
import com.mapbox.search.common.AsyncOperationTask
import com.mapbox.search.result.SearchResult
import java.lang.ref.WeakReference

class GetStarted : AppCompatActivity() {
    private lateinit var getStartedButton: Button
    private lateinit var locationProvider: LocationProvider
    private lateinit var searchEngine: SearchEngine
    private lateinit var searchRequestTask: AsyncOperationTask

    private val searchCallback = object : SearchCallback {
        override fun onResults(results: List<SearchResult>, responseInfo: ResponseInfo) {
            if (results.isEmpty()) {
                Toast.makeText(this@GetStarted, "No reverse geocoding results", Toast.LENGTH_SHORT).show()
            } else {
                val locationName = results[0].name
                // Save the location information to UserLocationProvider
                UserLocationProvider.setUserLocation(results[0].coordinate.latitude(), results[0].coordinate.longitude(), locationName)


                Toast.makeText(this@GetStarted, "Reverse geocoding results: $locationName", Toast.LENGTH_SHORT).show()
            }
        }

        override fun onError(e: Exception) {
            Toast.makeText(this@GetStarted, "Reverse geocoding error: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_get_started)

        locationProvider = LocationProvider(WeakReference(this))

        searchEngine = SearchEngine.createSearchEngineWithBuiltInDataProviders(
            SearchEngineSettings(getString(R.string.mapbox_access_token))
        )

        getStartedButton = findViewById(R.id.getStarted)
        getStartedButton.setOnClickListener {
            locationProvider.getLastKnownLocation(
                onLocationReady = { latitude, longitude ->
                    val userLocation = UserLocation(latitude, longitude)

                    // Perform reverse geocoding to get the location name
                    reverseGeocodeLocation(userLocation)
                },
                onError = {
                    Toast.makeText(this@GetStarted, "Failed to get location", Toast.LENGTH_SHORT).show()
                }
            )

            val intent = Intent(this, Login::class.java)
            startActivity(intent)
        }
    }

    private fun reverseGeocodeLocation(userLocation: UserLocation) {
        val options = ReverseGeoOptions(
            center = Point.fromLngLat(userLocation.longitude, userLocation.latitude),
            limit = 1
        )
        searchRequestTask = searchEngine.search(options, searchCallback)
    }

    override fun onDestroy() {
        searchRequestTask.cancel()
        super.onDestroy()
    }
}