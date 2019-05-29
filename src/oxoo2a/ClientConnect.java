package oxoo2a;

import java.io.*;
import java.net.Socket;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class ClientConnect {
    public ClientConnect ( Socket client, Map<String,ClientConnect> namedClients, String position ) {
        this.client = client;
        this.namedClients = namedClients;
        this.position = position;
        name = "John Doe - " + this.hashCode();
        synchronized (namedClients) {
            namedClients.put(name,this);
        }

        input = null;
        output = null;

        inputThread = new Thread(this::handleInput);
        inputThread.start();
    }

    public String getPosition () {
        return position;
    }

    private void handleHelloMessage ( Message m ) {
        // TODO Maybe it is not the first hello message from client
        Optional<String> trueName = m.get("name");
        if (trueName.isPresent()) {
            synchronized (namedClients) {
                namedClients.remove(name);
                namedClients.put(trueName.get(),this); // TODO Name might be in map already
            }
            name = trueName.get();
        }
        else
            System.out.printf("In message <%s> is no name defined\n",m.toString());
        Message welcome = Message.createWelcomeMessage(position);
        deliverMessage(welcome);
    }

    private void handleChatMessage ( Message m ) {
        Optional<String> receiver = m.get("receiver");
        Optional<String> content = m.get("content");
        if ((receiver.isPresent()) && (content.isPresent())) {
            Message answer = Message.createChatMessage(name,content.get(),false);
            if (receiver.get().equalsIgnoreCase("world")) {
                answer.set("world","true");
                Collection<ClientConnect> clients = namedClients.values();
                for (ClientConnect c : clients) {
                    c.deliverMessage(answer);
                }
            }
            else {
                ClientConnect destination = namedClients.get(receiver.get());
                if (destination != null)
                    destination.deliverMessage(answer);
                else
                    System.out.printf("Unknown receiver <%s>; ignoring chat message\n",receiver.get());
            }
        }
        else
            System.out.printf("Chat message <%s> has no receiver and/or content\n",m.toString());
    }

    private void handleGetStateMessage ( Message m ) {
        Map<String,String> positions = new HashMap<>();
        for ( ClientConnect c : namedClients.values()) {
            positions.put(c.name,c.getPosition());
        }
        Optional<String> positionsAsJSON = JSONProcessor.serialize(positions);
        if (positionsAsJSON.isEmpty()) {
            System.out.printf("Failed to serialize positions\n");
            return;
        }
        Message state = Message.createStateMessage(positionsAsJSON.get());
        deliverMessage(state);
    }

    private void handleInput () {
        try {
            input = new BufferedReader(new InputStreamReader(client.getInputStream()));
            String rawInput;
            do {
                rawInput = input.readLine();
                if (rawInput == null) {
                    System.out.printf("Client %s left!\n",name);
                    break;
                }
                System.out.printf("Receiving <%s> from %s\n",rawInput,name);
                Optional<Message> mOptional = Message.deserialize(rawInput);
                if (mOptional.isEmpty()) {
                    System.out.printf("Ignoring message <%s> (unable to deserialize)\n",rawInput);
                    continue;
                }
                Message m = mOptional.get();
                Optional<String> messageType = m.get("type");
                if (messageType.isEmpty()) {
                    System.out.printf("There is no type field in message <%s> from %s; ignoring\n",rawInput,name);
                    continue;
                }
                messageType.ifPresent(t -> {
                    if (t.equalsIgnoreCase("hello"))
                        handleHelloMessage(m);
                    else if (t.equalsIgnoreCase("chat"))
                        handleChatMessage(m);
                    else if (t.equalsIgnoreCase("getState"))
                        handleGetStateMessage(m);
                    else
                        System.out.printf("Chat message <%s> has no receiver and/or content\n", m.toString());
                });
            } while (true);
            client.close();
            namedClients.remove(name);
        }
        catch (IOException e) {
            Main.fatal("There was an IOException while receiving data ...",e);
        }

    }

    public synchronized void deliverMessage ( Message m ) {
        if (output == null) {
            try {
                output = new BufferedWriter(new OutputStreamWriter(client.getOutputStream()));
            }
            catch (IOException e) {
                Main.fatal("Unable to open output stream",e);
            }
        }
        Optional<String> rawOutputOptional = m.serialize();
        if (rawOutputOptional.isEmpty()) {
            System.out.printf("Discarding message <%s> (error while serializing)\n",m.toString());
            return;
        }
        String rawOutput = rawOutputOptional.get();
        System.out.printf("Sending <%s> to client %s\n",rawOutput,name);
        try {
            output.write(rawOutput);
            output.newLine();
            output.flush();
        }
        catch (IOException e) {
            Main.fatal("IOException while sending message",e);
        }
    }

    private Socket client;
    private String position;
    private Map<String,ClientConnect> namedClients;
    private Thread inputThread;
    private BufferedReader input;
    private BufferedWriter output;
    private String name;
}
