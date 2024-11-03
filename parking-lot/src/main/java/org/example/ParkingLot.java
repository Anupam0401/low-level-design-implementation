package org.example;

import org.example.vehicle.Vehicle;

import java.util.ArrayList;
import java.util.List;

public class ParkingLot {
    private static ParkingLot instance;
    private final List<Level> levels;

    private ParkingLot() {
        levels = new ArrayList<>();
    }

    public static synchronized ParkingLot getInstance() {
        if(instance == null) {
            instance = new ParkingLot();
        }
        return instance;
    }

    public void addLevel(Level level){
        levels.add(level);
    }

    public boolean parkVehicle(Vehicle vehicle) {
        for(Level level : levels) {
            if(level.parkVehicle(vehicle)) {
                System.out.println("Vehicle parked successfully");
                return true;
            }
        }
        System.out.println("No spot available for vehicle");
        return false;
    }

    public boolean removeVehicle(Vehicle vehicle) {
        for(Level level : levels) {
            if(level.removeVehicle(vehicle)) {
                System.out.println("Vehicle removed successfully");
                return true;
            }
        }
        System.out.println("Vehicle not found in parking lot");
        return false;
    }

    public void displayAvailablity() {
        for(Level level : levels) {
            level.displayAvailablity();
        }
    }
}