package com.denizenscript.denizen.events;

import com.denizenscript.denizen.Denizen;
import com.denizenscript.denizen.events.block.*;
import com.denizenscript.denizen.events.entity.*;
import com.denizenscript.denizen.events.item.*;
import com.denizenscript.denizen.events.npc.NPCNavigationScriptEvent;
import com.denizenscript.denizen.events.npc.NPCOpensScriptEvent;
import com.denizenscript.denizen.events.npc.NPCSpawnScriptEvent;
import com.denizenscript.denizen.events.npc.NPCStuckScriptEvent;
import com.denizenscript.denizen.events.player.*;
import com.denizenscript.denizen.events.server.*;
import com.denizenscript.denizen.events.vehicle.*;
import com.denizenscript.denizen.events.world.*;
import com.denizenscript.denizen.nms.NMSHandler;
import com.denizenscript.denizen.nms.NMSVersion;
import com.denizenscript.denizen.utilities.depends.Depends;
import com.denizenscript.denizencore.events.ScriptEvent;
import com.denizenscript.denizencore.events.ScriptEventCouldMatcher;

import java.util.Arrays;

public class ScriptEventRegistry {

    public static void registerCitizensEvents() {
        ScriptEvent.registerScriptEvent(NPCNavigationScriptEvent.class);
        ScriptEvent.registerScriptEvent(NPCOpensScriptEvent.class);
        ScriptEvent.registerScriptEvent(NPCSpawnScriptEvent.class);
        ScriptEvent.registerScriptEvent(NPCStuckScriptEvent.class);
    }


