package com.trudell.arcgis.explore.data

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import com.esri.arcgisruntime.concurrent.Job
import com.esri.arcgisruntime.portal.Portal
import com.esri.arcgisruntime.portal.PortalItem
import com.esri.arcgisruntime.tasks.offlinemap.DownloadPreplannedOfflineMapJob
import com.esri.arcgisruntime.tasks.offlinemap.OfflineMapTask
import com.trudell.arcgis.explore.ExploreApplication.Companion.MAP_PORTAL_ID
import com.trudell.arcgis.explore.ExploreApplication.Companion.OFFLINE_MAP_DIRECTORY
import com.trudell.arcgis.explore.ExploreApplication.Companion.PORTAL_URL
import kotlinx.coroutines.*
import java.io.File
import kotlin.coroutines.resume

class MapRepository(applicationContext: Context) {

    private val offlineMapDirectory by lazy { File(applicationContext.externalCacheDir?.path + OFFLINE_MAP_DIRECTORY) }

    fun createOfflineMapDirectory() {
        // Create a temporary directory in the app cache when needed (code from ArcGIS sample)
        offlineMapDirectory.also {
            when {
                it.mkdirs() -> Log.d(this.toString(),  "Created directory for offline map in " + it.path)
                it.exists() -> Log.d(this.toString(),  "Offline map directory already exists at " + it.path)
                else -> Log.d(this.toString(), "Error creating offline map directory at: " + it.path)
            }
        }
    }

    suspend fun downloadOfflineMapArea(mapAreaInfo: MapAreaInfo): Boolean = suspendCancellableCoroutine { continuation  ->
        var downloadPreplannedOfflineMapJob: DownloadPreplannedOfflineMapJob? = null

        val portal = Portal(PORTAL_URL)
        val portalItem = PortalItem(portal, MAP_PORTAL_ID)
        val offlineMapTask = OfflineMapTask(portalItem)
        val offlineMapParametersFuture = offlineMapTask.createDefaultDownloadPreplannedOfflineMapParametersAsync(mapAreaInfo.preplannedMapArea)
        val path = offlineMapDirectory.path + File.separator + mapAreaInfo.preplannedMapArea.portalItem?.title

        downloadPreplannedOfflineMapJob = offlineMapTask.downloadPreplannedOfflineMap(offlineMapParametersFuture.get(), path)
        downloadPreplannedOfflineMapJob.addJobDoneListener {

            if (downloadPreplannedOfflineMapJob?.status != Job.Status.SUCCEEDED) {
                val error = "Job finished with an error: " + downloadPreplannedOfflineMapJob?.error?.additionalMessage
                Log.d(this.toString(), error)
                return@addJobDoneListener
            }

            continuation.resume(downloadPreplannedOfflineMapJob.status == Job.Status.SUCCEEDED)
        }
        downloadPreplannedOfflineMapJob.start()
    }

    fun deleteOfflineMapArea(mapAreaInfo: MapAreaInfo) {
        val path = offlineMapDirectory.path + File.separator + mapAreaInfo.preplannedMapArea.portalItem?.title
        var areaDir = File(path)
        if(areaDir.exists()) {
            areaDir.deleteRecursively()
        }
    }

    suspend fun fetchOnlineMap(): MapInfo = suspendCancellableCoroutine { continuation ->
        var onlineMapInfo = MapInfo("", "", "")
        val portal = Portal(PORTAL_URL)
        val portalItem = PortalItem(portal, MAP_PORTAL_ID)

        portalItem.addDoneLoadingListener {
            onlineMapInfo.id = portalItem.itemId
            onlineMapInfo.title = portalItem.title
            onlineMapInfo.snippet = portalItem.snippet

            if(portalItem.thumbnailFileName != null) {
                val thumbnailFuture = portalItem.fetchThumbnailAsync()
                thumbnailFuture.addDoneListener {
                    val itemThumbnailData = thumbnailFuture.get()
                    if (itemThumbnailData != null && itemThumbnailData.isNotEmpty()) {
                        onlineMapInfo.imageBitmap =
                            BitmapFactory.decodeByteArray(
                                itemThumbnailData,
                                0,
                                itemThumbnailData.size
                            )
                    }
                    continuation.resume(onlineMapInfo)
                }
            } else {
                continuation.resume(onlineMapInfo)
            }
        }
        portalItem.loadAsync()
    }

    suspend fun fetchAvailableMapAreas(): List<MapAreaInfo> = suspendCancellableCoroutine { continuation ->
        val portal = Portal(PORTAL_URL)
        val portalItem = PortalItem(portal, MAP_PORTAL_ID)
        val offlineMapTask = OfflineMapTask(portalItem)
        val preplannedMapAreasFuture = offlineMapTask.preplannedMapAreasAsync

        val list = preplannedMapAreasFuture.get().map {
            // Convert all results to a MapAreaInfo object
            MapAreaInfo(it)
        }.onEach {
            // Set the status for areas previously downloaded
            val path = offlineMapDirectory.path + File.separator + it.preplannedMapArea.portalItem?.title
            it.mapAreaStatus = if(File(path).exists()) MapStatus.DOWNLOADED else MapStatus.DOWNLOADABLE

            // Get the geometry for the "areas of interest"
            it.preplannedMapArea.loadAsync()
        }

        // Get the thumbnail bitmaps for each area
        // I want to move this to individual item updates
        MainScope().launch {
            list.map { mapArea ->
                return@map async {
                    mapArea.imageBitmap = fetchAreaThumbnail(mapArea.preplannedMapArea.portalItem)
                }
            }.awaitAll()

            continuation.resume(list)
        }
    }

    private suspend fun fetchAreaThumbnail(portalItem: PortalItem): Bitmap = suspendCancellableCoroutine { continuation ->
        // Get the thumbnail for the area
        if(portalItem.thumbnailFileName != null) {
            val thumbnailFuture = portalItem.fetchThumbnailAsync()
            thumbnailFuture.addDoneListener {
                val itemThumbnailData = thumbnailFuture.get()
                if (itemThumbnailData != null && itemThumbnailData.isNotEmpty()) {
                    // Convert the thumbnail data to a usable bitmap
                    var imageBitmap =
                        BitmapFactory.decodeByteArray(
                            itemThumbnailData,
                            0,
                            itemThumbnailData.size
                        )
                    continuation.resume(imageBitmap)
                } else {
                    continuation.cancel()
                }
            }
        }
    }

}