package net.vieyrasoftware.net.physicstoolboxfieldvisualizer.android.activities.onboarding

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment

import com.github.paolorotolo.appintro.AppIntro
import net.vieyrasoftware.net.physicstoolboxfieldvisualizer.android.R
import net.vieyrasoftware.net.physicstoolboxfieldvisualizer.android.activities.VisualizerActivity

class OnBoardingActivity : AppIntro() {
    class Slide(private val layoutId: Int) : Fragment() {
        override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
            return inflater.inflate(
                    layoutId,
                    container,
                    false
            )
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        arrayOf(
                R.layout.fragment_intro_slide,
                R.layout.fragment_suggested_orientation_slide,
                R.layout.fragment_compass_slide,
                R.layout.fragment_video_recording_slide
        ).forEach {
            addSlide(Slide(it))
        }

        setBarColor(Color.parseColor("#d32f2f"))
        setSeparatorColor(Color.parseColor("#2196F3"))
        // Put this method in init()
        setFadeAnimation()
    }

    private fun loadMainActivity() {
        val intent = Intent(this, VisualizerActivity::class.java)
        startActivity(intent)
        finish()
    }

    override fun onSkipPressed() {
        loadMainActivity()
    }

    override fun onDonePressed() {
        loadMainActivity()
    }
}
