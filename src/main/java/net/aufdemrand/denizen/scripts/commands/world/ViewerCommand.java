package net.aufdemrand.denizen.scripts.commands.world;

import net.aufdemrand.denizen.BukkitScriptEntryData;
import net.aufdemrand.denizen.events.bukkit.SavesReloadEvent;
import net.aufdemrand.denizen.objects.dLocation;
import net.aufdemrand.denizen.objects.dPlayer;
import net.aufdemrand.denizen.utilities.DenizenAPI;
import net.aufdemrand.denizen.utilities.Utilities;
import net.aufdemrand.denizen.utilities.debugging.dB;
import net.aufdemrand.denizencore.exceptions.CommandExecutionException;
import net.aufdemrand.denizencore.exceptions.InvalidArgumentsException;
import net.aufdemrand.denizencore.objects.Element;
import net.aufdemrand.denizencore.objects.aH;
import net.aufdemrand.denizencore.scripts.ScriptEntry;
import net.aufdemrand.denizencore.scripts.commands.AbstractCommand;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

// TODO: should this command exist?
public class ViewerCommand extends AbstractCommand implements Listener {

    private enum Action {CREATE, MODIFY, REMOVE}

    private enum Type {SIGN_POST, WALL_SIGN}

    private enum Display {LOCATION, SCORE}

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
                scriptEntry.addObject("display, d", Display.valueOf(arg.getValue().toUpperCase()));

            else if (arg.matchesPrefix("i, id"))
                scriptEntry.addObject("id", arg.asElement());

            else if (!scriptEntry.hasObject("location")
                    && arg.matchesArgumentType(dLocation.class))
                // Location arg
                scriptEntry.addObject("location", arg.asType(dLocation.class).setPrefix("location"));

            else if (!scriptEntry.hasObject("direction")
                    && arg.matchesPrefix("direction, dir"))
                scriptEntry.addObject("direction", arg.asElement());

