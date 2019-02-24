package net.aufdemrand.denizen.scripts.commands.player;

import net.aufdemrand.denizen.BukkitScriptEntryData;
import net.aufdemrand.denizen.utilities.debugging.dB;
import net.aufdemrand.denizencore.exceptions.InvalidArgumentsException;
import net.aufdemrand.denizencore.objects.aH;
import net.aufdemrand.denizencore.scripts.ScriptEntry;
import net.aufdemrand.denizencore.scripts.commands.AbstractCommand;
import org.bukkit.entity.Player;


public class ExperienceCommand extends AbstractCommand {

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

        for (aH.Argument arg : aH.interpret(scriptEntry.getArguments())) {

            if (arg.matchesPrimitive(aH.PrimitiveType.Integer)) {
                amount = arg.asElement().asInt();
            }
            else if (arg.matches("SET", "GIVE", "TAKE")) {
                type = Type.valueOf(arg.asElement().asString().toUpperCase());
            }
            else if (arg.matches("LEVEL")) {
                level = true;
            }
            else if (arg.matches("SILENT")) {
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

            dB.report(scriptEntry, name, aH.debugObj("Type", type.toString())
                    + aH.debugObj("Quantity", level ? quantity + " levels" : quantity)
                    + aH.debugObj("Player", ((BukkitScriptEntryData) scriptEntry.entryData).getPlayer().getName()));

        }

        Player player = ((BukkitScriptEntryData) scriptEntry.entryData).getPlayer().getPlayerEntity();

        switch (type) {
            case SET:
                if (level) {
                    ((BukkitScriptEntryData) scriptEntry.entryData).getPlayer().setLevel(quantity);
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
