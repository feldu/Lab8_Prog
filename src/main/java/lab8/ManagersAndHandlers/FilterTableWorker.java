package lab8.ManagersAndHandlers;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.util.Duration;
import lab8.Collections.MeleeWeapon;
import lab8.Collections.SpaceMarine;
import lab8.Commands.Show;
import org.google.jhsheets.filtered.FilteredTableView;
import org.google.jhsheets.filtered.operators.*;
import org.google.jhsheets.filtered.tablecolumn.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Класс для работы пользователя с таблицей
 *
 * @author Остряков Егор, P3112
 */
public class FilterTableWorker {
    private static final Logger logger = LoggerFactory.getLogger(FilterTableWorker.class.getName());
    private ClientMessagesHandler updateCollectionClientMessagesHandler;
    private ObservableList<SpaceMarine> spaceMarinesData = FXCollections.observableArrayList();
    private VisualisationWorker visualisationWorker;

    @FXML
    private FilteredTableView<SpaceMarine> tableMarines;
    @FXML
    private FilterableLongTableColumn<SpaceMarine, Long> idColumn;
    @FXML
    private FilterableStringTableColumn<SpaceMarine, String> nameColumn;
    @FXML
    private FilterableIntegerTableColumn<SpaceMarine, Integer> coordinatesXColumn;
    @FXML
    private FilterableLongTableColumn<SpaceMarine, Long> coordinatesYColumn;
    @FXML
    private FilterableFloatTableColumn<SpaceMarine, Float> healthColumn;
    @FXML
    private FilterableLongTableColumn<SpaceMarine, Long> heartCountColumn;
    @FXML
    private FilterableBooleanTableColumn<SpaceMarine, Boolean> loyalColumn;
    @FXML
    private FilterableEnumTableColumn<SpaceMarine, MeleeWeapon> meleeWeaponColumn;
    @FXML
    private FilterableStringTableColumn<SpaceMarine, String> chapterNameColumn;
    @FXML
    private FilterableStringTableColumn<SpaceMarine, String> chapterWorldColumn;
    @FXML
    private FilterableStringTableColumn<SpaceMarine, String> userColumn;
    @FXML
    private FilterableDateTableColumn<SpaceMarine, LocalDate> creationDateColumn;

