package com.example.hw5;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;

public class CityWeatherActivity extends AppCompatActivity implements GetJSONData.IJSONActivity
{
    public static final String WEATHER_KEY = "WEATHER";
    public static final String MAX_KEY = "MAX";
    public static final String MIN_KEY = "MIN";
    private static final String API_KEY = "214634720744cde6";

    private ProgressDialog progressLoading;
    private TextView textViewLocation;
    private ListView listViewWeather;
    private Button btnAddToFavorites;

    private String cityName, stateInitials;
    private int minTemp, maxTemp;

    private ArrayList<Weather> weather;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_city_weather);

        setAllViews();

        Intent intent = getIntent();

        if(intent == null)
            Log.d("cityweatheractivity", "intent is null :'(");

        Bundle bundle = intent.getExtras();

        cityName      = bundle.getString(MainActivity.CITY_KEY);
        stateInitials = bundle.getString(MainActivity.STATE_KEY);

        textViewLocation.setText(getString(R.string.lbl_current_location) + ": " + cityName + ", " + stateInitials);
        progressLoading.show();

        //new GetJSONData(this).execute("http://api.wunderground.com/api/" + API_KEY + "/hourly/q/NC/Charlotte.json"); //TODO: Unlock this from charlotte
        new GetJSONData(this).execute(getJSONURI());
    }

    private String getJSONURI()
    {
        return "http://api.wunderground.com/api/" + API_KEY + "/hourly/q/" + stateInitials
                + "/" + cityName.replace(' ', '_') + ".json";
    }

    private void setAllViews()
    {
        progressLoading = new ProgressDialog(this);
        progressLoading.setTitle(getString(R.string.lbl_loading));
        progressLoading.setCancelable(false);

        textViewLocation = (TextView) findViewById(R.id.textViewLocation);
        listViewWeather  = (ListView) findViewById(R.id.listViewWeather);

        btnAddToFavorites = (Button) findViewById(R.id.btnAddToFavorites);

        //TODO: How to make in the menu like the PDF specifies?
        btnAddToFavorites.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                HashMap<String, FavoriteData> currFavorites = MainActivity.getFavorites();

                if(currFavorites == null)
                {
                    currFavorites = new HashMap<String, FavoriteData>();
                }

                String key = cityName.toLowerCase() + "_" + stateInitials.toLowerCase();

                if(currFavorites.containsKey(key))
                {
                    Toast.makeText(CityWeatherActivity.this, getString(R.string.toast_updated_favorites), Toast.LENGTH_LONG).show();
                }
                else
                {
                    Toast.makeText(CityWeatherActivity.this, getString(R.string.toast_added_to_favorites), Toast.LENGTH_LONG).show();
                }

                currFavorites.put(key, new FavoriteData(cityName, stateInitials, Calendar.getInstance().getTime(), weather.get(0).getTemperature())); // Closest temperature to now will always be at pos 0.

                MainActivity.saveFavorites(currFavorites);
            }
        });
    }

    @Override
    public void setJSONList(final ArrayList<Weather> weather)
    {
        progressLoading.dismiss();

        this.weather = weather;

        Comparator<Weather> comparator = new Comparator<Weather>()
        {
            @Override
            public int compare(Weather lhs, Weather rhs)
            {
                return ((Integer)lhs.getTemperature()).compareTo(rhs.getTemperature());
            }
        };

        maxTemp = Collections.max(weather, comparator).getTemperature();
        minTemp = Collections.min(weather, comparator).getTemperature();

        Log.d("cityweatheractivity", weather.toString());

        WeatherAdapter adapter = new WeatherAdapter(this, weather, R.layout.weather_layout_item);
        adapter.setNotifyOnChange(true);
        listViewWeather.setAdapter(adapter);

        listViewWeather.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id)
            {
                Weather currWeather = weather.get(position);
                currWeather.setStateInitials(stateInitials);
                currWeather.setCityName(cityName);

                Intent intent = new Intent(CityWeatherActivity.this, WeatherDetailsActivity.class);
                intent.putExtra(WEATHER_KEY, currWeather);
                intent.putExtra(MAX_KEY, maxTemp);
                intent.putExtra(MIN_KEY, minTemp);
                startActivity(intent);
            }
        });

    }
}
