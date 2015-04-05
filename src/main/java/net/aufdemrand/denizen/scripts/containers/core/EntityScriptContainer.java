package net.aufdemrand.denizen.scripts.containers.core;

import net.aufdemrand.denizen.objects.dNPC;
import net.aufdemrand.denizen.objects.dPlayer;
import net.aufdemrand.denizencore.objects.Element;
import net.aufdemrand.denizencore.objects.Mechanism;
import net.aufdemrand.denizencore.objects.dScript;
import net.aufdemrand.denizencore.scripts.containers.ScriptContainer;
import net.aufdemrand.denizen.objects.dEntity;
import net.aufdemrand.denizen.tags.BukkitTagContext;
import net.aufdemrand.denizencore.tags.TagManager;
import net.aufdemrand.denizen.utilities.debugging.dB;
import net.aufdemrand.denizencore.utilities.YamlConfiguration;

public class EntityScriptContainer extends ScriptContainer {

    // <--[language]
    // @name Entity Script Containers
    // @group Script Container System
    // @description
    // Entity script containers are an easy way to pre-define custom entities for use within scripts. Entity
    // scripts work with the dEntity object, and can be fetched with the Object Fetcher by using the
    // dEntity constructor e@EntityScriptName. Example: - spawn <player.location> e@MyEntity
    //
    // The following is the format for the container. Except for the 'entity_type' key (and the dScript
    // required 'type' key), all other keys are optional.
    //
    // <code>
    // # The name of the entity script is the same name that you can use to construct a new
    // # dEntity based on this entity script. For example, an entity script named 'space zombie'
    // # can be referred to as 'e@space zombie'.
    // Entity Script Name:
    //
    //   type: entity
    //
    //   # Must be a valid dEntity (EG e@zombie or e@pig[age=baby]) See 'dEntity' for more information.
    //   entity_type: e@base_entity
    //
    //   # Whether the entity has the default AI
    //   has_ai: true/false
    //
    //   # MORE OPTIONS ARE 'TODO'!
    //
    // </code>
    //
    // -->

    public EntityScriptContainer(YamlConfiguration configurationSection, String scriptContainerName) {
        super(configurationSection, scriptContainerName);
    }

   public dEntity getEntityFrom() {
       return getEntityFrom(null, null);
   }

    public dEntity getEntityFrom(dPlayer player, dNPC npc) {
        dEntity entity = null;
        try {
            if (contains("ENTITY_TYPE")) {
                String entityType = TagManager.tag((getString("ENTITY_TYPE", "")), new BukkitTagContext
                        (player, npc, false, null, shouldDebug(), new dScript(this)));
                entity = dEntity.valueOf(entityType);
            }

            else {
                throw new Exception("Missing entity_type argument!");
            }

            if (contains("HAS_AI")) {
                String has_ai = TagManager.tag((getString("HAS_AI", "false")), new BukkitTagContext
                        (player, npc, false, null, shouldDebug(), new dScript(this)));
                entity.adjust(new Mechanism(new Element("has_ai"), new Element(has_ai)));
            }

            if (entity == null || entity.isUnique())
                return null;

            entity.setEntityScript(getName());
        }
        catch (Exception e) {
            dB.echoError("Woah! An exception has been called with this entity script!");
            dB.echoError(e);
            entity = null;
        }

        return entity;

    }
}
