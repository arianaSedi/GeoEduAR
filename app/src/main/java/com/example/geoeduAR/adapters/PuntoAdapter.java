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

    private final ArrayList<PuntoEducativo> lista;

    private double latUsuario = 0;
    private double lngUsuario = 0;
    private boolean tieneUbicacion = false;

    public PuntoAdapter(ArrayList<PuntoEducativo> lista) {
        this.lista = lista;
    }

    public void actualizarUbicacion(double latUsuario, double lngUsuario) {

        this.latUsuario = latUsuario;
        this.lngUsuario = lngUsuario;
        this.tieneUbicacion = true;

        // ORDENAR POR DISTANCIA
        lista.sort((p1, p2) -> {

            float[] d1 = new float[1];
            float[] d2 = new float[1];

            Location.distanceBetween(
                    latUsuario,
                    lngUsuario,
                    p1.latitud,
                    p1.longitud,
                    d1
            );

            Location.distanceBetween(
                    latUsuario,
                    lngUsuario,
                    p2.latitud,
                    p2.longitud,
                    d2
            );

            return Float.compare(d1[0], d2[0]);
        });

        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public PuntoAdapter.ViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent,
            int viewType
    ) {

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_punto_educativo, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(
            @NonNull PuntoAdapter.ViewHolder holder,
            int position
    ) {

        PuntoEducativo punto = lista.get(position);
        holder.tvNombrePunto.setText(punto.nombre);
        holder.tvDescripcionPunto.setText(punto.descripcion);

        holder.btnUbicacion.setOnClickListener(v -> {

            try {
                String uri = "google.navigation:q=" + punto.latitud + "," + punto.longitud;
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri)
                );

                intent.setPackage("com.google.android.apps.maps");
                v.getContext().startActivity(intent);

            } catch (Exception e) {

                Toast.makeText(v.getContext(), "No se pudo abrir Google Maps", Toast.LENGTH_SHORT).show();
            }
        });

        // =========================
        // VALIDACION GPS
        // =========================
        if (tieneUbicacion) {

            float[] resultado = new float[1];

            Location.distanceBetween(
                    latUsuario,
                    lngUsuario,
                    punto.latitud,
                    punto.longitud,
                    resultado
            );

            float distancia = resultado[0];

            // MOSTRAR EL MÁS CERCANO
            String etiqueta =
                    position == 0
                            ? "Más cercano | "
                            : "";

            holder.tvRadioPunto.setText(
                    etiqueta
                            + "Distancia: "
                            + Math.round(distancia)
                            + " m | Radio: "
                            + punto.radioMetros
                            + " m"
            );

            boolean puedeAR =
                    distancia <= punto.radioMetros
                            && punto.disponibleAR;

            holder.btnVerAR.setEnabled(puedeAR);

            holder.btnVerAR.setAlpha(
                    puedeAR ? 1f : 0.5f
            );

        } else {

            holder.tvRadioPunto.setText("GPS no disponible | Radio: " + punto.radioMetros + " m"
            );

            holder.btnVerAR.setEnabled(false);
            holder.btnVerAR.setAlpha(0.5f);
        }

        holder.btnVerAR.setOnClickListener(v -> {

            if (punto.docenteId == null
                    || punto.docenteId.isEmpty()) {

                abrirAR(v, punto, null);
                return;
            }

            FirebaseDatabase.getInstance()
                    .getReference("docentes")
                    .child(punto.docenteId)
                    .get()
                    .addOnSuccessListener(snapshot -> {

                        Docente docente =
                                snapshot.getValue(Docente.class);

                        abrirAR(v, punto, docente);

                    })
                    .addOnFailureListener(e -> {

                        Toast.makeText(
                                v.getContext(),
                                "No se pudo cargar información del docente",
                                Toast.LENGTH_SHORT
                        ).show();

                        abrirAR(v, punto, null);
                    });
        });
    }
    private void abrirAR(
            View v,
            PuntoEducativo punto,
            Docente docente
    ) {

        String contenido = "";

        contenido += "Información del punto:\n\n";

        if (punto.descripcion != null) {
            contenido += punto.descripcion + "\n\n";
        }

        if (punto.contenidoAR != null) {
            contenido += punto.contenidoAR + "\n\n";
        }

        if (docente != null) {

            contenido +=
                    "Docente asociado al punto educativo:\n\n";

            contenido +=
                    "Este docente forma parte del entorno educativo "
                            + "de este punto y representa el conocimiento "
                            + "disponible en esta ubicación.\n\n";

            contenido += "Nombre: " + docente.nombre + "\n";
            contenido += "Cargo: " + docente.cargo + "\n";
            contenido += "Carrera: " + docente.carrera + "\n";
            contenido += "Profesión: " + docente.profesion + "\n";
            contenido += "Oficina: " + docente.oficina + "\n";
            contenido += "Correo: " + docente.correo + "\n\n";

            contenido += "Historia:\n" + docente.historia;

        } else {

            contenido += "No hay docente relacionado registrado.";
        }

        Intent intent = new Intent(v.getContext(), ARActivity.class);

        intent.putExtra("nombre", punto.nombre);
        intent.putExtra("descripcion", punto.descripcion);
        intent.putExtra("contenidoAR", contenido);
        intent.putExtra("modelo3D", punto.modelo3D);
        intent.putExtra("imagenReferencia", punto.imagenReferencia);
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
        TextView tvRadioPunto;
        MaterialButton btnUbicacion;
        MaterialButton btnVerAR;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            tvNombrePunto = itemView.findViewById(R.id.tvNombrePunto);
            tvDescripcionPunto = itemView.findViewById(R.id.tvDescripcionPunto);
            tvRadioPunto = itemView.findViewById(R.id.tvRadioPunto);
            btnVerAR = itemView.findViewById(R.id.btnVerAR);
            btnUbicacion = itemView.findViewById(R.id.btnUbicacion);
        }
    }
}