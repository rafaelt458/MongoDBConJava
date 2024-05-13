package com.laboratorio.mongodb08.controlador;

import com.laboratorio.mongodb08.bean.MongoBean;
import com.laboratorio.mongodb08.modelo.Persona;
import com.laboratorio.mongodb08.modelo.PersonaRequest;
import com.laboratorio.mongodb08.persistencia.PersonaDB;
import com.laboratorio.mongodb08.utiles.ConexionMongoDB;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.internal.MongoClientImpl;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@WebServlet(name = "PersonasController", urlPatterns = {"/personas"})
public class PersonasController extends HttpServlet {
    private static final Logger logger = LoggerFactory.getLogger(PersonasController.class);
    
    @Inject
    private MongoBean mongoBean;
    private ConexionMongoDB conexion;
    private PersonaDB personaDB;
    
    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        
        this.conexion = mongoBean.getConexion();
        MongoCollection<Persona> collection;
        try {
            collection = (MongoCollection<Persona>)conexion.getPOJOCollection("crudmongo", "personas", Persona.class);
        } catch (Exception e) {
            logger.error("No se puedo acceder a la colección de persoans");
            logger.error("Error: " + e.getMessage());
            personaDB = null;
            return;
        }
        
        /*
        collection.insertMany(Arrays.asList(
                new Persona("Nombre 1", "Apellido 1", new Date(), 1),
                new Persona("Nombre 2", "Apellido 2", new Date(), 2),
                new Persona("Nombre 3", "Apellido 3", new Date(), 3),
                new Persona("Nombre 4", "Apellido 4", new Date(), 4),
                new Persona("Nombre 5", "Apellido 5", new Date(), 5)
        )); */
        
        personaDB = new PersonaDB(collection);
    }

    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        if (personaDB == null) {
            // Mostrar página de error
            return;
        }
        
        String accion = request.getParameter("accion");
        if (accion == null) {
            accion = "N/A";
        }

        logger.info("Se está ejecutando el servlet. Acción: " + accion);
        
        String resultado;
        String mensaje;
        
        switch (accion) {
            case "agregar":
                crearPersona(request, response);
                break;
            case "guardar":
                resultado = validarPersona(request);
                if (resultado.isEmpty()) {
                    logger.info("Se procede a crear la persona!");
                    if (guardarPersona(request)) {
                        mensaje = "Se han guardado los datos de la persona correctamente";
                    } else {
                        mensaje = "Se ha presentado un error al guardar los datos de la persona";
                    }                    
                    listarPersonas(mensaje, request, response);
                } else {
                    mostrarErrores(resultado, request, response);
                }
                break;
            case "editar":
                if (!editarPersona(request, response)) {
                    mensaje = "Se ha presentado en error al recuperar los datos de la persona";
                    listarPersonas(mensaje, request, response);
                }
                break;
            case "eliminar":
                if (eliminarPersona(request)) {
                    mensaje = "Se han eliminado los datos de la persona correctamente";
                } else {
                    mensaje = "Se ha presentado un error al eliminar los datos de la persona";
                }
                listarPersonas(mensaje, request, response);
                break;
            case "listar":
            default:
                listarPersonas(null, request, response);
                break;
        }
    }
    
    private void listarPersonas(String mensaje, HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException{
        List<Persona> personas;

        try {
            personas = personaDB.getPersonas();
        } catch (Exception e) {
            logger.error("Error recuperando la lista de personas!");
            personas = new ArrayList<>();
        }

        request.setAttribute("lista_personas", personas);
        request.setAttribute("mensaje", mensaje);
        
        RequestDispatcher dispatcher = request.getRequestDispatcher("/personas.jsp");
        dispatcher.forward(request, response);
    }
    
    private void crearPersona(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException{
        
        PersonaRequest persona = new PersonaRequest();
        
        request.setAttribute("persona", persona);
        request.setAttribute("errores", null);
        
        RequestDispatcher dispatcher = request.getRequestDispatcher("/formulariopersona.jsp");
        dispatcher.forward(request, response);
    }
    
    private boolean editarPersona(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException{
     
        String codigo = request.getParameter("codigo");
        logger.info("Codigo: " + codigo);
        
        Persona persona;
        
        try {
            persona = personaDB.buscar(codigo);
            if (persona == null) {
                return false;
            }
        } catch (Exception e) {
            logger.error("Error recuperando los datos de la persona!");
            return false;
        }
        
        PersonaRequest personaRequest = new PersonaRequest(persona);
        
        request.setAttribute("persona", personaRequest);
        request.setAttribute("errores", null);
        
        RequestDispatcher dispatcher = request.getRequestDispatcher("/formulariopersona.jsp");
        dispatcher.forward(request, response);
        
        return true;
    }
    
    private String validarPersona(HttpServletRequest request) {
        String nombres = request.getParameter("nombres");
        String apellidos = request.getParameter("apellidos");
        String fechaNac = request.getParameter("fechaNac");
        logger.info("Fecha: " + fechaNac);
        String experiencia = request.getParameter("experiencia");
        
        return personaDB.validar(nombres, apellidos, fechaNac, experiencia);
    }
    
    private void mostrarErrores(String errores, HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException{
        
        String codigo = request.getParameter("codigo");
        String nombres = request.getParameter("nombres");
        String apellidos = request.getParameter("apellidos");
        String fechaNac = request.getParameter("fechaNac");
        logger.info("Fecha: " + fechaNac);
        String experiencia = request.getParameter("experiencia");
        
        PersonaRequest persona = new PersonaRequest(codigo, nombres, apellidos, fechaNac, experiencia);
        
        request.setAttribute("persona", persona);
        request.setAttribute("errores", errores);
        
        RequestDispatcher dispatcher = request.getRequestDispatcher("/formulariopersona.jsp");
        dispatcher.forward(request, response);
    }
    
    private boolean guardarPersona(HttpServletRequest request) {
        String codigo = request.getParameter("codigo");
        String nombres = request.getParameter("nombres");
        String apellidos = request.getParameter("apellidos");
        String fechaNac = request.getParameter("fechaNac");
        logger.info("Fecha: " + fechaNac);
        String experiencia = request.getParameter("experiencia");
        
        if (codigo.equals("0")) {
            try {
                return personaDB.insertar(nombres, apellidos, fechaNac, experiencia);
            } catch (Exception ex) {
                logger.error("Error guardando los datos de la persona!");
                return false;
            }
        } else {
            try {
                return personaDB.editar(codigo, nombres, apellidos, fechaNac, experiencia);
            } catch (Exception e) {
                logger.error("Error guardando los datos de la persona!");
                return false;
            }
        }
    }
    
    private boolean eliminarPersona(HttpServletRequest request) {
        String codigo = request.getParameter("codigo");
        try {
            return personaDB.eliminar(codigo);
        } catch (Exception e) {
            logger.error("Error eliminado los datos de la persona!");
            return false;
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }
}