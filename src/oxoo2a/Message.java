package oxoo2a;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.HashMap;
import java.util.Map;

public class Message {
    public Message () {
        data = new HashMap<>();
    }

    public void set (String key, String value ) {
        data.put(key,value);
    }

    public String get(String key ) {
        return data.get(key);
    }

    public String getJSON () {
        ObjectMapper mapper = new ObjectMapper();
        String dataAsJSON = null;
        try {
            dataAsJSON = mapper.writeValueAsString(data);
        }
        catch (JsonProcessingException e) {
            System.err.printf("Unable to serialize map <%s> to JSON\n",data.toString());
            System.err.println(e.getMessage());
        }
        return dataAsJSON;
    }

    public static Message FromJSON ( String raw ) {
        ObjectMapper mapper = new ObjectMapper();
        Message m = new Message();
        try {
            m.data = mapper.readValue(raw, m.data.getClass());
        }
        catch (Exception e) {
            System.err.printf("Unable to deserialize <%s> to map\n",raw);
            System.err.println(e.getMessage());
        };
        return m;
    }

    public static Message createWelcomeMessage ( String coordinate ) {
        Message m = new Message();
        m.set("type","welcome");
        m.set("position",coordinate);
        return m;
    }

    public static Message createChatMessage ( String sender, String content, boolean isWorldMessage ) {
        Message m = new Message();
        m.set("type","chat");
        m.set("sender",sender);
        m.set("content",content);
        m.set("world", isWorldMessage ? "true" : "false");
        return m;
    }

    private Map<String,String> data;
}
