package uk.ac.bristol.exception;

public class SpExceptions extends RuntimeException {
    public SpExceptions(String message) {
        super(message);
    }

    public static class GetMethodException extends SpExceptions {
        public GetMethodException() {
            super("Get Method Exception.");
        }

        public GetMethodException(String message) {
            super(message);
        }
    }

    public static class PostMethodException extends SpExceptions {
        public PostMethodException() {
            super("Post Method Exception.");
        }

        public PostMethodException(String message) {
            super(message);
        }
    }

    public static class PutMethodException extends SpExceptions {
        public PutMethodException() {
            super("Put Method Exception.");
        }

        public PutMethodException(String message) {
            super(message);
        }
    }

    public static class DeleteMethodException extends SpExceptions {
        public DeleteMethodException() {
            super("Delete Method Exception.");
        }

        public DeleteMethodException(String message) {
            super(message);
        }
    }

    public static class SystemException extends SpExceptions {
        public SystemException() {
            super("System Exception.");
        }

        public SystemException(String message) {
            super(message);
        }
    }

    public static class BusinessException extends SpExceptions {
        public BusinessException() {
            super("Business Exception.");
        }

        public BusinessException(String message) {
            super(message);
        }
    }

    public static class BadRequestException extends SpExceptions {
        public BadRequestException() {
            super("400 bad request.");
        }

        public BadRequestException(String message) {
            super(message);
        }
    }

    public static class UnauthorisedException extends SpExceptions {
        public UnauthorisedException() {
            super("401 unauthorized.");
        }

        public UnauthorisedException(String message) {
            super(message);
        }
    }

    public static class ForbiddenException extends SpExceptions {
        public ForbiddenException() {
            super("403 forbidden.");
        }

        public ForbiddenException(String message) {
            super(message);
        }
    }

    public static class NotFoundException extends SpExceptions {
        public NotFoundException() {
            super("404 not found.");
        }

        public NotFoundException(String message) {
            super(message);
        }
    }
}
