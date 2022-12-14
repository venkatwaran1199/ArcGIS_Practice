package com.example.arcgispractice;

import static android.content.ContentValues.TAG;

import static com.example.arcgispractice.R.drawable.*;

import androidx.annotation.LongDef;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.core.content.ContextCompat;

import android.annotation.SuppressLint;
import android.graphics.drawable.BitmapDrawable;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.Selection;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
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
import com.esri.arcgisruntime.navigation.RouteTracker;
import com.esri.arcgisruntime.symbology.PictureMarkerSymbol;
import com.esri.arcgisruntime.symbology.SimpleFillSymbol;
import com.esri.arcgisruntime.symbology.SimpleLineSymbol;
import com.esri.arcgisruntime.symbology.SimpleMarkerSymbol;
import com.esri.arcgisruntime.tasks.geocode.GeocodeResult;
import com.esri.arcgisruntime.tasks.geocode.LocatorTask;
import com.esri.arcgisruntime.tasks.geocode.ReverseGeocodeParameters;
import com.esri.arcgisruntime.tasks.geocode.SuggestResult;
import com.esri.arcgisruntime.tasks.networkanalysis.Route;
import com.esri.arcgisruntime.tasks.networkanalysis.RouteParameters;
import com.esri.arcgisruntime.tasks.networkanalysis.RouteResult;
import com.esri.arcgisruntime.tasks.networkanalysis.RouteTask;
import com.esri.arcgisruntime.tasks.networkanalysis.Stop;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

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
    private EditText searchView,fromText,toText;
    private ImageView btn_search,btn_route;
    private double maplat,mapLong;
    private FloatingActionButton FAB_Navigation;
    private RelativeLayout routelayout;
    private boolean fabstatus;
    String FromLatLong,ToLatLong;

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_main);
        //initialize map:
        initviews();
        MapView mapview=findViewById(R.id.mapview);
        fabstatus=true;
        //Instance of locatiorTask:
        LocatorTask locatorTask = new LocatorTask("https://geocode.arcgis.com/arcgis/rest/services/World/GeocodeServer");

//--------------------------------------------------------------------Map Display------------------------------------------------------------------------------
           //API KEY Config:
        ArcGISRuntimeEnvironment.setApiKey(API_KEY);

        //Add basemap:
        ArcGISMap arcsgisMap=new ArcGISMap(BasemapStyle.ARCGIS_STREETS);
        mapview.setMap(arcsgisMap);

      //  PictureMarkerSymbol PMS=new PictureMarkerSymbol("https://img.icons8.com/officexs/344/marker.png");
        //Get icon from drawable  folder:


        //add viewpoint to basemap:
        Point point=new Point(79.3200,10.4232, SpatialReferences.getWgs84());
        Viewpoint viewpoint=new Viewpoint(point,500000);
        mapview.setViewpointAsync(viewpoint,3);



        //Instance of graphics overlay:
        GraphicsOverlay graphicsOverlay = new GraphicsOverlay();
        //Connect graphics overlay with mapview:
        mapview.getGraphicsOverlays().add(graphicsOverlay);
//        BitmapDrawable pinStarBlueDrawable = (BitmapDrawable) ContextCompat.getDrawable(this, navigation);
//        if (pinStarBlueDrawable != null) {
//            // create the picture marker symbol asynchronously
//            ListenableFuture<PictureMarkerSymbol> pinStarBlueSymbolFuture = PictureMarkerSymbol.createAsync(pinStarBlueDrawable);
//            // listen for the create to finish
//            pinStarBlueSymbolFuture.addDoneListener(() -> {
//                try {
//                    // get the created picture marker symbol
//                    PictureMarkerSymbol pinStarBlueSymbol = pinStarBlueSymbolFuture.get();
//
//                    // set the size, if not set the image will be auto sized based on its size in pixels
//                    pinStarBlueSymbol.setHeight(30);
//                    pinStarBlueSymbol.setWidth(30);
//                    // set the offset, to align the base of the symbol aligns with the point geometry
//                    pinStarBlueSymbol.setOffsetY(10);
//                    //[DocRef: END]
//                    // add a new graphic with the same location as the initial viewpoint
//                    Point pinStarBluePoint = new Point(80.2707, 13.0827, SpatialReferences.getWgs84());
//                    Graphic pinStarBlueGraphic = new Graphic(pinStarBluePoint, pinStarBlueSymbol);
//                    graphicsOverlay.getGraphics().add(pinStarBlueGraphic);
//                } catch (Exception e) {
//                    String error = "Error loading picture marker symbol: " + e.getMessage();
//                    Log.e(TAG, error);
//                    Toast.makeText(this, error, Toast.LENGTH_LONG).show();
//                }
//            });
//        }


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


