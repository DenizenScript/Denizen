package com.denizenscript.denizen.objects.properties.item;

import com.denizenscript.denizen.objects.ItemTag;
import com.denizenscript.denizen.utilities.Utilities;
import com.denizenscript.denizencore.objects.Mechanism;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.core.ListTag;
import com.denizenscript.denizencore.objects.properties.Property;
import com.denizenscript.denizencore.tags.Attribute;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.meta.KnowledgeBookMeta;

import java.util.ArrayList;
import java.util.List;

public class ItemKnowledgeBookRecipes implements Property {

    public static boolean describes(ObjectTag item) {
        return item instanceof ItemTag && ((ItemTag) item).getBukkitMaterial() == Material.KNOWLEDGE_BOOK;
    }

    public static ItemKnowledgeBookRecipes getFrom(ObjectTag _item) {
        if (!describes(_item)) {
            return null;
        }
        else {
            return new ItemKnowledgeBookRecipes((ItemTag) _item);
        }
    }

    public static final String[] handledTags = new String[] {
            "knowledge_book_recipes"
    };

    public static final String[] handledMechs = new String[] {
            "knowledge_book_recipes"
    };

    public ItemKnowledgeBookRecipes(ItemTag _item) {
        item = _item;
    }

    public ListTag recipeList() {
        ListTag output = new ListTag();
        if (item.getItemMeta() instanceof KnowledgeBookMeta) {
            for (NamespacedKey key : ((KnowledgeBookMeta) item.getItemMeta()).getRecipes()) {
                output.add(key.toString());
            }
        }
        return output;
    }

    ItemTag item;

    @Override
    public ObjectTag getObjectAttribute(Attribute attribute) {

        if (attribute == null) {
            return null;
        }

        // <--[tag]
        // @attribute <ItemTag.knowledge_book_recipes>
        // @returns ListTag
        // @mechanism ItemTag.knowledge_book_recipes
        // @group properties
        // @description
        // Returns a recipes unlocked by this knowledge book. Recipes are in the Namespace:Key format, for example "minecraft:gold_nugget".
        // These keys are not necessarily 1:1 with material names, as seen in the example "minecraft:gold_ingot_from_nuggets".
        // -->
        if (attribute.startsWith("knowledge_book_recipes")) {
            return recipeList().getObjectAttribute(attribute.fulfill(1));
        }

        return null;
    }

    @Override
    public String getPropertyString() {
        ListTag recipes = recipeList();
        if (recipes.size() > 0) {
            return recipes.identify();
        }
        else {
            return null;
        }
    }

    @Override
    public String getPropertyId() {
        return "knowledge_book_recipes";
    }

    @Override
    public void adjust(Mechanism mechanism) {

        // <--[mechanism]
        // @object ItemTag
        // @name knowledge_book_recipes
        // @input ListTag
        // @description
        // Sets the item's knowledge book recipe list, in the Namespace:Key format.
        // @tags
        // <ItemTag.knowledge_book_recipes>
        // -->
        if (mechanism.matches("knowledge_book_recipes")) {
            KnowledgeBookMeta meta = (KnowledgeBookMeta) item.getItemMeta();
            List<NamespacedKey> recipes = new ArrayList<>();
            ListTag newRecipes = mechanism.valueAsType(ListTag.class);
            for (String str : newRecipes) {
                recipes.add(Utilities.parseNamespacedKey(str));
            }
            meta.setRecipes(recipes);
            item.setItemMeta(meta);
        }

    }
}
