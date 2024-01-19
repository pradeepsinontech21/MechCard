package com.mechcard.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize


@Parcelize
data class JobData(
    val customerName: String,
    val description: String,
    val id: String,
    val vehicleRegistrationNo: String
) : Parcelable {
    override fun toString(): String {
        return id
    }
}