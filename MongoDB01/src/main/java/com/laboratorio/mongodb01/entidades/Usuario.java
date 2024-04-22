package com.laboratorio.mongodb01.entidades;

public record Usuario(
        int codigo,
        String nombre,
        String username,
        String password
        ) {}