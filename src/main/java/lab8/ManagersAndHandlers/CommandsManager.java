package lab8.ManagersAndHandlers;

import lab8.Collections.SpaceMarine;
import lab8.Commands.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.util.*;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Класс управляющий выборкой команд
 *
 * @author Остряков Егор, P3112
 */

public class CommandsManager implements Serializable {
    private static final Logger logger = LoggerFactory.getLogger(CommandsManager.class.getName());
    private static HashMap<String, AbstractCommand> commands = new HashMap<>();
    private static CommandsManager commandsManager = new CommandsManager();
    private DatagramChannel serverDatagramChannel;
    private SocketAddress socketAddress;
    ReentrantLock lock = new ReentrantLock();

    /**
     * Конструктор при вызове которого в HashSet commands будут добавлены все доступные команды
     */
    private CommandsManager() {
        commands.put("add", new Add());
        commands.put("add_if_min", new AddIfMin());
        commands.put("clear", new Clear());
        commands.put("help", new Help());
        commands.put("info", new Info());
        commands.put("max_by_health", new MaxByHealth());
        commands.put("print_unique_health", new PrintUniqueHealth());
        commands.put("remove_by_loyal", new RemoveAnyByLoyal());
        commands.put("remove_by_id", new RemoveById());
        commands.put("remove_first", new RemoveFirst());
        commands.put("remove_greater", new RemoveGreater());
        commands.put("show", new Show());
        commands.put("update", new Update());
    }

    /**
     * Запускает выполнение команды
     *
     * @param command         команда, выполненеие которой нужно запустить
     * @param priorityQueue   коллекция, с которой команда взаимодействует
     * @param datagramChannel канал для передачи сообщений клиенту
     * @param socketAddress   адрес порта
     */
    public static void executeCommand(AbstractCommand command, PriorityQueue<SpaceMarine> priorityQueue, DatagramChannel datagramChannel, SocketAddress socketAddress, DataBaseManager dataBaseManager) throws IOException {
        commandsManager.setServerDatagramChannel(datagramChannel);
        commandsManager.setSocketAddress(socketAddress);
        logger.info("Выполнение команды");
        command.execute(priorityQueue, commandsManager, dataBaseManager);
        logger.info("Отправляем клиенту сообщение о завершении чтения");
        datagramChannel.send(ByteBuffer.wrap("I am fucking seriously, it's fucking EOF!!!".getBytes()), socketAddress);
    }


    /**
     * Организует вывод текстового сообщения клиенту
     *
     * @param line строка отслыемая клиенту
     */
    public void printToClient(String line) {
        try {
            ByteBuffer buffer = ByteBuffer.wrap((line.getBytes()));
            commandsManager.getServerDatagramChannel().send(buffer, commandsManager.getSocketAddress());
            logger.info("Отправляем ответ клиенту: {} ", new String(buffer.array()));
        } catch (IOException e) {
            logger.info("Не удалось отправить ответ клиенту {}", e.getMessage());
        }
    }

    /**
     * Отправляем коллекцию клиенту
     * @param priorityQueue коллекция
     */
    public void sendCollectionToClient(PriorityQueue<SpaceMarine> priorityQueue) {
        try {
            commandsManager.getServerDatagramChannel().send(ByteBuffer.wrap("CollAllrt".getBytes()), commandsManager.getSocketAddress());
            try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream(); ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteArrayOutputStream)) {
                objectOutputStream.writeInt(priorityQueue.size());
                for (SpaceMarine spaceMarine : priorityQueue) {
                    objectOutputStream.writeObject(spaceMarine);
                }
                ByteBuffer buffer = ByteBuffer.wrap(byteArrayOutputStream.toByteArray());
                commandsManager.getServerDatagramChannel().send(buffer, commandsManager.getSocketAddress());
                logger.info("Отправляем коллекцию клиенту");

            } catch (Exception e) {
                e.printStackTrace();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Переопределение интерфейса Comparator для сравнения элементов коллекции по полю Health
     */
    private static Comparator<SpaceMarine> idComparator = (o1, o2) -> (int) (o1.getHealth() - o2.getHealth());

    public HashMap<String, AbstractCommand> getCommands() {
        return commands;
    }

    public static Comparator<SpaceMarine> GetIdComparator() {
        return idComparator;
    }

    public void setServerDatagramChannel(DatagramChannel datagramChannel) {
        serverDatagramChannel = datagramChannel;
    }

    public DatagramChannel getServerDatagramChannel() {
        return serverDatagramChannel;
    }

    public void setSocketAddress(SocketAddress socketAddress) {
        this.socketAddress = socketAddress;
    }

    public SocketAddress getSocketAddress() {
        return socketAddress;
    }

    public ReentrantLock getLock() {
        return lock;
    }
}
