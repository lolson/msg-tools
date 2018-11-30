@echo off

set "JAVA_CMD=%JAVA_X_HOME%\bin\java"
if "%JAVA_X_HOME%" == "" set JAVA_CMD=java

rem ----------------------------------------------------------------------------
rem Adding jar files to CLASSPATH
rem ----------------------------------------------------------------------------
setLocal EnableDelayedExpansion
set CLASSPATH=
for %%i in (*.jar) do (
    set CLASSPATH=!CLASSPATH!;%%i
)
setLocal DisableDelayedExpansion
set CLASSPATH="%GMSEC_HOME%"\bin\gmsecapi.jar;%CLASSPATH%

set PATH=%GMSEC_HOME%\bin;%PATH%

rem ----------------------------------------------------------------------------
rem Starting the application
rem ----------------------------------------------------------------------------
"%JAVA_CMD%" %JAVA_OPTS% -Dspring.profiles.active=prod -Dloader.path=%APP_HOME%,%APP_HOME%\config,%GMSEC_HOME%\bin -jar ${app.name}.jar %*