package net.aufdemrand.denizen.utilities.nbt;

// NMS/CB imports start

import com.google.common.io.Files;
import net.aufdemrand.denizen.scripts.containers.core.InventoryScriptHelper;
import net.minecraft.server.v1_9_R1.*;
import org.bukkit.*;
import org.bukkit.World;
import org.bukkit.craftbukkit.v1_9_R1.inventory.CraftInventory;
import org.bukkit.craftbukkit.v1_9_R1.inventory.CraftInventoryPlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.UUID;

// NMS/CB imports end

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

public class ImprovedOfflinePlayer {

    private UUID player;
    private File file;
    private NBTTagCompound compound;
    private boolean exists = false;
    private boolean autosave = true;

    public ImprovedOfflinePlayer(UUID playeruuid) {
        this.exists = loadPlayerData(playeruuid);
    }

    public ImprovedOfflinePlayer(OfflinePlayer offlineplayer) {
        this.exists = loadPlayerData(offlineplayer.getUniqueId());
    }

    public org.bukkit.inventory.PlayerInventory getInventory() {
        if (InventoryScriptHelper.offlineInventories.containsKey(getUniqueId())) {
            return InventoryScriptHelper.offlineInventories.get(getUniqueId());
        }
        PlayerInventory inventory = new PlayerInventory(null);
        inventory.b(this.compound.getList("Inventory", 10));
        org.bukkit.inventory.PlayerInventory inv = new CraftInventoryPlayer(inventory);
        InventoryScriptHelper.offlineInventories.put(getUniqueId(), inv);
        return inv;
    }

    public void setInventory(org.bukkit.inventory.PlayerInventory inventory) {
        CraftInventoryPlayer inv = (CraftInventoryPlayer) inventory;
        this.compound.set("Inventory", inv.getInventory().a(new NBTTagList()));
        if (this.autosave) {
            savePlayerData();
        }
    }

    public Inventory getEnderChest() {
        if (InventoryScriptHelper.offlineEnderChests.containsKey(getUniqueId())) {
            return InventoryScriptHelper.offlineEnderChests.get(getUniqueId());
        }
        InventoryEnderChest endchest = new InventoryEnderChest();
        endchest.a(this.compound.getList("EnderItems", 10));
        org.bukkit.inventory.Inventory inv = new CraftInventory(endchest);
        InventoryScriptHelper.offlineEnderChests.put(getUniqueId(), inv);
        return inv;
    }

    public void setEnderChest(Inventory inventory) {
        this.compound.set("EnderItems", ((InventoryEnderChest) ((CraftInventory) inventory).getInventory()).h());
        if (this.autosave) {
            savePlayerData();
        }
    }

    public Location getLocation() {
        NBTTagList position = this.compound.getList("Pos", 6);
        NBTTagList rotation = this.compound.getList("Rotation", 5);
        return new Location(
                Bukkit.getWorld(new UUID(this.compound.getLong("WorldUUIDMost"),
                        this.compound.getLong("WorldUUIDLeast"))),
                position.e(0),
                position.e(1),
                position.e(2),
                rotation.f(0),
                rotation.f(1)
        );
    }

    public void setLocation(Location location) {
        World w = location.getWorld();
        UUID uuid = w.getUID();
        this.compound.setLong("WorldUUIDMost", uuid.getMostSignificantBits());
        this.compound.setLong("WorldUUIDLeast", uuid.getLeastSignificantBits());
        this.compound.setInt("Dimension", w.getEnvironment().ordinal());
        NBTTagList position = new NBTTagList();
        position.add(new NBTTagDouble(location.getX()));
        position.add(new NBTTagDouble(location.getY()));
        position.add(new NBTTagDouble(location.getZ()));
        this.compound.set("Pos", position);
        NBTTagList rotation = new NBTTagList();
        rotation.add(new NBTTagFloat(location.getYaw()));
        rotation.add(new NBTTagFloat(location.getPitch()));
        this.compound.set("Rotation", rotation);
        if (this.autosave) {
            savePlayerData();
        }
    }

