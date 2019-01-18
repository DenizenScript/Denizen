package net.aufdemrand.denizen.events.player;

import net.aufdemrand.denizen.BukkitScriptEntryData;
import net.aufdemrand.denizen.events.BukkitScriptEvent;
import net.aufdemrand.denizen.objects.dPlayer;
import net.aufdemrand.denizencore.objects.Element;
import net.aufdemrand.denizencore.objects.dObject;
import net.aufdemrand.denizencore.scripts.ScriptEntryData;
import net.aufdemrand.denizencore.scripts.containers.ScriptContainer;
import net.aufdemrand.denizencore.utilities.CoreUtilities;

public class PlayerReceivesMessageScriptEvent extends BukkitScriptEvent {

    // <--[event]
    // @Events
    // player receives message
    //
    // @Regex ^on player receives message$
    //
    // @Cancellable true
    //
    // @Warning Using this will forcibly sync the chat thread.
    //
    // @Triggers when a player receives any chat message from the server.
    //
    // @Context
    // <context.message> returns an Element of the message.
    // <context.raw_json> returns an Element of the raw JSON used for the message.
    // <context.system_message> returns true if the message is a system message (not player chat).
    //
    // @Determine
    // "MESSAGE:" + Element to change the message.
    // "RAW_JSON:" + Element to change the JSON used for the message.
    //
    // -->

    public PlayerReceivesMessageScriptEvent() {
        instance = this;
    }

    public static PlayerReceivesMessageScriptEvent instance;
    public Element message;
    public Element rawJson;
    public Element system;
    public dPlayer player;

    public boolean messageModified;
    public boolean rawJsonModified;

    public boolean loaded;

    @Override
    public boolean couldMatch(ScriptContainer scriptContainer, String s) {
        return CoreUtilities.toLowerCase(s).startsWith("player receives message");
    }

    @Override
    public boolean matches(ScriptPath path) {
        return true;
    }

    @Override
    public String getName() {
        return "PlayerReceivesMessage";
    }

    @Override
    public void init() {
        loaded = true;
    }

    @Override
    public void destroy() {
        loaded = false;
    }

    @Override
    public boolean applyDetermination(ScriptContainer container, String determination) {
        String lower = CoreUtilities.toLowerCase(determination);
        if (lower.startsWith("message:")) {
            message = new Element(determination.substring("message:".length()));
            messageModified = true;
            return true;
        }
        if (lower.startsWith("raw_json:")) {
            rawJson = new Element(determination.substring("raw_json:".length()));
            rawJsonModified = true;
            return true;
        }
        return super.applyDetermination(container, determination);
    }

    @Override
    public ScriptEntryData getScriptEntryData() {
        return new BukkitScriptEntryData(player, null);
    }

    @Override
    public dObject getContext(String name) {
        if (name.equals("message")) {
            return message;
        }
        if (name.equals("raw_json")) {
            return rawJson;
        }
        if (name.equals("system_message")) {
            return system;
        }
        return super.getContext(name);
    }
}
