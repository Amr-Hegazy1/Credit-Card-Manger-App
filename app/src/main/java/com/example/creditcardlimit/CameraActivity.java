package com.example.creditcardlimit;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.graphics.Rect;
import android.media.Image;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.util.Size;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.Camera;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.lifecycle.LifecycleOwner;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.Text;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;
import com.google.mlkit.vision.text.latin.TextRecognizerOptions;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class CameraActivity extends AppCompatActivity {
    private ListenableFuture<ProcessCameraProvider> cameraProviderFuture;
    PreviewView previewView;
    FloatingActionButton floatingActionButton;
    Executor executor;
    File dir;
    ImageProxy proxyImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);
        previewView = findViewById(R.id.previewView);

        executor = ContextCompat.getMainExecutor(this);
        cameraProviderFuture = ProcessCameraProvider.getInstance(this);
        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
                bindPreview(cameraProvider);
            } catch (ExecutionException | InterruptedException e) {
                // No errors need to be handled for this Future.
                // This should never be reached.
            }
        }, ContextCompat.getMainExecutor(this));
    }

    void bindPreview(@NonNull ProcessCameraProvider cameraProvider) {
        floatingActionButton = findViewById(R.id.floatingActionButton2);
        Preview preview = new Preview.Builder()
                .build();

        CameraSelector cameraSelector = new CameraSelector.Builder()
                .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                .build();

        ImageAnalysis imageAnalysis =
                new ImageAnalysis.Builder()
                        .setTargetResolution(new Size(1280, 720))
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                        .build();

        imageAnalysis.setAnalyzer(executor, new ImageAnalysis.Analyzer() {
            @Override
            public void analyze(@NonNull ImageProxy image) {
                int rotationDegrees = image.getImageInfo().getRotationDegrees();
                // insert your code here.
            }
        });

        ImageCapture imageCapture =
                new ImageCapture.Builder()
                        .setTargetRotation(previewView.getDisplay().getRotation())
                        .setIoExecutor(executor)
                        .build();

        preview.setSurfaceProvider(previewView.getSurfaceProvider());

        Camera camera = cameraProvider.bindToLifecycle((LifecycleOwner) this, cameraSelector, imageCapture, imageAnalysis, preview);
        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("UnsafeOptInUsageError")
            @RequiresApi(api = Build.VERSION_CODES.R)
            @Override
            public void onClick(View v) {
                OutputStream app_folder_path = null;
                try {
                    app_folder_path = new FileOutputStream(getApplicationContext().getFilesDir() + "/images/test.jpg");
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
                dir = new File(getApplicationContext().getFilesDir(),"images");
                File newFile = new File(dir,"test.jpg");

//                if (!dir.exists() && !dir.mkdirs()) {
//
//                }
//                ImageCapture.OutputFileOptions outputFileOptions =
//                        new ImageCapture.OutputFileOptions.Builder(newFile).build();
                imageCapture.takePicture(executor, new ImageCapture.OnImageCapturedCallback() {
                    @Override
                    public void onError(@NonNull @NotNull ImageCaptureException exception) {
                        super.onError(exception);
                        Toast.makeText(CameraActivity.this, "fashel", Toast.LENGTH_SHORT).show();
                    }


                    @Override
                            public void onCaptureSuccess(@NonNull @NotNull ImageProxy imageProxy) {
                                super.onCaptureSuccess(imageProxy);
                                proxyImage = imageProxy;
                                Toast.makeText(CameraActivity.this, "Gamed", Toast.LENGTH_SHORT).show();

                            }
                        });
                TextRecognizer recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS);
                InputImage image;

                image = InputImage.fromMediaImage(Objects.requireNonNull(proxyImage.getImage()),proxyImage.getImageInfo().getRotationDegrees());
                Task<Text> result =
                        recognizer.process(image)
                                .addOnSuccessListener(new OnSuccessListener<Text>() {
                                    @Override
                                    public void onSuccess(@NotNull Text visionText) {
                                        // Task completed successfully
                                        Toast.makeText(CameraActivity.this,visionText.toString(),Toast.LENGTH_LONG).show();
                                    }
                                })
                                .addOnFailureListener(
                                        new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                // Task failed with an exception
                                                Toast.makeText(CameraActivity.this,"ya3",Toast.LENGTH_LONG).show();
                                                e.printStackTrace();
                                            }
                                        });
                String resultText = result.getResult().getText();
                for (Text.TextBlock block : result.getResult().getTextBlocks()) {
                    String blockText = block.getText();
                    Point[] blockCornerPoints = block.getCornerPoints();
                    Rect blockFrame = block.getBoundingBox();
                    for (Text.Line line : block.getLines()) {
                        String lineText = line.getText();
                        Point[] lineCornerPoints = line.getCornerPoints();
                        Rect lineFrame = line.getBoundingBox();
                        for (Text.Element element : line.getElements()) {
                            String elementText = element.getText();
                            Point[] elementCornerPoints = element.getCornerPoints();
                            Rect elementFrame = element.getBoundingBox();
                        }
                    }
                }





            }
        });


    }
}
