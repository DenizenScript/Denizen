package net.aufdemrand.denizen.scripts.commands.player;

import net.aufdemrand.denizen.BukkitScriptEntryData;
import org.bukkit.entity.Player;

import net.aufdemrand.denizencore.exceptions.CommandExecutionException;
import net.aufdemrand.denizencore.exceptions.InvalidArgumentsException;
import net.aufdemrand.denizencore.scripts.ScriptEntry;
import net.aufdemrand.denizencore.scripts.commands.AbstractCommand;
import net.aufdemrand.denizencore.objects.aH;
import net.aufdemrand.denizen.utilities.debugging.dB;


public class ExperienceCommand extends AbstractCommand {

    private enum Type { SET, GIVE, TAKE }

    /**
     * with help from author: alkarin
     * https://github.com/alkarinv/BattleArena/blob/master/src/mc/alk/arena/util/ExpUtil.java
     */
    public static int getTotalExperience(Player p) {
        return getTotalExperience(p.getLevel(), p.getExp());
    }

    public static int getTotalExperience(int level, double bar) {
        return getTotalExpToLevel(level) + (int) (getExpToLevel(level + 1) * bar + 0.5);
    }

    public static int getExpToLevel(int level) {
        if (level < 16) {
            return 17;
        }
        else if (level < 31) {
            return 3 * level - 31;
        }
        else {
            return 7 * level - 155;
        }
    }

    public static int getTotalExpToLevel(int level) {
        if (level < 16) {
            return 17 * level;
        }
        else if (level < 31) {
            return (int) (1.5 * level * level - 29.5 * level + 360 );
        }
        else {
            return (int) (3.5 * level * level - 151.5 * level + 2220);
        }
    }

    public static void resetExperience(Player player) {
        player.setTotalExperience(0);
        player.setLevel(0);
        player.setExp(0);
    }

    public static void setTotalExperience(Player player, int exp) {
        resetExperience(player);

        if (exp > 0)
            player.giveExp(exp);
    }

    public static void setLevel(Player player, int level) {
        resetExperience(player);

        if (level > 0)
            player.giveExp(getExpToLevel(level));
    }

    public static void giveExperience(Player player, int exp) {
        final int currentExp = getTotalExperience(player);
        resetExperience(player);
        final int newexp = currentExp + exp;

        if (newexp > 0)
            player.giveExp(newexp);
    }

    /* Tail recursive way to count the level for the given exp, maybe better with iteration */
    public static int countLevel(int exp, int toLevel, int level) {
        if (exp < toLevel) {
            return level;
        }
        else {
            return countLevel(exp - toLevel, getTotalExpToLevel(level + 2) - getTotalExpToLevel(level + 1), ++level);
        }
    }

    /* Setting the new level and exp using the setExp and setLevel methods, should be soundless (not yet tested) */
    public static void setSilentTotalExperience(Player player, int exp) {
        resetExperience(player);

        if (exp > 0) {
            final int level = countLevel(exp, 17, 0);
            player.setLevel(level);
            final int expToLvl = exp - getTotalExpToLevel(level);
            player.setExp(expToLvl < 0 ? 0 : expToLvl / getExpToLevel(level + 1));
        }
    }

    /* Adding experience using the setExp and setLevel methods, should be soundless (not tested) */
    public static void giveSilentExperience(Player player, int exp) {
        final int currentExp = getTotalExperience(player);
        resetExperience(player);
        final int newexp = currentExp + exp;

        if (newexp > 0) {
            final int level = countLevel(newexp, 17, 0);
            player.setLevel(level);
            final int expToLvl = newexp - getTotalExpToLevel(level);
            player.setExp(expToLvl < 0 ? 0.0f : (float) expToLvl / (float) getExpToLevel(level + 1));
        }
    }

    @Override
    public void parseArgs(ScriptEntry scriptEntry) throws InvalidArgumentsException {
        // TODO: UPDATE COMMAND PARSING
        int amount = 0;
        Type type = Type.SET;
        boolean level = false;
        boolean silent = false;

        for (aH.Argument arg : aH.interpret(scriptEntry.getArguments())) {

            if (arg.matchesPrimitive(aH.PrimitiveType.Integer)) {
                amount = arg.asElement().asInt();
            }

            else if (arg.matches("SET", "GIVE", "TAKE"))
                type = Type.valueOf(arg.asElement().asString().toUpperCase());

            else if(arg.matches("LEVEL"))
                level = true;

            else if(arg.matches("SILENT"))
                silent = true;

            else
                arg.reportUnhandled();
        }

        scriptEntry.addObject("quantity", amount)
                .addObject("type", type)
                .addObject("level", level)
                .addObject("silent", silent);

    }


    @Override
    public void execute(ScriptEntry scriptEntry) throws CommandExecutionException {

        Type type = (Type) scriptEntry.getObject("type");
        Integer quantity = (Integer) scriptEntry.getObject("quantity");
        Boolean level = (Boolean) scriptEntry.getObject("level");
        Boolean silent = (Boolean) scriptEntry.getObject("silent");

        dB.report(scriptEntry, name, aH.debugObj("Type", type.toString())
            + aH.debugObj("Quantity", level ? quantity.toString() + " levels" : quantity.toString())
            + aH.debugObj("Player", ((BukkitScriptEntryData)scriptEntry.entryData).getPlayer().getName()));

        Player player = ((BukkitScriptEntryData)scriptEntry.entryData).getPlayer().getPlayerEntity();

        switch (type) {
            case SET:
                if(level)
                    ((BukkitScriptEntryData)scriptEntry.entryData).getPlayer().setLevel(quantity);
                else if ( !silent )
                    setTotalExperience(player, quantity);
                else
                    setSilentTotalExperience(player, quantity);
                break;

            case GIVE:
                if(level)
                    ((BukkitScriptEntryData)scriptEntry.entryData).getPlayer().setLevel(((BukkitScriptEntryData)scriptEntry.entryData).getPlayer().getPlayerEntity().getLevel() + quantity);
                else if ( !silent )
                    giveExperience(player, quantity);
                else
                    giveSilentExperience(player, quantity);
                break;

            case TAKE:
                if(level) {
                    int value = ((BukkitScriptEntryData)scriptEntry.entryData).getPlayer().getPlayerEntity().getLevel() - quantity;
                    ((BukkitScriptEntryData)scriptEntry.entryData).getPlayer().setLevel(value <= 0 ? 0: value);
                }
                else if ( !silent )
                    giveExperience(player, -quantity);
                else
                    giveSilentExperience(player, -quantity);
                break;
        }

    }
}
