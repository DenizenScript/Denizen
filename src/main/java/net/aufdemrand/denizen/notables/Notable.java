package net.aufdemrand.denizen.notables;

import java.util.List;

import net.citizensnpcs.api.npc.NPC;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.bukkit.Location;

/*
 * Notable object which holds a Location with a name to identify.
 * Also holds a list of NPCs (links) for use with a Location Trigger
 */

public class Notable {
	private final String name;
	private final Location location;
	private List<NPC> links;

	public Notable(String name, Location location) {
		this.location = location;
		this.name = name;
	}

	@Override
	public boolean equals(Object object) {
		if (object == null) return false;
		if (object == this) return true;
		if (object.getClass() != getClass())
			return false;

		Notable op = (Notable) object;
		return new EqualsBuilder().
				append(name, op.getName()).
				isEquals();
	}

	public boolean addLink(NPC npc) {
		if (links.contains(npc)) 
			return false;
		else links.add(npc);
		return true;
	}
	
	public boolean hasLink(NPC npc) {
	    if (!links.contains(npc))
            return false;
	    else return true;
	}

	public boolean removeLink(NPC npc) {
		if (!links.contains(npc))
			return false;
		else links.remove(npc);
		return true;
	}

	public List<NPC> getLinks() {
		return links;
	}

	public String getName() {
		return name;
	}

	public Location getLocation() {
		return location;
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder(13, 21). 
				append(name).
				toHashCode();
	}

	public String stringValue() {
		String linkString = "";
		for (NPC npc : links)
			linkString = ";" + npc.getId();
		return name + ";" + location.getWorld().getName() + ";" + location.getX() + ";" + location.getY() + ";" + location.getZ() + linkString; 
	}

	@Override
	public String toString() {
		String linkString = " Links: ";
		for (NPC npc : links)
			linkString = npc.getName() + "/" + npc.getId() + ", ";
		if (links.size() > 0)
			linkString = " Links: none";
		else linkString = linkString.substring(0, linkString.length() - 2);
		
		return "Name: " + name + " World: " + location.getWorld().getName() + " Location: " + location.getBlockX() + "," + location.getBlockY() + "," + location.getBlockZ() + linkString;	
	}
	
	public String describe() {
	    return "<e>" + name + " <a> " + location.getWorld().getName() + " <b> " + location.getBlockX() + "," + location.getBlockY() + "," + location.getBlockZ();
	}
	

}