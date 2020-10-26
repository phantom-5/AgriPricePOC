package com.example.helloworld;

import android.util.Log;

public class FiltersData {
    static String stateName="All";
    static String districtName="All";
    static String marketName="All";
    static String sortParam="None";
    static String commodityName="None";
    static int limitNumber=25;
    public static void reset(){
        Log.d("Ricky","reset called");
        stateName="All";
        districtName="All";
        marketName="All";
        sortParam="None";
        commodityName="None";
        limitNumber=25;
    }
}
