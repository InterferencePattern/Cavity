package com.louis.app.cavity.model

interface FileAssoc {
    fun getFilePath(): String
    fun getExternalFileName(): String
    fun getExternalSubDirectory(): String
}
