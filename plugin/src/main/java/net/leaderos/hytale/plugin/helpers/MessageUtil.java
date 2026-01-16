package net.leaderos.hytale.plugin.helpers;

import com.hypixel.hytale.protocol.MaybeBool;
import com.hypixel.hytale.server.core.Message;

import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MessageUtil {
    // Matches <tag>, <tag:arg>, </tag>
    private static final Pattern TAG_PATTERN = Pattern.compile("<(/?)([a-zA-Z0-9_]+)(?::([^>]+))?>");

    // Matches Minecraft color codes like &a, &e, &l, &r, etc.
    private static final Pattern MINECRAFT_CODE_PATTERN = Pattern.compile("&([0-9a-fk-or])");

    private static final Map<String, Color> NAMED_COLORS = new HashMap<>();
    private static final Map<Character, Color> MINECRAFT_COLORS = new HashMap<>();

    static {
        NAMED_COLORS.put("black", new Color(0, 0, 0));
        NAMED_COLORS.put("dark_blue", new Color(0, 0, 170));
        NAMED_COLORS.put("dark_green", new Color(0, 170, 0));
        NAMED_COLORS.put("dark_aqua", new Color(0, 170, 170));
        NAMED_COLORS.put("dark_red", new Color(170, 0, 0));
        NAMED_COLORS.put("dark_purple", new Color(170, 0, 170));
        NAMED_COLORS.put("gold", new Color(255, 170, 0));
        NAMED_COLORS.put("gray", new Color(170, 170, 170));
        NAMED_COLORS.put("dark_gray", new Color(85, 85, 85));
        NAMED_COLORS.put("blue", new Color(85, 85, 255));
        NAMED_COLORS.put("green", new Color(85, 255, 85));
        NAMED_COLORS.put("aqua", new Color(85, 255, 255));
        NAMED_COLORS.put("red", new Color(255, 85, 85));
        NAMED_COLORS.put("light_purple", new Color(255, 85, 255));
        NAMED_COLORS.put("yellow", new Color(255, 255, 85));
        NAMED_COLORS.put("white", new Color(255, 255, 255));

        // Minecraft color code mappings
        MINECRAFT_COLORS.put('0', new Color(0, 0, 0));           // Black
        MINECRAFT_COLORS.put('1', new Color(0, 0, 170));         // Dark Blue
        MINECRAFT_COLORS.put('2', new Color(0, 170, 0));         // Dark Green
        MINECRAFT_COLORS.put('3', new Color(0, 170, 170));       // Dark Aqua
        MINECRAFT_COLORS.put('4', new Color(170, 0, 0));         // Dark Red
        MINECRAFT_COLORS.put('5', new Color(170, 0, 170));       // Dark Purple
        MINECRAFT_COLORS.put('6', new Color(255, 170, 0));       // Gold
        MINECRAFT_COLORS.put('7', new Color(170, 170, 170));     // Gray
        MINECRAFT_COLORS.put('8', new Color(85, 85, 85));        // Dark Gray
        MINECRAFT_COLORS.put('9', new Color(85, 85, 255));       // Blue
        MINECRAFT_COLORS.put('a', new Color(85, 255, 85));       // Green
        MINECRAFT_COLORS.put('b', new Color(85, 255, 255));      // Aqua
        MINECRAFT_COLORS.put('c', new Color(255, 85, 85));       // Red
        MINECRAFT_COLORS.put('d', new Color(255, 85, 255));      // Light Purple
        MINECRAFT_COLORS.put('e', new Color(255, 255, 85));      // Yellow
        MINECRAFT_COLORS.put('f', new Color(255, 255, 255));     // White
    }

    private record StyleState(
            Color color,
            List<Color> gradient,
            boolean bold,
            boolean italic,
            boolean underlined,
            boolean monospace, String link) {

        StyleState() {
            this(null, null, false, false, false, false, null);
        }

        StyleState copy() {
            return new StyleState(color, gradient, bold, italic, underlined, monospace, link);
        }

        StyleState withColor(Color color) {
            return new StyleState(color, null, bold, italic, underlined, monospace, link);
        }

        StyleState withGradient(List<Color> gradient) {
            return new StyleState(null, gradient, bold, italic, underlined, monospace, link);
        }

        StyleState withBold(boolean bold) {
            return new StyleState(color, gradient, bold, italic, underlined, monospace, link);
        }

        StyleState withItalic(boolean italic) {
            return new StyleState(color, gradient, bold, italic, underlined, monospace, link);
        }

        StyleState withUnderlined(boolean underlined) {
            return new StyleState(color, gradient, bold, italic, underlined, monospace, link);
        }

        StyleState withMonospace(boolean monospace) {
            return new StyleState(color, gradient, bold, italic, underlined, monospace, link);
        }

        StyleState withLink(String link) {
            return new StyleState(color, gradient, bold, italic, underlined, monospace, link);
        }

        StyleState reset() {
            return new StyleState();
        }
    }

    /**
     * Parses a string containing TinyMsg formatting tags and Minecraft color codes, converting it into a Hytale Message.
     * <p>
     * This method processes all supported tags including colors, gradients, styles, and links.
     * It also supports Minecraft-style color codes (e.g., &a, &e, &l, &r).
     * Tags can be nested indefinitely for complex formatting.
     * </p>
     *
     * @param text The string to parse, containing TinyMsg formatting tags and/or Minecraft color codes
     * @return A formatted {@link Message} object ready to be sent to players
     * @throws NullPointerException if text is null
     * @see Message
     */
    public static Message parse(String text) {
        // First, convert Minecraft color codes to our internal format
        text = preprocessMinecraftCodes(text);

        if (!text.contains("<")) {
            return Message.raw(text);
        }

        Message root = Message.empty();

        // Stack keeps track of nested styles.
        Deque<StyleState> stateStack = new ArrayDeque<>();
        stateStack.push(new StyleState());

        Matcher matcher = TAG_PATTERN.matcher(text);
        int lastIndex = 0;

        while (matcher.find()) {
            int start = matcher.start();
            int end = matcher.end();

            // Handle text BEFORE this tag
            if (start > lastIndex) {
                String content = text.substring(lastIndex, start);
                Message segmentMsg = createStyledMessage(content, stateStack.peek());
                root.insert(segmentMsg);
            }

            // Process the tag
            boolean isClosing = "/".equals(matcher.group(1));
            String tagName = matcher.group(2).toLowerCase();
            String tagArg = matcher.group(3);

            if (isClosing) {
                if (stateStack.size() > 1) {
                    stateStack.pop();
                }
            } else {
                StyleState currentState = stateStack.peek();
                StyleState newState = currentState.copy();

                if (NAMED_COLORS.containsKey(tagName)) {
                    newState = newState.withColor(NAMED_COLORS.get(tagName));
                } else {
                    switch (tagName) {
                        case "color":
                        case "c":
                        case "colour":
                            Color c = parseColorArg(tagArg);
                            if (c != null) newState = newState.withColor(c);
                            break;

                        case "grnt":
                        case "gradient":
                            if (tagArg != null) {
                                List<Color> colors = parseGradientColors(tagArg);
                                if (!colors.isEmpty()) {
                                    newState = newState.withGradient(colors);
                                }
                            }
                            break;

                        case "bold":
                        case "b":
                            newState = newState.withBold(true);
                            break;

                        case "italic":
                        case "i":
                        case "em":
                            newState = newState.withItalic(true);
                            break;

                        case "underline":
                        case "u":
                            newState = newState.withUnderlined(true);
                            break;

                        case "monospace":
                        case "mono":
                            newState = newState.withMonospace(true);
                            break;

                        case "link":
                        case "url":
                            if (tagArg != null) newState = newState.withLink(tagArg);
                            break;

                        case "reset":
                        case "r":
                            stateStack.clear();
                            newState = new StyleState();
                            break;
                    }
                }
                stateStack.push(newState);
            }

            lastIndex = end;
        }

        if (lastIndex < text.length()) {
            String content = text.substring(lastIndex);
            Message segmentMsg = createStyledMessage(content, stateStack.peek());
            root.insert(segmentMsg);
        }

        return root;
    }

    /**
     * Converts Minecraft color codes (&a, &e, &l, etc.) into TinyMsg tag format.
     * This allows seamless integration of both formatting systems.
     *
     * @param text The text containing Minecraft color codes
     * @return Text with Minecraft codes converted to TinyMsg tags
     */
    private static String preprocessMinecraftCodes(String text) {
        if (!text.contains("&")) {
            return text;
        }

        StringBuilder result = new StringBuilder();
        Matcher matcher = MINECRAFT_CODE_PATTERN.matcher(text);
        int lastIndex = 0;

        while (matcher.find()) {
            result.append(text, lastIndex, matcher.start());

            char code = matcher.group(1).charAt(0);

            // Handle formatting codes
            switch (code) {
                case 'k': // Obfuscated - not commonly supported, skip
                    break;
                case 'l': // Bold
                    result.append("<bold>");
                    break;
                case 'm': // Strikethrough - not in original, skip
                    break;
                case 'n': // Underline
                    result.append("<u>");
                    break;
                case 'o': // Italic
                    result.append("<italic>");
                    break;
                case 'r': // Reset
                    result.append("<reset>");
                    break;
                default: // Color codes (0-9, a-f)
                    if (MINECRAFT_COLORS.containsKey(code)) {
                        Color color = MINECRAFT_COLORS.get(code);
                        String hex = String.format("#%02x%02x%02x",
                                color.getRed(), color.getGreen(), color.getBlue());
                        result.append("<color:").append(hex).append(">");
                    }
                    break;
            }

            lastIndex = matcher.end();
        }

        result.append(text.substring(lastIndex));
        return result.toString();
    }

    private static Message createStyledMessage(String content, StyleState state) {
        if (state.gradient != null && !state.gradient.isEmpty()) {
            return applyGradient(content, state);
        }

        Message msg = Message.raw(content);

        if (state.color != null) msg.color(state.color);
        if (state.bold) msg.bold(true);
        if (state.italic) msg.italic(true);
        if (state.monospace) msg.monospace(true);
        if (state.underlined) msg.getFormattedMessage().underlined = MaybeBool.True;
        if (state.link != null) msg.link(state.link);

        return msg;
    }

    private static Message applyGradient(String text, StyleState state) {
        Message container = Message.empty();
        List<Color> colors = state.gradient;
        int length = text.length();

        for (int index = 0; index < length; index++) {
            char ch = text.charAt(index);
            float progress = index / (float) Math.max(length - 1, 1);
            Color color = interpolateColor(colors, progress);

            Message charMsg = Message.raw(String.valueOf(ch)).color(color);

            if (state.bold) charMsg.bold(true);
            if (state.italic) charMsg.italic(true);
            if (state.monospace) charMsg.monospace(true);
            if (state.underlined) charMsg.getFormattedMessage().underlined = MaybeBool.True;
            if (state.link != null) charMsg.link(state.link);

            container.insert(charMsg);
        }
        return container;
    }

    private static Color parseColorArg(String arg) {
        if (arg == null) return null;
        return NAMED_COLORS.containsKey(arg) ? NAMED_COLORS.get(arg) : parseHexColor(arg);
    }

    private static List<Color> parseGradientColors(String arg) {
        List<Color> colors = new ArrayList<>();
        for (String part : arg.split(":")) {
            Color c = parseColorArg(part);
            if (c != null) colors.add(c);
        }
        return colors;
    }

    private static Color parseHexColor(String hex) {
        try {
            String clean = hex.replace("#", "");
            if (clean.length() == 6) {
                int r = Integer.parseInt(clean.substring(0, 2), 16);
                int g = Integer.parseInt(clean.substring(2, 4), 16);
                int b = Integer.parseInt(clean.substring(4, 6), 16);
                return new Color(r, g, b);
            }
            return null;
        } catch (Exception e) {
            return null;
        }
    }

    private static Color interpolateColor(List<Color> colors, float progress) {
        float clampedProgress = Math.max(0f, Math.min(1f, progress));
        float scaledProgress = clampedProgress * (colors.size() - 1);
        int index = Math.min((int) scaledProgress, colors.size() - 2);
        float localProgress = scaledProgress - index;

        Color c1 = colors.get(index);
        Color c2 = colors.get(index + 1);

        int r = (int) (c1.getRed() + (c2.getRed() - c1.getRed()) * localProgress);
        int g = (int) (c1.getGreen() + (c2.getGreen() - c1.getGreen()) * localProgress);
        int b = (int) (c1.getBlue() + (c2.getBlue() - c1.getBlue()) * localProgress);

        return new Color(r, g, b);
    }
}