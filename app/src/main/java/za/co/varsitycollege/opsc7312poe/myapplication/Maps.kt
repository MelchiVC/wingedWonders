package za.co.varsitycollege.opsc7312poe.myapplication

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.location.Location
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.annotation.DrawableRes
import androidx.appcompat.content.res.AppCompatResources
import com.mapbox.android.core.location.LocationEngine
import com.mapbox.android.core.location.LocationEngineListener
import com.mapbox.android.core.location.LocationEnginePriority
import com.mapbox.android.core.location.LocationEngineProvider
import com.mapbox.android.core.permissions.PermissionsListener
import com.mapbox.android.core.permissions.PermissionsManager
import com.mapbox.api.directions.v5.models.DirectionsResponse
import com.mapbox.geojson.Point
import com.mapbox.mapboxsdk.Mapbox
import com.mapbox.mapboxsdk.annotations.IconFactory
import com.mapbox.mapboxsdk.annotations.Marker
import com.mapbox.mapboxsdk.annotations.MarkerOptions
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory
import com.mapbox.mapboxsdk.geometry.LatLng
import com.mapbox.mapboxsdk.maps.MapView
import com.mapbox.mapboxsdk.maps.MapboxMap
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback
import com.mapbox.mapboxsdk.plugins.locationlayer.LocationLayerPlugin
import com.mapbox.mapboxsdk.plugins.locationlayer.modes.CameraMode
import com.mapbox.mapboxsdk.plugins.locationlayer.modes.RenderMode
import com.mapbox.services.android.navigation.ui.v5.NavigationLauncher
import com.mapbox.services.android.navigation.ui.v5.NavigationLauncherOptions
import com.mapbox.services.android.navigation.ui.v5.route.NavigationMapRoute
import com.mapbox.services.android.navigation.v5.navigation.NavigationRoute
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

