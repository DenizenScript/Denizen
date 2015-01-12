package net.aufdemrand.denizen.objects.properties.bukkit;

import net.aufdemrand.denizen.utilities.debugging.dB;

public class BukkitListProperties {
    public static boolean describes(dObject script) {
        return script instanceof dList;
    }

    public static BukkitQueueProperties getFrom(dObject script) {
        if (!describes(script)) return null;
        else return new BukkitQueueProperties((dList) list);
    }


    private BukkitQueueProperties(dList list) {
        this.queue = list;
    }

    dList list;

    @Override
    public String getAttribute(Attribute attribute) {

        // <--[tag]
        // @attribute <li@list.formatted>
        // @returns Element
        // @description
        // returns the list in a human-readable format.
        // EG, a list of "n@3|p@bob|potato" will return "GuardNPC, bob, and potato".
        // -->
        if (attribute.startsWith("formatted")) {
            if (isEmpty()) return new Element("").getAttribute(attribute.fulfill(1));
            StringBuilder dScriptArg = new StringBuilder();

            for (int n = 0; n < this.size(); n++) {
                if (get(n).startsWith("p@")) {
                    dPlayer gotten = dPlayer.valueOf(get(n));
                    if (gotten != null) {
                        dScriptArg.append(gotten.getName());
                    }
                    else {
                        dScriptArg.append(get(n).replaceAll("\\w+@", ""));
                    }
                }
                else if (get(n).startsWith("e@") || get(n).startsWith("n@")) {
                    dEntity gotten = dEntity.valueOf(get(n));
                    if (gotten != null) {
                        dScriptArg.append(gotten.getName());
                    }
                    else {
                        dScriptArg.append(get(n).replaceAll("\\w+@", ""));
                    }
                }
                else {
                    dScriptArg.append(get(n).replaceAll("\\w+@", ""));
                }

                if (n == this.size() - 2) {
                    dScriptArg.append(n == 0 ? " and ": ", and ");
                }
                else {
                    dScriptArg.append(", ");
                }
            }

            return new Element(dScriptArg.toString().substring(0, dScriptArg.length() - 2))
                    .getAttribute(attribute.fulfill(1));
        }

        // <--[tag]
        // @attribute <fl@flag_name.expiration>
        // @returns Duration
        // @description
        // returns a Duration of the time remaining on the flag, if it
        // has an expiration.
        // -->
        if (flag != null && attribute.startsWith("expiration")) {
            return flag.expiration()
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
