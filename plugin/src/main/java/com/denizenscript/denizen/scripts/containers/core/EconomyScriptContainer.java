package com.denizenscript.denizen.scripts.containers.core;

import com.denizenscript.denizen.Denizen;
import com.denizenscript.denizen.utilities.BukkitImplDeprecations;
import com.denizenscript.denizen.utilities.Settings;
import com.denizenscript.denizen.utilities.implementation.BukkitScriptEntryData;
import com.denizenscript.denizen.objects.PlayerTag;
import com.denizenscript.denizen.tags.BukkitTagContext;
import com.denizenscript.denizencore.utilities.debugging.Debug;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.core.ScriptTag;
import com.denizenscript.denizencore.scripts.ScriptEntry;
import com.denizenscript.denizencore.scripts.containers.ScriptContainer;
import com.denizenscript.denizencore.scripts.queues.core.InstantQueue;
import com.denizenscript.denizencore.tags.TagManager;
import com.denizenscript.denizencore.utilities.*;
import net.milkbowl.vault.economy.AbstractEconomy;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.plugin.ServicePriority;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Future;

public class EconomyScriptContainer extends ScriptContainer {

    // <--[language]
    // @name Economy Script Containers
    // @group Script Container System
    // @plugin Vault
    // @description
    // Economy script containers
    //
    // Economy script containers provide a Vault economy, which can be used in scripts by
    // <@link tag PlayerTag.money> and <@link command money>
    // and as well by any other plugin that relies on economy functionality (such as shop plugins).
    //
    // Note that vault economy bank systems are not currently supported.
    // Per-world economies are also not currently supported.
    //
    // Note that in most cases, you do not want to have multiple economy providers, as only one will actually be in use.
    //
    // ALL SCRIPT KEYS ARE REQUIRED.
    //
    // Economy scripts can be automatically disabled by adding "enabled: false" as a root key (supports any load-time-parseable tags).
    //
    // <code>
    // # The script name will be shown to the economy provider as the name of the economy system.
    // Economy_Script_Name:
    //
    //     type: economy
    //
    //     # The Bukkit service priority. Priorities are Lowest, Low, Normal, High, Highest.
    //     priority: normal
    //     # The name of the currency in the singular (such as "dollar" or "euro").
    //     name single: scripto
    //     # The name of the currency in the plural (such as "dollars" or "euros").
    //     name plural: scriptos
    //     # How many digits after the decimal to include. For example, '2' means '1.05' is a valid amount, but '1.005' will be rounded.
    //     digits: 2
    //     # Format the standard output for the money in human-readable format. Use "<[amount]>" for the actual amount to display.
    //     # Fully supports tags.
    //     format: $<[amount]>
    //     # A tag that returns the balance of a linked player. Use a 'proc[]' tag if you need more complex logic.
    //     # Must return a decimal number.
    //     balance: <player.flag[money]>
    //     # A tag that returns a boolean indicating whether the linked player has the amount specified by def "<[amount]>".
    //     # Use a 'proc[]' tag if you need more complex logic.
    //     # Must return 'true' or 'false'.
    //     has: <player.flag[money].is[or_more].than[<[amount]>]>
    //     # A script that removes the amount of money needed from a player.
    //     # Note that it's generally up to the systems calling this script to verify that the amount can be safely withdrawn, not this script itself.
    //     # However you may wish to verify that the player has the amount required within this script.
    //     # The script may determine a failure message if the withdraw was refused. Determine nothing for no error.
    //     # Use def 'amount' for the amount to withdraw.
    //     withdraw:
    //     - flag <player> money:-:<[amount]>
    //     # A script that adds the amount of money needed to a player.
    //     # The script may determine a failure message if the deposit was refused. Determine nothing for no error.
    //     # Use def 'amount' for the amount to deposit.
    //     deposit:
    //     - flag <player> money:+:<[amount]>
    //
    // </code>
    //
    // -->

    public static class DenizenEconomyProvider extends AbstractEconomy {

        public EconomyScriptContainer backingScript;

        public String autoTagAmount(String value, OfflinePlayer player, double amount) {
            int digits = fractionalDigits();
            String amountText;
            if (digits <= 0) {
                amountText = String.valueOf((long) amount);
            }
            else {
                DecimalFormat d = new DecimalFormat("0." + new String(new char[digits]).replace('\0', '0'), CoreUtilities.decimalFormatSymbols);
                amountText = d.format(amount);
            }
            DefinitionProvider defProvider = new SimpleDefinitionProvider();
            defProvider.addDefinition("amount", new ElementTag(amountText));
            if (value.contains("<amount")) {
                BukkitImplDeprecations.pseudoTagBases.warn(backingScript);
                value = value.replace("<amount", "<element[" + amountText + "]");
            }
            return autoTag(value, player, defProvider);
        }

