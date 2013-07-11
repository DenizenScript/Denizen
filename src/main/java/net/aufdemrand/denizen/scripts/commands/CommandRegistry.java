package net.aufdemrand.denizen.scripts.commands;

import java.util.HashMap;
import java.util.Map;

import net.aufdemrand.denizen.Denizen;
import net.aufdemrand.denizen.interfaces.dRegistry;
import net.aufdemrand.denizen.interfaces.RegistrationableInstance;
import net.aufdemrand.denizen.scripts.commands.core.*;
import net.aufdemrand.denizen.scripts.commands.item.*;
import net.aufdemrand.denizen.scripts.commands.player.*;
import net.aufdemrand.denizen.scripts.commands.server.AnnounceCommand;
import net.aufdemrand.denizen.scripts.commands.server.ExecuteCommand;
import net.aufdemrand.denizen.scripts.commands.server.ScoreboardCommand;
import net.aufdemrand.denizen.scripts.commands.entity.*;
import net.aufdemrand.denizen.scripts.commands.npc.*;
import net.aufdemrand.denizen.scripts.commands.world.*;
import net.aufdemrand.denizen.utilities.debugging.dB;

public class CommandRegistry implements dRegistry {

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

        registerCoreMember(AnchorCommand.class,
                "ANCHOR", "anchor [id:name] [assume|add|remove|walkto|walknear] (range:#)", 2);
        		
        registerCoreMember(AnimateCommand.class,
        		"ANIMATE", "animate [entities:<entity>|...] [animation:<name>]", 1);
        
        registerCoreMember(AnimateChestCommand.class,
        		"ANIMATECHEST", "animatechest [location:x,y,z,world] ({open}|close) (sound:{true}|false)", 1);
		
		registerCoreMember(AnnounceCommand.class,
        		"ANNOUNCE", "announce [\"announcement text\"] (to_ops)", 1);
        
        registerCoreMember(AssignmentCommand.class,
        		"ASSIGNMENT", "assignment [{set}|remove] (script:assignment_script)", 1);
		
        registerCoreMember(AttackCommand.class, 
        		"ATTACK", "attack (stop)", 0);

        registerCoreMember(BreakCommand.class,
                "BREAK", "break [l@location] as:[e@entity]", 1);
        
        registerCoreMember(BurnCommand.class,
        		"BURN", "burn [entities:<entity>|...] (duration:<value>)", 1);
        
        registerCoreMember(CastCommand.class, 
        		"CAST", "cast [effect] (duration:#{60s}) (power:#{1}) (target(s):npc|player|npc.#|player.player_name|entity_name)", 1);
        
        registerCoreMember(ChatCommand.class, 
        		"CHAT", "chat [\"chat text\"] (npcid:#) (target(s):npc.#|player.name{attached player})", 1);
        
        registerCoreMember(ChunkLoadCommand.class,
                "CHUNKLOAD", "chunkload ({add}|remove|removeall) [location:x,y,z,world] (duration:{-1s})", 1);

        registerCoreMember(CooldownCommand.class, 
        		"COOLDOWN", "cooldown (duration:#{60s}) (global|player:name{attached player}) (script:name)", 1);

        registerCoreMember(CopyBlockCommand.class,
                "COPYBLOCK", "copyblock [location:x,y,z,world] [to:x,y,z,world]", 1);

        registerCoreMember(CreateWorldCommand.class,
                "CREATEWORLD", "createworld [world_name] (g:generator_name)", 1);

        registerCoreMember(DefineCommand.class,
                "DEFINE", "define [id] [\"value\"]", 2);
        
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

        registerCoreMember(EngraveCommand.class,
        		"ENGRAVE", "engrave (set|remove) (target:player_name)", 0);
        
        registerCoreMember(EquipCommand.class, 
        		"EQUIP", "equip (hand:[#|material](:#)) (head:[#|material](:#)) (chest:[#|material](:#)) (legs:[#|material](:#)) (boots:[#|material](:#))", 1);
        
        registerCoreMember(ExecuteCommand.class,
        		"EXECUTE", "execute [as_player|as_op|as_npc|as_server] [\"Bukkit command\"]", 2);

        registerCoreMember(ExperienceCommand.class,
                "EXPERIENCE", "experience [{set}|give|take] (level) [#] (player:player_name)", 2);

        registerCoreMember(ExplodeCommand.class,
        		"EXPLODE", "explode (power:#) (location:x,y,z,world) (fire) (breakblocks)", 0);
        
        registerCoreMember(FailCommand.class, 
        		"FAIL", "fail (script:name{attached script}) (player:player_name)", 0);
        
        registerCoreMember(FeedCommand.class, 
        		"FEED", "feed (amt:#) (target:npc|{player})", 0);

        registerCoreMember(FinishCommand.class, 
        		"FINISH", "finish (script:name{attached script}) (player:player_name)", 0);

