package com.parking.dashboard.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Thrown when a requested parking location is not found.
 */
@ResponseStatus(HttpStatus.NOT_FOUND)
public class ParkingLocationNotFoundException extends RuntimeException {

    public ParkingLocationNotFoundException(Long id) {
        super("Parking location not found with ID: " + id);
    }

    public ParkingLocationNotFoundException(String message) {
        super(message);
    }
}
