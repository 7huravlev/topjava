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
import java.util.function.Function;
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
        Map<LocalDate, Integer> localDateCaloriesMap = new HashMap<>();
        for (UserMeal meal : meals) {
            localDateCaloriesMap.merge(meal.getDateTime().toLocalDate(), meal.getCalories(), Integer::sum);
        }

        // map result
        List<UserMealWithExcess> mealWithExcesses = new ArrayList<>();
        for (UserMeal meal : meals) {
            if (TimeUtil.isBetweenHalfOpen(meal.getDateTime().toLocalTime(), startTime, endTime)) {
                UserMealWithExcess mealWithExcess = new UserMealWithExcess(
                        meal.getDateTime(),
                        meal.getDescription(),
                        meal.getCalories(),
                        localDateCaloriesMap.get(meal.getDateTime().toLocalDate()) > caloriesPerDay);
                mealWithExcesses.add(mealWithExcess);
            }
        }

        return mealWithExcesses;
    }

    public static List<UserMealWithExcess> filteredByStreams(List<UserMeal> meals, LocalTime startTime, LocalTime endTime, int caloriesPerDay) {
        // group by localdate
        Map<LocalDate, Integer> localDateCaloriesMap = meals.stream()
                .collect(Collectors.toMap(m -> m.getDateTime().toLocalDate(), UserMeal::getCalories, Integer::sum));

        // map result
        Supplier<List<UserMealWithExcess>> supplier = ArrayList::new;

        BiConsumer<List<UserMealWithExcess>, UserMeal> accumulator = (umwe, um) -> umwe.add(new UserMealWithExcess(
                um.getDateTime(),
                um.getDescription(),
                um.getCalories(),
                localDateCaloriesMap.get(um.getDateTime().toLocalDate()) > caloriesPerDay
        ));

        BinaryOperator<List<UserMealWithExcess>> combiner = (umwe, umb) -> {
            umwe.addAll(umb);
            return umwe;
        };

        Function<List<UserMealWithExcess>, List<UserMealWithExcess>> finisher = (umb) -> umb;

        return meals.stream()
                .filter(t -> TimeUtil.isBetweenHalfOpen(t.getDateTime().toLocalTime(), startTime, endTime))
                .collect(Collector.of(
                        supplier,
                        accumulator,
                        combiner,
                        finisher));
    }
}
