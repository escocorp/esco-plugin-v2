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
import java.util.List;
import java.util.Objects;
import mindustry.game.Schematics;

public interface HexedMapContext extends MapContext {

    ImmutableSchematic DEFAULT_BASE_SCHEMATIC = loadDefaultBaseSchematic();

    String HEXED_PRESENCE_FLAG = "xpdustry:hexed-reloaded";

    Duration DEFAULT_GAME_DURATION = Duration.ofMinutes(90L);

    /**
 * Retrieves the list of hexes that define the generated map.
 *
 * @return the list of {@link Hex} objects representing the map's hexes
 */
List<Hex> getHexes();

    /**
 * Set the collection of hex tiles that define the map layout.
 *
 * @param hexes the list of hex tiles defining the map layout
 */
void setHexes(final List<Hex> hexes);

    /**
 * Gets the configured game duration for the generated map.
 *
 * @return the configured game duration for the map
 */
Duration getDuration();

    /**
 * Set the total game duration for the map.
 *
 * @param duration the desired game duration for the generated map
 */
void setDuration(final Duration duration);

    /**
 * Retrieves the immutable base schematic used as the starting layout for generated maps.
 *
 * @return the base {@code ImmutableSchematic} used for map generation
 */
ImmutableSchematic getBaseSchematic();

    /**
 * Sets the schematic used as the map's base foundation.
 *
 * @param schematic the base schematic to apply to the map
 */
void setBaseSchematic(final ImmutableSchematic schematic);

    /**
 * Retrieves the capture progress calculator used for hex capture mechanics.
 *
 * @return the current {@link HexedCaptureProgress} instance responsible for computing capture progress
 */
HexedCaptureProgress getCaptureCalculator();

    /**
 * Sets the capture progress calculator used to compute hex capture state and progress.
 *
 * @param calculator the HexedCaptureProgress instance to use for capture calculations
 */
void setCaptureCalculator(final HexedCaptureProgress calculator);

    /**
     * Loads the bundled default base schematic for hexed maps.
     *
     * The schematic is read from the packaged resource "/com/xpdustry/hexed/default.msch".
     *
     * @return the default {@code ImmutableSchematic} used as the base map
     * @throws RuntimeException if the resource cannot be found, read, or parsed
     */
    private static ImmutableSchematic loadDefaultBaseSchematic() {
        try (final var stream = SimpleHexedMapContext.class.getResourceAsStream("/com/xpdustry/hexed/default.msch")) {
            return new ImmutableSchematic(Schematics.read(Objects.requireNonNull(stream)));
        } catch (final Exception e) {
            throw new RuntimeException("Failed to load the default base schematic.", e);
        }
    }
}
