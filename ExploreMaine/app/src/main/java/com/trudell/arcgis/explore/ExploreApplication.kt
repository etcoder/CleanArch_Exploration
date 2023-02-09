package com.trudell.arcgis.explore

import android.app.Application
import com.trudell.arcgis.explore.data.AppContainer
import com.trudell.arcgis.explore.data.AppContainerImpl

class ExploreApplication  : Application() {

    // Keeping this info here for now, should be somewhere more secure
    companion object {
        val PORTAL_URL = "https://www.arcgis.com/"
        val MAP_PORTAL_ID = "3bc3179f17da44a0ac0bfdac4ad15664"
        val OFFLINE_MAP_DIRECTORY = "/arcgis_offline_maps"
    }

    lateinit var container: AppContainer

    override fun onCreate() {
        super.onCreate()
        container = AppContainerImpl(this)
    }
}