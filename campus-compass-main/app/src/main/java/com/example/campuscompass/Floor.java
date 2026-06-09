package com.example.campuscompass;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import java.util.ArrayList;
import java.util.HashMap;

public class Floor extends Fragment {
    View v;

    // Floor-specific room layouts based on your drawings
    // Ground Floor
    String[] groundFloorPlaces = {"Entrance", "AV Hall", "Placement Cell - 1", "Placement Cell - 2", "Placement Hall"};
    // First Floor
    String[] firstFloorPlaces = {"LH101", "LH102", "LH103", "LH104", "Seminar Hall", "Washroom Area", "Staff Room 1", "Staff Room 2"};
    // Second Floor
    String[] secondFloorPlaces = {"Lab 1", "Lab 2", "Washroom Area 2F", "Store Room", "LH201", "LH202", "LH203", "LH204", "Staff Room 2F"};

    TextView[][] nodes = new TextView[6][8];
    TextView[] mainNodes = new TextView[6];
    View[] lines = new View[11];
    TextView[] stairNodes = new TextView[2];

    int[][] ids = {
            {R.id.topleftNode0, R.id.topNode0, R.id.toprightNode0, R.id.rightNode0, R.id.bottomrightNode0, R.id.bottomNode0, R.id.bottomleftNode0, R.id.leftNode0},
            {R.id.topleftNode1, R.id.topNode1, R.id.toprightNode1, R.id.rightNode1, R.id.bottomrightNode1, R.id.bottomNode1, R.id.bottomleftNode1, R.id.leftNode1},
            {R.id.topleftNode2, R.id.topNode2, R.id.toprightNode2, R.id.rightNode2, R.id.bottomrightNode2, R.id.bottomNode2, R.id.bottomleftNode2, R.id.leftNode2},
            {R.id.topleftNode3, R.id.topNode3, R.id.toprightNode3, R.id.rightNode3, R.id.bottomrightNode3, R.id.bottomNode3, R.id.bottomleftNode3, R.id.leftNode3},
            {R.id.topleftNode4, R.id.topNode4, R.id.toprightNode4, R.id.rightNode4, R.id.bottomrightNode4, R.id.bottomNode4, R.id.bottomleftNode4, R.id.leftNode4},
            {R.id.topleftNode5, R.id.topNode5, R.id.toprightNode5, R.id.rightNode5, R.id.bottomrightNode5, R.id.bottomNode5, R.id.bottomleftNode5, R.id.leftNode5}
    };

    HashMap<Integer, Integer> placesPositionMapping = new HashMap<>();
    Location locations[];
    int currentLevel = 0;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        v = inflater.inflate(R.layout.fragment_floor, container, false);

        // Map place positions to array indices
        placesPositionMapping.put(PlacePosition.top, 1);
        placesPositionMapping.put(PlacePosition.bottom, 5);
        placesPositionMapping.put(PlacePosition.right, 3);
        placesPositionMapping.put(PlacePosition.left, 7);
        placesPositionMapping.put(PlacePosition.topLeft, 0);
        placesPositionMapping.put(PlacePosition.topRight, 2);
        placesPositionMapping.put(PlacePosition.bottomRight, 4);
        placesPositionMapping.put(PlacePosition.bottomLeft, 6);

        Bundle bundle = getArguments();
        currentLevel = bundle.getInt("level");

        // Initialize UI components
        initializeComponents();

        // Get floor layout based on level
        setupFloorLayout(currentLevel);

        // Setup room displays based on floor
        setupRoomDisplays(currentLevel);

        // Highlight active paths
        highlightActivePaths();

