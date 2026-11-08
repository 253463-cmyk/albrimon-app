package com.albrimon.app.data.model

data class Measurements(
    val bust: Int,
    val waist: Int,
    val hip: Int,
    val length: Int
)

data class Client(
    val id: Int,
    val name: String,
    val size: String,
    val age: Int,
    val isActive: Boolean,
    val phone: String,
    val measurements: Measurements
)
