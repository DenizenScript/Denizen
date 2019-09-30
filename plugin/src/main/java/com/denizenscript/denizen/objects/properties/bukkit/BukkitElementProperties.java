package com.denizenscript.denizen.objects.properties.bukkit;

import com.denizenscript.denizen.objects.*;
import com.denizenscript.denizen.scripts.containers.core.FormatScriptContainer;
import com.denizenscript.denizen.scripts.containers.core.ItemScriptHelper;
import com.denizenscript.denizen.utilities.FormattedTextHelper;
import com.denizenscript.denizencore.utilities.debugging.Debug;
import com.denizenscript.denizen.BukkitScriptEntryData;
import com.denizenscript.denizen.tags.BukkitTagContext;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.Mechanism;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.properties.Property;
import com.denizenscript.denizencore.scripts.ScriptRegistry;
import com.denizenscript.denizencore.tags.Attribute;
import net.md_5.bungee.chat.ComponentSerializer;
import org.bukkit.ChatColor;

import javax.xml.bind.DatatypeConverter;
import java.nio.charset.StandardCharsets;

public class BukkitElementProperties implements Property {

    public static boolean describes(ObjectTag element) {
        return element instanceof ElementTag;
    }

    public static BukkitElementProperties getFrom(ObjectTag element) {
        if (!describes(element)) {
            return null;
        }
        else {
            return new BukkitElementProperties((ElementTag) element);
        }
    }


    private BukkitElementProperties(ElementTag element) {
        this.element = element;
    }

    public static final String[] handledTags = new String[] {
            "aschunk", "as_chunk", "ascolor", "as_color", "ascuboid", "as_cuboid", "asentity", "as_entity",
            "asinventory", "as_inventory", "asitem", "as_item", "aslocation", "as_location", "asmaterial",
            "as_material", "asnpc", "as_npc", "asplayer", "as_player", "asworld", "as_world", "asplugin",
            "as_plugin", "last_color", "format", "strip_color", "parse_color", "to_itemscript_hash",
            "to_secret_colors", "from_secret_colors", "to_raw_json", "from_raw_json"
    };

    public static final String[] handledMechs = new String[] {
    }; // None

    ElementTag element;

