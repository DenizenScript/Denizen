package com.denizenscript.denizen.scripts.commands.entity;

import com.denizenscript.denizen.utilities.Utilities;
import com.denizenscript.denizen.utilities.debugging.Debug;
import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizen.objects.MaterialTag;
import com.denizenscript.denizencore.exceptions.InvalidArgumentsException;
import com.denizenscript.denizencore.objects.Argument;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.ArgumentHelper;
import com.denizenscript.denizencore.objects.core.ListTag;
import com.denizenscript.denizencore.scripts.ScriptEntry;
import com.denizenscript.denizencore.scripts.commands.AbstractCommand;
import com.denizenscript.denizencore.utilities.Deprecations;
import net.citizensnpcs.api.trait.trait.Equipment;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.Arrays;
import java.util.List;

public class HeadCommand extends AbstractCommand {

    public HeadCommand() {
        setName("head");
        setSyntax("head (<entity>|...) [skin:<player_name>]");
        setRequiredArguments(1, 2);
        isProcedural = false;
    }

    @Override
    public void parseArgs(ScriptEntry scriptEntry) throws InvalidArgumentsException {

        for (Argument arg : scriptEntry.getProcessedArgs()) {

            if (!scriptEntry.hasObject("material")
                    && arg.matchesArgumentType(MaterialTag.class)
                    && !arg.matchesPrefix("skin", "s")) {
                scriptEntry.addObject("material", arg.asType(MaterialTag.class));
            }
            else if (!scriptEntry.hasObject("skin")
                    && (arg.matchesPrefix("skin", "s"))) {
                scriptEntry.addObject("skin", arg.asElement());
            }
            else if (!scriptEntry.hasObject("entities")
                    && arg.matches("player")
                    && Utilities.entryHasPlayer(scriptEntry)) {
                scriptEntry.addObject("entities", Arrays.asList(Utilities.getEntryPlayer(scriptEntry).getDenizenEntity()));
            }
            else if (!scriptEntry.hasObject("entities")
                    && arg.matchesArgumentList(EntityTag.class)) {
                scriptEntry.addObject("entities", arg.asType(ListTag.class).filter(EntityTag.class, scriptEntry));
            }
            else {
                arg.reportUnhandled();
            }
        }

        // Use the NPC or the Player as the default entity
        scriptEntry.defaultObject("entities",
                (Utilities.entryHasNPC(scriptEntry) ? Arrays.asList(Utilities.getEntryNPC(scriptEntry).getDenizenEntity()) : null),
                (Utilities.entryHasPlayer(scriptEntry) ? Arrays.asList(Utilities.getEntryPlayer(scriptEntry).getDenizenEntity()) : null));

        if (!scriptEntry.hasObject("skin") && !scriptEntry.hasObject("material")) {
            throw new InvalidArgumentsException("Must specify a skin or material!");
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public void execute(ScriptEntry scriptEntry) {
        Deprecations.headCommand.warn(scriptEntry);
        List<EntityTag> entities = (List<EntityTag>) scriptEntry.getObject("entities");
        ElementTag skin = scriptEntry.getElement("skin");
        MaterialTag material = scriptEntry.getObjectTag("material");

        if (scriptEntry.dbCallShouldDebug()) {
            Debug.report(scriptEntry, getName(),
                    ArgumentHelper.debugObj("entities", entities.toString()) +
                            (skin != null ? skin.debug() : "") + (material != null ? material.debug() : ""));
        }

        ItemStack item = null;

        // Create head item with chosen skin, or item/skin
        if (skin != null) {
            item = new ItemStack(Material.PLAYER_HEAD);
            ItemMeta itemMeta = item.getItemMeta();
            ((SkullMeta) itemMeta).setOwner(skin.asString());
            item.setItemMeta(itemMeta);

        }
        else if (material != null) {
            item = new ItemStack(material.getMaterial());
        }

        // Loop through entities, apply the item/skin

        for (EntityTag entity : entities) {
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
                    Debug.echoError(scriptEntry.getResidingQueue(), entity.identify() + " is not a living entity!");
                }

            }
        }
    }
}
