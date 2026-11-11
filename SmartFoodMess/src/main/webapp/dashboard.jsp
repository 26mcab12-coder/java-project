<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="com.mess.model.User" %>
<%@ page import="java.time.LocalTime" %>
<%@ page import="java.time.LocalDate" %>
<%@ page import="java.time.format.DateTimeFormatter" %>
<%
    User user = (User) session.getAttribute("user");
    if (user == null) {
        response.sendRedirect(request.getContextPath() + "/login");
        return;
    }
    String role = user.getRole();
    String name = user.getName() != null ? user.getName() : "User";
    String initial = name.substring(0, 1).toUpperCase();
    String dashboardPage = "ADMIN".equals(role) ? "admin-dashboard.html" : "student-dashboard.html";

    int hour = LocalTime.now().getHour();
    String greeting = hour < 12 ? "Good Morning" : hour < 17 ? "Good Afternoon" : "Good Evening";
    String emoji = hour < 12 ? "☀️" : hour < 17 ? "🌤️" : "🌙";
    String today = LocalDate.now().format(DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy"));
    String mealTime = hour < 10 ? "Breakfast" : hour < 15 ? "Lunch" : "Dinner";
    String mealEmoji = hour < 10 ? "🥞" : hour < 15 ? "🍛" : "🍽️";
%>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Dashboard | Smart Food Mess</title>
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700;800;900&family=Playfair+Display:wght@700;800&display=swap" rel="stylesheet">
    <style>
        :root {
            --green: #1a6b3c;
            --green-dark: #0f4d2a;
            --green-deep: #0a3a1f;
            --gold: #c9a84c;
            --gold-bright: #f5d060;
            --gold-light: #fdf6e3;
            --ink: #1a2e1f;
            --muted: #6b8a72;
            --surface: #ffffff;
            --bg: #f5f9f6;
        }

        * { margin:0; padding:0; box-sizing:border-box; }
        body {
            font-family: 'Inter', system-ui, sans-serif;
            min-height: 100vh;
            background:
                radial-gradient(ellipse at 20% 0%, rgba(26,107,60,.08), transparent 50%),
                radial-gradient(ellipse at 80% 100%, rgba(201,168,76,.06), transparent 40%),
                var(--bg);
            color: var(--ink);
            overflow-x: hidden;
        }

        /* ---- Floating food decorations ---- */
        .food-float {
            position: fixed;
            font-size: 32px;
            opacity: .07;
            pointer-events: none;
            z-index: 0;
            animation: floatAnim var(--dur, 12s) ease-in-out infinite;
            animation-delay: var(--delay, 0s);
        }
        @keyframes floatAnim {
            0%,100% { transform: translateY(0) rotate(0deg); }
            25%     { transform: translateY(-18px) rotate(8deg); }
            50%     { transform: translateY(-8px) rotate(-5deg); }
            75%     { transform: translateY(-22px) rotate(6deg); }
        }

        /* ---- Header ---- */
        .header {
            background: linear-gradient(135deg, var(--green-deep), var(--green-dark));
            padding: 0 24px;
            position: sticky; top: 0; z-index: 50;
            box-shadow: 0 4px 24px rgba(10,58,31,.18);
        }
        .header::after {
            content: '';
            position: absolute; bottom: 0; left: 0; right: 0;
            height: 3px;
            background: linear-gradient(90deg, var(--gold), var(--gold-bright), var(--gold));
            background-size: 200% 100%;
            animation: shimmer 4s linear infinite;
        }
        @keyframes shimmer {
            0%   { background-position: -200% center; }
            100% { background-position: 200% center; }
        }
        .header-inner {
            max-width: 1200px; margin: 0 auto;
            display: flex; align-items: center;
            min-height: 64px; gap: 16px;
        }
        .logo {
            display: flex; align-items: center; gap: 10px;
            text-decoration: none;
        }
        .logo-icon {
            width: 36px; height: 36px;
            display: grid; place-items: center;
            background: linear-gradient(135deg, var(--gold-bright), var(--gold));
            border-radius: 10px; font-size: 13px; font-weight: 900;
            color: var(--green-deep);
            box-shadow: 0 4px 14px rgba(201,168,76,.35);
        }
        .logo-text { color: #f0f9f2; font-weight: 700; font-size: 15px; }
        .header-right {
            margin-left: auto;
            display: flex; align-items: center; gap: 12px;
        }
        .role-pill {
            padding: 4px 12px; border-radius: 999px;
            background: rgba(201,168,76,.15);
            border: 1px solid rgba(201,168,76,.3);
            color: var(--gold-bright); font-size: 10px;
            font-weight: 800; letter-spacing: .06em;
            text-transform: uppercase;
        }
        .avatar-circle {
            width: 36px; height: 36px; border-radius: 50%;
            display: grid; place-items: center;
            background: linear-gradient(135deg, var(--gold-bright), var(--gold));
            color: var(--green-deep); font-weight: 800; font-size: 15px;
            border: 2px solid rgba(255,255,255,.2);
        }
        .user-name { color: #d4e5d8; font-size: 13px; font-weight: 600; }

        /* ---- Hero Section ---- */
        .hero {
            max-width: 1200px; margin: 0 auto;
            padding: 60px 24px 40px;
            position: relative; z-index: 1;
        }
        .hero-greeting {
            display: inline-flex; align-items: center; gap: 8px;
            padding: 6px 14px; border-radius: 999px;
            background: var(--gold-light); color: var(--gold);
            font-size: 13px; font-weight: 700;
            margin-bottom: 16px;
            animation: fadeUp .5s ease both;
        }
        @keyframes fadeUp {
            from { opacity:0; transform:translateY(20px); }
            to   { opacity:1; transform:none; }
        }
        .hero h1 {
            font-family: 'Playfair Display', serif;
            font-size: clamp(32px, 5vw, 48px);
            font-weight: 800; letter-spacing: -.02em;
            color: var(--ink); line-height: 1.15;
            animation: fadeUp .6s ease both;
            animation-delay: .1s;
        }
        .hero h1 span { color: var(--green); }
        .hero-sub {
            margin-top: 10px; color: var(--muted);
            font-size: 16px; max-width: 500px;
            animation: fadeUp .6s ease both;
            animation-delay: .2s;
        }

        /* ---- Info Cards ---- */
        .cards {
            max-width: 1200px; margin: 0 auto;
            padding: 0 24px 40px;
            display: grid;
            grid-template-columns: repeat(auto-fit, minmax(260px, 1fr));
            gap: 20px;
            position: relative; z-index: 1;
        }
        .card {
            background: #fff;
            border: 1px solid #d4e5d8;
            border-radius: 16px;
            padding: 28px 24px;
            box-shadow: 0 2px 12px rgba(10,58,31,.05);
            transition: transform .25s, box-shadow .25s, border-color .25s;
            position: relative;
            overflow: hidden;
        }
        .card::before {
            content: '';
            position: absolute; top: 0; left: 0; right: 0;
            height: 4px;
            background: linear-gradient(90deg, var(--green), var(--gold));
            opacity: 0;
            transition: opacity .3s;
        }
        .card:hover::before { opacity: 1; }
        .card:hover {
            transform: translateY(-6px);
            box-shadow: 0 16px 40px rgba(10,58,31,.1);
            border-color: var(--gold);
        }
        .card:nth-child(1) { animation: cardIn .6s ease both; animation-delay: .2s; }
        .card:nth-child(2) { animation: cardIn .6s ease both; animation-delay: .35s; }
        .card:nth-child(3) { animation: cardIn .6s ease both; animation-delay: .5s; }
        .card:nth-child(4) { animation: cardIn .6s ease both; animation-delay: .65s; }
        @keyframes cardIn {
            from { opacity:0; transform:translateY(30px) scale(.97); }
            to   { opacity:1; transform:none; }
        }

        .card-icon {
            width: 52px; height: 52px;
            display: grid; place-items: center;
            border-radius: 14px; font-size: 24px;
            margin-bottom: 16px;
        }
        .card-icon.green  { background: #e8f5e9; }
        .card-icon.gold   { background: var(--gold-light); }
        .card-icon.purple  { background: #f3e8ff; }
        .card-icon.blue    { background: #dbeafe; }

        .card h3 {
            font-size: 16px; font-weight: 700;
            color: var(--ink); margin-bottom: 6px;
        }
        .card p {
            color: var(--muted); font-size: 13.5px;
            line-height: 1.55;
        }
        .card-value {
            font-family: 'Playfair Display', serif;
            font-size: 28px; font-weight: 800;
            color: var(--green); margin-top: 12px;
        }

        /* ---- CTA Button ---- */
        .cta-section {
            max-width: 1200px; margin: 0 auto;
            padding: 0 24px 60px;
            display: flex; gap: 14px;
            flex-wrap: wrap;
            position: relative; z-index: 1;
            animation: fadeUp .7s ease both;
            animation-delay: .6s;
        }
        .cta-btn {
            display: inline-flex; align-items: center; gap: 8px;
            padding: 14px 28px; border-radius: 12px;
            font-size: 15px; font-weight: 700;
            text-decoration: none; cursor: pointer;
            transition: transform .15s, box-shadow .15s;
            border: none; font-family: inherit;
        }
        .cta-btn:hover {
            transform: translateY(-2px);
        }
        .cta-primary {
            background: linear-gradient(135deg, var(--green), var(--green-dark));
            color: #fff;
            box-shadow: 0 6px 20px rgba(26,107,60,.3);
            position: relative; overflow: hidden;
        }
        .cta-primary::after {
            content: '';
            position: absolute; top: 0; left: -100%; width: 200%; height: 100%;
            background: linear-gradient(90deg, transparent, rgba(201,168,76,.12), transparent);
            animation: shimmer 3s ease-in-out infinite;
        }
        .cta-primary:hover { box-shadow: 0 10px 28px rgba(26,107,60,.35); }
        .cta-secondary {
            background: #fff;
            color: var(--green-dark);
            border: 1.5px solid #d4e5d8;
            box-shadow: 0 2px 8px rgba(10,58,31,.05);
        }
        .cta-secondary:hover { border-color: var(--gold); box-shadow: 0 6px 16px rgba(201,168,76,.12); }

        /* ---- Footer ---- */
        .footer {
            max-width: 1200px; margin: 0 auto;
            padding: 0 24px 40px;
            text-align: center;
            color: var(--muted); font-size: 12px;
            position: relative; z-index: 1;
        }
        .footer a { color: var(--green); text-decoration: none; font-weight: 600; }

        @media (max-width: 600px) {
            .hero { padding: 40px 16px 30px; }
            .cards { padding: 0 16px 30px; }
            .cta-section { padding: 0 16px 40px; }
            .user-name { display: none; }
        }
    </style>
</head>
<body>

<!-- Floating food decorations -->
<span class="food-float" style="top:8%;  left:5%;  --dur:11s; --delay:0s;">🍚</span>
<span class="food-float" style="top:15%; right:8%; --dur:14s; --delay:1s;">🥘</span>
<span class="food-float" style="top:40%; left:3%;  --dur:13s; --delay:2s;">☕</span>
<span class="food-float" style="top:55%; right:4%; --dur:10s; --delay:0.5s;">🍛</span>
<span class="food-float" style="top:75%; left:7%;  --dur:15s; --delay:3s;">🫓</span>
<span class="food-float" style="top:85%; right:6%; --dur:12s; --delay:1.5s;">🥗</span>
<span class="food-float" style="top:30%; right:15%;--dur:16s; --delay:2.5s;">🧁</span>
<span class="food-float" style="top:65%; left:12%; --dur:11s; --delay:0.8s;">🍲</span>

<!-- Header -->
<header class="header">
    <div class="header-inner">
        <a class="logo" href="<%= request.getContextPath() %>/">
            <span class="logo-icon">SM</span>
            <span class="logo-text">Smart Food Mess</span>
        </a>
        <div class="header-right">
            <span class="role-pill"><%= role %></span>
            <span class="user-name"><%= name %></span>
            <span class="avatar-circle"><%= initial %></span>
        </div>
    </div>
</header>

<!-- Hero -->
<section class="hero">
    <div class="hero-greeting"><%= emoji %> <%= greeting %></div>
    <h1>Welcome back, <span><%= name %></span>!</h1>
    <p class="hero-sub">
        It's <%= today %>. <%= mealEmoji %> Time for <strong><%= mealTime %></strong>.
        Your personalized mess dashboard is ready.
    </p>
</section>

<!-- Info Cards -->
<section class="cards">
    <div class="card">
        <div class="card-icon green">👤</div>
        <h3>Your Account</h3>
        <p>Logged in as <strong><%= name %></strong> with <strong><%= role %></strong> privileges.</p>
        <div class="card-value"><%= role %></div>
    </div>

    <div class="card">
        <div class="card-icon gold">🔐</div>
        <h3>Session Status</h3>
        <p>Your session is active and secured. Auto-expires after 30 minutes of inactivity.</p>
        <div class="card-value" style="font-size:14px; font-family:'Inter',sans-serif; color:var(--muted); word-break:break-all;">
            <%= session.getId().substring(0, Math.min(session.getId().length(), 16)) %>…
        </div>
    </div>

    <div class="card">
        <div class="card-icon purple"><%= mealEmoji %></div>
        <h3>Current Meal</h3>
        <p>Based on the current time, the mess is serving <strong><%= mealTime.toLowerCase() %></strong>.</p>
        <div class="card-value"><%= mealTime %></div>
    </div>

    <div class="card">
        <div class="card-icon blue">📅</div>
        <h3>Today's Date</h3>
        <p>Stay on track with your daily meal planning and opt-in schedule.</p>
        <div class="card-value" style="font-size:18px;"><%= today %></div>
    </div>
</section>

<!-- CTA Buttons -->
<section class="cta-section">
    <a class="cta-btn cta-primary" href="<%= dashboardPage %>">
        Go to Full Dashboard →
    </a>
    <a class="cta-btn cta-secondary" href="account-settings.html">
        ⚙️ Account Settings
    </a>
    <a class="cta-btn cta-secondary" href="logout" style="color:#c0392b; border-color:#f5c6cb;">
        🚪 Logout
    </a>
</section>

<!-- Footer -->
<footer class="footer">
    <p>Smart Food Mess Management System &copy; 2026 &middot;
       Built with <a href="#">Jakarta Servlets</a> &amp; <a href="#">JSP</a></p>
</footer>

</body>
</html>
