package edu.tcu.cs.projectpulse.shared;

/**
 * Unified API response wrapper.
 * Every controller returns Result<T> so the frontend always gets
 * a consistent shape: { flag, code, message, data }.
 */
public record Result<T>(
        boolean flag,
        Integer code,
        String message,
        T data
) {
    public static <T> Result<T> success(T data) {
        return new Result<>(true, StatusCode.SUCCESS, "Success", data);
    }

    public static <T> Result<T> success(String message, T data) {
        return new Result<>(true, StatusCode.SUCCESS, message, data);
    }

    public static <T> Result<T> error(int code, String message) {
        return new Result<>(false, code, message, null);
    }
}
