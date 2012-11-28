package net.aufdemrand.denizen.notables;

import java.util.ArrayList;
import java.util.List;

import net.aufdemrand.denizen.Denizen;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;

import org.bukkit.Bukkit;
import org.bukkit.Location;

public class NotableManager {

	Denizen plugin;
	
	public NotableManager(Denizen denizenPlugin) {
		this.plugin = denizenPlugin;
		loadNotables();
	}
	
	private final List<Notable> notables = new ArrayList<Notable>();
	
	public void loadNotables() {
		List<String> notablesList = plugin.getSaves().getStringList("Notables.List");
		if (notablesList.isEmpty()) return;
		notables.clear();
		for (String notable : notablesList) {
			String[] ns = notable.split(";");
			notables.add(new Notable(ns[0], new Location(Bukkit.getServer().getWorld(ns[1]), Double.valueOf(ns[2]), Double.valueOf(ns[3]), Double.valueOf(ns[4]))));
			if (ns.length > 5)
				for (int x = 5; x < ns.length; x++)
					try {
						getNotable(ns[0]).addLink(CitizensAPI.getNPCRegistry().getById(Integer.valueOf(ns[x])));
					} catch (Exception e) { plugin.getDebugger().echoDebug("Invalid NPC linked to Notable '%s'", ns[0]); }
		}
	}
	
	public void saveNotables() {
		List<String> notablesList = new ArrayList<String>();
		for (Notable notable : notables)
			notablesList.add(notable.stringValue());
		plugin.getSaves().set("Notables.List", notablesList);
	}
	
    public boolean addNotable(String name, Location location) {
        Notable newNotable = new Notable(name, location);
        if (notables.contains(newNotable))
            return false;
        notables.add(newNotable);
        saveNotables();
        return true;
    }
    
    public boolean addLink(String notableName, NPC npc) {
    	if (notables.contains(notableName)) {
    		getNotable(notableName).addLink(npc);
    		saveNotables();
    		return true;
    	}
    	else return false;
    }
    
    public boolean removeLink(String notableName, NPC npc) {
    	if (notables.contains(notableName)) {
    		getNotable(notableName).removeLink(npc);
    		saveNotables();
    		return true;
    	}
    	else return false;
    }
    
    public Notable getNotable(String name) {
        for (Notable notable : notables)
            if (notable.getName().equalsIgnoreCase(name))
                return notable;
        return null;
    }

    public List<Notable> getNotables() {
        return notables;
    }

    public boolean removeNotable(Notable notable) {
        if (notables.contains(notable)) {
            notables.remove(notable);
            return true;
        }
        return false;
    }
	
}
