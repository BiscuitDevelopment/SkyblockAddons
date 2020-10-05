package codes.biscuit.skyblockaddons.features.tablist;

import codes.biscuit.skyblockaddons.utils.TextUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiPlayerTabOverlay;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EnumPlayerModelParts;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TabListRenderer {

    public static final int MAX_LINES = 22;
    private static final int LINE_HEIGHT = 8 + 1;
    private static final int PADDING = 3;
    private static final int COLUMN_SPACING = 6;

    public static void render() {
        Minecraft mc = Minecraft.getMinecraft();

        List<RenderColumn> columns = TabListParser.getRenderColumns();
        if (columns == null) {
            return;
        }

        // Calculate maximums...
        int maxLines = 0;
        for (RenderColumn column : columns) {
            maxLines = Math.max(maxLines, column.getLines().size());
        }
        int totalWidth = 0;
        for (RenderColumn renderColumn : columns) {
            totalWidth += renderColumn.getMaxWidth() + COLUMN_SPACING;
        }
        totalWidth -= COLUMN_SPACING;
        int totalHeight = maxLines * LINE_HEIGHT;

        // Filter header and footer to only show hypixel advertisements...
        GuiPlayerTabOverlay tabList = mc.ingameGUI.getTabList();
        List<String> header = null;
        if (tabList.header != null) {
            header = new ArrayList<>(Arrays.asList(tabList.header.getFormattedText().split("\n")));
            header.removeIf((line) -> !line.contains(TabListParser.HYPIXEL_ADVERTISEMENT_CONTAINS));

            totalHeight += header.size() * LINE_HEIGHT + PADDING;
        }
        List<String> footer = null;
        if (tabList.footer != null) {
            footer = new ArrayList<>(Arrays.asList(tabList.footer.getFormattedText().split("\n")));
            footer.removeIf((line) -> !line.contains(TabListParser.HYPIXEL_ADVERTISEMENT_CONTAINS));

            totalHeight += footer.size() * LINE_HEIGHT + PADDING;
        }

        // Starting x & y, using the player's GUI scale
        ScaledResolution scaledResolution = new ScaledResolution(mc);
        int screenWidth = scaledResolution.getScaledWidth() / 2;
        int x = screenWidth - totalWidth/2;
        int y = 10;

        // Large background
        Gui.drawRect(x - COLUMN_SPACING, y - PADDING, screenWidth + totalWidth/2 + COLUMN_SPACING, 10 + totalHeight + PADDING, 0x80000000);

        // Draw header
        int headerY = y;
        if (header != null) {
            for (String line : header) {
                mc.fontRendererObj.drawStringWithShadow(line, x + totalWidth / 2F - mc.fontRendererObj.getStringWidth(line) / 2F, headerY, 0xFFFFFFFF);
                headerY += 8 + 1;
            }
            headerY += PADDING;
        }

        // Draw the middle lines
        int middleX = x;
        for (RenderColumn renderColumn : columns) {
            int middleY = headerY;

            // Column background
            Gui.drawRect(middleX - PADDING + 1, middleY - PADDING + 1, middleX + renderColumn.getMaxWidth() + PADDING - 2,
                    middleY + renderColumn.getLines().size() * LINE_HEIGHT + PADDING - 2, 0x20AAAAAA);

            for (TabLine tabLine : renderColumn.getLines()) {
                int savedX = middleX;

                if (tabLine.getType() == TabStringType.PLAYER) {
                    NetworkPlayerInfo networkPlayerInfo = mc.getNetHandler().getPlayerInfo(TextUtils.stripColor(tabLine.getText()));
                    if (networkPlayerInfo != null) {
                        EntityPlayer entityPlayer = mc.theWorld.getPlayerEntityByUUID(networkPlayerInfo.getGameProfile().getId());

                        mc.getTextureManager().bindTexture(networkPlayerInfo.getLocationSkin());
                        Gui.drawScaledCustomSizeModalRect(middleX, middleY, 8, 8, 8, 8, 8, 8, 64.0F, 64.0F);
                        if (entityPlayer != null && entityPlayer.isWearing(EnumPlayerModelParts.HAT)) {
                            Gui.drawScaledCustomSizeModalRect(middleX, middleY, 40.0F, 8, 8, 8, 8, 8, 64.0F, 64.0F);
                        }
                    }
                    middleX += 8 + 2;
                }

                if (tabLine.getType() == TabStringType.TITLE) {
                    mc.fontRendererObj.drawStringWithShadow(tabLine.getText(), (middleX + renderColumn.getMaxWidth() / 2F - tabLine.getWidth() / 2F), middleY, 0xFFFFFFFF);
                } else {
                    mc.fontRendererObj.drawStringWithShadow(tabLine.getText(), middleX, middleY, 0xFFFFFFFF);
                }
                middleY += LINE_HEIGHT;
                middleX = savedX;
            }

            middleX += renderColumn.getMaxWidth() + COLUMN_SPACING;
        }

        // Draw the footer
        if (footer != null) {
            int footerY = y + totalHeight - footer.size() * LINE_HEIGHT;
            for (String line : footer) {
                mc.fontRendererObj.drawStringWithShadow(line, x + totalWidth / 2F - mc.fontRendererObj.getStringWidth(line) / 2F, footerY, 0xFFFFFFFF);
                footerY += LINE_HEIGHT;
            }
        }
    }
}
