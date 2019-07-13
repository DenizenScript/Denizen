package com.denizenscript.denizen.objects.properties.entity;

import com.denizenscript.denizen.objects.dEntity;
import com.denizenscript.denizen.objects.dTrade;
import com.denizenscript.denizencore.objects.Mechanism;
import com.denizenscript.denizencore.objects.ListTag;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.properties.Property;
import com.denizenscript.denizencore.tags.Attribute;
import org.bukkit.inventory.Merchant;
import org.bukkit.inventory.MerchantRecipe;

import java.util.ArrayList;

public class EntityTrades implements Property {

    public static boolean describes(ObjectTag entity) {
        return entity instanceof dEntity
                && ((dEntity) entity).getBukkitEntity() instanceof Merchant;
    }

    public static EntityTrades getFrom(ObjectTag entity) {
        if (!describes(entity)) {
            return null;
        }
        return new EntityTrades((dEntity) entity);
    }

    public static final String[] handledTags = new String[] {
            "trades"
    };

    public static final String[] handledMechs = new String[] {
            "trades"
    };

    public ListTag getTradeRecipes() {
        if (entity.getBukkitEntity() instanceof Merchant) {
            ArrayList<dTrade> recipes = new ArrayList<>();
            for (MerchantRecipe recipe : ((Merchant) entity.getBukkitEntity()).getRecipes()) {
                recipes.add(new dTrade(recipe));
            }
            return new ListTag(recipes);
        }
        return null;
    }

    private dEntity entity;

    public EntityTrades(dEntity entity) {
        this.entity = entity;
    }

    public String getPropertyString() {
        if (((Merchant) entity.getBukkitEntity()).getRecipes() == null) {
            return null;
        }
        return getTradeRecipes().identify();
    }

    public String getPropertyId() {
        return "trades";
    }

    public String getAttribute(Attribute attribute) {
        if (attribute == null) {
            return null;
        }

        // <--[tag]
        // @attribute <e@entity.trades>
        // @returns ListTag(dTrade)
        // @mechanism dEntity.trades
        // @description
        // Returns a list of the Villager's trade recipes.
        // -->
        if (attribute.startsWith("trades")) {
            return getTradeRecipes().getAttribute(attribute.fulfill(1));
        }

        return null;
    }

    public void adjust(Mechanism mechanism) {

        // <--[mechanism]
        // @object dEntity
        // @name trades
        // @input ListTag(dTrade)
        // @description
        // Sets the trades that the entity will offer.
        // @tags
        // <e@entity.trades>
        // -->
        if (mechanism.matches("trades")) {
            ArrayList<MerchantRecipe> recipes = new ArrayList<>();
            for (dTrade recipe : mechanism.valueAsType(ListTag.class).filter(dTrade.class, mechanism.context)) {
                recipes.add(recipe.getRecipe());
            }
            ((Merchant) entity.getBukkitEntity()).setRecipes(recipes);
        }
    }
}
