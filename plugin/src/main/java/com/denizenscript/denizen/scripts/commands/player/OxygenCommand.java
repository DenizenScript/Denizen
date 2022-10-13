package com.denizenscript.denizen.scripts.commands.player;

import com.denizenscript.denizen.utilities.Utilities;
import com.denizenscript.denizencore.utilities.debugging.Debug;
import com.denizenscript.denizen.objects.PlayerTag;
import com.denizenscript.denizencore.exceptions.InvalidArgumentsException;
import com.denizenscript.denizencore.objects.Argument;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.scripts.ScriptEntry;
import com.denizenscript.denizencore.scripts.commands.AbstractCommand;

public class OxygenCommand extends AbstractCommand {

    public OxygenCommand() {
        setName("oxygen");
        setSyntax("oxygen [<#>] (type:{remaining}/maximum) (mode:{set}/add/remove)");
        setRequiredArguments(1, 3);
        isProcedural = false;
    }

    // <--[command]
    // @Name Oxygen
    // @Syntax oxygen [<#>] (type:{remaining}/maximum) (mode:{set}/add/remove)
    // @Required 1
    // @Maximum 3
    // @Short Gives or takes breath from the player.
    // @Group player
    //
    // @Description
    // Used to add to, remove from or set the amount of current oxygen of a player.
    // Also allows for the changing of the player's maximum oxygen level.
    // Value is in ticks, so 30 equals 1 bubble.
    //
    // @Tags
    // <PlayerTag.oxygen>
    // <PlayerTag.max_oxygen>
    //
    // @Usage
    // Use to set the player's current oxygen level to 5 bubbles.
    // - oxygen 150
    //
    // @Usage
    // Use to add 1 bubble to the player's current oxygen level.
    // - oxygen 30 mode:add
    //
    // @Usage
    // Use to set the player's maximum oxygen level to 20 bubbles.
    // - oxygen 600 type:maximum
    // -->

    public enum Type {MAXIMUM, REMAINING}

    public enum Mode {SET, ADD, REMOVE}

    @Override
    public void parseArgs(ScriptEntry scriptEntry) throws InvalidArgumentsException {
        for (Argument arg : scriptEntry) {
            if (!scriptEntry.hasObject("type")
                    && arg.matchesPrefix("type", "t")
                    && arg.matchesEnum(Type.class)) {
                scriptEntry.addObject("type", arg.asElement());
            }
            else if (!scriptEntry.hasObject("mode")
                    && arg.matchesPrefix("mode", "m")
                    && arg.matchesEnum(Mode.class)) {
                scriptEntry.addObject("mode", arg.asElement());
            }
            else if (!scriptEntry.hasObject("amount")
                    && arg.matchesInteger()) {
                scriptEntry.addObject("amount", arg.asElement());
            }
        }
        if (!Utilities.entryHasPlayer(scriptEntry) || !Utilities.getEntryPlayer(scriptEntry).isValid()) {
            throw new InvalidArgumentsException("Must have player context!");
        }
        if (!scriptEntry.hasObject("amount")) {
            throw new InvalidArgumentsException("Must specify a valid amount!");
        }
        scriptEntry.defaultObject("type", new ElementTag("REMAINING")).defaultObject("mode", new ElementTag("SET"));

    }

    @Override
    public void execute(ScriptEntry scriptEntry) {
        ElementTag type = scriptEntry.getElement("type");
        ElementTag mode = scriptEntry.getElement("mode");
        ElementTag amount = scriptEntry.getElement("amount");
        if (scriptEntry.dbCallShouldDebug()) {
            Debug.report(scriptEntry, getName(), type, mode, amount);
        }
        PlayerTag player = Utilities.getEntryPlayer(scriptEntry);
        switch (Type.valueOf(type.asString().toUpperCase())) {
            case MAXIMUM:
                switch (Mode.valueOf(mode.asString().toUpperCase())) {
                    case SET:
                        player.setMaximumAir(amount.asInt());
                        break;
                    case ADD:
                        player.setMaximumAir(player.getMaximumAir() + amount.asInt());
                        break;
                    case REMOVE:
                        player.setMaximumAir(player.getMaximumAir() - amount.asInt());
                        break;
                }
                break;
            case REMAINING:
                switch (Mode.valueOf(mode.asString().toUpperCase())) {
                    case SET:
                        player.setRemainingAir(amount.asInt());
                        break;
                    case ADD:
                        player.setRemainingAir(player.getRemainingAir() + amount.asInt());
                        break;
                    case REMOVE:
                        player.setRemainingAir(player.getRemainingAir() - amount.asInt());
                        break;
                }
                break;
        }
    }
}
