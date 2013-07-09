package net.aufdemrand.denizen.scripts.containers.core;

import net.aufdemrand.denizen.objects.dNPC;
import net.aufdemrand.denizen.scripts.containers.ScriptContainer;
import net.aufdemrand.denizen.objects.dEntity;
import net.aufdemrand.denizen.utilities.debugging.dB;
import net.minecraft.server.v1_6_R2.EntityLiving;
import net.minecraft.server.v1_6_R2.NBTTagCompound;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.craftbukkit.v1_6_R2.entity.CraftEntity;
import org.bukkit.entity.Player;

public class EntityScriptContainer extends ScriptContainer {

    public EntityScriptContainer(ConfigurationSection configurationSection, String scriptContainerName) {
        super(configurationSection, scriptContainerName);
    }

   public dEntity getEntityFrom() {
       return getEntityFrom(null, null);
   }

    public dEntity getEntityFrom(Player player, dNPC npc) {
        // Try to use this script to make an item.
        dEntity entity = null;
        try {
            // Check validity of material
            if (contains("TYPE")){

                // TODO:

            }

            // Set Id of the stack


        } catch (Exception e) {
            dB.echoError("Woah! An exception has been called with this entity script!");
            if (!dB.showStackTraces)
                dB.echoError("Enable '/denizen stacktrace' for the nitty-gritty.");
            else e.printStackTrace();
            entity = null;
        }

        return entity;

    }

}
