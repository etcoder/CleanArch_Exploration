package com.trudell.arcgis.explore.ui.map

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.ExperimentalLifecycleComposeApi
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.trudell.arcgis.explore.ui.home.*

@OptIn(ExperimentalLifecycleComposeApi::class)
@Composable
fun MapRoute ( exploreViewModel: ExploreViewModel, navController: NavHostController) {

    val uiState by exploreViewModel.uiState.collectAsStateWithLifecycle()

    MapScreen(uiState, onBack = { navController.popBackStack() })
}