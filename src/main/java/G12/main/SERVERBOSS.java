package G12.main;

import org.jspace.*;

public class SERVERBOSS {
    public static void main(String[] args) {
        try {

            // Create a repository
            SpaceRepository repository = new SpaceRepository();

            // Create a local space for the chat messages
            SequentialSpace lobby = new SequentialSpace();

            // Add the space to the repository
            repository.add("lobby",lobby);

            // Set the URI of the chat space
            String uri = "tcp://127.0.0.1:9001/lobby?keep";

            // Open a gate
            repository.addGate("tcp://127.0.0.1:9001/?keep");
            System.out.println("Opening repository gate at " + uri + "...");

            // This space room identifiers to port numbers
            SequentialSpace rooms = new SequentialSpace();
            new Thread(()->{
                Object[] request;
                try {
                    request = lobby.get(new ActualField("remove"), new FormalField(String.class));
                    System.out.println("Removing room " + request[1] + "...");
                    rooms.get(new ActualField(request[1]));
                    repository.remove(request[1].toString());

                } catch (InterruptedException e) {

                }
            }).start();
            // Keep serving requests to enter chatrooms
            while (true) {


                String roomURI;

                while (true) {
                    // Read request
                    Object[] request = lobby.get(new FormalField(String.class), new FormalField(String.class));
                    String roomID = (String) request[1];
                    switch ((String) request[0]) {
                        case "remove":
                            System.out.println("Removing room " + roomID + "...");
                            rooms.get(new ActualField(roomID));
                            repository.remove(roomID);
                            break;
                        case "enter":
                            System.out.println("requesting to enter " + roomID + "...");

                            // If room exists just prepare the response with the corresponding URI
                            Object[] the_room = rooms.queryp(new ActualField(roomID));
                            if (the_room != null) {
                                roomURI = "tcp://127.0.0.1:9001/" + the_room[0] + "?keep";
                            }
                            // If the room does not exist, create the room and launch a room handler
                            else {
                                System.out.println("Creating room " + roomID);
                                roomURI = "tcp://127.0.0.1:9001/" + roomID + "?keep";
                                System.out.println("Setting up chat space " + roomURI + "...");
                                Space chat = new SequentialSpace();

                                // Add the space to the repository
                                repository.add(roomID, chat);
                                rooms.put(roomID);
                            }

                            // Sending response back to the chat client
                            System.out.println("Telling to go for room " + roomID + " at " + roomURI + "...");
                            lobby.put("roomURI", roomID, roomURI);
                            break;
                        default:
                            break;
                    }

                }


            }

        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
