<?php
require "config.php";

// ---------- Get & validate input ----------
if ($_SERVER['REQUEST_METHOD'] !== 'POST') {
    header("Location: index.php");
    exit;
}

$consumer_name    = trim($_POST['consumer_name'] ?? '');
$consumer_no      = trim($_POST['consumer_no'] ?? '');
$previous_reading = $_POST['previous_reading'] ?? '';
$current_reading  = $_POST['current_reading'] ?? '';
$bill_date        = $_POST['bill_date'] ?? '';

if ($consumer_name === '' || $consumer_no === '' || $previous_reading === '' || $current_reading === '' || $bill_date === '') {
    header("Location: index.php?error=" . urlencode("All fields are required."));
    exit;
}

if (!is_numeric($previous_reading) || !is_numeric($current_reading) || $previous_reading < 0 || $current_reading < 0) {
    header("Location: index.php?error=" . urlencode("Please enter valid meter readings."));
    exit;
}

$previous_reading = (int) $previous_reading;
$current_reading  = (int) $current_reading;

if ($current_reading < $previous_reading) {
    header("Location: index.php?error=" . urlencode("Current reading cannot be less than the previous reading."));
    exit;
}

$units = $current_reading - $previous_reading;

// ---------- Slab-wise bill calculation ----------
// First 50 units      -> Rs 3.50/unit
// Next 100 units      -> Rs 4.00/unit  (51 - 150)
// Next 100 units      -> Rs 5.20/unit  (151 - 250)
// Above 250 units     -> Rs 6.50/unit

$slab1_units = 0; $slab1_rate = 3.50; $slab1_amount = 0;
$slab2_units = 0; $slab2_rate = 4.00; $slab2_amount = 0;
$slab3_units = 0; $slab3_rate = 5.20; $slab3_amount = 0;
$slab4_units = 0; $slab4_rate = 6.50; $slab4_amount = 0;

$remaining = $units;

// Slab 1: first 50
$slab1_units = min($remaining, 50);
$slab1_amount = $slab1_units * $slab1_rate;
$remaining -= $slab1_units;

// Slab 2: next 100
if ($remaining > 0) {
    $slab2_units = min($remaining, 100);
    $slab2_amount = $slab2_units * $slab2_rate;
    $remaining -= $slab2_units;
}

// Slab 3: next 100
if ($remaining > 0) {
    $slab3_units = min($remaining, 100);
    $slab3_amount = $slab3_units * $slab3_rate;
    $remaining -= $slab3_units;
}

// Slab 4: everything above 250
if ($remaining > 0) {
    $slab4_units = $remaining;
    $slab4_amount = $slab4_units * $slab4_rate;
    $remaining = 0;
}

$total_amount = $slab1_amount + $slab2_amount + $slab3_amount + $slab4_amount;

// ---------- Save to MySQL ----------
$stmt = $conn->prepare(
    "INSERT INTO bills (consumer_name, consumer_no, previous_reading, current_reading, units_consumed, bill_amount, bill_date) VALUES (?, ?, ?, ?, ?, ?, ?)"
);
$stmt->bind_param("ssiiids", $consumer_name, $consumer_no, $previous_reading, $current_reading, $units, $total_amount, $bill_date);
$stmt->execute();
$bill_id = $stmt->insert_id;
$stmt->close();
?>
<!DOCTYPE html>
<html lang="en">
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1.0">
<title>Bill Result</title>
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
        <h2>Bill Summary</h2>
        <p><b>Consumer Name:</b> <?php echo htmlspecialchars($consumer_name); ?></p>
        <p><b>Consumer No:</b> <?php echo htmlspecialchars($consumer_no); ?></p>
        <p><b>Bill Date:</b> <?php echo htmlspecialchars($bill_date); ?></p>
        <p><b>Previous Reading:</b> <?php echo $previous_reading; ?> units</p>
        <p><b>Current Reading:</b> <?php echo $current_reading; ?> units</p>
        <p><b>Total Units Consumed:</b> <?php echo $units; ?> units</p>

        <table>
            <tr>
                <th>Slab</th>
                <th>Units</th>
                <th>Rate (Rs/unit)</th>
                <th>Amount (Rs)</th>
            </tr>
            <tr>
                <td>First 50 units</td>
                <td><?php echo $slab1_units; ?></td>
                <td><?php echo number_format($slab1_rate, 2); ?></td>
                <td><?php echo number_format($slab1_amount, 2); ?></td>
            </tr>
            <tr>
                <td>Next 100 units (51-150)</td>
                <td><?php echo $slab2_units; ?></td>
                <td><?php echo number_format($slab2_rate, 2); ?></td>
                <td><?php echo number_format($slab2_amount, 2); ?></td>
            </tr>
            <tr>
                <td>Next 100 units (151-250)</td>
                <td><?php echo $slab3_units; ?></td>
                <td><?php echo number_format($slab3_rate, 2); ?></td>
                <td><?php echo number_format($slab3_amount, 2); ?></td>
            </tr>
            <tr>
                <td>Above 250 units</td>
                <td><?php echo $slab4_units; ?></td>
                <td><?php echo number_format($slab4_rate, 2); ?></td>
                <td><?php echo number_format($slab4_amount, 2); ?></td>
            </tr>
            <tr class="total-row">
                <td colspan="3">Total</td>
                <td>Rs. <?php echo number_format($total_amount, 2); ?></td>
            </tr>
        </table>

        <div class="amount-highlight">
            Total Payable Amount
            <span>Rs. <?php echo number_format($total_amount, 2); ?></span>
        </div>

        <div class="actions">
            <a href="print-bill.php?id=<?php echo $bill_id; ?>" class="btn" target="_blank">🖨️ Print Bill</a>
            <a href="index.php" class="btn btn-secondary">Calculate Another Bill</a>
            <a href="history.php" class="btn btn-secondary">View Bill History</a>
        </div>
    </div>
</div>

</body>
</html>
