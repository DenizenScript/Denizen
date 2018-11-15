package net.aufdemrand.denizen.objects.properties.bukkit;

import net.aufdemrand.denizen.BukkitScriptEntryData;
import net.aufdemrand.denizen.objects.*;
import net.aufdemrand.denizen.scripts.containers.core.FormatScriptContainer;
import net.aufdemrand.denizen.scripts.containers.core.ItemScriptHelper;
import net.aufdemrand.denizen.tags.BukkitTagContext;
import net.aufdemrand.denizencore.objects.Element;
import net.aufdemrand.denizencore.objects.Mechanism;
import net.aufdemrand.denizencore.objects.dObject;
import net.aufdemrand.denizencore.objects.properties.Property;
import net.aufdemrand.denizencore.scripts.ScriptRegistry;
import net.aufdemrand.denizencore.tags.Attribute;
import org.bukkit.ChatColor;

public class BukkitElementProperties implements Property {

    public static boolean describes(dObject element) {
        return element instanceof Element;
    }

    public static BukkitElementProperties getFrom(dObject element) {
        if (!describes(element)) {
            return null;
        }
        else {
            return new BukkitElementProperties((Element) element);
        }
    }


    private BukkitElementProperties(Element element) {
        this.element = element;
    }

    public static final String[] handledAttribs = new String[]{
            "aschunk", "as_chunk", "ascolor", "as_color", "ascuboid", "as_cuboid", "asentity", "as_entity",
            "asinventory", "as_inventory", "asitem", "as_item", "aslocation", "as_location", "asmaterial",
            "as_material", "asnpc", "as_npc", "asplayer", "as_player", "asworld", "as_world", "asplugin",
            "as_plugin", "last_color", "format", "strip_color", "parse_color", "to_itemscript_hash"};

    Element element;

