package com.denizenscript.denizen.scripts.commands.world;

import com.denizenscript.denizencore.exceptions.InvalidArgumentsRuntimeException;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.scripts.ScriptEntry;
import com.denizenscript.denizencore.scripts.commands.AbstractCommand;
import com.denizenscript.denizencore.scripts.commands.generator.*;
import com.denizenscript.denizencore.utilities.debugging.Debug;
import org.bukkit.Bukkit;
import org.bukkit.ServerTickManager;

public class TickCommand extends AbstractCommand {

    // <--[command]
    // @Name tick
    // @Syntax tick [rate:<rate>/step:<amount>/sprint:<amount>/freeze (cancel)/reset]
    // @Required 1
    // @Maximum 3
    // @Short Controls the server's tick rate.
    // @Group world
    //
    // @Description
    // Controls the server's tick rate.
    //
    // To change the tick rate, use the 'rate' argument to change the tick rate. The tick rate must be a number between 1.0 and 10000.0.
    // To reset the tick rate to the normal value (20.0), use the 'reset' argument.
    //
    // To freeze the tick rate, use the 'freeze' argument. To unfreeze, add the 'cancel' argument.
    // To step the tick rate while the server is frozen for a specific amount of time, use the 'step' argument and provide a duration.
    // If the server is not frozen when trying to use the 'step' argument, nothing will happen.
    //
    // To make the tick rate as fast as possible for a specific amount of time, use the 'sprint' argument and provide a duration.
    // To stop stepping or sprinting early, use 'cancel' as the input.
    //
    // For information about tick rate arguments, see <@link url https://minecraft.wiki/w/Commands/tick>
    //
    // @Usage
    // Use to set the tick rate to 30 ticks per second.
    // - tick rate:30
    //
    // @Usage
    // Use to step the tick rate for 1000 ticks.
    // - tick step:1000
    //
    // @Usage
    // Use to stop stepping early.
    // - tick step:cancel
    // -->

    // maybe change the reset to also automatically reset the step or sprint or freeze? or figure out how to do 'step cancel' and have it work with no input
    public TickCommand() {
        setName("tick");
        setSyntax("tick [rate:<rate>/step:<amount>/sprint:<amount>/freeze (cancel)/reset]");
        setRequiredArguments(1, 2);
        isProcedural = false;
        autoCompile();
    }

    public static void autoExecute(ScriptEntry scriptEntry,
                                   @ArgPrefixed @ArgName("rate") @ArgDefaultText("-1") float rate,
                                   @ArgPrefixed @ArgName("step") @ArgDefaultNull ElementTag step,
                                   @ArgPrefixed @ArgName("sprint") @ArgDefaultNull ElementTag sprint,
                                   @ArgName("freeze") boolean freeze,
                                   @ArgName("reset") boolean reset,
                                   @ArgName("cancel") boolean cancel) {
        ServerTickManager tickManager = Bukkit.getServerTickManager();
        if (rate != -1) {
            if (rate < 1 || rate > 10000) {
                Debug.echoError(scriptEntry, "Tick rate must between 1.0 and 10000.0 (inclusive)!");
                return;
            }
            tickManager.setTickRate(rate);
            return;
        }
        if (step != null) {
            if (!step.isInt() && !step.toString().equalsIgnoreCase("cancel")) {
                throw new InvalidArgumentsRuntimeException("Invalid step input! Must be either an integer or 'cancel'!");
            }
            if (step.isInt()) {
                tickManager.stepGameIfFrozen(step.asInt());
            }
            else {
                tickManager.stopStepping();
            }
            return;
        }
        if (sprint != null) {
            if (!sprint.isInt() && !sprint.toString().equalsIgnoreCase("cancel")) {
                throw new InvalidArgumentsRuntimeException("Invalid step input! Must be either an integer or 'cancel'!");
            }
            if (sprint.isInt()) {
                tickManager.requestGameToSprint(sprint.asInt());
            }
            else {
                tickManager.stopSprinting();
            }
            return;
        }
        if (freeze) {
            tickManager.setFrozen(!cancel);
            return;
        }
        if (reset) {
            tickManager.setTickRate(20);
            return;
        }
        throw new InvalidArgumentsRuntimeException("Must specify a tick action!");
    }
}
