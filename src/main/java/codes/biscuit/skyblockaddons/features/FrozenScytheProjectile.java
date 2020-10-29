package codes.biscuit.skyblockaddons.features;

import lombok.Getter;
import lombok.Setter;
import net.minecraft.block.Block;
import net.minecraft.block.BlockIce;
import net.minecraft.block.BlockPackedIce;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityArmorStand;
import net.minecraft.item.ItemBlock;
import net.minecraft.util.*;

import javax.vecmath.Matrix4f;
import java.util.*;

public class FrozenScytheProjectile {

    // A list of projectiles spawned in the world from the player's position
    @Getter private static Map<UUID, FrozenScytheProjectile> frozenScytheProjectiles = new HashMap<>();
    // A set of right click information from the last time the player right clicked with the frozen scythe
    @Setter private static Vec3 lastRightClickLookVec = null;
    @Setter private static Vec3 lastRightClickEyePos = null;
    @Setter private static long lastRightClickTime = 0;


    private Vec3 projectileLocation;
    @Getter
    private EntityArmorStand projectileStand;
    private int ticksMotionless = 0;
    @Getter
    private Map<UUID, Long> potentialHitEntities = new HashMap<>();



    public FrozenScytheProjectile(EntityArmorStand stand, Vec3 relativeProjectileLoc) {
        projectileStand = stand;
        projectileLocation = relativeProjectileLoc;
    }


    /*
    Returns the true position of the center of the ice/packed ice block on the armorstand
    Turns out it's not entirely useful since hypixel seems to register hits based on the armorstand's bb/position
    With the notable exception of the height of the ice block making a big difference
     */
    public Vec3 getProjectilePosition() {
        return projectileStand.getPositionVector().add(projectileLocation);
    }



    /*
    The projectile is dead when it is truly dead, or when it's been motionless for some time
    Motion seems to be the best way of figuring out whether the projectile is "active"
     */
    public boolean isDead() {
        return projectileStand.isDead || (projectileStand.ticksExisted > 5 && ticksMotionless > 5);
    }



    /*
    Called every tick
     */
    public void onUpdate() {
        if (getMotion() < .01) {
            ticksMotionless++;
        }
        else {
            ticksMotionless = 0;
        }
    }



    /*
    Get the projectile motion in the last tick
     */
    public double getMotion() {
        return projectileStand.getDistance(projectileStand.prevPosX, projectileStand.prevPosY, projectileStand.prevPosZ);
    }


    /*
    Get the (albeit weird) bounding box of the projectile.
    Does not work perfectly, but should intercept with the bb of an entity that is killed with the projectile
    May only work with enderman bounding boxes cause I didn't test on anything else...idk why it wouldn't work though.
     */
    public AxisAlignedBB getBoundingBox() {
        return new AxisAlignedBB(projectileStand.posX - 1.35, getProjectilePosition().yCoord - .1, projectileStand.posZ - 1.35,
                                 projectileStand.posX + 1.35, getProjectilePosition().yCoord + .1, projectileStand.posZ + 1.35);
    }



    /**
     * Returns an instance of FrozenScytheProjectile if this entity is in fact part of a frozen
     * scythe projectile, or null if not.
     */
    public static FrozenScytheProjectile checkAndReturnFrozenScytheProjectile(Entity targetEntity) {
        // Check if target entity is an armorstand close to the player, and that the player has right clicked w/ frozen scythe
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.thePlayer == null || lastRightClickLookVec == null || lastRightClickEyePos == null ||
                System.currentTimeMillis() - lastRightClickTime > 1000 ||
                !(targetEntity instanceof EntityArmorStand) ||
                !mc.thePlayer.getEntityBoundingBox().expand(2,2,2).intersectsWith(targetEntity.getEntityBoundingBox())) {
            return null;
        }
        EntityArmorStand stand = (EntityArmorStand)targetEntity;
        // Check if it's a small, invisible armorstand holding a block
        if (!stand.isInvisible() || !stand.isSmall() || stand.getHeldItem() == null ||
                !(stand.getHeldItem().getItem() instanceof ItemBlock)) {
            return null;
        }
        // Check that the held block is ice or packed ice
        Block heldBlock = ((ItemBlock)stand.getHeldItem().getItem()).getBlock();
        if (!(heldBlock instanceof BlockIce) && !(heldBlock instanceof BlockPackedIce)) {
            return null;
        }
        // Get the block's location relative to the armorstand
        Vec3 blockPos = getHeldBlockRelativeCenter(stand);
        //Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText(String.format("%5.4f, %5.4f, %5.4f", blockPos.xCoord, blockPos.yCoord, blockPos.zCoord)));
        Vec3 lookVec = lastRightClickLookVec;
        Vec3 projVec = new Vec3(stand.posX, stand.posY + .37, stand.posZ);
        projVec = projVec.subtract(lastRightClickEyePos);
        // Check if the player is within 2.5 blocks and looking within 20 degrees of the stand
        if (projVec.lengthVector() > 2.5 ||
                lookVec.dotProduct(projVec.normalize()) < MathHelper.cos((float) (20 / 180.0 * Math.PI))) {
            return null;
        }

