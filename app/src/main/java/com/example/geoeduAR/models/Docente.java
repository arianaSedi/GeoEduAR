package com.example.geoeduAR.models;

public class Docente {

    public String nombre;
    public String cargo;
    public String correo;
    public String oficina;
    public double latitud;
    public double longitud;
    public int radioMetros;
    public String profesion;
    public String pasatiempos;
    public String historia;
    public String carrera;
    public String modelo3D;
    public boolean disponibleAR;

    public Docente() {
    }

    public String getCargo() {
        return cargo;
    }

    public String getCarrera() {
        return carrera;
    }

    public String getCorreo() {
        return correo;
    }

    public boolean isDisponibleAR() {
        return disponibleAR;
    }

    public String getHistoria() {
        return historia;
    }

    public double getLatitud() {
        return latitud;
    }

    public double getLongitud() {
        return longitud;
    }

    public String getModelo3D() {
        return modelo3D;
    }

    public String getNombre() {
        return nombre;
    }

    public String getOficina() {
        return oficina;
    }

    public String getPasatiempos() {
        return pasatiempos;
    }

    public String getProfesion() {
        return profesion;
    }

    public int getRadioMetros() {
        return radioMetros;
    }
}