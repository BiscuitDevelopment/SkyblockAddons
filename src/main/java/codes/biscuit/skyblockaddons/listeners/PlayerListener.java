package codes.biscuit.skyblockaddons.listeners;

import codes.biscuit.skyblockaddons.SkyblockAddons;
import codes.biscuit.skyblockaddons.gui.SkyblockAddonsGui;
import codes.biscuit.skyblockaddons.utils.Feature;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.monster.EntityMagmaCube;
import net.minecraft.entity.monster.EntitySlime;
import net.minecraft.scoreboard.Score;
import net.minecraft.scoreboard.ScoreObjective;
import net.minecraft.scoreboard.ScorePlayerTeam;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.awt.*;
import java.util.Collection;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static net.minecraft.client.gui.Gui.icons;

public class PlayerListener {

    private boolean predictMana = false;
    private int mana = 0;
    private int maxMana = 100;
    private boolean openGUI = false;
    private boolean bossWarning = false;
    private long lastBoss = -1;
    private int soundTick = 1;
    private int manaTick = 1;
//    private Map<Long, String> spawnLog = new HashMap<>();

    private SkyblockAddons main;

    public PlayerListener(SkyblockAddons main) {
        this.main = main;
    }

    @SubscribeEvent()
    public void onWorldJoin(EntityJoinWorldEvent e) {
        if (e.entity == Minecraft.getMinecraft().thePlayer) {
            bossWarning = false;
            lastBoss = -1;
        }
    }

    @SubscribeEvent()
    public void onRenderBossWarning(RenderGameOverlayEvent.Post e) {
        if (bossWarning && e.type == RenderGameOverlayEvent.ElementType.TEXT) { // Render a title-like warning.
            Minecraft mc = Minecraft.getMinecraft();
            ScaledResolution scaledresolution = e.resolution;
            int i = scaledresolution.getScaledWidth();
            int j = scaledresolution.getScaledHeight();
            GlStateManager.pushMatrix();
            GlStateManager.translate((float) (i / 2), (float) (j / 2), 0.0F);
//            GlStateManager.enableBlend();
//            GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
            GlStateManager.pushMatrix();
            GlStateManager.scale(4.0F, 4.0F, 4.0F);
            String text;
            text = main.getConfigValues().getWarningColor().getChatFormatting() + "MagmaCube Boss!";
            mc.ingameGUI.getFontRenderer().drawString(text, (float) (-mc.ingameGUI.getFontRenderer().getStringWidth(text) / 2), -20.0F, 16777215, true);
            GlStateManager.popMatrix();
//            GlStateManager.disableBlend();
            GlStateManager.popMatrix();
        }
    }

    @SubscribeEvent()
    public void onChatReceive(ClientChatReceivedEvent e) {
        if (e.type == 2 && main.getConfigValues().getManaBarType() != Feature.ManaBarType.OFF) { // Render a title-like warning.
            String message = e.message.getUnformattedText();
            if (message.contains("\u270E Mana")) {
                String[] manaSplit = message.split(Pattern.quote("\u270E Mana"));
                if (manaSplit.length > 1) {
                    if (manaSplit[0].contains(EnumChatFormatting.AQUA.toString())) {
                        message = manaSplit[0].split(Pattern.quote(EnumChatFormatting.AQUA.toString()))[1];
                        manaSplit = message.split(Pattern.quote("/"));
                        mana = Integer.parseInt(manaSplit[0]);
                        maxMana = Integer.parseInt(manaSplit[1]);
                        e.message = new ChatComponentText(e.message.getUnformattedText().split(EnumChatFormatting.AQUA.toString())[0].trim());
                        predictMana = false;
                        return;
                    }
                }
            }
            predictMana = true;
        }
    }

