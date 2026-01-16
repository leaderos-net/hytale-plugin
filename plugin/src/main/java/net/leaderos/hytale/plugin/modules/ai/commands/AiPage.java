package net.leaderos.hytale.plugin.modules.ai.commands;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.protocol.packets.interface_.CustomPageLifetime;
import com.hypixel.hytale.protocol.packets.interface_.CustomUIEventBindingType;
import com.hypixel.hytale.protocol.packets.interface_.Page;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.entity.entities.player.pages.InteractiveCustomUIPage;
import com.hypixel.hytale.server.core.ui.builder.EventData;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.ui.builder.UIEventBuilder;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import net.leaderos.hytale.plugin.LeaderosPlugin;
import net.leaderos.hytale.plugin.helpers.ChatUtil;
import net.leaderos.hytale.shared.helpers.RequestUtil;
import net.leaderos.hytale.shared.model.Response;
import net.leaderos.hytale.shared.model.request.impl.ai.AiRequest;

import javax.annotation.Nonnull;
import java.net.HttpURLConnection;
import java.util.concurrent.CompletableFuture;

/**
 * FormPage - A page with input fields and checkboxes.
 *
 * This is the most complex example - it demonstrates:
 *   1. Reading input values from TextFields
 *   2. Reading checkbox states
 *   3. Using the @-prefix to bind input values to EventData
 *   4. Handling different button actions (Ask vs Close)
 *
 * KEY CONCEPT: The @-prefix
 *   - In the Codec: "@PlayerName" means "this value comes from an input"
 *   - In EventData.append(): "@PlayerName", "#PromptInput.Value" binds the input value
 *   - When the event fires, the current input value is automatically included
 */
public class AiPage extends InteractiveCustomUIPage<AiPage.FormEventData> {

    /**
     * FormEventData - Contains all data from the form.
     *
     * Fields:
     *   - action: Which button was clicked ("Ask" or "Close")
     *   - playerName: Value from the text input field
     *   - notifications: State of the notifications checkbox
     *   - coordinates: State of the coordinates checkbox
     *
     * The CODEC defines how to serialize/deserialize each field.
     */
    public static class FormEventData {
        public String action;           // Button action identifier
        public String prompt;       // Text input value

        /**
         * Codec for serializing/deserializing the event data.
         *
         * IMPORTANT: Fields that read from UI inputs use the @-prefix:
         *   - "Action" - regular field, set explicitly in EventData
         *   - "@PlayerName" - bound to an input element's value
         *   - "@Notifications" - bound to a checkbox's value
         *
         * The pattern for each field:
         *   .append(
         *       new KeyedCodec<>("FieldName", Codec.TYPE),  // Name and type
         *       (obj, val) -> obj.field = val,              // Setter
         *       obj -> obj.field                            // Getter
         *   )
         */
        public static final BuilderCodec<FormEventData> CODEC = BuilderCodec.builder(FormEventData.class, FormEventData::new)
                // Regular field - which button was clicked
                .append(new KeyedCodec<>("Action", Codec.STRING), (FormEventData o, String v) -> o.action = v, (FormEventData o) -> o.action)
                .add()
                // Input binding - text field value (note the @ prefix!)
                .append(new KeyedCodec<>("@Prompt", Codec.STRING), (FormEventData o, String v) -> o.prompt = v, (FormEventData o) -> o.prompt)
                .add()
                .build();
    }

    public AiPage(@Nonnull PlayerRef playerRef) {
        super(playerRef, CustomPageLifetime.CanDismissOrCloseThroughInteraction, FormEventData.CODEC);
    }

    @Override
    public void build(
            @Nonnull Ref<EntityStore> ref,
            @Nonnull UICommandBuilder commandBuilder,
            @Nonnull UIEventBuilder eventBuilder,
            @Nonnull Store<EntityStore> store
    ) {
        // Load the form UI
        commandBuilder.append("Pages/AiPage.ui");

        // Bind Save button with ALL input values
        // The EventData captures:
        //   - "Action" = "Save" (explicit value)
        //   - "@Prompt" = current value of #PromptInput TextField
        eventBuilder.addEventBinding(
                CustomUIEventBindingType.Activating,
                "#AskButton",
                new EventData()
                        .append("Action", "Ask")
                        .append("@Prompt", "#PromptInput.Value")                    // Read TextField value
        );

        // Bind Close button - only needs action, no input values
        /*eventBuilder.addEventBinding(
                CustomUIEventBindingType.Activating,
                "#CloseButton",
                new EventData().append("Action", "Close")
        );*/
    }

    @Override
    public void handleDataEvent(
            @Nonnull Ref<EntityStore> ref,
            @Nonnull Store<EntityStore> store,
            @Nonnull FormEventData data
    ) {
        Player player = store.getComponent(ref, Player.getComponentType());

        // Handle different actions
        if ("Ask".equals(data.action) && data.prompt != null && RequestUtil.canRequest(player.getUuid())) {
            CompletableFuture.runAsync(() -> {
                RequestUtil.addRequest(player.getUuid());
                ChatUtil.sendMessage(player, LeaderosPlugin.getInstance().getLangFile().getMessages().getAi().getGenerating());

                try {
                    // Get player locale
                    String locale = LeaderosPlugin.getInstance().getConfigFile().getSettings().getLang();

                    Response aiRequest = new AiRequest(data.prompt, locale).getResponse();
                    if (aiRequest.getResponseCode() == HttpURLConnection.HTTP_OK && aiRequest.getResponseMessage().getBoolean("status")) {
                        String message = aiRequest.getResponseMessage().getJSONObject("data").getString("message");
                        ChatUtil.sendMessage(player,
                                LeaderosPlugin.getInstance().getLangFile().getMessages().getAi().getAiMessage()
                                        .replace("{message}", message)
                        );
                    } else {
                        ChatUtil.sendMessage(player, LeaderosPlugin.getInstance().getLangFile().getMessages().getAi().getFailMessage());
                    }
                } catch (Exception e) {
                    ChatUtil.sendMessage(player, LeaderosPlugin.getInstance().getLangFile().getMessages().getAi().getFailMessage());
                }
                RequestUtil.invalidate(player.getUuid());
            });
        }
        // For "Close", we just close without doing anything

        // Close the page
        player.getPageManager().setPage(ref, store, Page.None);
    }
}