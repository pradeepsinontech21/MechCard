package com.mechcard.models

data class Mechanic(
    val code: String,
    val name: String,
    val faceID: String,
) {
    override fun toString(): String {
        return name
    }
}