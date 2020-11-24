package com.eegeo.mapapi.widgets;

import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;

import android.support.annotation.UiThread;

import com.eegeo.mapapi.EegeoMap;
import com.eegeo.mapapi.geometry.LatLng;
import com.eegeo.mapapi.polylines.Polyline;
import com.eegeo.mapapi.polylines.PolylineOptions;
import com.eegeo.mapapi.services.routing.*;


public class RouteView {

    private static double VERTICAL_LINE_HEIGHT = 5.0;

    private EegeoMap m_map = null;
    private Route m_route = null;
    private List<Polyline> m_polylines = new ArrayList();
    private boolean m_currentlyOnMap = false;

    private float m_width;
    private int m_colorARGB;
    private int m_forwardPathColorARGB;
    private float m_miterLimit;

    private Map<Integer, List<RoutingPolylineCreateParams>> m_routeStepToPolylineCreateParams = new HashMap<>();

    /**
     * Create a new RouteView for the given route and options, and add it to the map.
     *
     * @param map The EegeoMap to draw this RouteView on.
     * @param route The Route to display.
     * @param options Options for styling the route.
     */
    public RouteView(EegeoMap map, Route route, RouteViewOptions options) {
        this.m_map = map;
        this.m_route = route;
        this.m_width = options.getWidth();
        this.m_colorARGB = options.getColor();
        this.m_forwardPathColorARGB = options.getForwardPathColor();
        this.m_miterLimit = options.getMiterLimit();
//        addToMap();
    }


    /**
     * Add this RouteView back on to the map, if it has been removed.
     */
    public void addToMap() {
        for (RouteSection section: m_route.sections) {
            List<RouteStep> steps = section.steps;

            for (int i=0; i<steps.size(); ++i) {
                RouteStep step = steps.get(i);

                if (step.path.size() < 2) {
                    continue;
                }

                if (step.isMultiFloor) {
                    boolean isValidTransition = i > 0 && i < (steps.size() - 1) && step.isIndoors;

                    if (!isValidTransition) {
                        continue;
                    }

                    RouteStep stepBefore = steps.get(i-1);
                    RouteStep stepAfter = steps.get(i+1);

                    // TODO: Multi floor case is not being handleed at the moment
                    //addLinesForFloorTransition(step, stepBefore, stepAfter, false);

                }
                else {
//                    addLinesForRouteStep(step);

                    // TODO: verifying this logic to avoid flatten step index in this method
                    this.addLineCreationParamsForStep(step);
                }
            }
        }

        this.refreshPolylines();
        
        m_currentlyOnMap = true;
    }

    private void addLineCreationParamsForStep(RouteStep routeStep) {
        if(routeStep.path.size() < 2) {
            return;
        }
        m_routeStepToPolylineCreateParams.put(m_routeStepToPolylineCreateParams.size(), RouteViewHelper.createLinesForRouteDirection(routeStep, m_colorARGB));
    }

    private void refreshPolylines() {

        this.removeFromMap();
        assert (m_polylines.size() == 0);

        List<RoutingPolylineCreateParams> allPolylineCreateParams = new ArrayList<>();

        for(int i=0; i< m_routeStepToPolylineCreateParams.size(); i++) {
            allPolylineCreateParams.addAll(m_routeStepToPolylineCreateParams.get(i));
        }

        List<PolylineOptions> polyLineOptionsList = RouteViewAmalgamationHelper.createPolylines(allPolylineCreateParams, m_width, m_miterLimit);

        for(PolylineOptions polyLineOption: polyLineOptionsList) {
            Polyline routeLine = m_map.addPolyline(polyLineOption);
            m_polylines.add(routeLine);
        }

    }


    private PolylineOptions basePolylineOptions(RouteStep step) {
        PolylineOptions options = new PolylineOptions()
            .color(m_colorARGB)
            .width(m_width)
            .miterLimit(m_miterLimit);

        if (step.isIndoors) {
            options.indoor(step.indoorId, step.indoorFloorId);
        }

        return options;
    }

    private void addLinesForRouteStep(RouteStep step) {
        PolylineOptions options = basePolylineOptions(step);

        for (LatLng point: step.path) {
            options.add(point);
        }

        Polyline routeLine = m_map.addPolyline(options);
        m_polylines.add(routeLine);
    }

    private void addLinesForFloorTransition(RouteStep step, RouteStep stepBefore, RouteStep stepAfter, boolean isActiveStep) {
        int floorBefore = stepBefore.indoorFloorId;
        int floorAfter = stepAfter.indoorFloorId;
        double lineHeight = (floorAfter > floorBefore) ? VERTICAL_LINE_HEIGHT : -VERTICAL_LINE_HEIGHT;

        m_polylines.add(makeVerticalLine(step, floorBefore, lineHeight, isActiveStep));
        m_polylines.add(makeVerticalLine(step, floorAfter, -lineHeight, isActiveStep));
    }

