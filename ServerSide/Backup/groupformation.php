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
$request = $singupRawData->request;
$groupID = (int)$singupRawData->groupid;

$sql = "SELECT * FROM sessions WHERE session_id = '$sessionId'";
$getSessionId = $db->prepare($sql);
$getSessionId->execute();
$sessionIdResult = $getSessionId->fetch(PDO::FETCH_ASSOC);

$sql = "SELECT account_id FROM sessions WHERE session_id = '$sessionId'";
$getAccountID = $db->prepare($sql);
$getAccountID->execute();
$accountID = (int)$getAccountID->fetch(PDO::FETCH_ASSOC)['account_id'];

$response = array ( "groupid" => 0,
					"ready" => 0,
					"groupmembers" => array ()
	);

if ($request == 'quit') {
	$sql = "DELETE FROM groups WHERE group_id = $groupID AND account_id = $accountID";
	$deleteFromGroup = $db->prepare($sql);
	$deleteFromGroup->execute();

	$sql = "UPDATE groups SET open = 1 WHERE group_id = $groupID";
	$updateOpenGroup = $db->prepare($sql);
	$updateOpenGroup->execute();


} else if ($request == 'join') {

	if ($sessionIdResult != false) {

		$availableGroups = false;
		if ($groupID > 0) {
			$sql = "SELECT * from groups WHERE group_id = $groupID";
			$getAvailablesGroups = $db->prepare($sql);
			$getAvailablesGroups->execute();
			$availableGroups = $getAvailablesGroups->fetchAll(PDO::FETCH_ASSOC);
		} else {
			$sql = "SELECT * from groups WHERE open = 1";
			$getAvailablesGroups = $db->prepare($sql);
			$getAvailablesGroups->execute();
			$availableGroups = $getAvailablesGroups->fetchAll(PDO::FETCH_ASSOC);
		}

		if (count($availableGroups) > 0) {
			$userInGroup = false;
			$groupID = (int)$availableGroups[0]['group_id'];
			$response['groupid'] = $groupID;
			
			foreach ($availableGroups as $key => $value) {
				if ($value['account_id'] == $accountID) {
					$userInGroup = true;
					break;
				}
			}

			if ($userInGroup == false) {
			
				$sql = "INSERT INTO groups (id, group_id, open, timestamp, account_id) VALUES (NULL, $groupID, 1, '".date("Y-m-d H:i:s")."', $accountID)";
				$addToGroup = $db->prepare($sql);
				$addToGroup->execute();

				$sql = "SELECT * from groups WHERE open = 1";
				$getAvailablesGroups = $db->prepare($sql);
				$getAvailablesGroups->execute();
				$availableGroups = $getAvailablesGroups->fetchAll(PDO::FETCH_ASSOC);
			}
		
			if (count($availableGroups) >= 2) {
				$sql = "UPDATE groups SET open = 0 WHERE group_id = $groupID";
				$closeGroup = $db->prepare($sql);
				$closeGroup->execute();
				
				

				$response['ready'] = 1;
			}

			foreach ($availableGroups as $key => $value) {
				$sql = "SELECT name FROM players WHERE account_id = ".(int)$value['account_id'];
				$getPlayerName = $db->prepare($sql);
				$getPlayerName->execute();
				$playerName = $getPlayerName->fetch(PDO::FETCH_ASSOC)['name'];
				array_push($response['groupmembers'], $playerName);
			}

		} else {

			$sql = "SELECT MAX(group_id) FROM groups";
			$getMaxGroupId = $db->prepare($sql);
			$getMaxGroupId->execute();
			$maxGroupId = (int)$getMaxGroupId->fetch()[0];

			$sql = "INSERT INTO groups (id, group_id, open, timestamp, account_id) VALUES (NULL, ".++$maxGroupId.", 1, '".date("Y-m-d H:i:s")."', $accountID)";
			$createNewGroup = $db->prepare($sql);
			$createNewGroup->execute();

			$sql = "SELECT name FROM players WHERE account_id = $accountID";
			$getPlayerName = $db->prepare($sql);
			$getPlayerName->execute();
			$playerName = $getPlayerName->fetch(PDO::FETCH_ASSOC)['name'];
			$response['groupid'] = $maxGroupId;
			array_push($response['groupmembers'], $playerName);
		}
	}
}

print(json_encode($response));