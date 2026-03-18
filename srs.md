# PetrikShop Software Requirements Specification (SRS)

---

## Part A — Project-Wide Specifications

### A1. Project Details

- **Product Name:** PetrikShop
- **Product Description:** PetrikShop is an Indonesian local e-commerce application that lets shop administrators manage daily-life goods (products) and customer orders through a web-based interface.

---

### A2. Business Rules

| Rule ID | Business Rule Statement                                                                      |
|---------|----------------------------------------------------------------------------------------------|
| RU01    | Every product must have a unique, system-generated ID (UUID).                                |
| RU02    | An order must always contain at least one product; empty orders are not allowed.             |
| RU03    | An order's status must always be one of: WAITING_PAYMENT, SUCCESS, FAILED, CANCELLED.       |
| RU04    | Duplicate order IDs are not allowed; each order must have a globally unique identifier.      |

---

## Part B — Backlog Items

---

## Backlog 1: List All Products

**Description:**
This feature displays all products currently registered in the shop on a single page. The shop administrator can navigate to a dedicated URL and immediately see every product's name, quantity, and ID. This is the foundation for all other product management workflows.

**Requirement Group:** Product Management
**Priority:** High — Must Have

---

### Backlog 1: Business Value (User Story)

> As a **shop administrator**, I want **to see all products listed on a single page**, so that **I can quickly understand the current inventory and decide what actions to take next**.

---

### Backlog 1: Business Requirements

| BR ID  | BR Statement                                                                     | Related Rule ID | Considerations                                           |
|--------|----------------------------------------------------------------------------------|-----------------|----------------------------------------------------------|
| BR1.01 | The system must display all products stored in the repository on a list page.    | RU01            | Page must handle zero products gracefully (empty state). |

---

### Backlog 1: Functional Requirements

| FR ID  | FR Group           | FR Statement                                                                                                              | Dependent FR | Related BR ID |
|--------|--------------------|---------------------------------------------------------------------------------------------------------------------------|--------------|---------------|
| FR1.01 | Product Management | The system must display the product list page and return a successful response (HTTP 200) when accessed.                  | None         | BR1.01        |
| FR1.02 | Product Management | The product list page must display all products currently stored in the system.                                           | FR1.01       | BR1.01        |
| FR1.03 | Product Management | When no products are stored in the system, the product list page must display an empty state without error.               | FR1.01       | BR1.01        |
| FR1.04 | Product Management | The system must be capable of displaying the complete product list repeatedly without data loss or inconsistency.         | None         | BR1.01        |

---

### Backlog 1: Non-Functional Requirements

| NFR ID  | NFR Attribute | NFR Statement                                                                               | Related BR ID |
|---------|--------------|---------------------------------------------------------------------------------------------|--------------|
| NFR1.01 | Performance  | The product list page must render within 500 ms for a catalogue of up to 1,000 products.   | BR1.01        |
| NFR1.02 | Usability    | Each product entry must clearly display its unique identifier, name, and available quantity. | BR1.01       |

---

### Backlog 1: Data Flow

1. The administrator requests the product list page via the web interface.
2. The Presentation Layer receives the request and delegates to the Business Logic Layer.
3. The Business Logic Layer requests all product records from the Data Access Layer.
4. The Data Access Layer retrieves all stored product records and returns them to the Business Logic Layer.
5. The Business Logic Layer assembles the retrieved records into an ordered list and returns it to the Presentation Layer.
6. The Presentation Layer prepares the product list data for display.
7. The web interface renders the product list page, displaying each product's details.

**Depends on:** None

---

### Backlog 1: Requirements Traceability

| BR ID  | FR/NFR ID | Req Type        | Requirement Statement (brief)                                         | Test Case ID | ST | SIT | UAT | NFT |
|--------|-----------|-----------------|-----------------------------------------------------------------------|--------------|----|-----|-----|-----|
| BR1.01 | FR1.01    | Functional      | Product list page is accessible and returns a successful response     | TC01         | ✔  |     | ✔   |     |
| BR1.01 | FR1.02    | Functional      | All stored products are displayed on the product list page            | TC02         | ✔  |     |     |     |
| BR1.01 | FR1.03    | Functional      | Empty product store shows empty list without error                    | TC03         | ✔  |     |     |     |
| BR1.01 | FR1.04    | Functional      | Product list supports repeated display without data loss              | TC04         | ✔  |     |     |     |
| BR1.01 | NFR1.01   | Non-Functional  | Page renders within 500 ms for ≤1,000 products                        | TC05         |    |     |     | ✔   |
| BR1.01 | NFR1.02   | Non-Functional  | Each product entry displays its unique identifier, name, and quantity  | TC24         |    |     | ✔   |     |

