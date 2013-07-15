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
                "ANCHOR", "anchor [id:<name>] [assume/add/remove/walkto/walknear] (range:<#>)", 2);
        		
        registerCoreMember(AnimateCommand.class,
        		"ANIMATE", "animate [entities:<entity>|...] [animation:<name>]", 1);
        
        registerCoreMember(AnimateChestCommand.class,
        		"ANIMATECHEST", "animatechest [<location>] ({open}/close) (sound:{true}/false)", 1);
		
		registerCoreMember(AnnounceCommand.class,
        		"ANNOUNCE", "announce [\"<text>\"] (to_ops)", 1);
        
        registerCoreMember(AssignmentCommand.class,
        		"ASSIGNMENT", "assignment [{set}/remove] (script:<name>)", 1);
		
        registerCoreMember(AttackCommand.class, 
        		"ATTACK", "attack (stop)", 0);

        registerCoreMember(BreakCommand.class,
                "BREAK", "break [<location>] (entity:<entity>) (radius:<#.#>)", 1);
        
        registerCoreMember(BurnCommand.class,
        		"BURN", "burn [entities:<entity>|...] (duration:<value>)", 1);
        
        registerCoreMember(CastCommand.class, 
        		"CAST", "cast [effect] (duration:<#>) (power:<#>) (targets:<entity>|...)", 1);
        
        registerCoreMember(ChatCommand.class, 
        		"CHAT", "chat [\"<text>\"] (targets:<entity>|...)", 1);
        
        registerCoreMember(ChunkLoadCommand.class,
                "CHUNKLOAD", "chunkload ({add}/remove/removeall) [<location>] (duration:<value>)", 1);

        registerCoreMember(CooldownCommand.class, 
        		"COOLDOWN", "cooldown (duration:<#>) (global) (script:<name>)", 1);

        registerCoreMember(CopyBlockCommand.class,
                "COPYBLOCK", "copyblock [location:<location>] [to:<location>]", 1);

        registerCoreMember(CreateWorldCommand.class,
                "CREATEWORLD", "createworld [<name>] (g:<generator>)", 1);

        registerCoreMember(DefineCommand.class,
                "DEFINE", "define [<id>] [<value>]", 2);
        
        registerCoreMember(DetermineCommand.class, 
        		"DETERMINE", "determine [<value>]", 1);

        registerCoreMember(DisengageCommand.class, 
        		"DISENGAGE", "disengage", 0);

        registerCoreMember(DisplayItemCommand.class,
                "DISPLAYITEM", "displayitem (remove) [<item>] [<location>] (duration:<#>)", 2);

        registerCoreMember(DropCommand.class, 
        		"DROP", "drop [<item>/<entity>/<xp>] [<location>] (qty:<#>)", 1);
		
        registerCoreMember(EngageCommand.class,
        		"ENGAGE", "engage (duration:<#>)", 0);

        registerCoreMember(EngraveCommand.class,
        		"ENGRAVE", "engrave (set/remove)", 0);
        
        registerCoreMember(EquipCommand.class, 
        		"EQUIP", "equip (hand:<item>) (head:<item>) (chest:<item>) (legs:<item>) (boots:<item>)", 1);
        
        registerCoreMember(ExecuteCommand.class,
        		"EXECUTE", "execute [as_player/as_op/as_npc/as_server] [<Bukkit command>]", 2);

        registerCoreMember(ExperienceCommand.class,
                "EXPERIENCE", "experience [{set}/give/take] (level) [<#>]", 2);

        registerCoreMember(ExplodeCommand.class,
        		"EXPLODE", "explode (power:<#.#>) (<location>) (fire) (breakblocks)", 0);
        
        registerCoreMember(FailCommand.class, 
        		"FAIL", "fail (script:<name>)", 0);
        
        registerCoreMember(FeedCommand.class, 
        		"FEED", "feed (amt:<#>) (target:<entity>|...)", 0);

        registerCoreMember(FinishCommand.class, 
        		"FINISH", "finish (script:<name>)", 0);

        registerCoreMember(FireworkCommand.class,
        		"FIREWORK", "firework (<location>) (power:<#>) (type:<name>/random) (primary:<color>|...) (fade:<color>|...) (flicker) (trail)", 0);
        
        registerCoreMember(FishCommand.class, 
        		"FISH", "fish (catchfish) (stop) (<location>) (catchpercent:<#>)", 1);

        registerCoreMember(FlagCommand.class, 
        		"FLAG", "flag ({player}/npc/global) [<name>([<#>])](:<action>)[:<value>] (duration:<#>)", 1);
        
        registerCoreMember(FlyCommand.class,
        		"FLY", "fly (cancel) [entities:<entity>|...] (origin:<location>) (destinations:<location>|...) (speed:<#.#>)", 1);
        
        registerCoreMember(FollowCommand.class, 
        		"FOLLOW", "follow (stop) (lead:<#.#>)", 0);
		
        registerCoreMember(ForEachCommand.class,
        		"FOREACH", "foreach [location:<location>|...] [<script>]", 0); 
        
        registerCoreMember(GiveCommand.class,
        		"GIVE", "give [money/<item>] (qty:<#>) (engrave)", 1);
        
        registerCoreMember(GroupCommand.class,
                "GROUP", "group [add/remove] [<group>] (world:<name>)", 2);
		
        registerCoreMember(HeadCommand.class,
        		"HEAD", "head (player) [skin:<name>]", 0);
        
        registerCoreMember(HealCommand.class, 
        		"HEAL", "heal (qty:<#.#>) (target:<entity>)", 0);
        
        registerCoreMember(HealthCommand.class,
                "HEALTH", "health (state:true/false/toggle) (set_max:<#>)", 1);
		
        registerCoreMember(HurtCommand.class,
        		"HURT", "hurt (qty:<#.#>) (target:<entity>)", 0);
        
        registerCoreMember(IfCommand.class, 
        		"IF", "if [<value>] (!)(<operator> <value>) (&&/|| ...) [<commands>] (else <commands>)", 2);

        registerCoreMember(InventoryCommand.class,
                "INVENTORY", "inventory [open/copy/move/swap/add/remove/keep/exclude/fill/clear] (origin:<entity>/<location>) [destination:<entity>/<location>]", 2);
        
        registerCoreMember(InvisibleCommand.class,
                "INVISIBLE", "invisible [player/npc] [state:true/false/toggle]", 2);
		
        registerCoreMember(ListenCommand.class,
        		"LISTEN", "listen ({new}/cancel/finish) [kill/block/item/itemdrop/travel] [<requirements>]  [script:<name>] (id:<name>)", 2);
        
        registerCoreMember(LogCommand.class,
        		"LOG", "log [<text>] (type:severe/info/warning/fine/finer/finest) [file:<name>]", 2);
        
        registerCoreMember(LookCommand.class,
        		"LOOK", "look (<entity>|...) [<location>]", 1);
        
        registerCoreMember(LookcloseCommand.class,
        		"LOOKCLOSE", "lookclose [state:true/false]", 1);
		
        registerCoreMember(MidiCommand.class,
        		"MIDI", "midi [file:<name>] [<location>/listeners:<player>|...] (tempo:<#.#>)", 1);
        
        registerCoreMember(MountCommand.class,
        		"MOUNT", "mount (cancel) [entities:<entity>|...] (<location>)", 0);
        
        registerCoreMember(ModifyBlockCommand.class,
        		"MODIFYBLOCK", "modifyblock [<location>] [<block>] (radius:<#>) (height:<#>) (depth:<#>)", 2);
		
        registerCoreMember(NameplateCommand.class,
        		"NAMEPLATE", "nameplate [<chatcolor>] [set:<text>] (target:<player>)  +--> Requires ProtocolLib", 1);
        
        registerCoreMember(NarrateCommand.class,
        		"NARRATE", "narrate [\"<text>\"] (targets:<player>|...) (format:<name>)", 1);
        
        registerCoreMember(NewCommand.class, 
        		"NEW", "new [itemstack/entity/npc] [id:<name>] (<item> qty:<#>)", 2);
        
        registerCoreMember(OxygenCommand.class,
        		"OXYGEN", "oxygen (type:maximum/remaining) (mode:set/add/remove) [qty:<#>]", 1);
        
        registerCoreMember(PlayEffectCommand.class,
        		"PLAYEFFECT", "playeffect [<location>] [effect:<name>] (data:<#.#>) (visibility:<#.#>) (qty:<#>) (offset:<#.#>)", 2);
        
        registerCoreMember(PlaySoundCommand.class,
        		"PLAYSOUND", "playsound [<location>] [sound:<name>] (volume:<#.#>) (pitch:<#.#>)", 2);
        
        registerCoreMember(PermissionCommand.class,
        		"PERMISSION", "permission [add|remove] [permission] (player:<name>) (group:<name>) (world:<name>)", 2);

        registerCoreMember(PoseCommand.class,
        		"POSE", "pose (player/npc) [id:<name>]", 1);

        registerCoreMember(PauseCommand.class,
                "PAUSE", "pause [waypoints/navigation]", 1);
		
        registerCoreMember(QueueCommand.class,
        		"QUEUE", "queue (queue:<id>) [clear/pause/resume/delay:<#>]", 1);
        
        registerCoreMember(RandomCommand.class, 
        		"RANDOM", "random [<#>]", 1);
        
        registerCoreMember(RemoveCommand.class, 
        		"REMOVE", "remove [entities:<entity>|...] (region:<name>)", 0);

        registerCoreMember(RenameCommand.class,
                "RENAME", "rename [<npc>] [<name>]", 1);

        registerCoreMember(ResetCommand.class,
                "RESET", "reset [fails/finishes/cooldown] (script:<name>)", 1);

        registerCoreMember(RunCommand.class,
                "RUN", "run [<script>] (path:<name>) (as:<player>/<npc>) (define:<element>|...) (id:<name>) (delay:<duration>) (loop) (qty:<#>)", 1);

        registerCoreMember(RuntaskCommand.class,
        		"RUNTASK", "runtask [<name>] (instantly) (queue(:<name>)) (delay:<#>) (define:<element>|...)", 1);

        registerCoreMember(ScoreboardCommand.class,
                "SCOREBOARD", "scoreboard [set/remove/show/hide] [<name>] [value:<name>] (priority:<#>)", 1);
		
        registerCoreMember(ScribeCommand.class,
        		"SCRIBE", "scribe [script:<name>] (give/drop/equip) (<item>) (<location>)", 1);

        registerCoreMember(ShootCommand.class,
        		"SHOOT", "shoot [entities:<entity>|...] (origin:<player>/<npc>) (destination:<location>) (speed:<#.#>) (script:<name>)", 1);

        registerCoreMember(SignCommand.class, 
        		"SIGN", "sign (type:{sign_post}/wall_sign) [\"<line>|...\"] [<location>]", 1);
        
        registerCoreMember(SitCommand.class, 
        		"SIT", "sit (<location>)", 0);

        registerCoreMember(SpawnCommand.class,
        		"SPAWN", "spawn [entities:<entity>|...] (<location>) (target:<entity>)", 1);
        
        registerCoreMember(StandCommand.class, 
        		"STAND", "stand", 0);
        
        registerCoreMember(StrikeCommand.class,
        		"STRIKE", "strike (no_damage) [<location>]", 1);
        
        registerCoreMember(SwitchCommand.class,
        		"SWITCH", "switch [<location>] (state:[{toggle}/on/off]) (duration:<#>)", 1);
		
        registerCoreMember(TakeCommand.class,
        		"TAKE", "take [money/iteminhand/<item>] (qty:<#>)", 1);
        
        registerCoreMember(TeleportCommand.class,
        		"TELEPORT", "teleport (entities:<entity>|...) (<location>)", 1);
        
        registerCoreMember(TimeCommand.class,
                "TIME", "time [type:{global}/player] [<value>] (world:<name>)", 1);
        
        registerCoreMember(TriggerCommand.class, 
        		"TRIGGER", "trigger [name:chat/click/damage/proximity] (state:true/false) (cooldown:<#.#>) (radius:<#>)", 2);
		
        registerCoreMember(VulnerableCommand.class, 
        		"VULNERABLE", "vulnerable (state:{true}/false/toggle)", 0);
		
        registerCoreMember(WaitCommand.class, 
        		"WAIT", "wait (duration:<#>) (queue:<name>)", 0);

        registerCoreMember(WalkCommand.class,
                "WALK, WALKTO", "walk [<location>] (speed:<#>) (auto_range)", 1);

        registerCoreMember(WeatherCommand.class,
                "WEATHER", "weather [type:{global}/player] [sunny/storm/thunder] (world:<name>)", 1);
        
        registerCoreMember(YamlCommand.class,
                "YAML", "yaml [load/create/save:<file>]/[write:<key>]/[write:<key> value:<value>] [id:<name>]", 1);

        registerCoreMember(ZapCommand.class, 
        		"ZAP", "zap [<#>/step:<name>] (script:<name>) (duration:<#>)", 0);
        
        
        // STOP (There's no more commands, spazz!)

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
