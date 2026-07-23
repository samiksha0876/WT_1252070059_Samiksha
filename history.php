<?php
require "config.php";

$result = $conn->query("SELECT * FROM bills ORDER BY id DESC");
?>
<!DOCTYPE html>
<html lang="en">
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1.0">
<title>Bill History</title>
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
        <h2>All Saved Bills</h2>

        <?php if ($result && $result->num_rows > 0): ?>
        <div style="overflow-x:auto;">
        <table>
            <tr>
                <th>ID</th>
                <th>Consumer Name</th>
                <th>Consumer No</th>
                <th>Prev. Reading</th>
                <th>Curr. Reading</th>
                <th>Units</th>
                <th>Amount (Rs)</th>
                <th>Bill Date</th>
                <th>Print</th>
            </tr>
            <?php while ($row = $result->fetch_assoc()): ?>
            <tr>
                <td><?php echo $row['id']; ?></td>
                <td><?php echo htmlspecialchars($row['consumer_name']); ?></td>
                <td><?php echo htmlspecialchars($row['consumer_no']); ?></td>
                <td><?php echo $row['previous_reading']; ?></td>
                <td><?php echo $row['current_reading']; ?></td>
                <td><?php echo $row['units_consumed']; ?></td>
                <td><?php echo number_format($row['bill_amount'], 2); ?></td>
                <td><?php echo $row['bill_date']; ?></td>
                <td><a href="print-bill.php?id=<?php echo $row['id']; ?>" target="_blank">🖨️ Print</a></td>
            </tr>
            <?php endwhile; ?>
        </table>
        </div>
        <?php else: ?>
            <p>No bills saved yet. <a href="index.php">Calculate your first bill</a>.</p>
        <?php endif; ?>

        <div class="actions">
            <a href="index.php" class="btn">Back to Home</a>
        </div>
    </div>
</div>

</body>
</html>
