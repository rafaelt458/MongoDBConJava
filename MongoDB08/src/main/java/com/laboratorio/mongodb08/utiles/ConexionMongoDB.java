package com.laboratorio.mongodb08.utiles;

import com.laboratorio.mongodb08.monitor.MonitorCluster;
import com.laboratorio.mongodb08.monitor.MonitorComandos;
import com.laboratorio.mongodb08.monitor.MonitorPool;
import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.MongoException;
import com.mongodb.client.ListDatabasesIterable;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import java.io.IOException;
import java.util.Properties;
import java.util.concurrent.TimeUnit;
import org.bson.BsonDocument;
import org.bson.BsonInt64;
import org.bson.Document;
import org.bson.codecs.configuration.CodecProvider;
import org.bson.codecs.configuration.CodecRegistries;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.codecs.pojo.PojoCodecProvider;
import org.bson.conversions.Bson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConexionMongoDB {
    private static final Logger logger = LoggerFactory.getLogger(ConexionMongoDB.class);
    private final ConnectionString connectionString;
    private MongoClient client = null;
    private int minPoolSize;
    private int maxPoolSize;
    private int maxWaitTime;
    private int maxLifeTime;
    private int maxIdleTime;
    private String appName;

    public ConexionMongoDB() {
        Properties properties = null;

        try {
            ConfigFile configFile = new ConfigFile("mongodb.properties");
            properties = configFile.readPropertiesFile();
            
            this.minPoolSize = Integer.parseInt(properties.getProperty("minPoolSize"));
            this.maxPoolSize = Integer.parseInt(properties.getProperty("maxPoolSize"));
            this.maxWaitTime = Integer.parseInt(properties.getProperty("maxWaitTime"));
            this.maxLifeTime = Integer.parseInt(properties.getProperty("maxLifeTime"));
            this.maxIdleTime = Integer.parseInt(properties.getProperty("maxIdleTime"));
            this.appName = properties.getProperty("appName");
        } catch (IOException e) {
            logger.error("Error recuperando configuración: " + e.getMessage());
            this.connectionString = null;
            return;
        }

        String host = properties.getProperty("host");
        String port = properties.getProperty("port");
        String usuario = properties.getProperty("username");
        String clave = properties.getProperty("password");

        String uri = String.format("mongodb://%s:%s@%s:%s/", usuario, clave, host, port);
        this.connectionString = new ConnectionString(uri);
    }
    
    private boolean getPing(MongoDatabase database) {
        try {
            Bson comando = new BsonDocument("ping", new BsonInt64(1));
            Document resultado = database.runCommand(comando);
            logger.info("Resultado ping: " + resultado.toString());
        } catch (MongoException e) {
            logger.error("Error Ping: " + e.getMessage());
            return false;
        }
        
        return true;
    }

    public boolean crearConexion() throws MongoException {
        if (this.connectionString == null) {
            logger.error("Imposible crear conexión. No existe cadena de conexión.");
            return false;
        }

        try {
            MonitorCluster monitorCluster = new MonitorCluster();
            MonitorPool monitorPool = new MonitorPool();
            
            // MongoClient mongoClient = MongoClients.create(this.connectionString);
            MongoClient mongoClient = MongoClients.create(
                    MongoClientSettings.builder()
                        .applicationName(this.appName)
                        .applyConnectionString(this.connectionString)
                        .applyToClusterSettings(builder ->
                                builder.addClusterListener(monitorCluster)
                        )
                        .addCommandListener(new MonitorComandos())
                        .applyToConnectionPoolSettings(builder -> builder
                                .minSize(this.minPoolSize)
                                .maxSize(this.maxPoolSize)
                                .maxWaitTime(this.maxWaitTime, TimeUnit.SECONDS)
                                .maxConnectionLifeTime(this.maxLifeTime, TimeUnit.HOURS)
                                .maxConnectionIdleTime(this.maxIdleTime, TimeUnit.MINUTES)
                                .addConnectionPoolListener(monitorPool)
                        )
                        .build()
            );

            MongoDatabase database = mongoClient.getDatabase("admin");
            if (getPing(database)) {
                this.client = mongoClient;
                return true;
            }
        } catch (MongoException e) {
            logger.error("Error conexión: " + e.getMessage());
            throw e;
        }
        
        return false;
    }

    public void mostrarInformacionCluster() {
        if (this.client == null) {
            logger.warn("No hay conexión establecida");
            return;
        }

        logger.info("Información del cluster");
        logger.info(this.client.getClusterDescription().toString());
    }

    public void mostrarBasesDeDatos() {
        if (this.client == null) {
            logger.warn("No hay conexión establecida");
            return;
        }

        logger.info("Listado de bases de datos");

        ListDatabasesIterable<Document> databases = this.client.listDatabases();

        int i = 1;
        for (Document document : databases) {
            logger.info(String.format("%d-) %s", i, document.toString()));
            i++;
        }
    }
    
    public MongoDatabase getDatabaseWithCodec(String databaseName) throws Exception {
        if (this.client == null) {
            if (!this.crearConexion()) {
                throw new Exception("No se ha podido crear la conexión con el servidor MongoDB");
            }
        }
        
        CodecProvider pojoCodecProvider = PojoCodecProvider.builder().automatic(true).build();
        CodecRegistry pojoCodecRegistry = CodecRegistries.fromRegistries(
                MongoClientSettings.getDefaultCodecRegistry(), CodecRegistries.fromProviders(pojoCodecProvider));
        MongoDatabase database = this.client.getDatabase(databaseName).withCodecRegistry(pojoCodecRegistry);
        if (!getPing(database)) {
            throw new Exception("No se ha podido acceder a la base de datos");
        }
        
        return database;
    }
    
    public MongoDatabase getDatabase(String databaseName) throws Exception {
        if (this.client == null) {
            if (!this.crearConexion()) {
                throw new Exception("No se ha podido crear la conexión con el servidor MongoDB");
            }
        }
        
        MongoDatabase database = this.client.getDatabase(databaseName);
        if (!getPing(database)) {
            throw new Exception("No se ha podido acceder a la base de datos");
        }
        
        return database;
    }
    
    public void crearColeccion(String databaseName, String collectionName) {
        try {
            MongoDatabase database = this.getDatabase(databaseName);
            database.createCollection(collectionName);
        } catch (Exception e) {
            logger.error("Error creando colección: " + e.getMessage());
        }
    }
    
    public void mostrarColecciones(String databaseName) {
        try {
            MongoDatabase database = this.getDatabase(databaseName);
            int i = 1;
            logger.info("Colecciones de la base de datos: " + databaseName);
            for (String collectionName : database.listCollectionNames()) {
                logger.info(String.format("Colección %d: %s", i, collectionName));
                i++;
            }
        } catch (Exception e) {
            logger.error("Error mostrando colecciones: " + e.getMessage());
        }
    }
    
    public MongoCollection<?> getPOJOCollection(String databaseName, String collectionName, Class entidad) throws Exception {
        MongoDatabase database = getDatabaseWithCodec(databaseName);
        return database.getCollection(collectionName, entidad);
    }
    
    public MongoCollection<?> getRecordCollection(String databaseName, String collectionName, Class entidad) throws Exception {
        MongoDatabase database = getDatabase(databaseName);
        return database.getCollection(collectionName, entidad);
    }

    public void cerrarConexion() {
        if (this.client != null) {
            this.client.close();
            logger.info("Se ha cerrado la conexión con MongoDB");
        }
    }
}