package ru.javawebinar.topjava.util;

import ru.javawebinar.topjava.model.UserMeal;
import ru.javawebinar.topjava.model.UserMealWithExcess;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.Month;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Supplier;
import java.util.stream.Collector;
import java.util.stream.Collectors;

public class UserMealsUtil {
    public static void main(String[] args) {
        List<UserMeal> meals = Arrays.asList(
                new UserMeal(LocalDateTime.of(2020, Month.JANUARY, 30, 10, 0), "Завтрак", 500),
                new UserMeal(LocalDateTime.of(2020, Month.JANUARY, 30, 13, 0), "Обед", 1000),
                new UserMeal(LocalDateTime.of(2020, Month.JANUARY, 30, 20, 0), "Ужин", 500),
                new UserMeal(LocalDateTime.of(2020, Month.JANUARY, 31, 0, 0), "Еда на граничное значение", 100),
                new UserMeal(LocalDateTime.of(2020, Month.JANUARY, 31, 10, 0), "Завтрак", 1000),
                new UserMeal(LocalDateTime.of(2020, Month.JANUARY, 31, 13, 0), "Обед", 500),
                new UserMeal(LocalDateTime.of(2020, Month.JANUARY, 31, 20, 0), "Ужин", 410)
        );

        List<UserMealWithExcess> mealsTo = filteredByCycles(meals, LocalTime.of(7, 0), LocalTime.of(12, 0), 2000);
        mealsTo.forEach(System.out::println);

//        System.out.println(filteredByStreams(meals, LocalTime.of(7, 0), LocalTime.of(12, 0), 2000));
    }

    public static List<UserMealWithExcess> filteredByCycles(List<UserMeal> meals, LocalTime startTime, LocalTime endTime, int caloriesPerDay) {
        // group by localdate
        Map<LocalDate, Integer> mealMap = new HashMap<>();
        for (UserMeal meal : meals) {
            if (TimeUtil.isBetweenHalfOpen(meal.getDateTime().toLocalTime(), startTime, endTime)) {
                mealMap.merge(meal.getDateTime().toLocalDate(), meal.getCalories(), Integer::sum);
            }
        }

        // map result
        List<UserMealWithExcess> mealWithExcesses = new ArrayList<>();
        for (UserMeal meal : meals) {
            if (TimeUtil.isBetweenHalfOpen(meal.getDateTime().toLocalTime(), startTime, endTime)) {
                UserMealWithExcess mealWithExcess = new UserMealWithExcess(
                        meal.getDateTime(),
                        meal.getDescription(),
                        meal.getCalories(),
                        mealMap.get(meal.getDateTime().toLocalDate()) > caloriesPerDay);
                mealWithExcesses.add(mealWithExcess);
            }
        }

        return mealWithExcesses;
    }

    public static List<UserMealWithExcess> filteredByStreams(List<UserMeal> meals, LocalTime startTime, LocalTime endTime, int caloriesPerDay) {
        // group by localdate
        Map<LocalDate, Integer> mealMap = meals.stream()
                .filter(t -> TimeUtil.isBetweenHalfOpen(t.getDateTime().toLocalTime(), startTime, endTime))
                .collect(Collectors.toMap(p -> p.getDateTime().toLocalDate(), UserMeal::getCalories, Integer::sum));

        // map result
        Supplier<List<UserMealWithExcess>> supplier = ArrayList::new;

        BiConsumer<List<UserMealWithExcess>, UserMeal> accumulator = (t, f) -> t.add(new UserMealWithExcess(
                f.getDateTime(),
                f.getDescription(),
                f.getCalories(),
                mealMap.get(f.getDateTime().toLocalDate()) > caloriesPerDay
        ));

        BinaryOperator<List<UserMealWithExcess>> combiner = (l, r) -> {
            l.addAll(r);
            return l;
        };

        return meals.stream()
                .filter(t -> TimeUtil.isBetweenHalfOpen(t.getDateTime().toLocalTime(), startTime, endTime))
                .collect(Collector.of(
                        supplier,
                        accumulator,
                        combiner));
    }
}
