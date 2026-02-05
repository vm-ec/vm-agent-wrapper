# API Keys Setup Guide

## AWS Secrets Manager Setup

Your API keys are stored in AWS Secrets Manager with the following details:
- **Secret Name**: `srivani-keys`
- **Region**: `us-east-1`
- **Secret ARN**: `arn:aws:secretsmanager:us-east-1:916338043583:secret:srivani-keys-wrbXxA`

### Secret Format
The secret should be stored as JSON with the following structure:
```json
{
  "GEMINI_API_KEY": "your_gemini_key",
  "OPENAI_API_KEY": "your_openai_key"
}
```

## Running the Application

### Local Testing
Keys are hardcoded in `application-local.yaml` (not committed to GitHub):
```bash
mvn spring-boot:run
```

### Production/GitHub (with AWS Secrets Manager)
Run with prod profile to enable AWS Secrets Manager:
```bash
mvn spring-boot:run -Dspring.profiles.active=prod
```

## AWS Credentials Configuration

For production, ensure AWS credentials are configured:
- Use AWS CLI: `aws configure`
- Or set environment variables: `AWS_ACCESS_KEY_ID`, `AWS_SECRET_ACCESS_KEY`
- Or use IAM roles (recommended for EC2/ECS)

## Important Notes
- `application.yaml` has placeholder keys (safe for GitHub)
- `application-local.yaml` has real keys (DO NOT commit to GitHub)
- `application-prod.yaml` enables AWS Secrets Manager
- Use default profile for local, `prod` profile for production
