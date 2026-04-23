#!/bin/bash

# Script to cleanup broken avatar references in database
# This will remove database references to avatar files that no longer exist

echo "🧹 Starting avatar cleanup process..."

# Check if server is running
SERVER_URL="http://localhost:8080"
if ! curl -s "$SERVER_URL/api/maintenance/cleanup-broken-avatars" > /dev/null; then
    echo "❌ Server is not running at $SERVER_URL"
    echo "Please start the Spring Boot application first:"
    echo "  ./mvnw spring-boot:run"
    exit 1
fi

echo "✅ Server is running, starting cleanup..."

# Call the cleanup endpoint
RESPONSE=$(curl -s -X POST "$SERVER_URL/api/maintenance/cleanup-broken-avatars" \
    -H "Content-Type: application/json")

echo "📊 Cleanup Results:"
echo "$RESPONSE" | jq '.'

echo "✅ Avatar cleanup completed!"
echo ""
echo "📝 What this script did:"
echo "  - Scanned all user profiles in database"
echo "  - Checked if avatar/cover files exist on disk"
echo "  - Removed database references to missing files"
echo "  - This prevents HTTP 500 errors when loading avatars"
echo ""
echo "🔄 Users with broken avatars will now see default avatars"
echo "   They can re-upload their avatars to fix this"