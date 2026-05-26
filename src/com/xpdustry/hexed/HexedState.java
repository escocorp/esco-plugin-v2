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
package com.xpdustry.hexed;

import com.xpdustry.hexed.generation.ImmutableSchematic;
import com.xpdustry.hexed.model.Hex;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import mindustry.game.Team;
import org.jetbrains.annotations.Nullable;

public interface HexedState {

    /**
 * Retrieve all hexes present in the current game state.
 *
 * @return a list of all Hex objects in the state
 */
List<Hex> getHexes();

    /**
 * Retrieve all hexes controlled by the specified team.
 *
 * @param team the team whose controlled hexes to return
 * @return a list of hexes currently controlled by the given team; empty if none
 */
List<Hex> getControlled(final Team team);

    /**
 * Determines the controlling team for a hex.
 *
 * @param hex the hex to query
 * @return the team that controls the specified hex, or {@code null} if no team controls it; may return {@code Team.derelict}
 */
@Nullable Team getController(final Hex hex);

    /**
 * Retrieves the hex located at the given grid coordinates.
 *
 * @param x the hex's x coordinate
 * @param y the hex's y coordinate
 * @return the Hex at (x, y), or null if no hex exists at those coordinates
 */
@Nullable Hex getHex(final int x, final int y);

    /**
 * Indicates whether the given hex is currently available.
 *
 * @param hex the hex to query
 * @return `true` if the hex is available according to this state, `false` otherwise
 */
boolean isAvailable(final Hex hex);

    /**
 * Determines whether the specified team is in a dying state.
 *
 * @param team the team to check
 * @return true if the team is in a dying state, false otherwise
 */
boolean isDying(final Team team);

    /**
 * Provides the base immutable schematic used by this game mode.
 *
 * @return the immutable base schematic that defines the mode's default structure
 */
ImmutableSchematic getBaseSchematic();

    /**
 * Returns the progress of the given team toward controlling the specified hex.
 *
 * @param hex  the hex whose progress is being queried
 * @param team the team whose progress to retrieve
 * @return a float where larger values indicate greater progress toward controlling the hex
 */
float getProgress(final Hex hex, final Team team);

    /**
 * The overall duration for this game state (the match length or mode-specific time limit).
 *
 * @return the configured total duration for the state
 */
Duration getDuration();

    /**
 * Get the current match counter value used for countdowns and accumulators.
 *
 * @return the current counter as a {@link Duration}
 */
Duration getCounter();

    /**
 * Set the current counter value for the game state.
 *
 * @param counter the new countdown/accumulator value to use for the state
 */
void setCounter(final Duration counter);

    /**
 * Increase the stored counter value by the specified delta.
 *
 * @param delta the amount to add to the current counter; units and any conversion are implementation-defined (matching the unit used by {@code getCounter()})
 */
void incrementCounter(final float delta);

    /**
     * Build a per-team leaderboard counting how many hexes each team controls.
     *
     * The returned map associates each `Team` (excluding `null` and `Team.derelict`) with the number
     * of hexes it controls; each controlled hex contributes 1 to its team's count.
     *
     * @return a map from `Team` to the number of hexes controlled by that team
     */
    default Map<Team, Integer> getLeaderboard() {
        return this.getHexes().stream()
                .map(this::getController)
                .filter(team -> team != null && team != Team.derelict)
                .collect(Collectors.toMap(team -> team, team -> 1, Integer::sum));
    }
}
