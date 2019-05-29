package oxoo2a;

import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class MessageTest {

    @Test
    void getJSON() {
        Message m = new Message();
        m.set("name","fun");
        m.set("enjoy","life");
        Optional<String> j = m.serialize();
        assertTrue(j.isPresent());
        assertEquals(j.get(),"{\"name\":\"fun\",\"enjoy\":\"life\"}");
    }

    @Test
    void fromJSON() {
        String j = "{\"a\":\"b\",\"c\":\"d\"}";
        Optional<Message> mOptional = Message.deserialize(j);
        assertTrue(mOptional.isPresent());
        Message m = mOptional.get();
        Optional<String> jjOptional = m.serialize();
        assertTrue(jjOptional.isPresent());
        String jj = jjOptional.get();
        assertEquals(j,jj);
    }
}