package ali.abdullahmansour.egypt.Models;

public class ChatMessage
{
    String message,id;
    int hour,minute;

    public ChatMessage() {
    }

    public ChatMessage(String message, String id, int hour, int minute) {
        this.message = message;
        this.id = id;
        this.hour = hour;
        this.minute = minute;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getHour() {
        return hour;
    }

    public void setHour(int hour) {
        this.hour = hour;
    }

    public int getMinute() {
        return minute;
    }

    public void setMinute(int minute) {
        this.minute = minute;
    }
}
