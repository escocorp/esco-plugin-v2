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
package com.xpdustry.hexed.model;

import mindustry.Vars;

public interface Hex {

    /**
     * Create a hexagonal Hex with the given identifier and tile-space geometry.
     *
     * @param identifier unique identifier for the hex
     * @param x          tile-space X coordinate
     * @param y          tile-space Y coordinate
     * @param diameter   tile-space diameter of the hex
     * @return           a Hex instance representing a hexagon with the specified id and geometry
     */
    static Hex hexagon(final int identifier, final int x, final int y, final int diameter) {
        return new Hexagon(identifier, x, y, diameter);
    }

    /**
     * Create a square hex-shaped widget positioned in tile space with the given identifier.
     *
     * @param identifier unique integer identifier for the hex
     * @param x          tile-space X coordinate of the hex's origin
     * @param y          tile-space Y coordinate of the hex's origin
     * @param size       tile-space side length (width and height) of the square
     * @return           a new square Rectangle implementing Hex with the specified parameters
     */
    static Hex square(final int identifier, final int x, final int y, final int size) {
        return new Rectangle(identifier, x, y, size, size);
    }

    /**
     * Create a rectangular hex with the given identifier and tile-space geometry.
     *
     * @param identifier the unique identifier for the hex
     * @param x          the tile-space X coordinate of the rectangle's origin
     * @param y          the tile-space Y coordinate of the rectangle's origin
     * @param width      the rectangle's width in tile units
     * @param height     the rectangle's height in tile units
     * @return           a Hex representing an axis-aligned rectangle with the specified identifier and tile-space size
     */
    static Hex rectangle(final int identifier, final int x, final int y, final int width, final int height) {
        return new Rectangle(identifier, x, y, width, height);
    }

    /**
     * Get the X coordinate of this hex in world (pixel) space.
     *
     * @return the X coordinate in pixels (tile X converted to world units) as a float
     */
    default float getX() {
        return this.getTileX() * Vars.tilesize;
    }

    /**
     * Converts the tile-space Y coordinate to a world-space Y coordinate in pixels.
     *
     * @return the Y coordinate in world-space pixels (tile Y multiplied by Vars.tilesize)
     */
    default float getY() {
        return this.getTileY() * Vars.tilesize;
    }

    /**
     * Convert the shape's tile-space diameter to world-space units.
     *
     * @return the diameter in world-space units (pixels)
     */
    default float getDiameter() {
        return this.getTileDiameter() * Vars.tilesize;
    }

    /**
     * Get the world-space radius of the shape.
     *
     * @return the radius in world units (half of the shape's world-space diameter)
     */
    default float getRadius() {
        return this.getDiameter() / 2F;
    }

    /**
     * Get the radius in tile coordinates.
     *
     * @return the radius in tile units (tile diameter divided by 2)
     */
    default int getTileRadius() {
        return this.getTileDiameter() / 2;
    }

    /**
 * Gets the integer identifier of this hex.
 *
 * @return the identifier of this hex
 */
int getIdentifier();

    /**
 * Get the hex's X coordinate in tile-space.
 *
 * @return the X coordinate measured in tiles
 */
int getTileX();

    /**
 * Tile-space Y coordinate of this hex.
 *
 * @return the Y coordinate in tile units
 */
int getTileY();

    /**
 * The diameter of this shape in tile-space units.
 *
 * @return the diameter in tiles as an integer
 */
int getTileDiameter();

    /**
 * Check whether the point at the given coordinates lies within this hex's area.
 *
 * @param x the x coordinate in this hex's coordinate space (implementation-defined)
 * @param y the y coordinate in this hex's coordinate space (implementation-defined)
 * @return `true` if the point is inside the shape, `false` otherwise
 */
boolean contains(final int x, final int y);
}
