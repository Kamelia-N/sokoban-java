@echo off
setlocal
cd /d "%~dp0"
set "JDK="
if exist "%JAVA_HOME%\bin\javac.exe" set "JDK=%JAVA_HOME%"
if not defined JDK if exist "C:\Program Files\Java\jdk-17\bin\javac.exe" set "JDK=C:\Program Files\Java\jdk-17"
if not defined JDK (
    echo JDK introuvable. Installe un JDK et renseigne JAVA_HOME.
    pause
    exit /b 1
)
if not exist build mkdir build
"%JDK%\bin\javac.exe" -encoding UTF-8 -d build src\Sokoban.java
if errorlevel 1 (
    echo La compilation a echoue. Consulte les erreurs ci-dessus.
    pause
    exit /b 1
)
"%JDK%\bin\java.exe" -cp build Sokoban
pause
