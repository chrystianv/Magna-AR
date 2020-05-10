package net.vieyrasoftware.net.physicstoolboxfieldvisualizer.android.activities.settings

import android.os.Bundle
import android.view.Window
import androidx.appcompat.app.AppCompatActivity
import net.vieyrasoftware.net.physicstoolboxfieldvisualizer.android.R

class SettingsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(R.layout.activity_settings)
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                    .add(R.id.settings_activity_root, SettingsFragment(), "settingsFrag")
                    .commit()
        }
    }
}