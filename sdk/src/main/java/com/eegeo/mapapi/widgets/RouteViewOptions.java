package com.eegeo.mapapi.widgets;


/**
 * Defines creation parameters for a RouteView. The styling options match with those of the PolylineOptions class.
 */
public final class RouteViewOptions {

    private float m_width = 10.f;
    private int m_colorARGB = 0xff000000;
    private int m_colorActiveARGB = 0xffff0000;
    private float m_miterLimit = 10.f;


    public RouteViewOptions() {

    }

    /**
     * Sets the width of the RouteView's polylines in screen pixels.
     *
     * @param width The width in screen pixels.
     * @return The RouteViewOptions object on which the method was called, with the new width set.
     */
    public RouteViewOptions width(float width) {
        m_width = width;
        return this;
    }

    /**
     * Sets the color of the RouteView's polylines as a 32-bit ARGB color. The default value is opaque black (0xff000000).
     *
     * @param color The color to use.
     * @return The RouteViewOptions object on which the method was called, with the new color set.
     */
    public RouteViewOptions color(int color) {
        m_colorARGB = color;
        return this;
    }

    /**
     * Sets the current step color of the RouteView's polylines as a 32-bit ARGB color. The default value is opaque red (0xff000000).
     *
     * @param color The color to use.
     * @return The RouteViewOptions object on which the method was called, with the new color set.
     */
    public RouteViewOptions activeColor(int color) {
        m_colorActiveARGB = color;
        return this;
    }

    /**
     * Sets the miter limit of the RouteView's polylines, the maximum allowed ratio between the length of a miter
     * diagonal at a join, and the line width.
     *
     * @param miterLimit The miter limit.
     * @return The RouteViewOptions object on which the method was called, with the new miter limit set.
     */
    public RouteViewOptions miterLimit(float miterLimit) {
        m_miterLimit = miterLimit;
        return this;
    }

    float getWidth() {
        return m_width;
    }

    int getColor() {
        return m_colorARGB;
    }

    int getActiveColor() { return m_colorActiveARGB; }

    float getMiterLimit() {
        return m_miterLimit;
    }
}