        return new FrozenScytheProjectile(stand, blockPos);
    }



    // t2 matrix translates to arm height and inverts x/y direction
    private static final float[] t2v = {  -1, 0, 0, 0,
                                            0,-1, 0, 1.5078125f,
                                            0, 0, 1, 0,
                                            0, 0, 0, 1};
    private static final Matrix4f t2 = new Matrix4f(t2v);

    // The child matrix translates/rotates appropriately for a smaller armorstand
    private static final float[] childv = { .5f, 0, 0, 0,
                                              0,.4698463f, -0.17101f, .625f,
                                              0,.17101f,   .4698463f, 0,
                                              0, 0, 0, 1};
    private static final Matrix4f child = new Matrix4f(childv);

    // The sneak matrix translates everything down .2
    private static final float[] sneakv = { 1, 0, 0, 0,
                                            0, 1, 0, .203125f,
                                            0, 0, 1, 0,
                                            0, 0, 0, 1};
    private static final Matrix4f sneak = new Matrix4f(sneakv);

    // t3 matrix translates from body center of mass to the shoulder
    private static final float[] t3v = {1, 0, 0, -.3125f,
                                        0, 1, 0, .125f,
                                        0, 0, 1, 0,
                                        0, 0, 0, 1};
    private static final Matrix4f t3 = new Matrix4f(t3v);

    // t4 matrix translates from the shoulder to the wrist, and then from the wrist to the center of the held object
    private static final float[] t4v = {-1, 0, 0, -.0625f,
                                        0, .9659258f, .2588190f, .6498155f,
                                        0, .2588190f, -.9659258f,-.2759259f,
                                        0, 0, 0, 1};
    private static final Matrix4f t4 = new Matrix4f(t4v);


    /*
    Gets the <x,y,z> center of a block held by an armorstand, relative to armorstand position
    Can pass in any armorstand, but the result is only relevant if the armorstand is actually holding a block
    Note that it does not check if the armorstand is named dinnerbone, so ignores a flip in orientation
     */
    public static Vec3 getHeldBlockRelativeCenter(EntityArmorStand e) {
        if (e == null) {
            return null;
        }

        double entityRot = e.rotationYaw;
        Rotations armRot = e.getRightArmRotation();
        //Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText(armRot.to))

        Matrix4f ret = new Matrix4f();
        ret.setIdentity();

        Matrix4f tmp = new Matrix4f();

        // Rotate to the entity's rotation
        tmp.rotY((float)((180-entityRot) / 180.0F * Math.PI));
        ret.mul(tmp);

        // Translate to the body's neck and invert axes
        ret.mul(t2);

        // Scale if it's a child
        if (e.isSmall()) {
            ret.mul(child);
        }

        // Translate to the shoulder
        ret.mul(t3);

        // Rotate to the arm's rotation
        tmp.rotZ((float) (armRot.getZ() / 180F * Math.PI));
        ret.mul(tmp);
        tmp.rotY((float) (armRot.getY() / 180F * Math.PI));
        ret.mul(tmp);
        tmp.rotX((float) (armRot.getX() / 180F * Math.PI));
        ret.mul(tmp);

        // Translate appropriately if sneaking
        if (e.isSneaking()) {
            ret.mul(sneak);
        }

        // Translate down the arm and then out to the item center
        ret.mul(t4);
        // Return the translation
        return new Vec3(ret.m03, ret.m13, ret.m23);
    }
}
