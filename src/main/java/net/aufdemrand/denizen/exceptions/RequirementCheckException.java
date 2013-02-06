package net.aufdemrand.denizen.exceptions;

import net.aufdemrand.denizen.utilities.debugging.dB.Messages;

public class RequirementCheckException extends Exception {

	private static final long serialVersionUID = 3159123423217792068L;

	public RequirementCheckException(String msg) {
		super(msg);
	}
	
	public RequirementCheckException(Messages msg, String arg) {
		super(String.format(msg.toString(), arg));
	}
	
	public RequirementCheckException(Messages msg) {
		super(msg.toString());
	}
}
