package net.aufdemrand.denizen.npc.traits;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import net.aufdemrand.denizen.utilities.DenizenAPI;
import net.aufdemrand.denizen.utilities.depends.Depends;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.persistence.Persist;
import net.citizensnpcs.api.trait.Trait;
import net.citizensnpcs.api.util.DataKey;

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
	private final static String DEFAULT_KEY = "_default_";

    @Persist("text")
    private String text = null;

    @Persist(value="colors", collectionType=ConcurrentHashMap.class)
	private Map<String, String> colors = new ConcurrentHashMap<String, String>(8, 0.9f, 1);

	public NameplateTrait() {
		super("nameplate");
	}

    @Override
    public void onSpawn() {

        if(text != null) {
            Bukkit.getScheduler().scheduleSyncDelayedTask(DenizenAPI.getCurrentInstance(),
                    new Runnable() {
                        @Override
                        public void run() {
                            npc.getBukkitEntity().setCustomNameVisible(true);
                            npc.getBukkitEntity().setCustomName(text);
                        }
                    }, 2);
        }

		if(getColor() != null ) {
			refreshTag( getNPC() );
		}
    }

    @Override
    public void onDespawn() {
        if (npc.getBukkitEntity().getCustomName() != null)
            text = npc.getBukkitEntity().getCustomName();
    }

    public void setColor(ChatColor color) {
		this.setColor(color, DEFAULT_KEY);
    }
	
	public void setColor(ChatColor color, String player) {
		this.colors.put(player, color.name());
		refreshTag( getNPC() );
	}
	
	/**
	 * Returns the {@link ChatColor} prefixed to the nameplate
	 * 
	 * @return The stored {@link ChatColor}
	 */
	public ChatColor getColor() {
		try {
            return ChatColor.valueOf(colors.get(DEFAULT_KEY));
        } catch (Exception e) { return ChatColor.YELLOW; }
	}
	
	/**
	 * Returns the {@link ChatColor} prefixed to the nameplate for a specific
	 * player.
	 * 
	 * @param player The player name
	 * @return The stored {@link ChatColor} for the specific player
	 */
	public ChatColor getColor(String player) {
		if(!colors.containsKey(player)) return getColor();
		else return ChatColor.valueOf(colors.get(player));
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
	 * Returns true if a color has been set for this player
	 * 
	 * @param player The player name to check
	 * @return True if set, otherwise false
	 */
	public boolean hasColor(String player) {
		return getColor(player) != null;
	}
	
	/**
	 * Retrieve the trimmed nameplate including the set color (max. 16 chars).
	 * 
	 * @return The trimmed nameplate including color
	 */
	public String getTrimmedTag() {
		return getTrimmedTag(DEFAULT_KEY);
	}
	
	/**
	 * Retrieve the trimmed nameplate including the set color (max. 16 chars) for
	 * the specific player.
	 * 
	 * @param player The player name
	 * @return The trimmed nameplate including color
	 */
	public String getTrimmedTag(String player) {
		String tag = getNPC().getName();
		ChatColor c = getColor(player);
		
		if(c != null) tag = c + tag;
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
		Entity npcEntity = npc.getBukkitEntity();
		
		if(npcEntity != null) {
			Depends.protocolManager.updateEntity(npcEntity, players);
		}
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
