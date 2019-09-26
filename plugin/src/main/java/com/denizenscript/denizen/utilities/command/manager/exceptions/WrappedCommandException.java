package com.denizenscript.denizen.utilities.command.manager.exceptions;

public class WrappedCommandException extends CommandException {

    public WrappedCommandException(Throwable t) {
        super(t);
    }
}
