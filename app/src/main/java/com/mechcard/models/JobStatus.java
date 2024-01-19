package com.mechcard.models;

public class JobStatus {

    private String status;

    public JobStatus(String countryName)
    {
        status = countryName;
    }
    public String getJobStatus()
    {
        return status;
    }
}
