package net.jonuuh.bnt;

import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import org.lwjgl.input.Keyboard;

@Mod(modid = BetterNametags.MODID, version = BetterNametags.VERSION)
public class BetterNametags
{
    public static final String MODID = "bnt";
    public static final String VERSION = "1.0.0";

    private final Minecraft mc;
    private final KeyBinding debugKey;

    public BetterNametags()
    {
        this.mc = Minecraft.getMinecraft();
        this.debugKey = new KeyBinding("BNTDebug", Keyboard.KEY_L, "BNT");
    }

    @EventHandler
    public void init(FMLInitializationEvent event)
    {
        ClientRegistry.registerKeyBinding(debugKey);
        MinecraftForge.EVENT_BUS.register(new Renderer(mc, debugKey));
    }
}
