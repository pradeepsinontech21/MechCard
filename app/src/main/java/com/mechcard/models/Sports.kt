package com.mechcard.models


import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Sports(
    val icon: Int,
    val title: String,
    val originated: String,
    val about: String
) : Parcelable