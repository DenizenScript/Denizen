package net.aufdemrand.denizen.scripts.commands.player;

import org.bukkit.entity.Player;

import net.aufdemrand.denizen.exceptions.CommandExecutionException;
import net.aufdemrand.denizen.exceptions.InvalidArgumentsException;
import net.aufdemrand.denizen.scripts.ScriptEntry;
import net.aufdemrand.denizen.scripts.commands.AbstractCommand;
import net.aufdemrand.denizen.objects.aH;
import net.aufdemrand.denizen.utilities.debugging.dB;
import net.aufdemrand.denizen.utilities.debugging.dB.Messages;


public class ExperienceCommand extends AbstractCommand {

    private enum Type { SET, GIVE, TAKE }
    
    /**
     * @author alkarin
     * https://github.com/alkarinv/BattleArena/blob/master/src/mc/alk/arena/util/ExpUtil.java
     */
	public static int getTotalExperience(Player p){return getTotalExperience(p.getLevel(),p.getExp());}
	public static int getTotalExperience(int level, double bar){return getTotalExpToLevel(level) + (int) (getExpToLevel(level+1)*bar);}
	public static int getExpToLevel(int level) {if (level < 16){return 17;} else if (level < 31){return 3*level - 31;}else {return 7*level - 155;}}
	public static int getTotalExpToLevel(int level){if (level < 16){return 17*level;} else if (level < 31){	return (int) (1.5*level*level -29.5*level+360 );} else {return (int) (3.5*level*level-151.5*level+2220);}}
	public static void setTotalExperience(Player player, int exp){player.setTotalExperience(0);player.setLevel(0);player.setExp(0);if (exp > 0)player.giveExp(exp);}
	public static void setLevel(Player player, int level){player.setTotalExperience(0);player.setLevel(0);player.setExp(0);if (level > 0)player.giveExp(getExpToLevel(level));}
	public static void giveExperience(Player player, int exp){final int currentExp = getTotalExperience(player);player.setTotalExperience(0);player.setLevel(0);player.setExp(0);final int newexp = currentExp + exp;if (newexp > 0)player.giveExp(newexp);}
	
    @Override
    public void parseArgs(ScriptEntry scriptEntry) throws InvalidArgumentsException {

        int amount = 0;
        Type type = Type.SET;
        boolean level = false;

        for (String arg : scriptEntry.getArguments()) {

            if (aH.matchesQuantity(arg) || aH.matchesInteger(arg)) {
                amount = aH.getIntegerFrom(arg);
            }

            else if (aH.matchesArg("SET, GIVE, TAKE", arg))
                type = Type.valueOf(arg.toUpperCase());

            else if(aH.matchesArg("LEVEL", arg))
                level = true;

            else throw new InvalidArgumentsException(Messages.ERROR_UNKNOWN_ARGUMENT, arg);
        }

        scriptEntry.addObject("quantity", amount)
                .addObject("type", type)
                .addObject("level", level);

    }

    
    @Override
    public void execute(ScriptEntry scriptEntry) throws CommandExecutionException {

        Type type = (Type) scriptEntry.getObject("type");
        Integer quantity = (Integer) scriptEntry.getObject("quantity");
        Boolean level = (Boolean) scriptEntry.getObject("level");

        dB.report(name, aH.debugObj("Type", type.toString())
            + aH.debugObj("Quantity", level ? quantity.toString() + " levels" : quantity.toString())
            + aH.debugObj("Player", scriptEntry.getPlayer().getName()));

        Player player = scriptEntry.getPlayer().getPlayerEntity();

        switch (type) {
            case SET:
                if(level)
                    setLevel(player, quantity);
                else
                	setTotalExperience(player, quantity);
                break;

            case GIVE:
                if(level)
                    setLevel(player, player.getLevel() + quantity);
                else
                    giveExperience(player, quantity);
                break;

            case TAKE:
                if(level)
                    setLevel(player, player.getLevel() - quantity);
                else
                    giveExperience(player, -quantity);
                break;
        }

    }
}
