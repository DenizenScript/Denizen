package com.denizenscript.denizen.nms.util;

import org.bukkit.util.Vector;

public class BoundingBox {

    private Vector low;
    private Vector high;

    public BoundingBox(Vector location, Vector size) {
        this.low = location;
        this.high = size;
    }

    public Vector getLow() {
        return low;
    }

    public Vector getHigh() {
        return high;
    }

    public double distanceSquared(Vector point) {
        double x = Math.max(low.getX(), Math.min(point.getX(), high.getX()));
        double y = Math.max(low.getY(), Math.min(point.getY(), high.getY()));
        double z = Math.max(low.getZ(), Math.min(point.getZ(), high.getZ()));
        double xOff = x - point.getX();
        double yOff = y - point.getY();
        double zOff = z - point.getZ();
        return xOff * xOff + yOff * yOff + zOff * zOff;
    }
}
