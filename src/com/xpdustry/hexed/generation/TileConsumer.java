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

import java.util.List;

@FunctionalInterface
public interface TileConsumer {

    /**
     * Create a composite TileConsumer that forwards each accept call to every consumer in the given list.
     *
     * The returned consumer invokes the provided consumers in list order for each (x, y, tile) call.
     * Any exception thrown by an aggregated consumer propagates to the caller.
     *
     * @param consumers list of consumers to invoke for each tile; may be empty but must not be null
     * @return a TileConsumer that sequentially delegates accept(x, y, tile) to each consumer in {@code consumers}
     */
    static TileConsumer aggregate(final List<? extends TileConsumer> consumers) {
        return (x, y, tile) -> {
            for (final var consumer : consumers) {
                consumer.accept(x, y, tile);
            }
        };
    }

    /**
 * Consume the given MapTile at the specified tile coordinates.
 *
 * @param x horizontal tile coordinate
 * @param y vertical tile coordinate
 * @param tile the MapTile to be consumed or processed
 */
void accept(int x, int y, MapTile tile);
}