const val BASE_URL ="https://api.ebird.org/v2/ref/hotspot/"
class Maps : AppCompatActivity(), OnMapReadyCallback, LocationEngineListener,
    PermissionsListener, MapboxMap.OnMapClickListener {
    private lateinit var mapView: MapView
    private var map: MapboxMap? = null
    private var permissionsManager: PermissionsManager? = null
    private var locationEngine: LocationEngine? = null
    private var locationLayerPlugin: LocationLayerPlugin? = null
    private var originLocation: Location? = null
    private var originPosition: Point? = null
    private var destinationPosition: Point? = null

    private var destinationMarker: Marker? = null
    private lateinit var startButton: Button
    private var navigationMapRoute: NavigationMapRoute? = null

    private var isMapReady: Boolean = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Mapbox.getInstance(this, "sk.eyJ1IjoidHVtaS1tYWxla2EiLCJhIjoiY2xtaHVyNGtoMnU2MzNwbzVuZjVnaG9rZyJ9.545i0frZOY30MbdVLYHZKQ")
        setContentView(R.layout.activity_maps)
        mapView = findViewById(R.id.mapView)
        startButton = findViewById(R.id.navigateButton)
        mapView.onCreate(savedInstanceState)
        mapView.getMapAsync(this)
        startButton.setOnClickListener { // Launch navigation UI
            val options = NavigationLauncherOptions.builder()
                .origin(originPosition)
                .destination(destinationPosition)
                .shouldSimulateRoute(false)
                .build()
            NavigationLauncher.startNavigation(this@Maps, options)
        }
    }

    private fun addAnnotationToMap(location: Location) {
        // Check if the map is ready
        if (!isMapReady) {
            return
        }

        // Create a marker options
        val markerOptions = MarkerOptions()
            .position(LatLng(location.latitude, location.longitude))
            .icon(IconFactory.getInstance(this@Maps).fromResource(R.drawable.red_marker))

        // Add the marker to the map
        map?.addMarker(markerOptions)
    }

    private fun bitmapFromDrawableRes(context: Context, @DrawableRes resourceId: Int) =
        convertDrawableToBitmap(AppCompatResources.getDrawable(context, resourceId))

    private fun convertDrawableToBitmap(sourceDrawable: Drawable?): Bitmap? {
        if (sourceDrawable == null) {
            return null
        }
        return if (sourceDrawable is BitmapDrawable) {
            sourceDrawable.bitmap
        } else {
            // copying drawable object to not manipulate on the same reference
            val constantState = sourceDrawable.constantState ?: return null
            val drawable = constantState.newDrawable().mutate()
            val bitmap: Bitmap = Bitmap.createBitmap(
                drawable.intrinsicWidth, drawable.intrinsicHeight,
                Bitmap.Config.ARGB_8888
            )
            val canvas = Canvas(bitmap)
            drawable.setBounds(0, 0, canvas.width, canvas.height)
            drawable.draw(canvas)
            bitmap
        }
    }

    private fun getMapData(userLocation: Location) {
        val retrofitBuilder = Retrofit.Builder()
            .addConverterFactory(GsonConverterFactory.create())
            .baseUrl(BASE_URL)
            .build()
            .create(MapApiInterface::class.java)

        // Calculate the bounding box around the user's location to fetch hotspots within a certain radius
        val radiusInKm = 10.0 // Adjust this radius as needed
        val minLat = userLocation.latitude - (radiusInKm / 111.32) // 1 degree of latitude is approximately 111.32 kilometers
        val maxLat = userLocation.latitude + (radiusInKm / 111.32)
        val minLng = userLocation.longitude - (radiusInKm / (111.32 * Math.cos(Math.toRadians(userLocation.latitude))))
        val maxLng = userLocation.longitude + (radiusInKm / (111.32 * Math.cos(Math.toRadians(userLocation.latitude))))

        val retrofitData = retrofitBuilder.getData(minLat, maxLat, minLng, maxLng)

        retrofitData.enqueue(object : Callback<List<MapHotspotDataItem>?> {
            override fun onResponse(
                call: Call<List<MapHotspotDataItem>?>,
                response: Response<List<MapHotspotDataItem>?>
            ) {
                val responseBody = response.body()!!

                for (MapHotspotData in responseBody) {
                    // Create marker and add it to the map for each hotspot in the response
                    val markerOptions = MarkerOptions()
                        .position(LatLng(MapHotspotData.lat, MapHotspotData.lng))
                        .icon(bitmapFromDrawableRes(this@Maps, R.drawable.red_marker)?.let {
                            IconFactory.getInstance(this@Maps).fromBitmap(
                                it
                            )
                        })
                    map?.addMarker(markerOptions)
                }
            }

            override fun onFailure(call: Call<List<MapHotspotDataItem>?>, t: Throwable) {
                Toast.makeText(this@Maps, "Hotspots currently unavailable", Toast.LENGTH_SHORT).show()
            }
        })
    }

    override fun onMapReady(mapboxMap: MapboxMap) {
        map = mapboxMap
        map?.addOnMapClickListener(this)
        enableLocation()
        isMapReady = true  // Set the map readiness flag
        startButton.isEnabled = false

    }

    private fun enableLocation() {
        if (PermissionsManager.areLocationPermissionsGranted(this)) {
            initializeLocationEngine()
            initializeLocationLayer()
        } else {
            permissionsManager = PermissionsManager(this)
            permissionsManager?.requestLocationPermissions(this)
        }
    }

    @SuppressLint("MissingPermission")
    private fun initializeLocationEngine() {
        locationEngine = LocationEngineProvider(this).obtainBestLocationEngineAvailable()
        locationEngine?.priority = LocationEnginePriority.HIGH_ACCURACY
        locationEngine?.activate()

        val lastLocation = locationEngine?.lastLocation
        if (lastLocation != null) {
            onLocationChanged(lastLocation)
        } else {
            locationEngine?.addLocationEngineListener(this)
        }
    }

    @SuppressLint("MissingPermission")
    private fun initializeLocationLayer() {
        locationLayerPlugin = map?.let { LocationLayerPlugin(mapView, it, locationEngine) }
        locationLayerPlugin?.let {
            it.setLocationLayerEnabled(true)

            it.cameraMode = CameraMode.TRACKING
            it.renderMode = RenderMode.NORMAL
        }
    }

    private fun setCameraPosition(location: Location) {
        map?.animateCamera(
            CameraUpdateFactory.newLatLngZoom(
                LatLng(location.latitude, location.longitude),
                13.0
            )
        )
    }

    override fun onMapClick(point: LatLng) {
        destinationMarker?.let {
            map?.removeMarker(it)
        }
        destinationMarker = null
        destinationPosition = Point.fromLngLat(point.longitude, point.latitude)
        originPosition = Point.fromLngLat(originLocation?.longitude ?: 0.0, originLocation?.latitude ?: 0.0)
        getRoute(originPosition, destinationPosition)
        if (destinationPosition != null) {
            startButton.isEnabled = true
            startButton.setBackgroundResource(R.color.purple_200)
        } else {
            startButton.isEnabled = false

            startButton.setBackgroundResource(R.color.white)
        }
    }

    private fun getRoute(origin: Point?, destination: Point?) {
        NavigationRoute.builder()
            .accessToken(Mapbox.getAccessToken() ?: "")
            .origin(origin ?: Point.fromLngLat(0.0, 0.0))
            .destination(destination ?: Point.fromLngLat(0.0, 0.0))
            .build()
            .getRoute(object : Callback<DirectionsResponse?> {
                override fun onResponse(
                    call: Call<DirectionsResponse?>,
                    response: Response<DirectionsResponse?>
                ) {
                    if (response.isSuccessful) {
                        val body = response.body()
                        if (body != null && body.routes().isNotEmpty()) {
                            val currentRoute = body.routes()[0]
                            if (navigationMapRoute != null) {
                                navigationMapRoute?.removeRoute()
                            } else {
                                navigationMapRoute = map?.let {
                                    NavigationMapRoute(null, mapView,
                                        it
                                    )
                                }
                            }
                            navigationMapRoute?.addRoute(currentRoute)
                        } else {
                            Log.e(TAG, "No routes found")
                        }
                    } else {
                        Log.e(TAG, "Route request failed")
                    }
                }

                override fun onFailure(call: Call<DirectionsResponse?>, t: Throwable) {
                    Log.e(TAG, "Error: ${t.message}")
                }
            })
    }

    override fun onConnected() {
        locationEngine?.requestLocationUpdates()
    }

    override fun onLocationChanged(location: Location) {
        originLocation = location
        setCameraPosition(location)
        addAnnotationToMap(location) // Add hotspot markers when the user's location changes
        getMapData(location) // Fetch hotspot data near the user's current location
    }

    override fun onExplanationNeeded(permissionsToExplain: List<String>) {
        // Present toast or dialog for permission explanation
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        permissionsManager?.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    override fun onPermissionResult(granted: Boolean) {
        if (granted) {
            enableLocation()
        }else{
            Log.e(TAG, "Current location cant be fetched")
        }
    }

    override fun onStart() {
        super.onStart()
        locationEngine?.requestLocationUpdates()
        locationLayerPlugin?.onStart()
        mapView.onStart()
    }

    override fun onResume() {
        super.onResume()
        mapView.onResume()
    }

    override fun onPause() {
        super.onPause()
        mapView.onPause()
    }

    override fun onStop() {
        super.onStop()
        locationEngine?.removeLocationUpdates()
        locationLayerPlugin?.onStop()
        mapView.onStop()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        mapView.onSaveInstanceState(outState)
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapView.onLowMemory()
    }

    override fun onDestroy() {
        super.onDestroy()
        locationEngine?.deactivate()
        mapView.onDestroy()
    }

    companion object {
        private const val TAG = "MainActivity"
    }



}