package com.denizenscript.denizen.paper;

import com.denizenscript.denizen.Denizen;
import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizen.objects.PlayerTag;
import com.denizenscript.denizen.objects.WorldTag;
import com.denizenscript.denizen.paper.events.*;
import com.denizenscript.denizen.paper.properties.*;
import com.denizenscript.denizen.paper.tags.PaperTagBase;
import com.denizenscript.denizen.utilities.debugging.Debug;
import com.denizenscript.denizencore.events.ScriptEvent;
import com.denizenscript.denizencore.objects.properties.PropertyParser;
import org.bukkit.Bukkit;

public class PaperModule {

    public static void init() {
        Debug.log("Loading Paper support module...");

        // Events
        ScriptEvent.registerScriptEvent(new AreaEnterExitScriptEventPaperImpl());
        ScriptEvent.registerScriptEvent(new EntityKnocksbackEntityScriptEvent());
        ScriptEvent.registerScriptEvent(new EntityLoadCrossbowScriptEvent());
        ScriptEvent.registerScriptEvent(new EntityPathfindScriptEvent());
        ScriptEvent.registerScriptEvent(new ExperienceOrbMergeScriptEvent());
        ScriptEvent.registerScriptEvent(new PlayerAbsorbsExperienceScriptEvent());
        ScriptEvent.registerScriptEvent(new PlayerBeaconEffectScriptEvent());
        ScriptEvent.registerScriptEvent(new PlayerClicksFakeEntityScriptEvent());
        ScriptEvent.registerScriptEvent(new PlayerClicksInRecipeBookScriptEvent());
        ScriptEvent.registerScriptEvent(new PlayerEquipsArmorScriptEvent());
        ScriptEvent.registerScriptEvent(new PlayerJumpsScriptEventPaperImpl());
        ScriptEvent.registerScriptEvent(new PlayerSpectatesEntityScriptEvent());
        ScriptEvent.registerScriptEvent(new PlayerStopsSpectatingScriptEvent());
        ScriptEvent.registerScriptEvent(new PreEntitySpawnScriptEvent());
        ScriptEvent.registerScriptEvent(new ProjectileCollideScriptEvent());
        ScriptEvent.registerScriptEvent(new ServerListPingScriptEventPaperImpl());
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

        // Other helpers
        Bukkit.getPluginManager().registerEvents(new PaperEventHelpers(), Denizen.getInstance());
    }
}
