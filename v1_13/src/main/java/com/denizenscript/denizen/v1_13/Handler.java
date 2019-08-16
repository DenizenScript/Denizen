package com.denizenscript.denizen.v1_13;

import com.denizenscript.denizen.nms.NMSHandler;
import com.denizenscript.denizen.v1_13.impl.BiomeNMS_v1_13_R2;
import com.denizenscript.denizen.v1_13.impl.ProfileEditor_v1_13_R2;
import com.denizenscript.denizen.v1_13.impl.Sidebar_v1_13_R2;
import com.denizenscript.denizen.v1_13.impl.blocks.BlockLight_v1_13_R2;
import com.denizenscript.denizen.v1_13.impl.jnbt.CompoundTag_v1_13_R2;
import com.denizenscript.denizen.v1_13.impl.packets.handlers.DenizenPacketListener_v1_13_R2;
import com.denizenscript.denizen.nms.interfaces.*;
import com.denizenscript.denizen.nms.interfaces.packets.PacketHandler;
import com.denizenscript.denizen.nms.util.PlayerProfile;
import com.denizenscript.denizen.nms.util.jnbt.CompoundTag;
import com.denizenscript.denizen.nms.util.jnbt.Tag;
import com.denizenscript.denizen.v1_13.helpers.*;
import com.google.common.collect.Iterables;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import com.denizenscript.denizen.nms.abstracts.AnimationHelper;
import com.denizenscript.denizen.nms.abstracts.BiomeNMS;
import com.denizenscript.denizen.nms.abstracts.BlockLight;
import com.denizenscript.denizen.nms.abstracts.ParticleHelper;
import com.denizenscript.denizen.nms.abstracts.ProfileEditor;
import com.denizenscript.denizen.nms.abstracts.Sidebar;
import com.denizenscript.denizencore.utilities.debugging.Debug;
import net.minecraft.server.v1_13_R2.MinecraftServer;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Biome;
import org.bukkit.craftbukkit.v1_13_R2.CraftServer;
import org.bukkit.craftbukkit.v1_13_R2.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_13_R2.util.CraftMagicNumbers;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.spigotmc.AsyncCatcher;

import java.util.Map;

public class Handler extends NMSHandler {

    private final AdvancementHelper advancementHelper = new AdvancementHelper_v1_13_R2();
    private final AnimationHelper animationHelper = new AnimationHelper_v1_13_R2();
    private final BlockHelper blockHelper = new BlockHelper_v1_13_R2();
    private final ChunkHelper chunkHelper = new ChunkHelper_v1_13_R2();
    private final CustomEntityHelper customEntityHelper = new CustomEntityHelper_v1_13_R2();
    private final EntityHelper entityHelper = new EntityHelper_v1_13_R2();
    private final FishingHelper fishingHelper = new FishingHelper_v1_13_R2();
    private final ItemHelper itemHelper = new ItemHelper_v1_13_R2();
    private final SoundHelper soundHelper = new SoundHelper_v1_13_R2();
    private final PacketHelper packetHelper = new PacketHelper_v1_13_R2();
    private final ParticleHelper particleHelper = new ParticleHelper_v1_13_R2();
    private final PlayerHelper playerHelper = new PlayerHelper_v1_13_R2();
    private final WorldHelper worldHelper = new WorldHelper_v1_13_R2();

    private final ProfileEditor profileEditor = new ProfileEditor_v1_13_R2();

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
        return ((CraftMagicNumbers) CraftMagicNumbers.INSTANCE).getMappingsVersion().equals("7dd4b3ec31629620c41553e5c142e454");
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
    public AdvancementHelper getAdvancementHelper() {
        return advancementHelper;
    }

    @Override
    public AnimationHelper getAnimationHelper() {
        return animationHelper;
    }

    @Override
    public BlockHelper getBlockHelper() {
        return blockHelper;
    }

    @Override
    public ChunkHelper getChunkHelper() {
        return chunkHelper;
    }

    @Override
    public CustomEntityHelper getCustomEntityHelper() {
        return customEntityHelper;
    }

    @Override
    public EntityHelper getEntityHelper() {
        return entityHelper;
    }

    @Override
    public FishingHelper getFishingHelper() {
        return fishingHelper;
    }

    @Override
    public ItemHelper getItemHelper() {
        return itemHelper;
    }

    @Override
    public SoundHelper getSoundHelper() {
        return soundHelper;
    }

    @Override
    public PacketHelper getPacketHelper() {
        return packetHelper;
    }

    @Override
    public ParticleHelper getParticleHelper() {
        return particleHelper;
    }

    @Override
    public PlayerHelper getPlayerHelper() {
        return playerHelper;
    }

    @Override
    public WorldHelper getWorldHelper() {
        return worldHelper;
    }

    @Override
    public void enablePacketInterception(PacketHandler packetHandler) {
        DenizenPacketListener_v1_13_R2.enable(packetHandler);
    }

    @Override
    public CompoundTag createCompoundTag(Map<String, Tag> value) {
        return new CompoundTag_v1_13_R2(value);
    }

    @Override
    public Sidebar createSidebar(Player player) {
        return new Sidebar_v1_13_R2(player);
    }

    @Override
    public BlockLight createBlockLight(Location location, int lightLevel, long ticks) {
        return BlockLight_v1_13_R2.createLight(location, lightLevel, ticks);
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
                    gameProfile1 = minecraftServer.ap().fillProfileProperties(gameProfile1, true);
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
        return new BiomeNMS_v1_13_R2(biome);
    }
}
