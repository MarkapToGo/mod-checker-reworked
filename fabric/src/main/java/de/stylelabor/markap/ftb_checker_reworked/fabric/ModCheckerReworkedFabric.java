package de.stylelabor.markap.ftb_checker_reworked.fabric;

import de.stylelabor.markap.ftb_checker_reworked.ModCheckerReworked;
import de.stylelabor.markap.ftb_checker_reworked.platform.ModCheckerPlatform;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.loader.api.FabricLoader;

public class ModCheckerReworkedFabric implements ClientModInitializer, ModCheckerPlatform.IPlatformHelper {
    @Override
    public void onInitializeClient() {
        ModCheckerPlatform.init(this);
        ModCheckerReworked.getInstance().init();
    }

    @Override
    public boolean isModLoaded(String modId) {
        return FabricLoader.getInstance().isModLoaded(modId);
    }

    @Override
    public String getPlatformName() {
        return "Fabric";
    }
}
