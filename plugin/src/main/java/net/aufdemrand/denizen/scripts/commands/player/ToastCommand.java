package net.aufdemrand.denizen.scripts.commands.player;

import net.aufdemrand.denizen.BukkitScriptEntryData;
import net.aufdemrand.denizen.nms.NMSHandler;
import net.aufdemrand.denizen.nms.interfaces.AdvancementHelper;
import net.aufdemrand.denizen.nms.util.Advancement;
import net.aufdemrand.denizen.objects.dItem;
import net.aufdemrand.denizen.objects.dPlayer;
import net.aufdemrand.denizen.utilities.DenizenAPI;
import net.aufdemrand.denizen.utilities.debugging.dB;
import net.aufdemrand.denizencore.exceptions.CommandExecutionException;
import net.aufdemrand.denizencore.exceptions.InvalidArgumentsException;
import net.aufdemrand.denizencore.objects.Element;
import net.aufdemrand.denizencore.objects.aH;
import net.aufdemrand.denizencore.objects.dList;
import net.aufdemrand.denizencore.scripts.ScriptEntry;
import net.aufdemrand.denizencore.scripts.commands.AbstractCommand;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class ToastCommand extends AbstractCommand {

    @Override
    public void parseArgs(ScriptEntry scriptEntry) throws InvalidArgumentsException {

        for (aH.Argument arg : aH.interpret(scriptEntry.getArguments())) {

            if (!scriptEntry.hasObject("targets")
                    && arg.matchesPrefix("target", "targets", "t")
                    && arg.matchesArgumentList(dPlayer.class)) {
                scriptEntry.addObject("targets", arg.asType(dList.class).filter(dPlayer.class));
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

    }

    private static final NamespacedKey DEFAULT_BACKGROUND = NamespacedKey.minecraft("textures/gui/advancements/backgrounds/adventure.png");

    @SuppressWarnings("unchecked")
    @Override
    public void execute(ScriptEntry scriptEntry) throws CommandExecutionException {
        Element text = scriptEntry.getElement("text");
        Element frame = scriptEntry.getElement("frame");
        dItem icon = scriptEntry.getdObject("icon");
        final List<dPlayer> targets = (List<dPlayer>) scriptEntry.getObject("targets");

        if (scriptEntry.dbCallShouldDebug()) {
            dB.report(scriptEntry, name, text.debug() + frame.debug() + icon.debug() + aH.debugList("targets", targets));
        }

        final Advancement advancement = new Advancement(true,
                new NamespacedKey(DenizenAPI.getCurrentInstance(), UUID.randomUUID().toString()), null,
                icon.getItemStack(), text.asString(), "", DEFAULT_BACKGROUND,
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
