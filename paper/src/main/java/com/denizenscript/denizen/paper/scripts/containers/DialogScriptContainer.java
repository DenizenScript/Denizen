package com.denizenscript.denizen.paper.scripts.containers;

import com.denizenscript.denizen.objects.ItemTag;
import com.denizenscript.denizen.paper.PaperModule;
import com.denizenscript.denizen.utilities.implementation.BukkitScriptEntryData;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.core.MapTag;
import com.denizenscript.denizencore.scripts.ScriptEntry;
import com.denizenscript.denizencore.scripts.ScriptRegistry;
import com.denizenscript.denizencore.scripts.containers.ScriptContainer;
import com.denizenscript.denizencore.scripts.queues.ContextSource;
import com.denizenscript.denizencore.scripts.queues.core.InstantQueue;
import com.denizenscript.denizencore.tags.ParseableTag;
import com.denizenscript.denizencore.tags.TagContext;
import com.denizenscript.denizencore.tags.TagManager;
import com.denizenscript.denizencore.utilities.CoreUtilities;
import com.denizenscript.denizencore.utilities.YamlConfiguration;
import com.denizenscript.denizencore.utilities.debugging.Debug;
import com.denizenscript.denizencore.utilities.text.StringHolder;
import io.papermc.paper.connection.PlayerGameConnection;
import io.papermc.paper.dialog.Dialog;
import io.papermc.paper.dialog.DialogResponseView;
import io.papermc.paper.event.player.PlayerCustomClickEvent;
import io.papermc.paper.registry.data.dialog.ActionButton;
import io.papermc.paper.registry.data.dialog.DialogBase;
import io.papermc.paper.registry.data.dialog.action.DialogAction;
import io.papermc.paper.registry.data.dialog.body.DialogBody;
import io.papermc.paper.registry.data.dialog.body.ItemDialogBody;
import io.papermc.paper.registry.data.dialog.input.*;
import io.papermc.paper.registry.data.dialog.type.DialogType;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.nbt.api.BinaryTagHolder;
import net.kyori.adventure.text.Component;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.*;

public class DialogScriptContainer extends ScriptContainer {

    // <--[language]
    // @name Dialog Script Containers
    // @group Script Container System
    // @Plugin Paper
    // @description
    // Dialog script containers define client-side dialogs that can be shown to players.
    // Requires MC 1.21+.
    //
    // Use the <@link command dialog> command to show or close dialogs.
    // Respond to button clicks via the inline "on click:" sections inside button definitions.
    // In the response subscript there is available:
    // <context.inputs> - map of all input keys and their values
    // <context.button_id> - id of clicked button
    //
    // The following is the format for the container:
    //
    // <code>
    // my_dialog:
    //
    //     type: dialog
    //
    //     # Required: the title shown at the top of the dialog.
    //     # | All dialog scripts MUST have this key!
    //     title: My Dialog Title
    //
    //     # The dialog type. Can be: notice, confirmation, multi_action
    //     # 'notice' shows a single button, 'confirmation' shows yes/no buttons,
    //     # 'multi_action' shows a map of custom buttons.
    //     # | All dialog scripts MUST have this key.
    //     dialog_type: notice
    //
    //     # Optional body text shown below the title.
    //     # | SOME dialog scripts should have this key.
    //     body: This is the body text.
    //
    //     # Whether the player can press Escape to close the dialog.
    //     # | MOST dialog scripts should NOT specify this (defaults to true).
    //     closeable: true
    //
    //     # For 'notice' type: a single confirmation button section.
    //     # | ALL notice dialog scripts MUST have this key.
    //     button:
    //         label: OK
    //         tooltip: Click to confirm
    //         width: 30
    //         on click:
    //         - narrate "You clicked OK and entered: <context.inputs.get[my_text]>"
    //
    //     # For 'confirmation' type: yes and no button sections.
    //     # | ALL confirmation dialog scripts MUST have these keys.
    //     yes_button:
    //         label: Accept
    //         on click:
    //         - narrate "Accepted!"
    //     no_button:
    //         label: Decline
    //         on click:
    //         - narrate "Declined."
    //
    //     # For 'multi_action' type: a map of buttons where the key is the button ID.
    //     # | ALL Multi-action dialog scripts MUST have this key.
    //     buttons:
    //         option_1:
    //             label: Option 1
    //             tooltip: First option
    //             on click:
    //             - narrate "Chose option 1"
    //         option_2:
    //             label: Option 2
    //             on click:
    //             - narrate "Chose option 2"
    //
    //     # Optional input fields. Supported types: text, boolean, number, option
    //     # | SOME dialog scripts should have this key.
    //     inputs:
    //         my_text:
    //             type: text
    //             label: Enter text<&co>
    //             initial: <empty>
    //             max_length: 100
    //         my_bool:
    //             type: boolean
    //             label: Toggle this option
    //             initial: false
    //         my_number:
    //             type: number
    //             label: Pick a number
    //             min: 0
    //             max: 100
    //             initial: 50
    //             step: 5
    //             width: 80
    //         my_option:
    //             type: option
    //             label: Pick an option
    //             width: 65
    //             options:
    //             - Choice A
    //             - Choice B
    //             - Choice C
    //
    //     # Optional map of items to display in the dialog body (shown after the body text).
    //     # | SOME dialog scripts should have this key.
    //     items:
    //         sword:
    //             item: diamond_sword
    //             description: A powerful weapon
    //             show_tooltip: true
    //             show_decorations: true
    //             width: 64
    //             height: 64
    //         held:
    //             item: <player.item_in_hand>
    //             description: Your held item
    // </code>
    //
    // -->

