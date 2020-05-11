package com.denizenscript.denizen.paper;

import com.denizenscript.denizen.nms.NMSHandler;
import com.denizenscript.denizen.nms.NMSVersion;
import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizen.objects.PlayerTag;
import com.denizenscript.denizen.objects.WorldTag;
import com.denizenscript.denizen.paper.events.*;
import com.denizenscript.denizen.paper.properties.*;
import com.denizenscript.denizen.paper.tags.PaperTagBase;
import com.denizenscript.denizen.utilities.debugging.Debug;
import com.denizenscript.denizencore.events.ScriptEvent;
import com.denizenscript.denizencore.objects.properties.PropertyParser;

public class PaperModule {

    public static void init() {
        Debug.log("Loading Paper support module...");

        // Events
        ScriptEvent.registerScriptEvent(new EntityKnocksbackEntityScriptEvent());
        ScriptEvent.registerScriptEvent(new EntityPathfindScriptEvent());
        ScriptEvent.registerScriptEvent(new EntityShootsBowPaperScriptEventImpl());
        ScriptEvent.registerScriptEvent(new ExperienceOrbMergeScriptEvent());
        ScriptEvent.registerScriptEvent(new PlayerAbsorbsExperienceScriptEvent());
        ScriptEvent.registerScriptEvent(new PlayerBeaconEffectScriptEvent());
        ScriptEvent.registerScriptEvent(new PlayerEquipsArmorScriptEvent());
        ScriptEvent.registerScriptEvent(new PlayerJumpsPaperScriptEventImpl());
        ScriptEvent.registerScriptEvent(new PlayerSpectatesEntityScriptEvent());
        ScriptEvent.registerScriptEvent(new PlayerStopsSpectatingScriptEvent());
        if (NMSHandler.getVersion().isAtLeast(NMSVersion.v1_13)) {
            ScriptEvent.registerScriptEvent(new PreEntitySpawnScriptEvent());
        }
        ScriptEvent.registerScriptEvent(new ProjectileCollideScriptEvent());
        ScriptEvent.registerScriptEvent(new TNTPrimesScriptEvent());
        ScriptEvent.registerScriptEvent(new UnknownCommandScriptEvent());

        // Properties
        PropertyParser.registerProperty(EntityCanTick.class, EntityTag.class);
        PropertyParser.registerProperty(EntityExperienceOrb.class, EntityTag.class);
        PropertyParser.registerProperty(EntityFromSpawner.class, EntityTag.class);
        PropertyParser.registerProperty(EntitySpawnLocation.class, EntityTag.class);
        PropertyParser.registerProperty(WorldViewDistance.class, WorldTag.class);
        PropertyParser.registerProperty(PlayerAffectsMonsterSpawning.class, PlayerTag.class);

        // Paper Tags
        new PaperTagBase();
    }
}
