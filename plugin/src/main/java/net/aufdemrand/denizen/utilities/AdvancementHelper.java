package net.aufdemrand.denizen.utilities;

import java.util.List;
import java.util.UUID;

import net.aufdemrand.denizen.nms.NMSHandler;
import net.aufdemrand.denizen.utilities.debugging.dB;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.advancement.Advancement;
import org.bukkit.advancement.AdvancementProgress;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;

/**
 * Manages Advancements.
 * TODO: Advancement Script Containers?
 *
 * @author Mergu
 */
public class AdvancementHelper {

    private NamespacedKey id;
    private String icon;
    private String title;
    private String description;
    private String frame;
    private boolean announce;
    private boolean toast;

    /**
     * Initializes advancement variables.
     *
     * @param title       Advancement title
     * @param description Advancement description
     * @param icon        Advancement icon material
     * @param frame       Defines the type of advancement. Valid types are challenge/goal/task
     * @param announce    Whether to show the advancement in chat
     * @param toast       Whether to show the advancement toast
     */
    public AdvancementHelper(String title, String description, Material icon, String frame, boolean announce, boolean toast) {
        this.id = new NamespacedKey(NMSHandler.getJavaPlugin(), UUID.randomUUID().toString());
        this.title = title;
        this.description = description;
        this.icon = NMSHandler.getInstance().getItemHelper().getVanillaName(new ItemStack(icon));
        this.frame = frame;
        this.announce = announce;
        this.toast = toast;
    }

    /**
     * Shows the advancement to a collection of players.
     *
     * @param players Players to show the advancement to
     */
    public void showTo(final List<Player> players) {
        add();
        grant(players);
        new BukkitRunnable() {
            @Override
            public void run() {
                revoke(players);
                remove();
            }
        }.runTaskLater(NMSHandler.getJavaPlugin(), 20);
    }

    /**
     * Adds the advancement
     */
    private void add() {
        try {
            Bukkit.getUnsafe().loadAdvancement(id, getJson());
        } catch (IllegalArgumentException e) {
            dB.echoError("Error registering advancement!");
        }
    }

    /**
     * Removes the advancement
     */
    private void remove() {
        Bukkit.getUnsafe().removeAdvancement(id);
        Bukkit.getServer().reloadData();
    }

    /**
     * Grants this advancement by fulfilling its criteria
     *
     * @param players Players to grant the advancement to
     */
    private void grant(List<Player> players) {
        Advancement advancement = Bukkit.getAdvancement(id);
        AdvancementProgress progress;
        for (Player player : players) {
            progress = player.getAdvancementProgress(advancement);
            if (!progress.isDone()) {
                for (String criteria : progress.getRemainingCriteria()) {
                    progress.awardCriteria(criteria);
                }
            }
        }
    }

    /**
     * Revokes this advancement by revoking its criteria
     *
     * @param players Players to revoke the advancement from
     */
    private void revoke(List<Player> players) {
        Advancement advancement = Bukkit.getAdvancement(id);
        AdvancementProgress progress;
        for (Player player : players) {
            progress = player.getAdvancementProgress(advancement);
            if (progress.isDone()) {
                for (String criteria : progress.getAwardedCriteria()) {
                    progress.revokeCriteria(criteria);
                }
            }
        }
    }

    /**
     * Constructs a valid Advancement JSON String to be saved to the server's advancements folder.
     * See: https://minecraft.gamepedia.com/Advancements#JSON_Format
     *
     * @return Advancement JSON String
     */
    private String getJson() {

        JsonObject json = new JsonObject();

        JsonObject icon = new JsonObject();
        icon.addProperty("item", this.icon);

        JsonObject display = new JsonObject();
        display.add("icon", icon);
        display.addProperty("title", this.title);
        display.addProperty("description", this.description);
        display.addProperty("background", "minecraft:textures/gui/advancements/backgrounds/adventure.png");
        display.addProperty("frame", this.frame);
        display.addProperty("announce_to_chat", announce);
        display.addProperty("show_toast", toast);
        display.addProperty("hidden", true);

        JsonObject criteria = new JsonObject();
        JsonObject trigger = new JsonObject();

        trigger.addProperty("trigger", "minecraft:impossible");
        criteria.add("impossible", trigger);

        json.add("criteria", criteria);
        json.add("display", display);

        Gson gson = new GsonBuilder().setPrettyPrinting().create();

        return gson.toJson(json);

    }
}