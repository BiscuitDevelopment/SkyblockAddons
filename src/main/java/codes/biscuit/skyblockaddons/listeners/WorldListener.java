package codes.biscuit.skyblockaddons.listeners;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityFishHook;
import net.minecraft.util.*;
import net.minecraft.world.IWorldAccess;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.util.HashSet;
/*
This class allows us to approximate "our" trail of fish particles out of the recently-spawned fish particles in the world.

In general, we wish to identify those splash particles that belong to our fish trail, only.
We notice several identifying features of these (those in our fish trail) splash particles:
    1) Particles spawn with at a radial distance from the cast hook.
        - That distance differs by .1 blocks for sequential particles (with a small amount of variation)
        - During the rain, or in the case of dropped packets, the distance can be .2 blocks.
        - The distance to the hook always decreases
    2) Particles spawn at an angle (think polar coordinates) to the cast hook.
        - That angle differs based on a gaussian distribution for sequential particles
        - The gaussian distribution has mean 0 and standard deviation 4 degrees
        - Based on the distribution, very few sequential particles (<.27%) will differ in angle by more than
            3 standard distributions (12 degrees)
    3) Sequential particles approaching the hook generally come in every tick, but we can allow for 2 or 3 ticks
    4) Particles will not spawn farther than 8 blocks away from the cast hook

    (These are mostly discoverable in the EntityFishHook.java file. Hopefully, my making this algorithm won't cause servers to go ballistic...)

Based on these findings, we can try to do pattern recognition on recently spawned particles.

The algorithm:
When a new particle spawns, check if it matches these three conditions for each recently-spawned particle.
For any that do match the criteria, "link" the current particle to the previously-spawned particle.
We "link" the particles using a matrix. Set a '1' to matrix entry (i,j) for the particle i that links to j.
Do not set a '1' to the entry (j,i). We want to be able to 'link' things ONLY backwards in time.

Every game tick, calculate if any particles match the criteria. Many particles may match the criteria though...
So, we modify our criteria. Instead, find a few (i.e. a "trail" of) particles that each meet the criteria for the next
E.g. we find that particle 1 -> 5 -> 6 -> 10 (we identify that particle 1 meets the criteria for 5, 5 meets the criteria for 6, and so on)
These particles form a particle trail that not many (hopefully not any) other particles can form.
Given the particle trail (which we get via matrix multiplication), we return the most recent particle in the trail.
We then spawn a new distinct particle (in this case the lava drip particle) at the most recently spawned particle in a (the) particle trail.

This new, distinct particle can then be used to indicate the player's fish trail (and when to reel)


NOTES:
Consideration of time complexity is a huge part of the algorithm.
It is an O(n^2) algorithm, which for a large number of particles is unsustainable.
Each tick only gives us 50 milliseconds to compute stuff, and we don't want to waste it all on some stupid fishing indicator.
As a result, the class makes use of bit masks and bitwise operations, which are generally very fast.
Processing a new particle generally takes <.025 milliseconds and the "link" step generally takes <.050 milliseconds
So we are well below any critical thresholds for computation time

Out of convenience, we use the largest-available primitive: the long (64 bits).
This limits us to tracking the last 64 spawned particles, which may be sufficient for some cases.
However, in instances with more than 10+ fishers, we may not be able to link enough particles before they are overwritten by new ones.
In this case, the algorithm should not spawn any particles.

The main "linking" step uses binary matrix multiplication (wherein we OR the bitwise AND of each row/column to get a matrix entry)
This is the same as bitwise ANDing rows with columns, and asking if the result has at least one '1'.
One can think of this process as a markoff chain, where node connections are binary (either there is one or there isn't)
Just like in a markoff chain, squaring the adjacency matrix gives the connections "links" between nodes with two degrees of separation.
In this algorithm, we try to link four particles together, which involves an M^4 computation, where M is the adjacency matrix.

Admittedly, it seems cumbersome to implement IWorldAccess and attach ourselves as a world accessor
Since we are only interested in the particles.
But there doesn't seem to be a better way to do it...particle packets directly call accessor spawnParticle methods
And there isn't an event (that I've seen) that tracks particle spawns.
Presumably, we could access the EffectsRenderer fxlayers, or could do asm...
Maybe that would be appropriate...idk.
 */
public class WorldListener implements IWorldAccess {

    private final int WATER_WAKE_ID = EnumParticleTypes.WATER_WAKE.getParticleID();
    private final int LAVA_DRIP_ID = EnumParticleTypes.DRIP_LAVA.getParticleID();

    // Fish approach particles converge to the hook at a rate of .1 blocks (+- a small variation)
    private static final double DIST_VARIATION = .005;
    private static final double DIST_EXPECTED = .1;
    // Consecutive fish-approach particle-angle difference is a gaussian dist with standard dev. = 4 degrees
    // Taking 3 standard dev. (12 degrees) gives us an extremely good margin for error
    private static final double ANGLE_EXPECTED = 12;
    // Allow for 3 ticks between particles in a trail
    private static final int TIME_VARIATION = 2;

