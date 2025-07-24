# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

HuginProject is a modern Android POS (Point of Sale) application built with Kotlin and Jetpack Compose. The app manages product sales, receipts, and automatic reporting to a configured server.

## Architecture

- **Single Activity Architecture** with MainActivity handling the main UI
- **Jetpack Compose** for UI with Material Design 3
- **Room Database** for local data persistence
- **MVVM-like pattern** with Compose state management
- **Automatic background reporting** via HTTP to configurable server

## Key Technologies

- Kotlin with Jetpack Compose
- Room database (SQLite)
- Coroutines for async operations
- Material Design 3
- HTTP client for server communication

## Build Commands

```bash
# Build the project
./gradlew build

# Build and install debug APK
./gradlew installDebug

# Run tests
./gradlew test

# Clean build
./gradlew clean
```

## Database Schema

### Products Table
- `barcode` (Primary Key): Product identifier
- `name`: Product display name  
- `price`: Product price
- `vatRate`: VAT rate (0, 1, 10, or 20)

### Receipts Table
- `id` (Auto-generated): Receipt ID
- `timestamp`: Sale timestamp
- `totalAmount`: Total sale amount
- `vatAmount`: Total VAT amount
- `paymentMethod`: Payment type (CASH, CREDIT, COUPON)
- `items`: JSON array of purchased items

## Core Components

### Database Layer (`database/`)
- `AppDatabase`: Room database configuration
- `ProductDao` & `ReceiptDao`: Data access objects
- `DatabaseHelper`: Sample data initialization

### Models (`models/`)
- `Product`: Product entity with barcode, name, price, VAT
- `Receipt`: Receipt entity with items and payment info
- `BasketItem`: Shopping basket item representation

### UI Layer (`ui/`)
- Compose-based UI components
- State management for shopping basket
- VAT calculation and display logic

## Business Logic

### VAT Rates
- Supports multiple VAT rates: 0%, 1%, 10%, 20%
- Automatic VAT calculation on all transactions

### Payment Methods
- Cash payments
- Credit card payments  
- Coupon/voucher payments

### Reporting System
- Automatic hourly sales reports generated as XML
- HTTP transmission to configurable server endpoint
- Server URL configured at: `http://192.168.1.100:8080/api/reports`

## Development Notes

### Database Initialization
- Sample products automatically loaded on first run
- Products include various VAT rates for testing

### Server Configuration
- Report endpoint can be modified in the HTTP client configuration
- XML format includes timestamp, total amounts, and itemized details

### Testing
- Sample data available for development and testing
- Multiple product categories with different VAT rates