        registerCoreMember(FireworkCommand.class,
        		"FIREWORK", "firework (location:x,y,z,world) (power:#) (type:name|random) (primary:color1|color2|...) (fade:color1|color2|...) (flicker) (trail)", 0);
        
        registerCoreMember(FishCommand.class, 
        		"FISH", "fish (catchfish) (stop) (location:x,y,z,world) (catchpercent:#{65})", 1);

        registerCoreMember(FlagCommand.class, 
        		"FLAG", "flag ({player}|npc|global) [name([#])](:action)[:value] (duration:#)", 1);
        
        registerCoreMember(FlyCommand.class,
        		"FLY", "fly (cancel) [entities:<entity>|...] (origin:<x,y,z,world>) (destination(s):<x,y,z,world>|...) (speed:<#>)", 1);
        
        registerCoreMember(FollowCommand.class, 
        		"FOLLOW", "follow (stop)", 0);
		
        registerCoreMember(ForEachCommand.class,
        		"FOREACH", "foreach [location:x,y,z,world|x,y,z,world] ['script_to_run']", 0); 
        
        registerCoreMember(GiveCommand.class,
        		"GIVE", "give [money|item:#(:#)|item:material(:#)] (qty:#) (engrave)", 1);
        
        registerCoreMember(GroupCommand.class,
                "GROUP", "group [add|remove]  [group] (player:player_name) (world:world_name)", 2);
		
        registerCoreMember(HeadCommand.class,
        		"HEAD", "head (player) [skin:name]", 0);
        
        registerCoreMember(HealCommand.class, 
        		"HEAL", "heal (amt:#) (target:npc|{player})", 0);
        
        registerCoreMember(HealthCommand.class,
                "HEALTH", "health (state:true|false|toggle) (set_max:#)", 1);
		
        registerCoreMember(HurtCommand.class,
        		"HURT", "hurt (amt:#) (target:npc|{player})", 0);
        
        registerCoreMember(IfCommand.class, 
        		"IF", "if [comparable] (!)(operator) (compared_to) (bridge) (...) [command] (else) (command)  +--> see documentation.", 2);

        registerCoreMember(InventoryCommand.class,
                "INVENTORY", "inventory [open/copy/move/swap/add/remove/keep/exclude/fill/clear] (origin:<entity>/<x,y,z,world>) [destination:<entity>/<x,y,z,world>]", 2);
        
        registerCoreMember(InvisibleCommand.class,
                "INVISIBLE", "invisible [npc] [state:true|false|toggle]", 2);
		
        registerCoreMember(ListenCommand.class,
        		"LISTEN", "listen [listener_type] [id:listener_id] (...)  +--> see documentation - http://bit.ly/XJlKwm", 2);
        
        registerCoreMember(LogCommand.class,
        		"LOG", "log [\"message\"] (type:severe|info|warning|fine|finer|finest) [file:filename]", 2);
        
        registerCoreMember(LookCommand.class,
        		"LOOK", "look (player) [location:x,y,z,world]", 1);
        
        registerCoreMember(LookcloseCommand.class,
        		"LOOKCLOSE", "lookclose [state:true|false]", 1);
		
        registerCoreMember(MidiCommand.class,
        		"MIDI", "midi [file:<name>] (listener(s):[p@<name>|...])|(location:<x,y,z,world>) (tempo:<#.#>)", 1);
        
        registerCoreMember(MountCommand.class,
        		"MOUNT", "mount (cancel) [entities:<entity>|...] (location:<x,y,z,world>)", 0);
        
        registerCoreMember(ModifyBlockCommand.class,
        		"MODIFYBLOCK", "modifyblock [location:<x,y,z,world>] [<material>(:<data>)] (radius:<#>) (height:<#>) (depth:<#>)", 2);
		
        registerCoreMember(NameplateCommand.class,
        		"NAMEPLATE", "nameplate [set:text|chat_color] (target:<name>)", 1);
        
        registerCoreMember(NarrateCommand.class,
        		"NARRATE", "narrate [\"narration text\"] (target(s):p@<name>|...) (format:<name>)", 1);
        
        registerCoreMember(NewCommand.class, 
        		"NEW", "new itemstack [item:<material>] (qty:<#>)", 2);
        
        registerCoreMember(OxygenCommand.class,
        		"OXYGEN", "oxygen (type:maximum|remaining) (mode:set|add|remove) [qty:<#>]", 1);
        
        registerCoreMember(PlayEffectCommand.class,
        		"PLAYEFFECT", "playeffect [location:<x,y,z,world>] [effect:<name>] (data:<#>) (radius:<#>) (qty:<#>)", 2);
        
        registerCoreMember(PlaySoundCommand.class,
        		"PLAYSOUND", "playsound [location:<x,y,z,world>] [sound:<name>] (volume:<#>) (pitch:<#>)", 2);
        
        registerCoreMember(PermissionCommand.class,
        		"PERMISSION", "permission [add|remove] [permission] (player:<name>) (group:<name>) (world:<name>)", 2);

