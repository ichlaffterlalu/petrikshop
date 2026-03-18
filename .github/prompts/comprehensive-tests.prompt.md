---
mode: 'agent'
tools: ['codebase', 'editFiles', 'runCommands', 'todo']
description: >
  Generates a comprehensive, executable test suite (unit, integration, and
  functional/E2E) for any web framework.  Follows F.I.R.S.T. and DRY
  principles, covers positive / negative / edge cases, uses mocks for all
  external dependencies in unit tests, and enforces 100% line and branch
  coverage for all production source files.
---

# Comprehensive Test Suite Generator

> **Reference baseline:** `#tests` (the built-in VS Code Copilot test-generation skill)

---

## ⚠️ Critical Warning — Acknowledgement Required

**Before generating any tests, ask the developer the following question and wait for their explicit answer. Do NOT proceed until they confirm.**

> **"This prompt generates tests based on how the code currently behaves — not based on how it was intended to behave. If your code contains a bug, the generated tests will mirror that bug, pass successfully, and give you a false sense of confidence. You could end up shipping a broken product with a fully green test suite.**
>
> **This tool is designed for characterisation testing (locking in existing behaviour) and regression testing (catching unintended changes) — not for verifying that your code is correct.**
>
> **Do you understand this limitation and wish to proceed? (yes / no)"**

If the developer answers **no** or expresses doubt, stop and suggest they first review their code manually against its requirements before using this prompt.

Only continue to the task below if the developer explicitly answers **yes**.

---

## Your Task

Analyse **every source file** in the project and generate a complete, runnable test suite.  Do not skip any public method, constructor, or route handler.

---

## Principles to Follow

### F.I.R.S.T.
| Letter | Requirement |
|--------|-------------|
| **F**ast | Each test must complete in milliseconds. No network calls, no real file I/O, no `Thread.sleep` / `time.sleep`. Replace all slow collaborators with in-memory fakes, stubs, or mocks. |
| **I**solated | No test may depend on the side-effects of another. Rebuild every fixture from scratch in `@BeforeEach` / `setUp()` / `pytest.fixture`. Shared mutable state between tests is a defect, not a convenience. |
| **R**epeatable | The same test must produce the same result on every machine, at any time, in any order. Eliminate all non-determinism: wall-clock time, random numbers, hardcoded ports, file-system paths, and external services. |
| **S**elf-validating | Every test must contain at least one explicit assertion that unambiguously passes or fails. Include a descriptive failure message in any assertion whose result would not be self-evident (e.g. `assertEquals(expected, actual, "product id must be prepended with 'esh-'")`) . |
| **T**imely | Because this prompt generates characterisation tests against *existing* code, "timely" means: write these tests **before making any code change**. The suite must first capture what the code currently does as a stable baseline — only then can you safely refactor. |

### Arrange-Act-Assert (AAA)
Structure every test body in exactly three named phases, separated by a blank line (*Meszaros, xUnit Test Patterns, 2007, §Arrange-Act-Assert*):

```
// Arrange — build the minimum fixture the test needs; configure stubs/mocks
// Act     — invoke exactly ONE behaviour or endpoint
// Assert  — verify the outcome; no logic, no loops, no further production calls
```

- **One behaviour per test.** If Act contains more than one call to production code, split the test.
- **State vs. interaction assertions:** use state assertions (return value, field values) for queries; use interaction assertions (`verify` / `assert_called_once`) only for commands that must fire a side-effect — do not verify both in the same test unless the requirement demands both.
- Annotate skeletons and generated tests with `// Arrange` / `// Act` / `// Assert` (or `# Arrange` etc. for Python).

### Test Naming Convention
Name every test method using the pattern (*Osherove, The Art of Unit Testing, 3rd ed.*):

```
methodOrFeature_stateOrInput_expectedOutcome   (Java / JS / TS)
test_method_state_expectation                  (Python — snake_case)
```

Examples:
```
findAll_emptyRepository_returnsEmptyList
create_nullName_throwsIllegalArgumentException
test_create_order_duplicate_id_returns_none
```

