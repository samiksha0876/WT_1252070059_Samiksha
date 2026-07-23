<?php
// ---------------------------------------------------
// Database connection settings for XAMPP (default MySQL)
// Default XAMPP MySQL user is "root" with an EMPTY password.
// Change these only if you set a different username/password.
// ---------------------------------------------------
$db_host = "localhost";
$db_user = "root";
$db_pass = "";
$db_name = "electricity_billing";

$conn = mysqli_connect($db_host, $db_user, $db_pass, $db_name);

if (!$conn) {
    die("Database connection failed: " . mysqli_connect_error());
}
?>