    @SubscribeEvent()
    public void onRenderManaBar(RenderGameOverlayEvent.Post e) {
        if (e.type == RenderGameOverlayEvent.ElementType.EXPERIENCE && main.getConfigValues().getManaBarType() != Feature.ManaBarType.OFF) {
            Minecraft mc = Minecraft.getMinecraft();
            ScaledResolution res = new ScaledResolution(mc);
            int width = res.getScaledWidth();
            int height = res.getScaledHeight();
            mc.getTextureManager().bindTexture(icons);
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
            GlStateManager.disableBlend();

            short barWidth = 92;
            int left = width / 2 - 91 + barWidth;

            if (main.getConfigValues().getManaBarType() == Feature.ManaBarType.BAR
                    || main.getConfigValues().getManaBarType() == Feature.ManaBarType.BAR_TEXT) {
                float manaFill = (float) mana / maxMana;
                if (manaFill > 1) manaFill = 1;
                int filled = (int) (manaFill * barWidth);
                int top = height - 59;
                mc.ingameGUI.drawTexturedModalRect(left, top, 10, 84, barWidth, 5);
                if (filled > 0) {
                    mc.ingameGUI.drawTexturedModalRect(left, top, 10, 89, filled, 5);
                }

                if (main.getConfigValues().getManaBarType() == Feature.ManaBarType.TEXT
                        || main.getConfigValues().getManaBarType() == Feature.ManaBarType.BAR_TEXT) {
                    int color = new Color(47, 71, 249).getRGB();
                    String text = mana + "/" + maxMana;
                    int x = ((width - mc.ingameGUI.getFontRenderer().getStringWidth(text)) / 2) + (barWidth / 2);
                    int y = height - 65;
                    mc.ingameGUI.getFontRenderer().drawString(text, x + 1, y, 0);
                    mc.ingameGUI.getFontRenderer().drawString(text, x - 1, y, 0);
                    mc.ingameGUI.getFontRenderer().drawString(text, x, y + 1, 0);
                    mc.ingameGUI.getFontRenderer().drawString(text, x, y - 1, 0);
                    mc.ingameGUI.getFontRenderer().drawString(text, x, y, color);
                    GlStateManager.enableBlend();
                    GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
                }
            }
        }
    }

//        Minecraft mc = Minecraft.getMinecraft();
//        ScaledResolution p_175176_1_ = new ScaledResolution(mc);
//        int p_175176_2_ = p_175176_1_.getScaledWidth() / 2 - 91;
//        mc.getTextureManager().bindTexture(icons);
//        int i = mc.thePlayer.xpBarCap();
//
//        if (i > 0)
//        {
//            int j = 182; //182
//            int k = (int)(mc.thePlayer.experience * (float)(j + 1));
//            int l = p_175176_1_.getScaledHeight() - 20 + 3; // p_175176_1_.getScaledHeight() - 32 + 3
//            mc.ingameGUI.drawTexturedModalRect(p_175176_2_, l, 0, 64, j, 5);
//
//            if (k > 0)
//            {
//                mc.ingameGUI.drawTexturedModalRect(p_175176_2_, l, 0, 69, k, 5);
//            }
//        }
//
//        mc.mcProfiler.endSection();
//
//        if (mc.thePlayer.experienceLevel > 0)
//        {
//            mc.mcProfiler.startSection("expLevel");
//            int k1 = 8453920;
//            String s = "" + mc.thePlayer.experienceLevel;
//            int l1 = (p_175176_1_.getScaledWidth() - mc.ingameGUI.getFontRenderer().getStringWidth(s)) / 2;
//            int i1 = p_175176_1_.getScaledHeight() - 31 - 4;
//            int j1 = 0;
//            mc.ingameGUI.getFontRenderer().drawString(s, l1 + 1, i1, 0);
//            mc.ingameGUI.getFontRenderer().drawString(s, l1 - 1, i1, 0);
//            mc.ingameGUI.getFontRenderer().drawString(s, l1, i1 + 1, 0);
//            mc.ingameGUI.getFontRenderer().drawString(s, l1, i1 - 1, 0);
//            mc.ingameGUI.getFontRenderer().drawString(s, l1, i1, k1);
//            mc.mcProfiler.endSection();
//        }

