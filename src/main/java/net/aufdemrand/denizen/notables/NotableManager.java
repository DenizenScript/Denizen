package net.aufdemrand.denizen.notables;

import net.aufdemrand.denizen.Denizen;
import net.aufdemrand.denizen.utilities.debugging.dB;
import org.bukkit.Bukkit;
import org.bukkit.Location;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * This class manages a list of Notable objects.  Notable objects are named
 * locations in the game. 
 *
 * @author	aufdemrand
 */
public class NotableManager {
	//
	// Keeps a reference to the plugin that this manager is associated to.
	//
	Denizen denizen;

	//
	// This contains the Map of notable location names to the actual Location
	// objects.
	//
	private	Map<String,Notable>	notableMap = new ConcurrentHashMap<String,Notable> ();

	/**
	 * Create a new NotableManager without a reference to the plugin.
	 */
	public NotableManager () {
	}

	/**
	 * Create a new NotableMananger.
	 * 
	 * @param denizen
	 */
	public NotableManager(Denizen denizen) {
		this.denizen = denizen;
	}
	
	/**
	 * This method will add a Notable location to the manager's list of notable
	 * locations.
	 * 
	 * @param name	The name of the location.
	 * @param location	The location to set as the notable location.
	 * 
	 * @return	True if the notable was added, false otherwise.
	 */
	public boolean addNotable (String name, Location location) {
		this.notableMap.put (name, new Notable (name, location));
		saveNotables();
		return true;
	}

	/**
	 * Returns the Notable object associated to the specified name.
	 * 
	 * @param name	The name of the notable to find.
	 * 
	 * @return	The Notable object associated to the name, or null if not found.
	 */
	public Notable getNotable(String name) {
		return this.notableMap.get(name);
	}

	/**
	 * Returns the collection of Notable locations that are managed.
	 * 
	 * @return	The collection of Notables managed.
	 */
	public Collection<Notable> getNotables() {
		return this.notableMap.values();
	}

	/**
	 * This clears the manager's internal collection of Notable locations and
	 * loads them from external storage.  If there are NO notables found in the
	 * external storage, this will not clear the manager's internal list.
	 */
	public void loadNotables() {
		//
		// Grab the saved list, if it's empty, don't change anything.
		//
		if (this.denizen != null) {
			List<String> notablesList = denizen.getSaves().getStringList("Notables.List");
			if (notablesList.isEmpty()) {
				return;
			}
	
			//
			// Clear the internal collection and reload them.
			//
			this.notableMap.clear();
			for (String notable : notablesList) {
				String[] ns = notable.split(";");
				try {
					this.notableMap.put (ns[0], new Notable(ns[0], new Location(Bukkit.getServer().getWorld(ns[1]), Double.valueOf(ns[2]), Double.valueOf(ns[3]), Double.valueOf(ns[4]))));
				} catch (NumberFormatException nfe) {
					dB.echoError("NumberFormatException loading notable: " + notable);
				}
			}
		}
	}

	/**
	 * This method removes a Notable location from the manager's internal
	 * collection.
	 * 
	 * @param notable	The Notable to remove.
	 * 
	 * @return	The Notable that was removed, or null if not found.
	 */
	public Notable removeNotable(Notable notable) {
		return this.notableMap.remove(notable.getName()); 
	}

	/**
	 * This removes a Notable from teh manager's internal collection using the
	 * name of the Notable.
	 * 
	 * @param notableName	The name of the Notable to remove.
	 * 
	 * @return	The Notable that was removed, or null if not found.
	 */
	public Notable removeNotable(String notableName) {
		return this.notableMap.remove(notableName);
	}

	/**
	 * Saves the list of notable locations to external storage.
	 */
	public void saveNotables() {
		List<String> notablesList = new ArrayList<String>();
		for (Notable notable : this.notableMap.values()) {
			String	serializedNotable = notable.stringValue ();
			if (serializedNotable != null) {
				notablesList.add(notable.stringValue());
			}
		}

		if (this.denizen != null) {
			this.denizen.getSaves().set("Notables.List", notablesList);
		}
	}
}
