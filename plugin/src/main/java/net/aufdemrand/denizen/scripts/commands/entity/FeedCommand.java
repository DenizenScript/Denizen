package net.aufdemrand.denizen.scripts.commands.entity;

import net.aufdemrand.denizen.BukkitScriptEntryData;
import net.aufdemrand.denizen.npc.traits.HungerTrait;
import net.aufdemrand.denizen.objects.dNPC;
import net.aufdemrand.denizen.objects.dPlayer;
import net.aufdemrand.denizen.utilities.debugging.dB;
import net.aufdemrand.denizen.utilities.depends.Depends;
import net.aufdemrand.denizencore.exceptions.CommandExecutionException;
import net.aufdemrand.denizencore.exceptions.InvalidArgumentsException;
import net.aufdemrand.denizencore.objects.Element;
import net.aufdemrand.denizencore.objects.aH;
import net.aufdemrand.denizencore.scripts.ScriptEntry;
import net.aufdemrand.denizencore.scripts.commands.AbstractCommand;

public class FeedCommand extends AbstractCommand {

    @Override
    public void parseArgs(ScriptEntry scriptEntry) throws InvalidArgumentsException {

        for (aH.Argument arg : aH.interpret(scriptEntry.getArguments())) {
            if (arg.matchesPrimitive(aH.PrimitiveType.Integer)
                    && !scriptEntry.hasObject("amount")) {
                scriptEntry.addObject("amount", arg.asElement());
            }
            else if (arg.matchesArgumentType(dPlayer.class)
                    && !scriptEntry.hasObject("targetplayer")
                    && !scriptEntry.hasObject("targetnpc")) {
                scriptEntry.addObject("targetplayer", arg.asType(dPlayer.class));
            }
            else if (Depends.citizens != null && arg.matchesArgumentType(dNPC.class)
                    && !scriptEntry.hasObject("targetplayer")
                    && !scriptEntry.hasObject("targetnpc")) {
                scriptEntry.addObject("targetnpc", arg.asType(dNPC.class));
            }

            // Backwards compatibility
            else if (arg.matches("NPC")
                    && !scriptEntry.hasObject("targetplayer")
                    && !scriptEntry.hasObject("targetnpc")
                    && ((BukkitScriptEntryData) scriptEntry.entryData).hasNPC()) {
                scriptEntry.addObject("targetnpc", ((BukkitScriptEntryData) scriptEntry.entryData).getNPC());
            }
            else if (arg.matches("PLAYER")
                    && !scriptEntry.hasObject("targetplayer")
                    && !scriptEntry.hasObject("targetnpc")
                    && ((BukkitScriptEntryData) scriptEntry.entryData).hasPlayer()) {
                scriptEntry.addObject("targetplayer", ((BukkitScriptEntryData) scriptEntry.entryData).getPlayer());
            }
            else {
                arg.reportUnhandled();
            }

        }

        if (!scriptEntry.hasObject("targetplayer") &&
                !scriptEntry.hasObject("targetnpc")) {
            if (((BukkitScriptEntryData) scriptEntry.entryData).hasPlayer()) {
                scriptEntry.addObject("targetplayer", ((BukkitScriptEntryData) scriptEntry.entryData).getPlayer());
            }
            else if (((BukkitScriptEntryData) scriptEntry.entryData).hasNPC()) {
                scriptEntry.addObject("targetnpc", ((BukkitScriptEntryData) scriptEntry.entryData).getNPC());
            }
            else {
                throw new InvalidArgumentsException("Must specify a player!");
            }
        }

        if (!scriptEntry.hasObject("amount")) {
            scriptEntry.addObject("amount", new Element(9999)); // TODO: 9999?
        }
    }

    @Override
    public void execute(ScriptEntry scriptEntry) throws CommandExecutionException {

        dPlayer player = (dPlayer) scriptEntry.getObject("targetplayer");
        dNPC npc = (dNPC) scriptEntry.getObject("targetnpc");
        Element amount = scriptEntry.getElement("amount");

        dB.report(scriptEntry, getName(),
                (player == null ? "" : player.debug())
                        + (npc == null ? "" : npc.debug())
                        + amount.debug());

        if (npc != null) {
            if (!npc.getCitizen().hasTrait(HungerTrait.class)) {
                dB.echoError(scriptEntry.getResidingQueue(), "This NPC does not have the HungerTrait enabled! Use /trait hunger");
                return;
            }
            npc.getCitizen().getTrait(HungerTrait.class).feed(amount.asInt());
        }
        else if (player != null) {
            if (95999 - player.getPlayerEntity().getFoodLevel() < amount.asInt()) // Setting hunger too high = error
            {
                amount = new Element(95999 - player.getPlayerEntity().getFoodLevel());
            }
            player.getPlayerEntity().setFoodLevel(player.getPlayerEntity().getFoodLevel() + amount.asInt());
        }
        else {
            dB.echoError(scriptEntry.getResidingQueue(), "No target?"); // Mostly just here to quiet code analyzers.
        }
    }
}
