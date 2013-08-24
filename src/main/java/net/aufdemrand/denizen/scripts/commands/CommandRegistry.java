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
                "ANIMATE", "animate [<entity>|...] [animation:<name>]", 1);
        
        registerCoreMember(AnimateChestCommand.class,
                "ANIMATECHEST", "animatechest [<location>] ({open}/close) (sound:{true}/false)", 1);
        
        registerCoreMember(AnnounceCommand.class,
                "ANNOUNCE", "announce [\"<text>\"] (to_ops) (to_flagged:<flag>)", 1);
        
        registerCoreMember(AssignmentCommand.class,
                "ASSIGNMENT", "assignment [{set}/remove] (script:<name>)", 1);
        
        registerCoreMember(AttackCommand.class, 
                "ATTACK", "attack (cancel) (<entity>|...) (target:<entity>)", 0);

        registerCoreMember(BreakCommand.class,
                "BREAK", "break [<location>] (entity:<entity>) (radius:<#.#>)", 1);
        
        registerCoreMember(BurnCommand.class,
                "BURN", "burn [<entity>|...] (duration:<value>)", 1);
        
        registerCoreMember(CastCommand.class, 
                "CAST, POTION", "cast [<effect>] (remove) (duration:<value>) (power:<#>) (<entity>|...)", 1);
        
        registerCoreMember(ChatCommand.class, 
                "CHAT", "chat [\"<text>\"] (targets:<entity>|...)", 1);
        
        registerCoreMember(ChunkLoadCommand.class,
                "CHUNKLOAD", "chunkload ({add}/remove/removeall) [<location>] (duration:<value>)", 1);

        registerCoreMember(CompassCommand.class,
                "COMPASS", "compass [<location>]", 1);

        // <--[command]
        // @Name Cooldown
        // @Usage cooldown (duration:<value>) (global) (script:<name>)
        // @Stable 1.0
        // @Short Temporarily disables a script-container from meeting requirements.
        // @Author aufdemrand
        //
        // @Description
        // Cools down a script-container. If an interact-container, when on cooldown, scripts will not pass a
        // requirements check allowing the next highest priority script to trigger. If any other type of script, a
        // manual requirements check (<s@script_name.requirements.check>) will also return false until the cooldown
        // period is completed. Cooldown requires a type (player or global), a script, and a duration. It also requires
        // a valid link to a dPlayer if using player-type cooldown.
        //
        // Cooldown periods are persistent through a server restart as they are saved in the saves.yml.
        //
        // @Tags
        // <s@script_name.cooled_down[player]> will return whether the script is cooled down
        // <s@script_name.cooldown> will return the duration of the cooldown in progress.
        // <s@requirements.check> will also check script cooldown, as well as any requirements.
        //
        // @Usage
        // Use to keep the current interact script from meeting requirements.
        // - cooldown 20m
        //
        // @Usage
        // Use to keep a player from activating a script for a specified duration.
        // - cooldown 11h s:s@bonus_script
        // - cooldown 5s s:s@hit_indicator
        //
        // @Usage
        // Use the 'global' argument to indicate the script to be on cooldown for all players.
        // - cooldown global 24h s:s@daily_treasure_offering
        //
        // @Example
        //
        // -->

        registerCoreMember(CooldownCommand.class,
                "COOLDOWN", "cooldown (duration:<value>) (global) (script:<name>)", 1);

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
                "DISPLAYITEM", "displayitem (remove) [<item>] [<location>] (duration:<value>)", 2);

        registerCoreMember(DropCommand.class, 
                "DROP", "drop [<item>/<entity>/<xp>] [<location>] (qty:<#>)", 1);
        
        registerCoreMember(EngageCommand.class,
                "ENGAGE", "engage (duration:<value>)", 0);

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
                "FLAG", "flag ({player}/npc/global) [<name>([<#>])](:<action>)[:<value>] (duration:<value>)", 1);
        
        registerCoreMember(FlyCommand.class,
                "FLY", "fly (cancel) [<entity>|...] (origin:<location>) (destinations:<location>|...) (speed:<#.#>)", 1);
        
        registerCoreMember(FollowCommand.class, 
                "FOLLOW", "follow (stop) (lead:<#.#>) (target:<entity>)", 0);
        
        registerCoreMember(ForEachCommand.class,
                "FOREACH", "foreach [<object>|...] [<commands>]", 1);
        
        registerCoreMember(GiveCommand.class,
                "GIVE", "give [money/<item>] (qty:<#>) (engrave)", 1);
        
        registerCoreMember(GroupCommand.class,
                "GROUP", "group [add/remove] [<group>] (world:<name>)", 2);
        
        registerCoreMember(HeadCommand.class,
                "HEAD", "head (player) [skin:<name>]", 0);
        
        registerCoreMember(HealCommand.class, 
                "HEAL", "heal (<#.#>) (<entity>|...)", 0);
        
        registerCoreMember(HealthCommand.class,
                "HEALTH", "health [<#>]", 1);
        
        registerCoreMember(HurtCommand.class,
                "HURT", "hurt (<#.#>) (<entity>|...)", 0);
        
        registerCoreMember(IfCommand.class, 
                "IF", "if [<value>] (!)(<operator> <value>) (&&/|| ...) [<commands>] (else <commands>)", 2);

        registerCoreMember(InventoryCommand.class,
                "INVENTORY", "inventory [open/copy/move/swap/add/remove/keep/exclude/fill/clear] [destination:<inventory>] (origin:<inventory>)", 2);
        
        registerCoreMember(InvisibleCommand.class,
                "INVISIBLE", "invisible [player/npc] [state:true/false/toggle]", 2);
        
        registerCoreMember(LeashCommand.class,
                "LEASH", "leash (cancel) [<entity>|...] (holder:<entity>/<location>)", 1);
        
        registerCoreMember(ListenCommand.class,
                "LISTEN", "listen ({new}/cancel/finish) [kill/block/item/itemdrop/travel] [<requirements>] [script:<name>] (id:<name>)", 2);
        
        registerCoreMember(LogCommand.class,
                "LOG", "log [<text>] (type:severe/info/warning/fine/finer/finest) [file:<name>]", 2);
        
        registerCoreMember(LookCommand.class,
                "LOOK", "look (<entity>|...) [<location>] (duration:<duration>)", 1);
        
        registerCoreMember(LookcloseCommand.class,
                "LOOKCLOSE", "lookclose [state:true/false]", 1);
        
        registerCoreMember(MidiCommand.class,
                "MIDI", "midi [file:<name>] [<location>/listeners:<player>|...] (tempo:<#.#>)", 1);
        
        registerCoreMember(MountCommand.class,
                "MOUNT", "mount (cancel) [<entity>|...] (<location>)", 0);
        
        registerCoreMember(ModifyBlockCommand.class,
                "MODIFYBLOCK", "modifyblock [<location>] [<block>] (radius:<#>) (height:<#>) (depth:<#>)", 2);
        
        registerCoreMember(NameplateCommand.class,
                "NAMEPLATE", "nameplate [<chatcolor>] [set:<text>] (target:<player>)  +--> Requires ProtocolLib", 1);
        
        registerCoreMember(NarrateCommand.class,
                "NARRATE", "narrate [\"<text>\"] (targets:<player>|...) (format:<name>)", 1);
        
        registerCoreMember(NoteCommand.class,
                "NOTE", "note [<Notable dObject>] [as:<name>]", 2);
        
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
                "REMOVE", "remove [<entity>|...] (region:<name>)", 0);

        registerCoreMember(RenameCommand.class,
                "RENAME", "rename [<npc>] [<name>]", 1);

        registerCoreMember(RepeatCommand.class,
                "REPEAT", "repeat [<amount>] [<commands>]", 1);

        registerCoreMember(ResetCommand.class,
                "RESET", "reset [fails/finishes/cooldown] (script:<name>)", 1);

        registerCoreMember(RunCommand.class,
                "RUN", "run [<script>] (path:<name>) (as:<player>/<npc>) (define:<element>|...) (id:<name>) (delay:<value>) (loop) (qty:<#>)", 1);

        registerCoreMember(RuntaskCommand.class,
                "RUNTASK", "runtask [<name>] (instantly) (queue(:<name>)) (delay:<#>) (define:<element>|...)", 1);

        registerCoreMember(ScoreboardCommand.class,
                "SCOREBOARD", "scoreboard [set/remove/show/hide] [<name>] [value:<name>] (priority:<#>)", 1);
        
        registerCoreMember(ScribeCommand.class,
                "SCRIBE", "scribe [script:<name>] (give/drop/equip) (<item>) (<location>)", 1);

        registerCoreMember(ShootCommand.class,
                "SHOOT", "shoot [<entity>|...] (origin:<entity>/<location>) (destination:<location>) [{calculated} (height:<#.#>) (gravity:<#.#>)]/[custom speed:<#.#> duration:<value>] (script:<name>)", 1);
        
        registerCoreMember(ShowFakeCommand.class,
                "SHOWFAKE", "showfake [<material>] [<location>|...] (d:<duration>{10s})", 2);

        registerCoreMember(SignCommand.class, 
                "SIGN", "sign (type:{sign_post}/wall_sign) [\"<line>|...\"] [<location>] (direction:n/e/w/s)", 1);
        
        registerCoreMember(SitCommand.class, 
                "SIT", "sit (<location>)", 0);

        registerCoreMember(SpawnCommand.class,
                "SPAWN", "spawn [<entity>|...] (<location>) (target:<entity>)", 1);
        
        registerCoreMember(StandCommand.class, 
                "STAND", "stand", 0);
        
        registerCoreMember(StrikeCommand.class,
                "STRIKE", "strike (no_damage) [<location>]", 1);
        
        registerCoreMember(SwitchCommand.class,
                "SWITCH", "switch [<location>] (state:[{toggle}/on/off]) (duration:<value>)", 1);
        
        registerCoreMember(TakeCommand.class,
                "TAKE", "take [money/iteminhand/<item>] (qty:<#>)", 1);
        
        registerCoreMember(TeleportCommand.class,
                "TELEPORT", "teleport (<entity>|...) (<location>)", 1);
        
        registerCoreMember(TimeCommand.class,
                "TIME", "time [type:{global}/player] [<value>] (world:<name>)", 1);
        
        registerCoreMember(TriggerCommand.class, 
                "TRIGGER", "trigger [name:chat/click/damage/proximity] (state:true/false) (cooldown:<#.#>) (radius:<#>)", 2);
        
        registerCoreMember(ViewerCommand.class,
                "VIEWER", "viewer ({create <location>}/modify/remove) [id:<name>] (type:{sign_post}/wall_sign) (display:{location}/score/logged_in) (direction:n/e/w/s)", 2);
        
        registerCoreMember(VulnerableCommand.class, 
                "VULNERABLE", "vulnerable (state:{true}/false/toggle)", 0);
        
        registerCoreMember(WaitCommand.class, 
                "WAIT", "wait (<duration>) (queue:<name>)", 0);

        registerCoreMember(WalkCommand.class,
                "WALK, WALKTO", "walk [<location>] (speed:<#>) (auto_range)", 1);

        registerCoreMember(WeatherCommand.class,
                "WEATHER", "weather [type:{global}/player] [sunny/storm/thunder] (world:<name>)", 1);
        
        registerCoreMember(YamlCommand.class,
                "YAML", "yaml [load/create/save:<file>]/[write:<key>]/[write:<key> value:<value>] [id:<name>]", 1);

        registerCoreMember(ZapCommand.class, 
                "ZAP", "zap (<script>:)[<step>] (<duration>)", 0);
        
        
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
