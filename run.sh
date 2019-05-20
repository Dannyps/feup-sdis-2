if [ $# -eq 1 ]; then
    java -cp bin/ service.Init $1
else
    java -cp bin/ service.Init $1 $2
fi