package com.denizenscript.denizen.nms.abstracts;

/*
 * ImprovedOfflinePlayer, a library for Bukkit.
 * Copyright (C) 2013 one4me@github.com
 *
 * Permission is hereby granted, free of charge, to any person obtaining
 * a copy of this software and associated documentation files (the
 * "Software"), to deal in the Software without restriction, including
 * without limitation the rights to use, copy, modify, merge, publish,
 * distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to
 * the following conditions:
 *
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
 * LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
 * OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
 * WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 *
 * @name ImprovedOfflinePlayer
 * @version 1.6.0
 * @author one4me
 *
 *
 *
 * Modified by mcmonkey and the DenizenScript team, to update to more recent Minecraft versions and improve performance
 *
 */

import com.denizenscript.denizen.utilities.Settings;
import com.denizenscript.denizencore.DenizenCore;
import com.denizenscript.denizencore.utilities.CoreConfiguration;
import com.denizenscript.denizencore.utilities.debugging.Debug;
import com.denizenscript.denizencore.utilities.scheduling.OneTimeSchedulable;
import net.kyori.adventure.nbt.*;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.util.Vector;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.UnaryOperator;

public abstract class ImprovedOfflinePlayer {

    public static void invalidateNow(UUID id) {
        ImprovedOfflinePlayer player = offlinePlayers.remove(id);
        if (player != null) {
            if (player.inventory != null) {
                player.setInventory(player.inventory);
            }
            if (player.enderchest != null) {
                player.setEnderChest(player.enderchest);
            }
            if (player.modified) {
                player.saveToFile();
            }
        }
    }

    public static Map<UUID, ImprovedOfflinePlayer> offlinePlayers = new HashMap<>();

    public UUID player;
    public File file;
    public CompoundBinaryTag compound;
    public boolean exists;
    public PlayerInventory inventory;
    public Inventory enderchest;
    public boolean modified = false;
    public long timeLastLoaded;

    public void markModified() {
        if (CoreConfiguration.debugVerbose) {
            Debug.verboseLog("[Verbose] player data modified for " + player + ", wasModified=" + modified + ", delay=" + Settings.worldPlayerDataSaveDelay);
        }
        if (Settings.worldPlayerDataSaveDelay <= 0) {
            saveToFile();
            return;
        }
        if (!modified && Settings.worldPlayerDataSaveDelay < 60 * 60 * 24) {
            DenizenCore.schedule(new OneTimeSchedulable(() -> {
                if (modified && offlinePlayers.get(player) == this) {
                    modified = false;
                    CompoundBinaryTag tag = compound;
                    if (CoreConfiguration.debugVerbose) {
                        Debug.verboseLog("[Verbose] async-saving player data for " + player);
                    }
                    DenizenCore.runAsync(() -> saveInternal(tag));
                }
            }, Settings.worldPlayerDataSaveDelay));
        }
        modified = true;
    }

    public ImprovedOfflinePlayer(UUID playeruuid) {
        timeLastLoaded = DenizenCore.currentTimeMonotonicMillis;
        this.exists = loadPlayerData(playeruuid);
    }

    private void modifyAbilities(UnaryOperator<CompoundBinaryTag> editor) {
        CompoundBinaryTag abilitiesCompoundTag = this.compound.getCompound("abilities");
        this.compound = this.compound.put("abilities", editor.apply(abilitiesCompoundTag));
        markModified();
    }

    public CompoundBinaryTag getBukkitData() {
        return this.compound.getCompound("bukkit", null);
    }

    public String getName() {
        CompoundBinaryTag bukkitData = getBukkitData();
        return bukkitData != null ? bukkitData.getString("lastKnownName", null) : null;
    }

    public abstract PlayerInventory getInventory();

    public abstract void setInventory(PlayerInventory inventory);

    public abstract Inventory getEnderChest();

    public abstract void setEnderChest(Inventory inventory);

    public Location getLocation() {
        ListBinaryTag position = this.compound.getList("Pos");
        ListBinaryTag rotation = this.compound.getList("Rotation");
        return new Location(
                Bukkit.getWorld(new UUID(this.compound.getLong("WorldUUIDMost"),
                        this.compound.getLong("WorldUUIDLeast"))),
                position.getDouble(0),
                position.getDouble(1),
                position.getDouble(2),
                rotation.getFloat(0),
                rotation.getFloat(1)
        );
    }

