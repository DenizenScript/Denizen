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
import net.aufdemrand.denizencore.scripts.commands.CommandRegistry;

public class BukkitCommandRegistry extends CommandRegistry {

    @Override
    public void registerCoreMembers() {

        registerCoreCommands();

        // <--[command]
        // @Name Action
        // @Syntax action [<action name>|...] (<npc>|...) (context:<name>|<object>|...)
        // @Required 1
        // @Stable unstable
        // @Short Manually fires an NPC action.
        // @Author mcmonkey
        // @Group npc
        // @Description
        // This command will trigger an NPC action (an action within an 'assignment' type script attached to the NPC) exactly the same
        // as if an actual serverside event had caused it.
        // You can specify as many action names as you want in the list, they will all be fired.
        // You may also specify as many NPCs as you would like to run the action on, in a list.
        // If no NPCs are specified, the NPC linked to the script will be assumed.
        // The script's linked player and the specified NPC will automatically be sent through to the action.
        // To add context information (tags like <context.location>) to the action, simply specify all context values in a list.
        // Note that there are some inherent limitations... EG, you can't directly add a list to the context currently.
        // To do this, the best way is to just escape the list value (see <@link language property escaping>).
        // @Tags
        // TODO: Document Command Details
        // @Usage
        // Use to trigger a custom action
        // - action "custom action"
        // @Usage
        // Use to trigger multiple custom action with context on a different NPC
        // - action "player dances|target enemy" n@10 context:action|custom|target|<player.selected_npc>
        // -->
        registerCoreMember(ActionCommand.class,
                "ACTION", "action [<action name>|...] (<npc>|...) (context:<name>|<object>|...)", 1);


        // <--[command]
        // @Name ActionBar
        // @Syntax actionbar [<text>] (targets:<player>|...) (format:<name>)
        // @Required 1
        // @Stable stable
        // @Short Sends a message to a player's action bar.
        // @Author Fortifier42
        // @group player

        // @Description
        // Sends a message to the target's action bar area. If no target is specified it will default to the attached
        // player. Accepts the 'format:<name>' argument, which will reformat the text according to the specified
        // format script.

        // @Usage
        // Use to send a message to the player's action bar.
        // - actionbar "Hey there <player.name>!"

        // @Usage
        // Use to send a message to a list of players.
        // - actionbar "Hey, welcome to the server!" targets:p@Fortifier42|p@mcmonkey4eva|p@Morphan1

        // @Usage
        // Use to send a message to a list of players, with a formatted message.
        // - actionbar "Hey there!" targets:p@Fortifier42|p@mcmonkey4eva format:ServerChat
        // -->
        registerCoreMember(ActionBarCommand.class,
                "ACTIONBAR", "actionbar [<text>] (targets:<player>|...)", 1);


        // <--[command]
        // @Name Adjust
        // @Syntax adjust [<dObject>|...] [<mechanism>](:<value>)
        // @Required 2
        // @Stable stable
        // @Short Adjusts a dObjects mechanism.
        // @Author aufdemrand
        // @Group core
        // @Video /denizen/vids/Properties%20and%20Mechanisms

        // @Description
        // Many dObjects contains options and properties that need to be adjusted. Denizen employs a mechanism
        // interface to deal with those adjustments. To easily accomplish this, use this command with a valid object
        // mechanism, and sometimes accompanying value.

        // @Tags
        // <entry[saveName].result> returns the adjusted object.
        // <entry[saveName].result_list> returns a dList of adjusted objects.

        // @Usage
        // Use to set a custom display name on an entity.
        // - adjust e@1000 'custom_name:ANGRY!'
        // @Usage
        // Use as part of the steps to modify the item a player is holding
        // - adjust <player.item_in_hand> "lore:Advanced Item" save:myitem
        // - take iteminhand
        // - give <entry[myitem].result>

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
        // - chat "I have saved this location as <def[location_name]>.'
        // - anchor add <npc.location> "id:<def[location_name]>"

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
        //
        // Player animations require a Player-type entity or NPC. Available player animations include:
        // ARM_SWING, CRIT, HURT, and MAGIC_CRIT, SIT, SLEEP, SNEAK, STOP_SITTING, STOP_SLEEPING, STOP_SNEAKING,
        // START_USE_ITEM, STOP_USE_ITEM, EAT_FOOD
        //
        // All entities also have available Bukkit's entity effect list, which includes:
        // DEATH, FIREWORK_EXPLODE, HURT, IRON_GOLEM_ROSE, SHEEP_EAT, VILLAGER_ANGRY, VILLAGER_HAPPY
        // VILLAGER_HEART, WITCH_MAGIC, WOLF_HEARTS, WOLF_SHAKE, WOLF_SMOKE, ZOMBIE_TRANSFORM
        //
        // Note that the above list only applies where logical, EG 'WOLF_' animations only apply to wolves.
        // @Tags
        // None

        // @Usage
        // Use to make a player appear to get hurt.
        // - animate <player> animation:hurt

        // @Usage
        // Use to make a wolf NPC shake
        // - animate '<n@aufdemrand's wolf>' animation:wolf_shake

        // -->
        if (Depends.citizens != null) {
            registerCoreMember(AnimateCommand.class,
                    "ANIMATE", "animate [<entity>|...] [animation:<name>]", 2);
        }


        // <--[command]
        // @Name AnimateChest
        // @Syntax animatechest [<location>] ({open}/close) (sound:{true}/false) (<player>|...)
        // @Required 1
        // @Stable unstable
        // @Short Makes a chest appear to open or close.
        // @Author Jeebiss, mcmonkey
        // @Group world
        // @Description
        // This command animates a chest in the world to open or close at a specified location.
        // The command by default will open the chest. It accepts a sound argument which specifies whether
        // the open or close sound will play aswell as the animation. The sound plays by default and
        // can be disabled with 'sound:false' It also accepts a player or list of players to animate the chest to,
        // allowing only selected players to see the chest animate as opened or closed.
        // @Tags
        // TODO: Document Command Details
        // @Usage
        // Use to animate a chest to open at 15,89,-45 in world: world
        // - animatechest l@15,89,-45,world

        // @Usage
        // To then close the chest at 15,89,-45 in world: world
        // - animatechest l@15,89,-45,world close

        // @Usage
        // Use to animate a chest to open with no sound at 12,12,-64 in world: peter
        // - animatechest l@12,12,-64,peter sound:false

        // @Usage
        // If only a player by the name of Morphan1 should see the chest open
        // - animatechest l@12,12,-64,peter sound:false p@Morphan1

        // @Usage
        // The command also accepts a list of players to view the animation
        // - animatechest l@12,12,-64,peter sound:false p@Morphan1|p@mcmonkey4eva|p@Fortifier42
        // -->
        registerCoreMember(AnimateChestCommand.class,
                "ANIMATECHEST", "animatechest [<location>] ({open}/close) (sound:{true}/false) (<player>|...)", 1);


        // <--[command]
        // @Name Announce
        // @Syntax announce [<text>] (to_ops/to_console/to_flagged:<flag_name>) (format:<name>)
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
                "ANNOUNCE", "announce [<text>] (to_ops/to_console/to_flagged:<flag_name>) (format:<name>)", 1);


        // <--[command]
        // @Name Assignment
        // @Syntax assignment [set/remove] (script:<name>)
        // @Required 1
        // @Stable unstable
        // @Short Changes an NPC's assignment.
        // @Author aufdemrand
        // @Group npc
        // @Description
        // Changes an NPC's assignment as though you used the '/npc assignment' command.
        // Uses the script: argument, which accepts an assignment script type. For this command to work an npc must
        // be attached to the script queue or an npc specified with npc:n@npc.
        // @Tags
        // <n@npc.script>
        // @Usage
        // Use to assign an npc with an assignment script named 'Bob the Builder'.
        // - assignment set "script:Bob the Builder"
        // @Usage
        // Use to give an npc with the id of 3 an assignment.
        // - assignment set "script:Bob the Builder" npc:n@3
        // @Usage
        // Use to remove an npc's assignment.
        // - assignment remove
        // -->
        if (Depends.citizens != null) {
            registerCoreMember(AssignmentCommand.class,
                    "ASSIGNMENT", "assignment [set/remove] (script:<name>)", 1);
        }


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
        // @Name Ban
        // @Syntax ban ({ADD}/REMOVE) [<player>|...] (reason:<text>) (duration:<duration>)
        // @Required 1
        // @Stable stable
        // @Short Ban or un-ban a player or list of players.
        // @Author Fortifier42
        // @Group server
        // @Description
        // Add or remove bans from the server, with optional arguments for a reason and duration of a temporary
        // ban. By default will ban the specified player(s), and kick them from the server. You can set a reason for
        // which the player(s) will be banned, along with a duration (if you wish to temporarily ban them). If a
        // reason is not specified, it will default to "Banned.".  If a duration for the ban if not specified, they
        // will be banned permanently.
        // @Tags
        // <p@player.is_banned>
        // <p@player.ban_info.reason>
        // <p@player.ban_info.expiration>
        // <p@player.ban_info.created>

        // @Usage
        // Use to ban a player.
        // - ban p@mcmonkey4eva

        // @Usage
        // Use to ban a list of players with a reason.
        // - ban p@mcmonkey4eva|p@Morphan1 "reason:Didn't grow enough potatoes."

        // @Usage
        // Use to ban a list of players for 10 minutes with a reason.
        // - ban p@mcmonkey4eva|p@Morphan1 "reason:Didn't grow enough potatoes." duration:10m

        // @Usage
        // Use to unban a list of players.
        // - ban remove p@mcmonkey4eva|p@Morphan1

        // -->
        registerCoreMember(BanCommand.class,
                "BAN", "ban ({ADD}/REMOVE) [<player>|...] (reason:<text>) (duration:<duration>)", 1);


        // <--[command]
        // @Name Break
        // @Syntax break [<location>] (entity:<entity>) (radius:<#.#>)
        // @Required 1
        // @Stable unstable
        // @Short Makes the NPC walk over and break a block.
        // @Author aufdemrand
        // @Group world
        // @Description
        // By itself, the 'break' command will act as a NPC command in the sense that an attached
        // NPC will navigate to and break the block at the attached location. It can also accept a specified npc,
        // to fulfill the command, just specify a 'fetchable' npc object. It can also accept a radius to start
        // breaking the block from within. To specify the npc, prefix the npc with 'entity:'. To specify the
        // radius, prefix the radius with 'radius:'.

        // @Tags
        // <n@npc.navigator.is_navigating>
        // <n@npc.navigator.target_location>

        // @Usage
        // Use to make a npc break a block at 17,64,-87 in world
        // - break l@17,64,-87,world

        // @Usage
        // Use to make a npc with the id 12 break a block at 17,64,-87 in world
        // - break l@17,64,-87,world entity:n@12

        // @Usage
        // Use to make a npc with the name bob break a block at 17,64,-87 and start digging from 5 blocks away
        // - break l@17,64,-87,world entity:n@bob radius:5

        // -->
        if (Depends.citizens != null) {
            registerCoreMember(BreakCommand.class,
                    "BREAK", "break [<location>] (entity:<entity>) (radius:<#.#>)", 1);
        }


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

        // @Usage
        // Use to cancel fire on entities.
        // - burn <player.location.find.living_entities.within[10]> duration:0

        // -->
        registerCoreMember(BurnCommand.class,
                "BURN", "burn [<entity>|...] (duration:<value>)", 1);


        // <--[command]
        // @Name Cast
        // @Syntax cast [<effect>] (remove) (duration:<value>) (power:<#>) (<entity>|...) (no_ambient) (hide_particles)
        // @Required 1
        // @Stable Stable
        // @Short Casts a potion effect to a list of entities.
        // @Author aufdemrand, Jeebiss, Morphan1, mcmonkey
        // @Group entity

