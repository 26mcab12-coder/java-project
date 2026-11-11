# Smart Food Mess Management System

A Java web application for managing a college/hostel mess: user authentication, weekly menus, and student meal records.

Implemented so far:
- **Phase 1** – Project scaffolding, `mess_db` schema script, initial test data.
- **Phase 2** – POJO models, JDBC connection factory, `UserDAO` with credential validation.
- **Web UI** – static HTML pages with JavaScript validation and rendering, backed by authenticated Java servlets and a JSON data API.
- **Account & help** – role-aware account settings, display-name updates, application version details, a help center, and starter terms/privacy notices.

## Tech Stack

| Layer        | Technology                                   |
|--------------|----------------------------------------------|
| Language     | Java (JDK 11+)                               |
| Web          | Jakarta Servlet API 5.0                       |
| Persistence  | JDBC + MySQL                                 |
| Frontend     | HTML, CSS, JavaScript                        |
| Server       | Apache Tomcat 10.x                           |
| Build        | Maven (WAR packaging)                        |

## Project Structure

```
SmartFoodMess/
├── pom.xml
├── schema.sql
├── README.md
└── src/main/
    ├── java/com/mess/
    │   ├── model/        User, Menu, MealRecord
    │   ├── dao/          UserDAO
    │   ├── servlet/      LoginServlet
    │   └── util/         DBConnection
    └── webapp/
        ├── WEB-INF/web.xml
        ├── css/style.css
        ├── js/validation.js
        ├── *.html        Static login, registration, dashboard and admin pages
        ├── js/           Form validation and API-driven page rendering
        └── css/style.css
```

## Database Setup

1. Start MySQL, then run:

   ```sh
   mysql -u root -p < schema.sql
   ```

   If `mess_db` already existed before menu prices were added, the application
   adds the missing `price` column on its first menu request. This preserves
   existing menu entries. The MySQL user configured for the app must have
   permission to alter the `menu` table.

2. The app connects to `mess_db` on `localhost:3306` as `root`. The default
   password is `MySQL@123`; override it with the `MESS_DB_PASSWORD` environment
   variable if your local MySQL password is different. `MESS_DB_URL` and
   `MESS_DB_USER` can also override the connection URL and user.

### Test Users

| Role    | Email           | Password    |
|---------|-----------------|-------------|
| ADMIN   | admin@mess.com  | admin123    |
| STUDENT | john@student.com| student123  |

## Run locally

```sh
mvn jetty:run
```

Open **http://localhost:8080/SmartFoodMess/** after Maven reports that Jetty has
started. Stop the local server with `Ctrl+C`. This command uses the embedded
Jetty 11 server and does not require a separate Tomcat installation.

Requirements: Maven 3.6+, JDK 11+, MySQL 8.x. To build a WAR for deployment to
Apache Tomcat 10.0 or newer, run `mvn clean package` and deploy
`target/SmartFoodMess.war` to Tomcat's `webapps` folder.

## Notes

- Passwords are plain text in the schema for Phase 1 simplicity.
  A future hardening phase should switch to hashing (e.g., BCrypt).
- `LoginServlet` is mapped via `@WebServlet("/login")`; the static login form
  submits to `<context-path>/login` via POST. The login page displays fixed
  message codes from the query string.
- Protected HTML pages fetch their data from the session-protected
  `<context-path>/api/*` JSON endpoints. Admin/student updates continue to use
  the existing servlet form routes.
- Student meal history displays the expense captured from the menu price when
  a meal is opted into; opted-out meals cost ₹0.00. It shows a subtotal for each
  date, today's expense, and the all-time expense. Records without an expense
  are backfilled from the matching weekday and meal-type menu price when meal
  history is loaded. If a meal has no matching menu entry, its expense is shown
  as "Rate not set"; add the menu entry for that weekday and meal type to fill it.
- If MySQL is unavailable or the schema has not been imported, the login page
  displays a database setup message instead of incorrectly reporting invalid
  credentials.
- The Account & Settings page is available to administrators and students.
  Users can update their display name; role and email are read-only. The terms
  and privacy content is starter copy that the institution operating the
  portal must review before production use.
- Open Account & Settings from the signed-in user's name in the header. Its
  icon-labeled options reveal one section at a time.
- The student and admin dashboards show role-specific welcome messages that
  change by day of the week, along with a morning, afternoon or evening greeting.
- Passwords are currently stored as plain text in this initial version. Do not
  use real or reused credentials; password hashing and other security
  hardening are required before production deployment.