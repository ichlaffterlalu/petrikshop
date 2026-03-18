---
mode: 'agent'
tools: ['codebase', 'editFiles', 'runCommands', 'todo']
description: >
  Generates a comprehensive, F.I.R.S.T.-compliant test suite derived from the
  project's SRS document (srs.md).  Tests are written against the INTENDED
  behaviour as defined by the requirements — not against the existing code —
  making this the correct tool for Test-Driven Development.  Covers unit,
  integration, and functional/E2E tests with 100% line and branch coverage
  targets.
---

# SRS-Driven Test Suite Generator

> **Source of truth:** `srs.md` — the project's Software Requirements Specification.
> **Do NOT read or reference existing production code before writing tests.** Tests must express what the system *should* do, not what it *currently* does.

---

## Your Task

1. **Read `srs.md` in full** before writing a single test.
2. For every Backlog Item, derive test cases from:
   - The **Requirements Traceability** table (TC IDs, FR/NFR IDs, and the ST / SIT / UAT / NFT column ticks determine which test type to generate)
   - The **Acceptance Criterias** (scenario tags `[HAPPY PATH]` / `[EDGE CASE]` / `[ERROR CASE]` / `[NON-FUNCTIONAL]` determine the scenario type)
   - The **Data Flow** (layer sequence determines which layers to mock in unit tests, and which layers to wire together in integration tests)
   - The **Non-Functional Requirements** (the `NFR Attribute` column determines the test approach — see mapping below)
   - The **Business Rules** in Part A, Section A2 (invariants must appear as cross-cutting assertions, not only within their originating backlog)
3. Generate a complete, runnable test suite covering every TC ID in the RTM.
4. Achieve **100% line coverage and 100% branch coverage** on all production source files.

---

## How to Map SRS Artefacts to Tests

### Acceptance Criteria Tag → Scenario Type
| Tag | Test scenario type |
|-----|--------------------|
| `[HAPPY PATH]` | Positive case — valid input, expected successful output |
| `[EDGE CASE]` | Boundary case — zero, empty, minimum/maximum threshold, repeated calls |
| `[ERROR CASE]` | Negative case — invalid/malformed input, expected rejection or clear error |
| `[NON-FUNCTIONAL]` | NFR verification — see NFR Attribute table below |

### RTM Column → Test Level
| RTM column tick | Test level to generate |
|-----------------|------------------------|
| **ST** | Unit test (mock all external collaborators of the class under test) |
| **SIT** | Integration test (wire Business Logic + Data Access with an in-memory store) |
| **UAT** | Functional / E2E test (full stack, real browser or HTTP client, random port) |
| **NFT** | Non-functional test (performance timing assertion, load, or atomicity probe) |

### NFR Attribute → Test Approach
| NFR Attribute | Test approach |
|---------------|---------------|
| Performance | Measure elapsed time and assert it is within the stated threshold |
| Security | Assert that the protected endpoint returns the appropriate rejection when accessed without credentials |
| Reliability / Atomicity | Run the operation against a transactional store and assert that no partial state persists on failure |
| Auditability | Assert that audit fields (e.g. creation timestamp, author) are preserved exactly after a mutating operation |
| Usability | Assert that all required UI elements (labels, buttons, fields) are present and correctly rendered |

### `Dependent FR` Column → Fixture Setup Order
- If FR B is dependent on FR A, the test for FR B must arrange its fixture by first satisfying the precondition defined by FR A.
- In unit tests, this means the mock for the dependency returns a pre-built object (not a bare default).
- In integration tests, seed the in-memory store with the precondition data before the act step.

### Business Rule Invariants (Part A — A2)
- Every business rule in the A2 table is a **cross-cutting constraint**.
- Each rule must be asserted in at least one test per backlog that references it in the `Related Rule ID` column — not only in the backlog where the rule originates.
- Example: if RU01 (UUID uniqueness) appears in Backlog 1 and Backlog 2, both must contain a test that asserts the UUID contract.

---

## Principles to Follow

### F.I.R.S.T.
| Letter | Requirement |
|--------|-------------|
| **F**ast | Each test must complete in milliseconds. No network calls, no real file I/O, no `Thread.sleep` / `time.sleep`. Replace all slow collaborators with in-memory fakes, stubs, or mocks. |
| **I**solated | No test may depend on the side-effects of another. Rebuild every fixture from scratch in `@BeforeEach` / `setUp()` / `pytest.fixture`. Shared mutable state between tests is a defect, not a convenience. |
| **R**epeatable | The same test must produce the same result on every machine, at any time, in any order. Eliminate all non-determinism: wall-clock time, random numbers, hardcoded ports, file-system paths, and external services. |
| **S**elf-validating | Every test must contain at least one explicit assertion. Include a descriptive failure message in any assertion whose result would not be self-evident (e.g. `assertEquals(expected, actual, "TC02: product list must contain all seeded items")`). |
| **T**imely | Tests are written **before** production code — this is TDD. The failing test is the specification; the implementation is complete only when all tests pass. Never write a test after the implementation it verifies. |

### Arrange-Act-Assert (AAA)
Structure every test body in exactly three named phases, separated by a blank line (*Meszaros, xUnit Test Patterns, 2007, §Arrange-Act-Assert*):

