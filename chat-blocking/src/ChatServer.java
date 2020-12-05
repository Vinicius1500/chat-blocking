import java.io.*;
import java.net.ServerSocket;
import java.net.SocketException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;


public class ChatServer {
    
    public static final int PORT = 4000;

   
    private ServerSocket serverSocket;

    private final List<ClientSocket> clientSocketList;

    public ChatServer() {
        clientSocketList = new LinkedList<>();
    }

    public static void main(String[] args) {
        final ChatServer server = new ChatServer();
        try {
            server.start();
        } catch (IOException e) {
            System.err.println("Erro ao iniciar servidor: " + e.getMessage());
        }
    }

    private void start() throws IOException {
        serverSocket = new ServerSocket(PORT);
        System.out.println(
                "Servidor de chat bloqueante iniciado no endereço " + serverSocket.getInetAddress().getHostAddress() +
                " e porta " + PORT);

        clientConnectionLoop();
    }
    
    private void clientConnectionLoop() throws IOException {
        try {
            while (true) {
                System.out.println("Aguardando conexão de novo cliente");
                
                final ClientSocket clientSocket;
                try {
                    clientSocket = new ClientSocket(serverSocket.accept());
                    System.out.println("Cliente " + clientSocket.getRemoteSocketAddress() + " conectado");
                }catch(SocketException e){
                    System.err.println("Erro ao aceitar conexão do cliente. O servidor possivelmente está sobrecarregado:");
                    System.err.println(e.getMessage());
                    continue;
                }
             
                try {
                    new Thread(() -> clientMessageLoop(clientSocket)).start();
                    clientSocketList.add(clientSocket);
                }catch(OutOfMemoryError ex){
                    System.err.println(
                            "Não foi possível criar thread para novo cliente. O servidor possivelmente está sobrecarregdo. Conexão será fechada: ");
                    System.err.println(ex.getMessage());
                    clientSocket.close();
                }
            }
        } finally{
          
            stop();
        }
    }

    private void clientMessageLoop(final ClientSocket clientSocket){
        try {
            String msg;
            while((msg = clientSocket.getMessage()) != null){
                System.out.println("Mensagem recebida do cliente "+ clientSocket.getRemoteSocketAddress() +": " + msg);
                if("sair".equalsIgnoreCase(msg)){
                    return;
                }

                sendMsgToAll(clientSocket, msg);
            }
        } finally {
            clientSocket.close();
        }
    }
    
    private void sendMsgToAll(final ClientSocket sender, final String msg) {
        final Iterator<ClientSocket> iterator = clientSocketList.iterator();
        int count = 0;
        
        
        while (iterator.hasNext()) {
            
            final ClientSocket client = iterator.next();
           
            if (!client.equals(sender)) {
                if(client.sendMsg(msg))
                    count++;
                else iterator.remove();
            }
        }
        System.out.println("Mensagem encaminhada para " + count + " clientes");
    }

    private void stop()  {
        try {
            System.out.println("Finalizando servidor");
            serverSocket.close();
        } catch (IOException e) {
            System.err.println("Erro ao fechar socket do servidor: " + e.getMessage());
        }
    }
}