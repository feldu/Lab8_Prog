package lab8;

import lab8.Collections.SpaceMarine;
import lab8.ManagersAndHandlers.CommandsManager;
import lab8.ManagersAndHandlers.DataBaseManager;
import lab8.ManagersAndHandlers.ServerMessagesHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.time.LocalDate;
import java.util.PriorityQueue;

/**
 * Класс сервера Server
 *
 * @author Остряков Егор, P3112
 */
public class Server implements Runnable {
    private static final Logger logger = LoggerFactory.getLogger(Server.class.getName());
    private DatagramChannel datagramChannel;
    private SocketAddress socketAddress;
    private static java.time.LocalDate creationDate;
    private static int port = 1488;
    private DataBaseManager dataBaseManager;
    private PriorityQueue<SpaceMarine> priorityQueue = new PriorityQueue<>(CommandsManager.GetIdComparator());

    public Server(DataBaseManager dataBaseManager) {
        this.dataBaseManager = dataBaseManager;
    }


    /**
     * Ну это main()...
     *
     * @param args аргументы командной строки
     */
    public static void main(String[] args) {
        creationDate = LocalDate.now();
        Server server = new Server(new DataBaseManager());
        logger.info("Запускаем работу сервера по порту " + port);
        new Thread(server).start();
    }


    /**
     * Получает сообщение от клиента
     *
     * @param priorityQueue коллекция, с которой работаем
     * @throws IOException IOException
     */
    private void receive(PriorityQueue<SpaceMarine> priorityQueue) throws IOException {
        ByteBuffer byteBuffer = ByteBuffer.allocate(2000);
        byteBuffer.clear();
        socketAddress = datagramChannel.receive(byteBuffer);
        byteBuffer.flip();
        if (socketAddress != null && !new String(byteBuffer.array()).trim().isEmpty()) {
            ServerMessagesHandler messagesHandler = new ServerMessagesHandler(datagramChannel, socketAddress, dataBaseManager, priorityQueue, byteBuffer);
            messagesHandler.start();
        }
    }

    /**
     * Ну это run()...
     */
    @Override
    public void run() {
        dataBaseManager.updateCollectionFromDataBase(priorityQueue);
        socketAddress = new InetSocketAddress(port);
        try {
            datagramChannel = DatagramChannel.open();
            datagramChannel.bind(socketAddress);
            datagramChannel.configureBlocking(false);
            logger.info("Канал открыт и готов для приёма сообщений");
            logger.info("Попытка авторизовать пользователя");
            while (true) receive(priorityQueue);
        } catch (IOException e) {
            logger.error("Ошибка соединения");
        }
    }

    /**
     * @return время создания коллекции
     */
    public static LocalDate GetCreationDate() {
        return creationDate;
    }
}
