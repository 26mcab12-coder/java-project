(function addPremiumTheme() {
    if (document.querySelector('link[data-premium-theme]')) {
        return;
    }
    var premiumCss = document.createElement('link');
    premiumCss.rel = 'stylesheet';
    premiumCss.href = 'css/premium-dashboard.css?v=20261101.2';
    premiumCss.setAttribute('data-premium-theme', 'true');
    document.head.appendChild(premiumCss);
})();

document.addEventListener('DOMContentLoaded', function () {
    'use strict';

    var days = ['SUNDAY', 'MONDAY', 'TUESDAY', 'WEDNESDAY', 'THURSDAY', 'FRIDAY', 'SATURDAY'];
    var meals = ['BREAKFAST', 'LUNCH', 'DINNER'];
    var page = document.body.dataset.page;

    function textCell(row, value, className) {
        var cell = document.createElement('td');
        cell.textContent = value == null ? '' : String(value);
        if (className) {
            cell.className = className;
        }
        row.appendChild(cell);
        return cell;
    }

    function formatPrice(value) {
        if (value === null || value === undefined || value === '') {
            return '₹0.00';
        }
        var parsed = Number(value);
        if (!Number.isFinite(parsed)) {
            return '₹0.00';
        }
        return '₹' + parsed.toFixed(2);
    }

    function formatMenuEntry(items, price) {
        var label = items == null ? '' : String(items).trim();
        var formattedPrice = formatPrice(price);
        if (!label) {
            return formattedPrice;
        }
        return label + ' (' + formattedPrice + ')';
    }

    function emptyRow(body, columns, message) {
        if (!body) {
            return;
        }
        var row = document.createElement('tr');
        var cell = textCell(row, message, 'empty-cell');
        cell.colSpan = columns;
        body.replaceChildren(row);
    }

    function showFlash(message, isError) {
        if (!message) {
            return;
        }
        var flash = document.querySelector('[data-flash]');
        if (!flash) {
            return;
        }
        flash.textContent = message;
        flash.className = isError ? 'error-message' : 'flash-message';
        flash.hidden = false;
    }

    function showPageError(message) {
        var error = document.querySelector('[data-page-error]');
        if (error) {
            error.textContent = message;
            error.hidden = false;
        }
    }

    function updateHeader(data) {
        document.querySelectorAll('[data-user-name]').forEach(function (node) {
            node.textContent = data.name || '';
        });
        document.querySelectorAll('[data-user-initial]').forEach(function (node) {
            node.textContent = data.name ? data.name.trim().charAt(0).toUpperCase() : '?';
            node.title = data.name || '';
        });
        document.querySelectorAll('[data-today]').forEach(function (node) {
            node.textContent = data.today || '';
        });
        showFlash(data.flashMessage, data.flashError);
    }

    function updateWelcome(role) {
        var hour = new Date().getHours();
        var greeting = hour < 12 ? 'Good morning'
            : hour < 17 ? 'Good afternoon' : 'Good evening';
        var messages = role === 'ADMIN'
            ? [
                'A fresh week is a fresh chance to make every meal count.',
                'Your thoughtful planning helps the whole mess run smoothly.',
                'Small details make a big difference in everyone’s day.',
                'A well-organized mess starts with your steady leadership.',
                'Your care behind the scenes brings people together at the table.',
                'Thank you for making the daily routine feel welcoming.',
                'Here’s to a smooth service and a successful week ahead.'
            ]
            : [
                'A new week brings new flavors and good moments to share.',
                'Make today a little brighter, one good meal at a time.',
                'Good food and good company make any day better.',
                'Take a moment to enjoy something delicious today.',
                'Your next favorite meal could be waiting on the menu.',
                'Make room for good food and even better memories.',
                'Wishing you a relaxed day and something lovely on your plate.'
            ];
        var day = new Date().getDay();
        var greetingNode = document.querySelector('[data-time-greeting]');
        var messageNode = document.querySelector('[data-daily-welcome]');
        if (greetingNode) {
            greetingNode.textContent = greeting;
        }
        if (messageNode) {
            messageNode.textContent = messages[day];
        }
    }

    async function getData(endpoint) {
        var response = await fetch(new URL('api/' + endpoint, document.baseURI), {
            credentials: 'same-origin',
            headers: {'Accept': 'application/json'}
        });
        if (!response.headers.get('content-type') || !response.headers.get('content-type').includes('application/json')) {
            window.location.assign('login.html');
            throw new Error('Your session has expired. Please sign in again.');
        }
        if (!response.ok) {
            throw new Error('Could not load this page. Please try again.');
        }
        return response.json();
    }

    function addOptions(select, values) {
        if (!select) {
            return;
        }
        values.forEach(function (value) {
            var option = document.createElement('option');
            option.value = value;
            option.textContent = value;
            select.appendChild(option);
        });
    }

    function addActionForm(cell, endpoint, actionName, fieldName, fieldValue, label, buttonClass, confirmMessage) {
        var form = document.createElement('form');
        form.method = 'post';
        form.action = endpoint;
        form.className = 'inline-form';
        var actionInput = document.createElement('input');
        actionInput.type = 'hidden';
        actionInput.name = 'action';
        actionInput.value = actionName;
        form.appendChild(actionInput);
        var idInput = document.createElement('input');
        idInput.type = 'hidden';
        idInput.name = fieldName;
        idInput.value = fieldValue;
        form.appendChild(idInput);
        var button = document.createElement('button');
        button.type = 'submit';
        button.className = buttonClass;
        button.textContent = label;
        if (confirmMessage) {
            form.addEventListener('submit', function (event) {
                if (!window.confirm(confirmMessage)) {
                    event.preventDefault();
                }
            });
        }
        form.appendChild(button);
        cell.appendChild(form);
    }

    function appendDailyExpenseTotal(body, date, amount) {
        if (!body) {
            return;
        }
        var row = document.createElement('tr');
        row.className = 'daily-total-row';
        textCell(row, date, 'daily-total-label');
        textCell(row, '', 'daily-total-spacer');
        textCell(row, '', 'daily-total-spacer');
        var totalCell = textCell(row, amount == null ? 'Rate not set' : 'Total · ' + formatPrice(amount), 'daily-total-value');
        totalCell.colSpan = 1;
        body.appendChild(row);
    }

    async function loadAdminDashboard() {
        var data = await getData('admin/dashboard');
        updateHeader(data);
        updateWelcome('ADMIN');
        Object.keys(data).forEach(function (key) {
            var node = document.querySelector('[data-stat="' + key + '"]');
            if (node) {
                node.textContent = node.dataset.statFormat === 'currency'
                    ? (data[key] == null ? 'Rate not set' : formatPrice(data[key]))
                    : data[key];
            }
        });
        document.querySelectorAll('[data-meal-expense]').forEach(function (node) {
            var mealType = node.dataset.mealExpense;
            var expenses = data.todayExpensesByMeal || {};
            var amount = Object.prototype.hasOwnProperty.call(expenses, mealType) ? expenses[mealType] : 0;
            node.textContent = amount == null ? 'Rate not set' : formatPrice(amount);
        });
    }

    async function loadAdminMenu() {
        var data = await getData('admin/menu');
        updateHeader(data);
        addOptions(document.getElementById('addDay'), days);
        addOptions(document.getElementById('editDay'), days);
        addOptions(document.getElementById('addMeal'), meals);
        addOptions(document.getElementById('editMeal'), meals);

        var body = document.getElementById('menuTable');
        if (!data.menus.length) {
            emptyRow(body, 5, 'No menu entries yet. Add one above.');
        } else {
            body.replaceChildren();
            data.menus.forEach(function (menu) {
                var row = document.createElement('tr');
                textCell(row, menu.dayOfWeek, 'day-cell');
                textCell(row, menu.mealType);
                textCell(row, menu.items);
                textCell(row, formatPrice(menu.price));
                var actions = textCell(row, '', 'actions-cell');
                var edit = document.createElement('a');
                edit.className = 'btn btn-blue btn-sm';
                edit.href = 'admin-menu.html?edit=' + encodeURIComponent(menu.menuId);
                edit.textContent = 'Edit';
                actions.appendChild(edit);
                var form = document.createElement('form');
                form.method = 'post';
                form.action = 'admin/menu';
                form.className = 'inline-form';
                var actionInput = document.createElement('input');
                actionInput.type = 'hidden';
                actionInput.name = 'action';
                actionInput.value = 'delete';
                var idInput = document.createElement('input');
                idInput.type = 'hidden';
                idInput.name = 'menuId';
                idInput.value = menu.menuId;
                var button = document.createElement('button');
                button.type = 'submit';
                button.className = 'btn btn-red btn-sm';
                button.textContent = 'Delete';
                form.append(actionInput, idInput, button);
                actions.appendChild(form);
                body.appendChild(row);
            });
        }

        var editId = new URLSearchParams(window.location.search).get('edit');
        if (editId) {
            var editingMenu = data.menus.find(function (menu) {
                return String(menu.menuId) === editId;
            });
            if (editingMenu) {
                document.getElementById('editMenuId').value = editingMenu.menuId;
                document.getElementById('editDay').value = editingMenu.dayOfWeek;
                document.getElementById('editMeal').value = editingMenu.mealType;
                document.getElementById('editItems').value = editingMenu.items;
                document.getElementById('editPrice').value = editingMenu.price;
                document.getElementById('editSection').hidden = false;
            }
        }
    }

    async function loadStudents() {
        var data = await getData('admin/students');
        updateHeader(data);
        var body = document.getElementById('studentsTable');
        if (!data.students.length) {
            emptyRow(body, 5, 'No students registered yet.');
            return;
        }
        body.replaceChildren();
        data.students.forEach(function (student, index) {
            var row = document.createElement('tr');
            textCell(row, index + 1);
            var nameCell = textCell(row, student.name);
            var name = document.createElement('strong');
            name.textContent = student.name;
            nameCell.replaceChildren(name);
            textCell(row, student.email);
            textCell(row, student.createdAt || '');
            var actions = textCell(row, '', 'actions-cell');
            addActionForm(actions, 'admin/students', 'delete', 'userId', student.userId, 'Delete', 'btn btn-red btn-sm',
                'Delete student ' + student.name + '? This also removes their meal records.');
            body.appendChild(row);
        });
    }

    async function loadRecords() {
        var data = await getData('admin/records');
        updateHeader(data);
        var body = document.getElementById('recordsTable');
        if (!data.records.length) {
            emptyRow(body, 6, 'No meal records yet.');
            return;
        }
        body.replaceChildren();
        data.records.forEach(function (record, index) {
            var row = document.createElement('tr');
            textCell(row, index + 1);
            var nameCell = textCell(row, record.studentName);
            var name = document.createElement('strong');
            name.textContent = record.studentName;
            nameCell.replaceChildren(name);
            textCell(row, record.mealDate);
            textCell(row, record.mealType);
            var statusCell = textCell(row, '');
            var badge = document.createElement('span');
            badge.className = 'badge status-' + record.status.toLowerCase();
            badge.textContent = record.status;
            statusCell.appendChild(badge);
            var actions = textCell(row, '', 'actions-cell');
            if (record.status !== 'CONSUMED') {
                addActionForm(actions, 'admin/records', 'consume', 'recordId', record.recordId, 'Mark Consumed',
                    'btn btn-green btn-sm', null);
            }
            addActionForm(actions, 'admin/records', 'delete', 'recordId', record.recordId, 'Delete',
                'btn btn-red btn-sm', null);
            body.appendChild(row);
        });
    }

    function renderMealCard(container, type, status, today, menu, record) {
        var labels = {BREAKFAST: 'Breakfast', LUNCH: 'Lunch', DINNER: 'Dinner'};
        var icons = {BREAKFAST: '☀', LUNCH: '◉', DINNER: '☾'};
        var card = document.createElement('div');
        card.className = 'meal-card';
        var icon = document.createElement('span');
        icon.className = 'meal-emoji';
        icon.setAttribute('aria-hidden', 'true');
        icon.textContent = icons[type];
        card.appendChild(icon);
        var heading = document.createElement('h4');
        heading.textContent = labels[type];
        card.appendChild(heading);
        var items = document.createElement('p');
        items.className = 'meal-items';
        items.textContent = menu ? menu.items : 'Menu not published yet';
        card.appendChild(items);
        var price = document.createElement('p');
        price.className = 'meal-price';
        var mealExpense = record ? record.expense : menu && menu.price;
        var priceLabel = record && record.status === 'OPTED_OUT' ? 'Meal charge' : (record ? 'Meal charge' : 'Menu price');
        price.textContent = mealExpense == null ? 'Rate not set' : priceLabel + ' · ' + formatPrice(mealExpense);
        card.appendChild(price);
        var statusLine = document.createElement('p');
        statusLine.className = 'meal-status';
        statusLine.textContent = 'Status: ' + (status || 'Not opted');
        card.appendChild(statusLine);

        if (status === 'CONSUMED') {
            var note = document.createElement('p');
            note.className = 'note';
            note.textContent = 'Consumed · included in today’s spend';
            card.appendChild(note);
        } else {
            var optedIn = status === 'OPTED_IN';
            var form = document.createElement('form');
            form.method = 'post';
            form.action = 'student/opt';
            form.className = 'inline-form';
            [['action', optedIn ? 'optout' : 'optin'], ['mealType', type], ['date', today]].forEach(function (field) {
                var input = document.createElement('input');
                input.type = 'hidden';
                input.name = field[0];
                input.value = field[1];
                form.appendChild(input);
            });
            var button = document.createElement('button');
            button.type = 'submit';
            button.className = optedIn ? 'btn btn-red' : 'btn btn-green';
            button.textContent = optedIn ? 'Opt Out' : 'Opt In';
            form.appendChild(button);
            card.appendChild(form);
        }
        container.appendChild(card);
    }

    async function loadStudentDashboard() {
        var data = await getData('student/dashboard');
        updateHeader(data);
        updateWelcome('STUDENT');
        var mealsContainer = document.getElementById('todayMeals');
        if (mealsContainer) {
            mealsContainer.replaceChildren();
        }
        var todayDate = new Date(data.today + 'T00:00:00');
        var todayDay = days[todayDate.getDay()];
        data.mealTypes.forEach(function (type) {
            var menu = data.menus.find(function (entry) {
                return entry.dayOfWeek === todayDay && entry.mealType === type;
            });
            var record = data.history.find(function (entry) {
                return entry.mealDate === data.today && entry.mealType === type;
            });
            renderMealCard(mealsContainer, type, data.statuses[type], data.today, menu, record);
        });

        var weeklyBody = document.getElementById('weeklyMenu');
        if (!data.menus.length) {
            emptyRow(weeklyBody, 4, 'Menu has not been published yet. Check back later.');
        } else {
            weeklyBody.replaceChildren();
            data.days.forEach(function (day) {
                var row = document.createElement('tr');
                if (day === todayDay) {
                    row.className = 'today-row';
                }
                var dayCell = textCell(row, day, 'day-cell');
                var dayName = document.createElement('strong');
                dayName.textContent = day;
                if (day === todayDay) {
                    var todayLabel = document.createElement('span');
                    todayLabel.className = 'today-label';
                    todayLabel.textContent = 'TODAY';
                    dayName.appendChild(todayLabel);
                }
                dayCell.replaceChildren(dayName);
                ['BREAKFAST', 'LUNCH', 'DINNER'].forEach(function (type) {
                    var menu = data.menus.find(function (entry) {
                        return entry.dayOfWeek === day && entry.mealType === type;
                    });
                    textCell(row, menu ? formatMenuEntry(menu.items, menu.price) : '—');
                });
                weeklyBody.appendChild(row);
            });
        }

        var historyBody = document.getElementById('mealHistory');
        var eachTotal = document.getElementById('todayMealExpenseTotal');
        var overallTotal = document.getElementById('mealExpenseTotal');
        if (eachTotal) {
            eachTotal.textContent = data.todayExpenseTotal == null ? 'Rate not set' : formatPrice(data.todayExpenseTotal);
        }
        if (overallTotal) {
            overallTotal.textContent = formatPrice(data.expenseTotal);
        }
        if (!data.history.length) {
            emptyRow(historyBody, 4, 'No meal records yet. Opt in to your first meal above.');
        } else {
            historyBody.replaceChildren();
            var currentDate = null;
            data.history.forEach(function (record) {
                if (currentDate !== null && record.mealDate !== currentDate) {
                    appendDailyExpenseTotal(historyBody, currentDate, data.dailyExpenseTotals[currentDate]);
                }
                currentDate = record.mealDate;
                var row = document.createElement('tr');
                textCell(row, record.mealDate);
                textCell(row, record.mealType);
                var statusCell = textCell(row, '');
                var badge = document.createElement('span');
                badge.className = 'badge status-' + record.status.toLowerCase();
                badge.textContent = record.status;
                statusCell.appendChild(badge);
                textCell(row, record.expense == null ? 'Rate not set' : formatPrice(record.expense));
                historyBody.appendChild(row);
            });
            if (currentDate) {
                appendDailyExpenseTotal(historyBody, currentDate, data.dailyExpenseTotals[currentDate]);
            }
        }
    }

    async function loadAccountSettings() {
        var data = await getData('account');
        updateHeader(data);

        var isAdmin = data.role === 'ADMIN';
        document.querySelectorAll('[data-admin-only]').forEach(function (node) {
            node.hidden = !isAdmin || node.hasAttribute('data-settings-panel');
        });
        document.querySelectorAll('[data-student-only]').forEach(function (node) {
            node.hidden = isAdmin || node.hasAttribute('data-settings-panel');
        });
        var dashboardLink = document.querySelector('[data-dashboard-link]');
        if (dashboardLink) {
            dashboardLink.href = isAdmin ? 'admin-dashboard.html' : 'student-dashboard.html';
        }
        var roleLabel = document.querySelector('[data-role-label]');
        if (roleLabel) {
            roleLabel.textContent = data.role || '';
        }
        var accountMenuLabel = document.querySelector('[data-account-menu-label]');
        if (accountMenuLabel) {
            accountMenuLabel.textContent = isAdmin ? 'Admin Settings' : 'Account & Settings';
        }
        var accountEmail = document.querySelector('[data-account-email]');
        if (accountEmail) {
            accountEmail.textContent = data.email || 'Not available';
        }
        var accountRole = document.querySelector('[data-account-role]');
        if (accountRole) {
            accountRole.textContent = data.role || 'Not available';
        }
        var accountName = document.getElementById('accountName');
        if (accountName) {
            accountName.value = data.name || '';
        }

        var created = document.querySelector('[data-account-created]');
        if (data.createdAt) {
            created.textContent = new Date(data.createdAt + 'T00:00:00').toLocaleDateString();
        } else if (created) {
            created.textContent = 'Not available';
        }

        var settingsEyebrow = document.querySelector('[data-settings-eyebrow]');
        if (settingsEyebrow) {
            settingsEyebrow.textContent = isAdmin ? 'ADMINISTRATION' : 'STUDENT PORTAL';
        }
        var settingsTitle = document.querySelector('[data-settings-title]');
        if (settingsTitle) {
            settingsTitle.textContent = isAdmin ? 'Admin Settings' : 'Account & Settings';
        }
        document.title = (isAdmin ? 'Admin Settings' : 'Account & Settings') + ' | Smart Food Mess';

        document.querySelectorAll('[data-settings-option]').forEach(function (button) {
            button.addEventListener('click', function () {
                var panel = document.getElementById(button.dataset.settingsOption);
                var shouldOpen = panel && panel.hidden;
                document.querySelectorAll('[data-settings-panel]').forEach(function (item) {
                    item.hidden = true;
                });
                document.querySelectorAll('[data-settings-option]').forEach(function (option) {
                    option.setAttribute('aria-expanded', 'false');
                });
                if (panel && shouldOpen) {
                    panel.hidden = false;
                    button.setAttribute('aria-expanded', 'true');
                    panel.scrollIntoView({behavior: 'smooth', block: 'start'});
                }
            });
        });
    }

    var loaders = {
        'admin-dashboard': loadAdminDashboard,
        'admin-menu': loadAdminMenu,
        'admin-students': loadStudents,
        'admin-records': loadRecords,
        'student-dashboard': loadStudentDashboard,
        'account-settings': loadAccountSettings
    };

    if (loaders[page]) {
        loaders[page]().catch(function (error) {
            showPageError(error.message || 'Unable to load page data.');
        });
    }
});