---

### Backlog 1: Acceptance Criterias

- [HAPPY PATH]     (FR1.01, FR1.02) When the system contains one or more products, the product list page must be accessible and display all stored products.
- [HAPPY PATH]     (FR1.02) Each displayed product must correctly show its unique identifier, name, and available quantity as stored in the system.
- [EDGE CASE]      (FR1.03) When no products exist in the system, the product list page must render without error, displaying an empty state.
- [EDGE CASE]      (FR1.04) The product list must remain consistently accessible and complete across multiple views without data loss.
- [NON-FUNCTIONAL] (NFR1.01) The list page must render within 500 ms under normal load conditions.
- [NON-FUNCTIONAL] (NFR1.02) Each displayed product entry must clearly show its unique identifier, name, and available quantity.

---

## Backlog 2: Create a Product

**Description:**
This feature allows a shop administrator to register a new product in the inventory via a web form. The flow has two steps: a blank form page is displayed, and then the completed form is submitted to save the product, after which the administrator is redirected to the refreshed product list.

**Requirement Group:** Product Management
**Priority:** High — Must Have

---

### Backlog 2: Business Value (User Story)

> As a **shop administrator**, I want **to register a new product through a web form**, so that **customers can see and order newly available items**.

---

### Backlog 2: Business Requirements

| BR ID  | BR Statement                                                                                  | Related Rule ID | Considerations                                                     |
|--------|-----------------------------------------------------------------------------------------------|-----------------|--------------------------------------------------------------------|
| BR2.01 | The system must provide a form where an administrator can enter a product name and quantity.   | –               | The form must capture all required product data fields (name and quantity). |
| BR2.02 | On form submission, the system must generate a unique product ID and persist the new product. | RU01            | ID must be a UUID generated server-side, not supplied by the user. |

---

### Backlog 2: Functional Requirements

| FR ID  | FR Group           | FR Statement                                                                                                                                                        | Dependent FR | Related BR ID |
|--------|--------------------|---------------------------------------------------------------------------------------------------------------------------------------------------------------------|--------------|---------------|
| FR2.01 | Product Management | The system must display the product creation form page and return a successful response (HTTP 200) when accessed.                                                   | None         | BR2.01        |
| FR2.02 | Product Management | The product creation form must be presented with all input fields empty and ready for user input.                                                                   | FR2.01       | BR2.01        |
| FR2.03 | Product Management | On valid form submission, the system must save the new product and redirect the user to the product list page.                                                      | FR2.01       | BR2.01        |
| FR2.04 | Product Management | The system must automatically assign a unique identifier (UUID) to every new product at creation time, regardless of whether one was provided via the form.         | FR2.03       | BR2.02        |
| FR2.05 | Product Management | A product with a quantity of zero must be accepted and saved by the system (zero stock is a valid initial state).                                                   | FR2.03       | BR2.01        |
| FR2.06 | Product Management | A product submitted with an empty product name must be rejected and not saved.                                                                                     | FR2.03       | BR2.01        |

---

### Backlog 2: Non-Functional Requirements

| NFR ID  | NFR Attribute | NFR Statement                                                                                     | Related BR ID |
|---------|---------------|---------------------------------------------------------------------------------------------------|---------------|
| NFR2.01 | Security      | The create-product endpoint must not be accessible to unauthenticated users (when auth is added). | BR2.01        |
| NFR2.02 | Usability     | The create form must clearly label all input fields and provide a visible submit button.           | BR2.01        |

---

### Backlog 2: Data Flow

1. The administrator navigates to the product creation form page via the web interface.
2. The Presentation Layer receives the request and prepares an empty product form for display.
3. The web interface renders the product creation form with all fields empty and ready for input.
4. The administrator fills in the product name and quantity, then submits the form.
5. The Presentation Layer receives the submitted form data and delegates to the Business Logic Layer.
6. The Business Logic Layer validates and processes the new product data.
7. The Business Logic Layer generates a unique identifier (UUID) and instructs the Data Access Layer to persist the new product.
8. The Presentation Layer redirects the user to the updated product list page.

**Depends on:** Backlog 1 — the post-create redirect leads to the list page.

