package net.aufdemrand.denizen.scripts.commands;

/**
 * Simply used to indicate that a command can be 'held', so we don't wait for
 * commands that will never mark themselves 'finished'.
 *
 */
public interface Holdable {

    // Just so we know! We don't want to wait for commands that can't be 'held up'.
}
