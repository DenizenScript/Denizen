package net.aufdemrand.denizen.objects.properties.entity;

import net.aufdemrand.denizen.objects.dEntity;
import net.aufdemrand.denizen.objects.dTrade;
import net.aufdemrand.denizencore.objects.Mechanism;
import net.aufdemrand.denizencore.objects.dList;
import net.aufdemrand.denizencore.objects.dObject;
import net.aufdemrand.denizencore.objects.properties.Property;
import net.aufdemrand.denizencore.tags.Attribute;
import org.bukkit.inventory.Merchant;
import org.bukkit.inventory.MerchantRecipe;

import java.util.ArrayList;

public class EntityTrades implements Property {

    public static boolean describes(dObject entity) {
        return entity instanceof dEntity
                && ((dEntity) entity).getBukkitEntity() instanceof Merchant;
    }

    public static EntityTrades getFrom(dObject entity) {
        if (!describes(entity)) {
            return null;
        }
        return new EntityTrades((dEntity) entity);
    }

    private dEntity entity;

    public EntityTrades(dEntity entity) {
        this.entity = entity;
    }

    public String getPropertyString() {
        if (((Merchant) entity.getBukkitEntity()).getRecipes() == null) {
            return null;
        }
        return entity.getTradeRecipes().identify();
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
        // @returns dList(dTrade)
        // @mechanism dEntity.trades
        // @description
        // Returns a list of the Villager's trade recipes.
        // -->
        if (attribute.startsWith("trades")) {
            return entity.getTradeRecipes().getAttribute(attribute.fulfill(1));
        }

        return null;
    }

    public void adjust(Mechanism mechanism) {

        // <--[mechanism]
        // @object dEntity
        // @name trades
        // @input dList(dTrade)
        // @description
        // Sets the trades that the entity will offer.
        // @tags
        // <e@entity.trades>
        // -->
        if (mechanism.matches("trades")) {
            ArrayList<MerchantRecipe> recipes = new ArrayList<>();
            for (dTrade recipe : mechanism.getValue().asType(dList.class).filter(dTrade.class)) {
                recipes.add(recipe.getRecipe());
            }
            ((Merchant) entity.getBukkitEntity()).setRecipes(recipes);
        }
    }
}
