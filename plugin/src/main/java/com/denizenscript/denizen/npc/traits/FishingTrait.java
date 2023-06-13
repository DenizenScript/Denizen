package com.denizenscript.denizen.npc.traits;

import com.denizenscript.denizen.objects.NPCTag;
import com.denizenscript.denizencore.utilities.debugging.Debug;
import com.denizenscript.denizen.nms.NMSHandler;
import com.denizenscript.denizen.nms.interfaces.FishingHelper;
import com.denizenscript.denizencore.utilities.CoreUtilities;
import net.citizensnpcs.api.persistence.Persist;
import net.citizensnpcs.api.trait.Trait;
import net.citizensnpcs.util.PlayerAnimation;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.FishHook;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.projectiles.ProjectileSource;
import org.bukkit.util.Vector;

public class FishingTrait extends Trait {

    @Persist("fishing")
    public boolean fishing = false;
    @Persist("catch type")
    public FishingHelper.CatchType catchType = FishingHelper.CatchType.NONE;

    @Persist("fishing spot")
    public Location fishingLocation = null;

    public FishHook fishHook = null;
    public Item fish = null;

    @Persist("catch chance")
    public int catchPercent = 65;

    @Persist("reel tick rate")
    public int reelTickRate = 400;

    @Persist("cast tick rate")
    public int castTickRate = 75;

    int reelCount = 100;
    int castCount = 0;

    public boolean isCast = false;

    @Override
    public void onSpawn() {
        isCast = false;
    }

    @Override
    public void run() {
        if (fish != null) {
            if (fish.getLocation().distance(npc.getStoredLocation()) < 3) {
                try {
                    fish.remove();
                }
                catch (Exception e) {
                }
            }
        }
        if (!fishing) {
            isCast = false;
            return;
        }
        if (isCast) {
            reelCount++;
            if (reelCount >= reelTickRate) {
                reel();
                reelCount = 0;
                castCount = 0;
            }
        }
        else {
            castCount++;
            if (castCount >= castTickRate) {
                cast();
                castCount = 0;
            }
        }
    }

    // <--[action]
    // @Actions
    // start fishing
    //
    // @Triggers when the NPC starts fishing. See also <@link command fish>.
    //
    // @Context
    // None
    //
    // -->

    /**
     * Makes the NPC fish at the specified location
     * <p/>
     * TODO Reimplement variance, so each cast doesn't land in the exact same spot.
     *
     * @param location the location to fish at
     */
    public void startFishing(Location location) {
        new NPCTag(npc).action("start fishing", null);
        fishingLocation = location.clone();
        cast();
        fishing = true;
    }

    // <--[action]
    // @Actions
    // stop fishing
    //
    // @Triggers when the NPC stops fishing. See also <@link command fish>.
    //
    // @Context
    // None
    //
    // -->

    /**
     * Makes the stop fishing.
     */
    public void stopFishing() {
        new NPCTag(npc).action("stop fishing", null);
        reel();
        reelCount = 100;
        castCount = 0;
        fishingLocation = null;
        fishing = false;
    }

    public boolean scanForFishSpot(Location near, boolean horizontal) {
        Block block = near.getBlock();
        if (block.getType() == Material.WATER) {
            fishingLocation = near.clone();
            return true;
        }
        else if (block.getRelative(BlockFace.DOWN).getType() == Material.WATER) {
            fishingLocation = near.clone().add(0, -1, 0);
            return true;
        }
        else if (block.getRelative(BlockFace.DOWN).getRelative(BlockFace.DOWN).getType() == Material.WATER) {
            fishingLocation = near.clone().add(0, -2, 0);
            return true;
        }
        if (horizontal) {
            return scanForFishSpot(near.clone().add(1, 0, 0), false)
                    || scanForFishSpot(near.clone().add(-1, 0, 0), false)
                    || scanForFishSpot(near.clone().add(0, 0, 1), false)
                    || scanForFishSpot(near.clone().add(0, 0, -1), false);
        }
        return false;
    }

    public void startFishing() {
        fishing = true;
        Location search = npc.getStoredLocation().clone();
        Vector direction = npc.getStoredLocation().getDirection().clone();
        if (direction.getY() > -0.1) {
            direction.setY(-0.1);
        }
        for (int i = 0; i < 10; i++) {
            search.add(direction.clone());
            if (scanForFishSpot(search, true)) {
                break;
            }
        }
    }

