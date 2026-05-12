package com.github.manametalmod.gtnhnei;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraftforge.common.config.Configuration;

import codechicken.nei.api.API;
import codechicken.nei.api.IConfigureNEI;
import codechicken.nei.recipe.DefaultOverlayHandler;
import cpw.mods.fml.common.Loader;

/**
 * NEI discovers classes named NEI*Config that implement IConfigureNEI.
 */
public final class NEIManaMetalConfig implements IConfigureNEI {

    private static final String[] DEFAULT_PROFILES = new String[] { "mana_crafting_table", "metal_fusion_table" };

    @Override
    public void loadConfig() {
        for (IntegrationProfile profile : IntegrationProfile.loadAll()) {
            if (!profile.enabled) {
                continue;
            }

            Class<? extends GuiContainer> guiClass = loadGuiClass(profile.guiClassName, profile.category);
            if (guiClass == null) {
                continue;
            }

            API.registerGuiOverlay(guiClass, profile.overlayIdent, profile.overlayOffsetX, profile.overlayOffsetY);
            API.registerGuiOverlayHandler(guiClass, createOverlayHandler(profile), profile.overlayIdent);
            ManaMetalNeiSupportLog.info(
                    profile.category + ": registered NEI overlay ident '" + profile.overlayIdent + "' for "
                            + profile.guiClassName);

            if (profile.registerDefaultOverlayAlias) {
                API.registerGuiOverlay(guiClass, null, profile.overlayOffsetX, profile.overlayOffsetY);
                API.registerGuiOverlayHandler(guiClass, createOverlayHandler(profile), null);
                ManaMetalNeiSupportLog.info(
                        profile.category + ": registered NEI default/null overlay alias for " + profile.guiClassName);
            }
        }
    }

    @Override
    public String getName() {
        return ManaMetalNeiSupportMod.NAME;
    }

    @Override
    public String getVersion() {
        return ManaMetalNeiSupportMod.VERSION;
    }

    private static DefaultOverlayHandler createOverlayHandler(IntegrationProfile profile) {
        if (profile.useExplicitSlotMapping) {
            return new ConfigurableCraftingOverlayHandler(
                    profile.overlayOffsetX,
                    profile.overlayOffsetY,
                    profile.recipeGridRelX,
                    profile.recipeGridRelY,
                    profile.gridWidth,
                    profile.inputStackRelXs,
                    profile.inputStackRelYs,
                    profile.inputSlots);
        }

        return new DefaultOverlayHandler(profile.overlayOffsetX, profile.overlayOffsetY);
    }

    @SuppressWarnings("unchecked")
    private static Class<? extends GuiContainer> loadGuiClass(String className, String category) {
        try {
            Class<?> rawClass = Class.forName(className);
            if (!GuiContainer.class.isAssignableFrom(rawClass)) {
                ManaMetalNeiSupportLog.warn(
                        category + ": " + className + " is not a GuiContainer; NEI overlay was not registered.");
                return null;
            }
            return (Class<? extends GuiContainer>) rawClass;
        } catch (ClassNotFoundException e) {
            ManaMetalNeiSupportLog.warn(
                    category + ": GUI class not found: " + className
                            + ". Set the real class name in config/manametalmod_gtnh_nei_support.cfg.");
            return null;
        }
    }

    static final class IntegrationProfile {

        String category;
        boolean enabled;
        String guiClassName;
        String overlayIdent;
        int overlayOffsetX;
        int overlayOffsetY;
        boolean registerDefaultOverlayAlias;
        boolean useExplicitSlotMapping;
        int recipeGridRelX;
        int recipeGridRelY;
        int gridWidth;
        int[] inputStackRelXs;
        int[] inputStackRelYs;
        int[] inputSlots;

        static List<IntegrationProfile> loadAll() {
            List<IntegrationProfile> profiles = new ArrayList<IntegrationProfile>();
            Configuration config = new Configuration(new File(getConfigDir(), ManaMetalNeiSupportMod.MODID + ".cfg"));

            try {
                config.load();
                profiles.add(loadProfile(
                        config,
                        "mana_crafting_table",
                        true,
                        "project.studio.manametalmod.client.GuiManaCraftTable",
                        "CraftTable_Crafting",
                        5,
                        0,
                        true,
                        new int[] { 45, 45, 45, 66, 75, 84, 105, 105, 105 },
                        new int[] { 13, 31, 49, 49, 31, 49, 49, 31, 13 },
                        new int[] { 0, 1, 2, 3, 4, 5, 6, 7, 8 }));
                profiles.add(loadProfile(
                        config,
                        "metal_fusion_table",
                        false,
                        "project.studio.manametalmod.client.GuiMetalFusionTable",
                        "crafting",
                        5,
                        11,
                        false,
                        new int[0],
                        new int[0],
                        new int[] { 1, 2, 3, 4, 5, 6, 7, 8, 9 }));

                String[] extraProfiles = config.get(
                        "general",
                        "extraProfiles",
                        new String[0],
                        "Additional integration profile category names to load. Each category uses the same keys as mana_crafting_table.")
                        .getStringList();

                for (String extraProfile : extraProfiles) {
                    String category = extraProfile.trim();
                    if (category.length() > 0 && !isDefaultProfile(category)) {
                        profiles.add(loadProfile(
                                config,
                                category,
                                false,
                                "project.studio.manametalmod.client.GuiCustomCraftingTable",
                                "crafting",
                                5,
                                11,
                                false,
                                new int[0],
                                new int[0],
                                new int[] { 1, 2, 3, 4, 5, 6, 7, 8, 9 }));
                    }
                }
            } finally {
                if (config.hasChanged()) {
                    config.save();
                }
            }

            return profiles;
        }

