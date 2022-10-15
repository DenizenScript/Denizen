package com.denizenscript.denizen.utilities;

import com.denizenscript.denizencore.utilities.debugging.*;

public class BukkitImplDeprecations {

    // Added on 2018/12/23
    // Bad candidate for functionality removal - a bit handy to use in "/ex", despite being clearly bad in standard scripts.
    // Recommend never removing.
    public static Warning playerByNameWarning = new Warning("playerByNameWarning", "Warning: loading player by name - use the UUID instead (or use tag server.match_player)!");

    // Added on 2019/08/11
    // Recommend removal 2023 or later.
    public static Warning oldEconomyTags = new StrongWarning("oldEconomyTags", "player.money.currency* tags are deprecated in favor of server.economy.currency* tags.");

    // In Bukkit impl, Added on 2019/08/19
    public static Warning pointlessTextTags = new StrongWarning("pointlessTextTags", "Several text tags like '&dot' or '&cm' are pointless (there's no reason you can't just directly write them in). Please replace them with the actual intended text.");

    // Added on 2019/09/18, but was deprecated earlier.
    public static Warning worldContext = new StrongWarning("worldContext", "'context.world' in events containing a location or chunk context is deprecated: use 'context.location.world' or similar to get the world value.");
    public static Warning entityBreaksHangingEventContext = new StrongWarning("entityBreaksHangingEventContext", "'context.entity' in event 'on player breaks hanging' is deprecated: use 'context.breaker'.");
    public static Warning hangingBreaksEventContext = new StrongWarning("hangingBreaksEventContext", "'context.location' in event 'on hanging breaks' is deprecated: use 'context.hanging.location'.");
    public static Warning playerRightClicksEntityContext = new StrongWarning("playerRightClicksEntityContext", "'context.location' in event 'on player right clicks entity' is deprecated: use 'context.entity.location'.");
    public static Warning blockDispensesItemDetermination = new StrongWarning("blockDispensesItemDetermination", "Multiplier double determination for 'on block dispenses item' is deprecated: use 'context.velocity.mul[#]'.");
    public static Warning serverRedirectLogging = new StrongWarning("serverRedirectLogging", "server mechanism redirect_logging is deprecated: use the system mechanism by the same name.");

    // In Bukkit impl, Relevant as of 2019/09/25, made current on 2020/02/12
    private static String pointlessSubtagPrefix = "Most pointless sub-tags are deprecated in favor of explicit unique tags. ";
    public static Warning npcNicknameTag = new Warning("npcNicknameTag", pointlessSubtagPrefix + "npc.name.nickname is now just npc.nickname. Note that this historically appeared in the config.yml file, so check there if you're unsure what's using this tag.");
    public static Warning npcPreviousLocationTag = new Warning("npcPreviousLocationTag", pointlessSubtagPrefix + "npc.location.previous_location is now just npc.previous_location.");
    public static Warning npcAnchorListTag = new Warning("npcAnchorListTag", pointlessSubtagPrefix + "npc.anchor.list is now just npc.list_anchors.");
    public static Warning playerMoneyFormatTag = new Warning("playerMoneyFormatTag", pointlessSubtagPrefix + "player.money.format is now just player.formatted_money.");
    public static Warning playerFoodLevelFormatTag = new Warning("playerFoodLevelFormatTag", pointlessSubtagPrefix + "player.food_level.format is now just player.formatted_food_level.");
    public static Warning playerBanInfoTags = new Warning("playerBanInfoTags", pointlessSubtagPrefix + "player.ban_info.* tags are now just player.ban_*.");
    public static Warning playerNameTags = new Warning("playerNameTags", pointlessSubtagPrefix + "player.name.* tags are now just player.*_name.");
    public static Warning playerSidebarTags = new Warning("playerSidebarTags", pointlessSubtagPrefix + "player.sidebar.* tags are now just player.sidebar_*.");
    public static Warning playerAttackCooldownTags = new Warning("playerAttackCooldownTags", pointlessSubtagPrefix + "player.attack_cooldown.* tags are now just player.attack_cooldown_*.");
    public static Warning playerXpTags = new Warning("playerXpTags", pointlessSubtagPrefix + "player.xp.* tags are now just player.xp_*.");
    public static Warning entityHealthTags = new Warning("entityHealthTags", pointlessSubtagPrefix + "entity.health.* tags are now just entity.health_*.");
    public static Warning entityMaxOxygenTag = new Warning("entityMaxOxygenTag", pointlessSubtagPrefix + "entity.oxygen.max is now just entity.max_oxygen.");
    public static Warning itemBookTags = new Warning("itemBookTags", pointlessSubtagPrefix + "item.book.* tags are now just item.book_*.");
    public static Warning playerItemInHandSlotTag = new Warning("playerItemInHandSlotTag", pointlessSubtagPrefix + "player.item_in_hand_slot is now just player.held_item_slot.");

