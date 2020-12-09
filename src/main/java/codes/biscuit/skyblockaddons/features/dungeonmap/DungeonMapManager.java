package codes.biscuit.skyblockaddons.features.dungeonmap;

import codes.biscuit.skyblockaddons.SkyblockAddons;
import codes.biscuit.skyblockaddons.core.Feature;
import codes.biscuit.skyblockaddons.core.dungeons.DungeonPlayer;
import codes.biscuit.skyblockaddons.gui.buttons.ButtonLocation;
import codes.biscuit.skyblockaddons.misc.ChromaManager;
import codes.biscuit.skyblockaddons.utils.DrawUtils;
import codes.biscuit.skyblockaddons.utils.Utils;
import lombok.Getter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.MapItemRenderer;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.MathHelper;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Vec3;
import net.minecraft.util.Vec4b;
import net.minecraft.world.storage.MapData;
import org.lwjgl.opengl.GL11;

import java.util.*;

public class DungeonMapManager {

    private static SkyblockAddons main = SkyblockAddons.getInstance();
    private static final ResourceLocation DUNGEON_MAP = new ResourceLocation("skyblockaddons", "dungeonsmap.png");

    private static MapData mapData;
    @Getter private static float mapStartX = -1;
    @Getter private static float mapStartZ = -1;
    private static NavigableMap<Long, Vec3> previousLocations = new TreeMap<>();

