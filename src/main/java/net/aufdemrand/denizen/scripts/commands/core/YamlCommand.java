package net.aufdemrand.denizen.scripts.commands.core;

import net.aufdemrand.denizen.events.ReplaceableTagEvent;
import net.aufdemrand.denizen.exceptions.CommandExecutionException;
import net.aufdemrand.denizen.exceptions.InvalidArgumentsException;
import net.aufdemrand.denizen.scripts.ScriptEntry;
import net.aufdemrand.denizen.scripts.commands.AbstractCommand;
import net.aufdemrand.denizen.tags.Attribute;
import net.aufdemrand.denizen.utilities.DenizenAPI;
import net.aufdemrand.denizen.arguments.*;
import net.aufdemrand.denizen.utilities.debugging.dB;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 *
 * - yaml load:filename.yml    ...
 * - yaml create:filename.yml    ...
 * - yaml read:filename.yml key:yaml-key     ...
 * - yaml write:filename.yml key:yaml-key value:value    ...
 * - yaml save:filename.yml
 *
 */

public class YamlCommand extends AbstractCommand implements Listener {

    @Override
    public void onEnable() {
        Bukkit.getServer().getPluginManager().registerEvents(this, DenizenAPI.getCurrentInstance());
    }

    Map<String, YamlConfiguration> yamls = new HashMap<String, YamlConfiguration>();

    private YamlConfiguration getYaml(String id) {
        if (id == null) return null;
        return yamls.get(id.toUpperCase());
    }

    enum Action{ LOAD, CREATE, READ, WRITE, SAVE }

    @Override
    public void parseArgs(ScriptEntry scriptEntry) throws InvalidArgumentsException {

        Action action = null;
        String key = null;
        String value = null;
        String filename = null;
        String id = null;

        for (String arg : scriptEntry.getArguments()) {

            if (aH.matchesValueArg("LOAD, CREATE, SAVE", arg, aH.ArgumentType.Custom)) {
                action = Action.valueOf(arg.split(":")[0].toUpperCase());
                filename = aH.getStringFrom(arg);
            }

            else if (aH.matchesValueArg("READ, WRITE", arg, aH.ArgumentType.Custom)) {
                action = Action.valueOf(arg.split(":")[0].toUpperCase());
                key = aH.getStringFrom(arg);
            }

            else if (aH.matchesValueArg("VALUE", arg, aH.ArgumentType.Custom)) {
                value = aH.getStringFrom(arg);
            }

            else
                id = aH.getStringFrom(arg);

        }

        // Check for required arguments

        if (id == null)
            throw new InvalidArgumentsException("Must specify an id!");

        if (action == null)
            throw new InvalidArgumentsException("Must specify an action!");

        if ((action == Action.READ || action == Action.WRITE) && key == null)
            throw new InvalidArgumentsException("Must specify a key!");

        if ((action == Action.CREATE || action == Action.LOAD || action == Action.SAVE) && filename == null)
            throw new InvalidArgumentsException("Must specify a filename!");

        // Add objects back to script entry

        scriptEntry.addObject("filename", filename)
                .addObject("action", action)
                .addObject("key", key)
                .addObject("value", value)
                .addObject("id", id);
    }


    @Override
    public void execute(final ScriptEntry scriptEntry) throws CommandExecutionException {

        String filename = (String) scriptEntry.getObject("filename");
        String key = (String) scriptEntry.getObject("key");
        String value = (String) scriptEntry.getObject("value");
        Action action = (Action) scriptEntry.getObject("action");
        String id = (String) scriptEntry.getObject("id");

        YamlConfiguration yamlConfiguration;

        // Do action

        switch (action) {

            case LOAD:
                File file = new File(DenizenAPI.getCurrentInstance().getDataFolder(), filename);
                if (file == null) throw new CommandExecutionException("File cannot be found!");
                yamlConfiguration = YamlConfiguration.loadConfiguration(file);
                if (yamlConfiguration != null)
                    yamls.put(id.toUpperCase(), yamlConfiguration);
                break;

            case SAVE:
                if (yamls.containsKey(id.toUpperCase())) {
                    try {
                        yamls.get(id.toUpperCase()).save(new File(DenizenAPI.getCurrentInstance().getDataFolder(), filename));
                    } catch (IOException e) {
                        e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                    }
                }
                break;

            case WRITE:
                if (yamls.containsKey(id.toUpperCase()))
                    yamls.get(id.toUpperCase()).set(key, value);
                break;

            case CREATE:
                yamlConfiguration = YamlConfiguration.loadConfiguration(
                        new File(DenizenAPI.getCurrentInstance().getDataFolder(), filename));
                if (yamlConfiguration != null)
                    yamls.put(id.toUpperCase(), yamlConfiguration);
                break;
        }

    }


    @EventHandler
    public void yaml(ReplaceableTagEvent event) {

        if (!event.matches("yaml")) return;

        // YAML tag requires name context and type context.
        if (!event.hasNameContext() || !event.hasTypeContext()) {
            dB.echoError("YAML tag '" + event.raw_tag + "' is missing required context. Tag replacement aborted.");
            return;
        }

        // Set id (name context) and path (type context)
        String id = event.getNameContext();
        String path = event.getTypeContext();

        // Check if there is a yaml file loaded with the specified id
        if (!yamls.containsKey(id.toUpperCase())) {
            dB.echoError("YAML tag '" + event.raw_tag + "' has specified an invalid ID, or the specified id has already" +
                    "been closed. Tag replacement aborted.");
            return;
        }

        // Build the attribute to be filled.
        Attribute attribute = new Attribute(event.raw_tag, event.getScriptEntry());

        // Catch up with what has already been processed.
        attribute.fulfill(1);

        //
        // Check attributes
        //

        if (attribute.startsWith("contains")) {
            event.setReplaced(new Element(String.valueOf(getYaml(id).contains(path)))
                    .getAttribute(attribute.fulfill(1)));
            return;
        }

        if (attribute.startsWith("read")) {
            String value = getYaml(id).getString(path);
            if (value == null) {
                // If value is null, the key at the specified path didn't exist.
                dB.echoDebug("YAML tag '" + event.raw_tag + "' has returned null.");
                event.setReplaced("null");
                return;

            } else
                event.setReplaced(new Element(value).getAttribute(attribute.fulfill(1)));
        }

        if (attribute.startsWith("list_keys")) {
            ConfigurationSection section = getYaml(id).getConfigurationSection(path);
            if (section == null) {
                dB.echoDebug("YAML tag '" + event.raw_tag + "' has returned null.");
                event.setReplaced("null");
                return;
            }
            Set<String> keys = section.getKeys(false);
            if (keys == null) {
                dB.echoDebug("YAML tag '" + event.raw_tag + "' has returned null.");
                event.setReplaced("null");
                return;

            } else {
                ArrayList<String> list = new ArrayList<String>();
                list.addAll(keys);
                event.setReplaced(new dList(list).getAttribute(attribute.fulfill(1)));
            }
        }

        // Got this far? Invalid attribute.
        dB.echoError("YAML tag '" + event.raw_tag + "' has an invalid attribute. Tag replacement aborted.");

    }

}
