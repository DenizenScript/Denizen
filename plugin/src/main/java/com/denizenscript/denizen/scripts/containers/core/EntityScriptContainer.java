package com.denizenscript.denizen.scripts.containers.core;

import com.denizenscript.denizen.utilities.debugging.Debug;
import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizen.objects.NPCTag;
import com.denizenscript.denizen.objects.PlayerTag;
import com.denizenscript.denizen.tags.BukkitTagContext;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.Mechanism;
import com.denizenscript.denizencore.objects.core.ScriptTag;
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
    // scripts work with the EntityTag object, and can be fetched with the Object Fetcher by using the
    // EntityTag constructor of simply the script name. Example: - spawn <player.location> MyEntity
    //
    // The following is the format for the container.
    // Except for the 'entity_type' key (and the required 'type' key), all other keys are optional.
    //
    // You can also include a 'custom' key to hold any custom yaml data attached to the script.
    //
    // <code>
    // # The name of the entity script is the same name that you can use to construct a new
    // # EntityTag based on this entity script. For example, an entity script named 'space_zombie'
    // # can be referred to as 'space_zombie'.
    // Entity_Script_Name:
    //
    //   type: entity
    //
    //   # Must be a valid EntityTag (EG 'zombie' or 'pig[age=baby]') See 'EntityTag' for more information.
    //   # | All entity scripts MUST have this key!
    //   entity_type: BASE_ENTITY_TYPE_HERE
    //
    //   # Samples of mechanisms to use (any valid EntityTag mechanisms may be listed like this):
    //
    //   # Whether the entity has the default AI
    //   # | Do not copy this line, it is only an example.
    //   has_ai: true/false
    //
    //   # What age the entity is
    //   # | Do not copy this line, it is only an example.
    //   age: baby/adult/<#>
    // </code>
    //
    // MORE OPTIONS ARE LISTED HERE: <@link url /denizen/mecs/entitytag.>
    //
    // -->

    public EntityScriptContainer(YamlConfiguration configurationSection, String scriptContainerName) {
        super(configurationSection, scriptContainerName);
    }

    public EntityTag getEntityFrom() {
        return getEntityFrom(null, null);
    }

    public EntityTag getEntityFrom(PlayerTag player, NPCTag npc) {
        EntityTag entity = null;
        try {
            if (contains("entity_type")) {
                String entityType = TagManager.tag((getString("entity_type", "")), new BukkitTagContext(player, npc, new ScriptTag(this)));
                entity = EntityTag.valueOf(entityType);
            }
            else {
                throw new Exception("Missing entity_type argument!");
            }

            TagContext context = new BukkitTagContext(player, npc, new ScriptTag(this));
            Set<StringHolder> strings = getConfigurationSection("").getKeys(false);
            for (StringHolder string : strings) {
                if (!string.low.equals("entity_type") && !string.low.equals("type") && !string.low.equals("debug") && !string.low.equals("custom")) {
                    String value = TagManager.tag((getString(string.low, "")), context);
                    entity.safeAdjust(new Mechanism(new ElementTag(string.low), new ElementTag(value), context));
                }
            }

            if (entity == null || entity.isUnique()) {
                return null;
            }

            entity.setEntityScript(getName());
        }
        catch (Exception e) {
            Debug.echoError("Woah! An exception has been called with this entity script!");
            Debug.echoError(e);
            entity = null;
        }

        return entity;

    }
}
