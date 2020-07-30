package com.denizenscript.denizen.objects.properties.bukkit;

import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizen.objects.ItemTag;
import com.denizenscript.denizen.objects.MaterialTag;
import com.denizenscript.denizen.objects.PlayerTag;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.Mechanism;
import com.denizenscript.denizencore.objects.core.ListTag;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.properties.Property;
import com.denizenscript.denizencore.objects.properties.PropertyParser;
import org.bukkit.ChatColor;

public class BukkitListProperties implements Property {
    public static boolean describes(ObjectTag list) {
        return list instanceof ListTag;
    }

    public static BukkitListProperties getFrom(ObjectTag list) {
        if (!describes(list)) {
            return null;
        }
        else {
            return new BukkitListProperties((ListTag) list);
        }
    }

    private BukkitListProperties(ListTag list) {
        this.list = list;
    }

    public static final String[] handledMechs = new String[] {
    }; // None

    ListTag list;

    public static void registerTags() {

        // <--[tag]
        // @attribute <ListTag.formatted>
        // @returns ElementTag
        // @description
        // Returns the list in a human-readable format.
        // EG, a list of "<npc>|<player>|potato" will return "GuardNPC, bob, and potato".
        // -->
        PropertyParser.<BukkitListProperties>registerTag("formatted", (attribute, listObj) -> {
            ListTag list = listObj.list;
            if (list.isEmpty()) {
                return new ElementTag("");
            }
            StringBuilder output = new StringBuilder();
            for (int i = 0; i < list.size(); i++) {
                ObjectTag object = list.getObject(i);
                String val = object.toString();
                boolean handled = false;
                if (val.startsWith("p@")) {
                    PlayerTag gotten = object.asType(PlayerTag.class, attribute.context);
                    if (gotten != null) {
                        output.append(gotten.getName());
                        handled = true;
                    }
                }
                if (val.startsWith("e@") || val.startsWith("n@")) {
                    EntityTag gotten = object.asType(EntityTag.class, attribute.context);
                    if (gotten != null) {
                        output.append(gotten.getName());
                        handled = true;
                    }
                }
                if (val.startsWith("i@")) {
                    ItemTag item = object.asType(ItemTag.class, attribute.context);
                    if (item != null) {
                        output.append(item.formattedName());
                        handled = true;
                    }
                }
                if (val.startsWith("m@")) {
                    MaterialTag material = object.asType(MaterialTag.class, attribute.context);
                    if (material != null) {
                        output.append(material.name());
                        handled = true;
                    }
                }
                if (!handled) {
                    if (object instanceof ElementTag) {
                        output.append(val.replaceAll("\\w+@", ""));
                    }
                    else {
                        output.append(ChatColor.stripColor(object.debuggable()));
                    }
                }
                if (i == list.size() - 2) {
                    output.append(i == 0 ? " and " : ", and ");
                }
                else {
                    output.append(", ");
                }
            }
            return new ElementTag(output.toString().substring(0, output.length() - 2));
        });
    }

    @Override
    public String getPropertyString() {
        return null;
    }

    @Override
    public String getPropertyId() {
        return "BukkitListProperties";
    }

    @Override
    public void adjust(Mechanism mechanism) {
        // None
    }
}
