//package net.jonuuh.bnt;
//
//import net.minecraft.client.gui.GuiButton;
//import net.minecraft.client.gui.GuiScreen;
//import net.minecraftforge.fml.client.config.GuiSlider;
//
//import java.io.IOException;
//
//public class ConfigGui extends GuiScreen implements GuiSlider.ISlider
//{
//    private final int buttonID0 = 0;
//    private double sliderVal = 33;
//
//    /**
//     * Adds the buttons (and other controls) to the screen in question. Called when the GUI is displayed and when the
//     * window resizes, the buttonList is cleared beforehand.
//     */
//    @Override
//    public void initGui()
//    {
//        buttonList.clear(); // redundancy
//        buttonList.add(new GuiButton(buttonID0, 0, 0, 200, 20, "hello world"));
//        buttonList.add(new GuiSlider(5, 250, 250, 150, 20, "Test: ", "%", 0d, 100d, sliderVal, false, true, this));
//
////        buttonList.add(new GuiSlider());
//        super.initGui();
//    }
//
//    /**
//     * Draws the screen and all the components in it. Args : mouseX, mouseY, renderPartialTicks
//     *
//     * @param mouseX
//     * @param mouseY
//     * @param partialTicks
//     */
//    @Override
//    public void drawScreen(int mouseX, int mouseY, float partialTicks)
//    {
//        drawDefaultBackground();
//        super.drawScreen(mouseX, mouseY, partialTicks);
//    }
//
//    /**
//     * Called by the controls from the buttonList when activated. (Mouse pressed for buttons)
//     *
//     * @param button
//     */
//    @Override
//    protected void actionPerformed(GuiButton button) throws IOException
//    {
//        switch (button.id)
//        {
//            case buttonID0:
//                break;
//        }
//        super.actionPerformed(button);
//    }
//
//    /**
//     * Returns true if this GUI should pause the game when it is displayed in single-player
//     */
//    @Override
//    public boolean doesGuiPauseGame()
//    {
//        return false;
//    }
//
//    @Override
//    public void onChangeSliderValue(GuiSlider slider)
//    {
//        switch(slider.id)
//        {
//            case 5:
//                sliderVal = slider.getValue();
//                break;
//        }
//    }
//}
