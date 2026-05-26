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

import arc.math.geom.Point2;
import arc.struct.IntFloatMap;
import arc.struct.IntMap;
import arc.util.Time;
import arc.util.Timekeeper;
import com.xpdustry.hexed.generation.ImmutableSchematic;
import com.xpdustry.hexed.model.Hex;
import java.time.Duration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import mindustry.Vars;
import mindustry.game.Team;
import org.checkerframework.checker.nullness.qual.Nullable;

final class HexedStateImpl implements HexedState {

    private final Map<Hex, Team> controllers = new HashMap<>();
    private final List<Hex> hexes;
    private final Set<Team> dying = new HashSet<>();
    private final IntMap<Hex> positions = new IntMap<>();
    private final IntMap<Timekeeper> spawnTimers = new IntMap<>();
    private final IntMap<IntFloatMap> progress = new IntMap<>();
    private final Duration duration;
    private float counter = 0f;
    private final ImmutableSchematic base;
    private final HexedCaptureProgress calculator;

    /**
     * Creates a new HexedStateImpl storing the provided base schematic, capture progress
     * calculator, participating hexes, and match duration.
     *
     * Initializes internal state and indexes each hex by its packed tile coordinates.
     *
     * @param base       the base schematic associated with this state
     * @param calculator the capture-progress calculator used to compute per-hex progress
     * @param hexes      the list of hexes participating in the match (will be copied)
     * @param duration   the total duration for the match or mode
     */
    HexedStateImpl(
            final ImmutableSchematic base,
            final HexedCaptureProgress calculator,
            final List<Hex> hexes,
            final Duration duration) {
        this.base = base;
        this.calculator = calculator;
        this.duration = duration;
        this.hexes = List.copyOf(hexes);
        for (final var hex : this.hexes) {
            this.positions.put(Point2.pack(hex.getTileX(), hex.getTileY()), hex);
        }
    }

    /**
     * Provides the list of hex tiles managed by this state.
     *
     * @return an immutable list of Hex objects included in this state
     */
    @Override
    public List<Hex> getHexes() {
        return this.hexes;
    }

    /**
     * Return the list of hexes currently controlled by the given team.
     *
     * @param team the team whose controlled hexes to retrieve
     * @return a list of hexes controlled by {@code team}
     */
    @Override
    public List<Hex> getControlled(final Team team) {
        return this.hexes.stream()
                .filter(hex -> this.getController(hex) == team)
                .toList();
    }

    /**
     * Retrieves the current controller for the specified hex.
     *
     * @return the controlling {@link Team} for the hex, or {@code null} if the hex is uncontrolled
     */
    @Override
    public @Nullable Team getController(final Hex hex) {
        return this.controllers.get(hex);
    }

    /**
     * Retrieve the Hex at the given tile coordinates.
     *
     * @param x the tile X coordinate
     * @param y the tile Y coordinate
     * @return the Hex mapped to the specified tile coordinates, or `null` if none exists
     */
    @Override
    public @Nullable Hex getHex(final int x, final int y) {
        return this.positions.get(Point2.pack(x, y));
    }

    /**
     * Determines whether a hex tile is available for spawning or capture.
     *
     * A hex is available when it has no current controller and its per-hex spawn timer has expired.
     *
     * @param hex the hex tile to check
     * @return `true` if the hex is available, `false` otherwise
     */
    @Override
    public boolean isAvailable(final Hex hex) {
        return (this.getController(hex) == null)
                && this.spawnTimers
                        .get(Point2.pack(hex.getTileX(), hex.getTileY()), () -> new Timekeeper(6 * 60))
                        .get();
    }

    /**
     * Resets the per-hex spawn timer so the given hex becomes unavailable until its spawn delay elapses.
     *
     * @param hex the hex whose spawn timer will be reset
     */
    public void resetSpawnTimer(final Hex hex) {
        this.spawnTimers
                .get(Point2.pack(hex.getTileX(), hex.getTileY()), () -> new Timekeeper(6 * 60))
                .reset();
    }

    /**
     * Checks whether the given team is marked as dying.
     *
     * @param team the team to check
     * @return `true` if the team is marked as dying, `false` otherwise
     */
    @Override
    public boolean isDying(final Team team) {
        return this.dying.contains(team);
    }

    /**
     * Marks or unmarks a team as dying in this state.
     *
     * @param team  the team to update
     * @param dying true to mark the team as dying, false to clear the dying state
     */
    public void setDying(final Team team, final boolean dying) {
        if (dying) {
            this.dying.add(team);
        } else {
            this.dying.remove(team);
        }
    }

    /**
     * Returns the total configured duration for the match.
     *
     * @return the configured total Duration for the match
     */
    @Override
    public Duration getDuration() {
        return this.duration;
    }

    /**
     * Set the internal countdown counter from the given duration.
     *
     * @param counter the duration to store; the value is converted and kept as seconds in the internal counter
     */
    @Override
    public void setCounter(final Duration counter) {
        this.counter = counter.toMillis() * Time.toSeconds;
    }

    /**
     * Get the current internal counter as a Duration.
     *
     * @return the internal counter value represented as a Duration
     */
    @Override
    public Duration getCounter() {
        return Duration.ofMillis((long) ((this.counter / Time.toSeconds) * 1000L));
    }

    /**
     * Adds the specified amount of time to the internal counter.
     *
     * @param delta amount of time, in seconds, to add to the counter
     */
    @Override
    public void incrementCounter(final float delta) {
        this.counter += delta;
    }

    /**
     * Retrieve the base schematic associated with this state.
     *
     * @return the immutable base schematic used by this HexedState
     */
    @Override
    public ImmutableSchematic getBaseSchematic() {
        return this.base;
    }

    /**
     * Computes the capture progress for a team on a specific hex, scaled to the current controller when the controller is a different team.
     *
     * @param hex  the hex tile to query
     * @param team the team whose progress to return
     * @return the team's progress value for the hex; if another team currently controls the hex, returns (teamProgress / controllerProgress) * 100F, otherwise returns the raw progress value
     */
    @Override
    public float getProgress(final Hex hex, final Team team) {
        final var progress = this.getProgress0(hex, team);
        final var controller = this.getController(hex);
        if (controller != null && controller != team) {
            return (progress / this.getProgress0(hex, controller)) * 100F;
        }
        return progress;
    }

    /**
     * Get the stored raw capture progress for the given team at the specified hex as a percentage.
     *
     * @param hex  the hex tile to query
     * @param team the team whose progress to read
     * @return the team's progress value for the hex multiplied by 100 (percentage)
     */
    private float getProgress0(final Hex hex, final Team team) {
        return this.progress
                        .get(Point2.pack(hex.getTileX(), hex.getTileY()), () -> new IntFloatMap(4))
                        .get(team.id)
                * 100F;
    }

    /**
     * Recalculates capture progress for the given hex and updates its controller.
     *
     * Recomputes per-team progress for the specified hex, stores the updated progress,
     * and sets the hex's controller to the active team with the highest progress when
     * that team's progress is greater than or equal to 1.0; otherwise the controller
     * is cleared.
     */
    public void updateProgress(final Hex hex) {
        final var progress = this.progress.get(Point2.pack(hex.getTileX(), hex.getTileY()), () -> new IntFloatMap(4));
        progress.clear();
        this.calculator.calculate(hex, progress);
        final var data = Vars.state.teams.getActive().max(t -> progress.get(t.team.id));
        if (data != null && progress.get(data.team.id) >= 1F) {
            this.controllers.put(hex, data.team);
        } else {
            this.controllers.put(hex, null);
        }
    }
}
