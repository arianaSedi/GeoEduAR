package com.example.geoeduAR.models;

public class Docente {

    public String id;
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

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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

    public void setCargo(String cargo) {
        this.cargo = cargo;
    }

    public void setCarrera(String carrera) {
        this.carrera = carrera;
    }

    public void setCorreo(String correo) {
        this.correo = correo;
    }

    public void setDisponibleAR(boolean disponibleAR) {
        this.disponibleAR = disponibleAR;
    }

    public void setHistoria(String historia) {
        this.historia = historia;
    }

    public void setLatitud(double latitud) {
        this.latitud = latitud;
    }

    public void setLongitud(double longitud) {
        this.longitud = longitud;
    }

    public void setModelo3D(String modelo3D) {
        this.modelo3D = modelo3D;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public void setOficina(String oficina) {
        this.oficina = oficina;
    }

    public void setPasatiempos(String pasatiempos) {
        this.pasatiempos = pasatiempos;
    }

    public void setProfesion(String profesion) {
        this.profesion = profesion;
    }

    public void setRadioMetros(int radioMetros) {
        this.radioMetros = radioMetros;
    }
}