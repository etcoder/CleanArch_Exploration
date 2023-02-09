package com.trudell.arcgis.explore.data

import android.content.Context


// Dependency Injection at the application level
interface AppContainer {
    val mapRepository: MapRepository
}

class AppContainerImpl(private val applicationContext: Context) : AppContainer {

    override val mapRepository: MapRepository by lazy {
        MapRepository(applicationContext)
    }

}