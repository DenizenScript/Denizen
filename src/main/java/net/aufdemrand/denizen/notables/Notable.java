package net.aufdemrand.denizen.notables;

import java.util.ArrayList;
import java.util.List;

import net.citizensnpcs.api.CitizensAPI;

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
	private List<Integer> links = new ArrayList<Integer> ();

	public Notable(String name, Location location) {
		this.location = location;
		this.name = name.toUpperCase();
	}

	@Override
	public boolean equals(Object object) {
		if (object == null) return false;
		if (object == this) return true;
		if (object.getClass() != getClass())
			return false;

		Notable onote = (Notable) object;
		return new EqualsBuilder().
				append(name, onote.getName()).
				isEquals();
	}

	public boolean addLink(Integer npcid) {
		if (links.contains(npcid)) 
			return false;
		else links.add(npcid);
		return true;
	}
	
	public boolean hasLink(Integer npcid) {
	    if (!links.contains(npcid))
            return false;
	    else return true;
	}

	public boolean removeLink(Integer npcid) {
		if (!links.contains(npcid))
			return false;
		else links.remove(npcid);
		return true;
	}

	public List<Integer> getLinks() {
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
		for (Integer npcid : links)
			linkString = ";" + npcid;
		return name + ";" + location.getWorld().getName() + ";" + location.getX() + ";" + location.getY() + ";" + location.getZ() + linkString; 
	}

	@Override
	public String toString() {
		String linkString = " Links: ";
		for (Integer npcid : links)
			linkString = CitizensAPI.getNPCRegistry().getById(npcid) + "/" + npcid + ", ";
		if (links.size() > 0)
			linkString = " Links: none";
		else linkString = linkString.substring(0, linkString.length() - 2);
		
		return "Name: " + name + " World: " + location.getWorld().getName() + " Location: " + location.getBlockX() + "," + location.getBlockY() + "," + location.getBlockZ() + linkString;	
	}
	
	public String describe() {
	    return "<a>" + name + "<b> " + location.getWorld().getName() + "<c> " + location.getBlockX() + "," + location.getBlockY() + "," + location.getBlockZ();
	}
	

}