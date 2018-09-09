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
$sessionId = $singupRawData->sessionid;

$sql = "SELECT * FROM sessions WHERE session_id = '$sessionId'";
$getSessionId = $db->prepare($sql);
$getSessionId->execute();
$sessionIdResult = $getSessionId->fetch(PDO::FETCH_ASSOC);

$response = array ( "playerId" => 0,
					"playerName" => "",
					"playerRace" => 0,
					"playerClass" => 0,
					"playerXP" => 0,
					"playerLevel" => 0,
					"playerIntelligence" => 0,
					"playerHealth" => 0
				);

if($sessionIdResult != false) {
	$sql = "SELECT * FROM players WHERE account_id = ".(int)$sessionIdResult['account_id'];
	$getPlayerData = $db->prepare($sql);
	$getPlayerData->execute();
	$playerData = $getPlayerData->fetch(PDO::FETCH_ASSOC);

	$response['playerId'] = $playerData['id'];
	$response['playerName'] = $playerData['name'];
	$response['playerRace'] = (int)$playerData['race'];
	$response['playerClass'] = (int)$playerData['class'];
	$response['playerXP'] = (int)$playerData['xp'];
	$response['playerLevel'] = (int)$playerData['level'];
	$response['playerIntelligence'] = (int)$playerData['intelligence'];
	$response['playerHealth'] = (int)$playerData['health'];
}
print(json_encode($response));

