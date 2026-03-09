package com.example.loom_group_2;

public class TripLog {
    private String date, title, time, fuel;

    public TripLog(String date, String title, String time, String fuel) {
        this.date = date;
        this.title = title;
        this.time = time;
        this.fuel = fuel;
    }

    public String getDate() { return date; }
    public String getTitle() { return title; }
    public String getTime() { return time; }
    public String getFuel() { return fuel; }
}