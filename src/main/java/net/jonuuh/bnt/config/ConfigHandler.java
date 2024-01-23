package net.jonuuh.bnt.config;

import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.client.config.GuiConfigEntries;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.io.File;

public class ConfigHandler
{
    private static Configuration cfg;
    private static boolean renderSelf;
    private static boolean renderRect;
    private static float rectBaseAlpha;
    private static float rectBordAlpha;
    private static float rectBordWidth;
    private static float rectBordOffset;
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
        return renderSelf;
    }

    public static boolean doRenderRect()
    {
        return renderRect;
    }

    public static float getRectBaseAlpha()
    {
        return rectBaseAlpha;
    }

    public static float getRectBordAlpha()
    {
        return rectBordAlpha;
    }

    public static float getRectBordWidth()
    {
        return rectBordWidth;
    }

    public static float getRectBordOffset()
    {
        return rectBordOffset;
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
        renderSelf = cfg.getBoolean("A Render Self", "all", true, "Whether to render your own nametag");
        renderRect = cfg.getBoolean("B Render Background", "all", true, "Whether to render the nametag background");
        maxRange = getIntSlider("C Max Render Range", 0, 0, 100, "Max range to render nametags in (0 = infinite)");

        rectBaseAlpha = (float) getRoundedDoubleSlider("D Background Base Opacity", 0.4D, 0.0D, 1.0D, "Nametag background base opacity");
        rectBordAlpha = (float) getRoundedDoubleSlider("E Background Border Opacity", 0.4D, 0.0D, 1.0D, "Nametag background border opacity");
        rectBordWidth = (float) getRoundedDoubleSlider("F Background Border Width", 1.0D, 0.0D, 5.0D, "Nametag background border width");
        rectBordOffset = (float) getRoundedDoubleSlider("G Background Border Corner Offset", 1.0D, 0.0D, 5.0D, "Nametag background border corner offset");

        rectPadding = getIntSlider("H Background Padding", 2, 0, 10, "Nametag background padding amount");
        sepWidth = getIntSlider("I Separator Width", 4, 0, 25, "Width between elements in a nametag");
        yOffset = getIntSlider("J Nametag Y-Axis Offset", 0, -30, 30, "Nametag y axis offset relative to player");
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
