package net.aufdemrand.denizen.scripts.commands.player;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import net.aufdemrand.denizen.exceptions.CommandExecutionException;
import net.aufdemrand.denizen.exceptions.InvalidArgumentsException;
import net.aufdemrand.denizen.objects.Element;
import net.aufdemrand.denizen.objects.dScript;
import net.aufdemrand.denizen.scripts.ScriptEntry;
import net.aufdemrand.denizen.scripts.commands.AbstractCommand;
import net.aufdemrand.denizen.objects.aH;
import net.aufdemrand.denizen.utilities.debugging.dB;

/**
 * Initiates/finishes/cancels a 'quest listener'.
 *
 * @author Jeremy Schroeder
 */

public class ListenCommand extends AbstractCommand {

	/* LISTEN can be used several ways:
	 * 
	 * Issuing a new quest listener:
	 * LISTEN Listener_Type ID:ListenerID [Listener Arguments]
	 *   See documentation for Listener_Types and specific arguments for each type.
	 *   
	 * Canceling a quest listener in progress:
	 * LISTEN CANCEL ID:ListenerID
	 *   Cancels a listener.
	 *   
	 * Force-finishing a listener in progress:
	 * LISTEN FINISH ID:ListenerID
	 *   Force finishes a listener.. the outcome is exactly the same as the Player
	 *   completing the listener.
	 *   
	 * Remember: A PLAYER:player_name argument can always be used to specify a
	 *   specific player if necessary.
	 */

	/* 
	 * Arguments: [] - Required, () - Optional
	 * 
	 * [Listener_Type] The name of the type of listener. Only required when
	 *   issuing a new listener. See documentation for available types.
	 *   
	 * [ID] The unique name/id of the listener. This should be unique to the
	 *   player since it is used with replaceable tag data and cancelling/force
	 *   finishing.
	 * 
	 */

    private enum Action { NEW, CANCEL, FINISH }

    @Override
    public void parseArgs(ScriptEntry scriptEntry) throws InvalidArgumentsException {

        List<aH.Argument> arguments = new ArrayList<aH.Argument>();

        // - listen ({new}/cancel/finish) [<type>] [<id:name>]
        //   [<args>...]

        for (aH.Argument arg : aH.interpret(scriptEntry.getArguments())) {

            // <action>
            if (!scriptEntry.hasObject("action")
                    && arg.matchesEnum(Action.values()))
                scriptEntry.addObject("action", arg.asElement());

                // <type>
            else if (!scriptEntry.hasObject("type"))
                scriptEntry.addObject("type", arg.asElement());

                // <id:name>
            else if (!scriptEntry.hasObject("id")
                    && arg.matchesPrefix("id, i"))
                scriptEntry.addObject("id", arg.asElement());

                // <script>
            else if (!scriptEntry.hasObject("finish_script")
                    && arg.matchesPrefix("script")
                    && arg.matchesArgumentType(dScript.class))
                scriptEntry.addObject("finish_script", arg.asType(dScript.class));

            arguments.add(arg);
        }

        // Set defaults
        scriptEntry.defaultObject("action", new Element("new"));
        scriptEntry.defaultObject("id", UUID.randomUUID().toString());

        // Check for type (if using NEW) -- it's required
        if (!scriptEntry.hasObject("type")
                && scriptEntry.getElement("action").asString().equalsIgnoreCase("new"))
            throw new InvalidArgumentsException("Must specify a listener type!");

        if (scriptEntry.getPlayer() == null)
            throw new InvalidArgumentsException("Must specify a player!");

        // Add arguments
        scriptEntry.addObject("args", arguments);

    }

    @SuppressWarnings("unchecked")
    @Override
    public void execute(ScriptEntry scriptEntry) throws CommandExecutionException {
        Element action = scriptEntry.getElement("action");
        Element type = scriptEntry.getElement("type");
        Element id = scriptEntry.getElement("id");
        dScript finish_script = (dScript) scriptEntry.getObject("finish_script");

        dB.report(getName(), action.debug() + type.debug()
                + id.debug() + (finish_script != null ? finish_script.debug() : ""));

        List<aH.Argument> arguments = (ArrayList<aH.Argument>) scriptEntry.getObject("args");

        switch (Action.valueOf(action.asString().toUpperCase())) {

            case NEW:
                // First make sure there isn't already a 'quest listener' for this player with the specified ID.
                if (denizen.getListenerRegistry()
                        .getListenersFor(scriptEntry.getPlayer()) != null
                        && denizen.getListenerRegistry().getListenersFor(scriptEntry.getPlayer())
                        .containsKey(id.asString().toLowerCase())) {
                    dB.echoError("Cancelled creation of NEW listener! Listener ID '" + id.asString() + "' already exists!");
                    break;
                }

                try {
                    denizen.getListenerRegistry().get(type.asString())
                            .createInstance(scriptEntry.getPlayer(), id.asString())
                            .build(scriptEntry.getPlayer(),
                                    id.asString(),
                                    type.asString(),
                                    arguments,
                                    finish_script,
                                    scriptEntry.getNPC());

                } catch (Exception e) {
                    dB.echoDebug("Cancelled creation of NEW listener!");
                    e.printStackTrace();
                    try { denizen.getListenerRegistry().getListenerFor(scriptEntry.getPlayer(), id.asString()).cancel(); }
                    catch (Exception ex) { }
                }
                break;

            case FINISH:
                if (denizen.getListenerRegistry()
                        .getListenerFor(scriptEntry.getPlayer(), id.asString()) != null)
                    denizen.getListenerRegistry()
                            .getListenerFor(scriptEntry.getPlayer(), id.asString()).finish();
                break;

            case CANCEL:
                if (scriptEntry.getPlayer() != null) {
                    if (denizen.getListenerRegistry()
                            .getListenerFor(scriptEntry.getPlayer(), id.asString()) != null)
                        denizen.getListenerRegistry()
                                .getListenerFor(scriptEntry.getPlayer(), id.asString()).cancel();
                    break;
                }
                else
                    denizen.getSaves().set("Listeners." + scriptEntry.getPlayer().getName() + "." + id, null);
        }
    }



}