```
// Arrange — build the minimum fixture the test needs; configure stubs/mocks
// Act     — invoke exactly ONE behaviour or endpoint
// Assert  — verify the outcome; no logic, no loops, no further production calls
```

- **One behaviour per test.** If Act contains more than one call to production code, split the test.
- **State vs. interaction assertions:** use state assertions (return value, field values) for queries; use interaction assertions (`verify` / `assert_called_once`) only for commands that must fire a side-effect.
- Annotate every skeleton and generated test with `// Arrange` / `// Act` / `// Assert` comments.

### Test Naming Convention
Name every test method using the pattern (*Osherove, The Art of Unit Testing, 3rd ed.*):

```
tcN_methodOrFeature_stateOrInput_expectedOutcome   (Java / JS / TS)
test_tc_N_method_state_expectation                 (Python — snake_case)
```

The TC ID prefix ties each test back to the RTM. Examples:
```
void tc02_findAll_nonEmptyRepository_returnsAllProducts()
void tc03_findAll_emptyRepository_returnsEmptyList()
def test_tc12_create_order_no_duplicate_returns_saved_order():
```

### Test Double Selection (*Meszaros, xUnit Test Patterns*, ch. 11)
Always choose the minimum-power double for the job:

| Double | When to use | Example |
|--------|-------------|---------|
| **Stub** | Dependency that *returns data* your code reads (query). No call verification needed. | `when(repo.findAll()).thenReturn(List.of(...))` |
| **Mock** | Dependency that *receives a command* your code sends (write/notify). Verify the call. | `verify(repo).save(expectedProduct)` |
| **Fake** | Heavyweight dependency with behaviour (e.g. real DB logic). Use an in-memory implementation. | H2 for JPA, SQLite `:memory:` for SQLAlchemy |
| **Spy** | Wrap a real object and track *some* calls while keeping real behaviour. Use sparingly. | `@Spy` in Mockito |

Rules:
- Do **not** mock types you do not own (third-party libraries, framework internals).
- Do **not** use a mock where a stub suffices.
- Do **not** patch global module namespaces (e.g. `mocker.patch("myapp.services.MyClass")`) — use constructor injection + `MagicMock(spec=MyClass)` instead.

### Test Design Techniques (ISTQB Foundation Level Syllabus 4.0; ISO 29119-4)
Apply these techniques to derive test cases from SRS acceptance criteria:

- **Equivalence Partitioning (EP):** for each input field, identify valid and invalid partitions from the `[HAPPY PATH]` / `[ERROR CASE]` AC tags; write at least one test per partition.
- **Boundary Value Analysis (BVA):** for any AC item that specifies a numeric or length constraint (e.g. quantity ≥ 1, name ≤ 256 chars), test the exact boundary, one value below, and one value above.
- **State Transition:** where AC items describe observable state changes (e.g. order status `WAITING_PAYMENT` → `CANCELLED`), enumerate every valid and invalid transition.

### DRY (Don't Repeat Yourself)
- Extract shared fixture construction into `@BeforeEach` / `setUp()` / `pytest.fixture` or a builder/factory helper. A fixture must produce a **fresh, independent** object on every call.
- Never copy-paste the same construction block across test methods.
- Use parametrised tests (`@ParameterizedTest`, `pytest.mark.parametrize`, `test.each`) for the same assertion logic applied to multiple EP/BVA values (e.g. all four valid order statuses in a single parametrised test).
- Consolidate import boilerplate: one `conftest.py` / one `TestBase` class per layer, not per file.

### Coverage Requirements
Every generated test suite **must achieve 100% line coverage and 100% branch coverage** on all production source files (i.e. files under `src/main`, `app/`, `src/` — not the test files themselves). This means:

- **Every executable line** in production code must be reached by at least one test.
- **Every branch** of every conditional (`if`, `else`, `switch`/`when`, ternary, `try/catch`, early `return`, `&&` / `||` short-circuits) must be exercised in both the taken and not-taken directions.
- If a branch is truly unreachable (dead code), **remove the dead code** from production rather than leaving a coverage gap.
- Achieving 100% coverage does **not** mean the code is correct — it only means no line or branch was silently ignored. Pair coverage with meaningful assertions derived from the SRS.

> **How to verify:** Run the coverage report (see framework-specific commands below) and confirm 0 missed lines and 0 missed branches before considering the test suite complete.

---

## What to Generate

### 1. Unit Tests (ST column in RTM)
- **Scope:** One class / module / function at a time, isolated from all external collaborators. Covers both the Presentation Layer (controllers / views — with service collaborators stubbed) and the Business Logic Layer (services — with repository collaborators stubbed).
- **Test doubles:** use a *stub* for query dependencies (return canned data); use a *mock* to verify a specific command interaction occurred. Do not use a mock where a stub suffices.
- **Coverage per requirement — apply EP + BVA:**
  - ✅ `[HAPPY PATH]` — at least one valid-input equivalence partition per AC item; assert return value, state, or interaction
  - ❌ `[ERROR CASE]` — at least one invalid-input partition; verify exception type and message
  - 🔲 `[EDGE CASE]` — exact boundary value, one below, one above for every constrained field
- **Naming:** `tcN_methodOrFeature_stateOrInput_expectedOutcome` — the TC ID prefix must link back to the RTM.

