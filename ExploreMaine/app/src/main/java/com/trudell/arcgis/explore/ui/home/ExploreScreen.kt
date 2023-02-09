package com.trudell.arcgis.explore.ui.home

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.trudell.arcgis.explore.R
import com.trudell.arcgis.explore.data.MapAreaInfo
import com.trudell.arcgis.explore.data.MapInfo
import com.trudell.arcgis.explore.data.MapStatus
import com.trudell.arcgis.explore.ui.theme.Typography

@Composable
fun ExploreScreen(
    uiState: ExploreUiState,
    onMapSelected: () -> Unit,
    onMapAreaSelected: (mapArea: MapAreaInfo) -> Unit,
    onDownloadMapArea: (mapArea: MapAreaInfo) -> Unit,
    onDeleteMapArea: (mapArea: MapAreaInfo) -> Unit
) {
    Scaffold(
        topBar = {
            ExploreAppBar()
        },
        content = { padding ->
            Column(modifier = Modifier.padding(padding)) {
                if (uiState is ExploreUiState.HasMapInfo) {
                    OnlineMapHeader(uiState.mapInfo, onTap = onMapSelected)
                    DownloadableMapAreas(
                        uiState.mapAreas,
                        onTap = { onMapAreaSelected(it) },
                        onDownload = { onDownloadMapArea(it) },
                        onDelete = { onDeleteMapArea(it) })
                } else {
                    LoadingContentScreen()
                }
            }
        }
    )
}

@Composable
fun ExploreAppBar() {
    TopAppBar(
        title = {
            Text(
                text = stringResource(R.string.app_name),
                color = Color.White
            )
        }
    )
}

@Composable
fun LoadingContentScreen() {
    Column(
        modifier = Modifier
            .padding(20.dp, 60.dp)
            .fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Loading Content...",
            modifier = Modifier.padding(20.dp, 10.dp),
            style = Typography.h1,
            fontSize = 32.sp,
            textAlign = TextAlign.Center
        )
        Box(modifier = Modifier.wrapContentSize(), contentAlignment = Alignment.Center) {
            Image(
                painterResource(id = R.drawable.asset_main_placeholder),
                contentDescription = null,
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .size(150.dp)
                    .clip(CircleShape)
            )
            CircularProgressIndicator(
                modifier = Modifier.then(Modifier.size(160.dp))
            )
        }
    }
}

@Composable
fun OnlineMapHeader(onlineMap: MapInfo, onTap: () -> Unit) {
    val context = LocalContext.current

    Column {
        Text(
            text = "Web Map",
            modifier = Modifier.padding(20.dp, 10.dp),
            style = Typography.h1
        )

        OnlineMapInfo(
            onlineMap.title,
            onlineMap.snippet,
            onlineMap.imageBitmap,
            onTap = onTap
        )
        Divider(modifier = Modifier.height(1.dp), color = Color.LightGray)
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun OnlineMapInfo(
    title: String,
    description: String,
    thumbnail: Bitmap?,
    onTap: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(100.dp)
            .background(Color.White),
        onClick = onTap,
        shape = RectangleShape
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Spacer(Modifier.size(4.dp))
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(thumbnail)
                    .crossfade(true)
                    .build(),
                placeholder = painterResource(id = R.drawable.asset_main_placeholder),
                error = painterResource(id = R.drawable.asset_main_placeholder),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth(0.3f)
                    .fillMaxHeight()
                    .padding(16.dp, 10.dp)
            )
            Column(
                modifier = Modifier
                    .weight(1f)
            ) {
                Text(
                    text = title,
                    style = Typography.subtitle1
                )
                Text(
                    text = description,
                    style = Typography.body1,
                    maxLines = 3,
                )
            }
            Spacer(Modifier.size(20.dp))
        }
    }
}

@Composable
fun DownloadableMapAreas(
    mapAreas: List<MapAreaInfo>,
    onTap: (mapArea: MapAreaInfo) -> Unit,
    onDownload: (mapArea: MapAreaInfo) -> Unit,
    onDelete: (mapArea: MapAreaInfo) -> Unit
) {
    val context = LocalContext.current

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        Text(
            text = "Map Areas",
            modifier = Modifier.padding(20.dp, 10.dp),
            style = Typography.h1
        )
        // Lazy list to display all of the available areas for the online map
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
        ) {
            items(mapAreas) { area ->
                MapAreaItem(
                    area,
                    onTap = { onTap(area) },
                    onDownload = { onDownload(area) },
                    onDelete = { onDelete(area) })
            }
        }
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun MapAreaItem(
    mapAreaInfo: MapAreaInfo,
    onTap: () -> Unit,
    onDownload: () -> Unit,
    onDelete: () -> Unit
) {
    // Toggle to show/hide the delete menu
    var optionsVisible by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(100.dp)
            .background(Color.White),
        onClick = onTap,
        shape = RectangleShape
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Spacer(Modifier.size(4.dp))
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(mapAreaInfo.imageBitmap)
                    .crossfade(true)
                    .build(),
                placeholder = painterResource(id = R.drawable.asset_main_placeholder),
                error = painterResource(id = R.drawable.asset_main_placeholder),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth(0.3f)
                    .fillMaxHeight()
                    .padding(16.dp, 10.dp)
            )
            Column(
                modifier = Modifier
                    .weight(1f)
            ) {
                Text(
                    text = mapAreaInfo.preplannedMapArea.portalItem.title,
                    style = Typography.subtitle1
                )
                Text(
                    text = mapAreaInfo.preplannedMapArea.portalItem.snippet,
                    style = Typography.body1,
                    maxLines = 3,
                )
            }
            Box(modifier = Modifier.size(60.dp), contentAlignment = Alignment.Center) {
                when (mapAreaInfo.mapAreaStatus) {
                    // Show a download icon button if the area needs to be downloaded
                    MapStatus.DOWNLOADABLE -> {
                        IconButton(onClick = onDownload) {
                            Icon(Icons.Rounded.Download, "Download")
                        }
                    }
                    // Show a circular progress bar if the area is downloading
                    MapStatus.DOWNLOADING -> {
                        CircularProgressIndicator(
                            modifier = Modifier.then(Modifier.size(24.dp))
                        )
                    }
                    // Show a ... menu with an option to delete if the area is downloaded
                    MapStatus.DOWNLOADED -> {
                        Box {
                            IconButton(onClick = { optionsVisible = true }) {
                                Icon(Icons.Rounded.MoreVert, "Menu")
                            }
                            DropdownMenu(
                                modifier = Modifier.align(Alignment.TopEnd),
                                expanded = optionsVisible,
                                onDismissRequest = { optionsVisible = false }
                            ) {
                                DropdownMenuItem(onClick = {
                                    onDelete.invoke()
                                    optionsVisible = false
                                }) {
                                    Text("Delete")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}