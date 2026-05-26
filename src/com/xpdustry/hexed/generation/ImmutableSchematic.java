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

import com.xpdustry.distributor.api.collection.MindustryCollections;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;
import mindustry.game.Schematic;
import mindustry.world.Block;
import org.checkerframework.checker.nullness.qual.Nullable;

public final class ImmutableSchematic {

    private final List<Tile> tiles;
    private final int width;
    private final int height;
    private final SortedSet<String> labels;
    private final Map<String, String> tags;

    /**
     * Creates an immutable copy of the given schematic.
     *
     * Copies tiles, dimensions, labels, and tags from the source into unmodifiable, defensive collections.
     * Tiles are converted into ImmutableSchematic.Tile instances (configuration may be null, rotation mapped to the Tile.Rotation enum),
     * labels are stored as a sorted unmodifiable set, and tags are stored as an unmodifiable map.
     *
     * @param schematic the source schematic to copy
     */
    public ImmutableSchematic(final Schematic schematic) {
        this.tiles = MindustryCollections.immutableList(schematic.tiles).stream()
                .map(stile -> new ImmutableSchematic.Tile(
                        stile.x, stile.y, stile.block, stile.config, Tile.Rotation.from(stile.rotation)))
                .toList();
        this.width = schematic.width;
        this.height = schematic.height;
        this.labels =
                Collections.unmodifiableSortedSet(new TreeSet<>(MindustryCollections.immutableList(schematic.labels)));
        this.tags = Map.copyOf(MindustryCollections.immutableMap(schematic.tags));
    }

    /**
     * Creates an immutable schematic by copying the provided data and freezing collection inputs.
     *
     * The constructor makes defensive copies of the provided lists and maps so subsequent
     * modifications to the arguments do not affect the instance. The label set is copied
     * into a sorted, unmodifiable {@link java.util.SortedSet}.
     *
     * @param tiles  list of schematic tiles; the list is copied with {@link List#copyOf(List)}
     *               and stored as an unmodifiable list
     * @param width  schematic width in tiles
     * @param height schematic height in tiles
     * @param labels set of label strings; the set is copied into a new {@link java.util.TreeSet}
     *               to ensure sorting and then wrapped as an unmodifiable sorted set
     * @param tags   map of schematic tags; the map is copied with {@link Map#copyOf(Map)}
     */
    public ImmutableSchematic(
            final List<Tile> tiles,
            final int width,
            final int height,
            final SortedSet<String> labels,
            final Map<String, String> tags) {
        this.tiles = List.copyOf(tiles);
        this.width = width;
        this.height = height;
        this.labels = Collections.unmodifiableSortedSet(new TreeSet<>(labels));
        this.tags = Map.copyOf(tags);
    }

    /**
     * Immutable list of tiles contained in this schematic.
     *
     * @return an unmodifiable list of Tile records representing the schematic's tiles
     */
    public List<Tile> getTiles() {
        return this.tiles;
    }

    /**
     * Provides the schematic's width in tiles.
     *
     * @return the width of the schematic in tiles
     */
    public int getWidth() {
        return this.width;
    }

    /**
     * The schematic's height measured in tiles.
     *
     * @return the height of the schematic in tiles
     */
    public int getHeight() {
        return this.height;
    }

    /**
     * Get the sorted, unmodifiable set of schematic labels.
     *
     * @return the sorted, unmodifiable set of label strings
     */
    public SortedSet<String> getLabels() {
        return this.labels;
    }

    /**
     * Accesses the schematic's tags.
     *
     * @return an unmodifiable map of tag keys to tag values for this schematic.
     */
    public Map<String, String> getTags() {
        return this.tags;
    }

    /**
     * Retrieve the schematic's name from its tags, defaulting to "unknown" when not present.
     *
     * @return the value of the "name" tag, or "unknown" if the tag is absent
     */
    public String getName() {
        return this.tags.getOrDefault("name", "unknown");
    }

    /**
     * Retrieve the schematic description stored in tags, or an empty string if none is present.
     *
     * @return the value of the "description" tag, or an empty string if the tag is absent
     */
    public String getDescription() {
        return this.tags.getOrDefault("description", "");
    }

    public record Tile(int x, int y, Block block, @Nullable Object configuration, Rotation rotation) {

        public enum Rotation {
            RIGHT,
            TOP,
            LEFT,
            BOTTOM;

            /**
             * Maps a numeric rotation index to the corresponding Rotation enum value.
             *
             * @param rotation the rotation index (0→RIGHT, 1→TOP, 2→LEFT, 3→BOTTOM); values outside 0–3 wrap by modulo 4
             * @return the matching Rotation (`RIGHT`, `TOP`, `LEFT`, or `BOTTOM`)
             */
            static Rotation from(final byte rotation) {
                return values()[rotation % 4];
            }
        }
    }
}
