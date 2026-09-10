package net.vieyrasoftware.net.physicstoolboxfieldvisualizer.android.activities.settings

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AppSettingsAlt
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Forum
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.preference.PreferenceManager
import net.vieyrasoftware.net.physicstoolboxfieldvisualizer.android.ClickableSettingItem
import net.vieyrasoftware.net.physicstoolboxfieldvisualizer.android.FeedbackEmail
import net.vieyrasoftware.net.physicstoolboxfieldvisualizer.android.R
import net.vieyrasoftware.net.physicstoolboxfieldvisualizer.android.SettingsSection
import net.vieyrasoftware.net.physicstoolboxfieldvisualizer.android.SwitchSettingItem
import net.vieyrasoftware.net.physicstoolboxfieldvisualizer.android.ui.theme.SettingsTheme

/**
 * Material 3 Compose settings for Magna-AR, styled with the shared
 * SettingsTheme + SettingsComponents so it matches the other tool
 * settings screens. Preference keys are unchanged.
 */
class SettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Set default preferences if they don't exist
        val prefs = PreferenceManager.getDefaultSharedPreferences(this)
        if (!prefs.contains("numerical")) {
            prefs.edit().putBoolean("numerical", false).apply()
        }
        if (!prefs.contains("heatmap_switch")) {
            prefs.edit().putBoolean("heatmap_switch", false).apply()
        }

        setContent {
            SettingsTheme {
                MagnaARSettingsScreen(
                    onNavigateBack = { finish() }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MagnaARSettingsScreen(onNavigateBack: () -> Unit) {
    val context = LocalContext.current
    val prefs = remember { PreferenceManager.getDefaultSharedPreferences(context) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        stringResource(R.string.settings),
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { onNavigateBack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.back),
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                    titleContentColor = MaterialTheme.colorScheme.onSurface,
                    navigationIconContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(padding)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp)
            ) {
                VisualizationSection(prefs = prefs)

                Spacer(modifier = Modifier.height(16.dp))

                HeatmapSection(prefs = prefs)

                Spacer(modifier = Modifier.height(16.dp))

                FeedbackSection(context = context)

                Spacer(modifier = Modifier.height(16.dp))

                AboutSection(context = context)

                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

@Composable
private fun VisualizationSection(prefs: SharedPreferences) {
    var showNumerical by remember { mutableStateOf(prefs.getBoolean("numerical", false)) }

    // Make state reactive to SharedPreferences changes
    DisposableEffect(prefs) {
        val listener = SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
            when (key) {
                "numerical" -> showNumerical = prefs.getBoolean("numerical", false)
            }
        }
        prefs.registerOnSharedPreferenceChangeListener(listener)
        onDispose {
            prefs.unregisterOnSharedPreferenceChangeListener(listener)
        }
    }

    SettingsSection(
        title = stringResource(R.string.visualization),
        icon = Icons.Default.Visibility
    ) {
        SwitchSettingItem(
            title = stringResource(R.string.numerical_value),
            description = stringResource(R.string.numeric_description),
            checked = showNumerical,
            onCheckedChange = {
                showNumerical = it
                prefs.edit().putBoolean("numerical", it).apply()
            }
        )
    }
}

@Composable
private fun HeatmapSection(prefs: SharedPreferences) {
    var heatmapEnabled by remember { mutableStateOf(prefs.getBoolean("heatmap_switch", false)) }

    // Make state reactive to SharedPreferences changes
    DisposableEffect(prefs) {
        val listener = SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
            if (key == "heatmap_switch") {
                heatmapEnabled = prefs.getBoolean("heatmap_switch", false)
            }
        }
        prefs.registerOnSharedPreferenceChangeListener(listener)
        onDispose {
            prefs.unregisterOnSharedPreferenceChangeListener(listener)
        }
    }

    SettingsSection(
        title = stringResource(R.string.heatmap),
        icon = Icons.Default.Thermostat
    ) {
        SwitchSettingItem(
            title = stringResource(R.string.heatmap),
            description = stringResource(R.string.heatmap_summary),
            checked = heatmapEnabled,
            onCheckedChange = {
                heatmapEnabled = it
                prefs.edit().putBoolean("heatmap_switch", it).apply()
            }
        )
    }
}

@Composable
private fun FeedbackSection(context: Context) {
    SettingsSection(
        title = stringResource(R.string.feedback),
        icon = Icons.Default.Forum
    ) {
        ClickableSettingItem(
            title = stringResource(R.string.emaildeveloper),
            description = "support@vieyrasoftware.net",
            icon = Icons.Default.Email,
            onClick = {
                FeedbackEmail.send(context, "Magna-AR")
            }
        )

        ClickableSettingItem(
            title = stringResource(R.string.usage_resources),
            description = "www.magna-ar.net",
            icon = Icons.Default.Language,
            onClick = {
                val url = "https://www.magna-ar.net/"
                val intent = Intent(Intent.ACTION_VIEW).apply {
                    data = Uri.parse(url)
                }
                context.startActivity(intent)
            }
        )
    }
}

@Composable
private fun AboutSection(context: Context) {
    val versionName = remember {
        try {
            context.packageManager.getPackageInfo(context.packageName, 0).versionName
        } catch (e: PackageManager.NameNotFoundException) {
            null
        }
    }

    SettingsSection(
        title = stringResource(R.string.about),
        icon = Icons.Default.Info
    ) {
        // Version info (non-clickable)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.AppSettingsAlt,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(
                    text = stringResource(R.string.version),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = versionName ?: stringResource(R.string.unknown),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        ClickableSettingItem(
            title = stringResource(R.string.meet_team),
            description = stringResource(R.string.meet_development_team_description),
            icon = Icons.Default.Group,
            onClick = {
                context.startActivity(Intent(context, ProjectTeamActivity::class.java))
            }
        )
    }
}
