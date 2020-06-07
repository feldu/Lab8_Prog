package lab8.Collections;

import javafx.beans.property.*;

import java.io.Serializable;
import java.sql.Date;
import java.time.LocalDate;

/**
 * Класс SpaceMarine, объектами которого заполняется коллекция
 *
 * @author Остряков Егор, P3112
 */
public class SpaceMarine implements Serializable {
    public static long idSetter = 1;
    private long id; //Значение поля должно быть больше 0, Значение этого поля должно быть уникальным, Значение этого поля должно генерироваться автоматически
    private String name; //Поле не может быть null, Строка не может быть пустой
    private Coordinates coordinates; //Поле не может быть null
    private java.time.LocalDate creationDate; //Поле не может быть null, Значение этого поля должно генерироваться автоматически
    private Float health; //Поле не может быть null, Значение поля должно быть больше 0
    private long heartCount; //Значение поля должно быть больше 0, Максимальное значение поля: 3
    private Boolean loyal = null; //Поле может быть null
    private MeleeWeapon meleeWeapon; //Поле не может быть null
    private Chapter chapter; //Поле не может быть null
    private String createdByUser;

    /**
     * Конструктор класса
     *
     * @param name        имя
     * @param coordinates координаты
     * @param health      здоровье
     * @param heartCount  количество сердечек
     * @param loyal       показатель лояльности
     * @param meleeWeapon оружие ближнего боя
     * @param chapter     глава
     */
    public SpaceMarine(String name, Coordinates coordinates, Float health, Long heartCount, Boolean loyal, MeleeWeapon meleeWeapon, Chapter chapter) {
        this.creationDate = LocalDate.now();
        setId(idSetter++);
        setName(name);
        setCoordinates(coordinates);
        setHealth(health);
        setHeartCount(heartCount);
        setLoyal(loyal);
        setMeleeWeapon(meleeWeapon);
        setChapter(chapter);
    }

    public SpaceMarine(SpaceMarine spaceMarine) {
        this.creationDate = LocalDate.now();
        setId(idSetter++);
        setName(spaceMarine.name);
        setCoordinates(spaceMarine.coordinates);
        setHealth(spaceMarine.health);
        setHeartCount(spaceMarine.heartCount);
        setLoyal(spaceMarine.loyal);
        setMeleeWeapon(spaceMarine.meleeWeapon);
        setChapter(spaceMarine.chapter);
        setCreatedByUser(spaceMarine.createdByUser);
    }

    public long getId() {
        return id;
    }

    public LongProperty getIdProperty() {
        return new SimpleLongProperty(id);
    }

    public void setId(long id) {
        this.id = id;
    }

    public void setName(String name) {
        if (name != null && !name.isEmpty()) {
            this.name = name;
        }
    }

    public void setCoordinates(Coordinates coordinates) {
        if (coordinates != null) {
            this.coordinates = coordinates;
        }
    }

    public Float getHealth() {
        return health;
    }

    public FloatProperty getHealthProperty() {
        return new SimpleFloatProperty(health);
    }

    public void setHealth(Float health) {
        if (health > 0) {
            this.health = health;
        }
    }

    public void setHeartCount(long heartCount) {
        if (heartCount > 0 && heartCount <= 3) {
            this.heartCount = heartCount;
        }

    }

    public Boolean getLoyal() {
        return loyal;
    }

    public void setLoyal(Boolean loyal) {
        this.loyal = loyal;
    }

    public void setMeleeWeapon(MeleeWeapon meleeWeapon) {
        if (meleeWeapon != null) {
            this.meleeWeapon = meleeWeapon;
        }

    }

    public void setChapter(Chapter chapter) {
        if (chapter != null) {
            this.chapter = chapter;
        }
    }

    public String getName() {
        return name;
    }

    public StringProperty getNameProperty() {
        return new SimpleStringProperty(name);
    }

    public Coordinates getCoordinates() {
        return coordinates;
    }

    public long getHeartCount() {
        return heartCount;
    }

    public LongProperty getHeartCountProperty() {
        return new SimpleLongProperty(heartCount);
    }

    public MeleeWeapon getMeleeWeapon() {
        return meleeWeapon;
    }

    public Chapter getChapter() {
        return chapter;
    }

    public Date getCreationDate() {
        return Date.valueOf(creationDate);
    }

    public void setCreationDate(LocalDate creationDate) {
        this.creationDate = creationDate;
    }

    public void setCreatedByUser(String createdByUser) {
        this.createdByUser = createdByUser;
    }

    public String getCreatedByUser() {
        return createdByUser;
    }

    public StringProperty getCreatedByUserProperty() {
        return new SimpleStringProperty(createdByUser);
    }

    /**
     * @return строковое представление объекта
     */
    @Override
    public String toString() {
        return "SpaceMarine created by user " + createdByUser + " with " +
                "id = " + id +
                "{name: '" + name + '\'' +
                ", " + coordinates +
                ", creation date: " + creationDate +
                ", health: " + health +
                ", heart count: " + heartCount +
                ", loyal: " + loyal +
                ", melee weapon: " + meleeWeapon +
                ", " + chapter +
                "}\n";
    }

}