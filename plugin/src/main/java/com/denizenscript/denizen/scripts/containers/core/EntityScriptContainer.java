package com.denizenscript.denizen.scripts.containers.core;

import com.denizenscript.denizen.utilities.debugging.dB;
import com.denizenscript.denizen.objects.dEntity;
import com.denizenscript.denizen.objects.dNPC;
import com.denizenscript.denizen.objects.dPlayer;
import com.denizenscript.denizen.tags.BukkitTagContext;
import com.denizenscript.denizencore.objects.Element;
import com.denizenscript.denizencore.objects.Mechanism;
import com.denizenscript.denizencore.objects.dScript;
import com.denizenscript.denizencore.scripts.containers.ScriptContainer;
import com.denizenscript.denizencore.tags.TagContext;
import com.denizenscript.denizencore.tags.TagManager;
import com.denizenscript.denizencore.utilities.YamlConfiguration;
import com.denizenscript.denizencore.utilities.text.StringHolder;

import java.util.Set;

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
    // You can also include a 'custom' key to hold any custom yaml data attached to the script.
    //
    // <code>
    // # The name of the entity script is the same name that you can use to construct a new
    // # dEntity based on this entity script. For example, an entity script named 'space zombie'
    // # can be referred to as 'e@space zombie'.
    // Entity_Script_Name:
    //
    //   type: entity
    //
    //   # Must be a valid dEntity (EG e@zombie or e@pig[age=baby]) See 'dEntity' for more information.
    //   entity_type: e@base_entity
    //
    //   # Samples of mechanisms to use (any valid dEntity mechanisms may be listed like this):
    //
    //   # Whether the entity has the default AI
    //   has_ai: true/false
    //
    //   # What age the entity is
    //   age: baby/adult/<#>
    // </code>
    //
    // MORE OPTIONS ARE LISTED HERE: <@link url /denizen/mecs/dentity.>
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

            TagContext context = new BukkitTagContext(player, npc, false, null, shouldDebug(), new dScript(this));
            Set<StringHolder> strings = getConfigurationSection("").getKeys(false);
            for (StringHolder string : strings) {
                if (!string.low.equals("entity_type") && !string.low.equals("type") && !string.low.equals("debug") && !string.low.equals("custom")) {
                    String value = TagManager.tag((getString(string.low, "")), context);
                    entity.safeAdjust(new Mechanism(new Element(string.low), new Element(value), context));
                }
            }

            if (entity == null || entity.isUnique()) {
                return null;
            }

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
