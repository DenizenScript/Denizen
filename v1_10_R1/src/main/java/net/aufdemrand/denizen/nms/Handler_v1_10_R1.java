package net.aufdemrand.denizen.nms;

import com.google.common.collect.Iterables;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import net.aufdemrand.denizen.nms.abstracts.BiomeNMS;
import net.aufdemrand.denizen.nms.abstracts.ProfileEditor;
import net.aufdemrand.denizen.nms.abstracts.Sidebar;
import net.aufdemrand.denizen.nms.helpers.*;
import net.aufdemrand.denizen.nms.impl.BiomeNMS_v1_10_R1;
import net.aufdemrand.denizen.nms.impl.ProfileEditor_v1_10_R1;
import net.aufdemrand.denizen.nms.impl.Sidebar_v1_10_R1;
import net.aufdemrand.denizen.nms.impl.packets.handlers.DenizenPacketListener_v1_10_R1;
import net.aufdemrand.denizen.nms.interfaces.*;
import net.aufdemrand.denizen.nms.interfaces.packets.PacketHandler;
import net.aufdemrand.denizen.nms.util.PlayerProfile;
import net.minecraft.server.v1_10_R1.MinecraftServer;
import org.bukkit.Bukkit;
import org.bukkit.block.Biome;
import org.bukkit.craftbukkit.v1_10_R1.CraftServer;
import org.bukkit.craftbukkit.v1_10_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;

public class Handler_v1_10_R1 extends NMSHandler {

    private final BlockHelper blockHelper = new BlockHelper_v1_10_R1();
    private final ChunkHelper chunkHelper = new ChunkHelper_v1_10_R1();
    private final CustomEntityHelper customEntityHelper = new CustomEntityHelper_v1_10_R1(javaPlugin);
    private final EntityHelper entityHelper = new EntityHelper_v1_10_R1();
    private final FishingHelper fishingHelper = new FishingHelper_v1_10_R1();
    private final ItemHelper itemHelper = new ItemHelper_v1_10_R1();
    private final PacketHelper packetHelper = new PacketHelper_v1_10_R1();
    private final PlayerHelper playerHelper = new PlayerHelper_v1_10_R1();
    private final WorldHelper worldHelper = new WorldHelper_v1_10_R1();

    private final ProfileEditor profileEditor = new ProfileEditor_v1_10_R1(javaPlugin);

    @Override
    public Thread getMainThread() {
        return ((CraftServer) Bukkit.getServer()).getServer().primaryThread;
    }

    @Override
    public double[] getRecentTps() {
        return ((CraftServer) Bukkit.getServer()).getServer().recentTps;
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
    public PacketHelper getPacketHelper() {
        return packetHelper;
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
        DenizenPacketListener_v1_10_R1.enable(javaPlugin, packetHandler);
    }

    @Override
    public Sidebar createSidebar(Player player) {
        return new Sidebar_v1_10_R1(player);
    }

    @Override
    public PlayerProfile fillPlayerProfile(PlayerProfile playerProfile) {
        try {
            if (playerProfile != null) {
                GameProfile gameProfile = new GameProfile(playerProfile.getUniqueId(), playerProfile.getName());
                gameProfile.getProperties().put("textures",
                        new Property("value", playerProfile.getTexture(), playerProfile.getTextureSignature()));
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
                if (Iterables.getFirst(gameProfile1.getProperties().get("textures"), null) == null) {
                    gameProfile1 = minecraftServer.ay().fillProfileProperties(gameProfile1, true);
                }
                Property property = Iterables.getFirst(gameProfile1.getProperties().get("textures"), null);
                return new PlayerProfile(gameProfile1.getName(), gameProfile1.getId(),
                        property != null ? property.getValue() : null,
                        property != null ? property.getSignature() : null);
            }
        }
        catch (Exception e) {
            // Nothing for now
        }
        return null;
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
        return new BiomeNMS_v1_10_R1(biome);
    }
}
