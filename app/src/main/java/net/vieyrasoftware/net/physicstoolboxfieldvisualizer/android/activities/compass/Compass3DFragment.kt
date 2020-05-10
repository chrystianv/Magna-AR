package net.vieyrasoftware.net.physicstoolboxfieldvisualizer.android.activities.compass

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProviders
import com.google.ar.sceneform.Node
import com.google.ar.sceneform.SceneView
import com.google.ar.sceneform.math.Quaternion
import com.google.ar.sceneform.math.Vector3
import com.google.ar.sceneform.rendering.Color
import com.google.ar.sceneform.rendering.Material
import com.google.ar.sceneform.rendering.MaterialFactory
import com.google.ar.sceneform.rendering.ModelRenderable
import com.google.ar.schemas.lull.Quat
import net.vieyrasoftware.net.physicstoolboxfieldvisualizer.android.R
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin

class Compass3DFragment : Fragment() {
    companion object {
        fun newInstance() = Compass3DFragment()
    }

    private lateinit var compassData: CompassViewModel
    private lateinit var compassRenderView: SceneView
    private val compassArrowNode = Node()

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_compass3d, container, false)
        compassRenderView = view.findViewById(R.id.compass_display)
        display()
        return view
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        compassData = ViewModelProviders.of(this).get(CompassViewModel::class.java)
        compassData.getMagneticFieldLiveData().observe(this, Observer {
            // What is up?
            val desiredLookAt = Vector3(it[0], it[1], it[2]).normalized()
            val THRESHOLD = 0.9f;
            val upVec = if (abs(Vector3.dot(desiredLookAt, Vector3.up())) < THRESHOLD) {
                Vector3.up()
            } else {
                Vector3.right()
            }
            compassArrowNode.setLookDirection(
                    desiredLookAt, upVec
            )
        })
    }

    override fun onResume() {
        super.onResume()
        compassRenderView.resume()
    }

    override fun onPause() {
        super.onPause()
        compassRenderView.pause()
    }

    private fun display() {
        compassRenderView.scene.addChild(compassArrowNode)
        val modelNode = Node()
        compassArrowNode.addChild(modelNode)
        compassArrowNode.apply {
            localPosition = Vector3(0f, 0f, -20f)
        }
        ModelRenderable.builder()
                .setSource(this.context, Uri.parse("arrow.sfb"))
                .build()
                .thenCombine(MaterialFactory.makeOpaqueWithColor(this.context, Color(1f, 0f, 0f))) { m: ModelRenderable, mat: Material? ->
                    m.material = mat
                    m
                }
                .thenAccept { createdRenderable: ModelRenderable? ->
                    modelNode.apply {
                        renderable = createdRenderable
                        localRotation = Quaternion.axisAngle(Vector3.right(), 90f)
                        localPosition = Vector3(0f, 0f, -7.5f)
                        localScale = Vector3(1f, 1f, 1f).scaled(10f)
                    }
                }
    }
}