    @Override
    public ObjectTag getObjectAttribute(Attribute attribute) {

        // <--[tag]
        // @attribute <ElementTag.as_chunk>
        // @returns ChunkTag
        // @group conversion
        // @description
        // Returns the element as a chunk. Note: the value must be a valid chunk.
        // -->
        if (attribute.startsWith("aschunk")
                || attribute.startsWith("as_chunk")) {
            ObjectTag object = ElementTag.handleNull(element.asString(), ChunkTag.valueOf(element.asString(),
                    new BukkitTagContext(attribute.getScriptEntry(), false)), "dChunk", attribute.hasAlternative());
            if (object != null) {
                return object.getObjectAttribute(attribute.fulfill(1));
            }
        }

        // <--[tag]
        // @attribute <ElementTag.as_color>
        // @returns ColorTag
        // @group conversion
        // @description
        // Returns the element as a ColorTag. Note: the value must be a valid color.
        // -->
        if (attribute.startsWith("ascolor")
                || attribute.startsWith("as_color")) {
            ObjectTag object = ElementTag.handleNull(element.asString(), ColorTag.valueOf(element.asString(),
                    new BukkitTagContext(attribute.getScriptEntry(), false)), "dColor", attribute.hasAlternative());
            if (object != null) {
                return object.getObjectAttribute(attribute.fulfill(1));
            }
        }

        // <--[tag]
        // @attribute <ElementTag.as_cuboid>
        // @returns CuboidTag
        // @group conversion
        // @description
        // Returns the element as a cuboid. Note: the value must be a valid cuboid.
        // -->
        if (attribute.startsWith("ascuboid")
                || attribute.startsWith("as_cuboid")) {
            ObjectTag object = ElementTag.handleNull(element.asString(), CuboidTag.valueOf(element.asString(),
                    new BukkitTagContext(attribute.getScriptEntry(), false)), "dCuboid", attribute.hasAlternative());
            if (object != null) {
                return object.getObjectAttribute(attribute.fulfill(1));
            }
        }

        // <--[tag]
        // @attribute <ElementTag.as_entity>
        // @returns EntityTag
        // @group conversion
        // @description
        // Returns the element as an entity. Note: the value must be a valid entity.
        // -->
        if (attribute.startsWith("asentity")
                || attribute.startsWith("as_entity")) {
            ObjectTag object = ElementTag.handleNull(element.asString(), EntityTag.valueOf(element.asString(),
                    new BukkitTagContext(attribute.getScriptEntry(), false)), "dEntity", attribute.hasAlternative());
            if (object != null) {
                return object.getObjectAttribute(attribute.fulfill(1));
            }
        }

        // <--[tag]
        // @attribute <ElementTag.as_inventory>
        // @returns InventoryTag
        // @group conversion
        // @description
        // Returns the element as an inventory. Note: the value must be a valid inventory.
        // -->
        if (attribute.startsWith("asinventory")
                || attribute.startsWith("as_inventory")) {
            ObjectTag object = ElementTag.handleNull(element.asString(), InventoryTag.valueOf(element.asString(),
                    new BukkitTagContext(attribute.getScriptEntry(), false)), "dInventory", attribute.hasAlternative());
            if (object != null) {
                return object.getObjectAttribute(attribute.fulfill(1));
            }
        }

        // <--[tag]
        // @attribute <ElementTag.as_item>
        // @returns ItemTag
        // @group conversion
        // @description
        // Returns the element as an item. Additional attributes can be accessed by ItemTag.
        // Note: the value must be a valid item.
        // -->
        if (attribute.startsWith("asitem")
                || attribute.startsWith("as_item")) {
            ObjectTag object = ElementTag.handleNull(element.asString(), ItemTag.valueOf(element.asString(),
                    new BukkitTagContext(attribute.getScriptEntry(), false)), "dItem", attribute.hasAlternative());
            if (object != null) {
                return object.getObjectAttribute(attribute.fulfill(1));
            }
        }

        // <--[tag]
        // @attribute <ElementTag.as_location>
        // @returns LocationTag
        // @group conversion
        // @description
        // Returns the element as a location. Note: the value must be a valid location.
        // -->
        if (attribute.startsWith("aslocation")
                || attribute.startsWith("as_location")) {
            ObjectTag object = ElementTag.handleNull(element.asString(), LocationTag.valueOf(element.asString(),
                    new BukkitTagContext(attribute.getScriptEntry(), false)), "dLocation", attribute.hasAlternative());
            if (object != null) {
                return object.getObjectAttribute(attribute.fulfill(1));
            }
        }

        // <--[tag]
        // @attribute <ElementTag.as_material>
        // @returns MaterialTag
        // @group conversion
        // @description
        // Returns the element as a material. Note: the value must be a valid material.
        // -->
        if (attribute.startsWith("asmaterial")
                || attribute.startsWith("as_material")) {
            ObjectTag object = ElementTag.handleNull(element.asString(), MaterialTag.valueOf(element.asString(),
                    new BukkitTagContext(attribute.getScriptEntry(), false)), "dMaterial", attribute.hasAlternative());
            if (object != null) {
                return object.getObjectAttribute(attribute.fulfill(1));
            }
        }

        // <--[tag]
        // @attribute <ElementTag.as_npc>
        // @returns NPCTag
        // @group conversion
        // @description
        // Returns the element as an NPC. Note: the value must be a valid NPC.
        // -->
        if (attribute.startsWith("asnpc")
                || attribute.startsWith("as_npc")) {
            ObjectTag object = ElementTag.handleNull(element.asString(), NPCTag.valueOf(element.asString(),
                    new BukkitTagContext(attribute.getScriptEntry(), false)), "dNPC", attribute.hasAlternative());
            if (object != null) {
                return object.getObjectAttribute(attribute.fulfill(1));
            }
        }

        // <--[tag]
        // @attribute <ElementTag.as_player>
        // @returns PlayerTag
        // @group conversion
        // @description
        // Returns the element as a player. Note: the value must be a valid player. Can be online or offline.
        // -->
        if (attribute.startsWith("asplayer")
                || attribute.startsWith("as_player")) {
            ObjectTag object = ElementTag.handleNull(element.asString(), PlayerTag.valueOf(element.asString(),
                    new BukkitTagContext(attribute.getScriptEntry(), false)), "dPlayer", attribute.hasAlternative());
            if (object != null) {
                return object.getObjectAttribute(attribute.fulfill(1));
            }
        }

        // <--[tag]
        // @attribute <ElementTag.as_world>
        // @returns WorldTag
        // @group conversion
        // @description
        // Returns the element as a world.
        // -->
        if (attribute.startsWith("asworld")
                || attribute.startsWith("as_world")) {
            ObjectTag object = ElementTag.handleNull(element.asString(), WorldTag.valueOf(element.asString(),
                    new BukkitTagContext(attribute.getScriptEntry(), false)), "dWorld", attribute.hasAlternative());
            if (object != null) {
                return object.getObjectAttribute(attribute.fulfill(1));
            }
        }

        // <--[tag]
        // @attribute <ElementTag.as_plugin>
        // @returns PluginTag
        // @group conversion
        // @description
        // Returns the element as a plugin. Note: the value must be a valid plugin.
        // -->
        if (attribute.startsWith("asplugin")
                || attribute.startsWith("as_plugin")) {
            ObjectTag object = ElementTag.handleNull(element.asString(), PluginTag.valueOf(element.asString(),
                    new BukkitTagContext(attribute.getScriptEntry(), false)), "dPlugin", attribute.hasAlternative());
            if (object != null) {
                return object.getObjectAttribute(attribute.fulfill(1));
            }
        }

        // <--[tag]
        // @attribute <ElementTag.last_color>
        // @returns ElementTag
        // @group text checking
        // @description
        // Returns the ChatColors used last in an element.
        // -->
        if (attribute.startsWith("last_color")) {
            return new ElementTag(ChatColor.getLastColors(element.asString())).getObjectAttribute(attribute.fulfill(1));
        }

        // <--[tag]
        // @attribute <ElementTag.format[<script>]>
        // @returns ElementTag
        // @group text manipulation
        // @description
        // Returns the text re-formatted according to a format script.
        // -->
        if (attribute.startsWith("format")
                && attribute.hasContext(1)) {
            FormatScriptContainer format = ScriptRegistry.getScriptContainer(attribute.getContext(1));
            if (format == null) {
                Debug.echoError("Could not find format script matching '" + attribute.getContext(1) + "'");
                return null;
            }
            else {
                return new ElementTag(format.getFormattedText(element.asString(),
                        attribute.getScriptEntry() != null ? ((BukkitScriptEntryData) attribute.getScriptEntry().entryData).getNPC() : null,
                        attribute.getScriptEntry() != null ? ((BukkitScriptEntryData) attribute.getScriptEntry().entryData).getPlayer() : null))
                        .getObjectAttribute(attribute.fulfill(1));
            }
        }

        // <--[tag]
        // @attribute <ElementTag.strip_color>
        // @returns ElementTag
        // @group text manipulation
        // @description
        // Returns the element with all color encoding stripped.
        // -->
        if (attribute.startsWith("strip_color")) {
            return new ElementTag(ChatColor.stripColor(element.asString())).getObjectAttribute(attribute.fulfill(1));
        }

        // <--[tag]
        // @attribute <ElementTag.parse_color[<prefix>]>
        // @returns ElementTag
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
            return new ElementTag(ChatColor.translateAlternateColorCodes(prefix, element.asString()))
                    .getObjectAttribute(attribute.fulfill(1));
        }

