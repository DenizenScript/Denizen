package net.aufdemrand.denizen.objects.properties.item;

import net.aufdemrand.denizen.objects.dItem;
import net.aufdemrand.denizen.utilities.debugging.dB;
import net.aufdemrand.denizencore.objects.Element;
import net.aufdemrand.denizencore.objects.Mechanism;
import net.aufdemrand.denizencore.objects.dObject;
import net.aufdemrand.denizencore.objects.properties.Property;
import net.aufdemrand.denizencore.tags.Attribute;
import org.bukkit.Material;
import org.bukkit.potion.Potion;
import org.bukkit.potion.PotionType;

public class ItemPotion implements Property {

    public static boolean describes(dObject item) {
        return item instanceof dItem
                && ((dItem) item).getItemStack().getType() == Material.POTION;
    }

    public static ItemPotion getFrom(dObject _item) {
        if (!describes(_item)) return null;
        else return new ItemPotion((dItem) _item);
    }

    private ItemPotion(dItem item) {
        this.item = item;
    }

    dItem item;


    @Override
    public String getPropertyString() {
        if (item.getItemStack().getDurability() == 0)
            return null;
        // World record winning stupidest necessary workaround for a Bukkit issue
        if ((item.getItemStack().getDurability() & 0x40) != 0
                && PotionType.getByDamageValue(item.getItemStack().getDurability() & 0xF).isInstant()) {
            item.getItemStack().setDurability((short) (item.getItemStack().getDurability() & ~0x40));
        }
        Potion pot = Potion.fromItemStack(item.getItemStack());
        if (pot == null || pot.getType() == null)
            return String.valueOf(item.getItemStack().getDurability());
        return pot.getType().name() + "," + pot.getLevel() + "," + pot.hasExtendedDuration() + "," + pot.isSplash();
    }

    @Override
    public String getPropertyId() {
        return "potion";
    }

    @Override
    public String getAttribute(Attribute attribute) {

        if (attribute == null) {
            return null;
        }

        // <--[tag]
        // @attribute <i@item.has_potion_effect>
        // @returns Element(Boolean)
        // @mechanism dItem.potion
        // @description
        // Returns whether the potion has a potion effect.
        // -->
        if (attribute.startsWith("has_potion_effect")) {
            return new Element(item.getItemStack().getDurability() > 0)
                    .getAttribute(attribute.fulfill(1));
        }

        if (item.getItemStack().getDurability() > 0) {
            if (attribute.startsWith("potion_effect")) {
                attribute = attribute.fulfill(1);

                // <--[tag]
                // @attribute <i@item.potion_effect.is_splash>
                // @returns Element(Boolean)
                // @mechanism dItem.potion
                // @group properties
                // @description
                // Returns whether the potion is a splash potion.
                // -->
                if (attribute.startsWith("is_splash")) {
                    return new Element(Potion.fromItemStack(item.getItemStack()).isSplash())
                            .getAttribute(attribute.fulfill(1));
                }

                // <--[tag]
                // @attribute <i@item.potion_effect.is_extended>
                // @returns Element(Boolean)
                // @mechanism dItem.potion
                // @group properties
                // @description
                // Returns whether the potion has an extended duration.
                // -->
                if (attribute.startsWith("is_extended")) {
                    return new Element(Potion.fromItemStack(item.getItemStack()).hasExtendedDuration())
                            .getAttribute(attribute.fulfill(1));
                }

                // <--[tag]
                // @attribute <i@item.potion_effect.level>
                // @returns Element(Number)
                // @mechanism dItem.potion
                // @group properties
                // @description
                // Returns the level of this potion.
                // -->
                if (attribute.startsWith("level")) {
                    return new Element(Potion.fromItemStack(item.getItemStack()).getLevel())
                            .getAttribute(attribute.fulfill(1));
                }

                // <--[tag]
                // @attribute <i@item.potion_effect.type>
                // @returns Element
                // @mechanism dItem.potion
                // @group properties
                // @description
                // Returns the type name of this potion.
                // -->
                if (attribute.startsWith("type")) {
                    return new Element(Potion.fromItemStack(item.getItemStack()).getType().name())
                            .getAttribute(attribute.fulfill(1));
                }

                // <--[tag]
                // @attribute <i@item.potion_effect.data>
                // @returns Element(Number)
                // @mechanism dItem.potion
                // @group properties
                // @description
                // Returns the 'damage value' of the potion, if normal potion tags don't work.
                // -->
                if (attribute.startsWith("data")) {
                    return new Element(item.getItemStack().getDurability())
                            .getAttribute(attribute.fulfill(1));
                }

                // <--[tag]
                // @attribute <i@item.potion_effect>
                // @returns Element
                // @mechanism dItem.potion
                // @group properties
                // @description
                // Returns the potion effect on this item.
                // In the format Effect,Level,Extended,Splash
                // -->
                return new Element(getPropertyString())
                        .getAttribute(attribute);
            }
        }

        return null;
    }

    @Override
    public void adjust(Mechanism mechanism) {

        // <--[mechanism]
        // @object dItem
        // @name potion
        // @input Element
        // @description
        // Sets the potion's custom potion effects.
        // Input is a formed like: Effect,Level,Extended,Splash
        // EG, speed,1,true,false
        // Can also input a damage-value, EG '255'
        // @tags
        // <i@item.potion_effect>
        // <i@item.potion_effect.level>
        // <i@item.potion_effect.is_extended>
        // <i@item.potion_effects.is_splash>
        // -->
        if (mechanism.matches("potion")) {
            String[] data = mechanism.getValue().asString().split(",", 4);
            if (data.length < 4) {
                if (mechanism.getValue().isInt())
                    item.getItemStack().setDurability((short) mechanism.getValue().asInt());
                else
                    dB.echoError("Invalid effect format, use name,amplifier,extended,splash.");
            }
            else {
                Element data1 = new Element(data[1]);
                Element data2 = new Element(data[2]);
                Element data3 = new Element(data[3]);
                PotionType type;
                try {
                    type = PotionType.valueOf(data[0].toUpperCase());
                }
                catch (Exception ex) {
                    dB.echoError("Invalid potion effect type '" + data[0] + "'");
                    return;
                }
                if (type == null) {
                    dB.echoError("Invalid potion effect type '" + data[0] + "'");
                    return;
                }
                if (!data1.isInt()) {
                    dB.echoError("Cannot apply effect '" + data[0] + "': '" + data[1] + "' is not a valid integer!");
                    return;
                }
                if (!data2.isBoolean()) {
                    dB.echoError("Cannot apply effect '" + data[0] + "': '" + data[2] + "' is not a valid boolean!");
                    return;
                }
                if (!data3.isBoolean()) {
                    dB.echoError("Cannot apply effect '" + data[0] + "': '" + data[3] + "' is not a valid boolean!");
                    return;
                }
                Potion pot = new Potion(type);
                int d1 = data1.asInt();
                if (d1 >= 1 && d1 <= pot.getType().getMaxLevel()) {
                    pot.setLevel(d1);
                }
                if (!pot.getType().isInstant())
                    pot.setHasExtendedDuration(data2.asBoolean());
                pot.setSplash(data3.asBoolean());
                item.setDurability((short) 0);
                pot.apply(item.getItemStack());
            }
        }

    }
}
