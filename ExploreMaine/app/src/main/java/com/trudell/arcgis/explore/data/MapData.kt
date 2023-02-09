package com.trudell.arcgis.explore.data

import android.graphics.Bitmap
import com.esri.arcgisruntime.tasks.offlinemap.PreplannedMapArea

/**
 * Map Data
 *
 * This file holds all of the wrapper classes needed to display
 * the map and areas in the UI
 */

// Simple enum to track the state of download in the UI
enum class MapStatus {
    DOWNLOADABLE, DOWNLOADING, DOWNLOADED
}

// Wrapper class for the online map
data class MapInfo(
    var id: String,
    var title: String,
    var snippet: String,
    var imageBitmap: Bitmap? = null
)

// Wrapper class to track the download and thumbnail states
data class MapAreaInfo(
    var preplannedMapArea: PreplannedMapArea,
    var imageBitmap: Bitmap? = null,
    var mapAreaStatus: MapStatus = MapStatus.DOWNLOADABLE
)