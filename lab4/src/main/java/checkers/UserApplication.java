package checkers;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.Scanner;

public class UserApplication {

    private static int initGame() {
        System.out.println("Hello! Welcome to Trylma!");
        Scanner scanner = new Scanner(System.in);
        while(true) {
            try {
                System.out.println("Enter number of players: 2, 3, 4 or 6.");
                int number = Integer.parseInt(scanner.nextLine());
                if (number != 2 && number != 3 && number != 4 && number != 6) {
                    System.out.println("This number is wrong.");
                } else {
                    return number;
                }
            } catch (NumberFormatException e) {
                System.out.println("Input is not a number.");
            }
        }
    }

    public static void main(String[] args) {
        int numberOfPlayers = initGame();
        
         new Thread(() -> {
            try {
                ServerSocket serverSocket = new ServerSocket(8000);
                Server server = new Server(serverSocket);
                server.startServer();
            } catch (IOException e) {
                System.err.println("Error starting the server: " + e.getMessage());
            }
        }).start();


        ArrayList<Thread> threads = new ArrayList<>(numberOfPlayers);
        for (int i = 0; i < numberOfPlayers; i++) {
            Thread clientThread = getThread(i, numberOfPlayers);
            threads.add(clientThread);
            clientThread.start();
        }

        for (final Thread thread : threads) {
            try {
                thread.join();
            } catch (InterruptedException e) {
                System.out.println("Thread error.");
            }
        }
    }

    private static Thread getThread(int i, int numberOfPlayers) {
        final int clientNumber = i + 1;
        final Runnable runClient = () -> {
            try {
                System.out.println("Starting client " + clientNumber + "...");
                Client client = new Client("localhost", 8000);
                if (clientNumber == 1) {
                    client.sendNumberOfPlayers(numberOfPlayers);
                }
                client.launch();
            } catch (IOException e) {
                System.out.println("Connection failed.");
            }
        };
        return new Thread(runClient);
    }
}
