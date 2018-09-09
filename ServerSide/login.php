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


$response = array("status" => 0,
                  "sessionid" => "0");

$currentTime = time();

if ($users != false && $users['passwd'] == $userpasswd) {
	$response['status'] = 1;
	$sessionid = md5($username.$currentTime);
	$response['sessionid'] = $sessionid;
	
	$sql = "SELECT * FROM sessions WHERE account_id = '".$users['id']."'";
	$getSession = $db->prepare($sql);
	$getSession->execute();
	$sessions = $getSession->fetch(PDO::FETCH_ASSOC);
	
	$sql = "UPDATE accounts SET last_login = '".date("Y-m-d H:i:s")."' WHERE id = ".(int)$users['id'];
	$updateLastLoginTimestamp = $db->prepare($sql);
	$updateLastLoginTimestamp->execute();

	if($sessions != false) {
		$sql = "UPDATE sessions SET session_id = '$sessionid', ttl = 30 WHERE account_id = ".(int)$users['id'];
		$update = $db->prepare($sql);
		$update->execute();
	} else {
		$sql = "INSERT INTO sessions (id, account_id, session_id, ttl) VALUES (NULL,".(int)$users['id'].",'$sessionid', 30)";
		$insert = $db->prepare($sql);
		$insert->execute();
	}

}
print(json_encode($response));