The name must read as a sentence: *"Given an empty repository, when findAll is called, then it returns an empty list."*
Never use generic names like `testPositiveCase`, `test1`, or `testSuccess` — they provide no diagnostic information on failure.

### Test Double Selection (*Meszaros, xUnit Test Patterns*, ch. 11)
Always choose the minimum-power double for the job:

| Double | When to use | Example |
|--------|-------------|---------|
| **Stub** | Dependency that *returns data* your code reads (query). No call verification needed. | `when(repo.findAll()).thenReturn(List.of(...))` |
| **Mock** | Dependency that *receives a command* your code sends (write/notify). Verify the call. | `verify(repo).save(expectedProduct)` |
| **Fake** | Heavyweight dependency with behaviour (e.g. real DB logic). Use an in-memory implementation. | H2 for JPA, SQLite `:memory:` for SQLAlchemy |
| **Spy** | Wrap a real object and track *some* calls while keeping real behaviour. Use sparingly. | `@Spy` in Mockito |

Rules:
- Do **not** mock types you do not own (third-party libraries, framework internals). Use a fake or an integration test.
- Do **not** use a mock where a stub suffices — unnecessary `verify()` over-specifies the test and makes it fragile to implementation changes.
- Do **not** patch global module namespaces (e.g. `mocker.patch("myapp.services.MyClass")`) — use constructor injection + `MagicMock(spec=MyClass)` instead.

### Test Design Techniques (ISTQB Foundation Level Syllabus 4.0; ISO 29119-4)
Apply these techniques to derive test cases exhaustively rather than guessing "interesting" values:

- **Equivalence Partitioning (EP):** divide inputs into valid and invalid partitions; write at least one test per partition.
- **Boundary Value Analysis (BVA):** for numeric or length-constrained inputs, test the exact lower/upper boundary, one value below it, and one value above it (e.g. for a quantity ≥ 1 rule: test 0, 1, and 2).
- **Decision Table / State Transition:** for logic with multiple conditions or an observable state machine (e.g. order status transitions), enumerate every row of the decision table or every edge in the state diagram.

### DRY (Don't Repeat Yourself)
- Extract all shared fixture construction into `@BeforeEach` / `setUp()` / `pytest.fixture` or a dedicated builder/factory helper. A fixture method must produce a **fresh, independent** object on every call — never a shared mutable instance.
- Never copy-paste the same object-construction block across test methods.
- Use parametrised tests (`@ParameterizedTest`, `pytest.mark.parametrize`, `test.each`) for the same assertion logic applied to multiple EP/BVA values.
- Consolidate import boilerplate: one `conftest.py` / one `TestBase` class per test layer, not per test file.

### Coverage Requirements
Every generated test suite **must achieve 100% line coverage and 100% branch coverage** on all production source files (i.e. files under `src/main`, `app/`, `src/` — not the test files themselves).  This means:

- **Every executable line** in production code must be reached by at least one test.
- **Every branch** of every conditional (`if`, `else`, `switch`/`when`, ternary, `try/catch`, early `return`, `&&` / `||` short-circuits) must be exercised in both the taken and not-taken directions.
- If a branch is truly unreachable (dead code), **remove the dead code** from production rather than leaving a coverage gap.
- Achieving 100% coverage does **not** mean the code is correct — it only means no line or branch was silently ignored.  Pair coverage with meaningful assertions.

> **How to verify:** Run the coverage report (see framework-specific commands below) and confirm 0 missed lines and 0 missed branches before considering the test suite complete.

---

## What to Generate

### 1. Unit Tests
- **Scope:** One class / function at a time, isolated from all external collaborators. Covers both Presentation Layer classes (controller, view helpers — service collaborators stubbed/mocked) and Business Logic Layer classes (services — repository collaborators stubbed/mocked).
- **Test doubles:** use a *stub* to supply return values for queries; use a *mock* to verify that a specific command interaction occurred. Do not use a mock where a stub suffices.
- **Coverage per method — apply EP + BVA systematically:**
  - ✅ Happy path — at least one valid-input equivalence partition per method
  - ❌ Error / rejection path — at least one invalid-input partition; verify exception type and message where applicable
  - 🔲 Boundary values — exact boundary value, one below, one above for every constrained field

