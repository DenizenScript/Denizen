package com.denizenscript.denizen.paper;

import com.denizenscript.denizen.Denizen;
import com.denizenscript.denizen.nms.NMSHandler;
import com.denizenscript.denizen.nms.NMSVersion;
import com.denizenscript.denizen.nms.interfaces.packets.PacketOutChat;
import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizen.objects.ItemTag;
import com.denizenscript.denizen.paper.events.*;
import com.denizenscript.denizen.paper.properties.*;
import com.denizenscript.denizen.paper.tags.PaperTagBase;
import com.denizenscript.denizen.paper.utilities.PaperAPIToolsImpl;
import com.denizenscript.denizen.utilities.FormattedTextHelper;
import com.denizenscript.denizen.utilities.PaperAPITools;
import com.denizenscript.denizencore.events.ScriptEvent;
import com.denizenscript.denizencore.objects.properties.PropertyParser;
import com.denizenscript.denizencore.utilities.debugging.Debug;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.chat.ComponentSerializer;
import org.bukkit.Bukkit;

public class PaperModule {

    public static void init() {
        Debug.log("Loading Paper support module...");

        ScriptEvent.notNameParts.add(0, "PaperImpl");
        // Events
        ScriptEvent.registerScriptEvent(AnvilBlockDamagedScriptEvent.class);
        ScriptEvent.registerScriptEvent(AreaEnterExitScriptEventPaperImpl.class);
        if (NMSHandler.getVersion().isAtMost(NMSVersion.v1_18)) {
            ScriptEvent.registerScriptEvent(BellRingScriptEvent.class);
        }
        ScriptEvent.registerScriptEvent(BlockPreDispenseScriptEvent.class);
        ScriptEvent.registerScriptEvent(CreeperIgnitesScriptEvent.class);
        ScriptEvent.registerScriptEvent(EntityAddToWorldScriptEvent.class);
        ScriptEvent.registerScriptEvent(EntityKnocksbackEntityScriptEvent.class);
        ScriptEvent.registerScriptEvent(EntityLoadCrossbowScriptEvent.class);
        ScriptEvent.registerScriptEvent(EntityPathfindScriptEvent.class);
        ScriptEvent.registerScriptEvent(EntityRemoveFromWorldScriptEvent.class);
        ScriptEvent.registerScriptEvent(EntityStepsOnScriptEvent.class);
        if (NMSHandler.getVersion().isAtLeast(NMSVersion.v1_19)) {
            ScriptEvent.registerScriptEvent(EntityTeleportedByPortalScriptEvent.class);
        }
        ScriptEvent.registerScriptEvent(ExperienceOrbMergeScriptEvent.class);
        ScriptEvent.registerScriptEvent(PlayerAbsorbsExperienceScriptEvent.class);
        ScriptEvent.registerScriptEvent(PlayerBeaconEffectScriptEvent.class);
        if (NMSHandler.getVersion().isAtLeast(NMSVersion.v1_18)) {
            ScriptEvent.registerScriptEvent(PlayerChangesFramedItemScriptEvent.class);
        }
        ScriptEvent.registerScriptEvent(PlayerChoosesArrowScriptEvent.class);
        ScriptEvent.registerScriptEvent(PlayerChunkUnloadScriptEvent.class);
        ScriptEvent.registerScriptEvent(PlayerClicksFakeEntityScriptEvent.class);
        ScriptEvent.registerScriptEvent(PlayerClicksInRecipeBookScriptEvent.class);
        ScriptEvent.registerScriptEvent(PlayerClientOptionsChangeScriptEvent.class);
        ScriptEvent.registerScriptEvent(PlayerCompletesAdvancementScriptEventPaperImpl.class);
        ScriptEvent.registerScriptEvent(PlayerDeepSleepScriptEvent.class);
        ScriptEvent.registerScriptEvent(PlayerElytraBoostScriptEvent.class);
        ScriptEvent.registerScriptEvent(PlayerEquipsArmorScriptEvent.class);
        ScriptEvent.registerScriptEvent(PlayerGrantedAdvancementCriterionScriptEvent.class);
        if (NMSHandler.getVersion().isAtLeast(NMSVersion.v1_19)) {
            ScriptEvent.registerScriptEvent(PlayerInventorySlotChangeScriptEvent.class);
        }
        ScriptEvent.registerScriptEvent(PlayerItemTakesDamageScriptEventPaperImpl.class);
        ScriptEvent.registerScriptEvent(PlayerJumpsScriptEventPaperImpl.class);
        ScriptEvent.registerScriptEvent(PlayerLecternPageChangeScriptEvent.class);
        ScriptEvent.registerScriptEvent(PlayerLoomPatternSelectScriptEvent.class);
        if (NMSHandler.getVersion().isAtLeast(NMSVersion.v1_20)) {
            ScriptEvent.registerScriptEvent(PlayerOpenSignScriptEvent.class);
        }
        ScriptEvent.registerScriptEvent(PlayerPreparesGrindstoneCraftScriptEvent.class);
        ScriptEvent.registerScriptEvent(PlayerQuitsScriptEventPaperImpl.class);
        if (NMSHandler.getVersion().isAtLeast(NMSVersion.v1_18)) {
            ScriptEvent.registerScriptEvent(PlayerRaiseLowerItemScriptEventPaperImpl.class);
        }
        ScriptEvent.registerScriptEvent(PlayerSelectsStonecutterRecipeScriptEvent.class);
        if (NMSHandler.getVersion().isAtLeast(NMSVersion.v1_20)) {
            ScriptEvent.registerScriptEvent(PlayerShieldDisableScriptEvent.class);
        }
        ScriptEvent.registerScriptEvent(PlayerSpectatesEntityScriptEvent.class);
        ScriptEvent.registerScriptEvent(PlayerStopsSpectatingScriptEvent.class);
        if (NMSHandler.getVersion().isAtLeast(NMSVersion.v1_19)) {
            ScriptEvent.registerScriptEvent(PlayerTracksEntityScriptEvent.class);
        }
        ScriptEvent.registerScriptEvent(PlayerTradesWithMerchantScriptEvent.class);
        ScriptEvent.registerScriptEvent(PreEntitySpawnScriptEvent.class);
        if (NMSHandler.getVersion().isAtLeast(NMSVersion.v1_19)) {
            ScriptEvent.registerScriptEvent(PrePlayerAttackEntityScriptEvent.class);
        }
        ScriptEvent.registerScriptEvent(ProjectileCollideScriptEvent.class);
        ScriptEvent.registerScriptEvent(ServerListPingScriptEventPaperImpl.class);
        ScriptEvent.registerScriptEvent(ServerResourcesReloadedScriptEvent.class);
        ScriptEvent.registerScriptEvent(SkeletonHorseTrapScriptEvent.class);
        ScriptEvent.registerScriptEvent(TargetBlockHitScriptEvent.class);
        if (NMSHandler.getVersion().isAtMost(NMSVersion.v1_18)) {
            ScriptEvent.registerScriptEvent(TNTPrimesScriptEvent.class);
        }
        ScriptEvent.registerScriptEvent(UnknownCommandScriptEvent.class);
        if (NMSHandler.getVersion().isAtLeast(NMSVersion.v1_19)) {
            ScriptEvent.registerScriptEvent(WardenChangesAngerLevelScriptEvent.class);
        }
        ScriptEvent.registerScriptEvent(WorldGameRuleChangeScriptEvent.class);

        // Properties
        PropertyParser.registerProperty(EntityArmsRaised.class, EntityTag.class);
        if (NMSHandler.getVersion().isAtLeast(NMSVersion.v1_18)) {
            PropertyParser.registerProperty(EntityAutoExpire.class, EntityTag.class);
        }
        PropertyParser.registerProperty(EntityCarryingEgg.class, EntityTag.class);
        PropertyParser.registerProperty(EntityCanTick.class, EntityTag.class);
        PropertyParser.registerProperty(EntityDrinkingPotion.class, EntityTag.class);
        if (NMSHandler.getVersion().isAtLeast(NMSVersion.v1_19)) {
            PropertyParser.registerProperty(EntityEggLayTime.class, EntityTag.class);
            PropertyParser.registerProperty(EntityFriction.class, EntityTag.class);
        }
        PropertyParser.registerProperty(EntityLeftHanded.class, EntityTag.class);
        PropertyParser.registerProperty(EntityReputation.class, EntityTag.class);
        PropertyParser.registerProperty(EntityShouldBurn.class, EntityTag.class);
        if (NMSHandler.getVersion().isAtLeast(NMSVersion.v1_19)) {
            PropertyParser.registerProperty(EntitySneaking.class, EntityTag.class);
        }
        PropertyParser.registerProperty(EntityWitherInvulnerable.class, EntityTag.class);
        PropertyParser.registerProperty(ItemArmorStand.class, ItemTag.class);

        // Paper object extensions
        PaperElementExtensions.register();
        PaperEntityExtensions.register();
        PaperItemExtensions.register();
        PaperPlayerExtensions.register();
        PaperWorldExtensions.register();
        // Paper Tags
        new PaperTagBase();

        // Other helpers
        Bukkit.getPluginManager().registerEvents(new PaperEventHelpers(), Denizen.getInstance());
        PaperAPITools.instance = new PaperAPIToolsImpl();
        PacketOutChat.convertComponentToJsonString = (o) -> componentToJson((Component) o);
    }

    public static Component parseFormattedText(String text, ChatColor baseColor) {
        if (text == null) {
            return null;
        }
        try {
            return jsonToComponent(FormattedTextHelper.componentToJson(FormattedTextHelper.parse(text, baseColor)));
        }
        catch (Exception ex) {
            Debug.verboseLog("Failed to parse formatted text: " + text.replace(ChatColor.COLOR_CHAR, '&'));
            throw ex;
        }
    }

    public static String stringifyComponent(Component component) {
        if (component == null) {
            return null;
        }
        return FormattedTextHelper.stringify(ComponentSerializer.parse(componentToJson(component)));
    }

    public static Component jsonToComponent(String json) {
        if (json == null) {
            return null;
        }
        try {
            return GsonComponentSerializer.gson().deserialize(json);
        }
        catch (Exception ex) {
            Debug.verboseLog("Failed to parse json to component: " + json);
            throw ex;
        }
    }

    public static String componentToJson(Component component) {
        if (component == null) {
            return null;
        }
        return GsonComponentSerializer.gson().serialize(component);
    }
}
