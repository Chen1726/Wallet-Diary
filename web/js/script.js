const navButtons = document.querySelectorAll('.nav-btn');
const mobileNavButtons = document.querySelectorAll('.mobile-nav-btn');
const sectionButtons = document.querySelectorAll('[data-section-target]');
const sections = document.querySelectorAll('.page-section');

const formatCurrency = (value) => {
  const amount = Number(value || 0);
  return new Intl.NumberFormat('en-IN', {
    style: 'currency',
    currency: 'INR',
    minimumFractionDigits: 2,
    maximumFractionDigits: 2
  }).format(amount);
};

const formatDate = (dateText) => {
  if (!dateText) return '—';
  const parsedDate = new Date(`${dateText}T00:00:00`);
  if (Number.isNaN(parsedDate.getTime())) {
    return dateText;
  }
  return parsedDate.toLocaleDateString('en-IN', {
    day: '2-digit',
    month: 'short',
    year: 'numeric'
  });
};

const getCategoryClass = (category = '') => {
  const normalized = (category || '').toLowerCase();
  if (normalized.includes('food')) return 'category-food';
  if (normalized.includes('travel')) return 'category-travel';
  if (normalized.includes('shop')) return 'category-shopping';
  if (normalized.includes('edu')) return 'category-education';
  if (normalized.includes('bill')) return 'category-bills';
  return 'category-other';
};

const getCategoryIcon = (category = 'Other') => {
  const normalized = (category || 'Other').toLowerCase();
  if (normalized.includes('food')) return '🍔';
  if (normalized.includes('travel')) return '✈️';
  if (normalized.includes('shop')) return '🛍️';
  if (normalized.includes('edu')) return '📚';
  if (normalized.includes('bill')) return '🧾';
  return '💳';
};

const getCategoryColor = (category = 'Other') => {
  const normalized = (category || 'Other').toLowerCase();
  if (normalized.includes('food')) return '#1a8f88';
  if (normalized.includes('travel')) return '#5a7ef7';
  if (normalized.includes('shop')) return '#f2994a';
  if (normalized.includes('edu')) return '#7a5af8';
  if (normalized.includes('bill')) return '#f2c94c';
  return '#7d8ea3';
};

const setStatus = (elementId, message, type = 'info') => {
  const element = document.getElementById(elementId);
  if (!element) return;

  element.textContent = message;
  element.classList.remove('success', 'error', 'info');
  if (type) {
    element.classList.add(type);
  }
};

const clearStatus = (elementId) => {
  const element = document.getElementById(elementId);
  if (element) {
    element.textContent = '';
    element.classList.remove('success', 'error', 'info');
  }
};

const fetchJson = async (url, options = {}) => {
  try {
    const response = await fetch(url, {
      headers: {
        'Content-Type': 'application/json',
        Accept: 'application/json',
        ...(options.headers || {})
      },
      ...options
    });

    const text = await response.text();
    const data = text ? JSON.parse(text) : {};

    if (!response.ok) {
      throw new Error(data.error || 'Request failed. Please try again.');
    }

    return data;
  } catch (error) {
    if (error instanceof TypeError) {
      throw new Error('Unable to connect to the WalletDiary server. Please make sure it is running.');
    }
    throw error;
  }
};

const getCategoryTotals = (expenses) => {
  return Object.entries(
    expenses.reduce((totals, expense) => {
      const category = expense.category || 'Other';
      totals[category] = (totals[category] || 0) + Number(expense.amount || 0);
      return totals;
    }, {})
  ).sort((a, b) => b[1] - a[1]);
};

const getTopCategory = (expenses) => {
  const totals = getCategoryTotals(expenses);
  return totals.length ? totals[0] : null;
};

