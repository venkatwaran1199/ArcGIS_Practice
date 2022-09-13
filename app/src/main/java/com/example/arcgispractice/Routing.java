package com.example.arcgispractice;

import static android.content.ContentValues.TAG;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.esri.arcgisruntime.concurrent.ListenableFuture;
import com.esri.arcgisruntime.geometry.Point;
import com.esri.arcgisruntime.geometry.SpatialReferences;
import com.esri.arcgisruntime.mapping.Viewpoint;
import com.esri.arcgisruntime.mapping.view.Graphic;
import com.esri.arcgisruntime.mapping.view.GraphicsOverlay;
import com.esri.arcgisruntime.mapping.view.MapView;
import com.esri.arcgisruntime.symbology.SimpleLineSymbol;
import com.esri.arcgisruntime.symbology.SimpleMarkerSymbol;
import com.esri.arcgisruntime.tasks.networkanalysis.Route;
import com.esri.arcgisruntime.tasks.networkanalysis.RouteParameters;
import com.esri.arcgisruntime.tasks.networkanalysis.RouteResult;
import com.esri.arcgisruntime.tasks.networkanalysis.RouteTask;
import com.esri.arcgisruntime.tasks.networkanalysis.Stop;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class Routing {
    private Context context;
    private double OriginLatitude,OriginLongitude,DestinationLatitude,DestinationLongitude;
    private MapView mapview;

    public Routing(Context context, MapView mapview) {
        this.context = context;
        this.mapview = mapview;
    }

    public interface RoutingResponceListener {
        void onError(String message);

        void onResponse(String LatLong);
    }

    private static final String ROUTING_API = "https://route-api.arcgis.com/arcgis/rest/services/World/Route/NAServer/Route_World";

    public void CalculateRoute(String OriginCoords,String DestinationCoords){

        String[] arrOforigin = OriginCoords.split(",", 2);
        String orinlong=arrOforigin[0];
        String orinlat=arrOforigin[1];
        String[] arrOfdest = DestinationCoords.split(",", 2);
        String destlong=arrOfdest[0];
        String destlat=arrOfdest[1];

        OriginLongitude=Double.parseDouble(orinlat);
        OriginLatitude=Double.parseDouble(orinlong);
        DestinationLongitude=Double.parseDouble(destlat);
        DestinationLatitude=Double.parseDouble(destlong);

        Log.d(TAG, "CalculateRoute origin: "+OriginCoords);
        Log.d(TAG, "CalculateRoute destination: "+DestinationCoords);
        Log.d(TAG, "CalculateRouteFinalorinlat: "+OriginLatitude);
        Log.d(TAG, "CalculateRouteFinalorinlong: "+OriginLongitude);
        Log.d(TAG, "CalculateRouteFinaldestlat: "+DestinationLatitude);
        Log.d(TAG, "CalculateRouteFinaldestlong: "+DestinationLongitude);

         SimpleMarkerSymbol originSymbol = new SimpleMarkerSymbol((SimpleMarkerSymbol.Style.CIRCLE), 0xFFFFFFFF, 12);
        Graphic originGraphic = new Graphic(new Point(OriginLatitude,OriginLongitude, SpatialReferences.getWgs84()), originSymbol);

//        SimpleMarkerSymbol stopSymbol = new SimpleMarkerSymbol((SimpleMarkerSymbol.Style.CIRCLE), 0xFFFFFFFF, 8);
//        Graphic stopGraphic = new Graphic(new Point(79.3200, 10.4232, SpatialReferences.getWgs84()), stopSymbol);

        SimpleMarkerSymbol destinationSymbol = new SimpleMarkerSymbol((SimpleMarkerSymbol.Style.CIRCLE), 0xFF000000, 12);
        Graphic destinationGraphic = new Graphic(new Point(DestinationLatitude,DestinationLongitude, SpatialReferences.getWgs84()), destinationSymbol);

        Graphic routeGraphic = new Graphic();
        routeGraphic.setSymbol(new SimpleLineSymbol(SimpleLineSymbol.Style.SOLID, 0xFF0596FF, 4));

        //Instance of graphics overlay:
        GraphicsOverlay routegraphicsOverlay = new GraphicsOverlay();
        //Connect graphics overlay with mapview:
        mapview.getGraphicsOverlays().add(routegraphicsOverlay);

        routegraphicsOverlay.getGraphics().addAll(Arrays.asList(routeGraphic,originGraphic,destinationGraphic));

        //get origin, destination and stopping lat long into a list:
        List<Stop> stops = routegraphicsOverlay.getGraphics()
                .stream()
                .filter(graphic -> graphic.getGeometry() != null)
                .map(graphic -> new Stop((Point) graphic.getGeometry()))
                .collect(Collectors.toList());

        //Integrate our api key into routing service:
            RouteTask routeTask = new RouteTask(context, ROUTING_API);

        //Instance for adding parameters:
            ListenableFuture<RouteParameters> routeParametersFuture = routeTask.createDefaultParametersAsync();

            routeParametersFuture.addDoneListener(() -> {
            RouteParameters routeParameters = null;
            try {
                routeParameters = routeParametersFuture.get();
                routeParameters.setStops(stops);
                routeParameters.setReturnDirections(true);
                routeParameters.setDirectionsLanguage("en");
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
                //getting route from route result:
                Route route = routeResult.getRoutes().get(0);

                //Adding route geometry into our route graphics:
                routeGraphic.setGeometry(route.getRouteGeometry());
                mapview.setViewpointGeometryAsync(route.getRouteGeometry());

                //getting directions from route:
                //route.getDirectionManeuvers().forEach(step -> System.out.println(step.getDirectionText()));
            });
        });
    }

}
