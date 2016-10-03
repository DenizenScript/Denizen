package net.aufdemrand.denizen.nms.util;

import org.bukkit.util.Vector;

public class BoundingBox {

    private Vector position;
    private Vector size;

    public BoundingBox(Vector location, Vector size) {
        this.position = location;
        this.size = size;
    }

    public Vector getPosition() {
        return position;
    }

    public Vector getSize() {
        return size;
    }
}
