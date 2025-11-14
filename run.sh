#!/bin/bash

# Set environment variables
export BOT_TOKEN="f9LHodD0cOIfLEPKmsl3spfDctcqcC4Ckb3lBiVUCQA-T0ZARg1RjqodAyIIXX6jcStdg1cEr4vtFzH0H838"
export DB_URL="jdbc:postgresql://localhost:5432/bot"
export DB_USER="user"
export DB_PASSWORD="123"

# Start the application
./gradlew :app:run