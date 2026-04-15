@REM Maven Wrapper for Windows
@echo off
java %MAVEN_OPTS% -classpath ".mvn\wrapper\maven-wrapper.jar" ^
  "-Dmaven.multiModuleProjectDirectory=%~dp0" ^
  org.apache.maven.wrapper.MavenWrapperMain %*
