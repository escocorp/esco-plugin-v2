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

import java.util.Optional;
import mindustry.gen.Groups;
import mindustry.gen.Player;

final class HexedUtils {

    /**
 * Prevents instantiation of this utility class.
 */
private HexedUtils() {}

    /**
     * Builds a human-readable, formatted leaderboard from the provided game state.
     *
     * The leaderboard lists up to the top 10 teams sorted by captured-hex count in descending order.
     * If no entries exist, the returned string contains a message indicating no hexes have been captured.
     *
     * @param state the game state containing the leaderboard map (team → captured-hex count)
     * @return the formatted leaderboard string beginning with "[accent]Leaderboard:"; each entry contains a 1-based rank,
     * the player's colored name (or "Unknown" if not found), and the team's captured-hex count (e.g., "1.[white] Name [orange]>[white] 5 hexes")
     */
    public static String createLeaderboard(final HexedState state) {
        final var builder = new StringBuilder();
        builder.append("[accent]Leaderboard:");
        final var top = state.getLeaderboard().entrySet().stream()
                .sorted((a, b) -> Integer.compare(b.getValue(), a.getValue()))
                .limit(10L)
                .toList();
        if (top.isEmpty()) {
            // Should not be possible though
            builder.append("\n[orange]No one has captured any hexes yet!");
            return builder.toString();
        }
        for (int i = 0; i < top.size(); i++) {
            final var entry = top.get(i);
            builder.append("\n[yellow]")
                    .append(i + 1)
                    .append(".[white] ")
                    .append(Optional.ofNullable(Groups.player.find(player -> player.team() == entry.getKey()))
                            .map(Player::coloredName)
                            .orElse("Unknown"))
                    .append(" [orange]>[white] ")
                    .append(entry.getValue())
                    .append(" hexes");
        }
        return builder.toString();
    }
}
