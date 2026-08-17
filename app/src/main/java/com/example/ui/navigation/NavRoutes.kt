package com.example.ui.navigation

sealed class Screen(val route: String) {
  object Dashboard : Screen("dashboard")
  object Apiaries : Screen("apiaries")
  object Hives : Screen("hives")
  object Calendar : Screen("calendar")
  object Settings : Screen("settings")
  object HiveDetail : Screen("hive_detail/{hiveId}") {
    fun createRoute(hiveId: String) = "hive_detail/$hiveId"
  }
  object AddEditHive : Screen("add_edit_hive?hiveId={hiveId}&apiaryId={apiaryId}") {
    fun createRoute(hiveId: String? = null, apiaryId: String? = null): String {
      return buildString {
        append("add_edit_hive")
        val params = mutableListOf<String>()
        if (!hiveId.isNullOrEmpty()) params.add("hiveId=$hiveId")
        if (!apiaryId.isNullOrEmpty()) params.add("apiaryId=$apiaryId")
        if (params.isNotEmpty()) {
          append("?")
          append(params.joinToString("&"))
        }
      }
    }
  }
  object AddInspection : Screen("add_inspection/{hiveId}") {
    fun createRoute(hiveId: String) = "add_inspection/$hiveId"
  }
  object AddFeeding : Screen("add_feeding/{hiveId}") {
    fun createRoute(hiveId: String) = "add_feeding/$hiveId"
  }
}
