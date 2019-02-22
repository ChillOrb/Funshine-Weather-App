package com.example.raksh.funshine;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.raksh.funshine.model.DailyWeatherReport;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
//import com.google.android.gms.vision.text.Line;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class WeatherActivity extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener ,GoogleApiClient.ConnectionCallbacks,LocationListener {

    final String BASE_URL = "http://api.openweathermap.org/data/2.5/forecast";
    final String URL_COORD = "/?lat=";
    final String URL_UNITS = "&units=metric";
    final String URL_API_KEY = "&APPID=de79be148369a2c859bf69811af5af82";

    private GoogleApiClient mGoogleApiClient;
    private final int PERMISSION_LOCATION = 111;
    private ArrayList<DailyWeatherReport> ListToPrint = new ArrayList<>();

    private ImageView weatherIcon;
    private ImageView weatherIconMini;
    private TextView weatherDate;
    private TextView currentTemp;
    private TextView lowTemp;
    private TextView cityCountry;
    private TextView weatherDescription;

    WeatherAdapter adapter;

    //9.981189,76.2804663

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weather);

        weatherIcon = (ImageView) findViewById(R.id.weatherIcon);
        weatherIconMini = (ImageView) findViewById(R.id.weatherIconMini);
        weatherDate = (TextView) findViewById(R.id.weatherDate);
        currentTemp = (TextView) findViewById(R.id.currentTemp);
        lowTemp = (TextView) findViewById(R.id.lowTemp);
        cityCountry = (TextView) findViewById(R.id.cityCountry);
        weatherDescription = (TextView) findViewById(R.id.weatherDescription);

        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.content_weather_report);
        adapter = new WeatherAdapter(ListToPrint);

        recyclerView.setAdapter(adapter);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);

        recyclerView.setLayoutManager(linearLayoutManager);

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .enableAutoManage(this, this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();

    }

    public void downloadWeatherdata(Location loc) {
        Double las = loc.getLatitude();
        Double los = loc.getLongitude();
        final String fullcoords = URL_COORD + las + "&lon=" + los;
        final String url = BASE_URL + fullcoords + URL_UNITS + URL_API_KEY;

        final JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    JSONObject cityobj = response.getJSONObject("city");
                    String cityname = cityobj.getString("name");
                    String countryname = cityobj.getString("country");

                    JSONArray list = response.getJSONArray("list");

                    for (int i = 0; i < 5; i++) {
                        JSONObject listobj = list.getJSONObject(i);
                        JSONObject mainobj = listobj.getJSONObject("main");
                        Double curr_temp = mainobj.getDouble("temp");
                        Double max_temp = mainobj.getDouble("temp_max");
                        Double min_temp = mainobj.getDouble("temp_min");

                        JSONArray weatherarray = listobj.getJSONArray("weather");
                        JSONObject weatherobj = weatherarray.getJSONObject(0);
                        String weatherType = weatherobj.getString("main");

                        String rawDate = listobj.getString("dt_txt");

                        DailyWeatherReport reportOfWeather = new DailyWeatherReport(cityname, countryname, curr_temp.intValue(), max_temp.intValue(), min_temp.intValue(), weatherType, rawDate);
                        Log.v("JASON ", "updates = " + reportOfWeather.getWeather());
                        ListToPrint.add(reportOfWeather);

                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                updateUI();

                adapter.notifyDataSetChanged();

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.v("FUN", "ERROR: " + error.getLocalizedMessage());
                Toast.makeText(WeatherActivity.this, "Error in fetching latest weather forecast. LOL!", Toast.LENGTH_LONG).show();
            }
        });

        Volley.newRequestQueue(this).add(jsonObjectRequest);
    }

    public void updateUI() {
        if (ListToPrint.size() != 0) {
            DailyWeatherReport report = ListToPrint.get(0);

            switch (report.getWeather()) {

                case DailyWeatherReport.WEATHER_TYPE_CLOUDS:
                    weatherIcon.setImageDrawable(getResources().getDrawable(R.drawable.cloudy));
                    weatherIconMini.setImageDrawable(getResources().getDrawable(R.drawable.cloudy));
                    break;

                case DailyWeatherReport.WEATHER_TYPE_RAIN:
                    weatherIcon.setImageDrawable(getResources().getDrawable(R.drawable.rainy));
                    weatherIconMini.setImageDrawable(getResources().getDrawable(R.drawable.rainy_mini));
                    break;

                default:
                    weatherIcon.setImageDrawable(getResources().getDrawable(R.drawable.sunny));
                    weatherIconMini.setImageDrawable(getResources().getDrawable(R.drawable.sunny_mini));
            }

            weatherDate.setText("it is date");
            currentTemp.setText(Integer.toString(report.getCurr_temp()) + "°");
            lowTemp.setText(Integer.toString(report.getMin_temp()) + "°");
            cityCountry.setText(report.getCityname() + ", " + report.getCountryname());
            weatherDescription.setText(report.getWeather());
        }
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSION_LOCATION);
        } else {
            startLocationServices();
        }
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onLocationChanged(Location location) {
        downloadWeatherdata(location);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {
            case PERMISSION_LOCATION: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    startLocationServices();
                } else {
                    Toast.makeText(this, "Cannot run , You denied permission", Toast.LENGTH_LONG).show();
                }
            }
        }
    }

    public void startLocationServices() {
        try {
            LocationRequest req = LocationRequest.create().setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, req, this);
        } catch (SecurityException e) {
            Log.v("Exception = ", e.toString());
        }
    }


    public class WeatherAdapter extends RecyclerView.Adapter<WeatherReportViewHolder> {

        private ArrayList<DailyWeatherReport> mlistofDailyWeatherReport;

        public WeatherAdapter(ArrayList<DailyWeatherReport> listofDailyWeatherReport) {
            mlistofDailyWeatherReport = listofDailyWeatherReport;
        }

        @Override
        public WeatherReportViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View card = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_weather, parent, false);
            return new WeatherReportViewHolder(card);
        }

        @Override
        public void onBindViewHolder(WeatherReportViewHolder holder, int position) {
            DailyWeatherReport report = mlistofDailyWeatherReport.get(position);
            holder.UpdateUICard(report);
        }

        @Override
        public int getItemCount() {
            return mlistofDailyWeatherReport.size();
        }
    }

    public class WeatherReportViewHolder extends RecyclerView.ViewHolder {

        private ImageView rweatherIcon;
        private TextView rweatherDate;
        private TextView rtemp_high;
        private TextView rtemp_low;
        private TextView rweatherDescription;

        public WeatherReportViewHolder(View itemView) {
            super(itemView);

            rweatherIcon = (ImageView) itemView.findViewById(R.id.list_weather_icon);
            rweatherDate = (TextView) itemView.findViewById(R.id.list_weather_day);
            rtemp_high = (TextView) itemView.findViewById(R.id.list_weather_temp_high);
            rtemp_low = (TextView) itemView.findViewById(R.id.list_weather_temp_low);
            rweatherDescription = (TextView) itemView.findViewById(R.id.list_weather_description);
        }

        public void UpdateUICard(DailyWeatherReport report) {

            rweatherDate.setText(report.getLatestDate());
            rtemp_high.setText(report.getMax_temp() + "");
            rtemp_low.setText(report.getMin_temp() + "");
            rweatherDescription.setText(report.getWeather());

            switch (report.getWeather()) {
                case DailyWeatherReport.WEATHER_TYPE_CLOUDS:
                    rweatherIcon.setImageDrawable(getResources().getDrawable(R.drawable.cloudy_mini));
                    break;

                case DailyWeatherReport.WEATHER_TYPE_RAIN:
                    rweatherIcon.setImageDrawable(getResources().getDrawable(R.drawable.rainy_mini));
                    break;

                default:
                    rweatherIcon.setImageDrawable(getResources().getDrawable(R.drawable.sunny_mini));
            }

        }

    }

}