### 2. Integration Tests (SIT column in RTM)
- **Scope:** One application slice wired together — either Presentation Layer + Business Logic (repositories faked/stubbed), or Business Logic + Data Access Layer (real in-memory store). Cover both slices.
- Use an in-memory database (H2, SQLite `:memory:`, etc.) — never a shared external database. Tests must be fully self-contained.
- Assert all three observable points for every HTTP-level test: **status code**, **response body or view name**, and **model attributes or JSON fields** confirming the operation outcome as specified by the FR.

### 3. Functional / End-to-End Tests (UAT column in RTM)
- **Scope:** The running application as a black box, driven through a real browser (Selenium / Playwright) or a full HTTP client pointed at a randomly assigned port.
- Each test must correspond to a `[HAPPY PATH]` or `[ERROR CASE]` Acceptance Criteria item tagged UAT in the RTM.
- Each test must:
  1. Navigate to a URL or perform the user action described in the Data Flow
  2. Wait for the expected element or status
  3. Assert the visible result described in the Acceptance Criteria
- Use headless browser mode by default so tests are CI-friendly.

### 4. Non-Functional Tests (NFT column in RTM)
- **Scope:** Assertions about performance, atomicity, auditability, or security as specified in the NFR table.
- Use the NFR Attribute mapping above to determine the specific test approach for each NFR.
- Every `[NON-FUNCTIONAL]` Acceptance Criteria item with an NFT tick must have a corresponding test.

---

## Output Format

For each test file, output:

```
// FILE: <relative path to test file>
<full file content>
```

After generating all files, print a **summary table**:

| Test file | Type | TC IDs covered |
|-----------|------|----------------|
| … | Unit / Integration / Functional / Non-Functional | TC01, TC02, … |

---

## Framework-Specific Setup Examples

Use the example that matches your project's stack as a reference for dependencies, annotations, file locations, and test-runner commands.

---

### Java — Spring Boot (JUnit 5 + Mockito + Selenium)

**Dependencies** (`build.gradle.kts`):
```kotlin
testImplementation("org.springframework.boot:spring-boot-starter-test") // JUnit 5 + Mockito + MockMvc
testImplementation("org.seleniumhq.selenium:selenium-java:4.18.1")
testImplementation("io.github.bonigarcia:webdrivermanager:5.7.0")
testRuntimeOnly("com.h2database:h2")
```

**Gradle test tasks**:
```kotlin
tasks.named<Test>("test") {
    useJUnitPlatform()
    filter { excludeTestsMatching("*FunctionalTest") }
}
tasks.register<Test>("functionalTest") {
    testClassesDirs = sourceSets["test"].output.classesDirs
    classpath = sourceSets["test"].runtimeClasspath
    useJUnitPlatform()
    filter { includeTestsMatching("*FunctionalTest") }
    dependsOn("testClasses")
}
```

**Test resources** (`src/test/resources/application.properties`):
```properties
spring.datasource.url=jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1
spring.datasource.driverClassName=org.h2.Driver
spring.jpa.database-platform=org.hibernate.dialect.H2Dialect
spring.jpa.hibernate.ddl-auto=create-drop
```

**Unit test skeleton**:
```java
// FILE: src/test/java/.../service/ProductServiceTest.java
@ExtendWith(MockitoExtension.class)  // re-creates @Mock fields before every test
class ProductServiceTest {

    // ── Test doubles ───────────────────────────────────────────────────────
    @Mock  ProductRepository repo;   // stub: returns data; mock: verify writes
    @InjectMocks ProductServiceImpl service;

    // ── Shared fixture (rebuilt per test by MockitoExtension) ──────────────
    private Product sampleProduct;

    @BeforeEach
    void setUp() {
        sampleProduct = new Product("p1", "Pen", 3); // fresh independent instance
    }

    // TC02 — FR1.02: all stored products displayed
    @Test
    void tc02_findAll_nonEmptyRepository_returnsAllProducts() {
        // Arrange
        when(repo.findAll()).thenReturn(List.of(sampleProduct));

        // Act
        List<Product> result = service.findAll();

        // Assert
        assertThat(result).hasSize(1).as("TC02: repo product must be returned");
        assertThat(result.get(0).getName()).isEqualTo("Pen");
    }

    // TC03 — FR1.03: empty store returns empty list without error
    @Test
    void tc03_findAll_emptyRepository_returnsEmptyList() {
        // Arrange
        when(repo.findAll()).thenReturn(Collections.emptyList());

        // Act + Assert (trivial act — combined for brevity)
        assertThat(service.findAll()).isEmpty()
            .as("TC03: empty repo must return empty list");
    }
}
```

**Integration test skeleton**:
```java
// FILE: src/test/java/.../controller/ProductControllerTest.java
@WebMvcTest(ProductController.class)
class ProductControllerTest {

    @Autowired MockMvc mockMvc;
    @MockBean  ProductService service;  // stub the service layer

    // TC01 — FR1.01: list page returns HTTP 200 and renders the product list view
    @Test
    void tc01_getProductList_serviceReturnsEmpty_rendersListView() throws Exception {
        // Arrange
        when(service.findAll()).thenReturn(Collections.emptyList());

        // Act + Assert
        mockMvc.perform(get("/product/list"))
               .andExpect(status().isOk())
               .andExpect(view().name("productList"))
               .andExpect(model().attribute("products", hasSize(0)));
    }
}
```