    public FilterTableWorker(FilteredTableView<SpaceMarine> tableMarines, FilterableLongTableColumn<SpaceMarine, Long> idColumn, FilterableStringTableColumn<SpaceMarine, String> nameColumn, FilterableIntegerTableColumn<SpaceMarine, Integer> coordinatesXColumn, FilterableLongTableColumn<SpaceMarine, Long> coordinatesYColumn, FilterableFloatTableColumn<SpaceMarine, Float> healthColumn, FilterableLongTableColumn<SpaceMarine, Long> heartCountColumn, FilterableBooleanTableColumn<SpaceMarine, Boolean> loyalColumn, FilterableEnumTableColumn<SpaceMarine, MeleeWeapon> meleeWeaponColumn, FilterableStringTableColumn<SpaceMarine, String> chapterNameColumn, FilterableStringTableColumn<SpaceMarine, String> chapterWorldColumn, FilterableStringTableColumn<SpaceMarine, String> userColumn, FilterableDateTableColumn<SpaceMarine, LocalDate> creationDateColumn, ClientMessagesHandler updateCollectionClientMessagesHandler, VisualisationWorker visualisationWorker) {
        this.tableMarines = tableMarines;
        this.idColumn = idColumn;
        this.nameColumn = nameColumn;
        this.coordinatesXColumn = coordinatesXColumn;
        this.coordinatesYColumn = coordinatesYColumn;
        this.healthColumn = healthColumn;
        this.heartCountColumn = heartCountColumn;
        this.loyalColumn = loyalColumn;
        this.meleeWeaponColumn = meleeWeaponColumn;
        this.chapterNameColumn = chapterNameColumn;
        this.chapterWorldColumn = chapterWorldColumn;
        this.userColumn = userColumn;
        this.creationDateColumn = creationDateColumn;
        this.updateCollectionClientMessagesHandler = updateCollectionClientMessagesHandler;
        this.visualisationWorker = visualisationWorker;
        meleeWeaponColumn.setEnumValues(MeleeWeapon.values());
        try {
            updateCollectionClientMessagesHandler.connect("localhost", 1488);
            setUpTimer();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    /**
     * Настриваем фильтрацию колонок
     */
    public void setupFilterTable() {
        tableMarines.addEventHandler(ColumnFilterEvent.FILTER_CHANGED_EVENT, (EventHandler<ColumnFilterEvent>) t -> {
            final List<IFilterOperator> filters = t.sourceColumn().getFilters();
            logger.debug("Количество отфильрованных колонок: {} \n" +
                            "Отфильтрованные колонки: {} \n" +
                            "Текущий фильтр на колонке {}: "
                    , tableMarines.getFilteredColumns().size(), t.sourceColumn().getText(), t.sourceColumn().getText());
            filters.forEach(x -> System.out.println("Тип: " + x.getType() + ", значение: " + x.getValue()));
            filterIdColumn(spaceMarinesData);
            filterNameColumn(spaceMarinesData);
            filterCoordinatesXColumn(spaceMarinesData);
            filterCoordinatesYColumn(spaceMarinesData);
            filterHealthColumn(spaceMarinesData);
            filterHeartCountColumn(spaceMarinesData);
            filterLoyalColumn(spaceMarinesData);
            filterMeleeWeaponColumn(spaceMarinesData);
            filterChapterNameColumn(spaceMarinesData);
            filterChapterWorldColumn(spaceMarinesData);
            filterUserColumn(spaceMarinesData);
            filterCreationDateColumn(spaceMarinesData);
            tableMarines.setItems(spaceMarinesData);

        });
    }

    /**
     * Таймер для обновления клиенской коллекции
     */
    private void setUpTimer() {
        Timeline timeline = new Timeline(new KeyFrame(Duration.seconds(1), ev -> {
            updateTable();
            visualisationWorker.setupVisualisation();
        }));
        timeline.setCycleCount(Animation.INDEFINITE);
        timeline.play();
    }

    private boolean updateRQ = true;

    /**
     * Обновление таблицы
     */
    private void updateTable() {
        try {
            logger.debug("Отправляем запрос серверу на обновление клиентской коллекции");
            updateCollectionClientMessagesHandler.sendCommand(new Show());
            String answer = "";
            while (!answer.equals("collectionReceived")) answer = updateCollectionClientMessagesHandler.receiveAnswer();
            if (updateRQ) {
                spaceMarinesData.clear();
                logger.debug("Добавляем все элементы из клиентской коллекции в ObservableList: теперь они доступны для просмотра");
                spaceMarinesData.addAll(updateCollectionClientMessagesHandler.getClientPq());
                visualisationWorker.setupVisualisation();
                updateRQ = false;
            }
        } catch (IOException e) {
            logger.error("Не удалось обновить ObservableList");
        }
    }

    /**
     * Фильтр колонки id
     *
     * @param spaceMarinesData лист, хранящий отображаемые spaceMarin'ы
     */
    private void filterIdColumn(ObservableList<SpaceMarine> spaceMarinesData) {
        boolean firstFilter = false;
        List<SpaceMarine> addList = new ArrayList<>();
        final ObservableList<NumberOperator<Long>> filters = idColumn.getFilters();
        for (NumberOperator<Long> filter : filters) {
            if (filter.getType() == NumberOperator.Type.EQUALS) {
                addList = spaceMarinesData.stream().filter(x -> x.getId() == filter.getValue()).collect(Collectors.toList());
                firstFilter = true;
            } else if (filter.getType() == NumberOperator.Type.NOTEQUALS) {
                addList = spaceMarinesData.stream().filter(x -> x.getId() != filter.getValue()).collect(Collectors.toList());
                firstFilter = true;
            } else if (filter.getType() == NumberOperator.Type.GREATERTHAN) {
                addList = spaceMarinesData.stream().filter(x -> x.getId() > filter.getValue()).collect(Collectors.toList());
                firstFilter = true;
            } else if (filter.getType() == NumberOperator.Type.GREATERTHANEQUALS) {
                addList = spaceMarinesData.stream().filter(x -> x.getId() >= filter.getValue()).collect(Collectors.toList());
                firstFilter = true;
            } else if (filter.getType() == NumberOperator.Type.LESSTHAN) {
                addList = spaceMarinesData.stream().filter(x -> x.getId() < filter.getValue()).collect(Collectors.toList());
                firstFilter = true;
            } else if (filter.getType() == NumberOperator.Type.LESSTHANEQUALS) {
                addList = spaceMarinesData.stream().filter(x -> x.getId() <= filter.getValue()).collect(Collectors.toList());
                firstFilter = true;
            } else if (filter.getType() == NumberOperator.Type.NONE && !firstFilter) {
                updateRQ = true;
                updateTable();
                filters.clear();
                return;
            }
            spaceMarinesData.clear();
            spaceMarinesData.addAll(addList);
        }
    }
    /**
     * Фильтр колонки name
     *
     * @param spaceMarinesData лист, хранящий отображаемые spaceMarin'ы
     */
    private void filterNameColumn(ObservableList<SpaceMarine> spaceMarinesData) {
        List<SpaceMarine> addList = new ArrayList<>();
        final ObservableList<StringOperator> filters = nameColumn.getFilters();
        for (StringOperator filter : filters) {
            if (filter.getType() == StringOperator.Type.EQUALS) {
                addList = spaceMarinesData.stream().filter(x -> x.getName().equals(filter.getValue())).collect(Collectors.toList());
            } else if (filter.getType() == StringOperator.Type.NOTEQUALS) {
                addList = spaceMarinesData.stream().filter(x -> !x.getName().equals(filter.getValue())).collect(Collectors.toList());
            } else if (filter.getType() == StringOperator.Type.CONTAINS) {
                addList = spaceMarinesData.stream().filter(x -> x.getName().contains(filter.getValue())).collect(Collectors.toList());
            } else if (filter.getType() == StringOperator.Type.STARTSWITH) {
                addList = spaceMarinesData.stream().filter(x -> x.getName().startsWith(filter.getValue())).collect(Collectors.toList());
            } else if (filter.getType() == StringOperator.Type.ENDSWITH) {
                addList = spaceMarinesData.stream().filter(x -> x.getName().endsWith(filter.getValue())).collect(Collectors.toList());
            } else if (filter.getType() == StringOperator.Type.NONE) {
                updateRQ = true;
                updateTable();
                filters.clear();
                return;
            }
            spaceMarinesData.clear();
            spaceMarinesData.addAll(addList);
        }
    }
    /**
     * Фильтр колонки X
     *
     * @param spaceMarinesData лист, хранящий отображаемые spaceMarin'ы
     */
    private void filterCoordinatesXColumn(ObservableList<SpaceMarine> spaceMarinesData) {
        boolean firstFilter = false;
        List<SpaceMarine> addList = new ArrayList<>();
        final ObservableList<NumberOperator<Integer>> filters = coordinatesXColumn.getFilters();
        for (NumberOperator<Integer> filter : filters) {
            if (filter.getType() == NumberOperator.Type.EQUALS) {
                firstFilter = true;
                addList = spaceMarinesData.stream().filter(x -> x.getCoordinates().getX() == filter.getValue()).collect(Collectors.toList());
            } else if (filter.getType() == NumberOperator.Type.NOTEQUALS) {
                firstFilter = true;
                addList = spaceMarinesData.stream().filter(x -> x.getCoordinates().getX() != filter.getValue()).collect(Collectors.toList());
            } else if (filter.getType() == NumberOperator.Type.GREATERTHAN) {
                firstFilter = true;
                addList = spaceMarinesData.stream().filter(x -> x.getCoordinates().getX() > filter.getValue()).collect(Collectors.toList());
            } else if (filter.getType() == NumberOperator.Type.GREATERTHANEQUALS) {
                firstFilter = true;
                addList = spaceMarinesData.stream().filter(x -> x.getCoordinates().getX() >= filter.getValue()).collect(Collectors.toList());
            } else if (filter.getType() == NumberOperator.Type.LESSTHAN) {
                firstFilter = true;
                addList = spaceMarinesData.stream().filter(x -> x.getCoordinates().getX() < filter.getValue()).collect(Collectors.toList());
            } else if (filter.getType() == NumberOperator.Type.LESSTHANEQUALS) {
                firstFilter = true;
                addList = spaceMarinesData.stream().filter(x -> x.getCoordinates().getX() <= filter.getValue()).collect(Collectors.toList());
            } else if (filter.getType() == NumberOperator.Type.NONE && !firstFilter) {
                filters.clear();
                updateRQ = true;
                updateTable();
                return;
            }
            spaceMarinesData.clear();
            spaceMarinesData.addAll(addList);
        }
    }

    private void filterCoordinatesYColumn(ObservableList<SpaceMarine> spaceMarinesData) {
        boolean firstFilter = false;
        List<SpaceMarine> addList = new ArrayList<>();
        final ObservableList<NumberOperator<Long>> filters = coordinatesYColumn.getFilters();
        for (NumberOperator<Long> filter : filters) {
            if (filter.getType() == NumberOperator.Type.EQUALS) {
                firstFilter = true;
                addList = spaceMarinesData.stream().filter(x -> x.getCoordinates().getY() == filter.getValue()).collect(Collectors.toList());
            } else if (filter.getType() == NumberOperator.Type.NOTEQUALS) {
                firstFilter = true;
                addList = spaceMarinesData.stream().filter(x -> x.getCoordinates().getY() != filter.getValue()).collect(Collectors.toList());
            } else if (filter.getType() == NumberOperator.Type.GREATERTHAN) {
                firstFilter = true;
                addList = spaceMarinesData.stream().filter(x -> x.getCoordinates().getY() > filter.getValue()).collect(Collectors.toList());
            } else if (filter.getType() == NumberOperator.Type.GREATERTHANEQUALS) {
                firstFilter = true;
                addList = spaceMarinesData.stream().filter(x -> x.getCoordinates().getY() >= filter.getValue()).collect(Collectors.toList());
            } else if (filter.getType() == NumberOperator.Type.LESSTHAN) {
                firstFilter = true;
                addList = spaceMarinesData.stream().filter(x -> x.getCoordinates().getY() < filter.getValue()).collect(Collectors.toList());
            } else if (filter.getType() == NumberOperator.Type.LESSTHANEQUALS) {
                firstFilter = true;
                addList = spaceMarinesData.stream().filter(x -> x.getCoordinates().getY() <= filter.getValue()).collect(Collectors.toList());
            } else if (filter.getType() == NumberOperator.Type.NONE && !firstFilter) {
                filters.clear();
                updateRQ = true;
                updateTable();
                return;
            }
            spaceMarinesData.clear();
            spaceMarinesData.addAll(addList);
        }
    }
    /**
     * Фильтр колонки Y
     *
     * @param spaceMarinesData лист, хранящий отображаемые spaceMarin'ы
     */
    private void filterHealthColumn(ObservableList<SpaceMarine> spaceMarinesData) {
        boolean firstFilter = false;
        List<SpaceMarine> addList = new ArrayList<>();
        final ObservableList<NumberOperator<Float>> filters = healthColumn.getFilters();
        for (NumberOperator<Float> filter : filters) {
            if (filter.getType() == NumberOperator.Type.EQUALS) {
                firstFilter = true;
                addList = spaceMarinesData.stream().filter(x -> x.getHealth().equals(filter.getValue())).collect(Collectors.toList());
            } else if (filter.getType() == NumberOperator.Type.NOTEQUALS) {
                firstFilter = true;
                addList = spaceMarinesData.stream().filter(x -> !x.getHealth().equals(filter.getValue())).collect(Collectors.toList());
            } else if (filter.getType() == NumberOperator.Type.GREATERTHAN) {
                firstFilter = true;
                addList = spaceMarinesData.stream().filter(x -> x.getHealth() > filter.getValue()).collect(Collectors.toList());
            } else if (filter.getType() == NumberOperator.Type.GREATERTHANEQUALS) {
                firstFilter = true;
                addList = spaceMarinesData.stream().filter(x -> x.getHealth() >= filter.getValue()).collect(Collectors.toList());
            } else if (filter.getType() == NumberOperator.Type.LESSTHAN) {
                firstFilter = true;
                addList = spaceMarinesData.stream().filter(x -> x.getHealth() < filter.getValue()).collect(Collectors.toList());
            } else if (filter.getType() == NumberOperator.Type.LESSTHANEQUALS) {
                firstFilter = true;
                addList = spaceMarinesData.stream().filter(x -> x.getHealth() <= filter.getValue()).collect(Collectors.toList());
            } else if (filter.getType() == NumberOperator.Type.NONE && !firstFilter) {
                filters.clear();
                updateRQ = true;
                updateTable();
                return;
            }
            spaceMarinesData.clear();
            spaceMarinesData.addAll(addList);
        }
    }
    /**
     * Фильтр колонки heartCount
     *
     * @param spaceMarinesData лист, хранящий отображаемые spaceMarin'ы
     */
    private void filterHeartCountColumn(ObservableList<SpaceMarine> spaceMarinesData) {
        boolean firstFilter = false;
        List<SpaceMarine> addList = new ArrayList<>();
        final ObservableList<NumberOperator<Long>> filters = heartCountColumn.getFilters();
        for (NumberOperator<Long> filter : filters) {
            if (filter.getType() == NumberOperator.Type.EQUALS) {
                firstFilter = true;
                addList = spaceMarinesData.stream().filter(x -> x.getHeartCount() == filter.getValue()).collect(Collectors.toList());
            } else if (filter.getType() == NumberOperator.Type.NOTEQUALS) {
                firstFilter = true;
                addList = spaceMarinesData.stream().filter(x -> x.getHeartCount() != filter.getValue()).collect(Collectors.toList());
            } else if (filter.getType() == NumberOperator.Type.GREATERTHAN) {
                firstFilter = true;
                addList = spaceMarinesData.stream().filter(x -> x.getHeartCount() > filter.getValue()).collect(Collectors.toList());
            } else if (filter.getType() == NumberOperator.Type.GREATERTHANEQUALS) {
                firstFilter = true;
                addList = spaceMarinesData.stream().filter(x -> x.getHeartCount() >= filter.getValue()).collect(Collectors.toList());
            } else if (filter.getType() == NumberOperator.Type.LESSTHAN) {
                firstFilter = true;
                addList = spaceMarinesData.stream().filter(x -> x.getHeartCount() < filter.getValue()).collect(Collectors.toList());
            } else if (filter.getType() == NumberOperator.Type.LESSTHANEQUALS) {
                firstFilter = true;
                addList = spaceMarinesData.stream().filter(x -> x.getHeartCount() <= filter.getValue()).collect(Collectors.toList());
            } else if (filter.getType() == NumberOperator.Type.NONE && !firstFilter) {
                filters.clear();
                updateRQ = true;
                updateTable();
                return;
            }
            spaceMarinesData.clear();
            spaceMarinesData.addAll(addList);
        }
    }
    /**
     * Фильтр колонки loyal
     *
     * @param spaceMarinesData лист, хранящий отображаемые spaceMarin'ы
     */
    private void filterLoyalColumn(ObservableList<SpaceMarine> spaceMarinesData) {
        List<SpaceMarine> addList = new ArrayList<>();
        final ObservableList<BooleanOperator> filters = loyalColumn.getFilters();
        for (BooleanOperator filter : filters) {
            if (filter.getValue() == null) {
                filters.clear();
                updateTable();
                return;
            } else if (filter.getValue().toString().equals("true")) {
                addList = spaceMarinesData.stream().filter(spaceMarine -> spaceMarine.getLoyal() != null).filter(SpaceMarine::getLoyal).collect(Collectors.toList());
            } else if (filter.getValue().toString().equals("false")) {
                addList = spaceMarinesData.stream().filter(spaceMarine -> spaceMarine.getLoyal() != null).filter(spaceMarine -> !spaceMarine.getLoyal()).collect(Collectors.toList());
            } else if (filter.getType() == BooleanOperator.Type.NONE) {
                filters.clear();
                updateRQ = true;
                updateTable();
                return;
            }
            spaceMarinesData.clear();
            spaceMarinesData.addAll(addList);
        }
    }
    /**
     * Фильтр колонки meleeWeapon
     *
     * @param spaceMarinesData лист, хранящий отображаемые spaceMarin'ы
     */
    private void filterMeleeWeaponColumn(ObservableList<SpaceMarine> spaceMarinesData) {
        List<SpaceMarine> addList = new ArrayList<>();
        final ObservableList<EnumOperator<MeleeWeapon>> filters = meleeWeaponColumn.getFilters();
        List<SpaceMarine> meleeList = new ArrayList<>();
        for (EnumOperator<MeleeWeapon> filter : filters) {
            if (filter.getType() == BooleanOperator.Type.EQUALS) {
                addList = spaceMarinesData.stream().filter(spaceMarine -> spaceMarine.getMeleeWeapon().equals(filter.getValue())).collect(Collectors.toList());
            } else if (filter.getType() == BooleanOperator.Type.NONE) {
                filters.clear();
                updateRQ = true;
                updateTable();
                return;
            }
            meleeList.addAll(addList);
        }
        if (!meleeList.isEmpty()) spaceMarinesData.clear();
        spaceMarinesData.addAll(meleeList);
    }
    /**
     * Фильтр колонки chapterName
     *
     * @param spaceMarinesData лист, хранящий отображаемые spaceMarin'ы
     */
    private void filterChapterNameColumn(ObservableList<SpaceMarine> spaceMarinesData) {
        List<SpaceMarine> addList = new ArrayList<>();
        final ObservableList<StringOperator> filters = chapterNameColumn.getFilters();
        for (StringOperator filter : filters) {
            if (filter.getType() == StringOperator.Type.EQUALS) {
                addList = spaceMarinesData.stream().filter(x -> x.getChapter().getName().equals(filter.getValue())).collect(Collectors.toList());
            } else if (filter.getType() == StringOperator.Type.NOTEQUALS) {
                addList = spaceMarinesData.stream().filter(x -> !x.getChapter().getName().equals(filter.getValue())).collect(Collectors.toList());
            } else if (filter.getType() == StringOperator.Type.CONTAINS) {
                addList = spaceMarinesData.stream().filter(x -> x.getChapter().getName().contains(filter.getValue())).collect(Collectors.toList());
            } else if (filter.getType() == StringOperator.Type.STARTSWITH) {
                addList = spaceMarinesData.stream().filter(x -> x.getChapter().getName().startsWith(filter.getValue())).collect(Collectors.toList());
            } else if (filter.getType() == StringOperator.Type.ENDSWITH) {
                addList = spaceMarinesData.stream().filter(x -> x.getChapter().getName().endsWith(filter.getValue())).collect(Collectors.toList());
            } else if (filter.getType() == StringOperator.Type.NONE) {
                filters.clear();
                updateRQ = true;
                updateTable();
                return;
            }
            spaceMarinesData.clear();
            spaceMarinesData.addAll(addList);
        }
    }
    /**
     * Фильтр колонки chapterWorld
     *
     * @param spaceMarinesData лист, хранящий отображаемые spaceMarin'ы
     */
    private void filterChapterWorldColumn(ObservableList<SpaceMarine> spaceMarinesData) {
        List<SpaceMarine> addList = new ArrayList<>();
        final ObservableList<StringOperator> filters = chapterWorldColumn.getFilters();
        for (StringOperator filter : filters) {
            if (filter.getType() == StringOperator.Type.EQUALS) {
                addList = spaceMarinesData.stream().filter(x -> x.getChapter().getWorld().equals(filter.getValue())).collect(Collectors.toList());
            } else if (filter.getType() == StringOperator.Type.NOTEQUALS) {
                addList = spaceMarinesData.stream().filter(x -> !x.getChapter().getWorld().equals(filter.getValue())).collect(Collectors.toList());
            } else if (filter.getType() == StringOperator.Type.CONTAINS) {
                addList = spaceMarinesData.stream().filter(x -> x.getChapter().getWorld().contains(filter.getValue())).collect(Collectors.toList());
            } else if (filter.getType() == StringOperator.Type.STARTSWITH) {
                addList = spaceMarinesData.stream().filter(x -> x.getChapter().getWorld().startsWith(filter.getValue())).collect(Collectors.toList());
            } else if (filter.getType() == StringOperator.Type.ENDSWITH) {
                addList = spaceMarinesData.stream().filter(x -> x.getChapter().getWorld().endsWith(filter.getValue())).collect(Collectors.toList());
            } else if (filter.getType() == StringOperator.Type.NONE) {
                filters.clear();
                updateRQ = true;
                updateTable();
                return;
            }
            spaceMarinesData.clear();
            spaceMarinesData.addAll(addList);
        }
    }
    /**
     * Фильтр колонки user
     *
     * @param spaceMarinesData лист, хранящий отображаемые spaceMarin'ы
     */
    private void filterUserColumn(ObservableList<SpaceMarine> spaceMarinesData) {
        List<SpaceMarine> addList = new ArrayList<>();
        final ObservableList<StringOperator> filters = userColumn.getFilters();
        for (StringOperator filter : filters) {
            if (filter.getType() == StringOperator.Type.EQUALS) {
                addList = spaceMarinesData.stream().filter(x -> x.getCreatedByUser().equals(filter.getValue())).collect(Collectors.toList());
            } else if (filter.getType() == StringOperator.Type.NOTEQUALS) {
                addList = spaceMarinesData.stream().filter(x -> !x.getCreatedByUser().equals(filter.getValue())).collect(Collectors.toList());
            } else if (filter.getType() == StringOperator.Type.CONTAINS) {
                addList = spaceMarinesData.stream().filter(x -> x.getCreatedByUser().contains(filter.getValue())).collect(Collectors.toList());
            } else if (filter.getType() == StringOperator.Type.STARTSWITH) {
                addList = spaceMarinesData.stream().filter(x -> x.getCreatedByUser().startsWith(filter.getValue())).collect(Collectors.toList());
            } else if (filter.getType() == StringOperator.Type.ENDSWITH) {
                addList = spaceMarinesData.stream().filter(x -> x.getCreatedByUser().endsWith(filter.getValue())).collect(Collectors.toList());
            } else if (filter.getType() == StringOperator.Type.NONE) {
                filters.clear();
                updateRQ = true;
                updateTable();
                return;
            }
            spaceMarinesData.clear();
            spaceMarinesData.addAll(addList);
        }
    }
    /**
     * Фильтр колонки date
     *
     * @param spaceMarinesData лист, хранящий отображаемые spaceMarin'ы
     */
    private void filterCreationDateColumn(ObservableList<SpaceMarine> spaceMarinesData) {
        boolean firstFilter = false;
        List<SpaceMarine> addList = new ArrayList<>();
        final ObservableList<DateOperator> filters = creationDateColumn.getFilters();
        for (DateOperator filter : filters) {
            if (filter.getType() == NumberOperator.Type.EQUALS) {
                firstFilter = true;
                addList = spaceMarinesData.stream().filter(x -> x.getCreationDate() == filter.getValue()).collect(Collectors.toList());
            } else if (filter.getType() == DateOperator.Type.NOTEQUALS) {
                firstFilter = true;
                addList = spaceMarinesData.stream().filter(x -> x.getCreationDate() != filter.getValue()).collect(Collectors.toList());
            } else if (filter.getType() == DateOperator.Type.AFTER) {
                firstFilter = true;
                addList = spaceMarinesData.stream().filter(x -> x.getCreationDate().after(filter.getValue())).collect(Collectors.toList());
            } else if (filter.getType() == DateOperator.Type.AFTERON) {
                firstFilter = true;
                addList = spaceMarinesData.stream().filter(x -> x.getCreationDate().after(filter.getValue()) || x.getCreationDate() == filter.getValue()).collect(Collectors.toList());
            } else if (filter.getType() == DateOperator.Type.BEFORE) {
                firstFilter = true;
                addList = spaceMarinesData.stream().filter(x -> x.getCreationDate().before(filter.getValue())).collect(Collectors.toList());
            } else if (filter.getType() == DateOperator.Type.BEFOREON) {
                firstFilter = true;
                addList = spaceMarinesData.stream().filter(x -> x.getCreationDate().before(filter.getValue()) || x.getCreationDate() == filter.getValue()).collect(Collectors.toList());
            } else if (filter.getType() == DateOperator.Type.NONE && !firstFilter) {
                filters.clear();
                updateRQ = true;
                updateTable();
                return;
            }
            spaceMarinesData.clear();
            spaceMarinesData.addAll(addList);
        }
    }

    public ObservableList<SpaceMarine> getSpaceMarinesData() {
        return spaceMarinesData;
    }

    public void setUpdateRQ(boolean updateRQ) {
        this.updateRQ = updateRQ;
    }
}
