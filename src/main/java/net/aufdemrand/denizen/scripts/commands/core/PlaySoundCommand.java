package net.aufdemrand.denizen.scripts.commands.core;

import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.World;

import net.aufdemrand.denizen.exceptions.CommandExecutionException;
import net.aufdemrand.denizen.exceptions.InvalidArgumentsException;
import net.aufdemrand.denizen.scripts.ScriptEntry;
import net.aufdemrand.denizen.scripts.commands.AbstractCommand;

public class PlaySoundCommand extends AbstractCommand {

	@Override
	public void onEnable() {
		// nothing to do here
	}

	Sound theSound;
	World theWorld;
	Location location;
	
	@Override
	public void parseArgs(ScriptEntry scriptEntry) throws InvalidArgumentsException {
		
		theWorld = scriptEntry.getPlayer().getWorld();
		location = scriptEntry.getPlayer().getLocation();
	}

	@Override
	public void execute(String commandName) throws CommandExecutionException {
		theWorld.playSound(location, theSound, 1, 1);
	}

}