        // @Description
        // Casts or removes a potion effect to or from a list of entities. If you don't specify a duration,
        // it defaults to 60 seconds. If you don't specify a power level, it defaults to 1.
        // To cast an effect with a duration which displays as '**:**' or 'infinite' use a duration
        // of '1639s' (1639 seconds) or greater. While it may display as infinite, it will still wear off.
        //
        // If no player is specified, the command will target the player. If no player is present, the
        // command will target the NPC. If an NPC is not present, there will be an error!
        //
        // Optionally, specify "no_ambient" to hide some translucent additional particles, while still
        // rendering the main particles.
        // Optionally, specify "hide_particles" to remove the particle effects entirely.

        // @Tags
        // <e@entity.has_effect[<effect>]>

        // @Usage
        // Use to cast an effect onto the player for 120 seconds with a power of 3.
        // - cast jump d:120 p:3

        // @Usage
        // Use to remove an effect from the player.
        // - if <player.has_effect[jump]> {
        //   - cast jump remove <player>
        //   }
        //
        // -->
        registerCoreMember(CastCommand.class,
                "CAST", "cast [<effect>] (remove) (duration:<value>) (power:<#>) (<entity>|...) (no_ambient) (hide_particles)", 1);

        // TODO: Should the chat command be in the NPC group instead?
        // <--[command]
        // @Name Chat
        // @Syntax chat [<text>] (no_target/targets:<entity>|...) (talkers:<entity>|...) (range:<#.#>)
        // @Required 1
        // @Stable stable
        // @Short Causes a NPC/NPCs to send a chat message to nearby players.
        // @Author aufdemrand
        // @Group player

        // @Description
        // Chat uses an NPCs DenizenSpeechController provided by Denizen, typically inside 'interact' or 'task'
        // script-containers. Typically there is already player and NPC context inside a queue that is using
        // the 'chat' command. In this case, only a text string is required. Alternatively, target entities
        // can be specified to have any Entity chat to a different target/targets, or specify 'no_target' to
        // not send the message to any specific target.
        //
        // Chat from a NPC is formatted by the settings present in Denizen's config.yml. Players being chatted
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
        // the configuration node for the 'range' can be set to 0, however this is discouraged.

        // @Tags
        // None

        // @Usage
        // Use to emulate a NPC talking out loud to a Player within an interact script-container.
        // - chat "Hello, <player.name>! Nice day, eh?"

        // @Usage
        // Use to have a NPC talk to a group of individuals.
        // - flag <npc> talk_targets:!
        // - foreach <npc.location.find.players.within[6]> {
        //     - if <def[value].has_flag[clan_initiate]> {
        //       - flag <npc> talk_targets:->:<def[value]>
        //     }
        //   }
        // - chat targets:<npc.flag[talk_targets].as_list> "Welcome, initiate!"

        // -->
        if (Depends.citizens != null) {
            registerCoreMember(ChatCommand.class,
                    "CHAT", "chat [<text>] (no_target/targets:<entity>|...) (talkers:<entity>|...) (range:<#.#>)", 1);
        }


        // <--[command]
        // @Name ChunkLoad
        // @Syntax chunkload ({add}/remove/removeall) [<chunk>] (duration:<value>)
        // @Required 1
        // @Stable stable
        // @Short Keeps a chunk actively loaded and allowing NPC activity.
        // @Author spaceemotion, mcmonkey
        // @Group world
        // @Description
        // Forces a chunk to load and stay loaded in the world for the duration specified or until removed.  This is
        // persistent over server restarts. If no duration is specified it defaults to 0 (forever). While a chunk is
        // loaded all normal activity such as crop growth and npc activity continues.
        // @Tags
        // <w@world.loaded_chunks>
        // <ch@chunk.is_loaded>
        // @Usage
        // Use to load a chunk.
        // - chunkload ch@0,0,world
        // @Usage
        // Use to temporarily load a chunk.
        // - chunkload ch@0,0,world duration:5m
        // @Usage
        // Use to stop loading a chunk.
        // - chunkload remove ch@0,0,world
        // @Usage
        // Use to stop loading all chunks.
        // - chunkload removeall
        // -->
        registerCoreMember(ChunkLoadCommand.class,
                "CHUNKLOAD", "chunkload ({add}/remove/removeall) [<chunk>] (duration:<value>)", 1);


        // <--[command]
        // @Name Compass
        // @Syntax compass [<location>/reset]
        // @Required 1
        // @Stable stable
        // @Short Redirects the player's compass to target the given location.
        // @Author mcmonkey
        // @Group player
        // @Description
        // TODO: Document Command Details
        // @Tags
        // <p@player.compass.target>
        // @Usage
        // TODO: Document Command Details
        // -->
        registerCoreMember(CompassCommand.class,
                "COMPASS", "compass [<location>/reset]", 1);


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
        // @Syntax copyblock [<location>/<cuboid>] [to:<location>] (remove_original)
        // @Required 1
        // @Stable unstable
        // @Short Copies a block to another location, keeping all metadata.
        // @Author aufdemrand, David Cernat
        // @Group world
        // @Description
        // TODO: Document Command Details
        // @Tags
        // <l@location.material>
        // @Usage
        // TODO: Document Command Details
        // -->
        registerCoreMember(CopyBlockCommand.class,
                "COPYBLOCK", "copyblock [<location>/<cuboid>] [to:<location>] (remove_original)", 1);


        // <--[command]
        // @Name Create
        // @Syntax create [<entity>] [<name>] (<location>) (traits:<trait>|...)
        // @Required 1
        // @Stable experimental
        // @Short Creates a new NPC, and optionally spawns it at a location.
        // @Author aufdemrand
        // @Group npc
        // @Description
        // Creates an npc which the entity type specified, or specify an existing npc to create a copy. If no location
        // is specified the npc is created despawned. Use the 'save:<savename>' argument to return the npc for later
        // use in a script.
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
        if (Depends.citizens != null) {
            registerCoreMember(CreateCommand.class,
                    "CREATE", "create [<entity>] [<name>] (<location>)", 1);
        }


        // <--[command]
        // @Name CreateWorld
        // @Syntax createworld [<name>] (g:<generator>) (worldtype:<type>)
        // @Required 1
        // @Stable unstable
        // @Short Creates a new world
        // @Author aufdemrand, mcmonkey
        // @Group world
        // @Description
        // This command creates a new minecraft world with the specified name.
        // TODO: Document Command Details (generator)
        // It accepts a world type which can be specified with 'worldtype:'.
        // If a worldtype is not specified it will create a world with a worldtype of NORMAL.
        // Recognised world type are NORMAL (creates a normal world), FLAT (creates a world with flat terrain),
        // LARGE_BIOMES (creates a normal world with 16x larger biomes) and AMPLIFIED (creates a world with tall
        // mountain-like terrain)
        // @Tags
        // <server.list_worlds>
        // @Usage
        // Use to create a normal world with name survival
        // - createworld survival
        // @Usage
        // Use to create a flat world with the name superflat
        // - createworld superflat worldtype:FLAT
        // -->
        registerCoreMember(CreateWorldCommand.class,
                "CREATEWORLD", "createworld [<name>] (g:<generator>) (worldtype:<type>)", 1);


        // <--[command]
        // @Name Despawn
        // @Syntax despawn (<npc>|...)
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
        if (Depends.citizens != null) {
            registerCoreMember(DespawnCommand.class,
                    "DESPAWN", "despawn (<npc>)", 0);
        }


        // <--[command]
        // @Name Disengage
        // @Syntax disengage
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
        // - walk <player.location>
        // - wait 5s
        // - disengage
        //
        // -->
        if (Depends.citizens != null) {
            registerCoreMember(DisengageCommand.class,
                    "DISENGAGE", "disengage", 0);
        }


        // <--[command]
        // @Name DisplayItem
        // @Syntax displayitem [<item>] [<location>] (duration:<value>)
        // @Required 2
        // @Stable unstable
        // @Short Makes a non-touchable item spawn for players to view.
        // @Author aufdemrand, mcmonkey
        // @Group item
        // @Description
        // This command drops an item at the specified location which cannot be picked up by players.
        // It accepts a duration which determines how long the item will stay for until disappearing.
        // If no duration is specified the item will stay for 1 minute, after which the item will disappear.
        // @Tags
        // TODO: Document Command Details
        // <entry[saveName].dropped> returns a dEntity of the spawned item.
        // @Usage
        // Use to display a stone block dropped at a players location
        // - displayitem i@stone <player.location>
        // @Usage
        // Use to display a diamond sword dropped at 12,64,-847 in world survival
        // - displayitem i@diamond_sword l@12,64,-847,survival
        // @Usage
        // Use to display redstone dust dropped at -54,100,-87 in world creative disappear after 10 seconds
        // - displayitem i@redstone l@-54,100,-87,creative duration:10s
        // @Usage
        // Use to save the dropped item to save entry 'item_dropped'
        // - displayitem i@redstone l@-54,100,-87,creative duration:10s save:item_dropped
        // -->
        registerCoreMember(DisplayItemCommand.class,
                "DISPLAYITEM", "displayitem [<item>] [<location>] (duration:<value>)", 2);


        // <--[command]
        // @Name Drop
        // @Syntax drop [<entity_type>/xp/<item>|...] (<location>) (quantity:<#>) (speed:<#.#>) (delay:<duration>)
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
        // For all three usages, you can optionally specify an integer with 'quantity:'
        // prefix to drop multiple items/entities/xp.
        // For items, you can add 'speed:' to modify the launch velocity.
        // You can also add 'delay:' to set the pickup delay of the item.

        // @Tags
        // <entry[saveName].dropped_entities> returns a list of entities that were dropped.

        // @Usage
        // Use to drop some loot around the player.
        // - drop i@gold_nugget <cu@<player.location.add[-2,-2,-2]>|<player.location.add[2,2,2]>.get_spawnable_blocks.random>

        // @Usage
        // Use to reward a player with 500 xp.
        // - drop xp quantity:500 <player.location>

        // @Usage
        // Use to drop a nasty surprise (exploding TNT).
        // - drop e@primed_tnt <player.location>

        // @Usage
        // Use to drop an item with a pickup delay at the player's location.
        // - drop i@diamond_sword <player.location> delay:20s

        // -->
        registerCoreMember(DropCommand.class,
                "DROP", "drop [<entity_type>/xp/<item>|...] (<location>) (qty:<#>) (speed:<#.#>) (delay:<duration>)", 1);


        // <--[command]
        // @Name Engage
        // @Syntax engage (<duration>)
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
        // - walk <npc.anchor[mixing_station]>
        // - wait 10s
        // - walk <npc.anchor[service_station]>
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
        if (Depends.citizens != null) {
            registerCoreMember(EngageCommand.class,
                    "ENGAGE", "engage (<duration>)", 0);
        }


