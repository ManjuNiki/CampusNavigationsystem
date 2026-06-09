package com.example.campuscompass;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Deque;
import java.util.LinkedList;

public class ExploreActivity extends AppCompatActivity {
    Button explore;
    TextView building_name;
    Spinner source, dest;
    int curFloor = 0;
    int selectedLevel = 0;

    String[] places = {
            // Ground Floor (Level 0)
            "Entrance", "AV Hall", "Placement Cell - 1", "Placement Cell - 2", "Placement Hall",
            // First Floor (Level 1)
            "LH101", "LH102", "LH103", "LH104", "Seminar Hall", "Washroom Area", "Staff Room 1", "Staff Room 2",
            // Second Floor (Level 2)
            "Lab 1", "Lab 2", "Lab 3", "LH201", "LH202", "LH203", "LH204", "Staff Room 2F", "Store Room"
    };

    int[] placesLevels = {
            // Ground Floor (0)
            0, 0, 0, 0, 0,
            // First Floor (1)
            1, 1, 1, 1, 1, 1, 1, 1,
            // Second Floor (2)
            2, 2, 2, 2, 2, 2, 2, 2, 2
    };

    Location src = null, desti = null;
    Location newSrc;

    Deque<Location> traverse = new LinkedList<Location>();
    Deque<Location> smallest = new LinkedList<Location>();
    Button[] pills = new Button[3];

