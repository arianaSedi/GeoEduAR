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

    // FRAGMENTO PRINCIPAL DONDE SE MUESTRA LA CAMARA Y LA ESCENA AR
    private ArFragment arFragment;

    // MODELO 3D QUE SE CARGARA EN LA EXPERIENCIA DE REALIDAD AUMENTADA
    private ModelRenderable modeloRenderable;

    // ELEMENTOS VISUALES DE LA CARD DE INFORMACION AR
    private MaterialCardView cardInfoAR;
    private TextView tvNombreAR;
    private TextView tvDescripcionAR;
    private TextView tvAyudaAR;
    private TextView tvMensajeEscaneo;
    private MaterialButton btnRegresarAR;

    // DATOS RECIBIDOS DESDE EL PUNTO EDUCATIVO
    private String nombre;
    private String descripcion;
    private String modelo3D;
    private String contenidoAR;
    private String imagenReferencia;
    private String recursoMultimedia;
    private String tipoMultimedia;

    // COORDENADAS 3D PARA POSICIONAR EL MODELO EN EL ESPACIO AR
    private float posicionX;
    private float posicionY;
    private float posicionZ;

    // DATOS DEL DOCENTE ASOCIADO AL PUNTO
    private String docenteId;
    private String nombreDocente;

    // VARIABLES DE CONTROL PARA DETECCION DE IMAGEN Y COLOCACION DEL MODELO
    private boolean usarImagenReferencia;
    private boolean modeloColocado = false;
    private boolean baseImagenConfigurada = false;
    private MediaPlayer mediaPlayer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ar);

        // RECIBE LOS DATOS ENVIADOS DESDE EL ADAPTADOR DEL PUNTO EDUCATIVO
        nombre = getIntent().getStringExtra("nombre");
        descripcion = getIntent().getStringExtra("descripcion");
        modelo3D = getIntent().getStringExtra("modelo3D");
        contenidoAR = getIntent().getStringExtra("contenidoAR");

        imagenReferencia = getIntent().getStringExtra("imagenReferencia");
        usarImagenReferencia = getIntent().getBooleanExtra("usarImagenReferencia", false);

        docenteId = getIntent().getStringExtra("docenteId");
        nombreDocente = getIntent().getStringExtra("nombreDocente");

        recursoMultimedia = getIntent().getStringExtra("recursoMultimedia");
        tipoMultimedia = getIntent().getStringExtra("tipoMultimedia");

        posicionX = getIntent().getFloatExtra("posicionX", 0f);
        posicionY = getIntent().getFloatExtra("posicionY", 0f);
        posicionZ = getIntent().getFloatExtra("posicionZ", 0.3f);

        cardInfoAR = findViewById(R.id.cardInfoAR);
        tvNombreAR = findViewById(R.id.tvNombreAR);
        tvDescripcionAR = findViewById(R.id.tvDescripcionAR);
        tvAyudaAR = findViewById(R.id.tvAyudaAR);
        tvMensajeEscaneo = findViewById(R.id.tvMensajeEscaneo);
        btnRegresarAR = findViewById(R.id.btnRegresarAR);

        // OCULTA LA CARD AL INICIO Y MUESTRA EL MENSAJE DE GUIA
        cardInfoAR.setVisibility(View.GONE);
        tvMensajeEscaneo.setVisibility(View.VISIBLE);

        // MUESTRA UN MENSAJE DIFERENTE SEGUN SI SE USA IMAGEN DE REFERENCIA O PLANO
        if (usarImagenReferencia && imagenReferencia != null && !imagenReferencia.isEmpty()) {
            tvMensajeEscaneo.setText("Escanea la imagen relacionada con este docente");
        } else {
            tvMensajeEscaneo.setText("Estás dentro del rango. Toca un plano para ver al docente");
        }

        // BOTON PARA REGRESAR A LA PANTALLA ANTERIOR
        btnRegresarAR.setOnClickListener(v -> finish());

        // OBTIENE EL FRAGMENTO DE AR DEFINIDO EN EL XML
        arFragment = (ArFragment) getSupportFragmentManager().findFragmentById(R.id.arFragment);

        cargarModelo();

        // REVISA CONSTANTEMENTE SI LA CAMARA DETECTA UNA IMAGEN REGISTRADA
        arFragment.getArSceneView().getScene().addOnUpdateListener(this::detectarImagen);

        // PERMITE COLOCAR EL MODELO TOCANDO UN PLANO DETECTADO
        arFragment.setOnTapArPlaneListener((hitResult, plane, motionEvent) -> {

            if (modeloColocado) {
                Toast.makeText(this, "El modelo ya fue colocado", Toast.LENGTH_SHORT).show();
                return;
            }

            if (modeloRenderable == null) {
                Toast.makeText(this, "El modelo aún está cargando", Toast.LENGTH_SHORT).show();
                return;
            }

            // SI EL PUNTO USA IMAGEN, NO PERMITE COLOCARLO TOCANDO EL PLANO
            if (usarImagenReferencia && imagenReferencia != null && !imagenReferencia.isEmpty()) {
                Toast.makeText(this, "Este docente se muestra escaneando su imagen", Toast.LENGTH_SHORT).show();
                return;
            }

            // CREA UN ANCHOR EN EL LUGAR TOCADO POR EL USUARIO
            Anchor anchor = hitResult.createAnchor();

            // COLOCA EL MODELO 3D SOBRE EL ANCHOR
            colocarModelo(anchor);
            modeloColocado = true;

            mostrarInformacion("Docente mostrado por rango de ubicación.");
        });
    }

    private void cargarModelo() {

        // VALIDA QUE EXISTA UN MODELO 3D ASIGNADO
        if (modelo3D == null || modelo3D.isEmpty()) {
            Toast.makeText(this, "Este punto no tiene modelo 3D asignado", Toast.LENGTH_LONG).show();
            return;
        }

        // CARGA EL MODELO 3D DESDE LA RUTA RECIBIDA
        ModelRenderable.builder()
                .setSource(this, Uri.parse(modelo3D))
                .setIsFilamentGltf(true)
                .build()
                .thenAccept(renderable -> {
                    // GUARDA EL MODELO YA CARGADO PARA USARLO EN LA ESCENA AR
                    modeloRenderable = renderable;

                    Toast.makeText(this, "Modelo listo. Escanea imagen o toca un plano.", Toast.LENGTH_SHORT).show();
                })
                .exceptionally(throwable -> {
                    Toast.makeText(this, "Error cargando modelo: " + modelo3D, Toast.LENGTH_LONG).show();
                    return null;
                });
    }

    private void detectarImagen(FrameTime frameTime) {

        // NO DETECTA IMAGENES SI YA SE COLOCO EL MODELO O SI EL MODELO NO ESTA LISTO
        if (modeloColocado || modeloRenderable == null) return;

        // SI NO HAY IMAGEN DE REFERENCIA, NO SE USA DETECCION POR IMAGEN
        if (!usarImagenReferencia || imagenReferencia == null || imagenReferencia.isEmpty()) {
            return;
        }

        // OBTIENE LA SESION ACTUAL DE ARCORE
        Session session = arFragment.getArSceneView().getSession();
        if (session == null) return;

        // CONFIGURA UNA SOLA VEZ LA BASE DE IMAGENES DE REFERENCIA
        if (!baseImagenConfigurada) {
            configurarImagenReferencia(session);
        }

        // OBTIENE EL FRAME ACTUAL DE LA CAMARA
        Frame frame = arFragment.getArSceneView().getArFrame();
        if (frame == null) return;

        // OBTIENE LAS IMAGENES DETECTADAS POR ARCORE EN EL FRAME ACTUAL
        Collection<AugmentedImage> imagenes = frame.getUpdatedTrackables(AugmentedImage.class);

        for (AugmentedImage imagen : imagenes) {

            // SI LA IMAGEN ESTA SIENDO RASTREADA, SE COLOCA EL MODELO SOBRE ELLA
            if (imagen.getTrackingState() == TrackingState.TRACKING) {

                Anchor anchor = imagen.createAnchor(imagen.getCenterPose());
                colocarModelo(anchor);

                modeloColocado = true;

                mostrarInformacion("Imagen del docente detectada correctamente.");

                Toast.makeText(this, "Docente detectado por imagen", Toast.LENGTH_SHORT).show();
                break;
            }
        }
    }

    private void configurarImagenReferencia(Session session) {

        // SI NO HAY IMAGEN VALIDA, SE DESACTIVA LA DETECCION POR IMAGEN
        if (!usarImagenReferencia || imagenReferencia == null || imagenReferencia.trim().isEmpty()) {
            baseImagenConfigurada = true;
            usarImagenReferencia = false;
            return;
        }

        try {

            // OBTIENE LA CONFIGURACION ACTUAL DE ARCORE
            Config config = session.getConfig();

            // CREA LA BASE DE DATOS DE IMAGENES AUMENTADAS
            AugmentedImageDatabase database = new AugmentedImageDatabase(session);

            // LEE LA IMAGEN DE REFERENCIA DESDE LA CARPETA ASSETS/IMG
            InputStream inputStream = getAssets().open("img/" + imagenReferencia.trim());
            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);

            // SI LA IMAGEN NO SE PUEDE LEER, SE DESACTIVA DETECCION POR IMAGEN
            if (bitmap == null) {

                baseImagenConfigurada = true;
                usarImagenReferencia = false;

                tvMensajeEscaneo.setText("No se pudo leer la imagen. Toca un plano para ver al docente.");

                Toast.makeText(this, "Imagen inválida: " + imagenReferencia, Toast.LENGTH_LONG).show();
                return;
            }

            // AGREGA LA IMAGEN A LA BASE DE DATOS DE ARCORE
            database.addImage(imagenReferencia.trim(), bitmap);

            // ASIGNA LA BASE DE IMAGENES A LA CONFIGURACION DE LA SESION
            config.setAugmentedImageDatabase(database);
            session.configure(config);

            baseImagenConfigurada = true;

            Toast.makeText(this, "Imagen lista para escanear: " + imagenReferencia, Toast.LENGTH_SHORT).show();

        } catch (Exception e) {

            // SI FALLA LA CARGA DE LA IMAGEN, SE PERMITE USAR PLANO COMO RESPALDO
            baseImagenConfigurada = true;
            usarImagenReferencia = false;

            tvMensajeEscaneo.setText("No se encontró la imagen. Toca un plano para ver al docente."
            );

            Toast.makeText(this, "No se pudo cargar la imagen: " + imagenReferencia, Toast.LENGTH_LONG).show();
        }
    }

    private void colocarModelo(Anchor anchor) {

        // CREA UN NODO FIJO EN EL MUNDO REAL A PARTIR DEL ANCHOR
        AnchorNode anchorNode = new AnchorNode(anchor);
        anchorNode.setParent(arFragment.getArSceneView().getScene());

        // CREA UN NODO TRANSFORMABLE PARA PODER MOSTRAR Y MANIPULAR EL MODELO
        TransformableNode node = new TransformableNode(arFragment.getTransformationSystem());

        node.setParent(anchorNode);
        node.setRenderable(modeloRenderable);

        // UBICA EL MODELO SEGUN LAS COORD GUARDADAS EN FIREBASE
        node.setLocalPosition(new Vector3(posicionX, posicionY, posicionZ));

        // AJUSTA LA ESCALA DEL MOD
        node.setLocalScale(new Vector3(0.5f, 0.5f, 0.5f));

        // ROTA EL MODELO PARA QUE SE ORIENTE
        node.setLocalRotation(Quaternion.axisAngle(new Vector3(0f, 1f, 0f), 270f));

        node.select();

        // AL TOCAR EL MODELO, VUELVE A MOSTRAR LA CARD DE INFO
        node.setOnTapListener((hitTestResult, motionEvent) -> {
            mostrarInformacion("Modelo 3D del docente.");
        });
    }

    private void mostrarInformacion(String mensajeAyuda) {

        // OCULTA EL MENSAJE DE ESCANEO Y MUESTRA LA CARD INFOR
        tvMensajeEscaneo.setVisibility(View.GONE);
        cardInfoAR.setVisibility(View.VISIBLE);

        // MUESTRA EL NOMBRE DEL PUNTO O UN TEXTO POR DEFECTO
        tvNombreAR.setText(nombre != null && !nombre.trim().isEmpty() ? nombre : "Información educativa"
        );

        String texto = "";

        if (contenidoAR != null && !contenidoAR.trim().isEmpty()) {
            texto = contenidoAR;
        } else if (descripcion != null && !descripcion.trim().isEmpty()) {
            texto = descripcion;
        } else {
            texto = "No hay información disponible.";
        }

        // AGREGA INFORMACION DEL RECURSO MULTIMEDIA SI EXISTE
        if (recursoMultimedia != null && !recursoMultimedia.trim().isEmpty()) {

            // SI EL RECURSO ES AUDIO, LO REPRODUCE AUTOMATICAMENTE
            if (tipoMultimedia != null && tipoMultimedia.equalsIgnoreCase("audio")) {
                texto += "\n\nAudio disponible.";
                reproducirAudioSiExiste();
            } else {
                texto += "\n\nRecurso multimedia disponible.";
            }
        }

        // MARCA EL DOCENTE COMO ENCONTRADO EN FIREBASE
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

        // VALIDA QUE EL RECURSO SEA AUDIO
        if (tipoMultimedia == null || !tipoMultimedia.equalsIgnoreCase("audio")) return;

        // VALIDA QUE EXISTA UN ARCHIVO MULTIMEDIA
        if (recursoMultimedia == null || recursoMultimedia.trim().isEmpty()) return;

        try {

            // LIBERA EL REPRODUCTOR ANTERIOR SI YA EXISTIA
            if (mediaPlayer != null) {
                mediaPlayer.release();
                mediaPlayer = null;
            }

            AssetFileDescriptor afd = getAssets().openFd("multimedia/" + recursoMultimedia.trim());

            // CONFIGURA EL MEDIAPLAYER CON EL AUDIO ENCONTRADO
            mediaPlayer = new MediaPlayer();
            mediaPlayer.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());

            afd.close();

            // PREPARA Y REPRODUCE EL AUDIO
            mediaPlayer.prepare();
            mediaPlayer.start();

        } catch (Exception e) {
            Toast.makeText(this, "No se pudo reproducir el audio educativo", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        // LIBERA EL AUDIO AL CERRAR LA ACTIVIDAD PARA EVITAR CONSUMO DE MEMORIA
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }
}