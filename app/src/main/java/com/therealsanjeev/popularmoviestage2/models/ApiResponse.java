package com.therealsanjeev.popularmoviestage2.models;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class ApiResponse<T> {
    @SerializedName("results")
    public List<T> results;
}
