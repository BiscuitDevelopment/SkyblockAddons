package codes.biscuit.skyblockaddons.features.EntityOutlines;

import codes.biscuit.skyblockaddons.SkyblockAddons;
import codes.biscuit.skyblockaddons.core.Feature;
import codes.biscuit.skyblockaddons.core.Location;
import codes.biscuit.skyblockaddons.core.Translations;
import codes.biscuit.skyblockaddons.events.RenderEntityOutlineEvent;
import codes.biscuit.skyblockaddons.features.cooldowns.CooldownManager;
import codes.biscuit.skyblockaddons.gui.buttons.ButtonLocation;
import codes.biscuit.skyblockaddons.listeners.RenderListener;
import codes.biscuit.skyblockaddons.utils.ColorCode;
import codes.biscuit.skyblockaddons.utils.DrawUtils;
import codes.biscuit.skyblockaddons.utils.TextUtils;
import codes.biscuit.skyblockaddons.utils.Utils;
import lombok.Getter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityArmorStand;
import net.minecraft.entity.passive.*;
import net.minecraft.potion.Potion;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.client.event.RenderLivingEvent.Specials.Pre;
import net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;

import java.util.EnumSet;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FeatureTrackerQuest {

    private static final EnumSet<Location> mushroomIslandLocations = EnumSet.of(Location.MUSHROOM_DESERT, Location.TRAPPERS_DEN,
            Location.DESERT_SETTLEMENT, Location.OASIS, Location.GLOWING_MUSHROOM_CAVE, Location.MUSHROOM_GORGE,
            Location.SHEPHERDS_KEEP, Location.OVERGROWN_MUSHROOM_CAVE, Location.JAKES_HOUSE, Location.TREASURE_HUNTER_CAMP);

    private static final Pattern TRACKED_ANIMAL_NAME_PATTERN = Pattern.compile("\\[Lv[0-9]+] (?<rarity>[a-zA-Z]+) (?<animal>[a-zA-Z]+) .*❤");
    private static final Pattern TREVOR_FIND_ANIMAL_PATTERN = Pattern.compile("\\[NPC] Trevor The Trapper: You can find your [A-Z]+ animal near the [a-zA-Z ]+.");
    private static final Pattern ANIMAL_DIED_PATTERN = Pattern.compile("Your mob died randomly, you are rewarded [0-9]+ pelts?.");
    private static final Pattern ANIMAL_KILLED_PATTERN = Pattern.compile("Killing the animal rewarded you [0-9]+ pelts?.");

    private static final ResourceLocation TICKER_SYMBOL = new ResourceLocation("skyblockaddons", "tracker.png");
    private static boolean isTrackingAnimal = false;
    private static TrackedEntity entityToOutline = null;

    public FeatureTrackerQuest() {

    }

    /**
     * Draws cell-service-like bars to indicate the proximity to the tracked entity
     *
     * @param mc             the minecraft
     * @param scale          the button scale
     * @param buttonLocation the button location in gui location menu
     */
    // TODO: This should not be static after the feature refactor
    public static void drawTrackerLocationIndicator(Minecraft mc, float scale, ButtonLocation buttonLocation) {
        SkyblockAddons main = SkyblockAddons.getInstance();
        if (buttonLocation != null || isTrackingAnimal) {
            RenderListener listener = main.getRenderListener();
            float x = main.getConfigValues().getActualX(Feature.TREVOR_TRACKED_ENTITY_PROXIMITY_INDICATOR);
            float y = main.getConfigValues().getActualY(Feature.TREVOR_TRACKED_ENTITY_PROXIMITY_INDICATOR);

            int height = 9;
            int width = 3 * 11 + 9;

            x = listener.transformXY(x, width, scale);
            y = listener.transformXY(y, height, scale);

            if (buttonLocation != null) {
                buttonLocation.checkHoveredAndDrawBox(x, x + width, y, y + height, scale);
            }

            main.getUtils().enableStandardGLOptions();

            int maxTickers = 4;
            int fullTickers;
            if (buttonLocation != null) {
                fullTickers = 3;
            }
            // Flash indicator on and off when it's very far away
            else if (entityToOutline == null) {
                fullTickers = getFlashingTickers();
            }
            // Progressive distances away from player
            else if (entityToOutline.getDistanceToPlayer() < 16) {
                fullTickers = 4;
            } else if (entityToOutline.getDistanceToPlayer() < 32) {
                fullTickers = 3;
            } else if (entityToOutline.getDistanceToPlayer() < 48) {
                fullTickers = 2;
            } else if (entityToOutline.getDistanceToPlayer() < 64) {
                fullTickers = 1;
            } else {
                fullTickers = getFlashingTickers();
            }
            // Draw the indicator
            for (int tickers = 0; tickers < maxTickers; tickers++) {
                mc.getTextureManager().bindTexture(TICKER_SYMBOL);
                GlStateManager.enableAlpha();
                if (tickers < fullTickers) {
                    DrawUtils.drawModalRectWithCustomSizedTexture(x + tickers * 11, y, 0, 0, 9, 9, 18, 9, false);
                } else {
                    DrawUtils.drawModalRectWithCustomSizedTexture(x + tickers * 11, y, 9, 0, 9, 9, 18, 9, false);
                }
            }

            main.getUtils().restoreGLOptions();
        }
    }

    private static int getFlashingTickers() {
        if (CooldownManager.getRemainingCooldown("TREVOR_THE_TRAPPER_HUNT") % 2000 < 1000) {
            return 0;
        }
        return 1;
    }

    @SubscribeEvent
    public void onEntityOutline(RenderEntityOutlineEvent e) {
        if (e.getType() == RenderEntityOutlineEvent.Type.NO_XRAY) {
            if (SkyblockAddons.getInstance().getConfigValues().isEnabled(Feature.TREVOR_THE_TRAPPER_FEATURES) &&
                    SkyblockAddons.getInstance().getConfigValues().isEnabled(Feature.TREVOR_HIGHLIGHT_TRACKED_ENTITY) &&
                    isTrackingAnimal && entityToOutline != null && entityToOutline.getAnimal() != null &&
                    !Minecraft.getMinecraft().thePlayer.isPotionActive(Potion.blindness)) {
                e.queueEntityToOutline(entityToOutline.getAnimal(), entityToOutline.getRarity().getColorInt());
            }
        }
    }

    @SubscribeEvent
    public void onEntityEvent(LivingUpdateEvent e) {
        SkyblockAddons main = SkyblockAddons.getInstance();
        Entity entity = e.entity;
        if (SkyblockAddons.getInstance().getConfigValues().isEnabled(Feature.TREVOR_THE_TRAPPER_FEATURES) &&
                (main.getConfigValues().isEnabled(Feature.TREVOR_TRACKED_ENTITY_PROXIMITY_INDICATOR) || main.getConfigValues().isEnabled(Feature.TREVOR_HIGHLIGHT_TRACKED_ENTITY)) &&
                mushroomIslandLocations.contains(main.getUtils().getLocation())) {
            if (entity instanceof EntityArmorStand && entity.hasCustomName() && entity.ticksExisted > 30) {
                Matcher m = TRACKED_ANIMAL_NAME_PATTERN.matcher(TextUtils.stripColor(entity.getCustomNameTag()));
                if (m.matches()) {
                    TrackerRarity rarity = TrackerRarity.getFromString(m.group("rarity"));
                    TrackerType animal = TrackerType.getFromString(m.group("animal"));
                    if (rarity != null && animal != null) {
                        try {
                            TrackedEntity trackedEntity = new TrackedEntity((EntityArmorStand) entity, animal, rarity);
                            trackedEntity.attachAnimal(Minecraft.getMinecraft().theWorld.getEntitiesWithinAABB(animal.getClazz(),
                                    new AxisAlignedBB(entity.posX - 2, entity.posY - 2, entity.posZ - 2, entity.posX + 2, entity.posY + 2, entity.posZ + 2)));
                            entityToOutline = trackedEntity;
                        } catch (NullPointerException ignored) {
                        }
                    }
                }
            }
        }
    }

    @SubscribeEvent
    public void onChatReceived(ClientChatReceivedEvent e) {
        if (SkyblockAddons.getInstance().getConfigValues().isEnabled(Feature.TREVOR_THE_TRAPPER_FEATURES) &&
                e.type != 2 && SkyblockAddons.getInstance().getUtils().isOnSkyblock()) {
            String stripped = TextUtils.stripColor(e.message.getFormattedText());
            // Once the player has started the hunt, start some timers
            if (TREVOR_FIND_ANIMAL_PATTERN.matcher(stripped).matches()) {
                // Start the quest
                isTrackingAnimal = true;
                // The player has 10 minutes to kill the animal
                CooldownManager.put("TREVOR_THE_TRAPPER_HUNT", 600000);
                // The player has 30 seconds before they can receive another animal after killing the current one
                CooldownManager.put("TREVOR_THE_TRAPPER_RETURN", 30000);
            }
            // Once the player has killed the animal, remove the hunt timer
            else if (ANIMAL_DIED_PATTERN.matcher(stripped).matches() || ANIMAL_KILLED_PATTERN.matcher(stripped).matches()) {
                CooldownManager.remove("TREVOR_THE_TRAPPER_HUNT");
                onQuestEnded();
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onNameTagRender(Pre<EntityLivingBase> e) {
        Entity entity = e.entity;
        if (SkyblockAddons.getInstance().getConfigValues().isEnabled(Feature.TREVOR_THE_TRAPPER_FEATURES) &&
                !e.isCanceled() && SkyblockAddons.getInstance().getConfigValues().isEnabled(Feature.TREVOR_SHOW_QUEST_COOLDOWN) &&
                CooldownManager.isOnCooldown("TREVOR_THE_TRAPPER_RETURN")) {
            Pattern p = Pattern.compile("Trevor The Trapper");
            String s = TextUtils.stripColor(entity.getCustomNameTag());
            if (p.matcher(s).matches()) {
                String str = Utils.MESSAGE_PREFIX_SHORT + Translations.getMessage("messages.worldRenderedCooldownTime",
                        CooldownManager.getRemainingCooldown("TREVOR_THE_TRAPPER_RETURN") / 1000);
                DrawUtils.drawTextInWorld(str, e.x, e.y + entity.height + .75, e.z);
            }
        }
    }

    @SubscribeEvent
    public void onClientTick(ClientTickEvent e) {
        if (SkyblockAddons.getInstance().getConfigValues().isEnabled(Feature.TREVOR_THE_TRAPPER_FEATURES) &&
                e.phase == Phase.START && Minecraft.getMinecraft().thePlayer != null) {
            if (isTrackingAnimal && CooldownManager.getRemainingCooldown("TREVOR_THE_TRAPPER_HUNT") == 0) {
                onQuestEnded();
            } else if (entityToOutline != null) {
                entityToOutline.cacheDistanceToPlayer();
            }
        }
    }

    private void onQuestEnded() {
        isTrackingAnimal = false;
        entityToOutline = null;
    }


    private enum TrackerType {
        COW("Cow", EntityCow.class),
        PIG("Pig", EntityPig.class),
        SHEEP("Sheep", EntitySheep.class),
        RABBIT("Rabbit", EntityRabbit.class),
        CHICKEN("Chicken", EntityChicken.class);

        @Getter
        private final String name;
        @Getter
        private final Class<? extends Entity> clazz;

        TrackerType(String entityName, Class<? extends Entity> entityClass) {
            name = entityName;
            clazz = entityClass;
        }

        public static TrackerType getFromString(String s) {
            for (TrackerType type : values()) {
                if (type.name.equals(s)) {
                    return type;
                }
            }
            return null;
        }
    }

    private enum TrackerRarity {
        TRACKABLE("Trackable", ColorCode.WHITE),
        UNTRACKABLE("Untrackable", ColorCode.DARK_GREEN),
        UNDETECTED("Undetected", ColorCode.DARK_BLUE),
        ENDANGERED("Endangered", ColorCode.DARK_PURPLE),
        ELUSIVE("Elusive", ColorCode.GOLD);

        @Getter
        private final String nameTagName;
        @Getter
        private final ColorCode colorCode;
        @Getter
        private final int colorInt;

        TrackerRarity(String nameTag, ColorCode color) {
            nameTagName = nameTag;
            colorCode = color;
            colorInt = color.getColor();
        }

        public static TrackerRarity getFromString(String s) {
            for (TrackerRarity type : values()) {
                if (type.nameTagName.equals(s)) {
                    return type;
                }
            }
            return null;
        }
    }

    private static class TrackedEntity {
        @Getter
        private final EntityArmorStand armorStand;
        @Getter
        private final TrackerType type;
        @Getter
        private final TrackerRarity rarity;
        @Getter
        private Entity animal;
        @Getter
        private double distanceToPlayer;

        public TrackedEntity(EntityArmorStand theArmorStand, TrackerType trackerType, TrackerRarity trackerRarity) {
            armorStand = theArmorStand;
            type = trackerType;
            rarity = trackerRarity;
            cacheDistanceToPlayer();
        }

        public void attachAnimal(List<Entity> animalList) {
            if (animalList.size() == 0) {
                animal = null;
            }
            //System.out.println("hi");
            double minDist = Double.MAX_VALUE;
            for (Entity e : animalList) {
                // Minimize the distance between entities on the horizontal plane
                double horizDist = (e.posX - armorStand.posX) * (e.posX - armorStand.posX) + (e.posZ - armorStand.posZ) * (e.posZ - armorStand.posZ);
                //System.out.println(Math.abs(e.posY - armorStand.posZ));
                if (horizDist < minDist && Math.abs(e.posY - armorStand.posY) < 2) {
                    minDist = horizDist;
                    animal = e;
                }
            }
        }

        public void cacheDistanceToPlayer() {
            if (animal != null) {
                distanceToPlayer = Minecraft.getMinecraft().thePlayer.getDistanceToEntity(animal);
            } else {
                distanceToPlayer = Minecraft.getMinecraft().thePlayer.getDistanceToEntity(armorStand);
            }
        }
    }
}
