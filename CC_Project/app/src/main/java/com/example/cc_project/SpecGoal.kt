package com.example.cc_project
data class SpecGoal(
    val sgid: String = "",
    var specgoalname: String = "",
    var achieved: Boolean = false,
    var startdate: String = "",
    var finishdate: String = "",
    var repeat : Int = 0,
    var dayofweek:  List<Boolean> = listOf(false,false,false,false,false,false,false),
    var totalCheck: Int = 0,
    var currentCheck: Int = 0
)
