package com.mechcard.pref

import com.chibatching.kotpref.KotprefModel
import com.chibatching.kotpref.gsonpref.gsonNullablePref
import com.mechcard.models.ConfigData
import com.mechcard.models.SignInResponse

object MechCardPref : KotprefModel() {
    var accessToken by nullableStringPref()
    var refreshToken by nullableStringPref()
    var signedInMechanic by gsonNullablePref<SignInResponse>()
    var configData by gsonNullablePref<ConfigData>()
    var collectionId by nullableStringPref()
    var apiEndPoint by nullableStringPref()
    var isUserLogIn by booleanPref(false)
}