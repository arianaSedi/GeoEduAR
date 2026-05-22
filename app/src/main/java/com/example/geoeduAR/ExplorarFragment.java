package com.example.geoeduAR;

import android.Manifest;
import android.content.pm.PackageManager;
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
    private EditText etBuscarExplorar;
    private ProgressBar progressBarExplorar;
    private ArrayList<PuntoEducativo> listaOriginal;
    private FusedLocationProviderClient fusedLocationClient;

    // LANZADOR PARA PEDIR PERMISO DE UBICACION AL USUARIO
    private final ActivityResultLauncher<String> permisoUbicacion =
            registerForActivityResult(
                    new ActivityResultContracts.RequestPermission(),
                    isGranted -> {
                        // SI EL PERMISO FUE ACEPTADO, SE OBTIENE LA UBICACION
                        if (isGranted) {
                            obtenerUbicacionUsuario();
                        } else {
                            // SI EL PERMISO FUE DENEGADO, SE AVISA QUE NO SE PODRA USAR AR POR CERCANIA
                            tvEstadoExplorar.setText("Permiso de ubicación denegado");
                            Toast.makeText(requireContext(), "Activa el GPS para habilitar AR por cercanía", Toast.LENGTH_SHORT).show();
                        }
                    }
            );

    public ExplorarFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_explorar, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        recyclerExplorar = view.findViewById(R.id.recyclerExplorar);
        tvEstadoExplorar = view.findViewById(R.id.tvEstadoExplorar);
        etBuscarExplorar = view.findViewById(R.id.etBuscarExplorar);
        progressBarExplorar = view.findViewById(R.id.progressBarExplorar);

        lista = new ArrayList<>();
        listaOriginal = new ArrayList<>();
        adapter = new PuntoAdapter(lista);

        // CONFIGURA EL RECYCLERVIEW PARA MOSTRAR LOS PUNTOS EDUCATIVOS
        recyclerExplorar.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerExplorar.setAdapter(adapter);

        // INICIALIZA EL CLIENTE PARA OBTENER LA UBICACION DEL USUARIO
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity());

        cargarPuntosEducativos();
        verificarPermisoUbicacion();

        etBuscarExplorar.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filtrar(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    private void filtrar(String texto) {
        // EVITA ERRORES SI LA LISTA ORIGINAL AUN NO ESTA CARGADA
        if (listaOriginal == null) return;

        lista.clear();

        // CONVIERTE LA BUSQUEDA A MINUSCULAS PARA COMPARAR SIN IMPORTAR MAYUSCULAS
        String busqueda = texto.toLowerCase();

        // AGREGA A LA LISTA SOLO LOS PUNTOS QUE COINCIDEN CON EL TEXTO BUSCADO
        for (PuntoEducativo p : listaOriginal) {
            if (p.getNombre() != null && p.getNombre().toLowerCase().contains(busqueda)) {
                lista.add(p);
            }
        }
        adapter.notifyDataSetChanged();
    }

    private void verificarPermisoUbicacion() {
        // VERIFICA SI EL PERMISO DE UBICACION YA FUE CONCEDIDO
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

            // SI YA HAY PERMISO, OBTIENE LA UBI DEL USUARIO
            obtenerUbicacionUsuario();

        } else {
            // SINO LO SOLICITA AL USUARIO
            permisoUbicacion.launch(Manifest.permission.ACCESS_FINE_LOCATION);
        }
    }

    private void obtenerUbicacionUsuario() {
        // VALIDA OTRA VEZ EL PERMISO ANTES DE ACCEDER A LA UBI
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION
        ) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        // OBTIENE LA ULTIMA UBICACION CONOCIDA DEL DISPOSITIVO
        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(location -> {
                    if (location != null) {
                        double latUsuario = location.getLatitude();
                        double lngUsuario = location.getLongitude();

                        // ENVIA LA UBICACION AL ADAPTADOR PARA CALCULAR DISTANCIAS Y HABILITAR AR
                        adapter.actualizarUbicacion(latUsuario, lngUsuario);

                        tvEstadoExplorar.setText("Ubicación detectada. Selecciona un punto cercano.");
                    } else {
                        // MENSAJE SI NO SE PUDO OBTENER UBICACION
                        tvEstadoExplorar.setText("No se pudo obtener la ubicación. Activa GPS.");
                    }
                })
                .addOnFailureListener(e -> {
                    tvEstadoExplorar.setText("Error al obtener ubicación");
                    Toast.makeText(requireContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void cargarPuntosEducativos() {
        // LEE LOS PUNTOS EDUCATIVOS DESDE REALTIME DATABASE
        FirebaseDatabase.getInstance()
                .getReference("puntos_educativos")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        listaOriginal.clear();

                        // RECORRE CADA PUNTO EDUCATIVO GUARDADO EN FIREBASE
                        for (DataSnapshot item : snapshot.getChildren()) {
                            PuntoEducativo punto = item.getValue(PuntoEducativo.class);

                            // AGREGA EL PUNTO A LA LISTA ORIGINAL SI EXISTE
                            if (punto != null) {
                                listaOriginal.add(punto);
                            }

                            // APLICA EL FILTRO ACTUAL DESPUES DE CARGAR DATOS
                            filtrar(etBuscarExplorar.getText().toString());
                        }

                        obtenerUbicacionUsuario();
                        String textoActual = etBuscarExplorar.getText().toString();
                        filtrar(textoActual);

                        // OCULTA EL DE CARGAND
                        if (progressBarExplorar != null) {
                            progressBarExplorar.setVisibility(View.GONE);
                        }

                        // MUESTRA EL ESTADO SEGUN LA CANTIDAD DE PUNTOS ENCONTRADOS
                        if (listaOriginal.isEmpty()) {
                            tvEstadoExplorar.setText("No hay puntos registrados");
                        } else {
                            tvEstadoExplorar.setText(listaOriginal.size() + " puntos registrados");
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        //FIREBASE DEVUELVE UN ERROR
                        tvEstadoExplorar.setText("Error al cargar puntos");
                        Toast.makeText(requireContext(), error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
    }
}