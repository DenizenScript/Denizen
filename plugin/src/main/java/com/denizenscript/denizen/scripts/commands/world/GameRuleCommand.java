package com.denizenscript.denizen.scripts.commands.world;

import com.denizenscript.denizen.objects.WorldTag;
import com.denizenscript.denizen.utilities.world.GameRuleReflect;
import com.denizenscript.denizencore.exceptions.InvalidArgumentsException;
import com.denizenscript.denizencore.objects.Argument;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.scripts.ScriptEntry;
import com.denizenscript.denizencore.scripts.commands.AbstractCommand;
import com.denizenscript.denizencore.utilities.debugging.Debug;
import com.denizenscript.denizencore.utilities.debugging.DebugInternals;
import org.bukkit.Bukkit;
import org.bukkit.GameRule;
import org.bukkit.generator.WorldInfo;

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
    //
    // @Tags
    // <WorldTag.gamerule[<gamerule>]>
    //
    // @Usage
    // Use to disable fire spreading in world "Adventure".
    // - gamerule Adventure fire_spread_radius_around_player 0
    //
    // @Usage
    // Use to avoid mobs from destroying blocks (creepers, endermen...) and picking items up (zombies, skeletons...) in world "Adventure".
    // - gamerule Adventure mob_griefing false
    // -->

    @Override
    public void addCustomTabCompletions(TabCompletionsBuilder tab) {
        for (GameRule<?> gameRule : GameRuleReflect.values()) {
            tab.add(GameRuleReflect.getName(gameRule));
        }
        tab.add(Bukkit.getWorlds().stream().map(WorldInfo::getName).toList());
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
        ElementTag gameRuleInput = scriptEntry.getElement("gamerule");
        ElementTag valueInput = scriptEntry.getElement("value");
        if (scriptEntry.dbCallShouldDebug()) {
            Debug.report(scriptEntry, getName(), world, gameRuleInput, valueInput);
        }
        GameRule gameRule = GameRuleReflect.getByName(gameRuleInput.asString());
        if (gameRule == null) {
            Debug.echoError("Invalid game rule specified: " + gameRuleInput.asString() + '.');
            return;
        }
        Class<?> gameRuleType = GameRuleReflect.getType(gameRule);
        Object convertedValue;
        if (gameRuleType == Integer.class) {
            if (!valueInput.isInt()) {
                Debug.echoError("Invalid value specified: must be a number.");
                return;
            }
            convertedValue = valueInput.asInt();
        }
        else if (gameRuleType == Boolean.class) {
            if (!valueInput.isBoolean()) {
                Debug.echoError("Invalid value specified: must be a boolean.");
                return;
            }
            convertedValue = valueInput.asBoolean();
        }
        else {
            Debug.echoError("Unrecognized game rule type '" + DebugInternals.getFullClassNameOpti(gameRuleType) + "'! Please report this to the developers.");
            return;
        }
        if (!world.getWorld().setGameRule(gameRule, convertedValue)) {
            Debug.echoError(scriptEntry, "Invalid gamerule!");
        }
    }
}
