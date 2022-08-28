package com.denizenscript.denizen.scripts.commands.player;

import com.denizenscript.denizen.Denizen;
import com.denizenscript.denizen.utilities.Utilities;
import com.denizenscript.denizencore.utilities.debugging.Debug;
import com.denizenscript.denizen.nms.NMSHandler;
import com.denizenscript.denizen.nms.util.Advancement;
import com.denizenscript.denizen.objects.ItemTag;
import com.denizenscript.denizen.objects.PlayerTag;
import com.denizenscript.denizencore.exceptions.InvalidArgumentsException;
import com.denizenscript.denizencore.objects.Argument;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.core.ListTag;
import com.denizenscript.denizencore.scripts.ScriptEntry;
import com.denizenscript.denizencore.scripts.commands.AbstractCommand;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class ToastCommand extends AbstractCommand {

    public ToastCommand() {
        setName("toast");
        setSyntax("toast [<text>] (targets:<player>|...) (icon:<item>) (frame:{task}/challenge/goal)");
        setRequiredArguments(1, 4);
        isProcedural = false;
    }

    // <--[command]
    // @Name Toast
    // @Syntax toast [<text>] (targets:<player>|...) (icon:<item>) (frame:{task}/challenge/goal)
    // @Required 1
    // @Maximum 4
    // @Short Shows the player a custom advancement toast.
    // @Group player
    //
    // @Description
    // Displays a client-side custom advancement "toast" notification popup to the player(s).
    // If no target is specified it will default to the attached player.
    // The icon argument changes the icon displayed in the toast pop-up notification.
    // The frame argument changes the type of advancement.
    //
    // @Tags
    // None
    //
    // @Usage
    // Welcomes the player with an advancement toast.
    // - toast "Welcome <player.name>!"
    //
    // @Usage
    // Sends the player an advancement toast with a custom icon.
    // - toast "Diggy Diggy Hole" icon:iron_spade
    //
    // @Usage
    // Sends the player a "Challenge Complete!" type advancement toast.
    // - toast "You finished a challenge!" frame:challenge icon:diamond
    //
    // -->

    @Override
    public void parseArgs(ScriptEntry scriptEntry) throws InvalidArgumentsException {
        for (Argument arg : scriptEntry) {
            if (!scriptEntry.hasObject("targets")
                    && arg.matchesPrefix("target", "targets", "t")
                    && arg.matchesArgumentList(PlayerTag.class)) {
                scriptEntry.addObject("targets", arg.asType(ListTag.class).filter(PlayerTag.class, scriptEntry));
            }
            else if (!scriptEntry.hasObject("icon")
                    && arg.matchesPrefix("icon", "i")
                    && arg.matchesArgumentType(ItemTag.class)) {
                scriptEntry.addObject("icon", arg.asType(ItemTag.class));
            }
            else if (!scriptEntry.hasObject("frame")
                    && arg.matchesPrefix("frame", "f")
                    && arg.matchesEnum(Advancement.Frame.class)) {
                scriptEntry.addObject("frame", arg.asElement());
            }
            else if (!scriptEntry.hasObject("text")) {
                scriptEntry.addObject("text", arg.getRawElement());
            }
            else {
                arg.reportUnhandled();
            }
        }
        if (!scriptEntry.hasObject("text")) {
            throw new InvalidArgumentsException("Must specify a message!");
        }
        if (!scriptEntry.hasObject("targets")) {
            if (!Utilities.entryHasPlayer(scriptEntry)) {
                throw new InvalidArgumentsException("Must specify valid player targets!");
            }
            else {
                scriptEntry.addObject("targets", Collections.singletonList(Utilities.getEntryPlayer(scriptEntry)));
            }
        }
        scriptEntry.defaultObject("icon", new ItemTag(Material.AIR));
        scriptEntry.defaultObject("frame", new ElementTag("TASK"));
    }

    @Override
    public void execute(ScriptEntry scriptEntry) {
        ElementTag text = scriptEntry.getElement("text");
        ElementTag frame = scriptEntry.getElement("frame");
        ItemTag icon = scriptEntry.getObjectTag("icon");
        final List<PlayerTag> targets = (List<PlayerTag>) scriptEntry.getObject("targets");
        if (scriptEntry.dbCallShouldDebug()) {
            Debug.report(scriptEntry, name, text, frame, icon, db("targets", targets));
        }
        final Advancement advancement = new Advancement(true,
                new NamespacedKey(Denizen.getInstance(), UUID.randomUUID().toString()), null,
                icon.getItemStack(), text.asString(), "", null,
                Advancement.Frame.valueOf(frame.asString().toUpperCase()), true, false, true, 0, 0, 1);
        for (PlayerTag target : targets) {
            Player player = target.getPlayerEntity();
            if (player != null) {
                NMSHandler.advancementHelper.grant(advancement, player);
                NMSHandler.advancementHelper.revoke(advancement, player);
            }
        }
    }
}
