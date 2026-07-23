<!DOCTYPE html>
<html lang="en">
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1.0">
<title>Electricity Bill Calculator</title>
<link rel="stylesheet" href="style.css">
</head>
<body>

<div class="top-stripe"></div>
<div class="navbar">
    <div class="brand">
        <div class="emblem">⚡</div>
        <div class="brand-text">
            <h1>Electricity Billing Portal</h1>
            <p>Department of Power Distribution</p>
        </div>
    </div>
    <nav>
        <a href="index.php">Home</a>
        <a href="history.php">Bill History</a>
    </nav>
</div>

<div class="container">

    <div class="card">
        <h2>Tariff Rates</h2>
        <div class="rate-box">
            <ul>
                <li><b>First 50 units:</b> Rs. 3.50 / unit</li>
                <li><b>Next 100 units (51-150):</b> Rs. 4.00 / unit</li>
                <li><b>Next 100 units (151-250):</b> Rs. 5.20 / unit</li>
                <li><b>Above 250 units:</b> Rs. 6.50 / unit</li>
            </ul>
        </div>
    </div>

    <div class="card">
        <h2>Enter Consumer Details</h2>

        <?php if (isset($_GET['error'])): ?>
            <div class="alert"><?php echo htmlspecialchars($_GET['error']); ?></div>
        <?php endif; ?>

        <form action="calculate.php" method="POST">
            <div class="form-group">
                <label for="consumer_name">Consumer Name</label>
                <input type="text" id="consumer_name" name="consumer_name" placeholder="e.g. Ramesh Kumar" required>
            </div>

            <div class="form-group">
                <label for="consumer_no">Consumer / Meter Number</label>
                <input type="text" id="consumer_no" name="consumer_no" placeholder="e.g. MH-2024-001" required>
            </div>

            <div class="form-group">
                <label for="previous_reading">Previous Meter Reading (units)</label>
                <input type="number" id="previous_reading" name="previous_reading" min="0" step="1" placeholder="e.g. 4230" required>
            </div>

            <div class="form-group">
                <label for="current_reading">Current Meter Reading (units)</label>
                <input type="number" id="current_reading" name="current_reading" min="0" step="1" placeholder="e.g. 4410" required>
            </div>

            <div class="form-group">
                <label>Units Consumed (auto-calculated)</label>
                <input type="text" id="units_display" readonly placeholder="Enter both readings above" style="background:#f2efe6; color:#0b3954; font-weight:600;">
            </div>

            <div class="form-group">
                <label for="bill_date">Bill Date</label>
                <input type="date" id="bill_date" name="bill_date" required>
            </div>

            <button type="submit" class="btn">Calculate Bill</button>
        </form>
    </div>

    <p class="site-footer">This is a demo billing portal for educational purposes.</p>

</div>

<script>
    // Live-preview units consumed = current reading - previous reading
    const prevInput = document.getElementById('previous_reading');
    const currInput = document.getElementById('current_reading');
    const unitsDisplay = document.getElementById('units_display');

    function updateUnitsPreview() {
        const prev = parseFloat(prevInput.value);
        const curr = parseFloat(currInput.value);
        if (!isNaN(prev) && !isNaN(curr) && curr >= prev) {
            unitsDisplay.value = (curr - prev) + " units";
        } else if (!isNaN(prev) && !isNaN(curr) && curr < prev) {
            unitsDisplay.value = "Current reading must be greater than previous reading";
        } else {
            unitsDisplay.value = "";
        }
    }

    prevInput.addEventListener('input', updateUnitsPreview);
    currInput.addEventListener('input', updateUnitsPreview);
</script>

</body>
</html>