    public ParseableTag titleTag;
    public ParseableTag bodyTag;
    public boolean closeable;

    public final Map<String, TextInputEntry> textInputEntries = new HashMap<>();
    public final Map<String, NumberInputEntry> numberInputEntries = new HashMap<>();
    public final Map<String, BooleanInputEntry> booleanInputEntries = new HashMap<>();
    public final Map<String, OptionInputEntry> optionInputEntries = new HashMap<>();
    public final Map<String, ButtonEntry> buttonEntries = new HashMap<>();
    public final Map<String, ItemBodyEntry> itemBodyEntries = new HashMap<>();

    public DialogScriptContainer(YamlConfiguration configurationSection, String scriptContainerName) {
        super(configurationSection, scriptContainerName);
        canRunScripts = false;
        if (!contains("dialog_type", String.class)) {
            Debug.echoError(this, "Dialog script '" + getName() + "' missing required 'dialog_type' key.");
            return;
        }
        switch (CoreUtilities.toLowerCase(getString("dialog_type"))) {
            case "notice" -> {
                if (!contains("button", Map.class)) {
                    Debug.echoError(this, "Dialog script '" + getName() + "' missing required 'button' key when 'notice' dialog type.");
                    return;
                }
                loadButton("button", getConfigurationSection("button"));
            }
            case "confirmation" -> {
                if (!contains("yes_button", Map.class) || !contains("no_button", Map.class)) {
                    Debug.echoError(this, "Dialog script '" + getName() + "' missing required 'yes_button' and 'no_button' keys when 'confirmation' dialog type.");
                    return;
                }
                loadButton("yes_button", getConfigurationSection("yes_button"));
                loadButton("no_button", getConfigurationSection("no_button"));
            }
            case "multi_action" -> {
                if (!contains("buttons", Map.class)) {
                    Debug.echoError(this, "Dialog script '" + getName() + "' missing required 'buttons' key when 'multi_action' dialog type.");
                    return;
                }
                YamlConfiguration buttonsSection = getConfigurationSection("buttons");
                for (StringHolder buttonIdHolder : buttonsSection.getKeys(false)) {
                    String buttonId = buttonIdHolder.str;
                    YamlConfiguration btnSection = buttonsSection.getConfigurationSection(buttonId);
                    if (btnSection == null) {
                        continue;
                    }
                    loadButton(buttonId, btnSection);
                }
            }
            default -> {
                Debug.echoError(this, "Dialog script '" + getName() + "' has invalid 'dialog_type' key: " + getString("dialog_type"));
                return;
            }
        }
        titleTag = TagManager.parseTextToTag(getString("title", "Dialog"), CoreUtilities.basicContext);
        bodyTag = parseSection("body", getContents(), CoreUtilities.basicContext, null);
        closeable = Boolean.parseBoolean(getString("closeable", "true"));
        if (contains("inputs", Map.class)) {
            YamlConfiguration inputsSection = getConfigurationSection("inputs");
            for (StringHolder inputIdHolder : inputsSection.getKeys(false)) {
                String inputId = inputIdHolder.str;
                YamlConfiguration inputSection = inputsSection.getConfigurationSection(inputId);
                if (inputSection == null) {
                    continue;
                }
                switch (inputSection.getString("type", "text").toLowerCase()) {
                    case "text" -> {
                        ParseableTag label = parseSection("label", inputSection, CoreUtilities.basicContext, new ParseableTag(inputId));
                        ParseableTag initial = parseSection("initial", inputSection, CoreUtilities.basicContext, null);
                        ParseableTag maxLength = parseSection("max_length", inputSection, CoreUtilities.basicContext, null);
                        textInputEntries.put(inputId, new TextInputEntry(inputId, label, initial, maxLength));
                    }
                    case "number" -> {
                        ParseableTag label = parseSection("label", inputSection, CoreUtilities.basicContext, new ParseableTag(inputId));
                        ParseableTag initial = parseSection("initial", inputSection, CoreUtilities.basicContext, null);
                        ParseableTag min = parseSection("min", inputSection, CoreUtilities.basicContext, null);
                        ParseableTag max = parseSection("max", inputSection, CoreUtilities.basicContext, null);
                        ParseableTag step = parseSection("step", inputSection, CoreUtilities.basicContext, null);
                        ParseableTag width = parseSection("width", inputSection, CoreUtilities.basicContext, null);
                        numberInputEntries.put(inputId, new NumberInputEntry(inputId, label, initial, min, max, step, width));
                    }
                    case "boolean" -> {
                        ParseableTag label = parseSection("label", inputSection, CoreUtilities.basicContext, new ParseableTag(inputId));
                        ParseableTag initial = parseSection("initial", inputSection, CoreUtilities.basicContext, null);
                        booleanInputEntries.put(inputId, new BooleanInputEntry(inputId, label, initial));
                    }
                    case "option" -> {
                        if (!inputSection.contains("options")) {
                            Debug.echoError(this, "Dialog input '" + inputId + "' missing required 'options' key, skipping.");
                            continue;
                        }
                        ParseableTag width = parseSection("width", inputSection, CoreUtilities.basicContext, null);
                        ParseableTag label = parseSection("label", inputSection, CoreUtilities.basicContext, new ParseableTag(inputId));
                        List<String> stringOptions = getStringList("inputs." + inputId + ".options");
                        List<ParseableTag> options = new ArrayList<>(stringOptions.size());
                        for (String opt : stringOptions) {
                            options.add(TagManager.parseTextToTag(opt, CoreUtilities.basicContext));
                        }
                        optionInputEntries.put(inputId, new OptionInputEntry(inputId, label, options, width));
                    }
                }
            }
        }
        if (contains("items", Map.class)) {
            YamlConfiguration itemsSection = getConfigurationSection("items");
            for (StringHolder itemIdHolder : itemsSection.getKeys(false)) {
                YamlConfiguration itemSection = itemsSection.getConfigurationSection(itemIdHolder.str);
                if (itemSection == null) {
                    continue;
                }
                if (!itemSection.contains("item")) {
                    Debug.echoError(this, "Dialog items entry '" + itemIdHolder.str + "' missing required 'item' key, skipping.");
                    continue;
                }
                ParseableTag item = TagManager.parseTextToTag(itemSection.getString("item"), CoreUtilities.basicContext);
                ParseableTag description = parseSection("description", itemSection, CoreUtilities.basicContext, null);
                ParseableTag showTooltip = parseSection("show_tooltip", itemSection, CoreUtilities.basicContext, new ParseableTag("true"));
                ParseableTag showDecorations = parseSection("show_decorations", itemSection, CoreUtilities.basicContext, new ParseableTag("true"));
                ParseableTag width = parseSection("width", itemSection, CoreUtilities.basicContext, null);
                ParseableTag height = parseSection("height", itemSection, CoreUtilities.basicContext, null);
                itemBodyEntries.put(itemIdHolder.str, new ItemBodyEntry(item, description, showTooltip, showDecorations, width, height));
            }
        }
    }

