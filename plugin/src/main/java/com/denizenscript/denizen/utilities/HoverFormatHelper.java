package com.denizenscript.denizen.utilities;

import com.denizenscript.denizen.nms.NMSHandler;
import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizen.objects.ItemTag;
import com.denizenscript.denizencore.utilities.CoreUtilities;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.hover.content.Content;
import net.md_5.bungee.api.chat.hover.content.Entity;
import net.md_5.bungee.api.chat.hover.content.Item;
import net.md_5.bungee.api.chat.hover.content.Text;

public class HoverFormatHelper {

    public static boolean processHoverInput(HoverEvent.Action action, TextComponent hoverableText, String input) {
        Content content;
        if (action == HoverEvent.Action.SHOW_ITEM) {
            ItemTag item = ItemTag.valueOf(FormattedTextHelper.unescape(input), CoreUtilities.noDebugContext);
            if (item == null) {
                return true;
            }
            // TODO: Why is there not a direct conversion method for Spigot ItemStack -> BungeeChat Item?
            String itemNbt = NMSHandler.itemHelper.getRawHoverText(item.getItemStack());
            content = new Item(item.getBukkitMaterial().getKey().toString(), item.getAmount(), net.md_5.bungee.api.chat.ItemTag.ofNbt(itemNbt));
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
}
