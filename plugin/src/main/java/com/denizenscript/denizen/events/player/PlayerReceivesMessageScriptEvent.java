package com.denizenscript.denizen.events.player;

import com.denizenscript.denizen.objects.PlayerTag;
import com.denizenscript.denizen.utilities.FormattedTextHelper;
import com.denizenscript.denizen.utilities.implementation.BukkitScriptEntryData;
import com.denizenscript.denizen.events.BukkitScriptEvent;
import com.denizenscript.denizen.utilities.packets.NetworkInterceptHelper;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.scripts.ScriptEntryData;
import com.denizenscript.denizencore.utilities.CoreUtilities;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.chat.ComponentSerializer;

public class PlayerReceivesMessageScriptEvent extends BukkitScriptEvent {

    // <--[event]
    // @Events
    // player receives message
    //
    // @Regex ^on player receives message$
    //
    // @Group Player
    //
    // @Cancellable true
    //
    // @Warning Using this will forcibly sync the chat thread.
    //
    // @Triggers when a player receives any chat message from the server. This does not normally include *player* chat, instead prefer <@link event player chats> for that.
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
    // @Player Always.
    //
    // -->

    public PlayerReceivesMessageScriptEvent() {
        instance = this;
    }

    public static PlayerReceivesMessageScriptEvent instance;
    public ElementTag message;
    public ElementTag rawJson;
    public boolean didModify;
    public BaseComponent[] altMessageDetermination;
    public ElementTag system;
    public boolean modified;
    public PlayerTag player;
    public boolean loaded;

    public void reset() {
        player = null;
        message = null;
        rawJson = null;
        system = null;
        cancelled = false;
        modified = false;
        altMessageDetermination = null;
        didModify = false;
    }

    @Override
    public boolean couldMatch(ScriptPath path) {
        return path.eventLower.startsWith("player receives message");
    }

    @Override
    public void init() {
        NetworkInterceptHelper.enable();
        loaded = true;
    }

    @Override
    public void destroy() {
        loaded = false;
    }

    @Override
    public boolean applyDetermination(ScriptPath path, ObjectTag determinationObj) {
        if (determinationObj instanceof ElementTag) {
            String determination = determinationObj.toString();
            String lower = CoreUtilities.toLowerCase(determination);
            if (lower.startsWith("message:")) {
                message = new ElementTag(determination.substring("message:".length()), true);
                altMessageDetermination = FormattedTextHelper.parse(message.asString(), ChatColor.WHITE);
                modified = true;
                return true;
            }
            if (lower.startsWith("raw_json:")) {
                rawJson = new ElementTag(determination.substring("raw_json:".length()));
                altMessageDetermination = null;
                message = new ElementTag(FormattedTextHelper.stringify(ComponentSerializer.parse(rawJson.asString())), true);
                modified = true;
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
        switch (name) {
            case "message": return message;
            case "system_message": return system;
            case "raw_json":
                if (altMessageDetermination != null) {
                    return new ElementTag(ComponentSerializer.toString(altMessageDetermination), true);
                }
                return rawJson;
        }
        return super.getContext(name);
    }

    public PlayerReceivesMessageScriptEvent triggerNow() {
        PlayerReceivesMessageScriptEvent event = (PlayerReceivesMessageScriptEvent) fire();
        if (event.modified && event.altMessageDetermination == null) {
            event.altMessageDetermination = ComponentSerializer.parse(event.rawJson.asString());
        }
        return event;
    }
}
