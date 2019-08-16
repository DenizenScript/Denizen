package com.denizenscript.denizen.nms.v1_12;

import com.denizenscript.denizen.nms.NMSHandler;
import com.denizenscript.denizen.nms.v1_12.helpers.*;
import com.denizenscript.denizen.nms.v1_12.impl.BiomeNMSImpl;
import com.denizenscript.denizen.nms.v1_12.impl.packets.handlers.DenizenPacketListenerImpl;
import com.denizenscript.denizen.nms.v1_12.impl.ProfileEditorImpl;
import com.denizenscript.denizen.nms.v1_12.impl.SidebarImpl;
import com.denizenscript.denizen.nms.v1_12.impl.blocks.BlockLightImpl;
import com.denizenscript.denizen.nms.v1_12.impl.jnbt.CompoundTagImpl;
import com.google.common.collect.Iterables;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import com.denizenscript.denizen.nms.abstracts.BiomeNMS;
import com.denizenscript.denizen.nms.abstracts.BlockLight;
import com.denizenscript.denizen.nms.abstracts.ProfileEditor;
import com.denizenscript.denizen.nms.abstracts.Sidebar;
import com.denizenscript.denizen.nms.interfaces.packets.PacketHandler;
import com.denizenscript.denizen.nms.util.PlayerProfile;
import com.denizenscript.denizen.nms.util.jnbt.CompoundTag;
import com.denizenscript.denizen.nms.util.jnbt.Tag;
import com.denizenscript.denizencore.utilities.debugging.Debug;
import net.minecraft.server.v1_12_R1.MinecraftServer;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Biome;
import org.bukkit.craftbukkit.v1_12_R1.CraftServer;
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.spigotmc.AsyncCatcher;

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
    public Thread getMainThread() {
        return ((CraftServer) Bukkit.getServer()).getServer().primaryThread;
    }

    @Override
    public double[] getRecentTps() {
        return ((CraftServer) Bukkit.getServer()).getServer().recentTps;
    }

    @Override
    public void enablePacketInterception(PacketHandler packetHandler) {
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
                    gameProfile1 = minecraftServer.getUserCache().a(gameProfile.getId());
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
                    gameProfile1 = minecraftServer.az().fillProfileProperties(gameProfile1, true);
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
        return ((CraftServer) Bukkit.getServer()).getServer().P();
    }

    @Override
    public String getTitle(Inventory inventory) {
        return inventory.getTitle();
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
}
