package com.example.geoeduAR.adapters;

import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.geoeduAR.ARActivity;
import com.example.geoeduAR.models.Docente;
import com.example.geoeduAR.R;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;

public class DocenteAdapter extends RecyclerView.Adapter<DocenteAdapter.ViewHolder> {

    private final ArrayList<Docente> lista;

    public DocenteAdapter(ArrayList<Docente> lista) {
        this.lista = lista;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent,
            int viewType
    ) {

        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_docente, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(
            @NonNull ViewHolder holder,
            int position
    ) {

        Docente docente = lista.get(position);

        holder.tvNombreDocente.setText(docente.nombre);
        holder.tvCargoDocente.setText(docente.cargo);
        holder.tvOficinaDocente.setText(docente.oficina);

        holder.btnUbicarDocente.setOnClickListener(v -> {

            try {

                String uri = "google.navigation:q="
                        + docente.latitud
                        + ","
                        + docente.longitud;

                Intent intent = new Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse(uri)
                );

                intent.setPackage("com.google.android.apps.maps");

                v.getContext().startActivity(intent);

            } catch (Exception e) {

                Toast.makeText(
                        v.getContext(),
                        "No se pudo abrir Google Maps",
                        Toast.LENGTH_SHORT
                ).show();
            }
        });

        holder.btnVerARDocente.setOnClickListener(v -> {

            String info =
                    "Nombre: " + docente.nombre + "\n\n" +
                            "Cargo: " + docente.cargo + "\n\n" +
                            "Profesión: " + docente.profesion + "\n\n" +
                            "Carrera: " + docente.carrera + "\n\n" +
                            "Oficina: " + docente.oficina + "\n\n" +
                            "Correo: " + docente.correo + "\n\n" +
                            "Pasatiempos: " + docente.pasatiempos + "\n\n" +
                            "Historia:\n" + docente.historia;

            new androidx.appcompat.app.AlertDialog.Builder(v.getContext())
                    .setTitle(docente.nombre)
                    .setMessage(info)
                    .setPositiveButton("Cerrar", null)
                    .show();
        });
    }

    @Override
    public int getItemCount() {
        return lista.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        TextView tvNombreDocente;
        TextView tvCargoDocente;
        TextView tvOficinaDocente;
        MaterialButton btnVerARDocente;
        MaterialButton btnUbicarDocente;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            tvNombreDocente = itemView.findViewById(R.id.tvNombreDocente);
            tvCargoDocente = itemView.findViewById(R.id.tvCargoDocente);
            tvOficinaDocente = itemView.findViewById(R.id.tvOficinaDocente);
            btnVerARDocente = itemView.findViewById(R.id.btnVerARDocente);
            btnUbicarDocente = itemView.findViewById(R.id.btnUbicarDocente);
        }
    }
}