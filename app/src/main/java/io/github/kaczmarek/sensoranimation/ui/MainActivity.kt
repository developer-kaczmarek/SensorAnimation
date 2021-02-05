package io.github.kaczmarek.sensoranimation.ui

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import io.github.kaczmarek.sensoranimation.R
import io.github.kaczmarek.sensoranimation.utils.star.SensorManagerException
import io.github.kaczmarek.sensoranimation.utils.star.TiltSensorException

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        try {
            checkSensorManagerAndTiltSensor()
        } catch (e: Exception) {
            Log.e(TAG, e.message.toString())
        }
    }

    @Throws(TiltSensorException::class, SensorManagerException::class)
    private fun checkSensorManagerAndTiltSensor() {
        val sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager?
        sensorManager?.let {
            if (it.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR) == null) throw TiltSensorException()
        } ?: throw SensorManagerException()
    }

    companion object {
        const val TAG = "MainActivity"
    }
}