<?php
error_reporting(E_ALL);
header("Content-Type: application/json; charset=UTF-8");
try {
    $db = new PDO('mysql:host=localhost;dbname=DungeonCrafter;charset=utf8', 
    				  'avizny2', 
    				  'CS440@18');
} catch (Exception $e) {
    die('Error : '.$e->getMessage());
}

$singupRawData = json_decode(file_get_contents('php://input'));

$sessionid = $singupRawData->sessionid;
$playerName = $singupRawData->name;
$playerRace = $singupRawData->race;
$playerClass = $singupRawData->class;

$response = array("resultCode" => 0);


$sql = "SELECT * FROM sessions WHERE session_id = '$sessionid'";
$getSessionId = $db->prepare($sql);
$getSessionId->execute();
$sessionIdResult = $getSessionId->fetch(PDO::FETCH_ASSOC);

if ($sessionIdResult != false && $sessionIdResult['session_id'] == $sessionid) {
	$sql = "UPDATE players SET name = '$playerName', race = $playerRace, class = $playerClass, health = 100, xp = 10, intelligence = 100, strength = 100, level = 1 WHERE account_id = ".(int)$sessionIdResult['account_id'];
	$updateCharacter = $db->prepare($sql);
	$updateCharacter->execute();
	$response['resultCode'] = 1;

	$sql = "UPDATE accounts SET completed = 1 WHERE id = ".(int)$sessionIdResult['account_id'];
	$updateAccount = $db->prepare($sql);
	$updateAccount->execute();

}
print(json_encode($response));