    // Added on 2019/09/25, but was deprecated earlier.
    // Bad candidate for functionality removal - used to be commonly used
    public static Warning qtyTags = new StrongWarning("qtyTags", "'qty' in a tag or command is deprecated: use 'quantity'.");

    // Added on 2019/09/25
    // Prime candidate for functionality removal - tags were only recently added, and were always jank.
    public static Warning bookItemRawTags = new StrongWarning("bookItemRawTags", "Raw text tags for books were a placeholder. The normal (non-raw) tags now contain all needed data.");

    // Added on 2019/11/22
    public static Warning serverPluginNamesTag = new StrongWarning("serverPluginNamesTag", "'server.list_plugin_names' is deprecated: use 'server.list_plugins'");

    // Added on 2019/11/25
    public static Warning locationBiomeFormattedTag = new StrongWarning("locationBiomeFormattedTag", "'location.biome.formatted' is deprecated: use 'location.biome.name' (uses BiomeTag.name)");

    // Added on 2019/11/26
    public static Warning nbtCommand = new StrongWarning("nbtCommand", "The NBT command is deprecated: use item flags instead.");

    // Added on 2019/11/30
    public static Warning serverListMaterialNames = new StrongWarning("serverListMaterialNames", "The tag 'server.list_materials' is deprecated: use '<server.list_material_types.parse[name]>' to get a matching result.");
    public static Warning serverListBiomeNames = new StrongWarning("serverListBiomeNames", "The tag 'server.list_biomes' is deprecated: use '<server.list_biome_types.parse[name]>' to get a matching result.");

    // Added on 2019/12/24
    public static Warning entityRemainingAir = new StrongWarning("entityRemainingAir", "The mechanism 'EntityTag.remaining_air' is deprecated: use 'EntityTag.oxygen' instead (duration input vs. tick input).");

    // Added on 2019/07/13
    public static Warning oldParseTag = new StrongWarning("oldParseTag", "'parse:' tags are deprecated. Please use '.parsed' element tags instead.");

    // Added on 2019/09/09
    public static Warning oldNPCNavigator = new StrongWarning("oldNPCNavigator", "'npc.navigator.*' tags are deprecated. Just remove the '.navigator' part, they're the same after that.");

    // Added on 2019/09/24, made normal 2021/11/2021.
    public static Warning oldRecipeScript = new Warning("oldRecipeScript", "Item script single-recipe format is outdated. Use the modern 'recipes' list key (see meta docs).");

    // Added on 2020/01/15
    public static Warning worldRandomLoadedChunkTag = new StrongWarning("worldRandomLoadedChunkTag", "The 'world.random_loaded_chunk' tag is pointless. Use 'world.loaded_chunks.random' instead.");

    // Added on 2020/01/15
    public static Warning entityCustomIdTag = new StrongWarning("entityCustomIdTag", "The tag 'EntityTag.custom_id' is deprecated. Use '.script' instead, though it is technically equivalent to <ENTITY.script||<ENTITY.entity_type>>.");

