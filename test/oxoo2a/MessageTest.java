package oxoo2a;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MessageTest {

    @Test
    void getJSON() {
        Message m = new Message();
        m.set("name","fun");
        m.set("enjoy","life");
        String j = m.getJSON();
        assertNotNull(j);
        assertEquals(j,"{\"name\":\"fun\",\"enjoy\":\"life\"}");
    }

    @Test
    void fromJSON() {
        String j = "{\"a\":\"b\",\"c\":\"d\"}";
        Message m = Message.FromJSON(j);
        String jj = m.getJSON();
        assertEquals(j,jj);
    }
}