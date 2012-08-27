package net.aufdemrand.denizen.commands;

import java.rmi.activation.ActivationException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

import net.aufdemrand.denizen.Denizen;
import net.aufdemrand.denizen.commands.core.CastCommand;
import net.aufdemrand.denizen.commands.core.CooldownCommand;
import net.aufdemrand.denizen.commands.core.DropCommand;
import net.aufdemrand.denizen.commands.core.EngageCommand;
import net.aufdemrand.denizen.commands.core.ExecuteCommand;
import net.aufdemrand.denizen.commands.core.FailCommand;
import net.aufdemrand.denizen.commands.core.FeedCommand;
import net.aufdemrand.denizen.commands.core.FinishCommand;
import net.aufdemrand.denizen.commands.core.FlagCommand;
import net.aufdemrand.denizen.commands.core.GiveCommand;
import net.aufdemrand.denizen.commands.core.HealCommand;
import net.aufdemrand.denizen.commands.core.HintCommand;
import net.aufdemrand.denizen.commands.core.LookCommand;
import net.aufdemrand.denizen.commands.core.PauseCommand;
import net.aufdemrand.denizen.commands.core.PermissCommand;
import net.aufdemrand.denizen.commands.core.RefuseCommand;
import net.aufdemrand.denizen.commands.core.ResetCommand;
import net.aufdemrand.denizen.commands.core.SpawnCommand;
import net.aufdemrand.denizen.commands.core.StrikeCommand;
import net.aufdemrand.denizen.commands.core.SwitchCommand;
import net.aufdemrand.denizen.commands.core.TakeCommand;
import net.aufdemrand.denizen.commands.core.TalkCommand;
import net.aufdemrand.denizen.commands.core.TeleportCommand;
import net.aufdemrand.denizen.commands.core.WaitCommand;
import net.aufdemrand.denizen.commands.core.WalkToCommand;
import net.aufdemrand.denizen.commands.core.WeatherCommand;
import net.aufdemrand.denizen.commands.core.ZapCommand;


public class CommandRegistry {

	public Denizen plugin;
	private ArgumentHelper argumentHelper; 
	
	public CommandRegistry(Denizen denizen) {
		plugin = denizen;
		argumentHelper = new ArgumentHelper(plugin);
	}

	
	private Map<String, AbstractCommand> commands = new HashMap<String, AbstractCommand>();
	private Map<Class<? extends AbstractCommand>, String> commandsClass = new HashMap<Class<? extends AbstractCommand>, String>();
	

	
	public ArgumentHelper getArgumentHelper() {
		return argumentHelper;
	}

	public boolean registerCommand(String commandName, AbstractCommand commandClass) {
		this.commands.put(commandName.toUpperCase(), commandClass);
		this.commandsClass.put(commandClass.getClass(), commandName.toUpperCase());
		plugin.getLogger().log(Level.INFO, "Loaded " + commandName + " Command successfully!");
		return true;
	}


	public Map<String, AbstractCommand> listCommands() {
		return commands;
	}

	
	public <T extends AbstractCommand> T getCommand(Class<T> theClass) {
		if (commandsClass.containsKey(theClass))
			return (T) theClass.cast(commands.get(commandsClass.get(theClass)));
		else
			return null;
	}
	
	public AbstractCommand getCommand(String commandName) {
		if (commands.containsKey(commandName.toUpperCase()))
			return commands.get(commandName);
		else
			return null;
	}

	public void registerCoreCommands() {

		PauseCommand pauseCommand = new PauseCommand();
		ZapCommand zapCommand = new ZapCommand();
		EngageCommand engageCommand = new EngageCommand();
		SpawnCommand spawnCommand = new SpawnCommand();
		WaitCommand waitCommand = new WaitCommand();
		LookCommand lookCommand = new LookCommand();
		FailCommand failCommand = new FailCommand();
		FinishCommand finishCommand = new FinishCommand();
		ResetCommand resetCommand = new ResetCommand();
		FlagCommand flagCommand = new FlagCommand();
		StrikeCommand strikeCommand = new StrikeCommand();
		SwitchCommand switchCommand = new SwitchCommand();
		TalkCommand talkCommand = new TalkCommand();
		TeleportCommand teleportCommand = new TeleportCommand();
		ExecuteCommand executeCommand = new ExecuteCommand();
		TakeCommand takeCommand = new TakeCommand();
		GiveCommand giveCommand = new GiveCommand();
		PermissCommand permissCommand = new PermissCommand();
		RefuseCommand refuseCommand = new RefuseCommand();
		DropCommand dropCommand = new DropCommand();
		CooldownCommand cooldownCommand = new CooldownCommand();
		HintCommand hintCommand = new HintCommand();
		WeatherCommand weatherCommand = new WeatherCommand();
		CastCommand castCommand = new CastCommand();
		WalkToCommand walktoCommand = new WalkToCommand();
		HealCommand healCommand = new HealCommand();
		FeedCommand feedCommand = new FeedCommand();
		
		try {
			feedCommand.activateAs("FEED");
			healCommand.activateAs("HARM");
			healCommand.activateAs("HEAL");
			castCommand.activateAs("CAST");
			walktoCommand.activateAs("WALK");
			walktoCommand.activateAs("WALKTO");
			walktoCommand.activateAs("RETURN");			
			hintCommand.activateAs("HINT");
			weatherCommand.activateAs("WEATHER");
			pauseCommand.activateAs("PAUSE");
			pauseCommand.activateAs("RESUME");
			cooldownCommand.activateAs("COOLDOWN");
			dropCommand.activateAs("DROP");
			permissCommand.activateAs("PERMISS");
			refuseCommand.activateAs("REFUSE");
			giveCommand.activateAs("GIVE");
			takeCommand.activateAs("TAKE");
			executeCommand.activateAs("EXECUTE");
			teleportCommand.activateAs("TELEPORT");
			talkCommand.activateAs("CHAT");
			talkCommand.activateAs("WHISPER");
			talkCommand.activateAs("SHOUT");
			talkCommand.activateAs("NARRATE");
			talkCommand.activateAs("EMOTE");
			switchCommand.activateAs("SWITCH");
			zapCommand.activateAs("ZAP");
			engageCommand.activateAs("ENGAGE");
			engageCommand.activateAs("DISENGAGE");
			spawnCommand.activateAs("SPAWN");
			waitCommand.activateAs("WAIT");
			lookCommand.activateAs("LOOK");
			failCommand.activateAs("FAIL");
			finishCommand.activateAs("FINISH");
			resetCommand.activateAs("RESET");
			flagCommand.activateAs("FLAG");	
			strikeCommand.activateAs("STRIKE");
			
		} catch (ActivationException e) {
			plugin.getLogger().log(Level.SEVERE, "Oh no! Denizen has run into a problem registering the core commands!");
			e.printStackTrace();
		}
		
		
	}


}
