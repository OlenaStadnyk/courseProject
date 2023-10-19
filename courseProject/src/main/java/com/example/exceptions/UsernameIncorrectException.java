package com.example.exceptions;

public class UsernameIncorrectException extends Exception {

    public UsernameIncorrectException() {
        super("The entered username is incorrect. Please, try again");
    }
}
