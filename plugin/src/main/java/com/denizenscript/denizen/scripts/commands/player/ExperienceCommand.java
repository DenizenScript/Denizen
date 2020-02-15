package com.denizenscript.denizen.scripts.commands.player;

import com.denizenscript.denizen.utilities.Utilities;
import com.denizenscript.denizen.utilities.debugging.Debug;
import com.denizenscript.denizencore.exceptions.InvalidArgumentsException;
import com.denizenscript.denizencore.objects.Argument;
import com.denizenscript.denizencore.objects.ArgumentHelper;
import com.denizenscript.denizencore.scripts.ScriptEntry;
import com.denizenscript.denizencore.scripts.commands.AbstractCommand;
import org.bukkit.entity.Player;

public class ExperienceCommand extends AbstractCommand {

    // <--[command]
    // @Name Experience
    // @Syntax experience [{set}/give/take] (level) [<#>]
    // @Required 2
    // @Short Gives or takes experience points to the player.
    // @Group player
    //
    // @Description
    // This command allows modification of a players experience points.
    // Experience can be modified in terms of XP points, or by levels.
    // Note that the "set" command does not affect levels, but xp bar fullness.
    // (E.g. setting experience to 0 will not change a players level, but will
    // set the players experience bar to 0)
    //
    // @Tags
    // <PlayerTag.xp>
    // <PlayerTag.xp_to_next_level>
    // <PlayerTag.xp_total>
    // <PlayerTag.xp_level>
    //
    // @Usage
    // Use to set a player's experience bar to 0.
    // - experience set 0
    //
    // @Usage
    // Use give give a player 1 level.
    // - experience give level 1
    //
    // @Usage
    // Use to take 1 level from a player.
    //
    // - experience take level 1
    // @Usage
    // Use to give a player with the name steve 10 experience points.
    // - experience give 10 player:<[someplayer]>
    // -->

    private enum Type {SET, GIVE, TAKE}

    public static void setTotalExperience(Player player, int exp) {
        player.setTotalExperience(exp);
    }

    public static void setLevel(Player player, int level) {
        player.setLevel(level);
    }

    public static void giveExperience(Player player, int exp) {
        player.giveExp(exp);
    }

    public static void takeExperience(Player player, int toTake) {
        int pastLevelStart = (int) (player.getExp() * player.getExpToLevel());
        while (toTake >= pastLevelStart) {
            toTake -= pastLevelStart;
            player.setExp(0);
            if (player.getLevel() == 0) {
                return;
            }
            player.setLevel(player.getLevel() - 1);
            pastLevelStart = player.getExpToLevel();
        }
        int newAmount = pastLevelStart - toTake;
        player.setExp(newAmount / (float) player.getExpToLevel());
    }

    @Override
    public void parseArgs(ScriptEntry scriptEntry) throws InvalidArgumentsException {
        int amount = 0;
        Type type = Type.SET;
        boolean level = false;
        boolean silent = false;

        for (Argument arg : scriptEntry.getProcessedArgs()) {

            if (arg.matchesInteger()) {
                amount = arg.asElement().asInt();
            }
            else if (arg.matches("set", "give", "take")) {
                type = Type.valueOf(arg.asElement().asString().toUpperCase());
            }
            else if (arg.matches("level")) {
                level = true;
            }
            else if (arg.matches("silent")) {
                silent = true;
            }
            else {
                arg.reportUnhandled();
            }
        }

        scriptEntry.addObject("quantity", amount)
                .addObject("type", type)
                .addObject("level", level)
                .addObject("silent", silent);

    }

    @Override
    public void execute(ScriptEntry scriptEntry) {

        Type type = (Type) scriptEntry.getObject("type");
        int quantity = (int) scriptEntry.getObject("quantity");
        Boolean level = (Boolean) scriptEntry.getObject("level");
        //Boolean silent = (Boolean) scriptEntry.getObject("silent");

        if (scriptEntry.dbCallShouldDebug()) {

            Debug.report(scriptEntry, name, ArgumentHelper.debugObj("Type", type.toString())
                    + ArgumentHelper.debugObj("Quantity", level ? quantity + " levels" : quantity)
                    + ArgumentHelper.debugObj("Player", Utilities.getEntryPlayer(scriptEntry).getName()));

        }

        Player player = Utilities.getEntryPlayer(scriptEntry).getPlayerEntity();

        switch (type) {
            case SET:
                if (level) {
                    Utilities.getEntryPlayer(scriptEntry).setLevel(quantity);
                }
                else {
                    setTotalExperience(player, quantity);
                }
                break;

            case GIVE:
                if (level) {
                    player.setLevel(player.getLevel() + quantity);
                }
                else {
                    player.giveExp(quantity);
                }
                break;

            case TAKE:
                if (level) {
                    int value = player.getLevel() - quantity;
                    player.setLevel(value <= 0 ? 0 : value);
                }
                else {
                    takeExperience(player, quantity);
                }
                break;
        }

    }
}
