' Veteran GUI launcher - sessizce baslatir
Set sh = CreateObject("WScript.Shell")
Set fso = CreateObject("Scripting.FileSystemObject")
scriptDir = fso.GetParentFolderName(WScript.ScriptFullName)
sh.CurrentDirectory = scriptDir
' Style 7 = SW_SHOWMINNOACTIVE: minimize/activate etmez, PS kendi konsolunu gizleyecek
sh.Run "powershell.exe -NoProfile -ExecutionPolicy Bypass -File """ & scriptDir & "\veteran-ui.ps1""", 7, False
