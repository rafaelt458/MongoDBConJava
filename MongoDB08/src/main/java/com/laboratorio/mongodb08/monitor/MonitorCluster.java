package com.laboratorio.mongodb08.monitor;

import com.mongodb.connection.ClusterId;
import com.mongodb.event.ClusterClosedEvent;
import com.mongodb.event.ClusterDescriptionChangedEvent;
import com.mongodb.event.ClusterListener;
import com.mongodb.event.ClusterOpeningEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MonitorCluster implements ClusterListener {
    private static final Logger logger = LoggerFactory.getLogger(MonitorCluster.class);

    @Override
    public void clusterOpening(ClusterOpeningEvent event) {
        ClusterId clusterId = event.getClusterId();
        String descripcion =  clusterId.getDescription();
        logger.info("Se ha abierto el cluster: " + descripcion);
    }

    @Override
    public void clusterClosed(ClusterClosedEvent event) {
        ClusterId clusterId = event.getClusterId();
        String descripcion =  clusterId.getDescription();
        logger.info("Se ha cerrado el cluster: " + descripcion);
    }

    @Override
    public void clusterDescriptionChanged(ClusterDescriptionChangedEvent event) {
        ClusterId clusterId = event.getClusterId();
        String descripcion =  clusterId.getDescription();
        logger.info("Ha cambiado la descripción del cluster: " + descripcion);
    }   
}