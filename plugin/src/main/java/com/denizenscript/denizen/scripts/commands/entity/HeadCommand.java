package com.denizenscript.denizen.scripts.commands.entity;

import com.denizenscript.denizen.utilities.Utilities;
import com.denizenscript.denizencore.utilities.debugging.Debug;
import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizen.objects.MaterialTag;
import com.denizenscript.denizencore.exceptions.InvalidArgumentsException;
import com.denizenscript.denizencore.objects.Argument;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.core.ListTag;
import com.denizenscript.denizencore.scripts.ScriptEntry;
import com.denizenscript.denizencore.scripts.commands.AbstractCommand;
import com.denizenscript.denizen.utilities.BukkitImplDeprecations;
import net.citizensnpcs.api.trait.trait.Equipment;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.Collections;
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
        for (Argument arg : scriptEntry) {
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
                scriptEntry.addObject("entities", Collections.singletonList(Utilities.getEntryPlayer(scriptEntry).getDenizenEntity()));
            }
            else if (!scriptEntry.hasObject("entities")
                    && arg.matchesArgumentList(EntityTag.class)) {
                scriptEntry.addObject("entities", arg.asType(ListTag.class).filter(EntityTag.class, scriptEntry));
            }
            else {
                arg.reportUnhandled();
            }
        }
        scriptEntry.defaultObject("entities", Utilities.entryDefaultEntityList(scriptEntry, false));
        if (!scriptEntry.hasObject("skin") && !scriptEntry.hasObject("material")) {
            throw new InvalidArgumentsException("Must specify a skin or material!");
        }
    }

    @Override
    public void execute(ScriptEntry scriptEntry) {
        BukkitImplDeprecations.headCommand.warn(scriptEntry);
        List<EntityTag> entities = (List<EntityTag>) scriptEntry.getObject("entities");
        ElementTag skin = scriptEntry.getElement("skin");
        MaterialTag material = scriptEntry.getObjectTag("material");
        if (scriptEntry.dbCallShouldDebug()) {
            Debug.report(scriptEntry, getName(), db("entities", entities), skin, material);
        }
        ItemStack item = null;
        if (skin != null) {
            item = new ItemStack(Material.PLAYER_HEAD);
            ItemMeta itemMeta = item.getItemMeta();
            ((SkullMeta) itemMeta).setOwner(skin.asString());
            item.setItemMeta(itemMeta);
        }
        else if (material != null) {
            item = new ItemStack(material.getMaterial());
        }
        for (EntityTag entity : entities) {
            if (entity.isCitizensNPC()) {
                if (!entity.getDenizenNPC().getCitizen().hasTrait(Equipment.class)) {
                    entity.getDenizenNPC().getCitizen().addTrait(Equipment.class);
                }
                Equipment trait = entity.getDenizenNPC().getCitizen().getOrAddTrait(Equipment.class);
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
                    Debug.echoError(scriptEntry, entity.identify() + " is not a living entity!");
                }
            }
        }
    }
}
