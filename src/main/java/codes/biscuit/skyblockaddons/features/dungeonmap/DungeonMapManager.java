package codes.biscuit.skyblockaddons.features.dungeonmap;

import codes.biscuit.skyblockaddons.SkyblockAddons;
import codes.biscuit.skyblockaddons.core.Feature;
import codes.biscuit.skyblockaddons.core.chroma.ManualChromaManager;
import codes.biscuit.skyblockaddons.core.dungeons.DungeonPlayer;
import codes.biscuit.skyblockaddons.gui.buttons.ButtonLocation;
import codes.biscuit.skyblockaddons.utils.DrawUtils;
import codes.biscuit.skyblockaddons.utils.MathUtils;
import lombok.Getter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.MapItemRenderer;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.crash.CrashReport;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.*;
import net.minecraft.world.storage.MapData;
import net.minecraftforge.fml.relauncher.ReflectionHelper;
import org.lwjgl.opengl.GL11;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

public class DungeonMapManager {

    private static final SkyblockAddons main = SkyblockAddons.getInstance();
    private static final ResourceLocation DUNGEON_MAP = new ResourceLocation("skyblockaddons", "dungeonsmap.png");
    private static final Comparator<MapMarker> MAP_MARKER_COMPARATOR = (first, second) -> {
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
    };

    /** The factor the player's coordinates are multiplied by to calculate their map marker coordinates */
    private static final float COORDINATE_FACTOR = 1.33F;

    /** {@link EntityPlayerSP#lastReportedPosX} */
    static final Field lastReportedPosX;
    /** {@link EntityPlayerSP#lastReportedPosZ} */
    static final Field lastReportedPosZ;

    private static MapData mapData;
    /** The offset added to the player's x-coordinate when calculating their map marker coordinates */
    @Getter private static double markerOffsetX = 0;
    /** The offset added to the player's z-coordinate when calculating their map marker coordinates */
    @Getter private static double markerOffsetZ = 0;
    private static final NavigableMap<Long, Vec3> previousLocations = new TreeMap<>();

    static {
        try {
            lastReportedPosX = ReflectionHelper.findField(EntityPlayerSP.class, "bK", "field_175172_bI",
                    "lastReportedPosX");
            lastReportedPosX.setAccessible(true);
            lastReportedPosZ = ReflectionHelper.findField(EntityPlayerSP.class, "bM", "field_175167_bK",
                    "lastReportedPosZ");
            lastReportedPosZ.setAccessible(true);
        } catch (ReflectionHelper.UnableToFindFieldException e) {
            throw new ReportedException(CrashReport.makeCrashReport(e,
                    "Field not found, there's something really wrong here."));
        }
    }