---

### Backlog 2: Requirements Traceability

| BR ID  | FR/NFR ID | Req Type        | Requirement Statement (brief)                                               | Test Case ID | ST | SIT | UAT | NFT |
|--------|-----------|-----------------|-----------------------------------------------------------------------------|--------------|-----|-----|-----|-----|
| BR2.01 | FR2.01    | Functional      | Product creation form page is accessible and returns a successful response  | TC06         | ✔  |     | ✔   |     |
| BR2.01 | FR2.02    | Functional      | Product creation form is presented with all fields empty                    | TC07         | ✔  |     |     |     |
| BR2.01 | FR2.03    | Functional      | Valid form submission saves the product and redirects to the product list   | TC08         | ✔  |     | ✔   |     |
| BR2.02 | FR2.04    | Functional      | System assigns a unique UUID to every new product at creation time          | TC09         | ✔  |     |     |     |
| BR2.01 | FR2.05    | Functional      | Product with zero quantity is accepted and saved                            | TC10         | ✔  |     |     |     |
| BR2.01 | FR2.06    | Functional      | Product with empty name is rejected and not saved                           | TC11         | ✔  |     |     |     |
| BR2.01 | NFR2.01   | Non-Functional  | Create-product endpoint is inaccessible to unauthenticated users            | TC25         | ✔  |     |     |     |
| BR2.01 | NFR2.02   | Non-Functional  | Create form clearly labels all fields and has a visible submit button       | TC26         |    |     | ✔   |     |

---

### Backlog 2: Acceptance Criterias

- [HAPPY PATH] (FR2.01, FR2.02) Navigating to the product creation page must display the form successfully with all fields empty and ready for input.
- [HAPPY PATH] (FR2.03, FR2.04) After submitting a valid product creation form, the product must be saved with a system-generated UUID and the user must be redirected to the product list.
- [EDGE CASE]  (FR2.05) A product with a quantity of zero must be accepted and saved without error.
- [ERROR CASE]     (FR2.06) A product with an empty name must be rejected and not saved.
- [NON-FUNCTIONAL] (NFR2.01) When authentication is in place, the product creation endpoint must return an access rejection to any unauthenticated request.
- [NON-FUNCTIONAL] (NFR2.02) The product creation form must clearly label all input fields and provide a visible submit button.

---

## Backlog 3: Create an Order

**Description:**
This feature allows a shop administrator to record a new customer order. An order groups one or more products together, records the customer name (author) and a Unix timestamp, and starts with the status WAITING_PAYMENT. The system must prevent duplicate order IDs.

**Requirement Group:** Order Processing
**Priority:** High — Must Have

---

### Backlog 3: Business Value (User Story)

> As a **shop administrator**, I want **to create a new order linked to one or more products**, so that **customer purchases are formally recorded and can be tracked through the fulfilment process**.

---

### Backlog 3: Business Requirements

| BR ID  | BR Statement                                                                                        | Related Rule ID | Considerations                                                                          |
|--------|-----------------------------------------------------------------------------------------------------|-----------------|-----------------------------------------------------------------------------------------|
| BR3.01 | The system must allow creating an order with an ID, a list of products, a timestamp, and an author. | RU02, RU03      | The order's initial status must always be set to WAITING_PAYMENT.                       |
| BR3.02 | The system must reject creation of an order whose ID already exists in the repository.              | RU04            | The system must communicate the conflict gracefully without raising an unhandled error. |

---

### Backlog 3: Functional Requirements

| FR ID  | FR Group         | FR Statement                                                                                                                          | Dependent FR | Related BR ID |
|--------|------------------|---------------------------------------------------------------------------------------------------------------------------------------|--------------|---------------|
| FR3.01 | Order Processing | The system must successfully create and persist a new order when provided with a unique order ID and at least one associated product. | None         | BR3.01        |
| FR3.02 | Order Processing | The status of a newly created order must be WAITING_PAYMENT immediately after creation.                                              | FR3.01       | BR3.01        |
| FR3.03 | Order Processing | The system must indicate a conflict (without raising an unhandled error) when attempting to create an order whose ID already exists.  | FR3.01       | BR3.02        |
| FR3.04 | Order Processing | The system must reject an order creation attempt if no products are associated with the order.                                       | None         | BR3.01        |
| FR3.05 | Order Processing | The system must reject any attempt to assign an unrecognized status to an order.                                                     | None         | BR3.01        |

