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

import mindustry.game.Rules;

public interface MapContext {

    /**
 * Update the map's stored dimensions to the provided width and height.
 *
 * @param width  the new map width in tiles
 * @param height the new map height in tiles
 */
void resize(final int width, final int height);

    /**
 * Gets the current map width in tiles.
 *
 * @return the map width (number of tiles horizontally)
 */
int getWidth();

    /**
 * Get the current map height in tiles.
 *
 * @return the map height (number of rows)
 */
int getHeight();

    /**
 * Retrieve the tile at the specified map coordinates.
 *
 * @param x the x-coordinate (column) of the tile
 * @param y the y-coordinate (row) of the tile
 * @return the MapTile located at (x, y)
 */
MapTile getTile(int x, int y);

    /**
 * Gets the Rules that govern generation and validation for this map context.
 *
 * @return the Rules instance used by this map context
 */
Rules getRules();

    /**
 * Set the game rules used by this map context.
 *
 * These rules govern generation, validation, and other constraints applied to tiles and map logic.
 *
 * @param rules the Rules instance to associate with this context
 */
void setRules(final Rules rules);

    /**
 * Gets the map's name.
 *
 * @return the map's name
 */
String getMapName();

    /**
 * Sets the map's display name used to identify the map.
 *
 * @param name the new map name, or null to unset the name
 */
void setMapName(final String name);

    /**
 * Apply the given action to every tile in the map.
 *
 * @param action the consumer invoked for each tile; receives the tile's x and y coordinates and the corresponding MapTile instance
 */
void forEachTile(final TileConsumer action);

    /**
 * Applies the given TileConsumer to every tile in the rectangular region defined by an origin and size.
 *
 * The region is specified by its top-left corner (x, y) and its width (w) and height (h); the consumer
 * is invoked once per tile contained in that rectangle.
 *
 * @param x      x-coordinate of the rectangle's origin (left)
 * @param y      y-coordinate of the rectangle's origin (top)
 * @param w      width of the rectangle in tiles
 * @param h      height of the rectangle in tiles
 * @param action consumer to invoke for each tile in the region
 */
void forEachTile(final int x, final int y, final int w, final int h, final TileConsumer action);
}
