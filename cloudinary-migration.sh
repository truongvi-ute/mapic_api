#!/bin/bash

# Cloudinary Migration Script
# Usage: ./cloudinary-migration.sh <base-url>
# Example: ./cloudinary-migration.sh https://mapic-backend-ute.onrender.com

if [ -z "$1" ]; then
    echo "Usage: $0 <base-url>"
    echo "Example: $0 https://mapic-backend-ute.onrender.com"
    exit 1
fi

BASE_URL="$1"
API_BASE="$BASE_URL/api/admin/migration"

echo "🔍 Cloudinary Migration Tool"
echo "Base URL: $BASE_URL"
echo "=================================="

# Function to make API call and show result
make_api_call() {
    local endpoint="$1"
    local method="${2:-GET}"
    local description="$3"
    
    echo ""
    echo "📡 $description"
    echo "Endpoint: $method $endpoint"
    echo "----------------------------------"
    
    if [ "$method" = "GET" ]; then
        curl -s "$endpoint" | jq '.' 2>/dev/null || curl -s "$endpoint"
    else
        curl -s -X "$method" "$endpoint" | jq '.' 2>/dev/null || curl -s -X "$method" "$endpoint"
    fi
    
    echo ""
}

# Step 1: Check storage service
make_api_call "$API_BASE/check-storage-service" "GET" "Checking active storage service"

# Step 2: Check Cloudinary configuration
make_api_call "$API_BASE/check-cloudinary-config" "GET" "Checking Cloudinary configuration"

# Step 3: Check local files in database
make_api_call "$API_BASE/check-local-files" "GET" "Checking local file references in database"

# Step 4: Ask user if they want to proceed with migration
echo ""
echo "🤔 Do you want to proceed with URL migration?"
echo "This will update all local file references to Cloudinary URLs."
echo ""
read -p "Continue? (y/N): " -n 1 -r
echo ""

if [[ $REPLY =~ ^[Yy]$ ]]; then
    # Step 5: Dry run first
    make_api_call "$API_BASE/update-urls-to-cloudinary?dryRun=true" "POST" "Performing dry run migration"
    
    echo ""
    echo "🤔 Dry run completed. Do you want to execute the actual migration?"
    read -p "Execute migration? (y/N): " -n 1 -r
    echo ""
    
    if [[ $REPLY =~ ^[Yy]$ ]]; then
        # Step 6: Actual migration
        make_api_call "$API_BASE/update-urls-to-cloudinary?dryRun=false" "POST" "Executing actual migration"
        
        # Step 7: Verify migration
        make_api_call "$API_BASE/check-local-files" "GET" "Verifying migration results"
        
        echo ""
        echo "✅ Migration completed!"
        echo "Please test your application to ensure images are displaying correctly."
    else
        echo "❌ Migration cancelled."
    fi
else
    echo "❌ Migration cancelled."
fi

echo ""
echo "🏁 Script completed."