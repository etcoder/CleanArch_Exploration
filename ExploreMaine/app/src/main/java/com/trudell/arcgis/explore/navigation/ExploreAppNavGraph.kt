package com.trudell.arcgis.explore.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.trudell.arcgis.explore.data.AppContainer
import com.trudell.arcgis.explore.ui.home.ExploreRoute
import com.trudell.arcgis.explore.ui.home.ExploreViewModel
import com.trudell.arcgis.explore.ui.map.MapRoute

object ExploreDestinations {
    const val HOME_ROUTE = "explore"
    const val MAP_ROUTE = "map"
}

@Composable
fun ExploreNavGraph(
    appContainer: AppContainer,
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
    startDestination: String = ExploreDestinations.HOME_ROUTE
) {
    // We are going to use the same viewmodel for both pages to keep track
    // of the selected map area to display
    val exploreViewModel: ExploreViewModel = viewModel(
        factory = ExploreViewModel.provideFactory(appContainer.mapRepository)
    )

    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier
    ) {
        composable(ExploreDestinations.HOME_ROUTE) {
            ExploreRoute(exploreViewModel = exploreViewModel, navController = navController)
        }

        composable(ExploreDestinations.MAP_ROUTE) {
            MapRoute(exploreViewModel = exploreViewModel, navController = navController)
        }
    }
}