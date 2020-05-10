package net.vieyrasoftware.net.physicstoolboxfieldvisualizer.android.model.actors.wirewithcurrent.views

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatSeekBar
import androidx.fragment.app.Fragment
import net.vieyrasoftware.net.physicstoolboxfieldvisualizer.android.R
import net.vieyrasoftware.net.physicstoolboxfieldvisualizer.android.model.actors.wirewithcurrent.model.WireWithCurrentActor

class WireWithCurrentFragment : Fragment() {
    // Data
    private val actor: WireWithCurrentActor? = null
    // Views
    private var iSeek: AppCompatSeekBar? = null
    private var muSeek: AppCompatSeekBar? = null
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, instance: Bundle?): View? {
        val render = inflater.inflate(R.layout.fragment_wire_with_current_actor_params, container, false)
        iSeek = render.findViewById(R.id.wireWithCurrentFragmentISeekBar)
        muSeek = render.findViewById(R.id.wireWithCurrentFragmentMuSeekBar)
        return render
    }

    private fun bindData(actor: WireWithCurrentActor) {}
}