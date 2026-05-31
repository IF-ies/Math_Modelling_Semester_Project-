' Veteran GUI launcher - starts silently
Set sh = CreateObject("WScript.Shell")
Set fso = CreateObject("Scripting.FileSystemObject")
scriptDir = fso.GetParentFolderName(WScript.ScriptFullName)
sh.CurrentDirectory = scriptDir
' Style 7 = SW_SHOWMINNOACTIVE: does not minimize/activate; PS will hide its own console
sh.Run "powershell.exe -NoProfile -ExecutionPolicy Bypass -File """ & scriptDir & "\veteran-ui.ps1""", 7, False
