package net.jonuuh.bnt;

import net.jonuuh.bnt.config.ConfigHandler;
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
    private final KeyBinding toggleKey;
    private boolean doRendering = false;

    public Renderer(Minecraft mc, KeyBinding toggleKey)
    {
        this.mc = mc;
        this.toggleKey = toggleKey;
    }

    @SubscribeEvent
    public void onKeyInput(InputEvent.KeyInputEvent event)
    {
        if (toggleKey.isPressed())
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

        if (!getOnlinePlayerNames(mc).contains(player.getName()) || player.getName().isEmpty()) // Player not in NetHandler (tab list) or has no name
        {
            return;
        }

        if (player == mc.thePlayer && !ConfigHandler.doRenderSelf())
        {
            return;
        }

        if (ConfigHandler.getMaxRange() != 0 && mc.thePlayer.getDistanceToEntity(player) > ConfigHandler.getMaxRange())
        {
            return;
        }

        event.setCanceled(true); // Remove vanilla nametag

        String name = player.getDisplayName().getFormattedText();
        String health = getFormattedHealthText(player);
        Color rectC = new Color(0.0F, 0.0F, 0.0F, ConfigHandler.getRectAlpha());
        int headSize = 7;
        int sepWidth = ConfigHandler.getSepWidth();
        int x = (getFontRenderer().getStringWidth(name + health) / 2) + (headSize / 2) + sepWidth;
        int y = ConfigHandler.getYOffset();

        GlStateManager.pushMatrix();
        GlStateManager.translate((float) event.x, (float) (event.y + player.height + 0.5F), (float) event.z);
        GL11.glNormal3f(0.0F, 1.0F, 0.0F);
        GlStateManager.rotate(-getRenderManager().playerViewY, 0.0F, 1.0F, 0.0F);
        GlStateManager.rotate(getRenderManager().playerViewX, 1.0F, 0.0F, 0.0F);
        GlStateManager.scale(-0.02666667F, -0.02666667F, 0.02666667F); // 2 / 75

        if (player.isSneaking())
        {
            GlStateManager.translate(0.0F, 9.374999F, 0.0F);
            rectC = new Color(0.5F, 0.5F, 0.5F, ConfigHandler.getRectAlpha());
        }

        GlStateManager.disableLighting();
        GlStateManager.depthMask(false);
        GlStateManager.disableDepth();
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);

        // Draw nametag background
        if (ConfigHandler.doRenderRect())
        {
            Tessellator tessellator = Tessellator.getInstance();
            WorldRenderer worldrenderer = tessellator.getWorldRenderer();
            int rectPad = ConfigHandler.getRectPadding();

            if (ConfigHandler.doRectHPColor() && !player.isSneaking())
            {
                float one = 1.0F * ConfigHandler.getRectHPColorOffset();
                float third = (1 / 3.0F) * ConfigHandler.getRectHPColorOffset();
                float sixth = (2 / 3.0F) * ConfigHandler.getRectHPColorOffset();
                float alpha = ConfigHandler.getRectAlpha();
                float healthPercent = (player.getHealth() / player.getMaxHealth()) * 100;

                rectC = healthPercent >= 75 ? new Color(0.0F, sixth, 0.0F, alpha) // dark green
                        : healthPercent >= 50 ? new Color(third, one, third, alpha) // green
                        : healthPercent >= 25 ? new Color(one, third, third, alpha) // red
                        : new Color(sixth, 0.0F, 0.0F, alpha); // dark red
            }

            GlStateManager.disableTexture2D();
            drawRect(tessellator, worldrenderer, x, y, rectC, rectPad); // base
            drawRectBorder(tessellator, worldrenderer, x, y, rectC, rectPad); // border
            GlStateManager.enableTexture2D();
        }

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

