Add-Type -AssemblyName System.Windows.Forms
Add-Type -AssemblyName System.Drawing
[System.Windows.Forms.Application]::EnableVisualStyles()

# PowerShell konsol penceresini gizle (form acilmadan once)
$null = Add-Type -Name ConWin -Namespace Native -PassThru -MemberDefinition '
[DllImport("kernel32.dll")] public static extern IntPtr GetConsoleWindow();
[DllImport("user32.dll")] public static extern bool ShowWindow(IntPtr hWnd, int nCmdShow);
'
$consoleHandle = [Native.ConWin]::GetConsoleWindow()
if ($consoleHandle -ne [IntPtr]::Zero) {
    [void][Native.ConWin]::ShowWindow($consoleHandle, 0)
}

$scriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path
$srcDir    = Join-Path $scriptDir "src"
$outDir    = Join-Path $scriptDir "out"

# ---------- JDK bul (javac gerekli) ----------
# Donmus exe yerine: her acilista guncel kaynagi derle + calistir.
function Find-JavacDir {
    if ($env:JAVA_HOME -and (Test-Path (Join-Path $env:JAVA_HOME "bin\javac.exe"))) {
        return (Join-Path $env:JAVA_HOME "bin")
    }
    $onPath = Get-Command javac.exe -ErrorAction SilentlyContinue
    if ($onPath) { return (Split-Path $onPath.Source) }
    $userJdk = Get-ChildItem $env:USERPROFILE -Directory -Filter 'jdk*' -ErrorAction SilentlyContinue |
        ForEach-Object { Join-Path $_.FullName 'bin\javac.exe' } |
        Where-Object { Test-Path $_ } | Select-Object -First 1
    if ($userJdk) { return (Split-Path $userJdk) }
    $androidJbr = 'C:\Program Files\Android\Android Studio\jbr\bin\javac.exe'
    if (Test-Path $androidJbr) { return (Split-Path $androidJbr) }
    return $null
}

$jdkBin = Find-JavacDir
if (-not $jdkBin) {
    [System.Windows.Forms.MessageBox]::Show(
        "JDK bulunamadi (javac.exe gerekli).`nBir JDK kurun veya JAVA_HOME tanimlayin.",
        "Hata",
        [System.Windows.Forms.MessageBoxButtons]::OK,
        [System.Windows.Forms.MessageBoxIcon]::Error) | Out-Null
    return
}
$javacExe = Join-Path $jdkBin "javac.exe"
$javaExe  = Join-Path $jdkBin "java.exe"

