package net.aufdemrand.denizen.scripts.commands.world;

import net.aufdemrand.denizen.events.SavesReloadEvent;
import net.aufdemrand.denizen.exceptions.CommandExecutionException;
import net.aufdemrand.denizen.exceptions.InvalidArgumentsException;
import net.aufdemrand.denizen.objects.aH;
import net.aufdemrand.denizen.objects.dLocation;
import net.aufdemrand.denizen.scripts.ScriptEntry;
import net.aufdemrand.denizen.scripts.commands.AbstractCommand;
import net.aufdemrand.denizen.utilities.DenizenAPI;
import net.aufdemrand.denizen.utilities.Utilities;
import net.aufdemrand.denizen.utilities.debugging.dB;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;

/**
 * Creates special signs that auto-update with information.
 * - viewer ({create <location>}/modify/remove) [id:<name>] (type:{sign_post}/wall_sign) (display:{location}/score/logged_in)
 * @author Morphan1
 */
public class ViewerCommand extends AbstractCommand implements Listener {

	private enum Action { CREATE, MODIFY, REMOVE }
	private enum Type { SIGN_POST, WALL_SIGN }
	private enum Display { LOCATION, SCORE }
	
    static Map<String, Viewer> viewers = new ConcurrentHashMap<String, Viewer>();

	@Override
	public void parseArgs(ScriptEntry scriptEntry) throws InvalidArgumentsException {

		for (aH.Argument arg : aH.interpret(scriptEntry.getArguments())) {

        	if (!scriptEntry.hasObject("action")
                    && arg.matchesEnum(Action.values()))
                // add Action
                scriptEntry.addObject("action", Action.valueOf(arg.getValue().toUpperCase()));
        	
        	else if (!scriptEntry.hasObject("type")
                    && arg.matchesEnum(Type.values()))
                // add Action
                scriptEntry.addObject("type", Type.valueOf(arg.getValue().toUpperCase()));
        	
        	else if (!scriptEntry.hasObject("display")
                    && arg.matchesEnum(Display.values()))
                // add Action
                scriptEntry.addObject("display", Display.valueOf(arg.getValue().toUpperCase()));
        	
        	else if (arg.matchesPrefix("i, id"))
                scriptEntry.addObject("id", arg.asElement());
        	
        	else if (!scriptEntry.hasObject("location")
                    && arg.matchesArgumentType(dLocation.class))
                // Location arg
                scriptEntry.addObject("location", arg.asType(dLocation.class).setPrefix("location"));
        	
        }


        if (!scriptEntry.hasObject("action"))
            scriptEntry.addObject("action", Action.CREATE);
        
        if (!scriptEntry.hasObject("display"))
        	scriptEntry.addObject("display", Display.LOCATION);
        
        if (!scriptEntry.hasObject("id"))
        	throw new InvalidArgumentsException("Must specify a Viewer ID!");
        
        if (!scriptEntry.hasObject("location") && scriptEntry.getObject("action").equals(Action.CREATE))
            throw new InvalidArgumentsException("Must specify a Sign location!");
        
        if (!scriptEntry.hasObject("type"))
        	scriptEntry.addObject("type", Type.SIGN_POST);
	}


	@SuppressWarnings("unchecked")
	@Override
    public void execute(final ScriptEntry scriptEntry) throws CommandExecutionException {

		// Get objects
		Action action = (Action) scriptEntry.getObject("action");
		Type type = (Type) scriptEntry.getObject("type");
		Display display = (Display) scriptEntry.getObject("display");
        String id = (String) scriptEntry.getObject("id").toString();
        dLocation location = (dLocation) scriptEntry.getObject("location");
		String content = display.toString() + "; " + scriptEntry.getPlayer().getName();
		
        switch (action) {
        	
        	case CREATE:
        		if (viewers.containsKey(id)) {
        			dB.echoDebug("Viewer ID " + id + " already exists!");
        			return;
        		}
        		
        		Viewer viewer = new Viewer(id, content, location);
        		viewers.put(id, viewer);
        		
        		Block sign = location.getBlock();
        		sign.setType(Material.valueOf(type.name()));
                final BlockState signState = sign.getState();
                
                Utilities.setSignRotation(signState);
                
        		int task = Bukkit.getScheduler().scheduleSyncRepeatingTask(DenizenAPI.getCurrentInstance(), new Runnable() {
        			public void run() {
        				Player player = Bukkit.getPlayerExact(scriptEntry.getPlayer().getName());
        				if (player == null)
        					Utilities.setSignLines((Sign) signState, new String[]{"", scriptEntry.getPlayer().getName(), "is offline.", ""});
        				else
        					Utilities.setSignLines((Sign) signState, new String[]{String.valueOf(scriptEntry.getPlayer().getLocation().getX()), String.valueOf(scriptEntry.getPlayer().getLocation().getY()), String.valueOf(scriptEntry.getPlayer().getLocation().getZ()), scriptEntry.getPlayer().getWorld().getName()});
        				
        			}
        		}, 0, 20);
        		
        		viewer.setTask(task);
        		viewer.save();
        		
        		break;
        		
        		
        	case MODIFY:
        		// Insert stuff
        		break;
        		
        	case REMOVE:
        		if (!viewers.containsKey(id)) {
        			dB.echoDebug("Viewer ID " + id + " doesn't exist!");
        			return;
        		}
        		
        		Block block = viewers.get(id).getLocation().getBlock();
        		block.setType(Material.AIR);
            
        		Bukkit.getScheduler().cancelTask(viewers.get(id).getTask());
        		viewers.get(id).remove();
        		viewers.remove(id);
        }
    }

