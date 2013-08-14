package net.aufdemrand.denizen.utilities;

import net.aufdemrand.denizen.Denizen;
import net.aufdemrand.denizen.objects.dNPC;
import net.aufdemrand.denizen.scripts.queues.ScriptEngine;
import net.aufdemrand.denizen.scripts.commands.CommandRegistry;
import net.citizensnpcs.api.npc.NPC;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.Collection;

/**
 * Provides some static methods for working with Denizen and Denizen-enabled NPCs
 *
 * @author aufdemrand
 *
 */
public class DenizenAPI {

    /**
     * Returns a dNPC object when given a valid NPC. DenizenNPCs have some methods
     * specific to Denizen functionality as well as easy access to the attached NPC and LivingEntity.
     *
     * @param npc the Citizens NPC
     *
     * @return a dNPC
     *
     */
    public static dNPC getDenizenNPC(NPC npc) {
        return getCurrentInstance().getNPCRegistry().getDenizen(npc);
    }

    /**
     * Similar to getting NPCs from Citizens' NPCRegistry, but this will filter out
     * unspawned NPCs
     *
     * @return map of NPC, dNPC of all spawned NPCs
     *
     */
    public static Collection<dNPC> getSpawnedNPCs() {
        return getCurrentInstance().getNPCRegistry().getSpawnedNPCs();
    }

    /**
     * Gets the current instance of the Denizen plugin.
     *
     * @return Denizen instance
     */
    public static Denizen getCurrentInstance() {
        return (Denizen) Bukkit.getPluginManager().getPlugin("Denizen");
    }

    public static CommandRegistry _commandRegistry() {
        return getCurrentInstance().getCommandRegistry();
    }

    public static ScriptEngine _scriptEngine() {
        return getCurrentInstance().getScriptEngine();
    }

    public static FileConfiguration _saves() {
        return getCurrentInstance().getSaves();
    }


}