    // Added on 2020/01/15
    public static Warning playerActionBarMech = new StrongWarning("playerActionBarMech", "The mechanism 'PlayerTag.action_bar' is deprecated. Use the 'actionbar' command instead.");

    // Added on 2020/02/17
    // Prime candidate for functionality removal - command hasn't been used or recommended by anyone in years, and has clear faults that would have prevented usage for most users.
    public static Warning scribeCommand = new StrongWarning("scribeCommand", "The scribe command was created many years ago, in an earlier era of Denizen, and doesn't make sense to use anymore. Consider the 'equip', 'give', or 'drop' commands instead.");

    // Added 2020/04/24 but deprecated long ago.
    public static Warning takeCommandInventory = new StrongWarning("takeCommandInventory", "'take inventory' is deprecated: use 'inventory clear' instead.");
    public static Warning oldInventoryCommands = new StrongWarning("oldInventoryCommands", "The 'inventory' command sub-options 'add' and 'remove' are deprecated: use 'give' or 'take' command instead.");

    // Added 2020/04/24.
    public static Warning itemInventoryTag = new Warning("itemInventoryTag", "The tag 'item.inventory' is deprecated: use inventory_contents instead.");

    // Added 2020/05/21.
    public static Warning itemSkinFullTag = new Warning("itemSkinFullTag", pointlessSubtagPrefix + "item.skin.full is now item.skull_skin.");

    // Added 2020/06/03 but deprecated long ago.
    public static Warning oldBossBarMech = new Warning("oldBossBarMech", "The show_boss_bar mechanism is deprecated: use the bossbar command instead.");
    public static Warning oldTimeMech = new Warning("oldTimeMech", "The player.*time mechanisms are deprecated: use the time command instead.");
    public static Warning oldWeatherMech = new Warning("oldWeatherMech", "The player.*weather mechanisms are deprecated: use the weather command instead.");
    public static Warning oldKickMech = new Warning("oldKickMech", "The player.kick mechanism is deprecated: use the kick command instead.");
    public static Warning oldMoneyMech = new Warning("oldMoneyMech", "The player.money mechanism is deprecated: use the money command instead.");

    // added 2020/07/04, made normal 2021/11/2021.
    public static Warning cuboidFullTag = new Warning("cuboidFullTag", "The tag cuboid.full is deprecated: this should just never be used.");
    public static Warning furnaceTimeTags = new Warning("furnaceTimeTags", "The furnace_burn_time, cook time, and cook total time tag/mechs have been replaced by _duration instead of _time equivalents (using DurationTag now).");
    public static Warning playerTimePlayedTags = new Warning("playerTimePlayedTags", "The tags player.first_played, last_played, ban_expiration, and ban_created have been replaced by tags of the same name with '_time' added to the end (using TimeTag now).");

    // added 2020/07/19, made normal 2021/11/2021.
    public static Warning airLevelEventDuration = new Warning("airLevelEventDuration", "The 'entity changes air level' event uses 'air_duration' context now instead of the old tick count number.");
    public static Warning damageEventTypeMap = new Warning("damageEventTypeMap", "The 'entity damaged' context 'damage_[TYPE]' is deprecated in favor of 'damage_type_map', which is operated as a MapTag.");

    // added 2020/07/28, made normal 2021/11/2021.
    public static Warning headCommand = new Warning("headCommand", "The 'head' command is deprecated: use the 'equip' command with a 'player_head' item using the 'skull_skin' mechanism.");

    // added 2020/08/01, made normal 2021/11/2021.
    public static Warning entityRemoveWhenFar = new Warning("entityRemoveWhenFar", "The EntityTag remove_when_far_away property is deprecated in favor of the persistent property (which is the exact inverse).");
    public static Warning entityPlayDeath = new Warning("entityPlayDeath", "The EntityTag 'play_death' mechanism is deprecated: use the animate command.");

