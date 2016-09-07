package net.aufdemrand.denizen.nms;

import com.google.common.collect.Iterables;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import net.aufdemrand.denizen.nms.helpers.BlockHelper_v1_10_R1;
import net.aufdemrand.denizen.nms.helpers.EntityHelper_v1_10_R1;
import net.aufdemrand.denizen.nms.helpers.FishingHelper_v1_10_R1;
import net.aufdemrand.denizen.nms.helpers.ItemHelper_v1_10_R1;
import net.aufdemrand.denizen.nms.helpers.PlayerHelper_v1_10_R1;
import net.aufdemrand.denizen.nms.interfaces.BlockHelper;
import net.aufdemrand.denizen.nms.interfaces.EntityHelper;
import net.aufdemrand.denizen.nms.interfaces.FishingHelper;
import net.aufdemrand.denizen.nms.interfaces.ItemHelper;
import net.aufdemrand.denizen.nms.interfaces.PlayerHelper;
import net.aufdemrand.denizen.nms.util.PlayerProfile;
import net.minecraft.server.v1_10_R1.MinecraftServer;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_10_R1.CraftServer;

public class Handler_v1_10_R1 extends NMSHandler {

    private final BlockHelper blockHelper = new BlockHelper_v1_10_R1();
    private final EntityHelper entityHelper = new EntityHelper_v1_10_R1();
    private final FishingHelper fishingHelper = new FishingHelper_v1_10_R1();
    private final ItemHelper itemHelper = new ItemHelper_v1_10_R1();
    private final PlayerHelper playerHelper = new PlayerHelper_v1_10_R1();

    @Override
    public Thread getMainThread() {
        return ((CraftServer) Bukkit.getServer()).getServer().primaryThread;
    }

    @Override
    public BlockHelper getBlockHelper() {
        return blockHelper;
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
    public PlayerHelper getPlayerHelper() {
        return playerHelper;
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
                Property property = Iterables.getFirst(gameProfile.getProperties().get("textures"), null);
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
}
