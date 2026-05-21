package com.example.geoeduAR;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.geoeduAR.adapters.DocenteAdapter;
import com.example.geoeduAR.models.Docente;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class DocentesFragment extends Fragment {
    private RecyclerView recyclerDocentes;
    private TextView tvEstadoDocentes;
    private ArrayList<Docente> lista;
    private EditText etBuscarDocente;

    // LISTA ORIGINAL PARA CONSERVAR TODOS LOS DOCENTES AL FILTRAR
    private ArrayList<Docente> listaOriginal;
    private DocenteAdapter adapter;
    private ProgressBar progressBar;

    public DocentesFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_docentes, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        recyclerDocentes = view.findViewById(R.id.recyclerDocentes);
        tvEstadoDocentes = view.findViewById(R.id.tvEstadoDocentes);
        etBuscarDocente = view.findViewById(R.id.etBuscarDocente);
        progressBar = view.findViewById(R.id.progressBar);

        lista = new ArrayList<>();
        listaOriginal = new ArrayList<>();
        adapter = new DocenteAdapter(lista);

      //MOSTRAR LOS DOCENTES EN FORMA DE LISTA
        recyclerDocentes.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerDocentes.setAdapter(adapter);
        cargarDocentes();

        // ESCUCHA LOS CAMBIOS EN EL BUSCADOR PARA FILTRAR EN TIEMPO REAL
        etBuscarDocente.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filtrar(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void filtrar(String texto) {
        // CREA UNA LISTA TEMPORAL PARA GUARDAR LOS RESULTADOS FILTRADOS
        ArrayList<Docente> listaFiltrada = new ArrayList<>();

        // RECORRE LA LISTA ORIGINAL PARA BUSCAR COINCIDENCIAS POR NOMBRE
        for (Docente docente : listaOriginal) {
            if (docente.getNombre().toLowerCase().contains(texto.toLowerCase())) {
                listaFiltrada.add(docente);
            }
        }
        // ENVIA LA LISTA FILTRADA AL ADAPTADOR
        adapter.filtrarLista(listaFiltrada);
    }

    private void cargarDocentes() {
        FirebaseDatabase.getInstance()
                .getReference("docentes")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        // LIMPIA LAS LISTAS ANTES DE CARGAR DATOS NUEVOS
                        lista.clear();
                        listaOriginal.clear();

                        // RECORRE CADA DOCENTE GUARDADO EN FIREBASE
                        for (DataSnapshot item : snapshot.getChildren()) {
                            Docente docente = item.getValue(Docente.class);

                            if (docente != null) {
                                // GUARDA EL ID REAL DEL NODO PARA COMPARARLO CON DOCENTES_ENCONTRADOS
                                docente.id = item.getKey();

                                // AGREGA EL DOCENTE A LA LISTA VISIBLE Y A LA LISTA ORIGINAL
                                lista.add(docente);
                                listaOriginal.add(docente);
                            }
                        }

                        String textoActual = etBuscarDocente.getText().toString();
                        filtrar(textoActual);

                        if (progressBar != null) {
                            progressBar.setVisibility(View.GONE);
                        }

                        if (listaOriginal.isEmpty()) {
                            tvEstadoDocentes.setText("No hay docentes registrados");
                        } else {
                            tvEstadoDocentes.setText(listaOriginal.size() + " docentes registrados");
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        tvEstadoDocentes.setText("Error al cargar docentes");
                        Toast.makeText(requireContext(), error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
}