package za.co.varsitycollege.opsc7312poe.myapplication

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.location.LocationManager
import android.os.Bundle
import android.util.Log
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.DrawableRes
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.content.res.AppCompatResources
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.mapbox.android.gestures.MoveGestureDetector
import com.mapbox.geojson.Point
import com.mapbox.maps.CameraOptions
import com.mapbox.maps.MapView
import com.mapbox.maps.Style
import com.mapbox.maps.extension.style.expressions.dsl.generated.interpolate
import com.mapbox.maps.plugin.LocationPuck2D
import com.mapbox.maps.plugin.annotation.annotations
import com.mapbox.maps.plugin.annotation.generated.PointAnnotationOptions
import com.mapbox.maps.plugin.annotation.generated.createPointAnnotationManager
import com.mapbox.maps.plugin.gestures.OnMoveListener
import com.mapbox.maps.plugin.gestures.gestures
import com.mapbox.maps.plugin.locationcomponent.OnIndicatorBearingChangedListener
import com.mapbox.maps.plugin.locationcomponent.OnIndicatorPositionChangedListener
import com.mapbox.maps.plugin.locationcomponent.location
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import za.co.varsitycollege.opsc7312poe.myapplication.RetrofitService.createEBirdApiService
import java.lang.ref.WeakReference
import kotlin.collections.Map


class Map : AppCompatActivity() {
    private lateinit var locationPermissionHelper: LocationPermissionHelper
    private lateinit var bottomNavigationView: BottomNavigationView
    private lateinit var locName: TextView
    private var userLocation: UserLocation? = null
    private var userLatitude: Double= 0.0
    private var userLongitude: Double= 0.0
    private val apiKey = "keodjjotqkd0"
    private val onIndicatorBearingChangedListener = OnIndicatorBearingChangedListener {
        mapView.getMapboxMap().setCamera(CameraOptions.Builder().bearing(it).build())
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_map)
        locName= findViewById(R.id.locationTextView)
        locName.text= "Current location: "+ UserLocationProvider.getLocationName()
        //region SeekBar
        val distanceSeekBar = findViewById<SeekBar>(R.id.distanceSeekBar)
        val distanceTextView = findViewById<TextView>(R.id.distanceTextView)
        val locationTextView = findViewById<TextView>(R.id.locationTextView)
        val maxDistance = 100
        distanceSeekBar.max = maxDistance
        distanceTextView.text = "Maximum Distance: 0 km"

        distanceSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                // Update the TextView with the current selected distance
                distanceTextView.text = "Maximum Distance: $progress km"
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                // Display the selected value when the user starts dragging
                val progress = distanceSeekBar.progress
                distanceTextView.text = "Maximum Distance: $progress km"
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                // Display the selected value when the user stops dragging
                val progress = distanceSeekBar.progress
                distanceTextView.text = "Maximum Distance: $progress km"
            }
        })
      //endregion

        //region MapView Initialization
        //initializing the mapView
        mapView = findViewById(R.id.mapView)
        if (UserLocationProvider.getUserLatitude() != null) {
            userLatitude = UserLocationProvider.getUserLatitude()
            userLongitude = UserLocationProvider.getUserLongitude()

        } else {
            Toast.makeText(this, "Failed to get location", Toast.LENGTH_SHORT).show()
        }
        locationPermissionHelper = LocationPermissionHelper(WeakReference(this))
        locationPermissionHelper.checkPermissions {
            onMapReady(userLatitude,userLongitude)
        }
        //endregion

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

    //region Map logic
    //region Map initialization
    private val onIndicatorPositionChangedListener = OnIndicatorPositionChangedListener { location ->
        userLatitude = location.latitude()
        userLongitude = location.longitude()
        mapView.getMapboxMap().setCamera(CameraOptions.Builder().center(location).build())
        mapView.gestures.focalPoint = mapView.getMapboxMap().pixelForCoordinate(location)
        // You can also use the updated values here or display them in a toast if needed.

    }

    private val onMoveListener = object : OnMoveListener {
        override fun onMoveBegin(detector: MoveGestureDetector) {
            onCameraTrackingDismissed()
        }

        override fun onMove(detector: MoveGestureDetector): Boolean {
            return false
        }

        override fun onMoveEnd(detector: MoveGestureDetector) {}
    }
    private lateinit var mapView: MapView
    private fun onMapReady(latitude: Double,longitude: Double) {
        mapView.getMapboxMap().setCamera(
            CameraOptions.Builder()
                .zoom(14.0)
                .build()
        )
        mapView.getMapboxMap().loadStyleUri(
            Style.MAPBOX_STREETS
        ) {
            initLocationComponent()
            setupGesturesListener()
        }


        val apiKey = "keodjjotqkd0"
        val distanceInKm = 20.0
        val maxResults = 50


        CoroutineScope(Dispatchers.Main).launch {
            fetchHotspotsFromEBird(latitude, longitude, distanceInKm, maxResults, apiKey)
        }

    }

    private fun setupGesturesListener() {
        mapView.gestures.addOnMoveListener(onMoveListener)
    }

    private fun initLocationComponent() {
        val locationComponentPlugin = mapView.location
        locationComponentPlugin.updateSettings {
            this.enabled = true
            this.locationPuck = LocationPuck2D(
                bearingImage = AppCompatResources.getDrawable(
                    this@Map,
                    R.drawable.mapbox_mylocation_icon_default,
                ),
                shadowImage = AppCompatResources.getDrawable(
                    this@Map,
                    R.drawable.mapbox_mylocation_icon_default,
                ),
                scaleExpression = interpolate {
                    linear()
                    zoom()
                    stop {
                        literal(0.0)
                        literal(0.6)
                    }
                    stop {
                        literal(20.0)
                        literal(1.0)
                    }
                }.toJson())
        }
        locationComponentPlugin.addOnIndicatorPositionChangedListener(onIndicatorPositionChangedListener)
        locationComponentPlugin.addOnIndicatorBearingChangedListener(onIndicatorBearingChangedListener)
    }

    private fun onCameraTrackingDismissed() {
        Toast.makeText(this, "onCameraTrackingDismissed", Toast.LENGTH_SHORT).show()
        mapView.location
            .removeOnIndicatorPositionChangedListener(onIndicatorPositionChangedListener)
        mapView.location
            .removeOnIndicatorBearingChangedListener(onIndicatorBearingChangedListener)
        mapView.gestures.removeOnMoveListener(onMoveListener)
    }

    override fun onDestroy() {
        super.onDestroy()
        mapView.location
            .removeOnIndicatorBearingChangedListener(onIndicatorBearingChangedListener)
        mapView.location
            .removeOnIndicatorPositionChangedListener(onIndicatorPositionChangedListener)
        mapView.gestures.removeOnMoveListener(onMoveListener)
    }
    //endregion

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        locationPermissionHelper.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }
    //endregion
    //region API Logic and implementation
    private fun fetchHotspotsFromEBird(
        latitude: Double,
        longitude: Double,
        distanceInKm: Double,
        maxResults: Int,
        apiKey: String
    ) {
        val eBirdApiService = createEBirdApiService(apiKey)
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = eBirdApiService.getHotspots(latitude, longitude, maxResults, distanceInKm, "csv", apiKey)

                if (response.isSuccessful) {
                    val csvData = response.body()?.string()
                    if (!csvData.isNullOrBlank()) {
                        // Parse the CSV data to get the latitude and longitude values
                        Log.d("CSV Data", csvData)
                        val hotspots = parseCsvToHotspots(csvData)
                        addMarkersToMap(hotspots)
                    } else {
                        Log.e("CSV Error", "Empty or null CSV data")
                    }
                } else {
                    Log.e("API Error", "API request was not successful")
                }
            } catch (e: Exception) {
                Log.e("API Error", e.toString())
                e.printStackTrace()
            }
        }
    }
    private fun parseCsvToHotspots(csvData: String): List<hotspots> {
        val hotspots = mutableListOf<hotspots>()

        val lines = csvData.split("\n")
        for (line in lines) {
            val parts = line.split(",")
            if (parts.size >= 5) {
                val locLat = parts[4].toDouble()
                val locLng = parts[5].toDouble()
                val name = parts[6]
                if (locLat != null && locLng != null) {
                    // Create a Hotspot object and add it to the list
                    val hotspot = hotspots(locLat, locLng, name)
                    hotspots.add(hotspot)
                }
            }
        }

        return hotspots
    }

    private fun addMarkersToMap(hotspots: List<hotspots>) {
        // Ensure this code is executed on the main thread
        runOnUiThread {
            hotspots.forEach { hotspot ->
                val annotationApi = mapView.annotations
                val pointAnnotationManager = annotationApi.createPointAnnotationManager(mapView)

                val latitude = hotspot.latitude
                val longitude = hotspot.longitude
                val yourMarkerImageI = bitmapFromDrawableRes(this@Map, R.drawable.red_marker)
                // Create a PointAnnotationOptions for each hotspot
                val pointAnnotationOptions = yourMarkerImageI?.let {
                    PointAnnotationOptions()
                        .withPoint(Point.fromLngLat(longitude, latitude))
                        .withIconImage(it)
                }

                // Add the resulting pointAnnotation to the map
                if (pointAnnotationOptions != null) {
                    pointAnnotationManager.create(pointAnnotationOptions)
                }
            }
        }
    }
    private fun bitmapFromDrawableRes(context: Context, @DrawableRes resourceId: Int): Bitmap? {
        if (context == null) {
            return null
        }

        val drawable = AppCompatResources.getDrawable(context, resourceId)
        if (drawable is BitmapDrawable) {
            return drawable.bitmap
        } else {
            val constantState = drawable?.constantState ?: return null
            val newDrawable = constantState.newDrawable().mutate()
            val bitmap = Bitmap.createBitmap(
                newDrawable.intrinsicWidth,
                newDrawable.intrinsicHeight,
                Bitmap.Config.ARGB_8888
            )
            val canvas = Canvas(bitmap)
            newDrawable.setBounds(0, 0, canvas.width, canvas.height)
            newDrawable.draw(canvas)
            return bitmap
        }
    }
    //endregion

}

