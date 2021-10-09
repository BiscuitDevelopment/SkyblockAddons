package codes.biscuit.skyblockaddons.gui;

import codes.biscuit.skyblockaddons.SkyblockAddons;
import codes.biscuit.skyblockaddons.core.Feature;
import codes.biscuit.skyblockaddons.core.Translations;
import codes.biscuit.skyblockaddons.gui.buttons.ButtonToggleNew;
import codes.biscuit.skyblockaddons.gui.buttons.IslandButton;
import codes.biscuit.skyblockaddons.gui.buttons.IslandMarkerButton;
import codes.biscuit.skyblockaddons.misc.scheduler.SkyblockRunnable;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.gui.inventory.GuiChest;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.texture.TextureUtil;
import net.minecraft.init.Items;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.EnumMap;
import java.util.Locale;
import java.util.Map;

public class IslandWarpGui extends GuiScreen {

    @Getter @Setter private static Marker doubleWarpMarker;

    private static int TOTAL_WIDTH;
    private static int TOTAL_HEIGHT;

    public static float SHIFT_LEFT;
    public static float SHIFT_TOP;

    @Getter
    private final Map<IslandWarpGui.Marker, IslandWarpGui.UnlockedStatus> markers;

    private Marker selectedMarker;
    private boolean guiIsActualWarpMenu = false;
    private boolean foundAdvancedWarpToggle = false;

    public static float ISLAND_SCALE;

    public IslandWarpGui() {
        super();

        Map<Marker, UnlockedStatus> markers = new EnumMap<>(Marker.class);
        for (Marker marker : Marker.values()) {
            markers.put(marker, UnlockedStatus.UNLOCKED);
        }
        this.markers = markers;
    }

    public IslandWarpGui(Map<Marker, UnlockedStatus> markers) {
        super();

        this.markers = markers;
        this.guiIsActualWarpMenu = true;
    }

