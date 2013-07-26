package net.aufdemrand.denizen.scripts.commands.player;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import net.aufdemrand.denizen.exceptions.CommandExecutionException;
import net.aufdemrand.denizen.exceptions.InvalidArgumentsException;
import net.aufdemrand.denizen.objects.aH;
import net.aufdemrand.denizen.objects.aH.ArgumentType;
import net.aufdemrand.denizen.objects.dEntity;
import net.aufdemrand.denizen.scripts.ScriptEntry;
import net.aufdemrand.denizen.scripts.ScriptRegistry;
import net.aufdemrand.denizen.scripts.commands.AbstractCommand;
import net.aufdemrand.denizen.scripts.containers.core.FormatScriptContainer;
import net.aufdemrand.denizen.utilities.debugging.dB;
import net.aufdemrand.denizen.utilities.debugging.dB.Messages;

/**
 * Sends a message to the Player.
 * 
 * @author Jeremy Schroeder
 * Version 1.0 Last Updated 11/29 1:11
 */

public class NarrateCommand extends AbstractCommand {

    @Override
    public void parseArgs(ScriptEntry scriptEntry) throws InvalidArgumentsException {
    	
        String text = null;
        FormatScriptContainer format = null;
        List<Player> targets = new ArrayList<Player>();

        if (scriptEntry.getArguments().size() > 4) 
            throw new InvalidArgumentsException(Messages.ERROR_LOTS_OF_ARGUMENTS);

        // Iterate through arguments
        for (String arg : scriptEntry.getArguments()) {
            if (aH.matchesValueArg("FORMAT", arg, aH.ArgumentType.String)) {
                String formatStr = aH.getStringFrom(arg);
                format = ScriptRegistry.getScriptContainerAs(formatStr, FormatScriptContainer.class);
                
                if(format != null) dB.echoDebug("... format set to: " + formatStr);
                else dB.echoError("... could not find format for: " + formatStr);
                
            }
            // Add players to target list
            else if (aH.matchesValueArg("target, targets", arg, ArgumentType.Custom)) {
            	
                Entity entity = null;

                for (String target : aH.getListFrom(arg)) {
                	
                	entity = dEntity.valueOf(target).getBukkitEntity();
                	
                	if (entity != null && entity instanceof Player) {
                		
                		targets.add((Player) entity);
                	}
            		else {
            			dB.echoError("Invalid target '%s'!", target);
            		}
                }
			}
            else {
                text = arg;
            }
        }
        
		// If there are no targets, check if you can add this player
        // to the targets
        if (targets.size() == 0) {

        	if (scriptEntry.getPlayer() == null || !scriptEntry.getPlayer().isOnline()) {
        		throw new InvalidArgumentsException(Messages.ERROR_NO_PLAYER);
        	}
        	else {
        		targets.add(scriptEntry.getPlayer().getPlayerEntity());
        	}
        }
        
        
        if (text == null) throw new InvalidArgumentsException(Messages.ERROR_NO_TEXT);

        scriptEntry.addObject("text", text)
            	   .addObject("format", format)
            	   .addObject("targets", targets);;
    }

    @SuppressWarnings("unchecked")
	@Override
    public void execute(ScriptEntry scriptEntry) throws CommandExecutionException {
        // Get objects
		List<Player> targets = (List<Player>) scriptEntry.getObject("targets");
        String text = (String) scriptEntry.getObject("text");
        FormatScriptContainer format = (FormatScriptContainer) scriptEntry.getObject("format");

        // Report to dB
        dB.report(getName(),
                 aH.debugObj("Narrating", text)
                 + aH.debugObj("Targets", targets)
                 + (format != null ? aH.debugObj("Format", format.getName()) : ""));
        
        for (Player player : targets) {
        	player.sendMessage(format != null ? format.getFormattedText(scriptEntry) : text);
        }
    }

}