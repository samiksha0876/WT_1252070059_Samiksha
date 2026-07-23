<?php
require "config.php";

$id = $_GET['id'] ?? '';

if (!is_numeric($id)) {
    die("Invalid bill reference.");
}

$stmt = $conn->prepare("SELECT * FROM bills WHERE id = ?");
$stmt->bind_param("i", $id);
$stmt->execute();
$result = $stmt->get_result();
$bill = $result->fetch_assoc();
$stmt->close();

if (!$bill) {
    die("Bill not found.");
}

$units             = (int) $bill['units_consumed'];
$previous_reading  = (int) $bill['previous_reading'];
$current_reading   = (int) $bill['current_reading'];
$total_amount      = (float) $bill['bill_amount'];

// ---------- Recompute slab breakdown for display ----------
$slab1_units = 0; $slab1_rate = 3.50; $slab1_amount = 0;
$slab2_units = 0; $slab2_rate = 4.00; $slab2_amount = 0;
$slab3_units = 0; $slab3_rate = 5.20; $slab3_amount = 0;
$slab4_units = 0; $slab4_rate = 6.50; $slab4_amount = 0;

$remaining = $units;

$slab1_units = min($remaining, 50);
$slab1_amount = $slab1_units * $slab1_rate;
$remaining -= $slab1_units;

if ($remaining > 0) {
    $slab2_units = min($remaining, 100);
    $slab2_amount = $slab2_units * $slab2_rate;
    $remaining -= $slab2_units;
}

if ($remaining > 0) {
    $slab3_units = min($remaining, 100);
    $slab3_amount = $slab3_units * $slab3_rate;
    $remaining -= $slab3_units;
}

if ($remaining > 0) {
    $slab4_units = $remaining;
    $slab4_amount = $slab4_units * $slab4_rate;
    $remaining = 0;
}

$due_date = date('d-M-Y', strtotime($bill['bill_date'] . ' +15 days'));
$bill_no  = 'EB-' . date('Y', strtotime($bill['bill_date'])) . '-' . str_pad($bill['id'], 6, '0', STR_PAD_LEFT);
?>
<!DOCTYPE html>
<html lang="en">
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1.0">
<title>Printable Bill - <?php echo htmlspecialchars($bill_no); ?></title>
<link rel="stylesheet" href="style.css">
</head>
<body>

<div class="top-stripe no-print"></div>
<div class="navbar no-print">
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

    <div class="actions no-print" style="margin-bottom:16px;">
        <button onclick="window.print()" class="btn">🖨️ Print This Bill</button>
        <a href="history.php" class="btn btn-secondary">Back to History</a>
    </div>

    <div class="invoice card">

        <div class="invoice-header">
            <div>
                <div class="invoice-org">⚡ Electricity Billing Portal</div>
                <div class="invoice-sub">Department of Power Distribution</div>
                <div class="invoice-sub">Statement of Electricity Charges</div>
            </div>
            <div class="invoice-meta">
                <p><b>Bill No:</b> <?php echo htmlspecialchars($bill_no); ?></p>
                <p><b>Bill Date:</b> <?php echo date('d-M-Y', strtotime($bill['bill_date'])); ?></p>
                <p><b>Due Date:</b> <?php echo $due_date; ?></p>
            </div>
        </div>

        <hr class="invoice-divider">

        <div class="invoice-section">
            <h3>Consumer Details</h3>
            <table class="plain-table">
                <tr><td>Consumer Name</td><td><?php echo htmlspecialchars($bill['consumer_name']); ?></td></tr>
                <tr><td>Consumer / Meter No.</td><td><?php echo htmlspecialchars($bill['consumer_no']); ?></td></tr>
                <tr><td>Billing Period</td><td>1 month ending <?php echo date('d-M-Y', strtotime($bill['bill_date'])); ?></td></tr>
            </table>
        </div>

        <div class="invoice-section">
            <h3>Meter Reading Details</h3>
            <table>
                <tr>
                    <th>Previous Reading</th>
                    <th>Current Reading</th>
                    <th>Units Consumed</th>
                </tr>
                <tr>
                    <td><?php echo $previous_reading; ?></td>
                    <td><?php echo $current_reading; ?></td>
                    <td><?php echo $units; ?></td>
                </tr>
            </table>
        </div>

        <div class="invoice-section">
            <h3>Charges Breakdown</h3>
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
                    <td colspan="3">Total Payable</td>
                    <td>Rs. <?php echo number_format($total_amount, 2); ?></td>
                </tr>
            </table>
        </div>

        <div class="amount-highlight">
            Total Amount Payable
            <span>Rs. <?php echo number_format($total_amount, 2); ?></span>
            <small>Please pay on or before <?php echo $due_date; ?></small>
        </div>

        <div class="invoice-footer">
            <p>This is a computer-generated bill for demonstration purposes and does not require a signature.</p>
        </div>

    </div>

</div>

</body>
</html>
