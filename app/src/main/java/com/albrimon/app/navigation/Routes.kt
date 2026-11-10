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
    const val COSTS = "costs"

    const val CLIENT_DETAIL_PREFIX = "client_detail"
    const val CLIENT_DETAIL = "$CLIENT_DETAIL_PREFIX/{clientId}"
    fun clientDetail(id: Long) = "$CLIENT_DETAIL_PREFIX/$id"

    const val CLIENT_PROJECTS_PREFIX = "client_projects"
    const val CLIENT_PROJECTS = "$CLIENT_PROJECTS_PREFIX/{clientId}"
    fun clientProjects(id: Long) = "$CLIENT_PROJECTS_PREFIX/$id"

    const val CLIENT_FORM = "client_form"

    // Retazos: lista -> formulario (0 = nuevo) / plan de corte
    const val SCRAP_FORM_PREFIX = "scrap_form"
    const val SCRAP_FORM = "$SCRAP_FORM_PREFIX/{scrapId}"
    fun scrapForm(id: Long) = "$SCRAP_FORM_PREFIX/$id"
    const val SCRAP_PLAN = "scrap_plan"
    fun scrapPlan(projectId: Number) = "scrap_plan?projectId=$projectId"

    const val SOON = "soon/{title}"
    fun soon(title: String) = "soon/${Uri.encode(title)}"
}
