# Start CalculatorMultiServer
xterm -hold -e "java CalculatorMultiServer localhost 3000" &
echo "Starting CalculatorMultiServer... Waiting for initialization."
sleep 1

# Start Peer instances
xterm -hold -e "java Peer localhost 20000 localhost 20001" &
xterm -hold -e "java Peer localhost 20001 localhost 20002" &
xterm -hold -e "java Peer localhost 20002 localhost 20003" &
xterm -hold -e "java Peer localhost 20003 localhost 20004" &
xterm -hold -e "java Peer localhost 20004 localhost 20005" &
xterm -hold -e "java Peer localhost 20005 localhost 20000" &
echo "Peers started. Waiting for initialization."
sleep 1

# Start Token sender
xterm -hold -e "java Token localhost 20000 token" &
echo "Token sender started. Waiting for communication."

# Wait for background processes
wait
