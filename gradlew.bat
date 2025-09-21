@ECHO OFF
SETLOCAL
SET BASEDIR=%~dp0
SET WRAPPER_JAR=%BASEDIR%gradle\wrapper\gradle-wrapper.jar
SET WRAPPER_VERSION=8.7
SET WRAPPER_URL=https://repo1.maven.org/maven2/org/gradle/wrapper/gradle-wrapper/%WRAPPER_VERSION%/gradle-wrapper-%WRAPPER_VERSION%.jar
IF NOT EXIST "%WRAPPER_JAR%" (
    ECHO Downloading Gradle wrapper JAR %WRAPPER_VERSION%...
    powershell -Command "& {Param($u,$p) (New-Object Net.WebClient).DownloadFile($u,$p)}" %WRAPPER_URL% "%WRAPPER_JAR%"
)
SET JAVA_EXE=%JAVA_HOME%\bin\java.exe
IF NOT EXIST "%JAVA_EXE%" SET JAVA_EXE=java
"%JAVA_EXE%" -classpath "%WRAPPER_JAR%" org.gradle.wrapper.GradleWrapperMain %*