        // <--[command]
        // @Name Equip
        // @Syntax equip (<entity>|...) (hand:<item>) (head:<item>) (chest:<item>) (legs:<item>) (boots:<item>) (saddle:<item>) (horse_armor:<item>)
        // @Required 1
        // @Stable stable
        // @Short Equips items and armor on a list of entities.
        // @Author David Cernat
        // @Group entity
        // @Description
        // This command equips an item or armor to an entity or list of entities to the specified slot(s).
        // Set the item to 'i@air' to unequip any slot.
        // @Tags
        // <e@entity.equipment>
        // @Usage
        // Use to equip a stone block on the player's head.
        // - equip <player> head:i@stone
        // @Usage
        // Use to equip a iron helmet on two players named Bob and Steve.
        // - equip p@bob|p@steve head:i@iron_helmet
        // @Usage
        // Use to unequip all armor off the player.
        // - equip <player> head:i@air chest:i@air legs:i@air boots:i@air
        // @Usage
        // Use to equip a saddle on a horse.
        // - equip e@horse saddle:i@saddle
        // @Usage
        // Use to equip a saddle on a pig.
        // - equip e@pig saddle:i@saddle
        // -->
        registerCoreMember(EquipCommand.class,
                "EQUIP", "equip (<entity>|...) (hand:<item>) (head:<item>) (chest:<item>) (legs:<item>) (boots:<item>) (saddle:<item>) (horse_armor:<item>)", 1);


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
        // To add context information (tags like <context.location>) to the event, simply specify all context values in a list.
        // Note that there are some inherent limitations... EG, you can't directly add a list to the context currently.
        // To do this, the best way is to just escape the list value (see <@link language property escaping>).
        //
        // NOTE: This command is outdated and bound to be updated.
        // @Tags
        // <server.has_event[<event_name>]>
        // <server.get_event_handlers[<event_name>]>
        // <entry[saveName].determinations> returns a list of the determined values (if any) from the event.
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
        // @Syntax execute [as_player/as_op/as_npc/as_server] [<Bukkit command>] (silent)
        // @Required 2
        // @Stable stable
        // @Short Executes an arbitrary server command as if the player, NPC, or server typed it in.
        // @Author aufdemrand
        // @Group server
        // @Description
        // Allows the execution of server commands through a Denizen Script. Commands can be executed as the server,
        // as an npc, an op or as a player, as though it was typed by the respective source.
        // @Tags
        // <entry[saveName].output> returns the output to an as_server sender.
        // @Usage
        // Use to execute the save-all command as the server.
        // - execute as_server "save-all"
        // @Usage
        // Use to add the player to the op list as if an existing op had typed it.
        // - execute as_op "op <player.name>"
        // -->
        registerCoreMember(ExecuteCommand.class,
                "EXECUTE", "execute [as_player/as_op/as_npc/as_server] [<Bukkit command>] (silent)", 2);


        // <--[command]
        // @Name Experience
        // @Syntax experience [{set}/give/take] (level) [<#>]
        // @Required 2
        // @Stable TODO: Document Command Details
        // @Short Gives or takes experience points to the player.
        // @Author aufdemrand
        // @Group player
        // @Description
        // TODO: Document Command Details
        // @Tags
        // <p@player.xp>
        // <p@player.xp.to_next_level>
        // <p@player.xp.total>
        // <p@player.xp.level>
        // @Usage
        // Use to set a player's experience to 0
        // - experience 0
        // @Usage
        // Use give give a player 1 level
        // - experience give level 1
        // @Usage
        // Use to take 1 level from a player
        // - experience take level 1
        // @Usage
        // Use to give a player with the name Morphan1 10 experience points.
        // - experience give 10 player:p@Morphan1
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
        // This command causes an explosion at the location specified (or the npc / player location) which does not
        // destroy blocks or set fire to blocks within the explosion. It accepts a 'fire' option which will set blocks
        // on fire within the explosion radius. It also accepts a 'breakblocks' option which will cause the explosion to
        // break blocks within the power radius as well as creating an animation and sounds.
        // Default power: 1
        // Default location: npc.location, or if no NPC link, player.location.
        // It is highly recommended you specify a location to be safe.
        // @Tags
        // None
        // @Usage
        // Use to create an explosion at a player's location
        // - explode <player.location>

        // @Usage
        // Use to create an explosion at a player, which breaks blocks and causes fire with a power of 5
        // - explode power:5 <player.location> fire breakblocks

        // @Usage
        // Use to create an explosion with a power radius of 3 at a NPC's location
        // - explode power:3 <npc.location>

        // @Usage
        // Use to create an explosion with a power radius of 3 at a 12,12,-1297 in a world called survival which breaks blocks
        // - explode power:3 l@12,12,-1297,survival breakblocks

        // -->
        registerCoreMember(ExplodeCommand.class,
                "EXPLODE", "explode (power:<#.#>) (<location>) (fire) (breakblocks)", 0);


        // <--[command]
        // @Name Fail
        // @Syntax fail (script:<name>)
        // @Required 0
        // @Stable stable
        // @Deprecated This command is outdated, use flags instead!
        // @Short Marks a script as having failed.
        // @Author aufdemrand
        // @Group core
        // @Description
        // TODO: Document Command Details
        // @Tags
        // TODO: Document Command Details
        // @Usage
        // TODO: Document Command Details
        // -->
        registerCoreMember(FailCommand.class,
                "FAIL", "fail (script:<name>)", 0);


        // <--[command]
        // @Name Feed
        // @Syntax feed (amount:<#>) (target:<entity>|...)
        // @Required 0
        // @Stable unstable
        // @Short Feed the player or npc.
        // @Author aufdemrand, Jeebiss
        // @Group entity
        // @Description
        // Feeds the player or npc specified. By default targets the player attached to the script queue and feeds
        // a full amount. Accepts the 'amount:' argument, which is in half bar increments, for a total of 20 food
        // points. Also accepts the 'target:<entity>|...' argument to specify entity(s) which will be fed the amount.
        // NOTE: This command is outdated and bound to be updated.
        // @Tags
        // <p@player.food_level>
        // <p@player.food_level.formatted>
        // @Usage
        // Use to feed the player for 5 foodpoints or 2.5 bars.
        // - feed amount:5
        // @Usage
        // Use to feed an npc with id 5 for 10 foodpoints or 5 bars.
        // - feed amount:10 target:n@5
        // @Usage
        // Use to refill the food bar of all online players.
        // - feed target:<server.list_online_players>
        // -->
        registerCoreMember(FeedCommand.class,
                "FEED", "feed (amt:<#>) (target:<entity>|...)", 0);


        // <--[command]
        // @Name Finish
        // @Syntax finish (script:<name>)
        // @Required 0
        // @Stable stable
        // @Deprecated This command is outdated, use flags instead!
        // @Short Marks a script as having been completed successfully.
        // @Author aufdemrand
        // @Group core
        // @Description
        // TODO: Document Command Details
        // @Tags
        // TODO: Document Command Details
        // @Usage
        // TODO: Document Command Details
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
        // This command launches a firework from the specified location. The power option, which defaults to 1
        // if left empty, specifies how high the firework will go before exploding. The type option
        // which specifies the shape the firework will explode with. The primary option specifies what colour the
        // firework will initially explode as. The fade option specifies what colour the firework will
        // fade into after exploding. The flicker option means the firework will leave a trail behind it, and the
        // flicker option means the firework will explode with a flicker effect.
        // @Tags
        // <e@entity.firework_item> returns the firework item which was used to launch the firework.
        // TODO: Document Command Details
        // @Usage
        // Use to launch a star firework which explodes yellow and fades to white afterwards at the player's location
        // - firework <player.location> star primary:yellow fade:white
        // @Usage
        // Use to make the firework launch double the height before exploding
        // - firework <player.location> power:2 star primary:yellow fade:white
        // @Usage
        // Use to launch a firework which leaves a trail
        // - firework <player.location> random trail
        // @Usage
        // Use to launch a firework which leaves a trail and explodes with a flicker effect at 10,43,-76 in world
        // - firework l@10,43,-76,world random trail flicker
        // -->
        registerCoreMember(FireworkCommand.class,
                "FIREWORK", "firework (<location>) (power:<#>) (<type>/random) (primary:<color>|...) (fade:<color>|...) (flicker) (trail)", 0);


        // <--[command]
        // @Name Fish
        // @Syntax fish [<location>] (catch:{none}/default/junk/treasure/fish) (stop) (chance:<#>)
        // @Required 1
        // @Stable stable
        // @Short Causes an NPC to begin fishing around a specified location.
        // @Author Jeebiss, Morphan1
        // @Group npc
        // @Description
        // TODO: Document Command Details
        // @Tags
        // TODO: Document Command Details
        // @Usage
        // TODO: Document Command Details
        // -->
        registerCoreMember(FishCommand.class,
                "FISH", "fish [<location>] (catch:{none}/default/junk/treasure/fish) (stop) (chance:<#>)", 1);


        // <--[command]
        // @Name Flag
        // @Syntax flag ({player}/npc/server/<entity>) [<name>([<#>])](:<action>)[:<value>] (duration:<value>)
        // @Required 1
        // @Stable stable
        // @Short Sets or modifies a flag on the player, NPC, entity, or server.
        // @Author aufdemrand
        // @Group core
        // @Description
        // The flag command sets or modifies custom value storage database entries connected to
        // each player, each NPC, each entity, and the server.
        // TODO: Document Command Details
        // @Tags
        // <p@player.flag[<flag>]>
        // <n@npc.flag[<flag>]>
        // <server.flag[<flag>]>
        // @Usage
        // Use to create or set a flag on a player.
        // - flag player playstyle:agressive
        // @Usage
        // Use to flag an npc with a given tag value.
        // - flag npc location:<npc.location>
        // @Usage
        // Use to apply mathematical changes to a flag's value on a unique object.
        // - flag <context.damager> damage_dealt:+:<context.damage>
        // @Usage
        // Use to add an item to a server flag as a new value without removing existing values.
        // - flag server cool_people:->:p@TheBlackCoyote
        // @Usage
        // Use to add both multiple items as individual new values to a server flag.
        // - flag server cool_people:|:p@mcmonkey4eva|p@morphan1
        // @Usage
        // Use to remove an entry from a server flag.
        // - flag server cool_people:<-:p@morphan1
        // @Usage
        // Use to completely remove a flag.
        // - flag server cool_people:!
        // @Usage
        // Use to modify a specific index in a list flag.
        // - flag server myflag[3]:HelloWorld
        // -->
        registerCoreMember(FlagCommand.class,
                "FLAG", "flag ({player}/npc/server/<entity>) [<name>([<#>])](:<action>)[:<value>] (duration:<value>)", 1);


        // <--[command]
        // @Name Fly
        // @Syntax fly (cancel) [<entity>|...] (controller:<player>) (origin:<location>) (destinations:<location>|...) (speed:<#.#>) (rotationthreshold:<#.#>)
        // @Required 1
        // @Stable stable
        // @Short Make an entity fly where its controller is looking or fly to waypoints.
        // @Author David Cernat
        // @Group entity
        // @Description
        // TODO: Document Command Details
        // @Tags
        // TODO: Document Command Details
        // @Usage
        // TODO: Document Command Details
        // -->
        registerCoreMember(FlyCommand.class,
                "FLY", "fly (cancel) [<entity>|...] (controller:<player>) (origin:<location>) (destinations:<location>|...) (speed:<#.#>) (rotationthreshold:<#.#>)", 1);


        // <--[command]
        // @Name Follow
        // @Syntax follow (followers:<entity>|...) (stop) (lead:<#.#>) (max:<#.#>) (speed:<#.#>) (target:<entity>) (allow_wander)
        // @Required 0
        // @Stable stable
        // @Short Causes a list of entities to follow a target.
        // @Author aufdemrand, Morphan1
        // @Group entity
        // @Description
        // TODO: Document Command Details
        // The 'max' and 'allow_wander' arguments can only be used on non-NPC entities.
        // @Tags
        // <n@npc.navigator.target_entity> returns the entity the npc is following.
        // @Usage
        // To make an NPC follow the player in an interact script
        // - follow followers:<npc> target:<player>
        // TODO: Document Command Details
        // -->
        registerCoreMember(FollowCommand.class,
                "FOLLOW", "follow (followers:<entity>|...) (stop) (lead:<#.#>) (max:<#.#>) (speed:<#.#>) (target:<entity>) (allow_wander)", 0);


        // <--[command]
        // @Name Give
        // @Syntax give [money/xp/<item>|...] (quantity:<#>) (engrave) (to:<inventory>) (slot:<#>)
        // @Required 1
        // @Stable stable
        // @Short Gives the player an item, xp, or money.
        // @Author Jeebiss
        // @Group item
        // @Description
        // Gives the linked player or inventory any form of giveable object, including items, xp, or money.
        // TODO: Document Command Details
        // @Tags
        // <p@player.money>
        // @Usage
        // Use to give money to the player.
        // - give money quantity:10
        // @Usage
        // Use to give XP to the player.
        // - give xp quantity:10
        // @Usage
        // Use to give an item to the player.
        // - give i@iron_sword quantity:1
        // -->
        registerCoreMember(GiveCommand.class,
                "GIVE", "give [money/xp/<item>|...] (qty:<#>) (engrave) (to:<inventory>) (slot:<#>)", 1);


