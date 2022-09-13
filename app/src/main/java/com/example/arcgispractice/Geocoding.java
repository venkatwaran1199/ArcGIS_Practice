package com.example.arcgispractice;

import static android.content.ContentValues.TAG;

import android.util.Log;

import com.esri.arcgisruntime.concurrent.ListenableFuture;
import com.esri.arcgisruntime.tasks.geocode.GeocodeResult;
import com.esri.arcgisruntime.tasks.geocode.LocatorTask;

import java.text.DecimalFormat;
import java.util.List;

public class Geocoding {

        private  double lat,Long;
        private  double Lat_and_Long;

        public interface GeocodingResponceListener {
        void onError(String message);

        void onResponse(String LatLong);
    }

        public double GeocodingTask(String placeName,GeocodingResponceListener GCRS){
        LocatorTask locatorTask = new LocatorTask("https://geocode.arcgis.com/arcgis/rest/services/World/GeocodeServer");
      //  Log.d(TAG, "GeocodingTask: "+placeName);
        ListenableFuture<List<GeocodeResult>> results = locatorTask.geocodeAsync(placeName);
        results.addDoneListener(() -> {
            try {
             GeocodeResult result = results.get().get(0);
             lat=result.getDisplayLocation().getX();
             Long=result.getDisplayLocation().getY();
                String pattern = "#.####";
                DecimalFormat decimalFormat =  new DecimalFormat(pattern);
                String MapLat = decimalFormat.format(lat);
                String MapLong= decimalFormat.format(Long);

                GCRS.onResponse(MapLat+","+MapLong);
            //    Log.d(TAG, "Geocodinglat and long: "+MapLat+","+MapLong);
            } catch (Exception e) {
                Log.d(TAG, "error: "+e);
                GCRS.onError("Exception occur");
            }
        });
        return Lat_and_Long;
    }
}
