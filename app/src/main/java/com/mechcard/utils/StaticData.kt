package com.mechcard.utils

import android.content.Context
import com.mechcard.R
import com.mechcard.models.PendingJobs
import com.mechcard.models.Sports
import com.mechcard.models.StatusModel

fun sportsList(context: Context): List<Sports> {

    return listOf(
        Sports(
            R.drawable.erp_logo,
            context.getString(R.string.title_rugby),
            context.getString(R.string.subtitle_rugby),
            context.getString(R.string.about_rugby)
        ),
        Sports(
            R.drawable.erp_logo,
            context.getString(R.string.title_cricket),
            context.getString(R.string.subtitle_cricket),
            context.getString(R.string.about_cricket)
        ),
        Sports(
            R.drawable.erp_logo,
            context.getString(R.string.title_hockey),
            context.getString(R.string.subtitle_hockey),
            context.getString(R.string.about_hockey)
        ),
        Sports(
            R.drawable.erp_logo,
            context.getString(R.string.title_basketball),
            context.getString(R.string.subtitle_basketball),
            context.getString(R.string.about_basketball)
        ),
        Sports(
            R.drawable.erp_logo,
            context.getString(R.string.title_volleyball),
            context.getString(R.string.subtitle_volleyball),
            context.getString(R.string.about_volleyball)
        ),
        Sports(
            R.drawable.erp_logo,
            context.getString(R.string.title_esports),
            context.getString(R.string.subtitle_esports),
            context.getString(R.string.about_esports)
        ),
        Sports(
            R.drawable.erp_logo,
            context.getString(R.string.title_kabbadi),
            context.getString(R.string.subtitle_kabbadi),
            context.getString(R.string.about_kabbadi)
        ),
        Sports(
            R.drawable.erp_logo,
            context.getString(R.string.title_baseball),
            context.getString(R.string.subtitle_baseball),
            context.getString(R.string.about_baseball)
        ),
        Sports(
            R.drawable.erp_logo,
            context.getString(R.string.title_mma),
            context.getString(R.string.subtitle_mma),
            context.getString(R.string.about_mma)
        ),
        Sports(
            R.drawable.erp_logo,
            context.getString(R.string.title_soccer),
            context.getString(R.string.subtitle_soccer),
            context.getString(R.string.about_soccer)
        ),
        Sports(
            R.drawable.erp_logo,
            context.getString(R.string.title_handball),
            context.getString(R.string.subtitle_handball),
            context.getString(R.string.about_handball)
        ),
        Sports(
            R.drawable.erp_logo,
            context.getString(R.string.title_tennis),
            context.getString(R.string.subtitle_tennis),
            context.getString(R.string.about_tennis)
        )
    )
}


fun pendingJobList(context: Context):List<PendingJobs>{
   return  listOf(
       PendingJobs(jobid ="DAC/123","GH4584","Ajay Singh","hello this is dummy text for testing","Pending"),
       PendingJobs(jobid ="DTO/459","GH45314","Ramesh Kumar","hello this is dummy text for testing","Completed"),
       PendingJobs(jobid ="DAC/143","BH4564","Aabedd Khan","hello this is dummy text for testing","Pending"),
       PendingJobs(jobid ="DTO/450","SH48962","Sultan Khan","hello this is dummy text for testing","Pending"),
       PendingJobs(jobid ="DAC/127","VH4034","Jakir Sekh","hello this is dummy text for testing","Pending"),
       PendingJobs(jobid ="DTO/456","GH45364","Sarukh Khan","hello this is dummy text for testing","Pending"),
       PendingJobs(jobid ="DAC/149","PP4564","Harshvardhan Sinha","hello this is dummy text for testing","Pending"),
       PendingJobs(jobid ="DAC/456","SH4864","Priyanka Kothari","hello this is dummy text for testing","Pending"),
       PendingJobs(jobid ="DAC/145","BH4561","Akshay Kumar","hello this is dummy text for testing","Pending"),
       PendingJobs(jobid ="DAC/430","OH48664","Sanjay Khan","hello this is dummy text for testing","Pending"),

   )
}

fun pendingJobPageList(context: Context):List<PendingJobs>{
    return  listOf(
        PendingJobs(jobid ="DAC/143","BH4564","Harshvardhan Sinha","hello this is dummy text for testing","Pending"),
        PendingJobs(jobid ="DTO/456","GH45364","Sarukh Khan","hello this is dummy text for testing","Pending"),
        PendingJobs(jobid ="DAC/123","GH4534","Ajay Singh","hello this is dummy text for testing","Pending"),
        PendingJobs(jobid ="DTO/456","GH45364","Ramesh Kumar","hello this is dummy text for testing","Completed"),
        PendingJobs(jobid ="DAC/143","BH4564","Aabedd Khan","hello this is dummy text for testing","Pending"),
        PendingJobs(jobid ="DAC/436","SH48964","Sanjay Khan","hello this is dummy text for testing","Pending"),
        PendingJobs(jobid ="DAC/456","SH48964","Priyanka Kothari","hello this is dummy text for testing","Pending"),
        PendingJobs(jobid ="DAC/143","BH4564","Akshay Kumar","hello this is dummy text for testing","Pending"),
        PendingJobs(jobid ="DTO/456","SH48964","Sultan Khan","hello this is dummy text for testing","Pending"),
        PendingJobs(jobid ="DAC/123","GH4534","Jakir Sekh","hello this is dummy text for testing","Pending"),

        )
}


fun statustypeList(context: Context):List<StatusModel>{
    return listOf(
        StatusModel(1,"PENDING"),
        StatusModel(2,"COMPLETE")
    )
}