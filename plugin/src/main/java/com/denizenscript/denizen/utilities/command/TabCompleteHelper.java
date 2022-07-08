package com.denizenscript.denizen.utilities.command;

import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizen.objects.ItemTag;
import com.denizenscript.denizen.objects.MaterialTag;
import com.denizenscript.denizen.scripts.containers.core.EntityScriptHelper;
import com.denizenscript.denizen.scripts.containers.core.ItemScriptHelper;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.properties.PropertyParser;
import com.denizenscript.denizencore.scripts.commands.AbstractCommand;
import com.denizenscript.denizencore.utilities.CoreUtilities;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;

import java.util.Map;

public class TabCompleteHelper {

    public static void tabCompleteItems(AbstractCommand.TabCompletionsBuilder tab) {
        int bracket = tab.arg.indexOf('[');
        if (bracket == -1) {
            for (Material material : Material.values()) {
                if (material.isItem()) {
                    tab.add(material.name());
                }
            }
            tab.add(ItemScriptHelper.item_scripts.keySet());
            return;
        }
        String material = tab.arg.substring(0, bracket);
        ItemTag item = ItemTag.valueOf(material, CoreUtilities.noDebugContext);
        tabCompletePropertiesFor(tab, bracket, item);
    }

    public static void tabCompleteBlockMaterials(AbstractCommand.TabCompletionsBuilder tab) {
        int bracket = tab.arg.indexOf('[');
        if (bracket == -1) {
            for (Material material : Material.values()) {
                if (material.isBlock()) {
                    tab.add(material.name());
                }
            }
            return;
        }
        String material = tab.arg.substring(0, bracket);
        MaterialTag mat = MaterialTag.valueOf(material, CoreUtilities.noDebugContext);
        tabCompletePropertiesFor(tab, bracket, mat);
    }

    public static void tabCompleteEntityTypes(AbstractCommand.TabCompletionsBuilder tab) {
        int bracket = tab.arg.indexOf('[');
        if (bracket == -1) {
            tab.add(EntityType.values());
            tab.add(EntityScriptHelper.scripts.keySet());
            return;
        }
        String type = tab.arg.substring(0, bracket);
        EntityTag entity = EntityTag.valueOf(type, CoreUtilities.noDebugContext);
        if (entity == null || entity.isUnique()) {
            return;
        }
        tabCompletePropertiesFor(tab, bracket, entity);
    }

    public static void tabCompletePropertiesFor(AbstractCommand.TabCompletionsBuilder tab, int bracket, ObjectTag object) {
        if (object == null) {
            return;
        }
        int lastSemicolon = tab.arg.lastIndexOf(';');
        if (lastSemicolon == -1) {
            lastSemicolon = bracket;
        }
        String propertyPart = tab.arg.substring(lastSemicolon + 1);
        if (propertyPart.indexOf('=') != -1) {
            return;
        }
        String prefixPart = tab.arg.substring(0, lastSemicolon + 1);
        PropertyParser.ClassPropertiesInfo properties = PropertyParser.propertiesByClass.get(object.getClass());
        for (Map.Entry<String, PropertyParser.PropertyGetter> property : properties.propertiesByMechanism.entrySet()) {
            if (property.getKey().startsWith(propertyPart) && property.getValue().get(object) != null) {
                tab.add(prefixPart + property.getKey());
            }
        }
    }
}
