package com.trudell.arcgis.explore.ui

import androidx.compose.runtime.Composable
import androidx.navigation.compose.rememberNavController
import com.trudell.arcgis.explore.data.AppContainer
import com.trudell.arcgis.explore.navigation.ExploreNavGraph
import com.trudell.arcgis.explore.ui.theme.ExploreTheme

@Composable
fun ExploreApp(appContainer: AppContainer) {

    ExploreTheme() {
        val navController = rememberNavController()

        ExploreNavGraph(
            appContainer = appContainer,
            navController = navController
        )
    }

}