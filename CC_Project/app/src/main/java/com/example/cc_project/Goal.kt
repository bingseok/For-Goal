package com.example.cc_project

data class Goal (
    val gid : String = "",
    val goalname : String = "",
    var achieved : Boolean = false,
    var startdate : String = "",
    var finishdate : String = ""
)