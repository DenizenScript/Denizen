package com.denizenscript.denizen.scripts.commands.world;

import com.denizenscript.denizencore.exceptions.InvalidArgumentsRuntimeException;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.scripts.ScriptEntry;
import com.denizenscript.denizencore.scripts.commands.AbstractCommand;
import com.denizenscript.denizencore.scripts.commands.generator.*;
import org.bukkit.Bukkit;
import org.bukkit.ServerTickManager;

public class TickCommand extends AbstractCommand {

    // <--[command]
    // @Name tick
    // @Syntax tick [rate/step/sprint/freeze/reset] (amount:<amount>) (cancel)
    // @Required 1
    // @Maximum 2
    // @Short Controls the server's tick rate.
    // @Group world
    //
    // @Description
    // Controls the server's tick rate. Versions 1.20+ only.
    //
    // To change the tick rate, use the 'rate' argument and input the amount using 'amount'. The tick rate must be a number between 1.0 and 10000.0.
    // To reset the tick rate to the normal value (20.0), use the 'reset' argument.
    //
    // To freeze the tick rate, use the 'freeze' argument. To unfreeze, add the 'cancel' argument.
    // To step the tick rate while the server is frozen for a specific amount of time, use the 'step' argument and input the amount of ticks using 'amount'.
    // If the server is not frozen when trying to use the 'step' argument, nothing will happen.
    //
    // To make the tick rate as fast as possible for a specific amount of time, use the 'sprint' argument and input the amount of ticks using 'amount'
    // To stop stepping or sprinting early, use the 'cancel' argument.
    //
    // For information about tick rate arguments, see <@link url https://minecraft.wiki/w/Commands/tick>
    //
    // @Usage
    // Use to set the tick rate to 30 ticks per second.
    // - tick rate amount:30
    //
    // @Usage
    // Use to step the tick rate for 1000 ticks.
    // - tick step amount:1000
    //
    // @Usage
    // Use to stop stepping early.
    // - tick step cancel
    // -->

    public TickCommand() {
        setName("tick");
        setSyntax("tick [rate/step/sprint/freeze/reset] (amount:<amount>) (cancel)");
        setRequiredArguments(1, 2);
        isProcedural = false;
        autoCompile();
        addRemappedPrefixes("amount", "a");
    }

    public enum TickActions { RATE, STEP, SPRINT, FREEZE, RESET }

    public static void autoExecute(ScriptEntry scriptEntry,
                                   @ArgName("action") @ArgLinear TickActions action,
                                   @ArgName("amount") @ArgPrefixed @ArgDefaultNull ElementTag amount,
                                   @ArgName("cancel") boolean cancel) {
        ServerTickManager tickManager = Bukkit.getServerTickManager();
        switch (action) {
            case RATE -> {
                if (amount == null) {
                    throw new InvalidArgumentsRuntimeException("The rate action must have a decimal input!");
                }
                if (amount.isFloat()) {
                    if (amount.asFloat() < 1 || amount.asFloat() > 10000) {
                        throw new InvalidArgumentsRuntimeException("Invalid input! Tick rate must be a decimal between 1.0 and 10000.0 (inclusive)!");
                    }
                    tickManager.setTickRate(amount.asFloat());
                }
            }
            case STEP -> {
                if (cancel) {
                    tickManager.stopStepping();
                    return;
                }
                if (amount == null) {
                    throw new InvalidArgumentsRuntimeException("The step action must have an integer input!");
                }
                if (amount.isInt()) {
                    tickManager.stepGameIfFrozen(amount.asInt());
                }
                else {
                    throw new InvalidArgumentsRuntimeException("Invalid input! Step amount must be an integer!");
                }
            }
            case SPRINT -> {
                if (cancel) {
                    tickManager.stopSprinting();
                    return;
                }
                if (amount == null) {
                    throw new InvalidArgumentsRuntimeException("The sprint action must have an integer input!");
                }
                if (amount.isInt()) {
                    tickManager.requestGameToSprint(amount.asInt());
                }
                else {
                    throw new InvalidArgumentsRuntimeException("Invalid input! Sprint amount must be an integer!");
                }
            }
            case FREEZE -> tickManager.setFrozen(!cancel);
            case RESET -> tickManager.setTickRate(20);
        }
    }
}
