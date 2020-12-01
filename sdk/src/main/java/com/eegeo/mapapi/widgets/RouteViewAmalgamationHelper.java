package com.eegeo.mapapi.widgets;

import android.util.Pair;

import com.eegeo.mapapi.geometry.LatLng;
import com.eegeo.mapapi.polylines.PolylineOptions;

import java.util.ArrayList;
import java.util.List;

public class RouteViewAmalgamationHelper {

  public static List<PolylineOptions> createPolylines(List<RoutingPolylineCreateParams> params, float width, float miterLimit) {

    List<PolylineOptions> result = new ArrayList<>();
    List<Pair<Integer, Integer>> ranges = RouteViewAmalgamationHelper.buildAmalgamationRanges(params);

    for (Pair<Integer, Integer> range : ranges) {
      PolylineOptions polylineOption = RouteViewAmalgamationHelper.createAmalgamatedPolylineForRange(params, range.first, range.second, width, miterLimit);
      if (polylineOption != null) {
        result.add(polylineOption);
      }
    }
    return result;
  }


  public static List<Pair<Integer, Integer>> buildAmalgamationRanges(List<RoutingPolylineCreateParams> polylineCreateParams) {

    List<Pair<Integer, Integer>> ranges = new ArrayList<>();

    if (polylineCreateParams.isEmpty()) {
      return ranges;
    }

    int rangeStart = 0;
    for (int i = 1; i < polylineCreateParams.size(); ++i) {
      RoutingPolylineCreateParams a = polylineCreateParams.get(i - 1);
      RoutingPolylineCreateParams b = polylineCreateParams.get(i);

      if (!RouteViewAmalgamationHelper.canAmalgamate(a, b)) {
        ranges.add(new Pair<>(rangeStart, i));
        rangeStart = i;
      }
    }
    ranges.add(new Pair<>(rangeStart, polylineCreateParams.size()));

    return ranges;

  }

  public static boolean canAmalgamate(RoutingPolylineCreateParams a, RoutingPolylineCreateParams b) {

    if (a.isIndoor != b.isIndoor) {
      return false;
    }

    if (!a.indoorMapId.equals(b.indoorMapId)) {
      return false;
    }

    if (a.indoorFloorId != b.indoorFloorId) {
      return false;
    }

    if (a.color != b.color) {
      return false;
    }

    return true;
  }


  public static PolylineOptions createAmalgamatedPolylineForRange(List<RoutingPolylineCreateParams> polylineCreateParams, Integer rangeStartIndex, Integer rangeEndIndex, float width, float miterLimit) {
    assert (rangeStartIndex < rangeEndIndex);
    assert (rangeStartIndex >= 0);
    assert (rangeEndIndex <= polylineCreateParams.size());

    List<LatLng> joinedCoordinates = new ArrayList<>();
    List<Double> joinedPerPointElevations = new ArrayList<>();

    boolean anyPerPointElevations = false;

    for (int i = rangeStartIndex; i < rangeEndIndex; ++i) {
      RoutingPolylineCreateParams params = polylineCreateParams.get(i);
      List<LatLng> coordinates = params.path;

      joinedCoordinates.addAll(coordinates);

      if (params.perPointElevations != null) {
        anyPerPointElevations = true;
      }
    }

    if (anyPerPointElevations) {

      for (int i = rangeStartIndex; i < rangeEndIndex; ++i) {
        RoutingPolylineCreateParams params = polylineCreateParams.get(i);
        List<Double> perPointElevations = params.perPointElevations;

        if(perPointElevations == null) {

          for(int x = 0; x < params.path.size(); x++) {
            joinedPerPointElevations.add(0.0);
          }

        } else {
          joinedPerPointElevations.addAll(params.perPointElevations);
        }
      }

      RouteViewHelper.removeCoincidentPointsWithElevations(joinedCoordinates, joinedPerPointElevations);

    } else {
      RouteViewHelper.removeCoincidentPoints(joinedCoordinates);
    }

    if (joinedCoordinates.size() > 1) {

      RoutingPolylineCreateParams param = polylineCreateParams.get(rangeStartIndex);

      PolylineOptions options = new PolylineOptions()
              .color(param.color)
              .width(width)
              .miterLimit(miterLimit);

      if (param.isIndoor) {
        options.indoor(param.indoorMapId, param.indoorFloorId);
      }

      for(int i=0; i<joinedCoordinates.size(); i++) {
        LatLng point = joinedCoordinates.get(i);
        if(anyPerPointElevations) {
          Double elevation = joinedPerPointElevations.get(i);
          options.add(point, elevation);
        } else {
          options.add(point);
        }
      }

      return options;
    }

    return null;
  }
}

