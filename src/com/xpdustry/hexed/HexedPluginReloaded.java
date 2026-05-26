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

import com.xpdustry.distributor.api.Distributor;
import com.xpdustry.distributor.api.annotation.PluginAnnotationProcessor;
import com.xpdustry.distributor.api.plugin.AbstractMindustryPlugin;
import com.xpdustry.distributor.api.plugin.PluginListener;
import com.xpdustry.hexed.generation.AnukeHexedGenerator;
import com.xpdustry.hexed.generation.HexedMapContext;
import com.xpdustry.hexed.generation.HexedMapGenerator;
import com.xpdustry.hexed.generation.MapGenerator;
import com.xpdustry.hexed.generation.MapLoader;
import java.util.Objects;
import mindustry.Vars;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;

@SuppressWarnings("unused")
public final class HexedPluginReloaded extends AbstractMindustryPlugin implements HexedAPI {

    private final PluginAnnotationProcessor<?> processor = PluginAnnotationProcessor.events(this);
    private @MonotonicNonNull HexedStateImpl state = null;

    /**
     * Get the current hosted Hexed game state.
     *
     * @return the non-null {@link HexedState} instance representing the hosted game state
     * @throws NullPointerException if the state has not been initialized
     */
    @Override
    public HexedState getHexedState() {
        return Objects.requireNonNull(this.state);
    }

    /**
     * Accesses the current HexedStateImpl instance for internal use.
     *
     * @return the current HexedStateImpl instance; never null
     */
    HexedStateImpl getHexedState0() {
        return Objects.requireNonNull(this.state);
    }

    /**
     * Reports whether the Hexed game mode is active for the current match.
     *
     * @return `true` if the current match's rules contain the hexed presence flag, `false` otherwise.
     */
    @Override
    public boolean isEnabled() {
        return Vars.state.rules.tags.getBool(HexedMapContext.HEXED_PRESENCE_FLAG);
    }

    /**
     * Generates a Hexed game map with the given generator and initializes the plugin's hosted game state.
     *
     * @param generator the map generator that produces a HexedMapContext used to initialize the hosted game state
     * @return `true` if generation and state initialization succeeded, `false` if an error occurred
     */
    @Override
    public boolean start(final MapGenerator<HexedMapContext> generator) {
        try (final var loader = MapLoader.create()) {
            this.getLogger().info("Generating hexed map.");
            final var start = System.currentTimeMillis();
            final var context = loader.load(generator);
            this.getLogger().info("Generated hexed map in {} milliseconds.", System.currentTimeMillis() - start);
            this.state = new HexedStateImpl(
                    context.getBaseSchematic(),
                    context.getCaptureCalculator(),
                    context.getHexes(),
                    context.getDuration());
            return true;
        } catch (final Exception e) {
            this.getLogger().error("Failed to host a hexed game", e);
            return false;
        }
    }

    /**
     * Initializes the plugin by registering the hexed map generator and installing core plugin listeners.
     *
     * During initialization this registers an AnukeHexedGenerator with the service manager and adds
     * the HexedLogic, HexedRenderer, and HexedCommands listeners.
     */
    @Override
    public void onInit() {
        Distributor.get().getServiceManager().register(this, HexedMapGenerator.class, new AnukeHexedGenerator());
        this.addListener(new HexedLogic(this));
        this.addListener(new HexedRenderer(this));
        this.addListener(new HexedCommands(this));
    }

    /**
     * Registers the given plugin listener and processes any plugin-related annotations it contains.
     *
     * @param listener the plugin listener to register and scan for annotations
     */
    @Override
    protected void addListener(final PluginListener listener) {
        super.addListener(listener);
        this.processor.process(listener);
    }
}
