package arc.haldun.mylibrary.bug;

import java.io.Serializable;

@SuppressWarnings("unused")
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

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public Exception getException() {
        return exception;
    }

    public void setException(Exception exception) {
        this.exception = exception;
    }
}
