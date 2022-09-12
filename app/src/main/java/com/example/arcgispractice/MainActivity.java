package com.example.arcgispractice;

import static android.content.ContentValues.TAG;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;

import android.annotation.SuppressLint;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.esri.arcgisruntime.ArcGISRuntimeEnvironment;
import com.esri.arcgisruntime.concurrent.ListenableFuture;
import com.esri.arcgisruntime.data.ArcGISFeatureTable;
import com.esri.arcgisruntime.geometry.Point;
import com.esri.arcgisruntime.geometry.PointCollection;
import com.esri.arcgisruntime.geometry.Polygon;
import com.esri.arcgisruntime.geometry.Polyline;
import com.esri.arcgisruntime.geometry.SpatialReferences;
import com.esri.arcgisruntime.internal.jni.CoreRequest;
import com.esri.arcgisruntime.internal.jni.CoreRoute;
import com.esri.arcgisruntime.mapping.ArcGISMap;
import com.esri.arcgisruntime.mapping.Basemap;
import com.esri.arcgisruntime.mapping.BasemapStyle;
import com.esri.arcgisruntime.mapping.Viewpoint;
import com.esri.arcgisruntime.mapping.view.DefaultMapViewOnTouchListener;
import com.esri.arcgisruntime.mapping.view.Graphic;
import com.esri.arcgisruntime.mapping.view.GraphicsOverlay;
import com.esri.arcgisruntime.mapping.view.MapView;
import com.esri.arcgisruntime.symbology.SimpleFillSymbol;
import com.esri.arcgisruntime.symbology.SimpleLineSymbol;
import com.esri.arcgisruntime.symbology.SimpleMarkerSymbol;
import com.esri.arcgisruntime.tasks.geocode.GeocodeResult;
import com.esri.arcgisruntime.tasks.geocode.LocatorTask;
import com.esri.arcgisruntime.tasks.geocode.SuggestResult;
import com.esri.arcgisruntime.tasks.networkanalysis.Route;
import com.esri.arcgisruntime.tasks.networkanalysis.RouteParameters;
import com.esri.arcgisruntime.tasks.networkanalysis.RouteResult;
import com.esri.arcgisruntime.tasks.networkanalysis.RouteTask;
import com.esri.arcgisruntime.tasks.networkanalysis.Stop;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

public class MainActivity extends AppCompatActivity {

    public static final String ROUTING_API = "https://route-api.arcgis.com/arcgis/rest/services/World/Route/NAServer/Route_World";
    public static final String API_KEY = "AAPK026861e3641744818c90e0ae6c270d12Dg_pj59bbpWVU2LC2BztipOpD4DAmGBtw-PbeQhCQGeiyl5xeW2izNTyHmGr-ND-";
    private Point incidentPoint;
    private EditText searchView;
    private Button btn_search;
    double maplat;
    double mapLong;

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //initialize map:
        MapView mapview=findViewById(R.id.mapview);
        searchView=findViewById(R.id.searchView);
        btn_search=findViewById(R.id.searchbutton);



//--------------------------------------------------------------------Map Display------------------------------------------------------------------------------
           //API KEY Config:
        ArcGISRuntimeEnvironment.setApiKey(API_KEY);

        //Add basemap:
        ArcGISMap arcsgisMap=new ArcGISMap(Basemap.createOpenStreetMap());
        mapview.setMap(arcsgisMap);

        //add viewpoint to basemap:
        Point point=new Point(80.3579,13.1289, SpatialReferences.getWgs84());
        Viewpoint viewpoint=new Viewpoint(point,500000);
        mapview.setViewpointAsync(viewpoint,3);

