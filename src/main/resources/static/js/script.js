document.addEventListener("DOMContentLoaded", () => {
    // ── 1. Card Animation (Subtle entry effect) ─────────────────────
    const cards = document.querySelectorAll(
        ".card, .transaction, .form-card, .report-card, .table-card, .budget-card"
    );
    cards.forEach((card, index) => {
        card.style.opacity = "0";
        card.style.transform = "translateY(20px)";
        setTimeout(() => {
            card.style.transition = "all 0.5s ease";
            card.style.opacity = "1";
            card.style.transform = "translateY(0)";
        }, index * 80);
    });

    // ── 2. Active Navbar Highlight ─────────────────────────────────
    const currentPath = window.location.pathname;
    document.querySelectorAll(".nav-links a").forEach(link => {
        const href = link.getAttribute("href");
        if (currentPath === href || (currentPath === "/" && href === "/dashboard")) {
            link.style.color = "#2e7d32";
            link.style.fontWeight = "600";
            link.style.borderBottom = "2px solid #2e7d32";
            link.style.paddingBottom = "5px";
        }
    });

    // ── 3. Page Routing Logic ──────────────────────────────────────
    if (currentPath === "/" || currentPath === "/dashboard") {
        initDashboard();
    } else if (currentPath === "/add-expense") {
        initAddExpense();
    } else if (currentPath === "/budget") {
        initBudget();
    } else if (currentPath === "/reports") {
        initReports();
    }
});

// ── 4. Helper Function: Get overall monthly budget ────────────────
function getMonthlyBudgetLimit() {
    const budget = localStorage.getItem("overallBudget");
    return budget ? parseFloat(budget) : 25000; // Default: ₹25,000
}

// ── 5. DASHBOARD PAGE LOGIC ───────────────────────────────────────
async function initDashboard() {
    const limit = getMonthlyBudgetLimit();
    try {
        // Fetch budget details from server (passes the overall budget)
        const budgetRes = await fetch(`/api/budget?amount=${limit}`);
        if (budgetRes.ok) {
            const data = await budgetRes.json();
            document.getElementById("display-budget").textContent = `₹${data.budget.toLocaleString('en-IN')}`;
            document.getElementById("display-spent").textContent = `₹${data.totalSpent.toLocaleString('en-IN')}`;
            document.getElementById("display-remaining").textContent = `₹${data.remaining.toLocaleString('en-IN')}`;
            
            // Adjust color of remaining text if over budget
            const remEl = document.getElementById("display-remaining");
            if (data.isOver) {
                remEl.style.color = "#d32f2f";
            } else {
                remEl.style.color = "#2e7d32";
            }
        }

        // Fetch recent expenses
        const expensesRes = await fetch("/api/expenses");
        if (expensesRes.ok) {
            const expenses = await expensesRes.json();
            const listContainer = document.getElementById("transactions-list");
            listContainer.innerHTML = "";

            if (expenses.length === 0) {
                listContainer.innerHTML = `<p style="color: var(--muted); text-align: center; margin-top: 20px;">No expenses tracked yet. Add one!</p>`;
                return;
            }

            // Display latest 5 expenses (newest first)
            const sorted = [...expenses].reverse().slice(0, 5);
            sorted.forEach(exp => {
                const item = document.createElement("div");
                item.className = "transaction";
                item.innerHTML = `
                    <div>
                        <h4>${escapeHTML(exp.desc)}</h4>
                        <span>${escapeHTML(exp.cat)} | ${escapeHTML(exp.date)}</span>
                    </div>
                    <strong>₹${exp.amount.toLocaleString('en-IN')}</strong>
                `;
                listContainer.appendChild(item);
            });
        }
    } catch (err) {
        console.error("Dashboard init error:", err);
    }
}

// ── 6. ADD EXPENSE PAGE LOGIC ─────────────────────────────────────
function initAddExpense() {
    const dateInput = document.getElementById("expense-date");
    if (dateInput) {
        // Default to today's date
        dateInput.value = new Date().toISOString().split("T")[0];
    }

    const form = document.getElementById("add-expense-form");
    if (form) {
        form.addEventListener("submit", async (e) => {
            e.preventDefault();
            const desc = document.getElementById("expense-desc").value.trim();
            const amount = parseFloat(document.getElementById("expense-amount").value);
            const cat = document.getElementById("expense-cat").value;
            const date = document.getElementById("expense-date").value;

            if (!desc || isNaN(amount) || amount <= 0 || !date) {
                alert("Please fill all fields with valid data.");
                return;
            }

            try {
                const res = await fetch("/api/expenses", {
                    method: "POST",
                    headers: { "Content-Type": "application/json" },
                    body: JSON.stringify({ amount, cat, date, desc })
                });

                if (res.ok) {
                    window.location.href = "/dashboard";
                } else {
                    const err = await res.json();
                    alert(`Error: ${err.error || "Failed to add expense."}`);
                }
            } catch (err) {
                console.error("Error adding expense:", err);
                alert("Failed to submit expense. Is the server running?");
            }
        });
    }
}

