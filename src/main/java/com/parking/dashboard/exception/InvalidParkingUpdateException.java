package com.parking.dashboard.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Thrown when a parking update violates business rules
 * (e.g., occupancy > capacity, negative occupancy).
 */
@ResponseStatus(HttpStatus.BAD_REQUEST)
public class InvalidParkingUpdateException extends RuntimeException {

    public InvalidParkingUpdateException(String message) {
        super(message);
    }
}
