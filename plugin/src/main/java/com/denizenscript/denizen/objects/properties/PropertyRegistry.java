package com.denizenscript.denizen.objects.properties;

import com.denizenscript.denizen.objects.*;
import com.denizenscript.denizen.objects.properties.bukkit.*;
import com.denizenscript.denizen.objects.properties.entity.*;
import com.denizenscript.denizen.objects.properties.inventory.*;
import com.denizenscript.denizen.objects.properties.item.*;
import com.denizenscript.denizen.objects.properties.material.*;
import com.denizenscript.denizen.objects.properties.trade.*;
import com.denizenscript.denizen.nms.NMSHandler;
import com.denizenscript.denizen.nms.NMSVersion;
import com.denizenscript.denizencore.objects.properties.PropertyParser;

public class PropertyRegistry {

    public static void registerExtensions() {
        BukkitBinaryTagExtensions.register();
        BukkitElementExtensions.register();
        BukkitListExtensions.register();
        BukkitMapTagExtensions.register();
        BukkitQueueExtensions.register();
        BukkitScriptExtensions.register();
    }

    public static void registerMainProperties() {
        registerExtensions();

        // register core EntityTag properties
        PropertyParser.registerProperty(EntityAge.class, EntityTag.class);
        PropertyParser.registerProperty(EntityAggressive.class, EntityTag.class);
        PropertyParser.registerProperty(EntityAI.class, EntityTag.class);
        PropertyParser.registerProperty(EntityAnger.class, EntityTag.class);
        PropertyParser.registerProperty(EntityAngry.class, EntityTag.class);
        PropertyParser.registerProperty(EntityAreaEffectCloud.class, EntityTag.class);
        PropertyParser.registerProperty(EntityArmorBonus.class, EntityTag.class);
        PropertyParser.registerProperty(EntityArrowDamage.class, EntityTag.class);
        PropertyParser.registerProperty(EntityAttributeBaseValues.class, EntityTag.class);
        PropertyParser.registerProperty(EntityAttributeModifiers.class, EntityTag.class);
        PropertyParser.registerProperty(EntityArmorPose.class, EntityTag.class);
        PropertyParser.registerProperty(EntityArms.class, EntityTag.class);
        PropertyParser.registerProperty(EntityAware.class, EntityTag.class);
        PropertyParser.registerProperty(EntityBasePlate.class, EntityTag.class);
        PropertyParser.registerProperty(EntityBeamTarget.class, EntityTag.class);
        PropertyParser.registerProperty(EntityBoatType.class, EntityTag.class);
        PropertyParser.registerProperty(EntityBodyArrows.class, EntityTag.class);
        PropertyParser.registerProperty(EntityBoundingBox.class, EntityTag.class);
        PropertyParser.registerProperty(EntityCanJoinRaid.class, EntityTag.class);
        PropertyParser.registerProperty(EntityCharged.class, EntityTag.class);
        PropertyParser.registerProperty(EntityChestCarrier.class, EntityTag.class);
        PropertyParser.registerProperty(EntityColor.class, EntityTag.class);
        PropertyParser.registerProperty(EntityConversionPlayer.class, EntityTag.class);
        PropertyParser.registerProperty(EntityConversionTime.class, EntityTag.class);
        PropertyParser.registerProperty(EntityCritical.class, EntityTag.class);
        PropertyParser.registerProperty(EntityCustomName.class, EntityTag.class);
        PropertyParser.registerProperty(EntityCustomNameVisible.class, EntityTag.class);
        if (NMSHandler.getVersion().isAtLeast(NMSVersion.v1_17)) {
            PropertyParser.registerProperty(EntityDarkDuration.class, EntityTag.class);
        }
        PropertyParser.registerProperty(EntityDirection.class, EntityTag.class);
        PropertyParser.registerProperty(EntityDisabledSlots.class, EntityTag.class);
        PropertyParser.registerProperty(EntityPotionEffects.class, EntityTag.class);
        PropertyParser.registerProperty(EntityEquipment.class, EntityTag.class);
        PropertyParser.registerProperty(EntityExplosionFire.class, EntityTag.class);
        PropertyParser.registerProperty(EntityExplosionRadius.class, EntityTag.class);
        PropertyParser.registerProperty(EntityFirework.class, EntityTag.class);
        if (NMSHandler.getVersion().isAtLeast(NMSVersion.v1_17)) {
            PropertyParser.registerProperty(EntityFireworkLifetime.class, EntityTag.class);
        }
        PropertyParser.registerProperty(EntityFixed.class, EntityTag.class);
        PropertyParser.registerProperty(EntityFlags.class, EntityTag.class);
        PropertyParser.registerProperty(EntityFlower.class, EntityTag.class);
        if (NMSHandler.getVersion().isAtLeast(NMSVersion.v1_17)) {
            PropertyParser.registerProperty(EntityFreezeDuration.class, EntityTag.class);
        }
        PropertyParser.registerProperty(EntityFramed.class, EntityTag.class);
        PropertyParser.registerProperty(EntityGravity.class, EntityTag.class);
        PropertyParser.registerProperty(EntityHasNectar.class, EntityTag.class);
        PropertyParser.registerProperty(EntityHasStung.class, EntityTag.class);
        PropertyParser.registerProperty(EntityHealth.class, EntityTag.class);
        if (NMSHandler.getVersion().isAtLeast(NMSVersion.v1_19)) {
            PropertyParser.registerProperty(EntityHorns.class, EntityTag.class);
        }
        PropertyParser.registerProperty(EntityHive.class, EntityTag.class);
        PropertyParser.registerProperty(EntityImmune.class, EntityTag.class);
        PropertyParser.registerProperty(EntityInventory.class, EntityTag.class);
        PropertyParser.registerProperty(EntityInvulnerable.class, EntityTag.class);
        if (NMSHandler.getVersion().isAtLeast(NMSVersion.v1_17)) {
            PropertyParser.registerProperty(EntityInWaterTime.class, EntityTag.class);
        }
        PropertyParser.registerProperty(EntityIsShowingBottom.class, EntityTag.class);
        PropertyParser.registerProperty(EntityItem.class, EntityTag.class);
        PropertyParser.registerProperty(EntityItemInHand.class, EntityTag.class);
        PropertyParser.registerProperty(EntityItemInOffHand.class, EntityTag.class);
        PropertyParser.registerProperty(EntityJumpStrength.class, EntityTag.class);
        PropertyParser.registerProperty(EntityKnockback.class, EntityTag.class);
        PropertyParser.registerProperty(EntityMarker.class, EntityTag.class);
        PropertyParser.registerProperty(EntityMaterial.class, EntityTag.class);
        PropertyParser.registerProperty(EntityMaxFuseTicks.class, EntityTag.class);
        PropertyParser.registerProperty(EntityPainting.class, EntityTag.class);
        PropertyParser.registerProperty(EntityPatrolLeader.class, EntityTag.class);
        PropertyParser.registerProperty(EntityPatrolTarget.class, EntityTag.class);
        PropertyParser.registerProperty(EntityPickupStatus.class, EntityTag.class);
        PropertyParser.registerProperty(EntityPlayerCreated.class, EntityTag.class);
        if (NMSHandler.getVersion().isAtLeast(NMSVersion.v1_17)) {
            PropertyParser.registerProperty(EntityPlayingDead.class, EntityTag.class);
        }
        PropertyParser.registerProperty(EntityPotion.class, EntityTag.class);
        PropertyParser.registerProperty(EntityPowered.class, EntityTag.class);
        PropertyParser.registerProperty(EntityProfession.class, EntityTag.class);
        PropertyParser.registerProperty(EntityPumpkinHead.class, EntityTag.class);
        PropertyParser.registerProperty(EntityRiptide.class, EntityTag.class);
        PropertyParser.registerProperty(EntityRotation.class, EntityTag.class);
        PropertyParser.registerProperty(EntityScoreboardTags.class, EntityTag.class);
        PropertyParser.registerProperty(EntitySmall.class, EntityTag.class);
        PropertyParser.registerProperty(EntityShivering.class, EntityTag.class);
        PropertyParser.registerProperty(EntityShotAtAngle.class, EntityTag.class);
        PropertyParser.registerProperty(EntityShulkerPeek.class, EntityTag.class);
        PropertyParser.registerProperty(EntitySilent.class, EntityTag.class);
        PropertyParser.registerProperty(EntitySitting.class, EntityTag.class);
        PropertyParser.registerProperty(EntitySize.class, EntityTag.class);
        PropertyParser.registerProperty(EntitySpeed.class, EntityTag.class);
        PropertyParser.registerProperty(EntitySpell.class, EntityTag.class);
        PropertyParser.registerProperty(EntityStrength.class, EntityTag.class);
        PropertyParser.registerProperty(EntityTame.class, EntityTag.class);
        PropertyParser.registerProperty(EntityEyeTargetLocation.class, EntityTag.class);
        PropertyParser.registerProperty(EntityTrades.class, EntityTag.class);
        PropertyParser.registerProperty(EntityVillagerExperience.class, EntityTag.class);
        PropertyParser.registerProperty(EntityVillagerLevel.class, EntityTag.class);
        PropertyParser.registerProperty(EntityVisible.class, EntityTag.class);

        // register core InventoryTag properties
        PropertyParser.registerProperty(InventoryContents.class, InventoryTag.class);
        PropertyParser.registerProperty(InventoryHolder.class, InventoryTag.class);
        PropertyParser.registerProperty(InventorySize.class, InventoryTag.class);
        PropertyParser.registerProperty(InventoryTitle.class, InventoryTag.class);
        PropertyParser.registerProperty(InventoryTrades.class, InventoryTag.class);
        PropertyParser.registerProperty(InventoryUniquifier.class, InventoryTag.class);

        // register core ItemTag properties
        PropertyParser.registerProperty(ItemArmorPose.class, ItemTag.class);
        PropertyParser.registerProperty(ItemAttributeModifiers.class, ItemTag.class);
        PropertyParser.registerProperty(ItemAttributeNBT.class, ItemTag.class);
        PropertyParser.registerProperty(ItemBaseColor.class, ItemTag.class);
        PropertyParser.registerProperty(ItemBlockMaterial.class, ItemTag.class);
        PropertyParser.registerProperty(ItemBook.class, ItemTag.class);
        PropertyParser.registerProperty(ItemBookGeneration.class, ItemTag.class);
        PropertyParser.registerProperty(ItemDisplayname.class, ItemTag.class);
        PropertyParser.registerProperty(ItemDurability.class, ItemTag.class);
        PropertyParser.registerProperty(ItemCanDestroy.class, ItemTag.class);
        PropertyParser.registerProperty(ItemCanPlaceOn.class, ItemTag.class);
        PropertyParser.registerProperty(ItemColor.class, ItemTag.class);
        PropertyParser.registerProperty(ItemCustomModel.class, ItemTag.class);
        PropertyParser.registerProperty(ItemChargedProjectile.class, ItemTag.class);
        PropertyParser.registerProperty(ItemEnchantments.class, ItemTag.class);
        PropertyParser.registerProperty(ItemFirework.class, ItemTag.class);
        PropertyParser.registerProperty(ItemFlags.class, ItemTag.class);
        PropertyParser.registerProperty(ItemFrameInvisible.class, ItemTag.class);
        PropertyParser.registerProperty(ItemHidden.class, ItemTag.class);
        PropertyParser.registerProperty(ItemInventory.class, ItemTag.class);
        PropertyParser.registerProperty(ItemKnowledgeBookRecipes.class, ItemTag.class);
        PropertyParser.registerProperty(ItemLock.class, ItemTag.class);
        PropertyParser.registerProperty(ItemLodestoneLocation.class, ItemTag.class);
        PropertyParser.registerProperty(ItemLodestoneTracked.class, ItemTag.class);
        PropertyParser.registerProperty(ItemLore.class, ItemTag.class);
        PropertyParser.registerProperty(ItemMap.class, ItemTag.class);
        PropertyParser.registerProperty(ItemNBT.class, ItemTag.class);
        PropertyParser.registerProperty(ItemPatterns.class, ItemTag.class);
        PropertyParser.registerProperty(ItemPotion.class, ItemTag.class);
        PropertyParser.registerProperty(ItemQuantity.class, ItemTag.class);
        PropertyParser.registerProperty(ItemRawNBT.class, ItemTag.class);
        PropertyParser.registerProperty(ItemRepairCost.class, ItemTag.class);
        PropertyParser.registerProperty(ItemScript.class, ItemTag.class);
        PropertyParser.registerProperty(ItemSignContents.class, ItemTag.class);
        PropertyParser.registerProperty(ItemSkullskin.class, ItemTag.class);
        PropertyParser.registerProperty(ItemSpawnerCount.class, ItemTag.class);
        PropertyParser.registerProperty(ItemSpawnerDelay.class, ItemTag.class);
        PropertyParser.registerProperty(ItemSpawnerMaxNearbyEntities.class, ItemTag.class);
        PropertyParser.registerProperty(ItemSpawnerPlayerRange.class, ItemTag.class);
        PropertyParser.registerProperty(ItemSpawnerRange.class, ItemTag.class);
        PropertyParser.registerProperty(ItemSpawnerType.class, ItemTag.class);
        PropertyParser.registerProperty(ItemUnbreakable.class, ItemTag.class);

        // register core MaterialTag properties
        PropertyParser.registerProperty(MaterialAge.class, MaterialTag.class);
        PropertyParser.registerProperty(MaterialAttached.class, MaterialTag.class);
        PropertyParser.registerProperty(MaterialBlockType.class, MaterialTag.class);
        PropertyParser.registerProperty(MaterialBrewingStand.class, MaterialTag.class);
        PropertyParser.registerProperty(MaterialCampfire.class, MaterialTag.class);
        PropertyParser.registerProperty(MaterialCount.class, MaterialTag.class);
        PropertyParser.registerProperty(MaterialDelay.class, MaterialTag.class);
        PropertyParser.registerProperty(MaterialDirectional.class, MaterialTag.class);
        PropertyParser.registerProperty(MaterialDistance.class, MaterialTag.class);
        PropertyParser.registerProperty(MaterialDrags.class, MaterialTag.class);
        PropertyParser.registerProperty(MaterialFaces.class, MaterialTag.class);
        PropertyParser.registerProperty(MaterialHalf.class, MaterialTag.class);
        PropertyParser.registerProperty(MaterialHinge.class, MaterialTag.class);
        PropertyParser.registerProperty(MaterialInstrument.class, MaterialTag.class);
        PropertyParser.registerProperty(MaterialLocked.class, MaterialTag.class);
        PropertyParser.registerProperty(MaterialLeafSize.class, MaterialTag.class);
        PropertyParser.registerProperty(MaterialLevel.class, MaterialTag.class);
        PropertyParser.registerProperty(MaterialLightable.class, MaterialTag.class);
        PropertyParser.registerProperty(MaterialMode.class, MaterialTag.class);
        PropertyParser.registerProperty(MaterialNote.class, MaterialTag.class);
        PropertyParser.registerProperty(MaterialPersistent.class, MaterialTag.class);
        PropertyParser.registerProperty(MaterialPower.class, MaterialTag.class);
        PropertyParser.registerProperty(MaterialShape.class, MaterialTag.class);
        PropertyParser.registerProperty(MaterialSides.class, MaterialTag.class);
        PropertyParser.registerProperty(MaterialSnowable.class, MaterialTag.class);
        PropertyParser.registerProperty(MaterialSwitchable.class, MaterialTag.class);
        PropertyParser.registerProperty(MaterialSwitchFace.class, MaterialTag.class);
        PropertyParser.registerProperty(MaterialUnstable.class, MaterialTag.class);
        PropertyParser.registerProperty(MaterialWaterlogged.class, MaterialTag.class);

        // register core TradeTag properties
        if (NMSHandler.getVersion().isAtLeast(NMSVersion.v1_18)) {
            PropertyParser.registerProperty(TradeDemand.class, TradeTag.class);
        }
        PropertyParser.registerProperty(TradeHasXp.class, TradeTag.class);
        PropertyParser.registerProperty(TradeInputs.class, TradeTag.class);
        PropertyParser.registerProperty(TradeMaxUses.class, TradeTag.class);
        PropertyParser.registerProperty(TradePriceMultiplier.class, TradeTag.class);
        PropertyParser.registerProperty(TradeResult.class, TradeTag.class);
        if (NMSHandler.getVersion().isAtLeast(NMSVersion.v1_18)) {
            PropertyParser.registerProperty(TradeSpecialPrice.class, TradeTag.class);
        }
        PropertyParser.registerProperty(TradeUses.class, TradeTag.class);
        PropertyParser.registerProperty(TradeVillagerXP.class, TradeTag.class);
    }
}
