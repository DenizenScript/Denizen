package net.aufdemrand.denizen.scripts.commands.player;

import net.aufdemrand.denizen.BukkitScriptEntryData;
import net.aufdemrand.denizen.listeners.AbstractListener;
import net.aufdemrand.denizen.utilities.DenizenAPI;
import net.aufdemrand.denizen.utilities.debugging.dB;
import net.aufdemrand.denizencore.exceptions.CommandExecutionException;
import net.aufdemrand.denizencore.exceptions.InvalidArgumentsException;
import net.aufdemrand.denizencore.objects.Element;
import net.aufdemrand.denizencore.objects.aH;
import net.aufdemrand.denizencore.objects.dScript;
import net.aufdemrand.denizencore.scripts.ScriptEntry;
import net.aufdemrand.denizencore.scripts.commands.AbstractCommand;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ListenCommand extends AbstractCommand {


    private enum Action {NEW, CANCEL, FINISH}

    @Override
    public void parseArgs(ScriptEntry scriptEntry) throws InvalidArgumentsException {

        List<aH.Argument> arguments = new ArrayList<aH.Argument>();

        for (aH.Argument arg : aH.interpret(scriptEntry.getArguments())) {

            // <action>
            if (!scriptEntry.hasObject("action")
                    && arg.matchesEnum(Action.values()))
                scriptEntry.addObject("action", arg.asElement());

                // <id:name>
            else if (!scriptEntry.hasObject("id")
                    && arg.matchesPrefix("id", "i"))
                scriptEntry.addObject("id", arg.asElement());

                // <script>
            else if (!scriptEntry.hasObject("finish_script")
                    && arg.matchesPrefix("script")
                    && arg.matchesArgumentType(dScript.class))
                scriptEntry.addObject("finish_script", arg.asType(dScript.class));

                // <type>
            else if (!scriptEntry.hasObject("type"))
                scriptEntry.addObject("type", arg.asElement());

            arguments.add(arg);
        }

        // Set defaults
        scriptEntry.defaultObject("action", new Element("new"));
        scriptEntry.defaultObject("id", new Element(UUID.randomUUID().toString()));

        // Check for type (if using NEW) -- it's required
        if (!scriptEntry.hasObject("type")
                && scriptEntry.getElement("action").asString().equalsIgnoreCase("new"))
            throw new InvalidArgumentsException("Must specify a listener type!");

        // Player listeners require a player!
        if (((BukkitScriptEntryData) scriptEntry.entryData).getPlayer() == null)
            throw new InvalidArgumentsException("Must specify a player!");

        // Add arguments
        scriptEntry.addObject("args", arguments);

    }


    @Override
    public void execute(ScriptEntry scriptEntry) throws CommandExecutionException {
        Element action = scriptEntry.getElement("action");
        Element type = scriptEntry.getElement("type");
        Element id = scriptEntry.getElement("id");
        dScript finish_script = (dScript) scriptEntry.getObject("finish_script");

        dB.report(scriptEntry, getName(), action.debug() + (type != null ? type.debug() : "")
                + id.debug() + (finish_script != null ? finish_script.debug() : ""));

        dB.echoError(scriptEntry.getResidingQueue(),
                "Warning: Listen is outdated and may become unsupported in the future.");

        List<aH.Argument> arguments = (ArrayList<aH.Argument>) scriptEntry.getObject("args");

        switch (Action.valueOf(action.asString().toUpperCase())) {

            case NEW:
                // First make sure there isn't already a 'player listener' for this player with the specified ID.
                if (DenizenAPI.getCurrentInstance().getListenerRegistry()
                        .getListenersFor(((BukkitScriptEntryData) scriptEntry.entryData).getPlayer()) != null
                        && DenizenAPI.getCurrentInstance().getListenerRegistry().getListenersFor(((BukkitScriptEntryData) scriptEntry.entryData).getPlayer())
                        .containsKey(id.asString().toLowerCase())) {
                    dB.echoError(scriptEntry.getResidingQueue(), "Cancelled creation of NEW listener! Listener ID '" + id.asString() + "' already exists!");
                    break;
                }

                // Also make sure there is a valid script input
                if (finish_script == null) {
                    dB.echoError("Must specify a valid script!");
                    break;
                }

                try {
                    DenizenAPI.getCurrentInstance().getListenerRegistry().get(type.asString())
                            .createInstance(((BukkitScriptEntryData) scriptEntry.entryData).getPlayer(), id.asString())
                            .build(((BukkitScriptEntryData) scriptEntry.entryData).getPlayer(),
                                    id.asString(),
                                    type.asString(),
                                    arguments,
                                    finish_script,
                                    ((BukkitScriptEntryData) scriptEntry.entryData).getNPC());

                }
                catch (Exception e) {
                    dB.echoDebug(scriptEntry, "Cancelled creation of NEW listener!");

                    // Why? Maybe a wrong listener type...
                    if (DenizenAPI.getCurrentInstance().getListenerRegistry().get(type.asString()) == null)
                        dB.echoError(scriptEntry.getResidingQueue(), "Invalid listener type!");

                        // Just print the stacktrace if anything else, so we can debug other possible
                        // problems.
                    else
                        dB.echoError(scriptEntry.getResidingQueue(), e);

                    // Deconstruct the listener in case it was partially created while erroring out.
                    try {
                        DenizenAPI.getCurrentInstance().getListenerRegistry().getListenerFor
                                (((BukkitScriptEntryData) scriptEntry.entryData).getPlayer(), id.asString()).cancel();
                    }
                    catch (Exception ex) {
                    }
                }
                break;

            case FINISH:
                if (DenizenAPI.getCurrentInstance().getListenerRegistry()
                        .getListenerFor(((BukkitScriptEntryData) scriptEntry.entryData).getPlayer(), id.asString()) != null)
                    DenizenAPI.getCurrentInstance().getListenerRegistry()
                            .getListenerFor(((BukkitScriptEntryData) scriptEntry.entryData).getPlayer(), id.asString()).finish();
                break;

            case CANCEL:
                if (((BukkitScriptEntryData) scriptEntry.entryData).getPlayer() != null) {
                    if (id != null)
                        if (DenizenAPI.getCurrentInstance().getListenerRegistry()
                                .getListenerFor(((BukkitScriptEntryData) scriptEntry.entryData).getPlayer(), id.asString()) != null)
                            DenizenAPI.getCurrentInstance().getListenerRegistry()
                                    .getListenerFor(((BukkitScriptEntryData) scriptEntry.entryData).getPlayer(), id.asString()).cancel();
                        else
                            for (AbstractListener listener :
                                    DenizenAPI.getCurrentInstance().getListenerRegistry().getListenersFor(((BukkitScriptEntryData) scriptEntry.entryData).getPlayer()).values())
                                listener.cancel();
                }
                else
                    DenizenAPI.getCurrentInstance().getSaves().set("Listeners." + ((BukkitScriptEntryData) scriptEntry.entryData).getPlayer().getName() + "." + id, null);
                break;
        }
    }
}