        // <--[tag]
        // @attribute <ElementTag.to_itemscript_hash>
        // @returns ElementTag
        // @group conversion
        // @description
        // Shortens the element down to an itemscript hash ID, made of invisible color codes.
        // -->
        if (attribute.startsWith("to_itemscript_hash")) {
            return new ElementTag(ItemScriptHelper.createItemScriptID(element.asString()))
                    .getObjectAttribute(attribute.fulfill(1));
        }

        // <--[tag]
        // @attribute <ElementTag.to_secret_colors>
        // @returns ElementTag
        // @group conversion
        // @description
        // Hides the element's text in invisible color codes.
        // Inverts <@link tag ElementTag.from_secret_colors>.
        // -->
        if (attribute.startsWith("to_secret_colors")) {
            String text = element.asString();
            byte[] bytes = text.getBytes(StandardCharsets.UTF_8);
            String hex = DatatypeConverter.printHexBinary(bytes);
            StringBuilder colors = new StringBuilder(text.length() * 2);
            for (int i = 0; i < hex.length(); i++) {
                colors.append(ChatColor.COLOR_CHAR).append(hex.charAt(i));
            }
            return new ElementTag(colors.toString())
                    .getObjectAttribute(attribute.fulfill(1));
        }

        // <--[tag]
        // @attribute <ElementTag.from_secret_colors>
        // @returns ElementTag
        // @group conversion
        // @description
        // Un-hides the element's text from invisible color codes back to normal text.
        // Inverts <@link tag ElementTag.to_secret_colors>.
        // -->
        if (attribute.startsWith("from_secret_colors")) {
            String text = element.asString().replace(String.valueOf(ChatColor.COLOR_CHAR), "");
            byte[] bytes = DatatypeConverter.parseHexBinary(text);
            return new ElementTag(new String(bytes, StandardCharsets.UTF_8))
                    .getObjectAttribute(attribute.fulfill(1));
        }

        // <--[tag]
        // @attribute <ElementTag.to_raw_json>
        // @returns ElementTag
        // @group conversion
        // @description
        // Converts normal colored text to Minecraft-style "raw JSON" format.
        // Inverts <@link tag ElementTag.from_raw_json>.
        // -->
        if (attribute.startsWith("to_raw_json")) {
            return new ElementTag(ComponentSerializer.toString(FormattedTextHelper.parse(element.asString())))
                    .getObjectAttribute(attribute.fulfill(1));
        }

        // <--[tag]
        // @attribute <ElementTag.from_raw_json>
        // @returns ElementTag
        // @group conversion
        // @description
        // Un-hides the element's text from invisible color codes back to normal text.
        // Inverts <@link tag ElementTag.to_raw_json>.
        // -->
        if (attribute.startsWith("from_raw_json")) {
            return new ElementTag(FormattedTextHelper.stringify(ComponentSerializer.parse(element.asString())))
                    .getObjectAttribute(attribute.fulfill(1));
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
