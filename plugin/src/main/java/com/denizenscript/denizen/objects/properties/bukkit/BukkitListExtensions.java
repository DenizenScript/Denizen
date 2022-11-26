package com.denizenscript.denizen.objects.properties.bukkit;

import com.denizenscript.denizen.objects.*;
import com.denizenscript.denizen.utilities.Settings;
import com.denizenscript.denizencore.DenizenCore;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.core.ListTag;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.utilities.AsciiMatcher;
import org.bukkit.ChatColor;

import java.util.List;

public class BukkitListExtensions {

    public static AsciiMatcher LETTERS = new AsciiMatcher(AsciiMatcher.LETTERS_UPPER + AsciiMatcher.LETTERS_LOWER);

    public static void register() {

        // <--[tag]
        // @attribute <ListTag.formatted>
        // @returns ElementTag
        // @description
        // Returns the list in a human-readable format.
        // Note that this will parse the values within the list to be human-readable as well when possible.
        // EG, a list of "<npc>|<player>|potato" will return "GuardNPC, bob, and potato".
        // The exact formatting rules that will be followed are not guaranteed, other than that it will be a semi-clean human-readable format.
        // -->
        ListTag.tagProcessor.registerTag(ElementTag.class, "formatted", (attribute, list) -> {
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
                        if (gotten.isValid()) {
                            output.append(gotten.getName());
                        }
                        else {
                            output.append(gotten.getEntityType().getName());
                        }
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
                    String raw = ChatColor.stripColor(DenizenCore.implementation.applyDebugColors(object.debuggable()));
                    int at = raw.indexOf('@');
                    if (at != -1 && LETTERS.isOnlyMatches(raw.substring(0, at))) {
                        raw = raw.substring(at + 1);
                    }
                    output.append(raw);
                }
                if (i == list.size() - 2) {
                    output.append(i == 0 ? " and " : ", and ");
                }
                else {
                    output.append(", ");
                }
            }
            return new ElementTag(output.substring(0, output.length() - 2));
        });

        // <--[tag]
        // @attribute <ListTag.to_polygon>
        // @returns PolygonTag
        // @description
        // Converts a list of locations to a PolygonTag.
        // The Y-Min and Y-Max values will be assigned based the range of Y values in the locations given.
        // -->
        ListTag.tagProcessor.registerTag(PolygonTag.class, "to_polygon", (attribute, list) -> {
            List<LocationTag> locations = list.filter(LocationTag.class, attribute.context);
            if (locations == null || locations.isEmpty()) {
                return null;
            }
            if (locations.size() > Settings.blockTagsMaxBlocks()) {
                return null;
            }
            PolygonTag polygon = new PolygonTag(new WorldTag(locations.get(0).getWorldName()));
            polygon.yMin = locations.get(0).getY();
            polygon.yMax = polygon.yMin;
            for (LocationTag location : locations) {
                polygon.yMin = Math.min(polygon.yMin, location.getY());
                polygon.yMax = Math.max(polygon.yMax, location.getY());
                polygon.corners.add(new PolygonTag.Corner(location.getX(), location.getZ()));
            }
            polygon.recalculateBox();
            return polygon;
        });
    }
}
