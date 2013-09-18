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
        // @Name Anchor

        // @Usage anchor [id:<name>] [assume/remove/add <location>/walkto/walknear (r:#)]
        // @Required 2
        // @Stable Stable
        // @Short Controls a NPC's Anchor Trait.
        // @Author aufdemrand

        // @Description
        // The anchor system inside Citizens2 allows locations to be 'bound' to a NPC, saved by an 'id'. The anchor
        // command can add and remove new anchors, as well as the ability to teleport NPCs to anchors with the 'assume'
        // argument.
        // The Anchors Trait can also be used as a sort of 'waypoints' system. For ease of use, the anchor command
        // provides function for NPCs to walk to or walk near an anchor.
        // As the Anchor command is a NPC specific command, a valid npc object must be referenced in the script entry.
        // If none is provided by default, the use of the 'npc:n@id' argument, replacing the id with the npcid of the
        // NPC desired, can create a link, or alternatively override the default linked npc.

        // @Tags
        // <n@npc.anchor[anchor_name]>

        // @Usage
        // Use to add and remove anchors to a npc.
        // - define location_name <context.message>
        // - chat "I have saved this location as %location_name%.'
        // - anchor add <npc.location> "id:%location_name%"

        // @Usage
        // Use to make a NPC walk to or walk near a saved anchor.
        // - anchor walkto i:waypoint_1
        // - anchor walknear i:waypoint_2 r:5
        // -->
        registerCoreMember(AnchorCommand.class,
                "ANCHOR", "anchor [id:<name>] [assume/remove/add <location>/walkto/walknear (r:#)]", 2);

        // <--[command]
        // @Name Animate
        // @Usage animate [<entity>|...] [animation:<name>]
        // @Required 1
        // @Stable Todo
        // @Short Makes a list of entities perform a certain animation.
        // @Author David Cernat
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
        // @Name AnimateChest
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
        // @Name Announce
        // @Usage announce ["<text>"] (to_ops) (to_flagged:<flag>)
        // @Required 1
        // @Stable Todo
        // @Short Announces a message for everyone online to read.
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
        // @Name Assignment
        // @Usage assignment [{set}/remove] (script:<name>)
        // @Required 1
        // @Stable Todo
        // @Short Changes an NPC's assignment.
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
        // @Name Attack
        // @Usage attack (cancel) (<entity>|...) (target:<entity>)
        // @Required 0
        // @Stable Todo
        // @Short Makes a list of entities attack a target.
        // @Author David Cernat
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
        // @Name Break
        // @Usage break [<location>] (entity:<entity>) (radius:<#.#>)
        // @Required 1
        // @Stable Todo
        // @Short Breaks a block.
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
        // @Name Burn
        // @Usage burn [<entity>|...] (duration:<value>)
        // @Required 1
        // @Stable Todo
        // @Short Sets a list of entities on fire.
        // @Author David Cernat
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
        // @Name Cast, Potion
        // @Usage cast [<effect>] (remove) (duration:<value>) (power:<#>) (<entity>|...)
        // @Required 1
        // @Stable Stable
        // @Short Casts a potion effect to a list of entities.
        // @Author aufdemrand/Jeebiss/Morphan1
        //
        // @Description
        // Casts or removes a potion effect to or from a list of entities. If you don't specify a duration,
        // it defaults to 60 seconds. If you don't specify a power level, it defaults to 1.
        //
        // @Tags
        // <e@entity.has_effect[<effect>]> will return true if the entity has an effect.
        //
        // @Usage
        // Use to apply an effect to an entity
        // - potion jump <player> d:120 p:3
        // - narrate "You have been given the temporary ability to jump like a kangaroo."
        //
        // @Usage
        // Use to remove an effect from an entity
        // - if <p@Player.has_effect[jump]> {
        //   - potion jump remove <player>
        //   }
        //
        // @Example TODO
        //
        // -->
        registerCoreMember(CastCommand.class,
                "CAST, POTION", "cast [<effect>] (remove) (duration:<value>) (power:<#>) (<entity>|...)", 1);

        // <--[command]
        // @Name Chat
        // @Usage chat ["<text>"] (targets:<entity>|...)
        // @Required 1
        // @Stable Todo
        // @Short Causes the NPC to send a chat message to nearby players.
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
        // @Name ChunkLoad
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
        // @Name Compass
        // @Usage compass [<location>]
        // @Required 1
        // @Stable Todo
        // @Short Redirects the player's compass to target the given location.
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
        // @Name Cooldown
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
        // @Example TODO
        //
        // -->
        registerCoreMember(CooldownCommand.class,
                "COOLDOWN", "cooldown [<duration>] (global) (s:<script>)", 1);


        // <--[command]
        // @Name CopyBlock
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
        // @Name CreateWorld
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
        // @Name Define
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
        // @Name Determine
        // @Usage determine [<value>]
        // @Required 1
        // @Stable Todo
        // @Short Sets the outcome of an event.
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
        // @Name Disengage
        // @Usage disengage (npc:<npc>)
        // @Required 0
        // @Stable 1.0
        // @Short Enables a NPCs triggers that have been temporarily disabled by the engage command.
        // @Author aufdemrand
        //
        // @Description
        // Re-enables any toggled triggers that have been disabled by disengage. Using
        // disengage inside scripts must have a NPC to reference, or one may be specified
        // by supplying a valid dNPC object with the npc argument.
        //
        // This is mostly regarded as an 'interact script command', though it may be used inside
        // other script types. This is because disengage works with the trigger system, which is an
        // interact script-container feature.
        //
        // NPCs that are interacted with while engaged will fire an 'on unavailable' assignment
        // script-container action.
        //
        // @See Engage Command
        //
        // @Usage
        // Use to reenable a NPC's triggers, disabled via 'engage'.
        // - engage
        // - chat 'Be right there!'
        // - walkto <player.location>
        // - wait 5s
        // - disengage
        //
        // @Example
        // -->
        registerCoreMember(DisengageCommand.class,
                "DISENGAGE", "disengage (npc:<npc>)", 0);

        // <--[command]
        // @Name DisplayItem
        // @Usage displayitem (remove) [<item>] [<location>] (duration:<value>)
        // @Required 2
        // @Stable Todo
        // @Short Makes a non-touchable item spawn for players to view.
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
        // @Name Drop
        // @Usage drop [<item>/<entity>/<xp>] [<location>] (qty:<#>)
        // @Required 1
        // @Stable Todo
        // @Short Drops an item for players to pick up.
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
        // @Name Engage
        // @Usage engage (<duration>) (npc:<npc>)
        // @Required 0
        // @Stable 1.0
        // @Short Temporarily disables a NPCs toggled interact script-container triggers.
        // @Author aufdemrand
        //
        // @Description
        // Engaging a NPC will temporarily disable any interact script-container triggers. To reverse
        // this behavior, use either the disengage command, or specify a duration in which the engage
        // should timeout. Specifying an engage without a duration will render the NPC engaged until
        // a disengage is used on the NPC. Engaging a NPC affects all players attempting to interact
        // with the NPC.
        //
        // While engaged, all triggers and actions associated with triggers will not 'fire', except
        // the 'on unavailable' assignment script-container action, which will fire for triggers that
        // were enabled previous to the engage command.
        //
        // Engage can be useful when NPCs are carrying out a task that shouldn't be interrupted, or
        // to provide a good way to avoid accidental 'retrigger'.
        //
        // @See Disengage Command
        //
        // @Tags
        // <n@npc.engaged> will return true if the NPC is currently engaged, false otherwise.
        //
        // @Usage
        // Use to make a NPC appear 'busy'.
        // - engage
        // - chat 'Give me a few minutes while I mix you a potion!'
        // - walkto <npc.anchor[mixing_station]>
        // - wait 10s
        // - walkto <npc.anchor[service_station]>
        // - chat 'Here you go!'
        // - give potion <player>
        // - disengage
        //
        // @Usage
        // Use to avoid 'retrigger'.
        // - engage 5s
        // - take quest_item
        // - flag player finished_quests:->:super_quest
        //
        // @Example
        //
        // -->
        registerCoreMember(EngageCommand.class,
                "ENGAGE", "engage (<duration>) (npc:<npc>)", 0);

        // <--[command]
        // @Name Engrave
        // @Usage engrave (set/remove)
        // @Required 0
        // @Stable Todo
        // @Short Locks an item to a player *does not work currently*
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
        // @Name Equip
        // @Usage equip (player/{npc}) (hand:<item>) (head:<item>) (chest:<item>) (legs:<item>) (boots:<item>)
        // @Required 1
        // @Stable Todo
        // @Short Equips an item into the chosen inventory slot of the player or NPC.
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
                "EQUIP", "equip (player/{npc}) (hand:<item>) (head:<item>) (chest:<item>) (legs:<item>) (boots:<item>)", 1);

        // <--[command]
        // @Name Execute
        // @Usage execute [as_player/as_op/as_npc/as_server] [<Bukkit command>]
        // @Required 2
        // @Stable Todo
        // @Short Executes an arbitrary server command as if the player, NPC, or server typed it in.
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
        // @Name Experience
        // @Usage experience [{set}/give/take] (level) [<#>]
        // @Required 2
        // @Stable Todo
        // @Short Gives or takes experience points to the player.
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
        // @Name Explode
        // @Usage explode (power:<#.#>) (<location>) (fire) (breakblocks)
        // @Required 0
        // @Stable Todo
        // @Short Causes an explosion at the location.
        // @Author Alain Blanquet
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
        // @Name Fail
        // @Usage fail (script:<name>)
        // @Required 0
        // @Stable Todo
        // @Short Marks a script as having failed.
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
        // @Name Feed
        // @Usage feed (amt:<#>) (target:<entity>|...)
        // @Required 0
        // @Stable Todo
        // @Short Refills the player's food bar.
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
        // @Name Finish
        // @Usage finish (script:<name>)
        // @Required 0
        // @Stable Todo
        // @Short Marks a script as having been completed successfully.
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
        // @Name Firework
        // @Usage firework (<location>) (power:<#>) (type:<name>/random) (primary:<color>|...) (fade:<color>|...) (flicker) (trail)
        // @Required 0
        // @Stable Todo
        // @Short Todo
        // @Author David Cernat
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
        // @Name Fish
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
        // @Name Flag
        // @Usage flag ({player}/npc/global) [<name>([<#>])](:<action>)[:<value>] (duration:<value>)
        // @Required 1
        // @Stable Todo
        // @Short Sets or modifies a flag on the player, NPC, or server.
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
        // @Name Fly
        // @Usage fly (cancel) [<entity>|...] (controller:<player>) (origin:<location>) (destinations:<location>|...) (speed:<#.#>) (rotationthreshold:<#.#>)
        // @Required 1
        // @Stable Todo
        // @Short Make an entity fly where its controller is looking or fly to waypoints.
        // @Author David Cernat
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
                "FLY", "fly (cancel) [<entity>|...] (controller:<player>) (origin:<location>) (destinations:<location>|...) (speed:<#.#>) (rotationthreshold:<#.#>)", 1);

        // <--[command]
        // @Name Follow
        // @Usage follow (stop) (lead:<#.#>) (target:<entity>)
        // @Required 0
        // @Stable Todo
        // @Short Causes the NPC to follow a target.
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
        // @Name ForEach
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
        // @Name Give
        // @Usage give [money/<item>|...] (qty:<#>) (engrave) (to:<inventory>)
        // @Required 1
        // @Stable Todo
        // @Short Gives the player an item.
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
                "GIVE", "give [money/<item>|...] (qty:<#>) (engrave) (to:<inventory>)", 1);

        // <--[command]
        // @Name Group
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
        // @Name Head
        // @Usage head (player) [skin:<name>]
        // @Required 0
        // @Stable Todo
        // @Short Changes the NPC's head to look like a player's skin.
        // @Author David Cernat
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
        // @Name Heal
        // @Usage heal (<#.#>) (<entity>|...)
        // @Required 0
        // @Stable Todo
        // @Short Heals the player.
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
        // @Name Health
        // @Usage health (target:player/{npc}) [<#>]
        // @Required 1
        // @Stable Todo
        // @Short Changes the target's maximum health.
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
                "HEALTH", "health (target:player/{npc}) [<#>]", 1);

        // <--[command]
        // @Name Hurt
        // @Usage hurt (<#.#>) (<entity>|...)
        // @Required 0
        // @Stable Todo
        // @Short Hurts the player.
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
        // @Name If
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
        // @Name Inventory
        // @Usage inventory [open/copy/move/swap/add/remove/keep/exclude/fill/clear] [destination:<inventory>] (origin:<inventory>)
        // @Required 2
        // @Stable Todo
        // @Short Todo
        // @Author David Cernat
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
        // @Name Inject
        // @Usage inject (locally) [<script>] (path:<name>) (instantly)
        // @Required 1
        // @Stable 1.0
        // @Short Runs a script in the current ScriptQueue.
        // @Author aufdemrand
        // @Description
        // Todo
        // @Tags
        // Todo
        // @Usage
        // Todo
        // @Example
        // Todo
        // -->
        registerCoreMember(InjectCommand.class,
                "INJECT", "inject (locally) [<script>] (path:<name>) (instantly)", 1);

        // <--[command]
        // @Name Invisible
        // @Usage invisible [player/npc] [state:true/false/toggle]
        // @Required 2
        // @Stable Todo
        // @Short Makes the player or NPC turn invisible.
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
        // @Name Leash
        // @Usage leash (cancel) [<entity>|...] (holder:<entity>/<location>)
        // @Required 1
        // @Stable Todo
        // @Short Sticks a leash on target entity, held by a fence post or another entity.
        // @Author Alain Blanquet
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
        // @Name Listen
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
        // @Name Log
        // @Usage log [<text>] (type:severe/info/warning/fine/finer/finest) [file:<name>]
        // @Required 2
        // @Stable Todo
        // @Short Logs some debugging info to a file.
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
        // @Name Look
        // @Usage look (<entity>|...) [<location>] (duration:<duration>)
        // @Required 1
        // @Stable Todo
        // @Short Causes the NPC to look at a target location.
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
        // @Name LookClose
        // @Usage lookclose [state:true/false]
        // @Required 1
        // @Stable Todo
        // @Short Toggles whether the NPC will automatically look at nearby players.
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
        // @Name Midi
        // @Usage midi [file:<name>] (<location>/<player>|...) (tempo:<#.#>)
        // @Required 1
        // @Stable Stable
        // @Short Plays a midi file at a given location or to a list of players using note block sounds.
        // @Author David Cernat
        // @Description
        // This will fully load a midi song file stored in plugins/Denizen/midi/
        // The file must be a valid midi file with the extension .mid
        // It will continuously play the song as noteblock songs at the given location or group of players until the song ends.
        // By default, this will play for the connected player only.
        // @Tags
        // None.
        // @Usage
        // Use to play a midi song file at a given location
        // - midi file:Mysong <player.location>
        //
        // @Usage
        // Use to play a midi song file at a given location to the specified player
        // - midi file:Mysong <server.list_online_players>
        // @Example
        // Todo
        // -->
        registerCoreMember(MidiCommand.class,
                "MIDI", "midi [file:<name>] (<location>/<player>|...) (tempo:<#.#>)", 1);

        // <--[command]
        // @Name Mount
        // @Usage mount (cancel) [<entity>|...] (<location>)
        // @Required 0
        // @Stable Todo
        // @Short Mounts one entity onto another.
        // @Author David Cernat
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
        // @Name ModifyBlock
        // @Usage modifyblock [<location>] [<material>] (radius:<#>) (height:<#>) (depth:<#>)
        // @Required 2
        // @Stable Todo
        // @Short Changes a block's material.
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
                "MODIFYBLOCK", "modifyblock [<location>] [<material>] (radius:<#>) (height:<#>) (depth:<#>)", 2);

        // <--[command]
        // @Name Nameplate
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
        // @Name Narrate
        // @Usage narrate ["<text>"] (targets:<player>|...) (format:<name>)
        // @Required 1
        // @Stable Todo
        // @Short Shows some text to the player.
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
        // @Name Note
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
        // @Name Oxygen
        // @Usage oxygen (type:maximum/remaining) (mode:set/add/remove) [qty:<#>]
        // @Required 1
        // @Stable Todo
        // @Short Gives or takes breath from the player.
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
        // @Name Pause
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
        // @Name PlayEffect
        // @Usage playeffect [<location>] [effect:<name>] (data:<#.#>) (visibility:<#.#>) (qty:<#>) (offset:<#.#>)
        // @Required 2
        // @Stable Todo
        // @Short Plays a visible or audible effect at the location.
        // @Author David Cernat
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
        // @Name PlaySound
        // @Usage playsound [<location>] [sound:<name>] (volume:<#.#>) (pitch:<#.#>)
        // @Required 2
        // @Stable Todo
        // @Short Plays a sound at the location.
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
        // @Name Permission
        // @Usage permission [add|remove] [permission] (player:<name>) (group:<name>) (world:<name>)
        // @Required 2
        // @Stable Todo
        // @Short Gives or takes a permission node from the player.
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
        // @Name Pose
        // @Usage pose (player/npc) [id:<name>]
        // @Required 1
        // @Stable Todo
        // @Short Rotates the player or NPC to match a pose.
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
        // @Name Push
        // @Usage push [<entity>|...] (origin:<entity>/<location>) (destination:<location>) (speed:<#.#>) (<duration>) (<script>)
        // @Required 1
        // @Stable mostly
        // @Short Pushes entities through the air in a straight line.
        // @Author David Cernat
        // @Description
        // Pushes entities through the air in a straight line at a certain speed and for a certain duration, triggering a script when they hit an obstacle or stop flying.
        // @Tags
        // Todo
        // @Usage
        // Todo
        // @Example
        // - push e@cow destination:<l@Obelisk>
        // -->
        registerCoreMember(PushCommand.class,
                "PUSH", "push [<entity>|...] (origin:<entity>/<location>) (destination:<location>) (speed:<#.#>) (<duration>) (<script>)", 1);

        // <--[command]
        // @Name Queue
        // @Usage queue (queue:<id>) [clear/pause/resume/delay:<#>]
        // @Required 1
        // @Stable Todo
        // @Short Modifies the current state of a script queue.
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
        // @Name Random
        // @Usage random [<#>]
        // @Required 1
        // @Stable Todo
        // @Short Selects a random choice from the following script commands.
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
        // @Name Remove
        // @Usage remove [<entity>|...] (region:<name>)
        // @Required 0
        // @Stable Todo
        // @Short Despawns a list of entities.
        // @Author David Cernat
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
        // @Name Rename
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
        // @Name Repeat
        // @Usage repeat [<amount>] [<commands>]
        // @Required 1
        // @Stable Todo
        // @Short Runs a series of braced commands several times.
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
        // @Name Reset
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
        // @Name Run
        // @Usage run (locally) [<script>] (path:<name>) (as:<player>/<npc>) (def:<element>|...) (id:<name>) (instantly) (delay:<value>)
        // @Required 1
        // @Stable 1.0
        // @Short Runs a script in a new ScriptQueue.
        // @Author aufdemrand
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
                "RUN", "run (locally) [<script>] (path:<name>) (as:<player>/<npc>) (def:<element>|...) (id:<name>) (instantly) (delay:<value>)", 1);

        // <--[command]
        // @Name RunTask
        // @Deprecated This has been replaced by the Run command.
        // @Usage runtask [<name>] (instantly) (queue(:<name>)) (delay:<#>) (define:<element>|...)
        // @Required 1
        // @Stable Stable
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
        // @Name Scoreboard
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
        // @Name Scribe
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
        // @Name Shoot
        // @Usage shoot [<entity>|...] (origin:<entity>/<location>) (destination:<location>) (height:<#.#>) (gravity:<#.#>) (script:<name>))
        // @Required 1
        // @Stable work in progress
        // @Short Shoots an entity through the air up to a certain height.
        // @Author David Cernat
        // @Description
        // Shoots an entity through the air up to a certain height, optionally using a custom gravity value and triggering a script on impact with a surface.
        // @Tags
        // Todo
        // @Usage
        // Todo
        // @Example
        // Todo
        // -->
        registerCoreMember(ShootCommand.class,
                "SHOOT", "shoot [<entity>|...] (origin:<entity>/<location>) (destination:<location>) (height:<#.#>) (gravity:<#.#>) (script:<name>)", 1);

        // <--[command]
        // @Name ShowFake
        // @Usage showfake [<material>] [<location>|...] (d:<duration>{10s})
        // @Required 2
        // @Stable Todo
        // @Short Makes the player see a block change that didn't actually happen.
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
        // @Name Sign
        // @Usage sign (type:{sign_post}/wall_sign) ["<line>|..."] [<location>] (direction:n/e/w/s)
        // @Required 1
        // @Stable Todo
        // @Short Modifies a sign.
        // @Author David Cernat
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
        // @Name Sit
        // @Usage sit (<location>)
        // @Required 0
        // @Stable Todo
        // @Short Causes the NPC to sit.
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
        // @Name Spawn
        // @Usage spawn [<entity>|...] (<location>) (target:<entity>)
        // @Required 1
        // @Stable Todo
        // @Short Spawns an entity.
        // @Author David Cernat
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
        // @Name Stand
        // @Usage stand
        // @Required 0
        // @Stable Todo
        // @Short Causes the NPC to stand.
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
        // @Name Strike
        // @Usage strike (no_damage) [<location>]
        // @Required 1
        // @Stable Todo
        // @Short Strikes lightning down upon the location.
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
        // @Name Switch
        // @Usage switch [<location>] (state:[{toggle}/on/off]) (duration:<value>)
        // @Required 1
        // @Stable Todo
        // @Short Switches a lever.
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
        // @Name Take
        // @Usage take [money/iteminhand/<item>|...] (qty:<#>) (from:<inventory>)
        // @Required 1
        // @Stable Todo
        // @Short Takes an item from the player.
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
                "TAKE", "take [money/iteminhand/<item>|...] (qty:<#>) (from:<inventory>)", 1);

        // <--[command]
        // @Name Teleport
        // @Usage teleport (<entity>|...) [<location>]
        // @Required 1
        // @Stable Todo
        // @Short Teleports the player or NPC to a new location.
        // @Author David Cernat
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
                "TELEPORT", "teleport (<entity>|...) [<location>]", 1);

        // <--[command]
        // @Name Time
        // @Usage time [type:{global}/player] [<value>] (world:<name>)
        // @Required 1
        // @Stable Todo
        // @Short Changes the current time in the minecraft world.
        // @Author David Cernat
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
        // @Name Trait
        // @Usage trait (state:true/false/{toggle}) [<trait>]
        // @Required 1
        // @Stable Stable
        // @Short Adds or removes a trait from an NPC.
        // @Author Morphan1
        // @Description
        // Todo
        // @Tags
        // Todo
        // @Usage
        // Todo
        // @Example
        // Todo
        // -->
        registerCoreMember(TraitCommand.class,
                "TRAIT", "trait (state:true/false/{toggle}) [<trait>]", 1);


        // <--[command]
        // @Name Trigger
        // @Usage trigger [name:chat/click/damage/proximity] (state:true/false) (cooldown:<#.#>) (radius:<#>)
        // @Required 2
        // @Stable Todo
        // @Short Enables or disables a trigger.
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
        // @Name Viewer
        // @Usage viewer ({create <location>}/modify/remove) [id:<name>] (type:{sign_post}/wall_sign) (display:{location}/score/logged_in) (direction:n/e/w/s)
        // @Required 1
        // @Stable Experimental
        // @Short Creates a sign that auto-updates with information.
        // @Author Morphan1
        //
        // @Description
        // Creates a sign that auto-updates with information about a player, including their location, score, and
        // whether they're logged in or not.
        //
        // @Tags
        // None
        //
        // @Usage
        // Create a sign that shows the location of a player on a wall
        // - viewer player:ThatGuy create 113,76,-302,world id:PlayerLoc1 type:wall_sign display:location
        //
        // @Example
        // Todo
        // -->
        registerCoreMember(ViewerCommand.class,
                "VIEWER", "viewer ({create <location>}/modify/remove) [id:<name>] (type:{sign_post}/wall_sign) (display:{location}/score/logged_in) (direction:n/e/w/s)", 2);

        // <--[command]
        // @Name Vulnerable
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
        // @Name Wait
        // @Usage wait (<duration>) (queue:<name>)
        // @Required 0
        // @Stable Todo
        // @Short Delays a script for a specified amount of time.
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
        // @Name Walk, WalkTo
        // @Usage walkto [<location>] (speed:<#>) (auto_range)
        // @Required 1
        // @Stable Todo
        // @Short Causes the NPC to walk to another location.
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
        // @Name Weather
        // @Usage weather [type:{global}/player] [sunny/storm/thunder] (world:<name>)
        // @Required 1
        // @Stable Todo
        // @Short Changes the current weather in the minecraft world.
        // @Author David Cernat
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
        // @Name Yaml
        // @Usage yaml [load/create/savefile:<file>]/[write:<key>]/[write:<key> value:<value>] [id:<name>]
        // @Required 1
        // @Stable Todo
        // @Short Edits a YAML configuration file.
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
                "YAML", "yaml [load/create/savefile:<file>]/[write:<key>]/[write:<key> value:<value>] [id:<name>]", 1);

        // <--[command]
        // @Name Zap
        // @Usage zap (<script>:)[<step>] (<duration>)
        // @Required 0
        // @Stable Todo
        // @Short Changes the current script step.
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
