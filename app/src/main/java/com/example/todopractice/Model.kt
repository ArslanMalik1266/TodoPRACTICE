package com.example.todopractice

import java.io.Serializable


data class NoteModel(
    var id: String = "",
    var title: String = "",
    var description: String = "",
    var checked: Boolean = false,
    var timestamp: Long = System.currentTimeMillis()
) : Serializable
