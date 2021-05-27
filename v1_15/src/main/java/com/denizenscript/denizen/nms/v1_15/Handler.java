package com.denizenscript.denizen.nms.v1_15;

import com.denizenscript.denizen.nms.NMSHandler;
import com.denizenscript.denizen.nms.abstracts.*;
import com.denizenscript.denizen.nms.v1_15.helpers.*;
import com.denizenscript.denizen.nms.v1_15.impl.BiomeNMSImpl;
import com.denizenscript.denizen.nms.v1_15.impl.ProfileEditorImpl;
import com.denizenscript.denizen.nms.v1_15.impl.SidebarImpl;
import com.denizenscript.denizen.nms.v1_15.impl.blocks.BlockLightImpl;
import com.denizenscript.denizen.nms.v1_15.impl.jnbt.CompoundTagImpl;
import com.denizenscript.denizen.nms.v1_15.impl.network.handlers.DenizenPacketListenerImpl;
import com.denizenscript.denizen.nms.util.jnbt.Tag;
import com.denizenscript.denizen.utilities.FormattedTextHelper;
import com.denizenscript.denizen.utilities.packets.DenizenPacketHandler;
import com.google.common.collect.Iterables;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import com.denizenscript.denizen.nms.util.PlayerProfile;
import com.denizenscript.denizencore.utilities.ReflectionHelper;
import com.denizenscript.denizen.nms.util.jnbt.CompoundTag;
import com.denizenscript.denizencore.utilities.CoreUtilities;
import com.denizenscript.denizencore.utilities.debugging.Debug;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.HoverEvent;
import net.minecraft.server.v1_15_R1.*;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Biome;
import org.bukkit.craftbukkit.v1_15_R1.CraftServer;
import org.bukkit.craftbukkit.v1_15_R1.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_15_R1.inventory.CraftInventory;
import org.bukkit.craftbukkit.v1_15_R1.inventory.CraftInventoryCustom;
import org.bukkit.craftbukkit.v1_15_R1.persistence.CraftPersistentDataContainer;
import org.bukkit.craftbukkit.v1_15_R1.util.CraftChatMessage;
import org.bukkit.craftbukkit.v1_15_R1.util.CraftMagicNumbers;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.persistence.PersistentDataContainer;
import org.spigotmc.AsyncCatcher;

import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Map;

public class Handler extends NMSHandler {

    public Handler() {
        advancementHelper = new AdvancementHelperImpl();
        animationHelper = new AnimationHelperImpl();
        blockHelper = new BlockHelperImpl();
        chunkHelper = new ChunkHelperImpl();
        customEntityHelper = new CustomEntityHelperImpl();
        entityHelper = new EntityHelperImpl();
        fishingHelper = new FishingHelperImpl();
        itemHelper = new ItemHelperImpl();
        soundHelper = new SoundHelperImpl();
        packetHelper = new PacketHelperImpl();
        particleHelper = new ParticleHelperImpl();
        playerHelper = new PlayerHelperImpl();
        worldHelper = new WorldHelperImpl();
    }

    private final ProfileEditor profileEditor = new ProfileEditorImpl();

    private boolean wasAsyncCatcherEnabled;

    @Override
    public void disableAsyncCatcher() {
        wasAsyncCatcherEnabled = AsyncCatcher.enabled;
        AsyncCatcher.enabled = false;
    }

    @Override
    public void undisableAsyncCatcher() {
        AsyncCatcher.enabled = wasAsyncCatcherEnabled;
    }

    @Override
    public boolean isCorrectMappingsCode() {
        return ((CraftMagicNumbers) CraftMagicNumbers.INSTANCE).getMappingsVersion().equals("5684afcc1835d966e1b6eb0ed3f72edb");
    }

    @Override
    public double[] getRecentTps() {
        return ((CraftServer) Bukkit.getServer()).getServer().recentTps;
    }

    @Override
    public void enablePacketInterception(DenizenPacketHandler packetHandler) {
        DenizenPacketListenerImpl.enable(packetHandler);
    }

    @Override
    public CompoundTag createCompoundTag(Map<String, Tag> value) {
        return new CompoundTagImpl(value);
    }

    @Override
    public Sidebar createSidebar(Player player) {
        return new SidebarImpl(player);
    }

    @Override
    public BlockLight createBlockLight(Location location, int lightLevel, long ticks) {
        return BlockLightImpl.createLight(location, lightLevel, ticks);
    }