    @Override
    public String getAttribute(Attribute attribute) {

        // <--[tag]
        // @attribute <el@element.as_chunk>
        // @returns dChunk
        // @group conversion
        // @description
        // Returns the element as a chunk. Note: the value must be a valid chunk.
        // -->
        if (attribute.startsWith("aschunk")
                || attribute.startsWith("as_chunk")) {
            dObject object = Element.handleNull(element.asString(), dChunk.valueOf(element.asString(),
                    new BukkitTagContext(attribute.getScriptEntry(), false)), "dChunk", attribute.hasAlternative());
            if (object != null) {
                return object.getAttribute(attribute.fulfill(1));
            }
        }

        // <--[tag]
        // @attribute <el@element.as_color>
        // @returns dColor
        // @group conversion
        // @description
        // Returns the element as a dColor. Note: the value must be a valid color.
        // -->
        if (attribute.startsWith("ascolor")
                || attribute.startsWith("as_color")) {
            dObject object = Element.handleNull(element.asString(), dColor.valueOf(element.asString(),
                    new BukkitTagContext(attribute.getScriptEntry(), false)), "dColor", attribute.hasAlternative());
            if (object != null) {
                return object.getAttribute(attribute.fulfill(1));
            }
        }

        // <--[tag]
        // @attribute <el@element.as_cuboid>
        // @returns dCuboid
        // @group conversion
        // @description
        // Returns the element as a cuboid. Note: the value must be a valid cuboid.
        // -->
        if (attribute.startsWith("ascuboid")
                || attribute.startsWith("as_cuboid")) {
            dObject object = Element.handleNull(element.asString(), dCuboid.valueOf(element.asString(),
                    new BukkitTagContext(attribute.getScriptEntry(), false)), "dCuboid", attribute.hasAlternative());
            if (object != null) {
                return object.getAttribute(attribute.fulfill(1));
            }
        }

        // <--[tag]
        // @attribute <el@element.as_entity>
        // @returns dEntity
        // @group conversion
        // @description
        // Returns the element as an entity. Note: the value must be a valid entity.
        // -->
        if (attribute.startsWith("asentity")
                || attribute.startsWith("as_entity")) {
            dObject object = Element.handleNull(element.asString(), dEntity.valueOf(element.asString(),
                    new BukkitTagContext(attribute.getScriptEntry(), false)), "dEntity", attribute.hasAlternative());
            if (object != null) {
                return object.getAttribute(attribute.fulfill(1));
            }
        }

        // <--[tag]
        // @attribute <el@element.as_inventory>
        // @returns dInventory
        // @group conversion
        // @description
        // Returns the element as an inventory. Note: the value must be a valid inventory.
        // -->
        if (attribute.startsWith("asinventory")
                || attribute.startsWith("as_inventory")) {
            dObject object = Element.handleNull(element.asString(), dInventory.valueOf(element.asString(),
                    new BukkitTagContext(attribute.getScriptEntry(), false)), "dInventory", attribute.hasAlternative());
            if (object != null) {
                return object.getAttribute(attribute.fulfill(1));
            }
        }

        // <--[tag]
        // @attribute <el@element.as_item>
        // @returns dItem
        // @group conversion
        // @description
        // Returns the element as an item. Additional attributes can be accessed by dItem.
        // Note: the value must be a valid item.
        // -->
        if (attribute.startsWith("asitem")
                || attribute.startsWith("as_item")) {
            dObject object = Element.handleNull(element.asString(), dItem.valueOf(element.asString(),
                    new BukkitTagContext(attribute.getScriptEntry(), false)), "dItem", attribute.hasAlternative());
            if (object != null) {
                return object.getAttribute(attribute.fulfill(1));
            }
        }

        // <--[tag]
        // @attribute <el@element.as_location>
        // @returns dLocation
        // @group conversion
        // @description
        // Returns the element as a location. Note: the value must be a valid location.
        // -->
        if (attribute.startsWith("aslocation")
                || attribute.startsWith("as_location")) {
            dObject object = Element.handleNull(element.asString(), dLocation.valueOf(element.asString(),
                    new BukkitTagContext(attribute.getScriptEntry(), false)), "dLocation", attribute.hasAlternative());
            if (object != null) {
                return object.getAttribute(attribute.fulfill(1));
            }
        }

        // <--[tag]
        // @attribute <el@element.as_material>
        // @returns dMaterial
        // @group conversion
        // @description
        // Returns the element as a material. Note: the value must be a valid material.
        // -->
        if (attribute.startsWith("asmaterial")
                || attribute.startsWith("as_material")) {
            dObject object = Element.handleNull(element.asString(), dMaterial.valueOf(element.asString(),
                    new BukkitTagContext(attribute.getScriptEntry(), false)), "dMaterial", attribute.hasAlternative());
            if (object != null) {
                return object.getAttribute(attribute.fulfill(1));
            }
        }

        // <--[tag]
        // @attribute <el@element.as_npc>
        // @returns dNPC
        // @group conversion
        // @description
        // Returns the element as an NPC. Note: the value must be a valid NPC.
        // -->
        if (attribute.startsWith("asnpc")
                || attribute.startsWith("as_npc")) {
            dObject object = Element.handleNull(element.asString(), dNPC.valueOf(element.asString(),
                    new BukkitTagContext(attribute.getScriptEntry(), false)), "dNPC", attribute.hasAlternative());
            if (object != null) {
                return object.getAttribute(attribute.fulfill(1));
            }
        }

        // <--[tag]
        // @attribute <el@element.as_player>
        // @returns dPlayer
        // @group conversion
        // @description
        // Returns the element as a player. Note: the value must be a valid player. Can be online or offline.
        // -->
        if (attribute.startsWith("asplayer")
                || attribute.startsWith("as_player")) {
            dObject object = Element.handleNull(element.asString(), dPlayer.valueOf(element.asString(),
                    new BukkitTagContext(attribute.getScriptEntry(), false)), "dPlayer", attribute.hasAlternative());
            if (object != null) {
                return object.getAttribute(attribute.fulfill(1));
            }
        }

        // <--[tag]
        // @attribute <el@element.as_world>
        // @returns dWorld
        // @group conversion
        // @description
        // Returns the element as a world.
        // -->
        if (attribute.startsWith("asworld")
                || attribute.startsWith("as_world")) {
            dObject object = Element.handleNull(element.asString(), dWorld.valueOf(element.asString(),
                    new BukkitTagContext(attribute.getScriptEntry(), false)), "dWorld", attribute.hasAlternative());
            if (object != null) {
                return object.getAttribute(attribute.fulfill(1));
            }
        }

        // <--[tag]
        // @attribute <el@element.as_plugin>
        // @returns dPlugin
        // @group conversion
        // @description
        // Returns the element as a plugin. Note: the value must be a valid plugin.
        // -->
        if (attribute.startsWith("asplugin")
                || attribute.startsWith("as_plugin")) {
            dObject object = Element.handleNull(element.asString(), dPlugin.valueOf(element.asString(),
                    new BukkitTagContext(attribute.getScriptEntry(), false)), "dPlugin", attribute.hasAlternative());
            if (object != null) {
                return object.getAttribute(attribute.fulfill(1));
            }
        }

        // <--[tag]
        // @attribute <el@element.last_color>
        // @returns Element
        // @group text checking
        // @description
        // Returns the ChatColors used last in an element.
        // -->
        if (attribute.startsWith("last_color")) {
            return new Element(ChatColor.getLastColors(element.asString())).getAttribute(attribute.fulfill(1));
        }

        // <--[tag]
        // @attribute <el@element.format[<script>]>
        // @returns Element
        // @group text manipulation
        // @description
        // Returns the text re-formatted according to a format script.
        // See <@link example using format scripts>.
        // -->
        if (attribute.startsWith("format")
                && attribute.hasContext(1)) {
            FormatScriptContainer format = ScriptRegistry.getScriptContainer(attribute.getContext(1));
            if (format == null) {
                net.aufdemrand.denizencore.utilities.debugging.dB.echoError("Could not find format script matching '" + attribute.getContext(1) + "'");
                return null;
            }
            else {
                return new Element(format.getFormattedText(element.asString(),
                        attribute.getScriptEntry() != null ? ((BukkitScriptEntryData) attribute.getScriptEntry().entryData).getNPC() : null,
                        attribute.getScriptEntry() != null ? ((BukkitScriptEntryData) attribute.getScriptEntry().entryData).getPlayer() : null))
                        .getAttribute(attribute.fulfill(1));
            }
        }

        // <--[tag]
        // @attribute <el@element.strip_color>
        // @returns Element
        // @group text manipulation
        // @description
        // Returns the element with all color encoding stripped.
        // -->
        if (attribute.startsWith("strip_color")) {
            return new Element(ChatColor.stripColor(element.asString())).getAttribute(attribute.fulfill(1));
        }

        // <--[tag]
        // @attribute <el@element.parse_color[<prefix>]>
        // @returns Element
        // @group text manipulation
        // @description
        // Returns the element with all color codes parsed.
        // Optionally, specify a character to prefix the color ids. Defaults to '&' if not specified.
        // -->
        if (attribute.startsWith("parse_color")) {
            char prefix = '&';
            if (attribute.hasContext(1)) {
                prefix = attribute.getContext(1).charAt(0);
            }
            return new Element(ChatColor.translateAlternateColorCodes(prefix, element.asString()))
                    .getAttribute(attribute.fulfill(1));
        }

        // <--[tag]
        // @attribute <el@element.to_itemscript_hash>
        // @returns Element(Number)
        // @group conversion
        // @description
        // Shortens the element down to an itemscript hash ID, made of invisible color codes.
        // -->
        if (attribute.startsWith("to_itemscript_hash")) {
            return new Element(ItemScriptHelper.createItemScriptID(element.asString()))
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
        return "BukkitElementProperties";
    }

    @Override
    public void adjust(Mechanism mechanism) {
        // None
    }
}