//    drawRoundedRect(-x - rectPad, y - rectPad, 7 + (rectPad * 2), (x * 2) + (rectPad * 2));
//    private void drawRoundedRect(int x, int y, int height, int width)
//    {
//        mc.getTextureManager().bindTexture(new ResourceLocation("bnt", "background.png"));
//        Gui.drawScaledCustomSizeModalRect(x, y, 0, 0, 256, 256, width, height, 256.0F, 256.0F);
//    }

    private void drawRect(Tessellator tessellator, WorldRenderer worldrenderer, int x, int y, Color color, int pad)
    {
        worldrenderer.begin(7, DefaultVertexFormats.POSITION_COLOR);
        createVertex(worldrenderer, -x - pad, y - pad, color);
        createVertex(worldrenderer, -x - pad, y + 7 + pad, color);
        createVertex(worldrenderer, x + pad, y + 7 + pad, color);
        createVertex(worldrenderer, x + pad, y - pad, color);
        tessellator.draw();
    }

    private void drawRectBorder(Tessellator tessellator, WorldRenderer worldrenderer, int x, int y, Color color, int pad)
    {
        float bW = ConfigHandler.getRectBordWidth();
        float bO = ConfigHandler.getRectBordOffset(); // bW & bO should be equal for 45deg angle
        worldrenderer.begin(7, DefaultVertexFormats.POSITION_COLOR);
        createVertex(worldrenderer, -x - pad - bW, y - pad + bO, color);
        createVertex(worldrenderer, -x - pad - bW, y + 7 + pad - bO, color);
        createVertex(worldrenderer, -x - pad, y + 7 + pad, color);
        createVertex(worldrenderer, -x - pad, y - pad, color);
        tessellator.draw();

        worldrenderer.begin(7, DefaultVertexFormats.POSITION_COLOR);
        createVertex(worldrenderer, -x - pad, y + 7 + pad, color);
        createVertex(worldrenderer, -x - pad + bO, y + 7 + pad + bW, color);
        createVertex(worldrenderer, x + pad - bO, y + 7 + pad + bW, color);
        createVertex(worldrenderer, x + pad, y + 7 + pad, color);
        tessellator.draw();

        worldrenderer.begin(7, DefaultVertexFormats.POSITION_COLOR);
        createVertex(worldrenderer, x + pad, y - pad, color);
        createVertex(worldrenderer, x + pad, y + 7 + pad, color);
        createVertex(worldrenderer, x + pad + bW, y + 7 + pad - bO, color);
        createVertex(worldrenderer, x + pad + bW, y - pad + bO, color);
        tessellator.draw();

        worldrenderer.begin(7, DefaultVertexFormats.POSITION_COLOR);
        createVertex(worldrenderer, -x - pad + bO, y - pad - bW, color);
        createVertex(worldrenderer, -x - pad, y - pad, color);
        createVertex(worldrenderer, x + pad, y - pad, color);
        createVertex(worldrenderer, x + pad - bO, y - pad - bW, color);
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

//    private final Map<Character, Color> formattingCodeToColor = initMap();
//
//    private Map<Character, Color> initMap()
//    {
//        float alpha = ConfigHandler.getRectBordAlpha();
//        float zero = 0.0F;
//        float one = 1.0F;
//        float third = 0.333333F;
//        float sixth = 0.666666F;
//
//        Map<Character, Color> map = new HashMap<>();
//        map.put('0', new Color(zero, zero, zero, alpha));
//        map.put('1', new Color(zero, zero, sixth, alpha));
//        map.put('2', new Color(zero, sixth, zero, alpha));
//        map.put('3', new Color(zero, sixth, sixth, alpha));
//        map.put('4', new Color(sixth, zero, zero, alpha));
//        map.put('5', new Color(sixth, zero, sixth, alpha));
//        map.put('6', new Color(one, sixth, zero, alpha));
//        map.put('7', new Color(sixth, sixth, sixth, alpha));
//        map.put('8', new Color(third, third, third, alpha));
//        map.put('9', new Color(third, third, one, alpha));
//        map.put('a', new Color(third, one, third, alpha));
//        map.put('b', new Color(third, one, one, alpha));
//        map.put('c', new Color(one, third, third, alpha));
//        map.put('d', new Color(one, third, one, alpha));
//        map.put('e', new Color(one, one, third, alpha));
//        map.put('f', new Color(one, one, one, alpha));
//        return map;
//    }

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
