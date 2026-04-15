package com.example.loom_group_2.logic;

import com.example.loom_group_2.ui.RouteModel;
import java.util.ArrayList;
import java.util.List;

public class NavigationManager {
    private static NavigationManager instance;
    
    // Planned Trip (The "Next Trip" placeholder)
    private RouteModel plannedRoute;
    private String plannedDestName;
    private String plannedDestCoord;
    private List<String> plannedWaypoints = new ArrayList<>();
    private boolean isPlanned = false;

    // Active Navigation (The trip currently running)
    private RouteModel activeRoute;
    private String activeDestName;
    private String activeDestCoord;
    private List<String> activeWaypoints = new ArrayList<>();
    private boolean isNavigating = false;
    private boolean isTraveling = false;
    private long navigationStartTimeMillis = 0;

    private NavigationManager() {}

    public static synchronized NavigationManager getInstance() {
        if (instance == null) {
            instance = new NavigationManager();
        }
        return instance;
    }

    public void planTrip(RouteModel route, String destinationName, String destinationCoordinate, List<String> waypoints) {
        this.plannedRoute = route;
        this.plannedDestName = destinationName;
        this.plannedDestCoord = destinationCoordinate;
        this.plannedWaypoints = new ArrayList<>(waypoints);
        this.isPlanned = true;
    }

    public void startNavigationFromPlan() {
        if (isPlanned) {
            this.activeRoute = plannedRoute;
            this.activeDestName = plannedDestName;
            this.activeDestCoord = plannedDestCoord;
            this.activeWaypoints = new ArrayList<>(plannedWaypoints);
            this.isNavigating = true;
            this.isTraveling = false;
            this.navigationStartTimeMillis = System.currentTimeMillis();
            
            // Clear plan once it starts
            stopPlan();
        }
    }

    public void startImmediateNavigation(RouteModel route, String destinationName, String destinationCoordinate, List<String> waypoints) {
        this.activeRoute = route;
        this.activeDestName = destinationName;
        this.activeDestCoord = destinationCoordinate;
        this.activeWaypoints = new ArrayList<>(waypoints);
        this.isNavigating = true;
        this.isTraveling = false;
        this.navigationStartTimeMillis = System.currentTimeMillis();
    }

    public void stopNavigation() {
        this.activeRoute = null;
        this.activeDestName = null;
        this.activeDestCoord = null;
        this.activeWaypoints.clear();
        this.isNavigating = false;
        this.isTraveling = false;
        this.navigationStartTimeMillis = 0;
    }

    public void stopPlan() {
        this.plannedRoute = null;
        this.plannedDestName = null;
        this.plannedDestCoord = null;
        this.plannedWaypoints.clear();
        this.isPlanned = false;
    }

    public boolean isPlanned() { return isPlanned; }
    public boolean isNavigating() { return isNavigating; }
    
    public RouteModel getPlannedRoute() { return plannedRoute; }
    public String getPlannedDestName() { return plannedDestName; }
    
    public RouteModel getActiveRoute() { return activeRoute; }
    public String getActiveDestName() { return activeDestName; }
    
    public String getDestinationName() {
        return isNavigating ? activeDestName : (isPlanned ? plannedDestName : null);
    }

    public String getDestinationCoordinate() {
        return isNavigating ? activeDestCoord : (isPlanned ? plannedDestCoord : null);
    }

    public long getNavigationStartTimeMillis() {
        return navigationStartTimeMillis;
    }
    
    public List<String> getWaypoints() {
        return isNavigating ? activeWaypoints : plannedWaypoints;
    }

    public void setTraveling(boolean traveling) {
        this.isTraveling = traveling;
    }

    public boolean isTraveling() {
        return isTraveling;
    }
}
