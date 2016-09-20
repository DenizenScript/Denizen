package net.aufdemrand.denizen.scripts.containers.core;

import com.google.common.base.Predicate;
import net.aufdemrand.denizen.utilities.DenizenAPI;
import net.aufdemrand.denizen.utilities.DenizenAliasHelpTopic;
import net.aufdemrand.denizen.utilities.DenizenCommand;
import net.aufdemrand.denizen.utilities.DenizenCommandHelpTopic;
import net.aufdemrand.denizen.utilities.debugging.dB;
import org.bukkit.Server;
import org.bukkit.command.Command;
import org.bukkit.command.CommandMap;
import org.bukkit.command.defaults.VanillaCommand;
import org.bukkit.event.Listener;
import org.bukkit.help.HelpMap;
import org.bukkit.help.HelpTopic;
import org.bukkit.scheduler.BukkitRunnable;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class CommandScriptHelper implements Listener {

    private static Map<String, DenizenCommand> denizenCommands = new ConcurrentHashMap<String, DenizenCommand>(8, 0.9f, 1);
    private static Map<String, Command> overriddenCommands = new HashMap<String, Command>();
    private static Map<String, HelpTopic> overriddenHelpTopics = new HashMap<String, HelpTopic>();
    private static Map<String, Command> knownCommands = null;
    private static Map<String, HelpTopic> helpTopics = null;
    private static boolean hasCommandInformation = true;

    public CommandScriptHelper() {
        try {
            final Server server = DenizenAPI.getCurrentInstance().getServer();

            server.getPluginManager().registerEvents(this, DenizenAPI.getCurrentInstance());

            // Get the CommandMap for the server
            final Field commandMapField = server.getClass().getDeclaredField("commandMap");
            commandMapField.setAccessible(true);
            CommandMap commandMap = (CommandMap) commandMapField.get(server);

            // Get the knownCommands for the server's CommandMap
            final Field knownCommandsField = commandMap.getClass().getDeclaredField("knownCommands");
            knownCommandsField.setAccessible(true);
            knownCommands = (Map<String, Command>) knownCommandsField.get(commandMap);

            // Get the HelpMap for the server
            HelpMap helpMap = server.getHelpMap();

            // Get the helpTopics for the server's HelpMap
            final Field helpTopicsField = helpMap.getClass().getDeclaredField("helpTopics");
            helpTopicsField.setAccessible(true);
            helpTopics = (Map<String, HelpTopic>) helpTopicsField.get(helpMap);

            // The Minecraft Help command doesn't like our added commands,
            // so let's force the server to use Bukkit's version if it's running
            // Mojang's version.
            // TODO: figure out a different workaround?
            // TODO: config option for this?
            new BukkitRunnable() {
                @Override
                public void run() {
                    if (!(knownCommands.get("help") instanceof VanillaCommand)) {
                        return;
                    }
                    knownCommands.put("help", knownCommands.get("bukkit:help"));
                    helpTopics.put("/help", helpTopics.get("/bukkit:help"));
                }
            }.runTaskLater(DenizenAPI.getCurrentInstance(), 1);
        }
        catch (Exception e) {
            dB.echoError("Error getting the server's command information! Are you running a non-CraftBukkit server?");
            dB.echoError("Command scripts will not function!");
            //dB.echoError(e);
            hasCommandInformation = false;
        }
    }

    /**
     * Removes all registered {@link DenizenCommand DenizenCommands} from CraftBukkit and restores any
     * overridden Commands.
     *
     * @see #registerDenizenCommand(DenizenCommand)
     */
    public static void removeDenizenCommands() {
        if (!hasCommandInformation) {
            return;
        }
        for (String command : denizenCommands.keySet()) {
            knownCommands.remove(command);
            helpTopics.remove(command);
            if (overriddenCommands.containsKey(command)) {
                knownCommands.put(command, overriddenCommands.get(command));
                if (overriddenHelpTopics.containsKey(command)) {
                    helpTopics.put(command, overriddenHelpTopics.get(command));
                }
            }
        }
        denizenCommands.clear();
    }

    /**
     * Registers a {@link DenizenCommand} to CraftBukkit, including aliases and help command
     * information. This will override any existing command aliases or names.
     *
     * @param command the command to register.
     * @see #removeDenizenCommands()
     */
    public static void registerDenizenCommand(DenizenCommand command) {
        if (!hasCommandInformation) {
            return;
        }
        String name = command.getName();
        // Existing Denizen commands take priority!
        if (!denizenCommands.containsKey(name)) {
            // Register the command
            forceCommand(name, command, new DenizenCommandHelpTopic(command));
            // Register each alias
            for (String alias : command.getAliases()) {
                if (denizenCommands.containsKey(alias)) {
                    continue;
                }
                forceCommand(alias, command, new DenizenAliasHelpTopic("/" + alias, name,
                        DenizenAPI.getCurrentInstance().getServer().getHelpMap()));
            }
        }
    }

    /**
     * Forces CraftBukkit to recognize DenizenCommands, and overrides any existing
     * commands of the same name. This should be called for the name of the command
     * and each alias of the command.
     *
     * @param name      name or alias of the command.
     * @param command   the command.
     * @param helpTopic the help topic for the command or command alias.
     */
    private static void forceCommand(String name, DenizenCommand command, HelpTopic helpTopic) {
        // Override any existing non-DenizenCommand commands, but save them just in case
        // TODO: use fallback prefixes for overridden commands instead?
        if (knownCommands.containsKey(name)) {
            overriddenCommands.put(name, knownCommands.get(name));
            knownCommands.remove(name);
            if (helpTopics.containsKey(name)) {
                overriddenHelpTopics.put(name, helpTopics.get(name));
                helpTopics.remove(name);
            }
        }
        knownCommands.put(name, command);
        helpTopics.put(helpTopic.getName(), helpTopic);
        denizenCommands.put(name, command);
    }

    private static class IsCommandTopicPredicate implements Predicate<HelpTopic> {
        public boolean apply(HelpTopic topic) {
            return topic.getName().charAt(0) == '/';
        }
    }
}
