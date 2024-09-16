//package org.vaadin.example.views;
//
//import com.vaadin.flow.component.button.Button;
//import com.vaadin.flow.component.charts.Chart;
//import com.vaadin.flow.component.html.H1;
//import com.vaadin.flow.component.orderedlayout.VerticalLayout;
//import com.vaadin.flow.router.Route;
//import org.vaadin.addons.chartjs.ChartJs;
//import org.vaadin.addons.chartjs.config.BarChartConfig;
//import org.vaadin.addons.chartjs.data.BarDataset;
//import org.vaadin.addons.chartjs.data.Dataset;
//import org.vaadin.addons.chartjs.options.Position;
//import org.vaadin.example.MainLayout;
//import org.vaadin.example.service.*;
//
//import java.math.BigDecimal;
//import java.time.Month;
//import java.util.ArrayList;
//import java.util.List;
//import java.util.Map;
//
//@Route(value = "netCashflowForecast", layout = MainLayout.class)
//public class NetCashflowForecastView extends VerticalLayout {
//
//    private final ExpenseService expenseService;
//    private final SessionService sessionService;
//    private final UserService userService;
//    private ChartJs chart;
//
//    public NetCashflowForecastView(ExpenseService expenseService, SessionService sessionService,
//                                   UserService userService) {
//        this.expenseService = expenseService;
//        this.sessionService = sessionService;
//        this.userService = userService;
//
//
//        updateChart();
//        Button refreshButton = new Button("Refresh", e -> updateChart());
//        add(new H1("Net Cashflow Forecast"), chart, refreshButton);
//    }
//
//    private void configureChart() {
//        chart = new ChartJs(); //
//
//    }
//
//    private void updateChart() {
//        Long userId = sessionService.getLoggedInUserId();
//        Map<Month, BigDecimal> monthlyExpenses = expenseService.getExpensesForNext12Months(userId);
//
//        BarChartConfig config = new BarChartConfig();
//        config
//                .data()
//                .labels(monthlyExpenses.keySet().stream().map(Month::name).toArray(String[]::new))
//                .addDataset(new BarDataset()
//                        .label("Expenses")
//                        .backgroundColor("rgba(151,187,205,0.5)")
//                        .borderColor("white")
//                        .borderWidth(2)
//                        .data(monthlyExpenses.values().stream().map(BigDecimal::doubleValue).toArray(Double[]::new)))
//                .and()
//                .options()
//                .responsive(true)
//                .title()
//                .display(true)
//                .position(Position.LEFT)
//                .text("Projected Expenses for the Next 12 Months")
//                .and()
//                .done();
//
//        // Update the chart
//        chart = new ChartJs(config);
//        chart.setWidth("80%");
//    }
//
//}
package org.vaadin.example.views;

import ch.qos.logback.core.Layout;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import org.vaadin.addons.chartjs.ChartJs;
import org.vaadin.addons.chartjs.config.BarChartConfig;
import org.vaadin.addons.chartjs.data.BarDataset;
import org.vaadin.addons.chartjs.data.Dataset;
import org.vaadin.addons.chartjs.options.Position;
import org.vaadin.addons.chartjs.options.scale.Axis;
import org.vaadin.addons.chartjs.options.scale.LinearScale;
import org.vaadin.example.MainLayout;
import org.vaadin.example.service.ExpenseService;
import org.vaadin.example.service.SessionService;
import org.vaadin.example.service.UserService;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.Month;
import java.util.*;

@Route(value = "netCashflowForecast", layout = MainLayout.class)
public class NetCashflowForecastView extends VerticalLayout {

    private final ExpenseService expenseService;
    private final SessionService sessionService;
    private final UserService userService;
    private ChartJs chart;
    private BarChartConfig config;
    private int previousMonths = 3;

    public NetCashflowForecastView(ExpenseService expenseService, SessionService sessionService,
                                   UserService userService) {
        this.expenseService = expenseService;
        this.sessionService = sessionService;
        this.userService = userService;

        Div chartContainer = new Div();
        chartContainer.setClassName("chart-container");

        configureChart();  // Initialize the chart
        updateChart();     // Set the initial data

        HorizontalLayout noteLayout = new HorizontalLayout();
        noteLayout.add(chart);
//        noteLayout.setWidthFull();
//        noteLayout.setHeightFull()
        noteLayout.setHeight("100%");
        noteLayout.setWidth("100%");

        chartContainer.add(noteLayout);


        chartContainer.getStyle().set("background-color", "lightgrey");
        chartContainer.getStyle().set("width", "100%");
        chartContainer.getStyle().set("height", "500px");



        Button refreshButton = new Button("Refresh", e -> updateChart());
        add(new H1("Net Cashflow Forecast"), refreshButton);
        add(chartContainer);



    }

    private void configureChart() {
        // Initialize the chart with an empty dataset
        config = (BarChartConfig) new BarChartConfig()
                .data()
                .labels(new String[]{})  // Start with no labels
                .addDataset(new BarDataset().type()
                        .label("Expenses")
                        .backgroundColor("rgba(151,187,205,0.5)")
                        .borderColor("white")
                        .borderWidth(2)
                        .data(new Double[]{})  // Start with no data
                )
                .and()
                .options()
                .responsive(true).maintainAspectRatio(false)
                .title()
                .display(true)
                .position(Position.LEFT)
                .text("Projected Expenses for the Next 12 Months")
                .and()
                .scales()  // Add Y-axis configuration
                .add(Axis.Y, new LinearScale()
                        .ticks()
                        .min(0)  // Set minimum value to 0
                        .and()
                )
                .and()
                .done();

        chart = new ChartJs(config);
        chart.setWidth("100%");
    }

