package net.vieyrasoftware.net.physicstoolboxfieldvisualizer.android.activities.onboarding

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import androidx.preference.PreferenceManager
import androidx.appcompat.app.AppCompatActivity
import net.vieyrasoftware.net.physicstoolboxfieldvisualizer.android.R
import net.vieyrasoftware.net.physicstoolboxfieldvisualizer.android.activities.VisualizerActivity

class LaunchScreenActivity : AppCompatActivity() {
    companion object {
        const val SPLASH_SCREEN_DURATION = 2000L;
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        scheduleSplashScreen()
    }

    private fun scheduleSplashScreen() {
        Handler().postDelayed(
                {
                    routeToAppropriatePage()
                    finish()
                },
                SPLASH_SCREEN_DURATION
        )
    }

    private fun getSplashScreenDuration() = 2000L

    private fun routeToAppropriatePage() {
        // Fetch how many launches have occurred.
        val preferences = PreferenceManager.getDefaultSharedPreferences(this)
        val launch = preferences.getInt("launchCount", 0)

        print("Launch value: $launch")

        // Dialog only appears on first launch of the app.
        val nextActivityIntent = if (launch == 0) {
            Intent(this, OnBoardingActivity::class.java)
        } else {
            print("Not first launch.")
            Intent(this, VisualizerActivity::class.java)
        }
        startActivity(nextActivityIntent)
    }
}
