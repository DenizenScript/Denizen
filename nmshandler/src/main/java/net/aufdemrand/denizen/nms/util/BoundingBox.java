package net.aufdemrand.denizen.nms.util;

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
}
