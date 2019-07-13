package com.denizenscript.denizen.utilities.command.exceptions;

public class CommandException extends Exception {

    public CommandException() {
        super();
    }

    public CommandException(String message) {
        super(message);
    }

    public CommandException(Throwable t) {
        super(t);
    }
}
