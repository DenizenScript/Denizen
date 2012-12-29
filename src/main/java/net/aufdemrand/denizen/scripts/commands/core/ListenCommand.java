package net.aufdemrand.denizen.scripts.commands.core;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.entity.Player;

import net.aufdemrand.denizen.exceptions.CommandExecutionException;
import net.aufdemrand.denizen.exceptions.InvalidArgumentsException;
import net.aufdemrand.denizen.listeners.AbstractListener;
import net.aufdemrand.denizen.scripts.ScriptEntry;
import net.aufdemrand.denizen.scripts.commands.AbstractCommand;
import net.aufdemrand.denizen.scripts.helpers.ArgumentHelper.ArgumentType;
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

	Player player;
	AbstractListener listener;
	String id;
	ListenAction listenAction;
	String listenerType;
	List<String> listenerArguments;
	String script;

	@Override
	public void parseArgs(ScriptEntry scriptEntry) throws InvalidArgumentsException {

		listenAction = ListenAction.NEW;
		id = null;
		listenerType = null;

		// Set some defaults based on the scriptEntry
		player = scriptEntry.getPlayer();
		listenerArguments = new ArrayList<String>();

		// Parse Arguments
		for (String arg : scriptEntry.getArguments()) {

			if (aH.matchesArg("CANCEL", arg)) {
				listenAction = ListenAction.CANCEL;
				dB.echoDebug("...marked to CANCEL.");
				continue;

			}   else if (aH.matchesScript(arg)) {
				script = aH.getStringFrom(arg);
				dB.echoDebug(Messages.DEBUG_SET_SCRIPT, arg);
				continue;

			}   else if (aH.matchesArg("FINISH", arg)) {
				listenAction = ListenAction.FINISH;
				dB.echoDebug("...marked to FINISH.");
				continue;

			}   else if (aH.matchesValueArg("ID", arg, ArgumentType.String)) {
				id = aH.getStringFrom(arg);
				dB.echoDebug("...ID set: '%s'", id);
				continue;

			}   else if (denizen.getListenerRegistry().get(arg) != null) {
				listenerType = arg;
				dB.echoDebug("...TYPE set: '%s'", listenerType);
				continue;

			}	else listenerArguments.add(arg);
		}

		if (id == null) 
			if (player == null) throw new InvalidArgumentsException(Messages.ERROR_NO_PLAYER);
	}

	@Override
	public void execute(ScriptEntry scriptEntry) throws CommandExecutionException {
		switch (listenAction) {

		case NEW:
			try { 
				denizen.getListenerRegistry().get(listenerType).createInstance(player, id)
				.build(player, id, listenerType, listenerArguments, script);
			} catch (Exception e) { 
				dB.echoDebug("Cancelled creation of NEW listener!");
				try { denizen.getListenerRegistry().getListenerFor(player, id).cancel(); }
				catch (Exception ex) { }
			}
			break;

		case FINISH:
			if (denizen.getListenerRegistry().getListenerFor(player, id) != null)
				denizen.getListenerRegistry().getListenerFor(player, id).finish();
			break;

		case CANCEL:
			if (denizen.getListenerRegistry().getListenerFor(player, id) != null)
				denizen.getListenerRegistry().getListenerFor(player, id).cancel();
			break;
		}
	}

	@Override
	public void onEnable() {
		// Nothing to do here.    
	}
}