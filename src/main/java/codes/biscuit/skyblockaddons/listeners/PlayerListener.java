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
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.util.Collection;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.stream.Collectors;

public class PlayerListener {

    private boolean openGUI = false;
    private boolean bossWarning = false;
    private long lastBoss = -1;
    private int currentTick = 1;
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
    public void onRender(RenderGameOverlayEvent.Post e) {
        if (bossWarning) { // Render a title-like warning.
            Minecraft mc = Minecraft.getMinecraft();
            ScaledResolution scaledresolution = e.resolution;
            int i = scaledresolution.getScaledWidth();
            int j = scaledresolution.getScaledHeight();
            GlStateManager.pushMatrix();
            GlStateManager.translate((float) (i / 2), (float) (j / 2), 0.0F);
            GlStateManager.enableBlend();
            GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
            GlStateManager.pushMatrix();
            GlStateManager.scale(4.0F, 4.0F, 4.0F);
            String text;
            text = main.getConfigValues().getWarningColor().getChatFormatting() + "MagmaCube Boss!";
            mc.ingameGUI.getFontRenderer().drawString(text, (float) (-mc.ingameGUI.getFontRenderer().getStringWidth(text) / 2), -20.0F, 16777215, true);
            GlStateManager.popMatrix();
            GlStateManager.disableBlend();
            GlStateManager.popMatrix();
        }
    }

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
    public void onTickSound(TickEvent.ClientTickEvent e) {
        if (e.phase == TickEvent.Phase.START && !main.getConfigValues().getDisabledFeatures().contains(Feature.MAGMA_WARNING)) {
            Minecraft mc = Minecraft.getMinecraft();
            if (mc != null && mc.thePlayer != null) {
                if ((lastBoss == -1 || System.currentTimeMillis() - lastBoss > 1800000) && currentTick % 5 == 0) {
                    for (Entity entity : mc.theWorld.loadedEntityList) { // Loop through all the entities.
                        if (entity instanceof EntityMagmaCube) {
                            EntitySlime magma = (EntitySlime) entity;
                            int size = magma.getSlimeSize();
                            if (size > 10) { // Find a big magma boss
                                lastBoss = System.currentTimeMillis();
                                bossWarning = true; // Enable warning and disable again in four seconds.
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
                if (bossWarning && currentTick % 4 == 0) { // Play sound every 4 ticks or 1/5 second.
                    mc.thePlayer.playSound("random.orb", 1, 0.5F);
                }
            }
            currentTick++;
            if (currentTick > 20) {
                isOnIsland();
                currentTick = 1;
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
