package com.alaa.mohamedabdulazim.viewmodel

import android.app.Application
import android.content.Context
import android.hardware.*
import androidx.lifecycle.AndroidViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlin.math.*

class QiblaViewModel(app: Application) : AndroidViewModel(app), SensorEventListener {

    private val sensorManager = app.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
    private val magnetometer  = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)

    private val _azimuth       = MutableStateFlow(0f)
    val azimuth: StateFlow<Float> = _azimuth

    private val _qiblaAngle    = MutableStateFlow(0f)
    val qiblaAngle: StateFlow<Float> = _qiblaAngle

    private val _hasCompass    = MutableStateFlow(true)
    val hasCompass: StateFlow<Boolean> = _hasCompass

    private val gravity    = FloatArray(3)
    private val geomagnetic = FloatArray(3)
    private val alpha = 0.97f

    // Kaaba coordinates
    private val KAABA_LAT = 21.4225
    private val KAABA_LNG = 39.8262

    fun startListening(lat: Double, lng: Double) {
        if (accelerometer == null || magnetometer == null) {
            _hasCompass.value = false; return
        }
        calculateQiblaAngle(lat, lng)
        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_UI)
        sensorManager.registerListener(this, magnetometer,  SensorManager.SENSOR_DELAY_UI)
    }

    fun stopListening() {
        sensorManager.unregisterListener(this)
    }

    private fun calculateQiblaAngle(lat: Double, lng: Double) {
        val lat1 = Math.toRadians(lat)
        val lat2 = Math.toRadians(KAABA_LAT)
        val dLng = Math.toRadians(KAABA_LNG - lng)
        val x = sin(dLng) * cos(lat2)
        val y = cos(lat1) * sin(lat2) - sin(lat1) * cos(lat2) * cos(dLng)
        val bearing = Math.toDegrees(atan2(x, y)).toFloat()
        _qiblaAngle.value = (bearing + 360) % 360
    }

    override fun onSensorChanged(event: SensorEvent?) {
        event ?: return
        when (event.sensor.type) {
            Sensor.TYPE_ACCELEROMETER -> {
                gravity[0] = alpha * gravity[0] + (1 - alpha) * event.values[0]
                gravity[1] = alpha * gravity[1] + (1 - alpha) * event.values[1]
                gravity[2] = alpha * gravity[2] + (1 - alpha) * event.values[2]
            }
            Sensor.TYPE_MAGNETIC_FIELD -> {
                geomagnetic[0] = alpha * geomagnetic[0] + (1 - alpha) * event.values[0]
                geomagnetic[1] = alpha * geomagnetic[1] + (1 - alpha) * event.values[1]
                geomagnetic[2] = alpha * geomagnetic[2] + (1 - alpha) * event.values[2]
            }
        }
        val R = FloatArray(9); val I = FloatArray(9)
        if (SensorManager.getRotationMatrix(R, I, gravity, geomagnetic)) {
            val orientation = FloatArray(3)
            SensorManager.getOrientation(R, orientation)
            _azimuth.value = ((Math.toDegrees(orientation[0].toDouble()) + 360) % 360).toFloat()
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    override fun onCleared() { stopListening(); super.onCleared() }
}
