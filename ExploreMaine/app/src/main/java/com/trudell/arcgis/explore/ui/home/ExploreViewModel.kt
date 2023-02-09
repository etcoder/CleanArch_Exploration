package com.trudell.arcgis.explore.ui.home

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.trudell.arcgis.explore.data.MapAreaInfo
import com.trudell.arcgis.explore.data.MapInfo
import com.trudell.arcgis.explore.data.MapRepository
import com.trudell.arcgis.explore.data.MapStatus
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

/**
 * ExploreViewModel
 *
 * This the viewmodel for the Explore activity.  Here we maintain the online map state,
 * preplanned map areas (areas of interest for the online map) and download status for
 * each of these areas.
 */

sealed interface ExploreUiState {

    val isLoading: Boolean

    data class NoMapInfo(
        override val isLoading: Boolean
    ) : ExploreUiState


    data class HasMapInfo(
        val mapInfo: MapInfo,
        val mapAreas: List<MapAreaInfo>,
        val selectedArea: MapAreaInfo?,
        override val isLoading: Boolean
    ) : ExploreUiState
}

private data class ExploreViewModelState(
    val mapInfo: MapInfo? = null,
    val mapAreas: List<MapAreaInfo> = emptyList(),
    val selectedArea: MapAreaInfo? = null,
    val isLoading: Boolean = false
) {
    fun toUiState(): ExploreUiState =
        if (mapInfo == null) {
            ExploreUiState.NoMapInfo(
                isLoading = isLoading
            )
        } else {
            ExploreUiState.HasMapInfo(
                mapInfo = mapInfo,
                mapAreas = mapAreas,
                selectedArea = selectedArea,
                isLoading = isLoading
            )
        }
}

class ExploreViewModel(private val mapRepository: MapRepository) : ViewModel() {

    private val viewModelState = MutableStateFlow(ExploreViewModelState(isLoading = true))

    // UI state exposed to the UI
    val uiState = viewModelState
        .map(ExploreViewModelState::toUiState)
        .stateIn(
            viewModelScope,
            SharingStarted.Eagerly,
            viewModelState.value.toUiState()
        )

    init {
        // Show the loading state
        viewModelState.update { it.copy(isLoading = true) }
        Log.d(this.toString(), "Loading...")

        mapRepository.createOfflineMapDirectory()

        viewModelScope.launch(Dispatchers.IO) {
            Log.d(this.toString(), "fetchOnlineMap()")
            val mapResult = mapRepository.fetchOnlineMap()
            viewModelState.update {
                it.copy(mapInfo = mapResult)
            }

            Log.d(this.toString(), "fetchAvailableMapAreas()")
            val mapAreasResult = mapRepository.fetchAvailableMapAreas()
            viewModelState.update {
                it.copy(mapAreas = mapAreasResult)
            }

            viewModelState.update { it.copy(isLoading = false) }
            Log.d(this.toString(), "Done Loading.")
        }
    }

    fun selectMapArea(mapArea: MapAreaInfo) {
        viewModelState.update {
            it.copy(selectedArea = mapArea)
        }
    }

    fun clearSelectedMapArea() {
        viewModelState.update {
            it.copy(selectedArea = null)
        }
    }

    fun downloadMapArea(mapArea: MapAreaInfo) {
        viewModelScope.launch(Dispatchers.IO) {
            var updatedMapArea = mapArea.copy(mapAreaStatus = MapStatus.DOWNLOADING)
            // Set the status to downloading
            viewModelState.update {
                it.copy(mapAreas = updateMapAreas(mapArea, updatedMapArea, it.mapAreas))
            }

            // Start downloading and update the UI when we are finished
            val downloadResult = mapRepository.downloadOfflineMapArea(mapArea)

            // Set the status when complete
            viewModelState.update {
                it.copy(mapAreas = updateMapAreas(updatedMapArea, mapArea.copy(mapAreaStatus = if(downloadResult) MapStatus.DOWNLOADED else MapStatus.DOWNLOADABLE), it.mapAreas))
            }
        }
    }

    fun deleteDownloadedMapArea(mapArea: MapAreaInfo) {
        viewModelScope.launch {
            val updatedMapArea = mapArea.copy(mapAreaStatus = MapStatus.DOWNLOADABLE)
            // Set the status to downloadable
            viewModelState.update {
                it.copy(mapAreas = updateMapAreas(mapArea, updatedMapArea, it.mapAreas))
            }
            mapRepository.deleteOfflineMapArea(mapArea)
        }
    }

    // This create a NEW list to force the UI to update the map download status
    private fun updateMapAreas(oldMapArea: MapAreaInfo, newMapArea: MapAreaInfo, oldMapAreas: List<MapAreaInfo>) : List<MapAreaInfo> {
        val mapAreasTemp: MutableList<MapAreaInfo> = mutableListOf()
        var index = oldMapAreas.indexOf(oldMapArea)
        mapAreasTemp.addAll(oldMapAreas)
        mapAreasTemp[index] = newMapArea
        return mapAreasTemp
    }

    companion object {
        fun provideFactory(
            mapRepository: MapRepository,
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return ExploreViewModel(mapRepository) as T
            }
        }
    }
}