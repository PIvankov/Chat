package Chat;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Server {
    private static Map<String, Connection> connectionMap = new ConcurrentHashMap<>();

    public static void main(String[] args) {
        try {
            ConsoleHelper.writeMessage("Укажите порт сервера:");
            int serverPort = ConsoleHelper.readInt();
            ServerSocket serverSocket = null;

            try {
                serverSocket = new ServerSocket(serverPort);
                System.out.println("Сервер запущен.");

                while (true) {
                    Socket clientSocket = serverSocket.accept();
                    Handler clientHandler = new Handler(clientSocket);
                    clientHandler.start();
                }
            } finally {
                serverSocket.close();
            }

        } catch (IOException e) {
            System.out.println(e);
        }
    }

    private static class Handler extends Thread {
        private Socket socket;

        public Handler(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {

            ConsoleHelper.writeMessage("Установлено соединение с удаленным адресом - " + socket.getRemoteSocketAddress());
            String userName = "";

            try (Connection connection = new Connection(socket)) {

                userName = serverHandshake(connection);
                sendBroadcastMessage(new Message(MessageType.USER_ADDED, userName));

                notifyUsers(connection, userName);
                serverMainLoop(connection, userName);

            } catch (IOException e) {
                System.out.println(e);
            } catch (ClassNotFoundException e) {
                System.out.println(e);
            } finally {
                if (userName != "") {
                    connectionMap.remove(userName);
                    sendBroadcastMessage(new Message(MessageType.USER_REMOVED, userName));
                }
            }
        }

        private String serverHandshake(Connection connection) throws IOException, ClassNotFoundException {
            connection.send(new Message(MessageType.NAME_REQUEST, "Пожалуйста, укажите Ваше имя"));
            Message message = connection.receive();

            if (message.getType() == MessageType.USER_NAME) {
                String userName = message.getData();
                if (userName != null && !userName.isEmpty() && !connectionMap.containsKey(userName)) {
                    connectionMap.put(userName, connection);
                    connection.send(new Message(MessageType.NAME_ACCEPTED, "Ваше имя принято!"));
                    return userName;
                } else {
                    return serverHandshake(connection);
                }
            } else {
                return serverHandshake(connection);
            }
        }

        private void notifyUsers(Connection connection, String userName) throws IOException {
            for (Map.Entry<String, Connection> m : connectionMap.entrySet()) {
                if (!m.getKey().equals(userName)) {
                    connection.send(new Message(MessageType.USER_ADDED, m.getKey()));
                }
            }
        }

        private void serverMainLoop(Connection connection, String userName) throws IOException, ClassNotFoundException {
            while (true) {
                Message message = connection.receive();

                if (message.getType() == MessageType.TEXT) {
                    sendBroadcastMessage(new Message(MessageType.TEXT, userName + ": " + message.getData()));
                } else {
                    ConsoleHelper.writeMessage("Error");
                }
            }
        }
    }

    public static void sendBroadcastMessage(Message message) {
        for (Map.Entry<String, Connection> m : connectionMap.entrySet()) {
            try {
                m.getValue().send(message);
            } catch (IOException e) {
                System.out.println("Не удалось отправить сообщение :(");
            }
        }
    }
}
