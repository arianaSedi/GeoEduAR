package com.example.geoeduAR.adapters;

import android.content.Intent;
import android.location.Location;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.geoeduAR.ARActivity;
import com.example.geoeduAR.R;
import com.example.geoeduAR.models.Docente;
import com.example.geoeduAR.models.PuntoEducativo;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.database.FirebaseDatabase;
import java.util.ArrayList;
public class PuntoAdapter extends RecyclerView.Adapter<PuntoAdapter.ViewHolder> {

    // LISTA DE PUNTOS EDUCATIVOS QUE SE MOSTRARAN EN EL RECYCLERVIEW
    private ArrayList<PuntoEducativo> lista;

    // COORDENADAS ACTUALES DEL USUARIO
    private double latUsuario = 0;
    private double lngUsuario = 0;
    private boolean tieneUbicacion = false;
    public PuntoAdapter(ArrayList<PuntoEducativo> lista) {
        this.lista = lista;
    }

    // ACTUALIZA LA UBICACION DEL USUARIO Y ORDENA LOS PUNTOS POR CERCANIA
    public void actualizarUbicacion(double latUsuario, double lngUsuario) {

        this.latUsuario = latUsuario;
        this.lngUsuario = lngUsuario;
        this.tieneUbicacion = true;

        // ORDENA LA LISTA DE PUNTOS EDUCATIVOS SEGUN LA DISTANCIA AL USUARIO
        lista.sort((p1, p2) -> {

            float[] d1 = new float[1];
            float[] d2 = new float[1];

            // CALCULA LA DISTANCIA ENTRE EL USUARIO Y EL PRIMER PUNTO
            Location.distanceBetween(
                    latUsuario,
                    lngUsuario,
                    p1.latitud,
                    p1.longitud,
                    d1
            );

            // CALCULA LA DISTANCIA ENTRE EL USUARIO Y EL SEGUNDO PUNTO
            Location.distanceBetween(
                    latUsuario,
                    lngUsuario,
                    p2.latitud,
                    p2.longitud,
                    d2
            );

            // COMPARA DISTANCIAS PARA ORDENAR DE MENOR A MAYOR
            return Float.compare(d1[0], d2[0]);
        });

        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public PuntoAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_punto_educativo, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PuntoAdapter.ViewHolder holder, int position) {

        // OBTIENE EL PUNTO EDUCATIVO SEGUN SU POSICION EN LA LISTA
        PuntoEducativo punto = lista.get(position);

        holder.tvNombrePunto.setText(punto.nombre);
        holder.tvDescripcionPunto.setText(punto.descripcion);

        // VERIFICA SI EL PUNTO EDUCATIVO TIENE UN DOCENTE ASOCIADO
        if (punto.docenteId != null && !punto.docenteId.isEmpty()) {

            holder.tvDocentePunto.setText("Docente: Cargando...");

            // CONSULTA EN FIREBASE EL DOCENTE RELACIONADO CON ESTE PUNTO
            FirebaseDatabase.getInstance()
                    .getReference("docentes")
                    .child(punto.docenteId)
                    .get()
                    .addOnSuccessListener(snapshot -> {

                        Docente docente = snapshot.getValue(Docente.class);

                        // SI EL DOCENTE EXISTE, SE MUESTRA SU NOMBRE
                        if (docente != null && docente.nombre != null) {
                            holder.tvDocentePunto.setText("Docente: " + docente.nombre);
                        } else {
                            holder.tvDocentePunto.setText("Docente: No encontrado");
                        }
                    })
                    .addOnFailureListener(e -> {
                        holder.tvDocentePunto.setText("Docente: Error al cargar");
                    });

        } else {
            // SI NO HAY DOCENTE ASOCIADO
            holder.tvDocentePunto.setText("Docente: No asignado");
        }

        // BOTON PARA ABRIR GOOGLE MAPS CON LA UBICACION DEL PUNTO EDUCATIVO
        holder.btnUbicacion.setOnClickListener(v -> {

            try {
                // CREA LA RUTA DE NAVEGACION USANDO LATITUD Y LONGITUD
                String uri = "google.navigation:q=" + punto.latitud + "," + punto.longitud;

                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));

                // INDICA QUE SE ABRA CON GOOGLE MAPS
                intent.setPackage("com.google.android.apps.maps");
                v.getContext().startActivity(intent);

            } catch (Exception e) {
                Toast.makeText(v.getContext(), "No se pudo abrir Google Maps", Toast.LENGTH_SHORT).show();
            }
        });

        // VERIFICA SI YA EXISTE UBICACION DEL USUARIO PARA CALCULAR DISTANCIA
        if (tieneUbicacion) {

            float[] resultado = new float[1];

            // CALCULA LA DISTANCIA ENTRE EL USUARIO Y EL PUNTO EDUCATIVO
            Location.distanceBetween(
                    latUsuario,
                    lngUsuario,
                    punto.latitud,
                    punto.longitud,
                    resultado
            );

            float distancia = resultado[0];

            // MARCA EL PRIMER PUNTO COMO EL MAS CERCANO DESPUES DE ORDENAR LA LISTA
            String etiqueta = position == 0 ? "Más cercano | " : "";

            // MUESTRA DISTANCIA Y RADIO PERMITIDO PARA ACTIVAR AR
            holder.tvRadioPunto.setText(etiqueta + "Distancia: " + Math.round(distancia) + " m | Radio: " + punto.radioMetros + " m");

            // PERMITE USAR AR SOLO SI EL USUARIO ESTA DENTRO DEL RADIO Y EL PUNTO TIENE AR DISPONIBLE
            boolean puedeAR = distancia <= punto.radioMetros && punto.disponibleAR;

            // ACTIVA O DESACTIVA EL BOTON DE AR SEGUN LA DISTANCIA
            holder.btnVerAR.setEnabled(puedeAR);
            holder.btnVerAR.setAlpha(puedeAR ? 1f : 0.5f);

        } else {

            // SI NO HAY GPS, SE DESACTIVA
            holder.tvRadioPunto.setText("GPS no disponible | Radio: " + punto.radioMetros + " m");
            holder.btnVerAR.setEnabled(false);
            holder.btnVerAR.setAlpha(0.5f);
        }

        holder.btnVerAR.setOnClickListener(v -> {

            // SI NO HAY DOCENTE ASOCIADO, SE ABRE AR SOLO CON LA INFORMACION DEL PUNTO
            if (punto.docenteId == null || punto.docenteId.isEmpty()) {

                abrirAR(v, punto, null);
                return;
            }

            // CONSULTA EL DOCENTE EN FIREBASE ANTES DE ABRIR AR
            FirebaseDatabase.getInstance()
                    .getReference("docentes")
                    .child(punto.docenteId)
                    .get()
                    .addOnSuccessListener(snapshot -> {

                        Docente docente = snapshot.getValue(Docente.class);

                        // ABRE AR CON LA INFORMACION DEL PUNTO Y DEL DOCENTE
                        abrirAR(v, punto, docente);

                    })
                    .addOnFailureListener(e -> {

                        Toast.makeText(v.getContext(), "No se pudo cargar información del docente", Toast.LENGTH_SHORT).show();

                        // SI FALLA LA CONSULTA, SE ABRE AR SIN DATOS DEL DOCENTE
                        abrirAR(v, punto, null);
                    });
        });
    }

    // PREPARA Y ENVIA TODA LA INFORMACION NECESARIA PARA ABRIR ARACTIVITY
    private void abrirAR(View v, PuntoEducativo punto, Docente docente) {

        String contenido = "";

        contenido += "Información del punto:\n\n";

        // AGREGA EL CONTENIDO ESPECIFICO DE REALIDAD AUMENTADA DEL PUNTO
        if (punto.contenidoAR != null && !punto.contenidoAR.trim().isEmpty()) {
            contenido += punto.contenidoAR + "\n\n";
        }
        if (docente != null) {

            contenido += "Este docente forma parte del entorno educativo "
                    + "de este punto y representa el conocimiento "
                    + "disponible en esta ubicación.\n\n";

            if (docente.nombre != null && !docente.nombre.trim().isEmpty()) {
                contenido += "Nombre: " + docente.nombre + "\n";
            }
            if (docente.cargo != null && !docente.cargo.trim().isEmpty()) {
                contenido += "Cargo: " + docente.cargo + "\n";
            }
            if (docente.profesion != null && !docente.profesion.trim().isEmpty()) {
                contenido += "Profesión: " + docente.profesion + "\n";
            }
            if (docente.oficina != null && !docente.oficina.trim().isEmpty()) {
                contenido += "Oficina: " + docente.oficina + "\n";
            }
            if (docente.correo != null && !docente.correo.trim().isEmpty()) {
                contenido += "Correo: " + docente.correo + "\n";
            }
            if (docente.historia != null && !docente.historia.trim().isEmpty()) {
                contenido += "\nHistoria:\n" + docente.historia;
            }

        } else {

            // MENSAJE SI NO EXISTE DOCENTE RELACIONADO AL PUNTO
            contenido += "No hay docente relacionado registrado.";
        }

        // CREA EL INTENT PARA ENVIAR LOS DATOS A ARACTIVITY
        Intent intent = new Intent(v.getContext(), ARActivity.class);

        intent.putExtra("nombre", punto.nombre);
        intent.putExtra("descripcion", punto.descripcion);
        intent.putExtra("contenidoAR", contenido);
        intent.putExtra("modelo3D", punto.modelo3D);

        // ENVIA EL ID DEL DOCENTE PARA MARCARLO COMO ENCONTRADO EN FIREBASE
        intent.putExtra("docenteId", punto.docenteId);

        // VERIFICA SI EL PUNTO TIENE IMAGEN DE REFERENCIA PARA DETECCION AR
        boolean tieneImagenReferencia = punto.imagenReferencia != null && !punto.imagenReferencia.trim().isEmpty();

        // ENVIA SI SE USARA O NO DETECCION DE IMAGEN
        intent.putExtra("usarImagenReferencia", tieneImagenReferencia);
        intent.putExtra("imagenReferencia", tieneImagenReferencia ? punto.imagenReferencia : "");
        intent.putExtra("recursoMultimedia", punto.recursoMultimedia);
        intent.putExtra("tipoMultimedia", punto.tipoMultimedia);
        intent.putExtra("posicionX", punto.posicionX);
        intent.putExtra("posicionY", punto.posicionY);
        intent.putExtra("posicionZ", punto.posicionZ);

        v.getContext().startActivity(intent);
    }

    @Override
    public int getItemCount() {
        return lista.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        TextView tvNombrePunto;
        TextView tvDescripcionPunto;
        TextView tvDocentePunto;
        TextView tvRadioPunto;
        MaterialButton btnUbicacion;
        MaterialButton btnVerAR;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvNombrePunto = itemView.findViewById(R.id.tvNombrePunto);
            tvDescripcionPunto = itemView.findViewById(R.id.tvDescripcionPunto);
            tvDocentePunto = itemView.findViewById(R.id.tvDocentePunto);
            tvRadioPunto = itemView.findViewById(R.id.tvRadioPunto);
            btnVerAR = itemView.findViewById(R.id.btnVerAR);
            btnUbicacion = itemView.findViewById(R.id.btnUbicacion);
        }
    }

    // ACTUALIZA LA LISTA CUANDO SE REALIZA UNA BUSQUEDA O FILTRO
    public void filtrarlistaa(ArrayList<PuntoEducativo> listaFiltrada) {
        this.lista = listaFiltrada;
        notifyDataSetChanged();
    }
}