# ---------- Kaynagi derle (guncel kod) ----------
$srcFiles = (Get-ChildItem $srcDir -Recurse -Filter *.java).FullName
$compileLog = & $javacExe -encoding UTF-8 -d $outDir $srcFiles 2>&1
if ($LASTEXITCODE -ne 0) {
    [System.Windows.Forms.MessageBox]::Show(
        "Derleme hatasi:`n`n$($compileLog -join "`n")",
        "Hata",
        [System.Windows.Forms.MessageBoxButtons]::OK,
        [System.Windows.Forms.MessageBoxIcon]::Error) | Out-Null
    return
}

# Thread-safe queue
$queue = [System.Collections.Concurrent.ConcurrentQueue[string]]::new()

# ---------- Form ----------
$form = New-Object System.Windows.Forms.Form
$form.Text          = "Veteran - Urban Logistics & Distribution System"
$form.Size          = New-Object System.Drawing.Size(1050, 720)
$form.StartPosition = "CenterScreen"
$form.BackColor     = [System.Drawing.Color]::FromArgb(30, 30, 30)
$form.ForeColor     = [System.Drawing.Color]::White

# Title bar
$titleBar = New-Object System.Windows.Forms.Panel
$titleBar.Dock = "Top"; $titleBar.Height = 48
$titleBar.BackColor = [System.Drawing.Color]::FromArgb(45, 45, 48)

$titleLabel = New-Object System.Windows.Forms.Label
$titleLabel.Text = "  VETERAN  ::  Urban Logistics System"
$titleLabel.Font = New-Object System.Drawing.Font("Segoe UI", 14, [System.Drawing.FontStyle]::Bold)
$titleLabel.ForeColor = [System.Drawing.Color]::FromArgb(0, 200, 120)
$titleLabel.Dock = "Fill"; $titleLabel.TextAlign = "MiddleLeft"
$titleBar.Controls.Add($titleLabel)

# Side bar
$sideBar = New-Object System.Windows.Forms.Panel
$sideBar.Dock = "Right"; $sideBar.Width = 220
$sideBar.BackColor = [System.Drawing.Color]::FromArgb(37, 37, 38)

$buttonInfo = @(
    @{ Text = "1  Master Registry";      Cmd = "1" }
    @{ Text = "2  Intake Buffer";         Cmd = "2" }
    @{ Text = "3  Delivery Queue";        Cmd = "3" }
    @{ Text = "4  Truck Loading";         Cmd = "4" }
    @{ Text = "5  Address Directory";     Cmd = "5" }
    @{ Text = "6  City Map";              Cmd = "6" }
    @{ Text = "7  Shortest Path";         Cmd = "7" }
    @{ Text = "8  Minimum Spanning Tree"; Cmd = "8" }
    @{ Text = "9  Run Full Demo";         Cmd = "9" }
    @{ Text = "10 Yeni Paket Ekle";       Cmd = "10" }
    @{ Text = "11 Yeni Rota Ekle";        Cmd = "11" }
    @{ Text = "12 Paket Sil";             Cmd = "12" }
    @{ Text = "0  Exit Program";          Cmd = "0" }
)

# Input bar
$inputBar = New-Object System.Windows.Forms.Panel
$inputBar.Dock = "Bottom"; $inputBar.Height = 50
$inputBar.BackColor = [System.Drawing.Color]::FromArgb(45, 45, 48)
$inputBar.Padding = New-Object System.Windows.Forms.Padding(10, 8, 10, 8)

$sendBtn = New-Object System.Windows.Forms.Button
$sendBtn.Text = "Gonder"; $sendBtn.Dock = "Right"; $sendBtn.Width = 100
$sendBtn.FlatStyle = "Flat"
$sendBtn.BackColor = [System.Drawing.Color]::FromArgb(0, 120, 80)
$sendBtn.ForeColor = [System.Drawing.Color]::White
$sendBtn.Font = New-Object System.Drawing.Font("Segoe UI", 10, [System.Drawing.FontStyle]::Bold)

$inputBox = New-Object System.Windows.Forms.TextBox
$inputBox.Dock = "Fill"
$inputBox.Font = New-Object System.Drawing.Font("Consolas", 11)
$inputBox.BackColor = [System.Drawing.Color]::FromArgb(25, 25, 25)
$inputBox.ForeColor = [System.Drawing.Color]::White
$inputBox.BorderStyle = "FixedSingle"

$inputBar.Controls.Add($inputBox)
$inputBar.Controls.Add($sendBtn)

# Output
$output = New-Object System.Windows.Forms.RichTextBox
$output.Dock = "Fill"
$output.ReadOnly = $true
$output.Font = New-Object System.Drawing.Font("Consolas", 10)
$output.BackColor = [System.Drawing.Color]::Black
$output.ForeColor = [System.Drawing.Color]::FromArgb(180, 255, 180)
$output.BorderStyle = "None"
$output.DetectUrls = $false
$output.WordWrap = $false

$form.Controls.Add($output)
$form.Controls.Add($sideBar)
$form.Controls.Add($inputBar)
$form.Controls.Add($titleBar)

# ---------- Process ----------
$psi = New-Object System.Diagnostics.ProcessStartInfo
$psi.FileName = $javaExe
$psi.Arguments = "-Dstdout.encoding=UTF-8 -Dstderr.encoding=UTF-8 -Dfile.encoding=UTF-8 -cp `"$outDir`" Main"
$psi.WorkingDirectory = $scriptDir
$psi.UseShellExecute = $false
$psi.RedirectStandardInput = $true
$psi.RedirectStandardOutput = $true
$psi.RedirectStandardError = $true
$psi.CreateNoWindow = $true
$psi.StandardOutputEncoding = [System.Text.Encoding]::UTF8
$psi.StandardErrorEncoding = [System.Text.Encoding]::UTF8

$proc = New-Object System.Diagnostics.Process
$proc.StartInfo = $psi
[void]$proc.Start()

# Background thread: stdout'u oku, queue'ya yaz
$readerScript = {
    param($stream, $queueRef, $prefix)
    try {
        while (-not $stream.EndOfStream) {
            $line = $stream.ReadLine()
            if ($null -ne $line) {
                $queueRef.Enqueue($prefix + $line + "`n")
            }
        }
    } catch {}
}

# stdout reader
$psOut = [PowerShell]::Create()
[void]$psOut.AddScript($readerScript).AddArgument($proc.StandardOutput).AddArgument($queue).AddArgument("")
$asyncOut = $psOut.BeginInvoke()

# stderr reader
$psErr = [PowerShell]::Create()
[void]$psErr.AddScript($readerScript).AddArgument($proc.StandardError).AddArgument($queue).AddArgument("__ERR__")
$asyncErr = $psErr.BeginInvoke()

# Side button event'leri (proc artik var, kapatabilirler)
$btnY = 10
foreach ($info in $buttonInfo) {
    $btn = New-Object System.Windows.Forms.Button
    $btn.Text = $info.Text
    $btn.Tag = $info.Cmd
    $btn.Location = New-Object System.Drawing.Point(10, $btnY)
    $btn.Size = New-Object System.Drawing.Size(195, 38)
    $btn.FlatStyle = "Flat"
    $btn.BackColor = [System.Drawing.Color]::FromArgb(60, 60, 65)
    $btn.ForeColor = [System.Drawing.Color]::White
    $btn.Font = New-Object System.Drawing.Font("Segoe UI", 9)
    $btn.TextAlign = "MiddleLeft"
    $btn.FlatAppearance.BorderColor = [System.Drawing.Color]::FromArgb(0, 120, 80)
    $btn.Add_Click({
        param($s, $e)
        if (-not $proc.HasExited) {
            try {
                $proc.StandardInput.WriteLine($s.Tag)
                $proc.StandardInput.Flush()
                $queue.Enqueue("__USER__> " + $s.Tag + "`n")
            } catch {
                $queue.Enqueue("__ERR__[Input hatasi: $($_.Exception.Message)]`n")
            }
        }
    }.GetNewClosure())
    $sideBar.Controls.Add($btn)
    $btnY += 44
}

# UI Timer
$timer = New-Object System.Windows.Forms.Timer
$timer.Interval = 50
$timer.Add_Tick({
    $appended = $false
    $line = $null
    while ($queue.TryDequeue([ref]$line)) {
        $appended = $true
        $color = [System.Drawing.Color]::FromArgb(180, 255, 180)
        $text  = $line
        if ($line.StartsWith("__USER__")) {
            $color = [System.Drawing.Color]::FromArgb(255, 200, 0)
            $text  = $line.Substring(8)
        } elseif ($line.StartsWith("__ERR__")) {
            $color = [System.Drawing.Color]::OrangeRed
            $text  = $line.Substring(7)
        }
        $output.SelectionStart  = $output.TextLength
        $output.SelectionLength = 0
        $output.SelectionColor  = $color
        $output.AppendText($text)
    }
    if ($appended) {
        $output.SelectionStart = $output.TextLength
        $output.ScrollToCaret()
    }
}.GetNewClosure())
$timer.Start()

# Send button
$sendBtn.Add_Click({
    $cmd = $inputBox.Text
    if (-not $proc.HasExited) {
        try {
            $proc.StandardInput.WriteLine($cmd)
            $proc.StandardInput.Flush()
            $queue.Enqueue("__USER__> $cmd`n")
        } catch {
            $queue.Enqueue("__ERR__[$($_.Exception.Message)]`n")
        }
    }
    $inputBox.Clear()
    $inputBox.Focus()
}.GetNewClosure())

$inputBox.Add_KeyDown({
    if ($_.KeyCode -eq [System.Windows.Forms.Keys]::Enter) {
        $sendBtn.PerformClick()
        $_.Handled = $true
        $_.SuppressKeyPress = $true
    }
}.GetNewClosure())

$form.Add_Shown({ $inputBox.Focus() })

$form.Add_FormClosing({
    try {
        $timer.Stop()
        if (-not $proc.HasExited) {
            try { $proc.StandardInput.WriteLine("0") } catch {}
            Start-Sleep -Milliseconds 300
            if (-not $proc.HasExited) { $proc.Kill() }
        }
        try { $psOut.Stop(); $psOut.Dispose() } catch {}
        try { $psErr.Stop(); $psErr.Dispose() } catch {}
    } catch {}
}.GetNewClosure())

[System.Windows.Forms.Application]::Run($form)