    /* Store several metrics about recently spawned particles */

    // The angle of each particle relative to the player's cast hook
    private double[] particleAngl = new double[64];
    // The distance of each particle relative to the player's cast hook
    private double[] particleDist = new double[64];
    // We store a matrix for each particle combinationFor each particle combination, whether the particles' distance difference is within the number of other particles that have a distance difference of (D % DIST_EXPECTED) < DIST_VARIATION
    private long[] particleMatrixRows = new long[64];
    // The position of each particle
    private Vec3[] particlePos = new Vec3[64];
    // A set of the positions (designed to filter out the double particles that spawn at the same position)
    HashSet<Double> particleHash = new HashSet<>(64);
    // Stores the results of matrix multiplication
    private boolean[] matches = new boolean[64];
    // The time the particle spawned
    private long[] particleTime = new long[64];

    // Current index
    private int idx = 0;

    // State variables
    boolean cacheEmpty = true;
    boolean newParticles = true;
    private long lastMatch = 0;

    @SubscribeEvent
    public void onWorldLoadEvent(WorldEvent.Load e) {
        if (e.world != null && e.world.isRemote) {
            e.world.addWorldAccess(this);
            clearParticleCache();
        }
    }

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent e) {
        if (e.phase == TickEvent.Phase.START) {
            Minecraft mc = Minecraft.getMinecraft();
            if (mc != null && mc.thePlayer != null) {
                if (mc.thePlayer.fishEntity != null && !cacheEmpty) {
                    if (newParticles) {
                        long[] pow2 = new long[64];
                        long[] pow4 = new long[64];
                        // Main computation:
                        // After matrix multiplication, any '1's indicate there exists a path with <= matrix power
                        bitwiseMatrixSquare(pow2, particleMatrixRows);  // Square the matrix
                        bitwiseMatrixSquare(pow4, pow2);                // Quart? the matrix
                        boolean flag = false;
                        for (int i = 0; i < 64; i++) {
                            // Get the links for 4
                            matches[i] = pow4[i] != 0;
                            if (matches[i]) flag = true;
                        }
                        // We are up-to-date
                        newParticles = false;
                        // Last successful computed time
                        if (flag) { lastMatch = System.currentTimeMillis(); }
                    }
                    // Matches[i] tells us whether the particle i has a 4 particle "link" (4 degrees of separation).
                    if (System.currentTimeMillis() - lastMatch < 500) {
                        int i = idx;
                        do {
                            if (matches[i] && particlePos[i] != null) {
                                Minecraft.getMinecraft().renderGlobal.spawnParticle(LAVA_DRIP_ID, true,
                                        particlePos[i].xCoord, particlePos[i].yCoord + .1, particlePos[i].zCoord, 0, 0, 0);
                                // Just spawn one particle (the most recent linked one) per tick
                                break;
                            }
                            i = i == 0 ? 63 : i - 1;
                        }
                        while (i != idx);
                    }
                }
                // Reset override
                else if (mc.thePlayer.fishEntity == null) {
                    clearParticleCache();
                }
            }
        }
    }

    public void clearParticleCache() {
        if (cacheEmpty) return;
        for (int i = 0; i < 64; i++) {
            particleMatrixRows[i] = 0;
            particleDist[i] = Double.MAX_VALUE;
            particleAngl[i] = Double.MAX_VALUE;
            particlePos[i] = null;
            matches[i] = false;
            particleTime[i] = Long.MAX_VALUE;
        }
        particleHash.clear();
        idx = 0;
        cacheEmpty = true;
        newParticles = false;
    }

    public void markBlockForUpdate(BlockPos pos) {

    }


    public void notifyLightSet(BlockPos pos) {

    }


    public void markBlockRangeForRenderUpdate(int x1, int y1, int z1, int x2, int y2, int z2) {

    }


    public void playSound(String soundName, double x, double y, double z, float volume, float pitch) {

    }


    public void playSoundToNearExcept(EntityPlayer except, String soundName, double x, double y, double z, float volume, float pitch) {

    }


    public void spawnParticle(int particleID, boolean ignoreRange, double xCoord, double yCoord, double zCoord, double xOffset, double yOffset, double zOffset, int... parameters) {
        if (particleID == WATER_WAKE_ID) {
            //Minecraft.getMinecraft().renderGlobal.spawnParticle(LAVA_ID, ignoreRange, xCoord, yCoord, zCoord-.3, xOffset, yOffset, zOffset, parameters);
            EntityFishHook hook = Minecraft.getMinecraft().thePlayer.fishEntity;
            // It's extremely unlikely that two unrelated particles hash to the same place
            // However, normal fish particles come in pairs of two--the extra one is extraneous and we wish to ignore it
            double hash = 31 * (31 * 23 + xCoord) + zCoord;
            if (hook != null && !particleHash.contains(hash)) {
                double distToHook = Math.sqrt((xCoord - hook.posX) * (xCoord - hook.posX) + (zCoord - hook.posZ) * (zCoord - hook.posZ));
                // Particle trails start 2-8 blocks away. Ignore any that are far away
                if (distToHook > 8) { return; }
                // Save several particle metrics
                particleDist[idx] = distToHook;
                particleAngl[idx] = MathHelper.atan2(xCoord - hook.posX, zCoord - hook.posZ) * 180 / Math.PI;
                particlePos[idx] = new Vec3(xCoord, yCoord, zCoord);
                particleTime[idx] = Minecraft.getMinecraft().theWorld.getTotalWorldTime();
                //Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText(particleAngl[idx] + " " + particleDist[idx]));
                // Begin with a clean row
                long particleRowTmp1 = 0;
                long particleRowTmp2 = 0;
                // Mask to zero out the idx th element from a row
                long idxMask = ~(1 << (63 - idx));
                // Update the row for each particle
                for (int i = 0; i < 64; i++) {

                    // The newest spawned particle should be within a few degrees of the previous particle in the trail
                    double anglDiff = Math.abs(particleAngl[i] - particleAngl[idx]) % 360;
                    boolean anglMatch = (anglDiff > 180 ? 360 - anglDiff : anglDiff) < ANGLE_EXPECTED;
                    // The newest spawned particle should have a distance of -.1/-.2 to the previous particle in the trail
                    double distDiff1 = Math.abs(particleDist[i] - particleDist[idx] - DIST_EXPECTED);
                    double distDiff2 = Math.abs(particleDist[i] - particleDist[idx] - 2 * DIST_EXPECTED);
                    // The newest spawned particle should be within a few ticks of the previous particle in the trail
                    boolean timeMatch = (particleTime[idx] - particleTime[i]) < TIME_VARIATION; // Negative (okay) if unitialized
                    // Matrix multiplication assumes we are little endian (most significant bit is column 0)
                    // Default: if it's not raining and particles aren't being dropped, we expect .1 block distance
                    particleRowTmp1 |= (distDiff1 < DIST_VARIATION && anglMatch && timeMatch ? 1 : 0) << (63 - i);
                    // Special: if it's raining or particles are being dropped, we expect .2 block distance sometimes
                    particleRowTmp2 |= (distDiff2 < DIST_VARIATION && anglMatch && timeMatch ? 1 : 0) << (63 - i);
                    // De-link all previous particles from the current one (We want to trace particles back in time, only)
                    // Create a bitmask that zeros out the idx position of the ith particle (keep in mind we are in little endian)
                    // This step effectively zeros-put the idx column of the matrix
                    particleMatrixRows[i] &= idxMask;
                }
                // If we find no .1 distance particles, go to .2 distance particles...it's not perfect by any means lol
                particleMatrixRows[idx] = particleRowTmp1 != 0 ? particleRowTmp1 : particleRowTmp2;
                // Add hash to the set
                particleHash.add(hash);
                newParticles = true;
                cacheEmpty = false;
                // Wrap from 63 to 0
                idx = idx < 63 ? idx + 1 : 0;
            }
            // Clean up every once in a while in case someone casts their rod for a long time
            if (particleHash.size() > 100) {
                particleHash.clear();
            }
        }
    }


    /*
    Performs a bitwise square of a matrix based on it's rows
    We assume the matrix is 64 x 64
    Saves the resulting matrix to result in row form
     */
    private void bitwiseMatrixSquare(long[] result, long[] rows) {
        // First, get the column representation of the matrix by copying the matrix
        long[] cols = new long[64];
        // Iterate through columns
        for (int j = 0; j < 64; j++) {
            // Zero out column
            cols[j] = 0;
            // Create a mask to get jth column from the row
            long mask = 1 << (63 - j);
            // Iterate through rows
            for (int i = 0; i < 64; i++) {
                // Leftshift the i,j entry to the ith position (row) of the jth column
                cols[j] |= ((rows[i] & mask) != 0 ? 1 : 0) << i;
            }
        }
        // Perform the multiplication
        for (int i = 0; i < 64; i++) {
            result[i] = 0;
            for (int j = 0; j < 64; j++) {
                // Leftshift i,j entry to the ith position (row) of the jth column
                result[i] |= ((rows[i] & cols[j]) != 0 ? 1 : 0) << (63 - j);
            }
        }
    }

    public void onEntityAdded(Entity entityIn) {

    }


    public void onEntityRemoved(Entity entityIn) {

    }


    public void playRecord(String recordName, BlockPos blockPosIn) {

    }


    public void broadcastSound(int soundID, BlockPos pos, int data) {

    }


    public void playAuxSFX(EntityPlayer player, int sfxType, BlockPos blockPosIn, int data) {

    }


    public void sendBlockBreakProgress(int breakerId, BlockPos pos, int progress) {

    }
}