        // <--[command]
        // @Name Group
        // @Syntax group [add/remove/set] [<group>] (<world>)
        // @Required 2
        // @Stable TODO: Document Command Details
        // @Short Adds a player to, removes a player from or sets a players permissions group.
        // @Author GnomeffinWay
        // @Group player
        // @Plugin Vault
        // @Description
        // Controls a player's permission groups, which the ability to add, remove or set a player's groups.
        // The 'add' argument adds the player to the group and any parent groups, while the remove command does
        // the opposite, removing the player from the group and any inheriting groups. The set command removes all
        // existing groups and sets the player's group.
        // Note: This requires a permissions plugin.
        // @Tags
        // <p@player.in_group[<group>]>
        // <p@player.in_group[<group>].global>
        // <p@player.in_group[<group>].world>
        // @Usage
        // Use to add a player to the Admin group.
        // - group add Admin
        // @Usage
        // Use to remove a player from the Moderator group.
        // - group remove Moderator
        // @Usage
        // Use to set a player to the Member group in the Creative world.
        // - group set Member w@Creative
        // -->
        registerCoreMember(GroupCommand.class,
                "GROUP", "group [add/remove/set] [<group>] (<world>)", 2);


        // <--[command]
        // @Name Head
        // @Syntax head (<entity>|...) [skin:<player_name>]
        // @Required 1
        // @Stable stable
        // @Short Makes players or NPCs wear a specific player's head.
        // @Author David Cernat
        // @Group entity
        // @Description
        // Equips a player's head onto the player(s) or npc(s) specified. If no player or npc is specified, it defaults
        // to the player attached to the script queue. It accepts a single entity or list of entities.
        // @Tags
        // <i@item.skin>
        // @Usage
        // Use to stick an awesome head on your head with the head command.
        // - head <player> skin:mcmonkey4eva
        // @Usage
        // Use to equip an npc with id 5 with your own head.
        // - head n@5 skin:<player.name>
        // @Usage
        // Use to equip all online players with Notch's head.
        // - head <server.list_online_players> skin:Notch
        // -->
        registerCoreMember(HeadCommand.class,
                "HEAD", "head (<entity>|...) [skin:<player_name>]", 1);


        // <--[command]
        // @Name Heal
        // @Syntax heal (<#.#>) ({player}/<entity>|...)
        // @Required 0
        // @Stable stable
        // @Short Heals the player or list of entities.
        // @Author aufdemrand, Jeebiss, Morphan1, mcmonkey
        // @Group entity
        // @Description
        // This command heals a player, list of players, entity or list of entities. If no amount is specified it will
        // heal the specified player(s)/entity(s) fully.
        // @Tags
        // <e@entity.health>
        // @Usage
        // Use to fully heal a player
        // - heal
        // @Usage
        // Use to heal a player 5 hearts
        // - heal 10
        // @Usage
        // Use to heal a player by the name of Morphan1 fully
        // - heal p@Morphan1
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
        // @Syntax hurt (<#.#>) ({player}/<entity>|...) (cause:<cause>)
        // @Required 0
        // @Stable stable
        // @Short Hurts the player or a list of entities.
        // @Author aufdemrand, Jeebiss, morphan1, mcmonkey
        // @Group entity
        // @Description
        // Does damage to a list of entities, or to any single entity.
        //
        // If no entities are specified: if there is a linked player, the command targets that. If there is no linked
        // player but there is a linked NPC, the command targets the NPC. If neither is available, the command will error.
        //
        // Does a specified amount of damage usually, but, if no damage is specified, does precisely 1HP worth of damage
        // (half a heart).
        //
        // TODO: Document Command Details
        //
        // Optionally, specify (source:<entity>) to make the system treat that entity as the attacker,
        // be warned this does not always work as intended, and is liable to glitch.
        // Optionally, specify a damage cause to fire a proper damage event with the given cause,
        // only doing the damage if the event wasn't cancelled. Calculates the 'final damage' rather
        // than using the raw damage input number. See <@link language damage cause> for damage causes.
        // @Tags
        // <e@entity.health>
        // @Usage
        // Use to hurt the player for 1 HP.
        // - hurt
        // @Usage
        // Use to hurt the NPC for 5 HP.
        // - hurt 5 <npc>
        // @Usage
        // Use to cause the player to hurt the NPC for all its health (if unarmored).
        // - hurt <npc.health> <npc> cause:CUSTOM source:<player>
        // -->
        registerCoreMember(HurtCommand.class,
                "HURT", "hurt (<#.#>) (<entity>|...) (cause:<cause>)", 0);


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
        // <p@player.enderchest>
        // <n@npc.inventory>
        // <l@location.inventory>
        // @Usage
        // Use to open a chest inventory, at a location.
        // - inventory open d:l@123,123,123,world
        // @Usage
        // Use to open a virtual inventory with a title and some items.
        // - inventory open d:in@generic[size=27;title=BestInventory;contents=li@i@snow_ball;i@clay_brick]
        // @Usage
        // Use to open another player's inventory.
        // - inventory open d:<p@calico-kid.inventory>
        // @Usage
        // Use to remove all items from a chest, except any items in
        // the specified list.
        // - inventory keep d:in@location[holder=l@123,123,123,world] o:li@i@snow_ball|i@ItemScript
        // @Usage
        // Use to remove items specified in a chest from the current
        // player's inventory, regardless of the item count.
        // - inventory exclude origin:l@123,123,123,world
        // @Usage
        // Use to swap two players' inventories.
        // - inventory swap d:in@player[holder=p@mcmonkey4eva] o:<p@fullwall.inventory>
        // -->
        registerCoreMember(InventoryCommand.class,
                "INVENTORY", "inventory [open/close/copy/move/swap/add/remove/set/keep/exclude/fill/clear/update] (destination:<inventory>) (origin:<inventory>/<item>|...) (slot:<#>)", 1);


        // <--[command]
        // @Name Inject
        // @Syntax inject (locally) [<script>] (path:<name>) (instantly)
        // @Required 1
        // @Stable stable
        // @Short Runs a script in the current ScriptQueue.
        // @Video /denizen/vids/Run%20And%20Inject
        // @Author aufdemrand
        // @Group core
        // @Description
        // TODO: Document Command Details
        // @Tags
        // TODO: Document Command Details
        // @Usage
        // TODO: Document Command Details
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
        // TODO: Document Command Details
        // @Tags
        // TODO: Document Command Details
        // @Usage
        // TODO: Document Command Details
        // -->
        registerCoreMember(InvisibleCommand.class,
                "INVISIBLE", "invisible [<entity>] (state:true/false/toggle)", 1);

        // <--[command]
        // @Name kick
        // @Syntax kick [<player>|...] (reason:<text>)
        // @Required 1
        // @Stable stable
        // @Short Kicks a player from the server.
        // @Author Fortifier42
        // @Group player
        // @Description
        // Kick a player or a list of players from the server and optionally specify a reason.
        // If no reason is specified it defaults to "Kicked."
        // @Tags
        // None
        // @Usage
        // Use to kick the player with the default reason.
        // - kick <player>
        // @Usage
        // Use to kick the player with a reason.
        // - kick <player> "reason:Because I can."
        // @Usage
        // Use to kick another player with a reason.
        // - kick p@mcmonkey4eva "reason:Because I can."
        // -->
        registerCoreMember(KickCommand.class,
                "KICK", "kick [<player>|...] (reason:<text>)", 1);

        // <--[command]
        // @Name Leash
        // @Syntax leash (cancel) [<entity>|...] (holder:<entity>/<location>)
        // @Required 1
        // @Stable stable
        // @Short Sticks a leash on target entity, held by a fence post or another entity.
        // @Author Alain Blanquet, mcmonkey
        // @Group entity
        // @Description
        // TODO: Document Command Details
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
        // @Name Light
        // @Syntax light [<location>] [<#>/reset] (duration:<duration>)
        // @Required 2
        // @Stable stable
        // @Short Creates a light source at the location with a specified brightness.
        // @Author Morphan1
        // @Group world
        // @Description
        // This command can create and reset a light source at a specified location, regardless of the type
        // of block. It will be shown to all players near the location until it is reset.
        // The brightness must be between 0 and 15, inclusive.
        // Optionally, specify the amount of time the light should exist before being removed.
        // @Tags
        // <l@location.light>
        // <l@location.light.blocks>
        // @Usage
        // Use to create a bright light at a noted location.
        // - light l@MyFancyLightOfWool 15
        // @Usage
        // Use to reset the brightness of the location to its original state.
        // - light l@MyFancyLightOfWool reset
        // -->
        registerCoreMember(LightCommand.class,
                "LIGHT", "light [<location>] [<#>/reset] (duration:<duration>)", 2);


        // <--[command]
        // @Name Listen
        // @Syntax listen ({new}/cancel/finish) [kill/block/item/itemdrop/travel] [<requirements>] [script:<name>] (id:<name>)
        // @Required 2
        // @Stable unstable
        // @Deprecated This command may become unsupported in the future.
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
        // Itemdrop - TODO: Document Command Details
        // Travel - Used to detect when a player travels to an npc, to a specific location, into an area, or a specific distance.
        //
        // Detailed usage information can be found in the specific listener files.

        // @Tags
        // TODO: Document Command Details

        // @Usage
        // Use to listen for when the player kills 10 zombies.
        // - listen kill type:entity target:zombie quantity:10 script:ZombiesKilled

        // @Usage
        // Use to listen for when the player kills 3 zombies named "Boss"
        // - listen kill type:entity target:zombie quantity:3 mobnames:Boss script:ZombiesKilled

        // @Usage
        // Use to listen for when a player mines 1 iron ore.
        // - listen block type:break block:iron_ore quantity:1 script:IronMined

        // @Usage
        // Use to listen for when a player crafts 1 wooden sword.
        // - listen item type:craft item:wood_sword quantity:1 script:SwordCrafted

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
        // @Syntax log [<text>] (type:{info}/severe/warning/fine/finer/finest/none/clear) [file:<name>]
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
        //
        // Warning: Remember that file operations are dangerous! A typo in the filename could ruin your server.
        // It's recommended you use this command minimally.
        // @Tags
        // None.
        // @Usage
        // Use to log some information to a file.
        // - log "Security breach on level 3!" type:severe file:securitylog.txt
        // @Usage
        // Use to log a player's name and location when they did something dangerous.
        // - log "<player.name> used the '/EXPLODE' command at <player.location.simple>!" type:warning file:security.log
        // @Usage
        // Use to write information directly to a file.
        // - log "This won't have a date or type" type:none file:example.log
        // @Usage
        // Use to clear a log file and write some text at the start.
        // - log "// Log File Generated by my Denizen script, do not edit!" type:clear file:myfile.log
        // @Usage
        // Use to clear a log file entirely.
        // - log "" type:clear file:myfile.log
        // -->
        registerCoreMember(LogCommand.class,
                "LOG", "log [<text>] (type:{info}/severe/warning/fine/finer/finest/none/clear) [file:<name>]", 2);


        // <--[command]
        // @Name Look
        // @Syntax look (<entity>|...) [<location>] (duration:<duration>)
        // @Required 1
        // @Stable stable
        // @Short Causes the NPC or other entity to look at a target location.
        // @Author aufdemrand, mcmonkey
        // @Group entity
        // @Description
        // Makes the entity look towards the location, can be used on players. If a duration is set, the entity cannot
        // look away from the location until the duration has expired unless they are forces to look at a different
        // location.
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
        // @Syntax lookclose (<npc>) (state:<true/false>) (range:<#>) (realistic)
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
        if (Depends.citizens != null) {
            registerCoreMember(LookcloseCommand.class,
                    "LOOKCLOSE", "lookclose (<npc>) (state:<true/false>) (range:<#>) (realistic)", 0);
        }


