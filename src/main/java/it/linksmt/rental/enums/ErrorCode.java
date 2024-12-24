package it.linksmt.rental.enums;

import lombok.Getter;

@Getter
public enum ErrorCode {


    INVALID_CREDENTIALS(101, "Invalid username or password"),
    INVALID_TOKEN(102, "Token is invalid or expired"),
    UNAUTHORIZED_ACCESS(103, "You don't have permission to perform this action"),


    USER_NOT_FOUND(201, "User not found"),
    USER_ALREADY_EXISTS(202, "User already exists with this username/email"),
    USER_NOT_ELIGIBLE(203, "User is not eligible for this action"),


    VEHICLE_NOT_FOUND(301, "Vehicle not found"),
    VEHICLE_NOT_AVAILABLE(302, "Vehicle is already booked for these dates"),
    BAD_VEHICLE_DETAILS(303, "Bad vehicle details"),

    RESERVATION_NOT_FOUND(500, "Reservation not found"),
    RESERVATION_IS_CANCELLED_OR_ONGOING(501, "Reservation is cancelled or ongoing"),
    BAD_RESERVATION_DETAILS(502, "Bad reservation Details"),

    VALIDATION_ERROR(401, "Validation failed"),
    INTERNAL_SERVER_ERROR(501, "Internal server error"),
    BAD_REQUEST(402, "Invalid request");

    private final int code;
    private final String message;


    ErrorCode(int code, String message) {
        this.code = code;
        this.message = message;
    }


}