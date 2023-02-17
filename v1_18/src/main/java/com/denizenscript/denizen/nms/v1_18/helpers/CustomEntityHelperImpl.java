package com.denizenscript.denizen.nms.v1_18.helpers;

import com.denizenscript.denizen.nms.v1_18.impl.entities.CraftFakePlayerImpl;
import com.denizenscript.denizen.nms.v1_18.impl.entities.EntityFakeArrowImpl;
import com.denizenscript.denizen.nms.v1_18.impl.entities.EntityFakePlayerImpl;
import com.denizenscript.denizen.nms.v1_18.impl.entities.EntityItemProjectileImpl;
import com.denizenscript.denizencore.utilities.CoreUtilities;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import com.denizenscript.denizen.nms.NMSHandler;
import com.denizenscript.denizen.nms.interfaces.CustomEntityHelper;
import com.denizenscript.denizen.nms.interfaces.FakeArrow;
import com.denizenscript.denizen.nms.interfaces.FakePlayer;
import com.denizenscript.denizen.nms.interfaces.ItemProjectile;
import com.denizenscript.denizen.nms.util.PlayerProfile;
import com.denizenscript.denizencore.utilities.debugging.Debug;
import net.minecraft.server.level.ServerLevel;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_18_R2.CraftWorld;
import org.bukkit.craftbukkit.v1_18_R2.inventory.CraftItemStack;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

public class CustomEntityHelperImpl implements CustomEntityHelper {

    @Override
    public FakeArrow spawnFakeArrow(Location location) {
        CraftWorld world = (CraftWorld) location.getWorld();
        EntityFakeArrowImpl arrow = new EntityFakeArrowImpl(world, location);
        return arrow.getBukkitEntity();
    }

    @Override
    public ItemProjectile spawnItemProjectile(Location location, ItemStack itemStack) {
        CraftWorld world = (CraftWorld) location.getWorld();
        EntityItemProjectileImpl entity = new EntityItemProjectileImpl(world.getHandle(), location, CraftItemStack.asNMSCopy(itemStack));
        world.getHandle().addFreshEntity(entity, CreatureSpawnEvent.SpawnReason.CUSTOM);
        return entity.getBukkitEntity();
    }

    public FakePlayer spawnFakePlayer(Location location, String name, String skin, String blob, boolean doAdd) throws IllegalArgumentException {
        String fullName = name;
        String prefix = null;
        String suffix = null;
        if (name == null) {
            Debug.echoError("FAKE_PLAYER: null name, cannot spawn");
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
                    throw new IllegalArgumentException("You must specify a name with no more than 46 characters for FAKE_PLAYER entities!");
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
            throw new IllegalArgumentException("You must specify a name with no more than 16 characters for FAKE_PLAYER entity skins!");
        }
        CraftWorld world = (CraftWorld) location.getWorld();
        ServerLevel worldServer = world.getHandle();
        PlayerProfile playerProfile = new PlayerProfile(name, null);
        if (blob != null) {
            int sc = blob.indexOf(';');
            if (sc != -1) {
                playerProfile.setTexture(blob.substring(0, sc));
                playerProfile.setTextureSignature(blob.substring(sc + 1));
            }
        }
        else if (skin == null && !name.matches(".*[^A-Za-z0-9_].*")) {
            playerProfile = NMSHandler.instance.fillPlayerProfile(playerProfile);
        }
        if (skin != null) {
            PlayerProfile skinProfile = new PlayerProfile(skin, null);
            skinProfile = NMSHandler.instance.fillPlayerProfile(skinProfile);
            playerProfile.setTexture(skinProfile.getTexture());
            playerProfile.setTextureSignature(skinProfile.getTextureSignature());
        }
        UUID uuid = UUID.randomUUID();
        playerProfile.setUniqueId(uuid);

        GameProfile gameProfile = new GameProfile(playerProfile.getUniqueId(), playerProfile.getName());
        gameProfile.getProperties().put("textures",
                new Property("textures", playerProfile.getTexture(), playerProfile.getTextureSignature()));

        final EntityFakePlayerImpl fakePlayer = new EntityFakePlayerImpl(worldServer.getServer(), worldServer, gameProfile, doAdd);

        fakePlayer.forceSetPositionRotation(location.getX(), location.getY(), location.getZ(),
                location.getYaw(), location.getPitch());
        CraftFakePlayerImpl craftFakePlayer = fakePlayer.getBukkitEntity();
        craftFakePlayer.fullName = fullName;
        if (prefix != null) {
            Scoreboard scoreboard = Bukkit.getScoreboardManager().getMainScoreboard();
            String teamName = "FAKE_PLAYER_TEAM_" + fullName;
            String hash = null;
            try {
                hash = CoreUtilities.hash_md5(teamName.getBytes(StandardCharsets.UTF_8)).substring(0, 16);
            }
            catch (Exception e) {
                Debug.echoError(e);
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
}
