package net.aufdemrand.denizen.utilities.entity;

import com.mojang.authlib.GameProfile;
import net.aufdemrand.denizen.objects.properties.item.ItemSkullskin;
import net.aufdemrand.denizen.utilities.DenizenAPI;
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

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class CraftFakePlayer extends CraftPlayer implements DenizenCustomEntity {

    private static final Field gameProfileId;
    private final CraftServer server;

    static {
        Field field = null;
        try {
            field = GameProfile.class.getDeclaredField("id");
            field.setAccessible(true);
        } catch (Exception e) {
            dB.echoError(e);
        }
        gameProfileId = field;
    }

    public CraftFakePlayer(CraftServer server, EntityFakePlayer entity) {
        super(server, entity);
        this.server = server;
        setMetadata("NPC", new FixedMetadataValue(DenizenAPI.getCurrentInstance(), true));
    }

    @CreateEntity
    public static Player createFakePlayer(Location location, ArrayList<Mechanism> mechanisms) {
        String name = null;
        for (Mechanism mechanism : mechanisms) {
            if (mechanism.matches("name"))
                name = mechanism.getValue().asString();
        }
        if (name == null || name.length() == 0 || name.length() > 16) {
            dB.echoError("You must specify a name with no more than 16 characters for FAKE_PLAYER entities!");
            return null;
        }
        CraftWorld world = (CraftWorld) location.getWorld();
        WorldServer worldServer = world.getHandle();
        GameProfile gameProfile = new GameProfile(null, name);
        gameProfile = ItemSkullskin.fillGameProfile(gameProfile);
        UUID uuid = UUID.randomUUID();
        if (uuid.version() == 4) {
            long msb = uuid.getMostSignificantBits();
            msb &= ~0x0000000000004000L;
            msb |= 0x0000000000002000L;
            uuid = new UUID(msb, uuid.getLeastSignificantBits());
        }
        setProfileId(gameProfile, uuid);

        EntityFakePlayer fakePlayer = new EntityFakePlayer(worldServer.getMinecraftServer(), worldServer, gameProfile,
                new PlayerInteractManager(worldServer));
        fakePlayer.setPositionRotation(location.getX(), location.getY(), location.getZ(),
                location.getYaw(), location.getPitch());
        PacketPlayOutNamedEntitySpawn spawnPacket = new PacketPlayOutNamedEntitySpawn(fakePlayer);
        for (Player player : Bukkit.getServer().getOnlinePlayers()) {
            PacketHelper.sendPacket(player, spawnPacket);
        }
        return fakePlayer.getBukkitEntity();
    }

    private static void setProfileId(GameProfile gameProfile, UUID uuid) {
        try {
            gameProfileId.set(gameProfile, uuid);
        } catch (Exception e) {
            dB.echoError(e);
        }
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
