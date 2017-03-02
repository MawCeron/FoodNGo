package org.situvieraunplan.foodngo;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Random;

import javax.net.ssl.HttpsURLConnection;

public class MainActivity extends AppCompatActivity {

    LocationManager locationManager;
    String myCoordinates,jsonString, url_request;
    String placeName,placeLat,placeLong, placeCoord;
    String placeURL;
    Button btnIDC;
    ProgressDialog pd;
    static final String API_KEY = "AIzaSyAEeDZeyMPC2lyQ7LTRp0hqFt7U_rUTBVU";
    static final int MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        btnIDC = (Button) findViewById(R.id.idont);

        btnIDC.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                url_request = "https://maps.googleapis.com/maps/api/place/nearbysearch/json?" +
                        "location=" + myCoordinates +
                        "&radius=1000&types=restaurant|cafe&" +
                        "key=" + API_KEY;

                new JsonTask().execute(url_request);
            }
        });
    }

    @Override
    protected void onStart() {
        getLocation();
        super.onStart();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

            switch (requestCode) {
                case MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION: {
                    // If request is cancelled, the result arrays are empty.
                    if (grantResults.length > 0
                            && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                        Toast.makeText(MainActivity.this,
                                "permission was granted, :)",
                                Toast.LENGTH_LONG).show();
                        getLocation();

                    } else {
                        Toast.makeText(MainActivity.this,
                                "permission denied, ...:(",
                                Toast.LENGTH_LONG).show();
                    }

                }
            }
    }

    private void getLocation() {
        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this,Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.ACCESS_COARSE_LOCATION},
                    MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        }
        Criteria criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_FINE);
        criteria.setPowerRequirement(Criteria.POWER_LOW);
        criteria.setAltitudeRequired(false);
        criteria.setBearingRequired(false);
        criteria.setCostAllowed(true);

        String provider = locationManager.getBestProvider(criteria, true);
        if(provider != null){
            locationManager.requestLocationUpdates(provider,0,0,locationListener);
        }

        btnIDC.setVisibility(View.VISIBLE);
    }

    private final LocationListener locationListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            myCoordinates = location.getLatitude() + "," + location.getLongitude();
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {

        }

        @Override
        public void onProviderEnabled(String provider) {

        }

        @Override
        public void onProviderDisabled(String provider) {

        }
    };

    private void gpsDialog() {
            AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
            dialogBuilder.setTitle("JSON");
            dialogBuilder.setMessage(placeURL);
            AlertDialog dialog = dialogBuilder.create();
            dialog.show();
    }

    private class JsonTask extends AsyncTask<String,String,String>{
        @Override
        protected  void onPreExecute() {
            super.onPreExecute();

            pd = new ProgressDialog(MainActivity.this);
            pd.setMessage("Please Wait... We're choosing for you.");
            pd.setCancelable(false);
            pd.show();
        }

        @Override
        protected String doInBackground(String... params) {
            HttpsURLConnection connection;
            BufferedReader reader;

            try {
                URL url = new URL(params[0]);
                connection = (HttpsURLConnection) url.openConnection();
                connection.connect();

                InputStream stream = connection.getInputStream();

                reader = new BufferedReader(new InputStreamReader(stream));

                StringBuilder buffer = new StringBuilder();
                String line;

                while((line=reader.readLine()) != null){
                    buffer.append(line);
                }

                JSONObject jsonObject = new JSONObject(buffer.toString());
                JSONArray jsonArray = jsonObject.getJSONArray("results");
                int totalResults = jsonArray.length();
                Random random = new Random();
                int election = random.nextInt(totalResults + 1);

                placeName = jsonArray.getJSONObject(election).getString("name");
                String encName = URLEncoder.encode(placeName, "utf-8");

                placeLat = jsonArray.getJSONObject(election).getJSONObject("geometry").getJSONObject("location").getString("lat");
                placeLong = jsonArray.getJSONObject(election).getJSONObject("geometry").getJSONObject("location").getString("lng");
                placeCoord = placeLat+","+placeLong;


                return "geo:" + placeCoord + "?q=" + encName;

            } catch ( IOException | JSONException e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            if(pd.isShowing())
                pd.dismiss();
            placeURL = result;
            //gpsDialog();
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(placeURL));
            startActivity(intent);
        }
    }


}
