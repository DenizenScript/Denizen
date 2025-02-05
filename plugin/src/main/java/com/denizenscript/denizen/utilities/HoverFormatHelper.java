package com.denizenscript.denizen.utilities;

import com.denizenscript.denizen.nms.NMSHandler;
import com.denizenscript.denizen.nms.NMSVersion;
import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizen.objects.ItemTag;
import com.denizenscript.denizencore.utilities.CoreUtilities;
import com.denizenscript.denizencore.utilities.ReflectionHelper;
import com.denizenscript.denizencore.utilities.debugging.Debug;
import com.google.gson.*;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.hover.content.*;
import net.md_5.bungee.chat.ComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.Registry;
import org.bukkit.inventory.ItemStack;

import java.lang.reflect.Type;

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
            EntityTag entity = EntityTag.valueOf(FormattedTextHelper.unescape(input), CoreUtilities.basicContext);
            if (entity == null) {
                return true;
            }
            BaseComponent name = null;
            if (entity.getBukkitEntity() != null && entity.getBukkitEntity().isCustomNameVisible()) {
                name = new TextComponent();
                for (BaseComponent component : FormattedTextHelper.parse(entity.getBukkitEntity().getCustomName(), ChatColor.WHITE)) {
                    name.addExtra(component);
                }
            }
            content = new Entity(entity.getBukkitEntityType().getKey().toString(), entity.getUUID().toString(), name);
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
        else if (contentObject instanceof net.md_5.bungee.api.chat.hover.content.Entity entityHover) {
            // TODO: Maybe a stabler way of doing this?
            return "e@" + entityHover.getId();
        }
        else {
            throw new UnsupportedOperationException();
        }
    }

    public static void tryInitializeItemHoverFix() {
        if (!NMSHandler.getVersion().isAtLeast(NMSVersion.v1_20)) {
            return;
        }
        Gson bungeeGson = ReflectionHelper.getFieldValue(ComponentSerializer.class, "gson", null);
        if (bungeeGson == null) {
            return;
        }
        Gson fixedGson = bungeeGson.newBuilder()
                .registerTypeAdapter(FixedItemHover.class, new FixedItemHoverSerializer())
                .registerTypeAdapter(Item.class, new FixedItemHoverSerializer())
                .create();
        try {
            ReflectionHelper.getFinalSetter(ComponentSerializer.class, "gson").invoke(fixedGson);
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
