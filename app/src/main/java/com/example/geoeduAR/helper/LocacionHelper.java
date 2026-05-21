package com.example.geoeduAR.helper;

import android.location.Location;

public class LocacionHelper {

    // METODO PARA CALCULAR LA DISTANCIA EN METROS ENTRE EL USUARIO Y UN DESTINO
    public static float calcularDistanciaMetros(
            double latUsuario,
            double lngUsuario,
            double latDestino,
            double lngDestino
    ) {
        // ARREGLO DONDE ANDROID GUARDA EL RESULTADO DE LA DISTANCIA
        float[] resultado = new float[1];

        // CALCULA LA DIST ENTRE DOS COORDENADAS GPS
        Location.distanceBetween(latUsuario, lngUsuario, latDestino, lngDestino, resultado);

        // DEVUELVE LA DIST EN METROS
        return resultado[0];
    }
}