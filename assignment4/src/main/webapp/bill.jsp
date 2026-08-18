<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="java.text.DecimalFormat" %>
<%
    String name = request.getParameter("name");
    double units = 0;
    double bill = 0;
    String error = null;

    try {
        units = Double.parseDouble(request.getParameter("units"));
        if (units < 0) {
            error = "Units cannot be negative.";
        } else {
            if (units <= 50) {
                bill = units * 3.50;
            } else if (units <= 150) {
                bill = (50 * 3.50) + (units - 50) * 4.00;
            } else if (units <= 250) {
                bill = (50 * 3.50) + (100 * 4.00) + (units - 150) * 5.20;
            } else {
                bill = (50 * 3.50) + (100 * 4.00) + (100 * 5.20) + (units - 250) * 6.50;
            }
        }
    } catch (NumberFormatException e) {
        error = "Please enter a valid number for units.";
    }

    DecimalFormat df = new DecimalFormat("#,##0.00");
%>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Bill Result</title>
    <style>
        * { box-sizing: border-box; }
        body {
            font-family: 'Segoe UI', Arial, sans-serif;
            background: linear-gradient(135deg, #1e3c72, #2a5298);
            display: flex;
            justify-content: center;
            align-items: center;
            min-height: 100vh;
            margin: 0;
            padding: 20px;
        }
        .card {
            background: #fff;
            padding: 35px 30px;
            border-radius: 12px;
            box-shadow: 0 10px 30px rgba(0,0,0,0.3);
            width: 100%;
            max-width: 420px;
            text-align: center;
        }
        h2 { color: #1e3c72; margin-bottom: 20px; }
        .error { color: #c0392b; font-weight: bold; margin: 15px 0; }
        table {
            width: 100%;
            border-collapse: collapse;
            margin-top: 15px;
            text-align: left;
        }
        table td {
            padding: 10px;
            border-bottom: 1px solid #eee;
        }
        .total {
            font-size: 22px;
            font-weight: bold;
            color: #1e3c72;
            margin-top: 20px;
        }
        a.back {
            display: inline-block;
            margin-top: 25px;
            padding: 10px 20px;
            background: #1e3c72;
            color: #fff;
            text-decoration: none;
            border-radius: 6px;
        }
        a.back:hover { background: #2a5298; }
    </style>
</head>
<body>
    <div class="card">
        <h2>⚡ Bill Summary</h2>
        <% if (error != null) { %>
            <p class="error"><%= error %></p>
        <% } else { %>
            <table>
                <tr><td><b>Consumer Name:</b></td><td><%= name %></td></tr>
                <tr><td><b>Units Consumed:</b></td><td><%= units %></td></tr>
            </table>
            <div class="total">Total Bill: Rs. <%= df.format(bill) %></div>
        <% } %>
        <a class="back" href="index.jsp">Calculate Again</a>
    </div>
</body>
</html>