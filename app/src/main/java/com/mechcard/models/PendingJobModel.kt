package com.mechcard.models

import com.google.gson.annotations.SerializedName

data class PendingJobModel(@SerializedName("actionresult"   ) var actionresult   : results?             = results(),
                           @SerializedName("pendingJobList" ) var pendingJobList : ArrayList<PendingJobs> = arrayListOf(),
                           @SerializedName("pagingDetails"  ) var pagingDetails  : PagingDetails?            = PagingDetails())



data class results (

    @SerializedName("status"  ) var status  : String? = null,
    @SerializedName("message" ) var message : String? = null,
    @SerializedName("logid"   ) var logid   : String? = null

)

data class PagingDetails (

    @SerializedName("totalrecords" ) var totalrecords : Int? = null,
    @SerializedName("pagesize"     ) var pagesize     : Int? = null,
    @SerializedName("noofpages"    ) var noofpages    : Int? = null,
    @SerializedName("currentpage"  ) var currentpage  : Int? = null

)

//data class PendingJobList (
//
//    @SerializedName("jobid"            ) var jobid            : String? = null,
//    @SerializedName("vehicleid"        ) var vehicleid        : String? = null,
//    @SerializedName("customername"     ) var customername     : String? = null,
//    @SerializedName("description"      ) var description      : String? = null,
//    @SerializedName("status"           ) var status           : String? = null,
//    @SerializedName("jobdate"          ) var jobdate          : String? = null,
//    @SerializedName("vehiclebrand"     ) var vehiclebrand     : String? = null,
//    @SerializedName("vehiclemodelname" ) var vehiclemodelname : String? = null,
//    @SerializedName("vehiclemodelyear" ) var vehiclemodelyear : String? = null,
//    @SerializedName("vehiclecolor"     ) var vehiclecolor     : String? = null
//
//)