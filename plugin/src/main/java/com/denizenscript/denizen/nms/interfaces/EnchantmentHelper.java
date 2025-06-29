package com.denizenscript.denizen.nms.interfaces;

import com.denizenscript.denizen.Denizen;
import com.denizenscript.denizen.scripts.containers.core.EnchantmentScriptContainer;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.inventory.InventoryEvent;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.event.inventory.PrepareGrindstoneEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Map;

public class EnchantmentHelper {

    public Enchantment registerFakeEnchantment(EnchantmentScriptContainer.EnchantmentReference script) {
        throw new UnsupportedOperationException();
    }

    public String getRarity(Enchantment enchantment) {
        throw new UnsupportedOperationException();
    }

    public boolean isDiscoverable(Enchantment enchantment) {
        throw new UnsupportedOperationException();
    }

    public boolean isTradable(Enchantment enchantment) {
        throw new UnsupportedOperationException();
    }

    public boolean isCurse(Enchantment enchantment) {
        throw new UnsupportedOperationException();
    }

    public int getMinCost(Enchantment enchantment, int level) {
        throw new UnsupportedOperationException();
    }

    public int getMaxCost(Enchantment enchantment, int level) {
        throw new UnsupportedOperationException();
    }

    public String getFullName(Enchantment enchantment, int level) {
        throw new UnsupportedOperationException();
    }

    public float getDamageBonus(Enchantment enchantment, int level, String type) {
        throw new UnsupportedOperationException();
    }

    public int getDamageProtection(Enchantment enchantment, int level, EntityDamageEvent.DamageCause type, Entity attacker) {
        throw new UnsupportedOperationException();
    }

    public boolean eventsRegistered = false;

    public void verifyEventsRegistered() {
        if (eventsRegistered) {
            return;
        }
        Bukkit.getPluginManager().registerEvents(new EnchantmentBackSupportEvents(), Denizen.getInstance());
        eventsRegistered = true;
    }

    static class EnchantmentBackSupportEvents implements Listener {

        @EventHandler
        public void on(PrepareGrindstoneEvent event) {
            for (ItemStack input : event.getInventory().getContents()) {
                if (trySendingInventoryUpdate(input, event)) {
                    break;
                }
            }
        }

        @EventHandler
        public void on(PrepareAnvilEvent event) {
            trySendingInventoryUpdate(event.getResult(), event);
        }

        public static boolean trySendingInventoryUpdate(ItemStack item, InventoryEvent event) {
            if (item == null || item.getType() == Material.AIR) {
                return false;
            }
            ItemMeta itemMeta = item.getItemMeta();
            if (itemMeta == null || !itemMeta.hasEnchants()) {
                return false;
            }
            for (Enchantment enchantment : itemMeta.getEnchants().keySet()) {
                if (EnchantmentScriptContainer.getScriptFromEnchantment(enchantment) != null) {
                    Bukkit.getScheduler().runTaskLater(Denizen.getInstance(), () -> {
                        for (HumanEntity viewer : event.getViewers()) {
                            if (viewer instanceof Player player) {
                                player.updateInventory();
                            }
                        }
                    }, 1);
                    return true;
                }
            }
            return false;
        }

        @EventHandler(priority = EventPriority.MONITOR)
        public void on(EntityDamageByEntityEvent event) {
            if (event.getDamager() instanceof LivingEntity attacker && attacker.getEquipment() != null) {
                EntityEquipment equipment = attacker.getEquipment();
                processEnchantmentScripts(equipment.getItemInMainHand(), event, EnchantmentScriptContainer::doPostAttack);
                processEnchantmentScripts(equipment.getItemInOffHand(), event, EnchantmentScriptContainer::doPostAttack);
            }
            if (event.getEntity() instanceof LivingEntity attacked && attacked.getEquipment() != null) {
                EntityEquipment equipment = attacked.getEquipment();
                processEnchantmentScripts(equipment.getItemInMainHand(), event, EnchantmentScriptContainer::doPostHurt);
                processEnchantmentScripts(equipment.getItemInOffHand(), event, EnchantmentScriptContainer::doPostHurt);
                processEnchantmentScripts(equipment.getHelmet(), event, EnchantmentScriptContainer::doPostHurt);
                processEnchantmentScripts(equipment.getChestplate(), event, EnchantmentScriptContainer::doPostHurt);
                processEnchantmentScripts(equipment.getLeggings(), event, EnchantmentScriptContainer::doPostHurt);
                processEnchantmentScripts(equipment.getBoots(), event, EnchantmentScriptContainer::doPostHurt);
            }
        }

        @FunctionalInterface
        public interface EnchantmentSubScriptRunner {
            void runSubScript(EnchantmentScriptContainer enchantmentScript, Entity attacker, Entity victim, int level);
        }

        public static void processEnchantmentScripts(ItemStack item, EntityDamageByEntityEvent event, EnchantmentSubScriptRunner subScriptRunner) {
            if (item == null || item.getType() == Material.AIR) {
                return;
            }
            ItemMeta itemMeta = item.getItemMeta();
            if (itemMeta == null || !itemMeta.hasEnchants()) {
                return;
            }
            for (Map.Entry<Enchantment, Integer> entry : itemMeta.getEnchants().entrySet()) {
                EnchantmentScriptContainer enchantmentScript = EnchantmentScriptContainer.getScriptFromEnchantment(entry.getKey());
                if (enchantmentScript != null) {
                    subScriptRunner.runSubScript(enchantmentScript, event.getDamager(), event.getEntity(), entry.getValue());
                }
            }
        }
    }

    public enum Rarity {
        COMMON(10, 1),
        UNCOMMON(5, 2),
        RARE(2, 4),
        VERY_RARE(1, 8);

        final int weight, anvilCost;

        Rarity(int weight, int anvilCost) {
            this.weight = weight;
            this.anvilCost = anvilCost;
        }

        public int getWeight() {
            return weight;
        }

        public int getAnvilCost() {
            return anvilCost;
        }

        public static Rarity fromWeight(int weight) {
            int smallestDistance = Integer.MAX_VALUE;
            Rarity closest = null;
            for (Rarity rarity : Rarity.values()) {
                int distance = Math.abs(weight - rarity.getWeight());
                if (distance < smallestDistance) {
                    smallestDistance = distance;
                    closest = rarity;
                }
            }
            return closest;
        }
    }
}