    private static class Viewer {
        private String id;
        private String content;
        private dLocation location;
        private int task;

        private Viewer(String id) {
            this.id = id;
        }
        
        private Viewer(String id, String content, dLocation location) {
        	this.id = id;
        	this.content = content;
        	this.location = location;
        }

        void setContent(String content) {
            this.content = content;
        }

        void setLocation(dLocation location) {
            this.location = location;
        }
        
        void setTask(int task) {
        	this.task = task;
        }
        
        private String getContent() {
        	return this.content;
        }
        
        private dLocation getLocation() {
        	return this.location;
        }
        
        private int getTask() {
        	return this.task;
        }

        void save() {
            FileConfiguration saves = DenizenAPI.getCurrentInstance().getSaves();

            // Save content
            saves.set("Viewers." + id.toLowerCase() + ".content", content);
            // Save location
            saves.set("Viewers." + id.toLowerCase() + ".location", location.identify());
        }
        
        void remove() {
        	FileConfiguration saves = DenizenAPI.getCurrentInstance().getSaves();
        	
        	saves.set("Viewers." + id.toLowerCase(), null);
        }

    }

    @EventHandler
    public static void reloadViewers(SavesReloadEvent event) {
    	
    	for (Viewer viewer : viewers.values()) {
    		Bukkit.getScheduler().cancelTask(viewer.getTask());
    	}
    	
        viewers.clear();

        FileConfiguration saves = DenizenAPI.getCurrentInstance().getSaves();
        
        if (saves.contains("Viewers"))
        	for (String key : saves.getConfigurationSection("Viewers").getKeys(false)) {
        		Viewer viewer = new Viewer(key, saves.getString("Viewers." + key.toLowerCase() + ".content"), dLocation.valueOf(saves.getString("Viewers." + key.toLowerCase() + ".location")));
        		viewers.put(key, viewer);
        		final Sign sign = (Sign) viewer.getLocation().getBlock().getState();
        		final String[] content = viewer.getContent().split("; ");
        		if (viewer.getContent().startsWith("location")) {
        			int task = Bukkit.getScheduler().scheduleSyncRepeatingTask(DenizenAPI.getCurrentInstance(), new Runnable() {
        				public void run() {
        					Player player = Bukkit.getPlayerExact(content[1]);
        					if (player == null)
        						Utilities.setSignLines((Sign) sign, new String[]{"", content[1], "is offline.",""});
        					else
        						Utilities.setSignLines((Sign) sign, new String[]{String.valueOf(player.getLocation().getX()), String.valueOf(player.getLocation().getY()), String.valueOf(player.getLocation().getZ()), player.getWorld().getName()});
        				}
        			}, 0, 20);
        			viewer.setTask(task);
        		}
        	}
        else
        	return;
    }
    
    @EventHandler
    public static void blockBreaks(BlockBreakEvent event) {
    	dLocation location = new dLocation(event.getBlock().getLocation());
    	for (Viewer viewer : viewers.values())
    		if (Utilities.isBlock(location, viewer.getLocation())) {
    			event.getPlayer().sendMessage(ChatColor.RED + "You're not allowed to break that sign.");
    			event.setCancelled(true);
    		}
    }
    
    @Override
    public void onEnable() {
        DenizenAPI.getCurrentInstance().getServer().getPluginManager()
        	.registerEvents(this, DenizenAPI.getCurrentInstance());
    }
	
	@Override
	public void onDisable() {
		for (Viewer viewer : viewers.values())
			viewer.save();
	}

}