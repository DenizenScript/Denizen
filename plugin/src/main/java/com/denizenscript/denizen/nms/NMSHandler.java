package com.denizenscript.denizen.nms;

import com.denizenscript.denizen.nms.abstracts.*;
import com.denizenscript.denizen.nms.interfaces.*;
import com.denizenscript.denizen.nms.util.PlayerProfile;
import com.denizenscript.denizen.nms.util.jnbt.CompoundTag;
import com.denizenscript.denizen.nms.util.jnbt.Tag;
import net.md_5.bungee.api.chat.HoverEvent;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public abstract class NMSHandler {

    public static NMSHandler instance;
    private static NMSVersion version;
    private static JavaPlugin javaPlugin;

    public static boolean debugPackets = false;
    public static String debugPacketFilter = "";

    public static boolean initialize(JavaPlugin plugin) {
        javaPlugin = plugin;
        String bukkitVersion = Bukkit.getBukkitVersion();
        for (NMSVersion potentialVersion : NMSVersion.values()) {
            if (bukkitVersion.startsWith(potentialVersion.minecraftVersion)) {
                version = potentialVersion;
                break;
            }
        }
        if (version == null) {
            version = NMSVersion.NOT_SUPPORTED;
            instance = null;
            return false;
        }
        try {
            // Get the class of our handler for this version
            final Class<?> clazz = Class.forName("com.denizenscript.denizen.nms." + version.name() + ".Handler");
            if (NMSHandler.class.isAssignableFrom(clazz)) {
                // Found and loaded - good to go!
                instance = (NMSHandler) clazz.getDeclaredConstructor().newInstance();
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
    public static PacketHelper packetHelper;
    public static PlayerHelper playerHelper;
    public static WorldHelper worldHelper;
    public static EnchantmentHelper enchantmentHelper;

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

    public abstract BiomeNMS getBiomeNMS(World world, NamespacedKey key);

    public BiomeNMS getBiomeAt(Block block) {
        return NMSHandler.instance.getBiomeNMS(block.getWorld(), block.getBiome().getKey());
    }

    public abstract double[] getRecentTps();

    public abstract CompoundTag createCompoundTag(Map<String, Tag> value);

    public CompoundTag parseSNBT(String snbt) {
        throw new UnsupportedOperationException();
    }

    public abstract String getTitle(Inventory inventory);

    public void setInventoryTitle(InventoryView view, String title) {
        throw new UnsupportedOperationException();
    }

    public abstract String stringForHover(HoverEvent hover);

    public abstract ArrayList<String> containerListFlags(PersistentDataContainer container, String prefix);

    public abstract boolean containerHas(PersistentDataContainer container, String key);

    public abstract String containerGetString(PersistentDataContainer container, String key);

    public UUID getBossbarUUID(BossBar bar) {
        return null;
    }

    public void setBossbarUUID(BossBar bar, UUID id) {
    }

    public String updateLegacyName(Class<?> type, String legacyName) {
        return legacyName;
    }
}
