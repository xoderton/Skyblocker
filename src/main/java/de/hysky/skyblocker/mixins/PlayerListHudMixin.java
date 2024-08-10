package de.hysky.skyblocker.mixins;

import com.llamalad7.mixinextras.sugar.Local;
import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.skyblock.tabhud.TabHud;
import de.hysky.skyblocker.skyblock.tabhud.screenbuilder.ScreenMaster;
import de.hysky.skyblocker.skyblock.tabhud.util.PlayerListMgr;
import de.hysky.skyblocker.utils.Utils;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.PlayerListHud;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Environment(EnvType.CLIENT)
@Mixin(PlayerListHud.class)
public class PlayerListHudMixin {
    @Shadow
    private Text footer;

    @Inject(at = @At("HEAD"), method = "render", cancellable = true)
    public void skyblocker$renderTabHud(CallbackInfo info, @Local(argsOnly = true) DrawContext context, @Local(argsOnly = true) int w) {
        if (!Utils.isInDungeons() || !SkyblockerConfigManager.get().uiAndVisuals.tabHud.tabHudEnabled || TabHud.defaultTgl.isPressed()) {
            return;
        }

        ClientPlayNetworkHandler nwH = MinecraftClient.getInstance().getNetworkHandler();
        if (nwH == null) {
            return;
        }

        int h = MinecraftClient.getInstance().getWindow().getScaledHeight();
        float scale = SkyblockerConfigManager.get().uiAndVisuals.tabHud.tabHudScale / 100f;
        w = (int) (w / scale);
        h = (int) (h / scale);

        try {
            ScreenMaster.render(context, w, h);
            // Screen screen = Screen.getCorrect(w, h, footer);
            // screen.render(context);
            info.cancel();
        } catch (Exception e) {
            TabHud.LOGGER.error("[Skyblocker] Encountered unknown exception while drawing default hud", e);
        }
    }

    @Inject(at = @At("HEAD"), method = "setFooter")
    public void skblocker$updateFooter(@Nullable Text footer, CallbackInfo info) {
        PlayerListMgr.updateFooter(footer);
    }

}
