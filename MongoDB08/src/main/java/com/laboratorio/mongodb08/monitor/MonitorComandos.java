package com.laboratorio.mongodb08.monitor;

import com.mongodb.event.CommandFailedEvent;
import com.mongodb.event.CommandListener;
import com.mongodb.event.CommandStartedEvent;
import com.mongodb.event.CommandSucceededEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MonitorComandos implements CommandListener {
    private static final Logger logger = LoggerFactory.getLogger(MonitorComandos.class);
    
    @Override
    public void commandStarted(CommandStartedEvent event) {
        String comando = event.getCommandName();
        String databaseName = event.getDatabaseName();
        logger.info(String.format("Se ha iniciado la ejecución del comando %s en la base de datos %s",
                comando, databaseName));
    }

    @Override
    public void commandSucceeded(CommandSucceededEvent event) {
        String comando = event.getCommandName();
        String databaseName = event.getDatabaseName();
        logger.info(String.format("Se ha ejecutado correctamente el comando %s en la base de datos %s",
                comando, databaseName));
    }

    @Override
    public void commandFailed(CommandFailedEvent event) {
        String comando = event.getCommandName();
        String databaseName = event.getDatabaseName();
        logger.info(String.format("Ha ocurrido un error ejecutando el comando %s en la base de datos %s",
                comando, databaseName));
    }
}