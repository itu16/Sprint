@echo off
setlocal enabledelayedexpansion

:: Déclaration des variables

set "war_name=framework"
set "work_dir=I:\Cours\S4\Mr_Naina\Sprint\framework"
set "libservlet=I:\app_info\apache-tomcat-11.0.0-M4\lib\servlet-api.jar"
set "temp=%work_dir%\temp"
set "lib=%work_dir%\lib"
set "src=%work_dir%\src"

:: Effacer le dossier [temp]
if exist "%temp%" (
    rd /s /q "%temp%"
)

:: Créer la structure de dossier
mkdir "%temp%\lib"
mkdir "%temp%\classes"

dir /s /B "%src%\*.java" > sources.txt
:: Créer une liste de tous les fichiers .jar dans le répertoire lib et ses sous-répertoires
dir /s /B "%lib%\*.jar" > libs.txt

:: Exécuter la commande javac
javac -d "%temp%\classes" -cp "%libservlet%" @sources.txt
:: Supprimer les fichiers sources.txt et libs.txt après la compilation
del sources.txt
del libs.txt

:: Créer un fichier .jar nommé [war_name].jar à partir du dossier [temp] et son contenu dans le dossier [work_dir]
cd "%temp%\classes"
jar cvf "%lib%\%war_name%.jar" *

echo Deploiement termine.
pause
