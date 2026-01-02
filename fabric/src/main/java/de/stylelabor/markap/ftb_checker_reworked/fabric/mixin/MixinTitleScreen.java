package de.stylelabor.markap.ftb_checker_reworked.fabric.mixin;

import de.stylelabor.markap.ftb_checker_reworked.ModCheckerReworked;
import de.stylelabor.markap.ftb_checker_reworked.MissingModsScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.TitleScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TitleScreen.class)
public class MixinTitleScreen {

    @Inject(method = "init", at = @At("HEAD"))
    private void init(CallbackInfo ci) {
        if (ModCheckerReworked.getInstance().hasMissingMods()) {
            java.util.List<String> missingMods = ModCheckerReworked.getInstance().consumeMissingMods();
            if (missingMods != null && !missingMods.isEmpty()) {
                Minecraft.getInstance().setScreen(new MissingModsScreen(missingMods));
            }
        }
    }
}
