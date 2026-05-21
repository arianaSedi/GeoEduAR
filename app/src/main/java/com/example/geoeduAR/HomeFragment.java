package com.example.geoeduAR;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class HomeFragment extends Fragment {

    TextView tvUser;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_home, container, false);

        tvUser = view.findViewById(R.id.tvUser);

        // PROTEGER PANTALLA
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (user == null) {
            Navigation.findNavController(view).navigate(R.id.loginFragment);
            return view;
        }

        String nombre = user.getDisplayName();

        if (nombre == null || nombre.isEmpty()) {
            String correo = user.getEmail();
            nombre = correo.split("@")[0];
        }

        tvUser.setText("Bienvenido " + nombre);
        return view;
    }
}