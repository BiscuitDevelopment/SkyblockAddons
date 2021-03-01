package codes.biscuit.skyblockaddons.features;

import codes.biscuit.skyblockaddons.asm.hooks.EffectRendererHook;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.EntityFX;
import net.minecraft.client.particle.EntityFishWakeFX;
import net.minecraft.entity.projectile.EntityFishHook;
import net.minecraft.util.MathHelper;

import java.util.*;

/**
 *
 * This class allows us to approximate the trail of fish particles made by a fish converging to the player's bobber.
 * To do this, we use a variant on the Bellman Ford algorithm, as well as the exponentiation-by-squaring algorithm
 *
 * In general, we wish to identify those splash particles that belong to a fish converging on the player's bobber.
 * Notice several identifying features of these converging particles:
 *     1) Particles spawn at a radial distance from the cast hook.
 *         - That distance decreases by .1 blocks for sequential particles (with a small amount of variation)
 *         - During the rain, or in the case of dropped packets, the distance can be .2 blocks.
 *     2) Particles spawn at an angle (think polar coordinates) to the cast hook.
 *         - That angle differs based on a gaussian distribution for sequential particles
 *         - The gaussian distribution has mean 0 and standard deviation 4 degrees
 *         - Based on the distribution, very few sequential particles (<.27%) will differ in angle by more than
 *             3 standard distributions (12 degrees)
 *     3) Sequential particles approaching the hook generally come in every tick, but we can allow for 2 or 3 ticks
 *     4) Particles will not spawn farther than 8 blocks away from the cast hook
 *
 *     (These are mostly discoverable in the EntityFishHook.java file. Hopefully, my making this algorithm won't cause servers to go ballistic...)
 *
 * Consideration of time complexity is a huge part of the algorithm.
 * It is an O(n^2) algorithm, which for a large number of particles is unsustainable.
 * Each tick only gives us 50 milliseconds to compute stuff, so we're using bitwise operations.
 * Unfortunately, the longest primitive is 64 bits, so we're limited to tracking 64 particles at the moment.
 * This means when 10+ fishers are in the same spot, we may not be able to link the particles before we overwrite with new ones.
 *
 * Processing the per-particle step takes <.025 milliseconds and the per-tick step generally takes <.050 milliseconds
 * So we are well below any critical thresholds for computation time.
 *
 * @author Phoube
 */

public class FishParticleManager {

    /**
     * Fish approach particles converge to the hook at a rate of .1 blocks
     */
    private static final double DIST_EXPECTED = .1;
    /**
     * Fish approach particles converge to the hook w/ a small distance variation
     */
    private static final double DIST_VARIATION = .005;
    /**
     * Consecutive particle-angle difference is a gaussian dist. with standard dev. = 4 degrees
     * Taking 3 standard dev. (12 degrees) gives us an extremely good margin of error
     */
    private static final double ANGLE_EXPECTED = 12;
    /**
     * Allow for 4 ticks between particles in a trail
     */
    private static final int TIME_VARIATION = 4;

    /* Store several metrics about recently spawned particles */

    /**
     * The angle of each particle relative to the player's cast hook
     */
    private static final double[] particleAngl = new double[64];
    /**
     * The distance of each particle relative to the player's cast hook
     */
    private static final double[] particleDist = new double[64];
    /**
     * For each particle combination (i,j), store whether particle i could have followed particle j in a converging trail
     */
    private static final long[] particleMatrixRows = new long[64];
    /**
     * The position of each particle
     */
    @SuppressWarnings("unchecked")
    private static final ArrayList<EntityFX>[] particleList = new ArrayList[64];
    /**
     * A set of the positions (designed to filter out the double particles that spawn at the same position)
     */
    private static final LinkedHashMap<Double, List<EntityFX>> particleHash = new LinkedHashMap<>(64);
    /**
     * The time each particle spawned
     */
    private static final long[] particleTime = new long[64];
    /**
     * Current index
     */
    private static int idx = 0;
    /**
     * True if particles have spawned/been processed and are currently stored in memory
     */
    private static boolean cacheEmpty = true;

