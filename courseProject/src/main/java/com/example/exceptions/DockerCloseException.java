package com.example.exceptions;

import java.io.IOException;

public class DockerCloseException extends IOException {

    public DockerCloseException(String message) {
        super(message);
    }

    public DockerCloseException(String message, Throwable cause) {
        super(message, cause);
    }
}
