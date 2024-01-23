package net.jonuuh.bnt;

import net.jonuuh.bnt.config.ConfigHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import org.lwjgl.input.Keyboard;

@Mod(
        modid = "bnt",
        version = "1.2.0",
        acceptedMinecraftVersions = "[1.8.9]",
        guiFactory = "net.jonuuh.bnt.config.ConfigGuiFactory"
)
public class BetterNametags
{
    private final Minecraft mc;
    private final KeyBinding toggleKey;

    public BetterNametags()
    {
        this.mc = Minecraft.getMinecraft();
        this.toggleKey = new KeyBinding("Toggle Nametags", Keyboard.KEY_N, "BNT");
    }

    @EventHandler
    public void FMLPreInit(FMLPreInitializationEvent event)
    {
        MinecraftForge.EVENT_BUS.register(new ConfigHandler(event.getSuggestedConfigurationFile()));
    }

    @EventHandler
    public void FMLInit(FMLInitializationEvent event)
    {
        ClientRegistry.registerKeyBinding(toggleKey);
        MinecraftForge.EVENT_BUS.register(new Renderer(mc, toggleKey));
    }
}