// ── 7. BUDGET PAGE LOGIC ──────────────────────────────────────────
async function initBudget() {
    updateBudgetUI();

    // Overall budget update handler
    const setBudgetForm = document.getElementById("set-budget-form");
    if (setBudgetForm) {
        const input = document.getElementById("monthly-budget-input");
        input.value = getMonthlyBudgetLimit();

        setBudgetForm.addEventListener("submit", (e) => {
            e.preventDefault();
            const val = parseFloat(input.value);
            if (val > 0) {
                localStorage.setItem("overallBudget", val);
                updateBudgetUI();
                alert("Overall monthly budget updated successfully!");
            }
        });
    }

    // Category budget limit handler
    const categoryBudgetForm = document.getElementById("category-budget-form");
    if (categoryBudgetForm) {
        categoryBudgetForm.addEventListener("submit", async (e) => {
            e.preventDefault();
            const cat = document.getElementById("category-select").value;
            const limit = parseFloat(document.getElementById("category-limit-input").value);

            if (isNaN(limit) || limit <= 0) {
                alert("Please specify a valid limit.");
                return;
            }

            try {
                const res = await fetch("/api/budget/set", {
                    method: "POST",
                    headers: { "Content-Type": "application/json" },
                    body: JSON.stringify({ cat, limit })
                });

                if (res.ok) {
                    alert(`Budget limit set for ${cat} to ₹${limit.toLocaleString('en-IN')} successfully!`);
                    document.getElementById("category-limit-input").value = "";
                    updateBudgetUI(); // Refresh calculations
                } else {
                    alert("Failed to set category limit.");
                }
            } catch (err) {
                console.error("Category budget set error:", err);
            }
        });
    }
}

async function updateBudgetUI() {
    const limit = getMonthlyBudgetLimit();
    try {
        const res = await fetch(`/api/budget?amount=${limit}`);
        if (res.ok) {
            const data = await res.json();
            
            document.getElementById("budget-amount").textContent = `₹${data.budget.toLocaleString('en-IN')}`;
            document.getElementById("budget-spent-text").textContent = `Spent ₹${data.totalSpent.toLocaleString('en-IN')} of ₹${data.budget.toLocaleString('en-IN')}`;
            document.getElementById("budget-remaining").textContent = `₹${data.remaining.toLocaleString('en-IN')}`;
            
            const progressFill = document.getElementById("budget-progress-fill");
            const cappedPercentage = Math.min(data.percentage, 100);
            progressFill.style.width = `${cappedPercentage}%`;

            const statusEl = document.getElementById("budget-status");
            if (data.isOver) {
                statusEl.textContent = "⚠ Over Budget";
                statusEl.style.background = "#ffebee";
                statusEl.style.color = "#d32f2f";
                progressFill.style.background = "#d32f2f";
            } else {
                statusEl.textContent = "✓ On Track";
                statusEl.style.background = "#e8f5e9";
                statusEl.style.color = "#2e7d32";
                progressFill.style.background = "#2e7d32";
            }
        }
    } catch (err) {
        console.error("Error updating budget UI:", err);
    }
}

// ── 8. REPORTS PAGE LOGIC ─────────────────────────────────────────
async function initReports() {
    await loadReportCards();
    await loadExpensesTable("default");

    const sortBySelect = document.getElementById("sort-by");
    if (sortBySelect) {
        sortBySelect.addEventListener("change", (e) => {
            loadExpensesTable(e.target.value);
        });
    }
}

