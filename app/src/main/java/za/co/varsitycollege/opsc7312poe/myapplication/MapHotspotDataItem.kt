package za.co.varsitycollege.opsc7312poe.myapplication

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

data class MapHotspotDataItem(
    val lat: Double,
    val lng: Double,
    val locname: String
)