        public boolean validateThread() {
            if (!Bukkit.isPrimaryThread()) {
                if (Settings.allowAsyncPassThrough) {
                    return false;
                }
                Debug.echoError("Warning: economy access from wrong thread, blocked. Inform the developer of whatever plugin tried to read eco data that it is forbidden to do so async."
                        + " You can use config option 'Scripts.Economy.Pass async to main thread' to enable dangerous access.");
                try {
                    throw new RuntimeException("Stack reference");
                }
                catch (RuntimeException ex) {
                    Debug.echoError(ex);
                }
                return false;
            }
            return true;
        }

        public String autoTag(String value, OfflinePlayer player, DefinitionProvider defProvider) {
            if (value == null) {
                return null;
            }
            if (!validateThread()) {
                if (!Settings.allowAsyncPassThrough) {
                    return null;
                }
                try {
                    Future<String> future = Bukkit.getScheduler().callSyncMethod(Denizen.instance, () -> autoTag(value, player, defProvider));
                    return future.get();
                }
                catch (Throwable ex) {
                    Debug.echoError(ex);
                    return null;
                }
            }
            BukkitTagContext context = new BukkitTagContext(player == null ? null : new PlayerTag(player), null, new ScriptTag(backingScript));
            context.definitionProvider = defProvider;
            return TagManager.tag(value, context);
        }

        public String runSubScript(String pathName, OfflinePlayer player, double amount) {
            if (!validateThread()) {
                if (!Settings.allowAsyncPassThrough) {
                    return null;
                }
                try {
                    Future<String> future = Bukkit.getScheduler().callSyncMethod(Denizen.instance, () -> runSubScript(pathName, player, amount));
                    return future.get();
                }
                catch (Throwable ex) {
                    Debug.echoError(ex);
                    return null;
                }
            }
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
            return Integer.parseInt(backingScript.getString("digits", "2"));
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
            if (player == null) {
                Debug.echoError("Economy attempted BALANCE-CHECK to NULL player.");
                return 0;
            }
            try {
                return Double.parseDouble(autoTag(backingScript.getString("balance"), player, null));
            }
            catch (NumberFormatException ex) {
                Debug.echoError("Economy script '" + getName() + "' returned invalid balance for player '" + new PlayerTag(player).debuggable() + "': " + ex.getMessage());
                return 0;
            }
        }

        @Override
        public EconomyResponse withdrawPlayer(OfflinePlayer player, double amount) {
            if (player == null) {
                Debug.echoError("Economy attempted WITHDRAW to NULL player for " + amount);
                return null;
            }
            String determination = runSubScript("withdraw", player, amount);
            return new EconomyResponse(amount, getBalance(player), determination == null ?
                    EconomyResponse.ResponseType.SUCCESS : EconomyResponse.ResponseType.FAILURE, determination);
        }

        @Override
        public EconomyResponse depositPlayer(OfflinePlayer player, double amount) {
            if (player == null) {
                Debug.echoError("Economy attempted DEPOSIT to NULL player for " + amount);
                return null;
            }
            String determination = runSubScript("deposit", player, amount);
            return new EconomyResponse(amount, getBalance(player), determination == null ?
                    EconomyResponse.ResponseType.SUCCESS : EconomyResponse.ResponseType.FAILURE, determination);
        }

        @Override
        public boolean has(OfflinePlayer player, double amount) {
            if (player == null) {
                Debug.echoError("Economy attempted HAS-CHECK to NULL player for " + amount);
                return false;
            }
            return autoTagAmount(backingScript.getString("has"), player, amount).equalsIgnoreCase("true");
        }

        @Override
        public boolean hasAccount(String playerName) {
            return true;
        }

        @Override
        public boolean hasAccount(String playerName, String worldName) {
            return true;
        }

        public OfflinePlayer playerForName(String name) {
            UUID id = PlayerTag.getAllPlayers().get(CoreUtilities.toLowerCase(name));
            if (id == null) {
                Debug.echoError("Economy attempted access to unknown player '" + name + "'");
                return null;
            }
            return Bukkit.getOfflinePlayer(id);
        }

        @Override
        public double getBalance(String playerName) {
            return getBalance(playerForName(playerName));
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
            return has(playerForName(playerName), amount);
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
            return withdrawPlayer(playerForName(playerName), amount);
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
            return depositPlayer(playerForName(playerName), amount);
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
        String prioString = getString("priority", "normal");
        // Enumeration name casing is weird for ServicePriority.
        for (ServicePriority prio : ServicePriority.values()) {
            if (CoreUtilities.equalsIgnoreCase(prio.name(), prioString)) {
                return prio;
            }
        }
        return ServicePriority.Normal;
    }

    public DenizenEconomyProvider register() {
        DenizenEconomyProvider provider = new DenizenEconomyProvider();
        provider.backingScript = this;
        Bukkit.getServer().getServicesManager().register(Economy.class, provider, Denizen.getInstance(), getPriority());
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
        if (shouldEnable()) {
            providersRegistered.add(register());
        }
    }
}
