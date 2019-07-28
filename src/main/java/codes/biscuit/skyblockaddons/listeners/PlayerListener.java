package codes.biscuit.skyblockaddons.listeners;

import codes.biscuit.skyblockaddons.SkyblockAddons;
import codes.biscuit.skyblockaddons.gui.ButtonSlider;
import codes.biscuit.skyblockaddons.gui.LocationEditGui;
import codes.biscuit.skyblockaddons.gui.SettingsGui;
import codes.biscuit.skyblockaddons.gui.SkyblockAddonsGui;
import codes.biscuit.skyblockaddons.utils.ConfigValues;
import codes.biscuit.skyblockaddons.utils.CoordsPair;
import codes.biscuit.skyblockaddons.utils.Feature;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityArmorStand;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.monster.EntityMagmaCube;
import net.minecraft.entity.monster.EntitySlime;
import net.minecraft.entity.monster.EntityZombie;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.GuiIngameForge;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.lwjgl.opengl.GL11;

import java.math.BigDecimal;
import java.util.Timer;
import java.util.TimerTask;
import java.util.regex.Pattern;

import static net.minecraft.client.gui.Gui.icons;

public class PlayerListener {

    public final static ItemStack BONE = new ItemStack(Item.getItemById(352));
    public final static ResourceLocation MANA_BARS = new ResourceLocation("skyblockaddons", "manabars.png");

    private boolean sentUpdate = false;
    private boolean predictMana = false;
    private long lastWorldJoin = -1;
    private boolean fullInventoryWarning = false;
    private boolean bossWarning = false;
    private long lastBoss = -1;
    private int soundTick = 1;
    private int manaTick = 1;
    private long lastMinionSound = -1;

    private int defense = 0;
    private int health = 100;
    private int maxHealth = 100;
    private int mana = 0;
    private int maxMana = 100;

    private Feature.Accuracy magmaTimerAccuracy = null;
    private long magmaTime = 7200;

    private boolean openMainGUI = false;
    private boolean openSettingsGUI = false;

    private SkyblockAddons main;

    public PlayerListener(SkyblockAddons main) {
        this.main = main;
    }

    @SubscribeEvent()
    public void onWorldJoin(EntityJoinWorldEvent e) {
        if (e.entity == Minecraft.getMinecraft().thePlayer) {
            lastWorldJoin = System.currentTimeMillis();
            bossWarning = false;
            lastBoss = -1;
            soundTick = 1;
            manaTick = 1;
        }
    }

    @SubscribeEvent()
    public void onChatReceive(ClientChatReceivedEvent e) {
        String message = e.message.getUnformattedText();
        if (e.type == 2) {
            if (message.endsWith("\u270E Mana\u00A7r")) {
                try {
                    String[] splitMessage = message.split(Pattern.quote("     "));
                    String healthPart = splitMessage[0];
                    String defencePart = null;
                    String manaPart;
                    if (splitMessage.length > 2) {
                        defencePart = splitMessage[1];
                        manaPart = splitMessage[2];
                    } else {
                        manaPart = splitMessage[1];
                    }
                    String[] healthSplit = main.getUtils().getNumbersOnly(healthPart).split(Pattern.quote("/"));
                    health = Integer.parseInt(healthSplit[0]);
                    maxHealth = Integer.parseInt(healthSplit[1]);
                    if (defencePart != null) {
                        defense = Integer.valueOf(main.getUtils().getNumbersOnly(defencePart).trim());
                    }
                    String[] manaSplit = main.getUtils().getNumbersOnly(manaPart).split(Pattern.quote("/"));
                    mana = Integer.parseInt(manaSplit[0]);
                    maxMana = Integer.parseInt(manaSplit[1].trim());
                    predictMana = false;
                    StringBuilder newMessage = new StringBuilder();
                    boolean showHealth = main.getConfigValues().getHealthBarType() == Feature.BarType.OFF;
                    boolean showDefence = defencePart != null && main.getConfigValues().getDefenceIconType() == Feature.IconType.OFF;
                    boolean showMana = main.getConfigValues().getManaBarType() == Feature.BarType.OFF;
                    if (showHealth) {
                        newMessage.append(healthPart);
                    }
                    if (showDefence) {
                        if (showHealth) newMessage.append("     ");
                        newMessage.append(defencePart);
                    }
                    if (showMana) {
                        if (showHealth || showDefence) newMessage.append("     ");
                        newMessage.append(manaPart);
                    }
                    e.message = new ChatComponentText(newMessage.toString());
                    return;
                } catch (Exception ignored) {}
            }
            predictMana = true;
        } else {
            if (predictMana && message.startsWith("Used ") && message.endsWith("Mana)")) {
                int mana = Integer.parseInt(message.split(Pattern.quote("! ("))[1].split(Pattern.quote(" Mana)"))[0]);
                this.mana -= mana;
            }
        }
    }

