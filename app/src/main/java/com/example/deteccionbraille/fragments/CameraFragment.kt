package com.example.deteccionbraille.fragments

/*
 * Copyright 2022 The TensorFlow Authors. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *             http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import android.annotation.SuppressLint
import android.content.res.Configuration
import android.graphics.Bitmap
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ImageButton
import android.widget.Toast
import androidx.camera.core.AspectRatio
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageAnalysis.OUTPUT_IMAGE_FORMAT_RGBA_8888
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation
import com.example.deteccionbraille.ObjectDetectorHelper
import com.example.deteccionbraille.R
import com.example.deteccionbraille.databinding.FragmentCameraBinding
import org.tensorflow.lite.task.vision.detector.Detection
import java.util.LinkedList
import java.util.Locale
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class CameraFragment : Fragment(), ObjectDetectorHelper.DetectorListener {

    //etc
    private var tts: TextToSpeech? = null

    private val TAG = "ObjectDetection"

    private var _fragmentCameraBinding: FragmentCameraBinding? = null

    private val fragmentCameraBinding
        get() = _fragmentCameraBinding!!

    private lateinit var objectDetectorHelper: ObjectDetectorHelper
    private lateinit var bitmapBuffer: Bitmap
    private var preview: Preview? = null
    private var imageAnalyzer: ImageAnalysis? = null
    private var camera: Camera? = null
    private var cameraProvider: ProcessCameraProvider? = null

    /** Realizar operaciones de cámara bloqueantes utilizando este executor */
    private lateinit var cameraExecutor: ExecutorService

    override fun onResume() {
        super.onResume()
        // Asegurarse de que todos los permisos estén presentes, ya que el
        // usuario podría haberlos eliminado mientras la aplicación estaba en estado pausado.
        if (!PermissionsFragment.hasPermissions(requireContext())) {
            Navigation.findNavController(requireActivity(), R.id.fragment_container)
                .navigate(CameraFragmentDirections.actionCameraToPermissions())
        }
    }

    override fun onDestroyView() {
        _fragmentCameraBinding = null
        super.onDestroyView()

        // Apagar nuestro executor de fondo
        cameraExecutor.shutdown()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _fragmentCameraBinding = FragmentCameraBinding.inflate(inflater, container, false)

        return fragmentCameraBinding.root
    }

    //etc
    private lateinit var captureButton: ImageButton
    private fun captureButtonPressed() {
        val tempPredictionString = predictionString  // Almacena temporalmente la cadena de predicción

        // Limpiar la cadena de predicción antes de capturar los resultados
        predictionString = ""

        // Hablar las predicciones si las hay y TTS está inicializado
        if (tempPredictionString.isNotEmpty() && tts != null) {
            Log.d(TAG, "Hablando predicción: $tempPredictionString")
            tts!!.language = Locale("es", "colombia")
            tts?.speak(tempPredictionString, TextToSpeech.QUEUE_ADD, null, null)
        } else {
            Log.d(TAG, "No hay predicciones para hablar o el TTS no está inicializado")
        }
    }

    //etc

    @SuppressLint("MissingPermission")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //etc
        // Initialize captureButton
        captureButton = view.findViewById(R.id.read_predictions_button)
        objectDetectorHelper = ObjectDetectorHelper(
            context = requireContext(),
            objectDetectorListener = this
        )

        // Inicializar nuestro executor de fondo
        cameraExecutor = Executors.newSingleThreadExecutor()

        // Esperar a que las vistas estén correctamente dispuestas
        fragmentCameraBinding.viewFinder.post {
            // Configurar la cámara y sus casos de uso
            setUpCamera()
        }

        // Adjuntar oyentes a los widgets de control de la IU
        initBottomSheetControls()

        //etc
        initButton()
        //etc
    }

    //etc
    private fun initButton(){
        captureButton.setOnClickListener {
            Log.d(TAG, "boton presionado")
            if (captureButton.isEnabled) {
                captureButtonPressed()
            }
        }

        // Inicializar TextToSpeech
        tts = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                // TTS está listo para ser utilizado
            } else {
                Log.e(TAG, "Fallo la inicialización de Text To Speech")
            }
        }

    }
    //etc

    private fun initBottomSheetControls() {
        // Al hacer clic, disminuir el umbral de puntuación de detección
        fragmentCameraBinding.bottomSheetLayout.thresholdMinus.setOnClickListener {
            if (objectDetectorHelper.threshold >= 0.1) {
                objectDetectorHelper.threshold -= 0.1f
                updateControlsUi()
            }
        }

        // Al hacer clic, aumentar el umbral de puntuación de detección
        fragmentCameraBinding.bottomSheetLayout.thresholdPlus.setOnClickListener {
            if (objectDetectorHelper.threshold <= 0.8) {
                objectDetectorHelper.threshold += 0.1f
                updateControlsUi()
            }
        }

        // Al hacer clic, reducir el número de objetos que pueden ser detectados a la vez
        fragmentCameraBinding.bottomSheetLayout.maxResultsMinus.setOnClickListener {
            if (objectDetectorHelper.maxResults > 1) {
                objectDetectorHelper.maxResults--
                updateControlsUi()
            }
        }

        // Al hacer clic, aumentar el número de objetos que pueden ser detectados a la vez
        fragmentCameraBinding.bottomSheetLayout.maxResultsPlus.setOnClickListener {
            if (objectDetectorHelper.maxResults < 5) {
                objectDetectorHelper.maxResults++
                updateControlsUi()
            }
        }

        // Al hacer clic, disminuir el número de hilos utilizados para la detección
        fragmentCameraBinding.bottomSheetLayout.threadsMinus.setOnClickListener {
            if (objectDetectorHelper.numThreads > 1) {
                objectDetectorHelper.numThreads--
                updateControlsUi()
            }
        }

        // Al hacer clic, aumentar el número de hilos utilizados para la detección
        fragmentCameraBinding.bottomSheetLayout.threadsPlus.setOnClickListener {
            if (objectDetectorHelper.numThreads < 4) {
                objectDetectorHelper.numThreads++
                updateControlsUi()
            }
        }

        // Al hacer clic, cambiar el hardware subyacente utilizado para la inferencia. Las opciones actuales son CPU
        // GPU y NNAPI
        fragmentCameraBinding.bottomSheetLayout.spinnerDelegate.setSelection(0, false)
        fragmentCameraBinding.bottomSheetLayout.spinnerDelegate.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                    objectDetectorHelper.currentDelegate = p2
                    updateControlsUi()
                }

                override fun onNothingSelected(p0: AdapterView<*>?) {
                    /* sin operación */
                }
            }

        // Al hacer clic, cambiar el modelo subyacente utilizado para la detección de objetos
        fragmentCameraBinding.bottomSheetLayout.spinnerModel.setSelection(0, false)
        fragmentCameraBinding.bottomSheetLayout.spinnerModel.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                    objectDetectorHelper.currentModel = p2
                    updateControlsUi()
                }

                override fun onNothingSelected(p0: AdapterView<*>?) {
                    /* sin operación */
                }
            }
    }

    // Actualizar los valores mostrados en la hoja inferior. Restablecer el detector.
    private fun updateControlsUi() {
        fragmentCameraBinding.bottomSheetLayout.maxResultsValue.text =
            objectDetectorHelper.maxResults.toString()
        fragmentCameraBinding.bottomSheetLayout.thresholdValue.text =
            String.format("%.2f", objectDetectorHelper.threshold)
        fragmentCameraBinding.bottomSheetLayout.threadsValue.text =
            objectDetectorHelper.numThreads.toString()

        // Necesita ser borrado en lugar de reinicializado porque la GPU
        // el delegado necesita ser inicializado en el hilo que lo utiliza cuando corresponde
        objectDetectorHelper.clearObjectDetector()
        fragmentCameraBinding.overlay.clear()
    }

    // Inicializar CameraX y prepararse para vincular los casos de uso de la cámara
    private fun setUpCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())
        cameraProviderFuture.addListener(
            {
                // Proveedor de cámaras
                cameraProvider = cameraProviderFuture.get()

                // Construir y vincular los casos de uso de la cámara
                bindCameraUseCases()
            },
            ContextCompat.getMainExecutor(requireContext())
        )
    }

    // Declarar y vincular casos de uso de vista previa, captura y análisis
    @SuppressLint("UnsafeOptInUsageError")
    private fun bindCameraUseCases() {

        // Proveedor de cámaras
        val cameraProvider =
            cameraProvider ?: throw IllegalStateException("La inicialización de la cámara falló.")

        // Selector de cámara: asume que solo estamos usando la cámara trasera
        val cameraSelector =
            CameraSelector.Builder().requireLensFacing(CameraSelector.LENS_FACING_BACK).build()

        // Vista previa. Solo usando la relación de aspecto 4:3 porque es la más cercana a nuestros modelos
        preview =
            Preview.Builder()
                .setTargetAspectRatio(AspectRatio.RATIO_4_3)
                .setTargetRotation(fragmentCameraBinding.viewFinder.display.rotation)
                .build()

        // Análisis de imágenes. Usando RGBA 8888 para que coincida con cómo funcionan nuestros modelos
        imageAnalyzer =
            ImageAnalysis.Builder()
                .setTargetAspectRatio(AspectRatio.RATIO_4_3)
                .setTargetRotation(fragmentCameraBinding.viewFinder.display.rotation)
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .setOutputImageFormat(OUTPUT_IMAGE_FORMAT_RGBA_8888)
                .build()
                // El analizador puede entonces asignarse a la instancia
                .also {
                    it.setAnalyzer(cameraExecutor) { image ->
                        if (!::bitmapBuffer.isInitialized) {
                            // La rotación de la imagen y el búfer de imagen RGB se inicializan solo una vez
                            // que el analizador ha comenzado a ejecutarse
                            bitmapBuffer = Bitmap.createBitmap(
                                image.width,
                                image.height,
                                Bitmap.Config.ARGB_8888
                            )
                        }

                        detectObjects(image)
                    }
                }

        // Es necesario desvincular los casos de uso antes de volver a vincularlos
        cameraProvider.unbindAll()

        try {
            // Se pueden pasar un número variable de casos de uso aquí -
            // la cámara proporciona acceso a CameraControl & CameraInfo
            camera = cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageAnalyzer)

            // Adjuntar el proveedor de superficie del visor a la vista previa
            preview?.setSurfaceProvider(fragmentCameraBinding.viewFinder.surfaceProvider)
        } catch (exc: Exception) {
            Log.e(TAG, "La vinculación de casos de uso falló", exc)
        }
    }

    private fun detectObjects(image: ImageProxy) {
        // Copiar los bits RGB al búfer de bitmap compartido
        image.use { bitmapBuffer.copyPixelsFromBuffer(image.planes[0].buffer) }

        val imageRotation = image.imageInfo.rotationDegrees
        // Pasar Bitmap y rotación al ayudante de detección de objetos para procesamiento y detección
        objectDetectorHelper.detect(bitmapBuffer, imageRotation)
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        imageAnalyzer?.targetRotation = fragmentCameraBinding.viewFinder.display.rotation
    }

    //etc
    // Actualizar la IU después de que se hayan detectado objetos. Extrae la altura/ancho de la imagen original
    // para escalar y colocar correctamente los cuadros delimitadores a través de OverlayView
    private var predictionString = ""

    override fun onResults(
        results: MutableList<Detection>?,
        inferenceTime: Long,
        imageHeight: Int,
        imageWidth: Int
    ) {
        activity?.runOnUiThread {
            predictionString = ""  // Limpiar la cadena antes de agregar nuevos resultados

            if (results != null) {
                for (result in results) {
                    predictionString += result.categories[0].label + ", "
                }
            }

            // Eliminar la última coma si hay resultados
            if (predictionString.isNotEmpty()) {
                predictionString = predictionString.substring(0, predictionString.length - 2)
            }

            fragmentCameraBinding.bottomSheetLayout.inferenceTimeVal.text =
                String.format("%d ms", inferenceTime)

            // Pasar la información necesaria a OverlayView para dibujar en el lienzo
            fragmentCameraBinding.overlay.setResults(
                results ?: LinkedList<Detection>(),
                imageHeight,
                imageWidth
            )

            // Forzar un redibujo
            fragmentCameraBinding.overlay.invalidate()
        }
    }


    override fun onError(error: String) {
        activity?.runOnUiThread {
            Toast.makeText(requireContext(), error, Toast.LENGTH_SHORT).show()
        }
    }
}
