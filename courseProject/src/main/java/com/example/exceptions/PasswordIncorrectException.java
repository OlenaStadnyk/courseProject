package com.example.exceptions;

public class PasswordIncorrectException extends Exception {

    public PasswordIncorrectException() {
        super("Your password is wrong. Please, re-enter it");
    }
}
