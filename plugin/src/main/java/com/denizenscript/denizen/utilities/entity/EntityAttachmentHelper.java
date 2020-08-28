package com.denizenscript.denizen.utilities.entity;

import com.denizenscript.denizen.utilities.DenizenAPI;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

import java.util.*;

public class EntityAttachmentHelper {

    public static HashMap<UUID, PlayerAttachMap> attachedEntityToData = new HashMap<>();
    public static HashMap<UUID, EntityAttachedToMap> toEntityToData = new HashMap<>();

    public static class AttachmentData {

        public Entity attached, to;

        public boolean offsetRelative;

        public float yawAngleOffset, pitchAngleOffset;

        public Location positionalOffset;

        public Vector visiblePosition;

        public boolean syncServer;

        public boolean noRotate;

        public BukkitTask checkTask;

        public UUID forPlayer;

        public Vector fixedForOffset(Vector offset, float yaw, float pitch) {
            if (offsetRelative) {
                return offset.clone().add(EntityAttachmentHelper.fixOffset(positionalOffset.toVector(), -yaw + yawAngleOffset, pitch + pitchAngleOffset));
            }
            else {
                return offset.clone().add(positionalOffset.toVector());
            }
        }

        public void startTask() {
            if (checkTask != null) {
                checkTask.cancel();
            }
            checkTask = new BukkitRunnable() {
                @Override
                public void run() {
                    if (!attached.isValid() || !to.isValid()) {
                        cancelAndRemove();
                        return;
                    }
                    if (syncServer) {
                        Location goal = to.getLocation();
                        if (positionalOffset != null) {
                            goal = fixedForOffset(goal.toVector(), goal.getYaw(), goal.getPitch()).toLocation(goal.getWorld());
                        }
                        if (noRotate) {
                            Location attachLoc = attached.getLocation();
                            goal.setYaw(attachLoc.getYaw());
                            goal.setPitch(attachLoc.getPitch());
                        }
                        attached.teleport(goal);
                    }
                }
            }.runTaskTimer(DenizenAPI.getCurrentInstance(), 1, 1);
        }

        public void removeFrom(PlayerAttachMap map) {
            if (map != null) {
                if (forPlayer == null) {
                    map.everyoneAttachment = null;
                }
                else {
                    if (map.playerToAttachment != null) {
                        map.playerToAttachment.remove(forPlayer);
                    }
                    map.autoNull();
                }
            }
        }

        public void cancelAndRemove() {
            if (checkTask != null) {
                checkTask.cancel();
            }
            checkTask = null;
            EntityAttachedToMap map = toEntityToData.get(to.getUniqueId());
            if (map != null) {
                PlayerAttachMap subMap = map.attachedToMap.get(attached.getUniqueId());
                removeFrom(subMap);
                if (subMap.everyoneAttachment == null && subMap.playerToAttachment == null) {
                    map.attachedToMap.remove(attached.getUniqueId());
                    if (map.attachedToMap.isEmpty()) {
                        toEntityToData.remove(to.getUniqueId());
                    }
                }
            }
            PlayerAttachMap attachMap = attachedEntityToData.get(attached.getUniqueId());
            if (attachMap != null) {
                removeFrom(attachMap);
                if (attachMap.everyoneAttachment == null && attachMap.playerToAttachment == null) {
                    attachedEntityToData.remove(attached.getUniqueId());
                }
            }
        }
    }

    public static class PlayerAttachMap {

        public Entity attached;

        public AttachmentData everyoneAttachment;

        public HashMap<UUID, AttachmentData> playerToAttachment;

        public AttachmentData getAttachment(UUID player) {
            if (playerToAttachment == null) {
                return everyoneAttachment;
            }
            AttachmentData data = playerToAttachment.get(player);
            if (data != null) {
                return data;
            }
            return everyoneAttachment;
        }

        public void cancelAll() {
            if (everyoneAttachment != null) {
                everyoneAttachment.cancelAndRemove();
            }
            if (playerToAttachment != null) {
                for (AttachmentData attachment : new HashSet<>(playerToAttachment.values())) {
                    attachment.cancelAndRemove();
                }
            }
        }

        public void autoNull() {
            if (playerToAttachment != null && playerToAttachment.isEmpty()) {
                playerToAttachment = null;
            }
        }
    }

    public static class EntityAttachedToMap {

        public HashMap<UUID, PlayerAttachMap> attachedToMap = new HashMap<>();
    }

    public static byte adaptedCompressedAngle(byte angle, float offset) {
        float angleF = ((float) angle) * (360F / 256F);
        angleF += offset;
        angleF %= 180;
        return (byte)((int)(angleF * (256F / 360F)));
    }

    public static byte compressAngle(float angle) {
        angle %= 180;
        return (byte)((int)(angle * (256F / 360F)));
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

    public static void removeAttachment(UUID attachedId, UUID forPlayer) {
        PlayerAttachMap map = attachedEntityToData.get(attachedId);
        if (map == null) {
            return;
        }
        if (forPlayer == null) {
            attachedEntityToData.remove(attachedId);
            map.cancelAll();
        }
        else {
            if (map.playerToAttachment != null) {
                AttachmentData data = map.playerToAttachment.get(forPlayer);
                if (data != null) {
                    data.cancelAndRemove();
                }
            }
        }
    }

    public static void registerAttachment(AttachmentData attachment) {
        removeAttachment(attachment.attached.getUniqueId(), attachment.forPlayer);
        PlayerAttachMap map = attachedEntityToData.get(attachment.attached.getUniqueId());
        if (map == null) {
            map = new PlayerAttachMap();
            map.attached = attachment.attached;
            attachedEntityToData.put(attachment.attached.getUniqueId(), map);
        }
        if (attachment.forPlayer == null) {
            map.everyoneAttachment = attachment;
        }
        else {
            if (map.playerToAttachment == null) {
                map.playerToAttachment = new HashMap<>();
            }
            map.playerToAttachment.put(attachment.forPlayer, attachment);
        }
        EntityAttachedToMap toMap = toEntityToData.get(attachment.to.getUniqueId());
        if (toMap == null) {
            toMap = new EntityAttachedToMap();
            toEntityToData.put(attachment.to.getUniqueId(), toMap);
        }
        toMap.attachedToMap.put(attachment.attached.getUniqueId(), map);
        attachment.startTask();
    }

    public static void forceAttachMove(Entity attached, Entity to, Vector offset, boolean matchRotation) {
        removeAttachment(attached.getUniqueId(), null);
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

    public static boolean denyOriginalPacketSend(UUID player, UUID entity) {
        PlayerAttachMap attached = EntityAttachmentHelper.attachedEntityToData.get(entity);
        if (attached == null) {
            return false;
        }
        AttachmentData data = attached.getAttachment(player);
        if (data == null) {
            return false;
        }
        if (data.to.getUniqueId().equals(player)) {
            return false;
        }
        return true;
    }
}
