package de.stylelabor.markap.ftb_checker_reworked;

import com.mojang.logging.LogUtils;
import de.stylelabor.markap.ftb_checker_reworked.platform.ModCheckerPlatform;
import org.slf4j.Logger;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Common mod class containing platform-agnostic logic.
 * Platform-specific entry points call into this class.
 */
public class ModCheckerReworked {

    public static final String MOD_ID = "ftb_checker_reworked";
    private static final Logger LOGGER = LogUtils.getLogger();

    private static ModCheckerReworked instance;
    private List<String> missingMods;
    private boolean initialized = false;

    public static ModCheckerReworked getInstance() {
        if (instance == null) {
            instance = new ModCheckerReworked();
        }
        return instance;
    }

    /**
     * Initialize the mod and check for missing mods.
     */
    public void init() {
        LOGGER.info("Initializing {} on {}", MOD_ID, ModCheckerPlatform.getPlatformName());

        // Load configuration
        Config.getInstance().loadModsFromFile();

        // Check for missing mods
        checkForMissingMods();

        initialized = true;
    }

    /**
     * Check which required mods are missing.
     */
    private void checkForMissingMods() {
        List<String> requiredMods = Config.getInstance().getMods().stream()
                .map(mod -> mod.modId)
                .collect(Collectors.toList());

        LOGGER.info("Required Mods: {}", requiredMods);

        missingMods = requiredMods.stream()
                .filter(modId -> {
                    boolean isPresent = ModCheckerPlatform.isModLoaded(modId);
                    LOGGER.info("Mod ID: {}, Present: {}", modId, isPresent);
                    return !isPresent;
                })
                .collect(Collectors.toList());

        LOGGER.info("Missing Mods: {}", missingMods);
    }

    /**
     * Check if there are missing mods that need to be displayed.
     * 
     * @return true if there are missing mods
     */
    public boolean hasMissingMods() {
        return missingMods != null && !missingMods.isEmpty();
    }

    /**
     * Get the list of missing mods. Once retrieved, the list is cleared
     * so the screen is only shown once.
     * 
     * @return List of missing mod IDs, or null if none
     */
    public List<String> consumeMissingMods() {
        if (missingMods == null || missingMods.isEmpty()) {
            return null;
        }
        List<String> result = missingMods;
        missingMods = null;
        return result;
    }

    public boolean isInitialized() {
        return initialized;
    }
}
