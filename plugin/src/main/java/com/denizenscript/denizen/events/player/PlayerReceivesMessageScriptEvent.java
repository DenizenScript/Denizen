package com.denizenscript.denizen.events.player;

import com.denizenscript.denizen.objects.PlayerTag;
import com.denizenscript.denizen.utilities.implementation.BukkitScriptEntryData;
import com.denizenscript.denizen.events.BukkitScriptEvent;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.scripts.ScriptEntryData;
import com.denizenscript.denizencore.scripts.containers.ScriptContainer;
import com.denizenscript.denizencore.utilities.CoreUtilities;

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
    // <context.message> returns an ElementTag of the message.
    // <context.raw_json> returns an ElementTag of the raw JSON used for the message.
    // <context.system_message> returns true if the message is a system message (not player chat).
    //
    // @Determine
    // "MESSAGE:" + ElementTag to change the message.
    // "RAW_JSON:" + ElementTag to change the JSON used for the message.
    //
    // -->

    public PlayerReceivesMessageScriptEvent() {
        instance = this;
    }

    public static PlayerReceivesMessageScriptEvent instance;
    public ElementTag message;
    public ElementTag rawJson;
    public ElementTag system;
    public PlayerTag player;

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
    public boolean applyDetermination(ScriptPath path, ObjectTag determinationObj) {
        if (determinationObj instanceof ElementTag && !isDefaultDetermination(determinationObj)) {
            String determination = determinationObj.toString();
            String lower = CoreUtilities.toLowerCase(determination);
            if (lower.startsWith("message:")) {
                message = new ElementTag(determination.substring("message:".length()));
                messageModified = true;
                return true;
            }
            if (lower.startsWith("raw_json:")) {
                rawJson = new ElementTag(determination.substring("raw_json:".length()));
                rawJsonModified = true;
                return true;
            }
        }
        return super.applyDetermination(path, determinationObj);
    }

    @Override
    public ScriptEntryData getScriptEntryData() {
        return new BukkitScriptEntryData(player, null);
    }

    @Override
    public ObjectTag getContext(String name) {
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
