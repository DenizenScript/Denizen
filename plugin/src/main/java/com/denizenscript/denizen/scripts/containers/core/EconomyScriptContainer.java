package com.denizenscript.denizen.scripts.containers.core;

import com.denizenscript.denizen.utilities.DenizenAPI;
import com.denizenscript.denizen.BukkitScriptEntryData;
import com.denizenscript.denizen.objects.PlayerTag;
import com.denizenscript.denizen.tags.BukkitTagContext;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.ArgumentHelper;
import com.denizenscript.denizencore.objects.core.ScriptTag;
import com.denizenscript.denizencore.scripts.ScriptEntry;
import com.denizenscript.denizencore.scripts.containers.ScriptContainer;
import com.denizenscript.denizencore.scripts.queues.core.InstantQueue;
import com.denizenscript.denizencore.tags.TagManager;
import com.denizenscript.denizencore.utilities.CoreUtilities;
import com.denizenscript.denizencore.utilities.YamlConfiguration;
import net.milkbowl.vault.economy.AbstractEconomy;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.plugin.ServicePriority;

import java.util.ArrayList;
import java.util.List;

public class EconomyScriptContainer extends ScriptContainer {

    // <--[language]
    // @name Economy Script Containers
    // @group Script Container System
    // @plugin Vault
    // @description
    // Economy script containers
    //
    // Economy script containers provide a Vault economy, which can be used in scripts by
    // <@link tag PlayerTag.money> and <@link mechanism PlayerTag.money>
    // and as well by any other plugin that relies on economy functionality (such as shop plugins).
    //
    // Note that vault economy bank systems are not currently supported.
    // Per-world economies are also not currently supported.
    //
    // Note that in most cases, you do not want to have multiple economy providers, as only one will actually be in use.
    //
    // All script keys are required.
    //
    // <code>
    // # The script name will be shown to the economy provider as the name of the economy system.
    // Economy_Script_Name:
    //
    //   type: economy
    //
    //   # The Bukkit service priority. Priorities are Lowest, Low, Normal, High, Highest.
    //   priority: normal
    //   # The name of the currency in the singular (such as "dollar" or "euro").
    //   name single: scripto
    //   # The name of the currency in the plural (such as "dollars" or "euros").
    //   name plural: scriptos
    //   # How many digits after the decimal to include. For example, '2' means '1.05' is a valid amount, but '1.005' will be rounded.
    //   digits: 2
    //   # Format the standard output for the money in human-readable format. Use "<amount>" for the actual amount to display.
    //   # Fully supports tags.
    //   format: $<amount>
    //   # A tag that returns the balance of a linked player. Use a 'proc[]' tag if you need more complex logic.
    //   # Must return a decimal number.
    //   balance: <player.flag[money]>
    //   # A tag that returns a boolean indicating whether the linked player has the amount specified by auto-tag "<amount>".
    //   # Use a 'proc[]' tag if you need more complex logic.
    //   # Must return 'true' or 'false'.
    //   has: <player.flag[money].is[or_more].than[<amount>]>
    //   # A script that removes the amount of money needed from a player.
    //   # Note that it's generally up to the systems calling this script to verify that the amount can be safely withdrawn, not this script itself.
    //   # However you may wish to verify that the player has the amount required within this script.
    //   # The script may determine a failure message if the withdraw was refused. Determine nothing for no error.
    //   # Use def 'amount' for the amount to withdraw.
    //   withdraw:
    //   - flag <player> money:-:<[amount]>
    //   # A script that adds the amount of money needed to a player.
    //   # The script may determine a failure message if the deposit was refused. Determine nothing for no error.
    //   # Use def 'amount' for the amount to deposit.
    //   deposit:
    //   - flag <player> money:+:<[amount]>
    //
    // </code>
    //
    // -->

    public static class DenizenEconomyProvider extends AbstractEconomy {

        public EconomyScriptContainer backingScript;

        public String autoTagAmount(String value, OfflinePlayer player, double amount) {
            return autoTag(value.replace("<amount", "<element[" + amount + "]"), player);
        }

        public String autoTag(String value, OfflinePlayer player) {
            if (value == null) {
                return null;
            }
            return TagManager.tag(value, new BukkitTagContext(player == null ? null : new PlayerTag(player), null, new ScriptTag(backingScript)));
        }

        public String runSubScript(String pathName, OfflinePlayer player, double amount) {
            List<ScriptEntry> entries = backingScript.getEntries(new BukkitScriptEntryData(new PlayerTag(player), null), pathName);
            InstantQueue queue = new InstantQueue(backingScript.getName());
            queue.addEntries(entries);
            queue.addDefinition("amount", new ElementTag(amount));
            queue.start();
            if (queue.determinations != null && queue.determinations.size() > 0) {
                return queue.determinations.get(0);
            }
            return null;
        }

        @Override
        public boolean isEnabled() {
            return true;
        }

        @Override
        public String getName() {
            return backingScript.getName();
        }

        @Override
        public boolean hasBankSupport() {
            return false;
        }

        @Override
        public int fractionalDigits() {
            return ArgumentHelper.getIntegerFrom(backingScript.getString("digits", "2"));
        }

        @Override
        public String format(double amount) {
            return autoTagAmount(backingScript.getString("format"), null, amount);
        }

