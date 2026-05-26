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

public class SimpleMapContext implements MapContext {

    private int width = 1;
    private int height = 1;
    private MapTile[][] tiles = {{new MapTile()}};
    private String name = "Unknown";
    private Rules rules = new Rules();

    /**
     * Resizes the internal tile grid to the given dimensions, reallocating and initializing every cell.
     *
     * @param width  the new grid width; must be greater than or equal to 1
     * @param height the new grid height; must be greater than or equal to 1
     * @throws RuntimeException if {@code width} &lt; 1 or {@code height} &lt; 1 (message includes the invalid value)
     */
    @Override
    public void resize(final int width, final int height) {
        if (width < 1) throw new RuntimeException("Width cannot be lower than zero: " + width);
        if (height < 1) throw new RuntimeException("Height cannot be lower then zero: " + height);

        this.tiles = new MapTile[width][height];
        for (int y = 0; y < this.tiles.length; y++) {
            for (int x = 0; x < this.tiles[y].length; x++) {
                this.tiles[y][x] = new MapTile();
            }
        }

        this.width = width;
        this.height = height;
    }

    /**
     * Get the current map width in tiles.
     *
     * @return the map width (number of columns)
     */
    @Override
    public int getWidth() {
        return this.width;
    }

    /**
     * Retrieves the current map height in tiles.
     *
     * @return the height of the map (number of tile rows)
     */
    @Override
    public int getHeight() {
        return this.height;
    }

    /**
     * Accesses the map tile at the specified (x, y) coordinates.
     *
     * @param x the column index (horizontal coordinate), where 0 is the leftmost column
     * @param y the row index (vertical coordinate), where 0 is the topmost row
     * @return the MapTile located at the given coordinates
     * @throws ArrayIndexOutOfBoundsException if the coordinates are outside the map bounds
     */
    @Override
    public MapTile getTile(final int x, final int y) {
        return this.tiles[y][x];
    }

    /**
     * Provides a defensive copy of the stored game rules.
     *
     * @return a new Rules instance containing the same settings as the current rules
     */
    @Override
    public Rules getRules() {
        return this.rules.copy();
    }

    /**
     * Replaces the context's game rules with a copy of the provided rules.
     *
     * @param rules the rules to copy into this context; the context retains its own copy so subsequent mutations to the argument do not affect the stored rules
     */
    @Override
    public void setRules(final Rules rules) {
        this.rules = rules.copy();
    }

    /**
     * Retrieves the current map name.
     *
     * @return the current map name
     */
    @Override
    public String getMapName() {
        return this.name;
    }

    /**
     * Sets the map's name.
     *
     * @param name the new map name
     */
    @Override
    public void setMapName(final String name) {
        this.name = name;
    }

    /**
     * Iterates over every tile in the map and invokes the given action for each tile position.
     *
     * @param action consumer invoked with the tile's x-coordinate, y-coordinate, and the corresponding MapTile
     */
    @Override
    public void forEachTile(final TileConsumer action) {
        for (int y = 0; y < this.tiles.length; y++) {
            for (int x = 0; x < this.tiles[y].length; x++) {
                action.accept(x, y, this.tiles[y][x]);
            }
        }
    }

    /**
     * Iterates over a rectangular subregion of the map and invokes the given action for each tile.
     *
     * The region starts at (x, y) and spans w tiles horizontally and h tiles vertically.
     *
     * @param x      the starting x coordinate (column) of the region
     * @param y      the starting y coordinate (row) of the region
     * @param w      the width of the region in tiles
     * @param h      the height of the region in tiles
     * @param action consumer invoked for each tile with parameters (tileX, tileY, tile)
     */
    @Override
    public void forEachTile(final int x, final int y, final int w, final int h, final TileConsumer action) {
        for (int ry = y; ry < y + h; ry++) {
            for (int rx = x; rx < x + w; rx++) {
                action.accept(rx, ry, this.tiles[ry][rx]);
            }
        }
    }
}
