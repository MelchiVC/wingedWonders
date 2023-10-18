package za.co.varsitycollege.opsc7312poe.myapplication

import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

interface MapApiInterface {
    @GET("geo?lat=-26.127028&lng=28.144311&fmt=json")
    fun getData(@Query("minLat") minLat: Double,
                @Query("maxLat") maxLat: Double,
                @Query("minLng") minLng: Double,
                @Query("maxLng") maxLng: Double): Call<List<MapHotspotDataItem>>


}
val retrofit = Retrofit.Builder()
    .baseUrl("https://api.ebird.org/v2/")
    .addConverterFactory(GsonConverterFactory.create())
    .build()

val mapApiInterface = retrofit.create(MapApiInterface::class.java)
