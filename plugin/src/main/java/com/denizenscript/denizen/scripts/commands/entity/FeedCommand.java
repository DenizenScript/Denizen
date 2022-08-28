package com.denizenscript.denizen.scripts.commands.entity;

import com.denizenscript.denizen.utilities.Utilities;
import com.denizenscript.denizencore.utilities.debugging.Debug;
import com.denizenscript.denizen.utilities.depends.Depends;
import com.denizenscript.denizen.npc.traits.HungerTrait;
import com.denizenscript.denizen.objects.NPCTag;
import com.denizenscript.denizen.objects.PlayerTag;
import com.denizenscript.denizencore.exceptions.InvalidArgumentsException;
import com.denizenscript.denizencore.objects.Argument;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.scripts.ScriptEntry;
import com.denizenscript.denizencore.scripts.commands.AbstractCommand;

public class FeedCommand extends AbstractCommand {

    public FeedCommand() {
        setName("feed");
        setSyntax("feed (<entity>) (amount:<#>) (saturation:<#.#>)");
        setRequiredArguments(0, 3);
        isProcedural = false;
    }

    // <--[command]
    // @Name Feed
    // @Syntax feed (<entity>) (amount:<#>) (saturation:<#.#>)
    // @Required 0
    // @Maximum 3
    // @Short Feed the player or npc.
    // @Group entity
    //
    // @Description
    // Feeds the player or npc specified.
    //
    // By default targets the player attached to the script queue and feeds a full amount.
    //
    // Accepts the 'amount:' argument, which is in half bar increments, up to a total of 20 food points.
    // The amount may be negative, to cause hunger instead of satiating it.
    //
    // You can optionally also specify an amount to change the saturation by.
    // By default, the saturation change will be the same as the food level change.
    // This is also up to a total of 20 points. This value may also be negative.
    //
    // Also accepts the 'target:<entity>' argument to specify the entity which will be fed the amount.
    //
    // @Tags
    // <PlayerTag.food_level>
    // <PlayerTag.formatted_food_level>
    // <PlayerTag.saturation>
    //
    // @Usage
    // Use to feed the player for 5 foodpoints (or 2.5 bars).
    // - feed amount:5
    //
    // @Usage
    // Use to feed an NPC for 10 foodpoints (or 5 bars).
    // - feed <npc> amount:10
    //
    // @Usage
    // Use to feed a player from a definition fully without refilling saturation.
    // - feed <[player]> saturation:0
    // -->

    @Override
    public void parseArgs(ScriptEntry scriptEntry) throws InvalidArgumentsException {
        for (Argument arg : scriptEntry) {
            if (arg.matchesInteger()
                    && arg.matchesPrefix("amount", "amt", "quantity", "qty", "a", "q")
                    && !scriptEntry.hasObject("amount")) {
                scriptEntry.addObject("amount", arg.asElement());
            }
            else if (arg.matchesInteger()
                    && arg.matchesPrefix("saturation", "sat", "s")
                    && !scriptEntry.hasObject("saturation")) {
                scriptEntry.addObject("saturation", arg.asElement());
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
            else if (arg.matches("npc")
                    && !scriptEntry.hasObject("targetplayer")
                    && !scriptEntry.hasObject("targetnpc")
                    && Utilities.entryHasNPC(scriptEntry)) {
                scriptEntry.addObject("targetnpc", Utilities.getEntryNPC(scriptEntry));
            }
            else if (arg.matches("player")
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
        scriptEntry.defaultObject("amount", new ElementTag(20));
        scriptEntry.defaultObject("saturation", scriptEntry.getObject("amount"));
    }

    @Override
    public void execute(ScriptEntry scriptEntry) {
        PlayerTag player = scriptEntry.getObjectTag("targetplayer");
        NPCTag npc = scriptEntry.getObjectTag("targetnpc");
        ElementTag amount = scriptEntry.getElement("amount");
        ElementTag saturation = scriptEntry.getElement("saturation");
        if (scriptEntry.dbCallShouldDebug()) {
            Debug.report(scriptEntry, getName(), player, npc, amount, saturation);
        }
        if (npc != null) {
            if (!npc.getCitizen().hasTrait(HungerTrait.class)) {
                Debug.echoError(scriptEntry, "This NPC does not have the HungerTrait enabled! Use /trait hunger");
                return;
            }
            npc.getCitizen().getOrAddTrait(HungerTrait.class).feed(amount.asInt());
        }
        else {
            int result = Math.max(0, Math.min(20, player.getPlayerEntity().getFoodLevel() + amount.asInt()));
            player.getPlayerEntity().setFoodLevel(result);
            float satResult = Math.max(0, Math.min(20, player.getPlayerEntity().getSaturation() + saturation.asFloat()));
            player.getPlayerEntity().setSaturation(satResult);
            Debug.echoDebug(scriptEntry, "Player food level updated to " + result + " food and " +  satResult + " saturation.");
        }
    }
}
