package com.example.raksh.funshine.model;

/**
 * Created by Rakshit on 2-08-2018.
 */

public class DailyWeatherReport {

    public static final String WEATHER_TYPE_CLOUDS = "Clouds";
    public static final String WEATHER_TYPE_CLEAR = "Clear";
    public static final String WEATHER_TYPE_RAIN = "Rain";
    public static final String WEATHER_TYPE_WIND = "Wind";
    public static final String WEATHER_TYPE_SNOW = "Snow";

    private String cityname;
    private String countryname;
    private int curr_temp;
    private int max_temp;
    private int min_temp;
    private String weather;
    private String LatestDate;

    public String getCityname() {
        return cityname;
    }

    public String getCountryname() {
        return countryname;
    }

    public int getCurr_temp() {
        return curr_temp;
    }

    public int getMax_temp() {
        return max_temp;
    }

    public int getMin_temp() {
        return min_temp;
    }

    public String getWeather() {
        return weather;
    }

    public String getLatestDate() {
        return LatestDate;
    }

    public DailyWeatherReport(String cityname, String countryname, int curr_temp, int max_temp, int min_temp, String weather, String rawDate) {
        this.cityname = cityname;
        this.countryname = countryname;
        this.curr_temp = curr_temp;
        this.max_temp = max_temp;
        this.min_temp = min_temp;

        this.weather = weather;
        this.LatestDate = oldDateToNewDate(rawDate);
    }

    public String oldDateToNewDate(String oldDate){
        //old date to new fun

        return oldDate;
    }
}
