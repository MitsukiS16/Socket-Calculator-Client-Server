# Start Peer instances
#xterm -hold -e "java Peer localhost 5001 localhost 5000 localhost 5002 localhost 5003" &
#xterm -hold -e "java Peer localhost 5004 localhost 5003" &
#xterm -hold -e "java Peer localhost 5005 localhost 5003" &
xterm -hold -e "java App localhost 5001 localhost 5002" &
xterm -hold -e "java App localhost 5002 localhost 5001 localhost 5003 localhost 5004" &
xterm -hold -e "java App localhost 5003 localhost 5002" &
xterm -hold -e "java App localhost 5004 localhost 5002 localhost 5005  localhost 5006" &
xterm -hold -e "java App localhost 5005 localhost 5004" &
xterm -hold -e "java App localhost 5006 localhost 5004" &

echo "Peers started. Waiting for initialization."
