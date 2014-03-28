package net.aufdemrand.denizen.scripts.commands;

import java.util.HashMap;
import java.util.Map;

import net.aufdemrand.denizen.Denizen;
import net.aufdemrand.denizen.interfaces.RegistrationableInstance;
import net.aufdemrand.denizen.interfaces.dRegistry;
import net.aufdemrand.denizen.scripts.commands.core.*;
import net.aufdemrand.denizen.scripts.commands.entity.*;
import net.aufdemrand.denizen.scripts.commands.item.*;
import net.aufdemrand.denizen.scripts.commands.npc.*;
import net.aufdemrand.denizen.scripts.commands.player.*;
import net.aufdemrand.denizen.scripts.commands.server.*;
import net.aufdemrand.denizen.scripts.commands.world.*;
import net.aufdemrand.denizen.utilities.debugging.dB;
import net.aufdemrand.denizen.utilities.depends.Depends;

public class CommandRegistry implements dRegistry {

    public Denizen denizen;

    public CommandRegistry(Denizen denizen) {
        this.denizen = denizen;
    }

    private final Map<String, AbstractCommand> instances = new HashMap<String, AbstractCommand>();
    private final Map<Class<? extends AbstractCommand>, String> classes = new HashMap<Class<? extends AbstractCommand>, String>();

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
        return instances.get(commandName.toUpperCase());
    }

    @Override
    public <T extends RegistrationableInstance> T get(Class<T> clazz) {
        String command = classes.get(clazz);
        if (command != null) return clazz.cast(instances.get(command));
        else return null;
    }

    // <--[language]
    // @Name Command Syntax
    // @group Script Command System
    // @Description
    // Almost every Denizen command and requirement has arguments after the command itself.
    // These arguments are just snippets of text showing what exactly the command should do,
    // like what the chat command should say, or where the look command should point.
    // But how do you know what to put in the arguments?
    //
    // You merely need to look at the command's usage/syntax info.
    // Let's take for example:
    // <code>
    // - animatechest [<location>] ({open}/close) (sound:{true}/false)
    // </code>
    // Obviously, the command is 'animatechest'... but what does the rest of it mean?
    //
    // Anything in [brackets] is required... you MUST put it there.
    // Anything in (parenthesis) is optional... you only need to put it there if you want to.
    // Anything in {braces} is default... the command will just assume this if no argument is actually typed.
    // Anything in <> is non-literal... you must change what is inside of it.
    // Anything outside of <> is literal... you must put it exactly as-is.
    // <#> represents a number without a decimal, and <#.#> represents a number with a decimal
    // Lastly, input that ends with "|..." (EG, [<entity>|...] ) can take a list of the input indicated before it (In that example, a list of entities)
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
        // @Name Adjust
        // @Syntax adjust [<dObject>|...] [<mechanism>](:<value>)
        // @Required 2
        // @Stable stable
        // @Short Adjusts a dObjects mechanism.
        // @Author aufdemrand
        // @Group core

        // @Description
        // Many dObjects contains options and properties that need to be adjusted. Denizen employs a mechanism
        // interface to deal with those adjustments. To easily accomplish this, use this command with a valid object
        // mechanism, and sometimes accompanying value.

        // @Tags
        // <entry[saveName].result> returns the adjusted object.
        // <entry[saveName].result_list> returns a dList of adjusted objects.

        // @Usage
        // Use to set a custom display name on an entity.
        // - adjust e@1000 'set_custom_name:ANGRY!'

        // -->
        registerCoreMember(AdjustCommand.class,
                "ADJUST", "adjust [<dObject>|...] [<mechanism>](:<value>)", 2);

        // <--[command]
        // @Name Age
        // @Syntax age [<entity>|...] (adult/baby/<age>) (lock)
        // @Required 1
        // @Stable stable
        // @Short Sets the ages of a list of entities, optionally locking them in those ages.
        // @Author David Cernat
        // @Group entity

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

        // @Syntax anchor [id:<name>] [assume/remove/add <location>/walkto/walknear (r:#)]
        // @Required 2
        // @Stable stable
        // @Short Controls a NPC's Anchor Trait.
        // @Author aufdemrand
        // @Group npc

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
        // @Syntax animate [<entity>|...] [animation:<name>]
        // @Required 2
        // @Stable stable
        // @Short Makes a list of entities perform a certain animation.
        // @Author David Cernat
        // @Group entity

        // @Description
        // Minecraft implements several player and entity animations which the animate command can use, just
        // specify an entity and an animation.

        // Player animations require a Player-type entity or NPC. Available player animations include:
        // ARM_SWING, CRIT, HURT, and MAGIC_CRIT, SIT, SLEEP, SNEAK, STOP_SITTING, STOP_SLEEPING, STOP_SNEAKING

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
                "ANIMATE", "animate [<entity>|...] [animation:<name>]", 2);


        // <--[command]
        // @Name AnimateChest
        // @Syntax animatechest [<location>] ({open}/close) (sound:{true}/false) (<player>|...)
        // @Required 1
        // @Stable unstable
        // @Short Makes a chest appear to open or close.
        // @Author Jeebiss, mcmonkey
        // @Group world
        // @Description
        // Todo
        // @Tags
        // Todo
        // @Usage
        // Todo
        // -->
        registerCoreMember(AnimateChestCommand.class,
                "ANIMATECHEST", "animatechest [<location>] ({open}/close) (sound:{true}/false) (<player>|...)", 1);


        // <--[command]
        // @Name Announce
        // @Syntax announce ["<text>"] (to_ops/to_console/to_flagged:<flag_name>) (format:<name>)
        // @Required 1
        // @Stable stable
        // @Short Announces a message for everyone online to read.
        // @Author aufdemrand
        // @Group server

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
        // @Syntax assignment [{set}/remove] (script:<name>)
        // @Required 1
        // @Stable unstable
        // @Short Changes an NPC's assignment.
        // @Author aufdemrand
        // @Group npc
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
        // @Syntax attack (<entity>|...) (target:<entity>/cancel)
        // @Required 0
        // @Stable stable
        // @Short Makes an entity, or list of entities, attack a target.
        // @Author David Cernat
        // @Group entity

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
        // @Syntax break [<location>] (entity:<entity>) (radius:<#.#>)
        // @Required 1
        // @Stable unstable
        // @Short Makes the NPC walk over and break a block.
        // @Author aufdemrand
        // @Group world
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
        // @Syntax burn [<entity>|...] (duration:<value>)
        // @Required 1
        // @Stable stable
        // @Short Sets a list of entities on fire.
        // @Author David Cernat
        // @Group entity
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
        // @Syntax cast [<effect>] (remove) (duration:<value>) (power:<#>) (<entity>|...)
        // @Required 1
        // @Stable Stable
        // @Short Casts a potion effect to a list of entities.
        // @Author aufdemrand, Jeebiss, Morphan1
        // @Group entity

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

        // TODO: Should the chat command be in the NPC group instead?
        // <--[command]
        // @Name Chat
        // @Syntax chat ["<text>"] (no_target/targets:<entity>|...) (talkers:<npc>|...)
        // @Required 1
        // @Stable stable
        // @Short Causes a NPC/NPCs to send a chat message to nearby players.
        // @Author aufdemrand
        // @Group player

        // @Description
        // Chat uses a NPCs SpeechController provided by Citizens2, typically inside 'interact' or 'task'
        // script-containers. Typically there is already player and NPC context inside a queue that is using
        // the 'chat' command. In this case, only a text string is required. Alternatively, target entities
        // can be specified to have the NPC chat to a different target/targets, or specify 'no_target' to
        // not send the message to any specific player.
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
        // If sending messages to the Player without any surrounding entities hearing the message is desirable,
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
                "CHAT", "chat [\"<text>\"] (no_target/targets:<entity>|...)", 1);


        // <--[command]
        // @Name ChunkLoad
        // @Syntax chunkload ({add}/remove/removeall) [<location>] (duration:<value>)
        // @Required 1
        // @Stable stable
        // @Short Keeps a chunk actively loaded and allowing NPC activity.
        // @Author spaceemotion, mcmonkey
        // @Group world
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
        // @Syntax compass [<location>]
        // @Required 1
        // @Stable stable
        // @Short Redirects the player's compass to target the given location.
        // @Author mcmonkey
        // @Group player
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
        // @Syntax cooldown [<duration>] (global) (s:<script>)
        // @Required 1
        // @Stable stable
        // @Short Temporarily disables a script-container from meeting requirements.
        // @Author aufdemrand
        // @Group core

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
        // @Syntax copyblock [location:<location>] [to:<location>]
        // @Required 1
        // @Stable unstable
        // @Short Copies a block to another location, keeping all metadata.
        // @Author aufdemrand, David Cernat
        // @Group world
        // @Description
        // Todo
        // @Tags
        // <l@location.material>
        // @Usage
        // Todo
        // -->
        registerCoreMember(CopyBlockCommand.class,
                "COPYBLOCK", "copyblock [location:<location>] [to:<location>]", 1);


        // <--[command]
        // @Name Create
        // @Syntax create [<entity>] [<name>] (<location>) (traits:<trait>|...)
        // @Required 1
        // @Stable experimental
        // @Short Creates a new NPC, and optionally spawns it at a location.
        // @Author aufdemrand
        // @Group npc
        // @Description
        // Todo
        // @Tags
        // <server.list_npcs>
        // <entry[saveName].created_npc> returns the NPC that was created.
        // @Usage
        // Use to create a despawned NPC for later usage.
        // - create player Bob
        // @Usage
        // Use to create an NPC and spawn it immediately.
        // - create spider Joe <player.location>
        // -->
        registerCoreMember(CreateCommand.class,
                "CREATE", "create [<entity>] [<name>] (<location>)", 1);


        // <--[command]
        // @Name CreateWorld
        // @Syntax createworld [<name>] (g:<generator>) (worldtype:<type>)
        // @Required 1
        // @Stable unstable
        // @Short Creates a new world
        // @Author aufdemrand, mcmonkey
        // @Group world
        // @Description
        // Todo
        // @Tags
        // <server.list_worlds>
        // @Usage
        // Todo
        // -->
        registerCoreMember(CreateWorldCommand.class,
                "CREATEWORLD", "createworld [<name>] (g:<generator>) (worldtype:<type>)", 1);


        // <--[command]
        // @Name Define
        // @Syntax define [<id>] [<value>]
        // @Required 2
        // @Stable stable
        // @Short Creates a temporary variable inside a script queue.
        // @Author aufdemrand
        // @Group core

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
        // - define blocks '<player.flag[noticeable_blocks]>'
        // - narrate '[NOTICE] You have noticed <player.location.find.blocks[%blocks%].within[%range%].size>
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
        // @Name Despawn
        // @Syntax Despawn (<npc>|...)
        // @Required 0
        // @Stable stable
        // @Short Temporarily despawns the linked NPC or a list of NPCs.
        // @Author mcmonkey
        // @Group npc

        // @Description
        // This command will temporarily despawn either the linked NPC or
        // a list of other NPCs. Despawning means they are no longer visible
        // or interactable, but they still exist and can be respawned.

        // @Tags
        // <n@npc.is_spawned>

        // @Usage
        // Use to despawn the linked NPC.
        // - despawn

        // @Usage
        // Use to despawn several NPCs.
        // - despawn <npc>|<player.selected_npc>|n@32
        // -->
        registerCoreMember(DespawnCommand.class,
                "DESPAWN", "despawn (<npc>)", 0);


        // <--[command]
        // @Name Determine
        // @Syntax determine (passively) [<value>]
        // @Required 1
        // @Stable stable
        // @Short Sets the outcome of an event.
        // @Author aufdemrand
        // @Group core
        // @Description
        // Todo
        // @Tags
        // Todo
        // @Usage
        // Todo
        // -->
        registerCoreMember(DetermineCommand.class,
                "DETERMINE", "determine (passively) [<value>]", 1);


        // <--[command]
        // @Name Disengage
        // @Syntax disengage (npc:<npc>)
        // @Required 0
        // @Stable stable
        // @Short Enables a NPCs triggers that have been temporarily disabled by the engage command.
        // @Author aufdemrand
        // @Group npc

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
        // See <@link command Engage>
        //
        // @Tags
        // <n@npc.is_engaged>
        //
        // @Usage
        // Use to reenable a NPC's triggers, disabled via 'engage'.
        // - engage
        // - chat 'Be right there!'
        // - walkto <player.location>
        // - wait 5s
        // - disengage
        //
        // -->
        registerCoreMember(DisengageCommand.class,
                "DISENGAGE", "disengage (npc:<npc>)", 0);


        // <--[command]
        // @Name DisplayItem
        // @Syntax displayitem [<item>] [<location>] (duration:<value>)
        // @Required 2
        // @Stable unstable
        // @Short Makes a non-touchable item spawn for players to view.
        // @Author aufdemrand, mcmonkey
        // @Group item
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
        // @Syntax drop [<item>/<entity_type>/xp] [<location>] (qty:<#>) (speed:<#.#>)
        // @Required 1
        // @Stable stable
        // @Short Drops an item, entity, or experience orb on a location.
        // @Author aufdemrand
        // @Group world

        // @Description
        // To drop an item, just specify a valid item object. To drop
        // an entity, specify a generic entity object. Drop can also reward players
        // with experience orbs by using the 'xp' argument.
        //
        // For all three usages, you can optionally specify an integer with 'qty:'
        // prefix to drop multiple items/entities/xp.
        // For items, you can add 'speed:' to modify the launch velocity.

        // @Tags
        // None

        // @Usage
        // Use to drop some loot around the player.
        // - drop i@gold_nugget <cu@<player.location.add[-2,-2,-2]>|<player.location.add[2,2.2]>.get_spawnable_blocks.random>

        // @Usage
        // Use to reward a player
        // - drop xp qty:500 <player.location>

        // @Usage
        // Use to drop a nasty surprise (exploding TNT)
        // - drop e@primed_tnt <player.location>

        // -->
        registerCoreMember(DropCommand.class,
                "DROP", "drop [<item>/<entity_type>/xp] [<location>] (qty:<#>) (speed:<#.#>)", 1);


        // <--[command]
        // @Name Engage
        // @Syntax engage (<duration>) (npc:<npc>)
        // @Required 0
        // @Stable stable
        // @Short Temporarily disables a NPCs toggled interact script-container triggers.
        // @Author aufdemrand
        // @Group npc
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
        // See <@link command Disengage>
        //
        // @Tags
        // <n@npc.is_engaged>
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
        // @Name Equip
        // @Syntax equip (<entity>|...) (hand:<item>) (head:<item>) (chest:<item>) (legs:<item>) (boots:<item>)
        // @Required 1
        // @Stable stable
        // @Short Equips items and armor on a list of entities.
        // @Author David Cernat
        // @Group entity
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
        // @Name Event
        // @Syntax event [<event name>|...] (context:<name>|<object>|...)
        // @Required 1
        // @Stable unstable
        // @Short Manually fires a world event.
        // @Author mcmonkey
        // @Group core
        // @Description
        // This command will trigger a world event (an event within a 'world' type script) exactly the same
        // as if an actual serverside event had caused it.
        // You can specify as many event names as you want in the list, they will all be fired. It will also automatically
        // fire a duplicate of each event name with object identifiers (eg 'i@', see <@link language dobject>) removed.
        // The script's linked player and NPC will automatically be sent through to the event.
        // To add context information (tags like <context.location>) to the event, simplify specify all context values in a list.
        // Note that there are some inherent limitations... EG, you can't directly add a list to the context currently.
        // To do this, the best way is to just escape the list value (see <@link language property escaping>).
        // @Tags
        // <server.has_event[<event_name>]>
        // <server.get_event_handlers[<event_name>]>
        // <entry[saveName].determination> returns the determined value (if any) from the event.
        // @Usage
        // Use to trigger a custom event
        // - event "player triggers custom event"
        // @Usage
        // Use to trigger multiple custom events with context
        // - event "player triggers custom event|player causes event" context:event|custom|npc|<player.selected_npc>
        // -->
        registerCoreMember(EventCommand.class,
                "EVENT", "event [<event name>|...] (context:<name>|<object>|...)", 1);


        // <--[command]
        // @Name Execute
        // @Syntax execute [as_player/as_op/as_npc/as_server] [<Bukkit command>]
        // @Required 2
        // @Stable stable
        // @Short Executes an arbitrary server command as if the player, NPC, or server typed it in.
        // @Author aufdemrand
        // @Group server
        // @Description
        // Todo
        // @Tags
        // <entry[saveName].output> returns the output to an as_server sender.
        // @Usage
        // Todo
        // -->
        registerCoreMember(ExecuteCommand.class,
                "EXECUTE", "execute [as_player/as_op/as_npc/as_server] [<Bukkit command>]", 2);


        // <--[command]
        // @Name Experience
        // @Syntax experience [{set}/give/take] (level) [<#>]
        // @Required 2
        // @Stable Todo
        // @Short Gives or takes experience points to the player.
        // @Author aufdemrand
        // @Group player
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
        // @Syntax explode (power:<#.#>) (<location>) (fire) (breakblocks)
        // @Required 0
        // @Stable stable
        // @Short Causes an explosion at the location.
        // @Author Alain Blanquet
        // @Group world
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
        // @Syntax fail (script:<name>)
        // @Required 0
        // @Stable stable
        // @Short Marks a script as having failed.
        // @Author aufdemrand
        // @Group core
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
        // @Syntax feed (amt:<#>) (target:<entity>|...)
        // @Required 0
        // @Stable unstable
        // @Short Refills the player's food bar.
        // @Author aufdemrand, Jeebiss
        // @Group entity
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
        // @Syntax finish (script:<name>)
        // @Required 0
        // @Stable stable
        // @Short Marks a script as having been completed successfully.
        // @Author aufdemrand
        // @Group core
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
        // @Syntax firework (<location>) (power:<#>) (<type>/random) (primary:<color>|...) (fade:<color>|...) (flicker) (trail)
        // @Required 0
        // @Stable stable
        // @Short Launches a firework with specific coloring
        // @Author David Cernat
        // @Group world
        // @Description
        // Todo
        // @Tags
        // Todo
        // @Usage
        // Todo
        // -->
        registerCoreMember(FireworkCommand.class,
                "FIREWORK", "firework (<location>) (power:<#>) (<type>/random) (primary:<color>|...) (fade:<color>|...) (flicker) (trail)", 0);


        // <--[command]
        // @Name Fish
        // @Syntax fish (catchfish) (stop) (<location>) (catchpercent:<#>)
        // @Required 1
        // @Stable Todo
        // @Short Causes an NPC to begin fishing
        // @Author Jeebiss
        // @Group npc
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
        // @Syntax flag ({player}/npc/global) [<name>([<#>])](:<action>)[:<value>] (duration:<value>)
        // @Required 1
        // @Stable stable
        // @Short Sets or modifies a flag on the player, NPC, or server.
        // @Author aufdemrand
        // @Group core
        // @Description
        // Todo
        // @Tags
        // <p@player.flag[<flag>]>
        // <n@npc.flag[<flag>]>
        // <global.flag[<flag>]>
        // @Usage
        // @Usage
        // Use to create a flag named 'playstyle' on the player with 'agressive' as the value.
        // - flag player playstyle:agressive
        // @Usage
        // Use to create a flag on the npc with its current location as the value.
        // - flag npc location:<npc.location>
        // @Usage
        // Use to increase the context player flag 'damage_dealt' with the context damage as amount.
        // - flag <context.damager> damage_dealt:+:<context.damage>
        // @Usage
        // Use to add p@TheBlackCoyote to the server flag called 'cool_people' as a new value without removing existing values.
        // - flag server cool_people:->:p@TheBlackCoyote
        // @Usage
        // Use to add both p@mcmonkey4eva and p@morphan1 as individual new values to the server flag 'cool_people'.
        // - flag server cool_people:|:p@mcmonkey4eva|p@morphan1
        // @Usage
        // Use to remove p@morphan1 from the server flag 'cool_people'.
        // - flag server cool_people:<-:p@morphan1
        // @Usage
        // Use to completely remove a flag.
        // - flag server cool_people:!
        // -->
        registerCoreMember(FlagCommand.class,
                "FLAG", "flag ({player}/npc/global) [<name>([<#>])](:<action>)[:<value>] (duration:<value>)", 1);


        // <--[command]
        // @Name Fly
        // @Syntax fly (cancel) [<entity>|...] (controller:<player>) (origin:<location>) (destinations:<location>|...) (speed:<#.#>) (rotationthreshold:<#.#>)
        // @Required 1
        // @Stable stable
        // @Short Make an entity fly where its controller is looking or fly to waypoints.
        // @Author David Cernat
        // @Group entity
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
        // @Syntax follow (stop) (lead:<#.#>) (target:<entity>)
        // @Required 0
        // @Stable unstable
        // @Short Causes the NPC to follow a target (Currently experiencing bugs with lead: )
        // @Author aufdemrand
        // @Group npc
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
        // @Syntax foreach [<object>|...] [<commands>]
        // @Required 2
        // @Stable stable
        // @Short Loops through a dList, running a set of commands for each item.
        // @Author Morphan1, mcmonkey
        // @Group core

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
        // @Syntax give [money/xp/<item>|...] (qty:<#>) (engrave) (to:<inventory>) (slot:<#>)
        // @Required 1
        // @Stable stable
        // @Short Gives the player an item, xp, or money.
        // @Author Jeebiss
        // @Group item
        // @Description
        // Todo
        // @Tags
        // <p@player.money>
        // @Usage
        // Use to give money to the player.
        // - give money qty:10
        // @Usage
        // Use to give XP to the player.
        // - give xp qty:10
        // @Usage
        // Use to give an item to the player.
        // - give i@iron_sword qty:1
        // -->
        registerCoreMember(GiveCommand.class,
                "GIVE", "give [money/xp/<item>|...] (qty:<#>) (engrave) (to:<inventory>) (slot:<#>)", 1);


        // <--[command]
        // @Name Group
        // @Syntax group [add/remove] [<group>] (world:<name>)
        // @Required 2
        // @Stable Todo
        // @Short Adds a player to or removes a player from a permissions group.
        // @Author GnomeffinWay
        // @Group player
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
        // @Syntax head (<entity>|...) [skin:<player>]
        // @Required 1
        // @Stable stable
        // @Short Makes players or NPCs wear a specific player's head.
        // @Author David Cernat
        // @Group entity
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
        // @Syntax heal (<#.#>) ({player}/<entity>|...)
        // @Required 0
        // @Stable stable
        // @Short Heals the player or list of entities.
        // @Author aufdemrand, Jeebiss, Morphan1, mcmonkey
        // @Group entity
        // @Description
        // Todo
        // @Tags
        // <e@entity.health>
        // @Usage
        // Todo
        // -->
        registerCoreMember(HealCommand.class,
                "HEAL", "heal (<#.#>) ({player}/<entity>|...)", 0);


        // <--[command]
        // @Name Health
        // @Syntax health ({npc}/<entity>|...) [<#>] (state:{true}/false/toggle)
        // @Required 1
        // @Stable stable
        // @Short Changes the target's maximum health.
        // @Author mcmonkey
        // @Group entity
        // @Description
        // Use this command to modify an entity's maximum health. If the target is an NPC,
        // you can use the 'state' argument to enable, disable, or toggle the Health trait
        // (which is used to track the NPC's health, and handle actions such as 'on death')
        // the Health trait will be enabled by default.
        // By default, this command will target the linked NPC but can be set to target any
        // other living entity, such as a player or mob.
        // Additionally, you may input a list of entities, each one will calculate the effects
        // explained above.
        // @Tags
        // <e@entity.health>
        // <e@entity.health.max>
        // <n@npc.has_trait[health]>
        // @Usage
        // Use to set the NPC's maximum health to 50.
        // - health 50
        // @Usage
        // Use to disable tracking of health value on the NPC.
        // - health state:false
        // @Usage
        // Use to change a player's health limit to 50.
        // - health <player> 50
        // @Usage
        // Use to change a list of entities' health limits all to 50.
        // - health <player.location.find.living_entities.within[10]> 50
        // -->
        registerCoreMember(HealthCommand.class,
                "HEALTH", "health ({npc}/<entity>|...) [<#>] (state:{true}/false/toggle)", 1);


        // <--[command]
        // @Name Hurt
        // @Syntax hurt (<#.#>) ({player}/<entity>|...)
        // @Required 0
        // @Stable stable
        // @Short Hurts the player or a list of entities.
        // @Author aufdemrand, Jeebiss, morphan1, mcmonkey
        // @Group entity
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
        // @Syntax if [<value>] (!)(<operator> <value>) (&&/|| ...) [<commands>] (else <commands>)
        // @Required 2
        // @Stable stable
        // @Short Compares values, and runs one script if they match, or a different script if they don't match.
        // @Author aufdemrand, David Cernat
        // @Group core
        // @Description
        // Todo
        // @Tags
        // <el@element.is[<operator>].to[<element>]>
        // @Usage
        // Todo
        // -->
        registerCoreMember(IfCommand.class,
                "IF", "if [<value>] (!)(<operator> <value>) (&&/|| ...) [<commands>] (else <commands>)", 2);


        // <--[command]
        // @Name Inventory
        // @Syntax inventory [open/close/copy/move/swap/add/remove/set/keep/exclude/fill/clear/update] (destination:<inventory>) (origin:<inventory>/<item>|...) (slot:<#>)
        // @Required 1
        // @Stable stable
        // @Short Edits the inventory of a player, NPC, or chest.
        // @Author David Cernat, Morphan1
        // @Group item
        // @Description
        // Use this command to edit the state of inventories. By default, the destination inventory
        // is the current attached player's inventory. If you are copying, swapping, removing from
        // (including via "keep" and "exclude"), adding to, moving, or filling inventories, you'll need
        // both destination and origin inventories. Origin inventories may be specified as a list of
        // dItems, but destinations must be actual dInventories.
        // Using "open", "clear", or "update" only require a destination. "Update" also requires the
        // destination to be a valid player inventory.
        // Using "close" closes any inventory that the currently attached player has opened.
        // @Tags
        // <p@player.inventory>
        // <n@npc.inventory>
        // <l@location.inventory>
        // @Usage
        // Use to open a chest inventory, at a location.
        // - inventory open d:l@123,123,123,world
        // @Usage
        // Use to open another player's inventory.
        // - inventory open d:<p@calico-kid.inventory>
        // @Usage
        // Use to remove all items from a chest, except any items in
        // the specified list.
        // - inventory keep d:in@location[l@123,123,123,world] o:li@i@snow_ball|i@ItemScript
        // @Usage
        // Use to remove items specified in a chest from the current
        // player's inventory, regardless of the item count.
        // - inventory exclude origin:l@123,123,123,world
        // @Usage
        // Use to swap two players' inventories.
        // - inventory swap d:in@player[p@mcmonkey4eva] o:<p@fullwall.inventory>
        // -->
        registerCoreMember(InventoryCommand.class,
                "INVENTORY", "inventory [open/close/copy/move/swap/add/remove/set/keep/exclude/fill/clear/update] (destination:<inventory>) (origin:<inventory>/<item>|...) (slot:<#>)", 1);


        // <--[command]
        // @Name Inject
        // @Syntax inject (locally) [<script>] (path:<name>) (instantly)
        // @Required 1
        // @Stable stable
        // @Short Runs a script in the current ScriptQueue.
        // @Author aufdemrand
        // @Group core
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
        // @Syntax invisible [<entity>] (state:true/false/toggle)
        // @Required 1
        // @Stable unstable
        // @Short Makes an NPC or entity go invisible (Note: use '/npc playerlist' to make it work on NPCs!)
        // @Author aufdemrand, mcmonkey
        // @Group entity
        // @Description
        // Todo
        // @Tags
        // Todo
        // @Usage
        // Todo
        // -->
        registerCoreMember(InvisibleCommand.class,
                "INVISIBLE", "invisible [<entity>] (state:true/false/toggle)", 1);


        // <--[command]
        // @Name Leash
        // @Syntax leash (cancel) [<entity>|...] (holder:<entity>/<location>)
        // @Required 1
        // @Stable stable
        // @Short Sticks a leash on target entity, held by a fence post or another entity.
        // @Author Alain Blanquet, mcmonkey
        // @Group entity
        // @Description
        // Todo
        // @Tags
        // <e@entity.is_leashed>
        // <e@entity.get_leash_holder>
        // @Usage
        // Use to attach hold an entity in hand.
        // - leash <npc> holder:<player>
        // @Usage
        // Use to attach an entity to a fence post.
        // - leash <npc> holder:<player.location.cursor_on>
        // @Usage
        // Use to release an entity.
        // - leash cancel <npc>
        // -->
        registerCoreMember(LeashCommand.class,
                "LEASH", "leash (cancel) [<entity>|...] (holder:<entity>/<location>)", 1);


        // <--[command]
        // @Name Listen
        // @Syntax listen ({new}/cancel/finish) [kill/block/item/itemdrop/travel] [<requirements>] [script:<name>] (id:<name>)
        // @Required 2
        // @Stable unstable
        // @Short Listens for the player achieving various actions and runs a script when they are completed.
        // @Author aufdemrand, Jeebiss
        // @Group player

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
        // - listen block type:break block:iron_ore qty:1 script:IronMined

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
        // @Syntax log [<text>] (type:{info}/severe/warning/fine/finer/finest/none) [file:<name>]
        // @Required 2
        // @Stable stable
        // @Short Logs some debugging info to a file.
        // @Author SpaceEmotion, mcmonkey
        // @Group core
        // @Description
        // This is a quick and simple way to store debugging information for admins to read.
        // You just input a file name and some text, and it will store that information in the file
        // with a date/time stamp and the chosen type ('INFO' by default). If you don't want the
        // date/time stamp and type, you can set the type to 'none' and it will only add the
        // message text.
        // Regardless of type, each usage of the log command will add a new line to the file, you can't
        // just keep adding to one line.
        // You might choose to use this to record some important things, for example, every time a player
        // uses a dangerous command you might log the player's name and their location, so you'll know
        // who to blame if you find something damaged.
        // Remember that the file location is inside the server's primary folder. You might want to prefix
        // file names with a folder name, EG: 'file:logs/security.log'
        // @Tags
        // None.
        // @Usage
        // Use to log some information to a file.
        // - log "Security breach on level 3!" type:severe file:securitylog.txt
        // @Usage
        // Use to log a player's name and location when they did something dangerous.
        // - log "<player.name> used the '/EXPLODE' command at <player.location>!" type:warning file:security.log
        // @Usage
        // Use to write information directly to a file.
        // - log "This won't have a date or type" type:none file:example.log
        // -->
        registerCoreMember(LogCommand.class,
                "LOG", "log [<text>] (type:{info}/severe/warning/fine/finer/finest/none) [file:<name>]", 2);


        // <--[command]
        // @Name Look
        // @Syntax look (<entity>|...) [<location>] (duration:<duration>)
        // @Required 1
        // @Stable stable
        // @Short Causes the NPC or other entity to look at a target location.
        // @Author aufdemrand, mcmonkey
        // @Group entity
        // @Description
        // Todo
        // @Tags
        // <l@location.yaw>
        // <l@location.pitch>
        // @Usage
        // Use to point an npc towards a spot.
        // - look <npc> <player.location>
        // @Usage
        // Use to force a player to stare at a spot for some time.
        // - look <player> <npc.location> duration:10s
        // -->
        registerCoreMember(LookCommand.class,
                "LOOK", "look (<entity>|...) [<location>] (duration:<duration>)", 1);


        // <--[command]
        // @Name LookClose
        // @Syntax lookclose lookclose (<npc>) (state:<true/false>) (range:<#>) (realistic)
        // @Required 0
        // @Stable stable
        // @Short Interacts with a NPCs 'lookclose' trait as provided by Citizens2.
        // @Author aufdemrand
        // @Group npc

        // @Description
        // Use this command with any NPC to alter the state and options of its 'lookclose'
        // trait. When a NPC's 'lookclose' trait is toggled to true, the NPC's head will
        // follow nearby players. Specifying realistic will enable a higher precision
        // and detection of players, while taking into account 'line-of-sight', however can
        // use more CPU cycles. You may also specify a range integer to specify the number
        // of blocks that will trigger the NPC's attention.

        // @Usage
        // Use to cause the NPC to begin looking at nearby players.
        // - lookclose true <npc>

        // @Usage
        // Use to cause the NPC to stop looking at nearby players.
        // - lookclose false <npc>

        // @Usage
        // Change up the range and make the NPC more realistic
        // - lookclose true range:10 realistic
        // -->
        registerCoreMember(LookcloseCommand.class,
                "LOOKCLOSE", "lookclose (<npc>) (state:<true/false>) (range:<#>) (realistic)", 0);


        // <--[command]
        // @Name Midi
        // @Syntax midi (cancel) [<file>] (<location>/<entity>|...) (tempo:<#.#>)
        // @Required 1
        // @Stable stable
        // @Short Plays a midi file at a given location or to a list of players using note block sounds.
        // @Author David Cernat
        // @Group world

        // @Description
        // This will fully load a midi song file stored in the '../plugins/Denizen/midi/' folder. The file
        // must be a valid midi file with the extension '.mid'. It will continuously play the song as
        // noteblock songs at the given location or group of players until the song ends. If no location or
        // entitiy is specified, by default this will play for the attached player.
        //
        // Also, an example Midi song file has been included: "Denizen" by Black Coyote. He made it just for us!
        // Check out more of his amazing work at: http://www.youtube.com/user/BlaCoyProductions

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
        // @Syntax mount (cancel) [<entity>|...] (<location>)
        // @Required 0
        // @Stable stable
        // @Short Mounts one entity onto another.
        // @Author David Cernat
        // @Group entity
        // @Description
        // Todo
        // @Tags
        // <e@entity.get_vehicle>
        // <e@entity.is_inside_vehicle>
        // @Usage
        // Use to mount an NPC on top of a player.
        // - mount <npc>|<player>
        // @Usage
        // Use to spawn a mutant pile of mobs.
        // - mount cow|pig|sheep|chicken
        // @Usage
        // Use to place a diamond block above a player's head.
        // - mount falling_block,diamond_block|<player>
        // @Usage
        // Use to force an entity in a vehicle.
        // - mount <player>|boat
        // -->
        registerCoreMember(MountCommand.class,
                "MOUNT", "mount (cancel) [<entity>|...] (<location>)", 0);


        // <--[command]
        // @Name ModifyBlock
        // @Syntax modifyblock [<location>|...] [<material>] (radius:<#>) (height:<#>) (depth:<#>) (no_physics)
        // @Required 2
        // @Stable stable
        // @Short Modifies blocks.
        // @Author Jeebiss, aufdemrand
        // @Group world
        // @Description
        // Changes blocks in the world based on the criteria given. Specifying no radius/height/depth will result
        // in only the specified blocks being changed. Use 'no_physics' to place the blocks without
        // physics taking over the modified blocks. This is useful for block types such as portals. This does NOT
        // control physics for an extended period of time.
        // @Tags
        // <l@location.block.material>
        // @Usage
        // Todo
        // -->
        registerCoreMember(ModifyBlockCommand.class,
                "MODIFYBLOCK", "modifyblock [<location>] [<material>] (radius:<#>) (height:<#>) (depth:<#>) (no_physics)", 2);


        // <--[command]
        // @Name Nameplate
        // @Syntax nameplate [<chatcolor>] [set:<text>] (target:<player>)  +--> Requires ProtocolLib
        // @Required 1
        // @Stable unstable
        // @Short Changes something's nameplate, requires ProtocolLib (Unstable and broken!)
        // @Author spaceemotion
        // @Group entity
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
        // @Syntax narrate ["<text>"] (targets:<player>|...) (format:<name>)
        // @Required 1
        // @Stable stable
        // @Short Shows some text to the player.
        // @Author aufdemrand
        // @Group player
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
        // @Syntax note [<Notable dObject>] [as:<name>]
        // @Required 2
        // @Stable unstable
        // @Short Adds a new notable object.
        // @Author aufdemrand
        // @Group core
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
        // @Syntax oxygen (type:maximum/remaining) (mode:set/add/remove) [qty:<#>]
        // @Required 1
        // @Stable unstable
        // @Short Gives or takes breath from the player.
        // @Author David Cernat
        // @Group player
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
        // @Name Pause, Resume
        // @Syntax pause [waypoints/activity] (<duration>)
        // @Required 1
        // @Stable unstable
        // @Short Pauses/resumes an NPC's waypoint navigation or goal activity temporarily or permanently.
        // @Author aufdemrand
        // @Group npc
        // @Description
        // Todo
        // @Tags
        // <n@npc.navigator>
        // @Usage
        // Use to pause an NPC's waypoint navigation permanently.
        // @Usage
        // - pause waypoints
        // Use to pause an NPC's goal activity temporarily.
        // - pause activity 1m
        // @Usage
        // Use to pause an NPC's waypoint navigation and then resume it.
        // - pause navigation
        // - resume navigation
        // -->
        registerCoreMember(PauseCommand.class,
                "PAUSE, RESUME", "pause [waypoints/activity] (<duration>)", 1);


        // <--[command]
        // @Name PlayEffect
        // @Syntax playeffect [<location>|...] [effect:<name>] (data:<#.#>) (visibility:<#.#>) (qty:<#>) (offset:<#.#>) (targets:<player>|...)
        // @Required 2
        // @Stable stable
        // @Short Plays a visible or audible effect at the location.
        // @Author David Cernat
        // @Group world
        // @Description
        // Todo
        // See <@link language Particle Effects> for a list of valid effect names.
        // @Tags
        // Todo
        // @Usage
        // Todo
        // -->
        registerCoreMember(PlayEffectCommand.class,
                "PLAYEFFECT", "playeffect [<location>|...] [effect:<name>] (data:<#.#>) (visibility:<#.#>) (qty:<#>) (offset:<#.#>) (targets:<player>|...)", 2);


        // <--[command]
        // @Name PlaySound
        // @Syntax playsound [<location>|.../<player>|...] [sound:<name>] (volume:<#.#>) (pitch:<#.#>) (custom)
        // @Required 2
        // @Stable stable
        // @Short Plays a sound at the location or to a list of players.
        // @Author Jeebiss
        // @Group world
        // @Description
        // TODO
        // Optionally, specify 'custom' to play a custom sound added by a resource pack, changing the sound string to something like 'random.click'
        // @Tags
        // Todo
        // @Usage
        // Todo
        // -->
        registerCoreMember(PlaySoundCommand.class,
                "PLAYSOUND", "playsound [<location>|.../<player>|...] [sound:<name>] (volume:<#.#>) (pitch:<#.#>) (custom)", 2);


        // <--[command]
        // @Name Permission
        // @Syntax permission [add/remove] [permission] (player:<name>) (group:<name>) (world:<name>)
        // @Required 2
        // @Stable unstable
        // @Short Gives or takes a permission node to/from the player or group.
        // @Author GnomeffinWay
        // @Group player
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
        // @Syntax pose (add/remove/{assume}) [id:<name>] (player/{npc}) (<location>)
        // @Required 1
        // @Stable stable
        // @Short Rotates the player or NPC to match a pose, or adds/removes an NPC's poses.
        // @Author aufdemrand
        // @Group npc
        //
        // @Description
        // Makes a player or NPC assume the position of a pose saved on an NPC, removes a
        // pose with a specified ID from the current linked NPC, or adds a pose to the NPC
        // with an ID and a location, although the only thing that matters in the location
        // is the pitch and yaw.
        //
        // @Tags
        // <n@npc.has_pose[<name>]>
        // <n@npc.get_pose[<name>]>
        //
        // @Usage
        // Make an NPC assume a pose.
        // - pose id:MyPose1
        //
        // @Usage
        // Add a pose to an NPC. (Note that only the last 2 numbers matter)
        // - pose add id:MyPose2 l@0,0,0,-2.3,5.4,world
        //
        // @Usage
        // Remove a pose from an NPC.
        // - pose remove id:MyPose1
        // -->
        registerCoreMember(PoseCommand.class,
                "POSE", "pose (add/remove/{assume}) [id:<name>] (player/{npc}) (<location>)", 1);


        // <--[command]
        // @Name Push
        // @Syntax push [<entity>|...] (origin:<entity>/<location>) (destination:<location>) (speed:<#.#>) (duration:<duration>) (<script>)
        // @Required 1
        // @Stable stable
        // @Short Pushes entities through the air in a straight line.
        // @Author David Cernat, mcmonkey
        // @Group entity
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
        // @Syntax queue (queue:<id>) [clear/stop/pause/resume/delay:<#>]
        // @Required 1
        // @Stable stable
        // @Short Modifies the current state of a script queue.
        // @Author aufdemrand
        // @Group core
        // @Description
        // Todo
        // @Tags
        // <queue.id>
        // <queue.size>
        // <queue.exists[queue_id]>
        // @Usage
        // Todo
        // -->
        registerCoreMember(QueueCommand.class,
                "QUEUE", "queue (queue:<id>) [clear/stop/pause/resume/delay:<#>]", 1);


        // <--[command]
        // @Name Random
        // @Syntax random [<#>/{braced commands}]
        // @Required 1
        // @Stable stable
        // @Short Selects a random choice from the following script commands.
        // @Author aufdemrand, morphan1
        // @Group core
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
        // @Syntax remove [<entity>|...] (region:<name>)
        // @Required 1
        // @Stable Todo
        // @Short Despawns a list of entities, fully removing any NPCs.
        // @Author David Cernat
        // @Group entity
        // @Description
        // Todo
        // @Tags
        // <e@entity.is_spawned>
        // @Usage
        // Todo
        // -->
        registerCoreMember(RemoveCommand.class,
                "REMOVE", "remove [<entity>|...] (<world>) (region:<name>)", 1);


        // <--[command]
        // @Name Rename
        // @Syntax rename [<name>]
        // @Required 1
        // @Stable unstable
        // @Short Renames the linked NPC.
        // @Author aufdemrand
        // @Group npc
        // @Description
        // Todo
        // @Tags
        // <n@npc.name>
        // <n@npc.name.nickname>
        // @Usage
        // Use to rename the linked NPC.
        // - rename Bob
        // @Usage
        // Use to rename a different NPC.
        // - rename Bob npc:n@32
        // -->
        registerCoreMember(RenameCommand.class,
                "RENAME", "rename [<name>]", 1);


        // <--[command]
        // @Name Repeat
        // @Syntax repeat [<amount>] [<commands>]
        // @Required 1
        // @Stable stable
        // @Short Runs a series of braced commands several times.
        // @Author morphan1, mcmonkey
        // @Group core

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
        // @Syntax reset (<player>) [fails/finishes/cooldown/saves/global_cooldown] (<script>)
        // @Required 1
        // @Stable stable
        // @Short Resets various parts of Denizen's saves.yml, including script finishes/fails, a script's fails, finishes, or cooldowns.
        // @Author aufdemrand
        // @Group core
        // @Description
        // Todo
        // @Tags
        // Todo
        // @Usage
        // Todo
        // -->
        registerCoreMember(ResetCommand.class,
                "RESET", "reset (<player>) [fails/finishes/cooldown/saves/global_cooldown] (<script>)", 1);


        // <--[command]
        // @Name Rotate
        // @Syntax rotate (<entity>|...) (yaw:<value>) (pitch:<value>) (duration:<duration>) (frequency:<duration>)
        // @Required 1
        // @Stable stable
        // @Short Rotates a list of entities.
        // @Author David Cernat
        // @Group entity
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
        // @Syntax run (locally) [<script>] (path:<name>) (as:<player>/<npc>) (def:<element>|...) (id:<name>) (instantly) (delay:<value>)
        // @Required 1
        // @Stable stable
        // @Short Runs a script in a new ScriptQueue.
        // @Author aufdemrand
        // @Group core
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
        // @Syntax runtask [<name>] (instantly) (queue(:<name>)) (delay:<#>) (define:<element>|...)
        // @Required 1
        // @Stable unstable
        // @Short Runs a task script.
        // @Author aufdemrand
        // @Group core
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
        // @Name Schematic
        // @Syntax schematic [create/load/unload/rotate/paste/save] [name:<name>] (angle:<#>) (<location>) (<cuboid>) (noair)
        // @Required 2
        // @Stable unstable
        // @Short Creates, loads, pastes, and saves WorldEdit schematics.
        // @Author mcmonkey
        // @Group world
        // @Description
        // Todo
        // @Tags
        // <schematic[<name>].height>
        // <schematic[<name>].length>
        // <schematic[<name>].width>
        // <schematic[<name>].block[<location>]>
        // <schematic[<name>].origin>
        // <schematic[<name>].offset>
        // <schematic[<name>].blocks>
        // @Usage
        // Use to create a new schematic from a cuboid and an origin location
        // - schematic create name:MySchematic cu@<player.location.sub[5,5,5]>|<player.location.add[5,5,5]> <player.location>
        //
        // @Usage
        // Use to load a schematic
        // - schematic load name:MySchematic
        //
        // @Usage
        // Use to unload a schematic
        // - schematic unload name:MySchematic
        //
        // @Usage
        // Use to rotate a loaded schematic
        // - schematic rotate name:MySchematic angle:90
        //
        // @Usage
        // Use to paste a loaded schematic
        // - schematic paste name:MySchematic <player.location> noair
        //
        // @Usage
        // Use to save a created schematic
        // - schematic save name:MySchematic
        // -->
        if (Depends.worldEdit != null) // Temporary work-around...
        registerCoreMember(SchematicCommand.class,
                "SCHEMATIC", "schematic [create/load/unload/rotate/paste/save] [name:<name>] (angle:<#>) (<location>) (<cuboid>) (noair)", 2);

        // <--[command]
        // @Name Scoreboard
        // @Syntax scoreboard ({add}/remove) (viewers:<player>|...) (lines:<player>/<text>|...) (id:<value>/{main}) (objective:<value>) (criteria:<criteria>/{dummy}) (score:<#>) (displayslot:<value>/{sidebar}/none)
        // @Required 1
        // @Stable stable
        // @Short Add or removes viewers, objectives and scores from scoreboards.
        // @Author David Cernat
        // @Group server
        // @Description
        // Lets you make players see a certain scoreboard and then a certain objective in that scoreboard.
        //
        // There are currently three slots where objectives can be displayed: in the sidebar on the right of
        // the screen, below player names and in the player list that shows up when you press Tab. The names
        // of these slots can be found here:
        // http://jd.bukkit.org/rb/apidocs/org/bukkit/scoreboard/DisplaySlot.html
        //
        // Every objective has several lines of scores. Technically, the lines track players, but fake player
        // names can be used by Denizen to let you call the lines anything you want.
        //
        // When using the sidebar as the display slot, all the scores set for an objective will be displayed
        // there, but you will need to put actual player names in the lines to be able to use the below_name
        // display slot (which displays each player's score underneath his/her name) and the player_list
        // display slot (which displays each player's score to the right of his/her name in the player list).
        //
        // If you do not specify a display slot, the sidebar will be used. You can also use "none" as the
        // display slot if you want to add a hidden objective without automatically making it get displayed.
        //
        // You can set scores manually, or you can use different Minecraft criteria that set and update the
        // scores automatically. A list of these criteria can be found here:
        // http://minecraft.gamepedia.com/Scoreboard#Objectives
        //
        // You can use the "remove" argument to remove different parts of scoreboards. The more arguments
        // you use with it, the more specific your removal will be. For example, if you only use the "remove"
        // argument and the "id" argument, you will completely remove all the objectives in a scoreboard,
        // but if you specify an objective as well, you will only delete that one objective from that
        // scoreboard, and if you also specify certain lines, you will only delete those specific lines from
        // that objective. Similarly, if you use the "remove" argument along with the "id" and "viewers"
        // arguments, you will only remove those viewers from the scoreboard, not the entire scoreboard.
        //
        // @Tags
        // Todo
        // @Usage
        // Add a score for the player "mythan" to the default scoreboard under the objective "cookies" and let him see it
        // - scoreboard add obj:cookies lines:mythan score:1000 viewers:p@mythan
        //
        // @Usage
        // Add a new current objective called "food" to the "test" scoreboard with 3 lines that each have a score of 50:
        // - scoreboard add id:test obj:food lines:Cookies|Donuts|Cake score:50
        //
        // @Usage
        // Make a list of players see the scoreboard that has the id "test":
        // - scoreboard add viewers:p@Goma|p@mythan|p@Ares513 id:test
        //
        // @Usage
        // Change the value of one of the scores in the "food" objective:
        // - scoreboard add id:test obj:food lines:Cake score:9000
        //
        // @Usage
        // Remove one of the lines from the "food" objective in the "test" scoreboard
        // - scoreboard remove obj:food lines:Donuts
        //
        // @Usage
        // Remove one of the viewers of the "test" scoreboard:
        // - scoreboard remove viewers:p@mythan
        //
        // @Usage
        // Make the player "dimensionZ" see the health of other players below their names
        // - scoreboard add viewers:p@dimensionZ id:test obj:anything criteria:health displayslot:below_name
        //
        // @Usage
        // Make all the players on the world "survival" see each other's number of entity kills in the player list when pressing Tab
        // - scoreboard add "viewers:<w@survival.players>" id:test obj:anything criteria:totalKillCount displayslot:player_list
        // -->
        registerCoreMember(ScoreboardCommand.class,
                "SCOREBOARD", "scoreboard ({add}/remove) (viewers:<player>|...) (lines:<player>/<text>|...) (id:<value>/{main}) (objective:<value>) (criteria:<criteria>/{dummy}) (score:<#>) (displayslot:<value>/{sidebar}/none)", 1);


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
        // @Syntax scribe [<script>] (<item>/give/equip/{drop <location>})
        // @Required 1
        // @Stable Todo
        // @Short Writes information to a book from a book-type script or a book item.
        // @Author Jeebiss, aufdemrand
        // @Group item
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
        // @Syntax shoot [<entity>|...] (origin:<entity>/<location>) (destination:<location>) (height:<#.#>) (gravity:<#.#>) (speed:<#.#>) (script:<name>) (shooter:<entity>) (spread:<#.#>)
        // @Required 1
        // @Stable stable
        // @Short Shoots an entity through the air up to a certain height.
        // @Author David Cernat, mcmonkey
        // @Group entity
        // @Description
        // Shoots an entity through the air up to a certain height, optionally using a custom gravity value and triggering a script on impact with a target.
        // If the origin is not an entity, specify a shooter so the damage handling code knows how to assume shot the projectile.
        // Normally, a list of entities will spawn mounted on top of each other. To have them instead fire separately and spread out,
        // specify the 'spread' argument with a decimal number indicating how wide to spread the entities.
        // In the script ran when the arrow lands, the following definitions will be available:
        // %shot_entities% for all shot entities, %last_entity% for the last one (The controlling entity),
        // %location% for the last known location of the last shot entity, and
        // %hit_entities% for a list of any entities that were hit by fired projectiles.
        // @Tags
        // <entry[saveName].shot_entities> returns a dList of entities that were shot.
        // @Usage
        // Use to shoot an arrow from the NPC to perfectly hit the player.
        // - shoot arrow origin:<npc> destination:<player.location>
        // @Usage
        // Use to shoot an arrow out of the player with a given speed.
        // - shoot arrow origin:<player> speed:2
        // -->
        registerCoreMember(ShootCommand.class,
                "SHOOT", "shoot [<entity>|...] (origin:<entity>/<location>) (destination:<location>) (height:<#.#>) (gravity:<#.#>) (speed:<#.#>) (script:<name>) (shooter:<entity>) (spread:<#.#>)", 1);


        // <--[command]
        // @Name ShowFake
        // @Syntax showfake [<material>] [<location>|...] (to:<player>|...) (d:<duration>{10s})
        // @Required 2
        // @Stable stable
        // @Short Makes the player see a block change that didn't actually happen.
        // @Author aufdemrand
        // @Group player
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
        // @Syntax sign (type:{sign_post}/wall_sign) ["<line>|..."] [<location>] (direction:n/e/w/s)
        // @Required 1
        // @Stable stable
        // @Short Modifies a sign.
        // @Author David Cernat
        // @Group world
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
        // @Syntax sit (<location>)
        // @Required 0
        // @Stable unstable
        // @Short Causes the NPC to sit. (Does not currently work!)
        // @Author Jeebiss
        // @Group npc
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
        // @Syntax spawn [<entity>|...] [<location>] (target:<entity>) (persistent)
        // @Required 2
        // @Stable stable
        // @Short Spawns a list of entities at a certain location.
        // @Author David Cernat
        // @Group entity
        // @Description
        // Todo
        // @Tags
        // <e@entity.is_spawned>
        // <util.entity_is_spawned[<entity>]>
        // <entry[saveName].spawned_entities> returns a list of entities that were spawned.
        // @Usage
        // Todo
        // -->
        registerCoreMember(SpawnCommand.class,
                "SPAWN", "spawn [<entity>|...] [<location>] (target:<entity>) (persistent)", 2);


        // <--[command]
        // @Name Stand
        // @Syntax stand
        // @Required 0
        // @Stable unstable
        // @Short Causes the NPC to stand. (Does not currently work!)
        // @Author Jeebiss
        // @Group npc
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
        // @Syntax strike (no_damage) [<location>]
        // @Required 1
        // @Stable stable
        // @Short Strikes lightning down upon the location.
        // @Author aufdemrand
        // @Group world
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
        // @Syntax switch [<location>] (state:[{toggle}/on/off]) (duration:<value>)
        // @Required 1
        // @Stable stable
        // @Short Switches a lever.
        // @Author aufdemrand, Jeebiss, David Cernat
        // @Group world
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
        // @Syntax take [money/iteminhand/bydisplay:<name>/slot:<#>/<item>|...] (qty:<#>) (from:<inventory>)
        // @Required 1
        // @Stable stable
        // @Short Takes an item from the player.
        // @Author Jeebiss
        // @Group item
        // @Description
        // Todo
        // @Tags
        // <p@player.item_in_hand>
        // <p@player.money>
        // @Usage
        // Todo
        // -->
        registerCoreMember(TakeCommand.class,
                "TAKE", "take [money/iteminhand/bydisplay:<name>/slot:<#>/<item>|...] (qty:<#>) (from:<inventory>)", 1);

        // <--[command]
        // @Name Teleport
        // @Syntax teleport (<entity>|...) [<location>]
        // @Required 1
        // @Stable stable
        // @Short Teleports the player or NPC to a new location.
        // @Author David Cernat, aufdemrand
        // @Group entity
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
        // @Syntax time [type:{global}/player] [<value>] (world:<name>)
        // @Required 1
        // @Stable Todo
        // @Short Changes the current time in the minecraft world.
        // @Author David Cernat
        // @Group world
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
        // @Syntax trait (state:true/false/{toggle}) [<trait>]
        // @Required 1
        // @Stable Stable
        // @Short Adds or removes a trait from an NPC.
        // @Author Morphan1
        // @Group npc
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
        // @Syntax trigger [name:chat/click/damage/proximity] (state:{toggle}/true/false) (cooldown:<#.#>) (radius:<#>)
        // @Required 1
        // @Stable stable
        // @Short Enables or disables a trigger.
        // @Author aufdemrand
        // @Group npc
        // @Description
        // Todo
        // @Tags
        // Todo
        // @Usage
        // Todo
        // -->
        registerCoreMember(TriggerCommand.class,
                "TRIGGER", "trigger [name:chat/click/damage/proximity] (state:{toggle}/true/false) (cooldown:<#.#>) (radius:<#>)", 1);

        // <--[command]
        // @Name Viewer
        // @Syntax viewer ({create <location>}/modify/remove) [id:<name>] (type:{sign_post}/wall_sign) (display:{location}/score/logged_in) (direction:n/e/w/s)
        // @Required 1
        // @Stable unstable
        // @Short Creates a sign that auto-updates with information.
        // @Author Morphan1
        // @Group entity

        // @Description
        // Creates a sign that auto-updates with information about a player, including their location, score, and
        // whether they're logged in or not.

        // @Tags
        // None

        // @Usage
        // Create a sign that shows the location of a player on a wall
        // - viewer player:ThatGuy create 113,76,-302,world id:PlayerLoc1 type:wall_sign display:location

        // -->
        registerCoreMember(ViewerCommand.class,
                "VIEWER", "viewer ({create <location>}/modify/remove) [id:<name>] (type:{sign_post}/wall_sign) (display:{location}/score/logged_in) (direction:n/e/w/s)", 2);

        // <--[command]
        // @Name Vulnerable
        // @Syntax vulnerable (state:{true}/false/toggle)
        // @Required 0
        // @Stable unstable
        // @Short Sets whether an NPC is vulnerable.
        // @Author aufdemrand
        // @Group npc
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
        // @Syntax wait (<duration>) (queue:<name>)
        // @Required 0
        // @Stable stable
        // @Short Delays a script for a specified amount of time.
        // @Author aufdemrand
        // @Group core
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
        // @Syntax walk (<npc>|...) [<location>] (speed:<#.#>) (auto_range) (radius:<#.#>)
        // @Required 1
        // @Stable stable
        // @Short Causes an NPC or list of NPCs to walk to another location.
        // @Author aufdemrand
        // @Group npc
        // @Description
        // Todo
        // @Tags
        // <n@npc.navigator.is_navigating>
        // <n@npc.navigator.speed>
        // <n@npc.navigator.range>
        // <n@npc.navigator.target_location>
        // @Usage
        // Todo
        // -->
        registerCoreMember(WalkCommand.class,
                "WALK, WALKTO", "walk (<npc>|...) [<location>] (speed:<#>) (auto_range) (radius:<#.#>)", 1);

        // <--[command]
        // @Name Weather
        // @Syntax weather [type:{global}/player] [sunny/storm/thunder] (world:<name>)
        // @Required 1
        // @Stable Todo
        // @Short Changes the current weather in the minecraft world.
        // @Author David Cernat
        // @Group world
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
        // @Syntax yaml [create]/[load:<file>]/[unload]/[savefile:<file>]/[write:<key>]/[write:<key> value:<value> (split_list)]/[set <key>([<#>])(:<action>):<value>] [id:<name>]
        // @Required 2
        // @Stable Todo
        // @Short Edits a YAML configuration file.
        // @Author aufdemrand
        // @Group core
        // @Description
        // Todo
        // @Tags
        // <yaml[<idname>].contains[<path>]>
        // <yaml[<idname>].read[<path>]>
        // <yaml[<idname>].read[<path>].as_list>
        // <yaml[<idname>].list_keys[<path>]>
        // @Usage
        // Use to create a new YAML file
        // - yaml create id:myfile
        // @Usage
        // Use to load a YAML file from disk
        // - yaml load:myfile.yml id:myfile
        // @Usage
        // Use to write to a YAML file
        // - yaml write:my.key value:myvalue id:myfile
        // @Usage
        // Use to save a YAML file to disk
        // - yaml savefile:myfile.yml id:myfile
        // @Usage
        // Use to unload a YAML file from memory
        // - yaml unload id:myfile
        // @Usage
        // Use to modify a YAML file similarly to a flag
        // - yaml id:myfile set my.key:+:2
        // @Usage
        // Use to modify a YAML file similarly to a flag
        // - yaml id:myfile set my.key[2]:hello
        // -->
        registerCoreMember(YamlCommand.class,
                "YAML", "yaml [create]/[load:<file>]/[savefile:<file>]/[write:<key>]/[write:<key> value:<value> (split_list)] [id:<name>]", 2);

        // <--[command]
        // @Name Zap
        // @Syntax zap (<script>) [<step>] (<duration>)
        // @Required 0
        // @Stable stable
        // @Short Changes the current script step.
        // @Author aufdemrand
        // @Group core
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
                dB.echoError(e);
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
                dB.echoError(e);
            }
    }

}
