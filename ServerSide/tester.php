<?php

error_reporting(E_ALL);

$data = array(
	'sessionid' => '55e8ad28d09da7ee03f4d2bc8b496e9e',
    'playerId' 	=> 47,
	'groupId' 	=> 7);

$options = array(
  	'http'    => array(
    'method'  => 'POST',
    'content' => json_encode($data),
    'header'  =>  'Content-Type: application/json\r\n' .
                'Accept: application/json\r\n'));

$url = "http://alexviznytsya.me/dungeoncrafter/gameplay.php";

$context  = stream_context_create( $options );
$result = file_get_contents( $url, false, $context );
var_dump($result);