package com.denizenscript.denizen.utilities.inventory;

import com.denizenscript.denizencore.utilities.ReflectionHelper;
import com.denizenscript.denizencore.utilities.debugging.Debug;
import org.bukkit.entity.HumanEntity;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;

import java.lang.invoke.MethodHandle;

public class InventoryViewUtil {

    private static <T> T get(MethodHandle methodHandle, InventoryView view) {
        try {
            return (T) methodHandle.invoke(view);
        }
        catch (Throwable e) {
            Debug.echoError(e);
            return null;
        }
    }

    private static <T> T get(MethodHandle methodHandle, InventoryView view, int num) {
        try {
            return (T) methodHandle.invoke(view, num);
        }
        catch (Throwable e) {
            Debug.echoError(e);
            return null;
        }
    }

    private static final MethodHandle GET_TOP_INVENTORY = ReflectionHelper.getMethodHandle(InventoryView.class, "getTopInventory");

    public static Inventory getTopInventory(InventoryView view) {
        return get(GET_TOP_INVENTORY, view);
    }

    private static final MethodHandle GET_PLAYER = ReflectionHelper.getMethodHandle(InventoryView.class, "getPlayer");

    public static HumanEntity getPlayer(InventoryView view) {
        return get(GET_PLAYER, view);
    }

    private static final MethodHandle GET_TYPE = ReflectionHelper.getMethodHandle(InventoryView.class, "getType");

    public static InventoryType getType(InventoryView view) {
        return get(GET_TYPE, view);
    }

    private static final MethodHandle GET_ITEM = ReflectionHelper.getMethodHandle(InventoryView.class, "getItem", int.class);

    public static ItemStack getItem(InventoryView view, int slot) {
        return get(GET_ITEM, view, slot);
    }

    private static final MethodHandle GET_INVENTORY = ReflectionHelper.getMethodHandle(InventoryView.class, "getInventory", int.class);

    public static Inventory getInventory(InventoryView view, int slot) {
        return get(GET_INVENTORY, view, slot);
    }
}
