package net.aufdemrand.denizen.npc.traits;

import net.aufdemrand.denizen.utilities.DenizenAPI;
import net.aufdemrand.denizen.utilities.debugging.dB;
import net.aufdemrand.denizencore.utilities.CoreUtilities;
import net.citizensnpcs.api.persistence.Persist;
import net.citizensnpcs.api.trait.Trait;
import net.citizensnpcs.util.PlayerAnimation;
import net.minecraft.server.v1_10_R1.*;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_10_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_10_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.projectiles.ProjectileSource;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;

public class FishingTrait extends Trait {

    public enum CatchType {NONE, DEFAULT, JUNK, TREASURE, FISH}

    @Persist("fishing")
    private boolean fishing = false;
    @Persist("catch type")
    private CatchType catchType = CatchType.NONE;

    @Persist("fishing spot")
    private Location fishingLocation = null;

    ArrayList<Location> available = new ArrayList<Location>();
    EntityHuman eh = null;
    WorldServer nmsworld = null;
    Location fishingSpot = null;
    EntityFishingHook fishHook = null;
    EntityItem fish = null;

    @Persist("catch chance")
    int catchPercent = 65;

    int reelCount = 100;
    int castCount = 0;

    @Override
    public void run() {
        reelCount++;
        castCount++;
        if (fish != null) {
            if (fish.getBukkitEntity().getLocation().distance(npc.getEntity().getLocation()) < 3) {
                try {
                    fish.getBukkitEntity().remove();
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

    @Override
    public void onSpawn() {
        eh = ((CraftPlayer) npc.getEntity()).getHandle();
        nmsworld = ((CraftWorld) npc.getEntity().getWorld()).getHandle();
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
        Location from = null;
        Location to = null;

        fishHook = new EntityFishingHook(nmsworld, eh);
        nmsworld.addEntity(fishHook);
        from = npc.getEntity().getLocation();
        from = from.add(0, .33, 0);
        to = fishingLocation;

        Vector test = to.clone().subtract(from).toVector();
        Double elev = test.getY();
        Double testAngle = launchAngle(from, to, v, elev, g);
        if (testAngle == null) {
            return;
        }
        Double hangtime = hangtime(testAngle, v, elev, g);
        Vector victor = to.clone().subtract(from).toVector();
        Double dist = Math.sqrt(Math.pow(victor.getX(), 2) + Math.pow(victor.getZ(), 2));
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

        Projectile theHook = (Projectile) fishHook.getBukkitEntity();
        theHook.setShooter((ProjectileSource) npc.getEntity());
        theHook.setVelocity(victor);

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
            fishHook.getBukkitEntity().remove();
        }
        catch (Exception e) {
        }

        if (catchPercent > chance && fishHook != null && catchType != CatchType.NONE) {
            try {
                fish.getBukkitEntity().remove();
            }
            catch (Exception e) {
            }
            fish = new EntityItem(nmsworld, fishHook.locX, fishHook.locY, fishHook.locZ, getFishingResult());
            double d5 = npc.getEntity().getLocation().getX() - fishHook.locX;
            double d6 = npc.getEntity().getLocation().getY() - fishHook.locY;
            double d7 = npc.getEntity().getLocation().getZ() - fishHook.locZ;
            double d8 = (double) MathHelper.sqrt(d5 * d5 + d6 * d6 + d7 * d7);
            double d9 = 0.1D;

            fish.motX = d5 * d9;
            fish.motY = d6 * d9 + (double) MathHelper.sqrt(d8) * 0.08D;
            fish.motZ = d7 * d9;
            nmsworld.addEntity(fish);
            DenizenAPI.getDenizenNPC(npc).action("catch fish", null);
        }

        PlayerAnimation.ARM_SWING.play((Player) npc.getEntity());
    }

    public ItemStack getFishingResult() {
        if (catchType == CatchType.DEFAULT) {
            float f = nmsworld.random.nextFloat();
            int i = EnchantmentManager.g(fishHook.owner);
            int j = EnchantmentManager.h(fishHook.owner);
            float f1 = 0.1F - (float) i * 0.025F - (float) j * 0.01F;
            float f2 = 0.05F + (float) i * 0.01F - (float) j * 0.01F;

            f1 = MathHelper.a(f1, 0.0F, 1.0F);
            f2 = MathHelper.a(f2, 0.0F, 1.0F);
            if (f < f1) {
                return catchRandomJunk();
            }
            else {
                f -= f1;
                if (f < f2) {
                    return catchRandomTreasure();
                }
                else {
                    return catchRandomFish();
                }
            }
        }
        else if (catchType == CatchType.JUNK) {
            return catchRandomJunk();
        }
        else if (catchType == CatchType.TREASURE) {
            return catchRandomTreasure();
        }
        else if (catchType == CatchType.FISH) {
            return catchRandomFish();
        }
        else {
            return null;
        }
    }

    private ItemStack catchRandomJunk() {
        fishHook.owner.a(StatisticList.A, 1);
        LootTableInfo.a playerFishEvent2 = new LootTableInfo.a((WorldServer)fishHook.getWorld());
        playerFishEvent2.a((float)EnchantmentManager.f(fishHook.owner) + fishHook.owner.dd());
        List<ItemStack> itemStacks = fishHook.getWorld().ak().a(LootTables.am).a(CoreUtilities.getRandom(), playerFishEvent2.a());
        return itemStacks.get(CoreUtilities.getRandom().nextInt(itemStacks.size()));
    }

    private ItemStack catchRandomTreasure() {
        fishHook.owner.a(StatisticList.B, 1);
        LootTableInfo.a playerFishEvent2 = new LootTableInfo.a((WorldServer)fishHook.getWorld());
        playerFishEvent2.a((float)EnchantmentManager.f(fishHook.owner) + fishHook.owner.dd());
        List<ItemStack> itemStacks = fishHook.getWorld().ak().a(LootTables.an).a(CoreUtilities.getRandom(), playerFishEvent2.a());
        return itemStacks.get(CoreUtilities.getRandom().nextInt(itemStacks.size()));
    }

    private ItemStack catchRandomFish() {
        //float f3 = f - f2;
        fishHook.owner.a(StatisticList.z, 1);
        LootTableInfo.a playerFishEvent2 = new LootTableInfo.a((WorldServer)fishHook.getWorld());
        playerFishEvent2.a((float)EnchantmentManager.f(fishHook.owner) + fishHook.owner.dd());
        List<ItemStack> itemStacks = fishHook.getWorld().ak().a(LootTables.ao).a(CoreUtilities.getRandom(), playerFishEvent2.a());
        return itemStacks.get(CoreUtilities.getRandom().nextInt(itemStacks.size()));
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
        Double dist = Math.sqrt(Math.pow(victor.getX(), 2) + Math.pow(victor.getZ(), 2));
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

    public void setCatchType(CatchType catchType) {
        this.catchType = catchType;
    }

    public void setCatchPercent(int catchPercent) {
        this.catchPercent = catchPercent;
    }
}