**Functional test skeleton**:
```java
// FILE: src/test/java/.../functional/ProductListFunctionalTest.java
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ProductListFunctionalTest {

    @LocalServerPort int port;
    WebDriver driver;

    @BeforeEach
    void setUp() {
        WebDriverManager.chromedriver().setup();
        ChromeOptions opts = new ChromeOptions();
        opts.addArguments("--headless", "--no-sandbox");
        driver = new ChromeDriver(opts);
    }

    @AfterEach
    void tearDown() { driver.quit(); }

    // TC01 — UAT: product list page is accessible and renders the page heading
    @Test
    void tc01_uat_getProductListPage_pageLoads_rendersHeading() {
        // Arrange — server started by @SpringBootTest; nothing else to arrange

        // Act
        driver.get("http://localhost:" + port + "/product/list");

        // Assert
        WebElement body = driver.findElement(By.tagName("body"));
        assertNotNull(body, "TC01 UAT: page body must exist");
        assertFalse(body.getText().isBlank(), "TC01 UAT: page body must contain rendered content");
    }
}
```

**Coverage tool:** JaCoCo.

Add to `build.gradle.kts`:
```kotlin
plugins {
    jacoco
}

jacoco {
    toolVersion = "0.8.11"
}

tasks.named<JacocoReport>("jacocoTestReport") {
    dependsOn(tasks.named("test"))
    reports {
        xml.required = true
        html.required = true
    }
}

tasks.named<JacocoCoverageVerification>("jacocoTestCoverageVerification") {
    dependsOn(tasks.named("jacocoTestReport"))
    violationRules {
        rule {
            limit {
                counter = "LINE"
                value   = "COVEREDRATIO"
                minimum = "1.0".toBigDecimal()
            }
            limit {
                counter = "BRANCH"
                value   = "COVEREDRATIO"
                minimum = "1.0".toBigDecimal()
            }
        }
    }
}

tasks.named("check") {
    dependsOn(tasks.named("jacocoTestCoverageVerification"))
}
```

**Run commands**:
```bash
./gradlew test                                        # unit + integration
./gradlew functionalTest                              # Selenium E2E
./gradlew test jacocoTestReport                       # run tests + generate HTML/XML report
./gradlew test jacocoTestCoverageVerification         # fail build if < 100% line or branch
./gradlew check                                       # test + coverage verification (CI)
```

Coverage report: `build/reports/jacoco/test/html/index.html`

---

### Python — Django (pytest + pytest-django + Selenium)

**Dependencies** (`requirements-test.txt` or `pyproject.toml`):
```
pytest
pytest-django
pytest-mock
selenium
webdriver-manager
factory-boy
```

**Config** (`pytest.ini` or `pyproject.toml`):
```ini
[pytest]
DJANGO_SETTINGS_MODULE = myproject.settings.test
```

**Test settings** (`myproject/settings/test.py`):
```python
from .base import *
DATABASES = {
    "default": {"ENGINE": "django.db.backends.sqlite3", "NAME": ":memory:"}
}
from unittest.mock import MagicMock

# TC12 — FR3.01: order is created and persisted with unique ID and products
def test_tc12_create_order_noDuplicate_persistsAndReturnsOrder():
    # Arrange
    stub_repo = MagicMock()                             # constructor injection — no global patching
    stub_repo.find_by_id.return_value = None            # stub: no duplicate exists
    stub_repo.save.return_value = Order(id="uuid-1", status="WAITING_PAYMENT")
    svc = OrderService(repo=stub_repo)

    # Act
    result = svc.create(Order(id="uuid-1", products=[product_fixture()]))

    # Assert — state
    assert result.id == "uuid-1"
    assert result.status == "WAITING_PAYMENT"
    # Assert — interaction: save must have been called exactly once
    stub_repo.save.assert_called_once()

# TC14 — FR3.03: duplicate order ID returns conflict indicator
def test_tc14_create_order_duplicateId_returnsNone():
    # Arrange
    stub_repo = MagicMock()
    stub_repo.find_by_id.return_value = Order(id="uuid-1")   # duplicate exists
    svc = OrderService(repo=stub_repo)

    # Act
    result = svc.create(Order(id="uuid-1", products=[product_fixture()]))

    # Assert
    assert result is None, "TC14: duplicate order ID must return None"
    stub_repoo=mock_repo.return_value)

    result = svc.create(Order(id="uuid-1", products=[product_fixture()]))

    assert result is None  # or check the appropriate conflict indicator
    mock_repo.return_value.save.assert_not_called()
```

**Integration test skeleton**:
```python
# FILE: tests/integration/test_product_views.py
@pytest.mark.django_db
def test_tc01_product_list_view_returns_200(client):
    response = client.get("/product/list")
    assert response.status_code == 200

@pytest.mark.django_db
def test_tc02_product_list_view_displays_all_products(client):
    ProductFactory.create_batch(3)
    response = client.get("/product/list")
    assert len(response.context["products"]) == 3
```

**Functional test skeleton**:
```python
# FILE: tests/functional/test_product_list_functional.py
@pytest.fixture(scope="module")
def browser():
    service = ChromeService(ChromeDriverManager().install())
    opts = ChromeOptions()
    opts.add_argument("--headless")
    driver = webdriver.Chrome(service=service, options=opts)
    yield driver
    driver.quit()

# TC01 — UAT: product list page is accessible
def test_tc01_uat_product_list_page_loads(browser, live_server):
    browser.get(live_server.url + "/product/list")
    assert browser.find_element(By.TAG_NAME, "body")
```

