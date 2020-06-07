package lab8.Collections;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import java.io.Serializable;

/**
 * Класс с главой SpaceMarine
 *
 * @author Остряков Егор, P3112
 */
public class Chapter implements Serializable {
    private String name; //Поле не может быть null, Строка не может быть пустой
    private String world; //Поле не может быть null

    /**
     * @param name  имя главы
     * @param world название мира
     */
    public Chapter(String name, String world) {
        trySetName(name);
        trySetWorld(world);
    }

    /**
     * @return строковое представление объекта
     */
    @Override
    public String toString() {
        return "chapter{" +
                "name=: '" + name + '\'' +
                ", world: '" + world + '\'' +
                '}';
    }

    /**
     * @param name параметр сеттера имени
     */

    public void trySetName(String name) {
        if (name != null) {
            this.name = name;
        }
    }

    /**
     * @return имя
     */
    public String getName() {
        return name;
    }

    public StringProperty getNameProperty() {
        return new SimpleStringProperty(name);
    }

    /**
     * @param world параметр сеттера названия мира
     */
    public void trySetWorld(String world) {
        if (world != null) {
            this.world = world;
        }
    }

    /**
     * @return мир
     */
    public String getWorld() {
        return world;
    }

    public StringProperty getWorldProperty() {
        return new SimpleStringProperty(world);
    }
}