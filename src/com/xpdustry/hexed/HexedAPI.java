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

import com.xpdustry.hexed.generation.HexedMapContext;
import com.xpdustry.hexed.generation.MapGenerator;
import mindustry.Vars;

public interface HexedAPI {

    /**
     * Retrieve the active HexedAPI implementation for the loaded HexedReloaded mod.
     *
     * @return the HexedAPI instance provided by the HexedReloaded mod
     */
    static HexedAPI get() {
        return (HexedAPI) Vars.mods.getMod(HexedPluginReloaded.class).main;
    }

    /**
 * Gets the current Hexed mod state.
 *
 * @return the current {@link HexedState} instance representing the mod's runtime state
 */
HexedState getHexedState();

    /**
 * Indicates whether the Hexed gameplay mode is currently enabled.
 *
 * @return `true` if the Hexed gameplay logic is enabled, `false` otherwise.
 */
boolean isEnabled();

    /**
 * Starts the Hexed mode using the provided map generator.
 *
 * @param generator the map generator configured for HexedMapContext used to create the game map
 * @return `true` if Hexed was started successfully, `false` otherwise
 */
boolean start(final MapGenerator<HexedMapContext> generator);
}
