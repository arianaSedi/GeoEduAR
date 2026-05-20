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

    private ArrayList<Docente> lista;

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

            View dialogView = LayoutInflater.from(v.getContext()).inflate(R.layout.dialogodocente, null);

            androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(v.getContext());
            builder.setView(dialogView);
            androidx.appcompat.app.AlertDialog dialog = builder.create();

            if (dialog.getWindow() != null) {
                dialog.getWindow().setBackgroundDrawable(new android.graphics.drawable.ColorDrawable(android.graphics.Color.TRANSPARENT));
            }
            TextView tvNombre = dialogView.findViewById(R.id.tvDialogNombre);
            TextView tvCargo = dialogView.findViewById(R.id.tvDialogCargo);
            TextView tvProfesion = dialogView.findViewById(R.id.tvDialogProfesion);
            TextView tvOficina = dialogView.findViewById(R.id.tvDialogOficina);
            TextView tvCorreo = dialogView.findViewById(R.id.tvDialogCorreo);
            TextView tvPasatiempos = dialogView.findViewById(R.id.tvDialogPasatiempos);
            TextView tvHistoria = dialogView.findViewById(R.id.tvDialogHistoriaTexto);
            MaterialButton btnCerrar = dialogView.findViewById(R.id.btnDialogCerrar);

            tvNombre.setText(docente.getNombre() != null ? docente.getNombre() : "Sin Nombre");
            tvCargo.setText(docente.getCargo() != null ? docente.getCargo() : "Docente de la FMO");

            tvProfesion.setText("Profesión: " + (docente.getProfesion() != null ? docente.getProfesion() : "No especificada"));;
            tvOficina.setText("Oficina: " + (docente.getOficina() != null ? docente.getOficina() : "No asignada"));
            tvCorreo.setText("Correo: " + (docente.getCorreo() != null ? docente.getCorreo() : "No disponible"));
            tvPasatiempos.setText("Pasatiempos: " + (docente.getPasatiempos() != null ? docente.getPasatiempos() : "Ninguno"));
            tvHistoria.setText(docente.getHistoria() != null ? docente.getHistoria() : "Sin historia registrada en el sistema.");

            btnCerrar.setOnClickListener(view -> dialog.dismiss());
            dialog.show();
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
    public void filtrarLista(ArrayList<Docente> listaFiltrada) {
        this.lista = listaFiltrada;
        notifyDataSetChanged();
    }
}