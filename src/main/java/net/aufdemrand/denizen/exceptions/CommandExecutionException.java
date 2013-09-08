package net.aufdemrand.denizen.exceptions;

import net.aufdemrand.denizen.utilities.debugging.dB.Messages;

public class CommandExecutionException extends Exception {

    private static final long serialVersionUID = 3159123423457792068L;

    public CommandExecutionException(String msg) {
        super(msg);
    }

    public CommandExecutionException(Messages msg, String arg) {
        super(String.format(msg.toString(), arg));
    }

    public CommandExecutionException(Messages msg) {
        super(msg.toString());
    }
}
