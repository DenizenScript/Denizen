package net.aufdemrand.denizen.scripts.commands;

import net.aufdemrand.denizen.scripts.commands.core.*;
import net.aufdemrand.denizen.scripts.commands.entity.*;
import net.aufdemrand.denizen.scripts.commands.item.*;
import net.aufdemrand.denizen.scripts.commands.npc.*;
import net.aufdemrand.denizen.scripts.commands.player.*;
import net.aufdemrand.denizen.scripts.commands.server.*;
import net.aufdemrand.denizen.scripts.commands.world.*;
import net.aufdemrand.denizen.utilities.debugging.dB;
import net.aufdemrand.denizen.utilities.depends.Depends;
import net.aufdemrand.denizencore.exceptions.InvalidArgumentsException;
import net.aufdemrand.denizencore.scripts.ScriptEntry;
import net.aufdemrand.denizencore.scripts.commands.AbstractCommand;
import net.aufdemrand.denizencore.scripts.commands.CommandRegistry;

public class BukkitCommandRegistry extends CommandRegistry {

    public static class AutoNoCitizensCommand extends AbstractCommand {

        public static void registerFor(String name) {
            AutoNoCitizensCommand cmd = new AutoNoCitizensCommand();
            cmd.name = name;
            cmd.activate().as(name).withOptions("Requires Citizens", 0);
        }

        public String name;

        @Override
        public void parseArgs(ScriptEntry scriptEntry) throws InvalidArgumentsException {
        }

        @Override
        public void execute(ScriptEntry scriptEntry) {
            dB.echoError("The command '" + name + "' is only available when Citizens is on the server.");
        }
    }

