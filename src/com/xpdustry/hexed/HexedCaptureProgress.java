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

import arc.struct.IntFloatMap;
import com.xpdustry.hexed.model.Hex;

public interface HexedCaptureProgress {

    /**
     * Create an Anuke-style capture progress calculator configured with the given requirement.
     *
     * @param requirement the capture requirement threshold used when computing progress
     * @return a HexedCaptureProgress instance configured with the provided requirement
     */
    static HexedCaptureProgress anuke(final int requirement) {
        return new AnukeHexedCaptureProgress(requirement);
    }

    /**
     * Creates an Anuke-style capture progress calculator configured with the default requirement of 210.
     *
     * @return a HexedCaptureProgress configured with a requirement value of 210
     */
    static HexedCaptureProgress anuke() {
        return new AnukeHexedCaptureProgress(210);
    }

    /**
 * Compute and write capture progress for the given hex into the provided capture map.
 *
 * @param hex     the hex whose capture progress should be calculated
 * @param capture a mutable IntFloatMap that will be written to or updated with capture progress values (integer keys mapped to float progress values)
 */
void calculate(final Hex hex, final IntFloatMap capture);
}