    @Override
    public void initGui() {
        SkyblockAddons main = SkyblockAddons.getInstance();

        Map<Island, UnlockedStatus> islands = new EnumMap<>(Island.class);

        for (Map.Entry<Marker, UnlockedStatus> marker : markers.entrySet()) {
            Island island = marker.getKey().getIsland();
            UnlockedStatus currentStatus = islands.get(island);
            UnlockedStatus newStatus = marker.getValue();

            if (currentStatus == null || newStatus.ordinal() > currentStatus.ordinal()) {
                islands.put(island, newStatus);
            }
        }

        for (Map.Entry<Island, UnlockedStatus> island : islands.entrySet()) {
            this.buttonList.add(new IslandButton(island.getKey(), island.getValue(), markers));
        }

        int screenWidth = mc.displayWidth;
        int screenHeight = mc.displayHeight;

        ISLAND_SCALE = 0.7F/1080*screenHeight;

        float scale = ISLAND_SCALE;
        float totalWidth = TOTAL_WIDTH*scale;
        float totalHeight = TOTAL_HEIGHT*scale;
        SHIFT_LEFT = (screenWidth/2F-totalWidth/2F)/scale;
        SHIFT_TOP = (screenHeight/2F-totalHeight/2F)/scale;

        int x = Math.round(screenWidth/ISLAND_SCALE-SHIFT_LEFT-475);
        int y = Math.round(screenHeight/ISLAND_SCALE-SHIFT_TOP);

        if (guiIsActualWarpMenu) {
            this.buttonList.add(new ButtonToggleNew(x, y - 30 - 60 * 3, 50,
                    () -> {
                        // Finds the advanced mode toggle button to see if it's enabled or not.
                        GuiScreen guiScreen = Minecraft.getMinecraft().currentScreen;
                        if (guiScreen instanceof GuiChest) {
                            GuiChest gui = (GuiChest)guiScreen;

                            Slot toggleAdvancedModeSlot = gui.inventorySlots.getSlot(51);
                            if (toggleAdvancedModeSlot != null && toggleAdvancedModeSlot.getHasStack()) {
                                ItemStack toggleAdvancedModeItem = toggleAdvancedModeSlot.getStack();

                                if (Items.dye == toggleAdvancedModeItem.getItem()) {
                                    int damage = toggleAdvancedModeItem.getItemDamage();
                                    if (damage == 10) { // Lime Dye
                                        foundAdvancedWarpToggle = true;

                                        return true;
                                    } else if (damage == 8) { // Grey Dye
                                        foundAdvancedWarpToggle = true;

                                        return false;
                                    }
                                }
                            }
                        }
                        return false;
                    },
                    () -> {
                        if (!foundAdvancedWarpToggle) return;

                        // This will click the advanced mode button for you.
                        GuiScreen guiScreen = Minecraft.getMinecraft().currentScreen;
                        if (guiScreen instanceof GuiChest) {
                            GuiChest gui = (GuiChest) guiScreen;
                            this.mc.playerController.windowClick(gui.inventorySlots.windowId, 51, 0, 0, this.mc.thePlayer);
                        }
                    }));
            this.buttonList.add(new ButtonToggleNew(x, y - 30 - 60 * 2, 50,
                    () -> main.getConfigValues().isEnabled(Feature.FANCY_WARP_MENU),
                    () -> {
                        if (main.getConfigValues().getDisabledFeatures().contains(Feature.FANCY_WARP_MENU)) {
                            main.getConfigValues().getDisabledFeatures().remove(Feature.FANCY_WARP_MENU);
                        } else {
                            main.getConfigValues().getDisabledFeatures().add(Feature.FANCY_WARP_MENU);
                        }
                    }));
        }
        this.buttonList.add(new ButtonToggleNew(x, y - 30 - 60, 50,
                () -> main.getConfigValues().isEnabled(Feature.DOUBLE_WARP),
                () -> {
                    if (main.getConfigValues().getDisabledFeatures().contains(Feature.DOUBLE_WARP)) {
                        main.getConfigValues().getDisabledFeatures().remove(Feature.DOUBLE_WARP);
                    } else {
                        main.getConfigValues().getDisabledFeatures().add(Feature.DOUBLE_WARP);
                    }
                }));
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        ScaledResolution sr = new ScaledResolution(mc);
        int guiScale = sr.getScaleFactor();

        int startColor = new Color(0,0, 0, Math.round(255/3F)).getRGB();
        int endColor = new Color(0,0, 0, Math.round(255/2F)).getRGB();
        drawGradientRect(0, 0, sr.getScaledWidth(), sr.getScaledHeight(), startColor, endColor);

        drawCenteredString(mc.fontRendererObj, Translations.getMessage("warpMenu.click"), sr.getScaledWidth()/2, 10, 0xFFFFFFFF);
        drawCenteredString(mc.fontRendererObj, Translations.getMessage("warpMenu.mustUnlock"), sr.getScaledWidth()/2, 20, 0xFFFFFFFF);

        GlStateManager.pushMatrix();
        ISLAND_SCALE = 0.7F/1080*mc.displayHeight;
        float scale = ISLAND_SCALE;
        GlStateManager.scale(1F/guiScale, 1F/guiScale, 1);
        GlStateManager.scale(scale, scale, 1);

        float totalWidth = TOTAL_WIDTH*scale;
        float totalHeight = TOTAL_HEIGHT*scale;

        SHIFT_LEFT = (mc.displayWidth/2F-totalWidth/2F)/scale;
        SHIFT_TOP = (mc.displayHeight/2F-totalHeight/2F)/scale;
        GlStateManager.translate(SHIFT_LEFT, SHIFT_TOP, 0);

        GlStateManager.enableAlpha();
        GlStateManager.enableBlend();

        IslandButton lastHoveredButton = null;

        for (GuiButton button : buttonList) {
            if (button instanceof IslandButton) {
                IslandButton islandButton = (IslandButton)button;

                // Call this just so it calculates the hover, don't actually draw.
                islandButton.drawButton(mc, mouseX, mouseY, false);

                if (islandButton.isHovering()) {
                    if (lastHoveredButton != null) {
                        lastHoveredButton.setDisableHover(true);
                    }
                    lastHoveredButton = islandButton;
                }
            }
        }

        for (GuiButton guiButton : this.buttonList) {
            guiButton.drawButton(this.mc, mouseX, mouseY);
        }


        int x = Math.round(mc.displayWidth/ISLAND_SCALE-SHIFT_LEFT-500);
        int y = Math.round(mc.displayHeight/ISLAND_SCALE-SHIFT_TOP);
        GlStateManager.pushMatrix();
        float textScale = 3F;
        GlStateManager.scale(textScale, textScale, 1);
        if (guiIsActualWarpMenu) {
            mc.fontRendererObj.drawStringWithShadow(Feature.WARP_ADVANCED_MODE.getMessage(), x / textScale + 50, (y - 30 - 60 * 3) / textScale + 5, 0xFFFFFFFF);
            mc.fontRendererObj.drawStringWithShadow(Feature.FANCY_WARP_MENU.getMessage(), x / textScale + 50, (y - 30 - 60 * 2) / textScale + 5, 0xFFFFFFFF);
        }
        mc.fontRendererObj.drawStringWithShadow(Feature.DOUBLE_WARP.getMessage(), x / textScale + 50, (y - 30 - 60) / textScale + 5, 0xFFFFFFFF);
        GlStateManager.popMatrix();

        GlStateManager.popMatrix();

        detectClosestMarker(mouseX, mouseY);
    }

