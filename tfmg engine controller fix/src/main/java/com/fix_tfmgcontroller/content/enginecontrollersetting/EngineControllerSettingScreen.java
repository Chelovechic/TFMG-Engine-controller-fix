package com.fix_tfmgcontroller.content.enginecontrollersetting;

import com.fix_tfmgcontroller.gui.FTCGuiTextures;
import com.simibubi.create.foundation.gui.AllIcons;
import com.simibubi.create.foundation.gui.widget.IconButton;
import com.simibubi.create.foundation.gui.widget.ScrollInput;
import com.simibubi.create.foundation.utility.CreateLang;
import net.createmod.catnip.gui.AbstractSimiScreen;
import net.createmod.catnip.gui.element.GuiGameElement;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

public class EngineControllerSettingScreen extends AbstractSimiScreen {

    private ItemStack itemStack;
    private final FTCGuiTextures background = FTCGuiTextures.ENGINE_CONTROLLER_SETTING;
    private IconButton confirmButton;

    private ScrollInput gearRInput;
    private ScrollInput gear1Input;
    private ScrollInput gear2Input;
    private ScrollInput gear3Input;
    private ScrollInput gear4Input;
    private ScrollInput gear5Input;
    private ScrollInput gear6Input;

    // Значения по умолчанию (в процентах, 100 = 1.0x, 400 = 4.0x)
    private int gearR = -30;  // -0.3 (reverse)
    private int gear1 = 20;   // 0.2
    private int gear2 = 40;   // 0.4
    private int gear3 = 60;   // 0.6
    private int gear4 = 80;   // 0.8
    private int gear5 = 100;  // 1.0
    private int gear6 = 120;  // 1.2

    // Флаг отключения всех функций миксинов
    private boolean functionsDisabled = false;
    private IconButton functionsToggleButton;

    public EngineControllerSettingScreen(ItemStack stack) {
        super(CreateLang.translateDirect("gui.engine_controller_setting.title"));
        this.itemStack = stack;
        
        // Загружаем сохраненные значения из NBT
        if (stack.hasTag()) {
            CompoundTag nbt = stack.getTag();
            if (nbt.contains("GearRatios")) {
                CompoundTag ratios = nbt.getCompound("GearRatios");
                gearR = ratios.getInt("R");
                gear1 = ratios.getInt("1");
                gear2 = ratios.getInt("2");
                gear3 = ratios.getInt("3");
                gear4 = ratios.getInt("4");
                gear5 = ratios.getInt("5");
                gear6 = ratios.getInt("6");
            }
            if (nbt.contains("FunctionsDisabled")) {
                functionsDisabled = nbt.getBoolean("FunctionsDisabled");
            }
        }
    }

    public static void open(ItemStack stack) {
        net.minecraft.client.Minecraft.getInstance().setScreen(new EngineControllerSettingScreen(stack));
    }

    @Override
    protected void init() {
        setWindowSize(background.width, background.height);
        setWindowOffset(-20, 0);
        super.init();

        int x = guiLeft;
        int y = guiTop;
        int rowHeight = 22;
        int startY = y + 20;

        // Передача R (обратная)
        gearRInput = new ScrollInput(x + 58, startY, 58, 18)
                .calling(state -> gearR = state)
                .withRange(-400, -10)  // от -4.0 до -0.1
                .setState(gearR)
                .titled(Component.literal("R: "));
        addRenderableWidget(gearRInput);

        // Передача 1
        gear1Input = new ScrollInput(x + 58, startY + rowHeight, 58, 18)
                .calling(state -> gear1 = state)
                .withRange(10, 400)  // от 0.1 до 4.0
                .setState(gear1)
                .titled(Component.literal("1: "));
        addRenderableWidget(gear1Input);

        // Передача 2
        gear2Input = new ScrollInput(x + 58, startY + rowHeight * 2, 58, 18)
                .calling(state -> gear2 = state)
                .withRange(10, 400)
                .setState(gear2)
                .titled(Component.literal("2: "));
        addRenderableWidget(gear2Input);

        // Передача 3
        gear3Input = new ScrollInput(x + 58, startY + rowHeight * 3, 58, 18)
                .calling(state -> gear3 = state)
                .withRange(10, 400)
                .setState(gear3)
                .titled(Component.literal("3: "));
        addRenderableWidget(gear3Input);

        // Передача 4
        gear4Input = new ScrollInput(x + 58, startY + rowHeight * 4, 58, 18)
                .calling(state -> gear4 = state)
                .withRange(10, 400)
                .setState(gear4)
                .titled(Component.literal("4: "));
        addRenderableWidget(gear4Input);

        // Передача 5
        gear5Input = new ScrollInput(x + 58, startY + rowHeight * 5, 58, 18)
                .calling(state -> gear5 = state)
                .withRange(10, 400)
                .setState(gear5)
                .titled(Component.literal("5: "));
        addRenderableWidget(gear5Input);

        // Передача 6
        gear6Input = new ScrollInput(x + 58, startY + rowHeight * 6, 58, 18)
                .calling(state -> gear6 = state)
                .withRange(10, 400)
                .setState(gear6)
                .titled(Component.literal("6: "));
        addRenderableWidget(gear6Input);

        // Кнопка отключения всех функций миксинов
        functionsToggleButton = new IconButton(x + 10, y + background.height - 30, 
                functionsDisabled ? AllIcons.I_DISABLE : AllIcons.I_ACTIVE);
        functionsToggleButton.withCallback(() -> {
            functionsDisabled = !functionsDisabled;
            updateFunctionsButtonIcon();
        });
        addRenderableWidget(functionsToggleButton);

        confirmButton = new IconButton(x + background.width - 33, y + background.height - 24, AllIcons.I_CONFIRM);
        confirmButton.withCallback(this::onClose);
        addRenderableWidget(confirmButton);
    }