### 2. Integration Tests
- **Scope:** One application slice wired together — either the Presentation Layer + Business Logic Layer (with repositories faked/stubbed), or the Business Logic Layer + Data Access Layer (with a real in-memory store). Aim to test both slices.
- Use an in-memory database (H2, SQLite `:memory:`, `mongomock`, etc.) — never a shared external database. Tests must remain fully self-contained.
- Assert all three observable points for every HTTP-level test: **status code**, **response body or view name**, and **model attributes or JSON fields** that confirm the operation succeeded.

### 3. Functional / End-to-End Tests
- **Scope:** The running application as a black box, driven through a real browser (Selenium / Playwright) or an HTTP client.
- Start the server programmatically on a random port when the framework supports it.
- Each test must:
  1. Navigate to a URL / perform an action
  2. Wait for the expected element or status
  3. Assert the visible result
- Use headless browser mode by default so tests are CI-friendly.

---

## Output Format

For each test file, output:

```
// FILE: <relative path to test file>
<full file content>
```

After generating all files, print a **summary table**:

| Test file | Type | Cases covered |
|-----------|------|---------------|
| … | Unit / Integration / Functional | list of case names |

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
@ExtendWith(MockitoExtension.class)  // re-creates @Mock fields before every test
class ProductServiceTest {

    // ── Test doubles ───────────────────────────────────────────────────────
    @Mock  ProductRepository repo;    // stub: returns data; verify only when needed
    @InjectMocks ProductServiceImpl service;

    // ── Shared fixture (rebuilt before every test by MockitoExtension) ─────
    private Product sampleProduct;

    @BeforeEach
    void setUp() {
        sampleProduct = new Product("p1", "Pen", 5); // fresh independent instance
    }

    // ── AAA unit tests ────────────────────────────────────────────────────

    @Test
    void findAll_nonEmptyRepository_returnsAllProducts() {
        // Arrange
        when(repo.findAll()).thenReturn(List.of(sampleProduct));

        // Act
        List<Product> result = service.findAll();

        // Assert
        assertEquals(1, result.size(), "findAll must return all products from repo");
        assertEquals("Pen", result.get(0).getName());
    }

    @Test
    void findAll_emptyRepository_returnsEmptyList() {
        // Arrange
        when(repo.findAll()).thenReturn(Collections.emptyList());

        // Act + Assert (trivial act — combined for brevity)
        assertTrue(service.findAll().isEmpty(), "empty repo must yield empty list");
    }

    @Test  // BVA: quantity at exact lower boundary (0)
    void create_zeroQuantity_acceptedAndPersisted() {
        // Arrange
        Product zero = new Product(null, "Marker", 0);
        when(repo.save(any())).thenAnswer(inv -> inv.getArgument(0));

        // Act
        Product saved = service.create(zero);

        // Assert — state assertion
        assertEquals(0, saved.getQuantity(), "zero quantity is a valid boundary value");
        // Assert — interaction: repo.save must have been called exactly once
        verify(repo).save(any(Product.class));
    }

    @Test
    void create_nullName_throwsIllegalArgument() {
        assertThrows(IllegalArgumentException.class,
            () -> service.create(new Product(null, null, 1)),
            "null name must be rejected");
    }
}
```

**Integration test skeleton**:
```java
@WebMvcTest(ProductController.class)
class ProductControllerTest {

    @Autowired MockMvc mockMvc;
    @MockBean  ProductService service;  // stub the service layer

    private Product sampleProduct;

    @BeforeEach
    void setUp() {
        sampleProduct = new Product("p1", "Pen", 5);
    }

    @Test
    void getProductList_serviceReturnsProducts_rendersListViewWithData() throws Exception {
        // Arrange
        when(service.findAll()).thenReturn(List.of(sampleProduct));

        // Act + Assert (MockMvc fluent API merges act and assert)
        mockMvc.perform(get("/product/list"))
               .andExpect(status().isOk())
               .andExpect(view().name("productList"))
               .andExpect(model().attribute("products", hasSize(1)));
    }

