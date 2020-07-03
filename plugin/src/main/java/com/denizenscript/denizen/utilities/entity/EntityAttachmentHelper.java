package com.denizenscript.denizen.utilities.entity;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.util.Vector;

import java.util.*;

public class EntityAttachmentHelper {

    public static class AttachmentData {

        public Entity attached, to;

        public boolean offsetRelative, changeAngle;

        public float yawAngleOffset, pitchAngleOffset;

        public Location positionalOffset;
    }

    public static Vector fixOffset(Vector offset, double yaw, double pitch) {
        yaw = Math.toRadians(yaw);
        pitch = Math.toRadians(pitch);
        Vector offsetPatched = offset.clone();
        // x rotation
        double cosPitch = Math.cos(pitch);
        double sinPitch = Math.sin(pitch);
        double y1 = (offsetPatched.getY() * cosPitch) - (offsetPatched.getZ() * sinPitch);
        double z1 = (offsetPatched.getY() * sinPitch) + (offsetPatched.getZ() * cosPitch);
        offsetPatched.setY(y1);
        offsetPatched.setZ(z1);
        // y rotation
        double cosYaw = Math.cos(yaw);
        double sinYaw = Math.sin(yaw);
        double x2 = (offsetPatched.getX() * cosYaw) + (offsetPatched.getZ() * sinYaw);
        double z2 = (offsetPatched.getX() * -sinYaw) + (offsetPatched.getZ() * cosYaw);
        offsetPatched.setX(x2);
        offsetPatched.setZ(z2);
        return offsetPatched;
    }

    public static HashMap<UUID, UUID> attachmentsA = new HashMap<>(); // Key follows value
    public static HashMap<UUID, List<UUID>> attachments2 = new HashMap<>(); // Value follows key
    public static HashMap<UUID, Vector> attachmentOffsets = new HashMap<>();
    public static HashSet<UUID> attachmentRotations = new HashSet<>();
    public static HashMap<UUID, Vector> visiblePositions = new HashMap<>();

    public static void forceAttachMove(Entity a, Entity b, Vector offset, boolean matchRotation) {
        if (attachmentsA.containsKey(a.getUniqueId())) {
            UUID bid = attachmentsA.get(a.getUniqueId());
            List<UUID> subAttachments = attachments2.get(bid);
            subAttachments.remove(a.getUniqueId());
            if (subAttachments.isEmpty()) {
                attachments2.remove(bid);
            }
            attachmentsA.remove(a.getUniqueId());
            attachmentOffsets.remove(a.getUniqueId());
            attachmentRotations.remove(a.getUniqueId());
        }
        if (b == null) {
            return;
        }
        attachmentsA.put(a.getUniqueId(), b.getUniqueId());
        List<UUID> subAttachments = attachments2.get(b.getUniqueId());
        if (subAttachments == null) {
            subAttachments = new ArrayList<>();
            attachments2.put(b.getUniqueId(), subAttachments);
        }
        subAttachments.add(a.getUniqueId());
        attachmentOffsets.put(a.getUniqueId(), offset);
        if (matchRotation) {
            attachmentRotations.add(a.getUniqueId());
        }
    }
}