const applyCategoryChart = (chartElement, legendElement, expenses) => {
  if (!chartElement || !legendElement) return;

  const totals = getCategoryTotals(expenses);
  const totalAmount = totals.reduce((sum, [, value]) => sum + value, 0);

  if (!totals.length || totalAmount <= 0) {
    chartElement.style.background = 'conic-gradient(#dfe8f1 0 100%)';
    legendElement.innerHTML = '<div class="legend-item"><span class="legend-meta"><span class="legend-swatch" style="background:#dfe8f1"></span>No spending data</span><span class="legend-value">₹ 0.00</span></div>';
    return;
  }

  let current = 0;
  const gradient = totals.map(([category, value]) => {
    const amount = Number(value || 0);
    const percentage = totalAmount > 0 ? (amount / totalAmount) * 100 : 0;
    const start = current;
    const end = current + percentage;
    current = end;
    return `${getCategoryColor(category)} ${start}% ${end}%`;
  });

  chartElement.style.background = `conic-gradient(${gradient.join(', ')})`;

  const topCategory = totals[0];
  const centerValue = document.getElementById(chartElement.id === 'categoryChart' ? 'categoryLargestValue' : 'summaryLargestValue');
  if (centerValue) {
    centerValue.textContent = formatCurrency(topCategory[1]);
  }

  legendElement.innerHTML = totals
    .map(([category, value]) => {
      const percentage = totalAmount > 0 ? ((value / totalAmount) * 100).toFixed(1) : '0.0';
      return `
        <div class="legend-item">
          <span class="legend-meta"><span class="legend-swatch" style="background:${getCategoryColor(category)}"></span>${category}</span>
          <span class="legend-value">${percentage}%</span>
        </div>
      `;
    })
    .join('');
};

const renderRecentActivity = (listElement, expenses) => {
  if (!listElement) return;

  const recent = [...expenses]
    .sort((a, b) => new Date(b.expenseDate) - new Date(a.expenseDate))
    .slice(0, 5);

  if (!recent.length) {
    listElement.innerHTML = '<li class="empty-state">No recent expenses yet.</li>';
    return;
  }

  listElement.innerHTML = recent
    .map((expense) => {
      const category = expense.category || 'Other';
      return `
        <li>
          <div class="activity-main">
            <div class="activity-icon ${getCategoryClass(category)}">${getCategoryIcon(category)}</div>
            <div class="activity-text">
              <strong>${expense.description || 'No description'}</strong>
              <span>${category} · ${formatDate(expense.expenseDate)}</span>
            </div>
          </div>
          <div class="activity-meta">
            <strong>${formatCurrency(expense.amount)}</strong>
            <span>#${expense.expenseId}</span>
          </div>
        </li>
      `;
    })
    .join('');
};

const updateDashboardMetrics = (expenses) => {
  const totalAmount = expenses.reduce((sum, expense) => sum + Number(expense.amount || 0), 0);
  const totalExpenses = expenses.length;
  const average = totalExpenses ? totalAmount / totalExpenses : 0;
  const topCategory = getTopCategory(expenses);

  const dashboardTotalExpenses = document.getElementById('dashboardTotalExpenses');
  const dashboardTotalAmount = document.getElementById('dashboardTotalAmount');
  const dashboardAverageExpense = document.getElementById('dashboardAverageExpense');
  const dashboardTopCategory = document.getElementById('dashboardTopCategory');
  const heroTotalSpending = document.getElementById('heroTotalSpending');

  if (dashboardTotalExpenses) dashboardTotalExpenses.textContent = String(totalExpenses);
  if (dashboardTotalAmount) dashboardTotalAmount.textContent = formatCurrency(totalAmount);
  if (dashboardAverageExpense) dashboardAverageExpense.textContent = formatCurrency(average);
  if (dashboardTopCategory) dashboardTopCategory.textContent = topCategory ? topCategory[0] : '—';
  if (heroTotalSpending) heroTotalSpending.textContent = formatCurrency(totalAmount);

  applyCategoryChart(document.getElementById('categoryChart'), document.getElementById('categoryLegend'), expenses);
  renderRecentActivity(document.getElementById('recentActivityList'), expenses);
};

const updateSummaryMetrics = (expenses, summaryData = {}) => {
  const totalAmount = Number(summaryData.totalAmount || expenses.reduce((sum, expense) => sum + Number(expense.amount || 0), 0));
  const totalExpenses = Number(summaryData.totalExpenses || expenses.length || 0);
  const average = totalExpenses ? totalAmount / totalExpenses : 0;
  const topCategory = getTopCategory(expenses);

  const summaryTotalAmount = document.getElementById('summaryTotalAmount');
  const summaryTotalExpenses = document.getElementById('summaryTotalExpenses');
  const summaryAverageExpense = document.getElementById('summaryAverageExpense');
  const summaryTopCategory = document.getElementById('summaryTopCategory');

  if (summaryTotalAmount) summaryTotalAmount.textContent = formatCurrency(totalAmount);
  if (summaryTotalExpenses) summaryTotalExpenses.textContent = String(totalExpenses);
  if (summaryAverageExpense) summaryAverageExpense.textContent = formatCurrency(average);
  if (summaryTopCategory) summaryTopCategory.textContent = topCategory ? topCategory[0] : '—';

  applyCategoryChart(document.getElementById('summaryCategoryChart'), document.getElementById('summaryCategoryLegend'), expenses);
  renderRecentActivity(document.getElementById('summaryRecentList'), expenses);
};