    public static float IMAGE_SCALED_DOWN_FACTOR = 0.75F;

    @Override
    public void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        if (mouseButton == 0 && selectedMarker != null) {
            Minecraft.getMinecraft().displayGuiScreen(null);

            if (SkyblockAddons.getInstance().getConfigValues().isEnabled(Feature.DOUBLE_WARP)) {
                doubleWarpMarker = selectedMarker;

                // Remove the marker if it didn't trigger for some reason...
                SkyblockAddons.getInstance().getNewScheduler().scheduleDelayedTask(new SkyblockRunnable() {
                    @Override
                    public void run() {
                        if (doubleWarpMarker != null) {
                            doubleWarpMarker = null;
                        }
                    }
                }, 20);
            }
            if (selectedMarker != Marker.DWARVEN_FORGE) {
                Minecraft.getMinecraft().thePlayer.sendChatMessage("/warp " + selectedMarker.getWarpName());
            } else {
                // Weirdly, this command is /warpforge instead of /warp forge
                Minecraft.getMinecraft().thePlayer.sendChatMessage("/warp" + selectedMarker.getWarpName());
            }

        }

        int minecraftScale = new ScaledResolution(mc).getScaleFactor();
        float islandGuiScale = ISLAND_SCALE;

        mouseX *= minecraftScale;
        mouseY *= minecraftScale;

        mouseX /= islandGuiScale;
        mouseY /= islandGuiScale;

        mouseX -= IslandWarpGui.SHIFT_LEFT;
        mouseY -= IslandWarpGui.SHIFT_TOP;

