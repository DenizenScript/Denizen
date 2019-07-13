package com.denizenscript.denizen.scripts.commands.entity;

import com.denizenscript.denizen.utilities.MaterialCompat;
import com.denizenscript.denizen.utilities.Utilities;
import com.denizenscript.denizen.utilities.debugging.Debug;
import com.denizenscript.denizen.objects.dEntity;
import com.denizenscript.denizen.objects.dMaterial;
import com.denizenscript.denizencore.exceptions.InvalidArgumentsException;
import com.denizenscript.denizencore.objects.Argument;
import com.denizenscript.denizencore.objects.ElementTag;
import com.denizenscript.denizencore.objects.ArgumentHelper;
import com.denizenscript.denizencore.objects.ListTag;
import com.denizenscript.denizencore.scripts.ScriptEntry;
import com.denizenscript.denizencore.scripts.commands.AbstractCommand;
import net.citizensnpcs.api.trait.trait.Equipment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.Arrays;
import java.util.List;

public class HeadCommand extends AbstractCommand {

    // <--[command]
    // @Name Head
    // @Syntax head (<entity>|...) [skin:<player_name>]
    // @Required 1
    // @Short Makes players or NPCs wear a specific player's head.
    // @Group entity
    //
    // @Description
    // Equips a player's head onto the player(s) or npc(s) specified. If no player or npc is specified, it defaults
    // to the player attached to the script queue. It accepts a single entity or list of entities.
    //
    // @Tags
    // <i@item.skin>
    // <i@item.has_skin>
    //
    // @Usage
    // Use to stick an awesome head on your head with the head command.
    // - head <player> skin:mcmonkey4eva
    //
    // @Usage
    // Use to equip an npc with id 5 with your own head.
    // - head n@5 skin:<player.name>
    //
    // @Usage
    // Use to equip all online players with Notch's head.
    // - head <server.list_online_players> skin:Notch
    // -->

    @Override
    public void parseArgs(ScriptEntry scriptEntry) throws InvalidArgumentsException {

        for (Argument arg : ArgumentHelper.interpretArguments(scriptEntry.aHArgs)) {

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
                    && Utilities.entryHasPlayer(scriptEntry)) {
                scriptEntry.addObject("entities", Arrays.asList(Utilities.getEntryPlayer(scriptEntry).getDenizenEntity()));
            }
            else if (!scriptEntry.hasObject("entities")
                    && arg.matchesArgumentList(dEntity.class)) {
                scriptEntry.addObject("entities", arg.asType(ListTag.class).filter(dEntity.class, scriptEntry));
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

        List<dEntity> entities = (List<dEntity>) scriptEntry.getObject("entities");
        ElementTag skin = scriptEntry.getElement("skin");
        dMaterial material = scriptEntry.getdObject("material");

        // Report to dB
        if (scriptEntry.dbCallShouldDebug()) {
            Debug.report(scriptEntry, getName(),
                    ArgumentHelper.debugObj("entities", entities.toString()) +
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
                    Debug.echoError(scriptEntry.getResidingQueue(), entity.identify() + " is not a living entity!");
                }

            }
        }
    }
}