    @Test
    void postCreateProduct_validInput_redirectsToList() throws Exception {
        // Arrange
        when(service.create(any())).thenReturn(sampleProduct);

        // Act + Assert
        mockMvc.perform(post("/product/create")
                   .param("productName", "Pen")
                   .param("productQuantity", "5"))
               .andExpect(status().is3xxRedirection())
               .andExpect(redirectedUrl("/product/list"));
    }
}
```

**Functional test skeleton**:
```java
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

    @Test
    void getProductListPage_pageLoads_rendersBody() {
        // Arrange — server started by @SpringBootTest

        // Act
        driver.get("http://localhost:" + port + "/product/list");

        // Assert
        WebElement body = driver.findElement(By.tagName("body"));
        assertNotNull(body, "page body must exist");
        assertFalse(body.getText().isBlank(), "page body must contain rendered content");
    }
}
```

**Coverage tool:** JaCoCo (bundled with `spring-boot-starter-test` ecosystem).

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
./gradlew test                          # unit + integration
./gradlew functionalTest                # Selenium E2E
./gradlew test jacocoTestReport         # run tests + generate HTML/XML report
./gradlew test jacocoTestCoverageVerification  # fail build if < 100% line or branch
./gradlew check                         # test + coverage verification (recommended for CI)
```

Coverage report is written to `build/reports/jacoco/test/html/index.html`.

---

### Python — Django (pytest + pytest-django + Selenium)

**Dependencies** (`requirements-test.txt` or `pyproject.toml`):
```
pytest
pytest-django
pytest-mock
selenium
webdriver-manager
factory-boy   # optional fixture factory helper
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
```

**Unit test skeleton**:
```python
# Use constructor injection — do NOT patch global module namespaces
from unittest.mock import MagicMock

def test_create_product_nonDuplicate_persistsAndReturns(mocker):
    # Arrange
    stub_repo = MagicMock()                         # stub: supply return values
    stub_repo.find_by_id.return_value = None        # no duplicate
    stub_repo.save.return_value = Product(id="p1", name="Pen", qty=5)
    svc = ProductService(repo=stub_repo)            # constructor injection

    # Act
    result = svc.create(Product(id="p1", name="Pen", qty=5))

    # Assert — state
    assert result.id == "p1"
    # Assert — interaction: save must have been called exactly once
    stub_repo.save.assert_called_once()

def test_create_product_nullName_raisesValueError():
    # Arrange
    stub_repo = MagicMock()
    svc = ProductService(repo=stub_repo)

    # Act + Assert
    with pytest.raises(ValueError, match="name"):
        svc.create(Product(id=None, name=None, qty=1))
```

**Integration test skeleton** (Django test client):
```python
@pytest.mark.django_db
def test_product_list_view(client):
    ProductFactory.create_batch(3)
    response = client.get("/products/")
    assert response.status_code == 200
    assert len(response.context["products"]) == 3
```

**Functional test skeleton**:
```python
@pytest.fixture(scope="module")
def browser():
    service = ChromeService(ChromeDriverManager().install())
    opts = ChromeOptions(); opts.add_argument("--headless")
    driver = webdriver.Chrome(service=service, options=opts)
    yield driver
    driver.quit()

def test_home_page_loads(browser, live_server):
    browser.get(live_server.url + "/")
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
Report is written to `htmlcov/index.html`.

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
def test_create_product_delegates_to_repository(mocker):
    mock_repo = mocker.MagicMock()
    service = ProductService(repo=mock_repo)
    service.create({"name": "Pen", "qty": 5})
    mock_repo.save.assert_called_once()
```

**Integration test skeleton**:
```python
def test_get_product_list_returns_200(client):
    response = client.get("/products")
    assert response.status_code == 200

def test_post_create_product_redirects(client):
    response = client.post("/products/create", data={"name": "Pen", "qty": "5"})
    assert response.status_code in (302, 303)
```

**Run commands**:
```bash
pytest -k "not functional"     # unit + integration
pytest -k "functional"         # Selenium only
pytest                         # all
```

**Coverage** (same `pytest-cov` as Django):
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
async def ac():
    async with AsyncClient(app=app, base_url="http://test") as client:
        yield client
