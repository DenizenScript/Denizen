package net.aufdemrand.denizen.nms;

import com.google.common.collect.Iterables;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import net.aufdemrand.denizen.nms.abstracts.AnimationHelper;
import net.aufdemrand.denizen.nms.abstracts.BiomeNMS;
import net.aufdemrand.denizen.nms.abstracts.BlockLight;
import net.aufdemrand.denizen.nms.abstracts.ParticleHelper;
import net.aufdemrand.denizen.nms.abstracts.ProfileEditor;
import net.aufdemrand.denizen.nms.abstracts.Sidebar;
import net.aufdemrand.denizen.nms.helpers.*;
import net.aufdemrand.denizen.nms.impl.BiomeNMS_v1_12_R1;
import net.aufdemrand.denizen.nms.impl.ProfileEditor_v1_12_R1;
import net.aufdemrand.denizen.nms.impl.Sidebar_v1_12_R1;
import net.aufdemrand.denizen.nms.impl.blocks.BlockLight_v1_12_R1;
import net.aufdemrand.denizen.nms.impl.jnbt.CompoundTag_v1_12_R1;
import net.aufdemrand.denizen.nms.impl.packets.handlers.DenizenPacketListener_v1_12_R1;
import net.aufdemrand.denizen.nms.interfaces.*;
import net.aufdemrand.denizen.nms.interfaces.packets.PacketHandler;
import net.aufdemrand.denizen.nms.util.PlayerProfile;
import net.aufdemrand.denizen.nms.util.jnbt.CompoundTag;
import net.aufdemrand.denizen.nms.util.jnbt.Tag;
import com.denizenscript.denizencore.utilities.debugging.dB;
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

public class Handler_v1_12_R1 extends NMSHandler {

    private final AdvancementHelper advancementHelper = new AdvancementHelper_v1_12_R1();
    private final AnimationHelper animationHelper = new AnimationHelper_v1_12_R1();
    private final BlockHelper blockHelper = new BlockHelper_v1_12_R1();
    private final ChunkHelper chunkHelper = new ChunkHelper_v1_12_R1();
    private final CustomEntityHelper customEntityHelper = new CustomEntityHelper_v1_12_R1();
    private final EntityHelper entityHelper = new EntityHelper_v1_12_R1();
    private final FishingHelper fishingHelper = new FishingHelper_v1_12_R1();
    private final ItemHelper itemHelper = new ItemHelper_v1_12_R1();
    private final SoundHelper soundHelper = new SoundHelper_v1_12_R1();
    private final PacketHelper packetHelper = new PacketHelper_v1_12_R1();
    private final ParticleHelper particleHelper = new ParticleHelper_v1_12_R1();
    private final PlayerHelper playerHelper = new PlayerHelper_v1_12_R1();
    private final WorldHelper worldHelper = new WorldHelper_v1_12_R1();

    private final ProfileEditor profileEditor = new ProfileEditor_v1_12_R1();

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
        DenizenPacketListener_v1_12_R1.enable(packetHandler);
    }

    @Override
    public CompoundTag createCompoundTag(Map<String, Tag> value) {
        return new CompoundTag_v1_12_R1(value);
    }

    @Override
    public Sidebar createSidebar(Player player) {
        return new Sidebar_v1_12_R1(player);
    }

    @Override
    public BlockLight createBlockLight(Location location, int lightLevel, long ticks) {
        return BlockLight_v1_12_R1.createLight(location, lightLevel, ticks);
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
            if (dB.verbose) {
                dB.echoError(e);
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
        return new BiomeNMS_v1_12_R1(biome);
    }
}
