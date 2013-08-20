package net.aufdemrand.denizen.listeners.core;

import java.util.List;

import net.aufdemrand.denizen.objects.dCuboid;
import net.aufdemrand.denizen.objects.dNPC;
import net.aufdemrand.denizen.utilities.Utilities;
import net.aufdemrand.denizen.objects.dLocation;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

import net.aufdemrand.denizen.listeners.AbstractListener;
import net.aufdemrand.denizen.objects.aH;
import net.aufdemrand.denizen.utilities.debugging.dB;


/**
 * This is a listener that listens for a player to travel.  There are different
 * types of "traveling" this can entail:
 *
 * <ol>
 * <li>
 * Distance
 * <dd>Number of blocks to travel.</dd>
 * </li>
 * <li>
 * Target
 * <dd>A NPC to travel to.</dd>
 * </li>
 * <li>
 * Location
 * <dd>A location to travel to</dd>
 * </li>
 * </ol>
 *
 * @author Jeebiss, Jeremy Schroeder
 */
public class TravelListenerInstance extends AbstractListener implements Listener{

    enum TravelType { DISTANCE, TOLOCATION, TONPC, TOCUBOID }

    //
    // The type of Travel
    //
    private    TravelType type;

    //
    // End point criteria
    //
    private dNPC target;
    private dLocation end_point;
    private dCuboid end_cuboid;
    private int radius = 2;

    //
    // Counters
    //
    private Integer blocks_walked = 0;
    private Integer distance_required = null;

    /**
     * This method is called when an instance of the travel listener is created.
     * This class will then register with the event handler so we know when the
     * player moves so that a determination of whether or not the player has
     * reached the goal can be determined.
     */
    @Override
    public void constructed() {
        denizen.getServer().getPluginManager().registerEvents(this, denizen);
    }

    /**
     * This will be called when this travel listener is destroyed.  This allows
     * the class to unregister with the event handler so that no more events will
     * be received.
     */
    @Override
    public void deconstructed() {
        PlayerMoveEvent.getHandlerList().unregister(this);
    }

    @Override
    public void onBuild(List<aH.Argument> args) {
        for (aH.Argument arg : args) {

            if (type == null && arg.matchesEnum(TravelType.values()))
                type = TravelType.valueOf(arg.getValue().toUpperCase());

            else if (arg.matchesArgumentType(dCuboid.class))
                end_cuboid = arg.asType(dCuboid.class);

            else if (arg.matchesArgumentType(dLocation.class))
                end_point = arg.asType(dLocation.class);

            else if (arg.matchesArgumentType(dNPC.class))
                target = arg.asType(dNPC.class);

            else if (arg.matchesPrefix("d, distance")
                    && arg.matchesPrimitive(aH.PrimitiveType.Integer))
                distance_required = aH.getIntegerFrom(arg.getValue());

            else if (arg.matchesPrefix("r, radius")
                    && arg.matchesPrimitive(aH.PrimitiveType.Integer))
                radius = aH.getIntegerFrom(arg.getValue());

        }

        //
        // Check for mandatory arguments.
        //
        if (type == null) {
            dB.echoError("Missing TYPE argument! Valid: DISTANCE, TOLOCATION, TONPC, TOCUBOID");
            cancel();
        }
    }

    @Override
    public void onCancel() {
        // nothing to do here

    }

    @Override
    public void onFinish() {
        // nothing to do here

    }

    @Override
    public void onLoad() {
        type = TravelType.valueOf((String) get("Type"));
        distance_required = (Integer) get("Distance");
        blocks_walked = (Integer) get("Blocks Walked");
        end_cuboid = dCuboid.valueOf((String) get("End Cuboid"));
        end_point = dLocation.valueOf((String) get("End Location"));
    }

    @Override
    public void onSave() {
        store("Type", type.name());
        store("Distance", distance_required);
        store("Radius", radius);
        store("Blocks Walked", blocks_walked);
        if (end_point != null) store("End Location", end_point.identify());
        if (end_cuboid != null) store("End Cuboid", end_cuboid.identify());
    }

    @Override
    public String report() {
        if (type == TravelType.DISTANCE){
            return player.getName() + "has traveled " + blocks_walked + " blocks out of " + distance_required;
        } else if (type == TravelType.TOLOCATION) {
            return player.getName() + " is traveling to " + end_point;
        } else if (type == TravelType.TONPC) {
            return player.getName() + " is traveling to NPC " + target.getId();
        }
        return "Failed to create detailed report";
    }

    /**
     * This method will be called every time a player moves in the game.  It's
     * used to determine if a player has satisfied a certain travel goal.
     *
     * @param event    The player movement event.
     */
    @EventHandler
    public void walking(PlayerMoveEvent event) {
        // Only continue if the player moving owns this Listener
        if (!(event.getPlayer() == player.getPlayerEntity())) return;
        // Don't look if the player hasn't moved a block yet...
        if (event.getTo().getBlock().equals(event.getFrom().getBlock())) return;

        //
        // DISTANCE type Location Listener
        //
        if (type == TravelType.DISTANCE){
                blocks_walked++;
                check();
        }

        //
        // TOLOCATION type Location Listener
        //
        else if (type == TravelType.TOLOCATION) {
            if (!player.getPlayerEntity().getLocation().getWorld().equals(end_point.getWorld())) return;
            //if (player.getLocation().distance(endPoint) <= radius) {
            if (Utilities.checkLocation(player.getPlayerEntity(), end_point, radius)) {
                dB.echoDebug("...player reached location");
                finish();
            }
        }

        //
        // TONPC type Location Listener
        //
        else if (type == TravelType.TONPC) {
            if (Utilities.checkLocation(player.getPlayerEntity(),
                    target.getLocation(), radius)) {
                dB.echoDebug("...player reached NPC");
                finish();
            }
        }

    }

    private void check() {

        if (blocks_walked >= distance_required)
            finish();
    }
}
