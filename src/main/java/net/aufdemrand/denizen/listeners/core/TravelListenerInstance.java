package net.aufdemrand.denizen.listeners.core;

import java.util.List;

import net.aufdemrand.denizen.utilities.Utilities;
import net.aufdemrand.denizen.objects.dLocation;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

import net.aufdemrand.denizen.listeners.AbstractListener;
import net.aufdemrand.denizen.listeners.core.TravelListenerType.TravelType;
import net.aufdemrand.denizen.objects.aH;
import net.aufdemrand.denizen.objects.aH.ArgumentType;
import net.aufdemrand.denizen.utilities.debugging.dB;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;

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
 * @author Jeebiss
 */
public class TravelListenerInstance extends AbstractListener implements Listener{
	
	public	static	final	String	DISTANCE_ARG = "DISTANCE, D";
	public	static	final	String	TYPE_ARG = "TYPE";
	public	static	final	String	TARGET_ARG = "TARGET";
	public	static	final	String	RADIUS_ARG = "RADIUS, R";
	
	private	NPC target;
	private dLocation endPoint;
	private Integer blocksWalked = 0;
	private Integer distance = null;
	private Integer radius = 2;
	private	TravelType type;
	
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
	public void onBuild(List<String> args) {
		for (String arg : args) {
			if (aH.matchesLocation(arg)){
				endPoint = aH.getLocationFrom(arg);
				dB.echoDebug("...ending location set");
			} else if (aH.matchesValueArg(DISTANCE_ARG, arg, ArgumentType.Integer)) {
				distance = aH.getIntegerFrom(arg);
				dB.echoDebug("...distance set to: " + distance);
			} else if (aH.matchesValueArg(RADIUS_ARG, arg, ArgumentType.Integer)) {
				radius = aH.getIntegerFrom(arg);
				dB.echoDebug("...radius set to: " + radius);
			} else if (aH.matchesValueArg(TYPE_ARG, arg, ArgumentType.Custom)) {
				try {
					type = TravelType.valueOf(aH.getStringFrom(arg).toUpperCase());
					dB.echoDebug("...set TYPE to: " + aH.getStringFrom(arg));
				} catch (Exception e) {e.printStackTrace();}
			} else if (aH.matchesValueArg(TARGET_ARG, arg, ArgumentType.LivingEntity)) {
				if ((CitizensAPI.getNPCRegistry().getNPC(aH.getLivingEntityFrom(arg)) != null &&
						CitizensAPI.getNPCRegistry().isNPC(aH.getLivingEntityFrom(arg)))){
					target = CitizensAPI.getNPCRegistry().getNPC(aH.getLivingEntityFrom(arg));
					dB.echoDebug("...NPC set to: " + target.getId());
				}
			}
		}
		
		//
		// Check for mandatory arguments.
		//
		if (type == null) {
			dB.echoError("Missing TYPE argument! Valid: DISTANCE, TOLOCATION, TONPC");
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
		distance = (Integer) get("Distance");
		blocksWalked = (Integer) get("Blocks Walked");
		endPoint = dLocation.valueOf((String) get("End Location"));
	}

	@Override
	public void onSave() {
		store("Type", type.name());
		store("Distance", distance);
		store("Radius", radius);
		store("Blocks Walked", blocksWalked);
		store("End Location", endPoint.identify());
	}

	@Override
	public String report() {
		if (type == TravelType.DISTANCE){
			return player.getName() + "has traveled " + blocksWalked + " blocks out of " + distance;
		} else if (type == TravelType.TOLOCATION) {
			return player.getName() + " is traveling to " + endPoint;
		} else if (type == TravelType.TONPC) {
			return player.getName() + " is traveling to NPC " + target.getId();
		}
		return "Failed to create detailed report";
	}

	/**
	 * This method will be called every time a player moves in the game.  It's
	 * used to determine if a player has satisfied a certain travel goal.
	 * 
	 * @param event	The player movement event.
	 */
  @EventHandler
	public void walking(PlayerMoveEvent event) {
		if (!(event.getPlayer() == player)) return;
		
		if (type == TravelType.DISTANCE){
			if (!event.getTo().getBlock().equals(event.getFrom().getBlock())) {
				blocksWalked++;
				dB.echoDebug("..player moved a block");
				check();
			}
		} else if (type == TravelType.TOLOCATION) {
			if (!player.getPlayerEntity().getLocation().getWorld().equals(endPoint.getWorld())) return;
			//if (player.getLocation().distance(endPoint) <= radius) {
			if (Utilities.checkLocation(player.getPlayerEntity(), endPoint, radius)) {
				dB.echoDebug("...player reached location");
				finish();
			}
		} else if (type == TravelType.TONPC) {
			//if (player.getLocation().distance(target.getBukkitEntity().getLocation()) <= radius) {
			if (Utilities.checkLocation(player.getPlayerEntity(), target.getBukkitEntity().getLocation(), radius)) {
				dB.echoDebug("...player reached NPC");
				finish();
			}
		}
		
	}

	private void check() {
		if (blocksWalked >= distance) {
			finish();
		}
	}
}
