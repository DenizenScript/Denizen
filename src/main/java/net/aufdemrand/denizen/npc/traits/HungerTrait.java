package net.aufdemrand.denizen.npc.traits;

import net.aufdemrand.denizen.events.bukkit.ExhaustedNPCEvent;
import net.aufdemrand.denizen.utilities.DenizenAPI;
import net.citizensnpcs.api.ai.event.NavigationBeginEvent;
import net.citizensnpcs.api.ai.event.NavigationCancelEvent;
import net.citizensnpcs.api.ai.event.NavigationCompleteEvent;
import net.citizensnpcs.api.persistence.Persist;
import net.citizensnpcs.api.trait.Trait;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class HungerTrait extends Trait implements Listener {

    // Saved to the C2 saves.yml
    @Persist("maxhunger")
    private double maxhunger = 20.0;
    @Persist("currenthunger")
    private double currenthunger = 0.0;
    @Persist("multiplier")
    private int multiplier = 1;
    @Persist("allowexhaustion")
    private boolean allowexhaustion = false;

    // Used internally
    private boolean listening = false;
    private Location location = null;
    private int count = 0;

    /**
     * Watches the NPCs movement to calculate hunger loss. Loses 0.01 hunger points
     * per block moved, unless a modifier is used.
     *
     */
    @Override
    public void run() {
        if (!listening) return;
        // We'll only actually calculate hunger-loss once per second
        count++;
        if (count >= 20) {
            // Reset counter
            count = 0;
            double td = getDistance(npc.getBukkitEntity().getLocation());
            if (td > 0) {
                location = npc.getBukkitEntity().getLocation().clone();
                currenthunger = currenthunger - (td * 0.01 * multiplier);
            }
        }
    }

    // <--[action]
    // @Actions
    // exhausted
    //
    // @Triggers when the NPC is exhausted (Requires the Hunger trait)
    //
    // @Context
    // None
    //
    // -->
    /**
     * Listens for the NPC to move so hunger-loss can be calculated.
     * Cuts down on processing since loss is only calculated when moving.
     * Also checks for exhaustion, if enabled. If a NPC is exhausted, that is,
     * if currenthunger >= maxhunger, the NPC cannot move and a
     * NPCExhaustedEvent and 'On Exhausted:' action will fire.
     *
     */
    @EventHandler
    public void onMove(NavigationBeginEvent event) {

        // TODO: Check if NPC == this NPC?
        if (allowexhaustion) {
            if (isStarving()) {
                // Create NPCExhaustedEvent, give chance for outside plugins to cancel.
                ExhaustedNPCEvent e = new ExhaustedNPCEvent(npc);
                Bukkit.getServer().getPluginManager().callEvent(e);

                // If still exhausted, cancel navigation and fire 'On Exhausted:' action
                if (!e.isCancelled()) {
                    npc.getNavigator().cancelNavigation();
                    DenizenAPI.getDenizenNPC(npc).action("exhausted", null);

                    // No need to progress any further.
                    return;
                }
            }
        }

        location = npc.getBukkitEntity().getLocation();
        listening = true;
    }

    /**
     * Stops the listening process for hunger-loss since the NPC is no longer moving.
     *
     */
    @EventHandler
    public void onCancel(NavigationCancelEvent event) {
        listening = false;
    }

    /**
     * Stops the listening process for hunger-loss since the NPC is no longer moving.
     *
     */
    @EventHandler
    public void onCancel(NavigationCompleteEvent event) {
        listening = false;
    }

    public HungerTrait() {
        super("hunger");
    }

    /**
     * Gets the NPCs current hunger level. 0.00 = no hunger, NPC is satiated.
     *
     * @return current hunger level
     *
     */
    public double getHunger() {
        return currenthunger;
    }

    /**
     * Gets the upper bounds of the hunger level. Default is 20.0. Can be set higher
     * or lower to require more or less 'feeding'.
     *
     * @return max hunger level
     *
     */
    public double getMaxHunger() {
        return maxhunger;
    }

    /**
     * Gets the percentage of hunger based on currenthunger and maxhunger. 100 = npc
     * is starving. 0 = npc is satiated.
     *
     * @return hunger percentage
     */
    public int getHungerPercentage() {
        return (int) ((int) currenthunger / maxhunger);
    }

    /**
     * Gets the multiplier used to calculate hunger loss. Default is 1. Setting a higher value
     * reduces hunger quicker.
     *
     * @return current hunger multiplier
     *
     */
    public int getHungerMultiplier() {
        return multiplier;
    }

    /**
     * Sets the multiplier used to calculate hunger loss. Default is 1. Setting a higher value
     * reduces hunger quicker. Lower value, in turn, makes hunger loss slower.
     *
     * @param multiplier new multiplier
     *
     */
    public void setHungerMultiplier(int multiplier) {
        this.multiplier = multiplier;
    }

    /**
     * Sets the current hunger level.
     *
     * @param hunger new hunger level
     *
     */
    public void setHunger(double hunger) {
        if (currenthunger > maxhunger) currenthunger = maxhunger;
        else currenthunger = hunger;
    }

    /**
     * "Feeds" the NPC. The value used will reduce the total currenthunger value.
     *
     * @param hunger amount of hunger-points to reduce currenthunger by
     *
     */
    public void feed(double hunger) {
        currenthunger = currenthunger - hunger;
        if (currenthunger < 0) currenthunger = 0;
    }

    /**
     * Sets the max hunger value. Once hunger reaches this level the NPC is starving and
     * may not be able to move. This method does not change the NPC's currenthunger.
     *
     * @param hunger new max hunger value
     */
    public void setMaxhunger(double hunger) {
        maxhunger = hunger;
    }

    /**
     * Checks to see if the NPC is starving. If currenthunger >= maxhunger, the NPC is starving.
     *
     * @return true if NPC is starving
     *
     */
    public boolean isStarving() {
        return currenthunger >= maxhunger;
    }

    /**
     * Checks to see if the NPC is hungry. If currenthunger is 10% or more of maxhunger,
     * the NPC is hungry. A NPC that is starving is also hungry.
     *
     * @return true if the NPC is hungry
     *
     */
    public boolean isHungry() {
        return currenthunger > (maxhunger / 10);
    }

    // Used internally
    private double getDistance(Location location) {
        if (!npc.getBukkitEntity().getWorld().equals(location.getWorld())) {
            // World change, update location
            this.location = npc.getBukkitEntity().getLocation();
            return 0;
        }
        return location.distance(this.location);
    }
}
