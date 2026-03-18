# Software Requirements Specification (SRS)

<!--
  HOW TO USE THIS TEMPLATE
  ========================
  1. Fill in Part A once for the whole project.
  2. Copy the Backlog block (## Backlog N … ---) for each feature and fill every section.
  3. Replace every [placeholder] with real content.
  4. Delete or hide this comment block before publishing.

  SECTION GUIDE
  -------------
  Part A — Project-Wide Specifications
    A1. Project Details    : product name and plain-language description.
    A2. Business Rules     : system-wide invariants that every feature must respect.

  Part B — Backlog Items (one block per feature)
    Business Value         : who benefits and why (the "As a / I want / so that" story).
    Business Requirements  : WHAT the system must do at a feature level.
    Functional Requirements: precise, individually testable statements of observable behaviour.
    Non-Functional Req.    : quality constraints (performance, security, usability, etc.).
    Data Flow              : how a request travels through the system's layers.
    Requirements Traceability: maps each requirement to a test case and test level.
    Acceptance Criterias   : pass/fail criteria, tagged by scenario type.

  REQUIREMENT TYPES (for Acceptance Criterias)
  ---------------------------------------------
  [HAPPY PATH]     — standard valid input, system behaves as expected.
  [EDGE CASE]      — boundary or unusual-but-valid input (empty list, zero, max value, etc.).
  [ERROR CASE]     — invalid or forbidden input that the system must reject gracefully.
  [NON-FUNCTIONAL] — quality attribute that must be measurably satisfied.

  TEST LEVELS (for Requirements Traceability)
  -------------------------------------------
  ST  — System / Unit Test  : single class or function in isolation (dependencies mocked).
  SIT — System Integration  : multiple components wired together (in-memory DB, no network).
  UAT — User Acceptance Test: full stack through a browser or HTTP client.
  NFT — Non-Functional Test : performance, load, or security measurement.

  WRITING GOOD REQUIREMENTS
  -------------------------
  • BR  : "The system must …" — focus on policy or business rule, not implementation.
  • FR  : "The system must … when …" — one testable behaviour per row, observable from outside.
  • NFR : "The system must … within/under/at least …" — always include a measurable threshold.
  • Acceptance Criteria: write the criterion as a verifiable sentence, not as a test step.
-->

---

## Part A — Project-Wide Specifications

<!--
  Fill in Part A once for the whole project.
  These sections apply to EVERY backlog item — do not duplicate them per feature.
-->

### A1. Project Details

- **Product Name:** [Your product name]
- **Product Description:** [A brief, plain-language description of what this product does and who it serves.]

---

### A2. Business Rules

<!--
  A Business Rule is a constraint the system must always obey, regardless of which feature
  is being used. These represent policies, regulations, or invariants (things always true).
  Example: "RU01 — An order must always contain at least one product."
  Tip: FR/NFR in Part B should reference Rule IDs from this table (e.g., "Related Rule ID: RU01").
-->

| Rule ID | Business Rule Statement                                                      |
|---------|------------------------------------------------------------------------------|
| RU01    | [Describe a rule the system must always enforce.]                            |
| RU02    | [Add more rules as needed.]                                                  |

---

## Part B — Backlog Item Template

<!--
  ONE BLOCK PER FEATURE.
  Copy from "## Backlog N" down to (and including) the closing "---" for each new feature.
  Increment N for every new backlog item.
-->

---

## Backlog 1: [Feature Name]

<!--
  Write 2–4 sentences describing WHAT this feature does from the user's perspective.
  Focus on observable behaviour — do not mention classes, methods, or database tables.
-->

**Description:**
[Describe the feature here in plain language.]

**Requirement Group:** [The category this feature belongs to, e.g., "Product Management", "Order Processing"]
**Priority:** High / Medium / Low — [Must Have / Should Have / Could Have]

---

### Backlog 1: Business Value (User Story)

<!--
  Format: "As a [role], I want [what], so that [why]."
  • [role]  — the type of user who benefits (e.g., "shop administrator", "customer").
  • [what]  — the capability they need.
  • [why]   — the tangible business benefit — NOT a technical reason.
-->

> As a **[role]**, I want **[what I want to do]**, so that **[the benefit I get]**.

---

### Backlog 1: Business Requirements

<!--
  One row per distinct thing the system MUST do for this backlog item.
  • BR Statement    : "The system must …" — policy-level, not implementation-level.
  • Related Rule ID : reference an RU ID from Part A if this BR is constrained by a business rule.
  • Considerations  : edge cases, assumptions, or constraints the developer must be aware of.
-->

| BR ID  | BR Statement                                   | Related Rule ID | Considerations |
|--------|------------------------------------------------|-----------------|----------------|
| BR1.01 | [What must the system do?]                     | RU01            | [Notes]        |
| BR1.02 | [Second requirement for this backlog, if any.] | –               | [Notes]        |

---

### Backlog 1: Functional Requirements

<!--
  One row per individually testable system behaviour.
  • FR Statement  : "The system must … when …" — observable from outside, not internal logic.
  • Dependent FR  : list FRs that must be satisfied before this one can be tested.
  • Related BR ID : the BR this FR implements.
  Tip: each row here should map to exactly one test case in the RTM below.
-->

| FR ID  | FR Group | FR Statement                                          | Dependent FR | Related BR ID |
|--------|----------|-------------------------------------------------------|--------------|---------------|
| FR1.01 | [Group]  | [Precise testable statement of system behaviour.]     | None         | BR1.01        |
| FR1.02 | [Group]  | [Another precise testable statement.]                 | FR1.01       | BR1.01        |
| FR1.03 | [Group]  | [Edge or error behaviour that must also be handled.]  | None         | BR1.02        |

---

### Backlog 1: Non-Functional Requirements

<!--
  Quality constraints this feature must satisfy.
  • NFR Attribute : Performance / Security / Usability / Reliability / Auditability / …
  • NFR Statement : must include a measurable threshold (e.g., "within 500 ms", "at least 99.9%").
  Tip: Performance NFRs → NFT column in RTM. Security/Reliability → ST or SIT column.
-->

| NFR ID  | NFR Attribute | NFR Statement                                                        | Related BR ID |
|---------|---------------|----------------------------------------------------------------------|---------------|
| NFR1.01 | [Attribute]   | [What quality constraint must this feature satisfy?]                 | BR1.01        |
| NFR1.02 | [Attribute]   | [E.g., "The page must load in under 2 seconds for up to 100 users."] | BR1.01        |

---

### Backlog 1: Data Flow

<!--
  Trace the journey of a request through the application layers for this feature.
  Use numbered steps. Stick to layer names (Presentation / Business Logic / Data Access)
  rather than class or method names — the document must stay language-agnostic.
  Example layers: Presentation Layer → Business Logic Layer → Data Access Layer → (response)
-->

1. [Step 1: user action or incoming request]
2. [Step 2: how does the Presentation Layer handle the request?]
3. [Step 3: what does the Business Logic Layer do?]
4. [Step 4: what does the Data Access Layer retrieve or persist?]
5. [Step 5: what is returned or displayed to the user?]

**Depends on:** [Backlog X — reason why this feature needs the other to exist first, or "None"]

---

### Backlog 1: Requirements Traceability

<!--
  Maps every requirement row to a test case and the test level(s) that cover it.
  • Test Case ID : assign sequential IDs (TC01, TC02, …) across all backlogs.
  • ST  : System / Unit Test  — isolated, mocks all dependencies.
  • SIT : System Integration  — components wired together, in-memory data store.
  • UAT : User Acceptance Test — full stack, browser or HTTP client.
  • NFT : Non-Functional Test  — performance, load, or security measurement.
  Mark the applicable column(s) with ✔ for each test case.
-->

| BR ID  | FR/NFR ID | Req Type       | Requirement Statement (brief)         | Test Case ID | ST  | SIT | UAT | NFT |
|--------|-----------|----------------|---------------------------------------|--------------|-----|-----|-----|-----|
| BR1.01 | FR1.01    | Functional     | [Copy FR statement summary here]      | TC01         | ✔   |     | ✔   |     |
| BR1.01 | FR1.02    | Functional     | [Copy FR statement summary here]      | TC02         | ✔   |     |     |     |
| BR1.01 | NFR1.01   | Non-Functional | [Copy NFR statement summary here]     | TC03         |     |     |     | ✔   |

---

### Backlog 1: Acceptance Criterias

<!--
  One bullet per scenario. Tag each with its type and reference the FR/NFR ID in parentheses.
  A criterion must be a complete, verifiable sentence — not a test step or implementation note.
  Types: [HAPPY PATH] | [EDGE CASE] | [ERROR CASE] | [NON-FUNCTIONAL]
-->

- [HAPPY PATH]     (FR1.01) ...
- [HAPPY PATH]     (FR1.02) ...
- [EDGE CASE]      (FR1.03) ...
- [ERROR CASE]     (FR1.03) ...
- [NON-FUNCTIONAL] (NFR1.01) ...

---

## Backlog 2: [Feature Name]

**Description:**
[Describe the feature here in plain language.]

**Requirement Group:** [Category]
**Priority:** High / Medium / Low — [Must Have / Should Have / Could Have]

---

### Backlog 2: Business Value (User Story)

> As a **[role]**, I want **[what I want to do]**, so that **[the benefit I get]**.

---

### Backlog 2: Business Requirements

| BR ID  | BR Statement | Related Rule ID | Considerations |
|--------|--------------|-----------------|----------------|
| BR2.01 | [Statement]  | –               | [Notes]        |

---

### Backlog 2: Functional Requirements

| FR ID  | FR Group | FR Statement | Dependent FR | Related BR ID |
|--------|----------|--------------|--------------|---------------|
| FR2.01 | [Group]  | [Statement]  | None         | BR2.01        |

---

### Backlog 2: Non-Functional Requirements

| NFR ID  | NFR Attribute | NFR Statement | Related BR ID |
|---------|---------------|---------------|---------------|
| NFR2.01 | [Attribute]   | [Statement]   | BR2.01        |

---

### Backlog 2: Data Flow

1. [Step 1]
2. [Step 2]
3. [Step 3]

**Depends on:** [Backlog X — reason, or "None"]

---

### Backlog 2: Requirements Traceability

| BR ID  | FR/NFR ID | Req Type   | Requirement Statement (brief) | Test Case ID | ST  | SIT | UAT | NFT |
|--------|-----------|------------|-------------------------------|--------------|-----|-----|-----|-----|
| BR2.01 | FR2.01    | Functional | [Summary]                     | TC04         | ✔   |     | ✔   |     |

---

### Backlog 2: Acceptance Criterias

- [HAPPY PATH] (FR2.01) ...
- [EDGE CASE]  (FR2.01) ...
- [ERROR CASE] (FR2.01) ...
