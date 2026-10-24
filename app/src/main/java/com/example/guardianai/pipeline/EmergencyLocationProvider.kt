package com.example.guardianai.pipeline

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.core.content.ContextCompat
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource

@SuppressLint("MissingPermission")
object EmergencyLocationProvider {
    var latestLocation: Location? = null
    private var isTracking = false

    @Synchronized
    fun updateLocation(loc: Location?) {
        if (loc == null) return
        if (loc.latitude == 0.0 && loc.longitude == 0.0) return
        val current = latestLocation
        if (current == null || loc.time > current.time || loc.accuracy < current.accuracy) {
            latestLocation = loc
        }
    }

    private val locationListener = object : LocationListener {
        override fun onLocationChanged(location: Location) {
            updateLocation(location)
        }
        override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}
        override fun onProviderEnabled(provider: String) {}
        override fun onProviderDisabled(provider: String) {}
    }

    fun isSystemLocationEnabled(context: Context): Boolean {
        return try {
            val lm = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
            lm.isProviderEnabled(LocationManager.GPS_PROVIDER) || lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
        } catch (e: Exception) {
            false
        }
    }

    fun promptTurnOnSystemLocation(context: Context) {
        try {
            val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 2000L).build()
            val builder = LocationSettingsRequest.Builder().addLocationRequest(locationRequest)
            val client = LocationServices.getSettingsClient(context)
            client.checkLocationSettings(builder.build()).addOnFailureListener { exception ->
                if (exception is ResolvableApiException && context is Activity) {
                    try {
                        exception.startResolutionForResult(context, 1001)
                    } catch (e: Exception) {}
                }
            }
        } catch (e: Exception) {}
    }

    fun startActiveTracking(context: Context) {
        val hasFine = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        val hasCoarse = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
        if (!hasFine && !hasCoarse) return

        if (!isSystemLocationEnabled(context)) {
            promptTurnOnSystemLocation(context)
        }

        try {
            val fusedClient = LocationServices.getFusedLocationProviderClient(context)
            fusedClient.lastLocation.addOnSuccessListener { loc -> updateLocation(loc) }
            fusedClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, CancellationTokenSource().token)
                .addOnSuccessListener { loc -> updateLocation(loc) }
            fusedClient.getCurrentLocation(Priority.PRIORITY_BALANCED_POWER_ACCURACY, CancellationTokenSource().token)
                .addOnSuccessListener { loc -> updateLocation(loc) }
        } catch (e: Exception) {}

        if (isTracking) return

        try {
            val lm = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
            for (provider in lm.getProviders(true)) {
                val cached = try { lm.getLastKnownLocation(provider) } catch (e: Exception) { null }
                updateLocation(cached)
            }

            if (lm.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                try { lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 2000L, 1f, locationListener, Looper.getMainLooper()) } catch (e: Exception) {}
            }
            if (lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                try { lm.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 2000L, 1f, locationListener, Looper.getMainLooper()) } catch (e: Exception) {}
            }
            if (lm.isProviderEnabled(LocationManager.PASSIVE_PROVIDER)) {
                try { lm.requestLocationUpdates(LocationManager.PASSIVE_PROVIDER, 2000L, 1f, locationListener, Looper.getMainLooper()) } catch (e: Exception) {}
            }
            isTracking = true
        } catch (e: Exception) {}
    }

    fun getExactLocationAsync(context: Context, onLocationAcquired: (String, Location?) -> Unit) {
        startActiveTracking(context)

        if (!isSystemLocationEnabled(context)) {
            promptTurnOnSystemLocation(context)
        }

        val hasFine = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        val hasCoarse = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
        if (!hasFine && !hasCoarse) {
            val loc = latestLocation
            val link = if (loc != null && loc.latitude != 0.0) "https://maps.google.com/?q=${loc.latitude},${loc.longitude}" else "UNAVAILABLE"
            onLocationAcquired(link, loc)
            return
        }

        var isResolved = false
        val handler = Handler(Looper.getMainLooper())

        val resolveNow: (Location?) -> Unit = { loc ->
            synchronized(this) {
                if (!isResolved) {
                    isResolved = true
                    updateLocation(loc)
                    val best = latestLocation
                    val link = if (best != null && best.latitude != 0.0) {
                        "https://maps.google.com/?q=${best.latitude},${best.longitude}"
                    } else {
                        "UNAVAILABLE"
                    }
                    onLocationAcquired(link, best)
                }
            }
        }

        try {
            val fusedClient = LocationServices.getFusedLocationProviderClient(context)
            fusedClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, CancellationTokenSource().token)
                .addOnSuccessListener { loc ->
                    if (loc != null && loc.latitude != 0.0) {
                        updateLocation(loc)
                        resolveNow(loc)
                    }
                }
                .addOnFailureListener {
                    fusedClient.getCurrentLocation(Priority.PRIORITY_BALANCED_POWER_ACCURACY, CancellationTokenSource().token)
                        .addOnSuccessListener { l2 ->
                            if (l2 != null && l2.latitude != 0.0) {
                                updateLocation(l2)
                                resolveNow(l2)
                            } else {
                                resolveNow(latestLocation)
                            }
                        }
                        .addOnFailureListener {
                            resolveNow(latestLocation)
                        }
                }
        } catch (e: Exception) {}

        try {
            val lm = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
                val provider = if (lm.isProviderEnabled(LocationManager.GPS_PROVIDER)) LocationManager.GPS_PROVIDER else LocationManager.NETWORK_PROVIDER
                lm.getCurrentLocation(provider, android.os.CancellationSignal(), context.mainExecutor) { loc ->
                    if (loc != null && loc.latitude != 0.0) {
                        updateLocation(loc)
                        resolveNow(loc)
                    }
                }
            }
        } catch (e: Exception) {}

        handler.postDelayed({
            if (!isResolved) {
                resolveNow(latestLocation)
            }
        }, 6000L)
    }

    fun getBestAvailableCoordinates(context: Context): String {
        startActiveTracking(context)
        val loc = latestLocation
        return if (loc != null && loc.latitude != 0.0) {
            "https://maps.google.com/?q=${loc.latitude},${loc.longitude}"
        } else {
            try {
                val fusedClient = LocationServices.getFusedLocationProviderClient(context)
                fusedClient.lastLocation.addOnSuccessListener { l -> updateLocation(l) }
                val lm = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
                for (provider in lm.getProviders(true)) {
                    val l = try { lm.getLastKnownLocation(provider) } catch (e: Exception) { null }
                    updateLocation(l)
                }
            } catch (e: Exception) {}
            val best = latestLocation
            if (best != null && best.latitude != 0.0) {
                "https://maps.google.com/?q=${best.latitude},${best.longitude}"
            } else {
                "UNAVAILABLE"
            }
        }
    }

    fun requestHighPrecisionFollowUp(context: Context, onNewAccurateLocation: (Location) -> Unit) {
        val hasFine = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        val hasCoarse = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
        if (!hasFine && !hasCoarse) return

        try {
            val fusedClient = LocationServices.getFusedLocationProviderClient(context)
            fusedClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, CancellationTokenSource().token)
                .addOnSuccessListener { loc ->
                    if (loc != null && loc.latitude != 0.0) {
                        updateLocation(loc)
                        onNewAccurateLocation(loc)
                    }
                }
        } catch (e: Exception) {}

        try {
            val lm = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
                val provider = if (lm.isProviderEnabled(LocationManager.GPS_PROVIDER)) LocationManager.GPS_PROVIDER else LocationManager.NETWORK_PROVIDER
                lm.getCurrentLocation(provider, android.os.CancellationSignal(), context.mainExecutor) { loc ->
                    if (loc != null && loc.latitude != 0.0) {
                        updateLocation(loc)
                        onNewAccurateLocation(loc)
                    }
                }
            } else {
                val singleListener = object : LocationListener {
                    override fun onLocationChanged(location: Location) {
                        if (location.latitude != 0.0) {
                            updateLocation(location)
                            onNewAccurateLocation(location)
                        }
                        try { lm.removeUpdates(this) } catch (e: Exception) {}
                    }
                    override fun onStatusChanged(p: String?, s: Int, e: Bundle?) {}
                    override fun onProviderEnabled(p: String) {}
                    override fun onProviderDisabled(p: String) {}
                }
                if (lm.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                    lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0L, 0f, singleListener, Looper.getMainLooper())
                }
                if (lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                    lm.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0L, 0f, singleListener, Looper.getMainLooper())
                }
                Handler(Looper.getMainLooper()).postDelayed({
                    try { lm.removeUpdates(singleListener) } catch (e: Exception) {}
                }, 10000L)
            }
        } catch (e: Exception) {}
    }
}
