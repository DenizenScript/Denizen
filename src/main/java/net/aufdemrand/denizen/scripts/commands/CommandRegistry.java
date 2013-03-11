package net.aufdemrand.denizen.scripts.commands;

import java.util.HashMap;
import java.util.Map;

import net.aufdemrand.denizen.Denizen;
import net.aufdemrand.denizen.interfaces.DenizenRegistry;
import net.aufdemrand.denizen.interfaces.RegistrationableInstance;
import net.aufdemrand.denizen.scripts.commands.core.*;
import net.aufdemrand.denizen.utilities.debugging.dB;

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

        registerCoreMember(AnchorCommand.class,
                "ANCHOR", "anchor [id:name] [assume|add|remove|walkto|walknear] (range:#)", 2);
        
        registerCoreMember(AssignmentCommand.class, 
        		"ASSIGNMENT", "assignment [{set}|remove] (script:assignment_script)", 1);
        
        registerCoreMember(CastCommand.class, 
        		"CAST", "cast [effect] (duration:#{60s}) (power:#{1}) (target(s):npc|player|npc.#|player.player_name|entity_name)", 1);
        
        registerCoreMember(ChatCommand.class, 
        		"CHAT", "chat [\"chat text\"] (npcid:#) (target(s):npc.#|player.name{attached player})", 1);
        
        registerCoreMember(ClearCommand.class, 
        		"CLEAR", "clear (queue:name) (...)", 1);
        
        registerCoreMember(CooldownCommand.class, 
        		"COOLDOWN", "cooldown (duration:#{60s}) (global|player:name{attached player}) (script:name)", 1);
		
        registerCoreMember(DetermineCommand.class, 
        		"DETERMINE", "determine [\"value\"]", 1);
        
        registerCoreMember(DisengageCommand.class, 
        		"DISENGAGE", "disengage (npcid:#)", 0);

        registerCoreMember(DisplayItemCommand.class,
                "DISPLAYITEM", "displayitem [item_name|remove] [location:x,y,z,world] (duration:#)", 2);

        registerCoreMember(DropCommand.class, 
        		"DROP", "drop [item:#(:#)|item:material(:#)|xp] (qty:#{1}) (location:x,y,z,world)", 1);
		
        registerCoreMember(EngageCommand.class, 
        		"ENGAGE", "engage (duration:#) (npcid:#)", 0);
        
        registerCoreMember(ExecuteCommand.class, 
        		"EXECUTE", "execute [as_player|as_op|as_npc|as_server] [\"Bukkit command\"]", 2);
		
        registerCoreMember(FailCommand.class, 
        		"FAIL", "fail (script:name{attached script}) (player:player_name)", 0);
        
        registerCoreMember(FeedCommand.class, 
        		"FEED", "feed (amt:#) (target:npc|{player})", 0);

        registerCoreMember(FinishCommand.class, 
        		"FINISH", "finish (script:name{attached script}) (player:player_name)", 0);

        registerCoreMember(FinishCommand.class, 
        		"FISH", "fish (stop) (location:x,y,z,world)", 1);

        registerCoreMember(FlagCommand.class, 
        		"FLAG", "flag ({player}|npc|global) [name([#])](:action)[:value] (duration:#)", 1);
        
        registerCoreMember(FollowCommand.class, 
        		"FOLLOW", "follow (stop)", 0);
		
        registerCoreMember(GiveCommand.class, 
        		"GIVE", "give [money|item:#(:#)|item:material(:#)] (qty:#)", 1);
		
        registerCoreMember(HealCommand.class, 
        		"HEAL", "heal (amt:#) (target:npc|{player})", 0);
		
        registerCoreMember(IfCommand.class, 
        		"IF", "if [comparable] (!)(operator) (compared_to) (bridge) (...) [command] (else) (command)  +--> see documentation.", 2);
		
        registerCoreMember(ListenCommand.class, 
        		"LISTEN", "listen [listener_type] [id:listener_id] (...)  +--> see documentation - http://bit.ly/XJlKwm", 2);
        
        registerCoreMember(LookCommand.class, 
        		"LOOK", "look [location:x,y,z,world]", 1);
        
        registerCoreMember(LookcloseCommand.class, 
        		"LOOKCLOSE", "lookclose [toggle:true|false]", 1);
		
        registerCoreMember(ModifyBlockCommand.class, 
        		"MODIFYBLOCK", "modifyblock [location:x,y,z,world] [material:data] (radius:#) (height:#) (depth:#)", 2);
		
        registerCoreMember(NameplateCommand.class, 
        		"NAMEPLATE", "nameplate [chat_color] (target:player_name)", 1);
        
        registerCoreMember(NarrateCommand.class, 
        		"NARRATE", "narrate [\"narration text\"] (player:name) (format:format)", 1);
        
        registerCoreMember(NewCommand.class, 
        		"NEW", "new itemstack [item:material] (qty:#)", 2);

        registerCoreMember(PoseCommand.class,
                "POSE", "pose (player) [id:name]", 1);
		
        registerCoreMember(PlaySoundCommand.class, 
        		"PLAYSOUND", "playsound [location:x,y,z,world] [sound:name] (volume:#) (pitch:#)", 2);
		
        registerCoreMember(QueueCommand.class,
                "QUEUE", "queue (queue:id{residing_queue}) [clear|pause|resume|delay:#]", 1);
        
        registerCoreMember(RandomCommand.class, 
        		"RANDOM", "random [#]", 1);
        
        registerCoreMember(RemoveCommand.class, 
        		"REMOVE", "remove (npcid:#)", 0);

        registerCoreMember(RuntaskCommand.class,
        		"RUNTASK", "runtask [script_name] (instantly) (queue|queue:queue_name) (delay:#)", 1);
		
        registerCoreMember(ScribeCommand.class, 
        		"SCRIBE", "scribe [script:book_script] (give|{drop}|equip) (location:x,y,z,world) OR scribe [item:id.name] [script:book_script]", 1);

        registerCoreMember(ShootCommand.class, 
        		"SHOOT", "shoot [entity:name] (ride) (burn) (explode) (location:x,y,z,world) (script:name)", 1);

        registerCoreMember(SitCommand.class, 
        		"SIT", "sit (location:x,y,z,world)", 0);

        registerCoreMember(StandCommand.class, 
        		"STAND", "stand", 0);
        
        registerCoreMember(StrikeCommand.class, 
        		"STRIKE", "strike (no_damage) [location:x,y,z,world]", 1);
        
        registerCoreMember(SwitchCommand.class, 
        		"SWITCH", "switch [location:x,y,z,world] (state:[{toggle}|on|off]) (duration:#)", 1);
		
        registerCoreMember(TakeCommand.class, 
        		"TAKE", "take [money|iteminhand|item:#(:#)|item:material(:#)] (qty:#)", 1);
        
        registerCoreMember(TeleportCommand.class, 
        		"TELEPORT", "teleport (npc) [location:x,y,z,world] (target(s):[npc.#]|[player.name])", 1);
        
        registerCoreMember(TriggerCommand.class, 
        		"TRIGGER", "trigger [name:trigger_name] [(toggle:true|false)|(cooldown:#.#)|(radius:#)]", 2);
		
        registerCoreMember(VulnerableCommand.class, 
        		"VULNERABLE", "vulnerable (toggle:{true}|false|toggle)", 0);
		
        registerCoreMember(WaitCommand.class, 
        		"WAIT", "wait (duration:#{5s}) (queue:queue_type) (player:player_name{attached}) (npcid:#{attached})", 0);

        registerCoreMember(WalkToCommand.class,
                "WALKTO", "walkto [location:x,y,z,world] (speed:#)", 1);

        registerCoreMember(ZapCommand.class, 
        		"ZAP", "zap [#|step:step_name] (script:script_name{current_script}) (duration:#)", 0);

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
