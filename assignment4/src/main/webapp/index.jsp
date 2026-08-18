<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Electricity Bill Calculator</title>
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
            max-width: 400px;
        }
        h2 {
            text-align: center;
            color: #1e3c72;
            margin-bottom: 25px;
        }
        label {
            display: block;
            margin-bottom: 6px;
            font-weight: 600;
            color: #333;
        }
        input[type="text"], input[type="number"] {
            width: 100%;
            padding: 10px 12px;
            margin-bottom: 18px;
            border: 1px solid #ccc;
            border-radius: 6px;
            font-size: 15px;
        }
        input[type="submit"] {
            width: 100%;
            padding: 12px;
            background: #1e3c72;
            color: #fff;
            border: none;
            border-radius: 6px;
            font-size: 16px;
            cursor: pointer;
            transition: background 0.3s;
        }
        input[type="submit"]:hover { background: #2a5298; }
        .rates {
            margin-top: 20px;
            font-size: 13px;
            color: #555;
            border-top: 1px solid #eee;
            padding-top: 12px;
        }
        @media (max-width: 480px) {
            .card { padding: 25px 18px; }
        }
    </style>
</head>
<body>
    <div class="card">
        <h2>⚡ Electricity Bill Calculator</h2>
        <form action="bill.jsp" method="post">
            <label for="name">Consumer Name:</label>
            <input type="text" id="name" name="name" required>

            <label for="units">Units Consumed:</label>
            <input type="number" id="units" name="units" min="0" required>

            <input type="submit" value="Calculate Bill">
        </form>
        <div class="rates">
            <b>Tariff:</b><br>
            First 50 units: Rs. 3.50/unit<br>
            Next 100 units: Rs. 4.00/unit<br>
            Next 100 units: Rs. 5.20/unit<br>
            Above 250 units: Rs. 6.50/unit
        </div>
    </div>
</body>
</html>