package oxoo2a;

import java.io.*;
import java.net.Socket;
import java.util.Collection;
import java.util.Map;

public class ClientConnect {
    public ClientConnect ( Socket client, Map<String,ClientConnect> namedClients ) {
        this.client = client;
        this.namedClients = namedClients;
        name = "John Doe" + this.hashCode();
        synchronized (namedClients) {
            namedClients.put(name,this);
        }

        input = null;
        output = null;

        inputThread = new Thread(this::handleInput);
        inputThread.start();
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
                String messageType = m.get("type");
                if (messageType != null) {
                    if (messageType.equalsIgnoreCase("hello")) {
                        // TODO Maybe it is not the first hello message from client
                        String trueName = m.get("name");
                        if (trueName != null) {
                            synchronized (namedClients) {
                                namedClients.remove(name);
                                namedClients.put(trueName,this); // TODO Name might be in map already
                            }
                            name = trueName;
                        }
                        else
                            System.out.printf("In message <%s> is no name defined\n",rawInput);
                        Message welcome = Message.createWelcomeMessage("0,0,0"); // TODO compute position
                        deliverMessage(welcome);
                    }
                    else if (messageType.equalsIgnoreCase("chat")) {
                        String receiver = m.get("receiver");
                        String content = m.get("content");
                        if ((receiver != null) && (content != null)) {
                            Message answer = Message.createChatMessage(name,content,false);
                            if (receiver.equalsIgnoreCase("world")) {
                                answer.set("world","true");
                                Collection<ClientConnect> clients = namedClients.values();
                                for (ClientConnect c : clients) {
                                    c.deliverMessage(answer);
                                }
                            }
                            else {
                                // TODO unknown receiver
                                ClientConnect destination = namedClients.get(receiver);
                                destination.deliverMessage(answer);
                            }
                        }
                        else
                            System.out.printf("Chat message <%s> has no receiver and/or content\n",rawInput);
                    }
                }
                else
                    System.out.printf("There is no type field in message <%s> from %s\n",rawInput,name);
            } while (true);
            client.close();
            if (namedClients.containsKey(name)) {
                namedClients.remove(name);
            }
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
    private Map<String,ClientConnect> namedClients;
    private Thread inputThread;
    private BufferedReader input;
    private BufferedWriter output;
    private String name;
}