    public float getHealthFloat() {
        return this.compound.getFloat("HealF");
    }

    public void setHealthFloat(float input) {
        this.compound.setFloat("HealF", input);
        if (this.autosave) {
            savePlayerData();
        }
    }

    public double getMaxHealth() {
        return getAttributes().a("generic.maxHealth").getValue();
    }

    public void setMaxHealth(double input) {
        AttributeMapBase attributes = getAttributes();
        attributes.a("generic.maxHealth").setValue(input);
        setAttributes(attributes);
    }

    private boolean loadPlayerData(UUID uuid) {
        try {
            this.player = uuid;
            for (World w : Bukkit.getWorlds()) {
                this.file = new File(w.getWorldFolder(), "playerdata" + File.separator + this.player + ".dat");
                if (this.file.exists()) {
                    this.compound = NBTCompressedStreamTools.a(new FileInputStream(this.file));
                    return true;
                }
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public void savePlayerData() {
        if (this.exists) {
            try {
                NBTCompressedStreamTools.a(this.compound, new FileOutputStream(this.file));
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public boolean exists() {
        return this.exists;
    }

    public boolean getAutoSave() {
        return this.autosave;
    }

    public void setAutoSave(boolean autosave) {
        this.autosave = autosave;
    }

    public void copyDataTo(UUID playeruuid) {
        try {
            if (!playeruuid.equals(this.player)) {
                Player to = Bukkit.getPlayer(playeruuid);
                Player from = Bukkit.getPlayer(playeruuid);
                if (from != null) {
                    from.saveData();
                }
                Files.copy(this.file, new File(this.file.getParentFile(), playeruuid + ".dat"));
                if (to != null) {
                    to.teleport(from == null ? getLocation() : from.getLocation());
                    to.loadData();
                }
            }
            else {
                Player player = Bukkit.getPlayer(playeruuid);
                if (player != null) {
                    player.saveData();
                }
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public PlayerAbilities getAbilities() {
        PlayerAbilities pa = new PlayerAbilities();
        pa.a(this.compound);
        return pa;
    }

    public void setAbilities(PlayerAbilities abilities) {
        abilities.a(this.compound);
        if (this.autosave) {
            savePlayerData();
        }
    }

    public float getAbsorptionAmount() {
        return this.compound.getFloat("AbsorptionAmount");
    }

    public void setAbsorptionAmount(float input) {
        this.compound.setFloat("AbsorptionAmount", input);
        if (this.autosave) {
            savePlayerData();
        }
    }

    public AttributeMapBase getAttributes() {
        AttributeMapBase amb = (AttributeMapBase) new AttributeMapServer();
        GenericAttributes.a(amb, this.compound.getList("Attributes", 0));
        return amb;
    }

    public void setAttributes(AttributeMapBase attributes) {
        this.compound.set("Attributes", GenericAttributes.a(attributes));
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

    public void setBedSpawnLocation(Location location, Boolean override) {
        this.compound.setInt("SpawnX", (int) location.getX());
        this.compound.setInt("SpawnY", (int) location.getY());
        this.compound.setInt("SpawnZ", (int) location.getZ());
        this.compound.setString("SpawnWorld", location.getWorld().getName());
        this.compound.setBoolean("SpawnForced", override == null ? false : override);
        if (this.autosave) {
            savePlayerData();
        }
    }

    public float getExhaustion() {
        return this.compound.getFloat("foodExhaustionLevel");
    }

    public void setExhaustion(float input) {
        this.compound.setFloat("foodExhaustionLevel", input);
        if (this.autosave) {
            savePlayerData();
        }
    }

    public float getExp() {
        return this.compound.getFloat("XpP");
    }

    public void setExp(float input) {
        this.compound.setFloat("XpP", input);
        if (this.autosave) {
            savePlayerData();
        }
    }

    public float getFallDistance() {
        return this.compound.getFloat("FallDistance");
    }

    public void setFallDistance(float input) {
        this.compound.setFloat("FallDistance", input);
        if (this.autosave) {
            savePlayerData();
        }
    }

    public int getFireTicks() {
        return this.compound.getShort("Fire");
    }

    public void setFireTicks(int input) {
        this.compound.setShort("Fire", (short) input);
        if (this.autosave) {
            savePlayerData();
        }
    }

    public float getFlySpeed() {
        return this.compound.getCompound("abilities").getFloat("flySpeed");
    }

    public void setFlySpeed(float speed) {
        this.compound.getCompound("abilities").setFloat("flySpeed", speed);
        if (this.autosave) {
            savePlayerData();
        }
    }

    public int getFoodLevel() {
        return this.compound.getInt("foodLevel");
    }

    public void setFoodLevel(int input) {
        this.compound.setInt("foodLevel", input);
        if (this.autosave) {
            savePlayerData();
        }
    }

    public int getFoodTickTimer() {
        return this.compound.getInt("foodTickTimer");
    }

    public void setFoodTickTimer(int input) {
        this.compound.setInt("foodTickTimer", input);
        if (this.autosave) {
            savePlayerData();
        }
    }

    public GameMode getGameMode() {
        return GameMode.values()[this.compound.getInt("playerGameType")];
    }

    @SuppressWarnings("deprecation")//Will most likely break in 1.7
    public void setGameMode(GameMode input) {
        this.compound.setInt("playerGameType", input.getValue());
        if (this.autosave) {
            savePlayerData();
        }
    }

    public int getHealthInt() {
        return this.compound.getShort("Health");
    }

    public void setHealthInt(int input) {
        this.compound.setShort("Health", (short) input);
        if (this.autosave) {
            savePlayerData();
        }
    }

    public boolean getIsInvulnerable() {
        return compound.getBoolean("Invulnerable");
    }

    public void setIsInvulnerable(boolean input) {
        this.compound.setBoolean("Invulnerable", input);
        if (this.autosave) {
            savePlayerData();
        }
    }

    public boolean getIsOnGround() {
        return compound.getBoolean("OnGround");
    }

    public void setIsOnGround(boolean input) {
        this.compound.setBoolean("OnGround", input);
        if (this.autosave) {
            savePlayerData();
        }
    }

    public boolean getIsSleeping() {
        return this.compound.getBoolean("Sleeping");
    }

    public void setIsSleeping(boolean input) {
        this.compound.setBoolean("Sleeping", input);
        if (this.autosave) {
            savePlayerData();
        }
    }

    public int getItemInHand() {
        return this.compound.getInt("SelectedItemSlot");
    }

    public void setItemInHand(int input) {
        this.compound.setInt("SelectedItemSlot", input);
        if (this.autosave) {
            savePlayerData();
        }
    }

    public int getLevel() {
        return this.compound.getInt("XpLevel");
    }

    public void setLevel(int input) {
        this.compound.setInt("XpLevel", input);
        if (this.autosave) {
            savePlayerData();
        }
    }

    public UUID getUniqueId() {
        return this.player;
    }

    public int getPortalCooldown() {
        return this.compound.getInt("PortalCooldown");
    }

    public void setPortalCooldown(int input) {
        this.compound.setInt("PortalCooldown", input);
        if (this.autosave) {
            savePlayerData();
        }
    }

    @SuppressWarnings("deprecation")//Will most likely break in 1.7
    public ArrayList<PotionEffect> getPotionEffects() {
        ArrayList<PotionEffect> effects = new ArrayList<PotionEffect>();
        if (this.compound.hasKey("ActiveEffects")) {
            NBTTagList list = this.compound.getList("ActiveEffects", 0);
            for (int i = 0; i < list.size(); i++) {
                NBTTagCompound effect = (NBTTagCompound) list.get(i);
                byte amp = effect.getByte("Amplifier");
                byte id = effect.getByte("Id");
                int time = effect.getInt("Duration");
                effects.add(new PotionEffect(PotionEffectType.getById(id), time, amp));
            }
        }
        return effects;
    }

    @SuppressWarnings("deprecation")//Will most likely break in 1.7
    public void setPotionEffects(ArrayList<PotionEffect> effects) {
        if (effects.isEmpty()) {
            this.compound.remove("ActiveEffects");
            if (this.autosave) {
                savePlayerData();
            }
            return;
        }
        NBTTagList activeEffects = new NBTTagList();
        for (PotionEffect pe : effects) {
            NBTTagCompound eCompound = new NBTTagCompound();
            eCompound.setByte("Amplifier", (byte) (pe.getAmplifier()));
            eCompound.setByte("Id", (byte) (pe.getType().getId()));
            eCompound.setInt("Duration", (int) (pe.getDuration()));
            activeEffects.add(eCompound);
        }
        this.compound.set("ActiveEffects", activeEffects);
        if (this.autosave) {
            savePlayerData();
        }
    }

    public int getRemainingAir() {
        return this.compound.getShort("Air");
    }

    public void setRemainingAir(int input) {
        this.compound.setShort("Air", (short) input);
        if (this.autosave) {
            savePlayerData();
        }
    }

    public float getSaturation() {
        return this.compound.getFloat("foodSaturationLevel");
    }

    public void setSaturation(float input) {
        this.compound.setFloat("foodSaturationLevel", input);
        if (this.autosave) {
            savePlayerData();
        }
    }

    public float getScore() {
        return this.compound.getFloat("foodSaturationLevel");
    }

    public void setScore(int input) {
        this.compound.setInt("Score", input);
        if (this.autosave) {
            savePlayerData();
        }
    }

    public short getTimeAttack() {
        return this.compound.getShort("AttackTime");
    }

    public void setTimeAttack(short input) {
        this.compound.setShort("AttackTime", input);
        if (this.autosave) {
            savePlayerData();
        }
    }

    public short getTimeDeath() {
        return this.compound.getShort("DeathTime");
    }

    public void setTimeDeath(short input) {
        this.compound.setShort("DeathTime", input);
        if (this.autosave) {
            savePlayerData();
        }
    }

    public short getTimeHurt() {
        return this.compound.getShort("HurtTime");
    }

    public void setTimeHurt(short input) {
        this.compound.setShort("HurtTime", input);
        if (this.autosave) {
            savePlayerData();
        }
    }

    public short getTimeSleep() {
        return this.compound.getShort("SleepTimer");
    }

    public void setTimeSleep(short input) {
        this.compound.setShort("SleepTimer", input);
        if (this.autosave) {
            savePlayerData();
        }
    }

    public int getTotalExperience() {
        return this.compound.getInt("XpTotal");
    }

    public void setTotalExperience(int input) {
        this.compound.setInt("XpTotal", input);
        if (this.autosave) {
            savePlayerData();
        }
    }

    public Vector getVelocity() {
        NBTTagList list = this.compound.getList("Motion", 6);
        return new Vector(list.e(0), list.e(1), list.e(2));
    }

    public void setVelocity(Vector vector) {
        NBTTagList motion = new NBTTagList();
        motion.add(new NBTTagDouble(vector.getX()));
        motion.add(new NBTTagDouble(vector.getY()));
        motion.add(new NBTTagDouble(vector.getZ()));
        this.compound.set("Motion", motion);
        if (this.autosave) {
            savePlayerData();
        }
    }

    public float getWalkSpeed() {
        return this.compound.getCompound("abilities").getFloat("walkSpeed");
    }

    public void setWalkSpeed(float speed) {
        this.compound.getCompound("abilities").setFloat("walkSpeed", speed);
        if (this.autosave) {
            savePlayerData();
        }
    }
}
/*
 * Copyright (C) 2013 one4me@github.com
 */
