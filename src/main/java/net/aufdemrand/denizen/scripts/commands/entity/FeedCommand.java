package net.aufdemrand.denizen.scripts.commands.entity;

import net.aufdemrand.denizen.objects.Element;
import net.aufdemrand.denizen.objects.dNPC;
import net.aufdemrand.denizen.objects.dPlayer;

import net.aufdemrand.denizen.exceptions.CommandExecutionException;
import net.aufdemrand.denizen.exceptions.InvalidArgumentsException;
import net.aufdemrand.denizen.npc.traits.HungerTrait;
import net.aufdemrand.denizen.scripts.ScriptEntry;
import net.aufdemrand.denizen.scripts.commands.AbstractCommand;
import net.aufdemrand.denizen.objects.aH;
import net.aufdemrand.denizen.utilities.debugging.dB;

/**
 * Feeds a (Player) entity.
 *
 * @author Jeremy Schroeder, Mason Adkins
 */

public class FeedCommand extends AbstractCommand {

    @Override
    public void parseArgs(ScriptEntry scriptEntry) throws InvalidArgumentsException {

        for (aH.Argument arg : aH.interpret(scriptEntry.getArguments())) {
            if (arg.matchesPrimitive(aH.PrimitiveType.Integer)
                    && !scriptEntry.hasObject("amount"))
                scriptEntry.addObject("amount", arg.asElement());

            else if (arg.matchesArgumentType(dPlayer.class)
                    && !scriptEntry.hasObject("targetplayer")
                    && !scriptEntry.hasObject("targetnpc"))
                scriptEntry.addObject("targetplayer", arg.asType(dPlayer.class));

            else if (arg.matchesArgumentType(dNPC.class)
                    && !scriptEntry.hasObject("targetplayer")
                    && !scriptEntry.hasObject("targetnpc"))
                scriptEntry.addObject("targetnpc", arg.asType(dNPC.class));

            // Backwards compatibility
            else if (arg.matches("NPC")
                    && !scriptEntry.hasObject("targetplayer")
                    && !scriptEntry.hasObject("targetnpc")
                    && scriptEntry.hasNPC())
                scriptEntry.addObject("targetnpc", scriptEntry.getNPC());

            else if (arg.matches("PLAYER")
                    && !scriptEntry.hasObject("targetplayer")
                    && !scriptEntry.hasObject("targetnpc")
                    && scriptEntry.hasPlayer())
                scriptEntry.addObject("targetplayer", scriptEntry.getPlayer());

            else arg.reportUnhandled();

        }

        if (!scriptEntry.hasObject("targetplayer") &&
                !scriptEntry.hasObject("targetnpc")) {
            if (scriptEntry.hasPlayer())
                scriptEntry.addObject("targetplayer", scriptEntry.getPlayer());
            else if (scriptEntry.hasNPC())
                scriptEntry.addObject("targetnpc", scriptEntry.getNPC());
            else
                throw new InvalidArgumentsException("Must specify a player!");
        }

        if (!scriptEntry.hasObject("amount"))
            scriptEntry.addObject("amount", new Element(9999)); // TODO: 9999?
    }

    @Override
    public void execute(ScriptEntry scriptEntry) throws CommandExecutionException {

        dPlayer player = (dPlayer) scriptEntry.getObject("targetplayer");
        dNPC npc = (dNPC) scriptEntry.getObject("targetnpc");
        Element amount = scriptEntry.getElement("amount");

        dB.report(scriptEntry, getName(),
                (player == null?"": player.debug())
                +(npc == null?"":npc.debug())
                +amount.debug());

        if (npc != null) {
            if (!npc.getCitizen().hasTrait(HungerTrait.class)) {
                dB.echoError("This NPC does not have the HungerTrait enabled! Use /trait hunger");
                return;
            }
            npc.getCitizen().getTrait(HungerTrait.class).feed(amount.asInt());
        }
        else if (player != null) {
            if (95999 - player.getPlayerEntity().getFoodLevel() < amount.asInt()) // Setting hunger too high = error
                amount = new Element(95999 - player.getPlayerEntity().getFoodLevel());
            player.getPlayerEntity().setFoodLevel(player.getPlayerEntity().getFoodLevel() + amount.asInt());
        }
        else {
            dB.echoError("No target?"); // Mostly just here to quiet code analyzers.
        }
    }
}
