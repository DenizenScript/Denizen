package net.aufdemrand.denizen.utilities.entity;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import net.aufdemrand.denizen.objects.properties.item.ItemSkullskin;
import net.aufdemrand.denizen.utilities.DenizenAPI;
import net.aufdemrand.denizen.utilities.PlayerProfileEditor;
import net.aufdemrand.denizen.utilities.debugging.dB;
import net.aufdemrand.denizen.utilities.packets.PacketHelper;
import net.aufdemrand.denizencore.objects.Mechanism;
import net.minecraft.server.v1_8_R1.*;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_8_R1.CraftServer;
import org.bukkit.craftbukkit.v1_8_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_8_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class CraftFakePlayer extends CraftPlayer implements DenizenCustomEntity {

    private final CraftServer server;

    public CraftFakePlayer(CraftServer server, EntityFakePlayer entity) {
        super(server, entity);
        this.server = server;
        setMetadata("NPC", new FixedMetadataValue(DenizenAPI.getCurrentInstance(), true));
    }

    @CreateEntity
    public static Player createFakePlayer(Location location, ArrayList<Mechanism> mechanisms) {
        String name = null;
        String skin = null;
        for (Mechanism mechanism : mechanisms) {
            if (mechanism.matches("name")) {
                name = mechanism.getValue().asString();
            }
            else if (mechanism.matches("skin")) {
                skin = mechanism.getValue().asString();
            }
        }
        if (name == null || name.length() > 16) {
            dB.echoError("You must specify a name with no more than 16 characters for FAKE_PLAYER names!");
            return null;
        }
        if (skin != null && skin.length() > 16) {
            dB.echoError("You must specify a name with no more than 16 characters for FAKE_PLAYER skins!");
        }
        CraftWorld world = (CraftWorld) location.getWorld();
        WorldServer worldServer = world.getHandle();
        GameProfile gameProfile = new GameProfile(null, name);
        gameProfile = ItemSkullskin.fillGameProfile(gameProfile);
        if (skin != null) {
            gameProfile = new GameProfile(gameProfile.getId(), gameProfile.getName());
            GameProfile skinProfile = new GameProfile(null, skin);
            skinProfile = ItemSkullskin.fillGameProfile(skinProfile);
            for (Property texture : skinProfile.getProperties().get("textures"))
                gameProfile.getProperties().put("textures", texture);
        }
        UUID uuid = UUID.randomUUID();
        if (uuid.version() == 4) {
            long msb = uuid.getMostSignificantBits();
            msb &= ~0x0000000000004000L;
            msb |= 0x0000000000002000L;
            uuid = new UUID(msb, uuid.getLeastSignificantBits());
        }
        PlayerProfileEditor.setProfileId(gameProfile, uuid);

        final EntityFakePlayer fakePlayer = new EntityFakePlayer(worldServer.getMinecraftServer(), worldServer,
                gameProfile, new PlayerInteractManager(worldServer));
        fakePlayer.setPositionRotation(location.getX(), location.getY(), location.getZ(),
                location.getYaw(), location.getPitch());
        new BukkitRunnable() {
            @Override
            public void run() {
                PacketPlayOutNamedEntitySpawn spawnPacket = new PacketPlayOutNamedEntitySpawn(fakePlayer);
                PacketPlayOutPlayerInfo playerInfo = new PacketPlayOutPlayerInfo(EnumPlayerInfoAction.ADD_PLAYER, fakePlayer);
                for (Player player : Bukkit.getServer().getOnlinePlayers()) {
                    PacketHelper.sendPacket(player, spawnPacket);
                    PacketHelper.sendPacket(player, playerInfo);
                }
            }
        }.runTaskLater(DenizenAPI.getCurrentInstance(), 5);
        return fakePlayer.getBukkitEntity();
    }

    @Override
    public void setMetadata(String metadataKey, MetadataValue newMetadataValue) {
        this.server.getEntityMetadata().setMetadata(this, metadataKey, newMetadataValue);
    }

    @Override
    public List<MetadataValue> getMetadata(String metadataKey) {
        return this.server.getEntityMetadata().getMetadata(this, metadataKey);
    }

    @Override
    public boolean hasMetadata(String metadataKey) {
        return this.server.getEntityMetadata().hasMetadata(this, metadataKey);
    }

    @Override
    public void removeMetadata(String metadataKey, Plugin owningPlugin) {
        this.server.getEntityMetadata().removeMetadata(this, metadataKey, owningPlugin);
    }

    @Override
    public String getEntityTypeName() {
        return "FAKE_PLAYER";
    }
}
