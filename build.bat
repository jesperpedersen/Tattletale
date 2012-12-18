IF "%ANT_HOME%"=="" goto ant_home_fail
IF "%JAVA_HOME%"=="" goto java_home_fail
%ANT_HOME%\bin\ant %1 %2 %3 %4 %5 %6
exit /b 0
:ant_home_fail
echo "Maven build error. Please review the console output."
exit /b 1
:java_home_fail
echo "Maven build error. Please review the console output."
exit /b 1
