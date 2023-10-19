package com.denizenscript.denizen.scripts.commands.world;

import com.denizenscript.denizencore.utilities.debugging.Debug;
import com.denizenscript.denizen.objects.WorldTag;
import com.denizenscript.denizencore.exceptions.InvalidArgumentsException;
import com.denizenscript.denizencore.objects.Argument;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.scripts.ScriptEntry;
import com.denizenscript.denizencore.scripts.commands.AbstractCommand;
import org.bukkit.Bukkit;
import org.bukkit.generator.WorldInfo;

import java.util.stream.Collectors;

public class GameRuleCommand extends AbstractCommand {

    public GameRuleCommand() {
        setName("gamerule");
        setSyntax("gamerule [<world>] [<rule>] [<value>]");
        setRequiredArguments(3, 3);
        isProcedural = false;
    }

    // <--[command]
    // @Name Gamerule
    // @Syntax gamerule [<world>] [<rule>] [<value>]
    // @Required 3
    // @Maximum 3
    // @Short Sets a gamerule on the world.
    // @Group world
    //
    // @Description
    // Sets a gamerule on the world. A list of valid gamerules can be found here: <@link url https://minecraft.wiki/w/Game_rule>
    // Note: Be careful, gamerules are CASE SENSITIVE.
    //
    // @Tags
    // <WorldTag.gamerule[<gamerule>]>
    //
    // @Usage
    // Use to disable fire spreading in world "Adventure".
    // - gamerule Adventure doFireTick false
    //
    // @Usage
    // Use to avoid mobs from destroying blocks (creepers, endermen...) and picking items up (zombies, skeletons...) in world "Adventure".
    // - gamerule Adventure mobGriefing false
    // -->

    @Override
    public void addCustomTabCompletions(TabCompletionsBuilder tab) {
        tab.add(Bukkit.getWorlds().get(0).getGameRules());
        tab.add(Bukkit.getWorlds().stream().map(WorldInfo::getName).collect(Collectors.toSet()));
    }

    @Override
    public void parseArgs(ScriptEntry scriptEntry) throws InvalidArgumentsException {
        for (Argument arg : scriptEntry) {
            if (!scriptEntry.hasObject("world")
                    && arg.matchesArgumentType(WorldTag.class)) {
                scriptEntry.addObject("world", arg.asType(WorldTag.class));
            }
            else if (!scriptEntry.hasObject("gamerule")) {
                scriptEntry.addObject("gamerule", arg.asElement());
            }
            else if (!scriptEntry.hasObject("value")) {
                scriptEntry.addObject("value", arg.asElement());
            }
            else {
                arg.reportUnhandled();
            }
        }
        if (!scriptEntry.hasObject("world")) {
            throw new InvalidArgumentsException("Must specify a world!");
        }
        if (!scriptEntry.hasObject("gamerule")) {
            throw new InvalidArgumentsException("Must specify a gamerule!");
        }
        if (!scriptEntry.hasObject("value")) {
            throw new InvalidArgumentsException("Must specify a value!");
        }
    }

    @Override
    public void execute(ScriptEntry scriptEntry) {
        WorldTag world = scriptEntry.getObjectTag("world");
        ElementTag gamerule = scriptEntry.getElement("gamerule");
        ElementTag value = scriptEntry.getElement("value");
        if (scriptEntry.dbCallShouldDebug()) {
            Debug.report(scriptEntry, getName(), world, gamerule, value);
        }
        if (!world.getWorld().setGameRuleValue(gamerule.asString(), value.asString())) {
            Debug.echoError(scriptEntry, "Invalid gamerule!");
        }
    }
}
