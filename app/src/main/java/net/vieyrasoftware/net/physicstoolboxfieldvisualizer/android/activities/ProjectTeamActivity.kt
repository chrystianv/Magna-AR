package net.vieyrasoftware.net.physicstoolboxfieldvisualizer.android.activities

import android.os.Bundle
import android.text.Html
import android.text.Spannable
import android.text.TextPaint
import android.text.method.LinkMovementMethod
import android.text.style.URLSpan
import android.text.style.UnderlineSpan
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import net.vieyrasoftware.net.physicstoolboxfieldvisualizer.android.R


class ProjectTeamActivity : AppCompatActivity() {
    private lateinit var firstLink: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_team_page)

        val teamDescription = Html.fromHtml(getString(R.string.team_text_desc)) as Spannable
        for (urlSpan in teamDescription.getSpans(0, teamDescription.length, URLSpan::class.java)) {
            teamDescription.setSpan(object : UnderlineSpan() {
                override fun updateDrawState(tp: TextPaint) {
                    tp.isUnderlineText = false
                }
            }, teamDescription.getSpanStart(urlSpan), teamDescription.getSpanEnd(urlSpan), 0)
        }
        firstLink = findViewById(R.id.company_page_link)
        firstLink.text = teamDescription
        firstLink.movementMethod = LinkMovementMethod.getInstance()
    }
}