//---------------------------------------------------------------------Routing parameters--------------------------------------------------------
        FAB_Navigation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (fabstatus==true){
                    routelayout.setVisibility(View.VISIBLE);
                    searchView.setVisibility(View.INVISIBLE);
                    btn_search.setVisibility(View.INVISIBLE);
                    fromText.setFocusable(true);
                    FAB_Navigation.setImageResource(ic_baseline_minimize_24);
                    fabstatus=false;
                }
                else{
                    routelayout.setVisibility(View.INVISIBLE);
                    searchView.setVisibility(View.VISIBLE);
                    btn_search.setVisibility(View.VISIBLE);
                    FAB_Navigation.setImageResource(R.drawable.right_arrow);
                    fabstatus=true;
                }
            }
        });

//---------------------------------------------------------------------Geo coding----------------------------------------------------------------------------
        btn_search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(searchView.getText().toString().isEmpty()){
                    Toast.makeText(MainActivity.this, "Enter place...", Toast.LENGTH_SHORT).show();
                }else{
                    ListenableFuture<List<GeocodeResult>> results = locatorTask.geocodeAsync(searchView.getText().toString());
                    results.addDoneListener(() -> {
                        try {
                            GeocodeResult result = results.get().get(0);
                            maplat=result.getDisplayLocation().getX();
                            mapLong=result.getDisplayLocation().getY();
                            Log.d(TAG, "corodinates: "+maplat+","+mapLong);
                            Point searchpoint=new Point(maplat,mapLong, SpatialReferences.getWgs84());
                            Viewpoint viewpoint=new Viewpoint(searchpoint,5000);
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
            }
        });

//---------------------------------------------------------------------getting lat and long------------------------------------------------------
        btn_route.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fromText.getText().toString();
                toText.getText().toString();

                    //Getting lat long for the places:
                    Geocoding GeocodingTask=new Geocoding();
                    if((fromText.getText().toString().isEmpty()&toText.getText().toString().isEmpty())){
                    Toast.makeText(MainActivity.this, "Enter valid place", Toast.LENGTH_SHORT).show();
                }
                    else{
                    GeocodingTask.GeocodingTask(fromText.getText().toString(), new Geocoding.GeocodingResponceListener() {
                        @Override
                        public void onError(String message) {
                            Log.d(TAG, "onErrorFrom: "+message);
                        }

                        @Override
                        public void onResponse(String fromLatLong) {
                            FromLatLong=fromLatLong;
                        }
                    });
                    GeocodingTask.GeocodingTask(toText.getText().toString(), new Geocoding.GeocodingResponceListener() {
                                @Override
                                public void onError(String message) {
                                    Log.d(TAG, "onErrorTo: "+message);
                                }

                                @Override
                                public void onResponse(String toLatLong) {
                                    ToLatLong=toLatLong;
                                    Routing route=new Routing(MainActivity.this,(MapView) mapview);
                                    route.CalculateRoute(FromLatLong, ToLatLong, new Routing.RoutingResponceListener() {
                                        @Override
                                        public void onError(String message) {
                                            Toast.makeText(MainActivity.this,"error:"+ message, Toast.LENGTH_SHORT).show();
                                        }

                                        @Override
                                        public void onResponse(String LatLong) {
                                            routelayout.setVisibility(View.INVISIBLE);
                                            searchView.setVisibility(View.VISIBLE);
                                            btn_search.setVisibility(View.VISIBLE);
                                            FAB_Navigation.setImageResource(R.drawable.right_arrow);
                                            fabstatus=true;
                                            Toast.makeText(MainActivity.this, "Routing successfull", Toast.LENGTH_SHORT).show();
                                        }
                                    });
                                }
                            });
                }
            }
        });

//----------------------------------------------------------------------reverse geocode----------------------------------------------------------
        ListenableFuture<List<GeocodeResult>> new_results=locatorTask.reverseGeocodeAsync(point);
        new_results.addDoneListener(()->{
            try {
                List<GeocodeResult> newanswer=new_results.get();
                GeocodeResult geocode = newanswer.get(0);
                Toast.makeText(this, "answer: "+ geocode.getAttributes().get("LongLabel").toString(), Toast.LENGTH_SHORT).show();
                Log.d(TAG, "onCreatenewanswer: "+geocode.getAttributes().get("City").toString());
            } catch (Exception e) {
                Toast.makeText(this, "error", Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }
        });
        
    }



//Initializing members:
    private void initviews() {

        searchView=findViewById(R.id.searchView);
        btn_search=findViewById(R.id.searchbutton);
        FAB_Navigation=findViewById(R.id.FAB_NAV);
        fromText=findViewById(R.id.Fromtext);
        toText=findViewById(R.id.Totext);
        btn_route=findViewById(R.id.calculateroute);
        routelayout=findViewById(R.id.routelayout);
    }

}