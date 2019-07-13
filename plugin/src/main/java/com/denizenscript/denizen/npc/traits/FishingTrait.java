package com.denizenscript.denizen.npc.traits;

import com.denizenscript.denizen.utilities.DenizenAPI;
import com.denizenscript.denizen.utilities.debugging.dB;
import com.denizenscript.denizen.nms.NMSHandler;
import com.denizenscript.denizen.nms.interfaces.FishingHelper;
import com.denizenscript.denizencore.utilities.CoreUtilities;
import net.citizensnpcs.api.persistence.Persist;
import net.citizensnpcs.api.trait.Trait;
import net.citizensnpcs.util.PlayerAnimation;
import org.bukkit.Location;
import org.bukkit.entity.FishHook;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.projectiles.ProjectileSource;
import org.bukkit.util.Vector;

import java.util.ArrayList;

public class FishingTrait extends Trait {

    @Persist("fishing")
    private boolean fishing = false;
    @Persist("catch type")
    private FishingHelper.CatchType catchType = FishingHelper.CatchType.NONE;

    @Persist("fishing spot")
    private Location fishingLocation = null;

    ArrayList<Location> available = new ArrayList<>();

    Location fishingSpot = null;
    FishHook fishHook = null;
    Item fish = null;

    @Persist("catch chance")
    int catchPercent = 65;

    int reelCount = 100;
    int castCount = 0;

    @Override
    public void run() {
        reelCount++;
        castCount++;
        if (fish != null) {
            if (fish.getLocation().distance(npc.getEntity().getLocation()) < 3) {
                try {
                    fish.remove();
                }
                catch (Exception e) {
                }
            }
        }
        if (!fishing) {
            return;
        }

        if (reelCount == 400) {
            reel();
            reelCount = 0;
            castCount = 325;
        }

        if (castCount == 400) {
            cast();
            castCount = 0;
        }
    }

    // <--[action]
    // @Actions
    // start fishing
    //
    // @Triggers when the NPC starts fishing.
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
        DenizenAPI.getDenizenNPC(npc).action("start fishing", null);
        fishingLocation = location.clone();
        cast();
        fishing = true;
    }

    // <--[action]
    // @Actions
    // stop fishing
    //
    // @Triggers when the NPC stops fishing.
    //
    // @Context
    // None
    //
    // -->

    /**
     * Makes the stop fishing.
     */
    public void stopFishing() {
        DenizenAPI.getDenizenNPC(npc).action("stop fishing", null);
        reel();
        reelCount = 100;
        castCount = 0;
        fishingLocation = null;
        fishing = false;
    }

    /**
     * Makes the NPC fish in the nearest water
     * <p/>
     * TODO Needs logic for handling that.
     */
    public void startFishing() {
        fishing = true;
        fishingLocation = npc.getEntity().getLocation();
    }

    // <--[action]
    // @Actions
    // cast fishing rod
    //
    // @Triggers when the NPC casts a fishing rod.
    //
    // @Context
    // None
    //
    // -->
    private void cast() {
        DenizenAPI.getDenizenNPC(npc).action("cast fishing rod", null);

        if (fishingLocation == null) {
            dB.echoError("Fishing location not found!");
            return;
        }

        double v = 34;
        double g = 20;

        Location from = npc.getEntity().getLocation();
        from = from.add(0, .33, 0);
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
        v = v + (.5 * Math.pow(hangtime, 2));
        //Random rand = new Random(1234);
        v = v + (CoreUtilities.getRandom().nextDouble() - .8) / 2;
        victor = victor.multiply(v / 20.0);

        fishHook = NMSHandler.getInstance().getFishingHelper().spawnHook(from, (Player) npc.getEntity());
        fishHook.setShooter((ProjectileSource) npc.getEntity());
        fishHook.setVelocity(victor);

        PlayerAnimation.ARM_SWING.play((Player) npc.getEntity());
    }

    // <--[action]
    // @Actions
    // reel in fishing rod
    //
    // @Triggers when the NPC reels in its fishing rod.
    //
    // @Context
    // None
    //
    // -->
    private void reel() {
        DenizenAPI.getDenizenNPC(npc).action("reel in fishing rod", null);

        int chance = (int) (Math.random() * 100);

        try {
            fishHook.remove();
        }
        catch (Exception e) {
        }

        if (catchPercent > chance && fishHook != null && catchType != FishingHelper.CatchType.NONE) {
            try {
                fish.remove();
            }
            catch (Exception e) {
            }
            Location location = fishHook.getLocation();
            ItemStack result = NMSHandler.getInstance().getFishingHelper().getResult(fishHook, catchType);
            if (result != null) {
                fish = location.getWorld().dropItem(location, result);
                Location npcLocation = npc.getEntity().getLocation();
                double d5 = npcLocation.getX() - location.getX();
                double d6 = npcLocation.getY() - location.getY();
                double d7 = npcLocation.getZ() - location.getZ();
                double d8 = Math.sqrt(d5 * d5 + d6 * d6 + d7 * d7);
                double d9 = 0.1D;
                fish.setVelocity(new Vector(d5 * d9, d6 * d9 + Math.sqrt(d8) * 0.08D, d7 * d9));
            }
            DenizenAPI.getDenizenNPC(npc).action("catch fish", null);
        }

        PlayerAnimation.ARM_SWING.play((Player) npc.getEntity());
    }

    /**
     * Checks if the NPC is currently fishing
     *
     * @return boolean
     */
    public boolean isFishing() {
        return fishing;
    }

    /**
     * Gets the location the NPC is casting to
     * Returns null if the NPC isn't fishing
     *
     * @return Location
     */
    public Location getFishingLocation() {
        return fishingLocation;
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
