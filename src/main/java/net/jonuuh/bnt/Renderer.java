package net.jonuuh.bnt;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import org.lwjgl.opengl.GL11;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

public class Renderer
{
    private final Minecraft mc;
    private final KeyBinding debugKey;
    private boolean doRendering = false;

    public Renderer(Minecraft mc, KeyBinding debugKey)
    {
        this.mc = mc;
        this.debugKey = debugKey;
    }

    @SubscribeEvent
    public void onKeyInput(InputEvent.KeyInputEvent event)
    {
        if (debugKey.isPressed())
        {
            doRendering = !doRendering;
        }
    }

    @SubscribeEvent
    public void render(RenderLivingEvent.Specials.Pre<EntityLivingBase> event)
    {
        if (!doRendering || !(event.entity instanceof EntityPlayer))
        {
            return;
        }
        EntityPlayer player = (EntityPlayer) event.entity;

        // Return if entity not in NetHandler (tab list)
        if (!getOnlinePlayerNames(mc).contains(player.getName()))
        {
            return;
        }

        event.setCanceled(true);

        Color rectColor = new Color(0.0F, 0.0F, 0.0F, 0.40F);
        String sep = " ";
        String str = sep + player.getDisplayName().getFormattedText() + sep + getFormattedHealthText(player);
        int sepWidth = getFontRenderer().getStringWidth(sep);
        int x = (getFontRenderer().getStringWidth(str) / 2) + sepWidth;
        int y = 0;

        GlStateManager.pushMatrix();
        GlStateManager.translate((float) event.x, (float) (event.y + player.height + 0.5F), (float) event.z);
        GL11.glNormal3f(0.0F, 1.0F, 0.0F);
        GlStateManager.rotate(-getRenderManager().playerViewY, 0.0F, 1.0F, 0.0F);
        GlStateManager.rotate(getRenderManager().playerViewX, 1.0F, 0.0F, 0.0F);
        GlStateManager.scale(-0.02666667F, -0.02666667F, 0.02666667F); // 2 / 75

        if (player.isSneaking())
        {
            GlStateManager.translate(0.0F, 9.374999F, 0.0F);
            rectColor.r = 0.75F;
        }

        GlStateManager.disableLighting();
        GlStateManager.depthMask(false);
        GlStateManager.disableDepth();
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);

        // Draw nametag background
        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer worldrenderer = tessellator.getWorldRenderer();
        GlStateManager.disableTexture2D();
        worldrenderer.begin(7, DefaultVertexFormats.POSITION_COLOR);
        worldrenderer.pos(-x - 1, y - 1, 0.0D).color(rectColor.r, rectColor.g, rectColor.b, rectColor.a).endVertex();
        worldrenderer.pos(-x - 1, y + 8, 0.0D).color(rectColor.r, rectColor.g, rectColor.b, rectColor.a).endVertex();
        worldrenderer.pos(x + 1, y + 8, 0.0D).color(rectColor.r, rectColor.g, rectColor.b, rectColor.a).endVertex();
        worldrenderer.pos(x + 1, y - 1, 0.0D).color(rectColor.r, rectColor.g, rectColor.b, rectColor.a).endVertex();
        tessellator.draw();
        GlStateManager.enableTexture2D();

        // Draw nametag head and text
        drawPlayerHead(player, -x, y, 7);
        getFontRenderer().drawString(str, -x + (sepWidth * 2), y, -1);
        GlStateManager.enableDepth();
        GlStateManager.depthMask(true);
        drawPlayerHead(player, -x, y, 7);
        getFontRenderer().drawString(str, -x + (sepWidth * 2), y, -1);

        GlStateManager.enableLighting();
        GlStateManager.disableBlend();
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        GlStateManager.popMatrix();
    }

    private FontRenderer getFontRenderer()
    {
        return mc.fontRendererObj;
    }

    private RenderManager getRenderManager()
    {
        return mc.getRenderManager();
    }

    private Set<String> getOnlinePlayerNames(Minecraft mc)
    {
        Set<String> onlinePlayers = new HashSet<>();
        if (mc.getNetHandler() != null && mc.getNetHandler().getPlayerInfoMap() != null)
        {
            onlinePlayers = mc.getNetHandler().getPlayerInfoMap().stream()
                    .map(networkPlayerInfo -> networkPlayerInfo.getGameProfile().getName())
                    .collect(Collectors.toSet());
        }
        return onlinePlayers;
    }

    private String getFormattedHealthText(EntityPlayer player)
    {
        float health = player.getHealth();
        float healthPercent = (health / player.getMaxHealth()) * 100;
        EnumChatFormatting color = healthPercent >= 75 ? EnumChatFormatting.DARK_GREEN
                : healthPercent >= 50 ? EnumChatFormatting.GREEN
                : healthPercent >= 25 ? EnumChatFormatting.RED
                : EnumChatFormatting.DARK_RED;
        return new ChatComponentText(color + String.valueOf(roundToHalf(health))).getFormattedText();
    }

    private float roundToHalf(float num)
    {
        return (float) (Math.round(num * 2) / 2.0);
    }

    private void drawPlayerHead(EntityPlayer player, int x, int y, int size)
    {
        mc.getTextureManager().bindTexture(mc.getNetHandler().getPlayerInfo(player.getName()).getLocationSkin());
        Gui.drawScaledCustomSizeModalRect(x, y, 8, 8, 8, 8, size, size, 64.0F, 64.0F);
    }

    private static class Color
    {
        float r;
        float g;
        float b;
        float a;

        private Color(float r, float g, float b, float a)
        {
            this.r = r;
            this.g = g;
            this.b = b;
            this.a = a;
        }
    }
}
