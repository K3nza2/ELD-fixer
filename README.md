ELD FIXER 1.0

Setup:
<br>-Clone the repository<br>
-add .env file<br>
-add credentials.json <br>
(both .env and credentials.json need to be in the same file as the rest of the script.
When you open your files and see where you stored the files, you should see src and pom.xml file add your .env and credentials.json there)<br>
-Make sure you have java JDK installed (11.0 and higher, i used openjdk 17.0)<br>
-Install maven(for now it is needed, will be solved soon)<br>
-That should be it!<br>

RUN:<br>
To run the script go into your src/main/java/com/eldsolution folder and run the FirstScript.java (thats if you are running it from IDE)

alternative(running the script from the terminal):<br>
after changing to the folder where the script is stored do this command:<br>mvn exec:java -Dexec.mainClass="com.eldsolution.FirstScript"

For any questions or troubleshooting i am available to help.
