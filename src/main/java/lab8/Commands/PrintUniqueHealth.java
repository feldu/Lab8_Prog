package lab8.Commands;

import lab8.Collections.SpaceMarine;
import lab8.ManagersAndHandlers.CommandsManager;
import lab8.ManagersAndHandlers.DataBaseManager;

import java.util.HashSet;
import java.util.PriorityQueue;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Класс команды print_unique_health
 *
 * @author Остряков Егор, P3112
 */
public class PrintUniqueHealth extends AbstractCommand {
    public PrintUniqueHealth() {
        name = "print unique health";
        help = " выводит уникальные значения поля health";
    }

    /**
     * Выводит уникальные значения здоровья
     *
     * @param priorityQueue   коллекция, с которой работает пользователь
     * @param commandsManager объект класса CommandsManager
     * @param dataBaseManager объект класса DataBaseManager
     */
    @Override
    public void execute(PriorityQueue<SpaceMarine> priorityQueue, CommandsManager commandsManager, DataBaseManager dataBaseManager) {
        commandsManager.getLock().lock();
        try {
            if (priorityQueue.size() > 0) {
                HashSet<Float> healthSet = new HashSet<>(priorityQueue.size());
                PriorityQueue<Float> priorityNonUnique = new PriorityQueue<>(priorityQueue.size());
                priorityQueue.forEach(spaceMarine -> {
                    healthSet.add(spaceMarine.getHealth());
                    priorityNonUnique.add(spaceMarine.getHealth());
                });
                priorityNonUnique.removeIf(health -> {
                    if (healthSet.contains(health)) {
                        healthSet.remove(health);
                        return true;
                    }
                    return false;
                });
                String[] answer = new String[]{""};
                priorityQueue.forEach(spaceMarine -> {
                    if (!priorityNonUnique.contains(spaceMarine.getHealth()))
                        answer[0] += spaceMarine.getHealth() + " ";
                });
                commandsManager.printToClient("suc_PQH" + "_" + answer[0]);
            } else commandsManager.printToClient("err_empty");
        } catch (Exception e) {
            logger.error("Непредвиденная ошибка");
        } finally {
            commandsManager.getLock().unlock();
        }
    }
}
