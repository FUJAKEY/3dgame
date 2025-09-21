@ECHO OFF
SET DIR=%~dp0
SET WRAPPER_JAR=%DIR%gradle\wrapper\gradle-wrapper.jar
IF NOT EXIST %WRAPPER_JAR% (
  ECHO Downloading Gradle wrapper jar...
  powershell -Command "Invoke-WebRequest -UseBasicParsing -Uri 'https://raw.githubusercontent.com/gradle/gradle/v8.2.1/gradle/wrapper/gradle-wrapper.jar' -OutFile '%WRAPPER_JAR%'"
)
SET CLASSPATH=%WRAPPER_JAR%
IF "%JAVA_HOME%"=="" (
  SET JAVA_CMD=java
) ELSE (
  SET JAVA_CMD="%JAVA_HOME%\bin\java.exe"
)
%JAVA_CMD% -Xmx64m -Xms64m -classpath %CLASSPATH% org.gradle.wrapper.GradleWrapperMain %*