    // added 2020/08/19, made normal 2021/11/2021.
    public static Warning npcSpawnMechanism = new Warning("npcSpawnMechanism", "The NPCTag 'spawn' mechanism is deprecated: use the spawn command.");

    // Added 2020/05/17, made current on 2020/10/24.
    public static Warning itemFlagsProperty = new StrongWarning("itemFlagsProperty", "The item.flags property has been renamed to item.hides, to avoid confusion with the new flaggable itemtags system.");

    // Added 2020/11/22, made normal 2021/11/2021.
    public static Warning biomeSpawnableTag = new Warning("biomeSpawnableTag", pointlessSubtagPrefix + "The tag BiomeTag.spawnable_entities.(type) is deprecated: the type is now an input context instead.");

    // Added 2020/11/30, made normal 2021/11/2021.
    public static Warning npcDespawnMech = new Warning("npcDespawnMech", "The NPCTag despawn mechanism is deprecated: use the despawn command.");

    // Added 2021/02/25.
    public static Warning zapPrefix = new SlowWarning("zapPrefix", "The 'zap' command should be used with the scriptname and step as two separate arguments, not just one.");

    // Added 2020/03/05, made current on 2021/04/16.
    public static Warning oldPlayEffectSpecials = new SlowWarning("oldPlayEffectSpecials", "The playeffect input of forms like 'iconcrack_' have been deprecated in favor of using the special_data input (refer to meta docs).");

    // Added 2020/04/16.
    public static Warning entityStandingOn = new SlowWarning("entityStandingOn", pointlessSubtagPrefix + "entity.location.standing_on is now just entity.standing_on.");

    // Added 2021/05/02.
    public static Warning hurtSourceOne = new SlowWarning("hurtSourceOne", "The 'hurt' command's 'source_once' argument is deprecated due to being now irrelevant thanks to the new NMS backing for the hurt command.");

    // Added 2021/05/05.
    public static Warning materialLit = new SlowWarning("materialLit", "The MaterialTag property 'lit' is deprecated in favor of 'switched'.");
    public static Warning materialCampfire = new SlowWarning("materialCampfire", "The MaterialTag property 'campfire' are deprecated in favor of 'type'.");
    public static Warning materialDrags = new SlowWarning("materialDrags", "The MaterialTag property 'drags' are deprecated in favor of 'mode'.");

    // Added 2021/06/15, but was irrelevant years earlier.
    public static Warning itemMessage = new SlowWarning("itemMessage", "The PlayerTag mechanism 'item_message' is deprecated in favor of using the actionbar.");

    // Added 2021/09/08, but was irrelevant years earlier.
    public static Warning isValidTag = new SlowWarning("isValidTag", "The 'server.x_is_valid' style tags are deprecated: use '.exists', '.is_spawned.if_null[false]', etc.");

    // Added 2021/11/14.
    public static Warning blockSpreads = new SlowWarning("blockSpreads", "There are two '<block> spreads' events - use 'block spreads type:<block>' or 'liquid spreads type:<block>'");

    // Added 2021/11/15.
    public static Warning horseJumpsFormat = new SlowWarning("horseJumpsFormat", "The '<color> horse jumps' event is deprecated: don't put the color in the event line. (Deprecated for technical design reasons).");

    // Added 2019/11/11, made slow 2021/11/2021.
    public static Warning entityLocationCursorOnTag = new SlowWarning("entityLocationCursorOnTag", "entity.location.cursor_on tags should be replaced by entity.cursor_on (be careful with the slight differences though).");

    // Added 2021/05/05.
    public static Warning locationDistanceTag = new SlowWarning("locationDistanceTag", "locationtag.tree_distance is deprecated in favor of location.material.distance");

