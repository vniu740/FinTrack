package org.vaadin.example.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Month;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.vaadin.example.model.Expense;
import org.vaadin.example.repository.ExpenseRepository;

/**
 * Service class for managing expense-related operations.
 * This class interacts with the {@link ExpenseRepository} to perform CRUD operations on {@link Expense} entities.
 */
@Service
public class ExpenseService {

    @Autowired
    private ExpenseRepository expenseRepository;

    /**
     * Retrieves a list of expenses associated with a specific user ID.
     *
     * @param userId the ID of the user whose expenses are to be retrieved
     * @return a list of expenses associated with the specified user ID
     */
    public List<Expense> getExpensesByUserId(Long userId) {
        return expenseRepository.findByUserId(userId);
    }

    /**
     * Adds a new expense to the repository.
     *
     * @param expense the expense object to be added
     * @return the newly added expense object
     */
    public Expense addExpense(Expense expense) {
        return expenseRepository.save(expense);
    }

    /**
     * Finds an expense by its ID.
     *
     * @param id the ID of the expense to find
     * @return the expense object if found, or null if not found
     */
    public Expense findExpenseById(Long id) {
        return expenseRepository.findById(id).orElse(null);
    }

    /**
     * Deletes an expense by its ID.
     *
     * @param id the ID of the expense to be deleted
     */
    public void deleteExpense(Long id) {
        expenseRepository.deleteById(id);
    }

    /**
     * Calculates the total expenses for the current month for a specific user.
     *
     * @param userId the ID of the user whose total expenses are to be calculated
     * @return the total amount of expenses for the user in the current month
     */
    public BigDecimal getTotalExpensesForCurrentMonth(Long userId) {
        LocalDate startOfMonth = LocalDate.now().withDayOfMonth(1);
        LocalDate endOfMonth = LocalDate.now().withDayOfMonth(LocalDate.now().lengthOfMonth());
       
        return expenseRepository.findTotalExpensesForPeriod(userId, startOfMonth, endOfMonth);
    }

    /**
     * Retrieves a list of expenses associated with a specific budget ID.
     *
     * @param budgetId the ID of the budget whose expenses are to be retrieved
     * @return a list of expenses associated with the specified budget ID
     */
    public List<Expense> getExpensesByBudget(Long budgetId) {
        return expenseRepository.findByBudgetId(budgetId);
    }

    /**
     * Updates an existing expense in the repository.
     *
     * @param expense the expense object with updated information
     * @return the updated expense object
     */
    public Expense updateExpense(Expense expense) {
        if (expenseRepository.existsById(expense.getId())) {
            return expenseRepository.save(expense);
        } else {
            throw new IllegalArgumentException("Expense not found with ID: " + expense.getId());
        }
    }

  /**
   * Retrieves a map of expenses in the previous x months for a particular user
   *
   * @param userId the ID of the user whose total expenses are to be calculated
   * @param previousMonths the number of previous months that expenses should be retrieved for
   * @return
   */
  public Map<Month, BigDecimal> getExpensesForPreviousMonths(Long userId, int previousMonths) {
        Map<Month, BigDecimal> monthlyExpenses = new HashMap<>();
        List<Expense> expenses = getExpensesByUserId(userId);

        LocalDate dateOfCurrentMonth = LocalDate.now();
        LocalDate dateOfFirstMonth = dateOfCurrentMonth.minusMonths(previousMonths);

        for (int i = 0; i <  previousMonths; i++) {
            LocalDate monthDate = dateOfFirstMonth.plusMonths(i);
            Month month = Month.of(monthDate.getMonthValue());
            monthlyExpenses.put(month, BigDecimal.ZERO);
        }

        for (Expense expense : expenses) {
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(expense.getDate());
            LocalDate expenseDate = LocalDate.of(
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH) + 1,
                    calendar.get(Calendar.DAY_OF_MONTH)
            );

            if (!expenseDate.isBefore(dateOfFirstMonth.minusMonths(1)) && !expenseDate.isAfter(dateOfCurrentMonth)) {
                Month expenseMonth = Month.of(expenseDate.getMonthValue());
                monthlyExpenses.put(expenseMonth, monthlyExpenses.getOrDefault(expenseMonth, BigDecimal.ZERO).add(expense.getAmount()));
            }
        }

        return  monthlyExpenses;
    }

}