---

### Backlog 3: Non-Functional Requirements

| NFR ID  | NFR Attribute | NFR Statement                                                                               | Related BR ID |
|---------|---------------|---------------------------------------------------------------------------------------------|---------------|
| NFR3.01 | Reliability   | Order creation must be atomic: either the order is fully saved or it is not saved at all.   | BR3.01        |
| NFR3.02 | Auditability  | The creation timestamp must record the Unix epoch timestamp at the moment of order creation. | BR3.01       |

---

### Backlog 3: Data Flow

1. A new order containing a unique ID, associated products, a creation timestamp, and an author is submitted to the system.
2. The Business Logic Layer queries the Data Access Layer to verify that no order with the same ID already exists.
3. If no duplicate is found, the Data Access Layer persists the order, and the saved order is returned.
4. If a duplicate ID is found, the system returns a conflict indicator without raising an error.
5. Every newly created order is automatically assigned the WAITING_PAYMENT status by the system.

**Depends on:** Backlog 2 — orders must reference products that exist in the system.

---

### Backlog 3: Requirements Traceability

| BR ID  | FR/NFR ID | Req Type        | Requirement Statement (brief)                                                   | Test Case ID | ST | SIT | UAT | NFT |
|--------|-----------|-----------------|---------------------------------------------------------------------------------|--------------|-----|-----|-----|-----|
| BR3.01 | FR3.01    | Functional      | System successfully creates and persists an order with a unique ID and products | TC12         | ✔  |     | ✔   |     |
| BR3.01 | FR3.02    | Functional      | Newly created order's status is WAITING_PAYMENT                                 | TC13         | ✔  |     |     |     |
| BR3.02 | FR3.03    | Functional      | System returns a conflict indicator for duplicate order IDs                     | TC14         | ✔  |     |     |     |
| BR3.01 | FR3.04    | Functional      | System rejects order creation with no associated products                       | TC15         | ✔  |     |     |     |
| BR3.01 | FR3.05    | Functional      | System rejects orders with unrecognized status values                           | TC16         | ✔  |     |     |     |
| BR3.01 | NFR3.01   | Non-Functional  | Order creation is atomic                                                        | TC17         |    | ✔   |     | ✔   |
| BR3.01 | NFR3.02   | Non-Functional  | Creation timestamp records Unix epoch value at the moment of order creation     | TC27         | ✔  |     |     |     |

---

### Backlog 3: Acceptance Criterias

- [HAPPY PATH]     (FR3.01, FR3.02) When a valid order with a unique ID and at least one product is submitted, the system must persist the order and return it with the status WAITING_PAYMENT.
- [ERROR CASE]     (FR3.03) When an order with a duplicate ID is submitted, the system must indicate a conflict without raising an unhandled error.
- [ERROR CASE]     (FR3.04) The system must reject an order creation attempt containing no associated products.
- [ERROR CASE]     (FR3.05) The system must reject any order status value that is not one of: WAITING_PAYMENT, SUCCESS, FAILED, CANCELLED.
- [NON-FUNCTIONAL] (NFR3.01) Order creation must be transactionally consistent — a partial save must not occur.
- [NON-FUNCTIONAL] (NFR3.02) The order's creation timestamp must be recorded as the Unix epoch value at the exact moment the order is created.

---

## Backlog 4: Update Order Status

**Description:**
This feature allows a shop administrator to change the status of an existing order (e.g., from WAITING_PAYMENT to SUCCESS or CANCELLED). Only the four predefined status values are accepted. Attempting to update a non-existent order must result in a clear error response. All other order fields must remain unchanged.

**Requirement Group:** Order Processing
**Priority:** High — Must Have

---

### Backlog 4: Business Value (User Story)

> As a **shop administrator**, I want **to update the fulfilment status of an existing order**, so that **the order history accurately reflects the real-world state of each transaction**.

---

### Backlog 4: Business Requirements

| BR ID  | BR Statement                                                                                                | Related Rule ID | Considerations                                                                                  |
|--------|-------------------------------------------------------------------------------------------------------------|-----------------|-------------------------------------------------------------------------------------------------|
| BR4.01 | The system must allow updating the status of an existing order to any of the defined valid status values.   | RU03            | Only the status field is modified; all other order data must remain unchanged after the update. |
| BR4.02 | The system must respond with a clear error when an update is attempted on an order ID that does not exist.  | –               | The system must clearly report the error rather than silently ignoring the missing order.        |

