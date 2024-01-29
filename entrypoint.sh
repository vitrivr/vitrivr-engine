#!/bin/bash

# Define the log file
LOG_FILE="/tmp/vitrivr_engine.log"

# Start the vitrivr-engine-server in a screen session and enable logging
screen -L -Logfile $LOG_FILE -dmS vitrivr_engine ./vitrivr-engine-server /vitrivr-engine-config/config.json

# Wait a bit to ensure the screen session starts properly
sleep 2

# Tail the log file to stdout
tail -f $LOG_FILE
