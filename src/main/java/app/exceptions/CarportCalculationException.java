package app.exceptions;

import app.db.*;
public class CarportCalculationException extends Exception{
    public CarportCalculationException(String message) {
        super(message);
    }
    public CarportCalculationException(String message, Throwable cause) {
        super(message, cause);
    }
}
