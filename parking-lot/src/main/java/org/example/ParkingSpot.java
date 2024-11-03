package org.example;

import org.example.vehicle.Vehicle;
import org.example.vehicle.VehicleType;

public class ParkingSpot {
    private final int spotNumber;
    private final VehicleType vehicleType;
    private Vehicle parkedVehicle;

    public ParkingSpot(int spotNumber, VehicleType vehicleType) {
        this.spotNumber = spotNumber;
        this.vehicleType = vehicleType;
    }

    public synchronized boolean isAvailable() {
        return parkedVehicle == null;
    }

    public synchronized void parkVehicle(Vehicle vehicle) {
        if(isAvailable() && vehicle.getType() == vehicleType) {
            parkedVehicle = vehicle;
        }
        else {
            throw new IllegalArgumentException("Invalid vehicle type or spot is already occupied");
        }
    }

    public synchronized void removeVehicle() {
        parkedVehicle = null;
    }

    public int getSpotNumber() {
        return spotNumber;
    }

    public VehicleType getVehicleType() {
        return vehicleType;
    }

    public Vehicle getParkedVehicle() {
        return parkedVehicle;
    }
}
