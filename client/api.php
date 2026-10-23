<?php
declare(strict_types=1);

header('Cache-Control: no-store');
header('X-Content-Type-Options: nosniff');

function respond(int $status, array|string|null $payload): never {
    http_response_code($status);
    if ($payload === null) {
        exit;
    }
    header('Content-Type: application/json; charset=utf-8');
    if (is_string($payload)) {
        echo $payload;
    } else {
        echo json_encode($payload, JSON_UNESCAPED_UNICODE | JSON_UNESCAPED_SLASHES);
    }
    exit;
}

$service = $_GET['service'] ?? '';
$path = $_GET['path'] ?? '';
$method = strtoupper($_SERVER['REQUEST_METHOD'] ?? 'GET');

if (!in_array($method, ['GET', 'POST', 'PUT', 'DELETE'], true)) {
    header('Allow: GET, POST, PUT, DELETE');
    respond(405, ['code' => 'METHOD_NOT_ALLOWED', 'message' => 'Unsupported HTTP method']);
}

$hosts = [
    'bands' => ['base' => 'https://127.0.0.1:17272', 'cert' => __DIR__ . '/bands-ca.crt'],
    'grammy' => ['base' => 'https://127.0.0.1:35081/api', 'cert' => __DIR__ . '/grammy-ca.crt'],
];
if (!isset($hosts[$service])) {
    respond(400, ['code' => 'INVALID_SERVICE', 'message' => 'Unknown service']);
}

$allowedPath = $service === 'bands'
    ? preg_match('#^/music-bands(?:/(?:[1-9][0-9]*|average-number-of-participants|count-by-number-of-participants|search-by-name))?$#', $path) === 1
    : preg_match('#^/grammy/bands/[1-9][0-9]*/(?:singles/add|nominate/(?:PSYCHEDELIC_ROCK|HIP_HOP|SOUL|POST_ROCK|BRIT_POP))$#', $path) === 1;
if (!$allowedPath) {
    respond(404, ['code' => 'INVALID_API_PATH', 'message' => 'Path is not part of the published API']);
}

$query = $_GET;
unset($query['service'], $query['path']);
$url = $hosts[$service]['base'] . $path;
if ($query !== []) {
    $url .= '?' . http_build_query($query, '', '&', PHP_QUERY_RFC3986);
}

$requestHeaders = ['Accept: application/json'];
$body = file_get_contents('php://input');
if ($body !== false && $body !== '') {
    $requestHeaders[] = 'Content-Type: ' . ($_SERVER['CONTENT_TYPE'] ?? 'application/json');
}
$context = stream_context_create([
    'http' => [
        'method' => $method,
        'header' => implode("\r\n", $requestHeaders),
        'content' => $body === false ? '' : $body,
        'ignore_errors' => true,
        'timeout' => 15,
        'follow_location' => 0,
    ],
    'ssl' => [
        'verify_peer' => true,
        'verify_peer_name' => true,
        'cafile' => $hosts[$service]['cert'],
        'peer_name' => 'localhost',
    ],
]);

$responseBody = @file_get_contents($url, false, $context);
$responseHeaders = $http_response_header ?? [];
$status = 502;
$contentType = 'application/json; charset=utf-8';
foreach ($responseHeaders as $responseHeader) {
    if (preg_match('#^HTTP/\S+\s+(\d{3})#', $responseHeader, $matches)) {
        $status = (int)$matches[1];
    } elseif (stripos($responseHeader, 'Content-Type:') === 0) {
        $contentType = trim(substr($responseHeader, strlen('Content-Type:')));
    }
}
if ($responseBody === false && $responseHeaders === []) {
    respond(502, [
        'code' => 'UPSTREAM_UNAVAILABLE',
        'message' => ucfirst($service) . ' API did not respond; check that its HTTPS service is running.',
    ]);
}

http_response_code($status);
header('Content-Type: ' . $contentType);
if ($responseBody !== false) {
    echo $responseBody;
}