    public static ParseableTag parseSection(String sectionName, YamlConfiguration section, TagContext context, ParseableTag defaultValue) {
        return section.contains(sectionName) ? TagManager.parseTextToTag(section.getString(sectionName), context) : defaultValue;
    }

    public void loadButton(String buttonId, YamlConfiguration section) {
        ParseableTag label = parseSection("label", section, CoreUtilities.basicContext, new ParseableTag(buttonId));
        ParseableTag tooltip = parseSection("tooltip", section, CoreUtilities.basicContext, null);
        ParseableTag width = parseSection("width", section, CoreUtilities.basicContext, null);
        buttonEntries.put(buttonId, new ButtonEntry(buttonId, label, tooltip, width));
    }

    public TagContext fixContext(TagContext context) {
        context = (context == null ? CoreUtilities.basicContext : context).clone();
        context.debug = context.debug && shouldDebug();
        return context;
    }

    public Key buttonKey(String buttonId) {
        return Key.key("denizen", getName().toLowerCase() + "/" + buttonId.toLowerCase());
    }

    public ActionButton parseButton(String buttonId, TagContext context) {
        return parseButton(buttonEntries.get(buttonId), context);
    }

    public ActionButton parseButton(ButtonEntry buttonEntry, TagContext context) {
        Component label = PaperModule.parseFormattedText(buttonEntry.label().parse(context).toString(), ChatColor.WHITE);
        ActionButton.Builder buttonBuilder = ActionButton.builder(label);
        if (buttonEntry.tooltip() != null) {
            buttonBuilder.tooltip(PaperModule.parseFormattedText(buttonEntry.tooltip().parse(context).toString(), ChatColor.WHITE));
        }
        if (buttonEntry.width() != null) {
            String widthStr = buttonEntry.width().parse(context).toString();
            try {
                buttonBuilder.width(Integer.parseInt(widthStr));
            }
            catch (NumberFormatException ex) {
                Debug.echoError(this, "Invalid width for button '" + buttonEntry.id() + "': " + widthStr);
                return null;
            }
        }
        return buttonBuilder.action(DialogAction.customClick(buttonKey(buttonEntry.id()), (BinaryTagHolder) null)).build();
    }

