package arc.haldun.mylibrary.bug;

import java.io.Serializable;

public class ErrorFile implements Serializable {

    // File identifier
    public static final int MAGIC_NUMBER = 0x52524552;

    // File arguments
    private String date;
    private String userName;
    private int userId;
    private Exception exception;

    public ErrorFile(String date, String userName, int userId, Exception exception) {
        this.date = date;
        this.userName = userName;
        this.userId = userId;
        this.exception = exception;
    }
}
