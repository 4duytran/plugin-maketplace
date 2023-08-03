package org.example.event;

import org.example.plugin.MarketPluginHandler;

import java.util.Set;
import java.util.UUID;

public class NewEventPostSent implements NewEvent {
    private final UUID eventId;
    private final MarketPluginHandler marketPluginHandler;

    private final Set<String> pluginsIdLoaded;

    public NewEventPostSent(MarketPluginHandler marketPluginHandler, Set<String> ids) {
        this.eventId = UUID.randomUUID();
        this.marketPluginHandler = marketPluginHandler;
        this.pluginsIdLoaded = ids;
    }

    @Override
    public UUID getEventId() {
        return eventId;
    }

    public MarketPluginHandler getMarketPluginHandler() {
        return marketPluginHandler;
    }

    public Set<String> getPluginsIdLoaded() {
        return pluginsIdLoaded;
    }
}
