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
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;

public class LoginFragment extends Fragment {
    TextInputEditText etCorreo, etPassword;
    MaterialButton btnLogin;
    TextView tvRegistro;
    TextView tvRecuperar;

    // INSTANCIA DE FIREBASE AUTHENTICATION
    FirebaseAuth auth;

    public LoginFragment() {
    }

    public static LoginFragment newInstance(String param1, String param2) {
        LoginFragment fragment = new LoginFragment();
        Bundle args = new Bundle();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_login, container, false);

        etCorreo = view.findViewById(R.id.etCorreo);
        etPassword = view.findViewById(R.id.etPassword);
        tvRegistro = view.findViewById(R.id.tvRegistro);
        btnLogin = view.findViewById(R.id.btnLogin);
        tvRecuperar = view.findViewById(R.id.tvRecuperar);

        // INICIALIZA FIREBASE AUTHENTICATION
        auth = FirebaseAuth.getInstance();

        btnLogin.setOnClickListener(v -> login(view));

        // NAVEGA HACIA LA PANTALLA DE REGISTRO
        tvRegistro.setOnClickListener(v ->
                Navigation.findNavController(view).navigate(R.id.registroFragment));

        // ENVIA CORREO DE RECUPERACION DE CONTRASENA
        tvRecuperar.setOnClickListener(v -> recuperarPassword());
        return view;

    }

    private void login(View view) {

        String correo = etCorreo.getText().toString().trim();
        String pass = etPassword.getText().toString().trim();

        if (correo.isEmpty() || pass.isEmpty()) {
            Toast.makeText(getContext(), "Completa los campos", Toast.LENGTH_SHORT).show();
            return;
        }

        // INICIA SESION CON CORREO Y CONTRASENA USANDO FIREBASE
        auth.signInWithEmailAndPassword(correo, pass)
                .addOnCompleteListener(task -> {

                    // SI EL LOGIN ES CORRECTO, NAVEGA AL HOME
                    if (task.isSuccessful()) {

                        Toast.makeText(getContext(), "Bienvenido", Toast.LENGTH_SHORT).show();

                        Navigation.findNavController(view).navigate(R.id.homeFragment);

                    } else {

                        // SI FALLA, MUESTRA EL ERROR DEVUELTO POR FIREBASE
                        Toast.makeText(getContext(), task.getException().getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void recuperarPassword() {

        // OBTIENE EL CORREO ESCRITO POR EL USUARIO
        String correo = etCorreo.getText().toString().trim();

        if (correo.isEmpty()) {
            Toast.makeText(getContext(), "Ingresa tu correo primero", Toast.LENGTH_SHORT).show();
            return;
        }

        // ENVIA UN CORREO PARA RESTABLECER LA CONTRASENA
        auth.sendPasswordResetEmail(correo)
                .addOnCompleteListener(task -> {

                    // SI EL CORREO SE ENVIA, MUESTRA CONFIRMACION
                    if (task.isSuccessful()) {
                        Toast.makeText(getContext(), "Correo enviado correctamente", Toast.LENGTH_LONG).show();

                    } else {

                        // SI OCURRE ERROR, MUESTRA EL MENSAJE DEVUELTO POR FIREBASE
                        String error = task.getException() != null
                                ? task.getException().getMessage()
                                : "Error desconocido";

                        Toast.makeText(getContext(), error, Toast.LENGTH_LONG).show();
                    }
                });
    }

}