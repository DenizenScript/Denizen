package net.aufdemrand.denizen.scripts.commands.entity;

import net.aufdemrand.denizen.BukkitScriptEntryData;
import net.aufdemrand.denizen.objects.dEntity;
import net.aufdemrand.denizen.objects.dMaterial;
import net.aufdemrand.denizen.utilities.MaterialCompat;
import net.aufdemrand.denizen.utilities.debugging.dB;
import net.aufdemrand.denizencore.exceptions.CommandExecutionException;
import net.aufdemrand.denizencore.exceptions.InvalidArgumentsException;
import net.aufdemrand.denizencore.objects.Element;
import net.aufdemrand.denizencore.objects.aH;
import net.aufdemrand.denizencore.objects.dList;
import net.aufdemrand.denizencore.scripts.ScriptEntry;
import net.aufdemrand.denizencore.scripts.commands.AbstractCommand;
import net.citizensnpcs.api.trait.trait.Equipment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.Arrays;
import java.util.List;

public class HeadCommand extends AbstractCommand {

    @Override
    public void parseArgs(ScriptEntry scriptEntry) throws InvalidArgumentsException {

        for (aH.Argument arg : aH.interpret(scriptEntry.getArguments())) {

            if (!scriptEntry.hasObject("material")
                    && arg.matchesArgumentType(dMaterial.class)
                    && !arg.matchesPrefix("skin", "s")) {
                scriptEntry.addObject("material", arg.asType(dMaterial.class));
            }
            else if (!scriptEntry.hasObject("skin")
                    && (arg.matchesPrefix("skin", "s"))) {
                scriptEntry.addObject("skin", arg.asElement());
            }
            else if (!scriptEntry.hasObject("entities")
                    && arg.matches("player")
                    && ((BukkitScriptEntryData) scriptEntry.entryData).hasPlayer()) {
                scriptEntry.addObject("entities", Arrays.asList(((BukkitScriptEntryData) scriptEntry.entryData).getPlayer().getDenizenEntity()));
            }
            else if (!scriptEntry.hasObject("entities")
                    && arg.matchesArgumentList(dEntity.class)) {
                scriptEntry.addObject("entities", arg.asType(dList.class).filter(dEntity.class, scriptEntry));
            }
            else {
                arg.reportUnhandled();
            }
        }

        // Use the NPC or the Player as the default entity
        scriptEntry.defaultObject("entities",
                (((BukkitScriptEntryData) scriptEntry.entryData).hasNPC() ? Arrays.asList(((BukkitScriptEntryData) scriptEntry.entryData).getNPC().getDenizenEntity()) : null),
                (((BukkitScriptEntryData) scriptEntry.entryData).hasPlayer() ? Arrays.asList(((BukkitScriptEntryData) scriptEntry.entryData).getPlayer().getDenizenEntity()) : null));

        if (!scriptEntry.hasObject("skin") && !scriptEntry.hasObject("material")) {
            throw new InvalidArgumentsException("Must specify a skin or material!");
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public void execute(ScriptEntry scriptEntry) throws CommandExecutionException {

        List<dEntity> entities = (List<dEntity>) scriptEntry.getObject("entities");
        Element skin = scriptEntry.getElement("skin");
        dMaterial material = scriptEntry.getdObject("material");

        // Report to dB
        if (scriptEntry.dbCallShouldDebug()) {
            dB.report(scriptEntry, getName(),
                    aH.debugObj("entities", entities.toString()) +
                            (skin != null ? skin.debug() : "") + (material != null ? material.debug() : ""));
        }

        ItemStack item = null;

        // Create head item with chosen skin, or item/skin
        if (skin != null) {
            item = MaterialCompat.createPlayerHead();
            ItemMeta itemMeta = item.getItemMeta();
            ((SkullMeta) itemMeta).setOwner(skin.asString().replaceAll("[pP]@", "")); // TODO: 1.12 and up - switch to setOwningPlayer?
            item.setItemMeta(itemMeta);

        }
        else if (material != null) {
            item = new ItemStack(material.getMaterial());
        }

        // Loop through entities, apply the item/skin

        for (dEntity entity : entities) {
            if (entity.isCitizensNPC()) {
                if (!entity.getDenizenNPC().getCitizen().hasTrait(Equipment.class)) {
                    entity.getDenizenNPC().getCitizen().addTrait(Equipment.class);
                }
                Equipment trait = entity.getDenizenNPC().getCitizen().getTrait(Equipment.class);
                trait.set(1, item);

            }
            else if (entity.isPlayer()) {
                entity.getPlayer().getInventory().setHelmet(item);

            }
            else {
                if (entity.isLivingEntity() && entity.getLivingEntity().getEquipment() != null) {
                    entity.getLivingEntity().getEquipment().setHelmet(item);
                }
                else {
                    dB.echoError(scriptEntry.getResidingQueue(), entity.identify() + " is not a living entity!");
                }

            }
        }
    }
}
