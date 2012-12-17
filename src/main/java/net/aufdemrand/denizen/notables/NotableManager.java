package net.aufdemrand.denizen.notables;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import net.aufdemrand.denizen.Denizen;
import net.aufdemrand.denizen.utilities.debugging.dB;
import net.citizensnpcs.api.npc.NPC;

import org.bukkit.Bukkit;
import org.bukkit.Location;

public class NotableManager {

    Denizen denizen;

    public NotableManager(Denizen denizen) {
        this.denizen = denizen;
    }

    private List<Notable> notables = new ArrayList<Notable>();
    private Map<Integer, List<String>> links = new ConcurrentHashMap<Integer, List<String>>();

    public void loadNotables() {
        List<String> notablesList = denizen.getSaves().getStringList("Notables.List");
        if (notablesList.isEmpty()) return;
        notables.clear();
        for (String notable : notablesList) {
            String[] ns = notable.split(";");
            notables.add(new Notable(ns[0], new Location(Bukkit.getServer().getWorld(ns[1]), Double.valueOf(ns[2]), Double.valueOf(ns[3]), Double.valueOf(ns[4]))));
            if (ns.length > 5)
                for (int x = 5; x < ns.length; x++)
                    try {
                        // Add to Notable
                        getNotable(ns[0]).addLink(Integer.valueOf(ns[x]));
                        // Add to links list for easy recall by NPCID
                        List<String> tempList = new ArrayList<String>();
                        if (links.containsKey(Integer.valueOf(ns[x]))) tempList = links.get(Integer.valueOf(ns[x]));
                        tempList.add(ns[0]);
                        links.put(Integer.valueOf(ns[x]), tempList);
                    } catch (Exception e) { dB.echoDebug("Invalid NPC linked to Notable '%s'", ns[0]); }
        }
    }

    public void saveNotables() {
        List<String> notablesList = new ArrayList<String>();
        for (Notable notable : notables) {
            notablesList.add(notable.stringValue());
        }
        denizen.getSaves().set("Notables.List", notablesList);
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
        Notable notable = getNotable(notableName);
        if (notable == null) return false;
        // Add to notable
        getNotable(notableName).addLink(npc.getId());
        // Add to links list for easy recall by NPCID
        List<String> tempList = new ArrayList<String>();
        if (links.containsKey(Integer.valueOf(npc.getId()))) tempList = links.get(Integer.valueOf(npc.getId()));
        tempList.add(notableName.toUpperCase());
        links.put(Integer.valueOf(npc.getId()), tempList);
        // Save to saves.yml
        saveNotables();
        return true;
    }

    public boolean removeLink(String notableName, NPC npc) {
        Notable notable = getNotable(notableName);
        if (notable == null) return false;
        // Remove link
        getNotable(notableName).removeLink(npc.getId());
        // Get list to modify for links
        List<String> tempList = new ArrayList<String>();
        if (links.containsKey(Integer.valueOf(npc.getId()))) tempList = links.get(Integer.valueOf(npc.getId()));
        tempList.remove(notableName.toUpperCase());
        links.put(Integer.valueOf(npc.getId()), tempList);
        saveNotables();
        return true;
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

    public boolean removeNotable(String notableName) {
        Notable notable = getNotable(notableName);
        if (notable == null) return false;
        removeNotable(notable);
        return true;
    }
    
    public boolean removeNotable(Notable notable) {
        if (notables.contains(notable)) {
            notables.remove(notable);
            return true;
        }
        return false;
    }

}