```

**Unit test skeleton**:
```python
def test_get_item_calls_service(mocker):
    mock_svc = mocker.patch("myapp.routers.items.item_service")
    mock_svc.get.return_value = Item(id=1, name="Widget")
    # call the route function directly or via the async client
```

**Integration test skeleton**:
```python
@pytest.mark.asyncio
async def test_get_items_returns_200(ac):
    response = await ac.get("/items")
    assert response.status_code == 200
    assert isinstance(response.json(), list)

@pytest.mark.asyncio
async def test_create_item_returns_201(ac):
    response = await ac.post("/items", json={"name": "Widget", "price": 9.99})
    assert response.status_code == 201
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

**Unit test skeleton** (component):
```tsx
import { render, screen } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import ProductCard from "./ProductCard";

describe("ProductCard", () => {
  const product = { id: "p1", name: "Notebook", quantity: 5 };

  it("renders product name", () => {
    render(<ProductCard product={product} />);
    expect(screen.getByText("Notebook")).toBeInTheDocument();
  });

  it("calls onDelete with product id when button clicked", async () => {
    const onDelete = jest.fn();
    render(<ProductCard product={product} onDelete={onDelete} />);
    await userEvent.click(screen.getByRole("button", { name: /delete/i }));
    expect(onDelete).toHaveBeenCalledWith("p1");
  });
});
```

**Integration test skeleton** (service mock):
```tsx
jest.mock("../api/productApi");
import * as api from "../api/productApi";

it("displays products fetched from API", async () => {
  (api.fetchProducts as jest.Mock).mockResolvedValue([product]);
  render(<ProductList />);
  expect(await screen.findByText("Notebook")).toBeInTheDocument();
});
```

**Functional test skeleton** (Playwright):
```ts
import { test, expect } from "@playwright/test";

test("home page loads and shows nav", async ({ page }) => {
  await page.goto("/");
  await expect(page.locator("nav")).toBeVisible();
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
Report is written to `coverage/lcov-report/index.html`.

---

### Node.js — Next.js (Jest + React Testing Library + Playwright)

Same Jest / RTL setup as React.  Additional notes:

- Use `next/jest` config helper to resolve aliases and transform paths.
- For server components, use `@testing-library/react` with `renderToStaticMarkup` or test the underlying async function directly.
- Playwright config (`playwright.config.ts`): set `webServer.command = "next start"` and `baseURL`.
- Coverage: same `coverageThreshold` config as React above.

---

### Node.js — Vue (Vitest + Vue Test Utils + Playwright)

**Dependencies**:
```json
{
  "devDependencies": {
    "@vue/test-utils": "^2",
    "vitest": "^1",
    "jsdom": "^24",
    "@playwright/test": "^1"
  }
}
```

**Unit test skeleton**:
```ts
import { mount } from "@vue/test-utils";
import ProductCard from "./ProductCard.vue";

describe("ProductCard", () => {
  it("renders product name", () => {
    const wrapper = mount(ProductCard, { props: { name: "Pen", qty: 10 } });
    expect(wrapper.text()).toContain("Pen");
  });

  it("emits delete event on button click", async () => {
    const wrapper = mount(ProductCard, { props: { name: "Pen", qty: 10 } });
    await wrapper.find("button.delete").trigger("click");
    expect(wrapper.emitted("delete")).toBeTruthy();
  });
});
```

**Run commands**:
```bash
npx vitest run
npx playwright test
```

**Coverage** (Vitest built-in with `@vitest/coverage-v8`):
```bash
npx vitest run --coverage
```
Add to `vitest.config.ts` to enforce 100%:
```ts
coverage: {
  provider: 'v8',
  thresholds: { lines: 100, branches: 100, functions: 100, statements: 100 },
  reporter: ['text', 'html'],
},
```

---

### Rust — Rocket (built-in test + `reqwest` for integration)

**Dependencies** (`Cargo.toml`):
```toml
[dev-dependencies]
rocket = { version = "0.5", features = ["testing"] }
```

**Unit test skeleton**:
```rust
#[cfg(test)]
mod tests {
    use super::*;