    public Dialog buildDialog(TagContext context) {
        context = fixContext(context);
        Debug.pushErrorContext(this);
        try {
            List<DialogBody> body = new ArrayList<>();
            if (bodyTag != null) {
                body.add(DialogBody.plainMessage(PaperModule.parseFormattedText(bodyTag.parse(context).toString(), ChatColor.WHITE)));
            }
            for (Map.Entry<String, ItemBodyEntry> entry : itemBodyEntries.entrySet()) {
                ItemBodyEntry itemEntry = entry.getValue();
                String itemStr = itemEntry.item().parse(context).toString();
                ItemTag item = ItemTag.valueOf(itemStr, context);
                if (item == null) {
                    Debug.echoError(this, "Invalid item for dialog body: " + itemStr);
                    continue;
                }
                if (item.getBukkitMaterial() == Material.AIR) {
                    Debug.echoError(this, "Invalid item for dialog body: cannot be air");
                    continue;
                }
                ItemDialogBody.Builder itemBuilder = DialogBody.item(item.getItemStack());
                if (itemEntry.showTooltip() != null) {
                    itemBuilder.showTooltip(Boolean.parseBoolean(itemEntry.showTooltip().parse(context).toString()));
                }
                if (itemEntry.showDecorations() != null) {
                    itemBuilder.showDecorations(Boolean.parseBoolean(itemEntry.showDecorations().parse(context).toString()));
                }
                if (itemEntry.description() != null) {
                    Component descComp = PaperModule.parseFormattedText(itemEntry.description().parse(context).toString(), ChatColor.WHITE);
                    itemBuilder.description(DialogBody.plainMessage(descComp));
                }
                if (itemEntry.width() != null) {
                    String widthStr = itemEntry.width().parse(context).toString();
                    try {
                        itemBuilder.width(Integer.parseInt(widthStr));
                    }
                    catch (NumberFormatException ex) {
                        Debug.echoError(this, "Invalid width for dialog body: " + widthStr);
                        continue;
                    }
                }
                if (itemEntry.height() != null) {
                    String heightStr = itemEntry.height().parse(context).toString();
                    try {
                        itemBuilder.height(Integer.parseInt(heightStr));
                    }
                    catch (NumberFormatException ex) {
                        Debug.echoError(this, "Invalid height for dialog body: " + heightStr);
                        continue;
                    }
                }
                body.add(itemBuilder.build());
            }
            List<DialogInput> inputs = new ArrayList<>(textInputEntries.size() + numberInputEntries.size() + booleanInputEntries.size() + optionInputEntries.size());
            for (Map.Entry<String, TextInputEntry> entry : textInputEntries.entrySet()) {
                TextInputEntry textEntry = entry.getValue();
                Component labelComp = PaperModule.parseFormattedText(textEntry.label().parse(context).toString(), ChatColor.WHITE);
                TextDialogInput.Builder textBuilder = DialogInput.text(entry.getKey(), labelComp);
                if (textEntry.initial() != null) {
                    String initialStr = textEntry.initial().parse(context).toString();
                    textBuilder.initial(initialStr);
                }
                if (textEntry.maxLength() != null) {
                    String maxLengthStr = textEntry.maxLength().parse(context).toString();
                    try {
                        int maxLength = Integer.parseInt(maxLengthStr);
                        textBuilder.maxLength(maxLength);
                    }
                    catch (NumberFormatException ex) {
                        Debug.echoError(this, "Invalid max_length for text input: " + maxLengthStr);
                        continue;
                    }
                }
                inputs.add(textBuilder.build());
            }
            for (Map.Entry<String, NumberInputEntry> entry : numberInputEntries.entrySet()) {
                NumberInputEntry numberEntry = entry.getValue();
                Component labelComp = PaperModule.parseFormattedText(numberEntry.label().parse(context).toString(), ChatColor.WHITE);
                float min = 0f;
                if (numberEntry.min() != null) {
                    String minStr = numberEntry.min().parse(context).toString();
                    try {
                        min = Float.parseFloat(minStr);
                    }
                    catch (NumberFormatException ex) {
                        Debug.echoError(this, "Invalid min for number input: " + minStr);
                    }
                }
                float max = 100f;
                if (numberEntry.max() != null) {
                    String maxStr = numberEntry.max().parse(context).toString();
                    try {
                        max = Float.parseFloat(maxStr);
                    }
                    catch (NumberFormatException ex) {
                        Debug.echoError(this, "Invalid max for number input: " + maxStr);
                    }
                }
                NumberRangeDialogInput.Builder numberBuilder = DialogInput.numberRange(entry.getKey(), labelComp, min, max);
                if (numberEntry.width() != null) {
                    String widthStr = numberEntry.width().parse(context).toString();
                    try {
                        int width = Integer.parseInt(widthStr);
                        numberBuilder.width(width);
                    }
                    catch (NumberFormatException ex) {
                        Debug.echoError(this, "Invalid width for number input: " + widthStr);
                        continue;
                    }
                }
                if (numberEntry.initial() != null) {
                    String initialStr = numberEntry.initial().parse(context).toString();
                    try {
                        Float initial = Float.parseFloat(initialStr);
                        numberBuilder.initial(initial);
                    }
                    catch (NumberFormatException ex) {
                        Debug.echoError(this, "Invalid initial for number input: " + initialStr);
                        continue;
                    }
                }
                if (numberEntry.step() != null) {
                    String stepStr = numberEntry.step().parse(context).toString();
                    try {
                        Float step = Float.parseFloat(stepStr);
                        numberBuilder.step(step);
                    }
                    catch (NumberFormatException ex) {
                        Debug.echoError(this, "Invalid step for number input: " + stepStr);
                    }
                }
                inputs.add(numberBuilder.build());
            }
            for (Map.Entry<String, BooleanInputEntry> entry : booleanInputEntries.entrySet()) {
                BooleanInputEntry boolEntry = entry.getValue();
                Component labelComp = PaperModule.parseFormattedText(boolEntry.label().parse(context).toString(), ChatColor.WHITE);
                BooleanDialogInput.Builder boolBuilder = DialogInput.bool(entry.getKey(), labelComp);
                if (boolEntry.initial() != null) {
                    boolean initial = Boolean.parseBoolean(boolEntry.initial().parse(context).toString());
                    boolBuilder.initial(initial);
                }
                inputs.add(boolBuilder.build());
            }
            for (Map.Entry<String, OptionInputEntry> entry : optionInputEntries.entrySet()) {
                OptionInputEntry optionEntry = entry.getValue();
                Component labelComp = PaperModule.parseFormattedText(optionEntry.label().parse(context).toString(), ChatColor.WHITE);
                boolean isFirst = true;
                List<SingleOptionDialogInput.OptionEntry> entries = new ArrayList<>(optionEntry.options().size());
                for (ParseableTag optTag : optionEntry.options()) {
                    String parsed = optTag.parse(context).toString();
                    entries.add(SingleOptionDialogInput.OptionEntry.create(parsed, PaperModule.parseFormattedText(parsed, ChatColor.WHITE), isFirst));
                    isFirst = false;
                }
                SingleOptionDialogInput.Builder optionBuilder = DialogInput.singleOption(entry.getKey(), labelComp, entries);
                if (optionEntry.width() != null) {
                    String widthStr = optionEntry.width().parse(context).toString();
                    try {
                        int width = Integer.parseInt(widthStr);
                        optionBuilder.width(width);
                    }
                    catch (NumberFormatException ex) {
                        Debug.echoError(this, "Invalid width for option input: " + widthStr);
                        continue;
                    }
                }
                inputs.add(optionBuilder.build());
            }

            Component title = PaperModule.parseFormattedText(titleTag.parse(context).toString(), ChatColor.WHITE);
            DialogBase base = DialogBase.builder(title).canCloseWithEscape(closeable).body(body).inputs(inputs).build();

            DialogType type = switch (getString("dialog_type")) {
                case "confirmation" -> {
                    ActionButton yesButton = parseButton("yes_button", context);
                    ActionButton noButton = parseButton("no_button", context);
                    yield DialogType.confirmation(yesButton, noButton);
                }
                case "multi_action" -> {
                    List<ActionButton> buttons = new ArrayList<>(buttonEntries.size());
                    for (ButtonEntry buttonEntry : buttonEntries.values()) {
                        ActionButton button = parseButton(buttonEntry, context);
                        if (button != null) {
                            buttons.add(button);
                        }
                    }
                    yield DialogType.multiAction(buttons).build();
                }
                case "notice" -> {
                    ActionButton button = parseButton("button", context);
                    yield DialogType.notice(button);
                }
                default -> null; // wont happen
            };
            return Dialog.create(factory -> factory.empty().base(base).type(type));
        }
        catch (Exception e) {
            Debug.echoError(this, "Exception while building dialog!");
            Debug.echoError(e);
            return null;
        }
        finally {
            Debug.popErrorContext();
        }
    }

