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

		// <--[command]
		// @Name anchor
		// @Usage anchor [id:<name>] [assume/add/remove/walkto/walknear] (range:<#>)
		// @Required 2
		// @Stable Todo
		// @Short Todo
		// @Author Todo
		// @Description
		// Todo
		// @Tags
		// Todo
		// @Usage
		// Todo
		// @Example
		// Todo
		// -->
        registerCoreMember(AnchorCommand.class,
                "ANCHOR", "anchor [id:<name>] [assume/add/remove/walkto/walknear] (range:<#>)", 2);
                
		// <--[command]
		// @Name animate
		// @Usage animate [<entity>|...] [animation:<name>]
		// @Required 1
		// @Stable Todo
		// @Short Todo
		// @Author Todo
		// @Description
		// Todo
		// @Tags
		// Todo
		// @Usage
		// Todo
		// @Example
		// Todo
		// -->
        registerCoreMember(AnimateCommand.class,
                "ANIMATE", "animate [<entity>|...] [animation:<name>]", 1);
        
		// <--[command]
		// @Name animatechest
		// @Usage animatechest [<location>] ({open}/close) (sound:{true}/false)
		// @Required 1
		// @Stable Todo
		// @Short Todo
		// @Author Todo
		// @Description
		// Todo
		// @Tags
		// Todo
		// @Usage
		// Todo
		// @Example
		// Todo
		// -->
        registerCoreMember(AnimateChestCommand.class,
                "ANIMATECHEST", "animatechest [<location>] ({open}/close) (sound:{true}/false)", 1);
        
		// <--[command]
		// @Name announce
		// @Usage announce ["<text>"] (to_ops) (to_flagged:<flag>)
		// @Required 1
		// @Stable Todo
		// @Short Todo
		// @Author Todo
		// @Description
		// Todo
		// @Tags
		// Todo
		// @Usage
		// Todo
		// @Example
		// Todo
		// -->
        registerCoreMember(AnnounceCommand.class,
                "ANNOUNCE", "announce [\"<text>\"] (to_ops) (to_flagged:<flag>)", 1);
        
		// <--[command]
		// @Name assignment
		// @Usage assignment [{set}/remove] (script:<name>)
		// @Required 1
		// @Stable Todo
		// @Short Todo
		// @Author Todo
		// @Description
		// Todo
		// @Tags
		// Todo
		// @Usage
		// Todo
		// @Example
		// Todo
		// -->
        registerCoreMember(AssignmentCommand.class,
                "ASSIGNMENT", "assignment [{set}/remove] (script:<name>)", 1);
        
		// <--[command]
		// @Name attack
		// @Usage attack (cancel) (<entity>|...) (target:<entity>)
		// @Required 0
		// @Stable Todo
		// @Short Todo
		// @Author Todo
		// @Description
		// Todo
		// @Tags
		// Todo
		// @Usage
		// Todo
		// @Example
		// Todo
		// -->
        registerCoreMember(AttackCommand.class, 
                "ATTACK", "attack (cancel) (<entity>|...) (target:<entity>)", 0);

		// <--[command]
		// @Name break
		// @Usage break [<location>] (entity:<entity>) (radius:<#.#>)
		// @Required 1
		// @Stable Todo
		// @Short Todo
		// @Author Todo
		// @Description
		// Todo
		// @Tags
		// Todo
		// @Usage
		// Todo
		// @Example
		// Todo
		// -->
        registerCoreMember(BreakCommand.class,
                "BREAK", "break [<location>] (entity:<entity>) (radius:<#.#>)", 1);
        
		// <--[command]
		// @Name burn
		// @Usage burn [<entity>|...] (duration:<value>)
		// @Required 1
		// @Stable Todo
		// @Short Todo
		// @Author Todo
		// @Description
		// Todo
		// @Tags
		// Todo
		// @Usage
		// Todo
		// @Example
		// Todo
		// -->
        registerCoreMember(BurnCommand.class,
                "BURN", "burn [<entity>|...] (duration:<value>)", 1);
        
		// <--[command]
		// @Name cast
		// @Usage cast [<effect>] (remove) (duration:<value>) (power:<#>) (<entity>|...)
		// @Required 1
		// @Stable Todo
		// @Short Todo
		// @Author Todo
		// @Description
		// Todo
		// @Tags
		// Todo
		// @Usage
		// Todo
		// @Example
		// Todo
		// -->
		// <--[command]
		// @Name potion
		// @Usage potion [<effect>] (remove) (duration:<value>) (power:<#>) (<entity>|...)
		// @Required 1
		// @Stable Todo
		// @Short Todo
		// @Author Todo
		// @Description
		// Todo
		// @Tags
		// Todo
		// @Usage
		// Todo
		// @Example
		// Todo
		// -->
        registerCoreMember(CastCommand.class, 
                "CAST, POTION", "cast [<effect>] (remove) (duration:<value>) (power:<#>) (<entity>|...)", 1);
        
		// <--[command]
		// @Name chat
		// @Usage chat ["<text>"] (targets:<entity>|...)
		// @Required 1
		// @Stable Todo
		// @Short Todo
		// @Author Todo
		// @Description
		// Todo
		// @Tags
		// Todo
		// @Usage
		// Todo
		// @Example
		// Todo
		// -->
        registerCoreMember(ChatCommand.class, 
                "CHAT", "chat [\"<text>\"] (targets:<entity>|...)", 1);
        
		// <--[command]
		// @Name chunkload
		// @Usage chunkload ({add}/remove/removeall) [<location>] (duration:<value>)
		// @Required 1
		// @Stable Todo
		// @Short Todo
		// @Author Todo
		// @Description
		// Todo
		// @Tags
		// Todo
		// @Usage
		// Todo
		// @Example
		// Todo
		// -->
        registerCoreMember(ChunkLoadCommand.class,
                "CHUNKLOAD", "chunkload ({add}/remove/removeall) [<location>] (duration:<value>)", 1);

		// <--[command]
		// @Name compass
		// @Usage compass [<location>]
		// @Required 1
		// @Stable Todo
		// @Short Todo
		// @Author Todo
		// @Description
		// Todo
		// @Tags
		// Todo
		// @Usage
		// Todo
		// @Example
		// Todo
		// -->
        registerCoreMember(CompassCommand.class,
                "COMPASS", "compass [<location>]", 1);

        // <--[command]
        // @Name cooldown
        // @Usage cooldown [<duration>] (global) (s:<script>)
        // @Required 1
        // @Stable Stable
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
                "COOLDOWN", "cooldown [<duration>] (global) (s:<script>)", 1);


		// <--[command]
		// @Name copyblock
		// @Usage copyblock [location:<location>] [to:<location>]
		// @Required 1
		// @Stable Todo
		// @Short Todo
		// @Author Todo
		// @Description
		// Todo
		// @Tags
		// Todo
		// @Usage
		// Todo
		// @Example
		// Todo
		// -->
        registerCoreMember(CopyBlockCommand.class,
                "COPYBLOCK", "copyblock [location:<location>] [to:<location>]", 1);


		// <--[command]
		// @Name createworld
		// @Usage createworld [<name>] (g:<generator>)
		// @Required 1
		// @Stable Todo
		// @Short Todo
		// @Author Todo
		// @Description
		// Todo
		// @Tags
		// Todo
		// @Usage
		// Todo
		// @Example
		// Todo
		// -->
        registerCoreMember(CreateWorldCommand.class,
                "CREATEWORLD", "createworld [<name>] (g:<generator>)", 1);


		// <--[command]
		// @Name define
		// @Usage define [<id>] [<value>]
		// @Required 2
        // @Stable 1.0
        // @Short Creates a temporary variable inside a script queue.
        // @Author aufdemrand
        //
        // @Description
        // Definitions are queue-level (or script-level) 'variables' that can be used throughout a script, once
        // defined, by using %'s around the definition id/name. Definitions are only valid on the current queue and are
        // not transferred to any new queues constructed within the script, such as a 'run' command, without explicitly
        // specifying to do so.
        //
        // Definitions are lighter and faster than creating a temporary flag, but unlike flags, are only a single entry,
        // that is, you can't add or remove from the definition, but you can re-create it if you wish to specify a new
        // value. Definitions are also automatically destroyed when the queue is completed, so there is no worry for
        // leaving unused data hanging around.
        //
        // Definitions are also resolved before replaceable tags, meaning you can use them within tags, even as an
        // attribute. ie. <%player%.name>
        //
        // @Usage
        // Use to make complex tags look less complex, and scripts more readable.
        // - narrate 'You invoke your power of notice...'
        // - define range '<player.flag[range_level].mul[3]>'
        // - define blocks '<player.flag[noticeable_blocks>'
        // - narrate '[NOTICE] You have noticed <player.location.find.blocks[%blocks%].within[%range].size>
        // blocks in the area that may be of interest.'
        //
        // @Usage
        // Use to keep the value of a replaceable tag that you might use many times within a single script. Definitions
        // can be faster and cleaner than reusing a replaceable tag over and over.
        // - define arg1 <c.args.get[1]>
        // - if %arg1% == hello narrate 'Hello!'
        // - if %arg1% == goodbye narrate 'Goodbye!'
        //
        // @Usage
        // Use to pass some important information (arguments) on to another queue.
        // - run 'new_task' d:hello|world
        // 'new_task' now has some definitions, %1% and %2%, that contains the contents specified, 'hello' and 'world'.
        //
        // @Example
        //
        // -->
        registerCoreMember(DefineCommand.class,
                "DEFINE", "define [<id>] [<value>]", 2);


		// <--[command]
		// @Name determine
		// @Usage determine [<value>]
		// @Required 1
		// @Stable Todo
		// @Short Todo
		// @Author Todo
		// @Description
		// Todo
		// @Tags
		// Todo
		// @Usage
		// Todo
		// @Example
		// Todo
		// -->
        registerCoreMember(DetermineCommand.class, 
                "DETERMINE", "determine [<value>]", 1);

		// <--[command]
		// @Name disengage
		// @Usage disengage
		// @Required 0
		// @Stable Todo
		// @Short Todo
		// @Author Todo
		// @Description
		// Todo
		// @Tags
		// Todo
		// @Usage
		// Todo
		// @Example
		// Todo
		// -->
        registerCoreMember(DisengageCommand.class, 
                "DISENGAGE", "disengage", 0);

		// <--[command]
		// @Name displayitem
		// @Usage displayitem (remove) [<item>] [<location>] (duration:<value>)
		// @Required 2
		// @Stable Todo
		// @Short Todo
		// @Author Todo
		// @Description
		// Todo
		// @Tags
		// Todo
		// @Usage
		// Todo
		// @Example
		// Todo
		// -->
        registerCoreMember(DisplayItemCommand.class,
                "DISPLAYITEM", "displayitem (remove) [<item>] [<location>] (duration:<value>)", 2);

		// <--[command]
		// @Name drop
		// @Usage drop [<item>/<entity>/<xp>] [<location>] (qty:<#>)
		// @Required 1
		// @Stable Todo
		// @Short Todo
		// @Author Todo
		// @Description
		// Todo
		// @Tags
		// Todo
		// @Usage
		// Todo
		// @Example
		// Todo
		// -->
        registerCoreMember(DropCommand.class, 
                "DROP", "drop [<item>/<entity>/<xp>] [<location>] (qty:<#>)", 1);
        
		// <--[command]
		// @Name engage
		// @Usage engage (duration:<value>)
		// @Required 0
		// @Stable Todo
		// @Short Todo
		// @Author Todo
		// @Description
		// Todo
		// @Tags
		// Todo
		// @Usage
		// Todo
		// @Example
		// Todo
		// -->
        registerCoreMember(EngageCommand.class,
                "ENGAGE", "engage (duration:<value>)", 0);

		// <--[command]
		// @Name engrave
		// @Usage engrave (set/remove)
		// @Required 0
		// @Stable Todo
		// @Short Todo
		// @Author Todo
		// @Description
		// Todo
		// @Tags
		// Todo
		// @Usage
		// Todo
		// @Example
		// Todo
		// -->
        registerCoreMember(EngraveCommand.class,
                "ENGRAVE", "engrave (set/remove)", 0);
        
		// <--[command]
		// @Name equip
		// @Usage equip (hand:<item>) (head:<item>) (chest:<item>) (legs:<item>) (boots:<item>)
		// @Required 1
		// @Stable Todo
		// @Short Todo
		// @Author Todo
		// @Description
		// Todo
		// @Tags
		// Todo
		// @Usage
		// Todo
		// @Example
		// Todo
		// -->
        registerCoreMember(EquipCommand.class, 
                "EQUIP", "equip (hand:<item>) (head:<item>) (chest:<item>) (legs:<item>) (boots:<item>)", 1);
        
		// <--[command]
		// @Name execute
		// @Usage execute [as_player/as_op/as_npc/as_server] [<Bukkit command>]
		// @Required 2
		// @Stable Todo
		// @Short Todo
		// @Author Todo
		// @Description
		// Todo
		// @Tags
		// Todo
		// @Usage
		// Todo
		// @Example
		// Todo
		// -->
        registerCoreMember(ExecuteCommand.class,
                "EXECUTE", "execute [as_player/as_op/as_npc/as_server] [<Bukkit command>]", 2);

		// <--[command]
		// @Name experience
		// @Usage experience [{set}/give/take] (level) [<#>]
		// @Required 2
		// @Stable Todo
		// @Short Todo
		// @Author Todo
		// @Description
		// Todo
		// @Tags
		// Todo
		// @Usage
		// Todo
		// @Example
		// Todo
		// -->
        registerCoreMember(ExperienceCommand.class,
                "EXPERIENCE", "experience [{set}/give/take] (level) [<#>]", 2);

		// <--[command]
		// @Name explode
		// @Usage explode (power:<#.#>) (<location>) (fire) (breakblocks)
		// @Required 0
		// @Stable Todo
		// @Short Todo
		// @Author Todo
		// @Description
		// Todo
		// @Tags
		// Todo
		// @Usage
		// Todo
		// @Example
		// Todo
		// -->
        registerCoreMember(ExplodeCommand.class,
                "EXPLODE", "explode (power:<#.#>) (<location>) (fire) (breakblocks)", 0);
        
		// <--[command]
		// @Name fail
		// @Usage fail (script:<name>)
		// @Required 0
		// @Stable Todo
		// @Short Todo
		// @Author Todo
		// @Description
		// Todo
		// @Tags
		// Todo
		// @Usage
		// Todo
		// @Example
		// Todo
		// -->
        registerCoreMember(FailCommand.class, 
                "FAIL", "fail (script:<name>)", 0);
        
		// <--[command]
		// @Name feed
		// @Usage feed (amt:<#>) (target:<entity>|...)
		// @Required 0
		// @Stable Todo
		// @Short Todo
		// @Author Todo
		// @Description
		// Todo
		// @Tags
		// Todo
		// @Usage
		// Todo
		// @Example
		// Todo
		// -->
        registerCoreMember(FeedCommand.class, 
                "FEED", "feed (amt:<#>) (target:<entity>|...)", 0);

		// <--[command]
		// @Name finish
		// @Usage finish (script:<name>)
		// @Required 0
		// @Stable Todo
		// @Short Todo
		// @Author Todo
		// @Description
		// Todo
		// @Tags
		// Todo
		// @Usage
		// Todo
		// @Example
		// Todo
		// -->
        registerCoreMember(FinishCommand.class, 
                "FINISH", "finish (script:<name>)", 0);

		// <--[command]
		// @Name firework
		// @Usage firework (<location>) (power:<#>) (type:<name>/random) (primary:<color>|...) (fade:<color>|...) (flicker) (trail)
		// @Required 0
		// @Stable Todo
		// @Short Todo
		// @Author Todo
		// @Description
		// Todo
		// @Tags
		// Todo
		// @Usage
		// Todo
		// @Example
		// Todo
		// -->
        registerCoreMember(FireworkCommand.class,
                "FIREWORK", "firework (<location>) (power:<#>) (type:<name>/random) (primary:<color>|...) (fade:<color>|...) (flicker) (trail)", 0);
        
		// <--[command]
		// @Name fish
		// @Usage fish (catchfish) (stop) (<location>) (catchpercent:<#>)
		// @Required 1
		// @Stable Todo
		// @Short Todo
		// @Author Todo
		// @Description
		// Todo
		// @Tags
		// Todo
		// @Usage
		// Todo
		// @Example
		// Todo
		// -->
        registerCoreMember(FishCommand.class, 
                "FISH", "fish (catchfish) (stop) (<location>) (catchpercent:<#>)", 1);

		// <--[command]
		// @Name flag
		// @Usage flag ({player}/npc/global) [<name>([<#>])](:<action>)[:<value>] (duration:<value>)
		// @Required 1
		// @Stable Todo
		// @Short Todo
		// @Author Todo
		// @Description
		// Todo
		// @Tags
		// Todo
		// @Usage
		// Todo
		// @Example
		// Todo
		// -->
        registerCoreMember(FlagCommand.class, 
                "FLAG", "flag ({player}/npc/global) [<name>([<#>])](:<action>)[:<value>] (duration:<value>)", 1);
        
		// <--[command]
		// @Name fly
		// @Usage fly (cancel) [<entity>|...] (origin:<location>) (destinations:<location>|...) (speed:<#.#>)
		// @Required 1
		// @Stable Todo
		// @Short Todo
		// @Author Todo
		// @Description
		// Todo
		// @Tags
		// Todo
		// @Usage
		// Todo
		// @Example
		// Todo
		// -->
        registerCoreMember(FlyCommand.class,
                "FLY", "fly (cancel) [<entity>|...] (origin:<location>) (destinations:<location>|...) (speed:<#.#>)", 1);
        
		// <--[command]
		// @Name follow
		// @Usage follow (stop) (lead:<#.#>) (target:<entity>)
		// @Required 0
		// @Stable Todo
		// @Short Todo
		// @Author Todo
		// @Description
		// Todo
		// @Tags
		// Todo
		// @Usage
		// Todo
		// @Example
		// Todo
		// -->
        registerCoreMember(FollowCommand.class, 
                "FOLLOW", "follow (stop) (lead:<#.#>) (target:<entity>)", 0);
        
        // <--[command]
        // @Name foreach
        // @Usage foreach [<object>|...] [<commands>]
        // @Required 2
        // @Stable Experimental
        // @Short Loops through a dList, running a set of commands for each item.
        // @Author Morphan1/mcmonkey
        //
        // @Description
        // Loops through a dList of any type. For each item in the dList, the specified commands will be ran for 
        // that item. To call the value of the item while in the loop, you can use %value%.
        //
        // @Usage
        // Use to run commands on multiple items.
        // - foreach li@e@123|n@424|p@BobBarker {
        //   - announce "There's something at <%value%.location>!"
        //   }
        //
        // @Example
        //
        // -->
        registerCoreMember(ForEachCommand.class,
                "FOREACH", "foreach [<object>|...] [<commands>]", 2);
        
		// <--[command]
		// @Name give
		// @Usage give [money/<item>] (qty:<#>) (engrave)
		// @Required 1
		// @Stable Todo
		// @Short Todo
		// @Author Todo
		// @Description
		// Todo
		// @Tags
		// Todo
		// @Usage
		// Todo
		// @Example
		// Todo
		// -->
        registerCoreMember(GiveCommand.class,
                "GIVE", "give [money/<item>] (qty:<#>) (engrave)", 1);
        
		// <--[command]
		// @Name group
		// @Usage group [add/remove] [<group>] (world:<name>)
		// @Required 2
		// @Stable Todo
		// @Short Todo
		// @Author Todo
		// @Description
		// Todo
		// @Tags
		// Todo
		// @Usage
		// Todo
		// @Example
		// Todo
		// -->
        registerCoreMember(GroupCommand.class,
                "GROUP", "group [add/remove] [<group>] (world:<name>)", 2);
        
		// <--[command]
		// @Name head
		// @Usage head (player) [skin:<name>]
		// @Required 0
		// @Stable Todo
		// @Short Todo
		// @Author Todo
		// @Description
		// Todo
		// @Tags
		// Todo
		// @Usage
		// Todo
		// @Example
		// Todo
		// -->
        registerCoreMember(HeadCommand.class,
                "HEAD", "head (player) [skin:<name>]", 0);
        
		// <--[command]
		// @Name heal
		// @Usage heal (<#.#>) (<entity>|...)
		// @Required 0
		// @Stable Todo
		// @Short Todo
		// @Author Todo
		// @Description
		// Todo
		// @Tags
		// Todo
		// @Usage
		// Todo
		// @Example
		// Todo
		// -->
        registerCoreMember(HealCommand.class, 
                "HEAL", "heal (<#.#>) (<entity>|...)", 0);
        
		// <--[command]
		// @Name health
		// @Usage health [<#>]
		// @Required 1
		// @Stable Todo
		// @Short Todo
		// @Author Todo
		// @Description
		// Todo
		// @Tags
		// Todo
		// @Usage
		// Todo
		// @Example
		// Todo
		// -->
        registerCoreMember(HealthCommand.class,
                "HEALTH", "health [<#>]", 1);
        
		// <--[command]
		// @Name hurt
		// @Usage hurt (<#.#>) (<entity>|...)
		// @Required 0
		// @Stable Todo
		// @Short Todo
		// @Author Todo
		// @Description
		// Todo
		// @Tags
		// Todo
		// @Usage
		// Todo
		// @Example
		// Todo
		// -->
        registerCoreMember(HurtCommand.class,
                "HURT", "hurt (<#.#>) (<entity>|...)", 0);
        
		// <--[command]
		// @Name if
		// @Usage if [<value>] (!)(<operator> <value>) (&&/|| ...) [<commands>] (else <commands>)
		// @Required 2
		// @Stable Todo
		// @Short Todo
		// @Author Todo
		// @Description
		// Todo
		// @Tags
		// Todo
		// @Usage
		// Todo
		// @Example
		// Todo
		// -->
        registerCoreMember(IfCommand.class, 
                "IF", "if [<value>] (!)(<operator> <value>) (&&/|| ...) [<commands>] (else <commands>)", 2);

		// <--[command]
		// @Name inventory
		// @Usage inventory [open/copy/move/swap/add/remove/keep/exclude/fill/clear] [destination:<inventory>] (origin:<inventory>)
		// @Required 2
		// @Stable Todo
		// @Short Todo
		// @Author Todo
		// @Description
		// Todo
		// @Tags
		// Todo
		// @Usage
		// Todo
		// @Example
		// Todo
		// -->
        registerCoreMember(InventoryCommand.class,
                "INVENTORY", "inventory [open/copy/move/swap/add/remove/keep/exclude/fill/clear] [destination:<inventory>] (origin:<inventory>)", 2);
        
		// <--[command]
		// @Name invisible
		// @Usage invisible [player/npc] [state:true/false/toggle]
		// @Required 2
		// @Stable Todo
		// @Short Todo
		// @Author Todo
		// @Description
		// Todo
		// @Tags
		// Todo
		// @Usage
		// Todo
		// @Example
		// Todo
		// -->
        registerCoreMember(InvisibleCommand.class,
                "INVISIBLE", "invisible [player/npc] [state:true/false/toggle]", 2);
        
		// <--[command]
		// @Name leash
		// @Usage leash (cancel) [<entity>|...] (holder:<entity>/<location>)
		// @Required 1
		// @Stable Todo
		// @Short Todo
		// @Author Todo
		// @Description
		// Todo
		// @Tags
		// Todo
		// @Usage
		// Todo
		// @Example
		// Todo
		// -->
        registerCoreMember(LeashCommand.class,
                "LEASH", "leash (cancel) [<entity>|...] (holder:<entity>/<location>)", 1);
        
		// <--[command]
		// @Name listen
		// @Usage listen ({new}/cancel/finish) [kill/block/item/itemdrop/travel] [<requirements>] [script:<name>] (id:<name>)
		// @Required 2
		// @Stable Todo
		// @Short Todo
		// @Author Todo
		// @Description
		// Todo
		// @Tags
		// Todo
		// @Usage
		// Todo
		// @Example
		// Todo
		// -->
        registerCoreMember(ListenCommand.class,
                "LISTEN", "listen ({new}/cancel/finish) [kill/block/item/itemdrop/travel] [<requirements>] [script:<name>] (id:<name>)", 2);
        
		// <--[command]
		// @Name log
		// @Usage log [<text>] (type:severe/info/warning/fine/finer/finest) [file:<name>]
		// @Required 2
		// @Stable Todo
		// @Short Todo
		// @Author Todo
		// @Description
		// Todo
		// @Tags
		// Todo
		// @Usage
		// Todo
		// @Example
		// Todo
		// -->
        registerCoreMember(LogCommand.class,
                "LOG", "log [<text>] (type:severe/info/warning/fine/finer/finest) [file:<name>]", 2);
        
		// <--[command]
		// @Name look
		// @Usage look (<entity>|...) [<location>] (duration:<duration>)
		// @Required 1
		// @Stable Todo
		// @Short Todo
		// @Author Todo
		// @Description
		// Todo
		// @Tags
		// Todo
		// @Usage
		// Todo
		// @Example
		// Todo
		// -->
        registerCoreMember(LookCommand.class,
                "LOOK", "look (<entity>|...) [<location>] (duration:<duration>)", 1);
        
		// <--[command]
		// @Name lookclose
		// @Usage lookclose [state:true/false]
		// @Required 1
		// @Stable Todo
		// @Short Todo
		// @Author Todo
		// @Description
		// Todo
		// @Tags
		// Todo
		// @Usage
		// Todo
		// @Example
		// Todo
		// -->
        registerCoreMember(LookcloseCommand.class,
                "LOOKCLOSE", "lookclose [state:true/false]", 1);
        
		// <--[command]
		// @Name midi
		// @Usage midi [file:<name>] [<location>/listeners:<player>|...] (tempo:<#.#>)
		// @Required 1
		// @Stable Todo
		// @Short Todo
		// @Author Todo
		// @Description
		// Todo
		// @Tags
		// Todo
		// @Usage
		// Todo
		// @Example
		// Todo
		// -->
        registerCoreMember(MidiCommand.class,
                "MIDI", "midi [file:<name>] [<location>/listeners:<player>|...] (tempo:<#.#>)", 1);
        
		// <--[command]
		// @Name mount
		// @Usage mount (cancel) [<entity>|...] (<location>)
		// @Required 0
		// @Stable Todo
		// @Short Todo
		// @Author Todo
		// @Description
		// Todo
		// @Tags
		// Todo
		// @Usage
		// Todo
		// @Example
		// Todo
		// -->
        registerCoreMember(MountCommand.class,
                "MOUNT", "mount (cancel) [<entity>|...] (<location>)", 0);
        
		// <--[command]
		// @Name modifyblock
		// @Usage modifyblock [<location>] [<block>] (radius:<#>) (height:<#>) (depth:<#>)
		// @Required 2
		// @Stable Todo
		// @Short Todo
		// @Author Todo
		// @Description
		// Todo
		// @Tags
		// Todo
		// @Usage
		// Todo
		// @Example
		// Todo
		// -->
        registerCoreMember(ModifyBlockCommand.class,
                "MODIFYBLOCK", "modifyblock [<location>] [<block>] (radius:<#>) (height:<#>) (depth:<#>)", 2);
        
		// <--[command]
		// @Name nameplate
		// @Usage nameplate [<chatcolor>] [set:<text>] (target:<player>)  +--> Requires ProtocolLib
		// @Required 1
		// @Stable Todo
		// @Short Todo
		// @Author Todo
		// @Description
		// Todo
		// @Tags
		// Todo
		// @Usage
		// Todo
		// @Example
		// Todo
		// -->
        registerCoreMember(NameplateCommand.class,
                "NAMEPLATE", "nameplate [<chatcolor>] [set:<text>] (target:<player>)  +--> Requires ProtocolLib", 1);
        
		// <--[command]
		// @Name narrate
		// @Usage narrate ["<text>"] (targets:<player>|...) (format:<name>)
		// @Required 1
		// @Stable Todo
		// @Short Todo
		// @Author Todo
		// @Description
		// Todo
		// @Tags
		// Todo
		// @Usage
		// Todo
		// @Example
		// Todo
		// -->
        registerCoreMember(NarrateCommand.class,
                "NARRATE", "narrate [\"<text>\"] (targets:<player>|...) (format:<name>)", 1);
        
		// <--[command]
		// @Name note
		// @Usage note [<Notable dObject>] [as:<name>]
		// @Required 2
		// @Stable Todo
		// @Short Todo
		// @Author Todo
		// @Description
		// Todo
		// @Tags
		// Todo
		// @Usage
		// Todo
		// @Example
		// Todo
		// -->
        registerCoreMember(NoteCommand.class,
                "NOTE", "note [<Notable dObject>] [as:<name>]", 2);
        
		// <--[command]
		// @Name oxygen
		// @Usage oxygen (type:maximum/remaining) (mode:set/add/remove) [qty:<#>]
		// @Required 1
		// @Stable Todo
		// @Short Todo
		// @Author Todo
		// @Description
		// Todo
		// @Tags
		// Todo
		// @Usage
		// Todo
		// @Example
		// Todo
		// -->
        registerCoreMember(OxygenCommand.class,
                "OXYGEN", "oxygen (type:maximum/remaining) (mode:set/add/remove) [qty:<#>]", 1);
        
		// <--[command]
		// @Name playeffect
		// @Usage playeffect [<location>] [effect:<name>] (data:<#.#>) (visibility:<#.#>) (qty:<#>) (offset:<#.#>)
		// @Required 2
		// @Stable Todo
		// @Short Todo
		// @Author Todo
		// @Description
		// Todo
		// @Tags
		// Todo
		// @Usage
		// Todo
		// @Example
		// Todo
		// -->
        registerCoreMember(PlayEffectCommand.class,
                "PLAYEFFECT", "playeffect [<location>] [effect:<name>] (data:<#.#>) (visibility:<#.#>) (qty:<#>) (offset:<#.#>)", 2);
        
		// <--[command]
		// @Name playsound
		// @Usage playsound [<location>] [sound:<name>] (volume:<#.#>) (pitch:<#.#>)
		// @Required 2
		// @Stable Todo
		// @Short Todo
		// @Author Todo
		// @Description
		// Todo
		// @Tags
		// Todo
		// @Usage
		// Todo
		// @Example
		// Todo
		// -->
        registerCoreMember(PlaySoundCommand.class,
                "PLAYSOUND", "playsound [<location>] [sound:<name>] (volume:<#.#>) (pitch:<#.#>)", 2);
        
		// <--[command]
		// @Name permission
		// @Usage permission [add|remove] [permission] (player:<name>) (group:<name>) (world:<name>)
		// @Required 2
		// @Stable Todo
		// @Short Todo
		// @Author Todo
		// @Description
		// Todo
		// @Tags
		// Todo
		// @Usage
		// Todo
		// @Example
		// Todo
		// -->
        registerCoreMember(PermissionCommand.class,
                "PERMISSION", "permission [add|remove] [permission] (player:<name>) (group:<name>) (world:<name>)", 2);

		// <--[command]
		// @Name pose
		// @Usage pose (player/npc) [id:<name>]
		// @Required 1
		// @Stable Todo
		// @Short Todo
		// @Author Todo
		// @Description
		// Todo
		// @Tags
		// Todo
		// @Usage
		// Todo
		// @Example
		// Todo
		// -->
        registerCoreMember(PoseCommand.class,
                "POSE", "pose (player/npc) [id:<name>]", 1);

		// <--[command]
		// @Name pause
		// @Usage pause [waypoints/navigation]
		// @Required 1
		// @Stable Todo
		// @Short Todo
		// @Author Todo
		// @Description
		// Todo
		// @Tags
		// Todo
		// @Usage
		// Todo
		// @Example
		// Todo
		// -->
        registerCoreMember(PauseCommand.class,
                "PAUSE", "pause [waypoints/navigation]", 1);
        
		// <--[command]
		// @Name queue
		// @Usage queue (queue:<id>) [clear/pause/resume/delay:<#>]
		// @Required 1
		// @Stable Todo
		// @Short Todo
		// @Author Todo
		// @Description
		// Todo
		// @Tags
		// Todo
		// @Usage
		// Todo
		// @Example
		// Todo
		// -->
        registerCoreMember(QueueCommand.class,
                "QUEUE", "queue (queue:<id>) [clear/pause/resume/delay:<#>]", 1);
        
		// <--[command]
		// @Name random
		// @Usage random [<#>]
		// @Required 1
		// @Stable Todo
		// @Short Todo
		// @Author Todo
		// @Description
		// Todo
		// @Tags
		// Todo
		// @Usage
		// Todo
		// @Example
		// Todo
		// -->
        registerCoreMember(RandomCommand.class, 
                "RANDOM", "random [<#>]", 1);
        
		// <--[command]
		// @Name remove
		// @Usage remove [<entity>|...] (region:<name>)
		// @Required 0
		// @Stable Todo
		// @Short Todo
		// @Author Todo
		// @Description
		// Todo
		// @Tags
		// Todo
		// @Usage
		// Todo
		// @Example
		// Todo
		// -->
        registerCoreMember(RemoveCommand.class, 
                "REMOVE", "remove [<entity>|...] (region:<name>)", 0);

		// <--[command]
		// @Name rename
		// @Usage rename [<npc>] [<name>]
		// @Required 1
		// @Stable Todo
		// @Short Todo
		// @Author Todo
		// @Description
		// Todo
		// @Tags
		// Todo
		// @Usage
		// Todo
		// @Example
		// Todo
		// -->
        registerCoreMember(RenameCommand.class,
                "RENAME", "rename [<npc>] [<name>]", 1);

		// <--[command]
		// @Name repeat
		// @Usage repeat [<amount>] [<commands>]
		// @Required 1
		// @Stable Todo
		// @Short Todo
		// @Author Todo
		// @Description
		// Todo
		// @Tags
		// Todo
		// @Usage
		// Todo
		// @Example
		// Todo
		// -->
        registerCoreMember(RepeatCommand.class,
                "REPEAT", "repeat [<amount>] [<commands>]", 1);

		// <--[command]
		// @Name reset
		// @Usage reset [fails/finishes/cooldown] (script:<name>)
		// @Required 1
		// @Stable Todo
		// @Short Todo
		// @Author Todo
		// @Description
		// Todo
		// @Tags
		// Todo
		// @Usage
		// Todo
		// @Example
		// Todo
		// -->
        registerCoreMember(ResetCommand.class,
                "RESET", "reset [fails/finishes/cooldown] (script:<name>)", 1);

		// <--[command]
		// @Name run
		// @Usage run [<script>] (path:<name>) (as:<player>/<npc>) (define:<element>|...) (id:<name>) (delay:<value>) (loop) (qty:<#>)
		// @Required 1
		// @Stable Todo
		// @Short Todo
		// @Author Todo
		// @Description
		// Todo
		// @Tags
		// Todo
		// @Usage
		// Todo
		// @Example
		// Todo
		// -->
        registerCoreMember(RunCommand.class,
                "RUN", "run [<script>] (path:<name>) (as:<player>/<npc>) (define:<element>|...) (id:<name>) (delay:<value>) (loop) (qty:<#>)", 1);

		// <--[command]
		// @Name runtask
		// @Usage runtask [<name>] (instantly) (queue(:<name>)) (delay:<#>) (define:<element>|...)
		// @Required 1
		// @Stable Todo
		// @Short Todo
		// @Author Todo
		// @Description
		// Todo
		// @Tags
		// Todo
		// @Usage
		// Todo
		// @Example
		// Todo
		// -->
        registerCoreMember(RuntaskCommand.class,
                "RUNTASK", "runtask [<name>] (instantly) (queue(:<name>)) (delay:<#>) (define:<element>|...)", 1);

		// <--[command]
		// @Name scoreboard
		// @Usage scoreboard [set/remove/show/hide] [<name>] [value:<name>] (priority:<#>)
		// @Required 1
		// @Stable Todo
		// @Short Todo
		// @Author Todo
		// @Description
		// Todo
		// @Tags
		// Todo
		// @Usage
		// Todo
		// @Example
		// Todo
		// -->
        registerCoreMember(ScoreboardCommand.class,
                "SCOREBOARD", "scoreboard [set/remove/show/hide] [<name>] [value:<name>] (priority:<#>)", 1);
        
		// <--[command]
		// @Name scribe
		// @Usage scribe [script:<name>] (give/drop/equip) (<item>) (<location>)
		// @Required 1
		// @Stable Todo
		// @Short Todo
		// @Author Todo
		// @Description
		// Todo
		// @Tags
		// Todo
		// @Usage
		// Todo
		// @Example
		// Todo
		// -->
        registerCoreMember(ScribeCommand.class,
                "SCRIBE", "scribe [script:<name>] (give/drop/equip) (<item>) (<location>)", 1);

		// <--[command]
		// @Name shoot
		// @Usage shoot [<entity>|...] (origin:<entity>/<location>) (destination:<location>) [{calculated} (height:<#.#>) (gravity:<#.#>)]/[custom speed:<#.#> duration:<value>] (script:<name>)
		// @Required 1
		// @Stable Todo
		// @Short Todo
		// @Author Todo
		// @Description
		// Todo
		// @Tags
		// Todo
		// @Usage
		// Todo
		// @Example
		// Todo
		// -->
        registerCoreMember(ShootCommand.class,
                "SHOOT", "shoot [<entity>|...] (origin:<entity>/<location>) (destination:<location>) [{calculated} (height:<#.#>) (gravity:<#.#>)]/[custom speed:<#.#> duration:<value>] (script:<name>)", 1);
        
		// <--[command]
		// @Name showfake
		// @Usage showfake [<material>] [<location>|...] (d:<duration>{10s})
		// @Required 2
		// @Stable Todo
		// @Short Todo
		// @Author Todo
		// @Description
		// Todo
		// @Tags
		// Todo
		// @Usage
		// Todo
		// @Example
		// Todo
		// -->
        registerCoreMember(ShowFakeCommand.class,
                "SHOWFAKE", "showfake [<material>] [<location>|...] (d:<duration>{10s})", 2);

		// <--[command]
		// @Name sign
		// @Usage sign (type:{sign_post}/wall_sign) ["<line>|..."] [<location>] (direction:n/e/w/s)
		// @Required 1
		// @Stable Todo
		// @Short Todo
		// @Author Todo
		// @Description
		// Todo
		// @Tags
		// Todo
		// @Usage
		// Todo
		// @Example
		// Todo
		// -->
        registerCoreMember(SignCommand.class, 
                "SIGN", "sign (type:{sign_post}/wall_sign) [\"<line>|...\"] [<location>] (direction:n/e/w/s)", 1);
        
		// <--[command]
		// @Name sit
		// @Usage sit (<location>)
		// @Required 0
		// @Stable Todo
		// @Short Todo
		// @Author Todo
		// @Description
		// Todo
		// @Tags
		// Todo
		// @Usage
		// Todo
		// @Example
		// Todo
		// -->
        registerCoreMember(SitCommand.class, 
                "SIT", "sit (<location>)", 0);

		// <--[command]
		// @Name spawn
		// @Usage spawn [<entity>|...] (<location>) (target:<entity>)
		// @Required 1
		// @Stable Todo
		// @Short Todo
		// @Author Todo
		// @Description
		// Todo
		// @Tags
		// Todo
		// @Usage
		// Todo
		// @Example
		// Todo
		// -->
        registerCoreMember(SpawnCommand.class,
                "SPAWN", "spawn [<entity>|...] (<location>) (target:<entity>)", 1);
        
		// <--[command]
		// @Name stand
		// @Usage stand
		// @Required 0
		// @Stable Todo
		// @Short Todo
		// @Author Todo
		// @Description
		// Todo
		// @Tags
		// Todo
		// @Usage
		// Todo
		// @Example
		// Todo
		// -->
        registerCoreMember(StandCommand.class, 
                "STAND", "stand", 0);
        
		// <--[command]
		// @Name strike
		// @Usage strike (no_damage) [<location>]
		// @Required 1
		// @Stable Todo
		// @Short Todo
		// @Author Todo
		// @Description
		// Todo
		// @Tags
		// Todo
		// @Usage
		// Todo
		// @Example
		// Todo
		// -->
        registerCoreMember(StrikeCommand.class,
                "STRIKE", "strike (no_damage) [<location>]", 1);
        
		// <--[command]
		// @Name switch
		// @Usage switch [<location>] (state:[{toggle}/on/off]) (duration:<value>)
		// @Required 1
		// @Stable Todo
		// @Short Todo
		// @Author Todo
		// @Description
		// Todo
		// @Tags
		// Todo
		// @Usage
		// Todo
		// @Example
		// Todo
		// -->
        registerCoreMember(SwitchCommand.class,
                "SWITCH", "switch [<location>] (state:[{toggle}/on/off]) (duration:<value>)", 1);
        
		// <--[command]
		// @Name take
		// @Usage take [money/iteminhand/<item>] (qty:<#>)
		// @Required 1
		// @Stable Todo
		// @Short Todo
		// @Author Todo
		// @Description
		// Todo
		// @Tags
		// Todo
		// @Usage
		// Todo
		// @Example
		// Todo
		// -->
        registerCoreMember(TakeCommand.class,
                "TAKE", "take [money/iteminhand/<item>] (qty:<#>)", 1);
        
		// <--[command]
		// @Name teleport
		// @Usage teleport (<entity>|...) (<location>)
		// @Required 1
		// @Stable Todo
		// @Short Todo
		// @Author Todo
		// @Description
		// Todo
		// @Tags
		// Todo
		// @Usage
		// Todo
		// @Example
		// Todo
		// -->
        registerCoreMember(TeleportCommand.class,
                "TELEPORT", "teleport (<entity>|...) (<location>)", 1);
        
		// <--[command]
		// @Name time
		// @Usage time [type:{global}/player] [<value>] (world:<name>)
		// @Required 1
		// @Stable Todo
		// @Short Todo
		// @Author Todo
		// @Description
		// Todo
		// @Tags
		// Todo
		// @Usage
		// Todo
		// @Example
		// Todo
		// -->
        registerCoreMember(TimeCommand.class,
                "TIME", "time [type:{global}/player] [<value>] (world:<name>)", 1);
        
		// <--[command]
		// @Name trigger
		// @Usage trigger [name:chat/click/damage/proximity] (state:true/false) (cooldown:<#.#>) (radius:<#>)
		// @Required 2
		// @Stable Todo
		// @Short Todo
		// @Author Todo
		// @Description
		// Todo
		// @Tags
		// Todo
		// @Usage
		// Todo
		// @Example
		// Todo
		// -->
        registerCoreMember(TriggerCommand.class, 
                "TRIGGER", "trigger [name:chat/click/damage/proximity] (state:true/false) (cooldown:<#.#>) (radius:<#>)", 2);
        
		// <--[command]
		// @Name viewer
		// @Usage viewer ({create <location>}/modify/remove) [id:<name>] (type:{sign_post}/wall_sign) (display:{location}/score/logged_in) (direction:n/e/w/s)
		// @Required 2
		// @Stable Todo
		// @Short Todo
		// @Author Todo
		// @Description
		// Todo
		// @Tags
		// Todo
		// @Usage
		// Todo
		// @Example
		// Todo
		// -->
        registerCoreMember(ViewerCommand.class,
                "VIEWER", "viewer ({create <location>}/modify/remove) [id:<name>] (type:{sign_post}/wall_sign) (display:{location}/score/logged_in) (direction:n/e/w/s)", 2);
        
		// <--[command]
		// @Name vulnerable
		// @Usage vulnerable (state:{true}/false/toggle)
		// @Required 0
		// @Stable Todo
		// @Short Todo
		// @Author Todo
		// @Description
		// Todo
		// @Tags
		// Todo
		// @Usage
		// Todo
		// @Example
		// Todo
		// -->
        registerCoreMember(VulnerableCommand.class, 
                "VULNERABLE", "vulnerable (state:{true}/false/toggle)", 0);
        
		// <--[command]
		// @Name wait
		// @Usage wait (<duration>) (queue:<name>)
		// @Required 0
		// @Stable Todo
		// @Short Todo
		// @Author Todo
		// @Description
		// Todo
		// @Tags
		// Todo
		// @Usage
		// Todo
		// @Example
		// Todo
		// -->
        registerCoreMember(WaitCommand.class, 
                "WAIT", "wait (<duration>) (queue:<name>)", 0);

		// <--[command]
		// @Name walk
		// @Usage walk [<location>] (speed:<#>) (auto_range)
		// @Required 1
		// @Stable Todo
		// @Short Todo
		// @Author Todo
		// @Description
		// Todo
		// @Tags
		// Todo
		// @Usage
		// Todo
		// @Example
		// Todo
		// -->
		// <--[command]
		// @Name walkto
		// @Usage walkto [<location>] (speed:<#>) (auto_range)
		// @Required 1
		// @Stable Todo
		// @Short Todo
		// @Author Todo
		// @Description
		// Todo
		// @Tags
		// Todo
		// @Usage
		// Todo
		// @Example
		// Todo
		// -->
        registerCoreMember(WalkCommand.class,
                "WALK, WALKTO", "walk [<location>] (speed:<#>) (auto_range)", 1);

		// <--[command]
		// @Name weather
		// @Usage weather [type:{global}/player] [sunny/storm/thunder] (world:<name>)
		// @Required 1
		// @Stable Todo
		// @Short Todo
		// @Author Todo
		// @Description
		// Todo
		// @Tags
		// Todo
		// @Usage
		// Todo
		// @Example
		// Todo
		// -->
        registerCoreMember(WeatherCommand.class,
                "WEATHER", "weather [type:{global}/player] [sunny/storm/thunder] (world:<name>)", 1);
        
		// <--[command]
		// @Name yaml
		// @Usage yaml [load/create/save:<file>]/[write:<key>]/[write:<key> value:<value>] [id:<name>]
		// @Required 1
		// @Stable Todo
		// @Short Todo
		// @Author Todo
		// @Description
		// Todo
		// @Tags
		// Todo
		// @Usage
		// Todo
		// @Example
		// Todo
		// -->
        registerCoreMember(YamlCommand.class,
                "YAML", "yaml [load/create/save:<file>]/[write:<key>]/[write:<key> value:<value>] [id:<name>]", 1);

		// <--[command]
		// @Name zap
		// @Usage zap (<script>:)[<step>] (<duration>)
		// @Required 0
		// @Stable Todo
		// @Short Todo
		// @Author Todo
		// @Description
		// Todo
		// @Tags
		// Todo
		// @Usage
		// Todo
		// @Example
		// Todo
		// -->
        registerCoreMember(ZapCommand.class, 
                "ZAP", "zap (<script>:)[<step>] (<duration>)", 0);
        
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
