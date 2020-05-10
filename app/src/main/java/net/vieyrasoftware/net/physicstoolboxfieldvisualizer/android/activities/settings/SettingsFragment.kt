package net.vieyrasoftware.net.physicstoolboxfieldvisualizer.android.activities.settings

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.preference.CheckBoxPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.google.android.gms.oss.licenses.OssLicensesMenuActivity
import net.vieyrasoftware.net.physicstoolboxfieldvisualizer.android.R
import net.vieyrasoftware.net.physicstoolboxfieldvisualizer.android.activities.DemagnetizeActivity
import net.vieyrasoftware.net.physicstoolboxfieldvisualizer.android.activities.onboarding.OnBoardingActivity
import net.vieyrasoftware.net.physicstoolboxfieldvisualizer.android.activities.ProjectTeamActivity

class SettingsFragment : PreferenceFragmentCompat() {
    private var spheres: CheckBoxPreference? = null
    private var arrows: CheckBoxPreference? = null

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        addPreferencesFromResource(R.xml.pref_general)

        /* ***** Application ***** */
        spheres = findPreference("spheres")
        arrows = findPreference("arrows")
        Log.d("SettingsActivity", spheres.toString())
        Log.d("SettingsActivity", arrows.toString())
        spheres!!.onPreferenceChangeListener = Preference.OnPreferenceChangeListener { _: Preference?, _: Any? ->
            spheres!!.isChecked = true
            arrows!!.isChecked = false
            true
        }
        arrows!!.onPreferenceChangeListener = Preference.OnPreferenceChangeListener { _: Preference?, _: Any? ->
            spheres!!.isChecked = false
            arrows!!.isChecked = true
            true
        }

        val demagnetizePref: Preference? = findPreference("demagnetize_device")
        demagnetizePref!!.onPreferenceClickListener = Preference.OnPreferenceClickListener {
            startActivity(Intent(activity, DemagnetizeActivity::class.java))
            true
        }

        /* ***** Contact **** */
        val emailPref: Preference? = findPreference("email_developer")
        emailPref!!.onPreferenceClickListener = Preference.OnPreferenceClickListener {
            val sharingIntent = Intent(Intent.ACTION_SEND)
            sharingIntent.type = "plain/text"
            sharingIntent.putExtra(Intent.EXTRA_EMAIL, arrayOf("support@vieyrasoftware.net"))
            sharingIntent.putExtra(Intent.EXTRA_SUBJECT, "Magna AR")
            //	sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, xEvent.toString());
            startActivity(Intent.createChooser(sharingIntent, "Share via"))
            true
        }
        val webBrowser: Preference? = findPreference("website")
        webBrowser!!.onPreferenceClickListener = Preference.OnPreferenceClickListener {
            val url = "https://www.magna-ar.net/"
            val i = Intent(Intent.ACTION_VIEW)
            i.data = Uri.parse(url)
            startActivity(i)
            true
        }

        /* ******* License and info ******* */
        val meetTeam: Preference? = findPreference("team")
        meetTeam!!.onPreferenceClickListener = Preference.OnPreferenceClickListener {
            startActivity(Intent(context, ProjectTeamActivity::class.java))
            true
        }
        val licenses: Preference? = findPreference("licenses")
        licenses!!.onPreferenceClickListener = Preference.OnPreferenceClickListener {
            // When the user selects an option to see the licenses:
            startActivity(Intent(context, OssLicensesMenuActivity::class.java))
            true
        }

        val totalIntensityMap: Preference? = findPreference("mapone")
        totalIntensityMap!!.onPreferenceClickListener = Preference.OnPreferenceClickListener {
            val url = "https://www.ngdc.noaa.gov/geomag/WMM/data/WMM2015/WMM2015v2_F_MERC.pdf"
            val i = Intent(Intent.ACTION_VIEW)
            i.data = Uri.parse(url)
            startActivity(i)
            true
        }
        val totalInclinationMap: Preference? = findPreference("maptwo")
        totalInclinationMap!!.onPreferenceClickListener = Preference.OnPreferenceClickListener {
            val url = "https://www.ngdc.noaa.gov/geomag/WMM/data/WMM2015/WMM2015v2_I_MERC.pdf"
            val i = Intent(Intent.ACTION_VIEW)
            i.data = Uri.parse(url)
            startActivity(i)
            true
        }

        val tutorial: Preference? = findPreference("tutorial")
        tutorial!!.onPreferenceClickListener = Preference.OnPreferenceClickListener {
            // When the user selects an option to see the licenses:
            startActivity(Intent(context, OnBoardingActivity::class.java))
            true
        }
        val tutorialVideo: Preference? = findPreference("tutorialvideo")
        tutorialVideo!!.onPreferenceClickListener = Preference.OnPreferenceClickListener {
            val url = "https://www.youtube.com/watch?v=OEXWNcdImsc&t"
            val i = Intent(Intent.ACTION_VIEW)
            i.data = Uri.parse(url)
            startActivity(i)
            true
        }
    }
}