    /**
     * When a new particle spawns, check if it matches these conditions for each recently-spawned particle.
     * For any that do match the criteria, "link" the current particle to the previously-spawned particle.
     * We "link" the particles using a matrix. Set a '1' to matrix entry (i,j) for the particle i that links to j.
     * Do not set a '1' to the entry (j,i). We want to be able to 'link' things ONLY backwards in time.
     *
     * @param fishWakeParticle a newly spawned fish particle
     */
    public static void onFishWakeSpawn(EntityFishWakeFX fishWakeParticle) {

        EntityFishHook hook = Minecraft.getMinecraft().thePlayer.fishEntity;
        double xCoord = fishWakeParticle.posX;
        double zCoord = fishWakeParticle.posZ;

        // It's extremely unlikely that two unrelated particles hash to the same place
        // However, normal fish particles come in pairs of two--the extra one is extraneous and we wish to ignore it
        double hash = 31 * (31 * 23 + xCoord) + zCoord;
        if (hook != null && !particleHash.containsKey(hash)) {
            double distToHook = Math.sqrt((xCoord - hook.posX) * (xCoord - hook.posX) + (zCoord - hook.posZ) * (zCoord - hook.posZ));
            // Particle trails start 2-8 blocks away. Ignore any that are far away
            if (distToHook > 8) { return; }
            // Save several particle metrics
            particleDist[idx] = distToHook;
            particleAngl[idx] = MathHelper.atan2(xCoord - hook.posX, zCoord - hook.posZ) * 180 / Math.PI;
            particleTime[idx] = Minecraft.getMinecraft().theWorld.getTotalWorldTime();
            // We want O(1) index lookup and position-hash lookup. Use hashmap and array
            ArrayList<EntityFX> tmp = new ArrayList<>(Collections.singletonList(fishWakeParticle));
            particleList[idx] = tmp;
            particleHash.put(hash, tmp);
            // Use linked hash map's FIFO order to keep the hash at 64 elements
            if (particleHash.size() > 64) {
                Iterator<Map.Entry<Double, List<EntityFX>>> itr = particleHash.entrySet().iterator();
                while(particleHash.size() > 64) {
                    itr.next();
                    itr.remove();
                }
            }
            cacheEmpty = false;

            // Begin with a clean row
            long particleRowTmp1 = 0;
            long particleRowTmp2 = 0;
            // Mask to zero out the idx th element from a row
            long idxMask = ~(1L << (63 - idx));
            // Update the row for each particle
            for (int i = 0; i < 64; i++) {

                // The newest spawned particle should be within a few degrees of the previous particle in the trail
                double anglDiff = Math.abs(particleAngl[i] - particleAngl[idx]) % 360;
                boolean anglMatch = (anglDiff > 180 ? 360 - anglDiff : anglDiff) < ANGLE_EXPECTED;
                // The newest spawned particle should have a distance of -.1/-.2 to the previous particle in the trail
                double distDiff1 = Math.abs(particleDist[i] - particleDist[idx] - DIST_EXPECTED);
                double distDiff2 = Math.abs(particleDist[i] - particleDist[idx] - 2 * DIST_EXPECTED);
                // The newest spawned particle should be within a few ticks of the previous particle in the trail
                boolean timeMatch = (particleTime[idx] - particleTime[i]) <= TIME_VARIATION; // Negative (okay) if uninitialized
                // Matrix multiplication assumes we are little endian (most significant bit is column 0)
                // Default: if it's not raining and particles aren't being dropped, we expect .1 block distance
                particleRowTmp1 |= (distDiff1 < DIST_VARIATION && anglMatch && timeMatch ? 1L : 0L) << (63 - i);
                // Special: if it's raining or particles are being dropped, we expect .2 block distance sometimes
                particleRowTmp2 |= (distDiff2 < DIST_VARIATION && anglMatch && timeMatch ? 1L : 0L) << (63 - i);
                // De-link all previous particles from the current one (We want to trace particles back in time, only)
                // Create a bitmask that zeros out the idx position of the ith particle (keep in mind we are in little endian)
                // This step effectively zeros-put the idx column of the matrix
                particleMatrixRows[i] &= idxMask;
            }
            // If we find no .1 distance particles, go to .2 distance particles...it's not perfect by any means lol
            particleMatrixRows[idx] = particleRowTmp1 != 0 ? particleRowTmp1 : particleRowTmp2;
            // Recalculate the fish trails with the new particle
            calculateTrails();
            // Wrap from 63 to 0
            idx = idx < 63 ? idx + 1 : 0;
        }
        // Two fish-wake particles particles spawn at the same position. To get both, add the 2nd to the list
        else if (hook != null) {
            particleHash.get(hash).add(fishWakeParticle);
        }
        // Clear the cache if the player's hook isn't cast
        else {
            clearParticleCache();
        }
    }


