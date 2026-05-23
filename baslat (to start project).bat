@echo off
title Veteran - Urban Logistics System
echo Derleniyor... (Lutfen bekleyin)
"C:\Program Files\Java\jdk-24\bin\javac" -d out src/models/Package.java src/structures/linear/*.java src/structures/tree/*.java src/structures/graph/*.java src/Main.java

echo Program Baslatiliyor...
cls
"C:\Program Files\Java\jdk-24\bin\java" -cp out Main

echo.
pause
