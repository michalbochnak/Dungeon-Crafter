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


// Get info from client about himself and enemies:
$singupRawData = json_decode(file_get_contents('php://input'));

$info = $singupRawData->info;
$indoSessionID = $info->session_id;
$infoGroupID = (int)$info->group_id;
$infoPlayerID = (int)$info->player_id;
$infoPlaterDone = (int)$info->done;

// Get all player records from current group
$sql = "SELECT * FROM gameplay WHERE group_id = $groupID";
$getAllPlayerRecords = $db->prepare($sql);
$getAllPlayerRecords->execute();
$allPlayersRecords = $getAllPlayerRecords->fetchAll(PDO::FETCH_ASSOC);

$playerSQL = "UPDATE gameplay SET x_coord = :xVal, y_coord = :yVal, health = :hVal WHERE group_id = $infoGroupID AND player_id = :pVal";
$playerSQLUpdate = $db->prepare($playerSQL);

for ($i = 0; $i < count($singupRawData->players); $i++) {
    $value = $singupRawData->players[$i];
    $playerSQLUpdate->execute(array(":xVal" => (int)$value->x, 
                                     ":yVal" => (int)$value->y, 
                                     ":hVal" => (int)$value->health,
                                     ":pVal" => (int)$value->pid));

}


if ($infoPlaterDone == 1) {
    $sqlTurnUpdate = "UPDATE gameplay SET turn = :tVal WHERE group_id = $infoGroupID AND player_id = :pVal";
    $turnUpdate = $db->prepare($sqlTurnUpdate);
    $monster1 = null;
    $monster2 = null;
    for ($i = 0; $i < count($singupRawData->players); $i++) {
        $value = $singupRawData->players[$i];
        if ((int)$value->pid == $infoPlayerID) {
            $turnUpdate->execute(array(":tVal" => 0, ":pVal" => $infoPlayerID));
            if (($i + 1) < count($singupRawData->players) && (int)$singupRawData->players[$i + 1]->pid > 0) {
                $turnUpdate->execute(array(":tVal" => 1, ":pVal" => (int)$singupRawData->players[$i + 1]->pid));
            } else {
                $turnUpdate->execute(array(":tVal" => 1, ":pVal" => (int)$singupRawData->players[0]->pid));
            }
        } else if ((int)$value->pid < 0) {
            if ((int)$value->pid = -1) {
                $monster1 = $value;
            }
            if ((int)$value->pid = -2) {
                $monster2 = $value;
            }
        }
    }

    if($monster1 != null and (int)$monster1->turn == 1) {
        $turnUpdate->execute(array(":tVal" => 0, ":pVal" => -1));
    } else {
        $turnUpdate->execute(array(":tVal" => 1, ":pVal" => -1));
    }

    if($monster2 != null and (int)$monster2->turn == 1) {
        $turnUpdate->execute(array(":tVal" => 0, ":pVal" => -2));
    } else {
        $turnUpdate->execute(array(":tVal" => 1, ":pVal" => -2));
    }
}