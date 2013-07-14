package net.aufdemrand.denizen.npc.traits;

import net.citizensnpcs.api.event.NPCLeftClickEvent;
import net.citizensnpcs.api.trait.Trait;
import org.bukkit.event.EventHandler;


public class _templateTrait extends Trait {

    // MyPlugin _plugin;

    public _templateTrait() {
        super("mytrait");
    }

    @Override
    public void onAttach() {
        // my_plugin = Bukkit.getPluginManager().getPlugin("my_plugin");
    }

    @EventHandler
    public void leftClick(NPCLeftClickEvent event)
    {
        if (event.getNPC() == this.getNPC()) {

        }
    }


}
