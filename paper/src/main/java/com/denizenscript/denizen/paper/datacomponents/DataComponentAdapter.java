package com.denizenscript.denizen.paper.datacomponents;

import com.denizenscript.denizen.objects.ItemTag;
import com.denizenscript.denizen.objects.MaterialTag;
import com.denizenscript.denizen.objects.properties.item.ItemComponentsPatch;
import com.denizenscript.denizen.objects.properties.item.ItemProperty;
import com.denizenscript.denizen.utilities.Utilities;
import com.denizenscript.denizencore.objects.Mechanism;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.core.MapTag;
import com.denizenscript.denizencore.objects.properties.PropertyParser;
import com.denizenscript.denizencore.utilities.CoreUtilities;
import io.papermc.paper.datacomponent.DataComponentType;
import org.bukkit.Material;
import org.bukkit.Registry;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

public abstract class DataComponentAdapter<TP, TD extends ObjectTag> {
    
    // <--[language]
    // @name Item Components
    // @group Minecraft Logic
    // @description
    // Minecraft item components (see <@link url https://minecraft.wiki/w/Data_component_format>) are managed as follows:
    // Each item type has a default set of component values; a food item will have food components by default, a tool item will have tool components by default, etc.
    // Different items can override their type's default components, either by setting values that weren't there previously (e.g. making an inedible item edible), or by removing values that are there by default (e.g. making a shield item that can't block).
    // Items' overrides can later be reset, making them use their type's default values again.
    //
    // In Denizen, different item components are represented by item properties.
    // These properties allow both setting a component override on an item, and clearing/resetting it by providing no input.
    // Item properties' name will generally match their respective item component's name, but not always!
    // Due to this, features that take item component names as input (such as <@link tag ItemTag.is_overridden>) accept both Minecraft component names and Denizen property names.
    //
    // Here is an example of applying all of this in a script:
    // <code>
    // # We define a default apple item
    // - define apple <item[apple]>
    // # We remove the apple's "food" component, making eating it restore no food points (it is still consumable due to the "consumable" component).
    // - adjust def:apple removed:food
    // # This check will pass, as the apple's "food" component is overridden to have no value.
    // - if <[apple].is_overridden[food]>:
    //   - narrate "The apple has a changed food component! It will behave differently to a normal apple."
    // # We reset the apple item's food component, making it a normal apple.
    // - adjust def:apple food:
    // </code>
    // -->

    public static final Map<String, DataComponentType> COMPONENTS_BY_PROPERTY = new HashMap<>();
    public static final String[] EMPTY_STRING_ARRAY = new String[0];

    public static DataComponentType getComponentType(String name) {
        String nameLower = CoreUtilities.toLowerCase(name);
        DataComponentType componentType = Registry.DATA_COMPONENT_TYPE.get(Utilities.parseNamespacedKey(nameLower));
        if (componentType == null) {
            componentType = DataComponentAdapter.COMPONENTS_BY_PROPERTY.get(nameLower);
        }
        return componentType;
    }

    public static <TP, TD extends ObjectTag> void register(DataComponentAdapter<TP, TD> adapter) {
        DataComponentAdapter.Property.currentlyRegisteringComponentAdapter = adapter;
        PropertyParser.registerPropertyGetter(
                item -> !item.getItemStack().isEmpty() ? adapter.new Property(item) : null,
                ItemTag.class, EMPTY_STRING_ARRAY, EMPTY_STRING_ARRAY, DataComponentAdapter.Property.class);
        DataComponentAdapter.Property.currentlyRegisteringComponentAdapter = null;
        MaterialTag.tagProcessor.registerTag(adapter.denizenType, adapter.name, (attribute, materialTag) -> {
            Material material = materialTag.getMaterial();
            if (!material.isItem()) {
                attribute.echoError("Cannot get item component value from a block material.");
                return null;
            }
            TP internalValue = material.getDefaultData(adapter.componentType);
            return internalValue != null ? adapter.toDenizen(internalValue) : null;
        });
        String componentName = adapter.componentType.key().value();
        ItemComponentsPatch.registerHandledComponent(componentName);
        if (!adapter.name.equals(componentName)) {
            COMPONENTS_BY_PROPERTY.put(adapter.name, adapter.componentType);
        }
    }

    static {

        // <--[tag]
        // @attribute <ItemTag.is_overridden[<component>]>
        // @returns ElementTag(Boolean)
        // @description
        // Returns whether an item has a specific item component type overridden, see <@link language Item Components>.
        // -->
        ItemTag.tagProcessor.registerTag(ElementTag.class, ElementTag.class, "is_overridden", (attribute, object, param) -> {
            DataComponentType componentType = getComponentType(param.asString());
            if (componentType == null) {
                attribute.echoError("Invalid type specified, must be a valid item component type or property name.");
                return null;
            }
            return new ElementTag(object.getItemStack().isDataOverridden(componentType));
        });
    }

    public final DataComponentType.Valued<TP> componentType;
    public final Class<TD> denizenType;
    public final String name;

    public DataComponentAdapter(DataComponentType.Valued<TP> componentType, Class<TD> denizenType, String name) {
        this.componentType = componentType;
        this.denizenType = denizenType;
        this.name = name;
    }

    public abstract TD toDenizen(TP value);

    public abstract TP toPaper(TD value, Mechanism mechanism);

    public static <T> void setIfValid(Consumer<T> setter, MapTag data, String key, String type, Predicate<ElementTag> checker, Function<ElementTag, T> converter, Mechanism mechanism) {
        ElementTag value = data.getElement(key);
        if (value == null) {
            return;
        }
        T converted;
        if (!checker.test(value) || (converted = converter.apply(value)) == null) {
            mechanism.echoError("Invalid '" + key + "' specified: must be a " + type + '.');
            return;
        }
        setter.accept(converted);
    }

    public class Property extends ItemProperty<TD> {

        private static DataComponentAdapter<?, ?> currentlyRegisteringComponentAdapter;

        public Property(ItemTag item) {
            this.object = item;
        }

        @Override
        public TD getPropertyValue() {
            TP internalValue = getItemStack().getData(componentType);
            return internalValue == null ? null : toDenizen(internalValue);
        }

        @Override
        public TD getPropertyValueNoDefault() {
            return getItemStack().isDataOverridden(componentType) ? getPropertyValue() : null;
        }

        @Override
        public void setPropertyValue(TD value, Mechanism mechanism) {
            if (value == null) {
                getItemStack().resetData(componentType);
                return;
            }
            TP converted = toPaper(value, mechanism);
            if (converted != null) {
                getItemStack().setData(componentType, converted);
            }
        }

        @Override
        public String getPropertyId() {
            return name;
        }

        public static void register() {
            autoRegisterNullable(currentlyRegisteringComponentAdapter.name, DataComponentAdapter.Property.class, currentlyRegisteringComponentAdapter.denizenType, false);
        }
    }
}
