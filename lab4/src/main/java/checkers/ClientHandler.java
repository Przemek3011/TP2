package checkers;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.ArrayList;

public class ClientHandler implements Runnable {

    public static ArrayList<ClientHandler> clientHandlers = new ArrayList<>();
    private Socket socket;
    private BufferedReader bufferedReader;
    private BufferedWriter bufferedWriter;
    private String clientUsername;
    private static int clientCounter = 0;


    public ClientHandler(Socket socket){
        try {
            this.socket = socket;
            this.bufferedWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            this.bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            synchronized (ClientHandler.class) {
                clientCounter++;
                this.clientUsername = "Client " + clientCounter;
                broadcastMessage("SERVER: " + clientUsername + " has entered the game");
                clientHandlers.add(this);
            }

            bufferedWriter.write("SERVER: Connection established. Welcome, " + clientUsername + "!");
            bufferedWriter.newLine();
            bufferedWriter.flush();
        } catch (IOException e) {
            closeEverything(socket, bufferedReader, bufferedWriter);
        }
    }

    @Override
    public void run() {
        String messageFromClient;

        while (socket.isConnected()) {
            try {
                messageFromClient = bufferedReader.readLine(); 
                if(isValidTwoNumbers(messageFromClient)){
                broadcastMessage("is moving: " +messageFromClient);}
                
            } catch (IOException e) {
                closeEverything(socket, bufferedReader, bufferedWriter);
                break;
            }
        }
    }

    public void broadcastMessage(String messageToSend) {
        synchronized (ClientHandler.class) {
            for (ClientHandler clientHandler : clientHandlers) {
                try {
                    clientHandler.bufferedWriter.write(clientUsername + " : " + messageToSend);
                    clientHandler.bufferedWriter.newLine();
                    clientHandler.bufferedWriter.flush();
                } catch (IOException e) {
                    // Handle disconnections
                    clientHandler.closeEverything(clientHandler.socket, clientHandler.bufferedReader, clientHandler.bufferedWriter);
                }
            }
        }
    }

    public void removeClientHandler(){
        clientHandlers.remove(this);
        broadcastMessage("SERVER " + clientUsername + " has left");
    }

    public void closeEverything(Socket socket, BufferedReader bufferedReader, BufferedWriter bufferedWriter){
        removeClientHandler();
        try{
            if(bufferedReader != null){
                bufferedReader.close();
            }
            if(bufferedWriter != null){
                bufferedWriter.close();
            }
            if(socket != null){
                socket.close();
            }
        } catch (IOException e){
            e.printStackTrace();
        }
    }


    public boolean isValidTwoNumbers(String messageFromClient) {
        
        String[] parts = messageFromClient.split(" ");
    
        
        if (parts.length != 2) {
            return false;
        }
    
        
        try {
            Integer.parseInt(parts[0]); 
            Integer.parseInt(parts[1]);
        } catch (NumberFormatException e) {
            return false; 
        }
    
        
        return true;
    }
}
