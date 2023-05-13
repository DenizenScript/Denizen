package com.denizenscript.denizen.nms.v1_18;

public class ReflectionMappingsInfo {

    // Contents gathered via https://minidigger.github.io/MiniMappingViewer/#/mojang/server/1.18.2

    // net.minecraft.advancements.AdvancementList
    public static String AdvancementList_roots = "c";
    public static String AdvancementList_tasks = "d";

    // net.minecraft.world.level.block.state.BlockBehaviour
    public static String BlockBehaviour_explosionResistance = "aH";

    // net.minecraft.stats.RecipeBook
    public static String RecipeBook_known = "a";

    // net.minecraft.core.MappedRegistry
    public static String MappedRegistry_frozen = "bL";

    // net.minecraft.world.item.crafting.RecipeManager
    public static String RecipeManager_byName = "d";

    // net.minecraft.world.entity.Entity
    public static String Entity_onGround = "y";
    public static String Entity_DATA_SHARED_FLAGS_ID = "Z";
    public static String Entity_DATA_CUSTOM_NAME = "aM";
    public static String Entity_DATA_CUSTOM_NAME_VISIBLE = "aN";

    // net.minecraft.world.entity.LivingEntity
    public static String LivingEntity_attackStrengthTicker = "aQ";
    public static String LivingEntity_autoSpinAttackTicks = "bC";
    public static String LivingEntity_setLivingEntityFlag = "c";

    // net.minecraft.world.entity.player.Player
    public static String Player_DATA_PLAYER_ABSORPTION_ID = "d";
    public static String Player_DATA_PLAYER_MODE_CUSTOMISATION = "bP";

    // net.minecraft.server.level.ServerPlayer
    public static String ServerPlayer_respawnForced = "cU";

    // net.minecraft.world.entity.monster.EnderMan
    public static String EnderMan_DATA_CREEPY = "bX";

    // net.minecraft.world.entity.monster.Zombie
    public static String Zombie_inWaterTime = "ce";

    // net.minecraft.world.item.Item
    public static String Item_maxStackSize = "d";

    // net.minecraft.world.level.Level
    public static String Level_isClientSide = "x";

    // net.minecraft.server.level.ThreadedLevelLightEngine
    public static String ThreadedLevelLightEngine_addTask = "a";
    // net.minecraft.server.level.ThreadedLevelLightEngine$TaskType
    public static String ThreadedLevelLightEngine_TaskType_PRE_UPDATE = "a";

    // net.minecraft.world.entity.item.ItemEntity
    public static String ItemEntity_DATA_ITEM = "c";

    // net.minecraft.world.level.biome.Biome
    public static String Biome_climateSettings = "i";

    // net.minecraft.world.level.biome.Biome$ClimateSettings
    public static String Biome_ClimateSettings_temperature = "c";
    public static String Biome_ClimateSettings_downfall = "e";
    public static String Biome_ClimateSettings_precipitation = "b";

    // net.minecraft.world.level.biome.BiomeSpecialEffects
    public static String BiomeSpecialEffects_foliageColorOverride = "f";

    // net.minecraft.network.Connection
    public static String Connection_receiving = "k";

    // net.minecraft.server.network.ServerGamePacketListenerImpl
    public static String ServerGamePacketListenerImpl_aboveGroundTickCount = "C";
    public static String ServerGamePacketListenerImpl_aboveGroundVehicleTickCount = "E";
    public static String ServerGamePacketListenerImpl_connection = "a";

    // net.minecraft.network.protocol.game.ClientboundPlayerAbilitiesPacket
    public static String ClientboundPlayerAbilitiesPacket_walkingSpeed = "j";

    // net.minecraft.network.protocol.game.ClientboundSetEntityDataPacket
    public static String ClientboundSetEntityDataPacket_packedItems = "b";

    // net.minecraft.network.protocol.game.ClientboundSectionBlocksUpdatePacket
    public static String ClientboundSectionBlocksUpdatePacket_sectionPos = "b";
    public static String ClientboundSectionBlocksUpdatePacket_states = "d";
    public static String ClientboundSectionBlocksUpdatePacket_positions = "c";

    // net.minecraft.network.protocol.game.ClientboundMoveEntityPacket
    public static String ClientboundMoveEntityPacket_xa = "b";
    public static String ClientboundMoveEntityPacket_ya = "c";
    public static String ClientboundMoveEntityPacket_za = "d";
    public static String ClientboundMoveEntityPacket_yRot = "e";
    public static String ClientboundMoveEntityPacket_xRot = "f";

    // net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket
    public static String ClientboundSetEntityMotionPacket_id = "a";

    // net.minecraft.network.protocol.game.ClientboundTeleportEntityPacket
    public static String ClientboundTeleportEntityPacket_id = "a";
    public static String ClientboundTeleportEntityPacket_x = "b";
    public static String ClientboundTeleportEntityPacket_y = "c";
    public static String ClientboundTeleportEntityPacket_z = "d";
    public static String ClientboundTeleportEntityPacket_yRot = "e";
    public static String ClientboundTeleportEntityPacket_xRot = "f";

    // net.minecraft.network.protocol.game.ClientboundLevelChunkPacketData$BlockEntityInfo
    public static String ClientboundLevelChunkPacketData_BlockEntityInfo_packedXZ = "a";
    public static String ClientboundLevelChunkPacketData_BlockEntityInfo_y = "b";

    // net.minecraft.world.entity.projectile.FishingHook
    public static String FishingHook_nibble = "ar";
    public static String FishingHook_timeUntilLured = "as";
    public static String FishingHook_timeUntilHooked = "at";

    // net.minecraft.world.level.block.state.BlockBehaviour$BlockStateBase
    public static String BlockBehaviour_BlockStateBase_getFluidState = "o";

    // net.minecraft.world.level.material.FluidState
    public static String FluidState_isRandomlyTicking = "f";
    public static String FluidState_isEmpty = "c";
    public static String FluidState_createLegacyBlock = "g";
    public static String FluidState_animateTick = "a";

    // net.minecraft.tags.TagNetworkSerialization$NetworkPayload
    public static String TagNetworkSerialization_NetworkPayload_tags = "a";

    // net.minecraft.core.HolderSet$Named
    public static String HolderSet_Named_bind = "b";

    // net.minecraft.core.Holder$Reference
    public static String Holder_Reference_bindTags = "a";

    // net.minecraft.server.level.ServerLevel
    public static String ServerLevel_sleepStatus = "P";
}
