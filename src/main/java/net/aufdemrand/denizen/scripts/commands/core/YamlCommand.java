package net.aufdemrand.denizen.scripts.commands.core;

import net.aufdemrand.denizen.events.bukkit.ReplaceableTagEvent;
import net.aufdemrand.denizen.exceptions.CommandExecutionException;
import net.aufdemrand.denizen.exceptions.InvalidArgumentsException;
import net.aufdemrand.denizen.scripts.ScriptEntry;
import net.aufdemrand.denizen.scripts.commands.AbstractCommand;
import net.aufdemrand.denizen.tags.Attribute;
import net.aufdemrand.denizen.utilities.DenizenAPI;
import net.aufdemrand.denizen.objects.*;
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
 * - yaml savefile:filename.yml
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

        for (aH.Argument arg : aH.interpret(scriptEntry.getArguments())) {
            if (!scriptEntry.hasObject("action") &&
                    arg.matchesPrefix("LOAD")) {
                scriptEntry.addObject("action", new Element("LOAD"));
                scriptEntry.addObject("filename", arg.asElement());
            }

            else if (!scriptEntry.hasObject("action") &&
                    arg.matchesPrefix("SAVE, SAVEFILE, FILESAVE")) {
                scriptEntry.addObject("action", new Element("SAVE"));
                scriptEntry.addObject("filename", arg.asElement());
            }

            else if (!scriptEntry.hasObject("action") &&
                    arg.matches("CREATE")) {
                scriptEntry.addObject("action", new Element("CREATE"));
            }

            else if (!scriptEntry.hasObject("action") &&
                    arg.matchesPrefix("WRITE")) {
                scriptEntry.addObject("action", new Element("WRITE"));
                scriptEntry.addObject("key", arg.asElement());
            }

            else if (!scriptEntry.hasObject("value") &&
                    arg.matchesPrefix("VALUE")) {
                scriptEntry.addObject("value", arg.asElement());
            }

            else if (!scriptEntry.hasObject("id") &&
                    arg.matchesPrefix("ID")) {
                scriptEntry.addObject("id", arg.asElement());
            }

            else arg.reportUnhandled();
        }

        // Check for required arguments

        if (!scriptEntry.hasObject("id"))
            throw new InvalidArgumentsException("Must specify an id!");

        if (!scriptEntry.hasObject("action"))
            throw new InvalidArgumentsException("Must specify an action!");

        if (!scriptEntry.hasObject("key") &&
                scriptEntry.getElement("action").asString().equalsIgnoreCase("write"))
            throw new InvalidArgumentsException("Must specify a key!");
    }


    @Override
    public void execute(final ScriptEntry scriptEntry) throws CommandExecutionException {

        Element filename = scriptEntry.getElement("filename");
        Element key = scriptEntry.getElement("key");
        Element value = scriptEntry.getElement("value");
        Action action = Action.valueOf(scriptEntry.getElement("action").asString().toUpperCase());
        String id = scriptEntry.getElement("id").asString();

        YamlConfiguration yamlConfiguration;

        // Do action

        switch (action) {

            case LOAD:
                File file = new File(DenizenAPI.getCurrentInstance().getDataFolder(), filename.asString());
                if (!file.exists()) {
                    dB.echoError("File cannot be found!");
                    return;
                }
                yamlConfiguration = YamlConfiguration.loadConfiguration(file);
                if (yamlConfiguration != null)
                    yamls.put(id.toUpperCase(), yamlConfiguration);
                break;

            case SAVE:
                if (yamls.containsKey(id.toUpperCase())) {
                    try {
                        yamls.get(id.toUpperCase()).save(new File(DenizenAPI.getCurrentInstance().getDataFolder(), filename.asString()));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                break;

            case WRITE:
                if (yamls.containsKey(id.toUpperCase()))
                    yamls.get(id.toUpperCase()).set(key.asString(), value.asString());
                break;

            case CREATE:
                yamlConfiguration = new YamlConfiguration();
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

        // <--[tag]
        // @attribute <yaml[<id>].contains[<path>]>
        // @returns Element(Boolean)
        // @description
        // Returns true if the file has the specified path.
        // Otherwise, returns false.
        // -->
        if (attribute.startsWith("contains")) {
            event.setReplaced(new Element(getYaml(id).contains(path))
                    .getAttribute(attribute.fulfill(1)));
            return;
        }

        // <--[tag]
        // @attribute <yaml[<id>].read[<path>]>
        // @returns Element
        // @description
        // Returns the value of the key at the path.
        // -->
        if (attribute.startsWith("read")) {
            String value = getYaml(id).getString(path);
            if (value == null) {
                // If value is null, the key at the specified path didn't exist.
                dB.echoDebug(event.getScriptEntry(), "YAML tag '" + event.raw_tag + "' has returned null.");
                event.setReplaced(new Element("null").getAttribute(attribute.fulfill(1)));
                return;

            }
            else {
                event.setReplaced(new Element(value).getAttribute(attribute.fulfill(1)));
                return;
            }
        }

        // <--[tag]
        // @attribute <yaml[<id>].list_keys[<path>]>
        // @returns dList
        // @description
        // Returns a dList of all the keys at the path.
        // -->
        if (attribute.startsWith("list_keys")) {
            ConfigurationSection section = getYaml(id).getConfigurationSection(path);
            if (section == null) {
                dB.echoDebug(event.getScriptEntry(), "YAML tag '" + event.raw_tag + "' has returned null.");
                event.setReplaced(new Element("null").getAttribute(attribute.fulfill(1)));
                return;
            }
            Set<String> keys = section.getKeys(false);
            if (keys == null) {
                dB.echoDebug(event.getScriptEntry(), "YAML tag '" + event.raw_tag + "' has returned null.");
                event.setReplaced(new Element("null").getAttribute(attribute.fulfill(1)));
                return;

            } else {
                ArrayList<String> list = new ArrayList<String>();
                list.addAll(keys);
                event.setReplaced(new dList(list).getAttribute(attribute.fulfill(1)));
                return;
            }
        }

    }

}