    // Store the complete route for PathActivity
    ArrayList<Location> completeRoute = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_explore);
        makeLocations();

        explore = findViewById(R.id.showPath);
        explore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Check if both source and destination are selected
                if (src == null || desti == null) {
                    Toast.makeText(ExploreActivity.this,
                            "Please select both source and destination",
                            Toast.LENGTH_SHORT).show();
                    return;
                }

                // Check if route exists
                if (!src.getInRoute()) {
                    Toast.makeText(ExploreActivity.this,
                            "No route found. Please select different locations.",
                            Toast.LENGTH_SHORT).show();
                    return;
                }

                // Set the starting point for PathActivity
                CurrentPointer.current = src;

                // Collect images along the route
                ArrayList<Integer> routeImages = new ArrayList<>();
                ArrayList<String> routeNames = new ArrayList<>();

                for (Location loc : completeRoute) {
                    if (loc != null) {
                        routeImages.add(loc.getImage());
                        routeNames.add(loc.getName());
                        Log.d("ExploreActivity", "Added to route: " + loc.getName());
                    }
                }

                Log.d("ExploreActivity", "Starting navigation from: " + src.getName() +
                        " to: " + desti.getName() + " with " + routeImages.size() + " images");

                // Start PathActivity with route data
                Intent i = new Intent(getApplicationContext(), PathActivity.class);
                i.putIntegerArrayListExtra("route_images", routeImages);
                i.putStringArrayListExtra("route_names", routeNames);
                startActivity(i);
            }
        });

        source = findViewById(R.id.source);
        dest = findViewById(R.id.dest);

        ArrayAdapter<String> sourceAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_dropdown_item, places);
        source.setAdapter(sourceAdapter);
        ArrayAdapter<String> destAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_dropdown_item, places);
        dest.setAdapter(destAdapter);

        building_name = findViewById(R.id.wifi_name);
        building_name.setText("CMR Management Block");

        // Initialize with Ground Floor
        Bundle bundle = new Bundle();
        bundle.putInt("level", 0);
        Floor f = new Floor();
        f.setArguments(bundle);
        replaceFragment(f);
        CurrentPointer.current = LevelPointer.levels[0];

        source.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if (view != null) {
                    ((TextView) adapterView.getChildAt(0)).setTextColor(Color.BLACK);
                }
                src = getLocation(places[i], placesLevels[i]);
                Log.d("ExploreActivity", "Source selected: " + places[i] + " at level " + placesLevels[i]);

                if (src != null && desti != null) {
                    resetInRoute();
                    setRoute(src, desti);
                    refreshFloorDisplay(src.getLevel());

                    // Log the route
                    Log.d("ExploreActivity", "Route set from " + src.getName() + " to " + desti.getName());
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                Log.i("CAMPUS_COMPASS", "Nothing Selected");
            }
        });

        dest.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if (view != null) {
                    ((TextView) adapterView.getChildAt(0)).setTextColor(Color.BLACK);
                }
                desti = getLocation(places[i], placesLevels[i]);
                Log.d("ExploreActivity", "Destination selected: " + places[i] + " at level " + placesLevels[i]);

                if (src != null && desti != null) {
                    resetInRoute();
                    setRoute(src, desti);
                    refreshFloorDisplay(src.getLevel());

                    // Log the route
                    Log.d("ExploreActivity", "Route set from " + src.getName() + " to " + desti.getName());
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                Log.i("CAMPUS_COMPASS", "Nothing Selected");
            }
        });

        // Initialize floor navigation pills (G, 1, 2)
        for (int i = 0; i < pills.length; i++) {
            pills[i] = findViewById(getResources().getIdentifier("pill" + (i + 1), "id", getPackageName()));
            final int index = i;
            pills[i].setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    selectedLevel = index;
                    updateFloorPills(selectedLevel);
                    refreshFloorDisplay(selectedLevel);
                }
            });
        }

        updateFloorPills(0);
    }

    private void refreshFloorDisplay(int level) {
        selectedLevel = level;
        updateFloorPills(level);
        Bundle bundle = new Bundle();
        bundle.putInt("level", level);
        Floor f = new Floor();
        f.setArguments(bundle);
        replaceFragment(f);
    }

    private void updateFloorPills(int activeFloor) {
        for (int j = 0; j < pills.length; j++) {
            if (j == activeFloor) {
                pills[j].setBackgroundResource(R.drawable.selected_pill);
                pills[j].setTextColor(Color.WHITE);
            } else {
                pills[j].setBackgroundResource(R.drawable.pill_tab);
                pills[j].setTextColor(Color.BLACK);
            }
        }
    }

    private void replaceFragment(Fragment f) {
        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        ft.replace(R.id.frame, f);
        ft.commit();
    }

    private void makeLocations() {
        // Ground Floor (Level 0)
        {
            String[] groundFloorPlaces = {"Entrance", "AV Hall", "Placement Cell - 1", "Placement Cell - 2", "Placement Hall"};

            Location node0 = new Location("Entrance Area",
                    new ArrayList<>(Arrays.asList(groundFloorPlaces[0])),
                    new ArrayList<>(Arrays.asList(PlacePosition.left)),
                    R.drawable.entrance, 0, false, 0f, null, null, null, null, null);

            Location node1 = new Location("AV Hall Area",
                    new ArrayList<>(Arrays.asList(groundFloorPlaces[1])),
                    new ArrayList<>(Arrays.asList(PlacePosition.top)),
                    R.drawable.lh101, 0, false, 0f, null, null, null, null, null);

            Location node2 = new Location("Placement Cell 2 Area",
                    new ArrayList<>(Arrays.asList(groundFloorPlaces[3])),
                    new ArrayList<>(Arrays.asList(PlacePosition.topRight)),
                    R.drawable.lh104, 0, false, 0f, null, null, null, null, null);

            Location node3 = new Location("Corridor Junction",
                    new ArrayList<>(),
                    new ArrayList<>(),
                    R.drawable.lh101, 0, false, 0f, null, null, null, null, null);

            Location node4 = new Location("Placement Hall Area",
                    new ArrayList<>(Arrays.asList(groundFloorPlaces[4])),
                    new ArrayList<>(Arrays.asList(PlacePosition.bottom)),
                    R.drawable.lh104, 0, false, 0f, null, null, null, null, null);

            Location node5 = new Location("Main Corridor",
                    new ArrayList<>(Arrays.asList(groundFloorPlaces[2])),
                    new ArrayList<>(Arrays.asList(PlacePosition.left)),
                    R.drawable.lh104, 0, false, 0f, null, null, null, null, null);

            Location stairs1 = new Location("Stairs 1",
                    new ArrayList<>(),
                    new ArrayList<>(),
                    R.drawable.avhall, 0, true, 0f, null, null, null, null, null);

            Location stairs2 = new Location("Stairs 2",
                    new ArrayList<>(),
                    new ArrayList<>(),
                    R.drawable.avhall, 0, true, 0f, null, null, null, null, null);

            makeConnections(node0, node1, node2, node3, node4, node5, stairs1, stairs2);
            LevelPointer.levels[0] = node0;
        }

        // First Floor (Level 1)
        {
            String[] firstFloorPlaces = {"LH101", "LH102", "LH103", "LH104", "Seminar Hall", "Washroom Area", "Staff Room 1", "Staff Room 2"};

            Location node0 = new Location("LH101 Area",
                    new ArrayList<>(Arrays.asList(firstFloorPlaces[0])),
                    new ArrayList<>(Arrays.asList(PlacePosition.bottomLeft)),
                    R.drawable.lh101, 1, false, 0f, null, null, null, null, null);

            Location node1 = new Location("LH102-103 Area",
                    new ArrayList<>(Arrays.asList(firstFloorPlaces[1], firstFloorPlaces[2])),
                    new ArrayList<>(Arrays.asList(PlacePosition.left, PlacePosition.topLeft)),
                    R.drawable.lh104, 1, false, 0f, null, null, null, null, null);

            Location node2 = new Location("LH104 Area",
                    new ArrayList<>(Arrays.asList(firstFloorPlaces[3])),
                    new ArrayList<>(Arrays.asList(PlacePosition.top)),
                    R.drawable.lh104, 1, false, 0f, null, null, null, null, null);

            Location node3 = new Location("Seminar Hall Area",
                    new ArrayList<>(Arrays.asList(firstFloorPlaces[4], firstFloorPlaces[5])),
                    new ArrayList<>(Arrays.asList(PlacePosition.topRight, PlacePosition.right)),
                    R.drawable.lh104, 1, false, 0f, null, null, null, null, null);

            Location node4 = new Location("Staff Room Area",
                    new ArrayList<>(Arrays.asList(firstFloorPlaces[6], firstFloorPlaces[7])),
                    new ArrayList<>(Arrays.asList(PlacePosition.bottomRight, PlacePosition.right)),
                    R.drawable.lh104, 1, false, 0f, null, null, null, null, null);

            Location node5 = new Location("Central Corridor",
                    new ArrayList<>(),
                    new ArrayList<>(),
                    R.drawable.lh104, 1, false, 0f, null, null, null, null, null);

            Location stairs1 = new Location("Stairs 1",
                    new ArrayList<>(),
                    new ArrayList<>(),
                    R.drawable.avhall, 1, true, 0f, null, null, null, null, null);

            Location stairs2 = new Location("Stairs 2",
                    new ArrayList<>(),
                    new ArrayList<>(),
                    R.drawable.avhall, 1, true, 0f, null, null, null, null, null);

            makeConnections(node0, node1, node2, node3, node4, node5, stairs1, stairs2);
            LevelPointer.levels[1] = node0;
        }

        // Second Floor (Level 2)
        {
            String[] secondFloorPlaces = {"Lab 1", "Lab 2", "Lab 3", "LH201", "LH202", "LH203", "LH204", "Staff Room 2F", "Store Room"};

            Location node0 = new Location("Lab Area",
                    new ArrayList<>(Arrays.asList(secondFloorPlaces[0], secondFloorPlaces[1])),
                    new ArrayList<>(Arrays.asList(PlacePosition.bottomLeft, PlacePosition.left)),
                    R.drawable.lh104, 2, false, 0f, null, null, null, null, null);

            Location node1 = new Location("Lab 3 & Store Area",
                    new ArrayList<>(Arrays.asList(secondFloorPlaces[2], secondFloorPlaces[8])),
                    new ArrayList<>(Arrays.asList(PlacePosition.topLeft, PlacePosition.top)),
                    R.drawable.lh101, 2, false, 0f, null, null, null, null, null);

            Location node2 = new Location("LH201 Area",
                    new ArrayList<>(Arrays.asList(secondFloorPlaces[3])),
                    new ArrayList<>(Arrays.asList(PlacePosition.topRight)),
                    R.drawable.lh101, 2, false, 0f, null, null, null, null, null);

            Location node3 = new Location("LH202-204 Area",
                    new ArrayList<>(Arrays.asList(secondFloorPlaces[4], secondFloorPlaces[6])),
                    new ArrayList<>(Arrays.asList(PlacePosition.right, PlacePosition.bottomRight)),
                    R.drawable.lh101, 2, false, 0f, null, null, null, null, null);

            Location node4 = new Location("LH203 & Staff Room Area",
                    new ArrayList<>(Arrays.asList(secondFloorPlaces[5], secondFloorPlaces[7])),
                    new ArrayList<>(Arrays.asList(PlacePosition.bottomLeft, PlacePosition.bottomRight)),
                    R.drawable.lh101, 2, false, 0f, null, null, null, null, null);

            Location node5 = new Location("Central Corridor",
                    new ArrayList<>(),
                    new ArrayList<>(),
                    R.drawable.lh101, 2, false, 0f, null, null, null, null, null);

            Location stairs1 = new Location("Stairs 1",
                    new ArrayList<>(),
                    new ArrayList<>(),
                    R.drawable.avhall, 2, true, 0f, null, null, null, null, null);

            Location stairs2 = new Location("Stairs 2",
                    new ArrayList<>(),
                    new ArrayList<>(),
                    R.drawable.lh101, 2, true, 0f, null, null, null, null, null);

            makeConnections(node0, node1, node2, node3, node4, node5, stairs1, stairs2);
            LevelPointer.levels[2] = node0;
        }

        connectStairsBetweenFloors();
    }

    private void connectStairsBetweenFloors() {
        // Connect Ground to First Floor - Stairs 1
        LevelPointer.levels[0].getStairs().setUp(LevelPointer.levels[1].getStairs());
        LevelPointer.levels[0].getStairs().setUpAngle(-90);
        LevelPointer.levels[1].getStairs().setDown(LevelPointer.levels[0].getStairs());
        LevelPointer.levels[1].getStairs().setDownAngle(90);

        // Connect Ground to First Floor - Stairs 2
        LevelPointer.levels[0].getRight().getStairs().setUp(LevelPointer.levels[1].getRight().getStairs());
        LevelPointer.levels[0].getRight().getStairs().setUpAngle(-90);
        LevelPointer.levels[1].getRight().getStairs().setDown(LevelPointer.levels[0].getRight().getStairs());
        LevelPointer.levels[1].getRight().getStairs().setDownAngle(90);

        // Connect First to Second Floor - Stairs 1
        LevelPointer.levels[1].getStairs().setUp(LevelPointer.levels[2].getStairs());
        LevelPointer.levels[1].getStairs().setUpAngle(-90);
        LevelPointer.levels[2].getStairs().setDown(LevelPointer.levels[1].getStairs());
        LevelPointer.levels[2].getStairs().setDownAngle(90);

        // Connect First to Second Floor - Stairs 2
        LevelPointer.levels[1].getRight().getStairs().setUp(LevelPointer.levels[2].getRight().getStairs());
        LevelPointer.levels[1].getRight().getStairs().setUpAngle(-90);
        LevelPointer.levels[2].getRight().getStairs().setDown(LevelPointer.levels[1].getRight().getStairs());
        LevelPointer.levels[2].getRight().getStairs().setDownAngle(90);
    }

    private void makeConnections(Location node0, Location node1, Location node2, Location node3, Location node4, Location node5, Location stairs1, Location stairs2) {
        node0.setLeft(node1);
        node0.setRight(node5);
        node0.setBack(node3);

        node1.setRight(node0);
        node1.setBack(node2);

        node2.setFront(node1);
        node2.setRight(node3);

        node3.setFront(node0);
        node3.setLeft(node2);
        node3.setRight(node4);

        node4.setLeft(node3);
        node4.setFront(node5);

        node5.setBack(node4);
        node5.setLeft(node0);

        node0.setStairs(stairs1);
        node5.setStairs(stairs2);
        node4.setStairs(stairs2);

        stairs1.setFront(node0);
        stairs2.setFront(node5);
        stairs2.setBack(node4);
    }

    private Location getLocation(String src, int level) {
        Location startNode = LevelPointer.levels[level];
        if (startNode == null) {
            Log.e("ExploreActivity", "No start node for level " + level);
            return null;
        }

        if (startNode.getPlaces().contains(src)) {
            return startNode;
        }

        LinkedList<Location> queue = new LinkedList<>();
        ArrayList<Location> visited = new ArrayList<>();
        queue.add(startNode);
        visited.add(startNode);

        while (!queue.isEmpty()) {
            Location current = queue.poll();

            if (current.getPlaces().contains(src)) {
                return current;
            }

            Location[] connections = {current.getLeft(), current.getRight(),
                    current.getFront(), current.getBack()};

            for (Location connection : connections) {
                if (connection != null && !visited.contains(connection)) {
                    queue.add(connection);
                    visited.add(connection);
                }
            }
        }

        Log.e("ExploreActivity", "Location not found: " + src + " on level " + level);
        return null;
    }

    private void setRoute(Location src, Location desti) {
        if (src == null || desti == null) {
            Log.e("ExploreActivity", "Source or destination is null");
            return;
        }

        completeRoute.clear(); // Clear previous route

        Log.d("ExploreActivity", "Setting route from " + src.getName() + " (Level " + src.getLevel() +
                ") to " + desti.getName() + " (Level " + desti.getLevel() + ")");

        if (src.getLevel() == desti.getLevel()) {
            // Same floor navigation
            findSmallestRoute(src, desti);

            // Reverse the deque to get correct order and add to complete route
            ArrayList<Location> temp = new ArrayList<>();
            smallest.forEach(element -> {
                if (element != null) {
                    element.setInRoute(true);
                    temp.add(0, element); // Add at beginning to reverse
                    Log.d("ExploreActivity", "In route: " + element.getName());
                }
            });
            completeRoute.addAll(temp);

        } else {
            // Multi-floor navigation
            // First: Get route to stairs on source floor
            findSmallestRouteStairs(src);

            ArrayList<Location> temp = new ArrayList<>();
            ArrayList<Location> finalTemp = temp;
            smallest.forEach(element -> {
                if (element != null) {
                    element.setInRoute(true);
                    finalTemp.add(0, element); // Reverse order
                    Log.d("ExploreActivity", "In route to stairs: " + element.getName());
                }
            });
            completeRoute.addAll(temp);

            smallest.clear();
            for (int i = 0; i < 6; i++) smallest.addFirst(null);

            // Second: Traverse stairs between floors
            while (newSrc != null && newSrc.getLevel() != desti.getLevel()) {
                if (newSrc.getLevel() < desti.getLevel()) {
                    newSrc = newSrc.getUp();
                    if (newSrc != null) {
                        newSrc.setInRoute(true);
                        completeRoute.add(newSrc);
                        Log.d("ExploreActivity", "Going up to: " + newSrc.getName());
                    }
                } else {
                    newSrc = newSrc.getDown();
                    if (newSrc != null) {
                        newSrc.setInRoute(true);
                        completeRoute.add(newSrc);
                        Log.d("ExploreActivity", "Going down to: " + newSrc.getName());
                    }
                }
            }

            // Third: Get route from stairs to destination on target floor
            if (newSrc != null) {
                findSmallestRoute(newSrc, desti);
                temp = new ArrayList<>();
                ArrayList<Location> finalTemp1 = temp;
                smallest.forEach(element -> {
                    if (element != null) {
                        element.setInRoute(true);
                        finalTemp1.add(0, element); // Reverse order
                        Log.d("ExploreActivity", "In route from stairs: " + element.getName());
                    }
                });
                completeRoute.addAll(temp);
            }
        }

        Log.d("ExploreActivity", "Complete route size: " + completeRoute.size());
    }

    private void findSmallestRoute(Location src, Location dest) {
        if (src == null || dest == null) return;

        traverse.addLast(src);
        src.setInRoute(true);

        if (src == dest) {
            if (smallest.size() > traverse.size()) {
                smallest.clear();
                traverse.forEach(element -> {
                    smallest.addFirst(element);
                });
            }
            src.setInRoute(false);
            traverse.removeLast();
            return;
        }

        Location[] connections = {src.getLeft(), src.getRight(), src.getBack(), src.getFront()};

        for (Location connection : connections) {
            if (connection != null && !connection.getInRoute()) {
                findSmallestRoute(connection, dest);
            }
        }

        src.setInRoute(false);
        traverse.removeLast();
    }

    private void findSmallestRouteStairs(Location srcc) {
        if (srcc == null) return;

        traverse.addLast(srcc);
        srcc.setInRoute(true);

        if (srcc.getStairs() != null) {
            if (smallest.size() > traverse.size()) {
                smallest.clear();
                traverse.forEach(element -> {
                    smallest.addFirst(element);
                });
                smallest.addFirst(srcc.getStairs());
                newSrc = srcc.getStairs();
            }
            srcc.setInRoute(false);
            traverse.removeLast();
            return;
        }

        Location[] connections = {srcc.getLeft(), srcc.getRight(), srcc.getBack(), srcc.getFront()};

        for (Location connection : connections) {
            if (connection != null && !connection.getInRoute()) {
                findSmallestRouteStairs(connection);
            }
        }

        srcc.setInRoute(false);
        traverse.removeLast();
    }

    private void resetInRoute() {
        int[] arr = {0, 1, 2};
        traverse.clear();
        smallest.clear();
        completeRoute.clear(); // Clear complete route too
        for (int i = 0; i < 6; i++) smallest.addFirst(null);

        for (int level : arr) {
            Location startNode = LevelPointer.levels[level];
            if (startNode == null) continue;

            LinkedList<Location> queue = new LinkedList<>();
            ArrayList<Location> visited = new ArrayList<>();
            queue.add(startNode);
            visited.add(startNode);

            while (!queue.isEmpty()) {
                Location current = queue.poll();
                current.setInRoute(false);

                if (current.getStairs() != null) {
                    current.getStairs().setInRoute(false);
                }

                Location[] connections = {current.getLeft(), current.getRight(),
                        current.getFront(), current.getBack()};

                for (Location connection : connections) {
                    if (connection != null && !visited.contains(connection)) {
                        queue.add(connection);
                        visited.add(connection);
                    }
                }
            }
        }
    }
}