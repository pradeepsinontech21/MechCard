package com.mechcard.utils.clicklistner

import com.mechcard.models.JobData

interface FilterListeners {
    fun filteringFinished(filteredItemsCount: Int,data:List<JobData>)
}