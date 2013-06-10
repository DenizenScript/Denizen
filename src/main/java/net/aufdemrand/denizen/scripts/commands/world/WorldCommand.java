package net.aufdemrand.denizen.scripts.commands.world;

import net.aufdemrand.denizen.exceptions.CommandExecutionException;
import net.aufdemrand.denizen.exceptions.InvalidArgumentsException;
import net.aufdemrand.denizen.scripts.ScriptEntry;
import net.aufdemrand.denizen.scripts.commands.AbstractCommand;
import net.aufdemrand.denizen.objects.aH;
import net.aufdemrand.denizen.utilities.debugging.dB;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

/**
 * Various world based commands.
 * Such as: weather and times
 * 
 * @author spaceemotion
 */
public class WorldCommand extends AbstractCommand {

    private enum Action { WEATHER, TIME }
    
    private enum SubAction {
        // Time actions
        DAY, NIGHT, DUSK, DAWN,
        
        // Weather actions
        THUNDERING, STORM, SUNNY, THUNDERSTORM
    }
    
    private enum Type { GLOBAL, PLAYER } // TODO: Player type will come when Bukkit PR gets accepted

    @Override
    public void parseArgs(ScriptEntry scriptEntry) throws InvalidArgumentsException {
        Type type = Type.GLOBAL;
        Action action = null;
        SubAction sub = null;
        Player player = scriptEntry.getPlayer().getPlayerEntity();
        
        for (String arg : scriptEntry.getArguments()) {
            if (aH.matchesArg("player", arg)) {
                player = Bukkit.getServer().getPlayer(aH.getStringFrom("player"));
                type = Type.PLAYER;
                
            } else if (aH.matchesArg("weather", arg)) {
                action = Action.WEATHER;
                sub = SubAction.valueOf(aH.getStringFrom("weather"));
                if(sub == null) throw new InvalidArgumentsException("Invalid sub action for WEATHER!");

            } else if (aH.matchesArg("time", arg)) {
                action = Action.TIME;
                sub = SubAction.valueOf(aH.getStringFrom("time"));
                if(sub == null) throw new InvalidArgumentsException("Invalid sub action for TIME!");

            } else throw new InvalidArgumentsException(dB.Messages.ERROR_UNKNOWN_ARGUMENT, arg);
        }

        if (type == null)
            throw new InvalidArgumentsException("Must specify an action! Valid: WEATHER, TIME");

        
        scriptEntry.addObject("type", type);
        scriptEntry.addObject("action", action);
        scriptEntry.addObject("subaction", sub);
        scriptEntry.addObject("player", player);
    }
    
    @Override
    public void execute(ScriptEntry scriptEntry) throws CommandExecutionException {
        Type type = (Type) scriptEntry.getObject("time");
        Action action = (Action) scriptEntry.getObject("action");
        SubAction sub = (SubAction) scriptEntry.getObject("subaction");
        Player player = (Player) scriptEntry.getObject("player");
        
        dB.report(getName(),
            aH.debugObj("Type", type.name())
          + aH.debugObj("Action", action.name())
          + aH.debugObj("Sub-Action", sub.name())
          + ((type == Type.PLAYER) ? aH.debugObj("Player", player.getName()) : ""));
        
        switch(action) {
            case WEATHER:
                switch(sub) {
                    case SUNNY:
                        player.getWorld().setThundering(false);
                        player.getWorld().setStorm(false);
                        break;
                        
                    case THUNDERSTORM:
                        player.getWorld().setThundering(true);
                        
                    case STORM:
                        player.getWorld().setStorm(true);
                        break;
                        
                    case THUNDERING:
                        player.getWorld().setThundering(true);
                }
                
                break;
                
            case TIME:
                switch(sub) {
                    case DAY:
                        player.getWorld().setTime(0);
                        break;
                    
                    case NIGHT:
                        player.getWorld().setTime(13500);
                        break;
                    
                    case DUSK:
                        player.getWorld().setTime(12500);
                        break;
                    
                    case DAWN:
                        player.getWorld().setTime(23000);
                }
        }
    }
    
}