    public static class DialogEvents implements Listener {

        @EventHandler
        public void onDialogClick(PlayerCustomClickEvent event) {
            if (!(event.getCommonConnection() instanceof PlayerGameConnection gc)) {
                return;
            }
            Key key = event.getIdentifier();
            if (!key.namespace().equals("denizen")) {
                return;
            }
            String value = key.value();
            int slashIdx = value.indexOf('/');
            if (slashIdx < 0) {
                return;
            }
            String scriptKeyPart = value.substring(0, slashIdx);
            DialogScriptContainer container = ScriptRegistry.getScriptContainerAs(scriptKeyPart, DialogScriptContainer.class);
            if (container == null) {
                return;
            }
            String buttonId = value.substring(slashIdx + 1);
            String path = switch (buttonId) {
                case "button" -> "button.on click";
                case "yes_button" -> "yes_button.on click";
                case "no_button" -> "no_button.on click";
                default -> "buttons." + buttonId + ".on click";
            };
            if (!container.containsScriptSection(path)) {
                return;
            }
            BukkitScriptEntryData entryData = new BukkitScriptEntryData(gc.getPlayer());
            List<ScriptEntry> entries = container.getEntries(entryData, path);
            if (entries.isEmpty()) {
                return;
            }
            DialogResponseView responseView = event.getDialogResponseView();
            try {
                InstantQueue queue = new InstantQueue(container.getName() + "_" + buttonId);
                MapTag inputs = new MapTag();
                if (responseView != null) {
                    for (String id : container.booleanInputEntries.keySet()) {
                        Boolean val = responseView.getBoolean(id);
                        if (val != null) {
                            inputs.putObject(id, new ElementTag(val));
                        }
                    }
                    for (String id : container.numberInputEntries.keySet()) {
                        Float val = responseView.getFloat(id);
                        if (val != null) {
                            inputs.putObject(id, new ElementTag(val));
                        }
                    }
                    for (String id : container.textInputEntries.keySet()) {
                        String val = responseView.getText(id);
                        if (val != null) {
                            inputs.putObject(id, new ElementTag(val));
                        }
                    }
                    for (String id : container.optionInputEntries.keySet()) {
                        String val = responseView.getText(id);
                        if (val != null) {
                            inputs.putObject(id, new ElementTag(val));
                        }
                    }
                }
                ContextSource.SimpleMap src = new ContextSource.SimpleMap();
                src.contexts = new HashMap<>(2);
                src.contexts.put("inputs", inputs);
                src.contexts.put("button_id", new ElementTag(buttonId, true));
                queue.contextSource = src;
                queue.addEntries(entries);
                queue.start();
            }
            catch (Exception ex) {
                Debug.echoError("Exception while running inline script for dialog '" + container.getName() + "' button '" + buttonId + "':");
                Debug.echoError(ex);
            }
        }
    }

    public record ButtonEntry(String id, ParseableTag label, ParseableTag tooltip, ParseableTag width) {}

    public record ItemBodyEntry(ParseableTag item, ParseableTag description, ParseableTag showTooltip, ParseableTag showDecorations, ParseableTag width, ParseableTag height) {}

    public record TextInputEntry(String id, ParseableTag label, ParseableTag initial, ParseableTag maxLength) {}

    public record OptionInputEntry(String id, ParseableTag label, List<ParseableTag> options, ParseableTag width) {}

    public record BooleanInputEntry(String id, ParseableTag label, ParseableTag initial) {}

    public record NumberInputEntry(String id, ParseableTag label, ParseableTag initial, ParseableTag min, ParseableTag max, ParseableTag step, ParseableTag width) {}
}
