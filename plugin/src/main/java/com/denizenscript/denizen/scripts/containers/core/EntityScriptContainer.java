package com.denizenscript.denizen.scripts.containers.core;

import com.denizenscript.denizencore.utilities.debugging.Debug;
import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizen.objects.NPCTag;
import com.denizenscript.denizen.objects.PlayerTag;
import com.denizenscript.denizen.tags.BukkitTagContext;
import com.denizenscript.denizencore.flags.MapTagFlagTracker;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.Mechanism;
import com.denizenscript.denizencore.objects.core.ScriptTag;
import com.denizenscript.denizencore.scripts.containers.ScriptContainer;
import com.denizenscript.denizencore.tags.TagContext;
import com.denizenscript.denizencore.tags.TagManager;
import com.denizenscript.denizencore.utilities.CoreUtilities;
import com.denizenscript.denizen.utilities.BukkitImplDeprecations;
import com.denizenscript.denizencore.utilities.YamlConfiguration;
import com.denizenscript.denizencore.utilities.text.StringHolder;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
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
    // You can also include a 'custom' key to hold any custom data attached to the script.
    //
    // <code>
    // # The name of the entity script is the same name that you can use to construct a new
    // # EntityTag based on this entity script. For example, an entity script named 'space_zombie'
    // # can be referred to as 'space_zombie'.
    // Entity_Script_Name:
    //
    //     type: entity
    //
    //     # Must be a valid EntityTag (EG 'zombie' or 'pig[age=baby]') See 'EntityTag' for more information.
    //     # | All entity scripts MUST have this key!
    //     entity_type: BASE_ENTITY_TYPE_HERE
    //
    //     # If you want custom data that won't be parsed, use the 'data' root key.
    //     # | Some entity scripts should have this key!
    //     data:
    //         example_key: example value
    //
    //     # You can set flags on the entity when it spawns.
    //     # | Some entity scripts should have this key!
    //     flags:
    //         my_flag: my value
    //
    //     # Specify any mechanisms to apply the entity when it spawns.
    //     # | Some entity scripts should have this key!
    //     mechanisms:
    //
    //         # Samples of mechanisms to use (any valid EntityTag mechanisms may be listed like this):
    //
    //         # Whether the entity has the default AI
    //         # | Do not copy this line, it is only an example.
    //         has_ai: true/false
    //
    //         # What age the entity is
    //         # | Do not copy this line, it is only an example.
    //         age: baby/adult/<#>
    // </code>
    //
    // MORE MECHANISM OPTIONS ARE LISTED HERE: <@link url https://meta.denizenscript.com/Docs/Mechanisms/entitytag.>
    //
    // -->

    public EntityScriptContainer(YamlConfiguration configurationSection, String scriptContainerName) {
        super(configurationSection, scriptContainerName);
        EntityScriptHelper.scripts.put(CoreUtilities.toLowerCase(getName()), this);
        canRunScripts = false;
    }

    public EntityTag getEntityFrom() {
        return getEntityFrom(null, null);
    }

    public static HashSet<String> nonMechanismKeys = new HashSet<>(Arrays.asList("entity_type", "type", "debug", "custom", "data", "flags", "mechanisms"));

    public EntityTag getEntityFrom(PlayerTag player, NPCTag npc) {
        EntityTag entity;
        try {
            TagContext context = new BukkitTagContext(player, npc, new ScriptTag(this));
            if (contains("entity_type", String.class)) {
                String entityType = TagManager.tag((getString("entity_type", "")), context);
                entity = EntityTag.valueOf(entityType, context);
            }
            else {
                throw new Exception("Missing entity_type argument!");
            }
            if (contains("flags", Map.class)) {
                YamlConfiguration flagSection = getConfigurationSection("flags");
                MapTagFlagTracker tracker = new MapTagFlagTracker();
                for (StringHolder key : flagSection.getKeys(false)) {
                    tracker.setFlag(key.str, CoreUtilities.objectToTagForm(flagSection.get(key.str), context, true, true), null);
                }
                entity.safeAdjust(new Mechanism("flag_map", tracker.map, context));
            }
            if (contains("mechanisms", Map.class)) {
                YamlConfiguration mechSection = getConfigurationSection("mechanisms");
                Set<StringHolder> strings = mechSection.getKeys(false);
                for (StringHolder string : strings) {
                    ObjectTag obj = CoreUtilities.objectToTagForm(mechSection.get(string.low), context, true, true);
                    entity.safeAdjust(new Mechanism(string.low, obj, context));
                }
            }
            boolean any = false;
            Set<StringHolder> strings = getContents().getKeys(false);
            for (StringHolder string : strings) {
                if (!nonMechanismKeys.contains(string.low)) {
                    any = true;
                    ObjectTag obj = CoreUtilities.objectToTagForm(getContents().get(string.low), context, true, true);
                    entity.safeAdjust(new Mechanism(string.low, obj, context));
                }
            }
            if (any) {
                BukkitImplDeprecations.entityMechanismsFormat.warn(this);
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
