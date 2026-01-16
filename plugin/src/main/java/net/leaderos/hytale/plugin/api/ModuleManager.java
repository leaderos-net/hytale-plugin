package net.leaderos.hytale.plugin.api;

import lombok.SneakyThrows;
import net.leaderos.hytale.plugin.LeaderosPlugin;
import net.leaderos.hytale.plugin.configuration.Language;
import net.leaderos.hytale.plugin.helpers.ChatUtil;
import net.leaderos.hytale.shared.modules.Modulable;

import java.util.HashMap;

/**
 * ModuleManager of leaderos-plugin
 *
 * @author leaderos
 * @since 1.0
 */
public class ModuleManager {

    /**
     * List of modules
     */
    private static HashMap<String, Modulable> modules = new HashMap<>();

    /**
     * Module getter
     *
     * @param name of module
     * @return module
     */
    public static Modulable getModule(String name) {
        return modules.get(name);
    }

    /**
     * Registers module to module list
     *
     * @param module of leaderos-plugin
     */
    public void registerModule(Modulable module) {
        modules.put(module.getName(), module);
    }

    /**
     * Gets module status
     *
     * @param moduleName name of module
     * @return status
     */
    @SneakyThrows
    public static boolean getModuleStatus(String moduleName) {
        switch (moduleName) {
            case "Ai":
                return LeaderosPlugin.getInstance().getModulesFile().getAi().isStatus();
            case "Verify":
                return LeaderosPlugin.getInstance().getModulesFile().getVerify().isStatus();
            case "Discord":
                return LeaderosPlugin.getInstance().getModulesFile().getDiscord().isStatus();
            case "Credit":
                return LeaderosPlugin.getInstance().getModulesFile().getCredit().isStatus();
            case "Connect":
                return LeaderosPlugin.getInstance().getModulesFile().getConnect().isStatus();
            default:
                return false;
        }
    }

    /**
     * Enables all modules
     */
    public void enableModules() {
        Language lang = LeaderosPlugin.getInstance().getLangFile();
        modules.keySet().forEach(moduleName -> {
            Modulable module = getModule(moduleName);
            // Checks if module has dependency
            if (!module.getDependencies().isEmpty()) {
                // Dependency status of module
                boolean dependStatus = module.getDependencies().stream()
                        .allMatch(ModuleManager::getModuleStatus);
                // If requirements not met disables module
                if (!dependStatus) {
                    module.setEnabled(false);
                    String message = lang.getMessages().getInfo().getMissingDependency()
                            .replace("%module_name%", module.getName())
                            .replace("%dependencies%", module.getDependencyListAsString());
                    ChatUtil.sendConsoleMessage(message);
                    return;
                }
            }
            if (getModuleStatus(module.getName())) {
                module.setEnabled(true);
                module.onEnable();
                String message = lang.getMessages().getInfo().getModuleEnabled()
                        .replace("%module_name%", module.getName());
                ChatUtil.sendConsoleMessage(message);
            } else {
                module.setEnabled(false);
                String message = lang.getMessages().getInfo().getModuleClosed()
                        .replace("%module_name%", module.getName());
                ChatUtil.sendConsoleMessage(message);
            }
        });
    }

    /**
     * Disables all modules
     */
    public void disableModules() {
        modules.keySet().forEach(moduleName -> {
            Modulable module = modules.get(moduleName);
            if (module.isEnabled()) {
                module.setEnabled(false);
                module.onDisable();
                String message = LeaderosPlugin.getInstance().getLangFile().getMessages().getInfo().getModuleDisabled()
                        .replace("%module_name%", module.getName());
                ChatUtil.sendConsoleMessage(message);
            }
        });
    }

    /**
     * reload modules
     */
    private void reload() {
        modules.keySet().forEach(moduleName -> {
            Modulable module = modules.get(moduleName);
            if (module.isEnabled())
                module.onReload();
        });
    }

    /**
     * Reload modules
     */
    public void reloadModules() {
        reload();
        disableModules();
        enableModules();
    }

    /**
     * Constructor of ModuleManager
     */
    public ModuleManager() {
    }
}