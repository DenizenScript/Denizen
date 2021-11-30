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
import com.denizenscript.denizencore.utilities.ReflectionHelper;
import net.md_5.bungee.api.chat.HoverEvent;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;

public abstract class NMSHandler {

    private static NMSHandler instance;
    private static NMSVersion version;
    private static JavaPlugin javaPlugin;

    public static boolean debugPackets = false;

    public static boolean initialize(JavaPlugin plugin) {
        javaPlugin = plugin;
        Class<?> serverClass = javaPlugin.getServer().getClass();
        ReflectionHelper.giveReflectiveAccess(serverClass, ReflectionHelper.class);
        String packageName = serverClass.getPackage().getName();
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
                if (version.isAtMost(NMSVersion.v1_16)) {
                    enchantmentHelper = new EnchantmentHelper();
                }
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

    public static AdvancementHelper advancementHelper;
    public static AnimationHelper animationHelper;
    public static BlockHelper blockHelper;
    public static ChunkHelper chunkHelper;
    public static CustomEntityHelper customEntityHelper;
    public static EntityHelper entityHelper;
    public static FishingHelper fishingHelper;
    public static ItemHelper itemHelper;
    public static SoundHelper soundHelper;
    public static PacketHelper packetHelper;
    public static ParticleHelper particleHelper;
    public static PlayerHelper playerHelper;
    public static WorldHelper worldHelper;
    public static EnchantmentHelper enchantmentHelper;

    public static AdvancementHelper getAdvancementHelper() {
        return advancementHelper;
    }

    public static AnimationHelper getAnimationHelper() {
        return animationHelper;
    }

    public static BlockHelper getBlockHelper() {
        return blockHelper;
    }

    public static ChunkHelper getChunkHelper() {
        return chunkHelper;
    }

    public static CustomEntityHelper getCustomEntityHelper() {
        return customEntityHelper;
    }

    public static EntityHelper getEntityHelper() {
        return entityHelper;
    }

    public static FishingHelper getFishingHelper() {
        return fishingHelper;
    }

    public static ItemHelper getItemHelper() {
        return itemHelper;
    }

    public static SoundHelper getSoundHelper() {
        return soundHelper;
    }

    public static PacketHelper getPacketHelper() {
        return packetHelper;
    }

    public static ParticleHelper getParticleHelper() {
        return particleHelper;
    }

    public static PlayerHelper getPlayerHelper() {
        return playerHelper;
    }

    public static WorldHelper getWorldHelper() {
        return worldHelper;
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

    public List<BiomeNMS> getBiomes(World world) {
        throw new UnsupportedOperationException();
    }

    public abstract BiomeNMS getBiomeNMS(World world, String name);

    public BiomeNMS getBiomeAt(Block block) {
        return NMSHandler.getInstance().getBiomeNMS(block.getWorld(), block.getBiome().name());
    }

    public abstract double[] getRecentTps();

    public abstract CompoundTag createCompoundTag(Map<String, Tag> value);

    public abstract int getPort();

    public abstract String getTitle(Inventory inventory);

    public abstract String stringForHover(HoverEvent hover);

    public abstract ArrayList<String> containerListFlags(PersistentDataContainer container, String prefix);

    public abstract boolean containerHas(PersistentDataContainer container, String key);

    public abstract String containerGetString(PersistentDataContainer container, String key);
}