    private void updateFunctionsButtonIcon() {
        functionsToggleButton.setIcon(functionsDisabled ? AllIcons.I_DISABLE : AllIcons.I_ACTIVE);
    }

    @Override
    protected void renderWindow(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        int x = guiLeft;
        int y = guiTop;
        int rowHeight = 22;

        background.render(graphics, x, y);

        // Рендерим иконку предмета
        GuiGameElement.of(itemStack)
                .at(x + background.width + 6, y + background.height - 56, -200)
                .render(graphics);

        // Метки для передач
        int startY = y + 24;
        graphics.drawString(font, "R:", x + 30, startY, 0x5B5B5B, false);
        graphics.drawString(font, "1:", x + 30, startY + rowHeight, 0x5B5B5B, false);
        graphics.drawString(font, "2:", x + 30, startY + rowHeight * 2, 0x5B5B5B, false);
        graphics.drawString(font, "3:", x + 30, startY + rowHeight * 3, 0x5B5B5B, false);
        graphics.drawString(font, "4:", x + 30, startY + rowHeight * 4, 0x5B5B5B, false);
        graphics.drawString(font, "5:", x + 30, startY + rowHeight * 5, 0x5B5B5B, false);
        graphics.drawString(font, "6:", x + 30, startY + rowHeight * 6, 0x5B5B5B, false);

        // Показываем текущие значения (в формате x.xx)
        graphics.drawString(font, String.format("%.2f", gearRInput.getState() / 100.0f), x + 120, startY, 0xFFFFFF, false);
        graphics.drawString(font, String.format("%.2f", gear1Input.getState() / 100.0f), x + 120, startY + rowHeight, 0xFFFFFF, false);
        graphics.drawString(font, String.format("%.2f", gear2Input.getState() / 100.0f), x + 120, startY + rowHeight * 2, 0xFFFFFF, false);
        graphics.drawString(font, String.format("%.2f", gear3Input.getState() / 100.0f), x + 120, startY + rowHeight * 3, 0xFFFFFF, false);
        graphics.drawString(font, String.format("%.2f", gear4Input.getState() / 100.0f), x + 120, startY + rowHeight * 4, 0xFFFFFF, false);
        graphics.drawString(font, String.format("%.2f", gear5Input.getState() / 100.0f), x + 120, startY + rowHeight * 5, 0xFFFFFF, false);
        graphics.drawString(font, String.format("%.2f", gear6Input.getState() / 100.0f), x + 120, startY + rowHeight * 6, 0xFFFFFF, false);

        // Метка для кнопки отключения функций
        int buttonY = y + background.height - 30;
        graphics.drawString(font, functionsDisabled ? "Функции: OFF" : "Функции: ON", x + 30, buttonY + 3, functionsDisabled ? 0xFF5555 : 0x55FF55, false);
    }

    @Override
    public void removed() {
        CompoundTag ratios = new CompoundTag();
        ratios.putInt("R", gearRInput.getState());
        ratios.putInt("1", gear1Input.getState());
        ratios.putInt("2", gear2Input.getState());
        ratios.putInt("3", gear3Input.getState());
        ratios.putInt("4", gear4Input.getState());
        ratios.putInt("5", gear5Input.getState());
        ratios.putInt("6", gear6Input.getState());
        
        if (minecraft != null && minecraft.player != null) {
            net.minecraft.world.InteractionHand hand = net.minecraft.world.InteractionHand.MAIN_HAND;
            ItemStack handStack = minecraft.player.getItemInHand(hand);
            
            if (handStack.getItem() == itemStack.getItem()) {
                CompoundTag nbt = handStack.getOrCreateTag();
                nbt.put("GearRatios", ratios);
                nbt.putBoolean("FunctionsDisabled", functionsDisabled);
                minecraft.player.setItemInHand(hand, handStack);
                com.fix_tfmgcontroller.network.FTCPackets.getChannel().sendToServer(new UpdateGearRatiosPacket(ratios, functionsDisabled));
            } else {
                hand = net.minecraft.world.InteractionHand.OFF_HAND;
                handStack = minecraft.player.getItemInHand(hand);
                if (handStack.getItem() == itemStack.getItem()) {
                    CompoundTag nbt = handStack.getOrCreateTag();
                    nbt.put("GearRatios", ratios);
                    nbt.putBoolean("FunctionsDisabled", functionsDisabled);
                    minecraft.player.setItemInHand(hand, handStack);
                    
                    com.fix_tfmgcontroller.network.FTCPackets.getChannel().sendToServer(new UpdateGearRatiosPacket(ratios, functionsDisabled));
                }
            }
        }
        
        super.removed();
    }
}

