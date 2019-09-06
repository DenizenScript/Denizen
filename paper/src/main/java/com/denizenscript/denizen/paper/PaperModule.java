package com.denizenscript.denizen.paper;

import com.denizenscript.denizen.paper.events.PlayerEquipsArmorScriptEvent;
import com.denizenscript.denizen.paper.events.PlayerJumpsPaperScriptEventImpl;
import com.denizenscript.denizen.utilities.debugging.Debug;
import com.denizenscript.denizencore.events.ScriptEvent;

public class PaperModule {

    public static void init() {
        Debug.log("Loading Paper support module...");
        ScriptEvent.registerScriptEvent(new PlayerEquipsArmorScriptEvent());
        ScriptEvent.registerScriptEvent(new PlayerJumpsPaperScriptEventImpl());
    }
}
