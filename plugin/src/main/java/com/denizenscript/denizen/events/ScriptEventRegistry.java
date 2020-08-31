package com.denizenscript.denizen.events;

import com.denizenscript.denizen.Denizen;
import com.denizenscript.denizen.events.block.*;
import com.denizenscript.denizen.events.entity.*;
import com.denizenscript.denizen.events.item.*;
import com.denizenscript.denizen.events.npc.NPCSpawnScriptEvent;
import com.denizenscript.denizen.events.player.*;
import com.denizenscript.denizen.events.server.*;
import com.denizenscript.denizen.events.vehicle.*;
import com.denizenscript.denizen.events.world.*;
import com.denizenscript.denizen.utilities.depends.Depends;
import com.denizenscript.denizen.nms.NMSHandler;
import com.denizenscript.denizen.nms.NMSVersion;
import com.denizenscript.denizencore.events.ScriptEvent;

public class ScriptEventRegistry {

    public static void registerMainEvents() {

        // Block events
        ScriptEvent.registerScriptEvent(new BlockBuiltScriptEvent());
        ScriptEvent.registerScriptEvent(new BlockBurnsScriptEvent());
        ScriptEvent.registerScriptEvent(new BlockDispensesScriptEvent());
        ScriptEvent.registerScriptEvent(new BlockFadesScriptEvent());
        ScriptEvent.registerScriptEvent(new BlockFallsScriptEvent());
        ScriptEvent.registerScriptEvent(new BlockFormsScriptEvent());
        ScriptEvent.registerScriptEvent(new BlockGrowsScriptEvent());
        ScriptEvent.registerScriptEvent(new BlockIgnitesScriptEvent());
        ScriptEvent.registerScriptEvent(new BlockPhysicsScriptEvent());
        ScriptEvent.registerScriptEvent(new BlockSpreadsScriptEvent());
        ScriptEvent.registerScriptEvent(new BrewingStandFueledScriptEvent());
        ScriptEvent.registerScriptEvent(new BrewsScriptEvent());
        ScriptEvent.registerScriptEvent(new CauldronLevelChangeScriptEvent());
        ScriptEvent.registerScriptEvent(new FurnaceBurnsItemScriptEvent());
        ScriptEvent.registerScriptEvent(new FurnaceSmeltsItemScriptEvent());
        ScriptEvent.registerScriptEvent(new LeafDecaysScriptEvent());
        ScriptEvent.registerScriptEvent(new LiquidSpreadScriptEvent());
        ScriptEvent.registerScriptEvent(new NoteBlockPlaysNoteScriptEvent());
        ScriptEvent.registerScriptEvent(new PistonExtendsScriptEvent());
        ScriptEvent.registerScriptEvent(new PistonRetractsScriptEvent());
        ScriptEvent.registerScriptEvent(new RedstoneScriptEvent());

        // Entity events
        ScriptEvent.registerScriptEvent(new CreeperPoweredScriptEvent());
        ScriptEvent.registerScriptEvent(new DragonPhaseChangeScriptEvent());
        ScriptEvent.registerScriptEvent(new EntityAirLevelChangeScriptEvent());
        ScriptEvent.registerScriptEvent(new EntityBreaksHangingScriptEvent());
        ScriptEvent.registerScriptEvent(new EntityBreedScriptEvent());
        ScriptEvent.registerScriptEvent(new EntityChangesBlockScriptEvent());
        ScriptEvent.registerScriptEvent(new EntityCombustsScriptEvent());
        ScriptEvent.registerScriptEvent(new EntityCreatePortalScriptEvent());
        ScriptEvent.registerScriptEvent(new EntityDamagedScriptEvent());
        ScriptEvent.registerScriptEvent(new EntityDeathScriptEvent());
        ScriptEvent.registerScriptEvent(new EntityDespawnScriptEvent());
        ScriptEvent.registerScriptEvent(new EntityEntersPortalScriptEvent());
        ScriptEvent.registerScriptEvent(new EntityEntersVehicleScriptEvent());
        ScriptEvent.registerScriptEvent(new EntityExitsPortalScriptEvent());
        ScriptEvent.registerScriptEvent(new EntityExitsVehicleScriptEvent());
        ScriptEvent.registerScriptEvent(new EntityExplodesScriptEvent());
        ScriptEvent.registerScriptEvent(new EntityExplosionPrimesScriptEvent());
        ScriptEvent.registerScriptEvent(new EntityFoodLevelChangeScriptEvent());
        ScriptEvent.registerScriptEvent(new EntityFormsBlockScriptEvent());
        ScriptEvent.registerScriptEvent(new EntityGlideScriptEvent());
        ScriptEvent.registerScriptEvent(new EntityHealsScriptEvent());
        ScriptEvent.registerScriptEvent(new EntityInteractScriptEvent());
        ScriptEvent.registerScriptEvent(new EntityKilledScriptEvent());
        ScriptEvent.registerScriptEvent(new EntityPicksUpItemScriptEvent());
        ScriptEvent.registerScriptEvent(new EntityPotionEffectScriptEvent());
        ScriptEvent.registerScriptEvent(new EntityResurrectScriptEvent());
        if (!Denizen.supportsPaper) {
            ScriptEvent.registerScriptEvent(new EntityShootsBowEvent());
        }
        ScriptEvent.registerScriptEvent(new EntitySpawnerSpawnScriptEvent());
        ScriptEvent.registerScriptEvent(new EntitySpawnScriptEvent());
        ScriptEvent.registerScriptEvent(new EntitySwimScriptEvent());
        ScriptEvent.registerScriptEvent(new EntityTamesScriptEvent());
        ScriptEvent.registerScriptEvent(new EntityTargetsScriptEvent());
        ScriptEvent.registerScriptEvent(new EntityTeleportScriptEvent());
        ScriptEvent.registerScriptEvent(new EntityTransformScriptEvent());
        ScriptEvent.registerScriptEvent(new EntityUnleashedScriptEvent());
        ScriptEvent.registerScriptEvent(new FireworkBurstsScriptEvent());
        ScriptEvent.registerScriptEvent(new HangingBreaksScriptEvent());
        ScriptEvent.registerScriptEvent(new HorseJumpsScriptEvent());
        ScriptEvent.registerScriptEvent(new PigZappedScriptEvent());
        ScriptEvent.registerScriptEvent(new ProjectileHitsScriptEvent());
        ScriptEvent.registerScriptEvent(new ProjectileLaunchedScriptEvent());
        ScriptEvent.registerScriptEvent(new SheepDyedScriptEvent());
        ScriptEvent.registerScriptEvent(new SheepRegrowsScriptEvent());
        ScriptEvent.registerScriptEvent(new SlimeSplitsScriptEvent());
        if (NMSHandler.getVersion().isAtLeast(NMSVersion.v1_14)) {
            ScriptEvent.registerScriptEvent(new VillagerAcquiresTradeScriptEvent());
            ScriptEvent.registerScriptEvent(new VillagerChangesProfessionScriptEvent());
            ScriptEvent.registerScriptEvent(new VillagerReplenishesTradeScriptEvent());
        }

        // NPC events
        if (Depends.citizens != null) {
            ScriptEvent.registerScriptEvent(new NPCSpawnScriptEvent());
        }

        // Item events
        ScriptEvent.registerScriptEvent(new InventoryPicksUpItemScriptEvent());
        ScriptEvent.registerScriptEvent(new ItemDespawnsScriptEvent());
        ScriptEvent.registerScriptEvent(new ItemEnchantedScriptEvent());
        ScriptEvent.registerScriptEvent(new ItemMergesScriptEvent());
        ScriptEvent.registerScriptEvent(new ItemMoveScriptEvent());
        ScriptEvent.registerScriptEvent(new ItemRecipeFormedScriptEvent());
        ScriptEvent.registerScriptEvent(new ItemSpawnsScriptEvent());

        // Player events
        ScriptEvent.registerScriptEvent(new BiomeEnterExitScriptEvent());
        ScriptEvent.registerScriptEvent(new ChatScriptEvent());
        ScriptEvent.registerScriptEvent(new AreaEnterExitScriptEvent());
        ScriptEvent.registerScriptEvent(new HotbarScrollScriptEvent());
        ScriptEvent.registerScriptEvent(new ExperienceBottleBreaksScriptEvent());
        ScriptEvent.registerScriptEvent(new PlayerAnimatesScriptEvent());
        ScriptEvent.registerScriptEvent(new PlayerBreaksBlockScriptEvent());
        ScriptEvent.registerScriptEvent(new PlayerBreaksItemScriptEvent());
        ScriptEvent.registerScriptEvent(new PlayerChangesGamemodeScriptEvent());
        ScriptEvent.registerScriptEvent(new PlayerChangesSignScriptEvent());
        ScriptEvent.registerScriptEvent(new PlayerChangesWorldScriptEvent());
        ScriptEvent.registerScriptEvent(new PlayerChangesXPScriptEvent());
        ScriptEvent.registerScriptEvent(new PlayerClicksBlockScriptEvent());
        ScriptEvent.registerScriptEvent(new PlayerClicksInInventoryScriptEvent());
        ScriptEvent.registerScriptEvent(new PlayerClosesInvScriptEvent());
        ScriptEvent.registerScriptEvent(new PlayerCompletesAdvancementScriptEvent());
        ScriptEvent.registerScriptEvent(new PlayerConsumesScriptEvent());
        ScriptEvent.registerScriptEvent(new PlayerCraftsItemScriptEvent());
        ScriptEvent.registerScriptEvent(new PlayerDamagesBlockScriptEvent());
        ScriptEvent.registerScriptEvent(new PlayerDragsInInvScriptEvent());
        ScriptEvent.registerScriptEvent(new PlayerDropsItemScriptEvent());
        ScriptEvent.registerScriptEvent(new PlayerEditsBookScriptEvent());
        ScriptEvent.registerScriptEvent(new PlayerEmptiesBucketScriptEvent());
        ScriptEvent.registerScriptEvent(new PlayerEntersBedScriptEvent());
        ScriptEvent.registerScriptEvent(new PlayerFillsBucketScriptEvent());
        ScriptEvent.registerScriptEvent(new PlayerFishesScriptEvent());
        ScriptEvent.registerScriptEvent(new PlayerFlyingScriptEvent());
        ScriptEvent.registerScriptEvent(new PlayerHoldsShieldEvent());
        ScriptEvent.registerScriptEvent(new PlayerItemTakesDamageScriptEvent());
        ScriptEvent.registerScriptEvent(new PlayerJoinsScriptEvent());
        if (!Denizen.supportsPaper) {
            ScriptEvent.registerScriptEvent(new PlayerJumpScriptEvent.PlayerJumpsSpigotScriptEventImpl());
        }
        ScriptEvent.registerScriptEvent(new PlayerKickedScriptEvent());
        ScriptEvent.registerScriptEvent(new PlayerLeashesEntityScriptEvent());
        ScriptEvent.registerScriptEvent(new PlayerLeavesBedScriptEvent());
        ScriptEvent.registerScriptEvent(new PlayerLevelsUpScriptEvent());
        ScriptEvent.registerScriptEvent(new PlayerLocaleChangeScriptEvent());
        ScriptEvent.registerScriptEvent(new PlayerLoginScriptEvent());
        ScriptEvent.registerScriptEvent(new PlayerMendsItemScriptEvent());
        ScriptEvent.registerScriptEvent(new PlayerOpensInvScriptEvent());
        ScriptEvent.registerScriptEvent(new PlayerPlacesBlockScriptEvent());
        ScriptEvent.registerScriptEvent(new PlayerPlacesHangingScriptEvent());
        ScriptEvent.registerScriptEvent(new PlayerPreLoginScriptEvent());
        ScriptEvent.registerScriptEvent(new PlayerPreparesAnvilCraftScriptEvent());
        ScriptEvent.registerScriptEvent(new PlayerPreparesEnchantScriptEvent());
        ScriptEvent.registerScriptEvent(new PlayerQuitsScriptEvent());
        ScriptEvent.registerScriptEvent(new PlayerReceivesCommandsScriptEvent());
        ScriptEvent.registerScriptEvent(new PlayerReceivesMessageScriptEvent());
        ScriptEvent.registerScriptEvent(new PlayerRespawnsScriptEvent());
        ScriptEvent.registerScriptEvent(new PlayerRightClicksEntityScriptEvent());
        ScriptEvent.registerScriptEvent(new PlayerRiptideScriptEvent());
        ScriptEvent.registerScriptEvent(new PlayerShearsScriptEvent());
        ScriptEvent.registerScriptEvent(new PlayerSneakScriptEvent());
        ScriptEvent.registerScriptEvent(new PlayerSprintScriptEvent());
        ScriptEvent.registerScriptEvent(new PlayerStandsOnScriptEvent());
        ScriptEvent.registerScriptEvent(new PlayerStatisticIncrementsScriptEvent());
        ScriptEvent.registerScriptEvent(new PlayerSteersEntityScriptEvent());
        ScriptEvent.registerScriptEvent(new PlayerStepsOnScriptEvent());
        ScriptEvent.registerScriptEvent(new PlayerSwapsItemsScriptEvent());
        ScriptEvent.registerScriptEvent(new PlayerTakesFromFurnaceScriptEvent());
        if (NMSHandler.getVersion().isAtLeast(NMSVersion.v1_14)) {
            ScriptEvent.registerScriptEvent(new PlayerTakesFromLecternScriptEvent());
        }
        ScriptEvent.registerScriptEvent(new PlayerThrowsEggScriptEvent());
        ScriptEvent.registerScriptEvent(new PlayerUsesPortalScriptEvent());
        ScriptEvent.registerScriptEvent(new PlayerWalkScriptEvent());
        ScriptEvent.registerScriptEvent(new PlayerWalksOverScriptEvent());
        ScriptEvent.registerScriptEvent(new ResourcePackStatusScriptEvent());

        // Server events
        ScriptEvent.registerScriptEvent(new CommandScriptEvent());
        ScriptEvent.registerScriptEvent(new ListPingScriptEvent());
        ScriptEvent.registerScriptEvent(new ServerPrestartScriptEvent());
        ScriptEvent.registerScriptEvent(new ServerStartScriptEvent());
        ScriptEvent.registerScriptEvent(new TabCompleteScriptEvent());

        // Vehicle
        ScriptEvent.registerScriptEvent(new VehicleCollidesBlockScriptEvent());
        ScriptEvent.registerScriptEvent(new VehicleCollidesEntityScriptEvent());
        ScriptEvent.registerScriptEvent(new VehicleCreatedScriptEvent());
        ScriptEvent.registerScriptEvent(new VehicleDamagedScriptEvent());
        ScriptEvent.registerScriptEvent(new VehicleDestroyedScriptEvent());
        ScriptEvent.registerScriptEvent(new VehicleMoveScriptEvent());

        // World events
        ScriptEvent.registerScriptEvent(new ChunkLoadScriptEvent());
        ScriptEvent.registerScriptEvent(new ChunkUnloadScriptEvent());
        ScriptEvent.registerScriptEvent(new LightningStrikesScriptEvent());
        ScriptEvent.registerScriptEvent(new LingeringPotionSplashScriptEvent());
        if (NMSHandler.getVersion().isAtLeast(NMSVersion.v1_15)) {
            ScriptEvent.registerScriptEvent(new LootGenerateScriptEvent());
        }
        ScriptEvent.registerScriptEvent(new PortalCreateScriptEvent());
        ScriptEvent.registerScriptEvent(new PotionSplashScriptEvent());
        ScriptEvent.registerScriptEvent(new SpawnChangeScriptEvent());
        ScriptEvent.registerScriptEvent(new StructureGrowsScriptEvent());
        ScriptEvent.registerScriptEvent(new WeatherChangesScriptEvent());
        ScriptEvent.registerScriptEvent(new WorldInitsScriptEvent());
        ScriptEvent.registerScriptEvent(new WorldLoadsScriptEvent());
        ScriptEvent.registerScriptEvent(new WorldSavesScriptEvent());
        ScriptEvent.registerScriptEvent(new WorldUnloadsScriptEvent());
    }
}
