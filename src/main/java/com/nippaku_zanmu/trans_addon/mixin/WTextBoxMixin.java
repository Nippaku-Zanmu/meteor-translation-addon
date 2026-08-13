package com.nippaku_zanmu.trans_addon.mixin;

import meteordevelopment.meteorclient.gui.widgets.input.WTextBox;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.vehicle.minecart.Minecart;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(WTextBox.class)
public class WTextBoxMixin {

    @Unique
    private static final  Minecraft mc = Minecraft.getInstance();
    @Inject(method = "setFocused",at=@At("RETURN"))
    private void onSetFocused(boolean focused, CallbackInfo ci){
        if (mc.gui.screen() != null) {
            mc.onTextInputFocusChange(mc.gui.screen(),focused);
        }
    }
}
