package de.stylelabor.markap.ftb_checker_reworked.neoforge;

import de.stylelabor.markap.ftb_checker_reworked.ModCheckerReworked;
import de.stylelabor.markap.ftb_checker_reworked.platform.ModCheckerPlatform;
import net.neoforged.fml.ModList;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import de.stylelabor.markap.ftb_checker_reworked.MissingModsScreen;
import net.neoforged.neoforge.client.event.ScreenEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.bus.api.SubscribeEvent;
import net.minecraft.client.gui.screens.TitleScreen;
import java.util.List;

@Mod(ModCheckerReworked.MOD_ID)
public class ModCheckerReworkedNeoForge implements ModCheckerPlatform.IPlatformHelper {

    public ModCheckerReworkedNeoForge(IEventBus modEventBus) {
        // Register platform helper
        ModCheckerPlatform.init(this);

        // Register the setup method for modloading
        modEventBus.addListener(this::onClientSetup);

        NeoForge.EVENT_BUS.register(this);

    }

    private void onClientSetup(final FMLClientSetupEvent event) {
        ModCheckerReworked.getInstance().init();
    }

    @Override
    public boolean isModLoaded(String modId) {
        return ModList.get().isLoaded(modId);
    }

    @Override
    public String getPlatformName() {
        return "NeoForge";
    }

    @SubscribeEvent
    public void onScreenInit(ScreenEvent.Init.Post event) {
        if (event.getScreen() instanceof TitleScreen) {
            if (ModCheckerReworked.getInstance().hasMissingMods()) {
                List<String> missing = ModCheckerReworked.getInstance().consumeMissingMods();
                if (missing != null && !missing.isEmpty()) {
                    event.getScreen().getMinecraft().setScreen(new MissingModsScreen(missing));
                }
            }
        }
    }
}
