package com.denizenscript.denizen.paper.datacomponents;

import com.denizenscript.denizen.objects.ItemTag;
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
import org.bukkit.Registry;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

public abstract class DataComponentAdapter<D extends ObjectTag, C extends DataComponentType> {

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
    // - adjust def:apple remove_component:food
    // # This check will pass, as the apple's "food" component is overridden to have no value.
    // - if <[apple].is_overridden[food]>:
    //   - narrate "The apple has a modified food component! It will behave differently to a normal apple."
    // # We reset the apple item's food component by adjusting with no value, making it a normal apple.
    // - adjust def:apple food:
    // </code>
    // -->

    public static final Map<String, DataComponentType> COMPONENTS_BY_PROPERTY = new HashMap<>();
    public static final String[] EMPTY_STRING_ARRAY = new String[0];

    public static DataComponentType getComponentType(String name) {
        String nameLower = CoreUtilities.toLowerCase(name);
        DataComponentType componentType = Registry.DATA_COMPONENT_TYPE.get(Utilities.parseNamespacedKey(nameLower));
        if (componentType == null) {
            componentType = COMPONENTS_BY_PROPERTY.get(nameLower);
        }
        return componentType;
    }

    public static void register(DataComponentAdapter<?, ?> adapter) {
        DataComponentAdapter.Property.currentlyRegisteringComponentAdapter = adapter;
        PropertyParser.registerPropertyGetter(
                item -> !item.getItemStack().isEmpty() ? adapter.new Property(item) : null,
                ItemTag.class, EMPTY_STRING_ARRAY, EMPTY_STRING_ARRAY, DataComponentAdapter.Property.class);
        DataComponentAdapter.Property.currentlyRegisteringComponentAdapter = null;
        String componentName = adapter.componentType.key().asMinimalString();
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

    public final C componentType;
    public final Class<D> denizenType;
    public final String name;

    public DataComponentAdapter(C componentType, Class<D> denizenType, String name) {
        this.componentType = componentType;
        this.denizenType = denizenType;
        this.name = name;
    }

    public abstract D getValue(ItemStack item);

    public abstract void setValue(ItemStack item, D value, Mechanism mechanism);

    public boolean isDefaultValue(D value) {
        return false;
    }

    public static abstract class NonValued extends DataComponentAdapter<ElementTag, DataComponentType.NonValued> {

        public NonValued(DataComponentType.NonValued componentType, String name) {
            super(componentType, ElementTag.class, name);
        }

        @Override
        public ElementTag getValue(ItemStack item) {
            return new ElementTag(item.hasData(componentType));
        }

        @Override
        public void setValue(ItemStack item, ElementTag value, Mechanism mechanism) {
            if (!mechanism.requireBoolean()) {
                return;
            }
            if (value.asBoolean()) {
                item.setData(componentType);
            }
            else {
                item.unsetData(componentType);
            }
        }

        // Overridden and false = removed, managed by ItemTag.removed_components
        @Override
        public boolean isDefaultValue(ElementTag value) {
            return !value.asBoolean();
        }
    }

    public static abstract class Valued<D extends ObjectTag, P> extends DataComponentAdapter<D, DataComponentType.Valued<P>> {

        public static <T, D extends ObjectTag> void setIfValid(Consumer<T> setter, MapTag data, String key, Class<D> objectType, Predicate<D> checker, Function<D, T> converter, String type, Mechanism mechanism) {
            D value = data.getObjectAs(key, objectType, mechanism.context);
            if (value == null) {
                return;
            }
            T converted;
            if ((checker != null && !checker.test(value)) || (converted = converter.apply(value)) == null) {
                mechanism.echoError("Invalid '" + key + "' specified: must be a " + type + '.');
                return;
            }
            setter.accept(converted);
        }

        public Valued(Class<D> denizenType, DataComponentType.Valued<P> componentType, String name) {
            super(componentType, denizenType, name);
        }

        public abstract D toDenizen(P value);

        public abstract P fromDenizen(D value, Mechanism mechanism);

        @Override
        public D getValue(ItemStack item) {
            P data = item.getData(componentType);
            return data != null ? toDenizen(data) : null;
        }

        @Override
        public void setValue(ItemStack item, D value, Mechanism mechanism) {
            P converted = fromDenizen(value, mechanism);
            if (converted != null) {
                item.setData(componentType, converted);
            }
        }
    }

    public class Property extends ItemProperty<D> {

        private static DataComponentAdapter<?, ?> currentlyRegisteringComponentAdapter;

        public Property(ItemTag item) {
            this.object = item;
        }

        @Override
        public D getPropertyValue() {
            return getValue(getItemStack());
        }

        @Override
        public D getPropertyValueNoDefault() {
            if (!getItemStack().isDataOverridden(componentType)) {
                return null;
            }
            return super.getPropertyValueNoDefault();
        }

        @Override
        public boolean isDefaultValue(D value) {
            return DataComponentAdapter.this.isDefaultValue(value);
        }

        @Override
        public void setPropertyValue(D value, Mechanism mechanism) {
            if (value == null) {
                getItemStack().resetData(componentType);
                return;
            }
            setValue(getItemStack(), value, mechanism);
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