    public void setLocation(Location location) {
        World w = location.getWorld();
        UUID uuid = w.getUID();
        ListBinaryTag.Builder<DoubleBinaryTag> position = ListBinaryTag.builder(BinaryTagTypes.DOUBLE);
        position.add(DoubleBinaryTag.doubleBinaryTag(location.getX()));
        position.add(DoubleBinaryTag.doubleBinaryTag(location.getY()));
        position.add(DoubleBinaryTag.doubleBinaryTag(location.getZ()));
        ListBinaryTag.Builder<FloatBinaryTag> rotation = ListBinaryTag.builder(BinaryTagTypes.FLOAT);
        rotation.add(FloatBinaryTag.floatBinaryTag(location.getYaw()));
        rotation.add(FloatBinaryTag.floatBinaryTag(location.getPitch()));
        this.compound = CompoundBinaryTag.builder().put(this.compound)
                .putLong("WorldUUIDMost", uuid.getMostSignificantBits())
                .putLong("WorldUUIDLeast", uuid.getLeastSignificantBits())
                .putInt("Dimension", w.getEnvironment().ordinal())
                .put("Pos", position.build())
                .put("Rotation", rotation.build()).build();
        markModified();
    }

    public float getHealthFloat() {
        return this.compound.getFloat("Health");
    }

    public void setHealthFloat(float input) {
        this.compound = compound.putFloat("Health", input);
        markModified();
    }

    public abstract double getMaxHealth();

    public abstract void setMaxHealth(double input);

    protected abstract boolean loadPlayerData(UUID uuid);

    public void saveToFile() {
        if (exists && modified) {
            modified = false;
            saveInternal(compound);
        }
    }

    public abstract void saveInternal(CompoundBinaryTag compound);

    public boolean exists() {
        return this.exists;
    }

    public float getAbsorptionAmount() {
        return this.compound.getFloat("AbsorptionAmount");
    }

    public void setAbsorptionAmount(float input) {
        this.compound = compound.putFloat("AbsorptionAmount", input);
        markModified();
    }

    public void setBedSpawnLocation(Location location) {
        if (location == null && !compound.keySet().contains("SpawnDimension")) {
            return;
        }
        CompoundBinaryTag.Builder builder = CompoundBinaryTag.builder().put(compound);
        if (location != null) {
            builder.putInt("SpawnX", location.getBlockX())
                    .putInt("SpawnY", location.getBlockY())
                    .putInt("SpawnZ", location.getBlockZ())
                    .putFloat("SpawnAngle", location.getYaw())
                    .putString("SpawnDimension", location.getWorld().getKey().toString());
        }
        else {
            builder.remove("SpawnX").remove("SpawnY").remove("SpawnZ").remove("SpawnAngle").remove("SpawnDimension");
        }
        this.compound = builder.build();
        markModified();
        // Must save to file immediately to work with normal Spigot OfflinePlayer API
        saveToFile();
    }

    public boolean isSpawnForced() {
        return this.compound.getBoolean("SpawnForced");
    }

    public void setSpawnForced(boolean spawnForced) {
        this.compound = compound.putBoolean("SpawnForced", spawnForced);
        markModified();
    }

    public float getExhaustion() {
        return this.compound.getFloat("foodExhaustionLevel");
    }

    public void setExhaustion(float input) {
        this.compound = compound.putFloat("foodExhaustionLevel", input);
        markModified();
    }

    public float getExp() {
        return this.compound.getFloat("XpP");
    }

    public void setExp(float input) {
        this.compound = compound.putFloat("XpP", input);
        markModified();
    }

    public float getFallDistance() {
        return this.compound.getFloat("FallDistance");
    }

    public void setFallDistance(float input) {
        this.compound = compound.putFloat("FallDistance", input);
        markModified();
    }

    public int getFireTicks() {
        return this.compound.getShort("Fire");
    }

    public void setFireTicks(int input) {
        this.compound = compound.putShort("Fire", (short) input);
        markModified();
    }

    public float getFlySpeed() {
        return this.compound.getCompound("abilities").getFloat("flySpeed") * 2;
    }

    public void setFlySpeed(float speed) {
        modifyAbilities(abilitiesTag -> abilitiesTag.putFloat("flySpeed", speed / 2));
    }

    public int getFoodLevel() {
        return this.compound.getInt("foodLevel");
    }

    public void setFoodLevel(int input) {
        this.compound = compound.putInt("foodLevel", input);
        markModified();
    }

