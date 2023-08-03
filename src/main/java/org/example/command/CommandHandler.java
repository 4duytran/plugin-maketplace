package org.example.command;

import org.example.BlablaPluginsBuilder;
import org.example.plugin.MarketPluginHandler;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class CommandHandler {
    private final Logger logger = Logger.getLogger( CommandHandler.class.getName() );

    private static final String REGEX_COMMAND = "([^\"]\\S*|\".+?\")\\s*";
    private final MarketPluginHandler marketPluginHandler;

    private PostHandler postHandler;

    public CommandHandler(MarketPluginHandler marketPluginHandler) {
        this.marketPluginHandler = marketPluginHandler;
    }

    private static final List<String> commands = new ArrayList<>(
        List.of("help", "list", "load", "unload", "marketlist", "adopt", "post", "mylist", "stop")
    );

    public static void show(String message){
        System.out.println(message);
    }

    public boolean checkCommand(String command) {
        var firstCommand =  Arrays.asList(command.split(" ")).get(0);
        boolean found = commands.contains(firstCommand);
        if (!found) System.out.println("Cette commande n'existe pas - utilisez la commande 'help' pour avoir information");
        return found;
    }

    private void showRules() {
        show("""
            help: Afficher la liste des commandes disponibles dans le market.
            list: La list des plugins.
            marketlist: La liste des plugins dans le market.
            load [argument id]: Rajouter un plugin par son id dans la market (seulement les plugins qui sont disponibles -> voire la commande 'list').
            unload [argument id]: Retirer un plugin par son id depuis la market (seulement les plugins qui sont disponibles dans le market -> voire la commande 'marketlist').
            adopt [argument id]: S'abonner a un plugin (seulement les plugins qui sont disponibles dans la market -> voire la commande 'marketlist').
            mylist: Afficher votre liste des plugins.
            post [argument "message" argument id,id,...]: Permettre d'envoyer le message en appliquant les plugins sélectionnés (seulement les plugins qui sont disponibles dans votre liste -> voire la commande 'mylist').
        """
        );
    }

    public void apply(String command) {
        List<String> split = new ArrayList<String>();
        Matcher m = Pattern.compile(REGEX_COMMAND).matcher(command);
        while (m.find())
            split.add(m.group(1));

        switch (split.get(0)) {
            case "help" -> showRules();
            case "list" -> applyCommandList();
            case "marketlist" -> applyCommandMarketList();
            case "load" -> applyCommandLoad(split);
            case "unload" -> applyCommandUnLoad(split);
            case "adopt" -> applyCommandAdopt(split);
            case "mylist" -> applyCommandMyList();
            case "post" -> applyCommandPost(split);
        }
    }

    private void applyCommandList() {
        marketPluginHandler.getAllAvailablePlugins()
                .forEach( (k,v) ->
                        System.out.printf("%s - %s: %s%n", k, v.getClass().getSimpleName(), v.getDescription())
                );
    }

    private void applyCommandMarketList() {
        var list = marketPluginHandler.getAllPluginsInMarket();
        if (!list.isEmpty()) {
            list.forEach( (k,v) ->
                    System.out.printf("%s - %s: %s%n", k, v.getClass().getSimpleName(), v.getDescription())
            );
        } else show("La liste est vide");

    }

    private void applyCommandLoad(List<String> args) {
        try {
            var id = args.get(1);
            marketPluginHandler.load(id);
        } catch (IndexOutOfBoundsException e) {
            show("Les arguments ne sont pas valides");
        }
    }

    private void applyCommandUnLoad(List<String> args) {
        try {
            var id = args.get(1);
            marketPluginHandler.unLoad(id);
        } catch (IndexOutOfBoundsException e) {
            show("Les arguments ne sont pas valides");
        }
    }

    private void applyCommandAdopt(List<String> args) {
        try {
            var id = args.get(1);
            marketPluginHandler.setSubscribe(id);
        } catch (IndexOutOfBoundsException e) {
            show("Les arguments ne sont pas valides.");
        }
    }

    private void applyCommandMyList(){
        var list = marketPluginHandler.showMyListPlugins();
        if (!list.isEmpty()) {
            list.forEach(it ->
                System.out.printf("%s - %s: %s%n", it.getId(), it.getClass().getSimpleName(), it.getDescription())
            );
        } else show("Votre liste de plugin est vide");
    }

    private void applyCommandPost(List<String> args){
        try {
            var message = args.get(1);
            var ids = Arrays.asList(args.get(2).split(","));
            var pluginsByIds = marketPluginHandler.showMyListPlugins()
                    .stream()
                    .map(BlablaPluginsBuilder::getId)
                    .filter(ids::contains)
                    .collect(Collectors.toSet());

            if (pluginsByIds.isEmpty()) {
                show("Les plugins sélectionnés ne sont pas valides. Veuillez vérifier votre list des plugins.");
                return;
            }
            this.postHandler = new PostHandler(message, marketPluginHandler, pluginsByIds);
            this.postHandler.handle();
        } catch (RuntimeException e) {
            show("Les arguments ne sont pas valides.");
            logger.log(Level.SEVERE,  e.getMessage());
        } catch (InterruptedException e) {
            logger.log(Level.SEVERE,  e.getMessage());
            throw new RuntimeException(e);
        }
    }

}
