package net.aufdemrand.denizen.npc.traits;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_4_6.entity.CraftLivingEntity;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import net.aufdemrand.denizen.Denizen;

import net.citizensnpcs.api.ai.event.NavigationCompleteEvent;
import net.citizensnpcs.api.event.NPCPushEvent;
import net.citizensnpcs.api.exception.NPCLoadException;
import net.citizensnpcs.api.trait.Trait;
import net.citizensnpcs.api.util.DataKey;
import net.minecraft.server.v1_4_6.EntityLiving;

public class PushableTrait extends Trait implements Listener {

    private boolean pushable = true;
    private boolean returnable = false;

    private boolean pushed = false;
    private Location returnLocation = null;
    private Denizen denizen;
    private long pushedTimer = 0;
    private int delay = 2;

    public PushableTrait() {
        super("pushable");
        denizen = (Denizen) Bukkit.getServer().getPluginManager().getPlugin("Denizen");
    }

    // Loading/Saving

    @Override 
    public void load(DataKey key) throws NPCLoadException {
        pushable = key.getBoolean("toggled", true);
        returnable = key.getBoolean("returnable", false);
        delay = key.getInt("delay", 2);
    }

    @Override 
    public void save(DataKey key) {
        key.setBoolean("toggled", pushable);
        key.setBoolean("returnable", returnable);
        key.setInt("delay", delay);
    }

    // Setting/Adjusting

    public int getDelay() {
        return delay;
    }

    public boolean isPushable() {
        return pushable;
    }

    public boolean isReturnable() {
        return returnable;
    }

    public void setDelay(int delay) {
        this.delay = delay;
    }

    public void setPushable(boolean pushable) {
        this.pushable = pushable;
    }

    public void setReturnable(boolean returnable) {
        this.returnable = returnable;
    }

    public boolean toggle() {
        pushable = !pushable;
        if (!pushable) returnable = false;
        return pushable;
    }

    // Mechanics

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
                    denizen.getNPCRegistry().getDenizen(npc).action("push", pusher);
                    pushedTimer = System.currentTimeMillis() + (delay * 1000);
                }
            } // End push action
            if (!pushed && returnable) {
                pushed = true;
                returnLocation = npc.getBukkitEntity().getLocation().clone();
                denizen.getServer().getScheduler().scheduleSyncDelayedTask(
                        denizen, new Runnable() { 
                            @Override public void run() { navigateBack(); } }, delay * 20);
            }
        }
    }

    @EventHandler 
    public void NPCCompleteDestination (NavigationCompleteEvent event) {
        if (event.getNPC() == npc && pushed) {
            EntityLiving handle = ((CraftLivingEntity) npc.getBukkitEntity()).getHandle();
            handle.yaw = returnLocation.getYaw();
            handle.pitch = returnLocation.getPitch();
            handle.az = handle.yaw;
            pushed = false;
            // Push Return action
            denizen.getNPCRegistry().getDenizen(npc).action("push return", null);
        }
    }

    protected void navigateBack() {
        if (npc.getNavigator().isNavigating()) {
            pushed = false;
            return;
        } else if (pushed) {
            pushed = false; // Avoids NPCCompleteDestination from triggering
            npc.getNavigator().setTarget(returnLocation);
            pushed = true;
        }
    }

}