    // <--[action]
    // @Actions
    // cast fishing rod
    //
    // @Triggers when the NPC casts a fishing rod. See also <@link command fish>.
    //
    // @Context
    // None
    //
    // -->
    private void cast() {
        new NPCTag(npc).action("cast fishing rod", null);
        if (fishingLocation == null || fishingLocation.getWorld() == null || !fishingLocation.getWorld().equals(npc.getEntity().getWorld())) {
            Debug.echoError("Fishing location not found!");
            return;
        }
        isCast = true;
        double v = 34;
        double g = 20;
        Location from = npc.getStoredLocation();
        from = from.add(0, 0.33, 0);
        Location to = fishingLocation;
        Vector test = to.clone().subtract(from).toVector();
        double elev = test.getY();
        Double testAngle = launchAngle(from, to, v, elev, g);
        if (testAngle == null) {
            return;
        }
        double hangtime = hangtime(testAngle, v, elev, g);
        Vector victor = to.clone().subtract(from).toVector();
        double dist = Math.sqrt(Math.pow(victor.getX(), 2) + Math.pow(victor.getZ(), 2));
        elev = victor.getY();
        if (dist == 0) {
            return;
        }
        Double launchAngle = launchAngle(from, to, v, elev, g);
        if (launchAngle == null) {
            return;
        }
        victor.setY(Math.tan(launchAngle) * dist);
        victor = normalizeVector(victor);
        v += 0.5 * Math.pow(hangtime, 2);
        v += (CoreUtilities.getRandom().nextDouble() - 0.8) / 2;
        victor = victor.multiply(v / 20.0);

        if (npc.getEntity() instanceof Player) {
            fishHook = NMSHandler.fishingHelper.spawnHook(from, (Player) npc.getEntity());
            fishHook.setShooter((ProjectileSource) npc.getEntity());
            fishHook.setVelocity(victor);
            PlayerAnimation.ARM_SWING.play((Player) npc.getEntity());
        }
    }

    // <--[action]
    // @Actions
    // reel in fishing rod
    //
    // @Triggers when the NPC reels in its fishing rod. See also <@link command fish>.
    //
    // @Context
    // None
    //
    // -->

    // <--[action]
    // @Actions
    // catch fish
    //
    // @Triggers when the NPC catches a fish. See also <@link command fish>.
    //
    // @Context
    // None
    //
    // -->
    private void reel() {
        isCast = false;
        new NPCTag(npc).action("reel in fishing rod", null);
        int chance = (int) (Math.random() * 100);
        if (catchPercent > chance && fishHook != null && catchType != FishingHelper.CatchType.NONE) {
            try {
                fish.remove();
            }
            catch (Exception e) {
            }
            Location location = fishHook.getLocation();
            ItemStack result = NMSHandler.fishingHelper.getResult(fishHook, catchType);
            if (result != null) {
                fish = location.getWorld().dropItem(location, result);
                Location npcLocation = npc.getStoredLocation();
                double d5 = npcLocation.getX() - location.getX();
                double d6 = npcLocation.getY() - location.getY();
                double d7 = npcLocation.getZ() - location.getZ();
                double d8 = Math.sqrt(d5 * d5 + d6 * d6 + d7 * d7);
                double d9 = 0.1D;
                fish.setVelocity(new Vector(d5 * d9, d6 * d9 + Math.sqrt(d8) * 0.08D, d7 * d9));
            }
            new NPCTag(npc).action("catch fish", null);
        }
        if (fishHook != null && fishHook.isValid()) {
            fishHook.remove();
            fishHook = null;
        }
        if (npc.getEntity() instanceof Player) {
            PlayerAnimation.ARM_SWING.play((Player) npc.getEntity());
        }
    }

    public boolean isFishing() {
        return fishing;
    }

    public FishingTrait() {
        super("fishing");
    }

    public static Double launchAngle(Location from, Location to, double v, double elev, double g) {
        Vector victor = from.clone().subtract(to).toVector();
        double dist = Math.sqrt(Math.pow(victor.getX(), 2) + Math.pow(victor.getZ(), 2));
        double v2 = Math.pow(v, 2);
        double v4 = Math.pow(v, 4);
        double derp = g * (g * Math.pow(dist, 2) + 2 * elev * v2);

        if (v4 < derp) {
            return null;
        }
        else {
            return Math.atan((v2 - Math.sqrt(v4 - derp)) / (g * dist));
        }
    }

    public static double hangtime(double launchAngle, double v, double elev, double g) {
        double a = v * Math.sin(launchAngle);
        double b = -2 * g * elev;

        if (Math.pow(a, 2) + b < 0) {
            return 0;
        }

        return (a + Math.sqrt(Math.pow(a, 2) + b)) / g;
    }

    public static Vector normalizeVector(Vector victor) {
        double mag = Math.sqrt(Math.pow(victor.getX(), 2) + Math.pow(victor.getY(), 2) + Math.pow(victor.getZ(), 2));
        if (mag != 0) {
            return victor.multiply(1 / mag);
        }
        return victor.multiply(0);
    }

    public void setCatchType(FishingHelper.CatchType catchType) {
        this.catchType = catchType;
    }

    public void setCatchPercent(int catchPercent) {
        this.catchPercent = catchPercent;
    }
}