const setActiveSection = (targetSection) => {
  navButtons.forEach((button) => button.classList.toggle('active', button.dataset.section === targetSection));
  mobileNavButtons.forEach((button) => button.classList.toggle('active', button.dataset.section === targetSection));
  sections.forEach((section) => section.classList.toggle('active', section.id === targetSection));

  const pageTitle = document.getElementById('pageTitle');
  if (pageTitle) {
    const titles = {
      dashboard: 'Overview',
      'view-expenses': 'Transactions',
      'add-expense': 'Add Expense',
      'search-expense': 'Search',
      summary: 'Analytics'
    };
    pageTitle.textContent = titles[targetSection] || 'Overview';
  }
};

const showToast = (message, type = 'success') => {
  const container = document.getElementById('toastContainer');
  if (!container) return;

  const toast = document.createElement('div');
  toast.className = `toast ${type === 'info' ? 'toast-info' : type}`;
  toast.textContent = message;
  container.appendChild(toast);

  setTimeout(() => {
    toast.remove();
  }, 2600);
};

const loadDashboard = async () => {
  try {
    const [dashboardData, expenses] = await Promise.all([
      fetchJson('/api/dashboard'),
      fetchJson('/api/expenses')
    ]);

    const actualExpenses = Array.isArray(expenses) ? expenses : [];
    updateDashboardMetrics(actualExpenses);

    const totalAmount = Number(dashboardData.totalAmount || 0);
    const totalExpenses = Number(dashboardData.totalExpenses || actualExpenses.length || 0);
    const average = totalExpenses ? totalAmount / totalExpenses : 0;
    const topCategory = getTopCategory(actualExpenses);

    document.getElementById('dashboardTotalExpenses').textContent = String(totalExpenses);
    document.getElementById('dashboardTotalAmount').textContent = formatCurrency(totalAmount);
    document.getElementById('dashboardAverageExpense').textContent = formatCurrency(average);
    document.getElementById('dashboardTopCategory').textContent = topCategory ? topCategory[0] : '—';
    document.getElementById('heroTotalSpending').textContent = formatCurrency(totalAmount);
  } catch (error) {
    console.error(error);
    updateDashboardMetrics([]);
  }
};

const loadExpensesTable = async () => {
  const tbody = document.getElementById('expenseTableBody');
  const status = document.getElementById('expensesTableStatus');

  status.textContent = 'Loading expenses...';
  status.className = 'status-message info';
  tbody.innerHTML = '<tr><td colspan="6" class="empty-state">Loading expenses...</td></tr>';

  try {
    const expenses = await fetchJson('/api/expenses');

    if (!expenses || expenses.length === 0) {
      tbody.innerHTML = '<tr><td colspan="6" class="empty-state">No expenses found.</td></tr>';
      status.textContent = 'No expenses found.';
      status.className = 'status-message info';
      return;
    }

    tbody.innerHTML = expenses
      .map((expense) => `
        <tr>
          <td>${expense.expenseId}</td>
          <td>${formatCurrency(expense.amount)}</td>
          <td><span class="category-pill ${getCategoryClass(expense.category)}">${expense.category}</span></td>
          <td>${expense.description || '—'}</td>
          <td>${formatDate(expense.expenseDate)}</td>
          <td>
            <button class="mini-btn edit-btn" data-id="${expense.expenseId}" data-action="edit">Edit</button>
            <button class="mini-btn danger delete-btn" data-id="${expense.expenseId}" data-action="delete">Delete</button>
          </td>
        </tr>
      `)
      .join('');

    document.querySelectorAll('.edit-btn').forEach((button) => {
      button.addEventListener('click', () => {
        const id = Number(button.dataset.id);
        const expense = expenses.find((item) => item.expenseId === id);
        if (!expense) return;
        openEditModal(expense);
      });
    });

    document.querySelectorAll('.delete-btn').forEach((button) => {
      button.addEventListener('click', async () => {
        const id = Number(button.dataset.id);
        const confirmed = window.confirm('Are you sure you want to delete this expense?');
        if (!confirmed) return;

        try {
          const result = await fetchJson(`/api/expenses/${id}`, { method: 'DELETE' });
          status.textContent = result.message || 'Expense deleted successfully.';
          status.className = 'status-message success';
          showToast('Expense deleted successfully.', 'success');
          await refreshAllData();
        } catch (error) {
          console.error(error);
          status.textContent = error.message;
          status.className = 'status-message error';
          showToast(error.message || 'Something went wrong. Please try again.', 'error');
        }
      });
    });

    status.textContent = `Loaded ${expenses.length} expense${expenses.length === 1 ? '' : 's'}.`;
    status.className = 'status-message success';
  } catch (error) {
    console.error(error);
    tbody.innerHTML = '<tr><td colspan="6" class="empty-state">Unable to load expenses.</td></tr>';
    status.textContent = 'Unable to load expenses. Please try again.';
    status.className = 'status-message error';
  }
};

