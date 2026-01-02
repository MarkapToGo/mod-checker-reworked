package de.stylelabor.markap.ftb_checker_reworked.platform;

import java.util.ServiceLoader;

/**
 * Platform-specific services abstraction.
 * Uses ServiceLoader to find the platform-specific implementation.
 */
public class ModCheckerPlatform {

    private static IPlatformHelper IMPL;

    public static void init(IPlatformHelper impl) {
        IMPL = impl;
    }

    public static boolean isModLoaded(String modId) {
        if (IMPL == null) {
            throw new IllegalStateException("Platform helper not initialized!");
        }
        return IMPL.isModLoaded(modId);
    }

    public static String getPlatformName() {
        if (IMPL == null) {
            throw new IllegalStateException("Platform helper not initialized!");
        }
        return IMPL.getPlatformName();
    }

    public interface IPlatformHelper {
        boolean isModLoaded(String modId);

        String getPlatformName();
    }
}
