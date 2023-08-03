package org.example.plugin;

import org.example.BlablaPluginsBuilder;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Map;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;

public class RefreshPluginTask extends TimerTask {

    private static final Logger logger = Logger.getLogger( RefreshPluginTask.class.getName() );
    private final Map<String, BlablaPluginsBuilder> oldList;
    private final Map<String, BlablaPluginsBuilder> newList;
    public RefreshPluginTask(
            Map<String, BlablaPluginsBuilder> olds,
            Map<String, BlablaPluginsBuilder> news
    ) {
        this.oldList = olds;
        this.newList = news;
    }

    @Override
    public void run() {
        logger.log(Level.INFO,"Refresh plugin at: "
                + LocalDateTime.ofInstant(Instant.ofEpochMilli(scheduledExecutionTime()), ZoneId.systemDefault()));
        oldList.clear();
        oldList.putAll(newList);

    }
}
