package com.denizenscript.denizen.utilities.command.manager.exceptions;

public class NoPermissionsException extends CommandException {

    public NoPermissionsException() {
        super("You don't have permission to execute that command.");
    }
}
