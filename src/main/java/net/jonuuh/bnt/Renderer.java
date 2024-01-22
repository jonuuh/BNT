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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
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

        // Return if player not in NetHandler (tab list)
        if (!getOnlinePlayerNames(mc).contains(player.getName()))
        {
            return;
        }

        // Remove vanilla nametag
        event.setCanceled(true);

        String name = player.getDisplayName().getFormattedText();
        String health = getFormattedHealthText(player);
        Color rectBordC = formattingCodeToColor.getOrDefault(name.charAt(name.indexOf(player.getName()) - 1), new Color(1.0F, 1.0F, 1.0F, 1.0F));
        Color rectBaseC = new Color(0.0F, 0.0F, 0.0F, 0.4F);

        int headSize = 7;
        int sepWidth = 4;
        int x = (getFontRenderer().getStringWidth(name + health) / 2) + (headSize / 2) + sepWidth;
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
            rectBaseC.r = 0.75F;
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
        createVertex(worldrenderer, -x - 2, y - 2, rectBaseC);
        createVertex(worldrenderer, -x - 2, y + 9, rectBaseC);
        createVertex(worldrenderer, x + 2, y + 9, rectBaseC);
        createVertex(worldrenderer, x + 2, y - 2, rectBaseC);
        tessellator.draw();

        // Draw nametag border
        drawRectBorder(tessellator, worldrenderer, x, y, rectBordC);
        GlStateManager.enableDepth();
        GlStateManager.depthMask(true);
        drawRectBorder(tessellator, worldrenderer, x, y, rectBordC);
        GlStateManager.disableDepth();
        GlStateManager.depthMask(false);
        GlStateManager.enableTexture2D();

        // Draw nametag head and text
        drawNametagContent(player, x, y, headSize, name, health, sepWidth);
        GlStateManager.enableDepth();
        GlStateManager.depthMask(true);
        drawNametagContent(player, x, y, headSize, name, health, sepWidth);

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

    private void createVertex(WorldRenderer worldrenderer, double x, double y, Color c)
    {
        worldrenderer.pos(x, y, 0.0D).color(c.r, c.g, c.b, c.a).endVertex();
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
        return new ChatComponentText(color + String.valueOf(Math.round(health * 2) / 2.0)).getFormattedText(); // round to nearest 0.5
    }

    private void drawRectBorder(Tessellator tessellator, WorldRenderer worldrenderer, int x, int y, Color color)
    {
//        double bW = 0.5;
        worldrenderer.begin(7, DefaultVertexFormats.POSITION_COLOR);
        createVertex(worldrenderer, -x - 2.5, y - 1.5, color);
        createVertex(worldrenderer, -x - 2.5, y + 8.5, color);
        createVertex(worldrenderer, -x - 2.0, y + 9.0, color);
        createVertex(worldrenderer, -x - 2.0, y - 2.0, color);
        tessellator.draw();

        worldrenderer.begin(7, DefaultVertexFormats.POSITION_COLOR);
        createVertex(worldrenderer, -x - 2.0, y + 9.0, color);
        createVertex(worldrenderer, -x - 1.5, y + 9.5, color);
        createVertex(worldrenderer, x + 1.5, y + 9.5, color);
        createVertex(worldrenderer, x + 2.0, y + 9.0, color);
        tessellator.draw();

        worldrenderer.begin(7, DefaultVertexFormats.POSITION_COLOR);
        createVertex(worldrenderer, x + 2.0, y - 2.0, color);
        createVertex(worldrenderer, x + 2.0, y + 9.0, color);
        createVertex(worldrenderer, x + 2.5, y + 8.5, color);
        createVertex(worldrenderer, x + 2.5, y - 1.5, color);
        tessellator.draw();

        worldrenderer.begin(7, DefaultVertexFormats.POSITION_COLOR);
        createVertex(worldrenderer, -x - 1.5, y - 2.5, color);
        createVertex(worldrenderer, -x - 2.0, y - 2.0, color);
        createVertex(worldrenderer, x + 2.0, y - 2.0, color);
        createVertex(worldrenderer, x + 1.5, y - 2.5, color);
        tessellator.draw();
    }

    private void drawPlayerHead(EntityPlayer player, int x, int y, int size)
    {
        mc.getTextureManager().bindTexture(mc.getNetHandler().getPlayerInfo(player.getName()).getLocationSkin());
        Gui.drawScaledCustomSizeModalRect(x, y, 8, 8, 8, 8, size, size, 64.0F, 64.0F); // base
        Gui.drawScaledCustomSizeModalRect(x, y, 40, 8, 8, 8, size, size, 64.0F, 64.0F); // hat
    }

    private void drawNametagContent(EntityPlayer player, int x, int y, int headSize, String name, String health, int sepWidth)
    {
        drawPlayerHead(player, -x, y, headSize);
        getFontRenderer().drawString(name, -x + headSize + sepWidth, y, -1);
        getFontRenderer().drawString(health, -x + headSize + (sepWidth * 2) + getFontRenderer().getStringWidth(name) - 1, y, -1); // witchcraft
    }

    private final Map<Character, Color> formattingCodeToColor = initMap();

    private Map<Character, Color> initMap()
    {
        float alpha = 1.0F;
        float zero = 0.0F;
        float one = 1.0F;
        float third = 0.333333F;
        float sixth = 0.666666F;

        Map<Character, Color> map = new HashMap<>();
        map.put('0', new Color(zero, zero, zero, alpha));
        map.put('1', new Color(zero, zero, sixth, alpha));
        map.put('2', new Color(zero, sixth, zero, alpha));
        map.put('3', new Color(zero, sixth, sixth, alpha));
        map.put('4', new Color(sixth, zero, zero, alpha));
        map.put('5', new Color(sixth, zero, sixth, alpha));
        map.put('6', new Color(one, sixth, zero, alpha));
        map.put('7', new Color(sixth, sixth, sixth, alpha));
        map.put('8', new Color(third, third, third, alpha));
        map.put('9', new Color(third, third, one, alpha));
        map.put('a', new Color(third, one, third, alpha));
        map.put('b', new Color(third, one, one, alpha));
        map.put('c', new Color(one, third, third, alpha));
        map.put('d', new Color(one, third, one, alpha));
        map.put('e', new Color(one, one, third, alpha));
        map.put('f', new Color(one, one, one, alpha));
        return map;
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
