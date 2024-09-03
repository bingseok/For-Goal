package com.example.cc_project

class data {
    data class ParentItem(
        val id: String?,
        val title: String?,
        val children: ArrayList<ChildItem>?
    )

    data class ChildItem(
        val id: String?,
        val title: String?
    )
}