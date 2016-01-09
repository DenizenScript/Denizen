package net.aufdemrand.denizen.utilities.entity;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import net.aufdemrand.denizen.objects.properties.item.ItemSkullskin;
import net.aufdemrand.denizen.utilities.DenizenAPI;
import net.aufdemrand.denizen.utilities.PlayerProfileEditor;
import net.aufdemrand.denizen.utilities.debugging.dB;
import net.aufdemrand.denizencore.objects.Mechanism;
import net.minecraft.server.v1_8_R3.PlayerInteractManager;
import net.minecraft.server.v1_8_R3.WorldServer;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_8_R3.CraftServer;
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.plugin.Plugin;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class CraftFakePlayer extends CraftPlayer implements DenizenCustomEntity {

    private final CraftServer server;
    private String fullName;

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
        String fullName = name;
        String prefix = null;
        String suffix = null;
        if (name == null) {
            return null;
        }
        else if (fullName.length() > 16) {
            prefix = fullName.substring(0, 16);
            if (fullName.length() > 30) {
                int len = 30;
                name = fullName.substring(16, 30);
                if (name.matches(".*[^A-Za-z0-9_].*")) {
                    if (fullName.length() >= 32) {
                        len = 32;
                        name = fullName.substring(16, 32);
                    }
                    else if (fullName.length() == 31) {
                        len = 31;
                        name = fullName.substring(16, 31);
                    }
                }
                else if (name.length() > 46) {
                    dB.echoError("You must specify a name with no more than 46 characters for FAKE_PLAYER entities!");
                    return null;
                }
                else {
                    name = ChatColor.RESET + name;
                }
                suffix = fullName.substring(len);
            }
            else {
                name = fullName.substring(16);
                if (!name.matches(".*[^A-Za-z0-9_].*")) {
                    name = ChatColor.RESET + name;
                }
                if (name.length() > 16) {
                    suffix = name.substring(16);
                    name = name.substring(0, 16);
                }
            }
        }
        if (skin != null && skin.length() > 16) {
            dB.echoError("You must specify a name with no more than 16 characters for FAKE_PLAYER entity skins!");
        }
        CraftWorld world = (CraftWorld) location.getWorld();
        WorldServer worldServer = world.getHandle();
        GameProfile gameProfile = new GameProfile(null, name);
        if (skin == null && !name.matches(".*[^A-Za-z0-9_].*")) {
            gameProfile = ItemSkullskin.fillGameProfile(gameProfile);
        }
        if (skin != null) {
            GameProfile skinProfile = new GameProfile(null, skin);
            skinProfile = ItemSkullskin.fillGameProfile(skinProfile);
            for (Property texture : skinProfile.getProperties().get("textures")) {
                gameProfile.getProperties().put("textures", texture);
            }
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
        CraftFakePlayer craftFakePlayer = fakePlayer.getBukkitEntity();
        craftFakePlayer.fullName = fullName;
        if (prefix != null) {
            Scoreboard scoreboard = Bukkit.getScoreboardManager().getMainScoreboard();
            String teamName = "FAKE_PLAYER_TEAM_" + fullName;
            String hash = null;
            try {
                MessageDigest md = MessageDigest.getInstance("MD5");
                byte[] bytes = teamName.getBytes("UTF-8");
                md.update(bytes, 0, bytes.length);
                hash = new BigInteger(1, md.digest()).toString(16).substring(0, 16);
            }
            catch (Exception e) {
                dB.echoError(e);
            }
            if (hash != null) {
                Team team = scoreboard.getTeam(hash);
                if (team == null) {
                    team = scoreboard.registerNewTeam(hash);
                    team.setPrefix(prefix);
                    if (suffix != null) {
                        team.setSuffix(suffix);
                    }
                }
                team.addPlayer(craftFakePlayer);
            }
        }
        return craftFakePlayer;
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

    public String getFullName() {
        return fullName;
    }
}
