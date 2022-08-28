package com.denizenscript.denizen.scripts.commands.player;

import com.denizenscript.denizen.Denizen;
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
import com.denizenscript.denizencore.utilities.CoreUtilities;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AdvancementCommand extends AbstractCommand {

    public AdvancementCommand() {
        setName("advancement");
        setSyntax("advancement [id:<name>] (delete/grant:<players>/revoke:<players>/{create}) (parent:<name>) (icon:<item>) (title:<text>) (description:<text>) (background:<key>) (frame:<type>) (toast:<boolean>) (announce:<boolean>) (hidden:<boolean>) (x:<offset>) (y:<offset>) (progress_length:<#>)");
        setRequiredArguments(1, 14);
        isProcedural = false;
    }

    // <--[command]
    // @Name Advancement
    // @Syntax advancement [id:<name>] (delete/grant:<players>/revoke:<players>/{create}) (parent:<name>) (icon:<item>) (title:<text>) (description:<text>) (background:<key>) (frame:<type>) (toast:<boolean>) (announce:<boolean>) (hidden:<boolean>) (x:<offset>) (y:<offset>) (progress_length:<#>)
    // @Required 1
    // @Maximum 14
    // @Short Controls a custom advancement.
    // @Group player
    //
    // @Description
    // Controls custom Minecraft player advancements. You should generally create advancements manually on server start.
    // Currently, the ID argument may only refer to advancements added through this command.
    // The default action is to create and register a new advancement.
    // You may also delete an existing advancement, in which case do not provide any further arguments.
    // You may grant or revoke an advancement for a list of players, in which case do not provide any further arguments.
    // The parent argument sets the root advancement in the advancements menu, in the format "namespace:key".
    // If no namespace is specified, the parent is assumed to have been created through this command.
    // The icon argument sets the icon displayed in toasts and the advancements menu.
    // The title argument sets the title that will show on toasts and in the advancements menu.
    // The description argument sets the information that will show when scrolling over a chat announcement or in the advancements menu.
    // The background argument sets the image to use if the advancement goes to a new tab.
    // If the background is unspecified, defaults to "minecraft:textures/gui/advancements/backgrounds/stone.png".
    // The frame argument sets the type of advancement - valid arguments are CHALLENGE, GOAL, and TASK.
    // The toast argument sets whether the advancement should display a toast message when a player completes it. Default is true.
    // The announce argument sets whether the advancement should display a chat message to the server when a player completes it. Default is true.
    // The hidden argument sets whether the advancement should be hidden until it is completed.
    // The x and y arguments are offsets based on the size of an advancement icon in the menu. They are required for custom tabs to look reasonable.
    //
    // When creating an advancement, optionally specify 'progress_length' to make it require multiple parts.
    // When granting an advancement, optionally specify 'progress_length' to only grant partial progress.
    //
    // To award a pre-existing vanilla advancement, instead use <@link mechanism PlayerTag.award_advancement>
    //
    // WARNING: Failure to re-create advancements on every server start may result in loss of data - use <@link event server prestart>.
    //
    // If you mess with datapacks, you will also need to re-create advancements during <@link event server resources reloaded>
    //
    // @Tags
    // <PlayerTag.has_advancement[<advancement>]>
    // <PlayerTag.advancements>
    // <server.advancement_types>
    //
    // @Usage
    // Creates a new advancement that has a potato icon.
    // - advancement id:hello_world icon:baked_potato "title:Hello World" "description:You said hello to the world."
    //
    // @Usage
    // Creates a new advancement with the parent "hello_world" and a CHALLENGE frame. Hidden until it is completed.
    // - advancement id:hello_universe parent:hello_world icon:ender_pearl "title:Hello Universe" "description:You said hello to the UNIVERSE." frame:challenge hidden:true x:1
    //
    // @Usage
    // Grants the "hello_world" advancement to the current player.
    // - advancement id:hello_world grant:<player>
    //
    // -->

    @Override
    public void parseArgs(ScriptEntry scriptEntry) throws InvalidArgumentsException {
        for (Argument arg : scriptEntry) {
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
                scriptEntry.addObject("create", new ElementTag(true)); // unused, just to be explicit
            }
            else if (!scriptEntry.hasObject("delete")
                    && arg.matches("delete", "remove")) {
                scriptEntry.addObject("delete", new ElementTag(true));
            }
            else if (!scriptEntry.hasObject("grant")
                    && arg.matchesPrefix("grant", "give", "g")
                    && arg.matchesArgumentList(PlayerTag.class)) {
                scriptEntry.addObject("grant", arg.asType(ListTag.class));
            }
            else if (!scriptEntry.hasObject("revoke")
                    && arg.matchesPrefix("revoke", "take", "r")
                    && arg.matchesArgumentList(PlayerTag.class)) {
                scriptEntry.addObject("revoke", arg.asType(ListTag.class));
            }
            else if (!scriptEntry.hasObject("icon")
                    && arg.matchesPrefix("icon", "i")
                    && arg.matchesArgumentType(ItemTag.class)) {
                scriptEntry.addObject("icon", arg.asType(ItemTag.class));
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
                    && arg.matchesEnum(Advancement.Frame.class)) {
                scriptEntry.addObject("frame", arg.asElement());
            }
            else if (!scriptEntry.hasObject("toast")
                    && arg.matchesPrefix("toast", "show")
                    && arg.matchesBoolean()) {
                scriptEntry.addObject("toast", arg.asElement());
            }
            else if (!scriptEntry.hasObject("announce")
                    && arg.matchesPrefix("announce", "chat")
                    && arg.matchesBoolean()) {
                scriptEntry.addObject("announce", arg.asElement());
            }
            else if (!scriptEntry.hasObject("hidden")
                    && arg.matchesPrefix("hidden", "hide", "h")
                    && arg.matchesBoolean()) {
                scriptEntry.addObject("hidden", arg.asElement());
            }
            else if (!scriptEntry.hasObject("x")
                    && arg.matchesPrefix("x")
                    && arg.matchesFloat()) {
                scriptEntry.addObject("x", arg.asElement());
            }
            else if (!scriptEntry.hasObject("y")
                    && arg.matchesPrefix("y")
                    && arg.matchesFloat()) {
                scriptEntry.addObject("y", arg.asElement());
            }
            else if (!scriptEntry.hasObject("progress_length")
                    && arg.matchesPrefix("progress_length")
                    && arg.matchesInteger()) {
                scriptEntry.addObject("progress_length", arg.asElement());
            }
            else {
                arg.reportUnhandled();
            }
        }
        if (!scriptEntry.hasObject("id")) {
            throw new InvalidArgumentsException("Must specify an ID!");
        }
        scriptEntry.defaultObject("icon", new ItemTag(Material.AIR));
        scriptEntry.defaultObject("title", new ElementTag(""));
        scriptEntry.defaultObject("description", new ElementTag(""));
        scriptEntry.defaultObject("background", new ElementTag("minecraft:textures/gui/advancements/backgrounds/stone.png"));
        scriptEntry.defaultObject("frame", new ElementTag("TASK"));
        scriptEntry.defaultObject("toast", new ElementTag(true));
        scriptEntry.defaultObject("announce", new ElementTag(true));
        scriptEntry.defaultObject("hidden", new ElementTag(false));
        scriptEntry.defaultObject("x", new ElementTag(0f));
        scriptEntry.defaultObject("y", new ElementTag(0f));
    }

    public static final Map<NamespacedKey, Advancement> customRegistered = new HashMap<>();

    @Override
    public void execute(ScriptEntry scriptEntry) {
        ElementTag id = scriptEntry.getElement("id");
        ElementTag parent = scriptEntry.getElement("parent");
        ElementTag delete = scriptEntry.getElement("delete");
        ListTag grant = scriptEntry.getObjectTag("grant");
        ListTag revoke = scriptEntry.getObjectTag("revoke");
        ItemTag icon = scriptEntry.getObjectTag("icon");
        ElementTag title = scriptEntry.getElement("title");
        ElementTag description = scriptEntry.getElement("description");
        ElementTag background = scriptEntry.getElement("background");
        ElementTag frame = scriptEntry.getElement("frame");
        ElementTag toast = scriptEntry.getElement("toast");
        ElementTag announce = scriptEntry.getElement("announce");
        ElementTag hidden = scriptEntry.getElement("hidden");
        ElementTag x = scriptEntry.getElement("x");
        ElementTag y = scriptEntry.getElement("y");
        ElementTag progressLength = scriptEntry.getElement("progress_length");
        if (scriptEntry.dbCallShouldDebug()) {
            Debug.report(scriptEntry, name, id, parent, delete, grant, revoke, icon, title, description, background, progressLength, frame, toast, announce, hidden, x, y);
        }
        NamespacedKey key = new NamespacedKey(Denizen.getInstance(), id.asString());
        if (delete == null && grant == null && revoke == null) {
            NamespacedKey parentKey = null;
            NamespacedKey backgroundKey = null;
            if (parent != null) {
                List<String> split = CoreUtilities.split(parent.asString(), ':', 2);
                if (split.size() == 1) {
                    parentKey = new NamespacedKey(Denizen.getInstance(), split.get(0));
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
                    icon.getItemStack(), title.asString(), description.asString(),
                    backgroundKey, Advancement.Frame.valueOf(frame.asString().toUpperCase()),
                    toast.asBoolean(), announce.asBoolean(), hidden.asBoolean(), x.asFloat(), y.asFloat(), progressLength == null ? 1 : progressLength.asInt());
            NMSHandler.advancementHelper.register(advancement);
            customRegistered.put(key, advancement);
            return;
        }
        Advancement advancement = customRegistered.get(key);
        if (advancement == null) {
            Debug.echoError("Invalid advancement key '" + key + "': not registered. Are you sure you registered it using the 'advancement' command previously?"
                     + " Note that the 'advancement' command is not for vanilla advancements.");
            return;
        }
        if (delete != null) {
            NMSHandler.advancementHelper.unregister(advancement);
            customRegistered.remove(key);
        }
        else if (grant != null) {
            for (PlayerTag target : grant.filter(PlayerTag.class, scriptEntry)) {
                Player player = target.getPlayerEntity();
                if (player != null) {
                    if (progressLength == null) {
                        NMSHandler.advancementHelper.grant(advancement, player);
                    }
                    else {
                        NMSHandler.advancementHelper.grantPartial(advancement, player, progressLength.asInt());
                    }
                }
            }
        }
        else /*if (revoke != null)*/ {
            for (PlayerTag target : revoke.filter(PlayerTag.class, scriptEntry)) {
                Player player = target.getPlayerEntity();
                if (player != null) {
                    NMSHandler.advancementHelper.revoke(advancement, player);
                }
            }
        }
    }
}