**Run commands**:
```bash
pytest tests/unit/
pytest tests/integration/
pytest tests/functional/
pytest                      # all
```

**Coverage** (`pytest-cov`):
```bash
pip install pytest-cov
pytest --cov=myapp --cov-branch \
       --cov-report=html --cov-report=term-missing \
       --cov-fail-under=100 \
       tests/unit/ tests/integration/
```
Report: `htmlcov/index.html`

---

### Python — Flask (pytest + pytest-flask + Selenium)

**Dependencies**:
```
pytest
pytest-flask
pytest-mock
selenium
webdriver-manager
```

**Config** (`conftest.py`):
```python
import pytest
from myapp import create_app

@pytest.fixture
def app():
    app = create_app({"TESTING": True, "DATABASE": ":memory:"})
    yield app

@pytest.fixture
def client(app):
    return app.test_client()
```

**Unit test skeleton**:
```python
# FILE: tests/unit/test_product_service.py
from unittest.mock import MagicMock

# TC09 — FR2.04: UUID assigned to every new product
def test_tc09_create_product_newItem_assignsUuidAndPersists():
    # Arrange
    stub_repo = MagicMock()
    stub_repo.save.return_value = Product(id="uuid-p1", name="Pen", quantity=5)
    service = ProductService(repo=stub_repo)

    # Act
    product = service.create({"name": "Pen", "quantity": 5})

    # Assert — state
    assert product.id is not None, "TC09: created product must have a UUID"
    # Assert — interaction
    stub_repo.save.assert_called_once()
```

**Integration test skeleton**:
```python
# FILE: tests/integration/test_product_routes.py

# TC06 — FR2.01: create-product form page returns HTTP 200
def test_tc06_create_product_form_returns_200(client):
    response = client.get("/product/create")
    assert response.status_code == 200

# TC08 — FR2.03: valid form submission redirects to list
def test_tc08_create_product_valid_submission_redirects(client):
    response = client.post("/product/create", data={"name": "Pen", "quantity": "5"})
    assert response.status_code in (302, 303)
```

**Run commands**:
```bash
pytest -k "not functional"     # unit + integration
pytest -k "functional"         # Selenium only
pytest                         # all
```

**Coverage**:
```bash
pytest --cov=myapp --cov-branch \
       --cov-report=html --cov-report=term-missing \
       --cov-fail-under=100 \
       -k "not functional"
```

---

### Python — FastAPI (pytest + httpx + Selenium)

**Dependencies**:
```
pytest
pytest-asyncio
httpx
selenium
webdriver-manager
```

**Config** (`conftest.py`):
```python
import pytest
from httpx import AsyncClient
from myapp.main import app

@pytest.fixture
from unittest.mock import MagicMock

# TC13 — FR3.02: new order status is WAITING_PAYMENT
def test_tc13_create_order_newOrder_statusIsWaitingPayment():
    # Arrange
    stub_repo = MagicMock()
    stub_repo.find_by_id.return_value = None
    stub_repo.save.return_value = Order(id="o1", status="WAITING_PAYMENT")
    svc = OrderService(repo=stub_repo)

    # Act
    order = svc.create({"id": "o1", "products": [{"id": "p1"}], "author": "Alice"})

    # Assert
    assert order.status == "WAITING_PAYMENT", "TC13: initial order status must be r_service.py

# TC13 — FR3.02: new order status is WAITING_PAYMENT
def test_tc13_new_order_status_is_waiting_payment(mocker):
    mock_repo = mocker.MagicMock()
    mock_repo.find_by_id.return_value = None
    svc = OrderService(repo=mock_repo)
    order = svc.create({"id": "o1", "products": [{"id": "p1"}], "author": "Alice"})
    assert order.status == "WAITING_PAYMENT"
```

**Integration test skeleton**:
```python
# FILE: tests/integration/test_order_routes.py

# TC12 — FR3.01: order creation endpoint returns the saved order
@pytest.mark.asyncio
async def test_tc12_create_order_returns_saved_order(ac):
    response = await ac.post("/orders", json={"id": "o1", "products": [{"id": "p1"}], "author": "Alice"})
    assert response.status_code == 201
    assert response.json()["status"] == "WAITING_PAYMENT"
```

**Run commands**:
```bash
pytest tests/unit tests/integration
pytest tests/functional
```

**Coverage**:
```bash
pytest --cov=myapp --cov-branch \
       --cov-report=html --cov-report=term-missing \
       --cov-fail-under=100 \
       tests/unit tests/integration
```

---

### Node.js — React (Jest + React Testing Library + Playwright)

**Dependencies** (`package.json`):
```json
{
  "devDependencies": {
    "@testing-library/react": "^14",
    "@testing-library/jest-dom": "^6",
    "@testing-library/user-event": "^14",
    "jest": "^29",
    "jest-environment-jsdom": "^29",
    "@playwright/test": "^1"
  }
}
```

