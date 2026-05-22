package com.example.geoeduAR.models;

public class PuntoEducativo {

    public String nombre;
    public String descripcion;
    public double latitud;
    public double longitud;
    public int radioMetros;
    public String modelo3D;
    public boolean disponibleAR;
    public String contenidoAR;
    public String docenteId;

    public String imagenReferencia;
    public String recursoMultimedia;
    public String tipoMultimedia;

    public float posicionX;
    public float posicionY;
    public float posicionZ;
    public PuntoEducativo() {
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public double getLatitud() {
        return latitud;
    }

    public void setLatitud(double latitud) {
        this.latitud = latitud;
    }

    public double getLongitud() {
        return longitud;
    }

    public void setLongitud(double longitud) {
        this.longitud = longitud;
    }

    public int getRadioMetros() {
        return radioMetros;
    }

    public void setRadioMetros(int radioMetros) {
        this.radioMetros = radioMetros;
    }

    public String getModelo3D() {
        return modelo3D;
    }

    public void setModelo3D(String modelo3D) {
        this.modelo3D = modelo3D;
    }

    public boolean isDisponibleAR() {
        return disponibleAR;
    }

    public void setDisponibleAR(boolean disponibleAR) {
        this.disponibleAR = disponibleAR;
    }

    public String getContenidoAR() {
        return contenidoAR;
    }

    public void setContenidoAR(String contenidoAR) {
        this.contenidoAR = contenidoAR;
    }

    public String getDocenteId() {
        return docenteId;
    }

    public void setDocenteId(String docenteId) {
        this.docenteId = docenteId;
    }

    public String getImagenReferencia() {
        return imagenReferencia;
    }

    public void setImagenReferencia(String imagenReferencia) {
        this.imagenReferencia = imagenReferencia;
    }

    public String getRecursoMultimedia() {
        return recursoMultimedia;
    }

    public void setRecursoMultimedia(String recursoMultimedia) {
        this.recursoMultimedia = recursoMultimedia;
    }

    public String getTipoMultimedia() {
        return tipoMultimedia;
    }

    public void setTipoMultimedia(String tipoMultimedia) {
        this.tipoMultimedia = tipoMultimedia;
    }

    public float getPosicionX() {
        return posicionX;
    }

    public void setPosicionX(float posicionX) {
        this.posicionX = posicionX;
    }

    public float getPosicionY() {
        return posicionY;
    }

    public void setPosicionY(float posicionY) {
        this.posicionY = posicionY;
    }

    public float getPosicionZ() {
        return posicionZ;
    }

    public void setPosicionZ(float posicionZ) {
        this.posicionZ = posicionZ;
    }
}