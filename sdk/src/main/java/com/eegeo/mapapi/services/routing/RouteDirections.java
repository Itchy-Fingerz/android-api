package com.eegeo.mapapi.services.routing;

import com.eegeo.mapapi.geometry.LatLng;


/**
 * Directions information for a RouteStep of a Route.
 */
public class RouteDirections {

    /**
     * The type of motion to make at this step. For example, "turn".
     */
    public final String type;

    /**
     * A modification to the type. For example, "sharp right".
     */
    public final String modifier;

    /**
     * The LatLng this direction applies at.
     */
    public final LatLng location;

    /**
     * The bearing in degrees relative to north before taking this direction.
     */
    public final double bearingBefore;

    /**
     * The bearing in degrees relative to north after taking this direction.
     */
    public final double bearingAfter;

    RouteDirections(
            final String type,
            final String modifier,
            final LatLng location,
            final double bearingBefore,
            final double bearingAfter)
    {
        this.type = type;
        this.modifier = modifier;
        this.location = location;
        this.bearingBefore = bearingBefore;
        this.bearingAfter = bearingAfter;
    }
}

