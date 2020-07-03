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

        public Vector visiblePosition;

        public Vector fixedForOffset(Vector offset, float yaw, float pitch) {
            if (offsetRelative) {
                return EntityAttachmentHelper.fixOffset(positionalOffset.toVector(), -yaw, pitch);
            }
            else {
                return offset.add(positionalOffset.toVector());
            }
        }
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

    public static HashMap<UUID, AttachmentData> attachedEntityToData = new HashMap<>();
    public static HashMap<UUID, List<AttachmentData>> toEntityToData = new HashMap<>();

    public static void removeAttachment(UUID attachedId) {
        AttachmentData data = attachedEntityToData.get(attachedId);
        if (data != null) {
            List<AttachmentData> subAttachments = toEntityToData.get(data.to.getUniqueId());
            for (AttachmentData subData : subAttachments) {
                if (subData.attached.getUniqueId().equals(attachedId)) {
                    subAttachments.remove(subData);
                    break;
                }
            }
            if (subAttachments.isEmpty()) {
                toEntityToData.remove(data.to.getUniqueId());
            }
            attachedEntityToData.remove(attachedId);
        }
    }

    public static void registerAttachment(AttachmentData attachment) {
        attachedEntityToData.put(attachment.attached.getUniqueId(), attachment);
        List<AttachmentData> subAttachments = toEntityToData.get(attachment.to.getUniqueId());
        if (subAttachments == null) {
            subAttachments = new ArrayList<>();
            toEntityToData.put(attachment.to.getUniqueId(), subAttachments);
        }
        subAttachments.add(attachment);
    }

    public static void forceAttachMove(Entity attached, Entity to, Vector offset, boolean matchRotation) {
        removeAttachment(attached.getUniqueId());
        if (to == null) {
            return;
        }
        AttachmentData data = new AttachmentData();
        data.attached = attached;
        data.to = to;
        data.positionalOffset = offset == null ? null : offset.toLocation(null);
        data.offsetRelative = matchRotation;
        registerAttachment(data);
    }

    public static boolean shouldSendAttachOriginal(UUID player, UUID entity) {
        AttachmentData attached = EntityAttachmentHelper.attachedEntityToData.get(entity);
        return attached != null && !attached.to.getUniqueId().equals(player);
    }
}
