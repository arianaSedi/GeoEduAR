package com.example.geoeduAR;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class PerfilFragment extends Fragment {

    TextView tvProfileEmail;
    MaterialButton btnLogout;
    FirebaseAuth auth;

    public PerfilFragment() {
    }

    public static PerfilFragment newInstance(String param1, String param2) {
        PerfilFragment fragment = new PerfilFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_perfil, container, false);

        tvProfileEmail = view.findViewById(R.id.tvProfileEmail);
        btnLogout = view.findViewById(R.id.btnLogout);

        auth = FirebaseAuth.getInstance();

        // OBTIENE EL USUARIO QUE TIENE SESION INICIADA
        FirebaseUser user = auth.getCurrentUser();

        // SI NO HAY USUARIO ACTIVO, SE REDIRIGE AL LOGIN
        if (user == null) {
            Navigation.findNavController(view).navigate(R.id.loginFragment);
            return view;
        }

        // MUESTRA EL CORREO DEL USUARIO EN LA PANTALLA
        tvProfileEmail.setText(user.getEmail());

        btnLogout.setOnClickListener(v -> {

            // MUESTRA UN DIALOGO DE CONFIRMACION ANTES DE CERRAR SESION
            new MaterialAlertDialogBuilder(requireContext(), R.style.Dialog)
                    .setTitle("Cerrar sesión")
                    .setMessage("¿Estás seguro de que deseas cerrar sesión?")
                    .setPositiveButton("Sí", (dialog, which) -> {

                        // CIERRA LA SESION ACTUAL EN FIREBASE
                        auth.signOut();

                        Toast.makeText(getContext(), "Sesión cerrada", Toast.LENGTH_SHORT).show();

                        // REGRESA AL LOGIN DESPUES DE CERRAR SESION
                        Navigation.findNavController(view).navigate(R.id.loginFragment);

                    })
                    .setNegativeButton("Cancelar", null)
                    .show();

        });
        return view;
    }
}