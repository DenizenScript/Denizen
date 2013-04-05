package net.aufdemrand.denizen.scripts.commands.core;

import net.aufdemrand.denizen.Denizen;
import net.aufdemrand.denizen.events.ReplaceableTagEvent;
import net.aufdemrand.denizen.exceptions.CommandExecutionException;
import net.aufdemrand.denizen.exceptions.InvalidArgumentsException;
import net.aufdemrand.denizen.scripts.ScriptEntry;
import net.aufdemrand.denizen.scripts.commands.AbstractCommand;
import net.aufdemrand.denizen.scripts.containers.core.InteractScriptHelper;
import net.aufdemrand.denizen.tags.Attribute;
import net.aufdemrand.denizen.utilities.DenizenAPI;
import net.aufdemrand.denizen.utilities.arguments.Duration;
import net.aufdemrand.denizen.utilities.arguments.Element;
import net.aufdemrand.denizen.utilities.arguments.Script;
import net.aufdemrand.denizen.utilities.arguments.aH;
import net.aufdemrand.denizen.utilities.debugging.dB;
import net.aufdemrand.denizen.utilities.debugging.dB.Messages;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

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

        if (id == null)
            throw new InvalidArgumentsException("Must specify an id!");

        if (action == null)
            throw new InvalidArgumentsException("Must specify an action!");

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

        if (!event.hasNameContext()) return;
        if (!event.hasTypeContext()) return;

        String id = event.getNameContext();
        String path = event.getTypeContext();

        if (!yamls.containsKey(id.toUpperCase())) return;

        Attribute attribute = new Attribute(event.raw_tag, event.getScriptEntry());

        attribute.fulfill(1);

        if (attribute.startsWith("contains")) {
            event.setReplaced(new Element(String.valueOf(getYaml(id).contains(path)))
                    .getAttribute(attribute.fulfill(1)));
        }

        if (attribute.startsWith("read")) {
            event.setReplaced(new Element(getYaml(id).getString(path)).getAttribute(attribute.fulfill(1)));
        }

    }


}
