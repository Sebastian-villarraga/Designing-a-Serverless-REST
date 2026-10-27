param([Parameter(Mandatory=$true)][string]$BaseUrl)
$ErrorActionPreference = 'Stop'
$BaseUrl = $BaseUrl.TrimEnd('/')
function Test-Api([string]$Method, [string]$Path, [int]$Expected, [string]$Body = '') {
    $tempBody = [IO.Path]::GetTempFileName()
    $tempHeaders = [IO.Path]::GetTempFileName()
    $tempInput = [IO.Path]::GetTempFileName()
    try {
        $curlArgs = @('-sS', '-X', $Method, "$BaseUrl$Path", '-o', $tempBody, '-D', $tempHeaders, '-w', '%{http_code}')
        if ($Body) {
            [IO.File]::WriteAllText($tempInput, $Body, [Text.UTF8Encoding]::new($false))
            $curlArgs += @('-H', 'Content-Type: application/json', '--data-binary', "@$tempInput")
        }
        $status = & curl.exe @curlArgs
        if ($LASTEXITCODE -ne 0) { throw 'curl failed' }
        $response = [IO.File]::ReadAllText($tempBody)
        Write-Host "$Method $Path -> $status $response"
        if ([int]$status -ne $Expected) { throw "Expected $Expected, got $status" }
        if ($Expected -eq 204 -and $response.Length -ne 0) { throw '204 body must be empty' }
        if ($Expected -eq 201 -and [IO.File]::ReadAllText($tempHeaders) -notmatch '(?im)^location: /orders/ORD-') { throw 'Missing Location header' }
    } finally {
        Remove-Item -LiteralPath $tempBody, $tempHeaders, $tempInput
    }
}
Test-Api POST '/orders' 201 '{"customerId":"CUST-42","product":"MacBook Air","quantity":1}'
Test-Api GET '/orders/ORD-42' 200
Test-Api GET '/orders?customerId=CUST-42' 200
Test-Api GET '/orders?customerId=CUST-100' 200
Test-Api GET '/orders/ORD-999' 404
Test-Api POST '/orders' 400 '{}'
Test-Api DELETE '/orders/ORD-42' 204
