package com.denizenscript.denizen.scripts.commands.entity;

import com.denizenscript.denizen.utilities.Utilities;
import com.denizenscript.denizen.utilities.debugging.Debug;
import com.denizenscript.denizen.utilities.depends.Depends;
import com.denizenscript.denizen.npc.traits.HungerTrait;
import com.denizenscript.denizen.objects.NPCTag;
import com.denizenscript.denizen.objects.PlayerTag;
import com.denizenscript.denizencore.exceptions.InvalidArgumentsException;
import com.denizenscript.denizencore.objects.Argument;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.ArgumentHelper;
import com.denizenscript.denizencore.scripts.ScriptEntry;
import com.denizenscript.denizencore.scripts.commands.AbstractCommand;

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
    // <PlayerTag.food_level>
    // <PlayerTag.food_level.formatted>
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

        for (Argument arg : ArgumentHelper.interpretArguments(scriptEntry.aHArgs)) {
            if (arg.matchesPrimitive(ArgumentHelper.PrimitiveType.Integer)
                    && !scriptEntry.hasObject("amount")) {
                scriptEntry.addObject("amount", arg.asElement());
            }
            else if (arg.matchesArgumentType(PlayerTag.class)
                    && !scriptEntry.hasObject("targetplayer")
                    && !scriptEntry.hasObject("targetnpc")) {
                scriptEntry.addObject("targetplayer", arg.asType(PlayerTag.class));
            }
            else if (Depends.citizens != null && arg.matchesArgumentType(NPCTag.class)
                    && !scriptEntry.hasObject("targetplayer")
                    && !scriptEntry.hasObject("targetnpc")) {
                scriptEntry.addObject("targetnpc", arg.asType(NPCTag.class));
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
            scriptEntry.addObject("amount", new ElementTag(9999)); // TODO: 9999?
        }
    }

    @Override
    public void execute(ScriptEntry scriptEntry) {

        PlayerTag player = (PlayerTag) scriptEntry.getObject("targetplayer");
        NPCTag npc = (NPCTag) scriptEntry.getObject("targetnpc");
        ElementTag amount = scriptEntry.getElement("amount");

        if (scriptEntry.dbCallShouldDebug()) {

            Debug.report(scriptEntry, getName(),
                    (player == null ? "" : player.debug())
                            + (npc == null ? "" : npc.debug())
                            + amount.debug());

        }

        if (npc != null) {
            if (!npc.getCitizen().hasTrait(HungerTrait.class)) {
                Debug.echoError(scriptEntry.getResidingQueue(), "This NPC does not have the HungerTrait enabled! Use /trait hunger");
                return;
            }
            npc.getCitizen().getTrait(HungerTrait.class).feed(amount.asInt());
        }
        else if (player != null) {
            if (95999 - player.getPlayerEntity().getFoodLevel() < amount.asInt()) // Setting hunger too high = error
            {
                amount = new ElementTag(95999 - player.getPlayerEntity().getFoodLevel());
            }
            player.getPlayerEntity().setFoodLevel(player.getPlayerEntity().getFoodLevel() + amount.asInt());
        }
        else {
            Debug.echoError(scriptEntry.getResidingQueue(), "No target?"); // Mostly just here to quiet code analyzers.
        }
    }
}