    // Added 2022/05/07.
    public static Warning armorStandRawSlot = new SlowWarning("armorStandRawSlot", "The EntityTag.disabled_slots.raw tag and EntityTag.disabled_slots_raw mechanism are deprecated, use the EntityTag.disabled_slots_data tag and EntityTag.disabled_slots mechanism instead.");

    // Added 2022/07/28
    public static Warning internalEventReflectionContext = new SlowWarning("internalEventReflectionContext", "The context.field_<name> and fields special tags for 'internal bukkit event' are deprecated in favor of the 'reflect_event' global context.");

    // Added 2022/10/14
    public static Warning skeletonSwingArm = new SlowWarning("skeletonSwingArm", "The 'SKELETON_START/STOP_SWING_ARM' animations are deprecated in favor of the 'EntityTag.aggressive' property.");
    public static Warning entityArmsRaised = new SlowWarning("entityArmsRaised", "The 'EntityTag.arms_raised' property is deprecated in favor of 'EntityTag.aggressive'.");

    // ==================== VERY SLOW deprecations ====================
    // These are only shown minimally, so server owners are aware of them but not bugged by them. Only servers with active scripters (using 'ex reload') will see them often.

    // Added 2020/04/19, Relevant for many years now, bump to normal slow warning by 2023.
    public static Warning interactScriptPriority = new VerySlowWarning("interactScriptPriority", "Assignment script 'interact scripts' section should not have numbered priority values, these were removed years ago. Check https://guide.denizenscript.com/guides/troubleshooting/updates-since-videos.html#assignment-script-updates for more info.");

    // Added 2021/10/24, bump to normal slow warning by 2023.
    public static Warning entityArmorPose = new VerySlowWarning("entityArmorPose", "The old EntityTag.armor_pose and armor_pose_list tags are deprecated in favor of armor_pose_map.");

    // Added 2020/06/13, bump to normal slow warning by 2023.
    public static Warning listStyleTags = new VerySlowWarning("listStyleTags", "'list_' tags are deprecated: just remove the 'list_' prefix.");

    // Added 2020/07/03, bump to normal slow warning by 2023.
    public static Warning attachToMech = new VerySlowWarning("attachToMech", "The entity 'attach_to' mechanism is deprecated: use the new 'attach' command instead!");

    // Added 2020/07/12, bump to normal slow warning by 2023.
    public static Warning entityEquipmentSubtags = new VerySlowWarning("entityEquipmentSubtags", pointlessSubtagPrefix + " 'entity.equipment.slotname' is deprecated: use 'entity.equipment_map.get[slotname]' instead.");

    // Added 2020/12/25, bump to normal slow warning by 2023.
    public static Warning itemEnchantmentTags = new VerySlowWarning("itemEnchantmentTags", pointlessSubtagPrefix + "The ItemTag.enchantments.* tags are deprecated: use enchantment_map and relevant MapTag subtags.");

    // In Ppaper module, Added 2022/03/20
    // // bump to normal warning and/or past warning after 1.18 is the minimum supported version (change happened in MC 1.18)
    public static Warning paperNoTickViewDistance = new VerySlowWarning("paperNoTickViewDistance", "Paper's 'no_tick_view_distance' is deprecated in favor of modern minecraft's 'simulation_distance' and 'view_distance' separation");

    // Added 2021/04/13, bump to normal slow warning by 2023.
    public static Warning materialHasDataPackTag = new VerySlowWarning("materialHasDataPackTag", "The tag 'MaterialTag.has_vanilla_data_tag[...]' is deprecated in favor of MaterialTag.vanilla_tags.contains[<name>]");
    public static Warning materialPropertyTags = new VerySlowWarning("materialPropertyTags", "Old MaterialTag.is_x property tags are deprecated in favor of PropertyHolderObject.supports[property-name]");

    // ==================== FUTURE deprecations ====================

    // Added 2021/02/05, deprecate officially by 2023.
    public static Warning itemProjectile = new FutureWarning("itemProjectile", "The item_projectile custom entity type is deprecated: modern minecraft lets you set the item of any projectile, like 'snowball[item=stick]'");