    @SubscribeEvent()
    public void onRenderRegular(RenderGameOverlayEvent.Post e) {
        if (!main.isUsingLabymod() && main.getUtils().isOnSkyblock()) {
            if (e.type == RenderGameOverlayEvent.ElementType.EXPERIENCE) {
                renderOverlays(e.resolution);
            } else if (e.type == RenderGameOverlayEvent.ElementType.TEXT) {
                renderWarnings(e.resolution);
            }
        }
    }

    @SubscribeEvent()
    public void onRenderLabyMod(RenderGameOverlayEvent e) {
        if (main.isUsingLabymod() && main.getUtils().isOnSkyblock()) {
            renderOverlays(e.resolution);
            renderWarnings(e.resolution);
        }
    }

    private void renderWarnings(ScaledResolution scaledResolution) {
        Minecraft mc = Minecraft.getMinecraft();
        int i = scaledResolution.getScaledWidth();
        if (bossWarning) {
            int j = scaledResolution.getScaledHeight();
            GlStateManager.pushMatrix();
            GlStateManager.translate((float) (i / 2), (float) (j / 2), 0.0F);
//            GlStateManager.enableBlend();
//            GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
            GlStateManager.pushMatrix();
            GlStateManager.scale(4.0F, 4.0F, 4.0F);
            String text;
            text = main.getConfigValues().getColor(Feature.WARNING_COLOR).getChatFormatting() + main.getConfigValues().getMessage(ConfigValues.Message.MESSAGE_MAGMA_BOSS_WARNING);
            mc.ingameGUI.getFontRenderer().drawString(text, (float) (-mc.ingameGUI.getFontRenderer().getStringWidth(text) / 2), -20.0F, 16777215, true);
            GlStateManager.popMatrix();
//            GlStateManager.disableBlend();
            GlStateManager.popMatrix();
        }
        if (fullInventoryWarning && !main.getConfigValues().getDisabledFeatures().contains(Feature.FULL_INVENTORY_WARNING)) {
            int j = scaledResolution.getScaledHeight();
            GlStateManager.pushMatrix();
            GlStateManager.translate((float) (i / 2), (float) (j / 2), 0.0F);
            GlStateManager.pushMatrix();
            GlStateManager.scale(4.0F, 4.0F, 4.0F);
            String text;
            text = main.getConfigValues().getColor(Feature.WARNING_COLOR).getChatFormatting() + main.getConfigValues().getMessage(ConfigValues.Message.MESSAGE_FULL_INVENTORY);
            mc.ingameGUI.getFontRenderer().drawString(text, (float) (-mc.ingameGUI.getFontRenderer().getStringWidth(text) / 2), -20.0F, 16777215, true);
            GlStateManager.popMatrix();
            GlStateManager.popMatrix();
        }
        if (!main.getConfigValues().getDisabledFeatures().contains(Feature.MAGMA_BOSS_BAR)) {
            for (Entity entity : mc.theWorld.loadedEntityList) {
                if (entity instanceof EntityArmorStand) {
                    String name = entity.getDisplayName().getFormattedText();
                    if (name.contains("Magma Cube Boss ")) {
                        name = name.split(Pattern.quote("Magma Cube Boss "))[1];
                        mc.getTextureManager().bindTexture(icons);
                        GlStateManager.tryBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, 1, 0);
                        GlStateManager.enableBlend();
                        int j = 182;
                        int k = i / 2 - j / 2;
                        int health = 1;
                        int l = (int) (health * (float) (j + 1));
                        int i1 = 12;
                        mc.ingameGUI.drawTexturedModalRect(k, i1, 0, 74, j, 5);
                        mc.ingameGUI.drawTexturedModalRect(k, i1, 0, 74, j, 5);

                        if (l > 0) {
                            mc.ingameGUI.drawTexturedModalRect(k, i1, 0, 79, l, 5);
                        }
                        mc.ingameGUI.getFontRenderer().drawStringWithShadow(name, (float) (i / 2 - mc.ingameGUI.getFontRenderer().getStringWidth(name) / 2), (float) (i1 - 10), 16777215);
                        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
                        mc.getTextureManager().bindTexture(icons);
                        GlStateManager.disableBlend();
                    }
                }
            }
        }
    }

    private void renderOverlays(ScaledResolution sr) {
        Minecraft mc = Minecraft.getMinecraft();
        float scale = main.getUtils().denormalizeValue(main.getConfigValues().getGuiScale(), ButtonSlider.VALUE_MIN, ButtonSlider.VALUE_MAX, ButtonSlider.VALUE_STEP);
        float scaleMultiplier = 1F/scale;
        GlStateManager.pushMatrix();
        GlStateManager.scale(scale, scale, 1);
        if (main.getConfigValues().getManaBarType() != Feature.BarType.OFF && !(mc.currentScreen instanceof LocationEditGui)) {
            mc.getTextureManager().bindTexture(MANA_BARS);
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
            GlStateManager.disableBlend();

            if (main.getConfigValues().getManaBarType() == Feature.BarType.BAR
                    || main.getConfigValues().getManaBarType() == Feature.BarType.BAR_TEXT) {
                drawBar(Feature.MANA_BAR, scaleMultiplier, mc, sr, Feature.MANA_BAR_COLOR);
            }
            if (main.getConfigValues().getHealthBarType() == Feature.BarType.BAR
                    || main.getConfigValues().getHealthBarType() == Feature.BarType.BAR_TEXT) {
                drawBar(Feature.HEALTH_BAR, scaleMultiplier, mc, sr, Feature.HEALTH_BAR);
            }
            if (main.getConfigValues().getManaBarType() == Feature.BarType.TEXT
                    || main.getConfigValues().getManaBarType() == Feature.BarType.BAR_TEXT) {
                drawText(Feature.MANA_TEXT, scaleMultiplier, mc, sr, Feature.MANA_TEXT_COLOR);
//                int color = main.getConfigValues().getColor(Feature.MANA_TEXT_COLOR).getColor(255);
//                String text = mana + "/" + maxMana;
//                CoordsPair coordsPair = main.getConfigValues().getCoords(Feature.MANA_TEXT);
//                int x = (int) (coordsPair.getX() * sr.getScaledWidth()) + 60 - mc.ingameGUI.getFontRenderer().getStringWidth(text) / 2;
//                int y = (int) (coordsPair.getY() * sr.getScaledHeight()) + 4;
//                x+=25;
//                y+=10;
//                mc.ingameGUI.getFontRenderer().drawString(text, (int)(x*scaleMultiplier)-60+1, (int)(y*scaleMultiplier)-10, 0);
//                mc.ingameGUI.getFontRenderer().drawString(text, (int)(x*scaleMultiplier)-60-1, (int)(y*scaleMultiplier)-10, 0);
//                mc.ingameGUI.getFontRenderer().drawString(text, (int)(x*scaleMultiplier)-60, (int)(y*scaleMultiplier)+1-10, 0);
//                mc.ingameGUI.getFontRenderer().drawString(text, (int)(x*scaleMultiplier)-60, (int)(y*scaleMultiplier)-1-10, 0);
//                mc.ingameGUI.getFontRenderer().drawString(text, (int)(x*scaleMultiplier)-60, (int)(y*scaleMultiplier)-10, color);
//                GlStateManager.enableBlend();
//                GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
            }
        }
        if ((!main.getConfigValues().getDisabledFeatures().contains(Feature.SKELETON_BAR))
                && !(mc.currentScreen instanceof LocationEditGui) && main.getUtils().isWearingSkeletonHelmet()) {
            CoordsPair coordsPair = main.getConfigValues().getCoords(Feature.SKELETON_BAR);
            int width = (int)(coordsPair.getX()*sr.getScaledWidth());
            int height = (int)(coordsPair.getY()*sr.getScaledHeight());
            int bones = 0;
            for (Entity listEntity : mc.theWorld.loadedEntityList) {
                if (listEntity instanceof EntityItem &&
                        listEntity.ridingEntity instanceof EntityZombie && listEntity.ridingEntity.isInvisible() && listEntity.getDistanceToEntity(mc.thePlayer) <= 8) {
                    bones++;
                }
            }
            if (bones > 3) bones = 3;
            for (int boneCounter = 0; boneCounter < bones; boneCounter++) {
                mc.getRenderItem().renderItemIntoGUI(BONE, (int)((width+boneCounter*15*scale)*scaleMultiplier), (int)((height+2)*scaleMultiplier));
            }
        }
        GlStateManager.popMatrix();
    }

    private void drawBar(Feature feature, float scaleMultiplier, Minecraft mc, ScaledResolution sr, Feature colorFeature) {
        short barWidth = 92;
        float fill;
        if (feature == Feature.MANA_BAR) {
            fill = (float) mana / maxMana;
        } else {
            fill = (float) health / maxHealth;
        }
        if (fill > 1) fill = 1;
        float x;
        float y;
        if (feature == Feature.MANA_BAR) {
            CoordsPair coordsPair = main.getConfigValues().getCoords(Feature.MANA_BAR);
            x = coordsPair.getX();
            y = coordsPair.getY();
        } else {
            CoordsPair coordsPair = main.getConfigValues().getCoords(Feature.HEALTH_BAR);
            x = coordsPair.getX();
            y = coordsPair.getY();
        }
        int left = (int) (x * sr.getScaledWidth()) + 14;
        int filled = (int) (fill * barWidth);
        int top = (int) (y * sr.getScaledHeight()) + 10;
        int textureY = main.getConfigValues().getColor(colorFeature).ordinal()*10;
        mc.ingameGUI.drawTexturedModalRect(left*scaleMultiplier-60, top*scaleMultiplier-10, 0, textureY, barWidth, 5);
        if (filled > 0) {
            mc.ingameGUI.drawTexturedModalRect(left*scaleMultiplier-60, top*scaleMultiplier-10, 0, textureY+5, filled, 5);
        }
    }

    private void drawText(Feature feature, float scaleMultiplier, Minecraft mc, ScaledResolution sr, Feature colorFeature) {
        int color = main.getConfigValues().getColor(colorFeature).getColor(255);
        String text;
        float textX;
        float textY;
        if (feature == Feature.MANA_TEXT) {
            CoordsPair coordsPair = main.getConfigValues().getCoords(Feature.MANA_TEXT);
            textX = coordsPair.getX();
            textY = coordsPair.getY();
            text = mana + "/" + maxMana;
        } else if (feature == Feature.HEALTH_TEXT) {
            CoordsPair coordsPair = main.getConfigValues().getCoords(Feature.HEALTH_TEXT);
            textX = coordsPair.getX();
            textY = coordsPair.getY();
            text = health + "/" + maxHealth;
        } else if (feature == Feature.DEFENCE_TEXT) {
            CoordsPair coordsPair = main.getConfigValues().getCoords(Feature.DEFENCE_TEXT);
            textX = coordsPair.getX();
            textY = coordsPair.getY();
            text = String.valueOf(defense);
        } else {
            CoordsPair coordsPair = main.getConfigValues().getCoords(Feature.DEFENCE_PERCENTAGE);
            textX = coordsPair.getX();
            textY = coordsPair.getY();
            double doubleDefence = (double)defense;
            double percentage = ((doubleDefence/100)/((doubleDefence/100)+1))*100; //Formula taken from https://hypixel.net/threads/how-armor-works-and-the-diminishing-return-of-higher-defence.2178928/
            BigDecimal bigDecimal = new BigDecimal(percentage).setScale(1, BigDecimal.ROUND_HALF_UP);
            text = bigDecimal.toString()+"%";
        }
        int x = (int) (textX * sr.getScaledWidth()) + 60 - mc.ingameGUI.getFontRenderer().getStringWidth(text) / 2;
        int y = (int) (textY * sr.getScaledHeight()) + 4;
        x+=25;
        y+=10;
        mc.ingameGUI.getFontRenderer().drawString(text, (int)(x*scaleMultiplier)-60+1, (int)(y*scaleMultiplier)-10, 0);
        mc.ingameGUI.getFontRenderer().drawString(text, (int)(x*scaleMultiplier)-60-1, (int)(y*scaleMultiplier)-10, 0);
        mc.ingameGUI.getFontRenderer().drawString(text, (int)(x*scaleMultiplier)-60, (int)(y*scaleMultiplier)+1-10, 0);
        mc.ingameGUI.getFontRenderer().drawString(text, (int)(x*scaleMultiplier)-60, (int)(y*scaleMultiplier)-1-10, 0);
        mc.ingameGUI.getFontRenderer().drawString(text, (int)(x*scaleMultiplier)-60, (int)(y*scaleMultiplier)-10, color);
    }

    @SubscribeEvent()
    public void onRenderRemoveBars(RenderGameOverlayEvent.Pre e) {
        if (e.type == RenderGameOverlayEvent.ElementType.ALL) {
            if (main.getUtils().isOnSkyblock() && !main.getConfigValues().getDisabledFeatures().contains(Feature.HIDE_FOOD_ARMOR_BAR)) {
                GuiIngameForge.renderFood = false;
                GuiIngameForge.renderArmor = false;
            }
        }
    }

    @SubscribeEvent()
    public void onInteract(PlayerInteractEvent e) {
        if (!main.getConfigValues().getDisabledFeatures().contains(Feature.DISABLE_EMBER_ROD)) {
            Minecraft mc = Minecraft.getMinecraft();
            ItemStack heldItem = e.entityPlayer.getHeldItem();
            if (e.entityPlayer == mc.thePlayer && heldItem != null) {
                if (heldItem.getItem().equals(Items.blaze_rod) && heldItem.isItemEnchanted() && main.getUtils().getLocation() == Feature.Location.ISLAND) {
                    e.setCanceled(true);
                }
            }
        }
    }

    @SubscribeEvent()
    public void onTickMana(TickEvent.ClientTickEvent e) {
        if (e.phase == TickEvent.Phase.START) {
            manaTick++;
            if (manaTick == 20) {
                if (predictMana) {
                    mana += (maxMana/50);
                    if (mana>maxMana) mana = maxMana;
                }
            } else if (manaTick % 5 == 0) {
                Minecraft mc = Minecraft.getMinecraft();
                if (mc != null) {
                    EntityPlayerSP p = mc.thePlayer;
                    if (p != null) {
                        main.getUtils().checkIfInventoryIsFull(mc, p);
                        main.getUtils().checkIfWearingSkeletonHelmet(p);
                    }
                }
            } else if (manaTick > 20) {
                main.getUtils().checkGameAndLocation();
                Minecraft mc = Minecraft.getMinecraft();
                if (!sentUpdate && mc != null && mc.thePlayer != null && mc.theWorld != null) {
                    main.getUtils().checkUpdates();
                    sentUpdate = true;
                }
//                if (mc != null && mc.theWorld != null && mc.thePlayer != null) {
//                    for (Entity entity : mc.theWorld.loadedEntityList) {
//                        if (entity instanceof EntityOtherPlayerMP && entity.getDistanceToEntity(mc.thePlayer) < 5) {
//                            EntityOtherPlayerMP p = (EntityOtherPlayerMP)entity;
//                            System.out.println(p.posX);
//                            System.out.println(p.posY);
//                            System.out.println(p.posZ);
//                            boolean foundEntity = false;
//                            for (NetworkPlayerInfo networkPlayerInfo : mc.thePlayer.sendQueue.getPlayerInfoMap()) {
//                                if (networkPlayerInfo.getPlayerTeam() != null) { //networkPlayerInfo.getDisplayName().getUnformattedText().equals(entity.getName())
////                                    System.out.println(networkPlayerInfo.getPlayerTeam());
//                                    foundEntity = true;
//                                } else {
//                                    System.out.println("true");
//                                }
//                            }
//                            System.out.println(foundEntity);
//                        EntityOtherPlayerMP entityOtherPlayerMP = (EntityOtherPlayerMP)entity;
//                        GuiPlayerTabOverlay
//                        mc.theWorld.tab
//                        System.out.println(entityOtherPlayerMP.getna);
//                        }
//                    }
//                }
                manaTick = 1;
            }
        }
    }

    // Addition by Michael#3549
    @SubscribeEvent
    public void onEntityEvent(LivingEvent.LivingUpdateEvent e) {
        if (main.getUtils().getLocation() == Feature.Location.ISLAND && !main.getConfigValues().getDisabledFeatures().contains(Feature.MINION_STOP_WARNING)) {
            Entity entity = e.entity;
            if (entity instanceof EntityArmorStand && entity.hasCustomName()) {
                if (entity.getCustomNameTag().startsWith("\u00A7cI can\'t reach any ")) {
                    long now = System.currentTimeMillis();
                    if (now - lastMinionSound > 5000) {
                        lastMinionSound = now;
                        EntityPlayerSP p = Minecraft.getMinecraft().thePlayer;
                        p.playSound("random.orb", 1, 1);
                        String mobName = entity.getCustomNameTag().split(Pattern.quote("\u00A7cI can\'t reach any "))[1].toLowerCase();
                        if (mobName.lastIndexOf("s") == mobName.length() - 1) {
                            mobName = mobName.substring(0, mobName.length() - 1);
                        }
                        p.addChatMessage(new ChatComponentText(EnumChatFormatting.RED + "A " + mobName + " minion cannot reach and has stopped spawning!"));
                    }
                }
            }
        }
    }

    @SubscribeEvent()
    public void onTickMagmaBossChecker(TickEvent.ClientTickEvent e) {
        if (e.phase == TickEvent.Phase.START && !main.getConfigValues().getDisabledFeatures().contains(Feature.MAGMA_WARNING)) {
            Minecraft mc = Minecraft.getMinecraft();
            if (mc != null && mc.thePlayer != null) {
                if ((lastBoss == -1 || System.currentTimeMillis() - lastBoss > 1800000) && soundTick % 5 == 0) {
                    for (Entity entity : mc.theWorld.loadedEntityList) { // Loop through all the entities.
                        if (entity instanceof EntityMagmaCube) {
                            EntitySlime magma = (EntitySlime) entity;
                            int size = magma.getSlimeSize();
                            if (size > 10) { // Find a big magma boss
                                lastBoss = System.currentTimeMillis();
                                bossWarning = true; // Enable warning and disable again in four seconds.
                                soundTick = 16; // so the sound plays instantly
                                new Timer().schedule(new TimerTask() {
                                    @Override
                                    public void run() {
                                        bossWarning = false;
                                    }
                                }, main.getConfigValues().getWarningSeconds()*1000); // 4 second warning.
//                                logServer(mc);
                            }
                        }
                    }
                }
                if (bossWarning && soundTick % 4 == 0) { // Play sound every 4 ticks or 1/5 second.
                    mc.thePlayer.playSound("random.orb", 1, 0.5F);
                }
            }
            soundTick++;
            if (soundTick > 20) {
                soundTick = 1;
            }
        }
    }

    @SubscribeEvent()
    public void onRender(TickEvent.RenderTickEvent e) {
        if (openMainGUI) {
            Minecraft.getMinecraft().displayGuiScreen(new SkyblockAddonsGui(main));
            openMainGUI = false;
        } else if (openSettingsGUI) {
            Minecraft.getMinecraft().displayGuiScreen(new SettingsGui(main));
            openSettingsGUI = false;
        }
    }

    public boolean isBossWarning() {
        return bossWarning;
    }

//    @SubscribeEvent()
//    public void onPlaySound(PlaySoundEvent e) {
//        if (main.getUtils().getLocation() == Feature.Location.BLAZING_FORTRESS && e.sound.getSoundLocation().getResourcePath().equals("records.13")) {
////            magmaTime
//        }
//    }

    public void setOpenMainGUI(boolean openMainGUI) {
        this.openMainGUI = openMainGUI;
    }

    public void setOpenSettingsGUI(boolean openSettingsGUI) {
        this.openSettingsGUI = openSettingsGUI;
    }

    public void setFullInventoryWarning(boolean fullInventoryWarning) {
        this.fullInventoryWarning = fullInventoryWarning;
    }

    public boolean isFullInventoryWarning() {
        return fullInventoryWarning;
    }

    public long getLastWorldJoin() {
        return lastWorldJoin;
    }
}