        @Override
        public String currencyNamePlural() {
            return backingScript.getString("name plural", "moneys");
        }

        @Override
        public String currencyNameSingular() {
            return backingScript.getString("name single", "money");
        }

        @Override
        public double getBalance(OfflinePlayer player) {
            return ArgumentHelper.getDoubleFrom(autoTag(backingScript.getString("balance"), player));
        }

        @Override
        public EconomyResponse withdrawPlayer(OfflinePlayer player, double amount) {
            String determination = runSubScript("withdraw", player, amount);
            return new EconomyResponse(amount, getBalance(player), determination == null ?
                    EconomyResponse.ResponseType.SUCCESS : EconomyResponse.ResponseType.FAILURE, determination);
        }

        @Override
        public EconomyResponse depositPlayer(OfflinePlayer player, double amount) {
            String determination = runSubScript("deposit", player, amount);
            return new EconomyResponse(amount, getBalance(player), determination == null ?
                    EconomyResponse.ResponseType.SUCCESS : EconomyResponse.ResponseType.FAILURE, determination);
        }

        @Override
        public boolean has(OfflinePlayer player, double amount) {
            return ArgumentHelper.getBooleanFrom(autoTagAmount(backingScript.getString("has"), player, amount));
        }

        @Override
        public boolean hasAccount(String playerName) {
            return true;
        }

        @Override
        public boolean hasAccount(String playerName, String worldName) {
            return true;
        }

        @Override
        public double getBalance(String playerName) {
            return getBalance(Bukkit.getPlayerExact(playerName));
        }

        @Override
        public double getBalance(String playerName, String worldName) {
            return getBalance(playerName);
        }

        @Override
        public double getBalance(OfflinePlayer player, String worldName) {
            return getBalance(player);
        }

        @Override
        public boolean has(String playerName, double amount) {
            return has(Bukkit.getPlayerExact(playerName), amount);
        }

        @Override
        public boolean has(String playerName, String worldName, double amount) {
            return has(playerName, amount);
        }

        @Override
        public boolean has(OfflinePlayer player, String worldName, double amount) {
            return has(player, amount);
        }

        @Override
        public EconomyResponse withdrawPlayer(String playerName, double amount) {
            return withdrawPlayer(Bukkit.getPlayerExact(playerName), amount);
        }

        @Override
        public EconomyResponse withdrawPlayer(String playerName, String worldName, double amount) {
            return withdrawPlayer(playerName, amount);
        }

        @Override
        public EconomyResponse withdrawPlayer(OfflinePlayer player, String worldName, double amount) {
            return withdrawPlayer(player, amount);
        }

        @Override
        public EconomyResponse depositPlayer(String playerName, double amount) {
            return depositPlayer(Bukkit.getPlayerExact(playerName), amount);
        }

        @Override
        public EconomyResponse depositPlayer(String playerName, String worldName, double amount) {
            return depositPlayer(playerName, amount);
        }

        @Override
        public EconomyResponse depositPlayer(OfflinePlayer player, String worldName, double amount) {
            return depositPlayer(player, amount);
        }

        @Override
        public EconomyResponse createBank(String s, String s1) {
            return null;
        }

        @Override
        public EconomyResponse deleteBank(String s) {
            return null;
        }

        @Override
        public EconomyResponse bankBalance(String s) {
            return null;
        }

        @Override
        public EconomyResponse bankHas(String s, double v) {
            return null;
        }

        @Override
        public EconomyResponse bankWithdraw(String s, double v) {
            return null;
        }

        @Override
        public EconomyResponse bankDeposit(String s, double v) {
            return null;
        }

        @Override
        public EconomyResponse isBankOwner(String s, String s1) {
            return null;
        }

        @Override
        public EconomyResponse isBankMember(String s, String s1) {
            return null;
        }

        @Override
        public List<String> getBanks() {
            return null;
        }

        @Override
        public boolean createPlayerAccount(String s) {
            return false;
        }

        @Override
        public boolean createPlayerAccount(String s, String s1) {
            return false;
        }
    }

    public ServicePriority getPriority() {
        String prioString = CoreUtilities.toLowerCase(getString("PRIORITY", "normal"));
        // Enumeration name casing is weird for ServicePriority.
        for (ServicePriority prio : ServicePriority.values()) {
            if (CoreUtilities.toLowerCase(prio.name()).equals(prioString)) {
                return prio;
            }
        }
        return ServicePriority.Normal;
    }

    public DenizenEconomyProvider register() {
        DenizenEconomyProvider provider = new DenizenEconomyProvider();
        provider.backingScript = this;
        Bukkit.getServer().getServicesManager().register(Economy.class, provider, DenizenAPI.getCurrentInstance(), getPriority());
        return provider;
    }

    public static void cleanup() {
        for (DenizenEconomyProvider provider : providersRegistered) {
            Bukkit.getServer().getServicesManager().unregister(provider);
        }
        providersRegistered.clear();
    }

    public static List<DenizenEconomyProvider> providersRegistered = new ArrayList<>();

    public EconomyScriptContainer(YamlConfiguration configurationSection, String scriptContainerName) {
        super(configurationSection, scriptContainerName);
        providersRegistered.add(register());
    }
}
