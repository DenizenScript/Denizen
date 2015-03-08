package net.aufdemrand.denizen.objects.properties.item;

import com.google.common.collect.Iterables;
import com.mojang.authlib.GameProfile;
import net.aufdemrand.denizen.objects.dItem;
import net.aufdemrand.denizen.utilities.debugging.dB;
import net.aufdemrand.denizencore.objects.Element;
import net.aufdemrand.denizencore.objects.Mechanism;
import net.aufdemrand.denizencore.objects.dList;
import net.aufdemrand.denizencore.objects.dObject;
import net.aufdemrand.denizencore.objects.properties.Property;
import net.aufdemrand.denizencore.tags.Attribute;
import net.aufdemrand.denizencore.utilities.CoreUtilities;
import net.minecraft.server.v1_8_R2.GameProfileSerializer;
import net.minecraft.server.v1_8_R2.ItemStack;
import net.minecraft.server.v1_8_R2.MinecraftServer;
import net.minecraft.server.v1_8_R2.NBTTagCompound;
import org.bukkit.craftbukkit.v1_8_R2.inventory.CraftItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.UUID;

public class ItemSkullskin implements Property {

    public static boolean describes(dObject item) {
        return item instanceof dItem
                && ((dItem) item).getItemStack().getItemMeta() instanceof SkullMeta;
    }

    public static ItemSkullskin getFrom(dObject _item) {
        if (!describes(_item)) return null;
        else return new ItemSkullskin((dItem)_item);
    }


    private ItemSkullskin(dItem _item) {
        item = _item;
    }

    dItem item;

    @Override
    public String getAttribute(Attribute attribute) {

        if (attribute == null) return "null";

        // <--[tag]
        // @attribute <i@item.skin>
        // @returns Element
        // @mechanism dItem.skull_skin
        // @group properties
        // @description
        // Returns the UUID of the player whose skin a skull item uses.
        // Note: Item must be a 'skull_item' with a skin.
        // -->
        // <--[tag]
        // @attribute <i@item.skin.full>
        // @returns Element|Element
        // @mechanism dItem.skull_skin
        // @group properties
        // @description
        // Returns the UUID of the player whose skin a skull item uses, along
        // with the permanently cached texture property.
        // Note: Item must be a 'skull_item' with a skin.
        // -->

        if (attribute.startsWith("skin")) {
            String skin = getPropertyString();
            if (item.getItemStack().getDurability() == 3 && skin != null) {
                attribute = attribute.fulfill(1);
                if (attribute.startsWith("full")) {
                    return new Element(skin).getAttribute(attribute.fulfill(1));
                }
                return new Element(CoreUtilities.split(skin, '|').get(0)).getAttribute(attribute);
            }
            else
                dB.echoError("This skull_item does not have a skin set!");
        }

        // <--[tag]
        // @attribute <i@item.has_skin>
        // @returns Element(Boolean)
        // @mechanism dItem.skull_skin
        // @group properties
        // @description
        // Returns whether the item has a custom skin set.
        // (Only for human 'skull_item's)
        // -->
        if (attribute.startsWith("has_skin"))
            return new Element(item.getItemStack().getDurability() == 3 && getPropertyString() != null)
                    .getAttribute(attribute.fulfill(1));


        return null;
    }


    @Override
    public String getPropertyString() {
        // TODO: use Bukkit SkullMeta method when updated
        if (item.getItemStack().getDurability() == 3) {
            ItemStack itemStack = CraftItemStack.asNMSCopy(item.getItemStack());
            if (itemStack.hasTag()) {
                NBTTagCompound tag = itemStack.getTag();
                if (tag.hasKeyOfType("SkullOwner", 10)) {
                    GameProfile profile = GameProfileSerializer.deserialize(tag.getCompound("SkullOwner"));
                    com.mojang.authlib.properties.Property property = Iterables.getFirst(profile.getProperties().get("textures"), null);
                    UUID uuid = profile.getId();
                    return (uuid != null ? uuid : profile.getName()) + (property != null ? "|" + property.getValue() : "");
                }
            }
        }
        return null;
    }

    @Override
    public String getPropertyId() {
        return "skull_skin";
    }

    @Override
    public void adjust(Mechanism mechanism) {

        // <--[mechanism]
        // @object dItem
        // @name skull_skin
        // @input Element(|Element)
        // @description
        // Sets the player skin on a skull_item.
        // Optionally, use the second Element for the skin texture cache.
        // This will require the player's UUID, not their name.
        // @tags
        // <i@item.skin>
        // <i@item.skin.full>
        // <i@item.has_skin>
        // -->

        // TODO: use Bukkit SkullMeta method when updated
        if (mechanism.matches("skull_skin")) {
            if (item.getItemStack().getDurability() != 3)
                item.getItemStack().setDurability((short)3);
            dList list = mechanism.getValue().asType(dList.class);
            String idString = list.get(0);
            ItemStack itemStack = CraftItemStack.asNMSCopy(item.getItemStack());
            GameProfile profile;
            if (idString.contains("-")) {
                UUID uuid = UUID.fromString(idString);
                profile = new GameProfile(uuid, null);
            }
            else {
                profile = new GameProfile(null, idString);
            }
            profile = fillGameProfile(profile);
            if (list.size() > 1) {
                profile.getProperties().put("textures", new com.mojang.authlib.properties.Property("value", list.get(1)));
            }
            NBTTagCompound tag = itemStack.hasTag() ? itemStack.getTag() : new NBTTagCompound();
            tag.set("SkullOwner", GameProfileSerializer.serialize(new NBTTagCompound(), profile));
            itemStack.setTag(tag);
            item.setItemStack(CraftItemStack.asBukkitCopy(itemStack));
        }

    }

    public static GameProfile fillGameProfile(GameProfile gameProfile) {
        if (gameProfile != null) {
            GameProfile gameProfile1;
            if (gameProfile.getName() != null) {
                gameProfile1 = MinecraftServer.getServer().getUserCache().getProfile(gameProfile.getName());
            } else if (gameProfile.getId() != null) {
                gameProfile1 = MinecraftServer.getServer().getUserCache().a(gameProfile.getId());
            } else {
                gameProfile1 = gameProfile;
            }
            if (Iterables.getFirst(gameProfile1.getProperties().get("textures"), null) == null) {
                // gameProfile1 = MinecraftServer.getServer().aB().fillProfileProperties(gameProfile1, true); // TODO: 1.8.3 update
            }
            return gameProfile1;
        }
        return null;
    }
}
