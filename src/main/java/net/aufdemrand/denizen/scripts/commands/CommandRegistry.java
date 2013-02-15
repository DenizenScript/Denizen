package net.aufdemrand.denizen.scripts.commands;

import net.aufdemrand.denizen.Denizen;
import net.aufdemrand.denizen.interfaces.DenizenRegistry;
import net.aufdemrand.denizen.interfaces.RegistrationableInstance;
import net.aufdemrand.denizen.scripts.commands.core.*;
import net.aufdemrand.denizen.utilities.Depends;
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
        registerCoreMember(AnnounceCommand.class, 
        		"ANNOUNCE", "announce [\"announcement text\"] (to_ops)", 1);
        
        registerCoreMember(AttackCommand.class, 
        		"ATTACK", "attack (stop)", 0);
        
        registerCoreMember(AssignmentCommand.class, 
        		"ASSIGNMENT", "assignment [{set}|remove] (script:assignment_script)", 1);
        
        registerCoreMember(CastCommand.class, 
        		"CAST", "cast [potion_effect_type] (duration:#{60s}) (power:#{1}) (target(s):npc|player|npc.#|player.player_name|entity_name)", 1);

        if (Depends.protocolManager != null) registerCoreMember(ChairCommand.class,
        		"CHAIR", "chair (sit|stand) [location:x,y,z,world]", 1);
        
        registerCoreMember(ChatCommand.class, 
        		"CHAT", "chat [\"chat text\"] (npcid:#) (target(s):npc.#|player.name{attached player})", 1);
        
        registerCoreMember(ClearCommand.class, 
        		"CLEAR", "clear (queue:name) (...)", 1);
        
        registerCoreMember(CooldownCommand.class, 
        		"COOLDOWN", "cooldown (duration:#{60s}) (global|player:name{attached player}) (script:name)", 1);
		
        registerCoreMember(DetermineCommand.class, 
        		"DETERMINE", "determine [true|false]", 1);
        
        registerCoreMember(DisengageCommand.class, 
        		"DISENGAGE", "disengage (npcid:#)", 0);
        
        registerCoreMember(DropCommand.class, 
        		"DROP", "drop [item:#(:#)|item:material(:#)|xp] (qty:#{1}) (location:x,y,z,world)", 1);
		
        registerCoreMember(EngageCommand.class, 
        		"ENGAGE", "engage (duration:#) (npcid:#)", 0);
        
        registerCoreMember(ExecuteCommand.class, 
        		"EXECUTE", "execute [as_player|as_op|as_npc|as_server] [\"Bukkit command\"]", 2);
		
        registerCoreMember(FailCommand.class, 
        		"FAIL", "fail (player:name)", 0);
        
        registerCoreMember(FeedCommand.class, 
        		"FEED", "feed (amt:#) (target:npc|{player})", 0);
        
        registerCoreMember(FinishCommand.class, 
        		"FINISH", "finish (player:name)", 0);
        
        registerCoreMember(FlagCommand.class, 
        		"FLAG", "flag (DENIZEN|PLAYER|GLOBAL) [[NAME([#])]:[VALUE]|[NAME]:[FLAG_ACTION]:(VALUE)]", 1);
        
        registerCoreMember(FollowCommand.class, 
        		"FOLLOW", "follow (stop)", 0);
		
        registerCoreMember(GiveCommand.class, 
        		"GIVE", "give [MONEY|ITEM:#(:#)|ITEM:MATERIAL_TYPE(:#)] (QTY:#)", 1);
		
        registerCoreMember(HealCommand.class, 
        		"HEAL", "heal (AMT:#) (TARGET:NPC|PLAYER)", 0);
		
        registerCoreMember(IfCommand.class, 
        		"IF", "if (!)[COMPARABLE] (OPERATOR) (COMPARED_TO) (BRIDGE) (...) [COMMAND] (ELSE) (COMMAND) // see documentation.", 2);
		
        registerCoreMember(ListenCommand.class, 
        		"LISTEN", "listen [Listener_Type] [ID:ListenerID] [Listener Arguments] // see documentation.", 2);
        
        registerCoreMember(LookCommand.class, 
        		"LOOK", "look [location:x,y,z,world]", 1);
        
        registerCoreMember(LookcloseCommand.class, 
        		"LOOKCLOSE", "lookclose [TOGGLE:TRUE|FALSE] (RANGE:#.#) (REALISTIC)", 1);
		
        registerCoreMember(ModifyBlockCommand.class, 
        		"MODIFYBLOCK", "modifyblock [LOCATION:x,y,z,world] [MATERIAL:DATA VALUE] (RADIUS:##) (HEIGHT:##) (DEPTH:##)", 2);
		
        registerCoreMember(NameplateCommand.class, 
        		"NAMEPLATE", "nameplate [COLOR] (PLAYER)", 1);
        
        registerCoreMember(NarrateCommand.class, 
        		"NARRATE", "narrate [\"narration text\"] (player:name)", 1);
        
        registerCoreMember(NewCommand.class, 
        		"NEW", "new ITEMSTACK [ITEM:item] (QTY:qty)", 2);
		
        registerCoreMember(PlaySoundCommand.class, 
        		"PLAYSOUND", "playsound [LOCATION:x,y,z,world] [SOUND:NAME] (VOLUME:#) (PITCH:#)", 2);
		
        registerCoreMember(RandomCommand.class, 
        		"RANDOM", "random [#]", 1);
        
        registerCoreMember(RuntaskCommand.class, 
        		"RUNTASK", "runtask (id:id_name{script_name}) [script:script_name] (instant|queue:queue_type{player_task}) (delay:#{0})", 1);
		
        registerCoreMember(ScribeCommand.class, 
        		"SCRIBE", "scribe [script:book_script] (give|{drop}|equip) (location:x,y,z,world) OR scribe [item:id.name] [script:book_script]", 1);
        
        registerCoreMember(ShootCommand.class, 
        		"SHOOT", "shoot [entity:name] (ride) (burn) (explode) (location:x,y,z,world) (script:name)", 1);
        
        registerCoreMember(StrikeCommand.class, 
        		"STRIKE", "strike (no_damage) [location:x,y,z,world]", 1);
        
        registerCoreMember(SwitchCommand.class, 
        		"SWITCH", "switch [LOCATION:x,y,z,world] (STATE:ON|OFF|TOGGLE) (DURATION:#)", 1);
		
        registerCoreMember(TakeCommand.class, 
        		"TAKE", "take [MONEY|ITEMINHAND|ITEM:#(:#)|ITEM:MATERIAL_TYPE(:#)] (QTY:#)", 1);
        
        registerCoreMember(TeleportCommand.class, 
        		"TELEPORT", "teleport (NPC) [LOCATION:x,y,z,world] (TARGETS:[NPC.#]|[PLAYER.PlayerName])", 1);
        
        registerCoreMember(TriggerCommand.class, 
        		"TRIGGER", "trigger [NAME:Trigger_Name] [(TOGGLE:TRUE|FALSE)|(COOLDOWN:#.#)|(RADIUS:#)]", 2);
		
        registerCoreMember(VulnerableCommand.class, 
        		"VULNERABLE", "vulnerable (toggle:{true}|false|toggle)", 0);
		
        registerCoreMember(WaitCommand.class, 
        		"WAIT", "wait (duration:#{5s}) (queue:queue_type) (player:player_name{attached}) (npcid:#{attached})", 0);

        registerCoreMember(ZapCommand.class, 
        		"ZAP", "zap [#|STEP:step_name] (SCRIPT:script_name{current_script}) (DURATION:#{0})", 0);

        dB.echoApproval("Loaded core commands: " + instances.keySet().toString());
	}
	
    private <T extends AbstractCommand> void registerCoreMember(Class<T> cmd, String name, String hint, int args) {
        try {
            cmd.newInstance().activate().as(name).withOptions(hint, args);
        } catch(Exception e) {
            dB.echoError("Could not register command " + name + ": " + e.getMessage());
            if (dB.showStackTraces) e.printStackTrace();
        }
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
