package com.nippaku_zanmu.trans_addon.mixin;

import com.nippaku_zanmu.trans_addon.font_fix.FontFix;
import meteordevelopment.meteorclient.renderer.*;
import meteordevelopment.meteorclient.renderer.text.CustomTextRenderer;
import meteordevelopment.meteorclient.renderer.text.FontFace;
import meteordevelopment.meteorclient.renderer.text.TextRenderer;
import meteordevelopment.meteorclient.utils.render.color.Color;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.io.IOException;
import java.nio.ByteBuffer;

import static meteordevelopment.meteorclient.renderer.text.CustomTextRenderer.SHADOW_COLOR;

@Mixin(value = CustomTextRenderer.class)
public abstract class CustomTextRendererMixin implements TextRenderer {

    @Shadow
    @Final
    private MeshBuilder mesh = new MeshBuilder(MeteorRenderPipelines.UI_TEXT);


    @Unique
    private FontFix[] fonts_fix;
    @Unique
    private FontFix font_fix;

    @Shadow
    private boolean building;
    @Shadow
    private boolean scaleOnly;
    @Shadow
    private double fontScale = 1;
    @Shadow
    private double scale = 1;

//    public CustomTextRendererMixin(FontFace fontFace) throws IOException {
//        super(fontFace);
//    }


    @Inject(method = "<init>",at = @At("RETURN"))
    public void onInit(FontFace fontFace, CallbackInfo ci) throws IOException {

        ByteBuffer buffer = fontFace.readToDirectByteBuffer();
        this.fonts_fix = new FontFix[5];

        for(int i = 0; i < this.fonts_fix.length; ++i) {
            this.fonts_fix[i] = new FontFix(buffer, (int)Math.round(27.0 * ((double)i * 0.5 + 1.0)));
        }
    }



    /**
     * @author Nippaku_Zanmu
     * @reason  我只能用这种方法修复他 之前尝试过Mixin Font类 但是字体会乱码
     */
    @Override
    public void begin(GuiGraphicsExtractor graphics, double scale, boolean scaleOnly, boolean big) {
        if (building) throw new RuntimeException("CustomTextRenderer.begin() called twice");

        if (!scaleOnly) mesh.begin();

        if (big) {
            this.font_fix = fonts_fix[fonts_fix.length - 1];
        } else {
            double scaleA = Math.floor(scale * 10) / 10;

            int scaleI;
            if (scaleA >= 3) scaleI = 5;
            else if (scaleA >= 2.5) scaleI = 4;
            else if (scaleA >= 2) scaleI = 3;
            else if (scaleA >= 1.5) scaleI = 2;
            else scaleI = 1;

            font_fix = fonts_fix[scaleI - 1];
        }

        this.building = true;
        this.scaleOnly = scaleOnly;

        this.fontScale = font_fix.getHeight() / 27.0;
        this.scale = 1 + (scale - fontScale) / fontScale;
    }
    /**
     * @author Nippaku_Zanmu
     * @reason  我只能用这种方法修复他 之前尝试过Mixin Font类 但是字体会乱码
     */
    @Overwrite
    public double getWidth(String text, int length, boolean shadow) {
        if (text.isEmpty()) {
            return 0.0;
        } else {
            FontFix font = this.building ? this.font_fix : this.fonts_fix[0];
            return (font.getWidth(text, length) + (double)(shadow ? 1 : 0)) * this.scale / 1.5;
        }
    }
    /**
     * @author Nippaku_Zanmu
     * @reason  我只能用这种方法修复他 之前尝试过Mixin Font类 但是字体会乱码
     */
    public double getHeight(boolean shadow) {
        FontFix font = this.building ? this.font_fix : this.fonts_fix[0];
        return (double)(font.getHeight() + 1 + (shadow ? 1 : 0)) * this.scale / 1.5;
    }
    /**
     * @author Nippaku_Zanmu
     * @reason  我只能用这种方法修复他 之前尝试过Mixin Font类 但是字体会乱码
     */
    @Override
    public double render(String text, double x, double y, Color color, boolean shadow) {
        if (!building) throw new RuntimeException("VanillaTextRenderer.render() called without calling begin()");

        double width;
        if (shadow) {
            int preShadowA = SHADOW_COLOR.a;
            SHADOW_COLOR.a = (int) (color.a / 255.0 * preShadowA);

            width = font_fix.render(mesh, text, x + fontScale * scale / 1.5, y + fontScale * scale / 1.5, SHADOW_COLOR, scale / 1.5);
            font_fix.render(mesh, text, x, y, color, scale / 1.5);

            SHADOW_COLOR.a = preShadowA;
        } else {
            width = font_fix.render(mesh, text, x, y, color, scale / 1.5);
        }

        return width;
    }

    /**
     * @author Nippaku_Zanmu
     * @reason  我只能用这种方法修复他 之前尝试过Mixin Font类 但是字体会乱码
     */
    @Overwrite
    @Override
    public void end() {
        if (!building) throw new RuntimeException("CustomTextRenderer.end() called without calling begin()");

        if (!scaleOnly) {
            mesh.end();

            MeshRenderer.begin()
                .attachments(Minecraft.getInstance().gameRenderer.mainRenderTarget())
                .pipeline(MeteorRenderPipelines.UI_TEXT)
                .mesh(mesh)
                .sampler("u_Texture", font_fix.texture.getTextureView(), font_fix.texture.getSampler())
                .end();
        }

        building = false;
        scale = 1;
    }

    public void destroy() {}
}
