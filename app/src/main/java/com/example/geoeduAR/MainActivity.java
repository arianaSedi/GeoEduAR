package com.example.geoeduAR;

import android.os.Bundle;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {
    BottomNavigationView bottomNavigation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        bottomNavigation = findViewById(R.id.bottomNavigation);

        // OBTIENE EL NAVHOSTFRAGMENT QUE CONTIENE LOS FRAGMENTS DE LA APLICACION
        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.fragment_container);

        // SI NO SE ENCUENTRA SE DETIENE LA EJECUCION
        if (navHostFragment == null) return;

        // OBTIENE EL CONTROLADOR DE NAV
        NavController navController = navHostFragment.getNavController();

        // CONECTA EL BOTTOM NAVIGATION CON EL NAVCONTROLLER
        NavigationUI.setupWithNavController(bottomNavigation, navController);

        // SOLO DECIDE LA PANTALLA INICIAL CUANDO LA ACTIVITY SE CREA POR PRIMERA VEZ
        if (savedInstanceState == null) {

            // OBTIENE EL USUARIO
            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

            // SI YA HAY USUARIO LOGUEADO, ENTRA AL HOME
            if (user != null) {
                navController.navigate(R.id.homeFragment);
            } else {
                // SI NO HAY USUARIO, ENVIA AL LOGIN
                navController.navigate(R.id.loginFragment);
            }
        }

        navController.addOnDestinationChangedListener((controller, destination, arguments) -> {

            // OCULTA EN LOGIN Y REGISTRO
            if (destination.getId() == R.id.loginFragment || destination.getId() == R.id.registroFragment) {
                bottomNavigation.setVisibility(View.GONE);
            } else {
                // MUESTRA EN LAS DEMAS PANTALLAS
                bottomNavigation.setVisibility(View.VISIBLE);
            }
        });
    }
}