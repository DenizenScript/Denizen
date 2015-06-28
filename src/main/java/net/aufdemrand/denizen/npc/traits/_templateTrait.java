package net.aufdemrand.denizen.npc.traits;

import net.citizensnpcs.api.event.NPCLeftClickEvent;
import net.citizensnpcs.api.trait.Trait;
import org.bukkit.event.EventHandler;

// <--[language]
// @name Trait
// @group NPC Traits
// @description
// Traits are abilities and functions that are specific to NPCs. The trait system is
// implemented by Citizens2, but heavily utilized for NPC features throughout Denizen.
// -->


public class _templateTrait extends Trait {

    // MyPlugin _plugin;

    public _templateTrait() {
        super("mytrait");
        // Note: Don't register events, that's done for you.
    }

    @Override
    public void onAttach() {
        // my_plugin = Bukkit.getPluginManager().getPlugin("my_plugin");
    }

    @EventHandler
    public void leftClick(NPCLeftClickEvent event) {
        // if (event.getNPC() == this.getNPC()) {

        // }
    }
}
