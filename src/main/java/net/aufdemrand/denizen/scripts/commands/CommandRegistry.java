package net.aufdemrand.denizen.scripts.commands;

import java.util.HashMap;
import java.util.Map;

import net.aufdemrand.denizen.Denizen;
import net.aufdemrand.denizen.interfaces.dRegistry;
import net.aufdemrand.denizen.interfaces.RegistrationableInstance;
import net.aufdemrand.denizen.scripts.commands.core.*;
import net.aufdemrand.denizen.scripts.commands.item.*;
import net.aufdemrand.denizen.scripts.commands.player.*;
import net.aufdemrand.denizen.scripts.commands.server.*;
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
        if (classes.containsKey(clazz)) return clazz.cast(instances.get(classes.get(clazz)));
        else return null;
    }

    // <--[language]
    // @Name Command Syntax
    // @Description
    // Almost every Denizen command and requirement has arguments after the command itself.
    // These arguments are just snippets of text showing what exactly the command should do,
    // like what the chat command should say, or where the look command should point.
    // But how do you know what to put in the arguments?
    //
    // You merely need to look at the command's usage/syntax info.
    // Let's take for example:
    // - animatechest [<location>] ({open}/close) (sound:{true}/false)
    // Obviously, the command is 'animatechest'... but what does the rest of it mean?
    //
    // Anything in [brackets] is required... you MUST put it there.
    // Anything in (parenthesis) is optional... you only need to put it there if you want to
    // Anything in {braces} is default... the command will just assume this if no argument is actually typed.
    // Anything in <> is non-literal... you must change what is inside of it
    // Anything outside of <> is literal... you must put it exactly as-is
    // <#> represents a number without a decimal, and <#.#> represents a number with a decimal
    //
    // A few examples:
    // [<location>] is required and non-literal... you might fill it with '1,2,3,world' which is a valid location object.
    // (sound:{true}/false) is optional and has a default value of true... you can put sound:false to prevent sound, or leave it blank to allow sound.
    // (repeats:<#>) is optional, has no clear default, and is a number. You can put repeats:3 to repeat three times, or leave it blank to not repeat.
    // Note: Optional arguments without a default usually have a secret default... EG, the (repeats:<#>) above has a secret default of '0'.
    //
    // Also, you should never directly type in [], (), {}, or <> even though they are in the syntax info.
    // The only exception is in a replaceable tag (EG: <npc.has_trait[<traitname>]> will take <npc.has_trait[mytrait]> as a valid actual usage)
    //
    // -->

    @Override
    public void registerCoreMembers() {


        // <--[command]
        // @Name Age
        // @Usage age [<entity>|...] (adult/baby/<age>) (lock)
        // @Required 1
        // @Stable stable
        // @Short Sets the ages of a list of entities, optionally locking them in those ages.
        // @Author David Cernat

        // @Description
        // Some living entity types are 'ageable' which can affect an entities ability to breed, or whether they appear
        // as a baby or an adult. Using the 'age' command allows modification of an entity's age. Specify an entity and
        // either 'baby', 'adult', or an integer age to set the age of an entity. Using the 'lock' argument will
        // keep the entity from increasing its age automatically. NPCs which use ageable entity types can also be
        // specified.

        // @Tags
        // <e@entity.age>

        // @Usage
        // Use to make an ageable entity a permanant baby.
        // - age e@50 baby lock
        // ...or a mature adult.
        // - age e@50 adult lock

        // @Usage
        // Use to make a baby entity an adult.
        // - age n@puppy adult

        // @Usage
        // Use to mature an animals so that it is old enough to breed.
        // - age <player.location.find.entities.within[20]> 10

        // -->
        registerCoreMember(AgeCommand.class,
                "AGE", "age [<entity>|...] (adult/baby/<age>) (lock)", 1);


        // <--[command]
        // @Name Anchor

        // @Usage anchor [id:<name>] [assume/remove/add <location>/walkto/walknear (r:#)]
        // @Required 2
        // @Stable stable
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
        // @Stable stable
        // @Short Makes a list of entities perform a certain animation.
        // @Author David Cernat

        // @Description
        // Minecraft implements several player and entity animations which the animate command can use, just
        // specify an entity and an animation.

        // Player animations require a Player-type entity or NPC. Available player animations include:
        // ARM_SWING, CRIT, HURT, and MAGIC_CRIT

        // All entities, regardless of type, can utilize the 'hurt' animation. Additionally, wolf entities
        // and NPCs can also use: WOLF_SMOKE, WOLF_HEARTS, and WOLF_SHAKE. Sheep entities and NPCs also have
        // available the SHEEP_EAT animation.

        // @Tags
        // None

        // @Usage
        // Use to make a player appear to get hurt.
        // - animate <player> animation:hurt

        // @Usage
        // Use to make a wolf NPC shake
        // - animate '<n@aufdemrand's wolf>' animation:wolf_shake

        // -->
        registerCoreMember(AnimateCommand.class,
                "ANIMATE", "animate [<entity>|...] [animation:<name>]", 1);


        // <--[command]
        // @Name AnimateChest
        // @Usage animatechest [<location>] ({open}/close) (sound:{true}/false)
        // @Required 1
        // @Stable unstable
        // @Short Makes a chest open or close.
        // @Author Todo
        // @Description
        // Todo
        // @Tags
        // Todo
        // @Usage
        // Todo
        // -->
        registerCoreMember(AnimateChestCommand.class,
                "ANIMATECHEST", "animatechest [<location>] ({open}/close) (sound:{true}/false)", 1);


        // <--[command]
        // @Name Announce
        // @Usage announce ["<text>"] (to_ops/to_console/to_flagged:<flag_name>) (format:<name>)
        // @Required 1
        // @Stable stable
        // @Short Announces a message for everyone online to read.
        // @Author aufdemrand

        // @Description
        // Announce sends a raw message to players. Simply using announce with text will send
        // the message to all online players. Specifing the 'to_ops' argument will narrow down the players
        // in which the message is sent to ops only. Alternatively, using the 'to_flagged' argument
        // will send the message to players only if the specified flag does not equal true. You can also
        // use the 'to_console' argument to make it so it only shows in the server console. Announce
        // can also utilize a format script with the 'format' argument. See the format script-container
        // for more information.

        // @Tags
        // None

        // @Usage
        // Use to send an important message to your players.
        // - announce 'Warning! This server will restart in 5 minutes!'

        // @Usage
        // Use to send a message to a specific 'group' of players.
        // - announce to_flagged:clan_subang '[<player.name>] Best clan ever!'

        // @Usage
        // Use to easily send a message to all online ops.
        // - announce to_ops '<player.name> requires help!'

        // @Usage
        // Use to send a message to just the console (Primarily for debugging / logging).
        // - announce to_console 'Warning- <player.name> broke a mob spawner at location <player.location>'

        // -->
        registerCoreMember(AnnounceCommand.class,
                "ANNOUNCE", "announce [\"<text>\"] (to_ops/to_console/to_flagged:<flag_name>) (format:<name>)", 1);


        // <--[command]
        // @Name Assignment
        // @Usage assignment [{set}/remove] (script:<name>)
        // @Required 1
        // @Stable unstable
        // @Short Changes an NPC's assignment.
        // @Author aufdemrand
        // @Description
        // Todo
        // @Tags
        // <n@npc.script>
        // @Usage
        // Todo
        // -->
        registerCoreMember(AssignmentCommand.class,
                "ASSIGNMENT", "assignment [{set}/remove] (script:<name>)", 1);


        // <--[command]
        // @Name Attack
        // @Usage attack (<entity>|...) (target:<entity>/cancel)
        // @Required 0
        // @Stable stable
        // @Short Makes an entity, or list of entities, attack a target.
        // @Author David Cernat

        // @Description
        // By itself, the 'attack' command will act as a NPC command in the sense that an attached
        // NPC will attack the attached player, or specified target. It can also accept a specified entity,
        // or list of entities, to fulfill the command, just specify a 'fetchable' entity object. This includes
        // player objects (dPlayers) and NPC objects (dNPCs). To specify the target, prefix the entity
        // object with 'target:' or 't:'.
        //
        // To cancel an attack, use the 'cancel' argument instead of specifying a target.

        // @Tags
        // <n@npc.navigator.is_fighting>
        // <n@npc.navigator.attack_strategy>
        // <n@npc.navigator.target_entity>

        // @Usage
        // Use to make a NPC attack a player in an interact script.
        // - attack

        // @Usage
        // Use to make a NPC attack a nearby entity.
        // - attack target:<npc.location.find.living_entities.within[10].random>

        // @Usage
        // Use to make a specific entity attack an entity, including players or npcs.
        // - attack <player.location.find.living_entities.within[10].random> target:<player>

        // @Usage
        // Use to stop an attack
        // - attack n@Herobrine stop

        // -->
        registerCoreMember(AttackCommand.class,
                "ATTACK", "attack (<entity>|...) (target:<entity>/cancel)", 0);


        // <--[command]
        // @Name Break
        // @Usage break [<location>] (entity:<entity>) (radius:<#.#>)
        // @Required 1
        // @Stable unstable
        // @Short Makes the NPC walk over and break a block. (Doesn't work!)
        // @Author aufdemrand
        // @Description
        // Todo
        // @Tags
        // Todo
        // @Usage
        // Todo
        // -->
        registerCoreMember(BreakCommand.class,
                "BREAK", "break [<location>] (entity:<entity>) (radius:<#.#>)", 1);


        // <--[command]
        // @Name Burn
        // @Usage burn [<entity>|...] (duration:<value>)
        // @Required 1
        // @Stable stable
        // @Short Sets a list of entities on fire.
        // @Author David Cernat
        // @Description
        // Burn will set a list of entities on fire. Just specify a list of entities (or a single entity) and
        // optionally, a duration. Normal mobs and players will see damage afflicted, but NPCs will block damage
        // from a burn unless 'vulnerable'. Since this command sets the total time of fire, it can also be used
        // to cancel fire on a burning entity by specifying a duration of 0. Specifying no duration will result
        // in a 5 second burn.

        // @Tags
        // <e@entity.fire_time>

        // @Usage
        // Use to set an entity on fire.
        // - burn <player> duration:10s
        // - narrate 'You are on fire!'

        // @Usage
        // Use to cancel fire on entities.
        // - burn <player.location.find.living_entities.within[10]> duration:0
        // - narrate 'You have cast a spell of reduce burn!'

        // -->
        registerCoreMember(BurnCommand.class,
                "BURN", "burn [<entity>|...] (duration:<value>)", 1);


        // <--[command]
        // @Name Cast, Potion
        // @Usage cast [<effect>] (remove) (duration:<value>) (power:<#>) (<entity>|...)
        // @Required 1
        // @Stable Stable
        // @Short Casts a potion effect to a list of entities.
        // @Author aufdemrand, Jeebiss, Morphan1

        // @Description
        // Casts or removes a potion effect to or from a list of entities. If you don't specify a duration,
        // it defaults to 60 seconds. If you don't specify a power level, it defaults to 1.

        // @Tags
        // <e@entity.has_effect[<effect>]>

        // @Usage
        // Use to apply an effect to an entity
        // - potion jump <player> d:120 p:3
        // - narrate "You have been given the temporary ability to jump like a kangaroo."

        // @Usage
        // Use to remove an effect from an entity
        // - if <p@Player.has_effect[jump]> {
        //   - potion jump remove <player>
        //   }
        //
        // -->
        registerCoreMember(CastCommand.class,
                "CAST, POTION", "cast [<effect>] (remove) (duration:<value>) (power:<#>) (<entity>|...)", 1);

        // <--[command]
        // @Name Chat
        // @Usage chat ["<text>"] (targets:<entity>|...)
        // @Required 1
        // @Stable stable
        // @Short Causes the NPC to send a chat message to nearby players.
        // @Author aufdemrand

        // @Description
        // Chat uses a NPCs SpeechController provided by Citizens2, typically inside 'interact' or 'task'
        // script-containers. Typically there is already player and NPC context inside a queue that is using
        // the 'chat' command. In this case, only a text string is required. Alternatively, target entities
        // can be specified to have the NPC chat to a different target/targets.
        //
        // Chat from a NPC is formatted by the settings present in Citizens' config.yml. Players being chatted
        // to see a slightly different message than surrounding players. By default, a 'chat' will allow other
        // players nearby to also see the conversation. For example:
        // <code>
        // - chat 'Hello!'
        // </code>
        // The player being chatted to, by default the attached Player to the script queue, will see a message
        // 'Jack says to you, Hello!', however surrounding entities will see something along the lines of
        // 'Jack says to aufdemrand, Hello!'. The format for this is configurable.
        //
        // If sending messages to the Player without any surrounding entities hearing the message is desireable,
        // it is often times recommended to instead use the 'narrate' command. Alternatively, on a server-wide scale,
        // the configuration node for the 'max range' can be set to 0, however this is discouraged.

        // @Tags
        // None

        // @Usage
        // Use to emulate a NPC talking out loud to a Player within an interact script-container.
        // - chat "Hello, <player.name>! Nice day, eh?"

        // @Usage
        // Use to have a NPC talk to a group of individuals.
        // - flag <npc> talk_targets:!
        // - foreach <npc.location.find.living_entities.within[6]> {
        //     - if <%value%.is_player> && <%value%.flag[clan_initiate]>
        //       flag <npc> talk_targets:->:%value%
        //   }
        // - chat targets:<npc.flag[talk_targets].as_list> "Welcome, initiate!"

        // -->
        registerCoreMember(ChatCommand.class,
                "CHAT", "chat [\"<text>\"] (targets:<entity>|...)", 1);


        // <--[command]
        // @Name ChunkLoad
        // @Usage chunkload ({add}/remove/removeall) [<location>] (duration:<value>)
        // @Required 1
        // @Stable stable
        // @Short Keeps a chunk actively loaded and allowing NPC activity.
        // @Author spaceemotion, mcmonkey
        // @Description
        // Todo
        // @Tags
        // Todo
        // @Usage
        // Todo
        // -->
        registerCoreMember(ChunkLoadCommand.class,
                "CHUNKLOAD", "chunkload ({add}/remove/removeall) [<location>] (duration:<value>)", 1);


        // <--[command]
        // @Name Compass
        // @Usage compass [<location>]
        // @Required 1
        // @Stable stable
        // @Short Redirects the player's compass to target the given location.
        // @Author mcmonkey
        // @Description
        // Todo
        // @Tags
        // <p@player.compass.target>
        // @Usage
        // Todo
        // -->
        registerCoreMember(CompassCommand.class,
                "COMPASS", "compass [<location>]", 1);


        // <--[command]
        // @Name Cooldown
        // @Usage cooldown [<duration>] (global) (s:<script>)
        // @Required 1
        // @Stable stable
        // @Short Temporarily disables a script-container from meeting requirements.
        // @Author aufdemrand

        // @Description
        // Cools down a script-container. If an interact-container, when on cooldown, scripts will not pass a
        // requirements check allowing the next highest priority script to trigger. If any other type of script, a
        // manual requirements check (<s@script_name.requirements.check>) will also return false until the cooldown
        // period is completed. Cooldown requires a type (player or global), a script, and a duration. It also requires
        // a valid link to a dPlayer if using player-type cooldown.
        //
        // Cooldown periods are persistent through a server restart as they are saved in the saves.yml.

        // @Tags
        // <s@script_name.cooled_down[player]>
        // <s@script_name.cooldown>
        // <s@requirements.check>

        // @Usage
        // Use to keep the current interact script from meeting requirements.
        // - cooldown 20m

        // @Usage
        // Use to keep a player from activating a script for a specified duration.
        // - cooldown 11h s:s@bonus_script
        // - cooldown 5s s:s@hit_indicator

        // @Usage
        // Use the 'global' argument to indicate the script to be on cooldown for all players.
        // - cooldown global 24h s:s@daily_treasure_offering

        // -->
        registerCoreMember(CooldownCommand.class,
                "COOLDOWN", "cooldown [<duration>] (global) (s:<script>)", 1);


        // <--[command]
        // @Name CopyBlock
        // @Usage copyblock [location:<location>] [to:<location>]
        // @Required 1
        // @Stable unstable
        // @Short Copies a block to another location, keeping all metadata.
        // @Author aufdemrand, David Cernat
        // @Description
        // Todo
        // @Tags
        // <l@location.block. * >
        // @Usage
        // Todo
        // -->
        registerCoreMember(CopyBlockCommand.class,
                "COPYBLOCK", "copyblock [location:<location>] [to:<location>]", 1);


        // <--[command]
        // @Name CreateWorld
        // @Usage createworld [<name>] (g:<generator>)
        // @Required 1
        // @Stable unstable
        // @Short Creates a new world
        // @Author Todo
        // @Description
        // Todo
        // @Tags
        // <server.list_worlds>
        // @Usage
        // Todo
        // -->
        registerCoreMember(CreateWorldCommand.class,
                "CREATEWORLD", "createworld [<name>] (g:<generator>)", 1);


        // <--[command]
        // @Name Define
        // @Usage define [<id>] [<value>]
        // @Required 2
        // @Stable stable
        // @Short Creates a temporary variable inside a script queue.
        // @Author aufdemrand

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

        // @Tags
        // %id% to get the value assigned to an ID

        // @Usage
        // Use to make complex tags look less complex, and scripts more readable.
        // - narrate 'You invoke your power of notice...'
        // - define range '<player.flag[range_level].mul[3]>'
        // - define blocks '<player.flag[noticeable_blocks>'
        // - narrate '[NOTICE] You have noticed <player.location.find.blocks[%blocks%].within[%range].size>
        // blocks in the area that may be of interest.'

        // @Usage
        // Use to keep the value of a replaceable tag that you might use many times within a single script. Definitions
        // can be faster and cleaner than reusing a replaceable tag over and over.
        // - define arg1 <c.args.get[1]>
        // - if %arg1% == hello narrate 'Hello!'
        // - if %arg1% == goodbye narrate 'Goodbye!'

        // @Usage
        // Use to pass some important information (arguments) on to another queue.
        // - run 'new_task' d:hello|world
        // 'new_task' now has some definitions, %1% and %2%, that contains the contents specified, 'hello' and 'world'.

        // -->
        registerCoreMember(DefineCommand.class,
                "DEFINE", "define [<id>] [<value>]", 2);


        // <--[command]
        // @Name Determine
        // @Usage determine [<value>]
        // @Required 1
        // @Stable stable
        // @Short Sets the outcome of an event.
        // @Author aufdemrand
        // @Description
        // Todo
        // @Tags
        // Todo
        // @Usage
        // Todo
        // -->
        registerCoreMember(DetermineCommand.class,
                "DETERMINE", "determine [<value>]", 1);


        // <--[command]
        // @Name Disengage
        // @Usage disengage (npc:<npc>)
        // @Required 0
        // @Stable stable
        // @Short Enables a NPCs triggers that have been temporarily disabled by the engage command.
        // @Author aufdemrand

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

        // @Tags
        // <n@npc.engaged>

        // @Usage
        // Use to reenable a NPC's triggers, disabled via 'engage'.
        // - engage
        // - chat 'Be right there!'
        // - walkto <player.location>
        // - wait 5s
        // - disengage

        // -->
        registerCoreMember(DisengageCommand.class,
                "DISENGAGE", "disengage (npc:<npc>)", 0);


        // <--[command]
        // @Name DisplayItem
        // @Usage displayitem [<item>] [<location>] (duration:<value>)
        // @Required 2
        // @Stable unstable
        // @Short Makes a non-touchable item spawn for players to view.
        // @Author aufdemrand, mcmonkey
        // @Description
        // Todo
        // @Tags
        // Todo
        // @Usage
        // Todo
        // -->
        registerCoreMember(DisplayItemCommand.class,
                "DISPLAYITEM", "displayitem [<item>] [<location>] (duration:<value>)", 2);


        // <--[command]
        // @Name Drop
        // @Usage drop [<item>/<entity_type>/xp] [<location>] (qty:<#>)
        // @Required 1
        // @Stable stable
        // @Short Drops an item, entity, or experience orb on a location.
        // @Author aufdemrand

        // @Description
        // To drop an item, just specify a valid item object. To drop
        // an entity, specify a generic entity object. Drop can also reward players
        // with experience orbs by using the 'xp' argument.
        //
        // For all three usages, you can optionally specify an integer with 'qty:'
        // prefix to drop multiple items/entities/xp.

        // @Tags
        // None

        // @Usage
        // Use to drop some loot around the player.
        // - drop i@gold_nugget <cu@<player.location.add[-2,-2,-2]>|<player.location.add[2,2.2]>.get_spawnable_blocks.random>

        // @Usage
        // Use to reward a player
        // - drop xp qty:500 <player.location>

        // @Usage
        // Use to drop a nasty surpise (exploding TNT)
        // - drop e@primed_tnt <player.location>

        // -->
        registerCoreMember(DropCommand.class,
                "DROP", "drop [<item>/<entity_type>/xp] [<location>] (qty:<#>)", 1);


        // <--[command]
        // @Name Engage
        // @Usage engage (<duration>) (npc:<npc>)
        // @Required 0
        // @Stable stable
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
        // <n@npc.engaged>
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
        // -->
        registerCoreMember(EngageCommand.class,
                "ENGAGE", "engage (<duration>) (npc:<npc>)", 0);


        // <--[command]
        // @Name Engrave
        // @Usage engrave (set/remove)
        // @Required 0
        // @Stable unstable
        // @Short Locks an item to a player *does not work currently*
        // @Author Todo
        // @Description
        // Todo
        // @Tags
        // Todo
        // @Usage
        // Todo
        // -->
        registerCoreMember(EngraveCommand.class,
                "ENGRAVE", "engrave (set/remove)", 0);


        // <--[command]
        // @Name Equip
        // @Usage equip (<entity>|...) (hand:<item>) (head:<item>) (chest:<item>) (legs:<item>) (boots:<item>)
        // @Required 1
        // @Stable stable
        // @Short Equips items and armor on a list of entities.
        // @Author Todo
        // @Description
        // Todo
        // @Tags
        // <e@entity.equipment>
        // @Usage
        // Todo
        // -->
        registerCoreMember(EquipCommand.class,
                "EQUIP", "equip (<entity>|...) (hand:<item>) (head:<item>) (chest:<item>) (legs:<item>) (boots:<item>)", 1);


        // <--[command]
        // @Name Execute
        // @Usage execute [as_player/as_op/as_npc/as_server] [<Bukkit command>]
        // @Required 2
        // @Stable stable
        // @Short Executes an arbitrary server command as if the player, NPC, or server typed it in.
        // @Author Todo
        // @Description
        // Todo
        // @Tags
        // Todo
        // @Usage
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
        // <p@player.xp>
        // <p@player.xp.to_next_level>
        // <p@player.xp.total>
        // <p@player.xp.level>
        // @Usage
        // Todo
        // -->
        registerCoreMember(ExperienceCommand.class,
                "EXPERIENCE", "experience [{set}/give/take] (level) [<#>]", 2);


        // <--[command]
        // @Name Explode
        // @Usage explode (power:<#.#>) (<location>) (fire) (breakblocks)
        // @Required 0
        // @Stable stable
        // @Short Causes an explosion at the location.
        // @Author Alain Blanquet
        // @Description
        // Todo
        // @Tags
        // Todo
        // @Usage
        // Todo
        // -->
        registerCoreMember(ExplodeCommand.class,
                "EXPLODE", "explode (power:<#.#>) (<location>) (fire) (breakblocks)", 0);


        // <--[command]
        // @Name Fail
        // @Usage fail (script:<name>)
        // @Required 0
        // @Stable stable
        // @Short Marks a script as having failed.
        // @Author aufdemrand
        // @Description
        // Todo
        // @Tags
        // Todo
        // @Usage
        // Todo
        // -->
        registerCoreMember(FailCommand.class,
                "FAIL", "fail (script:<name>)", 0);


        // <--[command]
        // @Name Feed
        // @Usage feed (amt:<#>) (target:<entity>|...)
        // @Required 0
        // @Stable unstable
        // @Short Refills the player's food bar.
        // @Author aufdemrand, Jeebiss
        // @Description
        // Todo
        // @Tags
        // <p@player.food_level>
        // <p@player.food_level.formatted>
        // @Usage
        // Todo
        // -->
        registerCoreMember(FeedCommand.class,
                "FEED", "feed (amt:<#>) (target:<entity>|...)", 0);


        // <--[command]
        // @Name Finish
        // @Usage finish (script:<name>)
        // @Required 0
        // @Stable stable
        // @Short Marks a script as having been completed successfully.
        // @Author aufdemrand
        // @Description
        // Todo
        // @Tags
        // Todo
        // @Usage
        // Todo
        // -->
        registerCoreMember(FinishCommand.class,
                "FINISH", "finish (script:<name>)", 0);


        // <--[command]
        // @Name Firework
        // @Usage firework (<location>) (power:<#>) (type:<name>/random) (primary:<color>|...) (fade:<color>|...) (flicker) (trail)
        // @Required 0
        // @Stable Todo
        // @Short Launches a firework.
        // @Author David Cernat
        // @Description
        // Todo
        // @Tags
        // Todo
        // @Usage
        // Todo
        // -->
        registerCoreMember(FireworkCommand.class,
                "FIREWORK", "firework (<location>) (power:<#>) (type:<name>/random) (primary:<color>|...) (fade:<color>|...) (flicker) (trail)", 0);


        // <--[command]
        // @Name Fish
        // @Usage fish (catchfish) (stop) (<location>) (catchpercent:<#>)
        // @Required 1
        // @Stable Todo
        // @Short Causes an NPC to begin fishing
        // @Author Todo
        // @Description
        // Todo
        // @Tags
        // Todo
        // @Usage
        // Todo
        // -->
        registerCoreMember(FishCommand.class,
                "FISH", "fish (catchfish) (stop) (<location>) (catchpercent:<#>)", 1);


        // <--[command]
        // @Name Flag
        // @Usage flag ({player}/npc/global) [<name>([<#>])](:<action>)[:<value>] (duration:<value>)
        // @Required 1
        // @Stable stable
        // @Short Sets or modifies a flag on the player, NPC, or server.
        // @Author aufdemrand
        // @Description
        // Todo
        // @Tags
        // <p@player.flag[<flag>]>
        // <n@npc.flag[<flag>]>
        // <global.flag[<flag>]>
        // @Usage
        // Todo
        // -->
        registerCoreMember(FlagCommand.class,
                "FLAG", "flag ({player}/npc/global) [<name>([<#>])](:<action>)[:<value>] (duration:<value>)", 1);


        // <--[command]
        // @Name Fly
        // @Usage fly (cancel) [<entity>|...] (controller:<player>) (origin:<location>) (destinations:<location>|...) (speed:<#.#>) (rotationthreshold:<#.#>)
        // @Required 1
        // @Stable stable
        // @Short Make an entity fly where its controller is looking or fly to waypoints.
        // @Author David Cernat
        // @Description
        // Todo
        // @Tags
        // Todo
        // @Usage
        // Todo
        // -->
        registerCoreMember(FlyCommand.class,
                "FLY", "fly (cancel) [<entity>|...] (controller:<player>) (origin:<location>) (destinations:<location>|...) (speed:<#.#>) (rotationthreshold:<#.#>)", 1);


        // <--[command]
        // @Name Follow
        // @Usage follow (stop) (lead:<#.#>) (target:<entity>)
        // @Required 0
        // @Stable unstable
        // @Short Causes the NPC to follow a target (Currently experiencing bugs with lead: )
        // @Author aufdemrand
        // @Description
        // Todo
        // @Tags
        // Todo
        // @Usage
        // Todo
        // -->
        registerCoreMember(FollowCommand.class,
                "FOLLOW", "follow (stop) (lead:<#.#>) (target:<entity>)", 0);


        // <--[command]
        // @Name ForEach
        // @Usage foreach [<object>|...] [<commands>]
        // @Required 2
        // @Stable stable
        // @Short Loops through a dList, running a set of commands for each item.
        // @Author Morphan1, mcmonkey

        // @Description
        // Loops through a dList of any type. For each item in the dList, the specified commands will be ran for
        // that list entry. To call the value of the entry while in the loop, you can use %value%.

        // @Tags
        // %value% to get the current item in the loop

        // @Usage
        // Use to run commands for 'each entry' in a list of objects/elements.
        // - foreach li@e@123|n@424|p@BobBarker {
        //     - announce "There's something at <%value%.location>!"
        //   }

        // @Usage
        // Use to iterate through entries in any tag that returns a list
        // - foreach <server.list_online_players> {
        //       - narrate "Thanks for coming to our server! Here's a bonus $50.00!"
        //     - give %value% money qty:50
        //   }

        // -->
        registerCoreMember(ForEachCommand.class,
                "FOREACH", "foreach [<object>|...] [<commands>]", 2);


        // <--[command]
        // @Name Give
        // @Usage give [money/<item>|...] (qty:<#>) (engrave) (to:<inventory>)
        // @Required 1
        // @Stable stable
        // @Short Gives the player an item or money.
        // @Author Todo
        // @Description
        // Todo
        // @Tags
        // <p@player.money>
        // @Usage
        // Todo
        // -->
        registerCoreMember(GiveCommand.class,
                "GIVE", "give [money/<item>|...] (qty:<#>) (engrave) (to:<inventory>)", 1);


        // <--[command]
        // @Name Group
        // @Usage group [add/remove] [<group>] (world:<name>)
        // @Required 2
        // @Stable Todo
        // @Short Adds a player to or removes a player from a permissions group.
        // @Author Todo
        // @Description
        // Todo
        // @Tags
        // <p@player.in_group[<group>]>
        // <p@player.in_group[<group>].global>
        // <p@player.in_group[<group>].world>
        // @Usage
        // Todo
        // -->
        registerCoreMember(GroupCommand.class,
                "GROUP", "group [add/remove] [<group>] (world:<name>)", 2);


        // <--[command]
        // @Name Head
        // @Usage head (<entity>|...) [skin:<player>]
        // @Required 1
        // @Stable stable
        // @Short Makes players or NPCs wear a specific player's head.
        // @Author David Cernat
        // @Description
        // Todo
        // @Tags
        // Todo
        // @Usage
        // Todo
        // -->
        registerCoreMember(HeadCommand.class,
                "HEAD", "head (<entity>|...) [skin:<player>]", 1);


        // <--[command]
        // @Name Heal
        // @Usage heal (<#.#>) (<entity>|...)
        // @Required 0
        // @Stable stable
        // @Short Heals the player.
        // @Author aufdemrand, Jeebiss, morphan1, mcmonkey
        // @Description
        // Todo
        // @Tags
        // <e@entity.health>
        // @Usage
        // Todo
        // -->
        registerCoreMember(HealCommand.class,
                "HEAL", "heal (<#.#>) (<entity>|...)", 0);


        // <--[command]
        // @Name Health
        // @Usage health (target:player/{npc}) [<#>]
        // @Required 1
        // @Stable stable
        // @Short Changes the target's maximum health.
        // @Author mcmonkey
        // @Description
        // Todo
        // @Tags
        // <e@entity.health>
        // <e@entity.health.max>
        // @Usage
        // Todo
        // -->
        registerCoreMember(HealthCommand.class,
                "HEALTH", "health (target:player/{npc}) [<#>]", 1);


        // <--[command]
        // @Name Hurt
        // @Usage hurt (<#.#>) (<entity>|...)
        // @Required 0
        // @Stable stable
        // @Short Hurts the player.
        // @Author aufdemrand, Jeebiss, morphan1, mcmonkey
        // @Description
        // Todo
        // @Tags
        // <e@entity.health>
        // @Usage
        // Todo
        // -->
        registerCoreMember(HurtCommand.class,
                "HURT", "hurt (<#.#>) (<entity>|...)", 0);


        // <--[command]
        // @Name If
        // @Usage if [<value>] (!)(<operator> <value>) (&&/|| ...) [<commands>] (else <commands>)
        // @Required 2
        // @Stable stable
        // @Short Compares values, and runs one script if they match, or a different script if they don't match.
        // @Author aufdemrand, David Cernat
        // @Description
        // Todo
        // @Tags
        // Todo
        // @Usage
        // Todo
        // -->
        registerCoreMember(IfCommand.class,
                "IF", "if [<value>] (!)(<operator> <value>) (&&/|| ...) [<commands>] (else <commands>)", 2);


        // <--[command]
        // @Name Inventory
        // @Usage inventory [open/copy/move/swap/add/remove/keep/exclude/fill/clear] (destination:<inventory>) (origin:<inventory>)
        // @Required 1
        // @Stable stable
        // @Short Edits the inventory of a player, NPC, or chest.
        // @Author David Cernat, morphan1
        // @Description
        // Todo
        // @Tags
        // <player.inventory>
        // <npc.inventory>
        // <location.inventory>
        // @Usage
        // Todo
        // -->
        registerCoreMember(InventoryCommand.class,
                "INVENTORY", "inventory [open/copy/move/swap/add/remove/keep/exclude/fill/clear] (destination:<inventory>) (origin:<inventory>)", 1);


        // <--[command]
        // @Name Inject
        // @Usage inject (locally) [<script>] (path:<name>) (instantly)
        // @Required 1
        // @Stable stable
        // @Short Runs a script in the current ScriptQueue.
        // @Author aufdemrand
        // @Description
        // Todo
        // @Tags
        // Todo
        // @Usage
        // Todo
        // -->
        registerCoreMember(InjectCommand.class,
                "INJECT", "inject (locally) [<script>] (path:<name>) (instantly)", 1);


        // <--[command]
        // @Name Invisible
        // @Usage invisible [player/npc] [state:true/false/toggle]
        // @Required 2
        // @Stable unstable
        // @Short Makes the player or NPC turn invisible. (Does not fully work currently)
        // @Author aufdemrand
        // @Description
        // Todo
        // @Tags
        // Todo
        // @Usage
        // Todo
        // -->
        registerCoreMember(InvisibleCommand.class,
                "INVISIBLE", "invisible [player/npc] [state:true/false/toggle]", 2);


        // <--[command]
        // @Name Leash
        // @Usage leash (cancel) [<entity>|...] (holder:<entity>/<location>)
        // @Required 1
        // @Stable stable
        // @Short Sticks a leash on target entity, held by a fence post or another entity.
        // @Author Alain Blanquet, mcmonkey
        // @Description
        // Todo
        // @Tags
        // <e@entity.is_leashed>
        // <e@entity.get_leash_holder>
        // @Usage
        // Todo
        // -->
        registerCoreMember(LeashCommand.class,
                "LEASH", "leash (cancel) [<entity>|...] (holder:<entity>/<location>)", 1);


        // <--[command]
        // @Name Listen
        // @Usage listen ({new}/cancel/finish) [kill/block/item/itemdrop/travel] [<requirements>] [script:<name>] (id:<name>)
        // @Required 2
        // @Stable unstable
        // @Short Listens for the player achieving various actions and runs a script when they are completed.
        // @Author aufdemrand, Jeebiss

        // @Description
        // This will create a listener object that listens for when the player does specific actions,
        // and when the player has done the action enough times, this will run a specified script. Used
        // as the meat of quest style scripts, listeners are the foundation for getting information
        // about what the player is doing. This command has 4 basic arguments that apply to every listener,
        // and then accepts any number of additional arguments for the specific listener type. Once
        // created, a listener will remain active until it is finished by the player, finished via
        // a script, or canceled via a script.
        //
        // The currently available listener types are: Kill, Block, Item, Itemdrop, and Travel
        //
        // Summary:
        // Kill - Used to detect when a player kills an NPC, player, entity, or player in a specific permission group.
        // Block - Used to detect when a player breaks, places, or collects blocks in the specified list.
        // Item - Used to detect when a player crafts, smelts, or fishes an item on the specified list.
        // Itemdrop - TODO
        // Travel - Used to detect when a player travels to an npc, to a specific location, into an area, or a specific distance.
        //
        // Detailed usage information can be found in the specific listener files.

        // @Tags
        // Todo

        // @Usage
        // Use to listen for when the player kills 10 zombies.
        // - listen kill type:entity target:zombie qty:10 script:ZombiesKilled

        // @Usage
        // Use to listen for when a player mines 1 iron ore.
        // - listen block type:break block:iron_or qty:1 script:IronMined

        // @Usage
        // Use to listen for when a player crafts 1 wooden sword.
        // - listen item type:craft item:wood_sword qty:1 script:SwordCrafted

        // @Usage
        // Use to... (TODO: Itemdrop)

        // @Usage
        // Use to listen for when a player walks for 150 blocks.
        // - listen travel type:distance distance:150 script:DistanceTraveled
        // -->
        registerCoreMember(ListenCommand.class,
                "LISTEN", "listen ({new}/cancel/finish) [kill/block/item/itemdrop/travel] [<requirements>] [script:<name>] (id:<name>)", 2);


        // <--[command]
        // @Name Log
        // @Usage log [<text>] (type:severe/info/warning/fine/finer/finest) [file:<name>]
        // @Required 2
        // @Stable Todo
        // @Short Logs some debugging info to a file.
        // @Author spaceemotion
        // @Description
        // Todo
        // @Tags
        // Todo
        // @Usage
        // Todo
        // -->
        registerCoreMember(LogCommand.class,
                "LOG", "log [<text>] (type:severe/info/warning/fine/finer/finest) [file:<name>]", 2);


        // <--[command]
        // @Name Look
        // @Usage look (<entity>|...) [<location>] (duration:<duration>)
        // @Required 1
        // @Stable stable
        // @Short Causes the NPC or other entity to look at a target location.
        // @Author aufdemrand, mcmonkey
        // @Description
        // Todo
        // @Tags
        // <l@location.yaw>
        // <l@location.pitch>
        // @Usage
        // Todo
        // -->
        registerCoreMember(LookCommand.class,
                "LOOK", "look (<entity>|...) [<location>] (duration:<duration>)", 1);


        // <--[command]
        // @Name LookClose
        // @Usage lookclose [state:true/false]
        // @Required 1
        // @Stable unstable
        // @Short Toggles whether the NPC will automatically look at nearby players.
        // @Author aufdemrand
        // @Description
        // Todo
        // @Tags
        // Todo
        // @Usage
        // Todo
        // -->
        registerCoreMember(LookcloseCommand.class,
                "LOOKCLOSE", "lookclose [state:true/false]", 1);


        // <--[command]
        // @Name Midi
        // @Usage midi (cancel) [<file>] (<location>/<entity>|...) (tempo:<#.#>)
        // @Required 1
        // @Stable stable
        // @Short Plays a midi file at a given location or to a list of players using note block sounds.
        // @Author David Cernat

        // @Description
        // This will fully load a midi song file stored in the '../plugins/Denizen/midi/' folder. The file
        // must be a valid midi file with the extension '.mid'. It will continuously play the song as
        // noteblock songs at the given location or group of players until the song ends. If no location or
        // entitiy is specified, by default this will play for the attached player.
        //
        // Also, an example Midi song file has been included: "Denizen" by Black Coyote
        // He made it just for us! Check out more of his amazing work at:
        // http://www.youtube.com/user/BlaCoyProductions

        // @Tags
        // None

        // @Usage
        // Use to play a midi song file on the current player
        // - midi file:Denizen

        // @Usage
        // Use to play a midi song file at a given location
        // - midi file:Denizen <player.location>

        // @Usage
        // Use to play a midi song file at a given location to the specified player
        // - midi file:Denizen <server.list_online_players>

        // -->
        registerCoreMember(MidiCommand.class,
                "MIDI", "midi (cancel) [<file>] (<location>/<entity>|...) (tempo:<#.#>)", 1);


        // <--[command]
        // @Name Mount
        // @Usage mount (cancel) [<entity>|...] (<location>)
        // @Required 0
        // @Stable stable
        // @Short Mounts one entity onto another.
        // @Author David Cernat
        // @Description
        // Todo
        // @Tags
        // <e@entity.get_vehicle>
        // <e@entity.is_inside_vehicle>
        // @Usage
        // Todo
        // -->
        registerCoreMember(MountCommand.class,
                "MOUNT", "mount (cancel) [<entity>|...] (<location>)", 0);


        // <--[command]
        // @Name ModifyBlock
        // @Usage modifyblock [<location>] [<material>] (radius:<#>) (height:<#>) (depth:<#>)
        // @Required 2
        // @Stable unstable
        // @Short Changes a block's material.
        // @Author Jeebiss
        // @Description
        // Todo
        // @Tags
        // <l@location.block.material>
        // @Usage
        // Todo
        // -->
        registerCoreMember(ModifyBlockCommand.class,
                "MODIFYBLOCK", "modifyblock [<location>] [<material>] (radius:<#>) (height:<#>) (depth:<#>)", 2);


        // <--[command]
        // @Name Nameplate
        // @Usage nameplate [<chatcolor>] [set:<text>] (target:<player>)  +--> Requires ProtocolLib
        // @Required 1
        // @Stable unstable
        // @Short Changes something's nameplate, requires ProtocolLib (Unstable and broken!)
        // @Author spaceemotion
        // @Description
        // Todo
        // @Tags
        // Todo
        // @Usage
        // Todo
        // -->
        registerCoreMember(NameplateCommand.class,
                "NAMEPLATE", "nameplate [<chatcolor>] [set:<text>] (target:<player>)  +--> Requires ProtocolLib", 1);


        // <--[command]
        // @Name Narrate
        // @Usage narrate ["<text>"] (targets:<player>|...) (format:<name>)
        // @Required 1
        // @Stable stable
        // @Short Shows some text to the player.
        // @Author aufdemrand
        // @Description
        // Todo
        // @Tags
        // Todo
        // @Usage
        // Todo
        // -->
        registerCoreMember(NarrateCommand.class,
                "NARRATE", "narrate [\"<text>\"] (targets:<player>|...) (format:<name>)", 1);


        // <--[command]
        // @Name Note
        // @Usage note [<Notable dObject>] [as:<name>]
        // @Required 2
        // @Stable unstable
        // @Short Adds a new notable object.
        // @Author aufdemrand
        // @Description
        // Todo
        // @Tags
        // Todo
        // @Usage
        // Todo
        // -->
        registerCoreMember(NoteCommand.class,
                "NOTE", "note [<Notable dObject>] [as:<name>]", 2);


        // <--[command]
        // @Name Oxygen
        // @Usage oxygen (type:maximum/remaining) (mode:set/add/remove) [qty:<#>]
        // @Required 1
        // @Stable unstable
        // @Short Gives or takes breath from the player.
        // @Author Todo
        // @Description
        // Todo
        // @Tags
        // <p@player.oxygen>
        // <p@player.oxygen.max>
        // @Usage
        // Todo
        // -->
        registerCoreMember(OxygenCommand.class,
                "OXYGEN", "oxygen (type:maximum/remaining) (mode:set/add/remove) [qty:<#>]", 1);


        // <--[command]
        // @Name Pause
        // @Usage pause [waypoints/navigation]
        // @Required 1
        // @Stable unstable
        // @Short Pauses an NPC's navigation.
        // @Author Todo
        // @Description
        // Todo
        // @Tags
        // <n@npc.navigator>
        // @Usage
        // Todo
        // -->
        registerCoreMember(PauseCommand.class,
                "PAUSE", "pause [waypoints/navigation]", 1);


        // <--[command]
        // @Name PlayEffect
        // @Usage playeffect [<location>] [effect:<name>] (data:<#.#>) (visibility:<#.#>) (qty:<#>) (offset:<#.#>)
        // @Required 2
        // @Stable stable
        // @Short Plays a visible or audible effect at the location.
        // @Author David Cernat
        // @Description
        // Todo
        // @Tags
        // Todo
        // @Usage
        // Todo
        // -->
        registerCoreMember(PlayEffectCommand.class,
                "PLAYEFFECT", "playeffect [<location>] [effect:<name>] (data:<#.#>) (visibility:<#.#>) (qty:<#>) (offset:<#.#>)", 2);


        // <--[command]
        // @Name PlaySound
        // @Usage playsound [<location>] [sound:<name>] (volume:<#.#>) (pitch:<#.#>)
        // @Required 2
        // @Stable stable
        // @Short Plays a sound at the location.
        // @Author Todo
        // @Description
        // Todo
        // @Tags
        // Todo
        // @Usage
        // Todo
        // -->
        registerCoreMember(PlaySoundCommand.class,
                "PLAYSOUND", "playsound [<location>] [sound:<name>] (volume:<#.#>) (pitch:<#.#>)", 2);


        // <--[command]
        // @Name Permission
        // @Usage permission [add/remove] [permission] (player:<name>) (group:<name>) (world:<name>)
        // @Required 2
        // @Stable unstable
        // @Short Gives or takes a permission node to/from the player or group.
        // @Author Todo
        // @Description
        // Todo
        // @Tags
        // <p@player.has_permission[permission.node]>
        // <p@player.has_permission[permission.node].global>
        // <p@player.has_permission[permission.node].world>
        // @Usage
        // Todo
        // -->
        registerCoreMember(PermissionCommand.class,
                "PERMISSION", "permission [add/remove] [permission] (player:<name>) (group:<name>) (world:<name>)", 2);


        // <--[command]
        // @Name Pose
        // @Usage pose (player/npc) [id:<name>]
        // @Required 1
        // @Stable unstable
        // @Short Rotates the player or NPC to match a pose.
        // @Author aufdemrand
        // @Description
        // Todo
        // @Tags
        // Todo
        // @Usage
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
        // @Author David Cernat, mcmonkey
        // @Description
        // Pushes entities through the air in a straight line at a certain speed and for a certain duration, triggering a script when they hit an obstacle or stop flying.
        // @Tags
        // Todo
        // @Usage
        // Use to launch an arrow straight towards a target
        // - push arrow destination:<player.location>
        //
        // @Usage
        // Use to launch an entity into the air
        // - push cow
        //
        // -->
        registerCoreMember(PushCommand.class,
                "PUSH", "push [<entity>|...] (origin:<entity>/<location>) (destination:<location>) (speed:<#.#>) (<duration>) (<script>)", 1);


        // <--[command]
        // @Name Queue
        // @Usage queue (queue:<id>) [clear/pause/resume/delay:<#>]
        // @Required 1
        // @Stable stable
        // @Short Modifies the current state of a script queue.
        // @Author Todo
        // @Description
        // Todo
        // @Tags
        // Todo
        // @Usage
        // Todo
        // -->
        registerCoreMember(QueueCommand.class,
                "QUEUE", "queue (queue:<id>) [clear/pause/resume/delay:<#>]", 1);


        // <--[command]
        // @Name Random
        // @Usage random [<#>/{braced commands}]
        // @Required 1
        // @Stable stable
        // @Short Selects a random choice from the following script commands.
        // @Author aufdemrand, morphan1
        // @Description
        // Todo

        // @Tags
        // None

        // @Usage
        // Use to choose randomly from the following commands
        // - random 3
        // - narrate "hi"
        // - narrate "hello"
        // - narrate "hey"

        // @Usage
        // Use to choose randomly from a braced set of commands
        // - random {
        //   - narrate "hi"
        //   - narrate "hello"
        //   - narrate "hey"
        //   }

        // -->
        registerCoreMember(RandomCommand.class,
                "RANDOM", "random [<#>/{braced commands}]", 1);


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
        // <e@entity.is_spawned>
        // @Usage
        // Todo
        // -->
        registerCoreMember(RemoveCommand.class,
                "REMOVE", "remove [<entity>|...] (<world>) (region:<name>)", 0);


        // <--[command]
        // @Name Rename
        // @Usage rename [<npc>] [<name>]
        // @Required 1
        // @Stable Todo
        // @Short Renames an NPC.
        // @Author Todo
        // @Description
        // Todo
        // @Tags
        // <n@npc.name>
        // <n@npc.name.nickname>
        // @Usage
        // Todo
        // -->
        registerCoreMember(RenameCommand.class,
                "RENAME", "rename [<npc>] [<name>]", 1);


        // <--[command]
        // @Name Repeat
        // @Usage repeat [<amount>] [<commands>]
        // @Required 1
        // @Stable stable
        // @Short Runs a series of braced commands several times.
        // @Author morphan1, mcmonkey

        // @Description
        // Loops through a series of braced commands a specified number of times.
        // To get the number of loops so far, you can use %value%.

        // @Tags
        // %value% to get the number of loops so far

        // @Usage
        // Use loop through a command several times
        // - repeat 5 {
        //     - announce "Announce Number %value%"
        //   }
        // -->
        registerCoreMember(RepeatCommand.class,
                "REPEAT", "repeat [<amount>] [<commands>]", 1);


        // <--[command]
        // @Name Reset
        // @Usage reset [fails/finishes/cooldown] (script:<name>)
        // @Required 1
        // @Stable unstable
        // @Short Resets a script's fails, finishes, or cooldowns.
        // @Author Todo
        // @Description
        // Todo
        // @Tags
        // Todo
        // @Usage
        // Todo
        // -->
        registerCoreMember(ResetCommand.class,
                "RESET", "reset [fails/finishes/cooldown] (script:<name>)", 1);


        // <--[command]
        // @Name Rotate
        // @Usage rotate (<entity>|...) (yaw:<value>) (pitch:<value>) (duration:<duration>) (frequency:<duration>)
        // @Required 1
        // @Stable stable
        // @Short Rotates a list of entities.
        // @Author David Cernat
        // @Description
        // Todo
        // @Tags
        // <e@entity.location.yaw>
        // <e@entity.location.pitch>
        // @Usage
        // Todo
        // -->
        registerCoreMember(RotateCommand.class,
                "ROTATE", "rotate (cancel) (<entity>|...) (yaw:<value>) (pitch:<value>) (duration:<duration>) (infinite/frequency:<duration>)", 0);


        // <--[command]
        // @Name Run
        // @Usage run (locally) [<script>] (path:<name>) (as:<player>/<npc>) (def:<element>|...) (id:<name>) (instantly) (delay:<value>)
        // @Required 1
        // @Stable stable
        // @Short Runs a script in a new ScriptQueue.
        // @Author aufdemrand
        // @Description
        // Todo
        // @Tags
        // Todo
        // @Usage
        // Todo
        // -->
        registerCoreMember(RunCommand.class,
                "RUN", "run (locally) [<script>] (path:<name>) (as:<player>/<npc>) (def:<element>|...) (id:<name>) (instantly) (delay:<value>)", 1);


        // <--[command]
        // @Name RunTask
        // @Deprecated This has been replaced by the 'run' and 'inject' commands.
        // @Usage runtask [<name>] (instantly) (queue(:<name>)) (delay:<#>) (define:<element>|...)
        // @Required 1
        // @Stable unstable
        // @Short Runs a task script.
        // @Author aufdemrand
        // @Description
        // Todo
        // @Tags
        // Todo
        // @Usage
        // Todo
        // -->
        registerCoreMember(RuntaskCommand.class,
                "RUNTASK", "runtask [<name>] (instantly) (queue(:<name>)) (delay:<#>) (define:<element>|...)", 1);


        // <--[command]
        // @Name Scoreboard
        // @Usage scoreboard [set/remove/show/hide] [<name>] [value:<name>] (priority:<#>)
        // @Required 1
        // @Stable unstable
        // @Short Modifies a scoreboard (Not going to work much until Minecraft 1.7)
        // @Author aufdemrand
        // @Description
        // Todo
        // @Tags
        // Todo
        // @Usage
        // Todo
        // -->
        registerCoreMember(ScoreboardCommand.class,
                "SCOREBOARD", "scoreboard [set/remove/show/hide] [<name>] [value:<name>] (priority:<#>)", 1);


        /**
         * <b>dScript Usage:</b><br>
         * <pre>Scribe [SCRIPT:book_script] (GIVE|{DROP}|EQUIP) (LOCATION:x,y,z,world) (ITEM:ITEMSTACK.name)</pre>
         *
         * <ol><tt>Arguments: [] - Required () - Optional  {} - Default</ol></tt>
         *
         * <ol><tt>[SCRIPT:book_script]</tt><br>
         *         The name of the 'Book Script'. See below for format.</ol>
         *
         * <ol><tt>[GIVE|{DROP}|EQUIP]</tt><br>
         *         What to do with the book after it is written. If not specified, it will default
         *         to dropping the book near the NPC. Note: When using BOOK with an 'ITEMSTACK.name',
         *         no default action is set allowing other commands to modify the book.</ol>
         *
         * <ol><tt>(LOCATION:x,y,z,world)</tt><br>
         *         When using DROP, a location may be specified. Default location, if unspecified,
         *         is the attached NPC.</ol>
         *
         * <ol><tt>(ITEM:ITEMSTACK.name)</tt><br>
         *         Allows the use of a specific BOOK created with a 'saved ITEMSTACK' from the NEW
         *         command. If not specified, a new book will be used.</ol>
         *
         *
         * <br><b>Sample Book Script:</b><br>
         * <ol><pre>
         * "Cosmos Book":<br>
         *   Type: Book<br>
         *   Title: Cosmos, a Personal Voyage<br>
         *   Author: Carl Sagan<br>
         *   Text:<br>
         *   - Every one of us is, in the cosmic perspective, precious. If a human disagrees with<br>
         *     you, let him live. In a hundred billion galaxies, you will not find another<br>
         *   - The nitrogen in our DNA, the calcium in our teeth, the iron in our blood, the <br>
         *     carbon in our apple pies were made in the interiors of collapsing stars. We are <br>
         *     made of starstuff.<br>
         * </pre></ol>
         *
         * <p>Note: ScribeCommand also implements a replaceable tag for &#60;P>, which creates a new
         * paragraph in a written book's text.</p>
         *
         * <br><b>Example Usage:</b><br>
         * <ol><tt>
         *  - SCRIBE SCRIPT:Cosmos DROP<br>
         *  - SCRIBE ITEM:ITEMSTACK.ImportantBook 'SCRIPT:Spellbook of Haste'<br>
         * </ol></tt>
         *
         * <br><b>Extended Usage:</b><br>
         * <ol><tt>
         *  Script: <br>
         *  - ENGAGE NOW DURATION:10 <br>
         *  - LOOKCLOSE TOGGLE:TRUE DURATION:10 <br>
         *  - CHAT 'Use this book with care, as it is very powerful and could cause great harm<br>
         *    if put into the wrong hands!' <br>
         *  - WAIT 2 <br>
         *  - ^ANIMATE ANIMATION:ARM_SWING <br>
         *  - ^NEW ITEMSTACK ITEM:book ID:&#60;PLAYER.NAME>s_enchanted_spellbook<br>
         *  - ^SCRIBE ITEM:ITEMSTACK.&#60;PLAYER.NAME>s_enchanted_spellbook SCRIPT:silk_touch_description <br>
         *  - ^ENCHANT ITEM:ITEMSTACK.&#60;PLAYER.NAME>s_enchanted_spellbook ENCHANTMENT:SILKTOUCH<br>
         *  - ^LORE ADD ITEM:ITEMSTACK.&#60;PLAYER.NAME>s_enchanted_spellbook 'A spell of Silk-touch, level 1'<br>
         *  - DROP ITEM:ITEMSTACK.&#60;PLAYER.NAME>s_enchanted_spellbook<br>
         *  - NARRATE '&#60;NPC.NAME> drops an old book.' <br>
         * </ol></tt>
         *
         */
        // TODO: Combine the above outdated information with the new meta tags below

        // <--[command]
        // @Name Scribe
        // @Usage scribe [<script>] (<item>/give/equip/{drop <location>})
        // @Required 1
        // @Stable Todo
        // @Short Writes information to a book from a book-type script or a book item.
        // @Author Jeebiss, aufdemrand
        // @Description
        // Todo
        // @Tags
        // <i@item.book.author>
        // <i@item.book.title>
        // <i@item.book.page_count>
        // <i@item.book.get_page[<#>]>
        // <i@item.book.pages>
        // @Usage
        // Todo
        // -->
        registerCoreMember(ScribeCommand.class,
                "SCRIBE", "scribe [<script>] (<item>/give/equip/{drop <location>})", 1);


        // <--[command]
        // @Name Shoot
        // @Usage shoot [<entity>|...] (origin:<entity>/<location>) (destination:<location>) (height:<#.#>) (gravity:<#.#>) (script:<name>)
        // @Required 1
        // @Stable stable
        // @Short Shoots an entity through the air up to a certain height.
        // @Author David Cernat
        // @Description
        // Shoots an entity through the air up to a certain height, optionally using a custom gravity value and triggering a script on impact with a surface.
        // @Tags
        // Todo
        // @Usage
        // Todo
        // -->
        registerCoreMember(ShootCommand.class,
                "SHOOT", "shoot [<entity>|...] (origin:<entity>/<location>) (destination:<location>) (height:<#.#>) (gravity:<#.#>) (script:<name>)", 1);


        // <--[command]
        // @Name ShowFake
        // @Usage showfake [<material>] [<location>|...] (d:<duration>{10s})
        // @Required 2
        // @Stable stable
        // @Short Makes the player see a block change that didn't actually happen.
        // @Author aufdemrand
        // @Description
        // Todo
        // @Tags
        // Todo
        // @Usage
        // Todo
        // -->
        registerCoreMember(ShowFakeCommand.class,
                "SHOWFAKE", "showfake [<material>] [<location>|...] (d:<duration>{10s})", 2);


        // <--[command]
        // @Name Sign
        // @Usage sign (type:{sign_post}/wall_sign) ["<line>|..."] [<location>] (direction:n/e/w/s)
        // @Required 1
        // @Stable stable
        // @Short Modifies a sign.
        // @Author David Cernat
        // @Description
        // Todo
        // @Tags
        // Todo
        // @Usage
        // Todo
        // -->
        registerCoreMember(SignCommand.class,
                "SIGN", "sign (type:{sign_post}/wall_sign) [\"<line>|...\"] [<location>] (direction:n/e/w/s)", 1);


        // <--[command]
        // @Name Sit
        // @Usage sit (<location>)
        // @Required 0
        // @Stable unstable
        // @Short Causes the NPC to sit. (Does not currently work!)
        // @Author Todo
        // @Description
        // Todo
        // @Tags
        // <l@location.block.sign_contents>
        // @Usage
        // Todo
        // -->
        registerCoreMember(SitCommand.class,
                "SIT", "sit (<location>)", 0);


        // <--[command]
        // @Name Spawn
        // @Usage spawn [<entity>|...] (<location>) (target:<entity>) (persistent)
        // @Required 1
        // @Stable stable
        // @Short Spawns a list of entities at a certain location.
        // @Author David Cernat
        // @Description
        // Todo
        // @Tags
        // <e@entity.is_spawned>
        // <util.entity_is_spawned[<entity>]>
        // @Usage
        // Todo
        // -->
        registerCoreMember(SpawnCommand.class,
                "SPAWN", "spawn [<entity>|...] (<location>) (target:<entity>) (persistent)", 1);


        // <--[command]
        // @Name Stand
        // @Usage stand
        // @Required 0
        // @Stable unstable
        // @Short Causes the NPC to stand. (Does not currently work!)
        // @Author Todo
        // @Description
        // Todo
        // @Tags
        // Todo
        // @Usage
        // Todo
        // -->
        registerCoreMember(StandCommand.class,
                "STAND", "stand", 0);


        // <--[command]
        // @Name Strike
        // @Usage strike (no_damage) [<location>]
        // @Required 1
        // @Stable stable
        // @Short Strikes lightning down upon the location.
        // @Author Todo
        // @Description
        // Todo
        // @Tags
        // Todo
        // @Usage
        // Todo
        // -->
        registerCoreMember(StrikeCommand.class,
                "STRIKE", "strike (no_damage) [<location>]", 1);

        // <--[command]
        // @Name Switch
        // @Usage switch [<location>] (state:[{toggle}/on/off]) (duration:<value>)
        // @Required 1
        // @Stable stable
        // @Short Switches a lever.
        // @Author aufdemrand, Jeebiss, David Cernat
        // @Description
        // Todo
        // @Tags
        // Todo
        // @Usage
        // Todo
        // -->
        registerCoreMember(SwitchCommand.class,
                "SWITCH", "switch [<location>] (state:[{toggle}/on/off]) (duration:<value>)", 1);

        // <--[command]
        // @Name Take
        // @Usage take [money/iteminhand/<item>|...] (qty:<#>) (from:<inventory>)
        // @Required 1
        // @Stable stable
        // @Short Takes an item from the player.
        // @Author Todo
        // @Description
        // Todo
        // @Tags
        // <p@player.item_in_hand>
        // <p@player.money>
        // @Usage
        // Todo
        // -->
        registerCoreMember(TakeCommand.class,
                "TAKE", "take [money/iteminhand/<item>|...] (qty:<#>) (from:<inventory>)", 1);

        // <--[command]
        // @Name Teleport
        // @Usage teleport (<entity>|...) [<location>]
        // @Required 1
        // @Stable stable
        // @Short Teleports the player or NPC to a new location.
        // @Author David Cernat, aufdemrand
        // @Description
        // Todo
        // @Tags
        // <e@entity.location>
        // @Usage
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
        // <w@world.time>
        // <w@world.time.period>
        // @Usage
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
        // <n@npc.has_trait[<trait>]>
        // <n@npc.list_traits>
        // @Usage
        // Todo
        // -->
        registerCoreMember(TraitCommand.class,
                "TRAIT", "trait (state:true/false/{toggle}) [<trait>]", 1);


        // <--[command]
        // @Name Trigger
        // @Usage trigger [name:chat/click/damage/proximity] (state:true/false) (cooldown:<#.#>) (radius:<#>)
        // @Required 2
        // @Stable stable
        // @Short Enables or disables a trigger.
        // @Author aufdemrand
        // @Description
        // Todo
        // @Tags
        // Todo
        // @Usage
        // Todo
        // -->
        registerCoreMember(TriggerCommand.class,
                "TRIGGER", "trigger [name:chat/click/damage/proximity] (state:true/false) (cooldown:<#.#>) (radius:<#>)", 2);

        // <--[command]
        // @Name Viewer
        // @Usage viewer ({create <location>}/modify/remove) [id:<name>] (type:{sign_post}/wall_sign) (display:{location}/score/logged_in) (direction:n/e/w/s)
        // @Required 1
        // @Stable unstable
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
        // -->
        registerCoreMember(ViewerCommand.class,
                "VIEWER", "viewer ({create <location>}/modify/remove) [id:<name>] (type:{sign_post}/wall_sign) (display:{location}/score/logged_in) (direction:n/e/w/s)", 2);

        // <--[command]
        // @Name Vulnerable
        // @Usage vulnerable (state:{true}/false/toggle)
        // @Required 0
        // @Stable unstable
        // @Short Sets whether an NPC is vulnerable.
        // @Author aufdemrand
        // @Description
        // Todo
        // @Tags
        // Todo
        // @Usage
        // Todo
        // -->
        registerCoreMember(VulnerableCommand.class,
                "VULNERABLE", "vulnerable (state:{true}/false/toggle)", 0);

        // <--[command]
        // @Name Wait
        // @Usage wait (<duration>) (queue:<name>)
        // @Required 0
        // @Stable stable
        // @Short Delays a script for a specified amount of time.
        // @Author aufdemrand
        // @Description
        // Todo
        // @Tags
        // Todo
        // @Usage
        // Todo
        // -->
        registerCoreMember(WaitCommand.class,
                "WAIT", "wait (<duration>) (queue:<name>)", 0);

        // <--[command]
        // @Name Walk, WalkTo
        // @Usage walk [<location>] (speed:<#>) (auto_range)
        // @Required 1
        // @Stable stable
        // @Short Causes the NPC to walk to another location.
        // @Author aufdemrand
        // @Description
        // Todo
        // @Tags
        // <npc.navigator. * >
        // @Usage
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
        // <yaml[<idname>].contains[<path>]>
        // <yaml[<idname>].read[<path>]>
        // <yaml[<idname>].list_keys[<path>]>
        // @Usage
        // Todo
        // -->
        registerCoreMember(YamlCommand.class,
                "YAML", "yaml [load/create/savefile:<file>]/[write:<key>]/[write:<key> value:<value>] [id:<name>]", 1);

        // <--[command]
        // @Name Zap
        // @Usage zap (<script>) [<step>] (<duration>)
        // @Required 0
        // @Stable stable
        // @Short Changes the current script step.
        // @Author aufdemrand
        // @Description
        // Todo
        // @Tags
        // Todo
        // @Usage
        // Todo
        // -->
        registerCoreMember(ZapCommand.class,
                "ZAP", "zap (<script>) [<step>] (<duration>)", 0);

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
