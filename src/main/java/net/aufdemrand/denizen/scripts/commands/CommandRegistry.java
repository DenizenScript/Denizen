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
		new AnnounceCommand().activate().as("ANNOUNCE").withOptions("['Text to announce']", 1);
		new ScribeCommand().activate().as("SCRIBE").withOptions("[SCRIPT:book_script] (GIVE|{DROP}|EQUIP) (LOCATION:x,y,z,world) (ITEM:ITEMSTACK.name)", 0);
        new RuntaskCommand().activate().as("RUNTASK").withOptions("RUNTASK (ID:id_name{script_name}) [SCRIPT:script_name] (INSTANT|QUEUE:QueueType{PLAYER_TASK}) (DELAY:#{0})", 1);
		new CastCommand().activate().as("CAST").withOptions("[TYPE:PotionEffectType] (DURATION:#) (POWER:#) (TARGET:NPC|PLAYER)", 1);
		new ChatCommand().activate().as("CHAT").withOptions("['Message to chat'] (NPCID:#) (TARGETS:#|player_name)", 1);
		new CooldownCommand().activate().as("COOLDOWN").withOptions("[DURATION:#] (GLOBAL) (PLAYER:player_name) ('SCRIPT:name of script')", 1);
		new DisengageCommand().activate().as("DISENGAGE").withOptions("(NPCID:#)", 0);
		new DropCommand().activate().as("DROP").withOptions("[ITEM:item|EXP] (QTY:#) (LOCATION:location)", 1);
        new ClearCommand().activate().as("CLEAR").withOptions("[QUEUE:queue_type] (...)", 1);
		new EngageCommand().activate().as("ENGAGE").withOptions("(DURATION:#) (NPCID:#)", 0);
		new ExecuteCommand().activate().as("EXECUTE").withOptions("[AS_PLAYER|AS_SERVER|AS_NPC|AS_OP] ['Bukkit Command']", 2);
		new FailCommand().activate().as("FAIL").withOptions("(PLAYER:player_name)", 0);
		new FeedCommand().activate().as("FEED").withOptions("(AMT:#) (TARGET:NPC|PLAYER)", 0);
		new FinishCommand().activate().as("FINISH").withOptions("(PLAYER:player_name)", 0);
		new FlagCommand().activate().as("FLAG").withOptions("(DENIZEN|PLAYER|GLOBAL) [[NAME([#])]:[VALUE]|[NAME]:[FLAG_ACTION]:(VALUE)]", 1);
		new GiveCommand().activate().as("GIVE").withOptions("[MONEY|ITEM:#(:#)|ITEM:MATERIAL_TYPE(:#)] (QTY:#)", 1);
		new HealCommand().activate().as("HEAL").withOptions("(AMT:#) (TARGET:NPC|PLAYER)", 0);
		new IfCommand().activate().as("IF").withOptions("(!)[COMPARABLE] (OPERATOR) (COMPARED_TO) (BRIDGE) (...) [COMMAND] (ELSE) (COMMAND) // see documentation.", 2);
		new ListenCommand().activate().as("LISTEN").withOptions("[Listener_Type] [ID:ListenerID] [Listener Arguments] // see documentation.", 2);
		new LookcloseCommand().activate().as("LOOKCLOSE").withOptions("[TOGGLE:TRUE|FALSE] (RANGE:#.#) (REALISTIC)", 1);
		new NewCommand().activate().as("NEW").withOptions("ITEMSTACK [ITEM:item] (QTY:qty)", 2);
		new ModifyBlockCommand().activate().as("MODIFYBLOCK").withOptions("[LOCATION:x,y,z,world] [MATERIAL:DATA VALUE] (RADIUS:##) (HEIGHT:##) (DEPTH:##)", 2);
		new NarrateCommand().activate().as("NARRATE").withOptions("(PLAYER:player_name) ['Text to narrate']", 1);
		new PlaySoundCommand().activate().as("PLAYSOUND").withOptions("[LOCATION:x,y,z,world] [SOUND:NAME] (VOLUME:#) (PITCH:#)", 2);
		new RandomCommand().activate ().as("RANDOM").withOptions("[#]", 1);
		new SwitchCommand().activate().as("SWITCH").withOptions("[LOCATION:x,y,z,world] (STATE:ON|OFF|TOGGLE) (DURATION:#)", 1);
		new TakeCommand().activate ().as("TAKE").withOptions("[MONEY|ITEMINHAND|ITEM:#(:#)|ITEM:MATERIAL_TYPE(:#)] (QTY:#)", 1);
		new TeleportCommand().activate().as("TELEPORT").withOptions("(NPC) [LOCATION:x,y,z,world] (TARGETS:[NPCID:#]|[PLAYER:PlayerName])", 1);
		new TriggerCommand().activate().as("TRIGGER").withOptions("[NAME:Trigger_Name] [(TOGGLE:TRUE|FALSE)|(COOLDOWN:#.#)|(RADIUS:#)]", 2);
        new ZapCommand().activate().as("ZAP").withOptions("[#|STEP:step_name] (SCRIPT:script_name{current_script}) (DURATION:#{0})", 0);
        new FollowCommand().activate().as("FOLLOW").withOptions("(STOP)", 0);
        new AttackCommand().activate().as("ATTACK").withOptions("(STOP)", 0);
        new AssignmentCommand().activate().as("ASSIGNMENT").withOptions("[{SET}|REMOVE] (SCRIPT:assignment_script)", 1);
        new DetermineCommand().activate().as("DETERMINE").withOptions("[TRUE|FALSE]", 1);
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
