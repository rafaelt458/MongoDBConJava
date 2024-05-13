package com.laboratorio.mongodb08.monitor;

import com.mongodb.connection.ClusterId;
import com.mongodb.connection.ConnectionId;
import com.mongodb.connection.ServerId;
import com.mongodb.event.ConnectionCheckedInEvent;
import com.mongodb.event.ConnectionCheckedOutEvent;
import com.mongodb.event.ConnectionPoolListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MonitorPool implements ConnectionPoolListener {
    private static final Logger logger = LoggerFactory.getLogger(MonitorPool.class);

    @Override
    public void connectionCheckedOut(ConnectionCheckedOutEvent event) {
        ConnectionId connectionId = event.getConnectionId();
        ServerId serverId = connectionId.getServerId();
        ClusterId clusterId = serverId.getClusterId();
        logger.info("Se ha solicitado una conexión del pool del cluster: " + clusterId.getDescription());
    }

    @Override
    public void connectionCheckedIn(ConnectionCheckedInEvent event) {
        ConnectionId connectionId = event.getConnectionId();
        ServerId serverId = connectionId.getServerId();
        ClusterId clusterId = serverId.getClusterId();
        logger.info("Se ha liberado una conexión del pool del cluster: " + clusterId.getDescription());
    }
}