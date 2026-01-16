package net.leaderos.hytale.plugin.configuration;


import eu.okaeri.configs.OkaeriConfig;
import eu.okaeri.configs.annotation.Comment;
import eu.okaeri.configs.annotation.NameStrategy;
import eu.okaeri.configs.annotation.Names;
import lombok.Getter;
import lombok.Setter;

import java.util.Arrays;
import java.util.List;

/**
 * Modules config file
 * @author leaderos
 * @since 1.0
 */
@Getter
@Setter
@Names(strategy = NameStrategy.IDENTITY)
public class Modules extends OkaeriConfig {
    /**
     * AI module
     */
    private Ai Ai = new Ai();

    /**
     * AI module settings
     *
     * @since 1.0
     */
    @Getter
    @Setter
    public static class Ai extends OkaeriConfig {
        /**
         * Status of AI module
         */
        private boolean status = false;
    }
    
    /**
     * Verify module
     */
    private Verify Verify = new Verify();

    /**
     * Verify module settings
     *
     * @since 1.0
     * @author leaderos
     */
    @Getter
    @Setter
    public static class Verify extends OkaeriConfig {
        /**
         * Status of Verify module
         */
        private boolean status = false;
    }

    /**
     * Discord module
     */
    private Discord Discord = new Discord();

    /**
     * Discord module settings
     *
     * @since 1.0
     * @author rafaelflromao
     */
    @Getter
    @Setter
    public static class Discord extends OkaeriConfig {
        /**
         * Status of Discord mode
         */
        private boolean status = false;
    }

    /**
     * Credit module
     */
    private Credit Credit = new Credit();

    /**
     * Credit module settings
     *
     * @since 1.0
     * @author leaderos
     */
    @Getter
    @Setter
    public static class Credit extends OkaeriConfig {
        /**
         * Status of Cache mode
         */
        private boolean status = false;
    }

    /**
     * Connect module setting
     */
    private Connect Connect = new Connect();

    /**
     * Connect module settings
     *
     * @since 1.0
     * @author leaderos
     */
    @Getter
    @Setter
    public static class Connect extends OkaeriConfig {
        /**
         * Status of Connect mode
         */
        private boolean status = false;

        /**
         * Server token name
         */
        @Comment("You can get your server token from Dashboard > Store > Game Servers")
        private String serverToken = "YOUR_SERVER_TOKEN";

        /**
         * Executes commands only if player is online
         */
        @Comment("Executes commands only if player is online")
        private boolean onlyOnline = true;

        /**
         * Executes command with delay when player comes online (in seconds)
         */
        private int executeDelay = 5;

        /**
         * Fallback timer in minutes
         */
        @Comment({
                "If commands are not executed for any reason, this option will fetch the queue every x minutes.",
                "Set to 0 to disable this option."
        })
        private long fallbackTimer = 0;

        /**
         * You can activate this option if you are experiencing disconnections. (in seconds) (0 to disable)
         */
        @Comment({
                "You can activate this option if you are experiencing disconnections.",
                "Set 0 to disable this option. If you are experiencing disconnections, you can set this option to 10 or higher.",
                "This option will check the connection every x seconds and reconnect if the connection is lost.",
                "Restart the server after changing this option. Reload will not work!"
        })
        private long reconnectionTimer = 0;

        /**
         * List of commands that are blocked from being executed by the module
         */
        @Comment({
                "Blacklist of commands to be executed",
                "You can add commands that you do not want to be executed here.",
                "Example: /shutdown"
        })
        private List<String> commandBlacklist = Arrays.asList(
                "op",
                "stop"
        );
    }
}