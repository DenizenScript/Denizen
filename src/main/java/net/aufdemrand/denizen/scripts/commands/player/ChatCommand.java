package net.aufdemrand.denizen.scripts.commands.player;

import net.aufdemrand.denizen.Settings;
import net.aufdemrand.denizen.exceptions.CommandExecutionException;
import net.aufdemrand.denizen.exceptions.InvalidArgumentsException;
import net.aufdemrand.denizen.npc.speech.DenizenSpeechContext;
import net.aufdemrand.denizen.npc.speech.DenizenSpeechController;
import net.aufdemrand.denizen.objects.Element;
import net.aufdemrand.denizen.objects.aH;
import net.aufdemrand.denizen.objects.dEntity;
import net.aufdemrand.denizen.objects.dList;
import net.aufdemrand.denizen.scripts.ScriptEntry;
import net.aufdemrand.denizen.scripts.commands.AbstractCommand;
import net.aufdemrand.denizen.tags.TagManager;
import net.aufdemrand.denizen.utilities.debugging.dB;
import net.citizensnpcs.Settings.Setting;
import org.bukkit.entity.Entity;

/**
 * <p>Uses the Citizens SpeechController to 'chat', the default VocalChord for
 * of an NPC. Chat prefixes and setup is found in Citizen's config.yml file.</p>
 *
 * <b>dScript Usage:</b><br>
 * <pre>CHAT  ['message to chat.'] (TARGET(S):list_of_LivingEntities) (TALKER:NPC.#)</pre>
 *
 * <ol><tt>Arguments: [] - Required</ol></tt>
 *
 * <ol><tt>['message to chat']</tt><br>
 *         The chat message the Talker will use. This will be seen by all entities within range.</ol>
 *
 * <ol><tt>(TARGET(S):NONE|List of LivingEntities{Interact Player})</tt><br>
 *         The LivingEntities that the message is addressed to. Uses the dScript List format
 *         (item1|item2|etc). Valid entities are: PLAYER.player_name, NPC.npcid, or ENTITY.entity_name.
 *         If NONE is specified, the NPC speaking will have no target. Default target is set to
 *         the Player doing the interaction (if that information is available to the command).</ol>
 *
 * <ol><tt>(TALKER:NPC.npcid{Interact NPC})</tt><br>
 *         The NPC that will be doing the chatting. Defaults to the NPC interacted with (if that information is
 *         available to the command), but can be changed by using the NPC LivingEntity format (NPC.npcid).</ol>
 *
 * <br><b>Example Usage:</b><br>
 * <ol><tt>
 *  - CHAT 'Be careful out there! The road is long and dark.' <br>
 *  - CHAT TARGET:NONE 'Beer here! Beer for sale! ...anybody need a beer?' <br>
 *  - CHAT TARGETS:PLAYER.aufdemrand|PLAYER.Jeebiss|PLAYER.DrBix 'Ah, a group of adventurers! Great!'
 *  - CHAT TALKER:NPC.13 TARGET:NPC.<NPC.ID> 'Shut up, old man!'
 * </ol></tt>
 *
 * @author Jeremy Schroeder
 *
 */
public class ChatCommand extends AbstractCommand {

    // TODO: Make this class abstract to minimize code duplication for Whisper/Shout/etc.

    @Override
    public void parseArgs(ScriptEntry scriptEntry) throws InvalidArgumentsException {

        boolean specified_targets = false;
        boolean specified_talker = false;

        for (aH.Argument arg : aH.interpret(scriptEntry.getArguments())) {
            // Default target is the attached Player, if none specified otherwise.
            if (arg.matchesPrefix("target, targets, t")) {
                if (arg.matchesArgumentList(dEntity.class))
                    scriptEntry.addObject("targets", arg.asType(dList.class));
                specified_targets = true;
            }

            else if (arg.matches("no_target"))
                scriptEntry.addObject("targets", new dList());

                // Default talker is the attached NPC, if none specified otherwise.
            else if (arg.matchesPrefix("talker, talkers")) {
                if (arg.matchesArgumentList(dEntity.class))
                    scriptEntry.addObject("talkers", arg.asType(dList.class));
                specified_talker = true;

            }

            else if (arg.matchesPrefix("range", "r")) {
                if (arg.matchesPrimitive(aH.PrimitiveType.Double))
                    scriptEntry.addObject("range", arg.asElement());
            }

            else if (!scriptEntry.hasObject("message"))
                scriptEntry.addObject("message", new Element(arg.raw_value));

            else
                arg.reportUnhandled();
        }

        // Add default recipient as the attached Player if no recipients set otherwise
        if (!scriptEntry.hasObject("targets") && scriptEntry.hasPlayer() && !specified_targets)
            scriptEntry.defaultObject("targets", new dList(scriptEntry.getPlayer().identify()));

        // Add default talker as the attached NPC if no recipients set otherwise
        if (!scriptEntry.hasObject("talkers") && scriptEntry.hasNPC() && !specified_talker)
            scriptEntry.defaultObject("talkers", new dList(scriptEntry.getNPC().identify()));

        // Verify essential fields are set
        if (!scriptEntry.hasObject("targets"))
            throw new InvalidArgumentsException("Must specify valid targets!");

        if (!scriptEntry.hasObject("talkers"))
            throw new InvalidArgumentsException("Must specify valid talkers!");

        if (!scriptEntry.hasObject("message"))
            throw new InvalidArgumentsException("Must specify a message!");

        scriptEntry.defaultObject("range", new Element(Settings.ChatBystandersRange()));

    }

    @Override
    public void execute(ScriptEntry scriptEntry) throws CommandExecutionException {

        dList talkers = scriptEntry.getdObjectAs("talkers", dList.class);
        dList targets = scriptEntry.getdObjectAs("targets", dList.class);
        Element message = scriptEntry.getElement("message");
        Element chatRange = scriptEntry.getElement("range");

        dB.report(scriptEntry, getName(), talkers.debug() + targets.debug() + message.debug() + chatRange.debug());

        // Create new speech context
        DenizenSpeechContext context = new DenizenSpeechContext(TagManager.CleanOutputFully(message.asString()),
                scriptEntry, chatRange.asDouble());

        if (!targets.isEmpty()) {
            for (dEntity ent : targets.filter(dEntity.class)) {
                context.addRecipient(ent.getBukkitEntity());
            }
        }

        for (dEntity talker : talkers.filter(dEntity.class)) {

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
