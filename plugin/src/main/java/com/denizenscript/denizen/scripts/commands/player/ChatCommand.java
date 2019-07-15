package com.denizenscript.denizen.scripts.commands.player;

import com.denizenscript.denizen.utilities.Utilities;
import com.denizenscript.denizen.utilities.debugging.Debug;
import com.denizenscript.denizen.Settings;
import com.denizenscript.denizen.npc.speech.DenizenSpeechContext;
import com.denizenscript.denizen.npc.speech.DenizenSpeechController;
import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizencore.exceptions.InvalidArgumentsException;
import com.denizenscript.denizencore.objects.Argument;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.ArgumentHelper;
import com.denizenscript.denizencore.objects.core.ListTag;
import com.denizenscript.denizencore.scripts.ScriptEntry;
import com.denizenscript.denizencore.scripts.commands.AbstractCommand;
import com.denizenscript.denizencore.tags.TagManager;
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
    // - chat targets:<npc.location.find.players.within[6].filter[has_flag[clan_initiate]]> "Welcome, initiate!"
    // -->

    @Override
    public void parseArgs(ScriptEntry scriptEntry) throws InvalidArgumentsException {

        boolean specified_targets = false;
        boolean specified_talker = false;

        for (Argument arg : ArgumentHelper.interpretArguments(scriptEntry.aHArgs)) {
            // Default target is the attached Player, if none specified otherwise.
            if (arg.matchesPrefix("target", "targets", "t")) {
                if (arg.matchesArgumentList(EntityTag.class)) {
                    scriptEntry.addObject("targets", arg.asType(ListTag.class));
                }
                specified_targets = true;
            }
            else if (arg.matches("no_target")) {
                scriptEntry.addObject("targets", new ListTag());
            }

            // Default talker is the attached NPC, if none specified otherwise.
            else if (arg.matchesPrefix("talker", "talkers")) {
                if (arg.matchesArgumentList(EntityTag.class)) {
                    scriptEntry.addObject("talkers", arg.asType(ListTag.class));
                }
                specified_talker = true;

            }
            else if (arg.matchesPrefix("range", "r")) {
                if (arg.matchesPrimitive(ArgumentHelper.PrimitiveType.Double)) {
                    scriptEntry.addObject("range", arg.asElement());
                }
            }
            else if (!scriptEntry.hasObject("message")) {
                scriptEntry.addObject("message", new ElementTag(arg.raw_value));
            }
            else {
                arg.reportUnhandled();
            }
        }

        // Add default recipient as the attached Player if no recipients set otherwise
        if (!scriptEntry.hasObject("targets") && Utilities.entryHasPlayer(scriptEntry) && !specified_targets) {
            scriptEntry.defaultObject("targets", new ListTag(Utilities.getEntryPlayer(scriptEntry).identify()));
        }

        // Add default talker as the attached NPC if no recipients set otherwise
        if (!scriptEntry.hasObject("talkers") && Utilities.entryHasNPC(scriptEntry) && !specified_talker) {
            scriptEntry.defaultObject("talkers", new ListTag(Utilities.getEntryNPC(scriptEntry).identify()));
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

        scriptEntry.defaultObject("range", new ElementTag(Settings.chatBystandersRange()));

    }

    @Override
    public void execute(ScriptEntry scriptEntry) {

        ListTag talkers = scriptEntry.getdObject("talkers");
        ListTag targets = scriptEntry.getdObject("targets");
        ElementTag message = scriptEntry.getElement("message");
        ElementTag chatRange = scriptEntry.getElement("range");

        if (scriptEntry.dbCallShouldDebug()) {

            Debug.report(scriptEntry, getName(), talkers.debug() + targets.debug() + message.debug() + chatRange.debug());

        }

        // Create new speech context
        DenizenSpeechContext context = new DenizenSpeechContext(TagManager.cleanOutputFully(message.asString()),
                scriptEntry, chatRange.asDouble());

        if (!targets.isEmpty()) {
            for (EntityTag ent : targets.filter(EntityTag.class, scriptEntry)) {
                context.addRecipient(ent.getBukkitEntity());
            }
        }

        for (EntityTag talker : talkers.filter(EntityTag.class, scriptEntry)) {

            Entity entity = talker.getBukkitEntity();
            if (entity != null) {
                context.setTalker(entity);
                new DenizenSpeechController(entity).speak(context);
            }
            else {
                Debug.echoDebug(scriptEntry, "Chat Talker is not spawned! Cannot talk.");
            }

        }

    }
}