    private Polyline makeVerticalLine(RouteStep step, int floor, double height, boolean isActiveStep) {
        PolylineOptions options = basePolylineOptions(step)
                .indoor(step.indoorId, floor)
                .add(step.path.get(0), 0.0)
                .add(step.path.get(1), height);

        if (isActiveStep) {
            options.color(m_forwardPathColorARGB);
        }
        return m_map.addPolyline(options);
    }

    /**
     * Update the progress of turn by turn navigation on route.
     *
     * @param sectionIndex The index of current RouteSection.
     * @param stepIndex The index of current RouteStep.
     * @param closestPointOnRoute Closest point on the route in PointOnRoute.
     * @param indexOfPathSegmentStartVertex Vertex index where the path segment starts for the projected point. Can be used to separate traversed path.
     */

    public void updateRouteProgress(int sectionIndex, int stepIndex, LatLng closestPointOnRoute, int indexOfPathSegmentStartVertex) {
        for (Polyline polyline: m_polylines) {
            m_map.removePolyline(polyline);
        }

        for (int x=0; x<m_route.sections.size(); ++x) {
            List<RouteStep> steps = m_route.sections.get(x).steps;

            for (int i=0; i<steps.size(); ++i) {
                RouteStep step = steps.get(i);
                boolean isActiveStep = sectionIndex == x && stepIndex == i;

                if (step.path.size() < 2) {
                    continue;
                }
                if (step.isMultiFloor) {
                    boolean isValidTransition = i > 0 && i < (steps.size() - 1) && step.isIndoors;
                    if (!isValidTransition) {
                        continue;
                    }
                    RouteStep stepBefore = steps.get(i-1);
                    RouteStep stepAfter = steps.get(i+1);
                    addLinesForFloorTransition(step, stepBefore, stepAfter, isActiveStep);
                }
                else {
                    if(isActiveStep) {
                        addLinesForRouteStep(step, indexOfPathSegmentStartVertex, closestPointOnRoute);
                    } else {
                        addLinesForRouteStep(step);
                    }
                }
            }
        }
    }

    private void addLinesForRouteStep(RouteStep step, int splitIndex, LatLng closestPointOnPath) {
        List<LatLng> backPathArray = new ArrayList<>(step.path.subList(0, splitIndex+1));
        backPathArray.add(closestPointOnPath);
        addLinesForActiveStepSegment(step, backPathArray, false);

        List<LatLng> forwardPathArray = new ArrayList<>();
        forwardPathArray.add(closestPointOnPath);
        forwardPathArray.addAll(step.path.subList(splitIndex+1, step.path.size()));
        addLinesForActiveStepSegment(step, forwardPathArray, true);
    }

    private void addLinesForActiveStepSegment(RouteStep step, List<LatLng> pathSegment, boolean isForward) {
        List<LatLng> filteredPathSegment = RouteViewHelper.removeCoincidentPoints(pathSegment);
        if(filteredPathSegment.size() >= 2) {
            PolylineOptions basePolylineOptions = basePolylineOptions(step);
            if(isForward)
            {
                basePolylineOptions.color(m_forwardPathColorARGB);
            }
            for (LatLng point : filteredPathSegment) {
                basePolylineOptions.add(point);
            }
            Polyline routeLine = m_map.addPolyline(basePolylineOptions);
            m_polylines.add(routeLine);
        }
    }

    /**
     * Remove this RouteView from the map.
     */
    public void removeFromMap() {
        for (Polyline polyline: m_polylines) {
            m_map.removePolyline(polyline);
        }
        m_polylines.clear();
        m_currentlyOnMap = false;
    }


    /**
     * Sets the width of this RouteView's polylines.
     *
     * @param width The width of the polyline in screen pixels.
     */
    @UiThread
    public void setWidth(float width) {
        m_width = width;
        for (Polyline polyline: m_polylines) {
            polyline.setWidth(m_width);
        }
    }

    /**
     * Sets the color for this RouteView's polylines.
     *
     * @param color The color of the polyline as a 32-bit ARGB color.
     */
    @UiThread
    public void setColor(int color) {
        m_colorARGB = color;
        for (Polyline polyline: m_polylines) {
            polyline.setColor(m_colorARGB);
        }
    }

    /**
     * Sets the miter limit of this RouteView's polylines.
     *
     * @param miterLimit the miter limit, as a ratio between maximum allowed miter join diagonal
     *                   length and the line width.
     */
    @UiThread
    public void setMiterLimit(float miterLimit) {
        m_miterLimit = miterLimit;
        for (Polyline polyline: m_polylines) {
            polyline.setMiterLimit(m_miterLimit);
        }
    }
}

