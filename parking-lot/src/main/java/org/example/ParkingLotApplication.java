package org.example;

import org.example.vehicle.Car;
import org.example.vehicle.Motorcycle;
import org.example.vehicle.Truck;
import org.example.vehicle.Vehicle;

public class ParkingLotApplication {
    public static void main(String[] args) {
        ParkingLot parkingLot = ParkingLot.getInstance();
        parkingLot.addLevel(new Level(0, 100));
        parkingLot.addLevel(new Level(1, 50));

        Vehicle car = new Car("ABC123");
        Vehicle truck = new Truck("XYZ456");
        Vehicle motorcycle = new Motorcycle("DEF789");

        // Park vehicles
        System.out.println(parkingLot.enquireAvailabilityForVehicle(car));
        parkingLot.parkVehicle(car);
        parkingLot.parkVehicle(truck);
        System.out.println(parkingLot.enquireAvailabilityForVehicle(motorcycle));
        parkingLot.parkVehicle(motorcycle);

        parkingLot.removeVehicle(motorcycle);

        System.out.println(parkingLot.getParkingSpot(car));
        System.out.println(parkingLot.getParkingSpot(truck));
        System.out.println(parkingLot.getParkingSpot(motorcycle));
    }
}
