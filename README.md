# Salesforce State & Country/Territory Loader

Selenium + TestNG automation that bulk-loads states and provinces into Salesforce's **State and Country/Territory Picklists** from an Excel file.

## Why this exists

In Salesforce Setup, states are added one at a time: click **New State**, fill in the name and ISO code, tick Active and Visible, save, repeat. For a long list of countries and states this is slow and easy to get wrong.

This project does the same clicks for you. Give it an Excel sheet, run one command, and it adds every missing state, skips the ones that already exist, and reports anything Salesforce rejects.

## What it does

For each country in the sheet:

1. Opens **Configure States, Countries, and Territories** in Salesforce Setup.
2. Opens that country's edit page and reads the states already there.
3. For every state in the sheet:
   - **Already exists** → logs a warning and skips it.
   - **New** → clicks *New State*, fills the form, and saves.
   - **Salesforce shows an error** (for example a duplicate value) → logs the message, cancels the form, and carries on with the next state.
4. Returns to the country list and moves to the next country.

Runs are safe to repeat: states that were already added are skipped, so a re-run or a retry won't create duplicates.

Every run produces an HTML report (with a screenshot on failure) and a log file.

## Tech stack

| Area | Tool |
|---|---|
| Language / build | Java 17, Maven |
| Browser automation | Selenium 4 (Chrome or Edge, headless supported) |
| Test runner | TestNG (data provider, listeners, retry) |
| Excel input | Apache POI |
| Logging | Log4j2 (console + rolling file) |
| Reporting | ExtentReports |

## How it's built

```
Test            what to run: read the Excel file, log in, start the flow
  └── Flow      the loop and the decisions (skip existing, handle errors)
        └── Pages     one screen each: CountryListPage, StateListPage, NewStatePage
              └── Core / Utils     driver, config, waits, Excel reader
```

- **Page Object Model.** Locators and screen actions live in page classes only. The flow never touches a `By` or a `WebElement`.
- **Thread-safe driver.** `DriverManager` keeps one `WebDriver` per thread, so the framework is ready for parallel runs.
- **Iframe-aware.** Salesforce reloads the Setup iframe after every save. `FrameHandler` waits for the reload, and `NewStatePage.save()` detects success or failure by watching whether the old frame goes stale or an error message appears.
- **Results as data.** `save()` returns a `SaveResult` (success flag plus the error message), so the flow decides what to do.
- **No secrets in the repo.** Credentials come from environment variables or `-D` options.

### Project structure

```
src/
├── main/java/
│   ├── core/         ConfigReader, DriverFactory, DriverManager
│   ├── models/       StateData, SaveResult
│   ├── pages/        BasePage, common/LoginPage, address/*, components/FrameHandler
│   ├── flows/        StateLoadFlow
│   ├── reporting/    ExtentManager
│   └── utils/        ExcelDataReader, WaitUtils, ScreenshotUtils
├── main/resources/   config.properties, log4j2.xml
└── test/
    ├── java/         base/BaseTest, listeners/*, tests/state/StateLoadTest
    └── resources/    suites/smoke.xml, testdata/states_sample.xlsx
```

## Prerequisites

- JDK 17 or newer
- Maven 3.9 or newer
- Google Chrome (or Microsoft Edge). Selenium Manager downloads the matching driver automatically.
- A Salesforce org where you can open **Setup → State and Country/Territory Picklists**. Use a **sandbox or Developer Edition org first**, because the tool creates real records and has no undo.

## Quick start

```bash
git clone https://github.com/<your-username>/sf-state-territory-loader.git
cd sf-state-territory-loader
```

**1. Get the page URL.** In Salesforce Setup, open *State and Country/Territory Picklists → Configure States, Countries, and Territories* and copy the address from your browser.

**2. Set your credentials as environment variables** (never put them in the repo):

```bash
export SF_URL='<the URL you copied>'
export SF_USERNAME='you@example.com'
export SF_PASSWORD='your-password'
```

On Windows PowerShell:

```powershell
$env:SF_URL='...'; $env:SF_USERNAME='...'; $env:SF_PASSWORD='...'
```

Use single quotes on macOS/Linux, because URLs and passwords often contain `&`, `%`, `@` or `!`.

**3. Run:**

```bash
mvn test
```

Add `-Dheadless=true` to run without a visible browser.

## Input file

The sheet is read from `src/test/resources/testdata/states_sample.xlsx` (sheet name `Sheet1`). The first row is a header and is skipped.

| Country | Region | Description |
|---|---|---|
| IN | KA | Karnataka |
| IN | TN | Tamil Nadu |
| US | CA | California |

- **Country**: the country's ISO code, exactly as it appears in the Salesforce country list
- **Region**: the state's ISO/code value (typed into the *ISO Code* field)
- **Description**: the state's name (typed into the *Name* field)

To use your own file, replace the sample or add its path to the `@DataProvider` in `StateLoadTest`. Each file in the provider runs as its own test with its own browser session.

## Configuration

Values are looked up in this order: `-D` option → environment variable → `config.properties`.

| Key | Env var | Default | Meaning |
|---|---|---|---|
| `url` | `SF_URL` | none (required) | Setup page URL |
| `username` | `SF_USERNAME` | none (required) | Salesforce login |
| `password` | `SF_PASSWORD` | none (required) | Salesforce password |
| `browser` | `SF_BROWSER` | `chrome` | `chrome` or `edge` |
| `headless` | `SF_HEADLESS` | `false` | Run without a visible browser |
| `explicitTimeout` | | `15` | Normal wait, in seconds |
| `longTimeout` | | `45` | Wait for a save to complete, in seconds |
| `retryCount` | | `1` | How many times a failed test is retried |

Examples:

```bash
mvn test -Dheadless=true
mvn test -Dbrowser=edge
mvn test -Dsuite=smoke -DlongTimeout=60
```

## Output

- **Report:** `reports/Test-Execution-Report.html`, with a screenshot attached to any failure
- **Log:** `logs/automation.log`, with each line tagged by time, thread, test name, level and class
- Both folders are git-ignored and created on the first run.

## Troubleshooting

- **`Missing 'url'` (or username/password).** The environment variable isn't set in the terminal you're running from. Set it, or pass `-Durl=...`.
- **Salesforce asks for a verification code (OTP) or MFA.** Each Selenium run starts with a fresh browser profile, so Salesforce treats it as a new device. Add your IP under *Setup → Network Access → Trusted IP Ranges*, or use an org/user that isn't challenged. Token-based login is on the roadmap below.
- **Timeouts on save.** Increase `longTimeout`. Large orgs can reload the Setup frame slowly.
- **Element not found after a Salesforce release.** The locators are in the page classes under `pages/address`. Update them in one place.

## Known limitations and roadmap

- Login is by username and password only. Token login (`frontdoor.jsp` with an OAuth token) is planned for orgs that require MFA.
- Only adds states. It doesn't edit or delete existing ones.
- Planned: Docker image, GitHub Actions pipeline with secrets, Selenium Grid support, and more detailed per-country results in the report.

## Notes

This tool changes data in your Salesforce org. Try it on a sandbox, start with a small file, and check the result in Setup before running a full list.
