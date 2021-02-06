package io.github.kaczmarek.sensoranimation.ui

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.annotation.StringRes
import androidx.appcompat.app.AlertDialog
import io.github.kaczmarek.sensoranimation.R
import io.github.kaczmarek.sensoranimation.utils.star.SensorManagerException
import io.github.kaczmarek.sensoranimation.utils.star.TiltSensorException

class MainActivity : AppCompatActivity() {

    private var errorDialog: AlertDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        try {
            checkSensorManagerAndTiltSensor()
        } catch (e: Exception) {
            Log.e(TAG, e.message.toString())
            when (e) {
                is TiltSensorException -> showErrorDialog(R.string.error_description_tilt_sensor_not_found)
                is SensorManagerException -> showErrorDialog(R.string.error_description_sensor_manager_not_found)
            }
        }
    }

    /**
     * Метод для демонстранции AlertDialog при получении ошибки
     * @param description - строкой ресурс для описания в AlertDialog
     */
    private fun showErrorDialog(@StringRes description: Int) {
        if (errorDialog?.isShowing != true) {
            errorDialog = AlertDialog.Builder(this, R.style.ErrorAlertDialogTheme).apply {
                setTitle(R.string.error_title)
                setMessage(description)
                setPositiveButton(R.string.error_positive_action) { _, _ -> errorDialog?.dismiss() }
                setIcon(R.drawable.ic_sad)
            }.show()
        }
    }

    /**
     * Метод для проверки работоспособности SensorManager и сенсора наклона
     */
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