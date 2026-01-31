package com.fix_tfmgcontroller.gui;

import com.fix_tfmgcontroller.FixTFMGController;
import com.mojang.blaze3d.systems.RenderSystem;
import net.createmod.catnip.gui.UIRenderHelper;
import net.createmod.catnip.gui.element.ScreenElement;
import net.createmod.catnip.theme.Color;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public enum FTCGuiTextures implements ScreenElement {

    ENGINE_CONTROLLER_SETTING("engine_controller_setting", 173, 205),
    ;

    public static final int FONT_COLOR = 0x575F7A;

    public final ResourceLocation location;
    public final int width;
    public final int height;
    public final int startX;
    public final int startY;

    FTCGuiTextures(String location, int width, int height) {
        this(location, 0, 0, width, height);
    }

    FTCGuiTextures(int startX, int startY) {
        this("icons", startX * 16, startY * 16, 16, 16);
    }

    FTCGuiTextures(String location, int startX, int startY, int width, int height) {
        this(FixTFMGController.MODID, location, startX, startY, width, height);
    }

    FTCGuiTextures(String namespace, String location, int startX, int startY, int width, int height) {
        this.location = new ResourceLocation(namespace, "textures/gui/" + location + ".png");
        this.width = width;
        this.height = height;
        this.startX = startX;
        this.startY = startY;
    }

    @OnlyIn(Dist.CLIENT)
    public void bind() {
        RenderSystem.setShaderTexture(0, location);
    }

    @OnlyIn(Dist.CLIENT)
    public void render(GuiGraphics graphics, int x, int y) {
        graphics.blit(location, x, y, startX, startY, width, height);
    }

    @OnlyIn(Dist.CLIENT)
    public void render(GuiGraphics graphics, int x, int y, Color c) {
        bind();
        UIRenderHelper.drawColoredTexture(graphics, c, x, y, startX, startY, width, height);
    }

}

