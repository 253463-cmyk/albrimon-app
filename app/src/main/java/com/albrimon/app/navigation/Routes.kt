package com.albrimon.app.navigation

import android.net.Uri

object Routes {
    const val WELCOME = "welcome"
    const val LOGIN = "login"
    const val REGISTER = "register"
    const val HOME = "home"
    const val CLIENTS = "clients"
    const val PROJECTS = "projects"
    const val SCRAPS = "scraps"

    const val CLIENT_DETAIL_PREFIX = "client_detail"
    const val CLIENT_DETAIL = "$CLIENT_DETAIL_PREFIX/{clientId}"
    fun clientDetail(id: Int) = "$CLIENT_DETAIL_PREFIX/$id"

    const val CLIENT_PROJECTS_PREFIX = "client_projects"
    const val CLIENT_PROJECTS = "$CLIENT_PROJECTS_PREFIX/{clientId}"
    fun clientProjects(id: Int) = "$CLIENT_PROJECTS_PREFIX/$id"

    const val SOON = "soon/{title}"
    fun soon(title: String) = "soon/${Uri.encode(title)}"
}
