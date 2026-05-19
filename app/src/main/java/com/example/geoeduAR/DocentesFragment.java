package com.example.geoeduAR;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
    private DocenteAdapter adapter;

    public DocentesFragment() {
    }

    @Override
    public View onCreateView(
            LayoutInflater inflater,
            ViewGroup container,
            Bundle savedInstanceState
    ) {
        return inflater.inflate(R.layout.fragment_docentes, container, false);
    }

    @Override
    public void onViewCreated(
            @NonNull View view,
            @Nullable Bundle savedInstanceState
    ) {
        super.onViewCreated(view, savedInstanceState);

        recyclerDocentes = view.findViewById(R.id.recyclerDocentes);
        tvEstadoDocentes = view.findViewById(R.id.tvEstadoDocentes);

        lista = new ArrayList<>();
        adapter = new DocenteAdapter(lista);

        recyclerDocentes.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerDocentes.setAdapter(adapter);

        cargarDocentes();
    }

    private void cargarDocentes() {
        FirebaseDatabase.getInstance()
                .getReference("docentes")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        lista.clear();

                        for (DataSnapshot item : snapshot.getChildren()) {
                            Docente docente = item.getValue(Docente.class);

                            if (docente != null) {
                                lista.add(docente);
                            }
                        }

                        adapter.notifyDataSetChanged();

                        if (lista.isEmpty()) {
                            tvEstadoDocentes.setText("No hay docentes registrados");
                        } else {
                            tvEstadoDocentes.setText(lista.size() + " docentes registrados");
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        tvEstadoDocentes.setText("Error al cargar docentes");
                        Toast.makeText(
                                requireContext(),
                                error.getMessage(),
                                Toast.LENGTH_SHORT
                        ).show();
                    }
                });
    }
}