    // Added 2021/03/02, deprecate officially by 2023.
    public static Warning itemScriptColor = new FutureWarning("itemScriptColor", "The item script 'color' key is deprecated: use the 'color' mechanism under the 'mechanisms' key instead.");

    // In multiple places, Added 2021/11/20, deprecate officially by 2023.
    public static Warning pseudoTagBases = new FutureWarning("pseudoTagBases", "Pseudo-tags like '<text>', '<name>', '<amount>', and '<permission>' are deprecated in favor of definitions: just replace <text> with <[text]> or similar.");

    // Added 2020/10/18, deprecate officially by 2023.
    // Bad candidate for functionality removal due to frequency of use and likelihood of pre-existing data in save files.
    public static Warning itemDisplayNameMechanism = new FutureWarning("itemDisplayNameMechanism", "The item 'display_name' mechanism is now just the 'display' mechanism.");

    // Added 2020/12/05, deprecate officially by 2022.
    // Bad candidate for functionality removal due to frequency of use and likelihood of pre-existing data remaining in world data.
    public static Warning itemNbt = new FutureWarning("itemNbt", "The item 'nbt' property is deprecated: use ItemTag flags instead!");

    // Added 2021/02/03, deprecate officially by 2023.
    // Bad candidate for functional removal due to the "scriptname" variant being useful for debugging sometimes.
    public static Warning hasScriptTags = new FutureWarning("hasScriptTags", "The ItemTag.scriptname and EntityTag.scriptname and ItemTag.has_script and NPCTag.has_script tags are deprecated: use '.script.name' or a null check on .script.");

    // Added 2021/10/18, deprecate officially by 2023.
    public static Warning entityMechanismsFormat = new FutureWarning("entityMechanismsFormat", "Entity script containers previously allowed mechanisms in the script's root, however they should now be under a 'mechanisms' key.");

    // Added 2021/03/29, deprecate officially by 2023.
    public static Warning legacyAttributeProperties = new FutureWarning("legacyAttributeProperties", "The 'attribute' properties are deprecated in favor of the 'attribute_modifiers' properties which more fully implement the attribute system.");

    // Added 2021/07/26, deprecate officially by 2023.
    public static Warning itemEnchantmentsLegacy = new FutureWarning("itemEnchantmentsLegacy", "The tag 'ItemTag.enchantments' is deprecated: use enchantments_map, or enchantment_types.");
    public static Warning echantmentTagUpdate = new FutureWarning("echantmentTagUpdate", "Several legacy enchantment-related tags are deprecated in favor of using EnchantmentTag.");

    // Added 2021/03/27, deprecate officially by 2024.
    public static Warning locationFindEntities = new FutureWarning("locationFindEntities", "The tag 'LocationTag.find.entities.within' and 'blocks' tags are replaced by the 'find_entities' and 'find_blocks' versions. They are mostly compatible, but now have advanced matcher options.");
    public static Warning inventoryNonMatcherTags = new FutureWarning("inventoryNonMatcherTags", "The 'InventoryTag' tags 'contains', 'quantity', 'find', 'exclude' with raw items are deprecated and replaced by 'contains_item', 'quantity_item', 'find_item', 'exclude_item' that use advanced matcher logic.");
    public static Warning takeRawItems = new FutureWarning("takeRawItems", "The 'take' command's ability to remove raw items without any command prefix, and the 'material' and 'scriptname' options are deprecated: use the 'item:<matcher>' option.");

    // Added 2021/08/30, deprecate officially by 2023.
    public static Warning giveTakeMoney = new FutureWarning("giveTakeMoney", "The 'take' and 'give' commands option for 'money' are deprecated in favor of using the 'money' command.");

    // Added 2021/08/30, deprecate officially by 2024.
    public static Warning playerResourcePackMech = new FutureWarning("playerResourcePackMech", "The 'resource_pack' mechanism is deprecated in favor of using the 'resourcepack' command.");

