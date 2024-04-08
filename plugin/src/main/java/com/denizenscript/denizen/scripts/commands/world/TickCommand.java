package com.denizenscript.denizen.scripts.commands.world;

import com.denizenscript.denizencore.exceptions.InvalidArgumentsRuntimeException;
import com.denizenscript.denizencore.scripts.commands.AbstractCommand;
import com.denizenscript.denizencore.scripts.commands.generator.*;
import org.bukkit.Bukkit;
import org.bukkit.ServerTickManager;

public class TickCommand extends AbstractCommand {

    // <--[command]
    // @Name tick
    // @Syntax tick [rate/step/sprint/freeze/reset] (amount:<#.#>/cancel)
    // @Required 1
    // @Maximum 2
    // @Short Controls the server's tick rate.
    // @Group world
    //
    // @Description
    // Controls the server's tick rate. MC versions 1.20+ only.
    //
    // The tick rate affects things such as entity movement (including player movement), entity animations, plant growth, etc.
    // Be careful when setting the tick rate, as a high tick rate can overload the server.
    // If tick rate is high, crops will grow faster, animations will speed up, and so forth.
    // Conversely, if the tick rate is low, crops will grow slower, animations will slow down, and entity movement (including player movement) will slow down and appear to be in slow motion.
    // For a list of all the things that the tick rate affects, see <@link url https://minecraft.wiki/w/Tick>
    //
    // To change the tick rate, use the 'rate' argument and input the amount using 'amount'. The tick rate must be a number between 1.0 and 10000.0 (inclusive).
    // To reset the tick rate to the normal value (20.0), use the 'reset' argument.
    //
    // To freeze the tick rate, use the 'freeze' argument. To unfreeze, add the 'cancel' argument.
    // Freezing the tick rate will cause all entities (except for players and entities that a player is riding) to stop ticking.
    // This means that entity movement and animations will stop, as well as stop things like crop growth, daylight cycle, etc.
    //
    // To step the tick rate while the server is frozen for a specific amount of time, use the 'step' argument and input the amount of ticks using 'amount'.
    // Tick rate stepping is allowing the tick rate to resume for a specific amount of ticks while the server is frozen.
    // After the amount of specified ticks have passed, the tick rate will refreeze.
    // Step input should not be a number less than one.
    // If the server is not frozen when trying to use the 'step' argument, nothing will happen.
    //
    // To make the tick rate as fast as possible for a specific amount of time, use the 'sprint' argument and input the amount of ticks using 'amount'.
    // The tick rate will increase as much as possible without overloading the server for the amount of specified ticks.
    // Sprint input should be not be a number less than one.
    // For example, if you want to sprint 200 ticks, the tick rate will run 200 ticks as fast as possible and then return to the previous tick rate.
    // To stop stepping or sprinting early, use the 'cancel' argument.
    //
    // The tick rate resets to 20.0 on server restart.
    // For information about tick rate arguments, see <@link url https://minecraft.wiki/w/Commands/tick>
    //
    // @Warning Be careful, this command will affect plugins that depend on tick rate and may cause them to break. For example, setting the tick rate to 1 will cause the <@link event tick> event to fire once per second.
    //
    // @Usage
    // Use to set the tick rate to 30 ticks per second.
    // - tick rate amount:30
    //
    // @Usage
    // Use to stop stepping early if the server is currently stepping.
    // - tick step cancel
    //
    // @Usage
    // Use to reset tick rate.
    // - tick reset
    //
    // @Usage
    // Use to freeze the server.
    // - tick freeze
    //
    // @Usage
    // Use to unfreeze the server.
    // - tick freeze cancel
    // -->

    public TickCommand() {
        setName("tick");
        setSyntax("tick [rate/step/sprint/freeze/reset] (amount:<#.#>/cancel)");
        setRequiredArguments(1, 2);
        isProcedural = false;
        autoCompile();
    }

    public enum TickActions { RATE, STEP, SPRINT, FREEZE, RESET }

    public static void autoExecute(@ArgName("action") TickActions action,
                                   @ArgName("amount") @ArgPrefixed @ArgDefaultText("0") float amount,
                                   @ArgName("cancel") boolean cancel) {
        ServerTickManager tickManager = Bukkit.getServerTickManager();
        switch (action) {
            case RATE -> {
                if (amount < 1 || amount > 10000) {
                    throw new InvalidArgumentsRuntimeException("Invalid input! Tick rate must be a decimal number between 1.0 and 10000.0 (inclusive)!");
                }
                tickManager.setTickRate(amount);
            }
            case STEP -> {
                if (cancel) {
                    tickManager.stopStepping();
                    return;
                }
                if (amount < 1) {
                    throw new InvalidArgumentsRuntimeException("The step action must have a number input not less than 1!");
                }
                tickManager.stepGameIfFrozen((int)amount);
            }
            case SPRINT -> {
                if (cancel) {
                    tickManager.stopSprinting();
                    return;
                }
                if (amount < 1) {
                    throw new InvalidArgumentsRuntimeException("The sprint action must have a number input not less than 1!");
                }
                tickManager.requestGameToSprint((int)amount);
            }
            case FREEZE -> tickManager.setFrozen(!cancel);
            case RESET -> tickManager.setTickRate(20);
        }
    }
}
