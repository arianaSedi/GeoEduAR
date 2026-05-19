package com.example.geoeduAR;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.card.MaterialCardView;
import com.google.ar.core.Anchor;
import com.google.ar.core.AugmentedImage;
import com.google.ar.core.AugmentedImageDatabase;
import com.google.ar.core.Config;
import com.google.ar.core.Frame;
import com.google.ar.core.Session;
import com.google.ar.core.TrackingState;
import com.google.ar.sceneform.AnchorNode;
import com.google.ar.sceneform.FrameTime;
import com.google.ar.sceneform.math.Quaternion;
import com.google.ar.sceneform.math.Vector3;
import com.google.ar.sceneform.rendering.ModelRenderable;
import com.google.ar.sceneform.ux.ArFragment;
import com.google.ar.sceneform.ux.TransformableNode;

import java.io.InputStream;
import java.util.Collection;

public class ARActivity extends AppCompatActivity {

    private ArFragment arFragment;
    private ModelRenderable modeloRenderable;

    private MaterialCardView cardInfoAR;
    private TextView tvNombreAR;
    private TextView tvDescripcionAR;
    private TextView tvAyudaAR;

    private String nombre;
    private String descripcion;
    private String modelo3D;
    private String contenidoAR;
    private String imagenReferencia;
    private String recursoMultimedia;
    private String tipoMultimedia;

    private float posicionX;
    private float posicionY;
    private float posicionZ;

    private boolean modeloColocado = false;
    private boolean baseImagenConfigurada = false;

