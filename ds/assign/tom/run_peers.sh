# Start Peer instances
xterm -hold -e "java App localhost 5000" &
xterm -hold -e "java App localhost 5001" &
xterm -hold -e "java App localhost 5002" &
xterm -hold -e "java App localhost 5003" &
xterm -hold -e "java App localhost 5004" &
xterm -hold -e "java App localhost 5005" &

echo "Peers started. Waiting for initialization."
