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
import com.denizenscript.denizencore.utilities.ReflectionHelper;
import com.denizenscript.denizencore.utilities.debugging.Debug;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.event.HoverEventSource;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.inventory.ItemStack;

import java.lang.invoke.MethodHandle;
import java.util.Map;
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

    public static final MethodHandle ADVENTURE_COMPONENTS_TO_NMS = NMSHandler.getVersion().isAtLeast(NMSVersion.v1_20) ?
            ReflectionHelper.getMethodHandle(
                    ReflectionHelper.getClassOrThrow("io.papermc.paper.adventure.PaperAdventure"), "asVanilla", Map.class
            ) : null;

    public static String stringForHover(HoverEvent<?> hover) {
        if (hover.value() instanceof Component textHover) {
            return FormattedTextHelper.stringify(textHover);
        }
        else if (hover.value() instanceof HoverEvent.ShowItem itemHover) {
            Material material = Registry.MATERIAL.get(new NamespacedKey(itemHover.item().namespace(), itemHover.item().value()));
            if (material == null || !material.isItem()) {
                Debug.echoError("Invalid hover item type '" + itemHover.item() + "', please report this to the developers! See the stacktrace below for more information:");
                Debug.echoError(new RuntimeException());
                return null;
            }
            if (NMSHandler.getVersion().isAtMost(NMSVersion.v1_19)) {
                ItemStack item = new ItemStack(material, itemHover.count());
                if (itemHover.nbt() != null) {
                    item = Bukkit.getUnsafe().modifyItemStack(item, itemHover.nbt().string());
                }
                return new ItemTag(item).identify();
            }
            if (itemHover.dataComponents().isEmpty()) {
                return new ItemTag(material, itemHover.count()).identify();
            }
            try {
                Object nmsPatch = ADVENTURE_COMPONENTS_TO_NMS.invoke(itemHover.dataComponents());
                ItemStack item = NMSHandler.itemHelper.createItemWithNMSComponents(material, itemHover.count(), nmsPatch);
                return new ItemTag(item).identify();
            }
            catch (Throwable e) {
                Debug.echoError(e);
                return null;
            }
        }
        else if (hover.value() instanceof HoverEvent.ShowEntity entityHover) {
            return createEntityHoverData(entityHover.id(), entityHover.type(), entityHover.name()).savable();
        }
        else {
            Debug.echoError("Unrecognized hover event: " + hover);
            return null;
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
