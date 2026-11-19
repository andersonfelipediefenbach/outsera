# Hook UserPromptSubmit: registra cada prompt em docs/ai/prompts.md (auditoria).
# Compativel com Windows PowerShell 5.1+ e PowerShell 7. Nunca bloqueia o prompt.
$ErrorActionPreference = 'Stop'
try {
    $utf8 = New-Object System.Text.UTF8Encoding($false)
    [Console]::InputEncoding = $utf8
    $raw = [Console]::In.ReadToEnd()
    if ([string]::IsNullOrWhiteSpace($raw)) { exit 0 }
    $data = $raw | ConvertFrom-Json

    $root = $env:CLAUDE_PROJECT_DIR
    if ([string]::IsNullOrWhiteSpace($root)) { $root = $data.cwd }
    if ([string]::IsNullOrWhiteSpace($root)) { $root = (Get-Location).Path }

    $dir  = Join-Path $root 'docs\ai'
    $file = Join-Path $dir 'prompts.md'
    if (-not (Test-Path $dir)) { New-Item -ItemType Directory -Path $dir -Force | Out-Null }
    if (-not (Test-Path $file)) {
        [System.IO.File]::WriteAllText($file, "# Registro de prompts (IA)`n`nGerado automaticamente pelo hook UserPromptSubmit (.claude/hooks/log-prompt.ps1).`n", $utf8)
    }

    $prompt = [string]$data.prompt
    # Cerca de codigo dinamica: maior sequencia de crases no prompt + 1 (minimo 3)
    $max = 0
    foreach ($m in [regex]::Matches($prompt, '`+')) { if ($m.Length -gt $max) { $max = $m.Length } }
    $fence = '`' * [Math]::Max(3, $max + 1)

    $ts = (Get-Date).ToString('yyyy-MM-dd HH:mm:ss zzz')
    $entry = "`n## $ts`n`n- Sessao: ``$($data.session_id)```n`n${fence}text`n$prompt`n$fence`n"

    # Mutex para evitar escrita concorrente entre sessoes
    $mutex = New-Object System.Threading.Mutex($false, 'Global\ClaudePromptLog')
    [void]$mutex.WaitOne(5000)
    try { [System.IO.File]::AppendAllText($file, $entry, $utf8) }
    finally { $mutex.ReleaseMutex(); $mutex.Dispose() }
} catch {
    [Console]::Error.WriteLine("log-prompt: $($_.Exception.Message)")
}
exit 0
