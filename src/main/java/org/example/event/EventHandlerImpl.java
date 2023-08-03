package org.example.event;

import org.example.BlablaPluginsBuilder;
import org.example.plugin.MarketPluginHandler;

import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public class EventHandlerImpl implements EventHandler {

    @Override
    public void trigger(NewEvent event) {
        if (Objects.requireNonNull(event) instanceof NewEventPostSent p) {
            System.out.printf("Recu une nouvelle événement %s avec ID: %s%n", event.getClass().getSimpleName(), event.getEventId().toString());
            MarketPluginHandler marketPluginHandler = p.getMarketPluginHandler();
            Set<BlablaPluginsBuilder> pluginsMustUnload = marketPluginHandler.showMyListPlugins()
                    .stream()
                    .filter(it -> p.getPluginsIdLoaded().contains(it.getId()))
                    .collect(Collectors.toSet());
            unLoadPlugin(pluginsMustUnload);
        } else {
            // TODO: Implement other case if have more event
            throw new IllegalStateException("Unexpected event class: " + event);
        }

    }

    private void unLoadPlugin(Set<BlablaPluginsBuilder> plugins) {
        System.out.println("Réinitialiser les plugins apres avoir anvoyé le message");
        plugins.forEach(it -> {
            System.out.printf("Réinitialiser plugin %s ....%n", it.getClass().getSimpleName());
            it.after();
            it.unLoad();
        });
    }
}
