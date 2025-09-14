# setup-dynamodb-user.ps1
# Usage:
# 1) Start DynamoDB Local: java -Djava.library.path=.\DynamoDBLocal_lib -jar .\DynamoDBLocal.jar -sharedDb -dbPath .\local_db
# 2) From the folder containing this script run: .\setup-dynamodb-user.ps1
$endpoint = "http://localhost:8000"
$region = "ap-south-1"

Write-Host "Creating tables (if they don't already exist)..."

aws dynamodb create-table --table-name Students --attribute-definitions AttributeName=id,AttributeType=S --key-schema AttributeName=id,KeyType=HASH --billing-mode PAY_PER_REQUEST --endpoint-url $endpoint --region $region 2>$null
aws dynamodb create-table --table-name Courses --attribute-definitions AttributeName=courseId,AttributeType=S --key-schema AttributeName=courseId,KeyType=HASH --billing-mode PAY_PER_REQUEST --endpoint-url $endpoint --region $region 2>$null
aws dynamodb create-table --table-name StudentLogs --attribute-definitions AttributeName=logId,AttributeType=S --key-schema AttributeName=logId,KeyType=HASH --billing-mode PAY_PER_REQUEST --endpoint-url $endpoint --region $region 2>$null

Write-Host "Seeding courses from scripts_user/seed-courses.json..."

$seed = Get-Content -Raw -Path ".\scripts_user\seed-courses.json" | ConvertFrom-Json
foreach ($c in $seed) {
  $item = @"
{ "courseId": { "S": "$($c.courseId)" },
  "courseName": { "S": "$($c.courseName)" },
  "maxSeats": { "N": "$($c.maxSeats)" },
  "currentEnrolledCount": { "N": "$($c.currentEnrolledCount)" },
  "startDate": { "S": "$($c.startDate)" },
  "endDate": { "S": "$($c.endDate)" },
  "latestEnrollmentBy": { "S": "$($c.latestEnrollmentBy)" },
  "enrolledIds": { "L": [] },
  "waitlistIds": { "L": [] }
}
"@
  aws dynamodb put-item --table-name Courses --item $item --endpoint-url $endpoint --region $region
  Write-Host "Seeded $($c.courseId) - $($c.courseName)"
}

Write-Host "Done."