---

### Backlog 4: Functional Requirements

| FR ID  | FR Group         | FR Statement                                                                                                                              | Dependent FR | Related BR ID |
|--------|------------------|-------------------------------------------------------------------------------------------------------------------------------------------|--------------|---------------|
| FR4.01 | Order Processing | The system must successfully update an order's status and return the updated order when given a valid order ID and a valid status value.   | None         | BR4.01        |
| FR4.02 | Order Processing | All four valid status values (WAITING_PAYMENT, SUCCESS, FAILED, CANCELLED) must be accepted by the system without error.                  | FR4.01       | BR4.01        |
| FR4.03 | Order Processing | After a successful status update, all other order fields (order ID, associated products, creation timestamp, and author) must remain unchanged. | FR4.01  | BR4.01        |
| FR4.04 | Order Processing | The system must return a clear error when a status update is attempted on an order ID that does not exist.                                | None         | BR4.02        |
| FR4.05 | Order Processing | The system must reject any attempt to set an order status to an unrecognized value.                                                       | None         | BR4.01        |

---

### Backlog 4: Non-Functional Requirements

| NFR ID  | NFR Attribute | NFR Statement                                                                                              | Related BR ID |
|---------|---------------|------------------------------------------------------------------------------------------------------------|---------------|
| NFR4.01 | Auditability  | Every status change must preserve the original creation timestamp; it must never be overwritten or reset.  | BR4.01        |
| NFR4.02 | Reliability   | The status update must be atomic: the old status must not be lost if an error occurs mid-update.           | BR4.01        |

---

### Backlog 4: Data Flow

1. The administrator submits a status update request with the target order ID and the new status value.
2. The Business Logic Layer queries the Data Access Layer to locate the order with the given ID.
3. If the order is found, the Business Logic Layer applies the new status while preserving all other order fields.
4. The system validates the new status value; an unrecognized status is rejected before any change is persisted.
5. The Data Access Layer persists the updated order, which is then returned to the caller.
6. If no order with the given ID exists, the system returns a clear error indicating the order was not found.

**Depends on:** Backlog 3 — an order must exist before its status can be changed.

---

### Backlog 4: Requirements Traceability

| BR ID  | FR/NFR ID | Req Type        | Requirement Statement (brief)                                                   | Test Case ID | ST | SIT | UAT | NFT |
|--------|-----------|-----------------|---------------------------------------------------------------------------------|--------------|-----|-----|-----|-----|
| BR4.01 | FR4.01    | Functional      | System updates an order's status and returns the updated order for valid inputs | TC18         | ✔  |     | ✔   |     |
| BR4.01 | FR4.02    | Functional      | All four valid status values are accepted without error                         | TC19         | ✔  |     |     |     |
| BR4.01 | FR4.03    | Functional      | Order ID, products, timestamp, and author are unchanged after status update     | TC20         | ✔  |     |     |     |
| BR4.02 | FR4.04    | Functional      | System returns a clear error for status update on a non-existent order ID       | TC21         | ✔  |     |     |     |
| BR4.01 | FR4.05    | Functional      | System rejects invalid status values during status update                       | TC22         | ✔  |     |     |     |
| BR4.01 | NFR4.01   | Non-Functional  | Order creation timestamp is unchanged after status update                       | TC23         | ✔  |     |     |     |
| BR4.01 | NFR4.02   | Non-Functional  | Status update is atomic; old status is not lost if an error occurs mid-update   | TC28         |    | ✔   |     | ✔   |

---

### Backlog 4: Acceptance Criterias

- [HAPPY PATH]     (FR4.01, FR4.02) For each of the four valid status values, the system must accept the update and return the order with the new status correctly applied.
- [HAPPY PATH]     (FR4.03) After a successful status update, all order fields except status must be unchanged.
- [ERROR CASE]     (FR4.04) When a status update is attempted for a non-existent order ID, the system must return a clear error response.
- [ERROR CASE]     (FR4.05) The system must reject any status value not in the set [WAITING_PAYMENT, SUCCESS, FAILED, CANCELLED] (e.g., "DELIVERED" or an empty value).
- [NON-FUNCTIONAL] (NFR4.01) The creation timestamp must be preserved exactly and must not be overwritten or reset during a status update.
- [NON-FUNCTIONAL] (NFR4.02) A status update must be transactionally consistent — the order's previous status must not be lost if an error occurs mid-update.
