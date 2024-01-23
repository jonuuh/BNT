package net.jonuuh.bnt.config;

import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.client.config.GuiConfigEntries;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.io.File;

public class ConfigHandler
{
    private static Configuration cfg;
    private static boolean doRenderSelf;
    private static boolean doRenderRect;
    private static boolean doRectHPColor;
    private static float rectAlpha;
    private static float rectBordWidth;
    private static float rectBordOffset;
    private static float rectHPColorOffset;
    private static int rectPadding;
    private static int sepWidth;
    private static int yOffset;
    private static int maxRange;

    public ConfigHandler(File cfgFile)
    {
        // this might be a problem later? (try: if file == null)
        cfg = new Configuration(cfgFile);
        load();
    }

    public static Configuration getCfg()
    {
        return cfg;
    }

    public static boolean doRenderSelf()
    {
        return doRenderSelf;
    }

    public static boolean doRenderRect()
    {
        return doRenderRect;
    }

    public static boolean doRectHPColor()
    {
        return doRectHPColor;
    }

    public static float getRectAlpha()
    {
        return rectAlpha;
    }

    public static float getRectBordWidth()
    {
        return rectBordWidth;
    }

    public static float getRectBordOffset()
    {
        return rectBordOffset;
    }

    public static float getRectHPColorOffset()
    {
        return rectHPColorOffset;
    }

    public static int getRectPadding()
    {
        return rectPadding;
    }

    public static int getSepWidth()
    {
        return sepWidth;
    }

    public static int getYOffset()
    {
        return yOffset;
    }

    public static int getMaxRange()
    {
        return maxRange;
    }

    @SubscribeEvent
    public void onConfigChange(ConfigChangedEvent.OnConfigChangedEvent event)
    {
        if (event.modID.equalsIgnoreCase("bnt"))
        {
            cfg.save();
            load();
        }
    }

    private static void load()
    {
        cfg.load();
        doRenderSelf = cfg.getBoolean("A Render self", "all", true, "Whether to render your own nametag");
        doRenderRect = cfg.getBoolean("B Render background", "all", true, "Whether to render the nametag background");
        maxRange = getIntSlider("C Max render range", 0, 0, 100, "Max range to render nametags in (0 = infinite)");

        rectAlpha = (float) getRoundedDoubleSlider("D Background opacity", 0.3D, 0.0D, 1.0D, "Nametag background base opacity");
        rectBordWidth = (float) getRoundedDoubleSlider("E Background border width", 1.0D, 0.0D, 5.0D, "Nametag background border width");
        rectBordOffset = (float) getRoundedDoubleSlider("F Background border corner offset", 1.0D, 0.0D, 5.0D, "Nametag background border corner offset");
        doRectHPColor = cfg.getBoolean("G Color background with health", "all", false, "Whether to color the nametag background based on health");
        rectHPColorOffset = (float) getRoundedDoubleSlider("H Background health color opacity", 0.75D, 0.0D, 1.0D, "How much the health color should affect the nametag background");

        rectPadding = getIntSlider("I Background padding", 2, 0, 10, "Nametag background padding amount");
        sepWidth = getIntSlider("J Separator width", 4, 0, 25, "Width between elements in a nametag");
        yOffset = getIntSlider("K Nametag y-axis offset", 0, -30, 30, "Nametag y axis offset relative to player");
    }

    private static int getIntSlider(String key, int defaultV, int minV, int maxV, String com)
    {
        return cfg.get("all", key, defaultV, com, minV, maxV).setConfigEntryClass(GuiConfigEntries.NumberSliderEntry.class).getInt();
    }

    private static double getRoundedDoubleSlider(String key, double defaultV, double minV, double maxV, String com)
    {
        return sloppyRound(cfg.get("all", key, defaultV, com, minV, maxV).setConfigEntryClass(GuiConfigEntries.NumberSliderEntry.class).getDouble(), 4);
    }

    // Often gives slightly incorrect values, doesn't matter for its usage {https://www.baeldung.com/java-round-decimal-number}
    private static double sloppyRound(double value, int places)
    {
        return Math.round(value * Math.pow(10, places)) / Math.pow(10, places);
    }
}
