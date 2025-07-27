package com.example.myapplication


data class BackUpList(
    var id: Int,
    var name: String,
    var file: String,
    var date: String,
    var size: String,
    var bd: String,
    var username: String,
    var status: String,
)


data class BackUpListNameServer(
    var name: String
)
