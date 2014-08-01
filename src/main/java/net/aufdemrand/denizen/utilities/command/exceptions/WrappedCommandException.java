package net.aufdemrand.denizen.utilities.command.exceptions;

public class WrappedCommandException extends CommandException {

    public WrappedCommandException(Throwable t) {
        super(t);
    }
}
