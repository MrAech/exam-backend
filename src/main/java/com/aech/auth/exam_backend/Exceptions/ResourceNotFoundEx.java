package com.aech.auth.exam_backend.Exceptions;

public class ResourceNotFoundEx extends RuntimeException {

  public ResourceNotFoundEx(String message) {
    super(message);
  }

  public ResourceNotFoundEx() {

    // Not talking about George
    super("Resource not found");
  }
}

// he never dies