**Unit test skeleton**:
```tsx
// FILE: src/__tests__/unit/ProductList.test.tsx
import { render, screen } from "@testing-library/react";
import ProductList from "../../components/ProductList";

describe("ProductList", () => {
  const products = [{ id: "p1", name: "Notebook", quantity: 5 }];

  // TC02 — FR1.02: product list displays all stored products
  it("tc02_render_nonEmptyList_displaysAllProducts", () => {
    // Arrange + Act
    render(<ProductList products={products} />);

    // Assert
    expect(screen.getByText("Notebook")).toBeInTheDocument();
  });

  // TC03 — FR1.03: empty store shows empty state
  it("tc03_render_emptyList_displaysEmptyState", () => {
    // Arrange + Act
    render(<ProductList products={[]} />);

    // Assert
    expect(screen.getByText(/no products/i)).toBeInTheDocument();
  });
});
```

**Integration test skeleton**:
```tsx
// FILE: src/__tests__/integration/ProductList.integration.test.tsx
jest.mock("../../api/productApi");
import * as api from "../../api/productApi";

// TC01 — FR1.01: list page is accessible and renders successfully
it("tc01_fetchProducts_apiReturnsItems_rendersProductNames", async () => {
  // Arrange
  (api.fetchProducts as jest.Mock).mockResolvedValue([
    { id: "p1", name: "Notebook", quantity: 5 },
  ]);

  // Act
  render(<ProductListPage />);

  // Assert
  expect(await screen.findByText("Notebook")).toBeInTheDocument();
});
```

**Functional test skeleton**:
```ts
// FILE: e2e/productList.spec.ts
import { test, expect } from "@playwright/test";

// TC01 — UAT: product list page is accessible
test("tc01_uat_getProductListPage_pageLoads_rendersBody", async ({ page }) => {
  // Arrange — Playwright webServer config starts the server

  // Act
  await page.goto("/product/list");

  // Assert
  await expect(page.locator("body")).toBeVisible();
});
```

**Run commands**:
```bash
npx jest --testPathPattern="unit|integration"
npx playwright test
```

**Coverage** (Jest built-in):
```bash
npx jest --coverage --testPathPattern="unit|integration"
```

Add to `jest.config.js` to enforce 100%:
```js
coverageThreshold: {
  global: {
    lines: 100,
    branches: 100,
    functions: 100,
    statements: 100,
  },
},
```
Report: `coverage/lcov-report/index.html`

---

### Node.js — Next.js (Jest + React Testing Library + Playwright)

Same Jest / RTL setup as React (see above). Additional notes for SRS-driven tests:

- Use `next/jest` config helper to resolve path aliases and transforms.
- For pages with server-side data fetching (`getServerSideProps` / `getStaticProps`), test the data-fetching function as a unit test and render the page component with mocked props separately.
- Playwright config (`playwright.config.ts`): set `webServer.command = "next start"` and `baseURL` to the local dev server.
- TC IDs in test names follow the same `tcN_feature_state_outcome` convention as the React examples above.
- Coverage: same `coverageThreshold` config as the React section.

---

### Node.js — Vue (Vitest + Vue Test Utils + Playwright)

**Dependencies** (`package.json`):
```json
{
  "devDependencies": {
    "@vue/test-utils": "^2",
    "vitest": "^1",
    "@vitest/coverage-v8": "^1",
    "jsdom": "^24",
    "@playwright/test": "^1"
  }
}
```

**Unit test skeleton**:
```ts
// FILE: src/__tests__/unit/ProductCard.spec.ts
import { mount } from "@vue/test-utils";
import { describe, it, expect } from "vitest";
import ProductCard from "../../components/ProductCard.vue";

describe("ProductCard", () => {
  const product = { id: "p1", name: "Notebook", quantity: 5 };

  // TC02 — FR1.02: product list displays all stored products
  it("tc02_render_validProduct_displaysName", () => {
    // Arrange + Act
    const wrapper = mount(ProductCard, { props: product });

    // Assert
    expect(wrapper.text()).toContain("Notebook");
  });

  // TC04 — FR2.02: delete button emits correct event with product id
  it("tc04_deleteButton_clicked_emitsDeleteWithId", async () => {
    // Arrange
    const wrapper = mount(ProductCard, { props: product });

    // Act
    await wrapper.find("button.delete").trigger("click");

    // Assert
    expect(wrapper.emitted("delete")).toBeTruthy();
    expect(wrapper.emitted("delete")![0]).toEqual(["p1"]);
  });
});
```

**Integration test skeleton** (Pinia store + component):
```ts
// FILE: src/__tests__/integration/ProductListPage.integration.spec.ts
import { mount } from "@vue/test-utils";
import { createPinia, setActivePinia } from "pinia";
import { beforeEach, describe, it, expect, vi } from "vitest";
import ProductListPage from "../../pages/ProductList.vue";
import * as productApi from "../../api/productApi";

describe("ProductListPage integration", () => {
  beforeEach(() => setActivePinia(createPinia()));

  // TC01 — FR1.01: list page renders products fetched from the API
  it("tc01_mount_apiReturnsProducts_rendersProductNames", async () => {
    // Arrange
    vi.spyOn(productApi, "fetchProducts").mockResolvedValue([
      { id: "p1", name: "Notebook", quantity: 5 },
    ]);

    // Act
    const wrapper = mount(ProductListPage);
    await wrapper.vm.$nextTick();

    // Assert
    expect(wrapper.text()).toContain("Notebook");
  });
});
```

