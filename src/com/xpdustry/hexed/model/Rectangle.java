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

record Rectangle(int identifier, int x, int y, int w, int h) implements Hex {

    /**
     * Retrieve the rectangle's identifier.
     *
     * @return the identifier associated with this rectangle
     */
    @Override
    public int getIdentifier() {
        return this.identifier;
    }

    /**
     * Retrieves the rectangle's tile X coordinate.
     *
     * @return the X coordinate in tile space
     */
    @Override
    public int getTileX() {
        return this.x;
    }

    /**
     * Gets the tile Y coordinate.
     *
     * @return the tile Y coordinate
     */
    @Override
    public int getTileY() {
        return this.y;
    }

    /**
     * Calculates the tile diameter as the integer average of the rectangle's width and height.
     *
     * @return the tile diameter computed as (w + h) / 2 using integer division
     */
    @Override
    public int getTileDiameter() {
        return (this.w + this.h) / 2;
    }

    /**
     * Checks whether the given point lies within this rectangle's axis-aligned bounds.
     *
     * @param x the x-coordinate of the point to test
     * @param y the y-coordinate of the point to test
     * @return true if the point is inside the rectangle's bounds (left/top inclusive, right/bottom exclusive), false otherwise
     */
    @Override
    public boolean contains(final int x, final int y) {
        final var hw = (this.w / 2);
        final var hh = (this.h / 2);
        return x >= this.x - hw && x < this.x + hw && y >= this.y - hh && y < this.y + hh;
    }
}