        return v;
    }

    private void initializeComponents() {
        // Initialize main nodes
        for (int i = 0; i < 6; i++) {
            mainNodes[i] = v.findViewById(getResources().getIdentifier("Node" + i, "id", getContext().getPackageName()));
        }

        // Initialize connection lines
        lines[0] = v.findViewById(R.id.line01);
        lines[1] = v.findViewById(R.id.line12);
        lines[2] = v.findViewById(R.id.line23);
        lines[3] = v.findViewById(R.id.line34);
        lines[4] = v.findViewById(R.id.line50);
        lines[5] = v.findViewById(R.id.line0s);
        lines[6] = v.findViewById(R.id.line3s);
        lines[7] = v.findViewById(R.id.linestairs2);
        lines[8] = v.findViewById(R.id.line5s);
        lines[9] = v.findViewById(R.id.line4s);
        lines[10] = v.findViewById(R.id.linestairs1);

        // Initialize stairs
        stairNodes[0] = v.findViewById(R.id.stairs1);
        stairNodes[1] = v.findViewById(R.id.stairs2);

        // Initialize all position nodes
        for (int i = 0; i < 6; i++) {
            for (int j = 0; j < 8; j++) {
                nodes[i][j] = v.findViewById(ids[i][j]);
            }
        }
    }

    private void setupFloorLayout(int level) {
        // Get location data for current floor
        Location node0 = LevelPointer.levels[level];
        Location node1 = node0.getLeft();
        Location node2 = node1.getBack();
        Location node3 = node2.getRight();
        Location node4 = node3.getRight();
        Location node5 = node4.getFront();
        Location stairs1 = node0.getStairs();
        Location stairs2 = node5.getStairs();

        locations = new Location[]{node0, node1, node2, node3, node4, node5};

        // Adjust node positions based on floor layout
        adjustNodePositions(level);
    }

    private void adjustNodePositions(int level) {
        // Different positioning for each floor based on your drawings
        switch (level) {
            case 0: // Ground Floor
                setupGroundFloorLayout();
                break;
            case 1: // First Floor
                setupFirstFloorLayout();
                break;
            case 2: // Second Floor
                setupSecondFloorLayout();
                break;
        }
    }

    private void setupGroundFloorLayout() {
        // Based on your ground floor drawing:
        // Entrance (bottom left), AV Hall (top), Placement Cells, Placement Hall (bottom center)

        // Hide all nodes initially
        for (int i = 0; i < 6; i++) {
            mainNodes[i].setVisibility(View.VISIBLE);
        }

        // Position nodes according to ground floor layout
        // This would require adjusting the constraint layout programmatically
        // For now, we'll use the existing positions and update room labels
    }

    private void setupFirstFloorLayout() {
        // Based on your first floor drawing:
        // LH101-104, Seminar Hall, Staff Rooms, Washroom Area

        for (int i = 0; i < 6; i++) {
            mainNodes[i].setVisibility(View.VISIBLE);
        }
    }

    private void setupSecondFloorLayout() {
        // Based on your second floor drawing:
        // Labs, LH201-204, Staff Room, Store Room, Washroom Area

        for (int i = 0; i < 6; i++) {
            mainNodes[i].setVisibility(View.VISIBLE);
        }
    }

    private void setupRoomDisplays(int level) {
        // Clear all previous room labels
        clearRoomLabels();

        // Display rooms based on current floor
        for (int i = 0; i < 6; i++) {
            if (locations[i] != null) {
                ArrayList<String> places = locations[i].getPlaces();
                ArrayList<Integer> placePositions = locations[i].getPlacesPositions();

                for (int j = 0; j < places.size(); j++) {
                    Integer position = placePositions.get(j);
                    Integer mappedIndex = placesPositionMapping.get(position);

                    if (mappedIndex != null && mappedIndex < 8) {
                        String roomName = places.get(j);
                        nodes[i][mappedIndex].setText(roomName);
                        nodes[i][mappedIndex].setVisibility(View.VISIBLE);

                        // Apply color coding based on room type
                        applyRoomColorCoding(nodes[i][mappedIndex], roomName, level);
                    }
                }
            }
        }
    }

    private void applyRoomColorCoding(TextView roomLabel, String roomName, int level) {
        int textColor = Color.BLACK;
        int backgroundColor = Color.TRANSPARENT;

        // Color coding based on room type
        if (roomName.startsWith("LH") || roomName.contains("Lab")) {
            textColor = Color.rgb(0, 0, 139); // Dark blue for classrooms/labs
            backgroundColor = Color.rgb(230, 240, 255); // Light blue background
        } else if (roomName.contains("Staff") || roomName.contains("Office")) {
            textColor = Color.rgb(139, 0, 0); // Dark red for staff areas
            backgroundColor = Color.rgb(255, 230, 230); // Light red background
        } else if (roomName.contains("Hall") || roomName.contains("Auditorium") || roomName.contains("AV")) {
            textColor = Color.rgb(0, 139, 0); // Dark green for halls
            backgroundColor = Color.rgb(230, 255, 230); // Light green background
        } else if (roomName.contains("Placement")) {
            textColor = Color.rgb(139, 69, 0); // Brown for placement areas
            backgroundColor = Color.rgb(255, 245, 230); // Light orange background
        } else if (roomName.contains("Washroom") || roomName.contains("Store")) {
            textColor = Color.rgb(128, 128, 128); // Gray for utility areas
            backgroundColor = Color.rgb(245, 245, 245); // Light gray background
        } else {
            textColor = Color.BLACK;
            backgroundColor = Color.rgb(248, 248, 255); // Very light background
        }

        roomLabel.setTextColor(textColor);

        // Create rounded background
        GradientDrawable background = new GradientDrawable();
        background.setColor(backgroundColor);
        background.setCornerRadius(8);
        background.setStroke(2, textColor);
        roomLabel.setBackground(background);
        roomLabel.setPadding(8, 4, 8, 4);
    }

    private void clearRoomLabels() {
        for (int i = 0; i < 6; i++) {
            for (int j = 0; j < 8; j++) {
                nodes[i][j].setText("");
                nodes[i][j].setVisibility(View.INVISIBLE);
            }
        }
    }

    private void highlightActivePaths() {
        // Reset all visual states
        resetPathHighlights();

        if (locations == null) return;

        // Highlight connection lines between nodes in route
        highlightNodeConnections();

        // Highlight stairs if in route
        highlightStairs();

        // Highlight main nodes if in route
        highlightMainNodes();
    }

    private void resetPathHighlights() {
        // Reset all lines to normal state
        for (View line : lines) {
            if (line != null) {
                line.setBackgroundColor(Color.rgb(200, 200, 200)); // Gray for inactive
                line.setVisibility(View.VISIBLE);
            }
        }

        // Reset node colors
        for (TextView node : mainNodes) {
            if (node != null) {
                node.setBackgroundColor(Color.rgb(100, 100, 100)); // Dark gray for inactive nodes
            }
        }

        // Reset stairs
        for (TextView stair : stairNodes) {
            if (stair != null) {
                stair.setBackgroundColor(Color.rgb(100, 149, 237)); // Steel blue for inactive stairs
            }
        }
    }

    private void highlightNodeConnections() {
        if (locations.length < 6) return;

        Location node0 = locations[0];
        Location node1 = locations[1];
        Location node2 = locations[2];
        Location node3 = locations[3];
        Location node4 = locations[4];
        Location node5 = locations[5];

        // Highlight connections between nodes that are in route
        if (node0.getInRoute() && node1.getInRoute()) {
            lines[0].setBackgroundColor(Color.rgb(0, 255, 0)); // Green for active path
        }
        if (node1.getInRoute() && node2.getInRoute()) {
            lines[1].setBackgroundColor(Color.rgb(0, 255, 0));
        }
        if (node2.getInRoute() && node3.getInRoute()) {
            lines[2].setBackgroundColor(Color.rgb(0, 255, 0));
        }
        if (node3.getInRoute() && node4.getInRoute()) {
            lines[3].setBackgroundColor(Color.rgb(0, 255, 0));
        }
        if (node5.getInRoute() && node0.getInRoute()) {
            lines[4].setBackgroundColor(Color.rgb(0, 255, 0));
        }
    }

    private void highlightStairs() {
        if (locations.length < 6) return;

        Location node0 = locations[0];
        Location node3 = locations[3];
        Location node4 = locations[4];
        Location node5 = locations[5];

        // Get stairs from nodes
        Location stairs1 = node0.getStairs();
        Location stairs2 = node5.getStairs();

        // Highlight stairs connections
        if (stairs1 != null && stairs1.getInRoute()) {
            stairNodes[0].setBackgroundColor(Color.rgb(0, 255, 0));

            if (node0.getInRoute()) {
                lines[5].setBackgroundColor(Color.rgb(0, 255, 0));
            }
            if (node3.getInRoute()) {
                lines[6].setBackgroundColor(Color.rgb(0, 255, 0));
            }
        }

        if (stairs2 != null && stairs2.getInRoute()) {
            stairNodes[1].setBackgroundColor(Color.rgb(0, 255, 0));

            if (node5.getInRoute()) {
                lines[8].setBackgroundColor(Color.rgb(0, 255, 0));
            }
            if (node4.getInRoute()) {
                lines[9].setBackgroundColor(Color.rgb(0, 255, 0));
            }
        }

        // Highlight stair-to-stair connections
        if (stairs1 != null && stairs2 != null && stairs1.getInRoute() && stairs2.getInRoute()) {
            lines[7].setBackgroundColor(Color.rgb(0, 255, 0));
            lines[10].setBackgroundColor(Color.rgb(0, 255, 0));
        }
    }

    private void highlightMainNodes() {
        // Highlight main nodes that are in the route
        for (int i = 0; i < 6 && i < locations.length; i++) {
            if (locations[i] != null && locations[i].getInRoute()) {
                mainNodes[i].setBackgroundColor(Color.rgb(0, 255, 0)); // Green for active nodes
            }
        }
    }

    public static int getImageResourceId(Context context, String imageName) {
        return context.getResources().getIdentifier(imageName, "drawable", context.getPackageName());
    }

    // Helper method to create floor-specific room arrangements
    private void arrangeRoomsForFloor(int level) {
        switch (level) {
            case 0: // Ground Floor
                arrangeGroundFloorRooms();
                break;
            case 1: // First Floor
                arrangeFirstFloorRooms();
                break;
            case 2: // Second Floor
                arrangeSecondFloorRooms();
                break;
        }
    }

    private void arrangeGroundFloorRooms() {
        // Arrange rooms according to your ground floor drawing
        // This method can be expanded to programmatically position rooms
    }

    private void arrangeFirstFloorRooms() {
        // Arrange rooms according to your first floor drawing
    }

    private void arrangeSecondFloorRooms() {
        // Arrange rooms according to your second floor drawing
    }
}