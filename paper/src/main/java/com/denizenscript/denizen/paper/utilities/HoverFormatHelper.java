package com.denizenscript.denizen.paper.utilities;

import com.denizenscript.denizen.nms.NMSHandler;
import com.denizenscript.denizen.nms.NMSVersion;
import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizen.objects.ItemTag;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.core.MapTag;
import com.denizenscript.denizencore.tags.Attribute;
import com.denizenscript.denizencore.utilities.CoreUtilities;
import com.denizenscript.denizencore.utilities.debugging.Debug;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.event.HoverEventSource;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Registry;
import org.bukkit.inventory.ItemStack;

import java.util.UUID;

public class HoverFormatHelper {

    public static boolean processHoverInput(HoverEvent.Action<?> action, TextComponent.Builder hoverableText, String input) {
        HoverEventSource<?> content;
        if (action == HoverEvent.Action.SHOW_ITEM) {
            ItemTag item = ItemTag.valueOf(FormattedTextHelper.unescape(input), CoreUtilities.noDebugContext);
            if (item == null) {
                return true;
            }
            content = item.getItemStack();
        }
        else if (action == HoverEvent.Action.SHOW_ENTITY) {
            String rawInput = FormattedTextHelper.unescape(input);
            if (!rawInput.startsWith("map@")) {
                content = parseLegacyEntityHover(rawInput);
                if (content == null) {
                    return true;
                }
            }
            else {
                MapTag entityHoverData = MapTag.valueOf(rawInput, CoreUtilities.noDebugContext);
                if (entityHoverData == null) {
                    return true;
                }
                ElementTag uuidElement = entityHoverData.getElement("uuid");
                if (uuidElement == null) {
                    return true;
                }
                UUID uuid = CoreUtilities.tryParseUUID(uuidElement.asString());
                if (uuid == null) {
                    return true;
                }
                ElementTag type = entityHoverData.getElement("type");
                if (type == null) {
                    return true;
                }
                ElementTag rawName = entityHoverData.getElement("name");
                Component name = rawName != null ? FormattedTextHelper.parse(rawName.asString(), NamedTextColor.WHITE) : null;
                content = HoverEvent.showEntity(Key.key(type.asString()), uuid, name);
            }
        }
        else {
            content = FormattedTextHelper.parse(FormattedTextHelper.unescape(input), NamedTextColor.WHITE);
        }
        hoverableText.hoverEvent(content);
        return false;
    }

    public static String stringForHover(HoverEvent<?> hover) {
        if (hover.value() instanceof Component textHover) {
            return FormattedTextHelper.stringify(textHover);
        }
        else if (hover.value() instanceof HoverEvent.ShowItem itemHover) {
            Debug.log("Item hover: " + itemHover.dataComponents());
            ItemStack item = new ItemStack(Registry.MATERIAL.get(itemHover.item()), itemHover.count());
            if (NMSHandler.getVersion().isAtLeast(NMSVersion.v1_20)) {
//                item = NMSHandler.itemHelper.applyRawHoverComponentsJson(item, fixedItemHover.getComponents()); TODO
            }
            else if (itemHover.nbt() != null) {
                item = Bukkit.getUnsafe().modifyItemStack(item, itemHover.nbt().string());
            }
            return new ItemTag(item).identify();
        }
        else if (hover.value() instanceof HoverEvent.ShowEntity entityHover) {
            return createEntityHoverData(entityHover.id(), entityHover.type(), entityHover.name()).savable();
        }
        else {
            throw new UnsupportedOperationException();
        }
    }

    public static MapTag createEntityHoverData(UUID uuid, Key type, Component name) {
        MapTag entityHoverData = new MapTag();
        entityHoverData.putObject("uuid", new ElementTag(uuid.toString(), true));
        entityHoverData.putObject("type", new ElementTag(type.asString(), true));
        if (name != null) {
            entityHoverData.putObject("name", new ElementTag(FormattedTextHelper.stringify(name), true));
        }
        return entityHoverData;
    }

    public static String parseObjectToHover(ObjectTag object, HoverEvent.Action<?> action, Attribute attribute) {
        if (action == HoverEvent.Action.SHOW_ENTITY) {
            EntityTag toShow = object.asType(EntityTag.class, attribute.context);
            if (toShow == null) {
                attribute.echoError("Invalid hover object '" + object + "' specified for type 'SHOW_ENTITY': must be an EntityTag.");
                return null;
            }
            return createEntityHoverData(toShow.getUUID(), toShow.getBukkitEntityType().key(), toShow.getBukkitEntity() != null ? toShow.getBukkitEntity().customName() : null).savable();
        }
        else if (action == HoverEvent.Action.SHOW_ITEM) {
            ItemTag toShow = object.asType(ItemTag.class, attribute.context);
            if (toShow == null) {
                attribute.echoError("Invalid hover object '" + object + "' specified for type 'SHOW_ITEM': must be an ItemTag.");
                return null;
            }
            return toShow.identify();
        }
        else if (action == HoverEvent.Action.SHOW_TEXT) {
            return object.identify();
        }
        else {
            attribute.echoError("Using unsupported hover type: " + action + '.');
            return null;
        }
    }

    private static HoverEventSource<HoverEvent.ShowEntity> parseLegacyEntityHover(String input) {
        EntityTag entity = EntityTag.valueOf(input, CoreUtilities.basicContext);
        if (entity == null) {
            return null;
        }
        Component name = null;
        if (entity.getBukkitEntity() != null && entity.getBukkitEntity().isCustomNameVisible()) {
            name = entity.getBukkitEntity().customName();
        }
        return HoverEvent.showEntity(entity.getBukkitEntityType(), entity.getUUID(), name);
    }
}
