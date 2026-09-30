# Military Asset Management System

## Purpose of the Project
A comprehensive system for tracking and managing military assets efficiently.

## Architecture & Technology Stack
* **Frontend:** React + Vite
* **Backend:** Spring Boot
* **Database:** PostgreSQL
* **Authentication:** JWT (JSON Web Tokens)
* **Authorization:** Role-Based Access Control (RBAC)

## RBAC Roles
* **Admin**
* **Base Commander**
* **Logistics Officer**

## Deployment Configuration
This project is configured for cloud production deployment using environment variables.

### Architecture Overview
* **Frontend:** Deployed to Vercel
* **Backend:** Deployed to Render
* **Database:** Render PostgreSQL

### Environment Variables
For deployment, set the corresponding environment variables in your hosting platforms. Refer to `.env.example` in both `frontend` and `backend` directories.

## Local Setup

### Database Setup
1. Install PostgreSQL and create a database named `military_asset_management`.
2. Apply the schema found in `database/schema.sql` to initialize the tables.

### Backend Setup
1. Navigate to the `backend` directory.
2. Copy `.env.example` to `.env` and fill in your local database credentials.
3. Start the application: `./mvnw spring-boot:run`
4. The server will start on port 8081.

### Frontend Setup
1. Navigate to the `frontend` directory.
2. Copy `.env.example` to `.env.local` if you need to override the API URL.
3. Install dependencies: `npm install`
4. Start the Vite development server: `npm run dev`
5. Access the application at `http://localhost:5173`.

## Demo Credentials
Use these credentials for testing the system locally or during the demo:

* **Admin**: `admin@militaryasset.com` / `Admin@123`
* **Base Commander**: `commander@militaryasset.com` / `Commander@123`
* **Logistics Officer**: `logistics@militaryasset.com` / `Logistics@123`

## API Information
* **Authentication:** `POST /api/auth/login`, `POST /api/auth/signup`
* **Endpoints:** Require Bearer token (JWT) in the `Authorization` header.
* **Roles:** Role-based access is strictly enforced for all endpoints.

## Deployment Notes
1. **Frontend**: Deploy `frontend/` to Vercel. Set `VITE_API_URL` to your Render backend URL.
2. **Backend**: Deploy `backend/` to Render. Provide all environment variables from `.env.example`.
3. **Database**: Use a managed PostgreSQL instance and provide the connection details to the backend. Ensure `DEMO_DATA_ENABLED` is appropriately managed (e.g., set to `false` for real production).
