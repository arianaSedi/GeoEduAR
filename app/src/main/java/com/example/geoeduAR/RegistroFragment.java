package com.example.geoeduAR;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;

public class RegistroFragment extends Fragment {
    TextInputEditText etCorreo, etPassword;
    MaterialButton btnRegister;
    FirebaseAuth auth;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_registro, container, false);

        etCorreo = view.findViewById(R.id.etCorreo);
        etPassword = view.findViewById(R.id.etPassword);
        btnRegister = view.findViewById(R.id.btnRegister);

        // INICIALIZA FIREBASE AUTHENTICATION
        auth = FirebaseAuth.getInstance();

        btnRegister.setOnClickListener(v -> register(view));
        return view;
    }

    private void register(View view) {

        String correo = etCorreo.getText().toString().trim();
        String pass = etPassword.getText().toString().trim();

        if (correo.isEmpty() || pass.isEmpty()) {
            Toast.makeText(getContext(), "Completa los campos", Toast.LENGTH_SHORT).show();
            return;
        }

        // CREA UN NUEVO USUARIO EN FIREBASE CON CORREO Y CONTRASENA
        auth.createUserWithEmailAndPassword(correo, pass)
                .addOnCompleteListener(task -> {

                    // SI EL REGISTRO ES CORRECTO, REGRESA AL LOGIN
                    if (task.isSuccessful()) {

                        Toast.makeText(getContext(), "Usuario creado", Toast.LENGTH_SHORT).show();

                        Navigation.findNavController(view).navigate(R.id.loginFragment);

                    } else {

                        // SI OCURRE ERROR, MUESTRA EL MENSAJE DEVUELTO POR FIREBASE
                        Toast.makeText(getContext(), task.getException().getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }
}