// test


document.addEventListener('DOMContentLoaded', function () {
    function getTypeFromTime() {
        var hour = new Date().getHours();
        if (hour >= 5 && hour < 11) {
            return 'BREAKFAST';
        }
        if (hour >= 11 && hour < 16) {
            return 'LUNCH';
        }
        return 'DINNER';
    }

    function applyMealHighlight(type) {
        var showcase = document.querySelector('[data-meal-showcase]');
        if (!showcase) {
            return;
        }

        var heading = document.querySelector('[data-showcase-heading]');
        var badge = document.querySelector('[data-showcase-badge]');
        var text = document.querySelector('[data-showcase-text]');
        var foodItems = document.querySelectorAll('.food-item');
        var selected = {
            BREAKFAST: {
                badge: 'Breakfast special',
                heading: 'Breakfast Delight',
                text: 'Crispy dosas, warm idlis, and fresh flavors to power your morning.',
                foods: ['🥞', '🍳', '🥭', '🥯']
            },
            LUNCH: {
                badge: 'Lunch moment',
                heading: 'Lunch Feast',
                text: 'A satisfying lunch spread with comforting curries, rice, and vibrant sides.',
                foods: ['🍛', '🥗', '🍲', '🍚']
            },
            DINNER: {
                badge: 'Dinner glow',
                heading: 'Dinner Premium',
                text: 'A rich evening menu with warm comfort, fragrant spices, and a premium finish.',
                foods: ['🍽️', '🍛', '🥘', '🍲']
            }
        };

        var details = selected[type] || selected.BREAKFAST;
        showcase.setAttribute('data-meal', type);
        if (heading) heading.textContent = details.heading;
        if (badge) badge.textContent = details.badge;
        if (text) text.textContent = details.text;
        foodItems.forEach(function (item, index) {
            item.textContent = details.foods[index] || details.foods[0];
        });

        document.querySelectorAll('.meal-card').forEach(function (card) {
            card.classList.toggle('selected', (card.dataset.mealType || 'BREAKFAST') === type);
        });
    }

    var preferredType = getTypeFromTime();
    applyMealHighlight(preferredType);

    document.querySelectorAll('.meal-card').forEach(function (card) {
        card.addEventListener('click', function (event) {
            if (event.target && event.target.closest('button, form, input, label')) {
                return;
            }
            var type = card.dataset.mealType || 'BREAKFAST';
            applyMealHighlight(type);
        });
    });
});


document.addEventListener('DOMContentLoaded', function () {
    document.querySelectorAll('.meal-card').forEach(function (card) {
        if (!card.dataset.mealType) {
            var label = (card.querySelector('h4') || {}).textContent || '';
            var normalized = label.trim().toUpperCase();
            if (normalized.indexOf('BREAKFAST') >= 0) {
                card.dataset.mealType = 'BREAKFAST';
            } else if (normalized.indexOf('LUNCH') >= 0) {
                card.dataset.mealType = 'LUNCH';
            } else if (normalized.indexOf('DINNER') >= 0) {
                card.dataset.mealType = 'DINNER';
            }
        }
    });
});
