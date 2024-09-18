package org.vaadin.example.views;

import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.Month;
import java.util.*;
import org.vaadin.addons.chartjs.ChartJs;
import org.vaadin.addons.chartjs.config.BarChartConfig;
import org.vaadin.addons.chartjs.config.LineChartConfig;
import org.vaadin.addons.chartjs.data.BarDataset;
import org.vaadin.addons.chartjs.data.Dataset;
import org.vaadin.addons.chartjs.data.LineDataset;
import org.vaadin.addons.chartjs.options.Position;
import org.vaadin.addons.chartjs.options.scale.Axis;
import org.vaadin.addons.chartjs.options.scale.LinearScale;
import org.vaadin.example.MainLayout;
import org.vaadin.example.service.ExpenseService;
import org.vaadin.example.service.IncomeService;
import org.vaadin.example.service.SessionService;
import org.vaadin.example.service.UserService;

@Route(value = "netCashflowForecast", layout = MainLayout.class)
public class NetCashflowForecastView extends VerticalLayout {
    private final ExpenseService expenseService;
    private final SessionService sessionService;
    private final UserService userService;
    private final IncomeService incomeService;
    private ChartJs expenseChart;
    private BarChartConfig expenseConfig;
    private ChartJs cashflowChart;
    private LineChartConfig cashflowConfig;
    private int previousMonths = 3;
    private Map<Month, BigDecimal> predictedExpenses;
    private Map<Month, BigDecimal> predictedCashflows;

  /**
   * Constructs a new NetCashflowForecastView and initializes the components and layout.
   *
   * @param expenseService the service used to manage expense data
   * @param sessionService the service used to manage session-related data, particularly the
   *     logged-in user
   * @param userService the service used to manage user data
   * @param incomeService the service used to manage income data
   */
  public NetCashflowForecastView(
      ExpenseService expenseService, SessionService sessionService, UserService userService, IncomeService incomeService) {
        this.expenseService = expenseService;
        this.sessionService = sessionService;
        this.userService = userService;
        this.incomeService = incomeService;
        this.predictedExpenses = new HashMap<>();
        this.predictedCashflows = new LinkedHashMap<>();

        Div chartContainer = new Div();
        chartContainer.setClassName("chart-container");
        Div cashflowContainer = new Div();
        cashflowContainer.setClassName("chart-container");

        configureExpenseChart();
        configureCashflowChart();
        updateExpenseChart();
        updateCashflowChart();

        HorizontalLayout noteLayout = new HorizontalLayout();
        noteLayout.add(expenseChart);//
        noteLayout.setHeight("100%");
        noteLayout.setWidth("100%");

        HorizontalLayout cashflowLayout = new HorizontalLayout();
        cashflowLayout.add(cashflowChart);//
        cashflowLayout.setHeight("100%");
        cashflowLayout.setWidth("100%");

        chartContainer.add(noteLayout);
        cashflowContainer.add(cashflowLayout);


        // Create a ComboBox for selecting months
        ComboBox<Integer> monthsComboBox = new ComboBox<>("Select Months", Arrays.asList(3, 6, 12));
        monthsComboBox.setValue(previousMonths); 
        monthsComboBox.addValueChangeListener(e -> {
            previousMonths = e.getValue();
            updateExpenseChart();
            updateCashflowChart();
        });

        add(new H1("Net Cashflow Forecast"), monthsComboBox);
        add(chartContainer);
        add(cashflowContainer);
    }

  /**
   * Configures the expense chart so that data can be added later on.
   */
  private void configureExpenseChart() {
        // Initialize the chart with an empty dataset
        expenseConfig = (BarChartConfig) new BarChartConfig()
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
                .position(Position.TOP)
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

        expenseChart = new ChartJs(expenseConfig);
        expenseChart.setWidth("100%");
    }

  /** Configures the cashflow chart so that data can be added later on. */
  private void configureCashflowChart() {
    // Initialize the chart with an empty dataset
    cashflowConfig =
        (LineChartConfig)
            new LineChartConfig()
                .data()
                .labels(new String[] {}) // Start with no labels
                .addDataset(
                    new LineDataset()
                        .type()
                        .label("Net Cashflow")
                        .fill(false) // Don't fill the area under the line
                        .lineTension(0.1) // Set the line tension
                        .backgroundColor("rgba(75, 192, 192, 0.4)") // Background color
                        .borderColor("rgba(75, 192, 192, 1)") // Border color
                        .borderWidth(2) // Border width
                        .borderCapStyle("butt") // Border cap style
                        .borderJoinStyle("miter") // Border join style
                        .pointBorderColor("rgba(75, 192, 192, 1)") // Point border color
                        .pointBackgroundColor("#fff") // Point background color
                        .pointBorderWidth(1) // Point border width
                        .pointHoverRadius(5) // Point hover radius
                        .pointHoverBackgroundColor(
                            "rgba(75, 192, 192, 1)") // Point hover background color
                        .pointHoverBorderColor("rgba(220, 220, 220, 1)") // Point hover border color
                        .pointHoverBorderWidth(2) // Point hover border width
                        .pointRadius(1) // Point radius
                        .pointHitRadius(10)
                        .data(new Double[] {}) // Start with no data
                    )
                .and()
                .options()
                .responsive(true)
                .maintainAspectRatio(false)
                .title()
                .display(true)
                .position(Position.TOP)
                .text("Projected Net Cashflow for the Next 12 Months")
                .and()
                .scales() // Add Y-axis configuration
                .add(Axis.Y, new LinearScale().ticks().and())
                .and()
                .done();

    cashflowChart = new ChartJs(cashflowConfig);
    cashflowChart.setWidth("100%");
  }

