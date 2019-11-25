package com.denizenscript.denizen.paper;

import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizen.paper.events.*;
import com.denizenscript.denizen.paper.properties.EntityCanTick;
import com.denizenscript.denizen.utilities.debugging.Debug;
import com.denizenscript.denizencore.events.ScriptEvent;
import com.denizenscript.denizencore.objects.properties.PropertyParser;

public class PaperModule {

    public static void init() {
        Debug.log("Loading Paper support module...");
        // Events
        ScriptEvent.registerScriptEvent(new PlayerEquipsArmorScriptEvent());
        ScriptEvent.registerScriptEvent(new PlayerJumpsPaperScriptEventImpl());
        ScriptEvent.registerScriptEvent(new PlayerSpectatesEntityScriptEvent());
        ScriptEvent.registerScriptEvent(new PlayerStopsSpectatingScriptEvent());
        ScriptEvent.registerScriptEvent(new ProjectileCollideScriptEvent());
        ScriptEvent.registerScriptEvent(new TNTPrimesScriptEvent());
        // Properties
        PropertyParser.registerProperty(EntityCanTick.class, EntityTag.class);
    }
}
