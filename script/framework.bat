@echo off
setlocal enabledelayedexpansion

:: Déclaration des variables
set "jar_name=framework_servlet"
set "work_dir=I:\Cours\S4\Mr_Naina\Sprint"

set "temp=%work_dir%\temp"
set "src=%work_dir%\src"
set "libs=%work_dir%\lib"

@REM CHEMIN AMETRAHANA ANLE LIB
set "lib=%work_dir%\test\lib"


:: Effacer le dossier [temp]
if exist "%temp%" (
    rd /s /q "%temp%"
)

:: Créer la structure de dossier
mkdir "%temp%\classes"
mkdir "%lib%"
:: Compilation des fichiers .java dans src avec les options suivantes
:: Note: Assurez-vous que le chemin vers le compilateur Java (javac) est correctement configuré dans votre variable d'environnement PATH.
:: Créer une liste de tous les fichiers .java dans le répertoire src et ses sous-répertoires
dir /s /B "%src%\*.java" > sources.txt
:: Créer une liste de tous les fichiers .jar dans le répertoire lib et ses sous-répertoires
dir /s /B "%libs%\*.jar" > libs.txt
:: Construire le classpath
set "classpath="
for /F "delims=" %%i in (libs.txt) do set "classpath=!classpath!%%i;"
:: Exécuter la commande javac
javac -d "%temp%\classes" -cp "%classpath%" @sources.txt
:: Supprimer les fichiers sources.txt et libs.txt après la compilation
del sources.txt
del libs.txt

cd "%temp%\classes"
jar cvf "%lib%\%jar_name%.jar" *

cd "%work_dir%"
if exist "%temp%" (
    rd /s /q "%temp%"
)
echo Cretion Jar Terminer.
pause
