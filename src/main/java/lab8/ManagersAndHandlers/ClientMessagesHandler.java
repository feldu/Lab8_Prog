package lab8.ManagersAndHandlers;

import lab8.Collections.SpaceMarine;
import lab8.Commands.AbstractCommand;
import lab8.Controllers.MainController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.*;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.util.*;

/**
 * Класс клиента для обмена сообщениями с сервером
 *
 * @author Остряков Егор, P3112
 */
public class ClientMessagesHandler extends Thread {
    private static final Logger logger = LoggerFactory.getLogger(ClientMessagesHandler.class.getName());
    MainController mainController;
    private DatagramChannel datagramChannel;
    private SocketAddress socketAddress;
    private String login = "";
    private String password = "";
    private boolean authorized = false;
    private PriorityQueue<SpaceMarine> clientPq = new PriorityQueue<>((o1, o2) -> (int) (o1.getHealth() - o2.getHealth()));


    /**
     * Конструктор, сразу же открывает datagramChannel
     */
    public ClientMessagesHandler(MainController mainController) {
        this.mainController = mainController;
        try {
            logger.debug("Открываем селектор и канал датаграммы, отключаем блокировку");
            datagramChannel = DatagramChannel.open();
            datagramChannel.configureBlocking(false);
        } catch (IOException e) {
            logger.error("Ошибка ввода-вывода" + e.getMessage());
        } catch (Exception e) {
            logger.error("Непредвиденная ошибка" + e.getMessage());
        }
    }

    /**
     * Пытаетеся присоединиться к серверу
     *
     * @param hostname имя сервера
     * @param port     порт
     * @throws IOException IOException
     */
    public void connect(String hostname, int port) throws IOException {
        socketAddress = new InetSocketAddress(hostname, port);
        datagramChannel.connect(socketAddress);
        logger.debug("Устанавливаем соединение с {} по порту {}", hostname, port);
    }

    /**
     * Получает ответ от сервера
     *
     * @return полученное сообщение
     * @throws IOException IOException
     */
    public String receiveAnswer() throws IOException {
        ByteBuffer byteBuffer0 = ByteBuffer.allocate(10000);
        socketAddress = datagramChannel.receive(byteBuffer0);
        String answer = new String(byteBuffer0.array()).trim();
        if (answer.equals("CollAllrt".trim())) {
            ByteBuffer byteBuffer = ByteBuffer.allocate(100000);
            while (new String(byteBuffer.array()).trim().isEmpty()) {
                datagramChannel.receive(byteBuffer);
            }
            try (ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(byteBuffer.array());
                 ObjectInputStream objectInputStream = new ObjectInputStream(byteArrayInputStream)) {
                int size = objectInputStream.readInt();
                SpaceMarine o;
                clientPq.clear();
                for (int i = 0; i < size; i++) {
                    o = (SpaceMarine) objectInputStream.readObject();
                    if (o == null) break;
                    clientPq.add(o);
                }
                logger.debug("Клиентская коллекция обновлена");
            } catch (Exception e) {
                logger.error("Произошла непредвиденная ошибка {}", e.getMessage());
                e.printStackTrace();
            }
            return "collectionReceived";
        } else return decodeAnswer(answer);
    }

    /**
     * Метод для локализации полученных с сервера сообщений
     * @param answer нелокализованный ответ в виде определённой последовательности
     * @return ответ, переведённый на нужную локаль
     */
    private String decodeAnswer(String answer) {
        if (answer.isEmpty()) return "";
        if (answer.contains("I am fucking seriously, it's fucking EOF!!!")) return answer;
        if (answer.contains("suc_login") || answer.contains("suc_register")) return answer;
        System.err.println(answer);
        StringBuilder arg = new StringBuilder();
        String[] strings = answer.trim().split("_");
        if (strings.length >= 3) {
            for (int i = 2; i < strings.length; i++) {
                arg.append(strings[i]);
            }
        }
        String command = strings[0] + "_" + strings[1];
        System.err.println(command);
        String normalAnswer = mainController.getCurrentBundle().getString(command);
        System.err.println(normalAnswer+arg);
        return normalAnswer+arg;
    }

    /**
     * Отправляет команду серверу
     *
     * @param command передаваемая команда
     * @throws IOException IOException
     */
    public void sendCommand(AbstractCommand command) throws IOException {
        if (!command.getName().equals("show"))
        logger.debug("Отправляем команду {}", command.getName());
        ByteBuffer buffer = ByteBuffer.wrap(Objects.requireNonNull(Serialization.SerializeObject(command, login, password)));
        datagramChannel.send(buffer, socketAddress);
        if (command.getClass().getName().contains("Exit")) {
            logger.debug("Введена команда exit, завершаем работу клиента");
            System.exit(0);
        }
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getLogin() {
        return login;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setAuthorized(boolean authorized) {
        this.authorized = authorized;
    }

    public boolean isAuthorized() {
        return authorized;
    }

    public PriorityQueue<SpaceMarine> getClientPq() {
        return clientPq;
    }

}