    /**
     * Find a few (i.e. a "trail" of) particles that each meet the criteria for the subsequent particle in the trail
     * E.g. we find a trail 1 -> 5 -> 6 -> 10 (particle 1 meets the criteria for 5, 5 meets the criteria for 6, and so on)
     * Finding the trail of length n is found by computing M^n, where M is the pairwise matchings for each particle combination
     *
     * Given the particle trail, spawn a distinct particle (lava drip) at the most recently spawned particle in the particle trail.
     */
    private static void calculateTrails() {
        long[] pow2 = new long[64];
        long[] pow4 = new long[64];

        // Main computation:
        // Binary matrix multiplication (wherein we OR the bitwise AND of each row/column to get a matrix entry)
        // This is like a markoff chain, except node connections are binary (either there is one or there isn't)
        // Squaring the adjacency matrix gives the connections "links" between nodes with two degrees of separation.
        // Here we link four particles together, which involves an particleMatrixRows^4 computation
        // After matrix multiplication, any '1's indicate there exists a path with <= matrix power
        bitwiseMatrixSquare(pow2, particleMatrixRows);  // Square the matrix
        bitwiseMatrixSquare(pow4, pow2);                // Quart the matrix

        // Get the particles at the "head" of any trails of >= 4 particles, beginning at the most recently spawned particle
        // Track which particles have "linked" to previously-iterated particles
        long trailHeadTracker = 0;
        boolean first = true;
        long currTick = Minecraft.getMinecraft().theWorld.getTotalWorldTime();
        // Particle idx was the most recently spawned. Start there and go back through the particles.
        for (int i = idx; first || i != idx; i = i == 0 ? 63 : i - 1) {
            // If this particle is at the head of >= 4 particles
            if (pow4[i] != 0) {
                // If this particle is at the head of the trail, then it won't link to any other previously iterated particles
                long mask = 1L << (63 - i);
                if ((trailHeadTracker & mask) == 0 && currTick - particleTime[i] < 5) {
                    for (EntityFX entityFX : particleList[i]) {
                        EffectRendererHook.addParticleToOutline(entityFX);
                    }
                }
                // Keep track of all particles that linked to this particle.
                // Don't add these particles later, as they're not the head of the trail
                trailHeadTracker |= particleMatrixRows[i];
            }
            first = false;
        }
    }


    /**
     * Clears matrices, saved elements, etc.
     */
    public static void clearParticleCache() {
        if (cacheEmpty) return;
        for (int i = 0; i < 64; i++) {
            particleMatrixRows[i] = 0;
            particleDist[i] = Double.MAX_VALUE;
            particleAngl[i] = Double.MAX_VALUE;
            particleList[i] = null;
            particleTime[i] = Long.MAX_VALUE;
        }
        particleHash.clear();
        idx = 0;
        cacheEmpty = true;
    }


    /**
     * Performs a bitwise square of a matrix
     *
     * Saves the resulting matrix to result in row form
     * @param result saved result of the matrix squaring
     * @param rows input 64 x 64 bit matrix
     */
    private static void bitwiseMatrixSquare(long[] result, long[] rows) {
        // First, get the column representation of the matrix by copying the matrix
        long[] cols = new long[64];
        // Iterate through columns
        for (int j = 0; j < 64; j++) {
            // Zero out column
            cols[j] = 0;
            // Create a mask to get jth column from the row
            long mask = 1L << (63 - j);
            // Iterate through rows
            for (int i = 0; i < 64; i++) {
                // Leftshift the i,j entry to the ith position (row) of the jth column
                cols[j] |= ((rows[i] & mask) != 0 ? 1L : 0L) << i;
            }
        }
        // Perform the multiplication
        for (int i = 0; i < 64; i++) {
            result[i] = 0;
            for (int j = 0; j < 64; j++) {
                // Leftshift i,j entry to the ith position (row) of the jth column
                result[i] |= ((rows[i] & cols[j]) != 0 ? 1L : 0L) << (63 - j);
            }
        }
    }
}
