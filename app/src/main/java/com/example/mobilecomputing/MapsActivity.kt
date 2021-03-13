package com.example.mobilecomputing

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.PendingIntent
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Resources
import android.graphics.Color
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.*

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.android.gms.maps.model.MarkerOptions
import kotlinx.android.synthetic.main.reminder_item.*
import java.util.*
import kotlin.random.Random

const val LOCATION_REQUEST_CODE = 123
const val CAMERA_ZOOM_LEVEL = 13f
const val REQUEST_LOCATION_PERMISSION = 312

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var marker: Marker
    private var prevLatitude: Double = 65.05484255386753
    private var prevLongitude: Double = 25.467636441858307

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)
        val bundle :Bundle ?=intent.extras

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
                .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        if(bundle?.getDouble("latitude")!=null) {
            prevLatitude = bundle.getDouble("latitude")
            prevLongitude = bundle.getDouble("longitude")
        }  else {
            prevLatitude = 65.05484255386753
            prevLongitude = 25.467636441858307
        }

    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        mMap.uiSettings.isZoomControlsEnabled = true

        val reminderMarker = LatLng(prevLatitude,prevLongitude)

        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(reminderMarker, CAMERA_ZOOM_LEVEL))
        marker = mMap.addMarker(MarkerOptions().position(reminderMarker))
        mMap.addCircle(
            CircleOptions()
                .center(reminderMarker)
                .strokeColor(Color.argb(50,255,255,255))
                .fillColor(Color.argb(70,180, 180, 180))
                .radius(GEOFENCE_RADIUS.toDouble())
        )

        enableMyLocation()
        setLongClick(mMap)
        setPoiOnClick(mMap)
        setMapStyle(mMap)

    }

    private fun setLongClick(googleMap: GoogleMap) {
        googleMap.setOnMapLongClickListener {

            marker.remove()

            val snippet = String.format(
                    Locale.getDefault().toString(),
                    "Lat: %1$.5f, Lng: %2$.5f",
                    it.latitude,
                    it.longitude
            )

            marker = googleMap.addMarker(
                        MarkerOptions().position(it)
                            .title("Reminder location")
                            .snippet(snippet)
            )

            val latlng = LatLng(marker.position.latitude, marker.position.longitude)
            val key = Random.nextInt(10, 1000) + 5

            val intent = Intent()
                .putExtra("latitude", marker.position.latitude)
                .putExtra("longitude", marker.position.longitude)
            setResult(RESULT_OK, intent)
            finish()
        }
    }

    private fun setPoiOnClick(googleMap: GoogleMap) {
        googleMap.setOnPoiClickListener{

            marker.remove()

            marker = googleMap.addMarker(
                    MarkerOptions().position(it.latLng)
                            .title(it.name)
            )
        }
    }

    private fun setMapStyle(map: GoogleMap) {
        try {
            val success = map.setMapStyle(
                MapStyleOptions.loadRawResourceStyle(this, R.raw.style)
            )
            if (!success) {
                Log.e("Test", "Style parsing failed")
            }
        } catch (e: Resources.NotFoundException) {
            Log.e("Test", "Can't find style. Error: ", e)
        }
    }


    private fun isPermissionGranted(): Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    @SuppressLint("MissingPermission")
    private fun enableMyLocation() {
        if (isPermissionGranted()) {
            mMap.isMyLocationEnabled = true
        } else {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                REQUEST_LOCATION_PERMISSION
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if(requestCode == REQUEST_LOCATION_PERMISSION) {
            if(grantResults.contains(PackageManager.PERMISSION_GRANTED)) {
                enableMyLocation()
            }
        }
    }
}