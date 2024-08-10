package de.hysky.skyblocker.skyblock.profileviewer.inventory;

import com.google.gson.JsonObject;
import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.skyblock.item.ItemRarityBackgrounds;
import de.hysky.skyblocker.skyblock.profileviewer.ProfileViewerPage;
import de.hysky.skyblocker.skyblock.profileviewer.inventory.itemLoaders.InventoryItemLoader;
import it.unimi.dsi.fastutil.ints.IntIntPair;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.awt.*;
import java.util.Collections;
import java.util.List;

public class PlayerInventory implements ProfileViewerPage {
    private static final Identifier TEXTURE = Identifier.of("textures/gui/container/generic_54.png");
    private static final MinecraftClient CLIENT = MinecraftClient.getInstance();
    private static final TextRenderer textRenderer = CLIENT.textRenderer;
    private final List<ItemStack> containerList;
    private List<Text> tooltip = Collections.emptyList();

    public PlayerInventory(JsonObject inventory) {
        this.containerList = new InventoryItemLoader().loadItems(inventory);
    }

    // Z-STACKING forces this nonsense of separating the Background texture and Item Drawing :(
    public void render(DrawContext context, int mouseX, int mouseY, float delta, int rootX, int rootY) {
        drawContainerTextures(context, "armor", rootX, rootY + 108, IntIntPair.of(1, 4));
        drawContainerTextures(context, "inventory", rootX, rootY + 2, IntIntPair.of(4, 9));
        drawContainerTextures(context, "equipment", rootX + 90, rootY + 108, IntIntPair.of(1, 4));

        tooltip.clear();
        drawContainerItems(context, rootX, rootY + 108, IntIntPair.of(1, 4), 36, 40, mouseX, mouseY);
        drawContainerItems(context, rootX, rootY + 2, IntIntPair.of(4, 9), 0, 36, mouseX, mouseY);
        drawContainerItems(context, rootX + 90, rootY + 108, IntIntPair.of(1, 4), 40, containerList.size(), mouseX, mouseY);
        if (!tooltip.isEmpty()) context.drawTooltip(textRenderer, tooltip, mouseX, mouseY);
    }

    private void drawContainerTextures(DrawContext context, String containerName, int rootX, int rootY, IntIntPair dimensions) {
        if (containerName.equals("inventory")) {
            context.drawTexture(TEXTURE, rootX, rootY + dimensions.leftInt() + 10, 0, 136, dimensions.rightInt() * 18 + 7, dimensions.leftInt() * 18 + 17);
            context.drawTexture(TEXTURE, rootX + dimensions.rightInt() * 18 + 7, rootY, 169, 0, 7, dimensions.leftInt() * 18 + 21);
            context.drawTexture(TEXTURE, rootX, rootY, 0, 0, dimensions.rightInt() * 18 + 7, 14);
            context.drawTexture(TEXTURE, rootX + dimensions.rightInt() * 18 + 7, rootY + dimensions.leftInt() * 18 + 21, 169, 215, 7, 7);
        } else {
            context.drawTexture(TEXTURE, rootX, rootY, 0, 0, dimensions.rightInt() * 18 + 7, dimensions.leftInt() * 18 + 17);
            context.drawTexture(TEXTURE, rootX + dimensions.rightInt() * 18 + 7, rootY, 169, 0, 7, dimensions.leftInt() * 18 + 17);
            context.drawTexture(TEXTURE, rootX, rootY + dimensions.leftInt() * 18 + 17, 0, 215, dimensions.rightInt() * 18 + 7, 7);
            context.drawTexture(TEXTURE, rootX + dimensions.rightInt() * 18 + 7, rootY + dimensions.leftInt() * 18 + 17, 169, 215, 7, 7);
        }
        context.drawText(textRenderer, I18n.translate("skyblocker.profileviewer.inventory." + containerName), rootX + 7, rootY + 7, Color.DARK_GRAY.getRGB(), false);
    }

    private void drawContainerItems(DrawContext context, int rootX, int rootY, IntIntPair dimensions, int startIndex, int endIndex, int mouseX, int mouseY) {
        for (int i = 0; i < endIndex - startIndex; i++) {
            if (containerList.get(startIndex + i) == ItemStack.EMPTY) continue;
            int column = i % dimensions.rightInt();
            int row = i / dimensions.rightInt();

            int x = rootX + 8 + column * 18;
            int y = (rootY + 18 + row * 18) + (dimensions.leftInt() > 1 && row + 1 == dimensions.leftInt() ? 4 : 0);

            if (SkyblockerConfigManager.get().general.itemInfoDisplay.itemRarityBackgrounds) {
                ItemRarityBackgrounds.tryDraw(containerList.get(startIndex + i), context, x, y);
            }

            context.drawItem(containerList.get(startIndex + i), x, y);
            context.drawItemInSlot(textRenderer, containerList.get(startIndex + i), x, y);

            if (mouseX > x - 1 && mouseX < x + 16 && mouseY > y - 1 && mouseY < y + 16) {
                tooltip = containerList.get(startIndex + i).getTooltip(Item.TooltipContext.DEFAULT, CLIENT.player, CLIENT.options.advancedItemTooltips ? TooltipType.ADVANCED : TooltipType.BASIC);
            }
        }
    }
}
