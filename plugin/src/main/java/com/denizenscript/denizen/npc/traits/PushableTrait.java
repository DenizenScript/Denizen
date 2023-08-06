package com.denizenscript.denizen.npc.traits;

import com.denizenscript.denizen.Denizen;
import com.denizenscript.denizen.objects.NPCTag;
import com.denizenscript.denizen.objects.PlayerTag;
import com.denizenscript.denizencore.utilities.CoreUtilities;
import net.citizensnpcs.api.ai.event.NavigationCompleteEvent;
import net.citizensnpcs.api.event.NPCPushEvent;
import net.citizensnpcs.api.persistence.Persist;
import net.citizensnpcs.api.trait.Trait;
import net.citizensnpcs.util.NMS;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

// <--[language]
// @name Pushable Trait
// @group NPC Traits
// @description
// By default, NPCs created will allow players to 'noclip' them, or go right through. This is to
// avoid NPCs moving from their set location, but often times, this behavior may be undesired.
// The pushable trait allows NPCs to move when collided with, and optionally return to their
// original location after a specified amount of time.
//
// To enable the trait, use the '/npc pushable' command on any selected NPC. Once the trait is
// enabled, the '-r' option can be used to toggle returnable, and the '--delay #' option can be
// used to specify the number of seconds before the npc returns.
//
// Care should be taken when allowing NPCs to be pushable. Allowing NPCs to be pushed around
// complex structures can result in stuck NPCs. If the NPC is stuck, it may not return. Keeping
// a small delay, in situations like this, can be a good trade-off. Typically the lower the
// delay, the shorter distance a Player is able to push the NPC. The default delay is 2 seconds.
//
// The pushable trait also implements some actions that can be used in assignment scripts.
// This includes 'on push' and 'on push return'.

// -->

public class PushableTrait extends Trait implements Listener {

    // Saved to the C2 saves.yml
    @Persist("toggle")
    private boolean pushable = true;
    @Persist("returnable")
    private boolean returnable = false;
    @Persist("delay")
    private int delay = 2;

    // Used internally
    private boolean pushed = false;
    private Location returnLocation = null;
    private long pushedTimer = 0;

    public PushableTrait() {
        super("pushable");
    }

    /**
     * Gets the delay, as set by {@link #setDelay(int)}.
     *
     * @return delay, in seconds
     */
    public int getDelay() {
        return delay;
    }

    /**
     * Checks if this NPCs pushable setting is toggled.
     *
     * @return true if pushable
     */
    public boolean isPushable() {
        return pushable;
    }

    /**
     * Checks if this NPC is returnable when pushed.
     * Note: Does not take into account whether the NPC
     * is pushable, use {@link #isPushable()}
     *
     * @return true if returnable
     */
    public boolean isReturnable() {
        return returnable;
    }

    /**
     * Sets the delay, in seconds, in which the NPC will
     * return to its position after being pushed.
     * Note: Must be pushable.
     *
     * @param delay time in seconds to return after being pushed
     */
    public void setDelay(int delay) {
        this.delay = delay;
    }

    /**
     * Indicates that the NPC should be pushable. By default,
     * C2 NPCs are not pushable.
     *
     * @param pushable whether the NPC should be pushable
     */
    public void setPushable(boolean pushable) {
        this.pushable = pushable;
    }

    /**
     * Indicates that the NPC should return to its location
     * after being pushed. Takes into account a delay which
     * can be set with {@link #setDelay(int)} and
     * checked with {@link #getDelay()}}.
     *
     * @param returnable whether the NPC can return
     */
    public void setReturnable(boolean returnable) {
        this.returnable = returnable;
    }

    /**
     * Toggles the NPCs current pushable setting.
     *
     * @return {@link #isPushable()} after setting
     */
    public boolean toggle() {
        pushable = !pushable;
        if (!pushable) {
            returnable = false;
        }
        return pushable;
    }

    // <--[action]
    // @Actions
    // push
    //
    // @Triggers when the NPC is pushed by a player
    //
    // @Context
    // None
    //
    // -->

    /**
     * Fires an 'On Push:' action upon being pushed.
     */
    @EventHandler
    public void NPCPush(NPCPushEvent event) {
        if (event.getNPC() == npc && pushable) {
            event.setCancelled(false);
            // On Push action / Push Trigger
            if (CoreUtilities.monotonicMillis() > pushedTimer) {
                // Get pusher
                Player pusher = null;
                for (Entity le : event.getNPC().getEntity().getNearbyEntities(1, 1, 1)) {
                    if (le instanceof Player) {
                        pusher = (Player) le;
                    }
                }
                if (pusher != null) {
                    new NPCTag(npc).action("push", PlayerTag.mirrorBukkitPlayer(pusher));
                    pushedTimer = CoreUtilities.monotonicMillis() + ((long) delay * 1000L);
                }
            } // End push action
            if (!pushed && returnable) {
                pushed = true;
                returnLocation = npc.getStoredLocation().clone();
                Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(
                        Denizen.getInstance(), () -> {
                            if (npc.isSpawned()) {
                                navigateBack();
                            }
                        }, delay * 20L);
            }
        }
    }

    // <--[action]
    // @Actions
    // push return
    //
    // @Triggers when the NPC returns to its center after being pushed by a player.
    //
    // @Context
    // None
    //
    // -->

    /**
     * Fires a 'On Push Return:' action upon return after being pushed.
     */
    @EventHandler
    public void NPCCompleteDestination(NavigationCompleteEvent event) {
        if (event.getNPC() == npc && pushed) {
            Entity npcEntity = npc.getEntity();
            Location location = npcEntity.getLocation();
            location.setYaw(returnLocation.getYaw());
            location.setPitch(returnLocation.getPitch());
            NMS.setHeadYaw(npcEntity, returnLocation.getYaw());
            pushed = false;
            // Push Return action
            new NPCTag(npc).action("push return", null);
        }
    }

    protected void navigateBack() {
        if (npc.getNavigator().isNavigating()) {
            pushed = false;
        }
        else if (pushed) {
            pushed = false; // Avoids NPCCompleteDestination from triggering
            npc.getNavigator().setTarget(returnLocation);
            pushed = true;
        }
    }
}
