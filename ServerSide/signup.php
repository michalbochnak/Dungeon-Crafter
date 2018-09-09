<?php

error_reporting(E_ALL);

header("Content-Type: application/json; charset=UTF-8");


try {
    $db = new PDO('mysql:host=localhost;dbname=DungeonCrafter;charset=utf8', 
    				  'avizny2', 
    				  'CS440@18');
} catch (Exception $e) {
    die('Erreur : '.$e->getMessage());
}

$singupRawData = json_decode(file_get_contents('php://input'));

$username = trim($singupRawData->email);
$userpasswd = trim($singupRawData->password);

$sql = "SELECT * FROM accounts WHERE username = '$username'";
$getUsers = $db->prepare($sql);
$getUsers->execute();
$users = $getUsers->fetch(PDO::FETCH_ASSOC);


$response = array("status" => 2,
                  "sessionid" => "0");

$currentTime = time();

if ($users == false) {
	$response['status'] = 3;
	$sessionid = md5($username.$currentTime);
	$response['sessionid'] = $sessionid;

	$sql = "INSERT INTO accounts (id, username, passwd, created, last_login, active, confirmed, completed) VALUES ( NULL, '$username', '$userpasswd', NULL, '".date("Y-m-d H:i:s")."', '1', '	1', '0')";
	$createUser = $db->prepare($sql);
	$createUser->execute();

	$sql = "SELECT * FROM accounts WHERE username = '$username'";
	$getCreatedUserId = $db->prepare($sql);
	$getCreatedUserId->execute();
	$createdUserId = $getCreatedUserId->fetch(PDO::FETCH_ASSOC);

	$sql = "INSERT INTO players (id, account_id, name, race, class, xp, intelligence, strength, level) VALUES (NULL, ".(int)$createdUserId['id'].", '', 0, 0, 0, 0, 0, 0)";
	$createPlayer = $db->prepare($sql);
	$createPlayer->execute();

	$sql = "INSERT INTO sessions (id, account_id, session_id, ttl) VALUES (NULL, ".(int)$createdUserId['id'].",'$sessionid', 30)";
		$insert = $db->prepare($sql);
		$insert->execute();
}

print(json_encode($response));