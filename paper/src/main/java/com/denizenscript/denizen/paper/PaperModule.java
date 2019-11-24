package com.denizenscript.denizen.paper;

import com.denizenscript.denizen.paper.events.*;
import com.denizenscript.denizen.utilities.debugging.Debug;
import com.denizenscript.denizencore.events.ScriptEvent;

public class PaperModule {

    public static void init() {
        Debug.log("Loading Paper support module...");
        ScriptEvent.registerScriptEvent(new PlayerEquipsArmorScriptEvent());
        ScriptEvent.registerScriptEvent(new PlayerJumpsPaperScriptEventImpl());
        ScriptEvent.registerScriptEvent(new PlayerSpectatesEntityScriptEvent());
        ScriptEvent.registerScriptEvent(new PlayerStopsSpectatingScriptEvent());
        ScriptEvent.registerScriptEvent(new ProjectileCollideScriptEvent());
        ScriptEvent.registerScriptEvent(new TNTPrimesScriptEvent());
    }
}
