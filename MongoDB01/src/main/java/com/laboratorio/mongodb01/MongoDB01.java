package com.laboratorio.mongodb01;

import com.laboratorio.mongodb01.entidades.Articulo;
import com.laboratorio.mongodb01.entidades.Usuario;
import com.laboratorio.mongodb01.repositorio.ConexionMongoDB;
import com.mongodb.MongoException;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MongoDB01 {
    private static final Logger logger = LoggerFactory.getLogger(MongoDB01.class);
    
    public static void main(String[] args) {
        ConexionMongoDB mongoDB = new ConexionMongoDB();
        
        try {
            mongoDB.crearConexion();
        } catch (MongoException e) {
            logger.error("Error: " + e.getMessage());
            return;
        }
        
        mongoDB.mostrarInformacionCluster();
        
        mongoDB.mostrarBasesDeDatos();
        
        MongoCollection<Articulo> articulos;
        
        mongoDB.crearColeccion("tienda", "articulos");
        
        try {
            articulos = (MongoCollection<Articulo>)mongoDB.getPOJOCollection("tienda", "articulos", Articulo.class);
        } catch (Exception e) {
            mongoDB.cerrarConexion();
            logger.error("No ha sido posible acceder a la colección de artículos. Finaliza el programa!");
            logger.error("Error: " + e.getMessage());
            return;
        }
        
        logger.info("Se ha accedido a la colección Artículos");
        
        articulos.insertOne(new Articulo(new ObjectId(), "Laptop", 4, 825));
        
        MongoCollection<Usuario> usuarios;
        
        try {
            usuarios = (MongoCollection<Usuario>)mongoDB.getRecordCollection("tienda", "usuarios", Usuario.class);
        } catch (Exception e) {
            mongoDB.cerrarConexion();
            logger.error("No ha sido posible acceder a la colección de usuarios. Finaliza el programa!");
            logger.error("Error: " + e.getMessage());
            return;
        }
        
        logger.info("Se ha accedido a la colección Usuarios");
        
        usuarios.insertOne(new Usuario(1, "Laboratorio de Rafa", "labrafa", "1234"));
        
        mongoDB.crearColeccion("tienda", "prueba");
        
        mongoDB.mostrarColecciones("tienda");
        
        articulos.drop();
        
        try {
            MongoDatabase database = mongoDB.getDatabase("tienda");
            database.drop();
        } catch (Exception e) {
            logger.error("Error eliminando base de datos:" + e.getMessage());
        }
        
        mongoDB.cerrarConexion();
    }
}