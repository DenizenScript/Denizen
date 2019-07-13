package com.denizenscript.denizen.scripts.commands.player;

import com.denizenscript.denizen.utilities.DenizenAPI;
import com.denizenscript.denizen.utilities.debugging.dB;
import com.denizenscript.denizen.BukkitScriptEntryData;
import com.denizenscript.denizen.nms.NMSHandler;
import com.denizenscript.denizen.nms.interfaces.AdvancementHelper;
import com.denizenscript.denizen.nms.util.Advancement;
import com.denizenscript.denizen.objects.dItem;
import com.denizenscript.denizen.objects.dPlayer;
import com.denizenscript.denizencore.exceptions.InvalidArgumentsException;
import com.denizenscript.denizencore.objects.Argument;
import com.denizenscript.denizencore.objects.Element;
import com.denizenscript.denizencore.objects.ArgumentHelper;
import com.denizenscript.denizencore.objects.dList;
import com.denizenscript.denizencore.scripts.ScriptEntry;
import com.denizenscript.denizencore.scripts.commands.AbstractCommand;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class ToastCommand extends AbstractCommand {

    // <--[command]
    // @Name Toast
    // @Syntax toast [<text>] (targets:<player>|...) (icon:<item>) (frame:{task}/challenge/goal) (background:<texture>)
    // @Required 1
    // @Short Shows the player a custom advancement toast.
    // @Group player
    //
    // @Description
    // Displays a client-side custom advancement "toast" notification popup to the player(s).
    // If no target is specified it will default to the attached player.
    // The icon argument changes the icon displayed in the toast pop-up notification.
    // The frame argument changes the type of advancement.
    // The background texture can be specified as a file path with an optional namespace key prefix.
    // By default, the background texture is "minecraft:textures/gui/advancements/backgrounds/adventure.png"
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

        for (Argument arg : ArgumentHelper.interpretArguments(scriptEntry.aHArgs)) {

            if (!scriptEntry.hasObject("targets")
                    && arg.matchesPrefix("target", "targets", "t")
                    && arg.matchesArgumentList(dPlayer.class)) {
                scriptEntry.addObject("targets", arg.asType(dList.class).filter(dPlayer.class, scriptEntry));
            }
            else if (!scriptEntry.hasObject("icon")
                    && arg.matchesPrefix("icon", "i")
                    && arg.matchesArgumentType(dItem.class)) {
                scriptEntry.addObject("icon", arg.asType(dItem.class));
            }
            else if (!scriptEntry.hasObject("frame")
                    && arg.matchesPrefix("frame", "f")
                    && arg.matchesEnum(Advancement.Frame.values())) {
                scriptEntry.addObject("frame", arg.asElement());
            }
            else if (!scriptEntry.hasObject("background")
                    && arg.matchesOnePrefix("background")) {
                scriptEntry.addObject("background", arg.asElement());
            }
            else if (!scriptEntry.hasObject("text")) {
                scriptEntry.addObject("text", new Element(arg.raw_value));
            }
            else {
                arg.reportUnhandled();
            }
        }

        if (!scriptEntry.hasObject("text")) {
            throw new InvalidArgumentsException("Must specify a message!");
        }

        if (!scriptEntry.hasObject("targets")) {
            BukkitScriptEntryData data = (BukkitScriptEntryData) scriptEntry.entryData;
            if (!data.hasPlayer()) {
                throw new InvalidArgumentsException("Must specify valid player targets!");
            }
            else {
                scriptEntry.addObject("targets", Collections.singletonList(data.getPlayer()));
            }
        }

        scriptEntry.defaultObject("icon", new dItem(Material.AIR));
        scriptEntry.defaultObject("frame", new Element("TASK"));
        scriptEntry.defaultObject("background", new Element("textures/gui/advancements/backgrounds/adventure.png"));
    }

    @SuppressWarnings("unchecked")
    @Override
    public void execute(ScriptEntry scriptEntry) {
        Element text = scriptEntry.getElement("text");
        Element frame = scriptEntry.getElement("frame");
        Element background = scriptEntry.getElement("background");
        dItem icon = scriptEntry.getdObject("icon");
        final List<dPlayer> targets = (List<dPlayer>) scriptEntry.getObject("targets");

        if (scriptEntry.dbCallShouldDebug()) {
            dB.report(scriptEntry, name, text.debug() + frame.debug() + icon.debug()
                    + background.debug() + ArgumentHelper.debugList("targets", targets));
        }

        NamespacedKey backgroundKey;
        int index = background.asString().indexOf(':');
        if (index == -1) {
            backgroundKey = NamespacedKey.minecraft(background.asString());
        }
        else {
            backgroundKey = new NamespacedKey(background.asString().substring(0, index), background.asString().substring(index + 1));
        }

        final Advancement advancement = new Advancement(true,
                new NamespacedKey(DenizenAPI.getCurrentInstance(), UUID.randomUUID().toString()), null,
                icon.getItemStack(), text.asString(), "", backgroundKey,
                Advancement.Frame.valueOf(frame.asString().toUpperCase()), true, false, true, 0, 0);

        final AdvancementHelper advancementHelper = NMSHandler.getInstance().getAdvancementHelper();

        for (dPlayer target : targets) {
            Player player = target.getPlayerEntity();
            if (player != null) {
                advancementHelper.grant(advancement, player);
                advancementHelper.revoke(advancement, player);
            }
        }
    }
}
