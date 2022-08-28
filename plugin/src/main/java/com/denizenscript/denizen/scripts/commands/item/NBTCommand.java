package com.denizenscript.denizen.scripts.commands.item;

import com.denizenscript.denizencore.utilities.debugging.Debug;
import com.denizenscript.denizen.utilities.nbt.CustomNBT;
import com.denizenscript.denizen.objects.ItemTag;
import com.denizenscript.denizencore.exceptions.InvalidArgumentsException;
import com.denizenscript.denizencore.objects.Argument;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.scripts.ScriptEntry;
import com.denizenscript.denizencore.scripts.commands.AbstractCommand;
import com.denizenscript.denizen.utilities.BukkitImplDeprecations;
import org.bukkit.inventory.ItemStack;

public class NBTCommand extends AbstractCommand {

    public NBTCommand() {
        setName("nbt");
        setSyntax("(Deprecated)");
        setRequiredArguments(0, -1);
        isProcedural = false;
    }

    @Override
    public void parseArgs(ScriptEntry scriptEntry) throws InvalidArgumentsException {

        for (Argument arg : scriptEntry) {

            if (!scriptEntry.hasObject("key")
                    && arg.getRawValue().split(":", 2).length == 2) {
                String[] flagArgs = arg.getRawValue().split(":", 2);
                scriptEntry.addObject("key", new ElementTag(flagArgs[0]));
                scriptEntry.addObject("value", new ElementTag(flagArgs[1]));
            }
            else if (!scriptEntry.hasObject("item")
                    && arg.matchesArgumentType(ItemTag.class)) {
                scriptEntry.addObject("item", arg.asType(ItemTag.class));
            }
            else {
                arg.reportUnhandled();
            }

        }

        if (!scriptEntry.hasObject("item")) {
            throw new InvalidArgumentsException("Must specify item!");
        }

        if (!scriptEntry.hasObject("key") || !scriptEntry.hasObject("value")) {
            throw new InvalidArgumentsException("Must specify key and value!");
        }
    }

    @Override
    public void execute(ScriptEntry scriptEntry) {

        BukkitImplDeprecations.nbtCommand.warn(scriptEntry);

        ItemTag item = scriptEntry.getObjectTag("item");
        ElementTag key = scriptEntry.getElement("key");
        ElementTag value = scriptEntry.getElement("value");

        if (scriptEntry.dbCallShouldDebug()) {
            Debug.report(scriptEntry, getName(), item, key, value);
        }

        ItemStack itemStack = item.getItemStack();

        if (value.asString().equals("!")) {
            itemStack = CustomNBT.removeCustomNBT(itemStack, key.asString(), CustomNBT.KEY_DENIZEN);
        }
        else {
            itemStack = CustomNBT.addCustomNBT(itemStack, key.asString(), value.asString(), CustomNBT.KEY_DENIZEN);
        }

        scriptEntry.addObject("new_item", new ItemTag(itemStack));
    }
}
