package de.stylelabor.markap.ftb_checker_reworked;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.mojang.logging.LogUtils;
import org.slf4j.Logger;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Configuration handler using JSON file.
 * Platform-agnostic - no Forge/Fabric specific dependencies.
 */
public class Config {

    private static final Logger LOGGER = LogUtils.getLogger();
    private static Config instance;
    private ConfigData configData;

    public static Config getInstance() {
        if (instance == null) {
            instance = new Config();
        }
        return instance;
    }

    private Config() {
        configData = new ConfigData();
    }

    /**
     * Load mods configuration from JSON file.
     */
    public void loadModsFromFile() {
        try {
            Path path = Paths.get("config/mod_checker_reworked.json");
            if (!Files.exists(path)) {
                createDefaultConfigFile();
            } else {
                try (InputStreamReader reader = new InputStreamReader(Files.newInputStream(path))) {
                    configData = new Gson().fromJson(reader, ConfigData.class);
                    // Handle case where file might be valid JSON but not our object (or just nulls)
                    if (configData == null) {
                        configData = new ConfigData();
                    }
                    if (configData.mods == null) {
                        configData.mods = Collections.emptyList();
                    }
                } catch (Exception e) {
                    LOGGER.error("Failed to parse config file, likely old format. Recreating default.", e);
                    createDefaultConfigFile();
                }
            }
            LOGGER.info("Loaded {} mod configurations", configData.mods.size());
        } catch (IOException e) {
            LOGGER.error("Failed to load mod configuration", e);
        }
    }

    private void createDefaultConfigFile() throws IOException {
        Path configDir = Paths.get("config");
        if (!Files.exists(configDir)) {
            Files.createDirectories(configDir);
        }

        List<ModConfig> defaultMods = List.of(
                new ModConfig("ftbchunks", "https://www.curseforge.com/api/v1/mods/314906/files/5378090/download",
                        "https://www.curseforge.com/minecraft/mc-mods/ftb-chunks-forge", "2101.3.1"),
                new ModConfig("ftbquests", "https://www.curseforge.com/api/v1/mods/289412/files/5543955/download",
                        "https://www.curseforge.com/minecraft/mc-mods/ftb-quests-forge", "2101.4.1"),
                new ModConfig("ftblibrary", "https://www.curseforge.com/api/v1/mods/404465/files/5567591/download",
                        "https://www.curseforge.com/minecraft/mc-mods/ftb-library", "2101.1.4"),
                new ModConfig("ftbteams", "https://www.curseforge.com/api/v1/mods/404468/files/5267190/download",
                        "https://www.curseforge.com/minecraft/mc-mods/ftb-teams", "2101.3.0"),
                new ModConfig("ftbultimine", "https://www.curseforge.com/api/v1/mods/386134/files/5363345/download",
                        "https://www.curseforge.com/minecraft/mc-mods/ftb-ultimine", "2101.3.0"));

        ConfigData data = new ConfigData();
        data.mods = defaultMods;

        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        try (OutputStreamWriter writer = new OutputStreamWriter(
                Files.newOutputStream(Paths.get("config/mod_checker_reworked.json")))) {
            gson.toJson(data, writer);
        }
        this.configData = data;
        LOGGER.info("Created default configuration file");
    }

    public List<ModConfig> getMods() {
        return configData.mods;
    }

    public Optional<String> getDirectDownloadLink(String modId) {
        return configData.mods.stream()
                .filter(mod -> mod.modId.equals(modId))
                .map(mod -> mod.directDownloadLink)
                .findFirst();
    }

    public Optional<String> getWebsiteDownloadLink(String modId) {
        return configData.mods.stream()
                .filter(mod -> mod.modId.equals(modId))
                .map(mod -> mod.websiteDownloadLink)
                .findFirst();
    }

    public Optional<String> getVersion(String modId) {
        return configData.mods.stream()
                .filter(mod -> mod.modId.equals(modId))
                .map(mod -> mod.version != null ? mod.version : "")
                .findFirst();
    }

    private static class ConfigData {
        public List<ModConfig> mods = Collections.emptyList();
    }

    /**
     * Mod configuration data class.
     */
    public static class ModConfig {
        public String modId;
        public String directDownloadLink;
        public String websiteDownloadLink;
        public String version;

        public ModConfig() {
        }

        public ModConfig(String modId, String directDownloadLink, String websiteDownloadLink, String version) {
            this.modId = modId;
            this.directDownloadLink = directDownloadLink;
            this.websiteDownloadLink = websiteDownloadLink;
            this.version = version;
        }
    }
}
