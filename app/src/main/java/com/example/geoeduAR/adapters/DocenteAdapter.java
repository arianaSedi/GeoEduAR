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

import com.example.geoeduAR.models.Docente;
import com.example.geoeduAR.R;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

public class DocenteAdapter extends RecyclerView.Adapter<DocenteAdapter.ViewHolder> {

    // LISTA DE DOCENTES QUE SE MOSTRAR EN EL RECYCLERVIEW
    private ArrayList<Docente> lista;
    public DocenteAdapter(ArrayList<Docente> lista) {
        this.lista = lista;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // INFLA EL DISEÑO XML DE CADA CARD DE DOCENTE
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_docente, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        // OBTIENE EL DOCENTE SEGUN LA POSICION DE LA LISTA
        Docente docente = lista.get(position);

        // MUESTRA LOS DATOS PRINCIPALES DEL DOCENTE EN LA CARD
        holder.tvNombreDocente.setText(docente.nombre);
        holder.tvCargoDocente.setText(docente.cargo);
        holder.tvOficinaDocente.setText(docente.oficina);

        //POR DEFECTO EL DOCENTE APARECE COMO NO ENCONTRADO
        holder.tvEstadoEncontradoDocente.setText("No encontrado");
        holder.tvEstadoEncontradoDocente.setTextColor(holder.itemView.getContext().getResources().getColor(R.color.text_secondary));
        holder.tvEstadoEncontradoDocente.setBackgroundResource(R.drawable.no_encontrado);

        // VERIFICA SI EL DOCENTE YA FUE ENCONTRADO EN REALIDAD AUMENTADA
        if (docente.id != null && !docente.id.trim().isEmpty()) {

            FirebaseDatabase.getInstance()
                    .getReference("docentes_encontrados")
                    .child(docente.id)
                    .get()
                    .addOnSuccessListener(snapshot -> {

                        // OBTIENE EL VALOR BOOLEANO GUARDADO EN FIREBASE
                        Boolean encontrado = snapshot.getValue(Boolean.class);

                        // SI EL DOCENTE FUE ESCANEADO, SE MUESTRA COMO ENCONTRADO
                        if (encontrado != null && encontrado) {
                            holder.tvEstadoEncontradoDocente.setText("Encontrado");
                            holder.tvEstadoEncontradoDocente.setTextColor(holder.itemView.getContext().getResources().getColor(R.color.primary));
                            holder.tvEstadoEncontradoDocente.setBackgroundResource(R.drawable.bg_estado);
                        } else {

                            // SI NO EXISTE EN FIREBASE, SE MANTIENE COMO NO ENCONTRADO
                            holder.tvEstadoEncontradoDocente.setText("No encontrado");
                            holder.tvEstadoEncontradoDocente.setTextColor(holder.itemView.getContext().getResources().getColor(R.color.text_secondary));
                            holder.tvEstadoEncontradoDocente.setBackgroundResource(R.drawable.no_encontrado);
                        }
                    });
        }

        // BOTÓN PARA ABRIR GOOGLE MAPS CON LA UBICACION DEL DOCENTE
        holder.btnUbicarDocente.setOnClickListener(v -> {

            try {

                // CREA LA RUTA DE NAVEGACIÓN USANDO LATITUD Y LONGITUD DEL DOCENTE
                String uri = "google.navigation:q=" + docente.latitud + "," + docente.longitud;

                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));

                // INDICA QUE SE ABRA CON GOOGLE MAPS
                intent.setPackage("com.google.android.apps.maps");

                v.getContext().startActivity(intent);

            } catch (Exception e) {
                Toast.makeText(v.getContext(), "No se pudo abrir Google Maps", Toast.LENGTH_SHORT).show();
            }
        });

        // BOTON PARA MOSTRAR LA INFORMACION COMPLETA DEL DOCENTE EN UN DIALOGO
        holder.btnVerARDocente.setOnClickListener(v -> {

            View dialogView = LayoutInflater.from(v.getContext()).inflate(R.layout.dialogodocente, null);

            androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(v.getContext());

            builder.setView(dialogView);
            androidx.appcompat.app.AlertDialog dialog = builder.create();

            // HACE QUE EL FONDO DEL DIALOGO SEA TRANSPARENTE
            if (dialog.getWindow() != null) {
                dialog.getWindow().setBackgroundDrawable(
                        new android.graphics.drawable.ColorDrawable(
                                android.graphics.Color.TRANSPARENT
                        )
                );
            }

            // REFERENCIAS A LOS TEXTVIEW DEL DIALOGO
            TextView tvNombre = dialogView.findViewById(R.id.tvDialogNombre);
            TextView tvCargo = dialogView.findViewById(R.id.tvDialogCargo);
            TextView tvProfesion = dialogView.findViewById(R.id.tvDialogProfesion);
            TextView tvOficina = dialogView.findViewById(R.id.tvDialogOficina);
            TextView tvCorreo = dialogView.findViewById(R.id.tvDialogCorreo);
            TextView tvPasatiempos = dialogView.findViewById(R.id.tvDialogPasatiempos);
            TextView tvHistoria = dialogView.findViewById(R.id.tvDialogHistoriaTexto);
            MaterialButton btnCerrar = dialogView.findViewById(R.id.btnDialogCerrar);

            // MUESTRA LA INFORMACIÓN DEL DOCENTE, USANDO TEXTOS POR DEFECTO SI ALGÚN DATO VIENE VACÍO
            tvNombre.setText(docente.getNombre() != null ? docente.getNombre() : "Sin Nombre");
            tvCargo.setText(docente.getCargo() != null ? docente.getCargo() : "Docente de la FMO");

            tvProfesion.setText("Profesión: " + (docente.getProfesion() != null ? docente.getProfesion() : "No especificada"));
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

        // REFERENCIAS A LOS ELEMENTOS VISUALES DE CADA CARD
        TextView tvNombreDocente;
        TextView tvCargoDocente;
        TextView tvOficinaDocente;
        TextView tvEstadoEncontradoDocente;
        MaterialButton btnVerARDocente;
        MaterialButton btnUbicarDocente;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            // ENLAZA LOS ELEMENTOS DEL XML
            tvNombreDocente = itemView.findViewById(R.id.tvNombreDocente);
            tvCargoDocente = itemView.findViewById(R.id.tvCargoDocente);
            tvOficinaDocente = itemView.findViewById(R.id.tvOficinaDocente);
            tvEstadoEncontradoDocente = itemView.findViewById(R.id.tvEstadoEncontradoDocente);
            btnVerARDocente = itemView.findViewById(R.id.btnVerARDocente);
            btnUbicarDocente = itemView.findViewById(R.id.btnUbicarDocente);
        }
    }

    // ACTUALIZA LA LISTA CUANDO SE REALIZA UNA BUSQUEDA O FILTRO
    public void filtrarLista(ArrayList<Docente> listaFiltrada) {
        this.lista = listaFiltrada;
        notifyDataSetChanged();
    }
}