<?php
/**
 * Created by PhpStorm.
 * User: Artur
 * Date: 2/9/2018
 * Time: 2:56 PM
 */
error_reporting(E_ALL);

$host = '127.0.0.1';
$db   = 'DungeonCrafter';
$user = 'awojci5';
$pass = 'CS440@18';
$charset = 'utf8mb4';

$dsn = "mysql:host=$host;dbname=$db;charset=$charset";
$opt = [
    PDO::ATTR_ERRMODE            => PDO::ERRMODE_EXCEPTION,
    PDO::ATTR_DEFAULT_FETCH_MODE => PDO::FETCH_ASSOC,
    PDO::ATTR_EMULATE_PREPARES   => false,
];
$pdo = new PDO($dsn, $user, $pass, $opt);





//$stmt = $pdo->query("INSERT INTO users (username, passwd) VALUES ('julaaa', 'marian')");


$stmt = $pdo->query('SELECT * FROM users');

while ($row = $stmt->fetch())
{
    echo $row['username'] . "<br>";
    echo $row['passwd'] . "\n";
}