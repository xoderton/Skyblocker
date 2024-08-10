package de.hysky.skyblocker.mixins;

import de.hysky.skyblocker.skyblock.crimson.kuudra.ArrowPoisonWarning;
import net.minecraft.entity.player.PlayerInventory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerInventory.class)
public class PlayerInventoryMixin {
    @Shadow
    public int selectedSlot;

    @Inject(method = "scrollInHotbar", at = @At("TAIL"))
    private void skyblocker$onHotbarScroll(CallbackInfo ci) {
        ArrowPoisonWarning.tryWarn(selectedSlot);
    }
}
