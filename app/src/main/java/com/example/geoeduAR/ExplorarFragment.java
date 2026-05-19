package com.example.geoeduAR;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.geoeduAR.adapters.PuntoAdapter;
import com.example.geoeduAR.models.PuntoEducativo;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class ExplorarFragment extends Fragment {

    private RecyclerView recyclerExplorar;
    private TextView tvEstadoExplorar;
    private ArrayList<PuntoEducativo> lista;
    private PuntoAdapter adapter;

    private FusedLocationProviderClient fusedLocationClient;

    private final ActivityResultLauncher<String> permisoUbicacion =
            registerForActivityResult(
                    new ActivityResultContracts.RequestPermission(),
                    isGranted -> {
                        if (isGranted) {
                            obtenerUbicacionUsuario();
                        } else {
                            tvEstadoExplorar.setText("Permiso de ubicación denegado");
                            Toast.makeText(requireContext(), "Activa el GPS para habilitar AR por cercanía", Toast.LENGTH_SHORT).show();
                        }
                    }
            );

    public ExplorarFragment() {
    }

    @Override
    public View onCreateView(
            LayoutInflater inflater,
            ViewGroup container,
            Bundle savedInstanceState
    ) {
        return inflater.inflate(R.layout.fragment_explorar, container, false);
    }

    @Override
    public void onViewCreated(
            @NonNull View view,
            @Nullable Bundle savedInstanceState
    ) {
        super.onViewCreated(view, savedInstanceState);

        recyclerExplorar = view.findViewById(R.id.recyclerExplorar);
        tvEstadoExplorar = view.findViewById(R.id.tvEstadoExplorar);

        lista = new ArrayList<>();
        adapter = new PuntoAdapter(lista);

        recyclerExplorar.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerExplorar.setAdapter(adapter);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity());

        cargarPuntosEducativos();
        verificarPermisoUbicacion();
    }

    private void verificarPermisoUbicacion() {
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED) {

            obtenerUbicacionUsuario();

        } else {
            permisoUbicacion.launch(Manifest.permission.ACCESS_FINE_LOCATION);
        }
    }

    private void obtenerUbicacionUsuario() {
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
        ) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(location -> {
                    if (location != null) {
                        double latUsuario = location.getLatitude();
                        double lngUsuario = location.getLongitude();

                        adapter.actualizarUbicacion(latUsuario, lngUsuario);

                        tvEstadoExplorar.setText("Ubicación detectada. Selecciona un punto cercano.");
                    } else {
                        tvEstadoExplorar.setText("No se pudo obtener la ubicación. Activa GPS.");
                    }
                })
                .addOnFailureListener(e -> {
                    tvEstadoExplorar.setText("Error al obtener ubicación");
                    Toast.makeText(requireContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void cargarPuntosEducativos() {
        FirebaseDatabase.getInstance()
                .getReference("puntos_educativos")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        lista.clear();

                        for (DataSnapshot item : snapshot.getChildren()) {
                            PuntoEducativo punto = item.getValue(PuntoEducativo.class);

                            if (punto != null) {
                                lista.add(punto);
                            }
                        }

                        adapter.notifyDataSetChanged();

                        if (lista.isEmpty()) {
                            tvEstadoExplorar.setText("No hay puntos educativos registrados");
                        } else {
                            tvEstadoExplorar.setText(lista.size() + " puntos educativos disponibles");
                        }

                        obtenerUbicacionUsuario();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        tvEstadoExplorar.setText("Error al cargar puntos");
                        Toast.makeText(
                                requireContext(),
                                error.getMessage(),
                                Toast.LENGTH_SHORT
                        ).show();
                    }
                });
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
    }
}