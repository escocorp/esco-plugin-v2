/*
 * HexedReloaded, a reimplementation of the hexed gamemode from Anuke,
 * with more features and better performances.
 *
 * Copyright (C) 2025  Xpdustry
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.xpdustry.hexed.generation;

import arc.util.noise.Ridged;
import mindustry.content.Blocks;
import mindustry.world.Block;
import mindustry.world.blocks.environment.Floor;

public final class RiverNoiseFunction extends GeneratorFunction {

    private float scale = 40;
    private float threshold1 = 0f;
    private float threshold2 = 0.1f;
    private float octaves = 1;
    private float falloff = 0.5f;
    private Floor floor1 = Blocks.water.asFloor();
    private Floor floor2 = Blocks.deepwater.asFloor();
    private Block block = Blocks.sandWall;
    private Block target = Blocks.air;

    /**
     * Scale controlling noise frequency for the river generator.
     *
     * @return the scale value; the noise sampling uses `1f / scale`
     */
    public float getScale() {
        return this.scale;
    }

    /**
     * Set the noise scale that controls the spatial frequency of river features.
     *
     * @param scale the scale value; used as `1f / scale` when computing the noise frequency.
     *              Larger values produce broader, more widely spaced features, while smaller
     *              values produce finer, more frequent features.
     */
    public void setScale(final float scale) {
        this.scale = scale;
    }

    /**
     * Primary noise threshold that determines when the base floor change is applied.
     *
     * @return the primary noise threshold used to gate application of `floor1`
     */
    public float getThreshold1() {
        return this.threshold1;
    }

    /**
     * Sets the primary noise threshold that gates application of the base floor.
     *
     * @param threshold1 the noise cutoff; tiles with noise greater than or equal to this value become eligible for the primary floor change
     */
    public void setThreshold1(final float threshold1) {
        this.threshold1 = threshold1;
    }

    /**
     * Secondary noise threshold that gates application of the secondary floor.
     *
     * @return the threshold value; `floor2` is applied when the computed noise is greater than or equal to this value
     */
    public float getThreshold2() {
        return this.threshold2;
    }

    /**
     * Set the secondary noise threshold that gates application of the secondary floor.
     *
     * @param threshold2 the noise value; when computed noise is greater than or equal to this value the secondary floor (`floor2`) will be applied
     */
    public void setThreshold2(final float threshold2) {
        this.threshold2 = threshold2;
    }

    /**
     * Number of octaves applied when computing the ridged 2D noise.
     *
     * @return the octave count used by the noise generator
     */
    public float getOctaves() {
        return this.octaves;
    }

    /**
     * Set the number of octaves used by the ridged noise generator.
     *
     * @param octaves the number of noise octaves; larger values add finer detail to the generated noise
     */
    public void setOctaves(final float octaves) {
        this.octaves = octaves;
    }

    /**
     * Gets the falloff factor used by the ridged noise generator.
     *
     * @return the falloff factor that controls amplitude reduction between octaves
     */
    public float getFalloff() {
        return this.falloff;
    }

    /**
     * Sets the noise falloff used by the ridged-noise generator.
     *
     * @param falloff the falloff factor applied between octaves (typically between 0 and 1)
     */
    public void setFalloff(final float falloff) {
        this.falloff = falloff;
    }

    /**
     * Primary floor used when a tile meets the first noise threshold.
     *
     * @return the primary {@code Floor} to apply to matching tiles; {@code Blocks.air} indicates no floor change.
     */
    public Floor getFloor1() {
        return this.floor1;
    }

    /**
     * Set the primary floor applied to tiles when they meet the noise and target conditions.
     *
     * @param floor1 the primary Floor to apply; use `Blocks.air` to disable primary floor placement
     */
    public void setFloor1(final Floor floor1) {
        this.floor1 = floor1;
    }

    /**
     * Secondary floor applied when noise meets the higher threshold.
     *
     * @return the secondary Floor to apply when noise is greater than or equal to threshold2
     */
    public Floor getFloor2() {
        return this.floor2;
    }

    /**
     * Sets the secondary floor to apply when the noise value meets the higher threshold.
     *
     * @param floor2 the floor to apply for the secondary (deeper) river region
     */
    public void setFloor2(final Floor floor2) {
        this.floor2 = floor2;
    }

    /**
     * Gets the block used for placement on eligible tiles.
     *
     * @return the Block placed on solid tiles when the river noise conditions are met
     */
    public Block getBlock() {
        return this.block;
    }

    /**
     * Sets the block to place on solid tiles when river conditions are met.
     *
     * @param block the Block to place on solid tiles; pass {@code Blocks.air} to disable block placement
     */
    public void setBlock(final Block block) {
        this.block = block;
    }

    /**
     * The block used as a target filter for tile modifications.
     *
     * @return the target Block that tiles must match to be eligible for changes; may be `Blocks.air` to disable filtering
     */
    public Block getTarget() {
        return this.target;
    }

    /**
     * Sets the block value used to determine which tiles are eligible for modification.
     *
     * @param target the Block to match against a tile's current floor or block; if set to {@code Blocks.air}, the generator will consider all tiles eligible
     */
    public void setTarget(final Block target) {
        this.target = target;
    }

    /**
     * Applies river-like floor and block changes to the given tile based on ridged 2D noise and configured thresholds.
     *
     * <p>If the noise value is greater than or equal to {@code threshold1} and the tile matches the configured
     * {@code target} (or {@code target} is {@code Blocks.air}), this method modifies the tile:
     * it sets {@code floor1} when configured; replaces the tile's block with {@code block} when the current block is solid
     * and neither the current nor configured block is {@code Blocks.air}; and sets {@code floor2} when the noise is greater
     * than or equal to {@code threshold2} and {@code floor2} is configured.</p>
     *
     * @param x the tile x-coordinate
     * @param y the tile y-coordinate
     * @param tile the MapTile to potentially modify
     */
    @Override
    public void accept(final int x, final int y, final MapTile tile) {
        final float noise = Ridged.noise2d(
                this.getSeed() + 1,
                (int) ((float) x),
                (int) ((float) y),
                (int) this.octaves,
                this.falloff,
                1f / this.scale);

        if (noise >= this.threshold1
                && (this.target == Blocks.air || tile.getFloor() == this.target || tile.getBlock() == this.target)) {

            if (this.floor1 != Blocks.air) {
                tile.setFloor(this.floor1);
            }

            if (tile.getBlock().solid && this.block != Blocks.air && tile.getBlock() != Blocks.air) {
                tile.setBlock(this.block);
            }

            if (noise >= this.threshold2 && this.floor2 != Blocks.air) {
                tile.setFloor(this.floor2);
            }
        }
    }
}
