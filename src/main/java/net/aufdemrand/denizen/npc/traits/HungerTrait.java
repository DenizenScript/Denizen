package net.aufdemrand.denizen.npc.traits;

import net.citizensnpcs.api.ai.event.NavigationBeginEvent;
import net.citizensnpcs.api.ai.event.NavigationCancelEvent;
import net.citizensnpcs.api.ai.event.NavigationCompleteEvent;
import net.citizensnpcs.api.exception.NPCLoadException;
import net.citizensnpcs.api.persistence.Persist;
import net.citizensnpcs.api.trait.Trait;
import net.citizensnpcs.api.util.DataKey;
import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class HungerTrait extends Trait implements Listener {

    public HungerTrait() {
        super("hunger");
    }

    @Persist("") double maxhunger = 20.0;
    @Persist("") double currenthunger = 0.0;
    @Persist("") int multiplier = 1;

    @Override public void load(DataKey key) throws NPCLoadException {
//        maxhunger = key.getDouble("maxhunger", 20);
//        currenthunger = key.getDouble("currenthunger", 0);
//        multiplier = key.getInt("multiplier", 1);
    }

    @Override public void save(DataKey key) {
//        key.setDouble("maxhealth", maxhunger);
//        key.setDouble("currenthealth", currenthunger);
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
                currenthunger = currenthunger - (td * 0.01 * multiplier);
            }
        }
    }

    @EventHandler
    public void onMove(NavigationBeginEvent event) {
        location = npc.getBukkitEntity().getLocation();
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
        return currenthunger;
    }
    
    public double getMaxhunger() {
        return maxhunger;
    }
    
    public int getHungerPercentage() {
        return (int) ((int) currenthunger / maxhunger);
    }
    
    public int getHungerMultiplier() {
        return multiplier;
    }
    
    public void setHungerMultiplier(int multiplier) {
        this.multiplier = multiplier;
    }
    
    public void setHunger(double hunger) {
        currenthunger = hunger;
    }
    
    public void feed(double hunger) {
        currenthunger = currenthunger - hunger;
        if (currenthunger < 0) currenthunger = 0;
    }
    
    public void setMaxhunger(double hunger) {
        maxhunger = hunger;
    }
    
    public boolean isStarving() {
        if (currenthunger >= maxhunger) return true;
        else return false;
    }
    
    public boolean isHungry() {
        if (currenthunger > (maxhunger /10)) return true;
        else return false;
    }

}
