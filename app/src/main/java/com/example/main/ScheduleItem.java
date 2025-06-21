package com.example.main;

import java.io.Serializable;

public class ScheduleItem implements Serializable {
    private String place;   // 장소 이름
    private String time;    // 시간
    private int day;        // 몇일차
    private String region;  // 지역명 (시/도)

    // 생성자
    public ScheduleItem(String place, String time, int day, String region) {
        this.place = place;
        this.time = time;
        this.day = day;
        this.region = region;
    }

    // getter/setter
    public String getPlace() {
        return place;
    }

    public void setPlace(String place) {
        this.place = place;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public int getDay() {
        return day;
    }

    public void setDay(int day) {
        this.day = day;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    @Override
    public String toString() {
        return day + "일차 - " + time + " - " + place + " (" + region + ")";
    }
}