    private MediaPlayer mediaPlayer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ar);

        nombre = getIntent().getStringExtra("nombre");
        descripcion = getIntent().getStringExtra("descripcion");
        modelo3D = getIntent().getStringExtra("modelo3D");
        contenidoAR = getIntent().getStringExtra("contenidoAR");

        imagenReferencia = getIntent().getStringExtra("imagenReferencia");
        recursoMultimedia = getIntent().getStringExtra("recursoMultimedia");
        tipoMultimedia = getIntent().getStringExtra("tipoMultimedia");

        posicionX = getIntent().getFloatExtra("posicionX", 0f);
        posicionY = getIntent().getFloatExtra("posicionY", 0f);
        posicionZ = getIntent().getFloatExtra("posicionZ", 0.3f);

        cardInfoAR = findViewById(R.id.cardInfoAR);
        tvNombreAR = findViewById(R.id.tvNombreAR);
        tvDescripcionAR = findViewById(R.id.tvDescripcionAR);
        tvAyudaAR = findViewById(R.id.tvAyudaAR);

        cardInfoAR.setVisibility(View.VISIBLE);
        tvNombreAR.setText(nombre != null ? nombre : "Experiencia AR");
        tvDescripcionAR.setText("Apunta la cámara a la imagen de referencia. Si no se detecta, toca una superficie plana.");
        tvAyudaAR.setText("Busca el marcador del punto educativo o toca un plano detectado.");

        arFragment = (ArFragment) getSupportFragmentManager()
                .findFragmentById(R.id.arFragment);

        cargarModelo();

        arFragment.getArSceneView().getScene().addOnUpdateListener(this::detectarImagen);

        arFragment.setOnTapArPlaneListener((hitResult, plane, motionEvent) -> {
            if (modeloColocado) {
                Toast.makeText(this, "El modelo ya fue colocado", Toast.LENGTH_SHORT).show();
                return;
            }

            if (modeloRenderable == null) {
                Toast.makeText(this, "El modelo aún está cargando", Toast.LENGTH_SHORT).show();
                return;
            }

            Anchor anchor = hitResult.createAnchor();
            colocarModelo(anchor);
            modeloColocado = true;

            mostrarInformacion("Modelo colocado sobre plano detectado.");
        });
    }

    private void cargarModelo() {
        if (modelo3D == null || modelo3D.isEmpty()) {
            Toast.makeText(this, "Este punto no tiene modelo 3D asignado", Toast.LENGTH_LONG).show();
            return;
        }

        ModelRenderable.builder()
                .setSource(this, Uri.parse(modelo3D))
                .setIsFilamentGltf(true)
                .build()
                .thenAccept(renderable -> {
                    modeloRenderable = renderable;
                    Toast.makeText(this, "Modelo listo. Escanea imagen o toca un plano.", Toast.LENGTH_SHORT).show();
                })
                .exceptionally(throwable -> {
                    Toast.makeText(this, "Error cargando modelo: " + modelo3D, Toast.LENGTH_LONG).show();
                    return null;
                });
    }

    private void detectarImagen(FrameTime frameTime) {
        if (modeloColocado || modeloRenderable == null) return;

        Session session = arFragment.getArSceneView().getSession();
        if (session == null) return;

        if (!baseImagenConfigurada) {
            configurarImagenReferencia(session);
        }

        Frame frame = arFragment.getArSceneView().getArFrame();
        if (frame == null) return;

        Collection<AugmentedImage> imagenes =
                frame.getUpdatedTrackables(AugmentedImage.class);

        for (AugmentedImage imagen : imagenes) {
            if (imagen.getTrackingState() == TrackingState.TRACKING) {
                Anchor anchor = imagen.createAnchor(imagen.getCenterPose());
                colocarModelo(anchor);

                modeloColocado = true;
                mostrarInformacion("Imagen detectada correctamente.");

                Toast.makeText(this, "Imagen de referencia detectada", Toast.LENGTH_SHORT).show();
                break;
            }
        }
    }

    private void configurarImagenReferencia(Session session) {
        if (imagenReferencia == null || imagenReferencia.isEmpty()) {
            baseImagenConfigurada = true;
            return;
        }

        try {
            Config config = session.getConfig();

            AugmentedImageDatabase database = new AugmentedImageDatabase(session);

            InputStream inputStream = getAssets().open("img/" + imagenReferencia);
            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);

            database.addImage(imagenReferencia, bitmap);

            config.setAugmentedImageDatabase(database);
            session.configure(config);

            baseImagenConfigurada = true;

        } catch (Exception e) {
            baseImagenConfigurada = true;
            Toast.makeText(this,
                    "No se pudo cargar la imagen: " + imagenReferencia,
                    Toast.LENGTH_LONG
            ).show();
        }
    }

    private void colocarModelo(Anchor anchor) {
        AnchorNode anchorNode = new AnchorNode(anchor);
        anchorNode.setParent(arFragment.getArSceneView().getScene());

        TransformableNode node = new TransformableNode(arFragment.getTransformationSystem());
        node.setParent(anchorNode);
        node.setRenderable(modeloRenderable);

        // Posición sobre el punto detectado
        node.setLocalPosition(new Vector3(posicionX, posicionY, posicionZ));

        // Escala reducida para modelos humanos
        node.setLocalScale(new Vector3(0.5f, 0.5f, 0.5f));

        // No forzar rotación. Así evitamos que el docente aparezca acostado o torcido.
        // Si luego aparece de espaldas, solo ajustamos eje Y.
        node.setLocalRotation(
                Quaternion.axisAngle(new Vector3(0f, 1f, 0f), 270f)
        );

        node.select();

        node.setOnTapListener((hitTestResult, motionEvent) -> {
            mostrarInformacion("Modelo 3D del docente.");
        });
    }

    private void mostrarInformacion(String mensajeAyuda) {
        tvNombreAR.setText(nombre != null ? nombre : "Información educativa");

        String texto = "";

        if (contenidoAR != null && !contenidoAR.isEmpty()) {
            texto += contenidoAR;
        } else if (descripcion != null && !descripcion.isEmpty()) {
            texto += descripcion;
        } else {
            texto += "No hay información disponible.";
        }

        if (recursoMultimedia != null && !recursoMultimedia.isEmpty()) {
            texto += "\n\nMultimedia: " + recursoMultimedia;
            reproducirAudioSiExiste();
        }

        tvDescripcionAR.setText(texto);
        tvAyudaAR.setText(mensajeAyuda);
        cardInfoAR.setVisibility(View.VISIBLE);
    }

    private void reproducirAudioSiExiste() {
        if (tipoMultimedia == null || !tipoMultimedia.equalsIgnoreCase("audio")) return;
        if (recursoMultimedia == null || recursoMultimedia.isEmpty()) return;

        try {
            if (mediaPlayer != null) {
                mediaPlayer.release();
            }

            InputStream afd = getAssets().open("multimedia/" + recursoMultimedia);
            mediaPlayer = new MediaPlayer();
            mediaPlayer.setDataSource(afd.toString());

        } catch (Exception e) {
            // Para no romper la experiencia AR si el audio falla.
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }
}