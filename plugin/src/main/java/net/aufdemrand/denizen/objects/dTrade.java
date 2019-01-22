package net.aufdemrand.denizen.objects;

import net.aufdemrand.denizen.tags.BukkitTagContext;
import net.aufdemrand.denizencore.objects.*;
import net.aufdemrand.denizencore.objects.properties.Property;
import net.aufdemrand.denizencore.objects.properties.PropertyParser;
import net.aufdemrand.denizencore.tags.Attribute;
import net.aufdemrand.denizencore.tags.TagContext;
import net.aufdemrand.denizencore.utilities.CoreUtilities;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.MerchantRecipe;

import java.util.regex.Matcher;

public class dTrade implements dObject, Adjustable {

    // <--[language]
    // @name Merchant Trades
    // @group Merchant Trades
    // @description
    // Merchant trades are the parts of a special merchant inventory that is typically viewed by right clicking
    // a villager entity. Any number of trades can fit in a single merchant inventory.
    //
    // The properties that can be used to customize a merchant trade are:
    //
    // result=<item>
    // input=<item>(|<item>)
    // uses=<number of uses>
    // max_uses=<maximum number of uses>
    // has_xp=<true/false>
    //
    // For example, the following task script opens a virtual merchant inventory with two merchant trades. The
    // first trade offers a sponge for two emeralds for a sponge, can be used up to 10 times, and offers XP upon a
    // successful transaction. The second trade has zero maximum uses and displays a barrier.
    //
    //
    // <code>
    // open two trades:
    //     type: task
    //     script:
    //     - opentrades li@trade@trade[max_uses=10;inputs=i@emerald[quantity=2];result=i@sponge|trade@trade[result=i@barrier]
    // </code>
    // -->

    //////////////////
    //    OBJECT FETCHER
    ////////////////

    public static dTrade valueOf(String string) {
        return valueOf(string, null);
    }

    @Fetchable("trade")
    public static dTrade valueOf(String string, TagContext context) {
        if (string == null) {
            return null;
        }

        ///////
        // Handle objects with properties through the object fetcher
        Matcher m = ObjectFetcher.DESCRIBED_PATTERN.matcher(string);
        if (m.matches()) {
            return ObjectFetcher.getObjectFrom(dTrade.class, string, new BukkitTagContext(((BukkitTagContext) context).player,
                    ((BukkitTagContext) context).npc, false, null, !context.debug, null));
        }

        string = CoreUtilities.toLowerCase(string).replace("trade@", "");
        if (string.toLowerCase().matches("trade")) {
            MerchantRecipe recipe = new MerchantRecipe(new ItemStack(Material.AIR), 0);
            recipe.addIngredient(new ItemStack(Material.AIR));
            return new dTrade(recipe);
        }
        return null;
    }

    public static boolean matches(String arg) {
        return arg.matches("trade@trade");
    }

    ///////////////
    //   Constructors
    /////////////

    public dTrade(MerchantRecipe recipe) {
        this.recipe = recipe;
    }

    /////////////////////
    //   INSTANCE FIELDS/METHODS
    /////////////////

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

    //////////////////////////////
    //  DSCRIPT ARGUMENT METHODS
    /////////////////////////

    public String getPrefix() {
        return "trade";
    }

    public dTrade setPrefix(String prefix) {
        return this;
    }

    public String debug() {
        return getPrefix() + "='<A>" + identify() + "<G>'  ";
    }

    public boolean isUnique() {
        return false;
    }

    public String getObjectType() {
        return "Trade";
    }

    public String identify() {
        return getPrefix() + "@trade" + PropertyParser.getPropertiesString(this);
    }

    public String identifySimple() {
        return identify();
    }

    public String getAttribute(Attribute attribute) {
        if (attribute == null) {
            return null;
        }

        String returned = CoreUtilities.autoPropertyTag(this, attribute);
        if (returned != null) {
            return returned;
        }

        return new Element(identify()).getAttribute(attribute);
    }

    public void applyProperty(Mechanism mechanism) {
        adjust(mechanism);
    }

    public void adjust(Mechanism mechanism) {
        CoreUtilities.autoPropertyMechanism(this, mechanism);
    }
}
