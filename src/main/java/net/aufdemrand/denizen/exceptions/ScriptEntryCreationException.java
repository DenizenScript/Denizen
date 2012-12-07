package net.aufdemrand.denizen.exceptions;

import net.aufdemrand.denizen.utilities.debugging.Debugger.Messages;

public class ScriptEntryCreationException extends Exception {

	private static final long serialVersionUID = 3159123423457792068L;

	public ScriptEntryCreationException(String msg) {
		super(msg);
	}
	
	public ScriptEntryCreationException(Messages msg, String arg) {
		super(String.format(msg.toString(), arg));
	}
	
	public ScriptEntryCreationException(Messages msg) {
		super(msg.toString());
	}
}
