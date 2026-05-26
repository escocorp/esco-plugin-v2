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

import arc.math.geom.Intersector;

record Hexagon(int identifier, int x, int y, int diameter) implements Hex {

    /**
     * Obtain the hexagon's unique identifier.
     *
     * @return the identifier value assigned to this hexagon
     */
    @Override
    public int getIdentifier() {
        return this.identifier;
    }

    /**
     * Gets the tile x coordinate of this hexagon.
     *
     * @return the tile x coordinate
     */
    @Override
    public int getTileX() {
        return this.x;
    }

    /**
     * Tile Y coordinate of this hexagon.
     *
     * @return the tile Y coordinate
     */
    @Override
    public int getTileY() {
        return this.y;
    }

    /**
     * Gets the hexagon's tile diameter.
     *
     * @return the diameter that defines this hexagon's size
     */
    @Override
    public int getTileDiameter() {
        return this.diameter;
    }

    /**
     * Determines whether a point lies inside this hexagon.
     *
     * @param x the x coordinate of the point to test
     * @param y the y coordinate of the point to test
     * @return `true` if the point (x, y) is inside this hexagon, `false` otherwise
     */
    @Override
    public boolean contains(final int x, final int y) {
        return Intersector.isInsideHexagon(this.x, this.y, this.diameter, x, y);
    }
}