const loadSummary = async () => {
  try {
    const [summaryData, expenses] = await Promise.all([
      fetchJson('/api/summary'),
      fetchJson('/api/expenses')
    ]);

    const actualExpenses = Array.isArray(expenses) ? expenses : [];
    updateSummaryMetrics(actualExpenses, summaryData);
  } catch (error) {
    console.error(error);
    updateSummaryMetrics([]);
  }
};

const refreshAllData = async () => {
  await Promise.all([loadDashboard(), loadExpensesTable(), loadSummary()]);
};

const validateExpenseInput = ({ amount, category, expenseDate }) => {
  if (!amount || Number(amount) <= 0) {
    return 'Amount must be a valid positive number.';
  }

  if (!category || !category.trim()) {
    return 'Category is required.';
  }

  if (!expenseDate) {
    return 'Expense date is required.';
  }

  return '';
};

const handleAddExpense = async (event) => {
  event.preventDefault();
  clearStatus('addExpenseMessage');

  const form = event.target;
  const payload = {
    amount: form.amount.value,
    category: form.category.value,
    description: form.description.value,
    expenseDate: form.expenseDate.value
  };

  const validationMessage = validateExpenseInput(payload);
  if (validationMessage) {
    setStatus('addExpenseMessage', validationMessage, 'error');
    return;
  }

  const submitButton = document.getElementById('addExpenseBtn');
  submitButton.disabled = true;
  submitButton.textContent = 'Saving...';
  setStatus('addExpenseMessage', 'Saving expense...', 'info');

  try {
    const result = await fetchJson('/api/expenses', {
      method: 'POST',
      body: JSON.stringify(payload)
    });

    setStatus('addExpenseMessage', `Expense added successfully. New ID: ${result.expenseId}`, 'success');
    showToast('Expense added successfully ✓', 'success');
    form.reset();
    await refreshAllData();
  } catch (error) {
    console.error(error);
    setStatus('addExpenseMessage', error.message, 'error');
    showToast(error.message || 'Something went wrong. Please try again.', 'error');
  } finally {
    submitButton.disabled = false;
    submitButton.textContent = 'Save Expense';
  }
};

const openEditModal = (expense) => {
  const modal = document.getElementById('editExpenseModal');
  document.getElementById('editExpenseId').value = expense.expenseId;
  document.getElementById('editAmount').value = Number(expense.amount).toFixed(2);
  document.getElementById('editCategory').value = expense.category || '';
  document.getElementById('editDescription').value = expense.description || '';
  document.getElementById('editDate').value = expense.expenseDate || '';
  clearStatus('editExpenseMessage');
  modal.classList.remove('hidden');
  modal.setAttribute('aria-hidden', 'false');
};

const closeEditModal = () => {
  const modal = document.getElementById('editExpenseModal');
  modal.classList.add('hidden');
  modal.setAttribute('aria-hidden', 'true');
  document.getElementById('editExpenseForm').reset();
  clearStatus('editExpenseMessage');
};

