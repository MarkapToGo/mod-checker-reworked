package de.stylelabor.markap.ftb_checker_reworked;

import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Screen displayed when required mods are missing.
 * Uses pure Minecraft GUI APIs - platform agnostic.
 */
public class MissingModsScreen extends Screen {

    private static final Logger LOGGER = LoggerFactory.getLogger(MissingModsScreen.class);
    private final List<String> missingMods;
    private String modsList;
    private int downloadProgress = 0;
    private int totalModsToDownload = 0;
    private boolean isDownloading = false;
    private boolean showRestartScreen = false;

    public MissingModsScreen(List<String> missingMods) {
        super(Component.literal("Missing Mods"));
        this.missingMods = missingMods;
    }

    @Override
    protected void init() {
        LOGGER.info("Initializing MissingModsScreen with mods: {}", missingMods);
        int y = this.height / 4 + 24;

        if (missingMods.size() <= 5) {
            for (String modId : missingMods) {
                String formattedModName = formatModName(modId);
                int finalY = y;
                Config.getInstance().getWebsiteDownloadLink(modId).ifPresent(link -> this.addRenderableWidget(Button
                        .builder(
                                Component.literal("Download ")
                                        .append(Component.literal(formattedModName)
                                                .withStyle(style -> style.withBold(true).withColor(0xFFA500))),
                                button -> Util.getPlatform().openUri(URI.create(link)))
                        .bounds(this.width / 2 - 100, finalY, 200, 20)
                        .build()));
                y += 24;
            }
        } else {
            // Prepare the list of missing mods as a single line of text
            modsList = missingMods.stream()
                    .map(this::formatModName)
                    .reduce((mod1, mod2) -> mod1 + ", " + mod2)
                    .orElse("");
            y += 24;
        }

        // Stop all sounds, including background music
        Minecraft.getInstance().getSoundManager().stop();

        // Add the "Download automatically" button with increased margin
        y += 24;
        this.addRenderableWidget(Button.builder(
                Component.literal("Download automatically")
                        .withStyle(style -> style.withBold(true).withColor(0x90EE90)), // Light green color
                button -> {
                    showMessage();
                    this.isDownloading = true;
                    this.totalModsToDownload = missingMods.size();
                    this.downloadProgress = 0;
                    button.active = false; // Disable button to prevent double clicking
                    new Thread(() -> {
                        AtomicInteger progress = new AtomicInteger();
                        for (String modId : missingMods) {
                            Config.getInstance().getDirectDownloadLink(modId).ifPresent(link -> {
                                try {
                                    String version = Config.getInstance().getVersion(modId).orElse("");
                                    String fileName = version.isEmpty() ? modId + ".jar"
                                            : modId + "-" + version + ".jar";
                                    LOGGER.info("Starting download for {} to {}", modId, fileName);
                                    downloadMod(link, fileName);
                                } catch (Exception e) {
                                    LOGGER.error("Failed to download mod: {}", modId, e);
                                } finally {
                                    int currentProgress = progress.incrementAndGet();
                                    this.downloadProgress = currentProgress;
                                    if (currentProgress == missingMods.size()) {
                                        Objects.requireNonNull(this.minecraft)
                                                .execute(() -> {
                                                    isDownloading = false;
                                                    showRestartScreen = true;
                                                    this.clearWidgets();
                                                    this.addRenderableWidget(Button
                                                            .builder(
                                                                    Component.literal("Close (then manually restart)!"),
                                                                    b -> this.minecraft.stop())
                                                            .bounds(this.width / 2 - 100, this.height / 2 + 24, 200, 20)
                                                            .build());
                                                });
                                    }
                                }
                            });
                        }
                    }).start();
                })
                .bounds(this.width / 2 - 100, y, 200, 20)
                .build());

    }

    @Override
    public void render(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        // Draw a solid dark background to ensure stability across versions (avoiding
        // blit/blur crashes)
        guiGraphics.fill(0, 0, this.width, this.height, 0xFF000000);

        // Render the title
        guiGraphics.drawCenteredString(this.font, this.title.getString(), this.width / 2, 20, 0xFFFFFF);

        if (isDownloading && totalModsToDownload > 0) {
            int barWidth = 200;
            int barHeight = 20;
            int x = this.width / 2 - 100;
            int y = this.height / 4 + 24 + 24 + 24 + 10; // Position below the download button

            // Background of bar
            guiGraphics.fill(x, y, x + barWidth, y + barHeight, 0xFF404040); // Dark gray

            // Foreground of bar
            float progress = (float) downloadProgress / totalModsToDownload;
            int progressWidth = (int) (barWidth * progress);
            guiGraphics.fill(x, y, x + progressWidth, y + barHeight, 0xFF00FF00); // Green

            // Text
            String progressText = "Downloading: " + downloadProgress + " / " + totalModsToDownload;
            guiGraphics.drawCenteredString(this.font, progressText, this.width / 2, y + 6, 0xFFFFFF);
        }

        // Render buttons and other components
        super.render(guiGraphics, mouseX, mouseY, partialTicks);
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return false;
    }

    private void showMessage() {
        if (Objects.requireNonNull(this.minecraft).player != null) {
            Objects.requireNonNull(this.minecraft.player).displayClientMessage(Component.literal("Download started..."),
                    false);
        }
    }

    private String formatModName(String modId) {
        if (modId.toLowerCase().startsWith("ftb")) {
            String rest = modId.substring(3); // Get the part after "FTB"
            return "FTB " + rest.substring(0, 1).toUpperCase() + rest.substring(1).toLowerCase();
        } else {
            return modId.substring(0, 1).toUpperCase() + modId.substring(1).toLowerCase();
        }
    }

    private void downloadMod(String urlString, String fileName) throws IOException {
        URL url = URI.create(urlString).toURL();
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");

        Path modsFolder = Paths.get("mods");
        if (!Files.exists(modsFolder)) {
            Files.createDirectories(modsFolder);
        }

        Path filePath = modsFolder.resolve(fileName);
        try (InputStream in = connection.getInputStream()) {
            Files.copy(in, filePath);
            LOGGER.info("Downloaded mod to: {}", filePath);
        }
    }
}
