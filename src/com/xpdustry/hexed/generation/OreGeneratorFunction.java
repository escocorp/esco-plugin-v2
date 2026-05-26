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

import arc.util.noise.Simplex;
import java.util.ArrayList;
import java.util.List;
import mindustry.content.Blocks;
import mindustry.world.Block;
import mindustry.world.blocks.environment.OreBlock;

import static mindustry.Vars.content;

public final class OreGeneratorFunction extends GeneratorFunction {

    private float scale = 23;
    private float threshold = 0.81f;
    private float octaves = 2f;
    private float falloff = 0.3f;
    private float tilt = 0f;

    private OreBlock ore = (OreBlock) Blocks.oreCopper;
    private Block target = Blocks.air;

    /**
     * Creates a list of OreGeneratorFunction instances configured from overlay blocks that declare `oreDefault`.
     *
     * Each returned function is initialized with the block cast to `OreBlock` and copies that block's `oreThreshold`,
     * `oreScale`, and ore reference into the function.
     *
     * @return a list of OreGeneratorFunction objects built from overlay ore blocks
     */
    public static List<OreGeneratorFunction> getDefaultOreFunctions() {
        final List<OreGeneratorFunction> functions = new ArrayList<>();
        for (final var block : content.blocks().select(b -> b.isOverlay() && b.asFloor().oreDefault)) {
            final var function = new OreGeneratorFunction();
            final var ore = (OreBlock) block;
            function.threshold = ore.oreThreshold;
            function.scale = ore.oreScale;
            function.ore = ore;
            functions.add(function);
        }
        return functions;
    }

    /**
     * Creates the default ore generator functions adjusted for "hexed" terrain.
     *
     * Each returned function has its threshold decreased by 0.05. Additionally, a
     * special "scrap" OreGeneratorFunction (ore set to `Blocks.oreScrap` and its
     * scale slightly increased) is inserted at index 0.
     *
     * @return a list of configured OreGeneratorFunction instances with lowered thresholds and a prepended scrap entry
     */
    public static List<OreGeneratorFunction> getDefaultHexedOreFunctions() {
        final var functions = getDefaultOreFunctions();
        for (final var function : functions) {
            function.setThreshold(function.getThreshold() - 0.05F);
        }

        final var scrap = new OreGeneratorFunction();
        scrap.setOre(Blocks.oreScrap);
        scrap.setScale(scrap.getScale() + 2 / 2.1F);
        functions.add(0, scrap);

        return functions;
    }

    /**
     * Gets the noise scale controlling ore placement frequency.
     *
     * @return the current scale; larger values produce lower-frequency noise (larger features)
     */
    public float getScale() {
        return this.scale;
    }

    /**
     * Sets the noise scale used to control ore placement frequency.
     *
     * @param scale the scale value; the noise frequency used is 1 / scale (higher values produce lower-frequency, larger ore patches)
     */
    public void setScale(final float scale) {
        this.scale = scale;
    }

    /**
     * Threshold noise cutoff used to decide ore placement.
     *
     * @return the threshold value; ore is placed when noise is greater than this value
     */
    public float getThreshold() {
        return this.threshold;
    }

    /**
     * Set the noise cutoff threshold used to decide whether ore is placed.
     *
     * @param threshold noise cutoff; a tile's computed noise must be greater than this value for the ore overlay to be applied
     */
    public void setThreshold(final float threshold) {
        this.threshold = threshold;
    }

    /**
     * The number of octaves used by the simplex noise generator for ore placement.
     *
     * @return the octaves factor controlling how many noise layers are combined (higher values add finer detail)
     */
    public float getOctaves() {
        return this.octaves;
    }

    /**
     * Sets the number of octaves used when generating simplex noise for ore placement.
     *
     * @param octaves the number of noise octaves to use (affects noise detail)
     */
    public void setOctaves(final float octaves) {
        this.octaves = octaves;
    }

    /**
     * Gets the falloff parameter used by the noise generator.
     *
     * @return the falloff value that controls amplitude reduction between octaves
     */
    public float getFalloff() {
        return this.falloff;
    }

    /**
     * Sets the falloff parameter used by the simplex noise generator when creating ore patterns.
     *
     * @param falloff the falloff value controlling amplitude reduction between noise octaves
     */
    public void setFalloff(final float falloff) {
        this.falloff = falloff;
    }

    /**
     * Gets the tilt factor used to skew the noise sampling along the x axis.
     *
     * @return the tilt value; applied as `x * tilt` to the noise y input to bias ore distribution horizontally
     */
    public float getTilt() {
        return this.tilt;
    }

    /**
     * Sets the noise tilt that shifts the noise's y input based on the x coordinate.
     *
     * @param tilt amount added to the noise y input per unit of x
     */
    public void setTilt(final float tilt) {
        this.tilt = tilt;
    }

    /**
     * Gets the ore block used as the overlay when this generator places ore.
     *
     * @return the configured OreBlock used as the overlay
     */
    public OreBlock getOre() {
        return this.ore;
    }

    /**
     * Sets the ore overlay block used by this generator.
     *
     * @param ore the ore block to use; must be an instance of {@code OreBlock}
     */
    public void setOre(final Block ore) {
        this.ore = (OreBlock) ore;
    }

    /**
     * The placement target used to filter which tiles can receive the ore overlay.
     *
     * @return the target Block; if the target is Blocks.air, the ore is allowed regardless of floor/overlay type
     */
    public Block getTarget() {
        return this.target;
    }

    /**
     * Sets the placement target used to filter where this ore may be placed.
     *
     * @param target the block that a tile's floor or overlay must match to allow placement; use {@code Blocks.air} to allow placement on any compatible tile
     */
    public void setTarget(final Block target) {
        this.target = target;
    }

    /**
     * Evaluates placement conditions and sets the given tile's overlay to this function's ore when they are satisfied.
     *
     * <p>The overlay is set when all of the following are true: the computed 2D simplex noise at (x,y) exceeds
     * the configured threshold, the tile's current overlay is not {@code Blocks.spawn}, the tile matches the configured
     * target (target is {@code Blocks.air}, or the tile's floor or overlay equals the target), and the tile's floor has a surface.</p>
     *
     * @param x    the x-coordinate of the tile
     * @param y    the y-coordinate of the tile
     * @param tile the map tile to evaluate and potentially modify
     */
    @Override
    public void accept(final int x, final int y, final MapTile tile) {
        final float noise = Simplex.noise2d(
                this.getSeed(), this.octaves, this.falloff, 1f / this.scale, (float) x + 10, y + x * this.tilt + 10);
        if (noise > this.threshold
                && tile.getOverlay() != Blocks.spawn
                && (this.target == Blocks.air || tile.getFloor() == this.target || tile.getOverlay() == this.target)
                && tile.getFloor().hasSurface()) {
            tile.setOverlay(this.ore);
        }
    }
}