**Functional test skeleton** (Playwright):
```ts
// FILE: e2e/productList.spec.ts
import { test, expect } from "@playwright/test";

// TC01 — UAT: product list page is accessible
test("tc01_uat_getProductListPage_pageLoads_rendersContent", async ({ page }) => {
  // Arrange — Playwright webServer config starts the server

  // Act
  await page.goto("/product/list");

  // Assert
  await expect(page.locator("body")).toBeVisible();
});
```

**Run commands**:
```bash
npx vitest run
npx playwright test
```

**Coverage** (Vitest + `@vitest/coverage-v8`):
```bash
npx vitest run --coverage
```
Add to `vitest.config.ts` to enforce 100%:
```ts
coverage: {
  provider: "v8",
  thresholds: { lines: 100, branches: 100, functions: 100, statements: 100 },
  reporter: ["text", "html"],
},
```
Report: `coverage/index.html`

---

### Rust — Rocket (built-in test + Rocket test client)

**Dependencies** (`Cargo.toml`):
```toml
[dev-dependencies]
rocket = { version = "0.5", features = ["testing"] }
```

**Unit test skeleton**:
```rust
// FILE: src/services/product_service.rs (inline tests)
#[cfg(test)]
mod tests {
    use super::*;

    // TC02 — FR1.02: all stored products are returned
    #[test]
    fn tc02_find_all_non_empty_store_returns_all_products() {
        // Arrange
        let mut repo = InMemoryProductRepository::new();
        repo.save(Product::new("pen-1", "Pen", 5).unwrap());
        let svc = ProductService::new(repo);

        // Act
        let result = svc.find_all();

        // Assert
        assert_eq!(result.len(), 1, "TC02: one seeded product must be returned");
        assert_eq!(result[0].name(), "Pen");
    }

    // TC03 — FR1.03: empty store returns empty list
    #[test]
    fn tc03_find_all_empty_store_returns_empty_vec() {
        // Arrange
        let svc = ProductService::new(InMemoryProductRepository::new());

        // Act
        let result = svc.find_all();

        // Assert
        assert!(result.is_empty(), "TC03: empty repo must return empty vec");
    }

    // TC07 — FR2.03: blank name is rejected (EP: invalid partition)
    #[test]
    fn tc07_new_product_blank_name_returns_err() {
        // Arrange + Act + Assert (single-expression BVA check)
        assert!(
            Product::new("p-1", "", 5).is_err(),
            "TC07: blank name must be rejected"
        );
    }
}
```

**Integration test skeleton** (Rocket test client):
```rust
// FILE: tests/integration_test.rs
use rocket::http::Status;
use rocket::local::blocking::Client;

// TC01 — FR1.01: product list endpoint returns HTTP 200
#[test]
fn tc01_get_product_list_returns_200() {
    // Arrange
    let client = Client::tracked(build_rocket()).expect("valid Rocket instance");

    // Act
    let response = client.get("/product/list").dispatch();

    // Assert
    assert_eq!(response.status(), Status::Ok, "TC01: /product/list must return HTTP 200");
}

// TC08 — FR2.03: valid POST creates product and redirects (3xx)
#[test]
fn tc08_post_create_product_valid_input_redirects() {
    // Arrange
    let client = Client::tracked(build_rocket()).expect("valid Rocket instance");

    // Act
    let response = client
        .post("/product/create")
        .body("name=Pen&quantity=5")
        .dispatch();

    // Assert
    let code = response.status().code;
    assert!(
        (300..400).contains(&code),
        "TC08: valid create must redirect (3xx), got {code}"
    );
}
```

**Run commands**:
```bash
cargo test                        # unit + integration
cargo test -- --ignored           # slow / E2E tests marked #[ignore]
```

**Coverage** (`cargo-tarpaulin`):
```bash
cargo install cargo-tarpaulin
cargo tarpaulin --out Html --branch --fail-under 100
```
Report: `tarpaulin-report.html`

---

### C# — ASP.NET Core (xUnit + Moq + Selenium)

**Dependencies** (`.csproj`):
```xml
<PackageReference Include="Microsoft.AspNetCore.Mvc.Testing" Version="8.*" />
<PackageReference Include="Moq"                              Version="4.*" />
<PackageReference Include="xunit"                            Version="2.*" />
<PackageReference Include="xunit.runner.visualstudio"        Version="2.*" />
<PackageReference Include="Selenium.WebDriver"               Version="4.*" />
<PackageReference Include="Selenium.WebDriver.ChromeDriver"  Version="*"   />
```