            else arg.reportUnhandled();
        }


        if (!scriptEntry.hasObject("action"))
            scriptEntry.addObject("action", Action.CREATE);

        if (!scriptEntry.hasObject("display") && scriptEntry.getObject("action").equals(Action.CREATE))
            scriptEntry.addObject("display", Display.LOCATION);

        if (!scriptEntry.hasObject("id"))
            throw new InvalidArgumentsException("Must specify a Viewer ID!");

        if (!scriptEntry.hasObject("location") && scriptEntry.getObject("action").equals(Action.CREATE))
            throw new InvalidArgumentsException("Must specify a Sign location!");

        if (!scriptEntry.hasObject("type") && scriptEntry.getObject("action").equals(Action.CREATE))
            scriptEntry.addObject("type", Type.SIGN_POST);
    }


    @Override
    public void execute(final ScriptEntry scriptEntry) throws CommandExecutionException {

        dB.echoError(scriptEntry.getResidingQueue(), "This command is deprecated! If you use this, please "
                + "contact Morphan1 or mcmonkey on irc.esper.net#denizen-dev");

        // Get objects
        String direction = scriptEntry.hasObject("direction") ? ((Element) scriptEntry.getObject("direction")).asString() : null;
        Action action = (Action) scriptEntry.getObject("action");
        Type type = scriptEntry.hasObject("type") ? (Type) scriptEntry.getObject("type") : null;
        Display display = scriptEntry.hasObject("display") ? (Display) scriptEntry.getObject("display") : null;
        final String id = scriptEntry.getObject("id").toString();
        if (viewers.containsKey(id)) {
            ((BukkitScriptEntryData) scriptEntry.entryData).setPlayer(dPlayer.valueOf(viewers.get(id).getContent().split("; ")[1]));
        }
        dLocation location = scriptEntry.hasObject("location") ? (dLocation) scriptEntry.getObject("location") : null;
        String content = scriptEntry.hasObject("display") ? display.toString() + "; " +
                ((BukkitScriptEntryData) scriptEntry.entryData).getPlayer().getOfflinePlayer().getUniqueId() : null;

        switch (action) {

            case CREATE:
                if (viewers.containsKey(id)) {
                    dB.echoDebug(scriptEntry, "Viewer ID " + id + " already exists!");
                    return;
                }

                Viewer viewer = new Viewer(id, content, location);
                viewers.put(id, viewer);

                final Block sign = location.getBlock();
                sign.setType(Material.valueOf(type.name()));

                if (direction != null)
                    Utilities.setSignRotation(sign.getState(), direction);
                else
                    Utilities.setSignRotation(sign.getState());

                int task = Bukkit.getScheduler().scheduleSyncRepeatingTask(DenizenAPI.getCurrentInstance(), new Runnable() {
                    public void run() {
                        Player player = Bukkit.getPlayer(UUID.fromString(viewers.get(id).getContent().split("; ")[1]));
                        if (player == null)
                            Utilities.setSignLines((Sign) viewers.get(id).getLocation().getBlock().getState(), new String[]{"", viewers.get(id).getContent().split("; ")[1], "is offline.", ""});
                        else
                            Utilities.setSignLines((Sign) viewers.get(id).getLocation().getBlock().getState(), new String[]{String.valueOf((int) player.getLocation().getX()), String.valueOf((int) player.getLocation().getY()), String.valueOf((int) player.getLocation().getZ()), player.getWorld().getName()});

                    }
                }, 0, 20);

                viewer.setTask(task);
                viewer.save();

                break;


            case MODIFY:
                if (!viewers.containsKey(id)) {
                    dB.echoDebug(scriptEntry, "Viewer ID " + id + " doesn't exist!");
                    return;
                }
                if (content != null) viewers.get(id).setContent(content);
                if (location != null) {
                    if (type == null) type = Type.valueOf(viewers.get(id).getLocation().getBlock().getType().name());
                    Bukkit.getScheduler().cancelTask(viewers.get(id).getTask());
                    int newTask = Bukkit.getScheduler().scheduleSyncRepeatingTask(DenizenAPI.getCurrentInstance(), new Runnable() {
                        public void run() {
                            Player player = Bukkit.getPlayer(UUID.fromString(viewers.get(id).getContent().split("; ")[1]));
                            if (player == null)
                                Utilities.setSignLines((Sign) viewers.get(id).getLocation().getBlock().getState(), new String[]{"", viewers.get(id).getContent().split("; ")[1], "is offline.", ""});
                            else
                                Utilities.setSignLines((Sign) viewers.get(id).getLocation().getBlock().getState(), new String[]{String.valueOf((int) player.getLocation().getX()), String.valueOf((int) player.getLocation().getY()), String.valueOf((int) player.getLocation().getZ()), player.getWorld().getName()});

                        }
                    }, 0, 20);
                    viewers.get(id).getLocation().getBlock().setType(Material.AIR);
                    viewers.get(id).setLocation(location);
                    viewers.get(id).setTask(newTask);
                    location.getBlock().setType(Material.valueOf(type.name()));
                }

                break;

            case REMOVE:
                if (!viewers.containsKey(id)) {
                    dB.echoDebug(scriptEntry, "Viewer ID " + id + " doesn't exist!");
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
            for (final String id : saves.getConfigurationSection("Viewers").getKeys(false)) {
                Viewer viewer = new Viewer(id, saves.getString("Viewers." + id.toLowerCase() + ".content"), dLocation.valueOf(saves.getString("Viewers." + id.toLowerCase() + ".location")));
                viewers.put(id, viewer);
                if (viewer.getContent().startsWith("LOCATION")) {
                    int task = Bukkit.getScheduler().scheduleSyncRepeatingTask(DenizenAPI.getCurrentInstance(), new Runnable() {
                        public void run() {
                            Player player = Bukkit.getPlayer(UUID.fromString(viewers.get(id).getContent().split("; ")[1]));
                            if (player == null)
                                Utilities.setSignLines((Sign) viewers.get(id).getLocation().getBlock().getState(), new String[]{"", viewers.get(id).getContent().split("; ")[1], "is offline.", ""});
                            else
                                Utilities.setSignLines((Sign) viewers.get(id).getLocation().getBlock().getState(), new String[]{String.valueOf((int) player.getLocation().getX()), String.valueOf((int) player.getLocation().getY()), String.valueOf((int) player.getLocation().getZ()), player.getWorld().getName()});

                        }
                    }, 0, 20);
                    viewer.setTask(task);
                }
            }
    }

    @EventHandler
    public static void blockBreak(BlockBreakEvent event) {
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
