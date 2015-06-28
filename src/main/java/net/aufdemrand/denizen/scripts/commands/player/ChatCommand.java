package net.aufdemrand.denizen.scripts.commands.player;

import net.aufdemrand.denizen.BukkitScriptEntryData;
import net.aufdemrand.denizen.Settings;
import net.aufdemrand.denizen.npc.speech.DenizenSpeechContext;
import net.aufdemrand.denizen.npc.speech.DenizenSpeechController;
import net.aufdemrand.denizen.objects.dEntity;
import net.aufdemrand.denizen.utilities.debugging.dB;
import net.aufdemrand.denizencore.exceptions.CommandExecutionException;
import net.aufdemrand.denizencore.exceptions.InvalidArgumentsException;
import net.aufdemrand.denizencore.objects.Element;
import net.aufdemrand.denizencore.objects.aH;
import net.aufdemrand.denizencore.objects.dList;
import net.aufdemrand.denizencore.scripts.ScriptEntry;
import net.aufdemrand.denizencore.scripts.commands.AbstractCommand;
import net.aufdemrand.denizencore.tags.TagManager;
import org.bukkit.entity.Entity;

public class ChatCommand extends AbstractCommand {

    @Override
    public void parseArgs(ScriptEntry scriptEntry) throws InvalidArgumentsException {

        boolean specified_targets = false;
        boolean specified_talker = false;

        for (aH.Argument arg : aH.interpret(scriptEntry.getArguments())) {
            // Default target is the attached Player, if none specified otherwise.
            if (arg.matchesPrefix("target", "targets", "t")) {
                if (arg.matchesArgumentList(dEntity.class))
                    scriptEntry.addObject("targets", arg.asType(dList.class));
                specified_targets = true;
            }

            else if (arg.matches("no_target"))
                scriptEntry.addObject("targets", new dList());

                // Default talker is the attached NPC, if none specified otherwise.
            else if (arg.matchesPrefix("talker", "talkers")) {
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
        if (!scriptEntry.hasObject("targets") && ((BukkitScriptEntryData) scriptEntry.entryData).hasPlayer() && !specified_targets)
            scriptEntry.defaultObject("targets", new dList(((BukkitScriptEntryData) scriptEntry.entryData).getPlayer().identify()));

        // Add default talker as the attached NPC if no recipients set otherwise
        if (!scriptEntry.hasObject("talkers") && ((BukkitScriptEntryData) scriptEntry.entryData).hasNPC() && !specified_talker)
            scriptEntry.defaultObject("talkers", new dList(((BukkitScriptEntryData) scriptEntry.entryData).getNPC().identify()));

        // Verify essential fields are set
        if (!scriptEntry.hasObject("targets"))
            throw new InvalidArgumentsException("Must specify valid targets!");

        if (!scriptEntry.hasObject("talkers"))
            throw new InvalidArgumentsException("Must specify valid talkers!");

        if (!scriptEntry.hasObject("message"))
            throw new InvalidArgumentsException("Must specify a message!");

        scriptEntry.defaultObject("range", new Element(Settings.chatBystandersRange()));

    }

    @Override
    public void execute(ScriptEntry scriptEntry) throws CommandExecutionException {

        dList talkers = scriptEntry.getdObject("talkers");
        dList targets = scriptEntry.getdObject("targets");
        Element message = scriptEntry.getElement("message");
        Element chatRange = scriptEntry.getElement("range");

        dB.report(scriptEntry, getName(), talkers.debug() + targets.debug() + message.debug() + chatRange.debug());

        // Create new speech context
        DenizenSpeechContext context = new DenizenSpeechContext(TagManager.cleanOutputFully(message.asString()),
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
