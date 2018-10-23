package net.aufdemrand.denizen.scripts.commands.player;

import net.aufdemrand.denizen.nms.NMSHandler;
import net.aufdemrand.denizen.nms.interfaces.AdvancementHelper;
import net.aufdemrand.denizen.nms.util.Advancement;
import net.aufdemrand.denizen.objects.dMaterial;
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
import net.aufdemrand.denizencore.utilities.CoreUtilities;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AdvancementCommand extends AbstractCommand {

    @Override
    public void parseArgs(ScriptEntry scriptEntry) throws InvalidArgumentsException {

        for (aH.Argument arg : aH.interpret(scriptEntry.getArguments())) {

            if (!scriptEntry.hasObject("id")
                    && arg.matchesPrefix("id")) {
                scriptEntry.addObject("id", arg.asElement());
            }
            else if (!scriptEntry.hasObject("parent")
                    && arg.matchesPrefix("parent")) {
                scriptEntry.addObject("parent", arg.asElement());
            }
            else if (!scriptEntry.hasObject("create")
                    && arg.matches("create")) {
                scriptEntry.addObject("create", new Element(true)); // unused, just to be explicit
            }
            else if (!scriptEntry.hasObject("delete")
                    && arg.matches("delete", "remove")) {
                scriptEntry.addObject("delete", new Element(true));
            }
            else if (!scriptEntry.hasObject("grant")
                    && arg.matchesPrefix("grant", "give", "g")
                    && arg.matchesArgumentList(dPlayer.class)) {
                scriptEntry.addObject("grant", arg.asType(dList.class));
            }
            else if (!scriptEntry.hasObject("revoke")
                    && arg.matchesPrefix("revoke", "take", "r")
                    && arg.matchesArgumentList(dPlayer.class)) {
                scriptEntry.addObject("revoke", arg.asType(dList.class));
            }
            else if (!scriptEntry.hasObject("icon")
                    && arg.matchesPrefix("icon", "i")
                    && arg.matchesArgumentType(dMaterial.class)) {
                scriptEntry.addObject("icon", arg.asType(dMaterial.class));
            }
            else if (!scriptEntry.hasObject("title")
                    && arg.matchesPrefix("title", "text", "t")) {
                scriptEntry.addObject("title", arg.asElement());
            }
            else if (!scriptEntry.hasObject("description")
                    && arg.matchesPrefix("description", "desc", "d")) {
                scriptEntry.addObject("description", arg.asElement());
            }
            else if (!scriptEntry.hasObject("background")
                    && arg.matchesPrefix("background", "bg")) {
                scriptEntry.addObject("background", arg.asElement());
            }
            else if (!scriptEntry.hasObject("frame")
                    && arg.matchesPrefix("frame", "f")
                    && arg.matchesEnum(Advancement.Frame.values())) {
                scriptEntry.addObject("frame", arg.asElement());
            }
            else if (!scriptEntry.hasObject("toast")
                    && arg.matchesPrefix("toast", "show")
                    && arg.matchesPrimitive(aH.PrimitiveType.Boolean)) {
                scriptEntry.addObject("toast", arg.asElement());
            }
            else if (!scriptEntry.hasObject("announce")
                    && arg.matchesPrefix("announce", "chat")
                    && arg.matchesPrimitive(aH.PrimitiveType.Boolean)) {
                scriptEntry.addObject("announce", arg.asElement());
            }
            else if (!scriptEntry.hasObject("hidden")
                    && arg.matchesPrefix("hidden", "hide", "h")
                    && arg.matchesPrimitive(aH.PrimitiveType.Boolean)) {
                scriptEntry.addObject("hidden", arg.asElement());
            }
            else if (!scriptEntry.hasObject("x")
                    && arg.matchesPrefix("x")
                    && arg.matchesPrimitive(aH.PrimitiveType.Float)) {
                scriptEntry.addObject("x", arg.asElement());
            }
            else if (!scriptEntry.hasObject("y")
                    && arg.matchesPrefix("y")
                    && arg.matchesPrimitive(aH.PrimitiveType.Float)) {
                scriptEntry.addObject("y", arg.asElement());
            }
            else {
                arg.reportUnhandled();
            }
        }

        if (!scriptEntry.hasObject("id")) {
            throw new InvalidArgumentsException("Must specify an ID!");
        }

        scriptEntry.defaultObject("icon", dMaterial.AIR);
        scriptEntry.defaultObject("title", new Element(""));
        scriptEntry.defaultObject("description", new Element(""));
        scriptEntry.defaultObject("background", new Element("minecraft:textures/gui/advancements/backgrounds/stone.png"));
        scriptEntry.defaultObject("frame", new Element("TASK"));
        scriptEntry.defaultObject("toast", new Element(true));
        scriptEntry.defaultObject("announce", new Element(true));
        scriptEntry.defaultObject("hidden", new Element(false));
        scriptEntry.defaultObject("x", new Element(0f));
        scriptEntry.defaultObject("y", new Element(0f));
    }

    public static final Map<NamespacedKey, Advancement> customRegistered = new HashMap<NamespacedKey, Advancement>();

    @Override
    public void execute(ScriptEntry scriptEntry) throws CommandExecutionException {

        Element id = scriptEntry.getElement("id");
        Element parent = scriptEntry.getElement("parent");
        Element delete = scriptEntry.getElement("delete");
        dList grant = scriptEntry.getdObject("grant");
        dList revoke = scriptEntry.getdObject("revoke");
        dMaterial icon = scriptEntry.getdObject("icon");
        Element title = scriptEntry.getElement("title");
        Element description = scriptEntry.getElement("description");
        Element background = scriptEntry.getElement("background");
        Element frame = scriptEntry.getElement("frame");
        Element toast = scriptEntry.getElement("toast");
        Element announce = scriptEntry.getElement("announce");
        Element hidden = scriptEntry.getElement("hidden");
        Element x = scriptEntry.getElement("x");
        Element y = scriptEntry.getElement("y");

        if (scriptEntry.dbCallShouldDebug()) {
            dB.report(scriptEntry, name, id.debug() + (parent != null ? parent.debug() : "")
                    + (delete != null ? delete.debug() : "") + (grant != null ? grant.debug() : "")
                    + (revoke != null ? revoke.debug() : "")
                    + icon.debug() + title.debug() + description.debug()
                    + (background != null ? background.debug() : "")
                    + frame.debug() + toast.debug() + announce.debug() + hidden.debug() + x.debug() + y.debug());
        }

        final AdvancementHelper advancementHelper = NMSHandler.getInstance().getAdvancementHelper();

        NamespacedKey key = new NamespacedKey(DenizenAPI.getCurrentInstance(), id.asString());

        if (delete == null && grant == null && revoke == null) {
            NamespacedKey parentKey = null;
            NamespacedKey backgroundKey = null;
            if (parent != null) {
                List<String> split = CoreUtilities.split(parent.asString(), ':', 2);
                if (split.size() == 1) {
                    parentKey = new NamespacedKey(DenizenAPI.getCurrentInstance(), split.get(0));
                }
                else {
                    parentKey = new NamespacedKey(CoreUtilities.toLowerCase(split.get(0)), CoreUtilities.toLowerCase(split.get(1)));
                }
            }
            else if (background != null) {
                List<String> backgroundSplit = CoreUtilities.split(background.asString(), ':', 2);
                if (backgroundSplit.size() == 1) {
                    backgroundKey = NamespacedKey.minecraft(backgroundSplit.get(0));
                }
                else {
                    backgroundKey = new NamespacedKey(CoreUtilities.toLowerCase(backgroundSplit.get(0)), CoreUtilities.toLowerCase(backgroundSplit.get(1)));
                }
            }

            final Advancement advancement = new Advancement(false, key, parentKey,
                    icon.getMaterial(), icon.getData(), title.asString(), description.asString(),
                    backgroundKey, Advancement.Frame.valueOf(frame.asString().toUpperCase()),
                    toast.asBoolean(), announce.asBoolean(), hidden.asBoolean(), x.asFloat(), y.asFloat());

            advancementHelper.register(advancement);
            customRegistered.put(key, advancement);
        }
        else if (delete != null) {
            advancementHelper.unregister(customRegistered.get(key));
            customRegistered.remove(key);
        }
        else if (grant != null) {
            Advancement advancement = customRegistered.get(key);
            for (dPlayer target : grant.filter(dPlayer.class)) {
                Player player = target.getPlayerEntity();
                if (player != null) {
                    advancementHelper.grant(advancement, player);
                }
            }
        }
        else /*if (revoke != null)*/ {
            Advancement advancement = customRegistered.get(key);
            for (dPlayer target : revoke.filter(dPlayer.class)) {
                Player player = target.getPlayerEntity();
                if (player != null) {
                    advancementHelper.revoke(advancement, player);
                }
            }
        }
    }
}
