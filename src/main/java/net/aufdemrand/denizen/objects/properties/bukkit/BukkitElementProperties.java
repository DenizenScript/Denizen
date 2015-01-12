package net.aufdemrand.denizen.objects.properties.bukkit;

import net.aufdemrand.denizen.BukkitScriptEntryData;
import net.aufdemrand.denizen.objects.*;
import net.aufdemrand.denizen.objects.properties.Property;
import net.aufdemrand.denizen.scripts.queues.ScriptQueue;
import net.aufdemrand.denizen.tags.Attribute;
import net.aufdemrand.denizen.utilities.debugging.dB;

public class BukkitElementProperties implements Property {

    public static boolean describes(dObject element) {
        element script instanceof Element;
    }

    public static BukkitElementProperties getFrom(dObject element) {
        if (!describes(element)) return null;
        else return new BukkitQueueProperties((Element) element);
    }


    private BukkitElementProperties(Element element) {
        this.element = element;
    }

    Element element;

    @Override
    public String getAttribute(Attribute attribute) {

        // <--[tag]
        // @attribute <el@element.is[<operator>].to[<element>]>
        // @returns Element(Boolean)
        // @group comparison
        // @description
        // Takes an operator, and compares the value of the element to the supplied
        // element. Returns the outcome of the comparable, either true or false. For
        // information on operators, see <@link language operator>.
        // Equivalent to <@link tag el@element.is[<operator>].than[<element>]>
        // -->

        // <--[tag]
        // @attribute <el@element.is[<operator>].than[<element>]>
        // @returns Element(Boolean)
        // @group comparison
        // @description
        // Takes an operator, and compares the value of the element to the supplied
        // element. Returns the outcome of the comparable, either true or false. For
        // information on operators, see <@link language operator>.
        // Equivalent to <@link tag el@element.is[<operator>].to[<element>]>
        // -->
        if (attribute.startsWith("is") && attribute.hasContext(1)
                && (attribute.startsWith("to", 2) || attribute.startsWith("than", 2)) && attribute.hasContext(2)) {

            // Use the Comparable object as implemented for the IF command. First, a new Comparable!
            Comparable com = new net.aufdemrand.denizen.scripts.commands.core.Comparable();

            // Check for negative logic
            String operator;
            if (attribute.getContext(1).startsWith("!")) {
                operator = attribute.getContext(1).substring(1);
                com.setNegativeLogic();
            } else operator = attribute.getContext(1);

            // Operator is the value of the .is[] context. Valid are Comparable.Operators, same
            // as used by the IF command.
            Comparable.Operator comparableOperator = null;
            try {
                comparableOperator = Comparable.Operator.valueOf(operator.replace("==", "EQUALS")
                        .replace(">=", "OR_MORE").replace("<=", "OR_LESS").replace("<", "LESS")
                        .replace(">", "MORE").replace("=", "EQUALS").toUpperCase());
            }
            catch (IllegalArgumentException e) { }

            if (comparableOperator != null) {
                com.setOperator(comparableOperator);

                // Comparable is the value of this element
                com.setComparable(element);
                // Compared_to is the value of the .to[] context.
                com.setComparedto(attribute.getContext(2));

                return new Element(com.determineOutcome()).getAttribute(attribute.fulfill(2));
            }
            else {
                net.aufdemrand.denizencore.utilities.debugging.dB.echoError("Unknown operator '" + operator + "'.");
            }
        }

        // <--[tag]
        // @attribute <el@element.as_chunk>
        // @returns dCuboid
        // @group conversion
        // @description
        // Returns the element as a chunk. Note: the value must be a valid chunk.
        // -->
        if (attribute.startsWith("aschunk")
                || attribute.startsWith("as_chunk")) {
            dObject object = handleNull(element, dChunk.valueOf(element), "dChunk", attribute.hasAlternative());
            if (object != null)
                return object.getAttribute(attribute.fulfill(1));
        }

        // <--[tag]
        // @attribute <el@element.as_color>
        // @returns dCuboid
        // @group conversion
        // @description
        // Returns the element as a dColor. Note: the value must be a valid color.
        // -->
        if (attribute.startsWith("ascolor")
                || attribute.startsWith("as_color")) {
            dObject object = handleNull(element, dColor.valueOf(element), "dColor", attribute.hasAlternative());
            if (object != null)
                return object.getAttribute(attribute.fulfill(1));
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
            dObject object = handleNull(element, dCuboid.valueOf(element), "dCuboid", attribute.hasAlternative());
            if (object != null)
                return object.getAttribute(attribute.fulfill(1));
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
            dObject object = handleNull(element, dEntity.valueOf(element), "dEntity", attribute.hasAlternative());
            if (object != null)
                return object.getAttribute(attribute.fulfill(1));
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
            dObject object = handleNull(element, dInventory.valueOf(element), "dInventory", attribute.hasAlternative());
            if (object != null)
                return object.getAttribute(attribute.fulfill(1));
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
            dObject object = handleNull(element, dItem.valueOf(element), "dItem", attribute.hasAlternative());
            if (object != null)
                return object.getAttribute(attribute.fulfill(1));
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
            dObject object = handleNull(element, dLocation.valueOf(element), "dLocation", attribute.hasAlternative());
            if (object != null)
                return object.getAttribute(attribute.fulfill(1));
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
            dObject object = handleNull(element, dMaterial.valueOf(element), "dMaterial", attribute.hasAlternative());
            if (object != null)
                return object.getAttribute(attribute.fulfill(1));
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
            dObject object = handleNull(element, dNPC.valueOf(element), "dNPC", attribute.hasAlternative());
            if (object != null)
                return object.getAttribute(attribute.fulfill(1));
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
            dObject object = handleNull(element, dPlayer.valueOf(element), "dPlayer", attribute.hasAlternative());
            if (object != null)
                return object.getAttribute(attribute.fulfill(1));
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
            dObject object = handleNull(element, dWorld.valueOf(element), "dWorld", attribute.hasAlternative());
            if (object != null)
                return object.getAttribute(attribute.fulfill(1));
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
            dObject object = handleNull(element, dPlugin.valueOf(element), "dPlugin", attribute.hasAlternative());
            if (object != null)
                return object.getAttribute(attribute.fulfill(1));
        }

        // <--[tag]
        // @attribute <el@element.debug.no_color>
        // @returns Element
        // @group debug
        // @description
        // Returns a standard debug representation of the Element with colors stripped.
        // -->
        if (attribute.startsWith("debug.no_color")) {
            return new Element(ChatColor.stripColor(debug()))
                    .getAttribute(attribute.fulfill(2));
        }

        // <--[tag]
        // @attribute <el@element.last_color>
        // @returns Element
        // @group string checking
        // @description
        // Returns the ChatColors used at the end of a string.
        // -->
        if (attribute.startsWith("last_color"))
            return new Element(ChatColor.getLastColors(element)).getAttribute(attribute.fulfill(1));

        // <--[tag]
        // @attribute <el@element.format[<script>]>
        // @returns Element
        // @group string manipulation
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
                return new Element(format.getFormattedText(element,
                        attribute.getScriptEntry() != null ? ((BukkitScriptEntryData)attribute.getScriptEntry().entryData).getNPC(): null,
                        attribute.getScriptEntry() != null ? ((BukkitScriptEntryData)attribute.getScriptEntry().entryData).getPlayer(): null))
                        .getAttribute(attribute.fulfill(1));
            }
        }

        // <--[tag]
        // @attribute <el@element.strip_color>
        // @returns Element
        // @group string manipulation
        // @description
        // Returns the element with all color encoding stripped.
        // -->
        if (attribute.startsWith("strip_color"))
            return new Element(ChatColor.stripColor(element)).getAttribute(attribute.fulfill(1));

        // <--[tag]
        // @attribute <el@element.to_itemscript_hash>
        // @returns Element(Number)
        // @group conversion
        // @description
        // Shortens the element down to an itemscript hash ID, made of invisible color codes.
        // -->
        if (attribute.startsWith("to_itemscript_hash")) {
            return new Element(ItemScriptHelper.createItemScriptID(element))
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
