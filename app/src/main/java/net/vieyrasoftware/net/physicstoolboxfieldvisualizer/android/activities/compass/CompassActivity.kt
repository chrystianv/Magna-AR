package net.vieyrasoftware.net.physicstoolboxfieldvisualizer.android.activities.compass

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import net.vieyrasoftware.net.physicstoolboxfieldvisualizer.android.R

class CompassActivity : AppCompatActivity() {
    enum class CompassType {
        TWO_DIMENSIONAL,
        THREE_DIMENSIONAL
    }
    private lateinit var toggleCompassButton: Button
    private lateinit var currentCompass: CompassType

    private fun bindViews() {
        toggleCompassButton = findViewById(R.id.compass_change_button)
    }

    private fun changeCompassMode() {
        currentCompass = when (currentCompass) {
            CompassType.TWO_DIMENSIONAL -> CompassType.THREE_DIMENSIONAL
            CompassType.THREE_DIMENSIONAL -> CompassType.TWO_DIMENSIONAL
        }
        supportFragmentManager
                .beginTransaction()
                .replace(R.id.compass_container, when (currentCompass) {
                    CompassType.TWO_DIMENSIONAL -> CompassFragment.newInstance()
                    CompassType.THREE_DIMENSIONAL -> Compass3DFragment.newInstance()
                })
                .commitNow()
    }

    private fun loadFragments(instanceState: Bundle?) {
        if (instanceState == null) {
            supportFragmentManager.beginTransaction()
                    .replace(R.id.compass_container, CompassFragment.newInstance())
                    .commitNow()
            currentCompass = CompassType.TWO_DIMENSIONAL
        }
    }

    private fun bindListeners() {
        toggleCompassButton.setOnClickListener { this.changeCompassMode() }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_compass)

        bindViews()
        loadFragments(savedInstanceState)
        bindListeners()
    }
}