    /**
     * Adds or updates the expense chart data 
     */
    private void updateExpenseChart() {

        predictMonthlyExpenses(previousMonths);

        String[] labels = predictedExpenses.keySet().stream().map(Month::name).toArray(String[]::new);
        Double[] data = predictedExpenses.values().stream()
                .map(value -> value != null ? value.setScale(2, RoundingMode.HALF_UP).doubleValue() : 0.0)
                .toArray(Double[]::new);

    expenseConfig.data().labels(labels);

    for (Dataset<?, ?> ds : expenseConfig.data().getDatasets()) {
            if (ds instanceof BarDataset) {
                BarDataset bds = (BarDataset) ds;
                bds.dataAsList(List.of(data));
            }
        }

        expenseChart.update();
    }

  /** Adds or updates the cashflow chart data */
  private void updateCashflowChart() {

    predictNetCashflow();

    String[] labels = predictedCashflows.keySet().stream().map(Month::name).toArray(String[]::new);
    Double[] data =
        predictedCashflows.values().stream()
            .map(
                value ->
                    value != null ? value.setScale(2, RoundingMode.HALF_UP).doubleValue() : 0.0)
            .toArray(Double[]::new);

    cashflowConfig.data().labels(labels);

    for (Dataset<?, ?> ds : cashflowConfig.data().getDatasets()) {
      if (ds instanceof LineDataset) {
        LineDataset lds = (LineDataset) ds;
        lds.dataAsList(List.of(data));
      }
    }

    cashflowChart.update();
  }

  /**
   * Calculates and predicts the users net cashflow for the next 12 months based
   * on their total monthly incomes and predicted expenses
   */
  private void predictNetCashflow() {
    Long userId = sessionService.getLoggedInUserId();
    BigDecimal totalIncomePerMonth = incomeService.getTotalIncomeAllMonths(userId);

    Map<Month, BigDecimal> predictedExpensesCopy = new LinkedHashMap<>(predictedExpenses);
    for (Map.Entry<Month, BigDecimal> entry : predictedExpensesCopy.entrySet()) {
        BigDecimal predictedNetCashflow = totalIncomePerMonth.subtract(entry.getValue());
        predictedExpensesCopy.put(entry.getKey(), predictedNetCashflow);
    }

    predictedCashflows = predictedExpensesCopy;
}

    /**
     * Calculates and predicts the user's expenses for the next 12 months (assuming a linear trend)
     * based on the expenses of the last `previousMonths`.
     * 
     * @param previousMonths the number of months that the predicted expenses should be based on
     * @return
     */
    private void predictMonthlyExpenses(int previousMonths) {
       Long userId = sessionService.getLoggedInUserId();

        Map<Month, BigDecimal> monthlyPastExpenses = expenseService.getExpensesForPreviousMonths(userId, previousMonths);

        // Convert the map entries to a list and sort by month
        List<Map.Entry<Month, BigDecimal>> sortedEntries = new ArrayList<>(monthlyPastExpenses.entrySet());
        sortedEntries.sort(Comparator.comparing(Map.Entry::getKey));

        List<BigDecimal> expensesList = new ArrayList<>();
        for (Map.Entry<Month, BigDecimal> entry : sortedEntries) {
            expensesList.add(entry.getValue());
        }

        List<BigDecimal> changes = new ArrayList<>();

        // Calculate the expense changes between consecutive months
        for (int i = 1; i < expensesList.size(); i++) {
            BigDecimal monthExpense = expensesList.get(i);
            BigDecimal previousMonthExpense = expensesList.get(i - 1);

            BigDecimal currentChange;
            if (previousMonthExpense.compareTo(BigDecimal.ZERO) != 0 && monthExpense.compareTo(BigDecimal.ZERO) != 0) {
                currentChange = monthExpense.subtract(previousMonthExpense);
            } else {
                currentChange = BigDecimal.ZERO; 
            }
            changes.add(currentChange);
        }

        BigDecimal totalChange = changes.stream().reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal averageChange = totalChange.divide(new BigDecimal(expensesList.size() - 1), RoundingMode.HALF_EVEN);


        Map<Month, BigDecimal> newPredictedExpenses = new HashMap<>();
        for (int i = 1; i <= 12; i++) {
            Month futureMonth = LocalDate.now().plusMonths(i).getMonth();
            BigDecimal predictedMonthExpense = monthlyPastExpenses.get(LocalDate.now().getMonth()).add((averageChange.multiply(new BigDecimal(i))));
            newPredictedExpenses.put(futureMonth, predictedMonthExpense);
        }

     predictedExpenses = reorderMonths(newPredictedExpenses);
    }


    /**
     * Reorders the months in the map so that the start from the next month of the current date.
     * @param predictedExpenses Map of months and the values associated with that month
     * @return
     */
    private Map<Month, BigDecimal> reorderMonths(Map<Month, BigDecimal> predictedExpenses) {
        // Reorder months so that it starts from the next month of the current month
        List<Month> monthsOrder = new ArrayList<>();
        LocalDate startMonth = LocalDate.now().plusMonths(1);
        for (int i = 0; i < 12; i++) {
            monthsOrder.add(startMonth.plusMonths(i).getMonth());
        }

        Map<Month, BigDecimal> reorderedPredictedExpenses = new LinkedHashMap<>();
        for (Month month : monthsOrder) {
            reorderedPredictedExpenses.put(month, predictedExpenses.getOrDefault(month, BigDecimal.ZERO));
        }
        return reorderedPredictedExpenses;
    }
}