        //Instance of graphics overlay:
        GraphicsOverlay graphicsOverlay = new GraphicsOverlay();
        //Connect graphics overlay with mapview:
        mapview.getGraphicsOverlays().add(graphicsOverlay);

//-----------------------------------------------------------------click on map event-------------------------------------------------------------------------
        /*   mapview.setOnTouchListener(new DefaultMapViewOnTouchListener(this,mapview) {
            @Override
            public boolean onSingleTapConfirmed(MotionEvent e) {
                graphicsOverlay.getGraphics().clear();
                Point mappoint=mapview.screenToLocation(new android.graphics.Point(Math.round(e.getX()),Math.round(e.getY())));
                incidentPoint=new Point(mappoint.getX(),mappoint.getY(),SpatialReferences.getWebMercator());
                Double lat = mappoint.getX();
                Double Long = mappoint.getY();
                Log.d(TAG, "lat and long: "+lat+"and"+Long);
                Log.d(TAG, "incident point: "+incidentPoint);
                SimpleMarkerSymbol mapPointsymbol = new SimpleMarkerSymbol((SimpleMarkerSymbol.Style.CIRCLE), 0xF2F56FF9, 12);
                Graphic mappointgraphic=new Graphic(incidentPoint,mapPointsymbol);
                graphicsOverlay.getGraphics().add(mappointgraphic);
                return true;
            }
        });

        */

//--------------------------------------------------------------------Routing code----------------------------------------------------------------------------
        /*
        SimpleMarkerSymbol originSymbol = new SimpleMarkerSymbol((SimpleMarkerSymbol.Style.CIRCLE), 0xFFFFFFFF, 12);
        Graphic originGraphic = new Graphic(new Point(80.2707,13.0827, SpatialReferences.getWgs84()), originSymbol);

        SimpleMarkerSymbol stopSymbol = new SimpleMarkerSymbol((SimpleMarkerSymbol.Style.CIRCLE), 0xFFFFFFFF, 8);
        Graphic stopGraphic = new Graphic(new Point(79.3200, 10.4232, SpatialReferences.getWgs84()), stopSymbol);

        SimpleMarkerSymbol destinationSymbol = new SimpleMarkerSymbol((SimpleMarkerSymbol.Style.CIRCLE), 0xFF000000, 12);
        Graphic destinationGraphic = new Graphic(new Point(77.5385, 8.0883, SpatialReferences.getWgs84()), destinationSymbol);

        Graphic routeGraphic = new Graphic();
        routeGraphic.setSymbol(new SimpleLineSymbol(SimpleLineSymbol.Style.SOLID, 0xFF0596FF, 4));

        //Instance of graphics overlay:
        GraphicsOverlay routegraphicsOverlay = new GraphicsOverlay();
        //Connect graphics overlay with mapview:
        mapview.getGraphicsOverlays().add(routegraphicsOverlay);

        routegraphicsOverlay.getGraphics().addAll(Arrays.asList(routeGraphic,originGraphic, stopGraphic, destinationGraphic));

        //get origin, destination and stopping lat long into a list:
        List<Stop> stops = routegraphicsOverlay.getGraphics()
                .stream()
                .filter(graphic -> graphic.getGeometry() != null)
                .map(graphic -> new Stop((Point) graphic.getGeometry()))
                .collect(Collectors.toList());

        //Integrate our api key into routing service:
        RouteTask routeTask = new RouteTask(MainActivity.this, ROUTING_API);
       //   RouteTask routeTask = new RouteTask(this,"https://route-api.arcgis.com/arcgis/rest/services/World/Route/NAServer/Route_World/solve?f=json&token=AAPK026861e3641744818c90e0ae6c270d12Dg_pj59bbpWVU2LC2BztipOpD4DAmGBtw-PbeQhCQGeiyl5xeW2izNTyHmGr-ND-&stops=80.2707,13.0827;78.1198,9.9252;77.5385,8.0883;&startTime=now&returnDirections=true&directionsLanguage=eng");
        //Instance for adding parameters:
        ListenableFuture<RouteParameters> routeParametersFuture = routeTask.createDefaultParametersAsync();

        routeParametersFuture.addDoneListener(() -> {
            RouteParameters routeParameters = null;
            try {
                routeParameters = routeParametersFuture.get();
                routeParameters.setStops(stops);
                routeParameters.setReturnDirections(true);
                routeParameters.setDirectionsLanguage("eng");
            } catch (Exception e) {
                e.printStackTrace();
            }

            //Instance for route result:
            ListenableFuture<RouteResult> routeResultFuture = routeTask.solveRouteAsync(routeParameters);

            //getting route geometry from result:
            routeResultFuture.addDoneListener(() -> {
                //getting route result:
                RouteResult routeResult = null;
                try {
                    routeResult = routeResultFuture.get();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                Log.d(TAG, "onCreate() returned: " + routeResult);
                //getting route from route result:
                Route route = routeResult.getRoutes().get(0);
                Log.d(TAG, "onCreate() returned: " + route);

                //Adding route geometry into our route graphics:
                routeGraphic.setGeometry(route.getRouteGeometry());
                Log.d(TAG,"length: "+ route.getTotalLength());
                Log.d(TAG,"total time: "+ route.getTotalTime());
                //getting directions from route:
                route.getDirectionManeuvers().forEach(step -> System.out.println(step.getDirectionText()));
            });
        });


 */

//---------------------------------------------------------------------Geo coding----------------------------------------------------------------------------
      /*  btn_search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mapview.getGraphicsOverlays().clear();
                //Instance of locatiorTask:
                LocatorTask locatorTask = new LocatorTask("https://geocode.arcgis.com/arcgis/rest/services/World/GeocodeServer");

                ListenableFuture<List<GeocodeResult>> results = locatorTask.geocodeAsync(searchView.getText().toString());
                results.addDoneListener(() -> {
                    try {
                        GeocodeResult result = results.get().get(0);
                        maplat=result.getDisplayLocation().getX();
                        mapLong=result.getDisplayLocation().getY();
                        Log.d(TAG, "corodinates: "+maplat+","+mapLong);
                        System.out.println("Found " + result.getLabel());
                        System.out.println("at " + result.getDisplayLocation());
                        System.out.println("with score " + result.getScore());
                        Point searchpoint=new Point(maplat,mapLong, SpatialReferences.getWgs84());
                        Viewpoint viewpoint=new Viewpoint(searchpoint,500000);
                        mapview.setViewpointAsync(viewpoint,3);
                        SimpleMarkerSymbol mapPointsymbol = new SimpleMarkerSymbol((SimpleMarkerSymbol.Style.CIRCLE), 0xFF000000, 12);
                        //Instance of graphics overlay:
                        GraphicsOverlay graphicsOverlay = new GraphicsOverlay();
                        //Connect graphics overlay with mapview:
                        mapview.getGraphicsOverlays().add(graphicsOverlay);
                        Graphic mappointgraphic=new Graphic(searchpoint,mapPointsymbol);
                        graphicsOverlay.getGraphics().add(mappointgraphic);

                    } catch (Exception e) {
                        Log.d(TAG, "error: "+e);
                    }
                });
            }
        });*/

    }
}