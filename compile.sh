# Create folder for storing compiled java files (.class)
mkdir -p bin

# Compile generated java code
javac -cp bin/ -d bin/ src/*/*.java
#javac -g -d bin/ src/*.java

