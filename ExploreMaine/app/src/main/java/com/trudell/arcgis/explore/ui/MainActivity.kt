package com.trudell.arcgis.explore.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.view.WindowCompat
import com.trudell.arcgis.explore.ExploreApplication

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, true)

        // Keeping this around for a hard reset if needed
        //externalCacheDir?.deleteRecursively()

        val appContainer = (application as ExploreApplication).container
        setContent {
            ExploreApp(appContainer)
        }
    }
}