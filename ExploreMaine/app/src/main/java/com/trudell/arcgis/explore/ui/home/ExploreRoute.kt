package com.trudell.arcgis.explore.ui.home

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.ExperimentalLifecycleComposeApi
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.trudell.arcgis.explore.data.MapAreaInfo
import com.trudell.arcgis.explore.navigation.ExploreDestinations

@OptIn(ExperimentalLifecycleComposeApi::class)
@Composable
fun ExploreRoute(exploreViewModel: ExploreViewModel, navController: NavHostController) {

    val uiState by exploreViewModel.uiState.collectAsStateWithLifecycle()

    ExploreRoute(
        uiState = uiState,
        onMapSelected = {
            exploreViewModel.clearSelectedMapArea()
            navController.navigate(ExploreDestinations.MAP_ROUTE)
        },
        onMapAreaSelected = {
            exploreViewModel.selectMapArea(it)
            navController.navigate(ExploreDestinations.MAP_ROUTE)
        },
        onDownloadMapArea = {
            exploreViewModel.downloadMapArea(it)
        },
        onDeleteMapArea = {
            exploreViewModel.deleteDownloadedMapArea(it)
        }
    )

}

@Composable
fun ExploreRoute(
    uiState: ExploreUiState,
    onMapSelected: () -> Unit,
    onMapAreaSelected: (mapArea: MapAreaInfo) -> Unit,
    onDownloadMapArea: (mapArea: MapAreaInfo) -> Unit,
    onDeleteMapArea: (mapArea: MapAreaInfo) -> Unit
) {
    ExploreScreen(
        uiState,
        onMapSelected = onMapSelected,
        onMapAreaSelected = onMapAreaSelected,
        onDownloadMapArea = onDownloadMapArea,
        onDeleteMapArea = onDeleteMapArea
    )
}