package com.denizenscript.denizen.objects;

import com.denizenscript.denizencore.objects.*;
import com.denizenscript.denizencore.objects.properties.PropertyParser;
import com.denizenscript.denizencore.tags.Attribute;
import com.denizenscript.denizencore.tags.ObjectTagProcessor;
import com.denizenscript.denizencore.tags.TagContext;
import com.denizenscript.denizencore.utilities.CoreUtilities;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.MerchantRecipe;

import java.util.Collections;

public class TradeTag implements ObjectTag, Adjustable {

    // <--[ObjectType]
    // @name TradeTag
    // @prefix trade
    // @base ElementTag
    // @implements PropertyHolderObject
    // @format
    // The identity format for trades is just the text 'trade'. All other data is specified through properties.
    //
    // @description
    // Merchant trades are the parts of a special merchant inventory that is typically viewed by right clicking
    // a villager entity. Any number of trades can fit in a single merchant inventory.
    //
    // Trades are represented by TradeTags.
    //
    // The properties that can be used to customize a merchant trade are:
    //
    // result=<item>
    // inputs=<item>(|<item>)
    // uses=<number of uses>
    // max_uses=<maximum number of uses>
    // has_xp=true/false
    //
    // For example, the following command opens a virtual merchant inventory with two merchant trades.
    // The first trade offers a sponge for two emeralds, can be used up to 10 times,
    // and offers XP upon a successful transaction.
    // The second trade has zero maximum uses and displays a barrier.
    // <code>
    // - opentrades trade[max_uses=10;inputs=emerald[quantity=2];result=sponge]|trade[result=barrier]
    // </code>
    //
    // -->

    @Fetchable("trade")
    public static TradeTag valueOf(String string, TagContext context) {
        if (string == null) {
            return null;
        }
        if (ObjectFetcher.isObjectWithProperties(string)) {
            return ObjectFetcher.getObjectFromWithProperties(TradeTag.class, string, context);
        }
        string = CoreUtilities.toLowerCase(string).replace("trade@", "");
        if (string.toLowerCase().matches("trade")) {
            MerchantRecipe recipe = new MerchantRecipe(new ItemStack(Material.AIR), 0);
            recipe.setIngredients(Collections.singletonList(new ItemStack(Material.AIR)));
            return new TradeTag(recipe);
        }
        return null;
    }

    public static boolean matches(String str) {
        return valueOf(str, CoreUtilities.noDebugContext) != null;
    }

    public TradeTag(MerchantRecipe recipe) {
        this.recipe = recipe;
    }

    @Override
    public TradeTag duplicate() {
        MerchantRecipe result = new MerchantRecipe(recipe.getResult(), recipe.getUses(), recipe.getMaxUses(), recipe.hasExperienceReward(), recipe.getVillagerExperience(), recipe.getPriceMultiplier());
        result.setIngredients(recipe.getIngredients());
        return new TradeTag(result);
    }

    @Override
    public String toString() {
        return identify();
    }

    private MerchantRecipe recipe;

    public MerchantRecipe getRecipe() {
        return recipe;
    }

    public void setRecipe(MerchantRecipe recipe) {
        this.recipe = recipe;
    }

    @Override
    public String getPrefix() {
        return "trade";
    }

    @Override
    public TradeTag setPrefix(String prefix) {
        return this;
    }

    @Override
    public boolean isUnique() {
        return false;
    }

    @Override
    public String getObjectType() {
        return "Trade";
    }

    @Override
    public String debuggable() {
        return "<LG>trade@trade<Y>" + PropertyParser.getPropertiesDebuggable(this);
    }

    @Override
    public String identify() {
        return "trade@trade" + PropertyParser.getPropertiesString(this);
    }

    @Override
    public String identifySimple() {
        return identify();
    }

    public static void registerTags() {
        PropertyParser.registerPropertyTagHandlers(TradeTag.class, tagProcessor);
    }

    public static ObjectTagProcessor<TradeTag> tagProcessor = new ObjectTagProcessor<>();

    @Override
    public ObjectTag getObjectAttribute(Attribute attribute) {
        return tagProcessor.getObjectAttribute(this, attribute);
    }

    @Override
    public void applyProperty(Mechanism mechanism) {
        adjust(mechanism);
    }

    @Override
    public void adjust(Mechanism mechanism) {
        CoreUtilities.autoPropertyMechanism(this, mechanism);
    }
}
