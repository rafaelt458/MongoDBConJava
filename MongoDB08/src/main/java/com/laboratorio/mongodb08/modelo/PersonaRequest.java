package com.laboratorio.mongodb08.modelo;

import java.text.SimpleDateFormat;

public class PersonaRequest {
    private String id;
    private String nombres;
    private String apellidos;
    private String fechaNac;
    private String experiencia;

    public PersonaRequest() {
        this.id = "0";
        this.nombres = "";
        this.apellidos = "";
        this.fechaNac = null;
        this.experiencia = "0";
    }

    public PersonaRequest(String id, String nombres, String apellidos, String fechaNac, String experiencia) {
        this.id = id;
        this.nombres = nombres;
        this.apellidos = apellidos;
        this.fechaNac = fechaNac;
        this.experiencia = experiencia;
    }
    
    public PersonaRequest(Persona persona) {
        this.id = persona.getId().toString();
        this.nombres = persona.getNombres();
        this.apellidos = persona.getApellidos();
        SimpleDateFormat formato = new SimpleDateFormat("yyyy-MM-dd");
        this.fechaNac = formato.format(persona.getFechaNac());
        this.experiencia = String.valueOf(persona.getExperiencia());
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getNombres() {
        return nombres;
    }

    public void setNombres(String nombres) {
        this.nombres = nombres;
    }

    public String getApellidos() {
        return apellidos;
    }

    public void setApellidos(String apellidos) {
        this.apellidos = apellidos;
    }

    public String getFechaNac() {
        return fechaNac;
    }

    public void setFechaNac(String fechaNac) {
        this.fechaNac = fechaNac;
    }

    public String getExperiencia() {
        return experiencia;
    }

    public void setExperiencia(String experiencia) {
        this.experiencia = experiencia;
    }
}
