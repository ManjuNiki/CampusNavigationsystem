package com.example.campuscompass;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.BitmapFactory;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.github.chrisbanes.photoview.PhotoView;

import java.util.ArrayList;

public class PathActivity extends AppCompatActivity implements SensorEventListener {
    private PhotoView photoView;
    private SensorManager sensorManager;
    private Sensor gyroSensor;
    private long previousTimestamp = 0;

    ImageButton[] arrows = new ImageButton[4];
    ImageButton up;
    ImageButton down;
    ImageButton stairs;
    float[] arrowsAngles = {0, 180, -90, 90};
    Location current;
    Location previousCurrent = null;

    private float currentRotation = 0f; // Track total rotation

    // Route data
    ArrayList<Integer> routeImages;
    ArrayList<String> routeNames;
    int currentRouteIndex = 0;

    // UI for showing current location
    TextView locationNameText;
    TextView progressText;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_path);

        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);

        int pivotX = displayMetrics.widthPixels / 8;
        int pivotY = displayMetrics.heightPixels / 4;
        current = CurrentPointer.current;

        photoView = findViewById(R.id.photo_view);
        photoView.setScaleType(android.widget.ImageView.ScaleType.CENTER_CROP);

        // Get route data from intent
        routeImages = getIntent().getIntegerArrayListExtra("route_images");
        routeNames = getIntent().getStringArrayListExtra("route_names");

        if (routeImages == null || routeImages.isEmpty()) {
            Log.e("PathActivity", "No route images received!");
            routeImages = new ArrayList<>();
            routeNames = new ArrayList<>();
            // Fallback to current location's image
            if (current != null) {
                routeImages.add(current.getImage());
                routeNames.add(current.getName());
            }
        }

        Log.d("PathActivity", "Received " + routeImages.size() + " images in route");

        // Initialize location display UI (optional - you can add these to your layout)
        locationNameText = findViewById(R.id.location_name);
        progressText = findViewById(R.id.progress_text);

        // If these don't exist in your layout, handle gracefully
        if (locationNameText != null && routeNames != null && !routeNames.isEmpty()) {
            locationNameText.setText(routeNames.get(currentRouteIndex));
        }
        if (progressText != null) {
            progressText.setText((currentRouteIndex + 1) + " / " + routeImages.size());
        }

        arrows[0] = findViewById(R.id.front);
        arrows[1] = findViewById(R.id.back);
        arrows[2] = findViewById(R.id.left);
        arrows[3] = findViewById(R.id.right);
        stairs = findViewById(R.id.stair);
        up = findViewById(R.id.upStairs);
        down = findViewById(R.id.downStairs);

        for (int i = 0; i < 4; i++) {
            arrows[i].setPivotX(pivotX);
            arrows[i].setPivotY(pivotY);
            arrows[i].setRotation(arrowsAngles[i]);
            arrows[i].setRotationX(20);
            arrows[i].setVisibility(View.INVISIBLE);
        }

        stairs.setPivotX(pivotX);
        stairs.setPivotY(pivotY);
        stairs.setRotationX(20);
        stairs.setVisibility(View.INVISIBLE);

        up.setPivotX(pivotX);
        up.setPivotY(pivotY);
        up.setRotationX(20);
        up.setVisibility(View.INVISIBLE);

        down.setPivotX(pivotX);
        down.setPivotY(pivotY);
        down.setRotationX(20);
        down.setVisibility(View.INVISIBLE);

        updateNavigationArrows();

        arrows[0].setOnClickListener(view -> {
            if (current.getFront() != null) {
                current = current.getFront();
                advanceRoute();
                reCalibrate();
            }
        });

        arrows[1].setOnClickListener(view -> {
            if (current.getBack() != null) {
                current = current.getBack();
                advanceRoute();
                reCalibrate();
            }
        });

        arrows[2].setOnClickListener(view -> {
            if (current.getLeft() != null) {
                current = current.getLeft();
                advanceRoute();
                reCalibrate();
            }
        });

        arrows[3].setOnClickListener(view -> {
            if (current.getRight() != null) {
                current = current.getRight();
                advanceRoute();
                reCalibrate();
            }
        });

        stairs.setOnClickListener(view -> {
            if (current.getStairs() != null) {
                current = current.getStairs();
                Log.d("PathActivity", "Navigating to stairs");
                advanceRoute();
                reCalibrate();
            }
        });

        up.setOnClickListener(view -> {
            if (current.getUp() != null) {
                current = current.getUp();
                Log.d("PathActivity", "Going up stairs");
                advanceRoute();
                reCalibrate();
            }
        });

        down.setOnClickListener(view -> {
            if (current.getDown() != null) {
                current = current.getDown();
                Log.d("PathActivity", "Going down stairs");
                advanceRoute();
                reCalibrate();
            }
        });

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        gyroSensor = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);

        loadImage();
    }

    private void advanceRoute() {
        // Move to next image in route
        if (currentRouteIndex < routeImages.size() - 1) {
            currentRouteIndex++;
            updateLocationDisplay();
            Log.d("PathActivity", "Advanced to route index: " + currentRouteIndex);
        } else {
            Log.d("PathActivity", "Reached end of route");
        }
    }

    private void updateLocationDisplay() {
        if (locationNameText != null && routeNames != null && currentRouteIndex < routeNames.size()) {
            locationNameText.setText(routeNames.get(currentRouteIndex));
        }
        if (progressText != null) {
            progressText.setText((currentRouteIndex + 1) + " / " + routeImages.size());
        }
    }

    private void loadImage() {
        try {
            // Load the image for the current route position
            if (routeImages != null && currentRouteIndex < routeImages.size()) {
                int imageResource = routeImages.get(currentRouteIndex);
                photoView.setImageBitmap(BitmapFactory.decodeResource(getResources(), imageResource));
                Log.d("PathActivity", "Loaded image " + (currentRouteIndex + 1) + " of " + routeImages.size());

                if (routeNames != null && currentRouteIndex < routeNames.size()) {
                    Log.d("PathActivity", "Location: " + routeNames.get(currentRouteIndex));
                }
            } else {
                // Fallback to current location's image
                photoView.setImageBitmap(BitmapFactory.decodeResource(getResources(), current.getImage()));
                Log.d("PathActivity", "Loaded fallback image for: " + current.getName());
            }
        } catch (Exception e) {
            Log.e("PathActivity", "Error loading image", e);
        }
    }

    private void updateNavigationArrows() {
        // Hide all arrows first
        for (int i = 0; i < 4; i++) {
            arrows[i].setVisibility(View.INVISIBLE);
        }
        stairs.setVisibility(View.INVISIBLE);
        up.setVisibility(View.INVISIBLE);
        down.setVisibility(View.INVISIBLE);

        // Show only arrows for available routes
        if (current.getFront() != null && current.getFront().getInRoute()) {
            arrows[0].setVisibility(View.VISIBLE);
        }
        if (current.getBack() != null && current.getBack().getInRoute()) {
            arrows[1].setVisibility(View.VISIBLE);
        }
        if (current.getLeft() != null && current.getLeft().getInRoute()) {
            arrows[2].setVisibility(View.VISIBLE);
        }
        if (current.getRight() != null && current.getRight().getInRoute()) {
            arrows[3].setVisibility(View.VISIBLE);
        }
        if (current.getStairs() != null && current.getStairs().getInRoute()) {
            stairs.setVisibility(View.VISIBLE);
            stairs.setRotation(current.getStairsAngle() - currentRotation + current.getAngle());
        }
        if (current.getUp() != null && current.getUp().getInRoute()) {
            up.setVisibility(View.VISIBLE);
            up.setRotation(current.getUpAngle() - currentRotation + current.getAngle());
        }
        if (current.getDown() != null && current.getDown().getInRoute()) {
            down.setVisibility(View.VISIBLE);
            down.setRotation(current.getDownAngle() - currentRotation + current.getAngle());
        }

        Log.d("PathActivity", "Updated arrows for: " + current.getName());
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (gyroSensor != null) {
            sensorManager.registerListener(this, gyroSensor, SensorManager.SENSOR_DELAY_GAME);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(this);
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        if (sensorEvent.sensor.getType() == Sensor.TYPE_GYROSCOPE) {
            // Calculate time delta
            if (previousTimestamp == 0) {
                previousTimestamp = sensorEvent.timestamp;
                return;
            }

            float deltaTime = (sensorEvent.timestamp - previousTimestamp) / 1000000000.0f; // Convert to seconds
            previousTimestamp = sensorEvent.timestamp;

            // Calculate rotation change (Z-axis rotation in radians/second)
            float deltaRotation = sensorEvent.values[2] * deltaTime * 57.2957795f; // Convert to degrees

            // Update total rotation
            currentRotation -= deltaRotation; // Negative for correct direction

            // Apply rotation to photo view
            photoView.setRotation(currentRotation);

            // Update arrow rotations to stay fixed relative to real world
            for (int i = 0; i < 4; i++) {
                arrows[i].setRotation(arrowsAngles[i] - currentRotation + current.getAngle());
            }
            stairs.setRotation(current.getStairsAngle() - currentRotation + current.getAngle());
            up.setRotation(current.getUpAngle() - currentRotation + current.getAngle());
            down.setRotation(current.getDownAngle() - currentRotation + current.getAngle());
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        if (accuracy == SensorManager.SENSOR_STATUS_UNRELIABLE) {
            Log.w("PathActivity", "Gyroscope accuracy unreliable");
        }
    }

    private void reCalibrate() {
        // Reset rotation when moving to new location
        currentRotation = 0f;
        previousTimestamp = 0;
        photoView.setRotation(0f);

        // Reset arrow angles
        for (int i = 0; i < 4; i++) {
            arrows[i].setRotation(arrowsAngles[i] + current.getAngle());
        }
        stairs.setRotation(current.getStairsAngle() + current.getAngle());
        up.setRotation(current.getUpAngle() + current.getAngle());
        down.setRotation(current.getDownAngle() + current.getAngle());

        // Update UI
        updateNavigationArrows();
        loadImage();

        Log.d("PathActivity", "Recalibrated at: " + current.getName() + " (Level " + current.getLevel() + ")");
    }
}