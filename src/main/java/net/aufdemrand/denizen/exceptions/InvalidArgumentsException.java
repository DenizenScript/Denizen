package net.aufdemrand.denizen.exceptions;

import net.aufdemrand.denizen.utilities.debugging.dB.Messages;

public class InvalidArgumentsException extends Exception {

	private static final long serialVersionUID = 3159108944857792068L;

	public InvalidArgumentsException(String msg) {
		super(msg);
	}
	
	public InvalidArgumentsException(Messages msg, String arg) {
		super(String.format(msg.toString(), arg));
	}
	
	public InvalidArgumentsException(Messages msg) {
		super(msg.toString());
	}
}
