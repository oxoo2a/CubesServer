package oxoo2a;

import java.io.*;
import java.net.Socket;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;

public class ClientConnect {
    public ClientConnect ( Socket client, Map<String,ClientConnect> namedClients, String position ) {
        this.client = client;
        this.namedClients = namedClients;
        this.position = position;
        name = "John Doe" + this.hashCode();
        synchronized (namedClients) {
            namedClients.put(name,this);
        }

        input = null;
        output = null;

        inputThread = new Thread(this::handleInput);
        inputThread.start();
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
                Message m = Message.FromJSON(rawInput);

                Optional<String> messageType = m.get("type");
                if (messageType.isEmpty()) {
                    System.out.printf("There is no type field in message <%s> from %s\n",rawInput,name);
                    break;
                }
                messageType.ifPresent(t -> {
                    if (t.equalsIgnoreCase("hello"))
                        handleHelloMessage(m);
                    else if (t.equalsIgnoreCase("chat"))
                        handleChatMessage(m);
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
        String rawOutput = m.getJSON();
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
