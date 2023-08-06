package com.denizenscript.denizen.scripts.containers;

import com.denizenscript.denizen.scripts.containers.core.*;
import com.denizenscript.denizen.utilities.depends.Depends;
import com.denizenscript.denizencore.scripts.ScriptRegistry;

public class ContainerRegistry {

    public static void registerMainContainers() {
        ScriptRegistry._registerType("assignment", AssignmentScriptContainer.class);
        ScriptRegistry._registerType("book", BookScriptContainer.class);
        ScriptRegistry._registerType("command", CommandScriptContainer.class);
        if (Depends.vault != null) {
            ScriptRegistry._registerType("economy", EconomyScriptContainer.class);
        }
        ScriptRegistry._registerType("enchantment", EnchantmentScriptContainer.class);
        ScriptRegistry._registerType("entity", EntityScriptContainer.class);
        ScriptRegistry._registerType("format", FormatScriptContainer.class);
        ScriptRegistry._registerType("interact", InteractScriptContainer.class);
        ScriptRegistry._registerType("inventory", InventoryScriptContainer.class);
        ScriptRegistry._registerType("item", ItemScriptContainer.class);
        ScriptRegistry._registerType("map", MapScriptContainer.class);
        ScriptRegistry._registerType("version", VersionScriptContainer.class);
    }
}
