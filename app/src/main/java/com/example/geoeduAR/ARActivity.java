package com.example.geoeduAR;

import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
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
import com.google.firebase.database.FirebaseDatabase;

import java.io.InputStream;
import java.util.Collection;

public class ARActivity extends AppCompatActivity {

    private ArFragment arFragment;
    private ModelRenderable modeloRenderable;

    private MaterialCardView cardInfoAR;
    private TextView tvNombreAR;
    private TextView tvDescripcionAR;
    private TextView tvAyudaAR;
    private TextView tvMensajeEscaneo;
    private MaterialButton btnRegresarAR;

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

    private String docenteId;
    private String nombreDocente;

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

        docenteId = getIntent().getStringExtra("docenteId");
        nombreDocente = getIntent().getStringExtra("nombreDocente");

        recursoMultimedia = getIntent().getStringExtra("recursoMultimedia");
        tipoMultimedia = getIntent().getStringExtra("tipoMultimedia");

        posicionX = getIntent().getFloatExtra("posicionX", 0f);
        posicionY = getIntent().getFloatExtra("posicionY", 0f);
        posicionZ = getIntent().getFloatExtra("posicionZ", 0.3f);

        if (posicionZ == 0f) {
            posicionZ = 0.3f;
        }

        cardInfoAR = findViewById(R.id.cardInfoAR);
        tvNombreAR = findViewById(R.id.tvNombreAR);
        tvDescripcionAR = findViewById(R.id.tvDescripcionAR);
        tvAyudaAR = findViewById(R.id.tvAyudaAR);
        tvMensajeEscaneo = findViewById(R.id.tvMensajeEscaneo);
        btnRegresarAR = findViewById(R.id.btnRegresarAR);

        cardInfoAR.setVisibility(View.GONE);
        tvMensajeEscaneo.setVisibility(View.VISIBLE);

        if (tieneImagenReferencia()) {
            tvMensajeEscaneo.setText("Escanea la imagen relacionada con este docente");
        } else {
            tvMensajeEscaneo.setText("Estás dentro del rango. Toca un plano para ver al docente");
        }

        btnRegresarAR.setOnClickListener(v -> finish());

        arFragment = (ArFragment) getSupportFragmentManager().findFragmentById(R.id.arFragment);

        cargarModelo();

        arFragment.getArSceneView().getScene().addOnUpdateListener(this::detectarImagen);

