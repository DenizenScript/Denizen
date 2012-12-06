package net.aufdemrand.denizen.npc.traits;

import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import net.citizensnpcs.api.ai.event.NavigationBeginEvent;
import net.citizensnpcs.api.ai.event.NavigationCancelEvent;
import net.citizensnpcs.api.ai.event.NavigationCompleteEvent;
import net.citizensnpcs.api.exception.NPCLoadException;
import net.citizensnpcs.api.trait.Trait;
import net.citizensnpcs.api.util.DataKey;

public class HungerTrait extends Trait implements Listener {

    public HungerTrait() {
        super("hunger");
    }

    double maxHunger = 20.0;
    double currentHunger = 0.0;
    int multiplier = 1;

    @Override public void load(DataKey key) throws NPCLoadException {
        maxHunger = key.getDouble("maxhunger", 20);
        currentHunger = key.getDouble("currenthunger", 0);
        multiplier = key.getInt("multiplier", 1);
    }

    @Override public void save(DataKey key) {
        key.setDouble("maxhealth", maxHunger);
        key.setDouble("currenthealth", currentHunger);
    }

    boolean listening = false;
    Location location = null;
    private int count = 0;

    @Override public void run() {
        if (!listening) return;
        // We'll only actually calculate Hunger-loss once per second
        count++;
        if (count >= 20) {
            // Reset counter
            count = 0;
            double td = getDistance(npc.getBukkitEntity().getLocation());
            if (td > 0) {
                location = npc.getBukkitEntity().getLocation().clone();
                currentHunger = currentHunger - (td * 0.01 * multiplier);
            }
        }
    }

    @EventHandler
    public void onMove(NavigationBeginEvent event) {
        location = npc.getBukkitEntity().getLocation().clone();
        listening = true;
    }

    @EventHandler
    public void onCancel(NavigationCancelEvent event) {
        listening = false;
    }

    @EventHandler
    public void onCancel(NavigationCompleteEvent event) {
        listening = false;
    }

    public double getDistance(Location location) {
        if (!npc.getBukkitEntity().getWorld().equals(location.getWorld())) {
            // World change, update location
            this.location = npc.getBukkitEntity().getLocation();
            return 0;
        }
        return location.distance(this.location);
    }
    
    public double getHunger() {
        return currentHunger;
    }
    
    public double getMaxHunger() {
        return maxHunger;
    }
    
    public void setHunger(double hunger) {
        currentHunger = hunger;
    }
    
    public void setMaxHunger(double hunger) {
        maxHunger = hunger;
    }

}
