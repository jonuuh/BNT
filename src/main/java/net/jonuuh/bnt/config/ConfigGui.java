package net.jonuuh.bnt.config;

import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.common.config.ConfigElement;
import net.minecraftforge.fml.client.config.GuiConfig;

public class ConfigGui extends GuiConfig
{
    public ConfigGui(GuiScreen guiScreen)
    {
        super(guiScreen, (new ConfigElement(ConfigHandler.getCfg().getCategory("all"))).getChildElements(), "bnt", false, false, "Config");
    }

//    @Override
//    public void drawScreen(int mouseX, int mouseY, float partialTicks)
//    {
//        System.out.println("TESTING");
//    }

//    @Override
//    public void drawDefaultBackground()
//    {
//        System.out.println("TESTING");
//    }

//    @Override
//    public void drawWorldBackground(int tint)
//    {
//        this.drawGradientRect(0, 0, this.width, this.height, -1072689136, -804253680);
//    }
}
