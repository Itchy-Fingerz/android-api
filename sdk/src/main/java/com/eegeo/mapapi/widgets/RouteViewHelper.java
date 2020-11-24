package com.eegeo.mapapi.widgets;

import android.location.Location;
import com.eegeo.mapapi.geometry.LatLng;
import com.eegeo.mapapi.services.routing.RouteStep;

import java.util.ArrayList;
import java.util.List;


public class RouteViewHelper {

    // TODO: This is incorrect implementation for finding unique coordinates. Update the following logic with something similar to C++ unique

    public static List<LatLng> removeCoincidentPoints(List<LatLng> coordinates) {
        List<LatLng> uniqueCoordinates = new ArrayList<>();

        for(int i=0; i<coordinates.size(); i++) {
            LatLng firstLocation = coordinates.get(i);
            boolean isUnique = true;
            for(int j=i+1; j<coordinates.size(); j++) {
                LatLng secondLocation = coordinates.get(j);
                if (areApproximatelyEqual(firstLocation, secondLocation)) {
                    isUnique = false;
                }
            }
            if (isUnique) {
                uniqueCoordinates.add(firstLocation);
            }
        }
        return uniqueCoordinates;
    }

    public static boolean areApproximatelyEqual(LatLng firstLocation, LatLng secondLocation) {
        final double epsilonSq = 1e-6;
        float[] results = new float[1];
        Location.distanceBetween(firstLocation.latitude, firstLocation.longitude, secondLocation.latitude, secondLocation.longitude, results);
        return results[0] <= epsilonSq;
    }

    public static RoutingPolylineCreateParams  makeNavRoutingPolylineCreateParams(List<LatLng> coordinates, int color, String indoorId, int indoorFloorId) {

        // TODO: Setting per point elevations for straight line with null perpoint elevations
        // TODO: In future I nned to verify if this null logic works fine

        return new RoutingPolylineCreateParams(coordinates, color, indoorId, indoorFloorId, null);
    }

    public static List<RoutingPolylineCreateParams> createLinesForRouteDirection(RouteStep routeStep, int color) {

        // Create single latLng coordinates

        List<RoutingPolylineCreateParams> results = new ArrayList<>();

        List<LatLng> uniqueCoordinates = RouteViewHelper.removeCoincidentPoints(routeStep.path);

        if(uniqueCoordinates.size() > 1) {
            RoutingPolylineCreateParams polylineCreateParams = new RoutingPolylineCreateParams(
                    uniqueCoordinates,
                    color,
                    routeStep.indoorId,
                    routeStep.indoorFloorId,
                    null
            );
            results.add(polylineCreateParams);
        }

        return results;
    }
}