    // Added 2021/11/07, deprecate officially by 2024.
    public static Warning assignmentRemove = new FutureWarning("assignmentRemove", "'assignment remove' without a script is deprecated: use 'clear' to clear all scripts, or 'remove' to remove one at a time.");
    public static Warning npcScriptSingle = new FutureWarning("npcScriptSingle", "'npc.script' is deprecated in favor of 'npc.scripts' (plural).");

    // Added 2022/01/30, deprecate officially by 2023.
    public static Warning entityItemEnderman = new FutureWarning("entityItemEnderman", "The property 'entity.item' for endermen has been replaced by 'entity.material' due to usage of block materials.");

    // Added 2022/02/21, deprecate officially by 2024.
    public static Warning oldPotionEffects = new FutureWarning("oldPotionEffects", "The comma-separated-list potion effect tags like 'list_effects' are deprecated in favor of MapTag based tags - 'effects_data'. Refer to meta documentation for details.");

    // Added 2022/05/07, deprecate officially by 2024.
    public static Warning armorStandDisabledSlotsOldFormat = new FutureWarning("armorStandDisabledSlotsOldFormat", "The EntityTag.disabled_slots tag and the SLOT/ACTION format in the EntityTag.disabled_slots mechanism are deprecated in favour of the EntityTag.disabled_slots_data tag and the MapTag format.");

    // Added 2021/06/15, deprecate officially by 2024.
    // Bad candidate for functionality removal - tags have been around a long time and some were used often.
    public static Warning locationOldCursorOn = new FutureWarning("locationOldCursorOn", "Several of the old 'LocationTag.cursor_on', 'precise_target_position', 'precise_impact_normal' variants are deprecated in favor of the 'ray_trace' tags.");

    // Added 2021/06/17, deprecate officially by 2024.
    public static Warning debugBlockAlpha = new FutureWarning("debugBlockAlpha", "The 'alpha' argument for the 'debugblock' command is deprecated: put the alpha in the color input instead.");

    // Added 2021/06/19, deprecate officially by 2024.
    public static Warning entityMapTraceTag = new FutureWarning("entityMapTraceTag", "The tag 'EntityTag.map_trace' is deprecated in favor of EntityTag.trace_framed_map");

    // Added 2021/06/27, deprecate officially by 2025.
    public static Warning serverUtilTags = new FutureWarning("serverUtilTags", "Some 'server.' tags for core features are deprecated in favor of 'util.' equivalents, including 'java_version', '*_file', 'ram_*', 'disk_*', 'notes', 'last_reload', 'scripts', 'sql_connections', '*_time_*', ...");

    // Added 2021/06/27, deprecate officially by 2025.
    public static Warning serverObjectExistsTags = new FutureWarning("serverObjectExistsTags", "The 'object_is_valid' tag is a historical version of modern '.exists' or '.is_truthy' fallback tags.");

    // Added 2021/06/27, deprecate officially by 2025.
    public static Warning hsbColorGradientTag = new FutureWarning("hsbColorGradientTag", "The tag 'ElementTag.hsb_color_gradient' is deprecated: use 'color_gradient' with 'style=hsb'");

    // ==================== PAST deprecations of things that are already gone but still have a warning left behind ====================

    // Added on 2019/10/13
    public static Warning versionScripts = new StrongWarning("versionScripts", "Version script containers are deprecated due to the old script repo no longer being active.");

    // Added on 2019/03/08, removed 2020/10/24.
    public static Warning boundWarning = new StrongWarning("boundWarning", "Item script 'bound' functionality has never been reliable and should not be used. Consider replicating the concept with world events.");

    // Deprecated 2019/02/06, removed 2022/03/19.
    public static Warning globalTagName = new StrongWarning("globalTagName", "Using 'global' as a base tag is a deprecated alternate name. Please use 'server' instead.");

}