        // <--[command]
        // @Name Map
        // @Syntax map [<#>/new:<world>] [reset:<location>/image:<file> (resize)/text:<text>] (x:<#>) (y:<#>)
        // @Required 2
        // @Stable stable
        // @Short Modifies a new or existing map by adding images or text.
        // @Author Morphan1
        // @Group item

        // @Description
        // This command modifies an existing map, or creates a new one. Using this will override existing
        // non-Denizen map renderers with Denizen's custom map renderer.
        // You can reset this at any time by using the 'reset:<location>' argument, which will remove all
        // images and texts on the map and show the default world map at the specified location.
        // Note that all maps have a size of 128x128.
        // The file path is relative to the '../plugins/Denizen/' folder.
        // Use escaping to let the image and text arguments have tags based on the player viewing the map.
        // Custom maps do not persist over restarts.

        // @Usage
        // Use to add an auto-resized background image to map 3
        // - map 3 image:my_map_images/my_background.png resize

        // @Usage
        // Use to add an image with the top-left corner at the center of a new map
        // - map new:w@world image:my_map_images/my_center_image.png x:64 y:64

        // @Usage
        // Use to add some text to map 0
        // - map 0 "text:Hello World!" x:35 y:60

        // @Usage
        // Reset map to have the center at the player's location
        // - map 3 reset:<player.location>
        // -->
        registerCoreMember(MapCommand.class,
                "MAP", "map [<#>/new:<world>] [reset:<location>/image:<file> (resize)/text:<text>] (x:<#>) (y:<#>)", 2);

        // <--[command]
        // @Name Midi
        // @Syntax midi (cancel) [<file>] (<location>/<entity>|...) (tempo:<#.#>) (volume:<#.#>)
        // @Required 1
        // @Stable stable
        // @Short Plays a midi file at a given location or to a list of players using note block sounds.
        // @Author David Cernat
        // @Group world

        // @Description
        // This will fully load a midi song file stored in the '../plugins/Denizen/midi/' folder. The file
        // must be a valid midi file with the extension '.mid'. It will continuously play the song as
        // noteblock songs at the given location or group of players until the song ends. If no location or
        // entity is specified, by default this will play for the attached player.
        //
        // Also, an example Midi song file has been included: "Denizen" by Black Coyote. He made it just for us!
        // Check out more of his amazing work at: http://www.youtube.com/user/BlaCoyProductions

        // @Tags
        // None

        // @Usage
        // Use to play a midi song file on the current player/
        // - midi file:Denizen

        // @Usage
        // Use to play a midi song file at a given location/
        // - midi file:Denizen <player.location>

        // @Usage
        // Use to play a midi song file at a given location to the specified player(s), and wait for it to finish.
        // - ~midi file:Denizen <server.list_online_players>

        // -->
        registerCoreMember(MidiCommand.class,
                "MIDI", "midi (cancel) [<file>] (<location>/<entity>|...) (tempo:<#.#>) (volume:<#.#>)", 1);


        // <--[command]
        // @Name Mount
        // @Syntax mount (cancel) [<entity>|...] (<location>)
        // @Required 0
        // @Stable stable
        // @Short Mounts one entity onto another.
        // @Author David Cernat
        // @Group entity
        // @Description
        // Mounts an entity onto another as though in a vehicle. Can be used to force a player into a vehicle or to
        // mount an entity onto another entity. e.g. a player onto an npc. If the entity(s) don't exist they will be
        // spawned. Accepts a location, which the entities will be teleported to on mounting.
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
        // @Syntax modifyblock [<location>|.../<ellipsoid>/<cuboid>] [<material>|...] (radius:<#>) (height:<#>) (depth:<#>) (no_physics/naturally) (delayed) (<script>) (<percentages>)
        // @Required 2
        // @Stable stable
        // @Short Modifies blocks.
        // @Author Jeebiss, aufdemrand, mcmonkey
        // @Group world
        // @Description
        // Changes blocks in the world based on the criteria given. Specifying no radius/height/depth will result
        // in only the specified blocks being changed. Use 'no_physics' to place the blocks without
        // physics taking over the modified blocks. This is useful for block types such as portals. This does NOT
        // control physics for an extended period of time.
        // Specify <percentages> to give a chance of each material being placed (in any materail at all).
        // Use 'naturally' when setting a block to air to break it naturally, meaning that it will drop items.
        // Use 'delayed' to make the modifyblock slowly edit blocks at a time pace roughly equivalent to the server's limits.
        // Note that specify a list of locations will take more time in parsing than in the actual block modification.
        // Optionally, specify a script to be ran after the delayed edits finish. (Doesn't fire if delayed is not set.)
        // This command is ~holdable.
        // @Tags
        // <l@location.block.material>
        // @Usage
        // Use to change the block a player is looking at to stone.
        // - modifyblock <player.location.cursor_on> stone
        // @Usage
        // Use to modify an entire cuboid to half stone, half dirt.
        // - modifyblock cu@<player.location>|<player.location.cursor_on> li@stone|dirt
        // @Usage
        // Use to modify an entire cuboid to some stone, some dirt, and some left as it is.
        // - modifyblock cu@<player.location>|<player.location.cursor_on> li@stone|dirt li@25|25
        // @Usage
        // TODO: Document Command Details
        // -->
        registerCoreMember(ModifyBlockCommand.class,
                "MODIFYBLOCK", "modifyblock [<location>|.../<ellipsoid>/<cuboid>] [<material>|...] (radius:<#>) (height:<#>) (depth:<#>) (no_physics/naturally) (delayed) (<script>)", 2);


        // <--[command]
        // @Name Narrate
        // @Syntax narrate [<text>] (targets:<player>|...) (format:<name>)
        // @Required 1
        // @Stable stable
        // @Short Shows some text to the player.
        // @Author aufdemrand
        // @Group player
        // @Description
        // Prints some text into the target's chat area. If no target is specified it will default to the attached player
        // or the console. Accepts the 'format:<name>' argument, which will reformat the text according to the specified
        // format script.
        // @Tags
        // None
        // @Usage
        // Use to narrate text to the player.
        // - narrate "Hello World!"
        // @Usage
        // Use to narrate text to a list of players.
        // - narrate "Hello there." targets:p@mcmonkey4eva|p@Morphan1|p@Fortifier42
        // -->
        registerCoreMember(NarrateCommand.class,
                "NARRATE", "narrate [<text>] (targets:<player>|...) (format:<name>)", 1);


        // <--[command]
        // @Name Note
        // @Syntax note [<Notable dObject>/remove] [as:<name>]
        // @Required 2
        // @Stable unstable
        // @Short Adds or removes a notable object.
        // @Author aufdemrand
        // @Group core
        // @Description
        // TODO: Document Command Details
        // @Tags
        // TODO: Document Command Details
        // @Usage
        // Use to add a notable cuboid.
        // - note cu@1,2,3,world|4,5,6,world as:mycuboid
        // @Usage
        // Use to remove a notable cuboid.
        // - note remove as:mycuboid
        // -->
        registerCoreMember(NoteCommand.class,
                "NOTE", "note [<Notable dObject>/remove] [as:<name>]", 2);


        // <--[command]
        // @Name Oxygen
        // @Syntax oxygen [<#>] (type:{remaining}/maximum) (mode:{set}/add/remove)
        // @Required 1
        // @Stable unstable
        // @Short Gives or takes breath from the player.
        // @Author David Cernat
        // @Group player
        // @Description
        // Used to add to, remove from or set the amount of oxygen of a player. Also allows for the changing of the
        // player's maximum oxygen level.
        // @Tags
        // <p@player.oxygen>
        // <p@player.oxygen.max>
        // @Usage
        // TODO: Document Command Details
        // -->
        registerCoreMember(OxygenCommand.class,
                "OXYGEN", "oxygen [<#>] (type:{remaining}/maximum) (mode:{set}/add/remove)", 1);


        // <--[command]
        // @Name Pause, Resume
        // @Syntax pause [waypoints/activity] (<duration>)
        // @Required 1
        // @Stable unstable
        // @Short Pauses/resumes an NPC's waypoint navigation or goal activity temporarily or permanently.
        // @Author aufdemrand
        // @Group npc
        // @Description
        // TODO: Document Command Details
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
        // - pause waypoints
        // - resume waypoints
        // -->
        if (Depends.citizens != null) {
            registerCoreMember(PauseCommand.class,
                    "PAUSE, RESUME", "pause [waypoints/activity] (<duration>)", 1);
        }


        // <--[command]
        // @Name PlayEffect
        // @Syntax playeffect [<location>|...] [effect:<name>] (data:<#.#>) (visibility:<#.#>) (quantity:<#>) (offset:<#.#>,<#.#>,<#.#>) (targets:<player>|...)
        // @Required 2
        // @Stable stable
        // @Short Plays a visible or audible effect at the location.
        // @Author David Cernat, mcmonkey
        // @Group world
        // @Description
        // TODO: Document Command Details
        // See <@link language Particle Effects> for a list of valid effect names.
        // @Tags
        // TODO: Document Command Details
        // @Usage
        // TODO: Document Command Details
        // -->
        registerCoreMember(PlayEffectCommand.class,
                "PLAYEFFECT", "playeffect [<location>|...] [effect:<name>] (data:<#.#>) (visibility:<#.#>) (qty:<#>) (offset:<#.#>,<#.#>,<#.#>) (targets:<player>|...)", 2);


        // <--[command]
        // @Name PlaySound
        // @Syntax playsound [<location>|.../<player>|...] [sound:<name>] (volume:<#.#>) (pitch:<#.#>) (custom)
        // @Required 2
        // @Stable stable
        // @Short Plays a sound at the location or to a list of players.
        // @Author Jeebiss
        // @Group world
        // @Description
        // TODO: Document Command Details
        // Optionally, specify 'custom' to play a custom sound added by a resource pack, changing the sound string to something like 'random.click'
        // @Tags
        // TODO: Document Command Details
        // @Usage
        // TODO: Document Command Details
        // -->
        registerCoreMember(PlaySoundCommand.class,
                "PLAYSOUND", "playsound [<location>|.../<player>|...] [sound:<name>] (volume:<#.#>) (pitch:<#.#>) (custom)", 2);


        // <--[command]
        // @Name Permission
        // @Syntax permission [add/remove] [permission] (group:<name>) (<world>)
        // @Required 2
        // @Stable unstable
        // @Short Gives or takes a permission node to/from the player or group.
        // @Author GnomeffinWay
        // @Group player
        // @Plugin Vault
        // @Description
        // Adds or removes a permission node from a player or group. Accepts a world for world-based permissions
        // plugins. By default changes the attached player's permissions. Accepts the 'group:<name>' argument to change
        // a group's permission nodes rather than a player's.
        // Note: This requires a permissions plugin.
        // @Tags
        // <p@player.has_permission[permission.node]>
        // <p@player.has_permission[permission.node].global>
        // <p@player.has_permission[permission.node].world>
        // @Usage
        // Use to give the player a permissions node.
        // - permission add bukkit.version
        // @Usage
        // Use to remove a permissions node from a player.
        // - permission remove bukkit.version
        // @Usage
        // Use to give the group 'Members' a permission node.
        // - permission add bukkit.version group:Members
        // @Usage
        // Use to remove a permissions node from the group 'Members' in the Creative world.
        // - permission remove bukkit.version group:Members w@Creative
        // -->
        registerCoreMember(PermissionCommand.class,
                "PERMISSION", "permission [add/remove] [permission] (group:<name>) (<world>)", 2);


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
        if (Depends.citizens != null) {
            registerCoreMember(PoseCommand.class,
                    "POSE", "pose (add/remove/{assume}) [id:<name>] (player/{npc}) (<location>)", 1);
        }


