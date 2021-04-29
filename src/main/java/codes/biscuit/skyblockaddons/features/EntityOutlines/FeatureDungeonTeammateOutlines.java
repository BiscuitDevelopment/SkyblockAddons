package codes.biscuit.skyblockaddons.features.EntityOutlines;

import codes.biscuit.skyblockaddons.SkyblockAddons;
import codes.biscuit.skyblockaddons.core.Feature;
import codes.biscuit.skyblockaddons.events.RenderEntityOutlineEvent;
import codes.biscuit.skyblockaddons.utils.ColorCode;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.scoreboard.ScorePlayerTeam;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Controls the behavior of {@link codes.biscuit.skyblockaddons.core.Feature#MAKE_DUNGEON_TEAMMATES_GLOW}
 */
public class FeatureDungeonTeammateOutlines {

    /**
     * Entity-level predicate to determine whether a specific entity should be outlined.
     * Evaluates to {@code true} iff the entity should be outlined (i.e., accepts dungeon teammates)
     * Should be used in conjunction with the global-level predicate, {@link #GLOBAL_TEST()}.
     */
    private static final Predicate<Entity> ENTITY_TEST = e -> e instanceof EntityPlayer && e != Minecraft.getMinecraft().thePlayer;
    private static final Function<Entity, Integer> OUTLINE_COLOR = e -> {
        ScorePlayerTeam scoreplayerteam = (ScorePlayerTeam) ((EntityPlayer) e).getTeam();

        if (scoreplayerteam != null) {
            String formattedName = FontRenderer.getFormatFromString(scoreplayerteam.getColorPrefix());

            if (formattedName.length() >= 2) {
                return Minecraft.getMinecraft().fontRendererObj.getColorCode(formattedName.charAt(1));
            }
        }
        return ColorCode.GRAY.getColor();
    };

    public FeatureDungeonTeammateOutlines() {
    }

    /**
     * Global-level predicate to determine whether any entities should outlined.
     * Should be used in conjunction with the entity-level predicate, {@link #ENTITY_TEST}.
     * <p>
     * Don't accept if the player is on a personal island and the
     *
     * @return {@code false} iff no entities should be outlined (i.e., accept if the player is in a dungeon)
     */
    private static boolean GLOBAL_TEST() {
        return SkyblockAddons.getInstance().getConfigValues().isEnabled(Feature.MAKE_DUNGEON_TEAMMATES_GLOW) &&
                SkyblockAddons.getInstance().getUtils().isInDungeon();
    }

    /**
     * Queues items to be outlined that satisfy global- and entity-level predicates.
     *
     * @param e the outline event
     */
    @SubscribeEvent
    public void onRenderEntityOutlines(RenderEntityOutlineEvent e) {

        if (e.getType() == RenderEntityOutlineEvent.Type.XRAY) {
            // Test whether we should add any entities at all
            if (GLOBAL_TEST()) {
                // Queue specific items for outlining
                e.queueEntitiesToOutline(ENTITY_TEST, OUTLINE_COLOR);
            }
        }
    }
}
