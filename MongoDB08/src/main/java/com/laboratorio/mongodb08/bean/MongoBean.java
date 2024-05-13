package com.laboratorio.mongodb08.bean;

import com.laboratorio.mongodb08.utiles.ConexionMongoDB;
import com.mongodb.MongoException;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.enterprise.context.ApplicationScoped;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ApplicationScoped
public class MongoBean {
    private static final Logger logger = LoggerFactory.getLogger(MongoBean.class);
    private ConexionMongoDB conexion;
    
    public MongoBean() {
        conexion = new ConexionMongoDB();
    }
    
    @PostConstruct
    public void iniciar() {
        logger.info("Iniciando el pool de conexiones MongoDB");
        
        try {
            conexion.crearConexion();
        } catch (MongoException e) {
            logger.error("No ha sido posible iniciar el pool de conexiones MongoDB");
            logger.error("Detalle: " + e.getMessage());
            this.conexion = null;
            return;
        }
        
        logger.info("El pool de conexiones MongoDB ha iniciado correctamente!");
    }
    
    @PreDestroy
    public void finalizar() {
        logger.info("Liberando el pool de conexiones MongoDB");
        conexion.cerrarConexion();
    }

    public ConexionMongoDB getConexion() {
        return conexion;
    }

    public void setConexion(ConexionMongoDB conexion) {
        this.conexion = conexion;
    }
}