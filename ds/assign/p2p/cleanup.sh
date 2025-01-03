#!/bin/bash

# Function to check if a port is in use, and kill the process if it is
check_and_kill_port() {
    local port=$1
    # Check if the port is in use using ss
    pid=$(ss -ltnp | grep ":$port" | awk -F'[=,]' '{print $3}')
    
    if [ -z "$pid" ]; then
        echo "Port $port is okay."
    else
        echo "Port $port is in use. Process ID: $pid"
        echo "Killing process with PID: $pid"
        kill -9 "$pid"  # Kill the process
        if [ $? -eq 0 ]; then
            echo "Process $pid killed successfully."
        else
            echo "Failed to kill process $pid."
        fi
    fi
}

# Function to close all xterm windows
close_xterm_windows() {
    echo "Closing all xterm windows..."
    pids=$(pgrep xterm)  # Get all xterm process IDs

    if [ -z "$pids" ]; then
        echo "No xterm windows are currently open."
    else
        for pid in $pids; do
            echo "Killing xterm process with PID: $pid"
            kill -9 "$pid"  # Kill each xterm process
            if [ $? -eq 0 ]; then
                echo "xterm process $pid killed successfully."
            else
                echo "Failed to kill xterm process $pid."
            fi
        done
    fi
}

# Check and kill processes using specific ports
check_and_kill_port 5000
check_and_kill_port 5001
check_and_kill_port 5002
check_and_kill_port 5003
check_and_kill_port 5004
check_and_kill_port 5005
check_and_kill_port 5006
check_and_kill_port 5007

# Close all xterm windows
close_xterm_windows
