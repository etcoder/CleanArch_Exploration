package com.trudell.arcgis.explore.ui.map

import android.graphics.Color
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.esri.arcgisruntime.geometry.GeometryEngine
import com.esri.arcgisruntime.mapping.ArcGISMap
import com.esri.arcgisruntime.mapping.Viewpoint
import com.esri.arcgisruntime.mapping.view.Graphic
import com.esri.arcgisruntime.mapping.view.GraphicsOverlay
import com.esri.arcgisruntime.mapping.view.MapView
import com.esri.arcgisruntime.portal.Portal
import com.esri.arcgisruntime.portal.PortalItem
import com.esri.arcgisruntime.symbology.SimpleLineSymbol
import com.esri.arcgisruntime.symbology.SimpleRenderer
import com.trudell.arcgis.explore.ExploreApplication.Companion.PORTAL_URL
import com.trudell.arcgis.explore.ui.home.ExploreUiState

@Composable
fun MapScreen(uiState: ExploreUiState, onBack: () -> Unit) {

    if(uiState is ExploreUiState.HasMapInfo) {

        val portal = Portal(PORTAL_URL)
        val portalItem = PortalItem(portal, uiState.mapInfo.id)
        val onlineMap = ArcGISMap(portalItem)
        val selectedMapArea = uiState.selectedArea?.preplannedMapArea
        val mapTitle = if(selectedMapArea != null) selectedMapArea.portalItem.title else uiState.mapInfo.title

        Scaffold(
            topBar = {
                MapAppBar(mapTitle, onBack)
            },
            content = { padding ->
                Column(Modifier.padding(padding)) {

                    // If there is a map area selected to display
                    if (selectedMapArea != null) {

                        // Draw a red line around the "area of interest"
                        val areaOfInterestRenderer = SimpleRenderer(
                            SimpleLineSymbol(
                                SimpleLineSymbol.Style.SOLID,
                                Color.RED,
                                5.0f
                            )
                        )
                        val areasOfInterestGraphicsOverlay =
                            GraphicsOverlay().apply { renderer = areaOfInterestRenderer }
                        val areaOfInterest =
                            GeometryEngine.buffer(selectedMapArea.areaOfInterest, 50.0).extent

                        // Using an AndroidView to work in Jetpack Compose
                        AndroidView(modifier = Modifier.fillMaxSize(),
                            factory = { context ->
                                MapView(context).apply {
                                    map = onlineMap
                                    graphicsOverlays.add(areasOfInterestGraphicsOverlay)
                                    graphicsOverlays[0].graphics.add(Graphic(selectedMapArea.areaOfInterest))
                                    graphicsOverlays[0].isVisible = true
                                    setViewpointAsync(Viewpoint(areaOfInterest), 1.5f)
                                }
                            })
                    } // Otherwise we are displaying a simple online map
                    else {
                        AndroidView(modifier = Modifier.fillMaxSize(),
                            factory = { context ->
                                MapView(context).apply {
                                    map = onlineMap
                                }
                            })
                    }
                }
            }
        )
    }
}

@Composable
fun MapAppBar(title: String, onBack: () -> Unit) {
    TopAppBar(
        title = {
            Text(
                text = title,
                color = androidx.compose.ui.graphics.Color.White
            )
        },
        navigationIcon = {
            IconButton(onClick = onBack) {
                Icon(Icons.Rounded.ArrowBack, "Back")
            }
        }
    )
}
