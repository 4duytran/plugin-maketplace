package org.example.plugin;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.RemovalListener;
import org.example.BlablaPluginsBuilder;
import org.example.entity.Subscriber;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;
import java.util.regex.Pattern;

public class MarketPluginHandler {

    private static final Logger logger = Logger.getLogger( MarketPluginHandler.class.getName() );
    private static final Cache<String, BlablaPluginsBuilder> market;
    private Subscriber subscribe;
    private static final String REGEX_MAIL = "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$";

    static {
        market = CacheBuilder.newBuilder()
            .expireAfterWrite(24, TimeUnit.HOURS)
            .maximumSize(100)
            .removalListener(
                (RemovalListener<String, BlablaPluginsBuilder>) notification ->
                        logger.info("Remove plugin in cache with ID " + notification.getKey())
            )
            .build();
    }

    public MarketPluginHandler() throws ClassNotFoundException {
        Class.forName("org.example.plugin.PluginFactory");
    }

    public Map<String, BlablaPluginsBuilder> getAllAvailablePlugins() {
        return PluginFactory.getAvailablePlugins();
    }

    public Map<String, BlablaPluginsBuilder> getAllPluginsInMarket() {
        return market.asMap();
    }

    public void load(String id) {
        var plugin = PluginFactory.getPlugin(id);
        if (plugin != null) {
            if (market.getIfPresent(plugin.getId()) == null) {
                market.put(plugin.getId(), plugin);
                System.out.printf("Le plugin %s a bien rajouté dans la market%n", plugin.getClass().getSimpleName());
                System.out.println("""
                        INFORMATION: Ce plugin sera retiré automatiquement de notre marketplace après 24h \
                        afin d'optimiser l'espace de stockage.
                        Vous pouvez le rajouter à n'importe quel moment.
                        """);
            } else System.out.printf("Le plugin %s a deja existé dans la market%n", plugin.getClass().getSimpleName());

        } else {
            System.out.printf("Le plugin avec id: %s n'existe pas%n", id);
        }
    }

    public void unLoad(String id) {
        var plugin = market.getIfPresent(id);
        if (plugin != null) {
            market.invalidate(id);
            System.out.printf("Le plugin %s a bien été retiré de la market%n", plugin.getClass().getSimpleName());
            if(this.subscribe.getPlugins().remove(plugin)) {
                System.out.println("Ce plugin a été retiré également dans votre list des plugins");
            }
        } else {
            System.out.printf("Le plugin avec id: %s n'existe pas%n", id);
        }
    }

    public void setSubscribe(String pluginId) {
        var pluginExiste = this.subscribe.getPlugins()
                .stream()
                .filter(it -> Objects.equals(it.getId(), pluginId))
                .findFirst();

        if (pluginExiste.isPresent()) {
            System.out.println("Vous avez déjà abonné à ce plugin");
        } else {
            var pluginToAdd = market.getIfPresent(pluginId);
            if(pluginToAdd != null) {
                this.subscribe.getPlugins().add(pluginToAdd);
                System.out.println("Merci, ce plugin a bien rajouté dans votre liste");
            } else System.out.println("Veuillez vérifier si le plugin est bien dans le market ou plugin id est valid");
        }
    }

    public Set<BlablaPluginsBuilder> showMyListPlugins() {
        return this.subscribe.getPlugins();
    }

    public boolean register(String userMail) {
        boolean valid = false;
        Pattern pattern = Pattern.compile(REGEX_MAIL);
        if (pattern.matcher(userMail).matches()) {
            this.subscribe = new Subscriber(userMail);
            valid = true;
        }
        if(!valid) System.out.println("Email est invalid");
        return valid;
    }

    public Subscriber getSubscribe() {
        return subscribe;
    }
}
