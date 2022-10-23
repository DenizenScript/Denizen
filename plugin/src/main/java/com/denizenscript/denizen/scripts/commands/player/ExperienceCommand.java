package com.denizenscript.denizen.scripts.commands.player;

import com.denizenscript.denizen.objects.PlayerTag;
import com.denizenscript.denizen.utilities.Utilities;
import com.denizenscript.denizencore.exceptions.InvalidArgumentsRuntimeException;
import com.denizenscript.denizencore.scripts.commands.generator.ArgLinear;
import com.denizenscript.denizencore.scripts.commands.generator.ArgName;
import com.denizenscript.denizencore.scripts.ScriptEntry;
import com.denizenscript.denizencore.scripts.commands.AbstractCommand;
import com.denizenscript.denizencore.utilities.debugging.Debug;

public class ExperienceCommand extends AbstractCommand {

    public ExperienceCommand() {
        setName("experience");
        setSyntax("experience [set/give/take] (level) [<#>]");
        setRequiredArguments(2, 3);
        isProcedural = false;
        autoCompile();
    }

    // <--[command]
    // @Name Experience
    // @Syntax experience [set/give/take] (level) [<#>]
    // @Required 2
    // @Maximum 3
    // @Short Gives or takes experience points to the player.
    // @Group player
    //
    // @Description
    // This command allows modification of a players experience points.
    //
    // Experience can be modified in terms of XP points, or by levels.
    //
    // This command works with offline players, but using it on online players is safer.
    //
    // @Tags
    // <PlayerTag.xp>
    // <PlayerTag.xp_to_next_level>
    // <PlayerTag.xp_total>
    // <PlayerTag.xp_level>
    //
    // @Usage
    // Use to set a player's total experience to 0.
    // - experience set 0
    //
    // @Usage
    // Use to give a player 1 level.
    // - experience give level 1
    //
    // @Usage
    // Use to take 1 level from a player.
    // - experience take level 1
    //
    // @Usage
    // Use to give a player with the name steve 10 experience points.
    // - experience give 10 player:<[someplayer]>
    // -->

    public enum Type {SET, GIVE, TAKE}

    public static int XP_FOR_NEXT_LEVEL(int level) {
        return level >= 30 ? 112 + (level - 30) * 9 : (level >= 15 ? 37 + (level - 15) * 5 : 7 + level * 2);
    }

    public static long TOTAL_XP_FOR_LEVEL(int level) {
        long count = 0;
        for (int i = 0; i < level; i++) {
            count += XP_FOR_NEXT_LEVEL(i);
        }
        return count;
    }

    public static void setTotalExperience(PlayerTag player, int exp) {
        player.setTotalExperience(0);
        player.setLevel(0);
        player.setExp(0);
        giveExperiencePoints(player, exp);
    }

    public static void giveExperiencePoints(PlayerTag player, int amount) {
        if (player.isOnline()) {
            player.getPlayerEntity().giveExp(amount);
            return;
        }
        int level = player.getLevel();
        float xp = player.getExp() + (float)amount / (float)XP_FOR_NEXT_LEVEL(level);
        while(xp >= 1.0F) {
            xp = (xp - 1.0F) * (float)XP_FOR_NEXT_LEVEL(level);
            level++;
            xp /= (float)XP_FOR_NEXT_LEVEL(level);
        }
        player.setTotalExperience(Math.min(player.getTotalExperience() + amount, Integer.MAX_VALUE));
        player.setExp(xp);
        player.setLevel(level);
    }

    public static void takeExperience(PlayerTag player, int toTake) {
        int pastLevelStart = (int) (player.getExp() * XP_FOR_NEXT_LEVEL(player.getLevel()));
        while (toTake >= pastLevelStart) {
            toTake -= pastLevelStart;
            player.setExp(0);
            if (player.getLevel() == 0) {
                return;
            }
            player.setLevel(player.getLevel() - 1);
            pastLevelStart = XP_FOR_NEXT_LEVEL(player.getLevel());
        }
        int newAmount = pastLevelStart - toTake;
        player.setExp(newAmount / (float) XP_FOR_NEXT_LEVEL(player.getLevel()));
    }

    public static void autoExecute(ScriptEntry scriptEntry,
                                   @ArgName("type") Type type,
                                   @ArgName("level") boolean level,
                                   @ArgLinear @ArgName("quantity") int quantity) {
        PlayerTag player = Utilities.getEntryPlayer(scriptEntry);
        if (player == null) {
            throw new InvalidArgumentsRuntimeException("The Experience command requires a linked player.");
        }
        if (quantity < 0) {
            switch (type) {
                case SET:
                    Debug.echoError("Cannot set negative experience.");
                    return;
                case GIVE:
                    quantity = -quantity;
                    type = Type.TAKE;
                    break;
                case TAKE:
                    quantity = -quantity;
                    type = Type.GIVE;
                    break;
            }
        }
        switch (type) {
            case SET:
                if (level) {
                    player.setLevel(quantity);
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
                    giveExperiencePoints(player, quantity);
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