async function loadReportCards() {
    const limit = getMonthlyBudgetLimit();
    try {
        // Fetch stats
        const statsRes = await fetch("/api/stats");
        let totalSpent = 0;
        let topCat = "None";

        if (statsRes.ok) {
            const stats = await statsRes.json();
            totalSpent = stats.totalSpent;
            topCat = stats.topCategory || "None";
            
            document.getElementById("report-total-spent").textContent = `₹${totalSpent.toLocaleString('en-IN')}`;
            document.getElementById("report-top-category").textContent = escapeHTML(topCat);
        }

        // Fetch summary for count and category breakdown
        const summaryRes = await fetch("/api/summary");
        if (summaryRes.ok) {
            const summary = await summaryRes.json();
            document.getElementById("report-transactions-count").textContent = summary.count;

            // Render breakdown
            const breakdownContainer = document.getElementById("category-breakdown");
            breakdownContainer.innerHTML = "";

            const categories = Object.keys(summary.byCategory);
            const totalForPercentage = totalSpent || 1; // Avoid divide by zero

            categories.forEach(cat => {
                const spent = summary.byCategory[cat];
                if (spent > 0) {
                    const percentage = Math.round((spent / totalForPercentage) * 100);
                    const item = document.createElement("div");
                    item.className = "progress-item";
                    item.innerHTML = `
                        <div style="display:flex; justify-content:space-between;">
                            <span>${escapeHTML(cat)}</span>
                            <strong>₹${spent.toLocaleString('en-IN')} (${percentage}%)</strong>
                        </div>
                        <div class="progress"><div style="width:${percentage}%"></div></div>
                    `;
                    breakdownContainer.appendChild(item);
                }
            });

            if (breakdownContainer.children.length === 0) {
                breakdownContainer.innerHTML = `<p style="color: var(--muted); text-align:center; margin-top:10px;">No transaction breakdown available.</p>`;
            }
        }
    } catch (err) {
        console.error("Error loading report cards:", err);
    }
}

async function loadExpensesTable(sortBy) {
    try {
        let url = "/api/expenses";
        if (sortBy === "date") {
            url = "/api/sort?by=date";
        } else if (sortBy === "amount") {
            url = "/api/sort?by=amount";
        }

        const res = await fetch(url);
        if (res.ok) {
            let expenses = await res.json();
            
            // If sort is by amount, API sorts ascending usually. Let's make sure it's high to low for reports table.
            if (sortBy === "amount") {
                expenses = expenses.reverse(); 
            } else if (sortBy === "default") {
                expenses = expenses.reverse(); // Newest first
            }

            const tbody = document.getElementById("expenses-table-body");
            tbody.innerHTML = "";

            if (expenses.length === 0) {
                tbody.innerHTML = `<tr><td colspan="5" style="text-align: center; color: var(--muted); padding: 30px 0;">No expenses found.</td></tr>`;
                return;
            }

            expenses.forEach(exp => {
                const tr = document.createElement("tr");
                tr.innerHTML = `
                    <td>${escapeHTML(exp.desc)}</td>
                    <td>₹${exp.amount.toLocaleString('en-IN')}</td>
                    <td><span class="tag" style="padding: 4px 10px; font-size: 12px; display: inline-block;">${escapeHTML(exp.cat)}</span></td>
                    <td>${escapeHTML(exp.date)}</td>
                    <td style="text-align: right;">
                        <button class="delete-btn" data-id="${exp.id}" style="background: none; border: none; color: #d32f2f; font-weight: 600; cursor: pointer; padding: 5px 10px; border-radius: 6px; transition: .2s;">Delete</button>
                    </td>
                `;
                
                // Add hover style behavior to delete button
                const btn = tr.querySelector(".delete-btn");
                btn.addEventListener("mouseover", () => btn.style.background = "#ffebee");
                btn.addEventListener("mouseout", () => btn.style.background = "none");
                
                btn.addEventListener("click", async () => {
                    if (confirm(`Are you sure you want to delete "${exp.desc}"?`)) {
                        await deleteExpense(exp.id);
                    }
                });

                tbody.appendChild(tr);
            });
        }
    } catch (err) {
        console.error("Error loading table:", err);
    }
}

async function deleteExpense(id) {
    try {
        const res = await fetch(`/api/expenses/${id}`, {
            method: "DELETE"
        });
        if (res.ok) {
            // Refresh table and stats card details
            await loadReportCards();
            const sortBySelect = document.getElementById("sort-by");
            await loadExpensesTable(sortBySelect ? sortBySelect.value : "default");
        } else {
            alert("Failed to delete expense.");
        }
    } catch (err) {
        console.error("Error deleting expense:", err);
    }
}

// ── 9. HTML Escaper to prevent XSS ────────────────────────────────
function escapeHTML(str) {
    if (!str) return "";
    return str.replace(/[&<>'"]/g, 
        tag => ({
            '&': '&amp;',
            '<': '&lt;',
            '>': '&gt;',
            "'": '&#39;',
            '"': '&quot;'
        }[tag] || tag)
    );
}