        private static IntegrationProfile loadProfile(
                Configuration config,
                String category,
                boolean defaultEnabled,
                String defaultGuiClassName,
                String defaultOverlayIdent,
                int defaultOverlayOffsetX,
                int defaultOverlayOffsetY,
                boolean defaultRegisterDefaultOverlayAlias,
                int[] defaultInputStackRelXs,
                int[] defaultInputStackRelYs,
                int[] defaultInputSlots) {
            IntegrationProfile profile = new IntegrationProfile();
            profile.category = category;
            profile.enabled = config.getBoolean("enabled", category, defaultEnabled, "Enable this NEI integration.");
            profile.guiClassName = config.getString(
                    "guiClassName",
                    category,
                    defaultGuiClassName,
                    "Fully-qualified client GUI class name for this machine. This must be the class that extends GuiContainer, not the block, tile entity, or container class.");
            profile.overlayIdent = config.getString(
                    "overlayIdent",
                    category,
                    defaultOverlayIdent,
                    "NEI recipe overlay identifier. Use 'crafting' for normal 3x3 CraftingManager recipes. Custom recipe categories need their own NEI recipe handler before another identifier will work.");
            profile.overlayOffsetX = config.getInt(
                    "overlayOffsetX",
                    category,
                    defaultOverlayOffsetX,
                    -1000,
                    1000,
                    "X offset from NEI's recipe ingredient coordinates to this GUI's real Slot xDisplayPosition. Vanilla crafting table uses 5. If ghost ingredients render shifted left/right, adjust this.");
            profile.overlayOffsetY = config.getInt(
                    "overlayOffsetY",
                    category,
                    defaultOverlayOffsetY,
                    -1000,
                    1000,
                    "Y offset from NEI's recipe ingredient coordinates to this GUI's real Slot yDisplayPosition. Vanilla crafting table uses 11. If ghost ingredients render shifted up/down, adjust this.");
            profile.registerDefaultOverlayAlias = config.getBoolean(
                    "registerDefaultOverlayAlias",
                    category,
                    defaultRegisterDefaultOverlayAlias,
                    "Also register this integration using NEI's default/null overlay identifier. ManaMetalMod 7.5.2 needs this because its NEIManaCraftTable opens recipes with CraftTable_Crafting but does not override getOverlayIdentifier(), so overlay transfer queries the null identifier.");
            profile.useExplicitSlotMapping = config.getBoolean(
                    "useExplicitSlotMapping",
                    category,
                    true,
                    "Use inputSlots to map recipe ingredients to container slots. Keep this true for custom containers, because it avoids NEI's default crafting-grid mismatch checks.");
            profile.recipeGridRelX = config.getInt(
                    "recipeGridRelX",
                    category,
                    25,
                    -1000,
                    1000,
                    "NEI recipe display X coordinate of the top-left input slot. For NEI's vanilla shaped crafting handler this is 25.");
            profile.recipeGridRelY = config.getInt(
                    "recipeGridRelY",
                    category,
                    6,
                    -1000,
                    1000,
                    "NEI recipe display Y coordinate of the top-left input slot. For NEI's vanilla shaped crafting handler this is 6.");
            profile.gridWidth = config.getInt(
                    "gridWidth",
                    category,
                    3,
                    1,
                    9,
                    "Recipe input grid width used when mapping NEI ingredient positions to inputSlots. Standard crafting is 3. A 5x5 table would use 5.");
            profile.inputStackRelXs = config.get(
                    category,
                    "inputStackRelXs",
                    defaultInputStackRelXs,
                    "Optional exact NEI recipe ingredient X coordinates, in the same order as inputSlots. Use this for non-rectangular layouts such as ManaMetalMod's magic circle. Leave empty to use recipeGridRelX/recipeGridRelY/gridWidth.")
                    .getIntList();
            profile.inputStackRelYs = config.get(
                    category,
                    "inputStackRelYs",
                    defaultInputStackRelYs,
                    "Optional exact NEI recipe ingredient Y coordinates, in the same order as inputSlots. Must have the same length as inputStackRelXs and inputSlots.")
                    .getIntList();
            profile.inputSlots = config.get(
                    category,
                    "inputSlots",
                    defaultInputSlots,
                    "Container slot numbers for recipe inputs, row-major from top-left to bottom-right. Include only real recipe input slots. Do not include output, player inventory, hotbar, mana, catalyst, or decorative/ghost slots unless they are actual recipe inputs.")
                    .getIntList();

            return profile;
        }

        private static boolean isDefaultProfile(String category) {
            for (String defaultProfile : DEFAULT_PROFILES) {
                if (defaultProfile.equals(category)) {
                    return true;
                }
            }
            return false;
        }

        private static File getConfigDir() {
            return Loader.instance().getConfigDir();
        }
    }
}