    @Override
    public void registerCoreMembers() {

        registerCoreCommands();

        registerCoreMember(ActionCommand.class,"ACTION", "action [<action name>|...] (<npc>|...) (context:<name>|<object>|...)", 1);
        registerCoreMember(ActionBarCommand.class, "ACTIONBAR", "actionbar [<text>] (targets:<player>|...)", 1);
        registerCoreMember(AdvancementCommand.class, "ADVANCEMENT", "advancement [id:<name>] (delete/grant:<players>/revoke:<players>/{create}) (parent:<name>) (icon:<item>) (title:<text>) (description:<text>) (background:<key>) (frame:<type>) (toast:<boolean>) (announce:<boolean>) (hidden:<boolean>)", 1);
        registerCoreMember(AgeCommand.class, "AGE", "age [<entity>|...] (adult/baby/<age>) (lock)", 1);
        registerCoreMember(AnchorCommand.class, "ANCHOR", "anchor [id:<name>] [assume/remove/add <location>/walkto/walknear (r:#)]", 2);
        if (Depends.citizens != null) {
            registerCoreMember(AnimateCommand.class, "ANIMATE", "animate [<entity>|...] [animation:<name>]", 2);
        }
        else {
            AutoNoCitizensCommand.registerFor("ANIMATE");
        }
        registerCoreMember(AnimateChestCommand.class, "ANIMATECHEST", "animatechest [<location>] ({open}/close) (sound:{true}/false) (<player>|...)", 1);
        registerCoreMember(AnnounceCommand.class, "ANNOUNCE", "announce [<text>] (to_ops/to_console/to_flagged:<flag_name>) (format:<name>)", 1);
        if (Depends.citizens != null) {
            registerCoreMember(AssignmentCommand.class, "ASSIGNMENT", "assignment [set/remove] (script:<name>)", 1);
        }
        else {
            AutoNoCitizensCommand.registerFor("ASSIGNMENT");
        }
        registerCoreMember(AttackCommand.class, "ATTACK", "attack (<entity>|...) (target:<entity>/cancel)", 0);
        registerCoreMember(BanCommand.class, "BAN", "ban ({add}/remove) [<player>|.../addresses:<address>|...] (reason:<text>) (duration:<duration>) (source:<text>)", 1);
        registerCoreMember(BlockCrackCommand.class, "BLOCKCRACK", "blockcrack [<location>] [progress:<#>] (stack) (players:<player>|...)", 2);
        if (Depends.citizens != null) {
            registerCoreMember(BreakCommand.class, "BREAK", "break [<location>] (<npc>) (radius:<#.#>)", 1);
        }
        else {
            AutoNoCitizensCommand.registerFor("BREAK");
        }
        registerCoreMember(BossBarCommand.class, "BOSSBAR", "bossbar ({create}/update/remove) [<id>] (players:<player>|...) (title:<title>) (progress:<#.#>) (color:<color>) (style:<style>) (flags:<flag>|...)", 1);
        registerCoreMember(BurnCommand.class, "BURN", "burn [<entity>|...] (duration:<value>)", 1);
        registerCoreMember(CastCommand.class, "CAST", "cast [<effect>] (remove) (duration:<value>) (power:<#>) (<entity>|...) (no_ambient) (hide_particles)", 1);
        if (Depends.citizens != null) {
            registerCoreMember(ChatCommand.class, "CHAT", "chat [<text>] (no_target/targets:<entity>|...) (talkers:<entity>|...) (range:<#.#>)", 1);
        }
        else {
            AutoNoCitizensCommand.registerFor("CHAT");
        }
        registerCoreMember(ChunkLoadCommand.class, "CHUNKLOAD", "chunkload ({add}/remove/removeall) [<chunk>] (duration:<value>)", 1);
        registerCoreMember(CompassCommand.class, "COMPASS", "compass [<location>/reset]", 1);
        registerCoreMember(CooldownCommand.class, "COOLDOWN", "cooldown [<duration>] (global) (s:<script>)", 1);
        registerCoreMember(CopyBlockCommand.class, "COPYBLOCK", "copyblock [<location>/<cuboid>] [to:<location>] (remove_original)", 1);
        if (Depends.citizens != null) {
            registerCoreMember(CreateCommand.class, "CREATE", "create [<entity>] [<name>] (<location>)", 1);
        }
        else {
            AutoNoCitizensCommand.registerFor("CREATE");
        }
        registerCoreMember(CreateWorldCommand.class, "CREATEWORLD", "createworld [<name>] (g:<generator>) (worldtype:<type>) (environment:<environment>) (copy_from:<world>) (seed:<seed>)", 1);
        if (Depends.citizens != null) {
            registerCoreMember(DespawnCommand.class, "DESPAWN", "despawn (<npc>)", 0);
        }
        else {
            AutoNoCitizensCommand.registerFor("DESPAWN");
        }
        if (Depends.citizens != null) {
            registerCoreMember(DisengageCommand.class, "DISENGAGE", "disengage", 0);
        }
        else {
            AutoNoCitizensCommand.registerFor("DISENGAGE");
        }
        registerCoreMember(DisplayItemCommand.class, "DISPLAYITEM", "displayitem [<item>] [<location>] (duration:<value>)", 2);
        registerCoreMember(DropCommand.class, "DROP", "drop [<entity_type>/xp/<item>|...] (<location>) (qty:<#>) (speed:<#.#>) (delay:<duration>)", 1);
        if (Depends.citizens != null) {
            registerCoreMember(EngageCommand.class, "ENGAGE", "engage (<duration>)", 0);
        }
        else {
            AutoNoCitizensCommand.registerFor("ENGAGE");
        }
        registerCoreMember(EquipCommand.class, "EQUIP", "equip (<entity>|...) (offhand:<item>) (hand:<item>) (head:<item>) (chest:<item>) (legs:<item>) (boots:<item>) (saddle:<item>) (horse_armor:<item>)", 1);
        registerCoreMember(ExecuteCommand.class, "EXECUTE", "execute [as_player/as_op/as_npc/as_server] [<Bukkit command>] (silent)", 2);
        registerCoreMember(ExperienceCommand.class, "EXPERIENCE", "experience [{set}/give/take] (level) [<#>]", 2);
        registerCoreMember(ExplodeCommand.class, "EXPLODE", "explode (power:<#.#>) (<location>) (fire) (breakblocks)", 0);
        registerCoreMember(FakeItemCommand.class, "FAKEITEM", "fakeitem [<item>] [slot:<slot>] (duration:<duration>) (players:<player>|...) (player_only)", 2);
        registerCoreMember(FeedCommand.class, "FEED", "feed (amt:<#>) (target:<entity>)", 0);
        registerCoreMember(FileCopyCommand.class, "filecopy", "filecopy [origin:<origin>] [destination:<destination>] (overwrite)", 2);
        registerCoreMember(FireworkCommand.class, "FIREWORK", "firework (<location>) (power:<#>) (<type>/random) (primary:<color>|...) (fade:<color>|...) (flicker) (trail)", 0);
        registerCoreMember(FishCommand.class, "FISH", "fish [<location>] (catch:{none}/default/junk/treasure/fish) (stop) (chance:<#>)", 1);
        registerCoreMember(FlagCommand.class, "FLAG", "flag ({player}/npc/server/<entity>) [<name>([<#>])](:<action>)[:<value>] (duration:<value>)", 1);
        registerCoreMember(FlyCommand.class, "FLY", "fly (cancel) [<entity>|...] (controller:<player>) (origin:<location>) (destinations:<location>|...) (speed:<#.#>) (rotationthreshold:<#.#>)", 1);
        registerCoreMember(FollowCommand.class, "FOLLOW", "follow (followers:<entity>|...) (stop) (lead:<#.#>) (max:<#.#>) (speed:<#.#>) (target:<entity>) (allow_wander)", 0);
        registerCoreMember(GameRuleCommand.class, "GAMERULE", "gamerule [<world>] [<rule>] [<value>]", 3);
        registerCoreMember(GiveCommand.class, "GIVE", "give [money/xp/<item>|...] (qty:<#>) (engrave) (unlimit_stack_size) (to:<inventory>) (slot:<slot>)", 1);
        registerCoreMember(GlowCommand.class, "GLOW", "glow [<entity>|...] (<should glow>)", 1);
        registerCoreMember(GroupCommand.class, "GROUP", "group [add/remove/set] [<group>] (<world>)", 2);
        registerCoreMember(HeadCommand.class, "HEAD", "head (<entity>|...) [skin:<player_name>]", 1);
        registerCoreMember(HealCommand.class, "HEAL", "heal (<#.#>) ({player}/<entity>|...)", 0);
        registerCoreMember(HealthCommand.class, "HEALTH", "health ({npc}/<entity>|...) [<#>] (state:{true}/false/toggle)", 1);
        registerCoreMember(HurtCommand.class, "HURT", "hurt (<#.#>) (<entity>|...) (cause:<cause>)", 0);
        registerCoreMember(InventoryCommand.class, "INVENTORY", "inventory [open/close/copy/move/swap/add/remove/set/keep/exclude/fill/clear/update/adjust <mechanism>:<value>] (destination:<inventory>) (origin:<inventory>/<item>|...) (slot:<slot>)", 1);
        registerCoreMember(InjectCommand.class, "INJECT", "inject (locally) [<script>] (path:<name>) (instantly)", 1);
        registerCoreMember(InvisibleCommand.class, "INVISIBLE", "invisible [<entity>] (state:true/false/toggle)", 1);
        registerCoreMember(ItemCooldownCommand.class, "ITEMCOOLDOWN", "itemcooldown [<material>|...] (duration:<duration>)", 1);
        registerCoreMember(KickCommand.class, "KICK", "kick [<player>|...] (reason:<text>)", 1);
        registerCoreMember(LeashCommand.class, "LEASH", "leash (cancel) [<entity>|...] (holder:<entity>/<location>)", 1);
        registerCoreMember(LightCommand.class, "LIGHT", "light [<location>] [<#>/reset] (duration:<duration>)", 2);
        registerCoreMember(LogCommand.class, "LOG", "log [<text>] (type:{info}/severe/warning/fine/finer/finest/none/clear) [file:<name>]", 2);
        registerCoreMember(LookCommand.class, "LOOK", "look (<entity>|...) [<location>] (duration:<duration>)", 1);
        if (Depends.citizens != null) {
            registerCoreMember(LookcloseCommand.class, "LOOKCLOSE", "lookclose (<npc>) (state:<true/false>) (range:<#>) (realistic)", 0);
        }
        else {
            AutoNoCitizensCommand.registerFor("LOOKCLOSE");
        }
        registerCoreMember(MapCommand.class, "MAP", "map [<#>/new:<world>] [reset:<location>/image:<file> (resize)/script:<script>] (x:<#>) (y:<#>)", 2);
        registerCoreMember(MidiCommand.class, "MIDI", "midi (cancel) [<file>] (<location>/<entity>|...) (tempo:<#.#>) (volume:<#.#>)", 1);
        registerCoreMember(MoneyCommand.class, "MONEY", "money [give/take/set] (quantity:<#.#>) (players:<player>|...)", 1);
        registerCoreMember(MountCommand.class, "MOUNT", "mount (cancel) [<entity>|...] (<location>)", 0);
        registerCoreMember(ModifyBlockCommand.class, "MODIFYBLOCK", "modifyblock [<location>|.../<ellipsoid>/<cuboid>] [<material>|...] (radius:<#>) (height:<#>) (depth:<#>) (no_physics/naturally) (delayed) (<script>) (<percent chance>|...)", 2);
        registerCoreMember(NarrateCommand.class, "NARRATE", "narrate [<text>] (targets:<player>|...) (format:<name>)", 1);
        registerCoreMember(NBTCommand.class, "NBT", "nbt [<item>] [<key>:<value>]", 2);
        registerCoreMember(NoteCommand.class, "NOTE", "note [<Notable dObject>/remove] [as:<name>]", 2);
        registerCoreMember(OpenTradesCommand.class, "OPENTRADES", "opentrades [<entity>/<trade>|...] (title:<title>) (players:<player>|...)", 1);
        registerCoreMember(OxygenCommand.class, "OXYGEN", "oxygen [<#>] (type:{remaining}/maximum) (mode:{set}/add/remove)", 1);
        if (Depends.citizens != null) {
            registerCoreMember(PauseCommand.class, "PAUSE", "pause [waypoints/activity] (<duration>)", 1);
            registerCoreMember(PauseCommand.class, "RESUME", "resume [waypoints/activity] (<duration>)", 1);
        }
        else {
            AutoNoCitizensCommand.registerFor("PAUSE");
            AutoNoCitizensCommand.registerFor("RESUME");
        }
        registerCoreMember(PlayEffectCommand.class, "PLAYEFFECT", "playeffect [effect:<name>] [at:<location>|...] (data:<#.#>) (visibility:<#.#>) (qty:<#>) (offset:<#.#>,<#.#>,<#.#>) (targets:<player>|...)", 2);
        registerCoreMember(PlaySoundCommand.class, "PLAYSOUND", "playsound [<location>|.../<player>|...] [sound:<name>] (volume:<#.#>) (pitch:<#.#>) (custom) (sound_category:<category name>)", 2);
        registerCoreMember(PermissionCommand.class, "PERMISSION", "permission [add/remove] [permission] (group:<name>) (<world>)", 2);
        if (Depends.citizens != null) {
            registerCoreMember(PoseCommand.class, "POSE", "pose (add/remove/{assume}) [id:<name>] (player/{npc}) (<location>)", 1);
        }
        else {
            AutoNoCitizensCommand.registerFor("POSE");
        }
        registerCoreMember(PushCommand.class, "PUSH", "push [<entity>|...] (origin:<entity>/<location>) (destination:<location>) (speed:<#.#>) (<duration>) (script:<name>) (def:<element>|...) (force_along) (precision:<#>) (no_rotate) (no_damage)", 1);
        registerCoreMember(PushableCommand.class, "PUSHABLE", "pushable (state:true/false/{toggle}) (delay:<duration>) (returnable:true/false)", 0);
        registerCoreMember(QueueCommand.class, "QUEUE", "queue (<queue>) [clear/stop/pause/resume/delay:<#>]", 1);
        registerCoreMember(RandomCommand.class, "RANDOM", "random [<#>/<commands>]", 0);
        registerCoreMember(RemoveCommand.class, "REMOVE", "remove [<entity>|...] (<world>)", 1);
        if (Depends.citizens != null) {
            registerCoreMember(RenameCommand.class, "RENAME", "rename [<name>]", 1);
        }
        else {
            AutoNoCitizensCommand.registerFor("RENAME");
        }
        registerCoreMember(RepeatCommand.class, "REPEAT", "repeat [stop/next/<amount>] [<commands>] (as:<name>)", 1);
        registerCoreMember(ResetCommand.class, "RESET", "reset (<player>|...) [cooldown/saves/global_cooldown] (<script>)", 1);
        registerCoreMember(RotateCommand.class, "ROTATE", "rotate (cancel) (<entity>|...) (yaw:<#.#>) (pitch:<#.#>) (infinite/duration:<duration>) (frequency:<duration>)", 0);
        registerCoreMember(RunCommand.class, "RUN", "run (locally) [<script>] (path:<name>) (def:<element>|...) (id:<name>) (instantly) (speed:<value>) (delay:<value>)", 1);
        registerCoreMember(SchematicCommand.class, "SCHEMATIC", "schematic [create/load/unload/rotate/paste/save/flip_x/flip_y/flip_z] [name:<name>] (filename:<name>) (angle:<#>) (<location>) (<cuboid>) (delayed) (noair)", 2);
        registerCoreMember(ScoreboardCommand.class, "SCOREBOARD", "scoreboard ({add}/remove) (viewers:<player>|...) (lines:<player>/<text>|...) (id:<value>/{main}) (objective:<value>) (criteria:<criteria>/{dummy}) (score:<#>) (displayslot:<value>/{sidebar}/none)", 1);
        registerCoreMember(ScribeCommand.class, "SCRIBE", "scribe [<script>] (<item>/give/equip/{drop <location>})", 1);
        registerCoreMember(ShootCommand.class, "SHOOT", "shoot [<entity>|...] (origin:<entity>/<location>) (destination:<location>) (height:<#.#>) (speed:<#.#>) (script:<name>) (def:<element>|...) (shooter:<entity>) (spread:<#.#>) (lead:<location>) (no_rotate)", 1);
        registerCoreMember(ShowFakeCommand.class, "SHOWFAKE", "showfake [<material>|.../cancel] [<location>|...] (players:<player>|...) (d:<duration>{10s})", 2);
        registerCoreMember(SidebarCommand.class, "SIDEBAR", "sidebar (add/remove/{set}) (title:<title>) (lines:<#>|...) (values:<line>|...) (start:<#>/{num_of_lines}) (increment:<#>/{-1}) (players:<player>|...) (per_player)", 1);
        registerCoreMember(SignCommand.class, "SIGN", "sign (type:{automatic}/sign_post/wall_sign) [\"<line>|...\"] [<location>] (direction:n/s/e/w)", 1);
        registerCoreMember(SitCommand.class, "SIT", "sit (<location>)", 0);
        registerCoreMember(SpawnCommand.class, "SPAWN", "spawn [<entity>|...] (<location>) (target:<entity>) (persistent)", 1);
        if (Depends.citizens != null) {
            registerCoreMember(StandCommand.class, "STAND", "stand", 0);
        }
        else {
            AutoNoCitizensCommand.registerFor("STAND");
        }
        registerCoreMember(StatisticCommand.class, "STATISTIC", "statistic [<statistic>] [add/take/set] (<#>) (qualifier:<material>/<entity>) (players:<player>|...)", 2);
        registerCoreMember(StrikeCommand.class, "STRIKE", "strike (no_damage) [<location>]", 1);
        registerCoreMember(SwitchCommand.class, "SWITCH", "switch [<location>|...] (state:[{toggle}/on/off]) (duration:<value>)", 1);
        registerCoreMember(TakeCommand.class, "TAKE", "take [money/iteminhand/scriptname:<name>/bydisplay:<name>/bycover:<title>|<author>/slot:<slot>/nbt:<key>/<item>|...] (qty:<#>) (from:<inventory>)", 1);
        registerCoreMember(TeamCommand.class, "TEAM", "team (id:<scoreboard>/{main}) [name:<team>] (add:<entry>|...) (remove:<entry>|...) (prefix:<prefix>) (suffix:<suffix>)", 2);
        registerCoreMember(TeleportCommand.class, "TELEPORT", "teleport (<entity>|...) [<location>]", 1);
        registerCoreMember(TimeCommand.class, "TIME", "time ({global}/player) [<time duration>] (<world>)", 1);
        registerCoreMember(TitleCommand.class, "TITLE", "title (title:<text>) (subtitle:<text>) (fade_in:<duration>/{1s}) (stay:<duration>/{3s}) (fade_out:<duration>/{1s}) (targets:<player>|...)", 1);
        registerCoreMember(ToastCommand.class, "TOAST", "toast [<text>] (targets:<player>|...) (icon:<item>) (frame:{task}/challenge/goal) (background:<texture>)", 1);
        if (Depends.citizens != null) {
            registerCoreMember(TraitCommand.class, "TRAIT", "trait (state:true/false/{toggle}) [<trait>]", 1);
        }
        else {
            AutoNoCitizensCommand.registerFor("TRAIT");
        }
        registerCoreMember(TriggerCommand.class, "TRIGGER", "trigger [name:chat/click/damage/proximity] (state:{toggle}/true/false) (cooldown:<duration>) (radius:<#>)", 1);
        if (Depends.citizens != null) {
            registerCoreMember(VulnerableCommand.class, "VULNERABLE", "vulnerable (state:{true}/false/toggle)", 0);
        }
        else {
            AutoNoCitizensCommand.registerFor("VULNERABLE");
        }
        registerCoreMember(WalkCommand.class, "WALK", "walk (<entity>|...) [<location>/stop] (speed:<#>) (auto_range) (radius:<#.#>) (lookat:<location>)", 1);
        registerCoreMember(WeatherCommand.class, "WEATHER", "weather [type:{global}/player] [sunny/storm/thunder] (world:<name>)", 1);
        registerCoreMember(WhileCommand.class, "WHILE", "while [stop/next/<comparison tag>] [<commands>]", 1);
        registerCoreMember(WorldBorderCommand.class, "WORLDBORDER", "worldborder [<world>/<player>|...] (center:<location>) (size:<#.#>) (current_size:<#.#>) (damage:<#.#>) (damagebuffer:<#.#>) (warningdistance:<#>) (warningtime:<duration>) (duration:<duration>) (reset)", 2);
        registerCoreMember(YamlCommand.class, "YAML", "yaml [create]/[load:<file> (fix_formatting)]/[loadtext:<text> (fix_formatting)]/[unload]/[savefile:<file>]/[copykey:<source key> <target key> (to_id:<name>)]/[set <key>([<#>])(:<action>):<value>] [id:<name>]", 2);
        registerCoreMember(ZapCommand.class, "ZAP", "zap (<script>) [<step>] (<duration>)", 0);

        dB.echoApproval("Loaded core commands: " + instances.keySet().toString());
    }
}
