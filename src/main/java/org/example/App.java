package org.example;

import org.example.command.CommandHandler;
import org.example.plugin.MarketPluginHandler;

import java.io.IOException;
import java.util.Scanner;
import java.util.function.Predicate;

public class App 
{

    public static void main( String[] args ) throws ClassNotFoundException {
        run();
    }

    public static void run() throws ClassNotFoundException {
        MarketPluginHandler marketPluginHandler = new MarketPluginHandler();
        CommandHandler commandHandler = new CommandHandler(marketPluginHandler);
        Scanner sc = new Scanner(System.in);
        var registered = true;
        var run = true;
        var email = "";

        show("Bienvenue dans BlaBla plugin market, " +
                "Veuillez vous connecter avec email avant de continuer");

        while(registered) {
            email = getResponse(sc, "votre email:", marketPluginHandler::register);
            registered = false;
        }
        show("Merci "+email+ " , vous pouvez commencer a utiliser notre systeme plugin market");
        show("Pour avoir plus d'information vous pouvez utiliser la commande 'help'");

        while (run) {
            var response = getCommand(sc, "Veuillez entrer une commande:", (String s) ->
                    !s.isEmpty() && commandHandler.checkCommand(s)
            );
            if (response.equals("stop")) {
                run = false;
            } else commandHandler.apply(response);
        }

        show("Merci d'avoir utilisé notre system BlaBla Plugin market, à bien tot");
    }

    private static String getResponse(Scanner sc, String message, Predicate<String> condition) {
        String response;
        do {
            show(message);
            response = sc.nextLine();

        } while (!condition.test(response));
        sc.reset();
        return response;
    }

    private static String getCommand(Scanner sc, String message, Predicate<String> condition) {
        String response;
        do {
            show(message);
            response = sc.nextLine();

        } while (!condition.test(response));
        sc.reset();
        return response;
    }

    private static void show(String message){
        System.out.println(message);
    }


}

