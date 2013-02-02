package net.aufdemrand.denizen.scripts.commands;

import net.aufdemrand.denizen.Denizen;
import net.aufdemrand.denizen.interfaces.DenizenRegistry;
import net.aufdemrand.denizen.interfaces.RegistrationableInstance;
import net.aufdemrand.denizen.scripts.commands.core.*;
import net.aufdemrand.denizen.utilities.debugging.dB;

import java.util.HashMap;
import java.util.Map;

public class CommandRegistry implements DenizenRegistry {

	public Denizen denizen;

	public CommandRegistry(Denizen denizen) {
		this.denizen = denizen;
	}

	private Map<String, AbstractCommand> instances = new HashMap<String, AbstractCommand>();
	private Map<Class<? extends AbstractCommand>, String> classes = new HashMap<Class<? extends AbstractCommand>, String>();

	@Override
	public boolean register(String commandName, RegistrationableInstance commandInstance) {
		this.instances.put(commandName.toUpperCase(), (AbstractCommand) commandInstance);
		this.classes.put(((AbstractCommand) commandInstance).getClass(), commandName.toUpperCase());
		return true;
	}

	@Override
	public Map<String, AbstractCommand> list() {
		return instances;
	}

	@Override
	public AbstractCommand get(String commandName) {
		if (instances.containsKey(commandName.toUpperCase())) return instances.get(commandName.toUpperCase());
		else return null;
	}

	@Override
	public <T extends RegistrationableInstance> T get(Class<T> clazz) {
		if (classes.containsKey(clazz)) return (T) clazz.cast(instances.get(classes.get(clazz)));
		else return null;
	}

