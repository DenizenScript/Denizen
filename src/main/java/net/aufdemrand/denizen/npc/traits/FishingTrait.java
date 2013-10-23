package net.aufdemrand.denizen.npc.traits;

import java.util.ArrayList;
import net.aufdemrand.denizen.utilities.DenizenAPI;
import net.aufdemrand.denizen.utilities.Utilities;
import net.aufdemrand.denizen.utilities.debugging.dB;
import net.citizensnpcs.api.persistence.Persist;
import net.citizensnpcs.api.trait.Trait;
import net.citizensnpcs.util.PlayerAnimation;
import net.minecraft.server.v1_6_R3.EntityFishingHook;
import net.minecraft.server.v1_6_R3.EntityHuman;
import net.minecraft.server.v1_6_R3.EntityItem;
import net.minecraft.server.v1_6_R3.Item;
import net.minecraft.server.v1_6_R3.ItemStack;
import net.minecraft.server.v1_6_R3.MathHelper;
import net.minecraft.server.v1_6_R3.WorldServer;

import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_6_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_6_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.util.Vector;

public class FishingTrait extends Trait {

    @Persist("fishing")
    private boolean fishing = false;
    @Persist("catch fish")
    private boolean catchFish = false;

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

        if(fish != null)
            if(fish.getBukkitEntity().getLocation().distance(npc.getBukkitEntity().getLocation())<3) {
                try{
                    fish.getBukkitEntity().remove();
                } catch(Exception e) { }
            }

        if(reelCount == 400) {
            reel();
            reelCount = 0;
            castCount = 325;
        }

        if(castCount == 400) {
            cast();
            castCount = 0;
        }
    }

    @Override
    public void onSpawn() {
        eh = ((CraftPlayer) npc.getBukkitEntity()).getHandle();
        nmsworld = ((CraftWorld) npc.getBukkitEntity().getWorld()).getHandle();
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
     *
     * TODO Reimplement variance, so each cast doesn't land in the exact same spot.
     *
     * @param location
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

        fishingLocation = null;
        fishing = false;
    }

    /**
     * Makes the NPC fish in the nearest water
     *
     * TODO Needs logic for handling that.
     */
    public void startFishing() {
        fishing = true;
        fishingLocation = npc.getBukkitEntity().getLocation();
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

        if(fishingLocation == null) {
            dB.echoError("Fishing location not found!");
            return;
        }

        double v = 34;
        double g = 20;
        Location from = null;
        Location to = null;

        fishHook = new EntityFishingHook(nmsworld, eh);
        nmsworld.addEntity(fishHook);
        from = npc.getBukkitEntity().getLocation();
        from = from.add(0,.33,0);
        to = fishingLocation;

        Vector test = to.clone().subtract(from).toVector();
        Double elev = test.getY();
        Double testAngle = launchAngle(from, to, v, elev, g);
        if (testAngle == null) return;
        Double hangtime = hangtime(testAngle, v, elev, g);
        Vector victor = to.clone().subtract(from).toVector();
        Double dist = Math.sqrt(Math.pow(victor.getX(), 2) + Math.pow(victor.getZ(), 2));
        elev = victor.getY();

        if (dist == 0)
            return;

        Double launchAngle = launchAngle(from, to, v, elev, g);
        if (launchAngle == null)
            return;
        victor.setY(Math.tan(launchAngle) * dist);
        victor = normalizeVector(victor);
        v = v + (.5 * Math.pow(hangtime, 2));
        //Random rand = new Random(1234);
        v = v+ (Utilities.getRandom().nextDouble() - .8)/2;
        victor = victor.multiply(v / 20.0);

        Projectile theHook = (Projectile) fishHook.getBukkitEntity();
        theHook.setShooter(npc.getBukkitEntity());
        theHook.setVelocity(victor);

        PlayerAnimation.ARM_SWING.play((Player) npc.getBukkitEntity());
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

        int chance = (int)(Math.random()*100);

        try{
            fishHook.getBukkitEntity().remove();
        } catch(Exception e){ }

        if (chance>catchPercent && fishHook != null && catchFish) {
            try{
                fish.getBukkitEntity().remove();
            } catch(Exception e) { }
            fish = new EntityItem(nmsworld, fishHook.locX, fishHook.locY, fishHook.locZ, new ItemStack(Item.RAW_FISH));
            double d5 = npc.getBukkitEntity().getLocation().getX() - fishHook.locX;
            double d6 = npc.getBukkitEntity().getLocation().getY() - fishHook.locY;
            double d7 = npc.getBukkitEntity().getLocation().getZ() - fishHook.locZ;
            double d8 = (double) MathHelper.sqrt(d5 * d5 + d6 * d6 + d7 * d7);
            double d9 = 0.1D;

            fish.motX = d5 * d9;
            fish.motY = d6 * d9 + (double) MathHelper.sqrt(d8) * 0.08D;
            fish.motZ = d7 * d9;
            nmsworld.addEntity(fish);
            DenizenAPI.getDenizenNPC(npc).action("catch fish", null);
        }

        PlayerAnimation.ARM_SWING.play((Player) npc.getBukkitEntity());
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

    public static Double launchAngle(Location from, Location to, double v, double elev, double g){
        Vector victor = from.clone().subtract(to).toVector();
        Double dist =  Math.sqrt(Math.pow(victor.getX(), 2) + Math.pow(victor.getZ(), 2));
        double v2 = Math.pow(v,2);
        double v4 = Math.pow(v,4);
        double derp =  g*(g*Math.pow(dist,2)+2*elev*v2);

        if(v4 < derp)
            return null;
        else
            return Math.atan( (v2-   Math.sqrt(v4 - derp))/(g*dist));
    }

    public static double hangtime(double launchAngle, double v, double elev, double g){
        double a = v * Math.sin(launchAngle);
        double b = -2*g*elev;

        if(Math.pow(a, 2) + b < 0)
            return 0;

        return (a + Math.sqrt(Math.pow(a, 2) + b))  /  g;
    }

    public static Vector normalizeVector(Vector victor){
        double  mag = Math.sqrt(Math.pow(victor.getX(), 2) + Math.pow(victor.getY(), 2)  + Math.pow(victor.getZ(), 2));
        if (mag !=0)
            return victor.multiply(1/mag);
        return victor.multiply(0);
    }

    public void setCatchFish(Boolean catchFish) {
        this.catchFish = catchFish;
    }

    public void setCatchPercent(int catchPercent) {
        this.catchPercent = catchPercent;
    }

}
