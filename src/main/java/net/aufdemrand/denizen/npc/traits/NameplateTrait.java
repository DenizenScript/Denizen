package net.aufdemrand.denizen.npc.traits;

import java.util.ArrayList;
import java.util.List;

import net.aufdemrand.denizen.utilities.Depends;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.persistence.Persist;
import net.citizensnpcs.api.trait.Trait;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;


/**
 * <p>Adds the ability to change the nameplate (aka nametag) above a NPC's head and
 * add colors like red, green, blue and gold. Special cases like the magic color
 * are also being supported</p>
 * 
 * <p>Once an NPC with the nameplate trait is being spawned (or the nameplate is
 * being changed by commands) the server checks against the specific packet and
 * modifies it.</p>
 * 
 * <p><b>Please mind that the nameplates can only be 16 characters long,
 * including the color codes!</b></p>
 */
public class NameplateTrait extends Trait implements Listener {

	@Persist("")
	private ChatColor color = null;

	public NameplateTrait() {
		super("nameplate");
	}

    @Override
    public void onSpawn() {
		if(getColor() != null ) {
			refreshTag( getNPC() );
		}
    }

    public void setColor(ChatColor color) {
		this.color = color;
		
        refreshTag( getNPC() );
    }
	
	/**
	 * Returns the {@link ChatColor} prefixed to the nameplate
	 * 
	 * @return The stored {@link ChatColor}
	 */
	public ChatColor getColor() {
		return color;
	}
	
	
	/**
	 * Returns true if a color has been set
	 * 
	 * @return True if set, otherwise false
	 */
	public boolean hasColor() {
		return getColor() != null;
	}
	
	/**
	 * Retrieve the trimmed nameplate including the set color (max. 16 chars).
	 * 
	 * @return The trimmed nameplate including color
	 */
	public String getTrimmedTag() {
		String tag = getNPC().getName();
		
		if(color != null) tag = getColor() + tag;
		if(tag.length() > 16) tag = tag.substring(0, 16);
	
		return tag;
	}

	public void refreshTag(NPC npc) {
		if( Depends.protocolManager == null || !npc.isSpawned() ) return;
		
		int maxDistance = Bukkit.getServer().getViewDistance() * 16;
		List<Player> viewers = new ArrayList<Player>();
		
		for (Player p : getPlayersInRadius(npc, maxDistance)) {
			if( p.getEntityId() == npc.getBukkitEntity().getEntityId() ) continue;
			
			viewers.add(p);
		}
		
		refreshTag(npc, viewers);
	}
	
	private void refreshTag(NPC npc, List<Player> players) {
		try {
			Depends.protocolManager.updateEntity(npc.getBukkitEntity(), players);
		} catch (Exception e) {}
	}
	
	private List<Player> getPlayersInRadius( NPC npc, int distance ) {
		List<Player> players = new ArrayList<Player>();
		int dSquared = distance * distance;
		
		Location loc = npc.getBukkitEntity().getLocation();
		World world = npc.getBukkitEntity().getWorld();
		
		for ( Player p : Bukkit.getServer().getOnlinePlayers()) {
			if( p.getWorld() != world) continue;
			
			if( p.getLocation().distanceSquared(loc) <= dSquared) {
				players.add(p);
			}
		}
		
		return players;
	}
	
}
