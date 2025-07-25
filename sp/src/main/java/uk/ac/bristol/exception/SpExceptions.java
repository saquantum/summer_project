package uk.ac.bristol.exception;

public class SpExceptions extends RuntimeException {

    public SpExceptions(String message) {
        super(message);
    }

    public SpExceptions(String message, Throwable cause) {
        super(message, cause);
    }

    public static class GetMethodException extends SpExceptions {
        public GetMethodException(String message) {
            super(message);
        }

        public GetMethodException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    public static class PostMethodException extends SpExceptions {
        public PostMethodException(String message) {
            super(message);
        }

        public PostMethodException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    public static class PutMethodException extends SpExceptions {
        public PutMethodException(String message) {
            super(message);
        }

        public PutMethodException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    public static class DeleteMethodException extends SpExceptions {
        public DeleteMethodException(String message) {
            super(message);
        }

        public DeleteMethodException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    public static class SystemException extends SpExceptions {
        public SystemException(String message) {
            super(message);
        }

        public SystemException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    public static class BusinessException extends SpExceptions {
        public BusinessException(String message) {
            super(message);
        }

        public BusinessException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    public static class BadRequestException extends SpExceptions {
        public BadRequestException(String message) {
            super(message);
        }

        public BadRequestException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    public static class UnauthorisedException extends SpExceptions {
        public UnauthorisedException(String message) {
            super(message);
        }

        public UnauthorisedException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    public static class ForbiddenException extends SpExceptions {
        public ForbiddenException(String message) {
            super(message);
        }

        public ForbiddenException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    public static class NotFoundException extends SpExceptions {
        public NotFoundException(String message) {
            super(message);
        }

        public NotFoundException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
