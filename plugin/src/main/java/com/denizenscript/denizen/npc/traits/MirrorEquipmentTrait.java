package com.denizenscript.denizen.npc.traits;

import com.denizenscript.denizen.nms.NMSHandler;
import com.denizenscript.denizen.objects.ItemTag;
import com.denizenscript.denizen.scripts.commands.entity.FakeEquipCommand;
import com.denizenscript.denizen.scripts.commands.entity.RenameCommand;
import com.denizenscript.denizen.utilities.packets.NetworkInterceptHelper;
import net.citizensnpcs.api.persistence.Persist;
import net.citizensnpcs.api.trait.Trait;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockDispenseArmorEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EntityEquipment;

import java.util.HashMap;
import java.util.UUID;

public class MirrorEquipmentTrait extends Trait {

    @Persist("")
    public boolean mirror = true;

    public UUID mirroredUUID = null;

    public MirrorEquipmentTrait() {
        super("mirrorequipment");
    }

    public static class MirrorOverride extends FakeEquipCommand.EquipmentOverride {

        @Override
        public FakeEquipCommand.EquipmentOverride getVariantFor(Player player) {
            FakeEquipCommand.EquipmentOverride result = new FakeEquipCommand.EquipmentOverride();
            EntityEquipment playerEquip = player.getEquipment();
            result.hand = new ItemTag(playerEquip.getItemInMainHand());
            result.offhand = new ItemTag(playerEquip.getItemInOffHand());
            result.head = new ItemTag(playerEquip.getHelmet());
            result.chest = new ItemTag(playerEquip.getChestplate());
            result.legs = new ItemTag(playerEquip.getLeggings());
            result.boots = new ItemTag(playerEquip.getBoots());
            return result;
        }
    }

    public static MirrorOverride overrideInstance = new MirrorOverride();

    public void resend() {
        for (Player player : NMSHandler.entityHelper.getPlayersThatSee(npc.getEntity())) {
            NMSHandler.packetHelper.resetEquipment(player, (LivingEntity) npc.getEntity());
        }
    }

    public void resendIfNeeded(Player player) {
        if (player == null || !mirror || !npc.isSpawned()) {
            return;
        }
        if (!player.getWorld().equals(npc.getEntity().getWorld())) {
            return;
        }
        if (player.getLocation().distanceSquared(npc.getStoredLocation()) > 100 * 100) {
            return;
        }
        if (!NMSHandler.entityHelper.getPlayersThatSee(npc.getEntity()).contains(player)) {
            return;
        }
        NMSHandler.packetHelper.resetEquipment(player, (LivingEntity) npc.getEntity());
    }

    @EventHandler
    public void onPlayerInventoryClick(InventoryClickEvent event) {
        resendIfNeeded((Player) event.getWhoClicked());
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        resendIfNeeded(event.getPlayer());
    }

    @EventHandler
    public void onArmorDispense(BlockDispenseArmorEvent event) {
        if (event.getTargetEntity() instanceof Player) {
            resendIfNeeded((Player) event.getTargetEntity());
        }
    }

    public void mirrorOn() {
        NetworkInterceptHelper.enable();
        if (!npc.isSpawned()) {
            return;
        }
        if (!(npc.getEntity() instanceof LivingEntity)) {
            mirrorOff();
            return;
        }
        mirroredUUID = npc.getEntity().getUniqueId();
        HashMap<UUID, FakeEquipCommand.EquipmentOverride> mapping = FakeEquipCommand.overrides.computeIfAbsent(null, k -> new HashMap<>());
        mapping.put(mirroredUUID, overrideInstance);
    }

    public void mirrorOff() {
        if (mirroredUUID == null) {
            return;
        }
        HashMap<UUID, FakeEquipCommand.EquipmentOverride> mapping = FakeEquipCommand.overrides.get(null);
        if (mapping == null) {
            return;
        }
        if (mapping.remove(mirroredUUID) != null && npc.isSpawned()) {
            resend();
        }
    }

    public void enableMirror() {
        mirror = true;
        mirrorOn();
        if (npc.isSpawned()) {
            resend();
        }
    }

    public void disableMirror() {
        mirror = false;
        mirrorOff();
        if (RenameCommand.customNames.remove(mirroredUUID) != null && npc.isSpawned()) {
            resend();
        }
    }

    @Override
    public void onSpawn() {
        if (mirror) {
            mirrorOn();
        }
    }

    @Override
    public void onRemove() {
        if (mirror) {
            mirrorOff();
        }
    }

    @Override
    public void onAttach() {
        if (mirror) {
            mirrorOn();
        }
    }
}
