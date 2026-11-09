package com.albrimon.app

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

/** Punto de arranque de Hilt: genera el contenedor de dependencias de toda la app. */
@HiltAndroidApp
class AlbrimonApp : Application()