    public static void drawDungeonsMap(Minecraft mc, float scale, ButtonLocation buttonLocation) {
        if (buttonLocation == null && !main.getUtils().isInDungeon()) {
            markerOffsetX = -1;
            markerOffsetZ = -1;
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
        DrawUtils.drawRectAbsolute(x, y, x + size, y + size, 0x55000000);
        ManualChromaManager.renderingText(Feature.DUNGEONS_MAP_DISPLAY);
        DrawUtils.drawRectOutline(x, y, size, size, 1, color, main.getConfigValues().getChromaFeatures().contains(Feature.DUNGEONS_MAP_DISPLAY));
        ManualChromaManager.doneRenderingText();
        GlStateManager.color(1, 1, 1, 1);
        GL11.glEnable(GL11.GL_SCISSOR_TEST);

        main.getUtils().enableStandardGLOptions();

        GlStateManager.color(1, 1, 1, 1);

        float rotation = 180 - MathHelper.wrapAngleTo180_float(mc.thePlayer.rotationYaw);

        float zoomScaleFactor = MathUtils.denormalizeSliderValue(main.getConfigValues().getMapZoom().getValue(), 0.5F, 5, 0.1F);
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
                    if (foundMapData && ((markerOffsetX == -1 || markerOffsetZ == -1) || lastSecondTravel == 0)) {
                        if (mapData.mapDecorations != null) {
                            for (Map.Entry<String, Vec4b> entry : mapData.mapDecorations.entrySet()) {
                                // Icon type 1 is the green player marker...
                                if (entry.getValue().func_176110_a() == 1) {
                                    int mapMarkerX = entry.getValue().func_176112_b();
                                    int mapMarkerZ = entry.getValue().func_176113_c();

                                    markerOffsetX = calculateMarkerOffset(lastReportedPosX.getDouble(mc.thePlayer), mapMarkerX);
                                    markerOffsetZ = calculateMarkerOffset(lastReportedPosZ.getDouble(mc.thePlayer), mapMarkerZ);
                                }
                            }
                        }
                    }

                    if (rotate && rotateOnPlayer) {
                        rotationCenterX = toRenderCoordinate(toMapCoordinate(mc.thePlayer.posX,
                                markerOffsetX));
                        rotationCenterY = toRenderCoordinate(toMapCoordinate(mc.thePlayer.posZ,
                                markerOffsetZ));
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
                float ticks = System.currentTimeMillis() % 18000 / 50F;

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


    private static final Map<String, Vec4b> savedMapDecorations = new HashMap<>();

    public static void drawMapEdited(MapItemRenderer.Instance instance, boolean isScoreSummary, float markerScale) {
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

        // Don't add markers that we replaced with smooth client side ones
        Set<String> dontAddMarkerNames = new HashSet<>();

        // The final set of markers that will be used
        Set<MapMarker> allMarkers = new TreeSet<>(MAP_MARKER_COMPARATOR);

        Map<String, DungeonPlayer> teammates = main.getDungeonManager().getTeammates();

        // Grab all the world player entities and try to correlate them to the map
        for (EntityPlayer player : mc.theWorld.playerEntities) {
            // Try to find the player's map marker
            MapMarker playerMarker = null;
            if (teammates.containsKey(player.getName())) {
                DungeonPlayer dungeonTeammate = teammates.get(player.getName());
                playerMarker = getMapMarkerForPlayer(dungeonTeammate, player);
            }
            else if (player == mc.thePlayer) {
                playerMarker = getMapMarkerForPlayer(null, player);
            }

            if (playerMarker != null) {
                if (playerMarker.getMapMarkerName() != null) {
                    dontAddMarkerNames.add(playerMarker.getMapMarkerName());
                }
                allMarkers.add(playerMarker);
            }
        }

        // Grab all of the map icons to make sure we don't miss any that weren't correlated before
        for (Map.Entry<String, Vec4b> vec4b : savedMapDecorations.entrySet()) {
            // If we replaced this marker with a smooth one OR this is the player's marker, lets skip.
            if (dontAddMarkerNames.contains(vec4b.getKey()) || vec4b.getValue().func_176110_a() == 1) {
                continue;
            }

            // Check if this marker key is linked to a player
            DungeonPlayer foundDungeonPlayer = null;
            boolean linkedToPlayer = false;
            for (DungeonPlayer dungeonPlayer : teammates.values()) {
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

        markerScale = 4.0F / markerScale;

        for (MapMarker mapMarker : allMarkers) {
            GlStateManager.pushMatrix();
            GlStateManager.translate((float)startX + mapMarker.getX() / 2.0F + 64.0F, (float)startY + mapMarker.getZ() / 2.0F + 64.0F, -0.02F);
            GlStateManager.rotate((mapMarker.getRotation() * 360) / 16.0F, 0.0F, 0.0F, 1.0F);
            GlStateManager.scale(markerScale, markerScale, 3.0F);
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
                DrawUtils.drawRectAbsolute(-1.2, -1.2, 1.2, 1.2, 0xFF000000);

                GlStateManager.color(1, 1, 1, 1);

                if (main.getConfigValues().isEnabled(Feature.SHOW_CRITICAL_DUNGEONS_TEAMMATES) &&
                        teammates.containsKey(mapMarker.getPlayerName())) {
                    DungeonPlayer dungeonPlayer = teammates.get(mapMarker.getPlayerName());
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

    public static MapMarker getMapMarkerForPlayer(DungeonPlayer dungeonPlayer, EntityPlayer player) {
        MapMarker mapMarker;
        if (dungeonPlayer != null) {
            // If this player's marker already exists, lets update the saved one instead
            if (dungeonPlayer.getMapMarker() == null) {
                dungeonPlayer.setMapMarker(mapMarker = new MapMarker(player));
            } else {
                mapMarker = dungeonPlayer.getMapMarker();
                mapMarker.updateXZRot(player);
            }
        } else {
            mapMarker = new MapMarker(player);
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

        return mapMarker;
    }

    /**
     * Calculates {@code markerOffsetX} or {@code markerOffsetZ}.
     *
     * @param playerCoordinate the player's x/z coordinate from {@link EntityPlayer#getPosition()}
     * @param playerMarkerCoordinate the player's map marker x/z coordinate from {@code mapData}
     * @return the x/z offset used to calculate the player marker's coordinates
     */
    public static double calculateMarkerOffset(double playerCoordinate, int playerMarkerCoordinate) {
        return BigDecimal.valueOf(playerMarkerCoordinate - (COORDINATE_FACTOR * playerCoordinate))
                .setScale(5, RoundingMode.HALF_UP).doubleValue();
    }

    /**
     * Converts a player's actual x/z coordinate to their marker's x/z coordinate on the dungeon map.
     * The resulting coordinate is rounded to the nearest integer lower than the actual value.
     *
     * @param playerCoordinate the player's x/z coordinate from {@link EntityPlayer#posX} or {@link EntityPlayer#posZ}
     * @param markerOffset {@code markerOffsetX} or {@code markerOffsetZ}
     * @return the player marker's x/z coordinate
     */
    public static float toMapCoordinate(double playerCoordinate, double markerOffset) {
        return BigDecimal.valueOf((COORDINATE_FACTOR * playerCoordinate) + markerOffset)
                .setScale(5, RoundingMode.HALF_UP).floatValue();
    }

    /**
     * Converts a map marker's x/z coordinate to the screen coordinate used when rendering it.
     *
     * @param mapCoordinate the map marker's x/z coordinate
     * @return the screen coordinate used when rendering the map marker
     */
    public static float toRenderCoordinate(float mapCoordinate) {
        return mapCoordinate / 2.0F + 64.0F;
    }

    /**
     * Increases the zoom level of the dungeon map by 0.5.
     */
    public static void increaseZoomByStep() {
        float zoomScaleFactor = MathUtils.denormalizeSliderValue(getMapZoom(), 0.5F, 5F, 0.1F);
        setMapZoom(zoomScaleFactor + 0.5F);
    }

    /**
     * Decreases the zoom level of the dungeon map by 0.5.
     */
    public static void decreaseZoomByStep() {
        float zoomScaleFactor = MathUtils.denormalizeSliderValue(main.getConfigValues().getMapZoom().getValue(), 0.5F, 5F, 0.1F);
        setMapZoom(zoomScaleFactor - 0.5F);
    }

    /**
     * Returns the map zoom factor from {@link codes.biscuit.skyblockaddons.config.ConfigValues#mapZoom}.
     *
     * @return the map zoom factor from {@link codes.biscuit.skyblockaddons.config.ConfigValues#mapZoom}
     */
    public static float getMapZoom() {
        return main.getConfigValues().getMapZoom().getValue();
    }

    /**
     * Sets the map zoom factor in {@link codes.biscuit.skyblockaddons.config.ConfigValues#mapZoom}.
     * The new value must be between 0.5f and 5f inclusive.
     *
     * @param value the new map zoom factor
     */
    public static void setMapZoom(float value) {
        main.getConfigValues().getMapZoom().setValue(main.getUtils().normalizeValueNoStep(value, 0.5F, 5F));
        main.getConfigValues().saveConfig();
    }
}