	@Override
	public void registerCoreMembers() {

        String announceHint = "announce ['announcement text']";
        new AnnounceCommand().activate().as("ANNOUNCE").withOptions(announceHint, 1);

        String vulnerableHint = "vulnerable (toggle:{true}|false|toggle)";
        new VulnerableCommand().activate().as("VULNERABLE").withOptions(vulnerableHint, 0);
        
        String scribeHint = "scribe [script:book_script] (give|{drop}|equip) (location:x,y,z,world) OR scribe [item:id.name] [script:book_script]";
        new ScribeCommand().activate().as("SCRIBE").withOptions(scribeHint, 1);
        
        String runTaskCommand = "runtask (id:id_name{script_name}) [script:script_name] (instant|queue:queue_type{player_task}) (delay:#{0})";
        new RuntaskCommand().activate().as("RUNTASK").withOptions(runTaskCommand, 1);
        
        String castHint = "cast [potion_effect_type] (duration:#{60s}) (power:#{1}) (target(s):npc|player|npc.#|player.player_name|entity_name)";
        new CastCommand().activate().as("CAST").withOptions(castHint, 1);
        
        String chatHint = "chat ['message to chat'] (npcid:#) (target(s):npc.#|player.player_name{attached player})";
        new ChatCommand().activate().as("CHAT").withOptions(chatHint, 1);
        
        String cooldownHint = "cooldown (duration:#{60s}) (global|{player:player_name}) (script:script_name{)";
        new CooldownCommand().activate().as("COOLDOWN").withOptions(cooldownHint, 1);
        
        String waitHint = "wait (duration:#{5s}) (queue:queue_type) (player:player_name{attached}) (npcid:#{attached})";
        new WaitCommand().activate().as("WAIT").withOptions(waitHint, 0);
        
        String strikeHint = "strike (no_damage) [location:x,y,z,world]";
        new StrikeCommand().activate().as("STRIKE").withOptions(strikeHint, 1);
        
        String disengageHint = "disengage (NPCID:#)";
        new DisengageCommand().activate().as("DISENGAGE").withOptions(disengageHint, 0);
        
        String dropHint = "drop [ITEM:item|EXP] (QTY:#) (LOCATION:location)";
        new DropCommand().activate().as("DROP").withOptions(dropHint, 1);

        String clearHint = "clear [QUEUE:queue_type] (...)";
        new ClearCommand().activate().as("CLEAR").withOptions(clearHint, 1);

        String engageHint = "engage (DURATION:#) (NPCID:#)";
        new EngageCommand().activate().as("ENGAGE").withOptions(engageHint, 0);
        
        String executeHint = "execute [AS_PLAYER|AS_SERVER|AS_NPC|AS_OP] ['Bukkit Command']";
        new ExecuteCommand().activate().as("EXECUTE").withOptions(executeHint, 2);

        String failHint = "fail (PLAYER:player_name)";
        new FailCommand().activate().as("FAIL").withOptions(failHint, 0);

        String feedHint = "feed (AMT:#) (TARGET:NPC|PLAYER)";
        new FeedCommand().activate().as("FEED").withOptions(feedHint, 0);

        String finishHint = "finish (PLAYER:player_name)";
        new FinishCommand().activate().as("FINISH").withOptions(finishHint, 0);

        String flagHint = "flag (DENIZEN|PLAYER|GLOBAL) [[NAME([#])]:[VALUE]|[NAME]:[FLAG_ACTION]:(VALUE)]";
        new FlagCommand().activate().as("FLAG").withOptions(flagHint, 1);

        String giveHint = "give [MONEY|ITEM:#(:#)|ITEM:MATERIAL_TYPE(:#)] (QTY:#)";
        new GiveCommand().activate().as("GIVE").withOptions(giveHint, 1);

        String healHint = "heal (AMT:#) (TARGET:NPC|PLAYER)";
        new HealCommand().activate().as("HEAL").withOptions(healHint, 0);

        String ifHint = "if (!)[COMPARABLE] (OPERATOR) (COMPARED_TO) (BRIDGE) (...) [COMMAND] (ELSE) (COMMAND) // see documentation.";
        new IfCommand().activate().as("IF").withOptions(ifHint, 2);

        String listenHint = "listen [Listener_Type] [ID:ListenerID] [Listener Arguments] // see documentation.";
        new ListenCommand().activate().as("LISTEN").withOptions(listenHint, 2);

        String lookcloseHint = "lookclose [TOGGLE:TRUE|FALSE] (RANGE:#.#) (REALISTIC)";
        new LookcloseCommand().activate().as("LOOKCLOSE").withOptions(lookcloseHint, 1);

        String newHint = "new ITEMSTACK [ITEM:item] (QTY:qty)";
        new NewCommand().activate().as("NEW").withOptions(newHint, 2);

        String modifyBlockHint = "modifyblock [LOCATION:x,y,z,world] [MATERIAL:DATA VALUE] (RADIUS:##) (HEIGHT:##) (DEPTH:##)";
        new ModifyBlockCommand().activate().as("MODIFYBLOCK").withOptions(modifyBlockHint, 2);

        String narrateHint = "narrate (PLAYER:player_name) ['Text to narrate']";
        new NarrateCommand().activate().as("NARRATE").withOptions(narrateHint, 1);

        String playSoundHint = "playsound [LOCATION:x,y,z,world] [SOUND:NAME] (VOLUME:#) (PITCH:#)";
        new PlaySoundCommand().activate().as("PLAYSOUND").withOptions(playSoundHint, 2);

        String randomHint = "random [#]";
        new RandomCommand().activate ().as("RANDOM").withOptions(randomHint, 1);

        String shootHint = "shoot [ENTITY:entity] (RIDE) (LOCATION:location)";
        new ShootCommand().activate().as("SHOOT").withOptions(shootHint, 1);

        String switchHint = "switch [LOCATION:x,y,z,world] (STATE:ON|OFF|TOGGLE) (DURATION:#)";
        new SwitchCommand().activate().as("SWITCH").withOptions(switchHint, 1);

        String takeHint = "take [MONEY|ITEMINHAND|ITEM:#(:#)|ITEM:MATERIAL_TYPE(:#)] (QTY:#)";
        new TakeCommand().activate ().as("TAKE").withOptions(takeHint, 1);

        String teleportHint = "teleport (NPC) [LOCATION:x,y,z,world] (TARGETS:[NPC.#]|[PLAYER.PlayerName])";
        new TeleportCommand().activate().as("TELEPORT").withOptions(teleportHint, 1);

        String triggerHint = "trigger [NAME:Trigger_Name] [(TOGGLE:TRUE|FALSE)|(COOLDOWN:#.#)|(RADIUS:#)]";
        new TriggerCommand().activate().as("TRIGGER").withOptions(triggerHint, 2);

        String zapHint = "zap [#|STEP:step_name] (SCRIPT:script_name{current_script}) (DURATION:#{0})";
        new ZapCommand().activate().as("ZAP").withOptions(zapHint, 0);

        String followHint = "follow (STOP)";
        new FollowCommand().activate().as("FOLLOW").withOptions(followHint, 0);

        String attackHint = "attack (STOP)";
        new AttackCommand().activate().as("ATTACK").withOptions(attackHint, 0);

        String assignmentHint = "assignment [{SET}|REMOVE] (SCRIPT:assignment_script)";
        new AssignmentCommand().activate().as("ASSIGNMENT").withOptions(assignmentHint, 1);

        String determineHint = "determine [TRUE|FALSE]";
        new DetermineCommand().activate().as("DETERMINE").withOptions(determineHint, 1);
		
		String nameplateHint = "nameplate [COLOR] (PLAYER)";
		new NameplateCommand().activate().as("NAMEPLATE").withOptions(nameplateHint, 1);

        dB.echoApproval("Loaded core commands: " + instances.keySet().toString());
	}

	@Override
	public void disableCoreMembers() {
		for (RegistrationableInstance member : instances.values())
			try { 
				member.onDisable(); 
			} catch (Exception e) {
				dB.echoError("Unable to disable '" + member.getClass().getName() + "'!");
				if (dB.showStackTraces) e.printStackTrace();
			}
	}

}
