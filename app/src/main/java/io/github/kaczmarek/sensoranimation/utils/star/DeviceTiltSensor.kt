package io.github.kaczmarek.sensoranimation.utils.star

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager

interface TiltListener {

    fun onTilt(pitchRoll: Pair<Double, Double>)

}

interface TiltSensor {

    fun addListener(tiltListener: TiltListener)

    fun register()

    fun unregister()

}

class DeviceTiltSensor(context: Context) : SensorEventListener, TiltSensor {

    private val rotationMatrix = FloatArray(9)
    private val orientationAngles = FloatArray(3)

    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager?
    private var rotationVectorSensor: Sensor? = null

    private var listeners = mutableListOf<TiltListener>()

    init {
        rotationVectorSensor = sensorManager?.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)
    }

    override fun onSensorChanged(event: SensorEvent?) {
        event?.let {
            if (it.sensor.type == Sensor.TYPE_ROTATION_VECTOR) {
                SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values)
                SensorManager.getOrientation(rotationMatrix, orientationAngles)
                val pair = Pair(
                        orientationAngles[1].toDouble() * -1,
                        orientationAngles[2].toDouble()
                )
                listeners.forEach { tiltListener ->
                    tiltListener.onTilt(pair)
                }
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    override fun addListener(tiltListener: TiltListener) {
        listeners.add(tiltListener)
    }

    override fun register() {
        sensorManager?.registerListener(
                this,
                rotationVectorSensor,
                SensorManager.SENSOR_DELAY_NORMAL
        )
    }

    override fun unregister() {
        listeners.clear()
        sensorManager?.unregisterListener(this, rotationVectorSensor)
    }
}

class TiltSensorException : Exception("Rotation Vector Sensor is not available")
class SensorManagerException : Exception("SensorManager not found")