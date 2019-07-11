package net.aufdemrand.denizen.scripts.commands.entity;

import net.aufdemrand.denizen.npc.traits.HungerTrait;
import net.aufdemrand.denizen.objects.dNPC;
import net.aufdemrand.denizen.objects.dPlayer;
import net.aufdemrand.denizen.utilities.Utilities;
import net.aufdemrand.denizen.utilities.debugging.dB;
import net.aufdemrand.denizen.utilities.depends.Depends;
import net.aufdemrand.denizencore.exceptions.InvalidArgumentsException;
import net.aufdemrand.denizencore.objects.Element;
import net.aufdemrand.denizencore.objects.aH;
import net.aufdemrand.denizencore.scripts.ScriptEntry;
import net.aufdemrand.denizencore.scripts.commands.AbstractCommand;

public class FeedCommand extends AbstractCommand {

    // <--[command]
    // @Name Feed
    // @Syntax feed (amount:<#>) (target:<entity>)
    // @Required 0
    // @Short Feed the player or npc.
    // @Group entity
    //
    // @Description
    // Feeds the player or npc specified. By default targets the player attached to the script queue and feeds
    // a full amount. Accepts the 'amount:' argument, which is in half bar increments, for a total of 20 food
    // points. Also accepts the 'target:<entity>' argument to specify the entity which will be fed the amount.
    // NOTE: This command is outdated and bound to be updated.
    //
    // @Tags
    // <p@player.food_level>
    // <p@player.food_level.formatted>
    //
    // @Usage
    // Use to feed the player for 5 foodpoints or 2.5 bars.
    // - feed amount:5
    //
    // @Usage
    // Use to feed an npc with id 5 for 10 foodpoints or 5 bars.
    // - feed amount:10 target:n@5
    // -->

    @Override
    public void parseArgs(ScriptEntry scriptEntry) throws InvalidArgumentsException {

        for (aH.Argument arg : aH.interpretArguments(scriptEntry.aHArgs)) {
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
                    && Utilities.entryHasNPC(scriptEntry)) {
                scriptEntry.addObject("targetnpc", Utilities.getEntryNPC(scriptEntry));
            }
            else if (arg.matches("PLAYER")
                    && !scriptEntry.hasObject("targetplayer")
                    && !scriptEntry.hasObject("targetnpc")
                    && Utilities.entryHasPlayer(scriptEntry)) {
                scriptEntry.addObject("targetplayer", Utilities.getEntryPlayer(scriptEntry));
            }
            else {
                arg.reportUnhandled();
            }

        }

        if (!scriptEntry.hasObject("targetplayer") &&
                !scriptEntry.hasObject("targetnpc")) {
            if (Utilities.entryHasPlayer(scriptEntry)) {
                scriptEntry.addObject("targetplayer", Utilities.getEntryPlayer(scriptEntry));
            }
            else if (Utilities.entryHasNPC(scriptEntry)) {
                scriptEntry.addObject("targetnpc", Utilities.getEntryNPC(scriptEntry));
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
    public void execute(ScriptEntry scriptEntry) {

        dPlayer player = (dPlayer) scriptEntry.getObject("targetplayer");
        dNPC npc = (dNPC) scriptEntry.getObject("targetnpc");
        Element amount = scriptEntry.getElement("amount");

        if (scriptEntry.dbCallShouldDebug()) {

            dB.report(scriptEntry, getName(),
                    (player == null ? "" : player.debug())
                            + (npc == null ? "" : npc.debug())
                            + amount.debug());

        }

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
