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
| **F**ast | Each test must run in isolation without network calls, file I/O, or sleeping.  Use in-memory fakes or mocks for slow dependencies. |
| **I**solated | Tests must not share mutable state.  Use `@BeforeEach` / `setUp()` / `pytest.fixture` to rebuild fresh fixtures for every single test. |
| **R**epeatable | Tests must produce identical results on every run, on every machine, at any time.  No random data, no `new Date()`, no hardcoded ports. |
| **S**elf-validating | Every test must end with at least one assertion.  A test with no assertion is not a test. |
| **T**imely | Tests should be written alongside (or immediately after) the production code, not as an afterthought. |

### DRY (Don't Repeat Yourself)
- Extract shared object-construction logic into a `setUp` / `fixture` method or a factory helper.
- Never copy-paste the same `new MyObject(...)` call across multiple tests.
- Use parametrised tests (`@ParameterizedTest`, `pytest.mark.parametrize`, `test.each`, etc.) when the same assertion logic applies to multiple input values.

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
- **Scope:** One class / function at a time.
- **Mocking:** Mock **every** external dependency (repositories, HTTP clients, database sessions, message brokers, etc.) using the framework's standard mock library.
- **Coverage per method:**
  - ✅ Positive case — typical valid input, expected output
  - ❌ Negative case — invalid/malformed input, expected exception or error response
  - 🔲 Edge case — boundary values (empty list, zero, `null`, max-int, empty string, single element, exact threshold)

### 2. Integration Tests
- **Scope:** One slice of the application (web layer, persistence layer) wired together with the framework's test support.
- Use an in-memory database (H2, SQLite `:memory:`, `mongomock`, etc.) and a mocked HTTP client layer so tests remain self-contained.
- Test that the components **interact correctly** (routes resolve, middleware fires, ORM queries return expected rows, etc.).
- Every HTTP-level test must assert: **status code**, **response body / view name**, and **relevant model attributes or JSON fields**.

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
@ExtendWith(MockitoExtension.class)
class MyServiceTest {
    @Mock MyRepository repo;
    @InjectMocks MyServiceImpl service;

    @BeforeEach void setUp() { /* build fixtures */ }

    @Test void testPositiveCase() { /* when/then */ }
    @Test void testNegativeCase() { assertThrows(...); }
    @Test void testEdgeCase()    { /* boundary */ }
}
```

**Integration test skeleton**:
```java
@WebMvcTest(MyController.class)
class MyControllerTest {
    @Autowired MockMvc mockMvc;
    @MockBean  MyService service;

    @Test void testGetEndpoint() throws Exception {
        mockMvc.perform(get("/my-route"))
               .andExpect(status().isOk())
               .andExpect(view().name("myView"));
    }
}
```

**Functional test skeleton**:
```java
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class MyFunctionalTest {
    @LocalServerPort int port;
    WebDriver driver;

    @BeforeEach void setUp() {
        WebDriverManager.chromedriver().setup();
        ChromeOptions opts = new ChromeOptions();
        opts.addArguments("--headless", "--no-sandbox");
        driver = new ChromeDriver(opts);
    }
    @AfterEach  void tearDown() { driver.quit(); }

    @Test void testPageLoads() {
        driver.get("http://localhost:" + port + "/");
        assertNotNull(driver.findElement(By.tagName("body")));
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
# pytest-mock provides the `mocker` fixture — no class needed
def test_create_order_returns_order(mocker):
    mock_repo = mocker.patch("myapp.services.OrderRepository")
    mock_repo.return_value.save.return_value = Order(id=1)
    svc = OrderService(repo=mock_repo.return_value)

    result = svc.create(Order(id=1))

    assert result.id == 1
    mock_repo.return_value.save.assert_called_once()
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
