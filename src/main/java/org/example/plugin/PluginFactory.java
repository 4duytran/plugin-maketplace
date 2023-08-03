package org.example.plugin;

import org.example.BlablaPluginsBuilder;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.logging.Level;
import java.util.logging.Logger;

public class PluginFactory {

    private static final long REFRESH_TIME = 3600000;
    private static final Logger logger = Logger.getLogger( PluginFactory.class.getName() );
    private static final Map<String, BlablaPluginsBuilder> pluginList = new ConcurrentHashMap<>();

    private static final String PATH_PLUGIN = "./plugins";

    static {
        logger.log(Level.INFO,"Init all plugins");
        try {
            new Timer().schedule(new RefreshPluginTask(pluginList, initPlugins()), 0, REFRESH_TIME);
        } catch (IOException | ClassNotFoundException | NoSuchMethodException e) {
            logger.log(Level.SEVERE, e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    private static Map<String, BlablaPluginsBuilder> initPlugins() throws IOException, ClassNotFoundException, NoSuchMethodException {
        Map<String, BlablaPluginsBuilder> plugins = new HashMap<>();
        List<File> pluginFiles = List.of(Objects.requireNonNull(new File(PATH_PLUGIN).listFiles(file -> file.getName().endsWith(".jar"))));

        for (File plugin : pluginFiles) {

            try(JarFile jar = new JarFile(plugin);
                URLClassLoader clazzLoader = new URLClassLoader(new URL[]{plugin.toURI().toURL()}, ClassLoader.getSystemClassLoader())
            ) {
                Enumeration<JarEntry> entries = jar.entries();
                while (entries.hasMoreElements()) {
                    JarEntry entry = entries.nextElement();
                    String name = entry.getName();
                    if (name.endsWith(".class") && !name.contains("$")) {
                        name = name.substring(0, name.lastIndexOf('.')).replaceAll("/", ".");
                        Class<?> c =  clazzLoader.loadClass(name);
                        Object p;
                        if ( (p = c.getDeclaredConstructor().newInstance()) instanceof BlablaPluginsBuilder ) {
                            Arrays.stream(c.getDeclaredFields()).forEach(it -> it.setAccessible(true));
                            plugins.put( ((BlablaPluginsBuilder) p).getId(), (BlablaPluginsBuilder) p);
                        }
                    }
                }
            } catch (
                    InvocationTargetException
                    | InstantiationException
                    | IllegalAccessException e
            ) {
                logger.log(Level.SEVERE, e.getMessage(), e);
                throw new RuntimeException(e);
            }
        }
        return plugins;
    }

    public static BlablaPluginsBuilder getPlugin(String id) {
        return pluginList.get(id);
    }

    public static Map<String, BlablaPluginsBuilder> getAvailablePlugins() {
        return pluginList;
    }

}

