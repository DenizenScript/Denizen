package net.aufdemrand.denizen.scripts.commands.core;

import net.aufdemrand.denizen.Denizen;
import net.aufdemrand.denizen.exceptions.CommandExecutionException;
import net.aufdemrand.denizen.exceptions.InvalidArgumentsException;
import net.aufdemrand.denizen.scripts.ScriptEntry;
import net.aufdemrand.denizen.scripts.commands.AbstractCommand;
import net.aufdemrand.denizen.scripts.containers.core.InteractScriptHelper;
import net.aufdemrand.denizen.utilities.DenizenAPI;
import net.aufdemrand.denizen.utilities.arguments.Duration;
import net.aufdemrand.denizen.utilities.arguments.Script;
import net.aufdemrand.denizen.utilities.arguments.aH;
import net.aufdemrand.denizen.utilities.debugging.dB;
import net.aufdemrand.denizen.utilities.debugging.dB.Messages;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
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

public class YamlCommand extends AbstractCommand implements Listener{

    Map<String, YamlConfiguration> yamls = new HashMap<String, YamlConfiguration>();

    enum Action{ LOAD, CREATE, READ, WRITE, SAVE }

    @Override
    public void parseArgs(ScriptEntry scriptEntry) throws InvalidArgumentsException {

        Action action = null;
        String key = null;
        String value = null;
        String filename = null;
        String id = null;

        for (String arg : scriptEntry.getArguments()) {

            if (aH.matchesValueArg("LOAD, CREATE", arg, aH.ArgumentType.Custom)) {
                action = Action.valueOf(arg.split(":")[0].toUpperCase());
                filename = aH.getStringFrom(arg);
            }

            else if (aH.matchesArg("SAVE", arg))
                action = Action.SAVE;

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

        switch (action) {

            case LOAD:
                YamlConfiguration yamlConfiguration = YamlConfiguration.loadConfiguration(
                        new File(DenizenAPI.getCurrentInstance().getDataFolder(), filename));
                if (yamlConfiguration != null)
                    yamls.put(filename.toUpperCase(), yamlConfiguration);
                break;

            case SAVE:
                if (yamls.containsKey(filename.toUpperCase())) {
                    try {
                        yamls.get(filename.toUpperCase()).save(new File(DenizenAPI.getCurrentInstance().getDataFolder(), filename));
                    } catch (IOException e) {
                        e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                    }
                }
                break;

            case WRITE:
                if (yamls.containsKey(filename.toUpperCase()))
                    yamls.get(filename.toUpperCase()).set(key, value);
                break;

            case READ:
                if (yamls.containsKey(filename.toUpperCase()))
                    yamls.get(filename.toUpperCase()).getString(key);
                break;

            case CREATE:

                break;


        }


    }

}
