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
 */

import com.denizenscript.denizen.nms.util.jnbt.*;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.util.Vector;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public abstract class ImprovedOfflinePlayer {

    protected UUID player;
    protected File file;
    protected CompoundTag compound;
    protected boolean exists;
    protected boolean autosave = true;
    public static Map<UUID, PlayerInventory> offlineInventories = new HashMap<>();
    public static Map<UUID, Inventory> offlineEnderChests = new HashMap<>();

    public ImprovedOfflinePlayer(UUID playeruuid) {
        this.exists = loadPlayerData(playeruuid);
    }

    public abstract PlayerInventory getInventory();

    public abstract void setInventory(PlayerInventory inventory);

    public abstract Inventory getEnderChest();

    public abstract void setEnderChest(Inventory inventory);

    public Location getLocation() {
        JNBTListTag position = this.compound.getListTag("Pos");
        JNBTListTag rotation = this.compound.getListTag("Rotation");
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
        List<DoubleTag> position = new ArrayList<>();
        position.add(new DoubleTag(location.getX()));
        position.add(new DoubleTag(location.getY()));
        position.add(new DoubleTag(location.getZ()));
        List<FloatTag> rotation = new ArrayList<>();
        rotation.add(new FloatTag(location.getYaw()));
        rotation.add(new FloatTag(location.getPitch()));
        this.compound = this.compound.createBuilder()
                .putLong("WorldUUIDMost", uuid.getMostSignificantBits())
                .putLong("WorldUUIDLeast", uuid.getLeastSignificantBits())
                .putInt("Dimension", w.getEnvironment().ordinal())
                .put("Pos", new JNBTListTag(DoubleTag.class, position))
                .put("Rotation", new JNBTListTag(FloatTag.class, rotation)).build();
        if (this.autosave) {
            savePlayerData();
        }
    }

    public float getHealthFloat() {
        return this.compound.getFloat("Health");
    }

    public void setHealthFloat(float input) {
        this.compound = compound.createBuilder().putFloat("Health", input).build();
        if (this.autosave) {
            savePlayerData();
        }
    }

    public abstract double getMaxHealth();

    public abstract void setMaxHealth(double input);

    protected abstract boolean loadPlayerData(UUID uuid);

    public abstract void savePlayerData();

    public boolean exists() {
        return this.exists;
    }

    public boolean getAutoSave() {
        return this.autosave;
    }

    public void setAutoSave(boolean autosave) {
        this.autosave = autosave;
    }

    public float getAbsorptionAmount() {
        return this.compound.getFloat("AbsorptionAmount");
    }

    public void setAbsorptionAmount(float input) {
        this.compound = compound.createBuilder().putFloat("AbsorptionAmount", input).build();
        if (this.autosave) {
            savePlayerData();
        }
    }

    public Location getBedSpawnLocation() {
        return new Location(
                Bukkit.getWorld(this.compound.getString("SpawnWorld")),
                this.compound.getInt("SpawnX"),
                this.compound.getInt("SpawnY"),
                this.compound.getInt("SpawnZ")
        );
    }

    public boolean isSpawnForced() {
        return this.compound.getBoolean("SpawnForced");
    }

    public void setBedSpawnLocation(Location location, boolean override) {
        this.compound = compound.createBuilder()
                .putInt("SpawnX", (int) location.getX())
                .putInt("SpawnY", (int) location.getY())
                .putInt("SpawnZ", (int) location.getZ())
                .putString("SpawnWorld", location.getWorld().getName())
                .putBoolean("SpawnForced", override).build();
        if (this.autosave) {
            savePlayerData();
        }
    }

    public float getExhaustion() {
        return this.compound.getFloat("foodExhaustionLevel");
    }

    public void setExhaustion(float input) {
        this.compound = compound.createBuilder().putFloat("foodExhaustionLevel", input).build();
        if (this.autosave) {
            savePlayerData();
        }
    }

    public float getExp() {
        return this.compound.getFloat("XpP");
    }

    public void setExp(float input) {
        this.compound = compound.createBuilder().putFloat("XpP", input).build();
        if (this.autosave) {
            savePlayerData();
        }
    }

    public float getFallDistance() {
        return this.compound.getFloat("FallDistance");
    }

    public void setFallDistance(float input) {
        this.compound = compound.createBuilder().putFloat("FallDistance", input).build();
        if (this.autosave) {
            savePlayerData();
        }
    }

    public int getFireTicks() {
        return this.compound.getShort("Fire");
    }

    public void setFireTicks(int input) {
        this.compound = compound.createBuilder().putShort("Fire", (short) input).build();
        if (this.autosave) {
            savePlayerData();
        }
    }

    public float getFlySpeed() {
        return ((CompoundTag) this.compound.getValue().get("abilities")).getFloat("flySpeed");
    }

    public void setFlySpeed(float speed) {
        CompoundTag compoundTag = (CompoundTag) this.compound.getValue().get("abilities");
        compoundTag = compoundTag.createBuilder().putFloat("flySpeed", speed).build();
        this.compound = compound.createBuilder().put("abilities", compoundTag).build();
        if (this.autosave) {
            savePlayerData();
        }
    }

    public int getFoodLevel() {
        return this.compound.getInt("foodLevel");
    }

    public void setFoodLevel(int input) {
        this.compound = compound.createBuilder().putInt("foodLevel", input).build();
        if (this.autosave) {
            savePlayerData();
        }
    }

    public GameMode getGameMode() {
        return GameMode.getByValue(this.compound.getInt("playerGameType"));
    }

    @SuppressWarnings("deprecation")//Will most likely break in 1.7
    public void setGameMode(GameMode input) {
        this.compound = compound.createBuilder().putInt("playerGameType", input.getValue()).build();
        if (this.autosave) {
            savePlayerData();
        }
    }

    public boolean getIsOnGround() {
        return compound.getBoolean("OnGround");
    }

    public void setIsOnGround(boolean input) {
        this.compound = compound.createBuilder().putBoolean("OnGround", input).build();
        if (this.autosave) {
            savePlayerData();
        }
    }

    public int getItemInHand() {
        return this.compound.getInt("SelectedItemSlot");
    }

    public void setItemInHand(int input) {
        this.compound = compound.createBuilder().putInt("SelectedItemSlot", input).build();
        if (this.autosave) {
            savePlayerData();
        }
    }

    public int getLevel() {
        return this.compound.getInt("XpLevel");
    }

    public void setLevel(int input) {
        this.compound = compound.createBuilder().putInt("XpLevel", input).build();
        if (this.autosave) {
            savePlayerData();
        }
    }

    public UUID getUniqueId() {
        return this.player;
    }

    public int getRemainingAir() {
        return this.compound.getShort("Air");
    }

    public void setRemainingAir(int input) {
        this.compound = compound.createBuilder().putShort("Air", (short) input).build();
        if (this.autosave) {
            savePlayerData();
        }
    }

    public float getSaturation() {
        return this.compound.getFloat("foodSaturationLevel");
    }

    public void setSaturation(float input) {
        this.compound = compound.createBuilder().putFloat("foodSaturationLevel", input).build();
        if (this.autosave) {
            savePlayerData();
        }
    }

    public float getScore() {
        return this.compound.getFloat("foodSaturationLevel");
    }

    public void setScore(int input) {
        this.compound = compound.createBuilder().putInt("Score", input).build();
        if (this.autosave) {
            savePlayerData();
        }
    }

    public short getTimeAttack() {
        return this.compound.getShort("AttackTime");
    }

    public void setTimeAttack(short input) {
        this.compound = compound.createBuilder().putShort("AttackTime", input).build();
        if (this.autosave) {
            savePlayerData();
        }
    }

    public short getTimeDeath() {
        return this.compound.getShort("DeathTime");
    }

    public void setTimeDeath(short input) {
        this.compound = compound.createBuilder().putShort("DeathTime", input).build();
        if (this.autosave) {
            savePlayerData();
        }
    }

    public short getTimeHurt() {
        return this.compound.getShort("HurtTime");
    }

    public void setTimeHurt(short input) {
        this.compound = compound.createBuilder().putShort("HurtTime", input).build();
        if (this.autosave) {
            savePlayerData();
        }
    }

    public short getTimeSleep() {
        return this.compound.getShort("SleepTimer");
    }

    public void setTimeSleep(short input) {
        this.compound = compound.createBuilder().putShort("SleepTimer", input).build();
        if (this.autosave) {
            savePlayerData();
        }
    }

    public int getTotalExperience() {
        return this.compound.getInt("XpTotal");
    }

    public void setTotalExperience(int input) {
        this.compound = compound.createBuilder().putInt("XpTotal", input).build();
        if (this.autosave) {
            savePlayerData();
        }
    }

    public Vector getVelocity() {
        JNBTListTag list = this.compound.getListTag("Motion");
        return new Vector(list.getDouble(0), list.getDouble(1), list.getDouble(2));
    }

    public void setVelocity(Vector vector) {
        List<DoubleTag> motion = new ArrayList<>();
        motion.add(new DoubleTag(vector.getX()));
        motion.add(new DoubleTag(vector.getY()));
        motion.add(new DoubleTag(vector.getZ()));
        this.compound = compound.createBuilder().put("Motion", new JNBTListTag(DoubleTag.class, motion)).build();
        if (this.autosave) {
            savePlayerData();
        }
    }

    public float getWalkSpeed() {
        return ((CompoundTag) this.compound.getValue().get("abilities")).getFloat("walkSpeed");
    }

    public void setWalkSpeed(float speed) {
        CompoundTag compoundTag = (CompoundTag) this.compound.getValue().get("abilities");
        compoundTag = compoundTag.createBuilder().putFloat("walkSpeed", speed).build();
        this.compound = compound.createBuilder().put("abilities", compoundTag).build();
        if (this.autosave) {
            savePlayerData();
        }
    }

    public boolean getAllowFlight() {
        return ((CompoundTag) this.compound.getValue().get("abilities")).getBoolean("mayfly");
    }

    public void setAllowFlight(boolean allow) {
        CompoundTag compoundTag = (CompoundTag) this.compound.getValue().get("abilities");
        compoundTag = compoundTag.createBuilder().putBoolean("mayfly", allow).build();
        this.compound = compound.createBuilder().put("abilities", compoundTag).build();
        if (this.autosave) {
            savePlayerData();
        }
    }

    public void setLastDeathLocation(Location deathLoc) {
        CompoundTag compoundTag = (CompoundTag) this.compound.getValue().get("LastDeathLocation");
        compoundTag = compoundTag.createBuilder()
                .putIntArray("pos", new int[] {deathLoc.getBlockX(), deathLoc.getBlockY(), deathLoc.getBlockZ()})
                .putString("dimension", deathLoc.getWorld().getKey().toString())
                .build();
        this.compound = compound.createBuilder().put("LastDeathLocation", compoundTag).build();
        if (this.autosave) {
            savePlayerData();
        }
    }
}
