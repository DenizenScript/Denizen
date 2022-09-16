package com.denizenscript.denizen.scripts.containers.core;

import com.denizenscript.denizen.Denizen;
import com.denizenscript.denizen.utilities.command.scripted.DenizenAliasHelpTopic;
import com.denizenscript.denizen.utilities.command.scripted.DenizenCommand;
import com.denizenscript.denizen.utilities.command.scripted.DenizenCommandHelpTopic;
import com.denizenscript.denizencore.utilities.debugging.Debug;
import com.google.common.base.Predicate;
import com.denizenscript.denizen.utilities.Settings;
import com.denizenscript.denizen.events.bukkit.ScriptReloadEvent;
import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.command.Command;
import org.bukkit.command.CommandMap;
import org.bukkit.command.defaults.HelpCommand;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.help.HelpMap;
import org.bukkit.help.HelpTopic;
import org.bukkit.scheduler.BukkitRunnable;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

public class CommandScriptHelper implements Listener {

    public static Map<String, DenizenCommand> denizenCommands = new HashMap<>();
    public static Map<String, Command> overriddenCommands = new HashMap<>();
    public static Map<String, HelpTopic> overriddenHelpTopics = new HashMap<>();
    public static Map<String, Command> knownCommands = null;
    public static Map<String, HelpTopic> helpTopics = null;
    public static final Map<String, CommandScriptContainer> commandScripts = new HashMap<>();
    public static boolean hasCommandInformation = true;
    public static CommandScriptHelper instance;
    public static boolean isInitialized = false;

    public CommandScriptHelper() {
        instance = this;
        if (Settings.cache_commandScriptAutoInit) {
            init();
        }
    }

    public static void init() {
        if (isInitialized) {
            return;
        }
        isInitialized = true;
        try {
            Server server = Bukkit.getServer();

            server.getPluginManager().registerEvents(instance, Denizen.getInstance());

            // Get the CommandMap for the server
            Field commandMapField = server.getClass().getDeclaredField("commandMap");
            commandMapField.setAccessible(true);
            CommandMap commandMap = (CommandMap) commandMapField.get(server);

            // Get the knownCommands for the server's CommandMap
            Field kcf = null;
            Class commandMapClass = commandMap.getClass();
            while (kcf == null && commandMapClass != Object.class) {
                try {
                    kcf = commandMapClass.getDeclaredField("knownCommands");
                }
                catch (NoSuchFieldException e) {
                    commandMapClass = commandMapClass.getSuperclass();
                }
            }
            final Field knownCommandsField = kcf;
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
            if (Settings.overrideHelp()) {
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        if (knownCommands.get("help") instanceof HelpCommand) {
                            return;
                        }
                        knownCommands.put("help", knownCommands.get("bukkit:help"));
                        helpTopics.put("/help", helpTopics.get("/bukkit:help"));
                    }
                }.runTaskLater(Denizen.getInstance(), 1);
            }
        }
        catch (Exception e) {
            Debug.echoError("Error getting the server's command information! Are you running a non-CraftBukkit server?");
            Debug.echoError("Command scripts will not function!");
            //dB.echoError(e);
            hasCommandInformation = false;
        }
    }

    @EventHandler
    public void scriptReload(ScriptReloadEvent event) {
        for (CommandScriptContainer script : commandScripts.values()) {
            if (script.shouldEnable()) {
                registerDenizenCommand(new DenizenCommand(script));
            }
        }
        syncDenizenCommands();
    }

    public static final Method syncCommandsMethod;

    static {
        Method syncMethod = null;
        try {
            syncMethod = Bukkit.getServer().getClass().getDeclaredMethod("syncCommands");
            syncMethod.setAccessible(true);
        }
        catch (Exception e) {
            Debug.echoError("Failed to load helper to synchronize server commands.");
        }
        syncCommandsMethod = syncMethod;
    }

    /**
     * In 1.13+, commands are also sent to players client-side via packets.
     * We need to sync them for tab completion to work.
     */
    public static void syncDenizenCommands() {
        if (syncCommandsMethod != null) {
            try {
                syncCommandsMethod.invoke(Bukkit.getServer());
            }
            catch (Exception e) {
                Debug.echoError("Failed to synchronize server commands.");
            }
        }
    }

    /**
     * Removes all registered {@link DenizenCommand DenizenCommands} from CraftBukkit and restores any
     * overridden Commands.
     *
     * @see #registerDenizenCommand(DenizenCommand)
     */
    public static void removeDenizenCommands() {
        if (!hasCommandInformation || !isInitialized) {
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
                        Denizen.getInstance().getServer().getHelpMap()));
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
    public static void forceCommand(String name, DenizenCommand command, HelpTopic helpTopic) {
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
        public boolean test(HelpTopic topic) {
            return apply(topic);
        }

        public boolean apply(HelpTopic topic) {
            return topic.getName().charAt(0) == '/';
        }
    }
}
