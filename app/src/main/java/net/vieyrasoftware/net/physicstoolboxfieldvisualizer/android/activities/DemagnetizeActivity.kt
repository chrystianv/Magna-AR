package net.vieyrasoftware.net.physicstoolboxfieldvisualizer.android.activities

import android.graphics.Matrix
import android.os.Bundle
import android.util.DisplayMetrics
import android.util.Size
import android.view.Surface
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.camera.core.*
import kotlinx.android.synthetic.main.activity_demagnetize.*
import net.vieyrasoftware.net.physicstoolboxfieldvisualizer.android.R

class DemagnetizeActivity : AppCompatActivity() {
    private lateinit var backButton: Button
    private lateinit var nextButton: Button
    private lateinit var textView: TextView

    var counter = 0

    private var lensFacing = CameraX.LensFacing.BACK
    private val TAG = "MainActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_demagnetize)

        texture.post { startCamera() }

        // Every time the provided texture view changes, recompute layout
        texture.addOnLayoutChangeListener { _, _, _, _, _, _, _, _, _ ->
            updateTransform()

            textView = findViewById(R.id.text)
            nextButton = findViewById(R.id.btnnext)

            backButton = findViewById(R.id.btnBack)

            nextButton.setOnClickListener {
                counter++

                if (counter == 0) {
                    textView.setText(R.string.demagnetize_1)
                    backButton.visibility = View.INVISIBLE
                }

                if (counter == 1) {
                    textView.setText(R.string.demagnetize_2)
                    backButton.visibility = View.VISIBLE
                    nextButton.text = "DONE"
                }


                if (counter == 2) {
                    finish()
                }
            }

            backButton.setOnClickListener {
                counter--

                if (counter <= 0) {
                    textView.setText(R.string.demagnetize_1)
                    backButton.visibility = View.INVISIBLE
                    nextButton.text = "NEXT"
                }

                if (counter == 1) {
                    textView.setText(R.string.demagnetize_2)
                    backButton.visibility = View.VISIBLE
                    nextButton.text = "DONE"
                }
            }
        }

        bindViews()
    }

    private fun startCamera() {
        val metrics = DisplayMetrics().also { texture.display.getRealMetrics(it) }
        val screenSize = Size(metrics.widthPixels, metrics.heightPixels)

        val previewConfig = PreviewConfig.Builder().apply {
            setLensFacing(lensFacing)
            setTargetResolution(screenSize)
            setTargetRotation(windowManager.defaultDisplay.rotation)
            setTargetRotation(texture.display.rotation)
        }.build()

        val preview = Preview(previewConfig)
        preview.setOnPreviewOutputUpdateListener {
            texture.surfaceTexture = it.surfaceTexture
            updateTransform()
        }

        // Create configuration object for the image capture use case
        val imageCaptureConfig = ImageCaptureConfig.Builder()
                .apply {
                    setLensFacing(lensFacing)
                    setTargetRotation(texture.display.rotation)
                    setCaptureMode(ImageCapture.CaptureMode.MAX_QUALITY)
                }.build()

        val imageCapture = ImageCapture(imageCaptureConfig)

        CameraX.bindToLifecycle(this, preview, imageCapture)
    }

    private fun updateTransform() {
        val matrix = Matrix()
        val centerX = texture.width / 2f
        val centerY = texture.height / 2f

        val rotationDegrees = when (texture.display.rotation) {
            Surface.ROTATION_0 -> 0
            Surface.ROTATION_90 -> 90
            Surface.ROTATION_180 -> 180
            Surface.ROTATION_270 -> 270
            else -> return
        }
        matrix.postRotate(-rotationDegrees.toFloat(), centerX, centerY)
        texture.setTransform(matrix)
    }

    private fun bindViews() {
        // Toolbar + Layout
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
    }

    override fun onBackPressed() {
        moveTaskToBack(true)
    }
}
