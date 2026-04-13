package edu.tcu.cs.projectpulse.shared;

public interface StatusCode {
    int SUCCESS             = 200;
    int INVALID_ARGUMENT    = 400;
    int UNAUTHORIZED        = 401;
    int FORBIDDEN           = 403;
    int NOT_FOUND           = 404;
    int INTERNAL_SERVER_ERROR = 500;
}
