# Mini script to clear all .class files from the directories
cd Server
rm *.class
rm *.dat
cd ../Client
rm *.class
rm *.dat
cd ..
rm Submission.zip

# Mini script to automate compilation and movement of java files - hardcoded; will need to be changed as system is worked on
cd Server
javac KeyManager.java
cp KeyManager.class ../Client/
cd ../Client
javac AuctionItem.java
cp AuctionItem.class ../Server/
cd ../Server
javac *.java
cp Auction.class ../Client/
cd ../Client
javac Client.java

# After compilation, zip the entire content (Java files, class files, and shell scripts) into Submission.zip
#cd ..
#zip -r Submission.zip Server/* Client/* *.sh
