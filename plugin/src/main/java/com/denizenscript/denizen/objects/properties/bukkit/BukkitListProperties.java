package com.denizenscript.denizen.objects.properties.bukkit;

import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizen.objects.PlayerTag;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.Mechanism;
import com.denizenscript.denizencore.objects.core.ListTag;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.properties.Property;
import com.denizenscript.denizencore.objects.properties.PropertyParser;

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
        // EG, a list of "n@3|p@bob|potato" will return "GuardNPC, bob, and potato".
        // -->
        PropertyParser.<ListTag>registerTag("formatted", (attribute, list) -> {
            if (list.isEmpty()) {
                return new ElementTag("");
            }
            StringBuilder dScriptArg = new StringBuilder();

            for (int n = 0; n < list.size(); n++) {
                if (list.get(n).startsWith("p@")) {
                    PlayerTag gotten = PlayerTag.valueOf(list.get(n));
                    if (gotten != null) {
                        dScriptArg.append(gotten.getName());
                    }
                    else {
                        dScriptArg.append(list.get(n).replaceAll("\\w+@", ""));
                    }
                }
                else if (list.get(n).startsWith("e@") || list.get(n).startsWith("n@")) {
                    EntityTag gotten = EntityTag.valueOf(list.get(n));
                    if (gotten != null) {
                        dScriptArg.append(gotten.getName());
                    }
                    else {
                        dScriptArg.append(list.get(n).replaceAll("\\w+@", ""));
                    }
                }
                else {
                    dScriptArg.append(list.get(n).replaceAll("\\w+@", ""));
                }

                if (n == list.size() - 2) {
                    dScriptArg.append(n == 0 ? " and " : ", and ");
                }
                else {
                    dScriptArg.append(", ");
                }
            }

            return new ElementTag(dScriptArg.toString().substring(0, dScriptArg.length() - 2));
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
