package net.aufdemrand.denizen.npc.traits;

import java.lang.reflect.InvocationTargetException;

import net.aufdemrand.denizen.utilities.DenizenAPI;
import net.aufdemrand.denizen.utilities.Utilities;
import net.aufdemrand.denizen.utilities.debugging.dB;
import net.citizensnpcs.api.ai.event.NavigationBeginEvent;
import net.citizensnpcs.api.persistence.Persist;
import net.citizensnpcs.api.trait.Trait;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;

import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.WrappedDataWatcher;

public class SittingTrait extends Trait implements Listener  {
	
	@Persist("sitting")
    private boolean sitting = false;
	
	@Persist("chair location")
	private Location location = null;
	
	/**
	 * Checks if the NPC still exists, if it is currently 
	 * sitting, and if it has moved. Resends the sit packet
	 * if the NPC is still sitting at his location. If it
	 * has moved more then 1 block from the chair, it sends
	 * the stand packet.
	 */
	@Override
	public void run() {
		
		if (npc == null) {
			sitting = false;
			return;
		}
		
		if (!sitting) return;
		
		if (!Utilities.checkLocation(npc.getBukkitEntity(), location, 1)) {
           sendStandPacket();
           sitting = false;
        } else {
           sendSitPacket();
        }
	}
	
	/**
	 * Sends the sit packet to all the currently
	 * online players.
	 */
	public void sendSitPacket() {
		/*
		 * Send the sit packet to all online players.
		 */
		try {
            PacketContainer packet = ProtocolLibrary.getProtocolManager().createPacket(40);
            packet.getSpecificModifier(int.class).write(0, npc.getBukkitEntity().getEntityId());
            WrappedDataWatcher watcher = new WrappedDataWatcher();
            watcher.setObject(0, (byte) 4);
            packet.getWatchableCollectionModifier().write(0, watcher.getWatchableObjects());

            for (Player player : DenizenAPI.getCurrentInstance().getServer().getOnlinePlayers()) {
                if (npc.getBukkitEntity().getWorld().equals(player.getWorld())) {
                    try {
                        ProtocolLibrary.getProtocolManager().sendServerPacket(player, packet);
                    } catch (InvocationTargetException e) {
                        dB.echoError("...error sending packet to player: " + player.getName());
                    }
                }
            }

        } catch (Error e) {
            dB.echoError("ProtocolLib required for SIT command!!");
        }
	}
	
	/**
	 * Sends the stand packet to all the currently
	 * online players.
	 */
	public void sendStandPacket() {
		/*
		 * Send the stand packet to all online players.
		 */
		try {
            PacketContainer packet = ProtocolLibrary.getProtocolManager().createPacket(40);
            packet.getSpecificModifier(int.class).write(0, npc.getBukkitEntity().getEntityId());
            WrappedDataWatcher watcher = new WrappedDataWatcher();
            watcher.setObject(0, (byte) 0);
            packet.getWatchableCollectionModifier().write(0, watcher.getWatchableObjects());

            for (Player player : DenizenAPI.getCurrentInstance().getServer().getOnlinePlayers()) {
                if (npc.getBukkitEntity().getWorld().equals(player.getWorld())) {
                    try {
                        ProtocolLibrary.getProtocolManager().sendServerPacket(player, packet);
                    } catch (InvocationTargetException e) {
                        dB.echoError("...error sending packet to player: " + player.getName());
                    }
                }
            }
        } catch (Error e) {
            dB.echoError("ProtocolLib required for SIT command!!");
        }
	}
	/**
	 * Makes the NPC sit
	 */
	public void sit() {
		
		if (sitting = true) {
			return;
		}
		
		sendSitPacket();
		
		sitting = true;
		location = npc.getBukkitEntity().getLocation();
		return;
		
	}
	
	/**
	 * Makes the NPC sit a the specified location
	 * 
	 * @param location
	 */
	public void sit(Location location) {
		
		if (sitting = true) {
			return;
		}
		
		/*
		 * Teleport NPC to the location before
		 * sending the sit packet to the clients.
		 */
		npc.getBukkitEntity().teleport(location);
		
		sendSitPacket();

		sitting = true;
		this.location = location;
	}
	
	/**
	 * Makes the NPC stand
	 */
	public void stand() {
		
		if (sitting = false) {
			return;
		}
		
		sendStandPacket();
		
		location = null;
		sitting = false;
	}
	
	/**
	 * Checks if the NPC is currently sitting
	 * 
	 * @return boolean
	 */
	public boolean isSitting() {
		return sitting;
	}
	
	/**
	 * Gets the chair the NPC is sitting on
	 * Returns null if the NPC isnt sitting
	 * 
	 * @return Location
	 */
	public Location getChair() {
		return location;
	}
	
	
	/**
     * If the NPC starts to navigate, and
     * he is sitting, he better stand up.
     *
     */
    @EventHandler
    public void onNavigationBeginEvent(NavigationBeginEvent event) {
        if (sitting)
            stand();
    }
    
    /**
     * If someone tries to break the poor
     * NPC's chair, we need to stop them!
     *
     */
    @EventHandler(ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        if (event.getBlock().getLocation().equals(location)) {
            event.setCancelled(true);
        }
    }
	
	protected SittingTrait() {
		super("sitting");
	}

}