    private void updateChart() {
//        Long userId = sessionService.getLoggedInUserId();
//        Map<Month, BigDecimal> monthlyExpenses = expenseService.getExpensesForPreviousMonths(userId, previousMonths);
//
//        // Convert labels and data to arrays
//        String[] labels = monthlyExpenses.keySet().stream().map(Month::name).toArray(String[]::new);
//        Double[] data = monthlyExpenses.values().stream()
//                .map(value -> value != null ? value.setScale(2, RoundingMode.HALF_UP).doubleValue() : 0.0)
//                .toArray(Double[]::new);
//
//        // Debugging: Check if data and labels match
//        System.out.println("Labels length: " + labels.length + " Data length: " + data.length);
//
//        System.out.println("Monthly Expenses Map: " + monthlyExpenses);
//
//        // Update labels
//        config.data().getLabels().clear();
//        config.data().labels(labels);
//
//        // Update dataset with real data
//        if (!config.data().getDatasets().isEmpty()) {
//            for (Dataset<?, ?> ds : config.data().getDatasets()) {
//                if (ds instanceof BarDataset) {
//                    for (int i = 0; i < labels.length; i++) {
//                        System.out.println("Month: " + labels[i] + " Data: " + data[i]);
//                    }
//                    BarDataset bds = (BarDataset) ds;
//                    bds.getData().clear();  // Clear existing data
//                    bds.dataAsList(List.of(data));  // Add real data
//                }
//            }
//        }
//
//        // Update the chart
//        chart.update();

        Long userId = sessionService.getLoggedInUserId();
        Map<Month, BigDecimal> predictedExpenses = predictMonthlyExpenses(userId, previousMonths);

        String[] labels = predictedExpenses.keySet().stream().map(Month::name).toArray(String[]::new);
        Double[] data = predictedExpenses.values().stream()
                .map(value -> value != null ? value.setScale(2, RoundingMode.HALF_UP).doubleValue() : 0.0)
                .toArray(Double[]::new);

        config.data().getLabels().clear();
        config.data().labels(labels);

        for (Dataset<?, ?> ds : config.data().getDatasets()) {
            if (ds instanceof BarDataset) {
                BarDataset bds = (BarDataset) ds;
                bds.getData().clear();
                bds.dataAsList(List.of(data));
            }
        }

        chart.update();
    }


    private Map<Month, BigDecimal> predictMonthlyExpenses(Long userId, int previousMonths) {
        Map<Month, BigDecimal> monthlyPastExpenses = expenseService.getExpensesForPreviousMonths(userId, previousMonths);

        // Convert the map entries to a list and sort by month
        List<Map.Entry<Month, BigDecimal>> sortedEntries = new ArrayList<>(monthlyPastExpenses.entrySet());
        sortedEntries.sort(Comparator.comparing(Map.Entry::getKey));

        // Extract the sorted expenses into a list
        List<BigDecimal> expensesList = new ArrayList<>();
        for (Map.Entry<Month, BigDecimal> entry : sortedEntries) {
            expensesList.add(entry.getValue());
        }

        List<BigDecimal> changes = new ArrayList<>();

        for (int i = 0; i < expensesList.size(); i++) {
            System.out.println("MEOWY POO: " + expensesList.get(i));
        }
//         Calculate the decimal change between consecutive months
        for (int i = 1; i < expensesList.size(); i++) {
            BigDecimal monthExpense = expensesList.get(i);
            BigDecimal previousMonthExpense = expensesList.get(i - 1);

            BigDecimal currentPercentageChange;
            if (previousMonthExpense.compareTo(BigDecimal.ZERO) != 0) {
                currentPercentageChange = monthExpense.subtract(previousMonthExpense)
                        .divide(previousMonthExpense, RoundingMode.HALF_UP);
            } else {
                currentPercentageChange = BigDecimal.ZERO; // or handle this in another way, such as skipping this month
            }
            changes.add(currentPercentageChange);
        }
          changes.add(BigDecimal.valueOf(1));
          changes.add(BigDecimal.valueOf(4));
          changes.add(BigDecimal.valueOf(3));

        // Calculate the average trend across all the months
        BigDecimal totalTrend = changes.stream().reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal averageTrend = totalTrend.divide(new BigDecimal(changes.size()), RoundingMode.HALF_UP);
        System.out.println("Average trend = " + averageTrend);

        // Start calculating the predicted expenses by using the last confirmed monthly expense
        BigDecimal predictedMonthExpense = monthlyPastExpenses.get(LocalDate.now().getMonth());
        System.out.println("Initial predicted month expense: " + LocalDate.now().getMonth() + " " +  monthlyPastExpenses.get(LocalDate.now().getMonth()));

        Map<Month, BigDecimal> predictedExpenses = new HashMap<>();
        for (int i = 1; i <= 12; i++) {
            Month futureMonth = LocalDate.now().plusMonths(i).getMonth();
            predictedMonthExpense = predictedMonthExpense.add(predictedMonthExpense.multiply(averageTrend));
            System.out.println("Predicted Month expense for: " + futureMonth + " = " + predictedMonthExpense);
            predictedExpenses.put(futureMonth, predictedMonthExpense);
        }

        // Reorder months so that it starts from the next month
        List<Month> monthsOrder = new ArrayList<>();
        LocalDate startMonth = LocalDate.now().plusMonths(1); // Start from next month
        for (int i = 0; i < 12; i++) {
            monthsOrder.add(startMonth.plusMonths(i).getMonth());
        }

        // Reorder the predictedExpenses map
        Map<Month, BigDecimal> reorderedPredictedExpenses = new LinkedHashMap<>();
        for (Month month : monthsOrder) {
            reorderedPredictedExpenses.put(month, predictedExpenses.getOrDefault(month, BigDecimal.ZERO));
        }

        return reorderedPredictedExpenses;
    }
}