    #[test]
    fn test_create_product_with_valid_data() {
        let product = Product::new("Pen", 10).unwrap();
        assert_eq!(product.name(), "Pen");
    }

    #[test]
    fn test_create_product_zero_quantity_is_err() {
        assert!(Product::new("Pen", 0).is_err());
    }
}
```

**Integration test skeleton** (Rocket test client):
```rust
use rocket::local::blocking::Client;
use rocket::http::Status;

#[test]
fn test_get_products_returns_200() {
    let client = Client::tracked(rocket()).expect("valid rocket");
    let response = client.get("/products").dispatch();
    assert_eq!(response.status(), Status::Ok);
}
```

**Run commands**:
```bash
cargo test                     # unit + integration
cargo test -- --ignored        # slow / E2E tests marked #[ignore]
```

**Coverage** (`cargo-tarpaulin`):
```bash
cargo install cargo-tarpaulin
cargo tarpaulin --out Html --branch --fail-under 100
```
Report is written to `tarpaulin-report.html`.

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
public class OrderServiceTests
{
    private readonly Mock<IOrderRepository> _repoMock = new();
    private readonly OrderService _sut;

    public OrderServiceTests() => _sut = new OrderService(_repoMock.Object);

    [Fact]
    public void CreateOrder_WithNewId_SavesAndReturnsOrder()
    {
        var order = new Order("o-001", new List<Product> { new() }, 1000, "alice");
        _repoMock.Setup(r => r.FindById("o-001")).Returns((Order?)null);
        _repoMock.Setup(r => r.Save(order)).Returns(order);

        var result = _sut.CreateOrder(order);

        Assert.Equal(order, result);
        _repoMock.Verify(r => r.Save(order), Times.Once);
    }

    [Fact]
    public void CreateOrder_WithDuplicateId_ReturnsNull()
    {
        _repoMock.Setup(r => r.FindById("o-001")).Returns(new Order());
        Assert.Null(_sut.CreateOrder(new Order { Id = "o-001" }));
    }
}
```

**Integration test skeleton** (WebApplicationFactory):
```csharp
public class ProductsControllerTests : IClassFixture<WebApplicationFactory<Program>>
{
    private readonly HttpClient _client;
    public ProductsControllerTests(WebApplicationFactory<Program> factory)
        => _client = factory.CreateClient();

    [Fact]
    public async Task GetProductList_Returns200()
    {
        var response = await _client.GetAsync("/product/list");
        Assert.Equal(HttpStatusCode.OK, response.StatusCode);
    }
}
```

**Functional test skeleton** (Selenium):
```csharp
public class HomePageFunctionalTests : IClassFixture<WebApplicationFactory<Program>>
{
    private readonly string _baseUrl;
    public HomePageFunctionalTests(WebApplicationFactory<Program> factory)
        => _baseUrl = factory.Server.BaseAddress.ToString();

    [Fact]
    public void HomePage_Loads()
    {
        var options = new ChromeOptions(); options.AddArgument("--headless");
        using var driver = new ChromeDriver(options);
        driver.Navigate().GoToUrl(_baseUrl);
        Assert.NotNull(driver.FindElement(By.TagName("body")));
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
Report is written to `TestResults/coverage.opencover.xml`. Use `reportgenerator` for HTML.

---

## Checklist (verify before finishing)

- [ ] Every public method / endpoint has at least one positive test
- [ ] Every method that can throw has at least one negative test
- [ ] Boundary values are tested (empty, zero, null, max)
- [ ] No test shares mutable state with another test
- [ ] No test has copy-pasted fixture construction — use `setUp` helpers
- [ ] All unit tests mock their dependencies
- [ ] Integration tests use an in-memory database or mock external services
- [ ] Functional tests run headlessly and clean up the browser after each test
- [ ] **Tests have been executed and pass before being committed**
- [ ] **Line coverage is 100%** — coverage report shows 0 missed lines
- [ ] **Branch coverage is 100%** — coverage report shows 0 missed branches
- [ ] Coverage verification task / threshold flag is wired into the CI pipeline so a drop in coverage fails the build automatically
