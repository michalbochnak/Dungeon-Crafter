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

// Settings:
$maxPlayersInGroup = 2;
$xMax = 17;
$yMax = 12;
srand(time());

$singupRawData = json_decode(file_get_contents('php://input'));
$sessionId = $singupRawData->sessionid;
$request = $singupRawData->request;
$groupID = (int) $singupRawData->groupid;




// Response array to client:
$response = array("groupid" => $groupID, 
				  "ready" => 0, 
				  "groupmembers" => array ());
				  

// Get account ID from logged in user using session ID
$sql = "SELECT account_id FROM sessions WHERE session_id = '$sessionId'";

$getAccountID = $db->prepare($sql);
$getAccountID->execute();
$accountID = (int)$getAccountID->fetch(PDO::FETCH_ASSOC)['account_id'];

// Get player information:
$sql = "SELECT * FROM players WHERE account_id = $accountID";
$getPlayerInfo = $db->prepare($sql);
$getPlayerInfo->execute();
$playerInfo = $getPlayerInfo->fetch(PDO::FETCH_ASSOC);

// Parse player request:
if ($request == 'quit' && $groupID > 0) {
	
	// Remove player from group:
	$playerId = (int)$playerInfo['id'];
	$sql = "DELETE FROM groups WHERE group_id = $groupID AND player_id = $playerId";
	$deleteFromGroup = $db->prepare($sql);
	$deleteFromGroup->execute();

} else if ($request == 'join') {
	
	// Check if user has already assigned group ID. Otherwise, add user to group:
	if ($groupID > 0) {
		$sql = "SELECT * FROM groups WHERE group_id = $groupID";
		$getGroup = $db->prepare($sql);
		$getGroup->execute();
		$groups = $getGroup->fetchAll(PDO::FETCH_ASSOC);

		// Check if ready status can be replied:
		if (count($groups) == $maxPlayersInGroup) {
			$response['ready'] = 1;
			$xCoord = 0;
			$yCoord = 0;
			$coordQuery = "SELECT * FROM gameplay WHERE group_id = :groupId AND x_coord = :xCoord AND y_coord = :yCoord";
			while(true) {
				$xCoord = rand(1, $xMax);
				$yCoord = rand(1, $yMax);
				
				$tempResp = $db->prepare($coordQuery);
				$tempResp->execute(array (":groupId" =>$groupID, ":xCoord" => $xCoord, ":yCoord" => $yCoord));
				if ($tempResp->fetch(PDO::FETCH_ASSOC) == null) {
					break;
				}
			}

			$turn = 0;
			if($groups[0]['player_id'] == $playerInfo['id']) {
				$turn = 1;
			}
			
			

			$sql = "INSERT INTO gameplay (id, group_id, player_id, x_coord, y_coord, race, health, class, turn) VALUES (NULL, $groupID, ".(int)$playerInfo['id'].", $xCoord, $yCoord, ".(int)$playerInfo['race'].", 100, ".(int)$playerInfo['class'].", $turn)";
			$insertUserIntoGameplay = $db->prepare($sql);
			$insertUserIntoGameplay->execute();
			
		}
	
	} else {
		// Get available groups:
		$sql = "SELECT * FROM groups WHERE open = 1";
		$getGroups = $db->prepare($sql);
		$getGroups->execute();
		$groups = $getGroups->fetchAll(PDO::FETCH_ASSOC);

		// Check if any group is available for user to join, and if so join.
		// Otherwise create new one and than join:
		if (count($groups) > 0) {
			$assignedGroupID = (int)$groups[0]['group_id'];
			$sql = "INSERT INTO groups (id, group_id, player_id, open, timestamp) VALUES (NULL, $assignedGroupID, ".(int)$playerInfo['id'].", 1, '".date("Y-m-d H:i:s")."')";
			$addToGroup = $db->prepare($sql);
			$addToGroup->execute();
			
			$response['groupid'] = $assignedGroupID;

			$sql = "SELECT * FROM groups WHERE group_id = $assignedGroupID";
			$getUpdatedGroups = $db->prepare($sql);
			$getUpdatedGroups->execute();
			$updatedGroups = $getUpdatedGroups->fetchAll(PDO::FETCH_ASSOC);

			if (count($updatedGroups) == $maxPlayersInGroup) {
				$sql = "UPDATE groups SET open = 0 WHERE group_id = $assignedGroupID";
				$closeGroup = $db->prepare($sql);
				$closeGroup->execute();

				$monster1 = array(rand(1, $xMax), rand(1, $yMax));
				$monster2 = array(1,1);
				while(true) {
					$monster2[0] = rand(1, $xMax);
					$monster2[1] = rand(1, $yMax);
	
					if ($monster1[0] != $monster2[0] || $monster1[1] != $monster2[1]) {
						break;
					}
				}
				$sql1 = "INSERT INTO gameplay (id, group_id, player_id, x_coord, y_coord, race, health, class, turn) VALUES (NULL, $assignedGroupID, -1, ".$monster1[0].", ".$monster1[1].", 105, 100, -1, 1)";
				$sql2 = "INSERT INTO gameplay (id, group_id, player_id, x_coord, y_coord, race, health, class, turn) VALUES (NULL, $assignedGroupID, -2, ".$monster2[0].", ".$monster2[1].", 106, 100, -1, 0)";
				$insertMonsters = $db->prepare($sql1);
				$insertMonsters->execute();
				$insertMonsters = $db->prepare($sql2);
				$insertMonsters->execute();
			}

		} else {
			$sql = "SELECT MAX(group_id) FROM groups";
			$getMaxGroupId = $db->prepare($sql);
			$getMaxGroupId->execute();
			$maxGroupId = (int)$getMaxGroupId->fetch()[0];

			$sql = "INSERT INTO groups (id, group_id, player_id, open, timestamp) VALUES (NULL, ".++$maxGroupId.", ".(int)$playerInfo['id'].", 1, '".date("Y-m-d H:i:s")."')";
			$createNewGroup = $db->prepare($sql);
			$createNewGroup->execute();

			$response['groupid'] = $maxGroupId;
		}
	}
	$sql = "SELECT * FROM groups WHERE group_id = ".$response['groupid'];
	$getGroups = $db->prepare($sql);
	$getGroups->execute();
	$groups = $getGroups->fetchAll(PDO::FETCH_ASSOC);

	foreach ($groups as $key => $value) {
		$sql = "SELECT name FROM players WHERE id = ".(int)$value['player_id'];
		$getPlayerName = $db->prepare($sql);
		$getPlayerName->execute();
		$playerName = $getPlayerName->fetch(PDO::FETCH_ASSOC)['name'];
		array_push($response['groupmembers'], $playerName);
	}
}

print(json_encode($response));