        // <--[command]
        // @Name Push
        // @Syntax push [<entity>|...] (origin:<entity>/<location>) (destination:<location>) (speed:<#.#>) (duration:<duration>) (<script>) (force_along) (precision:<#>) (no_rotate) (no_damage)
        // @Required 1
        // @Stable stable
        // @Short Pushes entities through the air in a straight line.
        // @Author David Cernat, mcmonkey
        // @Group entity
        // @Description
        // Pushes entities through the air in a straight line at a certain speed and for a certain duration, triggering a script when they hit an obstacle or stop flying.
        // @Tags
        // TODO: Document Command Details
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
                "PUSH", "push [<entity>|...] (origin:<entity>/<location>) (destination:<location>) (speed:<#.#>) (<duration>) (<script>) (force_along) (precision:<#>) (no_rotate) (no_damage)", 1);


        // <--[command]
        // @Name Pushable
        // @Syntax pushable (state:true/false/{toggle}) (delay:<duration>) (returnable:true/false)
        // @Required 0
        // @Stable stable
        // @Short Edits the pushable trait for NPCs.
        // @Author Morphan1
        // @Group npc
        // @Description
        // Enables, disables, toggles, or edits the Pushable trait on the attached NPC.
        // @Tags
        // None
        // @Usage
        // Use to toggle the Pushable trait for a specified NPC.
        // - pushable npc:n@321
        // @Usage
        // Use to enable the Pushable trait and return after 2 seconds.
        // - pushable state:true delay:2s returnable:true
        // -->
        registerCoreMember(PushableCommand.class,
                "PUSHABLE", "pushable (state:true/false/{toggle}) (delay:<duration>) (returnable:true/false)", 0);


        // <--[command]
        // @Name Queue
        // @Syntax queue (<queue>) [clear/stop/pause/resume/delay:<#>]
        // @Required 1
        // @Stable stable
        // @Short Modifies the current state of a script queue.
        // @Author aufdemrand
        // @Group core
        // @Description
        // TODO: Document Command Details
        // @Tags
        // <queue>
        // <queue.id>
        // <queue.size>
        // <queue.exists[queue_id]>
        // @Usage
        // Use to clear the current queue.
        // - queue clear
        // @Usage
        // Use to force-stop a given queue.
        // - queue <server.flag[OtherQueue]> clear
        // @Usage
        // Use to delay the current queue (use <@link command wait> instead!)
        // - queue delay:5t
        // @Usage
        // Use to pause the given queue.
        // - queue <server.flag[OtherQueue]> pause
        // @Usage
        // Use to resume the given queue.
        // - queue <server.flag[OtherQueue]> resume
        // -->
        registerCoreMember(QueueCommand.class,
                "QUEUE", "queue (queue:<id>) [clear/stop/pause/resume/delay:<#>]", 1);


        // <--[command]
        // @Name Random
        // @Syntax random [<#>/<commands>]
        // @Required 0
        // @Stable stable
        // @Short Selects a random choice from the following script commands.
        // @Author aufdemrand, morphan1
        // @Group core
        // @Description
        // TODO: Document Command Details

        // @Tags
        // <entry[saveName].possibilities> returns an Element of the possibility count.
        // <entry[saveName].selected> returns an Element of the selected number.

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
                "RANDOM", "random [<#>/<commands>]", 1);


        // <--[command]
        // @Name Remove
        // @Syntax remove [<entity>|...]
        // @Required 1
        // @Stable TODO: Document Command Details
        // @Short Despawns a list of entities, fully removing any NPCs.
        // @Author David Cernat
        // @Group entity
        // @Description
        // TODO: CUBOID OPTION
        // TODO: Document Command Details
        // @Tags
        // <e@entity.is_spawned>
        // @Usage
        // TODO: Document Command Details
        // -->
        registerCoreMember(RemoveCommand.class,
                "REMOVE", "remove [<entity>|...] (<world>)", 1);


        // <--[command]
        // @Name Rename
        // @Syntax rename [<name>]
        // @Required 1
        // @Stable unstable
        // @Short Renames the linked NPC.
        // @Author aufdemrand
        // @Group npc
        // @Description
        // TODO: Document Command Details
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
        if (Depends.citizens != null) {
            registerCoreMember(RenameCommand.class,
                    "RENAME", "rename [<name>]", 1);
        }


        // <--[command]
        // @Name Repeat
        // @Syntax repeat [stop/next/<amount>] [<commands>]
        // @Required 1
        // @Stable stable
        // @Short Runs a series of braced commands several times.
        // @Author morphan1, mcmonkey
        // @Group core
        // @Video /denizen/vids/Loops

        // @Description
        // Loops through a series of braced commands a specified number of times.
        // To get the number of loops so far, you can use <def[value]>.
        //
        // To stop a repeat loop, do - repeat stop
        //
        // To jump immediately to the next number in the loop, do - repeat next

        // @Tags
        // <def[value]> to get the number of loops so far

        // @Usage
        // Use loop through a command several times
        // - repeat 5 {
        //     - announce "Announce Number <def[value]>"
        //   }
        // -->
        registerCoreMember(RepeatCommand.class,
                "REPEAT", "repeat [stop/next/<amount>] [<commands>]", 1);


        // <--[command]
        // @Name Reset
        // @Syntax reset (<player>|...) [fails/finishes/cooldown/saves/global_cooldown] (<script>)
        // @Required 1
        // @Stable stable
        // @Short Resets various parts of Denizen's saves.yml, including a script's fails, finishes, or cooldowns.
        // @Author aufdemrand
        // @Group core
        // @Description
        // TODO: Document Command Details
        // @Tags
        // TODO: Document Command Details
        // @Usage
        // TODO: Document Command Details
        // -->
        registerCoreMember(ResetCommand.class,
                "RESET", "reset (<player>|...) [fails/finishes/cooldown/saves/global_cooldown] (<script>)", 1);


        // <--[command]
        // @Name Rotate
        // @Syntax rotate (<entity>|...) (yaw:<value>) (pitch:<value>) (duration:<duration>) (frequency:<duration>)
        // @Required 1
        // @Stable stable
        // @Short Rotates a list of entities.
        // @Author David Cernat
        // @Group entity
        // @Description
        // TODO: Document Command Details
        // @Tags
        // <e@entity.location.yaw>
        // <e@entity.location.pitch>
        // @Usage
        // TODO: Document Command Details
        // -->
        registerCoreMember(RotateCommand.class,
                "ROTATE", "rotate (cancel) (<entity>|...) (yaw:<value>) (pitch:<value>) (duration:<duration>) (infinite/frequency:<duration>)", 0);


        // <--[command]
        // @Name Run
        // @Syntax run (locally) [<script>] (path:<name>) (def:<element>|...) (id:<name>) (instantly) (delay:<value>)
        // @Required 1
        // @Stable stable
        // @Short Runs a script in a new ScriptQueue.
        // @Video /denizen/vids/Run%20And%20Inject
        // @Author aufdemrand
        // @Group core
        // @Description
        // TODO: Document Command Details
        // @Tags
        // <entry[saveName].created_queue>
        // @Usage
        // TODO: Document Command Details
        // -->
        registerCoreMember(RunCommand.class,
                "RUN", "run (locally) [<script>] (path:<name>) (def:<element>|...) (id:<name>) (instantly) (delay:<value>)", 1);


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
        // TODO: Document Command Details
        // @Tags
        // TODO: Document Command Details
        // @Usage
        // TODO: Document Command Details
        // -->
        registerCoreMember(RuntaskCommand.class,
                "RUNTASK", "runtask [<name>] (instantly) (queue(:<name>)) (delay:<#>) (define:<element>|...)", 1);


        // <--[command]
        // @Name Schematic
        // @Syntax schematic [create/load/unload/rotate/paste/save] [name:<name>] (angle:<#>) (<location>) (<cuboid>) (delayed)
        // @Group World
        // @Required 2
        // @Stable unstable
        // @Short Creates, loads, pastes, and saves schematics (Sets of blocks).
        // @Author mcmonkey

        // @Description
        // Todo

        // @Tags
        // <schematic[<name>].height>
        // <schematic[<name>].length>
        // <schematic[<name>].width>
        // <schematic[<name>].block[<location>]>
        // <schematic[<name>].origin>
        // <schematic[<name>].blocks>

        // @Usage
        // Use to create a new schematic from a cuboid and an origin location
        // - schematic create name:MySchematic cu@<player.location.sub[5,5,5]>|<player.location.add[5,5,5]> <player.location>

        // @Usage
        // Use to load a schematic
        // - schematic load name:MySchematic

        // @Usage
        // Use to unload a schematic
        // - schematic unload name:MySchematic

        // @Usage
        // Use to rotate a loaded schematic
        // - schematic rotate name:MySchematic angle:90

        // @Usage
        // Use to paste a loaded schematic
        // - schematic paste name:MySchematic <player.location> noair

        // @Usage
        // Use to save a created schematic
        // - schematic save name:MySchematic

        // -->
        registerCoreMember(SchematicCommand.class,
                "SCHEMATIC", "schematic [create/load/unload/rotate/paste/save] [name:<name>] (angle:<#>) (<location>) (<cuboid>) (delayed)", 2);


        // <--[command]
        // @Name Sidebar
        // @Syntax sidebar (add/remove/{set}) (title:<title>) (lines:<#>|...) (values:<line>|...) (start:<#>/{num_of_lines}) (increment:<#>/{-1}) (players:<player>|...) (per_player)
        // @Required 1
        // @Stable stable
        // @Short Controls clientside-only sidebars.
        // @Author Morphan1
        // @Group player
        // @Description
        // This command was created as a simpler replacement for using the Scoreboard command to display
        // per-player sidebars. By using packets and dummies, it enables you to have non-flickering, fully
        // functional sidebars without wasting processing speed and memory on creating new Scoreboards for
        // every single player.
        //
        // Using this command, you can add, remove, or set lines on the scoreboard. The 'lines' parameter
        // is used to specify which line you want to set using 'values:' or remove. It can also be used to
        // add lines in between existing lines. To change multiple lines at once, simply use a list in both
        // the 'lines:' and 'values:' arguments and have each index correspond with the other.
        //
        // Setting the title of the sidebar is extremely simple, and can be done by using the 'title:'
        // parameter in any case where the action is 'set'.
        //
        // To control which score numbers are shown, use the 'start:' and 'increment:' arguments in any case
        // where the action is 'set'. 'Start' is the score where the first line will be shown with. The default
        // 'start' value is determined by how many items are specified in 'values:'. 'Increment' is the difference
        // between each score and the default is -1. Using the default values of these, the sidebar displays each
        // line in order with the score counting down from the total number of lines to 1.
        //
        // The per_player argument is also available, and helps to reduce the number of loops required for
        // updating multiple players' sidebars. When it is specified, all tags in the command will fill based
        // on each individual player in the players list. So, for example, you could have <player.name> on a
        // lines and it will show each player specified their name on that line.
        //
        // @Tags
        // <p@player.sidebar.lines>
        // <p@player.sidebar.title>
        // <p@player.sidebar.scores>
        // <p@player.sidebar.start>
        // <p@player.sidebar.increment>
        //
        // @Usage
        // Show all online players a sidebar.
        // - sidebar set "title:Hello World!" "values:This is|My Message!|Wee!" "players:<server.list_online_players>"
        //
        // @Usage
        // Show a few players their ping.
        // - sidebar set "title:Info" "value:Ping<&co> <player.ping>" "players:p@Morphan1|p@mcmonkey4eva|p@Matterom" per_player
        //
        // @Usage
        // Set a line on the sidebar a player is viewing.
        // - sidebar set "line:2" "value:This is my line now!"
        //
        // @Usage
        // Add a line to the bottom of the sidebar.
        // - sidebar add "value:This is the bottom!"
        //
        // @Usage
        // Remove multiple lines from the sidebar.
        // - sidebar remove "lines:2|4|6"
        //
        // @Usage
        // Stop showing the sidebar.
        // - sidebar remove
        // -->
        registerCoreMember(SidebarCommand.class,
                "SIDEBAR", "sidebar (add/remove/{set}) (title:<title>) (lines:<#>|...) (values:<line>|...) (start:<#>/{num_of_lines}) (increment:<#>/{-1}) (players:<player>|...) (per_player)", 1);


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
        // TODO: Document Command Details
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
        // @Stable TODO: Document Command Details
        // @Short Writes information to a book from a book-type script or a book item.
        // @Author Jeebiss, aufdemrand
        // @Group item
        // @Description
        // TODO: Document Command Details
        // @Tags
        // <i@item.book.author>
        // <i@item.book.title>
        // <i@item.book.page_count>
        // <i@item.book.get_page[<#>]>
        // <i@item.book.pages>
        // @Usage
        // TODO: Document Command Details
        // -->
        registerCoreMember(ScribeCommand.class,
                "SCRIBE", "scribe [<script>] (<item>/give/equip/{drop <location>})", 1);


        // <--[command]
        // @Name Shoot
        // @Syntax shoot [<entity>|...] (origin:<entity>/<location>) (destination:<location>) (height:<#.#>) (gravity:<#.#>) (speed:<#.#>) (script:<name>) (shooter:<entity>) (spread:<#.#>) (lead:<location>) (no_rotate)
        // @Required 1
        // @Stable stable
        // @Short Shoots an entity through the air, useful for things like firing arrows.
        // @Author David Cernat, mcmonkey
        // @Group entity
        // @Description
        // Shoots an entity through the air up to a certain height, optionally using a custom gravity value and triggering a script on impact with a target.
        // If the origin is not an entity, specify a shooter so the damage handling code knows how to assume shot the projectile.
        // Normally, a list of entities will spawn mounted on top of each other. To have them instead fire separately and spread out,
        // specify the 'spread' argument with a decimal number indicating how wide to spread the entities.
        // In the script ran when the arrow lands, the following definitions will be available:
        // <def[shot_entities]> for all shot entities, <def[last_entity]> for the last one (The controlling entity),
        // <def[location]> for the last known location of the last shot entity, and
        // <def[hit_entities]> for a list of any entities that were hit by fired projectiles.
        // Optionally, specify a speed and 'lead' value to use the experimental arrow-aiming system.
        // Optionally, add 'no_rotate' to prevent the shoot command from rotating launched entities.
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
                "SHOOT", "shoot [<entity>|...] (origin:<entity>/<location>) (destination:<location>) (height:<#.#>) (gravity:<#.#>) (speed:<#.#>) (script:<name>) (shooter:<entity>) (spread:<#.#>) (lead:<location>) (no_rotate)", 1);


        // <--[command]
        // @Name ShowFake
        // @Syntax showfake [<material>|.../cancel] [<location>|...] (players:<player>|...) (d:<duration>{10s})
        // @Required 2
        // @Stable stable
        // @Short Makes the player see a block change that didn't actually happen.
        // @Author aufdemrand, Morphan1, mcmonkey
        // @Group player
        // @Description
        // TODO: Document Command Details
        // @Tags
        // TODO: Document Command Details
        // @Usage
        // TODO: Document Command Details
        // -->
        registerCoreMember(ShowFakeCommand.class,
                "SHOWFAKE", "showfake [<material>|.../cancel] [<location>|...] (players:<player>|...) (d:<duration>{10s})", 2);


        // <--[command]
        // @Name Sign
        // @Syntax sign (type:{sign_post}/wall_sign) ["<line>|..."] [<location>] (direction:n/e/w/s)
        // @Required 1
        // @Stable stable
        // @Short Modifies a sign.
        // @Author David Cernat, mcmonkey
        // @Group world
        // @Description
        // TODO: Document Command Details
        // Specify 'automatic' as a type to use whatever sign type and direction is already placed there.
        // If there is not already a sign there, defaults to a sign_post.
        // @Tags
        // <l@location.block.sign_contents>
        // @Usage
        // TODO: Document Command Details
        // -->
        registerCoreMember(SignCommand.class,
                "SIGN", "sign (type:{automatic}/sign_post/wall_sign) [\"<line>|...\"] [<location>] (direction:n/s/e/w)", 1);


        // <--[command]
        // @Name Sit
        // @Syntax sit (<location>)
        // @Required 0
        // @Stable unstable
        // @Short Causes the NPC to sit. To make them stand, see <@link command Stand>.
        // @Author Jeebiss, mcmonkey
        // @Group npc
        // @Description
        // TODO: Document Command Details
        // @Tags
        // TODO: Document Command Details
        // @Usage
        // TODO: Document Command Details
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
        // Spawn an entity or list of entities at the specified location. Accepts the 'target:<entity>' argument which
        // will cause all spawned entities to follow and attack the targetted entity.
        // If the persistent argument is present, the entity will not despawn when no players are within range, causing
        // the enity to remain until killed.
        // @Tags
        // <e@entity.is_spawned>
        // <server.entity_is_spawned[<entity>]>
        // <entry[saveName].spawned_entities> returns a list of entities that were spawned.
        // @Usage
        // Use to spawn a spider at the player's location.
        // - spawn spider <player.location>
        // @Usage
        // Use to spawn a spider at the player's location which will automatically target the player.
        // - spawn spider <player.location> target:<player>
        // @Usage
        // Use to spawn a swarm of creepers around the npc, which will not despawn until killed.
        // - spawn creeper|creeper|creeper|creeper|creeper <npc.location> persistent
        // -->
        registerCoreMember(SpawnCommand.class,
                "SPAWN", "spawn [<entity>|...] [<location>] (target:<entity>) (persistent)", 2);


        // <--[command]
        // @Name SQL
        // @Syntax sql [id:<ID>] [disconnect/connect:<server> (username:<username>) (password:<password>)/query:<query>/update:<update>]
        // @Required 1
        // @Stable unstable
        // @Short Interacts with a MySQL server.
        // @Author mcmonkey
        // @Group core
        // @Description
        // This command is used to interact with a MySQL server. It can update the database or query it for information.
        // The general usage order is connect -> update/query -> disconnect.
        // It is not required that you disconnect right after using, and in fact encouraged that you keep a connection open where possible.
        // When connecting, the server format is IP:Port/Database, EG 'localhost:3306/test'.
        // Note that when using tag, it is recommended you escape unusual inputs to avoid SQL injection.
        // The SQL command is merely a wrapper for SQL queries, and further usage details should be gathered from an official
        // MySQL query reference rather than from Denizen command help.
        // SQL connections are not instant - they can take several seconds, or just never connect at all.
        // It is recommended you hold the connection command by doing "- ~sql ..." rather than just "- sql ..."
        // as this will delay the commands following the connect command until after the connection is established.
        // @Tags
        // <entry[saveName].result> returns a dList of all rows from a query or update command, of the form li@escaped_text/escaped_text|escaped_text/escaped_text
        // <entry[saveName].affected_rows> returns how many rows were affected by an update command.
        // @Usage
        // Use to connect to an SQL server.
        // - ~sql id:name connect:localhost:3306/test username:space password:space
        // @Usage
        // Use to connect to an SQL server with a UTF8 text encoding
        // - ~sql id:name connect:localhost:3306/test?characterEncoding=utf8 username:space password:space
        // @Usage
        // Use to update an SQL server.
        // - sql id:name "update:CREATE table things(id int,column_name1 varchar(255),column_name2 varchar(255));"
        // @Usage
        // Use to update an SQL server.
        // - sql id:name "update:INSERT INTO things VALUES (3, 'hello', 'space');"
        // @Usage
        // Use to query an SQL server.
        // - sql id:name "query:SELECT id,column_name1,column_name2 FROM things;" save:saveName
        // - narrate <entry[saveName].result>
        // @Usage
        // Use to query an SQL server.
        // - sql id:name "query:SELECT id,column_name1,column_name2 FROM things WHERE id=3;" save:saveName2
        // - narrate <entry[saveName2].result>
        // @Usage
        // Use to disconnect from an SQL server.
        // - sql disconnect id:name
        // -->
        registerCoreMember(SQLCommand.class,
                "SQL", "sql [id:<ID>] [disconnect/connect:<server> (username:<username>) (password:<password>)/query:<query>/update:<update>]", 2);


        // <--[command]
        // @Name Stand
        // @Syntax stand
        // @Required 0
        // @Stable unstable
        // @Short Causes the NPC to stand. To make them sit, see <@link command Sit>.
        // @Author Jeebiss
        // @Group npc
        // @Description
        // TODO: Document Command Details
        // @Tags
        // TODO: Document Command Details
        // @Usage
        // TODO: Document Command Details
        // -->
        if (Depends.citizens != null) {
            registerCoreMember(StandCommand.class,
                    "STAND", "stand", 0);
        }


        // <--[command]
        // @Name Statistic
        // @Syntax statistic [<statistic>] [add/take/set] (<#>) (qualifier:<material>/<entity>)
        // @Required 2
        // @Stable stable
        // @Short Changes the specified statistic value for a player.
        // @Author Morphan1
        // @Group player
        // @Description
        // TODO: Document Command Details
        // @Tags
        // <p@player.statistic[<statistic>]>
        // <p@player.statistic[<statistic>].qualifier[<material>/<entity>]>
        // @Usage
        // TODO: Document Command Details
        // -->
        registerCoreMember(StatisticCommand.class,
                "STATISTIC", "statistic [<statistic>] [add/take/set] (<#>) (qualifier:<material>/<entity>) (players:<player>|...)", 2);


        // <--[command]
        // @Name Strike
        // @Syntax strike (no_damage) [<location>]
        // @Required 1
        // @Stable stable
        // @Short Strikes lightning down upon the location.
        // @Author aufdemrand
        // @Group world
        // @Description
        // Causes lightning to strike at the specified location, which can optionally have damage disabled.
        // The lightning will still cause fires to start, even without the 'no_damage' argument.
        // Lightning caused by this command will cause creepers to activate. Using the no_damage argument makes the
        // lightning do no damage to the player or any other entities, and means creepers struck will not activate.
        // @Tags
        // None
        // @Usage
        // Use to cause lightning to strike the player.
        // - strike <player.location>
        // @Usage
        // Use to strike the player with lightning causing no damage.
        // - strike no_damage <player.location>
        // -->
        registerCoreMember(StrikeCommand.class,
                "STRIKE", "strike (no_damage) [<location>]", 1);

        // <--[command]
        // @Name Switch
        // @Syntax switch [<location>|...] (state:[{toggle}/on/off]) (duration:<value>)
        // @Required 1
        // @Stable stable
        // @Short Switches a lever.
        // @Author aufdemrand, Jeebiss, David Cernat
        // @Group world
        // @Description
        // TODO: Document Command Details
        // @Tags
        // TODO: Document Command Details
        // @Usage
        // TODO: Document Command Details
        // -->
        registerCoreMember(SwitchCommand.class,
                "SWITCH", "switch [<location>|...] (state:[{toggle}/on/off]) (duration:<value>)", 1);

        // <--[command]
        // @Name Take
        // @Syntax take [money/iteminhand/bydisplay:<name>/bycover:<title>|<author>/slot:<#>/<item>|...] (quantity:<#>) (from:<inventory>)
        // @Required 1
        // @Stable stable
        // @Short Takes an item from the player.
        // @Author Jeebiss
        // @Group item
        // @Description
        // TODO: Document Command Details
        // @Tags
        // <p@player.item_in_hand>
        // <p@player.money>
        // @Usage
        // TODO: Document Command Details
        // -->
        registerCoreMember(TakeCommand.class,
                "TAKE", "take [money/iteminhand/bydisplay:<name>/bycover:<title>|<author>/slot:<#>/<item>|...] (qty:<#>) (from:<inventory>)", 1);

        // <--[command]
        // @Name Team
        // @Syntax team (id:<scoreboard>/{main}) [name:<team>] (add:<player>|...) (remove:<player>|...) (prefix:<prefix>) (suffix:<suffix>)
        // @Required 2
        // @Stable stable
        // @Short Controls scoreboard teams.
        // @Author Morphan1
        // @Group player
        // @Description
        // The Team command allows you to add modify a team's prefix and suffix, as well as adding
        // and removing players from teams.
        // NOTE: Prefixes and suffixes cannot be longer than 16 characters!
        // @Tags
        // None
        // @Usage
        // Use to add a player to a team.
        // - team name:red add:<player>
        // @Usage
        // Use to change the prefix for a team.
        // - team name:red "prefix:[<red>Red Team<reset>]"
        // -->
        registerCoreMember(TeamCommand.class,
                "TEAM", "team (id:<scoreboard>/{main}) [name:<team>] (add:<player>|...) (remove:<player>|...) (prefix:<prefix>) (suffix:<suffix>)", 2);

        // <--[command]
        // @Name Teleport
        // @Syntax teleport (<entity>|...) [<location>]
        // @Required 1
        // @Stable stable
        // @Short Teleports the entity(s) to a new location.
        // @Author David Cernat, aufdemrand
        // @Group entity
        // @Description
        // Teleports the entity or entities to the new location. Entities can be teleported between worlds using this
        // command, assuming the location is valid.
        // @Tags
        // <e@entity.location>
        // @Usage
        // TODO: Document Command Details
        // -->
        registerCoreMember(TeleportCommand.class,
                "TELEPORT", "teleport (<entity>|...) [<location>]", 1);

        // <--[command]
        // @Name Time
        // @Syntax time ({global}/player) [<time duration>] (<world>)
        // @Required 1
        // @Stable TODO: Document Command Details
        // @Short Changes the current time in the minecraft world.
        // @Author David Cernat, mcmonkey
        // @Group world
        // @Description
        // Changes the current time in a world or the time that a player sees the world in.
        // TODO: Document Command Details
        // If no world is specified, defaults to the NPCs world. If no NPC is available,
        // defaults to the player's world. If no player is available, an error will be thrown.
        // @Tags
        // <w@world.time>
        // <w@world.time.period>
        // @Usage
        // Use to set the time in the NPC or Player's world.
        // - time 500t
        // @Usage
        // Use to make the player see a different time than everyone else.
        // - time player 500t
        // @Usage
        // Use to set the time in a specific world.
        // - time 500t w@myworld
        // @Usage
        // TODO: Document Command Details
        // -->
        registerCoreMember(TimeCommand.class,
                "TIME", "time ({global}/player) [<time duration>] (<world>)", 1);

        // <--[command]
        // @Name Title
        // @Syntax title (title:<text>) (subtitle:<text>) (fade_in:<duration>/{1s}) (stay:<duration>/{3s}) (fade_out:<duration>/{1s}) (targets:<player>|...)
        // @Required 1
        // @Stable stable
        // @Short Displays a title to specified players.
        // @Author Morphan1
        // @Group player
        // @Description
        // Shows the players a large, noticeable wall of text in the center of the screen.
        // You may add timings for fading in, staying there, and fading out.
        // The defaults for these are: 1 second, 3 seconds, and 1 second, respectively.
        // @Tags
        // None
        // @Usage
        // Use to alert players of impending server restart.
        // - title "title:<red>Server Restarting" "subtitle:<red>In 1 minute!" stay:1m targets:<server.list_online_players>
        // @Usage
        // Use to inform the player about the area they have just entered.
        // - title "title:<green>Tatooine" "subtitle:<gold>What a desolate place this is."
        // -->
        registerCoreMember(TitleCommand.class,
                "TITLE", "title (title:<text>) (subtitle:<text>) (fade_in:<duration>/{1s}) (stay:<duration>/{3s}) (fade_out:<duration>/{1s}) (targets:<player>|...)", 1);

        // <--[command]
        // @Name Trait
        // @Syntax trait (state:true/false/{toggle}) [<trait>]
        // @Required 1
        // @Stable Stable
        // @Short Adds or removes a trait from an NPC.
        // @Author Morphan1
        // @Group npc
        // @Description
        // TODO: Document Command Details
        // @Tags
        // <n@npc.has_trait[<trait>]>
        // <n@npc.list_traits>
        // @Usage
        // TODO: Document Command Details
        // -->
        if (Depends.citizens != null) {
            registerCoreMember(TraitCommand.class,
                    "TRAIT", "trait (state:true/false/{toggle}) [<trait>]", 1);
        }


        // <--[command]
        // @Name Trigger
        // @Syntax trigger [name:chat/click/damage/proximity] (state:{toggle}/true/false) (cooldown:<duration>) (radius:<#>)
        // @Required 1
        // @Stable stable
        // @Short Enables or disables a trigger.
        // @Author aufdemrand
        // @Group npc
        // @Description
        // TODO: Document Command Details
        // @Tags
        // <n@npc.has_trigger[<trigger>]>
        // @Usage
        // Use to enable the click trigger.
        // - trigger name:click state:true
        // @Usage
        // Use to enable the chat trigger with a 10-second cooldown and a radius of 5 blocks.
        // - trigger name:chat state:true cooldown:10s radius:5
        // @Usage
        // TODO: Document Command Details
        // -->
        registerCoreMember(TriggerCommand.class,
                "TRIGGER", "trigger [name:chat/click/damage/proximity] (state:{toggle}/true/false) (cooldown:<duration>) (radius:<#>)", 1);

        // <--[command]
        // @Name Viewer
        // @Syntax viewer ({create <location>}/modify/remove) [id:<name>] (type:{sign_post}/wall_sign) (display:{location}/score/logged_in) (direction:n/e/w/s)
        // @Required 1
        // @Stable unstable
        // @Short Creates a sign that auto-updates with information.
        // @Author Morphan1
        // @Group world

        // @Description
        // Creates a sign that auto-updates with information about a player, including their location, score, and
        // whether they're logged in or not.

        // @Tags
        // None

        // @Usage
        // Create a sign that shows the location of a player on a wall.
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
        // TODO: Document Command Details
        // @Tags
        // TODO: Document Command Details
        // @Usage
        // TODO: Document Command Details
        // -->
        if (Depends.citizens != null) {
            registerCoreMember(VulnerableCommand.class,
                    "VULNERABLE", "vulnerable (state:{true}/false/toggle)", 0);
        }

        // <--[command]
        // @Name Wait
        // @Syntax wait (<duration>) (queue:<name>)
        // @Required 0
        // @Stable stable
        // @Short Delays a script for a specified amount of time.
        // @Author aufdemrand
        // @Group core
        // @Description
        // Pauses the script queue for the duration specified. If no duration is specified it defaults to 3 seconds.
        // Accepts the 'queue:<name>' argument which allows the delay of a different queue.
        // @Tags
        // <q@queue.speed>
        // @Usage
        // Use to delay the current queue for 1 minute.
        // - wait 1m
        // -->
        registerCoreMember(WaitCommand.class,
                "WAIT", "wait (<duration>) (queue:<name>)", 0);

        // <--[command]
        // @Name Walk
        // @Syntax walk (<entity>|...) [<location>/stop] (speed:<#.#>) (auto_range) (radius:<#.#>)
        // @Required 1
        // @Stable stable
        // @Short Causes an entity or list of entities to walk to another location.
        // @Author aufdemrand, Morphan1
        // @Group entity
        // @Description
        // TODO: Document Command Details
        // @Tags
        // <n@npc.navigator.is_navigating>
        // <n@npc.navigator.speed>
        // <n@npc.navigator.range>
        // <n@npc.navigator.target_location>
        // @Usage
        // TODO: Document Command Details
        // -->
        registerCoreMember(WalkCommand.class,
                "WALK, WALKTO", "walk (<entity>|...) [<location>/stop] (speed:<#>) (auto_range) (radius:<#.#>)", 1);

        // <--[command]
        // @Name Weather
        // @Syntax weather [type:{global}/player] [sunny/storm/thunder] (world:<name>)
        // @Required 1
        // @Stable TODO: Document Command Details
        // @Short Changes the current weather in the minecraft world.
        // @Author David Cernat
        // @Group world
        // @Description
        // TODO: Document Command Details
        // @Tags
        // TODO: Document Command Details
        // @Usage
        // TODO: Document Command Details
        // -->
        registerCoreMember(WeatherCommand.class,
                "WEATHER", "weather [type:{global}/player] [sunny/storm/thunder] (world:<name>)", 1);

        // <--[command]
        // @Name While
        // @Syntax while [stop/next/<comparison tag>] [<commands>]
        // @Required 1
        // @Stable stable
        // @Short Runs a series of braced commands until the tag returns false.
        // @Author mcmonkey
        // @Group core
        // @Video /denizen/vids/Loops

        // @Description
        // TODO: Document Command Details

        // @Tags
        // <def[loop_index]> to get the number of loops so far.

        // @Usage
        // Use loop through a command several times.
        // - define value 1
        // - while <def[value].is[OR_LESS].than[5]> {
        //     - announce "Loop <def[loop_index]> value <def[value]>"
        //     - define value <def[value].add[1]>
        //   }
        // @Usage
        // TODO: Document Command Details
        // -->
        registerCoreMember(WhileCommand.class,
                "WHILE", "while [stop/next/<comparison tag>] [<commands>]", 1);


        // <--[command]
        // @Name Yaml
        // @Syntax yaml [create]/[load:<file> (fix_formatting)]/[unload]/[savefile:<file>]/[set <key>([<#>])(:<action>):<value>] [id:<name>]
        // @Required 2
        // @Stable stable
        // @Short Edits a YAML configuration file.
        // @Author aufdemrand, mcmonkey
        // @Group core
        // @Description
        // Edits a YAML configuration file.
        // This can be used for interacting with other plugins' configuration files.
        // It can also be used for storing your own script's data.
        // It can even be used to edit Denizen's saves/config, or even Denizen scripts.
        // TODO: Document Command Details
        // When loading a script, optionally add 'fix_formatting' to run the file through
        // Denizen's built in script preparser to correct common YAML errors,
        // such as tabs instead of spaces or comments inside braced blocks.
        // @Tags
        // <yaml[<idname>].contains[<path>]>
        // <yaml[<idname>].read[<path>]>
        // <yaml[<idname>].read[<path>].as_list>
        // <yaml[<idname>].list_keys[<path>]>
        // @Usage
        // Use to create a new YAML file.
        // - yaml create id:myfile
        // @Usage
        // Use to load a YAML file from disk.
        // - yaml load:myfile.yml id:myfile
        // @Usage
        // Use to modify a YAML file similarly to a flag.
        // - yaml id:myfile set my.key:HelloWorld
        // @Usage
        // Use to save a YAML file to disk.
        // - yaml savefile:myfile.yml id:myfile
        // @Usage
        // Use to unload a YAML file from memory.
        // - yaml unload id:myfile
        // @Usage
        // Use to modify a YAML file similarly to a flag.
        // - yaml id:myfile set my.key:+:2
        // @Usage
        // Use to modify a YAML file similarly to a flag.
        // - yaml id:myfile set my.key[2]:hello
        // -->
        registerCoreMember(YamlCommand.class,
                "YAML", "yaml [create]/[load:<file> (fix_formatting)]/[unload]/[savefile:<file>]/[set <key>([<#>])(:<action>):<value>] [id:<name>]", 2);

        // <--[command]
        // @Name Zap
        // @Syntax zap (<script>) [<step>] (<duration>)
        // @Required 0
        // @Stable stable
        // @Short Changes the current script step.
        // @Author aufdemrand
        // @Group core
        // @Description
        // TODO: Document Command Details
        // @Tags
        // <s@script.step[<player>]>
        // @Usage
        // Use to change the step to 2
        // - zap 2
        // @Usage
        // Use to change the step to 3 in a script called Interact_Example.
        // - zap 3 s@Interact_Example
        // @Usage
        // Use to change the step to 1 for player bob in a script called InteractScript.
        // - zap 1 s@InteractScript player:p@bob
        // -->
        registerCoreMember(ZapCommand.class,
                "ZAP", "zap (<script>) [<step>] (<duration>)", 0);

        dB.echoApproval("Loaded core commands: " + instances.keySet().toString());
    }
}
