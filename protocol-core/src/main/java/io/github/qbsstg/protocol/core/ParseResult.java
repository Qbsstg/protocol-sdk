package io.github.qbsstg.protocol.core;

public final class ParseResult<T extends ProtocolFrame> {

    private final ParseStatus status;
    private final T frame;
    private final String message;
    private final int consumedBytes;

    private ParseResult(ParseStatus status, T frame, String message, int consumedBytes) {
        this.status = status;
        this.frame = frame;
        this.message = message;
        this.consumedBytes = consumedBytes;
    }

    public static <T extends ProtocolFrame> ParseResult<T> success(T frame, int consumedBytes) {
        if (frame == null) {
            throw new IllegalArgumentException("frame must not be null");
        }
        return new ParseResult<T>(ParseStatus.SUCCESS, frame, null, consumedBytes);
    }

    public static <T extends ProtocolFrame> ParseResult<T> incomplete() {
        return new ParseResult<T>(ParseStatus.INCOMPLETE, null, null, 0);
    }

    public static <T extends ProtocolFrame> ParseResult<T> error(String message, int consumedBytes) {
        return new ParseResult<T>(ParseStatus.ERROR, null, message, consumedBytes);
    }

    public ParseStatus getStatus() {
        return status;
    }

    public T getFrame() {
        return frame;
    }

    public String getMessage() {
        return message;
    }

    public int getConsumedBytes() {
        return consumedBytes;
    }

    public boolean isSuccess() {
        return status == ParseStatus.SUCCESS;
    }

    public boolean isIncomplete() {
        return status == ParseStatus.INCOMPLETE;
    }

    public boolean isError() {
        return status == ParseStatus.ERROR;
    }
}