    public static void drawDungeonsMap(Minecraft mc, float scale, ButtonLocation buttonLocation) {
        if (buttonLocation == null && !main.getUtils().isInDungeon()) {
            mapStartX = -1;
            mapStartZ = -1;
            mapData = null;
        }

        ItemStack possibleMapItemStack = mc.thePlayer.inventory.getStackInSlot(8);
        if (buttonLocation == null && (possibleMapItemStack == null || possibleMapItemStack.getItem() != Items.filled_map ||
                !possibleMapItemStack.hasDisplayName()) && mapData == null) {
            return;
        }
        boolean isScoreSummary = false;
        if (buttonLocation == null && possibleMapItemStack != null && possibleMapItemStack.getItem() == Items.filled_map) {
            isScoreSummary = possibleMapItemStack.getDisplayName().contains("Your Score Summary");

            if (!possibleMapItemStack.getDisplayName().contains("Magical Map") && !isScoreSummary) {
                return;
            }
        }

        float x = main.getConfigValues().getActualX(Feature.DUNGEONS_MAP_DISPLAY);
        float y = main.getConfigValues().getActualY(Feature.DUNGEONS_MAP_DISPLAY);

        GlStateManager.pushMatrix();

        int originalSize = 128;
        float initialScaleFactor = 0.5F;

        int size = (int) (originalSize * initialScaleFactor);

        int minecraftScale = new ScaledResolution(mc).getScaleFactor();
        GL11.glEnable(GL11.GL_SCISSOR_TEST);
        // Scissor is in screen coordinates...
        GL11.glScissor(Math.round((x - size / 2f * scale) * minecraftScale),
                mc.displayHeight - Math.round((y + size / 2F * scale) * minecraftScale), Math.round(size * minecraftScale * scale), Math.round(size * minecraftScale * scale));

        x = main.getRenderListener().transformXY(x, size, scale);
        y = main.getRenderListener().transformXY(y, size, scale);

        if (buttonLocation != null) {
            buttonLocation.checkHoveredAndDrawBox(x, x + size, y, y + size, scale);
        }

        GL11.glDisable(GL11.GL_SCISSOR_TEST);
        int color = main.getConfigValues().getColor(Feature.DUNGEONS_MAP_DISPLAY);
        DrawUtils.drawRect(x, y, x + size, y + size, 0x55000000);
        ChromaManager.renderingText(Feature.DUNGEONS_MAP_DISPLAY);
        DrawUtils.drawRectOutline(x, y, size, size, 1, color, main.getConfigValues().getChromaFeatures().contains(Feature.DUNGEONS_MAP_DISPLAY));
        ChromaManager.doneRenderingText();
        GlStateManager.color(1, 1, 1, 1);
        GL11.glEnable(GL11.GL_SCISSOR_TEST);

        main.getUtils().enableStandardGLOptions();

        GlStateManager.color(1, 1, 1, 1);

        float rotation = 180 - MathHelper.wrapAngleTo180_float(mc.thePlayer.rotationYaw);

        float zoomScaleFactor = main.getUtils().denormalizeScale(main.getConfigValues().getMapZoom().getValue(), 0.5F, 5, 0.1F);
        if (isScoreSummary) {
            zoomScaleFactor = 1;
        }

        float totalScaleFactor = initialScaleFactor * zoomScaleFactor;

        float mapSize = (originalSize * totalScaleFactor);

        GlStateManager.scale(totalScaleFactor, totalScaleFactor, 1);
        x /= totalScaleFactor;
        y /= totalScaleFactor;
        GlStateManager.translate(x, y, 0);

        float rotationCenterX = originalSize * initialScaleFactor;
        float rotationCenterY = originalSize * initialScaleFactor;

        float centerOffset = -((mapSize - size) / zoomScaleFactor);
        GlStateManager.translate(centerOffset, centerOffset, 0);

        boolean rotate = main.getConfigValues().isEnabled(Feature.ROTATE_MAP);
        boolean rotateOnPlayer = main.getConfigValues().isEnabled(Feature.CENTER_ROTATION_ON_PLAYER);

        if (isScoreSummary) {
            rotate = false;
        }

        if (buttonLocation == null) {
            try {
                boolean foundMapData = false;
                MapData newMapData = null;
                if (possibleMapItemStack != null) {
                    newMapData = Items.filled_map.getMapData(possibleMapItemStack, mc.theWorld);
                }
                if (newMapData != null) {
                    mapData = newMapData;
                    foundMapData = true;
                }

                if (mapData != null) {
                    float playerX = (float) mc.thePlayer.posX;
                    float playerZ = (float) mc.thePlayer.posZ;

                    // TODO Feature Rewrite: Replace with per-tick service...
                    long now = System.currentTimeMillis();
                    previousLocations.entrySet().removeIf(entry -> entry.getKey() < now - 1000);
                    Vec3 currentVector = mc.thePlayer.getPositionVector();
                    previousLocations.put(now, currentVector);

                    double lastSecondTravel = -1;
                    Map.Entry<Long, Vec3> closestEntry = previousLocations.ceilingEntry(now - 1000);
                    if (closestEntry != null) {
                        Vec3 lastSecondVector = closestEntry.getValue();
                        if (lastSecondVector != null) {
                            lastSecondTravel = lastSecondVector.distanceTo(currentVector);
                        }
                    }
                    if (foundMapData && ((DungeonMapManager.mapStartX == -1 || DungeonMapManager.mapStartZ == -1) || lastSecondTravel == 0)) {
                        if (mapData.mapDecorations != null) {
                            for (Map.Entry<String, Vec4b> entry : mapData.mapDecorations.entrySet()) {
                                // Icon type 1 is the green player marker...
                                if (entry.getValue().func_176110_a() == 1) {
                                    float mapMarkerX = entry.getValue().func_176112_b() / 2.0F + 64.0F;
                                    float mapMarkerZ = entry.getValue().func_176113_c() / 2.0F + 64.0F;

                                    // 1 pixel on Hypixel map represents 1.5 blocks...
                                    float mapStartX = playerX - mapMarkerX * 1.5F;
                                    float mapStartZ = playerZ - mapMarkerZ * 1.5F;

                                    DungeonMapManager.mapStartX = Math.round(mapStartX / 16F) * 16F;
                                    DungeonMapManager.mapStartZ = Math.round(mapStartZ / 16F) * 16F;

//                                    Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText(String.valueOf(this.mapStartX)));
                                }
                            }
                        }
                    }

                    float playerMarkerX = (playerX - mapStartX) / 1.5F;
                    float playerMarkerZ = (playerZ - mapStartZ) / 1.5F;

                    if (rotate && rotateOnPlayer) {
                        rotationCenterX = playerMarkerX;
                        rotationCenterY = playerMarkerZ;
                    }

                    if (rotate) {
                        if (rotateOnPlayer) {
                            GlStateManager.translate(size - rotationCenterX, size - rotationCenterY, 0);
                        }

                        GlStateManager.translate(rotationCenterX, rotationCenterY, 0);
                        GlStateManager.rotate(rotation, 0, 0, 1);
                        GlStateManager.translate(-rotationCenterX, -rotationCenterY, 0);
                    }

                    MapItemRenderer.Instance instance = mc.entityRenderer.getMapItemRenderer().getMapRendererInstance(mapData);
                    drawMapEdited(instance, isScoreSummary, zoomScaleFactor);
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        } else {
            if (rotate) {
                long ticks = System.currentTimeMillis() % 18000 / 50;

                GlStateManager.translate(rotationCenterX, rotationCenterY, 0);
                GlStateManager.rotate(ticks, 0, 0, 1);
                GlStateManager.translate(-rotationCenterX, -rotationCenterY, 0);
            }

            mc.getTextureManager().bindTexture(DUNGEON_MAP);
            DrawUtils.drawModalRectWithCustomSizedTexture(0, 0, 0, 0, 128, 128, 128, 128);
        }
//        main.getUtils().drawRect(rotationCenterX-2, rotationCenterY-2, rotationCenterX+2, rotationCenterY+2, 0xFFFF0000);
        GL11.glDisable(GL11.GL_SCISSOR_TEST);

        GlStateManager.popMatrix();
//        main.getUtils().drawRect(mapCenterX-2, mapCenterY-2, mapCenterX+2, mapCenterY+2, 0xFF00FF00);

        main.getUtils().restoreGLOptions();
    }


    private static Map<String, Vec4b> savedMapDecorations = new HashMap<>();

    public static void drawMapEdited(MapItemRenderer.Instance instance, boolean isScoreSummary, float zoom) {
        Minecraft mc = Minecraft.getMinecraft();
        int startX = 0;
        int startY = 0;
        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer worldrenderer = tessellator.getWorldRenderer();
        float f = 0.0F;
        GlStateManager.enableTexture2D();
        mc.getTextureManager().bindTexture(instance.location);
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(1, 771, 0, 1);
        worldrenderer.begin(7, DefaultVertexFormats.POSITION_TEX);
        worldrenderer.pos((float)(startX) + f, (float)(startY + 128) - f, -0.009999999776482582D).tex(0.0D, 1.0D).endVertex();
        worldrenderer.pos((float)(startX + 128) - f, (float)(startY + 128) - f, -0.009999999776482582D).tex(1.0D, 1.0D).endVertex();
        worldrenderer.pos((float)(startX + 128) - f, (float)(startY) + f, -0.009999999776482582D).tex(1.0D, 0.0D).endVertex();
        worldrenderer.pos((float)(startX) + f, (float)(startY) + f, -0.009999999776482582D).tex(0.0D, 0.0D).endVertex();
        tessellator.draw();
        GlStateManager.enableAlpha();
        GlStateManager.disableBlend();
        mc.getTextureManager().bindTexture(MapItemRenderer.mapIcons);
        int decorationCount = 0;

        // We don't need to show any markers...
        if (isScoreSummary) return;

        // Prevent marker flickering...
        if (!instance.mapData.mapDecorations.isEmpty()) {
            savedMapDecorations.clear();
            savedMapDecorations.putAll(instance.mapData.mapDecorations);
        }

        // Don't add markers that we replaced with smooth client side ones...
        Set<String> dontAddMarkerNames = new HashSet<>();

        // Add these markers later because they are the smooth client side ones
        // and should get priority.
        Set<MapMarker> markersToAdd = new LinkedHashSet<>();
        Map<String, DungeonPlayer> dungeonPlayers = main.getDungeonManager().getPlayers();

        for (Map.Entry<String, DungeonPlayer> dungeonPlayerEntry : dungeonPlayers.entrySet()) {
            DungeonPlayer dungeonPlayer = dungeonPlayerEntry.getValue();
            EntityPlayer entityPlayer = Utils.getPlayerFromName(dungeonPlayerEntry.getKey());

            MapMarker mapMarker;
            // If this player's marker already exists, lets update the saved one instead
            if (dungeonPlayer.getMapMarker() == null) {
                dungeonPlayer.setMapMarker(mapMarker = new MapMarker(entityPlayer));
            } else {
                mapMarker = dungeonPlayer.getMapMarker();
            }

            // Check if there is a vanilla marker around the same spot as our custom
            // marker. If so, we probably found the corresponding marker for this player.
            int duplicates = 0;
            Map.Entry<String, Vec4b> duplicate = null;
            for (Map.Entry<String, Vec4b> vec4b : savedMapDecorations.entrySet()) {
                if (vec4b.getValue().func_176110_a() == mapMarker.getIconType() &&
                        Math.abs(vec4b.getValue().func_176112_b() - mapMarker.getX()) <= 5 &&
                        Math.abs(vec4b.getValue().func_176113_c() - mapMarker.getZ()) <= 5) {
                    duplicates++;
                    duplicate = vec4b;
                }
            }

            // However, if we find more than one duplicate marker, we can't be
            // certain that this we found the player's corresponding marker.
            if (duplicates == 1) {
                mapMarker.setMapMarkerName(duplicate.getKey());
            }

            // For the ones that we replaced, lets make sure we skip the vanilla ones later.
            if (mapMarker.getMapMarkerName() != null) {
                dontAddMarkerNames.add(mapMarker.getMapMarkerName());
            }
            markersToAdd.add(mapMarker);
        }

        // The final set of markers that will be used....
        Set<MapMarker> allMarkers = new TreeSet<>((first, second) -> {
            boolean firstIsNull = first.getMapMarkerName() == null;
            boolean secondIsNull = second.getMapMarkerName() == null;

            if (first.getIconType() != second.getIconType()) {
                return Byte.compare(second.getIconType(), first.getIconType());
            }

            if (firstIsNull && secondIsNull) {
                return 0;
            } else if (firstIsNull) {
                return 1;
            } else if (secondIsNull) {
                return -1;
            }

            return second.getMapMarkerName().compareTo(first.getMapMarkerName());
        });

        for (Map.Entry<String, Vec4b> vec4b : savedMapDecorations.entrySet()) {
            // If we replaced this marker with a smooth one OR this is the player's marker, lets skip.
            if (dontAddMarkerNames.contains(vec4b.getKey()) || vec4b.getValue().func_176110_a() == 1) continue;

            // Check if this marker key is linked to a player
            DungeonPlayer foundDungeonPlayer = null;
            boolean linkedToPlayer = false;
            for (DungeonPlayer dungeonPlayer : dungeonPlayers.values()) {
                if (dungeonPlayer.getMapMarker() != null && dungeonPlayer.getMapMarker().getMapMarkerName() != null &&
                        vec4b.getKey().equals(dungeonPlayer.getMapMarker().getMapMarkerName())) {
                    linkedToPlayer = true;
                    foundDungeonPlayer = dungeonPlayer;
                    break;
                }
            }

            // Vec4b
            // a -> Icon Type
            // b -> X
            // c -> Z
            // d -> Icon Direction instance.mapData.mapDecorations.values()

            // If this isn't linked to a player, lets just add the marker normally...
            if (!linkedToPlayer) {
                allMarkers.add(new MapMarker(vec4b.getValue().func_176110_a(), vec4b.getValue().func_176112_b(),
                        vec4b.getValue().func_176113_c(), vec4b.getValue().func_176111_d()));
            } else {
                // This marker is linked to a player, lets update that marker's data to the server's
                MapMarker mapMarker = foundDungeonPlayer.getMapMarker();
                mapMarker.setX(vec4b.getValue().func_176112_b());
                mapMarker.setZ(vec4b.getValue().func_176113_c());
                mapMarker.setRotation(vec4b.getValue().func_176111_d());
                allMarkers.add(mapMarker);
            }
        }
        // Add the smooth markers from before
        allMarkers.addAll(markersToAdd);

        for (MapMarker mapMarker : allMarkers) {
            GlStateManager.pushMatrix();
            GlStateManager.translate((float)startX + mapMarker.getX() / 2.0F + 64.0F, (float)startY + mapMarker.getZ() / 2.0F + 64.0F, -0.02F);
            GlStateManager.rotate((mapMarker.getRotation() * 360) / 16.0F, 0.0F, 0.0F, 1.0F);
            GlStateManager.scale(4.0F/zoom, 4.0F/zoom, 3.0F);
            byte iconType = mapMarker.getIconType();
            float f1 = (float)(iconType % 4) / 4.0F;
            float f2 = (float)(iconType / 4) / 4.0F;
            float f3 = (float)(iconType % 4 + 1) / 4.0F;
            float f4 = (float)(iconType / 4 + 1) / 4.0F;

            NetworkPlayerInfo markerNetworkPlayerInfo = null;
            if (main.getConfigValues().isEnabled(Feature.SHOW_PLAYER_HEADS_ON_MAP) && mapMarker.getPlayerName() != null) {
                for (NetworkPlayerInfo networkPlayerInfo : mc.getNetHandler().getPlayerInfoMap()) {
                    if (mapMarker.getPlayerName().equals(networkPlayerInfo.getGameProfile().getName())) {
                        markerNetworkPlayerInfo = networkPlayerInfo;
                        break;
                    }
                }
            }

            if (markerNetworkPlayerInfo != null) {
                GlStateManager.rotate(180, 0.0F, 0.0F, 1.0F);
                DrawUtils.drawRect(-1.2, -1.2, 1.2, 1.2, 0xFF000000);

                GlStateManager.color(1, 1, 1, 1);

                if (main.getConfigValues().isEnabled(Feature.SHOW_CRITICAL_DUNGEONS_TEAMMATES) &&
                        dungeonPlayers.containsKey(mapMarker.getPlayerName())) {
                    DungeonPlayer dungeonPlayer = dungeonPlayers.get(mapMarker.getPlayerName());
                    if (dungeonPlayer.isLow()) {
                        GlStateManager.color(1, 1, 0.5F, 1);
                    } else if (dungeonPlayer.isCritical()) {
                        GlStateManager.color(1, 0.5F, 0.5F, 1);
                    }
                }

                mc.getTextureManager().bindTexture(markerNetworkPlayerInfo.getLocationSkin());
                DrawUtils.drawScaledCustomSizeModalRect(-1, -1, 8.0F, 8, 8, 8, 2, 2, 64.0F, 64.0F, false);
                if (mapMarker.isWearingHat()) {
                    DrawUtils.drawScaledCustomSizeModalRect(-1, -1, 40.0F, 8, 8, 8, 2, 2, 64.0F, 64.0F, false);
                }
            } else {
                GlStateManager.translate(-0.125F, 0.125F, 0.0F);
                mc.getTextureManager().bindTexture(MapItemRenderer.mapIcons);
                worldrenderer.begin(7, DefaultVertexFormats.POSITION_TEX);
                float eachDecorationZOffset = -0.001F;
                worldrenderer.pos(-1.0D, 1.0D, (float)decorationCount * eachDecorationZOffset).tex(f1, f2).endVertex();
                worldrenderer.pos(1.0D, 1.0D, (float)decorationCount * eachDecorationZOffset).tex(f3, f2).endVertex();
                worldrenderer.pos(1.0D, -1.0D, (float)decorationCount * eachDecorationZOffset).tex(f3, f4).endVertex();
                worldrenderer.pos(-1.0D, -1.0D, (float)decorationCount * eachDecorationZOffset).tex(f1, f4).endVertex();
                tessellator.draw();
            }
            GlStateManager.color(1, 1, 1, 1);
            GlStateManager.popMatrix();
            ++decorationCount;
        }
    }

}
