package com.example.geoeduAR.helper;

import android.location.Location;

public class LocacionHelper {

    public static float calcularDistanciaMetros(
            double latUsuario,
            double lngUsuario,
            double latDestino,
            double lngDestino
    ) {
        float[] resultado = new float[1];

        Location.distanceBetween(
                latUsuario,
                lngUsuario,
                latDestino,
                lngDestino,
                resultado
        );

        return resultado[0];
    }
}