        registerCoreMember(PoseCommand.class,
        		"POSE", "pose (player) [id:<name>]", 1);

        registerCoreMember(PauseCommand.class,
                "PAUSE", "pause [waypoints|navigation]", 1);
		
        registerCoreMember(QueueCommand.class,
        		"QUEUE", "queue (queue:<id>{<residing_queue>}) [clear|pause|resume|delay:<#>]", 1);
        
        registerCoreMember(RandomCommand.class, 
        		"RANDOM", "random [#]", 1);
        
        registerCoreMember(RemoveCommand.class, 
        		"REMOVE", "remove (type:<entity>/npcid:<#>) (region:<name>)", 0);

        registerCoreMember(RenameCommand.class,
                "RENAME", "rename (npc:n@...) [new_name]", 1);

        registerCoreMember(ResetCommand.class,
                "RESET", "reset [fails|finishes|cooldown] (script:script_name{attached script})", 1);

        registerCoreMember(RunCommand.class,
                "RUN", "run [script] (path:...) (as:p@player|n@npc) (def:...|...) (id:id_name) (delay:duration) (loop) (q:#)", 1);

        registerCoreMember(RuntaskCommand.class,
        		"RUNTASK", "runtask [script_name] (instantly) (queue|queue:queue_name) (delay:#)", 1);

        registerCoreMember(ScoreboardCommand.class,
                "SCOREBOARD", "", 1);
		
        registerCoreMember(ScribeCommand.class,
        		"SCRIBE", "scribe [script:book_script] (give|{drop}|equip) (location:x,y,z,world) OR scribe [item:id.name] [script:book_script]", 1);

        registerCoreMember(ShootCommand.class,
        		"SHOOT", "shoot [projectile(s):<entity>|...] (origin:p@<name>/n@<id>) (destination:<x,y,z,world>) (speed:<#>) (script:<name>)", 1);

        registerCoreMember(SignCommand.class, 
        		"SIGN", "sign (type:{sign_post}/wall_sign) [\"<text>|<text>|<text>|<text>\"] [location:<x,y,z,world>]", 1);
        
        registerCoreMember(SitCommand.class, 
        		"SIT", "sit (location:x,y,z,world)", 0);

        registerCoreMember(SpawnCommand.class,
        		"SPAWN", "spawn [entities:<entity>|...] (location:<x,y,z,world>) (target:<entity>)", 1);
        
        registerCoreMember(StandCommand.class, 
        		"STAND", "stand", 0);
        
        registerCoreMember(StrikeCommand.class,
        		"STRIKE", "strike (no_damage) [location:x,y,z,world]", 1);
        
        registerCoreMember(SwitchCommand.class,
        		"SWITCH", "switch [location:x,y,z,world] (state:[{toggle}|on|off]) (duration:#)", 1);
		
        registerCoreMember(TakeCommand.class,
        		"TAKE", "take [money|iteminhand|item:#(:#)|item:material(:#)] (qty:#)", 1);
        
        registerCoreMember(TeleportCommand.class,
        		"TELEPORT", "teleport (npc) [location:x,y,z,world] (target(s):[n@#]|[p@name])", 1);
        
        registerCoreMember(TimeCommand.class,
                "TIME", "time [type:{global}|player] [<value>] (world:<name>)", 1);
        
        registerCoreMember(TriggerCommand.class, 
        		"TRIGGER", "trigger [name:trigger_name] [(state:true|false)|(cooldown:#.#)|(radius:#)]", 2);
		
        registerCoreMember(VulnerableCommand.class, 
        		"VULNERABLE", "vulnerable (state:{true}|false|toggle)", 0);
		
        registerCoreMember(WaitCommand.class, 
        		"WAIT", "wait (duration:#{5s}) (queue:queue_type) (player:player_name{attached}) (npcid:#{attached})", 0);

        registerCoreMember(WalkCommand.class,
                "WALK, WALKTO", "walk [l@location] (speed:percentage) (auto_range)", 1);

        registerCoreMember(WeatherCommand.class,
                "WEATHER", "weather [type:{global}|player] [sunny|storm|thunder] (world:<name>)", 1);
        
        registerCoreMember(YamlCommand.class,
                "YAML", "...", 1);

        registerCoreMember(ZapCommand.class, 
        		"ZAP", "zap [#|step:step_name] (script:script_name{current_script}) (duration:#)", 0);

        dB.echoApproval("Loaded core commands: " + instances.keySet().toString());
	}
	
    private <T extends AbstractCommand> void registerCoreMember(Class<T> cmd, String names, String hint, int args) {
    	for (String name : names.split(", ")) {
    	
    		try {            	
                cmd.newInstance().activate().as(name).withOptions(hint, args);
            } catch(Exception e) {
                dB.echoError("Could not register command " + name + ": " + e.getMessage());
                if (dB.showStackTraces) e.printStackTrace();
            }
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
