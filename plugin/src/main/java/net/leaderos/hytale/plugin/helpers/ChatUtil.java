package net.leaderos.hytale.plugin.helpers;

import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandSender;
import com.hypixel.hytale.server.core.console.ConsoleSender;
import com.hypixel.hytale.server.core.entity.entities.Player;
import net.leaderos.hytale.plugin.LeaderosPlugin;
import net.leaderos.hytale.shared.helpers.Placeholder;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.logging.Level;
import java.util.stream.Collectors;

/**
 * @author leaderos
 * @since 1.0
 */
public class ChatUtil {

    /**
     * Applies chat color formats to message
     * @param message to convert
     * @return String of converted message
     */
    public static Message color(String message) {
        return MessageUtil.parse(message);
    }

    /**
     * Applies chat color formats to list
     * @param list to convert
     * @return List of converted message
     */
    public static List<Message> color(List<String> list) {
        return list.stream().map(ChatUtil::color).collect(Collectors.toList());
    }

    /**
     * Get colored message with prefix
     * @param message to send
     */
    public static Message getMessage(String message) {
        return color(
                replacePlaceholders(message, new Placeholder("{prefix}",
                        LeaderosPlugin.getInstance().getLangFile().getMessages().getPrefix()))
        );
    }

    public static void sendConsoleMessage(String message) {
        ConsoleSender.INSTANCE.sendMessage(
                color(replacePlaceholders(message, new Placeholder("{prefix}",
                        LeaderosPlugin.getInstance().getLangFile().getMessages().getPrefix()))
                )
        );
    }

    public static void sendMessage(@NotNull CommandSender target, String message) {
        target.sendMessage(color(
                replacePlaceholders(message, new Placeholder("{prefix}",
                        LeaderosPlugin.getInstance().getLangFile().getMessages().getPrefix()))
        ));
    }

    public static void sendMessage(@NotNull Player target, String message) {
        target.sendMessage(color(
                replacePlaceholders(message, new Placeholder("{prefix}",
                        LeaderosPlugin.getInstance().getLangFile().getMessages().getPrefix()))
        ));
    }

    public static void sendMessage(@NotNull CommandSender target, List<String> messages) {
        String combinedMessage = messages.stream()
                .collect(Collectors.joining("\n"));

        target.sendMessage(color(
                replacePlaceholders(combinedMessage, new Placeholder("{prefix}",
                        LeaderosPlugin.getInstance().getLangFile().getMessages().getPrefix()))
        ));
    }

    public static void sendMessage(@NotNull Player target, List<String> messages) {
        String combinedMessage = messages.stream()
                .collect(Collectors.joining("\n"));

        target.sendMessage(color(
                replacePlaceholders(combinedMessage, new Placeholder("{prefix}",
                        LeaderosPlugin.getInstance().getLangFile().getMessages().getPrefix()))
        ));
    }

    /**
     * Replaces placeholder data on string
     * <p><b>also format chat messages too @see ChatUtil#color(String)</b></p>
     *
     * @param string to be converted
     * @param placeholders additional placeholder data
     * @return converted string value
     */
    public static String replacePlaceholders(String string, Placeholder... placeholders) {
        for (Placeholder placeholder : placeholders) {
            string = string.replace(placeholder.getKey(), placeholder.getValue());
        }
        return string;
    }

    /**
     * Replaces placeholder data on list
     *
     * @param list to be converted
     * @param placeholders additional placeholder data
     * @return converted list value
     */
    public static List<String> replacePlaceholders(List<String> list, Placeholder... placeholders) {
        return list.stream().map(s-> replacePlaceholders(s, placeholders)).collect(Collectors.toList());
    }
}
