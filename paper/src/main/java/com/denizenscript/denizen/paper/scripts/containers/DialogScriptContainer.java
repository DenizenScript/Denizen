package com.denizenscript.denizen.paper.scripts.containers;

import com.denizenscript.denizen.paper.PaperModule;
import com.denizenscript.denizen.utilities.implementation.BukkitScriptEntryData;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.core.MapTag;
import com.denizenscript.denizencore.scripts.ScriptEntry;
import com.denizenscript.denizencore.scripts.containers.ScriptContainer;
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
import io.papermc.paper.registry.data.dialog.input.DialogInput;
import io.papermc.paper.registry.data.dialog.input.SingleOptionDialogInput;
import io.papermc.paper.registry.data.dialog.type.DialogType;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.nbt.api.BinaryTagHolder;
import net.kyori.adventure.text.Component;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DialogScriptContainer extends ScriptContainer {

    // <--[language]
    // @name Dialog Script Containers
    // @group Script Container System
    // @Plugin Paper
    // @description
    // Dialog script containers define Paper client-side dialogs that can be shown to players.
    // Requires Paper 1.21.6 or later.
    //
    // Use the <@link command dialog> command to show or close dialogs.
    // Respond to button clicks via the <@link event player clicks dialog button> event,
    // or with inline "on click:" sections inside button definitions.
    //
    // The following is the format for the container:
    //
    // <code>
    // my_dialog:
    //
    //   type: dialog
    //
    //   # Required: the title shown at the top of the dialog.
    //   # | All dialog scripts MUST have this key!
    //   title: My Dialog Title
    //
    //   # The dialog type. Can be: notice, confirmation, multi_action
    //   # 'notice' shows a single button, 'confirmation' shows yes/no buttons,
    //   # 'multi_action' shows a map of custom buttons.
    //   # | Most dialog scripts SHOULD have this key (defaults to notice).
    //   dialog_type: notice
    //
    //   # Optional body text shown below the title.
    //   # | SOME dialog scripts should have this key.
    //   body: This is the body text.
    //
    //   # Whether the player can press Escape to close the dialog.
    //   # | MOST dialog scripts should NOT specify this (defaults to true).
    //   closeable: true
    //
    //   # Optional label shown when this dialog appears as a button in a dialog list.
    //   # | SOME dialog scripts might have this key.
    //   external_title: Open my dialog
    //
    //   # For 'notice' type: a single button section.
    //   # | MOST notice dialog scripts should have this key.
    //   button:
    //     label: OK
    //     tooltip: Click to confirm
    //     on click:
    //     - narrate "You clicked OK and entered: <context.inputs.get[my_text]>"
    //
    //   # For 'confirmation' type: yes and no button sections.
    //   # | Confirmation dialog scripts should have these keys.
    //   yes_button:
    //     label: Accept
    //     on click:
    //     - narrate "Accepted!"
    //   no_button:
    //     label: Decline
    //     on click:
    //     - narrate "Declined."
    //
    //   # For 'multi_action' type: a map of buttons where the YAML key is the button ID.
    //   # | Multi-action dialog scripts should have this key.
    //   buttons:
    //     option_1:
    //       label: Option 1
    //       tooltip: First option
    //       on click:
    //       - narrate "Chose option 1"
    //     option_2:
    //       label: Option 2
    //       on click:
    //       - narrate "Chose option 2"
    //
    //   # Optional input fields. The YAML key is the input ID (used in context.inputs).
    //   # Supported types: text, boolean, number, option
    //   # | SOME dialog scripts should have this key.
    //   inputs:
    //     my_text:
    //       type: text
    //       label: Enter text:
    //       initial: ""
    //       max_length: 100
    //     my_bool:
    //       type: boolean
    //       label: Toggle this option
    //       initial: false
    //     my_number:
    //       type: number
    //       label: Pick a number
    //       min: 0
    //       max: 100
    //       initial: 50
    //       step: 5
    //     my_option:
    //       type: option
    //       label: Pick an option
    //       options:
    //       - Choice A
    //       - Choice B
    //       - Choice C
    // </code>
    //
    // -->

    public static Map<String, DialogScriptContainer> dialogScripts = new HashMap<>();

    // input ID -> input type ("text", "boolean", "number", "option")
    public Map<String, String> inputTypes = new HashMap<>();

    public DialogScriptContainer(YamlConfiguration configurationSection, String scriptContainerName) {
        super(configurationSection, scriptContainerName);
        canRunScripts = false;
        dialogScripts.put(getName(), this);
        if (contains("inputs", Map.class)) {
            YamlConfiguration inputsSection = getConfigurationSection("inputs");
            for (StringHolder inputIdHolder : inputsSection.getKeys(false)) {
                String inputId = inputIdHolder.str;
                YamlConfiguration inputSection = inputsSection.getConfigurationSection(inputId);
                if (inputSection != null && inputSection.contains("type")) {
                    inputTypes.put(inputId, inputSection.getString("type").toLowerCase());
                }
            }
        }
    }

    public TagContext fixContext(TagContext context) {
        context = (context == null ? CoreUtilities.basicContext : context).clone();
        context.debug = context.debug && shouldDebug();
        return context;
    }

    public static String toKeyValue(String name) {
        return CoreUtilities.toLowerCase(name).replaceAll("[^a-z0-9_.\\-]", "_");
    }

    public Key buttonKey(String buttonId) {
        return Key.key("denizen", toKeyValue(getName()) + "/" + toKeyValue(buttonId));
    }

    public ActionButton parseButton(String buttonId, YamlConfiguration buttonSection, TagContext context) {
        ParseableTag tag;
        String labelText = buttonSection.contains("label") ?
            TagManager.tag(buttonSection.getString("label"), context) : buttonId;
        Component label = PaperModule.parseFormattedText(labelText, ChatColor.WHITE);
        Component tooltip = null;
        if (buttonSection.contains("tooltip")) {
            tooltip = PaperModule.parseFormattedText(TagManager.tag(buttonSection.getString("tooltip"), context), ChatColor.WHITE);
        }
        return ActionButton.builder(label)
            .tooltip(tooltip)
            .action(DialogAction.customClick(buttonKey(buttonId), (BinaryTagHolder) null))
            .build();
    }

    public List<DialogInput> parseInputs(TagContext context) {
        if (!contains("inputs", Map.class)) {
            return List.of();
        }
        List<DialogInput> inputs = new ArrayList<>();
        YamlConfiguration inputsSection = getConfigurationSection("inputs");
        for (StringHolder inputIdHolder : inputsSection.getKeys(false)) {
            String inputId = inputIdHolder.str;
            YamlConfiguration inputSection = inputsSection.getConfigurationSection(inputId);
            if (inputSection == null) {
                continue;
            }
            String type = inputSection.contains("type") ? inputSection.getString("type").toLowerCase() : "text";
            String labelText = inputSection.contains("label") ?
                TagManager.tag(inputSection.getString("label"), context) : inputId;
            Component labelComp = PaperModule.parseFormattedText(labelText, ChatColor.WHITE);
            try {
                DialogInput input = switch (type) {
                    case "boolean" -> {
                        boolean initial = inputSection.contains("initial") &&
                            CoreUtilities.equalsIgnoreCase(inputSection.getString("initial"), "true");
                        yield DialogInput.bool(inputId, labelComp).initial(initial).build();
                    }
                    case "number" -> {
                        float min = 0f;
                        if (inputSection.contains("min")) {
                            try {
                                min = Float.parseFloat(inputSection.getString("min"));
                            }
                            catch (NumberFormatException ex) {
                                Debug.echoError(this, "Invalid min for input '" + inputId + "'");
                            }
                        }
                        float max = 100f;
                        if (inputSection.contains("max")) {
                            try {
                                max = Float.parseFloat(inputSection.getString("max"));
                            }
                            catch (NumberFormatException ex) {
                                Debug.echoError(this, "Invalid max for input '" + inputId + "'");
                            }
                        }
                        //float min = inputSection.contains("min") ? Float.parseFloat(inputSection.getString("min")) : 0f;
                        //float max = inputSection.contains("max") ? Float.parseFloat(inputSection.getString("max")) : 100f;
                        Float initial = inputSection.contains("initial") ? Float.parseFloat(inputSection.getString("initial")) : null;
                        Float step = inputSection.contains("step") ? Float.parseFloat(inputSection.getString("step")) : null;
                        yield DialogInput.numberRange(inputId, labelComp, min, max).initial(initial).step(step).build();
                    }
                    case "option" -> {
                        List<SingleOptionDialogInput.OptionEntry> entries = new ArrayList<>();
                        if (inputSection.contains("options")) {
                            boolean firstSelected = true;
                            for (String opt : inputSection.getStringList("options")) {
                                String taggedOpt = TagManager.tag(opt, context);
                                entries.add(SingleOptionDialogInput.OptionEntry.create(taggedOpt, null, firstSelected));
                                firstSelected = false;
                            }
                        }
                        yield DialogInput.singleOption(inputId, labelComp, entries).build();
                    }
                    default -> { // "text"
                        String initial = inputSection.contains("initial") ?
                            TagManager.tag(inputSection.getString("initial"), context) : "";
                        int maxLength = 32;
                        if (inputSection.contains("max_length")) {
                            try {
                                maxLength = Integer.parseInt(inputSection.getString("max_length"));
                            }
                            catch (NumberFormatException ex) {
                                Debug.echoError(this, "Invalid max_length for input '" + inputId + "'");
                            }
                        }
                        yield DialogInput.text(inputId, labelComp).initial(initial).maxLength(maxLength).build();
                    }
                };
                inputs.add(input);
            }
            catch (Exception ex) {
                Debug.echoError(this, "Failed to parse input '" + inputId + "': " + ex.getMessage());
            }
        }
        return inputs;
    }

    public Dialog buildDialog(TagContext context) {
        context = fixContext(context);
        Debug.pushErrorContext(this);
        try {
            String titleText = TagManager.tag(getString("title", "Dialog"), context);
            Component title = PaperModule.parseFormattedText(titleText, ChatColor.WHITE);
            List<DialogBody> body = new ArrayList<>();
            if (contains("body", String.class)) {
                String bodyText = TagManager.tag(getString("body"), context);
                body.add(DialogBody.plainMessage(PaperModule.parseFormattedText(bodyText, ChatColor.WHITE)));
            }
            List<DialogInput> inputs = parseInputs(context);
            boolean closeable = !contains("closeable", String.class) ||
                CoreUtilities.equalsIgnoreCase(getString("closeable", "true"), "true");
            Component externalTitle = null;
            if (contains("external_title", String.class)) {
                externalTitle = PaperModule.parseFormattedText(TagManager.tag(getString("external_title"), context), ChatColor.WHITE);
            }
            DialogBase base = DialogBase.builder(title)
                .externalTitle(externalTitle)
                .canCloseWithEscape(closeable)
                .body(body)
                .inputs(inputs)
                .build();
            String dialogTypeStr = contains("dialog_type", String.class) ?
                getString("dialog_type").toLowerCase() : "notice";
            DialogType type = switch (dialogTypeStr) {
                case "confirmation" -> {
                    ActionButton yesButton = contains("yes_button", Map.class) ?
                        parseButton("yes_button", getConfigurationSection("yes_button"), context) :
                        ActionButton.builder(Component.text("Yes")).action(DialogAction.customClick(buttonKey("yes_button"), null)).build();
                    ActionButton noButton = contains("no_button", Map.class) ?
                        parseButton("no_button", getConfigurationSection("no_button"), context) :
                        ActionButton.builder(Component.text("No")).action(DialogAction.customClick(buttonKey("no_button"), null)).build();
                    yield DialogType.confirmation(yesButton, noButton);
                }
                case "multi_action" -> {
                    List<ActionButton> buttons = new ArrayList<>();
                    if (contains("buttons", Map.class)) {
                        YamlConfiguration buttonsSection = getConfigurationSection("buttons");
                        for (StringHolder buttonIdHolder : buttonsSection.getKeys(false)) {
                            String buttonId = buttonIdHolder.str;
                            YamlConfiguration btnSection = buttonsSection.getConfigurationSection(buttonId);
                            if (btnSection != null) {
                                buttons.add(parseButton(buttonId, btnSection, context));
                            }
                        }
                    }
                    yield DialogType.multiAction(buttons).build();
                }
                default -> { // "notice"
                    ActionButton button = contains("button", Map.class) ?
                        parseButton("button", getConfigurationSection("button"), context) :
                        ActionButton.builder(Component.text("OK")).action(DialogAction.customClick(buttonKey("button"), null)).build();
                    yield DialogType.notice(button);
                }
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

    public MapTag buildInputsMap(DialogResponseView response) {
        MapTag map = new MapTag();
        for (Map.Entry<String, String> entry : inputTypes.entrySet()) {
            String id = entry.getKey();
            switch (entry.getValue()) {
                case "boolean" -> {
                    Boolean val = response.getBoolean(id);
                    if (val != null) {
                        map.putObject(id, new ElementTag(val));
                    }
                }
                case "number" -> {
                    Float val = response.getFloat(id);
                    if (val != null) {
                        map.putObject(id, new ElementTag(val));
                    }
                }
                default -> { // "text", "option"
                    String val = response.getText(id);
                    if (val != null) {
                        map.putObject(id, new ElementTag(val));
                    }
                }
            }
        }
        return map;
    }

    public String getInlineScriptPath(String buttonId) {
        return switch (buttonId) {
            case "button" -> "button.on click";
            case "yes_button" -> "yes_button.on click";
            case "no_button" -> "no_button.on click";
            default -> "buttons." + buttonId + ".on click";
        };
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
            DialogScriptContainer container = null;
            for (Map.Entry<String, DialogScriptContainer> entry : DialogScriptContainer.dialogScripts.entrySet()) {
                if (DialogScriptContainer.toKeyValue(entry.getKey()).equals(scriptKeyPart)) {
                    container = entry.getValue();
                }
            }
            if (container == null) {
                return;
            }
            String btnId = value.substring(slashIdx + 1);
            String path = container.getInlineScriptPath(btnId);
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
                InstantQueue queue = new InstantQueue(container.getName() + "_" + btnId);
                queue.addDefinition("inputs", responseView != null ? container.buildInputsMap(responseView) : new MapTag());
                queue.addDefinition("button_id", new ElementTag(btnId, true));
                queue.addEntries(entries);
                queue.start();
            }
            catch (Exception ex) {
                Debug.echoError("Exception while running inline script for dialog '" + container.getName() + "' button '" + btnId + "':");
                Debug.echoError(ex);
            }
        }
    }
}
