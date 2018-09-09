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

$sessionID = $singupRawData->sessionid;
$groupID = (int)$singupRawData->groupId;
$playerID = (int)$singupRawData->playerId;

$response = array ("players" => array(), "enemies" => array());

$sql = "SELECT * FROM gameplay WHERE group_id = $groupID";
$getAllPlayers = $db->prepare($sql);
$getAllPlayers->execute();
$allPlayers  = $getAllPlayers->fetchAll(PDO::FETCH_ASSOC);

$playerNotInGroup = true;
foreach ($allPlayers as $key => $value) {
  if((int)$value['player_id'] == $playerID) {
    $playerNotInGroup = false;
    break;
  }
}

if($playerNotInGroup == true) {
  print(json_encode($response));
  exit();
}

$playerNameSQL = "SELECT * FROM players WHERE id = :playerId";
$getPlayerName = $db->prepare($playerNameSQL);

foreach ($allPlayers as $key => $value) {
    if((int)$value['player_id'] < 0) {
    

    array_push($response['enemies'], array("playerId" => (int)$value['player_id'], 
                                            "playerName" => "Enemy",
                                            "x" => (int)$value['x_coord'], 
                                            "y" => (int)$value['y_coord'],
                                            "health" => (int)$value['health'],
                                            "turn" => (int)$value['turn'],
                                            "race" => (int)$value['race']));
    } else {
        $getPlayerName->execute(array(":playerId" => (int)$value['player_id']));
        $playerName = $getPlayerName->fetch(PDO::FETCH_ASSOC)['name'];
        array_push($response['players'], array("playerId" => (int)$value['player_id'],
                                               "playerName" => $playerName,
                                               "x" => (int)$value['x_coord'], 
                                               "y" => (int)$value['y_coord'],
                                               "health" => (int)$value['health'],
                                               "turn" => (int)$value['turn'],
                                               "race" => (int)$value['race']));
    }
}

print(json_encode($response));