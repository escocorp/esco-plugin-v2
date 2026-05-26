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

import com.xpdustry.hexed.HexedCaptureProgress;
import com.xpdustry.hexed.model.Hex;
import java.time.Duration;
import java.util.Collections;
import java.util.List;
import mindustry.content.Items;
import mindustry.game.Gamemode;
import mindustry.type.ItemStack;

@SuppressWarnings("this-escape")
public class SimpleHexedMapContext extends SimpleMapContext implements HexedMapContext {

    private List<Hex> hexes = Collections.emptyList();
    private Duration duration = DEFAULT_GAME_DURATION;
    private ImmutableSchematic schematic = DEFAULT_BASE_SCHEMATIC;
    private HexedCaptureProgress calculator = HexedCaptureProgress.anuke();

    {
        // TODO Use an "apply" rules instead of get
        final var rules = this.getRules();
        Gamemode.pvp.apply(rules);
        rules.pvp = true;
        rules.tags.put(HEXED_PRESENCE_FLAG, "true");
        rules.loadout = ItemStack.list(
                Items.copper,
                300,
                Items.lead,
                500,
                Items.graphite,
                150,
                Items.metaglass,
                150,
                Items.silicon,
                150,
                Items.plastanium,
                50);
        rules.buildCostMultiplier = 1f;
        rules.buildSpeedMultiplier = 0.75F;
        rules.blockHealthMultiplier = 1.2f;
        rules.unitBuildSpeedMultiplier = 1f;
        rules.polygonCoreProtection = true;
        rules.unitDamageMultiplier = 1.1f;
        rules.canGameOver = false;
        this.setRules(rules);
    }

    /**
     * Gets the list of hexes configured for this map context.
     *
     * @return the stored list of {@code Hex} objects used by the context
     */
    @Override
    public List<Hex> getHexes() {
        return this.hexes;
    }

    /**
     * Replaces the stored hex list with an immutable copy of the provided list.
     *
     * @param hexes the list of hexes to store; a defensive, unmodifiable copy will be kept
     * @throws NullPointerException if {@code hexes} is null or contains a null element
     */
    @Override
    public void setHexes(final List<Hex> hexes) {
        this.hexes = List.copyOf(hexes);
    }

    /**
     * Gets the configured game duration for this map context.
     *
     * @return the configured game duration used for matches in this context
     */
    @Override
    public Duration getDuration() {
        return this.duration;
    }

    /**
     * Sets the game duration used for matches on this map context.
     *
     * @param duration the duration of a match (must not be null)
     */
    @Override
    public void setDuration(final Duration duration) {
        this.duration = duration;
    }

    /**
     * Retrieves the base schematic used by this map context.
     *
     * @return the current base ImmutableSchematic for the map
     */
    @Override
    public ImmutableSchematic getBaseSchematic() {
        return this.schematic;
    }

    /**
     * Sets the base schematic used as the map's starting structure.
     *
     * @param schematic the schematic to use as the map's base
     */
    @Override
    public void setBaseSchematic(final ImmutableSchematic schematic) {
        this.schematic = schematic;
    }

    /**
     * Gets the capture progress calculator used to compute hex capture progress.
     *
     * @return the current HexedCaptureProgress instance used by this context
     */
    @Override
    public HexedCaptureProgress getCaptureCalculator() {
        return this.calculator;
    }

    /**
     * Configures the capture-progress calculator used by this context to compute hex capture progress.
     *
     * @param calculator the HexedCaptureProgress implementation to use
     */
    @Override
    public void setCaptureCalculator(final HexedCaptureProgress calculator) {
        this.calculator = calculator;
    }
}