    @Override
    public PlayerProfile fillPlayerProfile(PlayerProfile playerProfile) {
        try {
            if (playerProfile != null) {
                GameProfile gameProfile = new GameProfile(playerProfile.getUniqueId(), playerProfile.getName());
                gameProfile.getProperties().get("textures").clear();
                if (playerProfile.getTextureSignature() != null) {
                    gameProfile.getProperties().put("textures", new Property("textures", playerProfile.getTexture(), playerProfile.getTextureSignature()));
                }
                else {
                    gameProfile.getProperties().put("textures", new Property("textures", playerProfile.getTexture()));
                }
                MinecraftServer minecraftServer = ((CraftServer) Bukkit.getServer()).getServer();
                GameProfile gameProfile1 = null;
                if (gameProfile.getId() != null) {
                    gameProfile1 = minecraftServer.getUserCache().getProfile(gameProfile.getId());
                }
                if (gameProfile1 == null && gameProfile.getName() != null) {
                    gameProfile1 = minecraftServer.getUserCache().getProfile(gameProfile.getName());
                }
                if (gameProfile1 == null) {
                    gameProfile1 = gameProfile;
                }
                if (playerProfile.hasTexture()) {
                    gameProfile1.getProperties().get("textures").clear();
                    if (playerProfile.getTextureSignature() != null) {
                        gameProfile1.getProperties().put("textures", new Property("textures", playerProfile.getTexture(), playerProfile.getTextureSignature()));
                    }
                    else {
                        gameProfile1.getProperties().put("textures", new Property("textures", playerProfile.getTexture()));
                    }
                }
                if (Iterables.getFirst(gameProfile1.getProperties().get("textures"), null) == null) {
                    gameProfile1 = minecraftServer.getMinecraftSessionService().fillProfileProperties(gameProfile1, true);
                }
                Property property = Iterables.getFirst(gameProfile1.getProperties().get("textures"), null);
                return new PlayerProfile(gameProfile1.getName(), gameProfile1.getId(),
                        property != null ? property.getValue() : null,
                        property != null ? property.getSignature() : null);
            }
        }
        catch (Exception e) {
            if (Debug.verbose) {
                Debug.echoError(e);
            }
        }
        return null;
    }

    @Override
    public int getPort() {
        return ((CraftServer) Bukkit.getServer()).getServer().getPort();
    }

    @Override
    public String getTitle(Inventory inventory) {
        IInventory nms = ((CraftInventory) inventory).getInventory();
        if (nms instanceof INamableTileEntity) {
            return CraftChatMessage.fromComponent(((INamableTileEntity) nms).getDisplayName());
        }
        else if (MINECRAFT_INVENTORY.isInstance(nms)) {
            try {
                return (String) INVENTORY_TITLE.get(nms);
            }
            catch (IllegalAccessException e) {
                Debug.echoError(e);
            }
        }
        return "Chest";
    }

    public static final Class MINECRAFT_INVENTORY;
    public static final Field INVENTORY_TITLE;
    public static final Field ENTITY_BUKKITYENTITY = ReflectionHelper.getFields(Entity.class).get("bukkitEntity");

    static {
        Class minecraftInv = null;
        Field title = null;
        try {
            for (Class clzz : CraftInventoryCustom.class.getDeclaredClasses()) {
                if (CoreUtilities.toLowerCase(clzz.getName()).contains("minecraftinventory")) { // MinecraftInventory.
                    minecraftInv = clzz;
                    title = clzz.getDeclaredField("title");
                    title.setAccessible(true);
                    break;
                }
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        MINECRAFT_INVENTORY = minecraftInv;
        INVENTORY_TITLE = title;
    }

    @Override
    public PlayerProfile getPlayerProfile(Player player) {
        GameProfile gameProfile = ((CraftPlayer) player).getProfile();
        Property property = Iterables.getFirst(gameProfile.getProperties().get("textures"), null);
        return new PlayerProfile(gameProfile.getName(), gameProfile.getId(),
                property != null ? property.getValue() : null,
                property != null ? property.getSignature() : null);
    }

    @Override
    public ProfileEditor getProfileEditor() {
        return profileEditor;
    }

    @Override
    public BiomeNMS getBiomeNMS(Biome biome) {
        return new BiomeNMSImpl(biome);
    }

    @Override
    public String stringForHover(HoverEvent hover) {
        return FormattedTextHelper.stringify(hover.getValue(), ChatColor.WHITE);
    }

    @Override
    public ArrayList<String> containerListFlags(PersistentDataContainer container, String prefix) {
        prefix = "denizen:" + prefix;
        ArrayList<String> output = new ArrayList<>();
        for (String key : ((CraftPersistentDataContainer) container).getRaw().keySet()) {
            if (key.startsWith(prefix)) {
                output.add(key.substring(prefix.length()));
            }
        }
        return output;
    }

    @Override
    public boolean containerHas(PersistentDataContainer container, NamespacedKey key) {
        return ((CraftPersistentDataContainer) container).getRaw().containsKey(key.toString());
    }

    @Override
    public String containerGetString(PersistentDataContainer container, NamespacedKey key) {
        NBTBase base = ((CraftPersistentDataContainer) container).getRaw().get(key.toString());
        if (base instanceof NBTTagString) {
            return base.asString();
        }
        else if (base instanceof NBTTagByteArray) {
            return new String(((NBTTagByteArray) base).getBytes(), StandardCharsets.UTF_8);
        }
        return null;
    }
}
