@echo off

SETLOCAL

pushd .


REM
REM  TODO -  Depending on how long this is supposed to live, convert this to ant.   It was supposed to be a one-off hacky script, but it grew...
REM 

if "%1"=="help" (
	@echo USAGE:  build-sdk-prime.cmd [workspace-root] [original-eclipse-sdk.zip]
	GOTO :END_SCRIPT
)

if "%1"==""  (
	@echo you must specify a "workspace-root"
	@echo USAGE:  build-sdk-prime.cmd [workspace-root] [original-eclipse-sdk.zip]
	GOTO :END_SCRIPT
)

if "%2"==""  (
	@echo you must specify a "original-eclipse-sdk.zip"
	@echo USAGE:  build-sdk-prime.cmd [workspace-root] [original-eclipse-sdk.zip]
	GOTO :END_SCRIPT
)


if NOT EXIST "%1" (
	@echo workspace-root %1 does not exist
	@echo USAGE:  build-sdk-prime.cmd [workspace-root] [original-eclipse-sdk.zip]
	GOTO :END_SCRIPT
)

if NOT EXIST "%2" (
	@echo eclipse sdk zip file %2 does not exist
	@echo USAGE:  build-sdk-prime.cmd [workspace-root] [original-eclipse-sdk.zip]
	GOTO :END_SCRIPT
)


@set ROOT=%1
@set FULL_SDK_ZIP=%2

@set TEMPDIR=\temp\eclipse_sdk_mod

@echo Creating working directory %TEMPDIR%
if exist %TEMPDIR% rm -rf %TEMPDIR%
mkdir %TEMPDIR%
cd %TEMPDIR%
@echo ...done.

REM
REM  HACKHACK:  the sh scripts generates a .cmd script which will set the variable DATE_SUFFIX
REM  to the output of the sh command `date  +%Y_%m_%d_%H_%M_%S`.  Yeah, yeah.  I should have 
REM  used ant for consistency.
REM
cd %TEMPDIR%
sh %ROOT%\org.eclipse.jdt.apt.core\scripts\make_set_date_suffix_cmd.sh
call set_date_suffix.cmd

@set RC=RC2
@set SDK_APT_ZIP=eclipse-SDK-APT-3.1%RC%-win32-%DATE_SUFFIX%.zip
@set FULL_SDK_APT_ZIP=%TEMPDIR%\%SDK_APT_ZIP%


REM
REM  build plugins
REM

@echo Building jdt.apt.core, jdt.apt.ui and jdt.core plugins...

cd %ROOT%\org.eclipse.jdt.apt.core
cmd /c ant -f scripts\exportplugin.xml

cd %ROOT%\org.eclipse.jdt.apt.ui
cmd /c ant -f scripts\exportplugin.xml

cd %ROOT%\org.eclipse.jdt.core
cmd /c ant -f scripts\exportplugin.xml

@echo ...finished building plugins.


REM
REM  explode existing zip 
REM

cd %TEMPDIR%

@echo Exploding existing SDK zip file %FULL_SDK_ZIP%...
jar xf %FULL_SDK_ZIP%
@echo ... Done.

REM
REM  update SDK with apt.ui plugin
REM

@echo Copying org.eclipse.jdt.apt.ui plugin jar and source zip...
copy /Y %ROOT%\..\plugin-export\org.eclipse.jdt.apt.ui_1.0.0\org.eclipse.jdt.apt.ui_1.0.0.jar %TEMPDIR%\eclipse\plugins\
mkdir %TEMPDIR%\eclipse\plugins\org.eclipse.jdt.source_3.1.0\org.eclipse.jdt.apt.ui_1.0.0
copy /Y %ROOT%\..\plugin-export\org.eclipse.jdt.apt.ui_1.0.0\aptui_src.zip %TEMPDIR%\eclipse\plugins\org.eclipse.jdt.source_3.1.0\src\org.eclipse.jdt.apt.ui_1.0.0\
@echo ...Done

REM
REM update SDK with jdt core
REM

@echo Copying org.eclipse.jdt.core plugin jar and source zip...
copy /Y %ROOT%\..\plugin-export\org.eclipse.jdt.core_3.1.0\org.eclipse.jdt.core_3.1.0.jar %TEMPDIR%\eclipse\plugins\
copy /Y %ROOT%\..\plugin-export\org.eclipse.jdt.core_3.1.0\src.zip %TEMPDIR%\eclipse\plugins\org.eclipse.jdt.source_3.1.0\src\org.eclipse.jdt.core_3.1.0\
@echo ...Done

REM
REM copy apt-core src.zip to SDK's directory
REM

@echo Copying aptcore_src.zip...
mkdir %TEMPDIR%\eclipse\plugins\org.eclipse.jdt.source_3.1.0\org.eclipse.jdt.apt.core_1.0.0\
copy /Y %ROOT%\..\plugin-export\org.eclipse.jdt.apt.core_1.0.0\aptcore_src.zip %TEMPDIR%\eclipse\plugins\org.eclipse.jdt.source_3.1.0\src\org.eclipse.jdt.apt.core_1.0.0\
@echo ...done.

REM
REM explode the apt-core .zip file into the SDK directory
REM

@echo Exploding org.eclipse.jdt.apt.core_1.0.0.jar into SDK...
mkdir %TEMPDIR%\eclipse\plugins\org.eclipse.jdt.apt.core_1.0.0
cd  %TEMPDIR%\eclipse\plugins\org.eclipse.jdt.apt.core_1.0.0
jar xf %ROOT%\..\plugin-export\org.eclipse.jdt.apt.core_1.0.0\org.eclipse.jdt.apt.core_1.0.0.jar
@echo ...Done.



REM
REM now zip up the new SDK 
REM

@echo Zipping up %SDK_APT_ZIP%...
cd %TEMPDIR%
jar cf %FULL_SDK_APT_ZIP% eclipse
@echo ...done.


@echo ******************************************************************************
@echo  Modified Eclipse SDK.zip file is at:
@echo         %FULL_SDK_APT_ZIP%
@echo ******************************************************************************


:END_SCRIPT
popd

ENDLOCAL



