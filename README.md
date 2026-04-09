# Hotel Management System (OSDL Project)

A desktop-based Hotel Management System developed for OSDL lab work using Java and JavaFX.

This project demonstrates how core software engineering ideas from OSDL can be blended into a practical application:
- Object-Oriented Design (encapsulation, inheritance, polymorphism)
- Layered/MVC-style structure (models, controllers, utilities)
- Persistent state management
- Modular build and dependency management with Maven
- UI + business logic integration in a maintainable way

## 1. Project Goal

The system helps manage hotel operations through two dashboards:
- Admin side: room inventory, booking management, analytics, maintenance workflow, guest lookup
- User side: room search/filter, booking, checkout, invoice export

It is designed as a full flow from room setup -> booking -> billing -> analytics.

## 2. Tech Stack

- Language: Java 17
- UI Framework: JavaFX 17 (Controls + FXML)
- Build Tool: Apache Maven
- JavaFX Runner Plugin: javafx-maven-plugin
- Data Storage: Local file-based persistence via Java Object Serialization (.dat files)
- Styling: CSS for JavaFX UI

### Stack Blend (Why this combination)

This project intentionally combines:
- Java OOP model classes for domain representation (Room, Booking, Guest, etc.)
- JavaFX FXML views for clean UI layout separation
- Controller classes for interaction and event handling
- Utility/service classes for reusable business rules (billing, seasonal pricing, persistence)
- Maven for consistent builds and dependency control

Result: a desktop app that is easy to run, easy to demonstrate in lab, and aligned with OSDL design principles.

## 3. Architecture Overview

### Packages

- com.hotel.models
  - Domain entities such as Room variants, Booking, Guest, Feedback, BillItem
- com.hotel.controllers
  - LoginController, AdminController, UserController
- com.hotel.utils
  - HotelManager (singleton state manager)
  - BillingService (invoice generation)
  - SeasonalPricing (dynamic pricing)
  - FileHandler (save/load data)

### View Layer

- FXML files define screens:
  - Login.fxml
  - AdminDashboard.fxml
  - UserDashboard.fxml

### Resource Layer

- Shared stylesheet:
  - styles.css

## 4. Key Features

- Role-based login screen (Admin/User)
- Add, edit, search, delete, and maintain rooms
- Availability filtering by type and max price
- Date-safe booking flow with overlap validation
- Seasonal/dynamic pricing multipliers
- Checkout and invoice generation
- Export invoice as text file
- Guest lookup and booking visibility
- Dashboard analytics (occupancy, booking count, revenue)
- Basic persistence of rooms/bookings/feedback using .dat files

## 5. OSDL Concepts Demonstrated

- Inheritance and specialization
  - Room base class with Standard/Deluxe/Luxury variants
- Encapsulation
  - Domain logic and state updates are contained in model/util classes
- Separation of concerns
  - UI layout, event handling, business logic, and persistence are split by package
- Reusability and modularity
  - Billing, pricing, and file handling are separated as utilities/services
- State management pattern
  - HotelManager singleton coordinates global state and persistence

## 6. Prerequisites

Install these before running:
- JDK 17
- Maven 3.8+

Verify:
- java -version
- mvn -version

## 7. How to Run

### Option A: Maven command

From project root:

mvn clean javafx:run

### Option B: Windows batch file

Double-click or run:

run.bat

## 8. Default Login Credentials

- Admin
  - Username: admin
  - Password: admin123

- User
  - Username: user
  - Password: user123

## 9. Data Files

The app stores serialized data in local files at project root:
- rooms_data.dat
- bookings_data.dat
- feedback_data.dat (created when feedback is saved)

These files preserve state between runs.

## 10. Suggested Demo Flow (For Lab Viva)

1. Login as Admin and add a few rooms.
2. Switch to User and book a room for valid dates.
3. Try an overlapping booking to show validation.
4. Checkout and show generated invoice.
5. Export invoice to file.
6. Return to Admin dashboard and explain occupancy/revenue changes.
7. Mention seasonal pricing effect on estimated booking amount.

## 11. Future Improvements

- Database integration (MySQL/PostgreSQL) instead of serialized files
- Password hashing and secure authentication
- Detailed reporting and charts export
- Unit tests for pricing, billing, and booking validation
- Packaging as native executable

---
Prepared for OSDL lab submission to demonstrate practical software design using Java + JavaFX + Maven.
