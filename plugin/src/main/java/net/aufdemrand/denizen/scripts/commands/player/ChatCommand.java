package net.aufdemrand.denizen.scripts.commands.player;

import net.aufdemrand.denizen.Settings;
import net.aufdemrand.denizen.npc.speech.DenizenSpeechContext;
import net.aufdemrand.denizen.npc.speech.DenizenSpeechController;
import net.aufdemrand.denizen.objects.dEntity;
import net.aufdemrand.denizen.utilities.Utilities;
import net.aufdemrand.denizen.utilities.debugging.dB;
import net.aufdemrand.denizencore.exceptions.InvalidArgumentsException;
import net.aufdemrand.denizencore.objects.Element;
import net.aufdemrand.denizencore.objects.aH;
import net.aufdemrand.denizencore.objects.dList;
import net.aufdemrand.denizencore.scripts.ScriptEntry;
import net.aufdemrand.denizencore.scripts.commands.AbstractCommand;
import net.aufdemrand.denizencore.tags.TagManager;
import org.bukkit.entity.Entity;

public class ChatCommand extends AbstractCommand {

    // TODO: Should the chat command be in the NPC group instead?
    // <--[command]
    // @Name Chat
    // @Syntax chat [<text>] (no_target/targets:<entity>|...) (talkers:<entity>|...) (range:<#.#>)
    // @Required 1
    // @Plugin Citizens
    // @Short Causes an NPC/NPCs to send a chat message to nearby players.
    // @Group player
    //
    // @Description
    // Chat uses an NPCs DenizenSpeechController provided by Denizen, typically inside 'interact' or 'task'
    // script-containers. Typically there is already player and NPC context inside a queue that is using
    // the 'chat' command. In this case, only a text input is required. Alternatively, target entities
    // can be specified to have any Entity chat to a different target/targets, or specify 'no_target' to
    // not send the message to any specific target.
    //
    // Chat from an NPC is formatted by the settings present in Denizen's config.yml. Players being chatted
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
    //
    // @Tags
    // None
    //
    // @Usage
    // Use to emulate an NPC talking out loud to a Player within an interact script-container.
    // - chat "Hello, <player.name>! Nice day, eh?"
    //
    // @Usage
    // Use to have an NPC talk to a group of individuals.
    // - flag <npc> talk_targets:!
    // - foreach <npc.location.find.players.within[6]> {
    //     - if <def[value].has_flag[clan_initiate]> {
    //       - flag <npc> talk_targets:->:<def[value]>
    //     }
    //   }
    // - chat targets:<npc.flag[talk_targets].as_list> "Welcome, initiate!"
    // -->

    @Override
    public void parseArgs(ScriptEntry scriptEntry) throws InvalidArgumentsException {

        boolean specified_targets = false;
        boolean specified_talker = false;

        for (aH.Argument arg : aH.interpretArguments(scriptEntry.aHArgs)) {
            // Default target is the attached Player, if none specified otherwise.
            if (arg.matchesPrefix("target", "targets", "t")) {
                if (arg.matchesArgumentList(dEntity.class)) {
                    scriptEntry.addObject("targets", arg.asType(dList.class));
                }
                specified_targets = true;
            }
            else if (arg.matches("no_target")) {
                scriptEntry.addObject("targets", new dList());
            }

            // Default talker is the attached NPC, if none specified otherwise.
            else if (arg.matchesPrefix("talker", "talkers")) {
                if (arg.matchesArgumentList(dEntity.class)) {
                    scriptEntry.addObject("talkers", arg.asType(dList.class));
                }
                specified_talker = true;

            }
            else if (arg.matchesPrefix("range", "r")) {
                if (arg.matchesPrimitive(aH.PrimitiveType.Double)) {
                    scriptEntry.addObject("range", arg.asElement());
                }
            }
            else if (!scriptEntry.hasObject("message")) {
                scriptEntry.addObject("message", new Element(arg.raw_value));
            }
            else {
                arg.reportUnhandled();
            }
        }

        // Add default recipient as the attached Player if no recipients set otherwise
        if (!scriptEntry.hasObject("targets") && Utilities.entryHasPlayer(scriptEntry) && !specified_targets) {
            scriptEntry.defaultObject("targets", new dList(Utilities.getEntryPlayer(scriptEntry).identify()));
        }

        // Add default talker as the attached NPC if no recipients set otherwise
        if (!scriptEntry.hasObject("talkers") && Utilities.entryHasNPC(scriptEntry) && !specified_talker) {
            scriptEntry.defaultObject("talkers", new dList(Utilities.getEntryNPC(scriptEntry).identify()));
        }

        // Verify essential fields are set
        if (!scriptEntry.hasObject("targets")) {
            throw new InvalidArgumentsException("Must specify valid targets!");
        }

        if (!scriptEntry.hasObject("talkers")) {
            throw new InvalidArgumentsException("Must specify valid talkers!");
        }

        if (!scriptEntry.hasObject("message")) {
            throw new InvalidArgumentsException("Must specify a message!");
        }

        scriptEntry.defaultObject("range", new Element(Settings.chatBystandersRange()));

    }

    @Override
    public void execute(ScriptEntry scriptEntry) {

        dList talkers = scriptEntry.getdObject("talkers");
        dList targets = scriptEntry.getdObject("targets");
        Element message = scriptEntry.getElement("message");
        Element chatRange = scriptEntry.getElement("range");

        if (scriptEntry.dbCallShouldDebug()) {

            dB.report(scriptEntry, getName(), talkers.debug() + targets.debug() + message.debug() + chatRange.debug());

        }

        // Create new speech context
        DenizenSpeechContext context = new DenizenSpeechContext(TagManager.cleanOutputFully(message.asString()),
                scriptEntry, chatRange.asDouble());

        if (!targets.isEmpty()) {
            for (dEntity ent : targets.filter(dEntity.class, scriptEntry)) {
                context.addRecipient(ent.getBukkitEntity());
            }
        }

        for (dEntity talker : talkers.filter(dEntity.class, scriptEntry)) {

            Entity entity = talker.getBukkitEntity();
            if (entity != null) {
                context.setTalker(entity);
                new DenizenSpeechController(entity).speak(context);
            }
            else {
                dB.echoDebug(scriptEntry, "Chat Talker is not spawned! Cannot talk.");
            }

        }

    }
}
