package com.eegeo.mapapi.widgets;

import android.location.Location;
import android.util.Pair;

import com.eegeo.mapapi.geometry.LatLng;
import com.eegeo.mapapi.services.routing.RouteStep;

import java.util.ArrayList;
import java.util.Arrays;
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

    // TODO: This is incorrect implementation for finding unique coordinates. Update the following logic with something similar to C++ unique

    public static List<Pair<LatLng, Double>> removeCoincidentPointsWithElevations(List<LatLng> coordinates, List<Double> perPointElevations) {

        assert(coordinates.size() == perPointElevations.size());

        List<Pair<LatLng, Double>> pairsList = new ArrayList<>();

        for(int i=0; i < coordinates.size(); i++) {
            LatLng coordinate = coordinates.get(i);
            Double elevation = perPointElevations.get(i);
            pairsList.add(new Pair<>(coordinate, elevation));
        }

        List<Pair<LatLng, Double>> uniqueCoordinates = new ArrayList<>();

        for(int i=0; i<pairsList.size(); i++) {
            Pair<LatLng, Double> firstLocation = pairsList.get(i);

            boolean isUnique = true;
            for(int j=i+1; j<pairsList.size(); j++) {
                Pair<LatLng, Double> secondLocation = pairsList.get(j);
                if (areCoordinateElevationPairApproximatelyEqual(firstLocation, secondLocation)) {
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

    public static boolean areCoordinateElevationPairApproximatelyEqual(Pair<LatLng, Double> a, Pair<LatLng, Double> b) {
        final double elevationEpsilon = 1e-3;

        if(!areApproximatelyEqual(a.first, b.first)) {
            return false;
        }

        return Math.abs(a.second - b.second) < elevationEpsilon;
    }

    public static RoutingPolylineCreateParams  makeNavRoutingPolylineCreateParams(List<LatLng> coordinates, int color, String indoorId, int indoorFloorId) {
        return new RoutingPolylineCreateParams(coordinates, color, indoorId, indoorFloorId, null);
    }

    public static RoutingPolylineCreateParams  makeNavRoutingPolylineCreateParams(List<LatLng> coordinates, int color, String indoorId, int indoorFloorId, float heightStart, float heightEnd) {
        return new RoutingPolylineCreateParams(coordinates, color, indoorId, indoorFloorId, Arrays.asList(Double.valueOf(heightStart), Double.valueOf(heightEnd)));
    }

    public static List<RoutingPolylineCreateParams> createLinesForRouteDirection(RouteStep routeStep, int color) {
        List<RoutingPolylineCreateParams> results = new ArrayList<>();

        List<LatLng> uniqueCoordinates = RouteViewHelper.removeCoincidentPoints(routeStep.path);
        if(uniqueCoordinates.size() > 1) {
            RoutingPolylineCreateParams polylineCreateParams = makeNavRoutingPolylineCreateParams(uniqueCoordinates, color, routeStep.indoorId, routeStep.indoorFloorId);
            results.add(polylineCreateParams);
        }

        return results;
    }

    public static List<RoutingPolylineCreateParams> createLinesForRouteDirection(RouteStep routeStep, int forwardColor, int backwardColor, int splitIndex, LatLng closestPointOnPath) {
        int coordinatesSize = routeStep.path.size();
        boolean hasReachedEnd = splitIndex == (routeStep.path.size()-1);

        if (hasReachedEnd) {
            return createLinesForRouteDirection(routeStep, backwardColor);
        }
        else
        {
            List<RoutingPolylineCreateParams> results = new ArrayList<>();

            int forwardPathSize = coordinatesSize - (splitIndex + 1);
            int backwardPathSize = coordinatesSize - forwardPathSize;

            List<LatLng> forwardPath = new ArrayList<>(forwardPathSize);
            List<LatLng> backwardPath = new ArrayList<>(backwardPathSize);

            //Forward path starts with the split point
            forwardPath.add(closestPointOnPath);

            for (int i = 0; i < coordinatesSize; i++) {
                if(i<=splitIndex) {
                    backwardPath.add(routeStep.path.get(i));
                } else {
                    forwardPath.add(routeStep.path.get(i));
                }
            }

            //Backward path ends with the split point
            backwardPath.add(closestPointOnPath);

            backwardPath = removeCoincidentPoints(backwardPath);
            forwardPath = removeCoincidentPoints(forwardPath);

            if(backwardPath.size() > 1) {
                results.add(makeNavRoutingPolylineCreateParams(backwardPath, backwardColor, routeStep.indoorId, routeStep.indoorFloorId));
            }

            if(forwardPath.size() > 1) {
                results.add(makeNavRoutingPolylineCreateParams(forwardPath, forwardColor, routeStep.indoorId, routeStep.indoorFloorId));
            }

            return results;
        }
    }

    public static List<RoutingPolylineCreateParams> createLinesForFloorTransition(RouteStep routeStep, int floorBefore, int floorAfter, int color) {
        float verticalLineHeight = 5.0f;
        float lineHeight = (floorAfter > floorBefore) ? verticalLineHeight : -verticalLineHeight;

        int coordinateCount = routeStep.path.size();
        assert (coordinateCount >= 2): "Can't make a floor transition line with a single point";

        List<LatLng> startCoords = new ArrayList<>(2);
        startCoords.add(routeStep.path.get(0));
        startCoords.add(routeStep.path.get(1));

        List<LatLng> endCoords = new ArrayList<>(2);
        endCoords.add(routeStep.path.get(coordinateCount-2));
        endCoords.add(routeStep.path.get(coordinateCount-1));

        List<RoutingPolylineCreateParams> results = new ArrayList<>(2);
        results.add(makeNavRoutingPolylineCreateParams(startCoords, color, routeStep.indoorId, floorBefore, 0.0f, lineHeight));
        results.add(makeNavRoutingPolylineCreateParams(endCoords, color, routeStep.indoorId, floorAfter, -lineHeight, 0.0f));

        return results;
    }
}
