package main.Server;

import main.BasicClasses.Flat;
import main.BasicClasses.House;
import main.Common.FlatData;

import java.time.LocalDate;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;
import java.util.Vector;
import java.util.stream.Collectors;

public class CollectionManager {
    private LocalDate creationDate;
    private Vector<Flat> collection;
    private FlatFactory flatFactory;

    public CollectionManager(Vector<Flat> collection) {
        this.creationDate = LocalDate.now();
        this.collection = collection == null ? new Vector<>() : collection;
        this.flatFactory = new FlatFactory();
    }

    public String info() {
        return collection.getClass() + " " + collection.size() + " " + creationDate;
    }

    public List<Flat> show() {
        return collection.stream()
                .filter(Objects::nonNull)
                .sorted(Comparator.comparing(Flat::getName, Comparator.nullsLast(String::compareTo)))
                .collect(Collectors.toList());
    }

    public Flat add(FlatData data) {
        Flat f1 = flatFactory.create(data);
        collection.add(f1);
        return f1;
    }

    public void update(long id, FlatData data) {
        Flat oldFlat = find_by_id(id);
        Flat newFlat = flatFactory.create(data);
        newFlat.setId(id);
        collection.set(collection.indexOf(oldFlat), newFlat);
    }

    public void remove_by_id(long id) {
        Flat obj = find_by_id(id);
        collection.remove(obj);
    }

    public void clear() {
        collection.clear();
    }

    public void remove_last() {
        if (collection.isEmpty()) {
            throw new NoSuchElementException("Коллекция пуста!");
        }
        collection.remove(collection.lastElement());
    }

    public int remove_lower(FlatData data) {
        Flat f1 = flatFactory.create(data);
        List<Flat> toRemove = collection.stream()
                .filter(flat -> flat.compareTo(f1) < 0)
                .collect(Collectors.toList());
        collection.removeAll(toRemove);
        return toRemove.size();
    }

    public Flat find_by_id(long id) throws IllegalArgumentException {
        Optional<Flat> flat = collection.stream()
                .filter(Objects::nonNull)
                .filter(i -> i.getId() == id)
                .findFirst();

        return flat.orElseThrow(() -> new IllegalArgumentException("Элемент с таким id не найден!"));
    }

    public int count_greater_than_house(House h1) {
        if (h1 == null) {
            throw new IllegalArgumentException("House не передан!");
        }
        return (int) collection.stream()
                .filter(Objects::nonNull)
                .map(Flat::getHouse)
                .filter(Objects::nonNull)
                .filter(j -> j.compareTo(h1) > 0)
                .count();
    }

    public String average_of_number_of_rooms() {
        if (collection.isEmpty()) {
            return "Коллекция пуста!";
        }
        double average = collection.stream()
                .filter(Objects::nonNull)
                .mapToInt(Flat::getNumberOfRooms)
                .average()
                .orElse(0);
        return "Среднее количество комнат:" + (float) average;
    }

    public List<Flat> filter_by_new(Boolean isNew) {
        return collection.stream()
                .filter(Objects::nonNull)
                .filter(i -> Objects.equals(i.getIsNew(), isNew))
                .collect(Collectors.toList());
    }

    public void reorder() {
        Collections.reverse(collection);
    }

    public Vector<Flat> getCollection() {
        return collection;
    }
}
