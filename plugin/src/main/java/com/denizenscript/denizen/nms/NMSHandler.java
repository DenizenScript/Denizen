package com.denizenscript.denizen.nms;

import com.denizenscript.denizen.nms.interfaces.*;
import com.denizenscript.denizen.nms.util.PlayerProfile;
import com.denizenscript.denizen.nms.util.jnbt.CompoundTag;
import com.denizenscript.denizen.nms.util.jnbt.Tag;
import com.denizenscript.denizen.nms.abstracts.AnimationHelper;
import com.denizenscript.denizen.nms.abstracts.BiomeNMS;
import com.denizenscript.denizen.nms.abstracts.BlockLight;
import com.denizenscript.denizen.nms.abstracts.ParticleHelper;
import com.denizenscript.denizen.nms.abstracts.ProfileEditor;
import com.denizenscript.denizen.nms.abstracts.Sidebar;
import com.denizenscript.denizen.utilities.packets.DenizenPacketHandler;
import org.bukkit.Location;
import org.bukkit.block.Biome;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.UUID;

public abstract class NMSHandler {

    private static NMSHandler instance;
    private static NMSVersion version;
    private static JavaPlugin javaPlugin;

    public static boolean debugPackets = false;

    public static boolean initialize(JavaPlugin plugin) {
        javaPlugin = plugin;
        String packageName = javaPlugin.getServer().getClass().getPackage().getName();
        int indexOfSubRevision = packageName.indexOf('R');
        if (indexOfSubRevision > 0) {
            // "v1_14_R1" should become "v1_14"
            packageName = packageName.substring(0, indexOfSubRevision - 1);
        }
        try {
            // Check if we support this MC version
            version = NMSVersion.valueOf(packageName.substring(packageName.lastIndexOf('.') + 1));
        }
        catch (Exception e) {
            version = NMSVersion.NOT_SUPPORTED;
            instance = null;
            return false;
        }
        try {
            // Get the class of our handler for this version
            final Class<?> clazz = Class.forName("com.denizenscript.denizen.nms." + version.name() + ".Handler");
            if (NMSHandler.class.isAssignableFrom(clazz)) {
                // Found and loaded - good to go!
                instance = (NMSHandler) clazz.newInstance();
                return true;
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        // Someone made an oopsie and didn't implement this version properly :(
        version = NMSVersion.NOT_SUPPORTED;
        instance = null;
        return false;
    }

    public static NMSHandler getInstance() {
        return instance;
    }

    public static NMSVersion getVersion() {
        return version;
    }

    public static JavaPlugin getJavaPlugin() {
        return javaPlugin;
    }

    public AdvancementHelper advancementHelper;
    public AnimationHelper animationHelper;
    public BlockHelper blockHelper;
    public ChunkHelper chunkHelper;
    public CustomEntityHelper customEntityHelper;
    public EntityHelper entityHelper;
    public FishingHelper fishingHelper;
    public ItemHelper itemHelper;
    public SoundHelper soundHelper;
    public PacketHelper packetHelper;
    public ParticleHelper particleHelper;
    public PlayerHelper playerHelper;
    public WorldHelper worldHelper;

    public static AdvancementHelper getAdvancementHelper() {
        return getInstance().advancementHelper;
    }

    public static AnimationHelper getAnimationHelper() {
        return getInstance().animationHelper;
    }

    public static BlockHelper getBlockHelper() {
        return getInstance().blockHelper;
    }

    public static ChunkHelper getChunkHelper() {
        return getInstance().chunkHelper;
    }

    public static CustomEntityHelper getCustomEntityHelper() {
        return getInstance().customEntityHelper;
    }

    public static EntityHelper getEntityHelper() {
        return getInstance().entityHelper;
    }

    public static FishingHelper getFishingHelper() {
        return getInstance().fishingHelper;
    }

    public static ItemHelper getItemHelper() {
        return getInstance().itemHelper;
    }

    public static SoundHelper getSoundHelper() {
        return getInstance().soundHelper;
    }

    public static PacketHelper getPacketHelper() {
        return getInstance().packetHelper;
    }

    public static ParticleHelper getParticleHelper() {
        return getInstance().particleHelper;
    }

    public static PlayerHelper getPlayerHelper() {
        return getInstance().playerHelper;
    }

    public static WorldHelper getWorldHelper() {
        return getInstance().worldHelper;
    }

    public boolean isCorrectMappingsCode() {
        return true;
    }

    public abstract void disableAsyncCatcher();

    public abstract void undisableAsyncCatcher();

    public abstract Sidebar createSidebar(Player player);

    public abstract BlockLight createBlockLight(Location location, int lightLevel, long ticks);

    public abstract PlayerProfile fillPlayerProfile(PlayerProfile playerProfile);

    public abstract PlayerProfile getPlayerProfile(Player player);

    public abstract ProfileEditor getProfileEditor();

    public abstract BiomeNMS getBiomeNMS(Biome biome);

    public abstract double[] getRecentTps();

    public abstract void enablePacketInterception(DenizenPacketHandler packetHandler);

    public abstract CompoundTag createCompoundTag(Map<String, Tag> value);

    public abstract int getPort();

    public abstract String getTitle(Inventory inventory);

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


    public HashMap<UUID, UUID> attachmentsA = new HashMap<>(); // Key follows value
    public HashMap<UUID, UUID> attachments2 = new HashMap<>(); // Value follows key
    public HashMap<UUID, Vector> attachmentOffsets = new HashMap<>();
    public HashSet<UUID> attachmentRotations = new HashSet<>();
    public HashMap<UUID, Vector> visiblePositions = new HashMap<>();

    public void forceAttachMove(Entity a, Entity b, Vector offset, boolean matchRotation) {
        if (attachmentsA.containsKey(a.getUniqueId())) {
            attachments2.remove(attachmentsA.get(a.getUniqueId()));
            attachmentsA.remove(a.getUniqueId());
            attachmentOffsets.remove(a.getUniqueId());
            attachmentRotations.remove(a.getUniqueId());
        }
        if (b == null) {
            return;
        }
        attachmentsA.put(a.getUniqueId(), b.getUniqueId());
        attachments2.put(b.getUniqueId(), a.getUniqueId());
        attachmentOffsets.put(a.getUniqueId(), offset);
        if (matchRotation) {
            attachmentRotations.add(a.getUniqueId());
        }
    }
}