const handleUpdateExpense = async (event) => {
  event.preventDefault();
  clearStatus('editExpenseMessage');

  const form = event.target;
  const expenseId = document.getElementById('editExpenseId').value;
  const payload = {
    amount: form.editAmount.value,
    category: form.editCategory.value,
    description: form.editDescription.value,
    expenseDate: form.editDate.value
  };

  const validationMessage = validateExpenseInput(payload);
  if (validationMessage) {
    setStatus('editExpenseMessage', validationMessage, 'error');
    return;
  }

  const saveButton = form.querySelector('button[type="submit"]');
  saveButton.disabled = true;
  saveButton.textContent = 'Saving...';
  setStatus('editExpenseMessage', 'Updating expense...', 'info');

  try {
    const result = await fetchJson(`/api/expenses/${expenseId}`, {
      method: 'PUT',
      body: JSON.stringify(payload)
    });

    setStatus('editExpenseMessage', result.message || 'Expense updated successfully.', 'success');
    showToast('Expense updated successfully.', 'success');
    closeEditModal();
    await refreshAllData();
  } catch (error) {
    console.error(error);
    setStatus('editExpenseMessage', error.message, 'error');
    showToast(error.message || 'Unable to update expense.', 'error');
  } finally {
    saveButton.disabled = false;
    saveButton.textContent = 'Save Changes';
  }
};

const handleSearchExpense = async () => {
  const query = document.getElementById('searchCategory').value.trim();
  const resultsBox = document.getElementById('searchResults');
  const searchButton = document.getElementById('searchExpenseBtn');

  if (!query) {
    resultsBox.innerHTML = '<p>Please enter a category or description to search.</p>';
    return;
  }

  searchButton.disabled = true;
  searchButton.textContent = 'Searching...';
  resultsBox.innerHTML = '<p>Searching expenses...</p>';

  try {
    const expenses = await fetchJson('/api/expenses');
    const searchTerm = query.toLowerCase();
    const matches = expenses.filter((expense) => {
      const categoryMatch = (expense.category || '').toLowerCase().includes(searchTerm);
      const descriptionMatch = (expense.description || '').toLowerCase().includes(searchTerm);
      return categoryMatch || descriptionMatch;
    });

    if (!matches.length) {
      resultsBox.innerHTML = '<p>No expenses found.</p><p>Try another category or description.</p>';
      return;
    }

    resultsBox.innerHTML = `
      <div class="search-result-meta">${matches.length} result${matches.length === 1 ? '' : 's'}</div>
      <ul class="search-result-list">
        ${matches
          .map((expense) => `
            <li>
              <div class="search-result-main">
                <strong>${expense.description || 'No description'}</strong>
                <span>${expense.category} · ${formatDate(expense.expenseDate)}</span>
              </div>
              <strong>${formatCurrency(expense.amount)}</strong>
            </li>
          `)
          .join('')}
      </ul>
    `;
  } catch (error) {
    console.error(error);
    resultsBox.innerHTML = `<p>${error.message}</p>`;
  } finally {
    searchButton.disabled = false;
    searchButton.textContent = 'Search';
  }
};

const clearSearch = () => {
  document.getElementById('searchCategory').value = '';
  document.getElementById('searchResults').innerHTML = '<p>Search results will appear here.</p>';
};

navButtons.forEach((button) => {
  button.addEventListener('click', () => setActiveSection(button.dataset.section));
});

mobileNavButtons.forEach((button) => {
  button.addEventListener('click', () => setActiveSection(button.dataset.section));
});

sectionButtons.forEach((button) => {
  button.addEventListener('click', () => setActiveSection(button.dataset.sectionTarget));
});

document.getElementById('addExpenseForm').addEventListener('submit', handleAddExpense);
document.getElementById('searchExpenseBtn').addEventListener('click', handleSearchExpense);
document.getElementById('clearSearchBtn').addEventListener('click', clearSearch);
document.getElementById('editExpenseForm').addEventListener('submit', handleUpdateExpense);
document.getElementById('closeEditModalBtn').addEventListener('click', closeEditModal);
document.getElementById('cancelEditBtn').addEventListener('click', closeEditModal);
window.addEventListener('click', (event) => {
  const modal = document.getElementById('editExpenseModal');
  if (event.target === modal) {
    closeEditModal();
  }
});

setActiveSection('dashboard');
refreshAllData();