    public GameMode getGameMode() {
        return GameMode.getByValue(this.compound.getInt("playerGameType"));
    }

    public void setGameMode(GameMode input) {
        this.compound = compound.putInt("playerGameType", input.getValue());
        markModified();
    }

    public boolean getIsOnGround() {
        return compound.getBoolean("OnGround");
    }

    public void setIsOnGround(boolean input) {
        this.compound = compound.putBoolean("OnGround", input);
        markModified();
    }

    public int getItemInHand() {
        return this.compound.getInt("SelectedItemSlot");
    }

    public void setItemInHand(int input) {
        this.compound = compound.putInt("SelectedItemSlot", input);
        markModified();
    }

    public int getLevel() {
        return this.compound.getInt("XpLevel");
    }

    public void setLevel(int input) {
        this.compound = compound.putInt("XpLevel", input);
        markModified();
    }

    public UUID getUniqueId() {
        return this.player;
    }

    public int getRemainingAir() {
        return this.compound.getShort("Air");
    }

    public void setRemainingAir(int input) {
        this.compound = compound.putShort("Air", (short) input);
        markModified();
    }

    public float getSaturation() {
        return this.compound.getFloat("foodSaturationLevel");
    }

    public void setSaturation(float input) {
        this.compound = compound.putFloat("foodSaturationLevel", input);
        markModified();
    }

    public float getScore() {
        return this.compound.getFloat("foodSaturationLevel");
    }

    public void setScore(int input) {
        this.compound = compound.putInt("Score", input);
        markModified();
    }

    public short getTimeAttack() {
        return this.compound.getShort("AttackTime");
    }

    public void setTimeAttack(short input) {
        this.compound = compound.putShort("AttackTime", input);
        markModified();
    }

    public short getTimeDeath() {
        return this.compound.getShort("DeathTime");
    }

    public void setTimeDeath(short input) {
        this.compound = compound.putShort("DeathTime", input);
        markModified();
    }

    public short getTimeHurt() {
        return this.compound.getShort("HurtTime");
    }

    public void setTimeHurt(short input) {
        this.compound = compound.putShort("HurtTime", input);
        markModified();
    }

    public short getTimeSleep() {
        return this.compound.getShort("SleepTimer");
    }

    public void setTimeSleep(short input) {
        this.compound = compound.putShort("SleepTimer", input);
        markModified();
    }

    public int getTotalExperience() {
        return this.compound.getInt("XpTotal");
    }

    public void setTotalExperience(int input) {
        this.compound = compound.putInt("XpTotal", input);
        markModified();
    }

    public Vector getVelocity() {
        ListBinaryTag list = this.compound.getList("Motion");
        return new Vector(list.getDouble(0), list.getDouble(1), list.getDouble(2));
    }

    public void setVelocity(Vector vector) {
        ListBinaryTag.Builder<DoubleBinaryTag> motionBuilder = ListBinaryTag.builder(BinaryTagTypes.DOUBLE);
        motionBuilder.add(DoubleBinaryTag.doubleBinaryTag(vector.getX()));
        motionBuilder.add(DoubleBinaryTag.doubleBinaryTag(vector.getY()));
        motionBuilder.add(DoubleBinaryTag.doubleBinaryTag(vector.getZ()));
        this.compound = compound.put("Motion", motionBuilder.build());
        markModified();
    }

    public float getWalkSpeed() {
        return this.compound.getCompound("abilities").getFloat("walkSpeed") * 2;
    }

    public void setWalkSpeed(float speed) {
        modifyAbilities(abilitiesTag -> abilitiesTag.putFloat("walkSpeed", speed / 2));
    }

    public boolean getAllowFlight() {
        return this.compound.getCompound("abilities").getBoolean("mayfly");
    }

    public void setAllowFlight(boolean allow) {
        modifyAbilities(abilitiesTag -> abilitiesTag.putBoolean("mayfly", allow));
    }

    public void setLastDeathLocation(Location deathLoc) {
        CompoundBinaryTag compoundTag = this.compound.getCompound("LastDeathLocation");
        compoundTag = CompoundBinaryTag.builder().put(compoundTag)
                .putIntArray("pos", new int[] {deathLoc.getBlockX(), deathLoc.getBlockY(), deathLoc.getBlockZ()})
                .putString("dimension", deathLoc.getWorld().getKey().toString()).build();
        this.compound = compound.put("LastDeathLocation", compoundTag);
        markModified();
    }
}
