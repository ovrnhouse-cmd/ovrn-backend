# API Routes Testing

This document provides a list of available API routes for the WOLF-OVRN backend and example `curl` commands to test them.

**Base URL:** `http://localhost:8080`

---

## 🔐 Authentication
Note: This application uses OAuth2 and JWT stored in cookies.

### Get Current User
Returns the currently authenticated user details.
```bash
curl -X GET http://localhost:8080/api/auth/me \
  -H "Accept: application/json"
```

### Logout
Clears the authentication cookie.
```bash
curl -X POST http://localhost:8080/api/auth/logout \
  -H "Accept: application/json"
```

---

## 📦 Products

### List Products
Supports pagination, search, category filtering, and sorting.
```bash
# Basic list
curl -X GET "http://localhost:8080/api/products?page=0&limit=10"

# Search and filter
curl -X GET "http://localhost:8080/api/products?search=phone&category=electronics&isPremium=true&minPrice=1000&maxPrice=5000"

# Sorting (field,direction)
curl -X GET "http://localhost:8080/api/products?sort=price,desc"
```

### Get Product by Slug
```bash
curl -X GET http://localhost:8080/api/products/{slug}
```

### Create Product (Admin)
```bash
curl -X POST http://localhost:8080/api/products \
  -H "Content-Type: application/json" \
  -d '{
    "name": "New Product",
    "description": "Product Description",
    "sellingPrice": 89.99,
    "markedPrice": 99.99,
    "inStock": true,
    "categoryId": "f52a59bd-73bc-4f0f-8635-e439f3134a7a",
    "images": ["image_url_1", "image_url_2"]
  }'
```

### Update Product (Admin)
```bash
curl -X PATCH http://localhost:8080/api/products/{uuid} \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Updated Name",
    "sellingPrice": 89.99,
    "markedPrice": 109.99,
    "inStock": false
  }'
```

### Delete Product (Admin)
```bash
curl -X DELETE http://localhost:8080/api/products/{uuid}
```

---

## 🏷️ Categories

### List All Categories
```bash
curl -X GET http://localhost:8080/api/categories
```

---

## 🛒 Orders

### List All Orders (Admin)
```bash
curl -X GET "http://localhost:8080/api/orders?page=0&limit=10"
```

### List My Orders
```bash
curl -X GET http://localhost:8080/api/orders/me
```

### Create Order (Step 1)
Creates an application order and calculates the total amount securely on the backend.
```bash
curl -X POST http://localhost:8080/api/orders \
  -H "Content-Type: application/json" \
  -d '{
    "items": [
      {
        "productId": "{uuid}",
        "quantity": 2
      }
    ],
    "shippingAddress": { "address": "123 Main St, City, Country" }
  }'
```

---

## 💳 Payments

### Create Payment (Step 2)
Initiates a payment for an existing order. **Note: Do NOT pass the amount.** The backend automatically fetches the exact amount from the previously created Order. You can call this endpoint multiple times if a user cancels and retries.
```bash
curl -X POST http://localhost:8080/api/payments/create-order \
  -H "Content-Type: application/json" \
  -d '{
    "orderId": "{order_uuid}"
  }'
```

### Verify Payment (Step 3)
Verifies the Razorpay signature after the user successfully completes the payment on the frontend.
```bash
curl -X POST http://localhost:8080/api/payments/verify \
  -H "Content-Type: application/json" \
  -d '{
    "razorpayOrderId": "order_xyz123",
    "razorpayPaymentId": "pay_abc456",
    "razorpaySignature": "signature_hash_here"
  }'
```

---

## 👥 Users

### List All Users (Admin)
```bash
curl -X GET http://localhost:8080/api/users
```

### Update User Role (Admin)
```bash
curl -X PATCH http://localhost:8080/api/users/{uuid}/role \
  -H "Content-Type: application/json" \
  -d '{
    "role": "ADMIN"
  }'
```
