package net.aufdemrand.denizen.scripts.commands.item;

import net.aufdemrand.denizen.objects.dItem;
import net.aufdemrand.denizen.utilities.debugging.dB;
import net.aufdemrand.denizen.utilities.nbt.CustomNBT;
import net.aufdemrand.denizencore.exceptions.InvalidArgumentsException;
import net.aufdemrand.denizencore.objects.Element;
import net.aufdemrand.denizencore.objects.aH;
import net.aufdemrand.denizencore.scripts.ScriptEntry;
import net.aufdemrand.denizencore.scripts.commands.AbstractCommand;
import org.bukkit.inventory.ItemStack;

public class NBTCommand extends AbstractCommand {

    @Override
    public void parseArgs(ScriptEntry scriptEntry) throws InvalidArgumentsException {

        for (aH.Argument arg : aH.interpret(scriptEntry.getArguments())) {

            if (!scriptEntry.hasObject("key")
                    && arg.raw_value.split(":", 2).length == 2) {
                String[] flagArgs = arg.raw_value.split(":", 2);
                scriptEntry.addObject("key", new Element(flagArgs[0]));
                scriptEntry.addObject("value", new Element(flagArgs[1]));
            }
            else if (!scriptEntry.hasObject("item")
                    && arg.matchesArgumentType(dItem.class)) {
                scriptEntry.addObject("item", arg.asType(dItem.class));
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

        dItem item = scriptEntry.getdObject("item");
        Element key = scriptEntry.getElement("key");
        Element value = scriptEntry.getElement("value");

        if (scriptEntry.dbCallShouldDebug()) {

            dB.report(scriptEntry, getName(), item.debug() + key.debug() + value.debug());

        }

        ItemStack itemStack = item.getItemStack();

        if (value.asString().equals("!")) {
            itemStack = CustomNBT.removeCustomNBT(itemStack, key.asString(), CustomNBT.KEY_DENIZEN);
        }
        else {
            itemStack = CustomNBT.addCustomNBT(itemStack, key.asString(), value.asString(), CustomNBT.KEY_DENIZEN);
        }

        scriptEntry.addObject("new_item", new dItem(itemStack));
    }
}
