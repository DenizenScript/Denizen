package com.denizenscript.denizen.events.player;

public class PlayerReceivesActionbarScriptEvent extends PlayerReceivesMessageScriptEvent {

    // <--[event]
    // @Events
    // player receives actionbar
    //
    // @Regex ^on player receives actionbar$
    //
    // @Group Player
    //
    // @Cancellable true
    //
    // @Warning Triggering new actionbar messages in this event will cause it to re-fire.
    //
    // @Triggers when a player receives any actionbar from the server.
    //
    // @Context
    // <context.message> returns an ElementTag of the actionbar.
    // <context.raw_json> returns an ElementTag of the raw JSON used for the actionbar.
    //
    // @Determine
    // "MESSAGE:<ElementTag>" to change the actionbar.
    // "RAW_JSON:<ElementTag>" to change the JSON used for the actionbar.
    //
    // @Player Always.
    //
    // -->

    public PlayerReceivesActionbarScriptEvent() {
        instance = this;
    }

    public static PlayerReceivesActionbarScriptEvent instance;

    @Override
    public boolean couldMatch(ScriptPath path) {
        return path.eventLower.startsWith("player receives actionbar");
    }
}
