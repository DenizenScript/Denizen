package net.aufdemrand.denizen.npc.traits;

import net.aufdemrand.denizen.objects.dPlayer;
import net.aufdemrand.denizen.utilities.DenizenAPI;
import net.citizensnpcs.api.ai.event.NavigationCompleteEvent;
import net.citizensnpcs.api.event.NPCPushEvent;
import net.citizensnpcs.api.persistence.Persist;
import net.citizensnpcs.api.trait.Trait;
import net.minecraft.server.v1_5_R3.EntityLiving;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_5_R3.entity.CraftLivingEntity;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

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
     * @param pushable
     */
    public void setPushable(boolean pushable) {
        this.pushable = pushable;
    }

    /**
     * Indicates that the NPC should return to its location
     * after being pushed. Takes into account a delay which
     * can be set with {@link #setReturnable(boolean)} and
     * checked with {@link #isReturnable()}.
     *
     * @param returnable
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
        if (!pushable) returnable = false;
        return pushable;
    }

    /**
     * Fires an 'On Push:' action upon being pushed.
     *
     * @param event
     */
    @EventHandler
    public void NPCPush (NPCPushEvent event) {
        if (event.getNPC() == npc && pushable) {
            event.setCancelled(false);
            // On Push action / Push Trigger
            if (System.currentTimeMillis() > pushedTimer) {
                // Get pusher
                Player pusher = null;
                for (Entity le : ((CraftLivingEntity)event.getNPC().getBukkitEntity()).getNearbyEntities(1, 1, 1))
                    if (le instanceof Player) pusher = (Player) le;
                if (pusher != null) {
                    DenizenAPI.getDenizenNPC(npc).action("push", dPlayer.mirrorBukkitPlayer(pusher));
                    pushedTimer = System.currentTimeMillis() + (delay * 1000);
                }
            } // End push action
            if (!pushed && returnable) {
                pushed = true;
                returnLocation = npc.getBukkitEntity().getLocation().clone();
                Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(
                        DenizenAPI.getCurrentInstance(), new Runnable() {
                            @Override public void run() { navigateBack(); } }, delay * 20);
            }
        }
    }

    /**
     * Fires a 'On Push Return:' action upon return after being pushed.
     *
     * @param event
     */
    @EventHandler 
    public void NPCCompleteDestination (NavigationCompleteEvent event) {
        if (event.getNPC() == npc && pushed) {
            EntityLiving handle = ((CraftLivingEntity) npc.getBukkitEntity()).getHandle();
            handle.yaw = returnLocation.getYaw();
            handle.pitch = returnLocation.getPitch();
            // !--- START NMS OBFUSCATED
            handle.aA = handle.yaw; // The head's yaw
            // !--- END NMS OBFUSCATED
            pushed = false;
            // Push Return action
            DenizenAPI.getDenizenNPC(npc).action("push return", null);
        }
    }

    protected void navigateBack() {
        if (npc.getNavigator().isNavigating()) {
            pushed = false;
        } else if (pushed) {
            pushed = false; // Avoids NPCCompleteDestination from triggering
            npc.getNavigator().setTarget(returnLocation);
            pushed = true;
        }
    }

}
