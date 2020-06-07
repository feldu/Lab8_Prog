package lab8.Commands;

import lab8.Collections.*;
import lab8.ManagersAndHandlers.CommandsManager;
import lab8.ManagersAndHandlers.DataBaseManager;
import lab8.Server;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.PriorityQueue;

/**
 * Абстрактный класс, от которого наследуются все команды
 *
 * @author Остряков Егор, P3112
 */
public abstract class AbstractCommand implements Serializable {
    protected static final Logger logger = LoggerFactory.getLogger(Server.class.getName());
    protected String name;
    protected String help;
    protected String args;
    protected boolean needObjectToExecute = false;
    protected SpaceMarine spaceMarine = null;

    /**
     * Метод выполнения команды
     *
     * @param priorityQueue   коллекция, с которой работает пользователь
     * @param commandsManager объект класса CommandsManager
     * @param dataBaseManager объект класса DataBaseManager
     */
    public abstract void execute(PriorityQueue<SpaceMarine> priorityQueue, CommandsManager commandsManager, DataBaseManager dataBaseManager);

    public String getName() {
        return name;
    }

    public String getHelp() {
        return help;
    }

    public String getArgs() {
        return args;
    }

    public void setArgs(String args) {
        this.args = args;
    }

    public void setSpaceMarine(SpaceMarine spaceMarine) {
        this.spaceMarine = spaceMarine;
    }

}