        super.mouseClicked(mouseX, mouseY, mouseButton);
    }


    public void detectClosestMarker(int mouseX, int mouseY) {
        int minecraftScale = new ScaledResolution(mc).getScaleFactor();
        float islandGuiScale = ISLAND_SCALE;

        mouseX *= minecraftScale;
        mouseY *= minecraftScale;

        mouseX /= islandGuiScale;
        mouseY /= islandGuiScale;

        mouseX -= IslandWarpGui.SHIFT_LEFT;
        mouseY -= IslandWarpGui.SHIFT_TOP;

        IslandWarpGui.Marker hoveredMarker = null;
        double markerDistance = IslandMarkerButton.MAX_SELECT_RADIUS + 1;

        for (GuiButton button : this.buttonList) {
            if (button instanceof IslandButton) {
                IslandButton islandButton = (IslandButton) button;

                for (IslandMarkerButton marker : islandButton.getMarkerButtons()) {
                    double distance = marker.getDistance(mouseX, mouseY);

                    if (distance != -1 && distance < markerDistance) {
                        hoveredMarker = marker.getMarker();
                        markerDistance = distance;
                    }
                }
            }
        }

        selectedMarker = hoveredMarker;

//        if (hoveredMarker != null) System.out.println(hoveredMarker.getLabel()+" "+markerDistance);
    }

    @Getter
    public enum Island {
        THE_END("The End", 290, -10),
        BLAZING_FORTRESS("Blazing Fortress", 900, -50),
        THE_PARK("The Park", 103, 370),
        SPIDERS_DEN("Spider's Den", 500, 420),
        DEEP_CAVERNS("Deep Caverns", 1400, 200),
        GOLD_MINE("Gold Mine", 1130, 475),
        MUSHROOM_DESERT("Mushroom Desert", 1470, 475),
        THE_BARN("The Barn", 1125, 800),
        HUB("Hub", 300, 724),
        PRIVATE_ISLAND("Private Island", 275, 1122),
        DUNGEON_HUB("Dungeon Hub", 1500, 1050);

        private final String label;
        private final int x;
        private final int y;
        private int w;
        private int h;

        private final ResourceLocation resourceLocation;
        private BufferedImage bufferedImage;

        Island(String label, int x, int y) {
            this.label = label;
            this.x = x;
            this.y = y;

            this.resourceLocation = new ResourceLocation("skyblockaddons", "islands/" + this.name().toLowerCase(Locale.US).replace("_", "") + ".png");
            try {
                bufferedImage = TextureUtil.readBufferedImage(Minecraft.getMinecraft().getResourceManager().getResource(this.resourceLocation).getInputStream());
                this.w = bufferedImage.getWidth();
                this.h = bufferedImage.getHeight();

                if (label.equals("The End")) {
                    IslandWarpGui.IMAGE_SCALED_DOWN_FACTOR = this.w/573F; // The original end HD texture is 573 pixels wide.

                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }

            this.w /= IMAGE_SCALED_DOWN_FACTOR;
            this.h /= IMAGE_SCALED_DOWN_FACTOR;

            if (this.y + this.h > TOTAL_HEIGHT) {
                TOTAL_HEIGHT = this.y + this.h;
            }
            if (this.x + this.w > TOTAL_WIDTH) {
                TOTAL_WIDTH = this.x + this.w;
            }
        }
    }

    //TODO: Maybe change these to load from a file at some point
    @Getter
    public enum Marker {
        PRIVATE_ISLAND("home", Translations.getMessage("warpMenu.home"), Island.PRIVATE_ISLAND, true, 72, 90),

        HUB("hub", Translations.getMessage("warpMenu.spawn"), Island.HUB, true, 600, 200),
        CASTLE("castle", "Castle", Island.HUB, 130, 80),
        DARK_AUCTION("da", "Sirius Shack", Island.HUB, 385, 415),
        CRYPT("crypt", "Crypts", Island.HUB, 550, 100),
        DUNGEON_HUB("dungeon_hub", "Dungeon Hub", Island.HUB, false, 400, 175),

        SPIDERS_DEN("spider", Translations.getMessage("warpMenu.spawn"), Island.SPIDERS_DEN, true, 345, 240),
        SPIDERS_DEN_NEST("nest", "Top of Nest", Island.SPIDERS_DEN, 450, 30),

        THE_PARK("park", Translations.getMessage("warpMenu.spawn"), Island.THE_PARK, true, 263, 308),
        HOWLING_CAVE("howl", "Howling Cave", Island.THE_PARK, 254, 202),
        THE_PARK_JUNGLE("jungle", "Jungle", Island.THE_PARK, 194, 82),

        THE_END("end", Translations.getMessage("warpMenu.spawn"), Island.THE_END, true, 440, 291),
        DRAGONS_NEST("drag", "Dragon's Nest", Island.THE_END, 260, 248),
        VOID_SEPULTURE("void", "Void Sepulture", Island.THE_END, true, 370, 227),

        BLAZING_FORTRESS("nether", Translations.getMessage("warpMenu.spawn"), Island.BLAZING_FORTRESS, true, 80, 350),
        BLAZING_FORTRESS_MAGMA("magma", "Magma Cube Arena", Island.BLAZING_FORTRESS, 350, 200),

        THE_BARN("barn", Translations.getMessage("warpMenu.spawn"), Island.THE_BARN, true, 140, 150),
        MUSHROOM_DESERT("desert", Translations.getMessage("warpMenu.spawn"), Island.MUSHROOM_DESERT, true, 210, 295),

        GOLD_MINE("gold", Translations.getMessage("warpMenu.spawn"), Island.GOLD_MINE, true, 86, 259),
        DEEP_CAVERNS("deep", Translations.getMessage("warpMenu.spawn"), Island.DEEP_CAVERNS, true, 97, 213),
        DWARVEN_MINES("mines", "Dwarven Mines", Island.DEEP_CAVERNS, false, 150, 320),
        DWARVEN_FORGE("forge", "Forge", Island.DEEP_CAVERNS, false, 220, 350),

        DUNGEON_HUB_ISLAND("dungeon_hub", Translations.getMessage("warpMenu.spawn"), Island.DUNGEON_HUB, false, 35, 80),
        ;

        private final String warpName;
        private final String label;
        private final Island island;
        private final boolean advanced;
        private final int x;
        private final int y;

        Marker(String warpName, String label, Island island, int x, int y) {
            this(warpName, label, island, false, x, y);
        }

        Marker(String warpName, String label, Island island, boolean advanced, int x, int y) {
            this.warpName = warpName;
            this.label = label;
            this.island = island;
            this.x = x;
            this.y = y;
            this.advanced = advanced;
        }

        public static Marker fromWarpName(String warpName) {
            for (Marker marker : values()) {
                if (marker.warpName.equals(warpName)) {
                    return marker;
                }
            }

            return null;
        }
    }

    @Getter
    public enum UnlockedStatus {
        UNKNOWN(Translations.getMessage("warpMenu.unknown")),
        NOT_UNLOCKED(Translations.getMessage("warpMenu.notUnlocked")),
        IN_COMBAT(Translations.getMessage("warpMenu.inCombat")),
        UNLOCKED(null),
        ;

        private final String message;

        UnlockedStatus(String message) {
            this.message = message;
        }
    }
}
