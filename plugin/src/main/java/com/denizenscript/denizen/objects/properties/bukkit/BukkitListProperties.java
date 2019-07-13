package com.denizenscript.denizen.objects.properties.bukkit;

import com.denizenscript.denizen.utilities.DenizenAPI;
import com.denizenscript.denizen.objects.dEntity;
import com.denizenscript.denizen.objects.dPlayer;
import com.denizenscript.denizencore.objects.ElementTag;
import com.denizenscript.denizencore.objects.Mechanism;
import com.denizenscript.denizencore.objects.ListTag;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.properties.Property;
import com.denizenscript.denizencore.tags.Attribute;

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

    public static final String[] handledTags = new String[] {
            "expiration", "formatted"
    };

    public static final String[] handledMechs = new String[] {
    }; // None
    ListTag list;

    @Override
    public String getAttribute(Attribute attribute) {

        // <--[tag]
        // @attribute <ListTag.formatted>
        // @returns ElementTag
        // @description
        // Returns the list in a human-readable format.
        // EG, a list of "n@3|p@bob|potato" will return "GuardNPC, bob, and potato".
        // -->
        if (attribute.startsWith("formatted")) {
            if (list.isEmpty()) {
                return new ElementTag("").getAttribute(attribute.fulfill(1));
            }
            StringBuilder dScriptArg = new StringBuilder();

            for (int n = 0; n < list.size(); n++) {
                if (list.get(n).startsWith("p@")) {
                    dPlayer gotten = dPlayer.valueOf(list.get(n));
                    if (gotten != null) {
                        dScriptArg.append(gotten.getName());
                    }
                    else {
                        dScriptArg.append(list.get(n).replaceAll("\\w+@", ""));
                    }
                }
                else if (list.get(n).startsWith("e@") || list.get(n).startsWith("n@")) {
                    dEntity gotten = dEntity.valueOf(list.get(n));
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

            return new ElementTag(dScriptArg.toString().substring(0, dScriptArg.length() - 2))
                    .getAttribute(attribute.fulfill(1));
        }

        // <--[tag]
        // @attribute <fl@flag_name.expiration>
        // @returns DurationTag
        // @description
        // Returns a Duration of the time remaining on the flag, if it
        // has an expiration.
        // -->
        if (attribute.startsWith("expiration") && list.flag != null) {
            return DenizenAPI.getCurrentInstance().getFlag(list.flag).expiration()
                    .getAttribute(attribute.fulfill(1));
        }

        return null;
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
