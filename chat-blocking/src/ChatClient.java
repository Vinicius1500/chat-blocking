
import java.io.IOException;
import java.net.Socket;
import java.util.Scanner;


public class ChatClient implements Runnable {
    
    public static final String SERVER_ADDRESS = "127.0.0.1";
    private final Scanner scanner;
    private ClientSocket clientSocket;
    public static void main(String[] args) {
        try {
            ChatClient client = new ChatClient();
            client.start();
        } catch (IOException e) {
            System.out.println("Erro ao conectar ao servidor: " + e.getMessage());
        }
    }
    
    
    public ChatClient(){
        scanner = new Scanner(System.in);
    }

    private void start() throws IOException {
        final Socket socket = new Socket(SERVER_ADDRESS, ChatServer.PORT);
        clientSocket = new ClientSocket(socket);
        System.out.println(
            "Cliente conectado ao servidor no endereço " + SERVER_ADDRESS +
            " e porta " + ChatServer.PORT);

        new Thread(this).start();
        messageLoop();
    }

    private void messageLoop() {
        String msg;
        do {
            System.out.print("Digite uma msg (ou 'sair' para encerrar): ");
            msg = scanner.nextLine();
            clientSocket.sendMsg(msg);
        } while(!"sair".equalsIgnoreCase(msg));
        clientSocket.close();
    }

    @Override
    public void run() {
        String msg;
        while((msg = clientSocket.getMessage())!=null) {
            System.out.println("Servidor diz: " + msg);
        }
    }
}