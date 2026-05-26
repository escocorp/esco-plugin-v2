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

import mindustry.content.Blocks;
import mindustry.game.Team;
import mindustry.type.Item;
import mindustry.type.Liquid;
import mindustry.type.UnitType;
import mindustry.world.Block;
import mindustry.world.blocks.environment.Floor;
import org.checkerframework.checker.nullness.qual.Nullable;

public final class MapTile implements Cloneable {

    private Block block = Blocks.stoneWall;
    private Block overlay = Blocks.air;
    private Floor floor = Blocks.stone.asFloor();
    private Building building = new Building();

    /**
     * Accesses the per-tile Building state container.
     *
     * @return the Building instance associated with this tile
     */
    public Building getBuilding() {
        return this.building;
    }

    /**
     * Gets the tile's base block.
     *
     * @return the tile's base {@link Block}
     */
    public Block getBlock() {
        return this.block;
    }

    /**
     * Sets the tile's base block and clears the tile's building configuration.
     *
     * @param block the new base Block for this tile
     */
    public void setBlock(final Block block) {
        this.block = block;
        this.getBuilding().setConfiguration(null);
    }

    /**
     * Gets the tile's overlay block.
     *
     * @return the overlay block; defaults to {@code Blocks.air}
     */
    public Block getOverlay() {
        return this.overlay;
    }

    /**
     * Sets the overlay block for this tile.
     *
     * @param overlay the block to use as the tile's overlay (use {@code Blocks.air} to clear the overlay)
     */
    public void setOverlay(final Block overlay) {
        this.overlay = overlay;
    }

    /**
     * Gets the floor material used by this tile.
     *
     * @return the tile's floor material
     */
    public Floor getFloor() {
        return this.floor;
    }

    /**
     * Sets the tile's floor material.
     *
     * @param floor the Floor to assign to this tile
     */
    public void setFloor(final Floor floor) {
        this.floor = floor;
    }

    /**
     * Creates a copy of this MapTile where the nested Building is cloned.
     *
     * The returned MapTile is a separate instance whose `building` field is a cloned Building.
     *
     * @return a MapTile whose Building has been cloned from this instance
     */
    @Override
    public MapTile clone() {
        try {
            final var clone = (MapTile) super.clone();
            clone.building = clone.building.clone();
            return clone;
        } catch (final CloneNotSupportedException e) {
            throw new RuntimeException(e);
        }
    }

    public final class Building implements Cloneable {

        private float health = -1;
        private @Nullable Object configuration = null;
        private Team team = Team.derelict;

        /**
 * Initializes a Building; private to restrict instantiation to the enclosing MapTile.
 */
private Building() {}

        /**
         * Provide the building's effective health, falling back to the parent tile's block health when unset.
         *
         * @return the building's health; if the stored health is less than 0, returns MapTile.this.block.health
         */
        public float getHealth() {
            return this.health < 0 ? MapTile.this.block.health : this.health;
        }

        /**
         * Sets the building's health; values less than -1 are clamped to -1 so the tile will use its block's health.
         *
         * @param health the new health value; values below -1 will be stored as -1
         */
        public void setHealth(final float health) {
            this.health = Math.max(health, -1);
        }

        /**
         * Get the building's stored configuration object, or null if none.
         *
         * @return the stored configuration object, or `null` if there is no configuration
         */
        public @Nullable Object getConfiguration() {
            return this.configuration;
        }

        /**
         * Sets the building configuration for this tile and validates it against the parent block.
         *
         * If `configuration` is non-null, its runtime type is normalized to `Item.class`, `Block.class`,
         * `Liquid.class`, or `UnitType.class` when applicable, and the parent tile's `block` must be
         * configurable and contain a matching entry in its `configurations` map.
         *
         * @param configuration the configuration object to assign, or `null` to clear it
         * @throws IllegalArgumentException if `configuration` is non-null but the parent block is not
         *                                  configurable or does not support the configuration's type
         */
        public void setConfiguration(final @Nullable Object configuration) {
            this.configuration = configuration;
            if (configuration != null) {
                Class<?> type = configuration.getClass();
                if (configuration instanceof Item) {
                    type = Item.class;
                } else if (configuration instanceof Block) {
                    type = Block.class;
                } else if (configuration instanceof Liquid) {
                    type = Liquid.class;
                } else if (configuration instanceof UnitType) {
                    type = UnitType.class;
                }
                if (!(MapTile.this.block.configurable && MapTile.this.block.configurations.containsKey(type))) {
                    throw new IllegalArgumentException(
                            "Unsupported configuration type for block " + MapTile.this.block + ": " + configuration);
                }
            }
        }

        /**
         * Gets the building's current team affiliation.
         *
         * @return the team that owns this building
         */
        public Team getTeam() {
            return this.team;
        }

        /**
         * Sets the building's owning team.
         *
         * @param team the team to assign to this building
         */
        public void setTeam(final Team team) {
            this.team = team;
        }

        /**
         * Creates and returns a shallow copy of this Building.
         *
         * @return a shallowly cloned Building instance
         * @throws RuntimeException if cloning is not supported (wraps the original CloneNotSupportedException)
         */
        @Override
        public Building clone() {
            try {
                return (Building) super.clone();
            } catch (final CloneNotSupportedException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
