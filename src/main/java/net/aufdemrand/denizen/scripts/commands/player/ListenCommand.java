package net.aufdemrand.denizen.scripts.commands.player;

import java.util.ArrayList;
import java.util.List;

import net.aufdemrand.denizen.exceptions.CommandExecutionException;
import net.aufdemrand.denizen.exceptions.InvalidArgumentsException;
import net.aufdemrand.denizen.scripts.ScriptEntry;
import net.aufdemrand.denizen.scripts.commands.AbstractCommand;
import net.aufdemrand.denizen.objects.aH;
import net.aufdemrand.denizen.objects.aH.ArgumentType;
import net.aufdemrand.denizen.utilities.debugging.dB;
import net.aufdemrand.denizen.utilities.debugging.dB.Messages;

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

	private enum ListenAction { NEW, CANCEL, FINISH }

	@Override
	public void parseArgs(ScriptEntry scriptEntry) throws InvalidArgumentsException {

        String id = null;
        ListenAction listenAction = ListenAction.NEW;
        String listenerType = null;
        List<String> listenerArguments;

		// Set some defaults based on the scriptEntry
		listenerArguments = new ArrayList<String>();

		// Parse Arguments
		for (String arg : scriptEntry.getArguments()) {

			if (aH.matchesArg("CANCEL", arg)) {
				listenAction = ListenAction.CANCEL;
				dB.echoDebug("...marked to CANCEL.");

            }   else if (aH.matchesScript(arg)) {
				scriptEntry.setScript(aH.getStringFrom(arg));
				dB.echoDebug(Messages.DEBUG_SET_SCRIPT, arg);

            }   else if (aH.matchesArg("FINISH", arg)) {
				listenAction = ListenAction.FINISH;
				dB.echoDebug("...marked to FINISH.");

            }   else if (aH.matchesValueArg("ID", arg, ArgumentType.String)) {
				id = aH.getStringFrom(arg);
				dB.echoDebug("...ID set: '%s'", id);

            }   else if (denizen.getListenerRegistry().get(arg) != null) {
				listenerType = arg;
				dB.echoDebug("...TYPE set: '%s'", listenerType);

            }	else listenerArguments.add(arg);
		}

		if (id == null) id = scriptEntry.getScript().getName();
		if (scriptEntry.getPlayer() == null )
            throw new InvalidArgumentsException(Messages.ERROR_NO_PLAYER);

        scriptEntry.addObject("action", listenAction);
        scriptEntry.addObject("type", listenerType);
        scriptEntry.addObject("args", listenerArguments);
        scriptEntry.addObject("id", id);
	}

	@SuppressWarnings("unchecked")
	@Override
	public void execute(ScriptEntry scriptEntry) throws CommandExecutionException {
		ListenAction listenAction = (ListenAction) scriptEntry.getObject("action");
        String listenerType = (String) scriptEntry.getObject("type");
        List<String> listenerArguments = (List<String>) scriptEntry.getObject("args");
        String id = (String) scriptEntry.getObject("id");

        switch (listenAction) {

		case NEW:
			try { 
				denizen.getListenerRegistry().get(listenerType).createInstance(scriptEntry.getPlayer(), id)
				.build(scriptEntry.getPlayer(),
                        id,
                        listenerType,
                        listenerArguments,
                        scriptEntry.getScript().getName(),
                        scriptEntry.getNPC());

            } catch (Exception e) {
				dB.echoDebug("Cancelled creation of NEW listener!");
				try { denizen.getListenerRegistry().getListenerFor(scriptEntry.getPlayer(), id).cancel(); }
				catch (Exception ex) { }
			}
			break;

		case FINISH:
			if (denizen.getListenerRegistry().getListenerFor(scriptEntry.getPlayer(), id) != null)
				denizen.getListenerRegistry().getListenerFor(scriptEntry.getPlayer(), id).finish();
			break;

		case CANCEL:
            if (scriptEntry.getPlayer() != null) {
			if (denizen.getListenerRegistry().getListenerFor(scriptEntry.getPlayer(), id) != null)
				denizen.getListenerRegistry().getListenerFor(scriptEntry.getPlayer(), id).cancel();
			break;
            }
            else denizen.getSaves().set("Listeners." + scriptEntry.getPlayer().getName() + "." + id, null);
		}
	}

	@Override
	public void onEnable() {
		// Nothing to do here.    
	}
}