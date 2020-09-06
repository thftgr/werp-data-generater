@echo off
setlocal
cd %~dp0 >outx.txt
rem variable a
set sum=0
set sumz=
for /f "delims=" %%a in (list.txt) do (
call set /a sum=%%sum%%+1
call set sumz=00000%%sum%%
call set sumz=%%sumz:~-4%%
call set source.a.find.[%%sumz%%]=%%a
)
set source.a.find

rem variable delete
for /f "usebackq tokens=1* delims==" %%f in (`set source`) do (
for /f "usebackq tokens=1* delims==" %%c in (`set source^|find /c "%%g"`) do (
if %%c gtr 1 (
set %%f=
)
)
)

for /f "usebackq tokens=1* delims==" %%o in (`set source.a.find^|sort`) do (
echo %%p>>outx.txt
)
pause