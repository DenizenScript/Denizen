package net.aufdemrand.denizen.scripts.commands.world;

import org.bukkit.Location;
import org.bukkit.Sound;

import net.aufdemrand.denizen.exceptions.CommandExecutionException;
import net.aufdemrand.denizen.exceptions.InvalidArgumentsException;
import net.aufdemrand.denizen.scripts.ScriptEntry;
import net.aufdemrand.denizen.scripts.commands.AbstractCommand;
import net.aufdemrand.denizen.objects.aH;
import net.aufdemrand.denizen.objects.aH.ArgumentType;
import net.aufdemrand.denizen.utilities.debugging.dB;

public class AnimateChestCommand extends AbstractCommand {
    
    enum ChestAction { OPEN, CLOSE }
    
    @Override
    public void parseArgs(ScriptEntry scriptEntry)
            throws InvalidArgumentsException {
        String chestAction = "OPEN";
        Location location = null;
        Boolean sound = true;
        
        for (String arg : scriptEntry.getArguments()) {
            if (aH.matchesArg("OPEN, CLOSE", arg)) {
                chestAction = aH.getStringFrom(arg);
                dB.echoDebug("...chest action set: " + chestAction);
            } else if (aH.matchesLocation(arg)) {
                location = aH.getLocationFrom(arg);
                dB.echoDebug("...location set");
            } else if (aH.matchesValueArg("SOUND", arg, ArgumentType.Custom)) {
                sound = aH.getBooleanFrom(arg);
                if (sound) dB.echoDebug("...sound enabled");
                else dB.echoDebug("...sound disabled");
            } else throw new InvalidArgumentsException(dB.Messages.ERROR_UNKNOWN_ARGUMENT, arg);

        }
        
        if (location == null) dB.echoError("...location is invalid");
        
        scriptEntry.addObject("location", location)
            .addObject("sound", sound)
            .addObject("chestAction", chestAction);
    }

    @Override
    public void execute(ScriptEntry scriptEntry)
            throws CommandExecutionException {
        Location location = (Location) scriptEntry.getObject("location");
        ChestAction action = ChestAction.valueOf(((String) scriptEntry.getObject("chestAction")).toUpperCase());
        Boolean sound = (Boolean) scriptEntry.getObject("sound");
        
        switch (action) {
        case OPEN:
            if (sound) scriptEntry.getPlayer().getPlayerEntity().playSound(location, Sound.CHEST_OPEN, 1, 1);
            scriptEntry.getPlayer().getPlayerEntity().playNote(location, (byte)1, (byte)1);
            dB.echoDebug("...opening chest");
            break;
            
        case CLOSE:
            if (sound) scriptEntry.getPlayer().getPlayerEntity().getWorld().playSound(location, Sound.CHEST_CLOSE, 1, 1);
            scriptEntry.getPlayer().getPlayerEntity().playNote(location, (byte)1, (byte)0);
            dB.echoDebug("...closing chest");
            break;
            
        default:
            dB.echoError("...error animating chest");
            break;
        }
    }

}
