package com.laboratorio.mongodb08.persistencia;

import com.laboratorio.mongodb08.modelo.Persona;
import com.mongodb.client.MongoCollection;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PersonaDB {

    private static final Logger logger = LoggerFactory.getLogger(PersonaDB.class);
    private final MongoCollection<Persona> collection;

    public PersonaDB(MongoCollection<Persona> collection) {
        this.collection = collection;
    }

    public List<Persona> getPersonas() {
        List<Persona> personas = new ArrayList<>();

        try {
            collection.find().into(personas);
        } catch (Exception e) {
            logger.error("Error: " + e.getMessage());
        }

        return personas;
    }

    public Persona buscar(String codigo) {
        try {
            ObjectId id = new ObjectId(codigo);
            Document query = new Document("_id", id);
            return collection.find(query).first();
        } catch (Exception e) {
            logger.error("Error: " + e.getMessage());
            return null;
        }
    }

    public boolean insertar(String nombres, String apellidos, String fechaNac, String experiencia) {
        try {
            SimpleDateFormat formato = new SimpleDateFormat("yyyy-MM-dd");
            Date fecha = formato.parse(fechaNac);
            Persona persona = new Persona(nombres, apellidos, fecha, Integer.parseInt(experiencia));
            collection.insertOne(persona);
        } catch (NumberFormatException | ParseException e) {
            logger.error("Error: " + e.getMessage());
            return false;
        }

        return true;
    }

    public boolean editar(String codigo, String nombres, String apellidos, String fechaNac, String experiencia) {
        try {
            SimpleDateFormat formato = new SimpleDateFormat("yyyy-MM-dd");
            Date fecha = formato.parse(fechaNac);
            
            ObjectId id = new ObjectId(codigo);
            Document query = new Document("_id", id);
            
            Document persona = new Document("$set", new Document()
                    .append("nombres", nombres)
                    .append("apellidos", apellidos)
                    .append("fechaNac", fecha)
                    .append("experiencia", Integer.valueOf(experiencia))
            );
            
            collection.updateOne(query, persona);
        } catch (NumberFormatException | ParseException e) {
            logger.error("Error: " + e.getMessage());
            return false;
        }
        
        return true;
    }

    public boolean eliminar(String codigo) throws Exception {
        try {
            ObjectId id = new ObjectId(codigo);
            Document query = new Document("_id", id);
            collection.deleteOne(query);
        } catch (Exception e) {
            logger.error("Error: " + e.getMessage());
            return false;
        }
        
        return true;
    }

    public String validar(String nombres, String apellidos, String fechaNac, String experiencia) {
        StringBuilder resultado = new StringBuilder("");

        if (nombres.isEmpty()) {
            resultado.append("<p>Los nombres no pueden estar vacíos.</p>");
        } else {
            if (nombres.length() < 2) {
                resultado.append("<p>Los nombres deben tener al menos 2 caracteres.</p>");
            }
        }

        if (apellidos.isEmpty()) {
            resultado.append("<p>Los apellidos no pueden estar vacíos.</p>");
        } else {
            if (nombres.length() < 2) {
                resultado.append("<p>Los apellidos deben tener al menos 2 caracteres.</p>");
            }
        }

        if (fechaNac.isEmpty()) {
            resultado.append("<p>La fecha de nacimiento no puede estar vacía.</p>");
        } else {
            if (!fechaNac.matches("^(19|20)(((([02468][048])|([13579][26]))-02-29)|(\\d{2})-((02-((0[1-9])|1\\d|2[0-8]))|((((0[13456789])|1[012]))-((0[1-9])|((1|2)\\d)|30))|(((0[13578])|(1[02]))-31)))$")) {
                resultado.append("<p>La fecha tiene un formato incorrecto.</p>");
            }
        }

        if (experiencia.isEmpty()) {
            resultado.append("<p>La experiencia no puede estar vacía.</p>");
        } else {
            if (!experiencia.matches("^[0-9]+$")) {
                resultado.append("<p>La experiencia debe ser un número.</p>");
            }
        }

        return resultado.toString();
    }
}
