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

import arc.files.Fi;
import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.util.function.Consumer;
import mindustry.Vars;
import mindustry.core.GameState.State;
import mindustry.gen.Groups;
import mindustry.io.SaveIO;
import mindustry.maps.Map;
import mindustry.net.Administration.Config;
import mindustry.net.Packets.KickReason;
import mindustry.world.Tiles;

public final class MapLoader implements Closeable {

    private final boolean paused;

    /**
     * Create a MapLoader configured to manage map loading and server state transitions.
     *
     * @return a new MapLoader instance
     */
    public static MapLoader create() {
        return new MapLoader();
    }

    /**
     * Creates a MapLoader, capturing the current paused state and preparing the server for map loading.
     *
     * If the game is currently running, all players are kicked with reason {@code serverRestarting}, the game
     * state is set to the menu, and the network server is closed.
     */
    private MapLoader() {
        this.paused = Vars.state.isPaused();
        if (Vars.state.isGame()) {
            Groups.player.each(player -> player.kick(KickReason.serverRestarting));
            Vars.state.set(State.menu);
            Vars.net.closeServer();
        }
    }

    /**
     * Loads the given map into the current game world.
     *
     * @param map the Mindustry map to load into the world
     */
    public void load(final Map map) {
        Vars.world.loadMap(map);
    }

    /**
     * Loads a saved map from the given file and clears the current sector reference.
     *
     * @param file the save file to load
     */
    public void load(final File file) {
        SaveIO.load(new Fi(file));
        Vars.state.rules.sector = null;
    }

    /**
     * Generates a new world using the provided tiles generator and dimensions.
     *
     * @param width     the world width in tiles
     * @param height    the world height in tiles
     * @param generator a callback that receives the Tiles grid to populate during generation
     */
    public void load(final int width, final int height, final Consumer<Tiles> generator) {
        Vars.logic.reset();
        Vars.world.loadGenerator(width, height, generator::accept);
    }

    /**
     * Loads a map produced by the given generator into the game world and applies its rules and metadata.
     *
     * This resets game logic, clears existing tile entities, replaces the world's tile grid with the generator's
     * tiles (including floors, overlays, blocks, and building state/configuration), and finalizes the map load
     * so the world's rules and map name tag reflect the generated context.
     *
     * @param generator the MapGenerator that produces the MapContext to load
     * @param <C>       the concrete MapContext type produced by the generator
     * @return          the generated MapContext produced by the generator
     */
    public <C extends MapContext> C load(final MapGenerator<C> generator) {
        Vars.logic.reset();
        Vars.world.beginMapLoad();

        // Clear tile entities
        for (final var tile : Vars.world.tiles) {
            if (tile != null && tile.build != null) {
                tile.build.remove();
            }
        }

        final var context = generator.generate();
        Vars.world.tiles = new Tiles(context.getWidth(), context.getHeight());
        Vars.world.tiles.fill();
        context.forEachTile((x, y, tile) -> {
            final var original = Vars.world.tiles.get(x, y);
            original.setFloor(tile.getFloor());
            original.setOverlay(tile.getOverlay());
            original.setBlock(tile.getBlock());
            if (tile.getBlock().hasBuilding()) {
                original.build.health = tile.getBuilding().getHealth();
                original.build.team = tile.getBuilding().getTeam();
                original.build.configure(tile.getBuilding().getConfiguration());
            }
        });

        Vars.world.endMapLoad();
        Vars.state.rules = context.getRules();
        Vars.state.map.tags.put("name", context.getMapName());
        return context;
    }

    /**
     * Restores the game's paused/playing state captured at construction and attempts to host the server on the configured port.
     *
     * If hosting the server fails, sets the game state to the main menu and rethrows the encountered IOException.
     *
     * @throws IOException if starting the server host fails
     */
    @Override
    public void close() throws IOException {
        Vars.state.set(this.paused ? State.paused : State.playing);
        try {
            Vars.net.host(Config.port.num());
        } catch (final IOException exception) {
            Vars.state.set(State.menu);
            throw exception;
        }
    }
}
