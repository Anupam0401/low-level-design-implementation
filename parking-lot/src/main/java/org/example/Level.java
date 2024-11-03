package org.example;

import org.example.vehicle.Vehicle;
import org.example.vehicle.VehicleType;

import java.util.ArrayList;
import java.util.List;

public class Level {
    private final int floor;
    private final List<ParkingSpot> parkingSpots;

    public Level(int floor, int numberOfSpots) {
        this.floor = floor;
        parkingSpots = new ArrayList<>(numberOfSpots);
        int motorcycleSpots = numberOfSpots * 50 / 100;
        int carSpots = numberOfSpots * 40 / 100;

        for (int i = 0; i < motorcycleSpots; i++) {
            parkingSpots.add(new ParkingSpot(i, VehicleType.MOTORCYCLE));
        }
        for (int i = motorcycleSpots; i < motorcycleSpots + carSpots; i++) {
            parkingSpots.add(new ParkingSpot(i, VehicleType.CAR));
        }
        for (int i = motorcycleSpots + carSpots; i < numberOfSpots; i++) {
            parkingSpots.add(new ParkingSpot(i, VehicleType.TRUCK));
        }
    }

    public synchronized boolean parkVehicle(Vehicle vehicle) {
        for (ParkingSpot spot : parkingSpots) {
            if (spot.isAvailable() && spot.getVehicleType() == vehicle.getType()) {
                spot.parkVehicle(vehicle);
                return true;
            }
        }
        return false;
    }

    public synchronized boolean removeVehicle(Vehicle vehicle) {
        for (ParkingSpot spot : parkingSpots) {
            if (!spot.isAvailable() && spot.getParkedVehicle() == vehicle) {
                spot.removeVehicle();
                return true;
            }
        }
        return false;
    }

    public void displayAvailablity() {
        for (ParkingSpot spot : parkingSpots) {
            System.out.println("Spot number: " + spot.getSpotNumber() + " is available: " + spot.isAvailable());
        }
    }

    public List<ParkingSpot> getParkingSpots() {
        return parkingSpots;
    }
}