**Unit test skeleton**:
```csharp
// FILE: Tests/Unit/OrderServiceTests.cs
public class OrderServiceTests
{
    // ── Test doubles ────────────────────────────────────────────────────────
    private readonly Mock<IOrderRepository> _repoMock = new();
    private readonly OrderService _sut;

    public OrderServiceTests() => _sut = new OrderService(_repoMock.Object);

    // TC12 — FR3.01: order created and persisted with unique ID and products
    [Fact]
    public void Tc12_CreateOrder_NewId_SavesAndReturnsOrder()
    {
        // Arrange
        var order = new Order("o-001", new List<Product> { new() }, 1000, "alice");
        _repoMock.Setup(r => r.FindById("o-001")).Returns((Order?)null); // stub: no duplicate
        _repoMock.Setup(r => r.Save(order)).Returns(order);

        // Act
        var result = _sut.CreateOrder(order);

        // Assert — state
        Assert.Equal(order, result);
        // Assert — interaction: Save must have been called exactly once
        _repoMock.Verify(r => r.Save(order), Times.Once);
    }

    // TC14 — FR3.03: duplicate order ID returns null (conflict indicator)
    [Fact]
    public void Tc14_CreateOrder_DuplicateId_ReturnsNull()
    {
        // Arrange
        _repoMock.Setup(r => r.FindById("o-001")).Returns(new Order { Id = "o-001" });

        // Act
        var result = _sut.CreateOrder(new Order { Id = "o-001" });

        // Assert
        Assert.Null(result);
        _repoMock.Verify(r => r.Save(It.IsAny<Order>()), Times.Never);
    }

    // TC13 — FR3.02: new order status is WAITING_PAYMENT
    [Fact]
    public void Tc13_CreateOrder_NewOrder_StatusIsWaitingPayment()
    {
        // Arrange
        var order = new Order("o-002", new List<Product> { new() }, 500, "bob");
        _repoMock.Setup(r => r.FindById("o-002")).Returns((Order?)null);
        _repoMock.Setup(r => r.Save(It.IsAny<Order>())).Returns<Order>(o => o);

        // Act
        var result = _sut.CreateOrder(order);

        // Assert
        Assert.Equal(OrderStatus.WaitingPayment, result?.Status);
    }
}
```

**Integration test skeleton** (WebApplicationFactory):
```csharp
// FILE: Tests/Integration/ProductControllerTests.cs
public class ProductControllerTests : IClassFixture<WebApplicationFactory<Program>>
{
    private readonly HttpClient _client;

    public ProductControllerTests(WebApplicationFactory<Program> factory)
        => _client = factory.CreateClient();

    // TC01 — FR1.01: product list page returns HTTP 200 with rendered content
    [Fact]
    public async Task Tc01_GetProductList_Returns200WithContent()
    {
        // Arrange — WebApplicationFactory wires up the full pipeline with in-memory store

        // Act
        var response = await _client.GetAsync("/product/list");
        var body = await response.Content.ReadAsStringAsync();

        // Assert
        Assert.Equal(HttpStatusCode.OK, response.StatusCode);
        Assert.Contains("product", body, StringComparison.OrdinalIgnoreCase);
    }
}
```

**Functional test skeleton** (Selenium):
```csharp
// FILE: Tests/Functional/ProductListFunctionalTests.cs
public class ProductListFunctionalTests : IClassFixture<WebApplicationFactory<Program>>
{
    private readonly string _baseUrl;

    public ProductListFunctionalTests(WebApplicationFactory<Program> factory)
        => _baseUrl = factory.Server.BaseAddress.ToString();

    // TC01 — UAT: product list page is accessible and renders content
    [Fact]
    public void Tc01_Uat_GetProductListPage_BodyHasContent()
    {
        // Arrange
        var options = new ChromeOptions();
        options.AddArgument("--headless");

        // Act
        using var driver = new ChromeDriver(options);
        driver.Navigate().GoToUrl(_baseUrl + "product/list");

        // Assert
        var body = driver.FindElement(By.TagName("body"));
        Assert.NotNull(body);
        Assert.False(
            string.IsNullOrWhiteSpace(body.Text),
            "TC01 UAT: page body must have rendered content"
        );
    }
}
```

**Run commands**:
```bash
dotnet test --filter "Category!=Functional"   # unit + integration
dotnet test --filter "Category=Functional"    # Selenium E2E
dotnet test                                   # all
```

**Coverage** (Coverlet):
```xml
<!-- add to test .csproj -->
<PackageReference Include="coverlet.collector" Version="6.*" />
```
```bash
dotnet test --collect:"XPlat Code Coverage" \
  -- DataCollectionRunSettings.DataCollectors.DataCollector.Configuration.Format=opencover

# enforce 100% line + branch
dotnet test /p:CollectCoverage=true \
           /p:CoverletOutputFormat=opencover \
           /p:Threshold=100 \
           /p:ThresholdType="line,branch"
```
Report: `TestResults/coverage.opencover.xml` (use `reportgenerator` for HTML output).

---

## Checklist (verify before finishing)

- [ ] Every TC ID in the RTM has at least one corresponding test
- [ ] All `[HAPPY PATH]` Acceptance Criteria have a passing unit or integration test
- [ ] All `[ERROR CASE]` Acceptance Criteria have a test that verifies the rejection
- [ ] All `[EDGE CASE]` Acceptance Criteria have a boundary-value test (BVA applied)
- [ ] All `[NON-FUNCTIONAL]` Acceptance Criteria with NFT column ticks have a non-functional test
- [ ] No test shares mutable state with another test
- [ ] Every fixture is rebuilt from scratch in `@BeforeEach` / `setUp()` / `pytest.fixture` — no shared instances
- [ ] Constructor injection used throughout — no global module patching
- [ ] All unit tests use stubs or mocks for their external collaborators
- [ ] Integration tests use an in-memory database or stub external services
- [ ] Functional tests run headlessly and clean up the browser after each test
- [ ] **Tests have been executed and pass before being committed**
- [ ] **Line coverage is 100%** — coverage report shows 0 missed lines
- [ ] **Branch coverage is 100%** — coverage report shows 0 missed branches
- [ ] Coverage verification is wired into CI so a drop in coverage fails the build automatically