        arFragment.setOnTapArPlaneListener((hitResult, plane, motionEvent) -> {

            if (modeloColocado) {
                Toast.makeText(this, "El modelo ya fue colocado", Toast.LENGTH_SHORT).show();
                return;
            }

            if (tieneImagenReferencia()) {
                Toast.makeText(this, "Este punto se muestra escaneando su imagen", Toast.LENGTH_SHORT).show();
                return;
            }

            if (modeloRenderable == null) {
                Toast.makeText(this, "El modelo aún no está listo o no cargó", Toast.LENGTH_SHORT).show();
                return;
            }

            Anchor anchor = hitResult.createAnchor();

            colocarModeloEnPlano(anchor);
            modeloColocado = true;

            mostrarInformacion("Modelo mostrado por rango de ubicación.");
        });
    }

    private boolean tieneImagenReferencia() {
        return imagenReferencia != null && !imagenReferencia.trim().isEmpty();
    }

    private void cargarModelo() {

        if (modelo3D == null || modelo3D.trim().isEmpty()) {
            modeloRenderable = null;
            Toast.makeText(this, "Este punto no tiene modelo 3D asignado", Toast.LENGTH_LONG).show();
            return;
        }

        String rutaModelo = modelo3D.trim();

        if (!rutaModelo.startsWith("http") && !rutaModelo.startsWith("file:///android_asset/")) {
            rutaModelo = "file:///android_asset/" + rutaModelo;
        }

        ModelRenderable.builder()
                .setSource(this, Uri.parse(rutaModelo))
                .setIsFilamentGltf(true)
                .build()
                .thenAccept(renderable -> {
                    modeloRenderable = renderable;

                    if (tieneImagenReferencia()) {
                        tvMensajeEscaneo.setText("Modelo listo. Escanea la imagen.");
                    } else {
                        tvMensajeEscaneo.setText("Modelo listo. Toca un plano.");
                    }

                    Toast.makeText(this, "Modelo cargado: " + modelo3D, Toast.LENGTH_LONG).show();
                })
                .exceptionally(throwable -> {
                    modeloRenderable = null;
                    Toast.makeText(this, "Error cargando modelo: " + modelo3D, Toast.LENGTH_LONG).show();
                    return null;
                });
    }

    private void detectarImagen(FrameTime frameTime) {

        if (modeloColocado) return;

        if (!tieneImagenReferencia()) {
            return;
        }

        Session session = arFragment.getArSceneView().getSession();
        if (session == null) return;

        if (!baseImagenConfigurada) {
            configurarImagenReferencia(session);
        }

        Frame frame = arFragment.getArSceneView().getArFrame();
        if (frame == null) return;

        Collection<AugmentedImage> imagenes = frame.getUpdatedTrackables(AugmentedImage.class);

        for (AugmentedImage imagen : imagenes) {

            if (imagen.getTrackingState() == TrackingState.TRACKING) {

                Toast.makeText(this, "ARCore detectó: " + imagen.getName(), Toast.LENGTH_SHORT).show();

                if (modeloRenderable == null) {
                    Toast.makeText(this, "Imagen detectada, pero el modelo no está listo", Toast.LENGTH_LONG).show();
                    return;
                }

                /*
                 * La imagen SOLO activa el modelo.
                 * Ya NO usamos imagen.createAnchor(...), porque eso lo pone encima de la imagen.
                 * Ahora creamos un anchor frente a la cámara.
                 */
                Anchor anchorFrenteCamara = crearAnchorFrenteCamara();

                colocarModeloFrenteCamara(anchorFrenteCamara);

                modeloColocado = true;

                mostrarInformacion("Imagen detectada correctamente. Modelo 3D mostrado frente a la cámara.");

                Toast.makeText(this, "Modelo activado por imagen", Toast.LENGTH_SHORT).show();

                break;
            }
        }
    }

    private Anchor crearAnchorFrenteCamara() {

        Frame frame = arFragment.getArSceneView().getArFrame();
        Session session = arFragment.getArSceneView().getSession();

        if (frame == null || session == null) {
            return null;
        }

        /*
         * Posición inicial del modelo frente al teléfono:
         *
         * X = izquierda/derecha
         * Y = arriba/abajo
         * Z = distancia frente a la cámara
         *
         * -1.0f significa 1 metro enfrente de la cámara.
         */
        com.google.ar.core.Pose poseCamara = frame.getCamera().getPose();

        com.google.ar.core.Pose poseFrenteCamara = poseCamara.compose(
                com.google.ar.core.Pose.makeTranslation(0f, -0.1f, -1.0f)
        );

        return session.createAnchor(poseFrenteCamara);
    }

    private void configurarImagenReferencia(Session session) {

        if (!tieneImagenReferencia()) {
            baseImagenConfigurada = true;
            tvMensajeEscaneo.setText("Toca un plano para ver el modelo.");
            return;
        }

        String nombreImagen = imagenReferencia.trim();

        try {

            String[] archivos = getAssets().list("img");
            boolean existe = false;

            if (archivos != null) {
                for (String archivo : archivos) {
                    if (archivo.equals(nombreImagen)) {
                        existe = true;
                        break;
                    }
                }
            }

            if (!existe) {
                baseImagenConfigurada = true;
                imagenReferencia = "";

                tvMensajeEscaneo.setText("No se encontró la imagen en assets/img. Toca un plano.");

                Toast.makeText(
                        this,
                        "No existe en assets/img: " + nombreImagen,
                        Toast.LENGTH_LONG
                ).show();

                return;
            }

            InputStream inputStream = getAssets().open("img/" + nombreImagen);
            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
            inputStream.close();

            if (bitmap == null) {
                baseImagenConfigurada = true;
                imagenReferencia = "";

                tvMensajeEscaneo.setText("La imagen existe, pero no se pudo leer.");

                Toast.makeText(
                        this,
                        "Imagen inválida o dañada: " + nombreImagen,
                        Toast.LENGTH_LONG
                ).show();

                return;
            }

            Config config = session.getConfig();
            AugmentedImageDatabase database = new AugmentedImageDatabase(session);

            try {
                database.addImage(nombreImagen, bitmap, 0.20f);
            } catch (Exception e) {
                baseImagenConfigurada = true;
                imagenReferencia = "";

                tvMensajeEscaneo.setText("La imagen no es buena para ARCore. Toca un plano.");

                Toast.makeText(
                        this,
                        "ARCore rechazó la imagen: " + nombreImagen,
                        Toast.LENGTH_LONG
                ).show();

                return;
            }

            config.setAugmentedImageDatabase(database);
            session.configure(config);

            baseImagenConfigurada = true;

            Toast.makeText(
                    this,
                    "Imagen lista para escanear: " + nombreImagen,
                    Toast.LENGTH_LONG
            ).show();

        } catch (Exception e) {

            baseImagenConfigurada = true;
            imagenReferencia = "";

            tvMensajeEscaneo.setText("Error configurando imagen. Toca un plano.");

            Toast.makeText(
                    this,
                    "Error real: " + e.getMessage(),
                    Toast.LENGTH_LONG
            ).show();
        }
    }

    private void colocarModeloFrenteCamara(Anchor anchor) {

        if (anchor == null) {
            Toast.makeText(this, "No se pudo crear el anchor frente a la cámara", Toast.LENGTH_SHORT).show();
            return;
        }

        if (modeloRenderable == null) {
            Toast.makeText(this, "No hay modelo 3D disponible", Toast.LENGTH_SHORT).show();
            return;
        }

        AnchorNode anchorNode = new AnchorNode(anchor);
        anchorNode.setParent(arFragment.getArSceneView().getScene());

        TransformableNode node = new TransformableNode(arFragment.getTransformationSystem());
        node.setParent(anchorNode);
        node.setRenderable(modeloRenderable);

        node.setLocalPosition(new Vector3(0f, 0f, 0f));
        node.setLocalScale(new Vector3(0.3f, 0.3f, 0.3f));

        /*
         * X corrige que el modelo esté acostado.
         * Y corrige hacia dónde mira el frente del modelo.
         */
        Quaternion levantarModelo = Quaternion.axisAngle(new Vector3(0f, 0f, 1f), 90f);
        Quaternion girarFrente = Quaternion.axisAngle(new Vector3(0f, 1f, 0f), 270f);

        node.setLocalRotation(
                Quaternion.multiply(levantarModelo, girarFrente)
        );

        node.getTranslationController().setEnabled(true);
        node.getScaleController().setEnabled(true);
        node.getRotationController().setEnabled(true);

        node.select();

        Toast.makeText(this, "Modelo colocado vertical frente a la cámara", Toast.LENGTH_LONG).show();

        node.setOnTapListener((hitTestResult, motionEvent) -> {
            node.select();
            mostrarInformacion("Modelo 3D del docente.");
        });
    }

    private void colocarModeloEnPlano(Anchor anchor) {

        if (modeloRenderable == null) {
            Toast.makeText(this, "No hay modelo 3D disponible", Toast.LENGTH_SHORT).show();
            return;
        }

        AnchorNode anchorNode = new AnchorNode(anchor);
        anchorNode.setParent(arFragment.getArSceneView().getScene());

        TransformableNode node = new TransformableNode(arFragment.getTransformationSystem());

        node.setParent(anchorNode);
        node.setRenderable(modeloRenderable);

        node.setLocalPosition(new Vector3(posicionX, posicionY, posicionZ));

        node.setLocalScale(new Vector3(0.5f, 0.5f, 0.5f));

        node.setLocalRotation(
                Quaternion.axisAngle(new Vector3(0f, 1f, 0f), 190f)
        );

        node.getTranslationController().setEnabled(true);
        node.getScaleController().setEnabled(true);
        node.getRotationController().setEnabled(true);

        node.select();

        node.setOnTapListener((hitTestResult, motionEvent) -> {
            node.select();
            mostrarInformacion("Modelo 3D del docente.");
        });
    }

    private void mostrarInformacion(String mensajeAyuda) {

        tvMensajeEscaneo.setVisibility(View.GONE);
        cardInfoAR.setVisibility(View.VISIBLE);

        tvNombreAR.setText(
                nombre != null && !nombre.trim().isEmpty()
                        ? nombre
                        : "Información educativa"
        );

        String texto = "";

        if (contenidoAR != null && !contenidoAR.trim().isEmpty()) {
            texto = contenidoAR;
        } else if (descripcion != null && !descripcion.trim().isEmpty()) {
            texto = descripcion;
        } else {
            texto = "No hay información disponible.";
        }

        if (recursoMultimedia != null && !recursoMultimedia.trim().isEmpty()) {

            if (tipoMultimedia != null && tipoMultimedia.equalsIgnoreCase("audio")) {
                texto += "\n\nAudio disponible.";
                reproducirAudioSiExiste();
            } else {
                texto += "\n\nRecurso multimedia disponible.";
            }
        }

        if (docenteId != null && !docenteId.trim().isEmpty()) {
            FirebaseDatabase.getInstance()
                    .getReference("docentes_encontrados")
                    .child(docenteId)
                    .setValue(true);
        }

        tvDescripcionAR.setText(texto);
        tvAyudaAR.setText(mensajeAyuda);

        cardInfoAR.setVisibility(View.VISIBLE);
    }

    private void reproducirAudioSiExiste() {

        if (tipoMultimedia == null || !tipoMultimedia.equalsIgnoreCase("audio")) return;

        if (recursoMultimedia == null || recursoMultimedia.trim().isEmpty()) return;

        try {

            if (mediaPlayer != null) {
                mediaPlayer.release();
                mediaPlayer = null;
            }

            AssetFileDescriptor afd = getAssets().openFd("multimedia/" + recursoMultimedia.trim());

            mediaPlayer = new MediaPlayer();
            mediaPlayer.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());

            afd.close();

            mediaPlayer.prepare();
            mediaPlayer.start();

        } catch (Exception e) {
            Toast.makeText(this, "No se pudo reproducir el audio educativo", Toast.LENGTH_SHORT).show();
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