    @SubscribeEvent()
    public void onInteract(PlayerInteractEvent e) {
        if (!main.getConfigValues().getDisabledFeatures().contains(Feature.DISABLE_EMBER_ROD)) {
            Minecraft mc = Minecraft.getMinecraft();
            if (e.entityPlayer == mc.thePlayer && e.entityPlayer.getHeldItem() != null) {
                if (mc.thePlayer.getHeldItem().getItem().getRegistryName().equals("minecraft:blaze_rod") && mc.thePlayer.getHeldItem().isItemEnchanted()) {
                    if (isOnIsland()) {
                        e.setCanceled(true);
                    }
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
            } else if (manaTick > 20) {
                manaTick = 1;
            }
        }
    }


    @SubscribeEvent()
    public void onTickSound(TickEvent.ClientTickEvent e) {
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
                isOnIsland();
                soundTick = 1;
            }
        }
    }

    @SubscribeEvent()
    public void onRender(TickEvent.RenderTickEvent e) {
        if (isOpenGUI()) {
            Minecraft.getMinecraft().displayGuiScreen(new SkyblockAddonsGui(main));
            setOpenGUI(false);
        }
    }

    private boolean isOnIsland() { // Most of this is replicated from the scoreboard rendering code so not many comments here xD
        Minecraft mc = Minecraft.getMinecraft();
        if (mc != null && mc.theWorld != null) {
            Scoreboard scoreboard = mc.theWorld.getScoreboard();
            ScoreObjective scoreobjective = null;
            ScorePlayerTeam scoreplayerteam = scoreboard.getPlayersTeam(mc.thePlayer.getName());
            if (scoreplayerteam != null) {
                int randomNumber = scoreplayerteam.getChatFormat().getColorIndex();
                if (randomNumber >= 0) {
                    scoreobjective = scoreboard.getObjectiveInDisplaySlot(3 + randomNumber);
                }
            }
            ScoreObjective scoreobjective1 = scoreobjective != null ? scoreobjective : scoreboard.getObjectiveInDisplaySlot(1);
            if (scoreobjective1 != null) {
                Collection<Score> collection = scoreboard.getSortedScores(scoreobjective1);
                List<Score> list = Lists.newArrayList(collection.stream().filter(score -> score.getPlayerName() != null && !score.getPlayerName().startsWith("#")).collect(Collectors.toList()));
                if (list.size() > 15) {
                    collection = Lists.newArrayList(Iterables.skip(list, collection.size() - 15));
                } else {
                    collection = list;
                }
                for (Score score1 : collection) {
                    ScorePlayerTeam scoreplayerteam1 = scoreboard.getPlayersTeam(score1.getPlayerName());
                    String s1 = ScorePlayerTeam.formatPlayerName(scoreplayerteam1, score1.getPlayerName());
                    if (s1.equals(" \u00A77\u23E3 \u00A7aYour Isla\uD83C\uDFC0\u00A7and")) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public boolean isBossWarning() {
        return bossWarning;
    }

    //    private void logServer(Minecraft mc) {
//        if (mc.ingameGUI.getTabList().header != null) {
//            List<IChatComponent> siblings = mc.ingameGUI.getTabList().header.getSiblings(); // Bring back AT if doing this
//            if (siblings.size() > 2) {
//                String dateAndServer = siblings.get(siblings.size() - 3).getUnformattedText();
//                spawnLog.put(System.currentTimeMillis(), dateAndServer.split(Pattern.quote("  "))[1]);
//            }
//        }
//    }

    public void setOpenGUI(boolean openGUI) {
        this.openGUI = openGUI;
    }

    private boolean isOpenGUI() {
        return openGUI;
    }
}
