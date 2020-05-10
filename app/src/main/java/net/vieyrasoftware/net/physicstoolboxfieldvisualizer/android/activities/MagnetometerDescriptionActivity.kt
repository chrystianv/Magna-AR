package net.vieyrasoftware.net.physicstoolboxfieldvisualizer.android.activities

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorManager
import android.os.Bundle
import android.text.Html
import android.text.Spannable
import android.text.TextPaint
import android.text.method.LinkMovementMethod
import android.text.style.URLSpan
import android.text.style.UnderlineSpan
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import net.vieyrasoftware.net.physicstoolboxfieldvisualizer.android.R

class MagnetometerDescriptionActivity : AppCompatActivity() {
    private lateinit var sensorName: String
    private lateinit var vendorName: String
    internal var powerValue: Float = 0.toFloat()
    internal var maximumRangeValue: Float = 0.toFloat()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_magnetometer_description)

        val firstLink = findViewById<View>(R.id.textView16) as TextView

        val s = Html.fromHtml(getString(R.string.magnetometer_text_desc)) as Spannable
        for (u in s.getSpans(0, s.length, URLSpan::class.java)) {
            s.setSpan(object : UnderlineSpan() {
                override fun updateDrawState(tp: TextPaint) {
                    tp.isUnderlineText = false
                }
            }, s.getSpanStart(u), s.getSpanEnd(u), 0)
        }
        firstLink.text = s
        firstLink.movementMethod = LinkMovementMethod.getInstance()

        val sm = Context.SENSOR_SERVICE
        val sensorManager = getSystemService(sm) as SensorManager

        // gForceDescription
        val someSensor = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)

        val sensorNameTextView: TextView = findViewById(R.id.sensorName)

        val vendorTextView: TextView = findViewById(R.id.vendor)

        // Device may not have sensor
        if (someSensor != null) {
            sensorName = someSensor.name
            vendorName = someSensor.vendor

            sensorNameTextView.text = getString(R.string.sensor_name) + " " + sensorName
            vendorTextView.text = getString(R.string.vendor) + ": " + vendorName
        }
    }
}


