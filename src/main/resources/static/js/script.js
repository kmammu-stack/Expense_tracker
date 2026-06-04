document.addEventListener("DOMContentLoaded", () => {

    // Card animation
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
        }, index * 100);

    });

    // Active nav link
    const currentPath = window.location.pathname;

    document.querySelectorAll(".nav-links a").forEach(link => {

        if (currentPath.includes(link.getAttribute("href"))) {
            link.style.color = "#2e7d32";
            link.style.fontWeight = "600";
        }

    });

    // Expense form validation
    const form = document.getElementById("expenseForm");

    if (form) {

        form.addEventListener("submit", (e) => {

            const description =
                document.getElementById("description").value.trim();

            const amount =
                document.getElementById("amount").value.trim();

            if (!description || !amount) {

                e.preventDefault();
                alert("Please complete all fields.");

            }

        });

    }

    // Budget progress
    const spent = document.getElementById("spent");
    const budget = document.getElementById("budget");
    const progress = document.querySelector(".progress-fill");

    if (spent && budget && progress) {

        const percentage =
            (parseFloat(spent.textContent) /
            parseFloat(budget.textContent)) * 100;

        progress.style.width = `${percentage}%`;

    }

});