    public static void registerMainEvents() {
        // Special data for matching to register
        ScriptEvent.extraMatchers.add((event, path) -> BukkitScriptEvent.runAutomaticPlayerSwitches(event, path) && BukkitScriptEvent.runAutomaticNPCSwitches(event, path));

        // <--[data]
        // @name not_switches
        // @values item_flagged, world_flagged, area_flagged, inventory_flagged, player_flagged, npc_flagged, entity_flagged, vanilla_tagged, raw_exact, item_enchanted, material_flagged, location_in, block_flagged
        // -->
        ScriptEvent.ScriptPath.notSwitches.addAll(Arrays.asList("item_flagged", "world_flagged", "area_flagged", "inventory_flagged",
                "player_flagged", "npc_flagged", "entity_flagged", "vanilla_tagged", "raw_exact", "item_enchanted", "material_flagged", "location_in", "block_flagged"));

        // <--[data]
        // @name global_switches
        // @values bukkit_priority, assigned, flagged, permission, location_flagged
        // -->
        ScriptEvent.globalSwitches.addAll(Arrays.asList("bukkit_priority", "assigned", "flagged", "permission", "location_flagged"));
        ScriptEventCouldMatcher.knownValidatorTypes.put("entity", BukkitScriptEvent::couldMatchEntity);
        ScriptEventCouldMatcher.knownValidatorTypes.put("hanging", BukkitScriptEvent::couldMatchEntity);
        ScriptEventCouldMatcher.knownValidatorTypes.put("projectile", BukkitScriptEvent::couldMatchEntity);
        ScriptEventCouldMatcher.knownValidatorTypes.put("vehicle", BukkitScriptEvent::couldMatchVehicle);
        ScriptEventCouldMatcher.knownValidatorTypes.put("item", BukkitScriptEvent::couldMatchItem);
        ScriptEventCouldMatcher.knownValidatorTypes.put("inventory", BukkitScriptEvent::couldMatchInventory);
        ScriptEventCouldMatcher.knownValidatorTypes.put("block", BukkitScriptEvent::couldMatchBlock);
        ScriptEventCouldMatcher.knownValidatorTypes.put("material", BukkitScriptEvent::couldMatchBlockOrItem);
        ScriptEventCouldMatcher.knownValidatorTypes.put("area", BukkitScriptEvent::couldMatchArea);
        ScriptEventCouldMatcher.knownValidatorTypes.put("world", (t) -> true); // TODO: ?
        ScriptEventCouldMatcher.knownValidatorTypes.put("biome", (t) -> true); // TODO: ?

        ScriptEvent.notNameParts.add(0, "SpigotImpl");

        // Block events
        ScriptEvent.registerScriptEvent(BlockBuiltScriptEvent.class);
        ScriptEvent.registerScriptEvent(BlockBurnsScriptEvent.class);
        ScriptEvent.registerScriptEvent(BlockCooksSmeltsItemScriptEvent.class);
        ScriptEvent.registerScriptEvent(BlockDestroyedByExplosionEvent.class);
        ScriptEvent.registerScriptEvent(BlockDispensesScriptEvent.class);
        ScriptEvent.registerScriptEvent(BlockExplodesScriptEvent.class);
        ScriptEvent.registerScriptEvent(BlockFadesScriptEvent.class);
        ScriptEvent.registerScriptEvent(BlockFallsScriptEvent.class);
        ScriptEvent.registerScriptEvent(BlockFormsScriptEvent.class);
        ScriptEvent.registerScriptEvent(BlockGrowsScriptEvent.class);
        ScriptEvent.registerScriptEvent(BlockIgnitesScriptEvent.class);
        ScriptEvent.registerScriptEvent(BlockPhysicsScriptEvent.class);
        ScriptEvent.registerScriptEvent(BlockSpreadsScriptEvent.class);
        ScriptEvent.registerScriptEvent(BrewingStandFueledScriptEvent.class);
        ScriptEvent.registerScriptEvent(BrewsScriptEvent.class);
        ScriptEvent.registerScriptEvent(CauldronLevelChangeScriptEvent.class);
        ScriptEvent.registerScriptEvent(FurnaceBurnsItemScriptEvent.class);
        ScriptEvent.registerScriptEvent(LeafDecaysScriptEvent.class);
        ScriptEvent.registerScriptEvent(LiquidLevelChangeScriptEvent.class);
        ScriptEvent.registerScriptEvent(LiquidSpreadScriptEvent.class);
        ScriptEvent.registerScriptEvent(NoteBlockPlaysNoteScriptEvent.class);
        ScriptEvent.registerScriptEvent(PistonExtendsScriptEvent.class);
        ScriptEvent.registerScriptEvent(PistonRetractsScriptEvent.class);
        ScriptEvent.registerScriptEvent(RedstoneScriptEvent.class);

        // Entity events
        if (!Denizen.supportsPaper) {
            ScriptEvent.registerScriptEvent(AreaEnterExitScriptEvent.class);
        }
        ScriptEvent.registerScriptEvent(CreeperPoweredScriptEvent.class);
        ScriptEvent.registerScriptEvent(DragonPhaseChangeScriptEvent.class);
        ScriptEvent.registerScriptEvent(EntityAirLevelChangeScriptEvent.class);
        ScriptEvent.registerScriptEvent(EntityBreaksHangingScriptEvent.class);
        ScriptEvent.registerScriptEvent(EntityBreedScriptEvent.class);
        ScriptEvent.registerScriptEvent(EntityChangesBlockScriptEvent.class);
        ScriptEvent.registerScriptEvent(EntityChangesPoseScriptEvent.class);
        ScriptEvent.registerScriptEvent(EntityCombustsScriptEvent.class);
        ScriptEvent.registerScriptEvent(EntityCreatePortalScriptEvent.class);
        ScriptEvent.registerScriptEvent(EntityDamagedScriptEvent.class);
        ScriptEvent.registerScriptEvent(EntityDeathScriptEvent.class);
        ScriptEvent.registerScriptEvent(EntityDespawnScriptEvent.class);
        ScriptEvent.registerScriptEvent(EntityDropsItemScriptEvent.class);
        ScriptEvent.registerScriptEvent(EntityEntersPortalScriptEvent.class);
        ScriptEvent.registerScriptEvent(EntityEntersVehicleScriptEvent.class);
        ScriptEvent.registerScriptEvent(EntityExitsPortalScriptEvent.class);
        ScriptEvent.registerScriptEvent(EntityExitsVehicleScriptEvent.class);
        ScriptEvent.registerScriptEvent(EntityExplodesScriptEvent.class);
        ScriptEvent.registerScriptEvent(EntityExplosionPrimesScriptEvent.class);
        ScriptEvent.registerScriptEvent(EntityFoodLevelChangeScriptEvent.class);
        ScriptEvent.registerScriptEvent(EntityFormsBlockScriptEvent.class);
        ScriptEvent.registerScriptEvent(EntityGlideScriptEvent.class);
        ScriptEvent.registerScriptEvent(EntityHealsScriptEvent.class);
        ScriptEvent.registerScriptEvent(EntityInteractScriptEvent.class);
        ScriptEvent.registerScriptEvent(EntityKilledScriptEvent.class);
        ScriptEvent.registerScriptEvent(EntityPicksUpItemScriptEvent.class);
        ScriptEvent.registerScriptEvent(EntityPotionEffectScriptEvent.class);
        ScriptEvent.registerScriptEvent(EntityResurrectScriptEvent.class);
        ScriptEvent.registerScriptEvent(EntityShootsBowScriptEvent.class);
        ScriptEvent.registerScriptEvent(EntitySpawnerSpawnScriptEvent.class);
        ScriptEvent.registerScriptEvent(EntitySpawnScriptEvent.class);
        ScriptEvent.registerScriptEvent(EntitySwimScriptEvent.class);
        ScriptEvent.registerScriptEvent(EntityTamesScriptEvent.class);
        ScriptEvent.registerScriptEvent(EntityTargetsScriptEvent.class);
        ScriptEvent.registerScriptEvent(EntityTeleportScriptEvent.class);
        ScriptEvent.registerScriptEvent(EntityTransformScriptEvent.class);
        ScriptEvent.registerScriptEvent(EntityUnleashedScriptEvent.class);
        ScriptEvent.registerScriptEvent(FireworkBurstsScriptEvent.class);
        ScriptEvent.registerScriptEvent(HangingBreaksScriptEvent.class);
        ScriptEvent.registerScriptEvent(HorseJumpsScriptEvent.class);
        ScriptEvent.registerScriptEvent(PiglinBarterScriptEvent.class);
        ScriptEvent.registerScriptEvent(PigZappedScriptEvent.class);
        ScriptEvent.registerScriptEvent(ProjectileHitsBlockScriptEvent.class);
        ScriptEvent.registerScriptEvent(ProjectileHitsEntityScriptEvent.class);
        ScriptEvent.registerScriptEvent(ProjectileLaunchedScriptEvent.class);
        ScriptEvent.registerScriptEvent(SheepDyedScriptEvent.class);
        ScriptEvent.registerScriptEvent(SheepRegrowsScriptEvent.class);
        ScriptEvent.registerScriptEvent(SlimeSplitsScriptEvent.class);
        ScriptEvent.registerScriptEvent(VillagerAcquiresTradeScriptEvent.class);
        ScriptEvent.registerScriptEvent(VillagerChangesProfessionScriptEvent.class);
        ScriptEvent.registerScriptEvent(VillagerReplenishesTradeScriptEvent.class);

        // NPC events
        if (Depends.citizens != null) {
            registerCitizensEvents();
        }

        // Item events
        ScriptEvent.registerScriptEvent(InventoryPicksUpItemScriptEvent.class);
        ScriptEvent.registerScriptEvent(ItemDespawnsScriptEvent.class);
        ScriptEvent.registerScriptEvent(ItemEnchantedScriptEvent.class);
        ScriptEvent.registerScriptEvent(ItemMergesScriptEvent.class);
        ScriptEvent.registerScriptEvent(ItemMoveScriptEvent.class);
        ScriptEvent.registerScriptEvent(ItemRecipeFormedScriptEvent.class);
        ScriptEvent.registerScriptEvent(ItemSpawnsScriptEvent.class);

        // Player events
        ScriptEvent.registerScriptEvent(BiomeEnterExitScriptEvent.class);
        ScriptEvent.registerScriptEvent(BlockDropsItemScriptEvent.class);
        ScriptEvent.registerScriptEvent(ChatScriptEvent.class);
        ScriptEvent.registerScriptEvent(HotbarScrollScriptEvent.class);
        ScriptEvent.registerScriptEvent(ExperienceBottleBreaksScriptEvent.class);
        ScriptEvent.registerScriptEvent(PlayerAnimatesScriptEvent.class);
        ScriptEvent.registerScriptEvent(PlayerBreaksBlockScriptEvent.class);
        ScriptEvent.registerScriptEvent(PlayerBreaksItemScriptEvent.class);
        ScriptEvent.registerScriptEvent(PlayerChangesMainHandScriptEvent.class);
        ScriptEvent.registerScriptEvent(PlayerChangesGamemodeScriptEvent.class);
        ScriptEvent.registerScriptEvent(PlayerChangesSignScriptEvent.class);
        ScriptEvent.registerScriptEvent(PlayerChangesWorldScriptEvent.class);
        ScriptEvent.registerScriptEvent(PlayerChangesXPScriptEvent.class);
        ScriptEvent.registerScriptEvent(PlayerClicksBlockScriptEvent.class);
        ScriptEvent.registerScriptEvent(PlayerClicksInInventoryScriptEvent.class);
        ScriptEvent.registerScriptEvent(PlayerClosesInvScriptEvent.class);
        if (!Denizen.supportsPaper) {
            ScriptEvent.registerScriptEvent(PlayerCompletesAdvancementScriptEvent.class);
        }
        ScriptEvent.registerScriptEvent(PlayerConsumesScriptEvent.class);
        ScriptEvent.registerScriptEvent(PlayerCraftsItemScriptEvent.class);
        ScriptEvent.registerScriptEvent(PlayerDamagesBlockScriptEvent.class);
        ScriptEvent.registerScriptEvent(PlayerDragsInInvScriptEvent.class);
        ScriptEvent.registerScriptEvent(PlayerEditsBookScriptEvent.class);
        ScriptEvent.registerScriptEvent(PlayerEmptiesBucketScriptEvent.class);
        ScriptEvent.registerScriptEvent(PlayerEntersBedScriptEvent.class);
        ScriptEvent.registerScriptEvent(PlayerFillsBucketScriptEvent.class);
        ScriptEvent.registerScriptEvent(PlayerFishesScriptEvent.class);
        ScriptEvent.registerScriptEvent(PlayerFlyingScriptEvent.class);
        ScriptEvent.registerScriptEvent(PlayerHearsSoundScriptEvent.class);
        ScriptEvent.registerScriptEvent(PlayerHoldsItemEvent.class);
        ScriptEvent.registerScriptEvent(PlayerIncreasesExhaustionLevelScriptEvent.class);
        ScriptEvent.registerScriptEvent(PlayerItemTakesDamageScriptEvent.class);
        ScriptEvent.registerScriptEvent(PlayerJoinsScriptEvent.class);
        if (!Denizen.supportsPaper) {
            ScriptEvent.registerScriptEvent(PlayerJumpScriptEvent.PlayerJumpsSpigotScriptEventImpl.class);
        }
        ScriptEvent.registerScriptEvent(PlayerKickedScriptEvent.class);
        ScriptEvent.registerScriptEvent(PlayerLeashesEntityScriptEvent.class);
        ScriptEvent.registerScriptEvent(PlayerLeavesBedScriptEvent.class);
        ScriptEvent.registerScriptEvent(PlayerLevelsUpScriptEvent.class);
        ScriptEvent.registerScriptEvent(PlayerLocaleChangeScriptEvent.class);
        ScriptEvent.registerScriptEvent(PlayerLoginScriptEvent.class);
        ScriptEvent.registerScriptEvent(PlayerMendsItemScriptEvent.class);
        ScriptEvent.registerScriptEvent(PlayerOpensInvScriptEvent.class);
        ScriptEvent.registerScriptEvent(PlayerPickupArrowScriptEvent.class);
        ScriptEvent.registerScriptEvent(PlayerPlacesBlockScriptEvent.class);
        ScriptEvent.registerScriptEvent(PlayerPlacesHangingScriptEvent.class);
        ScriptEvent.registerScriptEvent(PlayerPreLoginScriptEvent.class);
        ScriptEvent.registerScriptEvent(PlayerPreparesAnvilCraftScriptEvent.class);
        ScriptEvent.registerScriptEvent(PlayerPreparesEnchantScriptEvent.class);
        ScriptEvent.registerScriptEvent(PlayerQuitsScriptEvent.class);
        ScriptEvent.registerScriptEvent(PlayerReceivesActionbarScriptEvent.class);
        ScriptEvent.registerScriptEvent(PlayerReceivesCommandsScriptEvent.class);
        ScriptEvent.registerScriptEvent(PlayerReceivesMessageScriptEvent.class);
        ScriptEvent.registerScriptEvent(PlayerReceivesPacketScriptEvent.class);
        ScriptEvent.registerScriptEvent(PlayerReceivesTablistUpdateScriptEvent.class);
        ScriptEvent.registerScriptEvent(PlayerRespawnsScriptEvent.class);
        ScriptEvent.registerScriptEvent(PlayerRightClicksEntityScriptEvent.class);
        ScriptEvent.registerScriptEvent(PlayerRiptideScriptEvent.class);
        ScriptEvent.registerScriptEvent(PlayerSendPacketScriptEvent.class);
        ScriptEvent.registerScriptEvent(PlayerShearsScriptEvent.class);
        ScriptEvent.registerScriptEvent(PlayerSmithsItemScriptEvent.class);
        ScriptEvent.registerScriptEvent(PlayersPrepareSmithingTableScriptEvent.class);
        ScriptEvent.registerScriptEvent(PlayerSneakScriptEvent.class);
        ScriptEvent.registerScriptEvent(PlayerSprintScriptEvent.class);
        ScriptEvent.registerScriptEvent(PlayerStandsOnScriptEvent.class);
        ScriptEvent.registerScriptEvent(PlayerStatisticIncrementsScriptEvent.class);
        ScriptEvent.registerScriptEvent(PlayerSteersEntityScriptEvent.class);
        ScriptEvent.registerScriptEvent(PlayerStepsOnScriptEvent.class);
        ScriptEvent.registerScriptEvent(PlayerSwapsItemsScriptEvent.class);
        ScriptEvent.registerScriptEvent(PlayerTakesFromFurnaceScriptEvent.class);
        ScriptEvent.registerScriptEvent(PlayerTakesFromLecternScriptEvent.class);
        ScriptEvent.registerScriptEvent(PlayerThrowsEggScriptEvent.class);
        ScriptEvent.registerScriptEvent(PlayerTriggersRaidScriptEvent.class);
        ScriptEvent.registerScriptEvent(PlayerUsesPortalScriptEvent.class);
        ScriptEvent.registerScriptEvent(PlayerWalkScriptEvent.class);
        ScriptEvent.registerScriptEvent(PlayerWalksOverScriptEvent.class);
        ScriptEvent.registerScriptEvent(ResourcePackStatusScriptEvent.class);

        // Server events
        ScriptEvent.registerScriptEvent(CommandScriptEvent.class);
        ScriptEvent.registerScriptEvent(InternalEventScriptEvent.class);
        if (!Denizen.supportsPaper) {
            ScriptEvent.registerScriptEvent(ListPingScriptEvent.ListPingScriptEventSpigotImpl.class);
        }
        ScriptEvent.registerScriptEvent(ServerPrestartScriptEvent.class);
        ScriptEvent.registerScriptEvent(ServerStartScriptEvent.class);
        ScriptEvent.registerScriptEvent(TabCompleteScriptEvent.class);

        // Vehicle
        ScriptEvent.registerScriptEvent(VehicleCollidesBlockScriptEvent.class);
        ScriptEvent.registerScriptEvent(VehicleCollidesEntityScriptEvent.class);
        ScriptEvent.registerScriptEvent(VehicleCreatedScriptEvent.class);
        ScriptEvent.registerScriptEvent(VehicleDamagedScriptEvent.class);
        ScriptEvent.registerScriptEvent(VehicleDestroyedScriptEvent.class);
        ScriptEvent.registerScriptEvent(VehicleMoveScriptEvent.class);

        // World events
        if (NMSHandler.getVersion().isAtLeast(NMSVersion.v1_17)) {
            ScriptEvent.registerScriptEvent(ChunkLoadEntitiesScriptEvent.class);
        }
        ScriptEvent.registerScriptEvent(ChunkLoadScriptEvent.class);
        if (NMSHandler.getVersion().isAtLeast(NMSVersion.v1_17)) {
            ScriptEvent.registerScriptEvent(ChunkUnloadEntitiesScriptEvent.class);
        }
        ScriptEvent.registerScriptEvent(ChunkUnloadScriptEvent.class);
        if (NMSHandler.getVersion().isAtLeast(NMSVersion.v1_17)) {
            ScriptEvent.registerScriptEvent(GenericGameEventScriptEvent.class);
        }
        ScriptEvent.registerScriptEvent(LightningStrikesScriptEvent.class);
        ScriptEvent.registerScriptEvent(LingeringPotionSplashScriptEvent.class);
        ScriptEvent.registerScriptEvent(LootGenerateScriptEvent.class);
        ScriptEvent.registerScriptEvent(PortalCreateScriptEvent.class);
        ScriptEvent.registerScriptEvent(PotionSplashScriptEvent.class);
        ScriptEvent.registerScriptEvent(RaidFinishesScriptEvent.class);
        ScriptEvent.registerScriptEvent(RaidSpawnsWaveScriptEvent.class);
        ScriptEvent.registerScriptEvent(RaidStopsScriptEvent.class);
        ScriptEvent.registerScriptEvent(SpawnChangeScriptEvent.class);
        ScriptEvent.registerScriptEvent(StructureGrowsScriptEvent.class);
        ScriptEvent.registerScriptEvent(ThunderChangesScriptEvent.class);
        ScriptEvent.registerScriptEvent(TimeChangeScriptEvent.class);
        ScriptEvent.registerScriptEvent(WeatherChangesScriptEvent.class);
        ScriptEvent.registerScriptEvent(WorldInitsScriptEvent.class);
        ScriptEvent.registerScriptEvent(WorldLoadsScriptEvent.class);
        ScriptEvent.registerScriptEvent(WorldSavesScriptEvent.class);
        ScriptEvent.registerScriptEvent(WorldUnloadsScriptEvent.class);
    }
}
