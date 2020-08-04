package com.denizenscript.denizen.objects.properties.entity;

import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizen.objects.TradeTag;
import com.denizenscript.denizencore.objects.Mechanism;
import com.denizenscript.denizencore.objects.core.ListTag;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.properties.Property;
import com.denizenscript.denizencore.tags.Attribute;
import org.bukkit.inventory.Merchant;
import org.bukkit.inventory.MerchantRecipe;

import java.util.ArrayList;

public class EntityTrades implements Property {

    public static boolean describes(ObjectTag entity) {
        return entity instanceof EntityTag
                && ((EntityTag) entity).getBukkitEntity() instanceof Merchant;
    }

    public static EntityTrades getFrom(ObjectTag entity) {
        if (!describes(entity)) {
            return null;
        }
        return new EntityTrades((EntityTag) entity);
    }

    public static final String[] handledTags = new String[] {
            "trades"
    };

    public static final String[] handledMechs = new String[] {
            "trades"
    };

    public ListTag getTradeRecipes() {
        ArrayList<TradeTag> recipes = new ArrayList<>();
        for (MerchantRecipe recipe : ((Merchant) entity.getBukkitEntity()).getRecipes()) {
            recipes.add(new TradeTag(recipe));
        }
        return new ListTag(recipes);
    }

    private EntityTag entity;

    public EntityTrades(EntityTag entity) {
        this.entity = entity;
    }

    public String getPropertyString() {
        return getTradeRecipes().identify();
    }

    public String getPropertyId() {
        return "trades";
    }

    public ObjectTag getObjectAttribute(Attribute attribute) {
        if (attribute == null) {
            return null;
        }

        // <--[tag]
        // @attribute <EntityTag.trades>
        // @returns ListTag(TradeTag)
        // @mechanism EntityTag.trades
        // @description
        // Returns a list of the villager's trade recipes.
        // -->
        if (attribute.startsWith("trades")) {
            return getTradeRecipes().getObjectAttribute(attribute.fulfill(1));
        }

        return null;
    }

    public void adjust(Mechanism mechanism) {

        // <--[mechanism]
        // @object EntityTag
        // @name trades
        // @input ListTag(TradeTag)
        // @description
        // Sets the trades that the villager will offer.
        // @tags
        // <EntityTag.trades>
        // -->
        if (mechanism.matches("trades")) {
            ArrayList<MerchantRecipe> recipes = new ArrayList<>();
            for (TradeTag recipe : mechanism.valueAsType(ListTag.class).filter(TradeTag.class, mechanism.context)) {
                recipes.add(recipe.getRecipe());
            }
            ((Merchant) entity.getBukkitEntity()).setRecipes(recipes);
        }
    }
}
