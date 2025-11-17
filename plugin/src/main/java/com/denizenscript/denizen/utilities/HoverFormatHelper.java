package com.denizenscript.denizen.utilities;

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
import com.google.gson.*;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.hover.content.*;
import net.md_5.bungee.chat.ChatVersion;
import net.md_5.bungee.chat.ComponentSerializer;
import net.md_5.bungee.chat.VersionedComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.Registry;
import org.bukkit.inventory.ItemStack;

import java.lang.reflect.Type;
import java.util.UUID;

public class HoverFormatHelper {

    public static boolean processHoverInput(HoverEvent.Action action, TextComponent hoverableText, String input) {
        Content content;
        if (action == HoverEvent.Action.SHOW_ITEM) {
            ItemTag item = ItemTag.valueOf(FormattedTextHelper.unescape(input), CoreUtilities.noDebugContext);
            if (item == null) {
                return true;
            }
            if (NMSHandler.getVersion().isAtLeast(NMSVersion.v1_20)) {
                content = new FixedItemHover(item.getBukkitMaterial().getKey().toString(), item.getAmount(), NMSHandler.itemHelper.getRawHoverComponentsJson(item.getItemStack()));
            }
            else {
                content = new Item(item.getBukkitMaterial().getKey().toString(), item.getAmount(), net.md_5.bungee.api.chat.ItemTag.ofNbt(NMSHandler.itemHelper.getLegacyHoverNbt(item)));
            }
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
                ElementTag uuid = entityHoverData.getElement("uuid");
                if (uuid == null) {
                    return true;
                }
                ElementTag type = entityHoverData.getElement("type");
                ElementTag rawName = entityHoverData.getElement("name");
                BaseComponent name = rawName != null ? new TextComponent(FormattedTextHelper.parse(rawName.asString(), ChatColor.WHITE)) : null;
                content = new Entity(type != null ? type.asString() : null, uuid.asString(), name);
            }
        }
        else {
            content = new Text(FormattedTextHelper.parse(FormattedTextHelper.unescape(input), ChatColor.WHITE));
        }
        hoverableText.setHoverEvent(new HoverEvent(action, content));
        return false;
    }

    public static String stringForHover(HoverEvent hover) {
        if (hover.getContents().isEmpty()) {
            return "";
        }
        Content contentObject = hover.getContents().get(0);
        if (contentObject instanceof Text textHover) {
            Object value = textHover.getValue();
            if (value instanceof BaseComponent[] componentsValue) {
                return FormattedTextHelper.stringify(componentsValue);
            }
            else {
                return value.toString();
            }
        }
        else if (contentObject instanceof Item itemHover) {
            ItemStack item = new ItemStack(Registry.MATERIAL.get(Utilities.parseNamespacedKey(itemHover.getId())), itemHover.getCount() == -1 ? 1 : itemHover.getCount());
            if (itemHover instanceof FixedItemHover fixedItemHover && fixedItemHover.getComponents() != null) {
                item = NMSHandler.itemHelper.applyRawHoverComponentsJson(item, fixedItemHover.getComponents());
            }
            else if (NMSHandler.getVersion().isAtMost(NMSVersion.v1_19) && itemHover.getTag() != null && itemHover.getTag().getNbt() != null) {
                item = Bukkit.getUnsafe().modifyItemStack(item, itemHover.getTag().getNbt());
            }
            return new ItemTag(item).identify();
        }
        else if (contentObject instanceof Entity entityHover) {
            return createEntityHoverData(entityHover.getId(), entityHover.getType(), entityHover.getName()).savable();
        }
        else {
            throw new UnsupportedOperationException();
        }
    }

    public static MapTag createEntityHoverData(String uuid, String type, BaseComponent name) {
        MapTag entityHoverData = new MapTag();
        entityHoverData.putObject("uuid", new ElementTag(uuid, true));
        if (type != null) {
            entityHoverData.putObject("type", new ElementTag(type, true));
        }
        else {
            try {
                // This isn't even optional, but is in Bungee for some reason - try our best to have a value
                org.bukkit.entity.Entity found = EntityTag.getEntityForID(UUID.fromString(uuid));
                if (found != null) {
                    entityHoverData.putObject("type", new ElementTag(found.getType().getKey().toString(), true));
                }
            }
            catch (IllegalArgumentException ignore) {}
        }
        if (name != null) {
            entityHoverData.putObject("name", new ElementTag(FormattedTextHelper.stringify(name), true));
        }
        return entityHoverData;
    }

    public static String parseObjectToHover(ObjectTag object, HoverEvent.Action action, Attribute attribute) {
        return switch (action) {
            case SHOW_ENTITY -> {
                EntityTag toShow = object.asType(EntityTag.class, attribute.context);
                if (toShow == null) {
                    attribute.echoError("Invalid hover object '" + object + "' specified for type 'SHOW_ENTITY': must be an EntityTag.");
                    yield null;
                }
                BaseComponent[] customName = PaperAPITools.instance.getCustomNameComponent(toShow.getBukkitEntity());
                yield createEntityHoverData(toShow.getUUID().toString(), toShow.getBukkitEntityType().getKey().toString(), customName != null ? new TextComponent(customName) : null).savable();
            }
            case SHOW_ITEM -> {
                ItemTag toShow = object.asType(ItemTag.class, attribute.context);
                if (toShow == null) {
                    attribute.echoError("Invalid hover object '" + object + "' specified for type 'SHOW_ITEM': must be an ItemTag.");
                    yield null;
                }
                yield toShow.identify();
            }
            case SHOW_TEXT -> object.toString();
            default -> {
                attribute.echoError("Using unsupported hover type: " + action + '.');
                yield null;
            }
        };
    }

    private static Entity parseLegacyEntityHover(String input) {
        EntityTag entity = EntityTag.valueOf(input, CoreUtilities.basicContext);
        if (entity == null) {
            return null;
        }
        BaseComponent name = null;
        if (entity.getBukkitEntity() != null && entity.getBukkitEntity().isCustomNameVisible()) {
            name = new TextComponent();
            for (BaseComponent component : FormattedTextHelper.parse(entity.getBukkitEntity().getCustomName(), ChatColor.WHITE)) {
                name.addExtra(component);
            }
        }
        return new Entity(entity.getBukkitEntityType().getKey().toString(), entity.getUUID().toString(), name);
    }

    public static void tryInitializeItemHoverFix() {
        if (!NMSHandler.getVersion().isAtLeast(NMSVersion.v1_20)) {
            return;
        }
        Gson bungeeGson = FormattedTextHelper.getBungeeGson();
        if (bungeeGson == null) {
            return;
        }
        Gson fixedGson = bungeeGson.newBuilder()
                .registerTypeAdapter(FixedItemHover.class, new FixedItemHoverSerializer())
                .registerTypeAdapter(Item.class, new FixedItemHoverSerializer())
                .create();
        try {
            if (NMSHandler.getVersion().isAtLeast(NMSVersion.v1_21)) {
                ReflectionHelper.setFieldValue(VersionedComponentSerializer.class, "gson", VersionedComponentSerializer.forVersion(ChatVersion.V1_21_5), fixedGson);
            }
            else {
                ReflectionHelper.getFinalSetter(ComponentSerializer.class, "gson").invoke(fixedGson);
            }
        }
        catch (Throwable e) {
            Debug.echoError(e);
        }
    }

    public static class FixedItemHover extends Item {

        private final JsonObject components;

        public FixedItemHover(String id, int count, JsonObject components) {
            super(id, count, null);
            this.components = components;
        }

        public JsonObject getComponents() {
            return components;
        }
    }

    public static class FixedItemHoverSerializer extends ItemSerializer {

        @Override
        public Item deserialize(JsonElement element, Type type, JsonDeserializationContext context) throws JsonParseException {
            Item deserialized = super.deserialize(element, type, context);
            if (deserialized.getTag() != null) {
                return deserialized;
            }
            JsonObject componentsObject = element.getAsJsonObject().getAsJsonObject("components");
            if (componentsObject == null) {
                return deserialized;
            }
            return new FixedItemHover(deserialized.getId(), deserialized.getCount(), componentsObject);
        }

        @Override
        public JsonElement serialize(Item content, Type type, JsonSerializationContext context) {
            JsonElement serialized = super.serialize(content, type, context);
            if (!(content instanceof FixedItemHover fixedItemHover) || fixedItemHover.getComponents() == null) {
                return serialized;
            }
            JsonObject serializedObject = serialized.getAsJsonObject();
            serializedObject.remove("tag");
            serializedObject.add("components", fixedItemHover.getComponents());
            return serializedObject;
        }
    }
}
