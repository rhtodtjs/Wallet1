package www.seekerslab.com.seekeschat;

import com.google.firebase.database.IgnoreExtraProperties;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

//
@IgnoreExtraProperties
public class ChatModel implements  Serializable{

    public Map<String, Boolean> users = new HashMap<>(); //채팅방 유저들
    public Map<String, Comment> comments = new HashMap<>();

    public ChatModel(){

    }
    public ChatModel( Map<String, Boolean> users, Map<String, Comment> comments){
        this.users = users;
        this.comments = comments;
    }


    public Map<String, Boolean> getUsers() {
        return users;
    }

    public Map<String, Comment> getComments() {
        return comments;
    }

    public static class Comment implements Serializable{
        String uid;
        String message;
        String timeStamp;
        Map<String, Object> readUsers = new HashMap<>();

        public Comment(){

        }

        public Comment(String uid, String message, String timeStamp, Map<String, Object> readUsers) {
            this.uid = uid;
            this.message = message;
            this.timeStamp = timeStamp;
            this.readUsers = readUsers;
        }

        public Map<String, Object> getReadUsers() {
            return readUsers;
        }

        public String getUid() {
            return uid;
        }

        public String getMessage() {
            return message;
        }

        public String getTimeStamp(){
            return timeStamp;
        }
    }
}
