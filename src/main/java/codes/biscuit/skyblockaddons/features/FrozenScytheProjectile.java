package codes.biscuit.skyblockaddons.features;

import codes.biscuit.skyblockaddons.core.EntityAggregate;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.block.Block;
import net.minecraft.block.BlockIce;
import net.minecraft.block.BlockPackedIce;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityArmorStand;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.monster.EntityEnderman;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.item.ItemBlock;
import net.minecraft.util.*;

import javax.vecmath.Matrix4f;
import java.awt.*;
import java.util.*;

public class FrozenScytheProjectile {

    @Getter
    private static Map<UUID, FrozenScytheProjectile> frozenScytheProjectiles = new HashMap<>();
    @Setter
    private static Vec3 lastRightClickLookVec = null;
    @Setter
    private static Vec3 lastRightClickEyePos = null;
    @Setter
    private static long lastRightClickTime = 0;

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
    Returns the true position of the center of the ice block on the armorstand
    Turns out it's not entirely useful since hypixel seems to register hits based on the armorstand
    With the notable exception of the height of the projectile
     */
    public Vec3 getProjectilePosition() {
        return projectileStand.getPositionVector().add(projectileLocation);
    }

    // Normal in flight scythe speed is about 1.8 blocks per tick
    public boolean isDead() {
        return projectileStand.isDead || (projectileStand.ticksExisted > 5 && ticksMotionless > 5);
    }

    /*
    Called every tick
     */
    public void onUpdate() {
        if (getMotion() < .01) {
            ticksMotionless++;

            //Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText((projectileStand.isCollided ? "true" : "false")));
        }
        else {
            ticksMotionless = 0;
        }
        //long currTime = System.currentTimeMillis();
        // Keep track of only recently intercepted entities
        /*
        for (Map.Entry<UUID, Long> entry : potentialHitEntities.entrySet()) {
            if (currTime - entry.getValue() > 150) {
                Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText("Rem: " + entry.getKey().toString()));
            }
        }
        potentialHitEntities.values().removeIf((hitTime) -> currTime - hitTime > 150);
        //Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText("Middle: " + potentialHitEntities.size()));
        List<EntityEnderman> list = Minecraft.getMinecraft().theWorld.getEntitiesWithinAABB(EntityEnderman.class, getBoundingBox());
        for (EntityEnderman enderman : list) {
            potentialHitEntities.put(enderman.getUniqueID(), currTime);
            Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText("Add: " + enderman.getUniqueID().toString()));
            //Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText(String.format("Hit1 %5.3f, %5.3f, %5.3f | %5.3f, %5.3f, %5.3f", enderman.posX - projectileStand.posX, enderman.posY - projectileStand.posY, enderman.posZ - projectileStand.posZ,
            //        enderman.posX - getProjectilePosition().xCoord, enderman.posY - getProjectilePosition().yCoord, enderman.posZ - getProjectilePosition().zCoord)));
        }
         */
        //for (UUID id : potentialHitEntities.keySet()) {
        //    Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText(id.toString()));
        //}

    }

    public double getMotion() {
        return projectileStand.getDistance(projectileStand.prevPosX, projectileStand.prevPosY, projectileStand.prevPosZ);
    }

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
        /*
        // Check a small range around the player
        List<EntityArmorStand> stands = Minecraft.getMinecraft().theWorld.getEntitiesWithinAABB(EntityArmorStand.class,
                mc.thePlayer.getEntityBoundingBox().expand(2, 2, 2));

        ArrayList<UUID> projectiles = new ArrayList<>();
        EntityArmorStand farthestProjectile = null;
        double farthestProjectileDistance = 0;

        for (EntityArmorStand stand : stands) {

            // Check if it's a recently spawned, small, invisible armorstand holding a block
            if (stand.ticksExisted >= 5 || !stand.isInvisible() || !stand.isSmall() || stand.getHeldItem() == null ||
                    !(stand.getHeldItem().getItem() instanceof ItemBlock)) {
                 continue;
            }
            // Check that the held block is ice or packed ice
            Block heldBlock = ((ItemBlock)stand.getHeldItem().getItem()).getBlock();
            if (!(heldBlock instanceof BlockIce) && !(heldBlock instanceof BlockPackedIce)) {
                continue;
            }
            Vec3 lookVec = lastRightClickLookVec;
            Vec3 projectileVec = new Vec3(stand.posX, stand.posY + .37, stand.posZ);
            projectileVec = projectileVec.subtract(lastRightClickEyePos);
            // Check if the player is within 2.5 blocks and is looking within 20 degrees of the stand
            if (projectileVec.lengthVector() > 2.5 ||
                    lookVec.dotProduct(projectileVec.normalize()) < MathHelper.cos((float) (20 / 180.0 * Math.PI))) {
                continue;
            }
            projectiles.add(stand.getUniqueID());
            // Track the "first" projectile only
            if (projectileVec.lengthVector() > farthestProjectileDistance) {
                farthestProjectile = stand;
                farthestProjectileDistance = projectileVec.lengthVector();
            }
        }
        // Frozen scythe projectiles have 5 stands
        if (projectiles.size() < 5) {
            return null;
        }
        // Insert the farthest projectile a thte f
        projectiles.remove(farthestProjectile);


        Vec3 blockPos = getHeldBlockRelativeCenter(farthestProjectile);

        return new FrozenScytheProjectile(farthestProjectile, blockPos, projectiles);
        */
    }



    /*
    Was the given enderman recently in contact with one of the frozen scythe projectiles?
    Return the projectile if so, null if not
     */
    /*
    public static FrozenScytheProjectile checkProjectileRecentlyNearEnderman(UUID endermanID) {
        //
        for (FrozenScytheProjectile f : frozenScytheProjectiles.values()) {
            if (f.potentialHitEntities.containsKey(endermanID) &&
                    System.currentTimeMillis() - f.potentialHitEntities.get(endermanID) <= 150) {
                return f;
            }
        }

        return null;
    }
    */


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
