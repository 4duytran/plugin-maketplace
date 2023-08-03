package org.example.command;

import org.example.BlablaPluginsBuilder;
import org.example.event.EventHandler;
import org.example.event.EventHandlerImpl;
import org.example.event.NewEventPostSent;
import org.example.plugin.MarketPluginHandler;

import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class PostHandler {

    private final Logger logger = Logger.getLogger( PostHandler.class.getName() );
    private final String message;
    private final MarketPluginHandler marketPluginHandler;
    private final EventHandler eventHandler;
    private final Set<String> pluginIdNeeded;


    public PostHandler(String message, MarketPluginHandler marketPluginHandler, Set<String> ids) {
        this.message = message;
        this.marketPluginHandler = marketPluginHandler;
        this.pluginIdNeeded = ids;
        this.eventHandler = new EventHandlerImpl();
    }

    public void handle() throws InterruptedException {
        var plugins = marketPluginHandler.getSubscribe()
                .getPlugins()
                .stream()
                .filter(it -> pluginIdNeeded.contains(it.getId()))
                .collect(Collectors.toSet());

        if (isValid(message, plugins)) {
            send(message, plugins);
            eventHandler.trigger(
                new NewEventPostSent(
                    this.marketPluginHandler,
                    plugins.stream().map(BlablaPluginsBuilder::getId).collect(Collectors.toSet())
                )
            );
        }
    }

    private boolean isValid(String message, Set<BlablaPluginsBuilder> plugins ) {
        return !message.isEmpty() && !plugins.isEmpty();
    }

    private void send(String message, Set<BlablaPluginsBuilder> plugins) throws InterruptedException {
        loadPlugin(plugins);
        Thread.sleep(2000);
        executeBeforeSend(plugins);
        System.out.println("============================");
        System.out.println(message);
        System.out.println("Le message a envoyé ....");
        System.out.println("============================");
    }

    private void loadPlugin(Set<BlablaPluginsBuilder> plugins) {
        System.out.println("Charger les plugins avant d'envoyer votre message");
        plugins.forEach(it -> {
            System.out.printf("Charger plugin %s ....%n", it.getClass().getSimpleName());
            it.load();
        });
    }

    private void executeBeforeSend(Set<BlablaPluginsBuilder> plugins) {

        plugins.forEach(it -> {
            System.out.printf("Exécuter plugin %s ....%n ", it.getClass().getSimpleName());
            try {
                it.before();
            } catch (RuntimeException e){
                logger.log(Level.SEVERE, e.getMessage());
            }
        });
    }

}
