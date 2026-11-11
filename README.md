# Salesforce State & Country/Territory Loader

Selenium + TestNG automation that bulk-loads states and provinces into Salesforce's **State and Country/Territory Picklists** from an Excel file. It can run several countries in parallel, either as local browsers or as browser containers on a Selenium Grid in Docker.

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
4. Returns to the country list.

Runs are safe to repeat: states that were already added are skipped, so a re-run or a retry won't create duplicates.

Every run produces an HTML report (with a screenshot on failure) and a log file.

## Tech stack

| Area | Tool |
|---|---|
| Language / build | Java 17, Maven |
| Browser automation | Selenium 4 (Chrome or Edge, headless supported) |
| Parallel browsers | Selenium Grid in Docker (Docker Compose, Chromium nodes) |
| Test runner | TestNG (data provider, listeners, retry) |
| Excel input | Apache POI |
| Logging | Log4j2 (console + rolling file) |
| Reporting | ExtentReports |

## How it's built

```
Test            what to run: read the Excel file, start one test per country
  └── Flow      the loop and the decisions (skip existing, handle errors)
        └── Pages     one screen each: CountryListPage, StateListPage, NewStatePage
              └── Core / Utils     driver, config, session, waits, Excel reader
```

- **Page Object Model.** Locators and screen actions live in page classes only. The flow never touches a `By` or a `WebElement`.
- **Thread-safe driver.** `DriverManager` keeps one `WebDriver` per thread, so each country gets its own browser.
- **One test per country.** The data provider turns the Excel sheet into one row per country and runs them in parallel.
- **Browser limit.** `BaseTest` uses a semaphore so no more than `parallelism` browsers are open at once. This works the same from an IDE, Maven or CI. (TestNG's `data-provider-thread-count` in the suite file is honoured when TestNG runs directly, but Maven Surefire does not apply it, hence the semaphore.)
- **Local or Grid.** `DriverFactory` starts a local Chrome or Edge. If `gridUrl` is set, it connects to a Selenium Grid instead and wraps the remote driver with Selenium's `Augmenter`, so the session loader can send Chrome DevTools commands.
- **Iframe-aware.** Salesforce reloads the Setup iframe after every save. `FrameHandler` waits for the reload, and `NewStatePage.save()` detects success or failure by watching whether the old frame goes stale or an error message appears.
- **Results as data.** `save()` returns a `SaveResult` (success flag plus the error message), so the flow decides what to do.
- **No secrets in the repo.** Credentials come from environment variables or `-D` options. A saved session lives in a git-ignored folder.

### Project structure

```
docker-compose.yml        Selenium Grid: one hub and scalable Chromium nodes
src/
├── main/java/
│   ├── core/         ConfigReader, DriverFactory, DriverManager, SessionLoader
│   ├── models/       StateData, SaveResult
│   ├── pages/        BasePage, common/LoginPage, address/*, components/FrameHandler
│   ├── flows/        StateLoadFlow
│   ├── reporting/    ExtentManager
│   └── utils/        ExcelDataReader, WaitUtils, ScreenshotUtils
├── main/resources/   config.properties, log4j2.xml
└── test/
    ├── java/         base/BaseTest, listeners/*, tests/state/StateLoadTest,
    │                 tools/SessionCapture
    └── resources/    suites/smoke.xml, testdata/states_sample.xlsx
```

## Prerequisites

- JDK 17 or newer
- Maven 3.9 or newer
- Google Chrome (or Microsoft Edge) for local runs. Selenium Manager downloads the matching driver automatically.
- Docker Desktop, only if you want to run on the Grid.
- A Salesforce org where you can open **Setup → State and Country/Territory Picklists**. Use a **sandbox or Developer Edition org first**, because the tool creates real records and has no undo.

## Quick start

```bash
git clone https://github.com/<your-username>/sf-state-territory-loader.git
cd sf-state-territory-loader
```

**1. Get the page URL.** In Salesforce Setup, open *State and Country/Territory Picklists → Configure States, Countries, and Territories* and copy the address from your browser.

**2. Set the URL** as an environment variable:

```bash
export SF_URL='<the URL you copied>'
```

On Windows PowerShell: `$env:SF_URL='...'`. Use single quotes on macOS/Linux, because URLs and passwords often contain `&`, `%`, `@` or `!`.

**3. Choose how to log in.**

*Option A: username and password* (orgs that don't ask for a verification code):

```bash
export SF_USERNAME='you@example.com'
export SF_PASSWORD='your-password'
```

*Option B: a saved session* (orgs that ask for MFA or a verification code). See [Logging in with a saved session](#logging-in-with-a-saved-session).

**4. Run:**

```bash
mvn test -Dsuite=smoke
```

Add `-Dheadless=true` to run without a visible browser.

## Logging in with a saved session

Selenium starts every browser with a fresh profile, so Salesforce may ask for a verification code each time. Instead of automating that step, you log in **once by hand** and the tests reuse that session.

1. Set `SF_URL`, then run `tools.SessionCapture` as a Java application from your IDE (in Eclipse: *Run As → Java Application*; add `SF_URL` on the *Environment* tab of its run configuration).
2. A Chrome window opens. Log in by hand, including any verification code. When the State and Country page is showing, click in the IDE console and press Enter.
3. The Salesforce cookies are saved to `session/cookies.json`.
4. Run the tests as usual. While that file exists, the login page is skipped and every browser starts already logged in. Delete the file to go back to username and password.

Things to know:

- **The cookie file is a login.** Anyone who has it is signed in as you until the session expires. It lives in `session/`, which is git-ignored. Never commit, email or share it, delete it when you're done, and log out of the Salesforce session afterwards.
- **Sessions expire**, after a few hours by default (the org's session timeout). If tests start landing on the login page, run `SessionCapture` again. A re-run is safe, because existing states are skipped.
- Salesforce may lock a session to the IP address it started from. Capture and run from the same machine or network.

## Running in parallel

The data provider creates one test per country, so a sheet with five countries can use up to five browsers. `parallelism` (default `3`) caps how many are open at once.

```bash
mvn test -Dsuite=smoke -Dparallelism=3
```

The most you can gain is limited by the number of countries in the sheet, and the run lasts as long as the slowest country.

## Running on a Selenium Grid in Docker

`docker-compose.yml` defines a Selenium hub and Chromium node containers. Each node runs one browser session. The tests run on your machine and ask the hub for browsers.

```bash
# start the hub and three browser containers
docker compose up -d --scale chromium=3

# check the Grid: http://localhost:4444/ui/   (sessions and a live view are shown there)

# run the tests against it; keep parallelism equal to the number of containers
export SF_GRIDURL=http://localhost:4444
mvn test -Dsuite=smoke -Dparallelism=3

# stop everything
docker compose down
```

How it fits together:

- `--scale chromium=3` is the number of browser containers. Browsers available at once = containers × `SE_NODE_MAX_SESSIONS` (set to 1 in the compose file).
- `SE_NODE_GRID_URL=http://localhost:4444` makes the Grid advertise an address your machine can reach for the DevTools websocket. Without it, the session starts and then hangs, because the default address is only valid inside Docker's private network.
- `shm_size: 2gb` stops Chromium crashing from Docker's small default shared memory.
- The window size is set with `--window-size=1920,1080` on the Grid, instead of maximizing.
- Each Chromium container needs roughly 1 GB of memory and a CPU core when busy. Check `docker stats`, and the memory limit in Docker Desktop's *Settings → Resources*, before adding more.
- Use spaces, not tabs, when editing `docker-compose.yml`.

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

To use your own file, replace the sample or change the path in the `@DataProvider` in `StateLoadTest`. Each country in the sheet runs as its own test with its own browser session.

## Configuration

Values are looked up in this order: `-D` option → environment variable → `config.properties`.

| Key | Env var | Default | Meaning |
|---|---|---|---|
| `url` | `SF_URL` | none (required) | Setup page URL |
| `username` | `SF_USERNAME` | none | Salesforce login (not needed with a saved session) |
| `password` | `SF_PASSWORD` | none | Salesforce password (not needed with a saved session) |
| `browser` | `SF_BROWSER` | `chrome` | `chrome` or `edge` |
| `headless` | `SF_HEADLESS` | `false` | Run without a visible browser |
| `gridUrl` | `SF_GRIDURL` | empty (local browser) | Selenium Grid address, for example `http://localhost:4444` |
| `parallelism` | `SF_PARALLELISM` | `3` | Maximum browsers open at once |
| `explicitTimeout` | | `15` | Normal wait, in seconds |
| `longTimeout` | | `45` | Wait for a save to complete, in seconds |
| `retryCount` | | `1` | How many times a failed test is retried |

Examples:

```bash
mvn test -Dsuite=smoke -Dheadless=true
mvn test -Dsuite=smoke -Dbrowser=edge
mvn test -Dsuite=smoke -Dparallelism=2 -DlongTimeout=60
```

## Output

- **Report:** `reports/Test-Execution-Report.html`, with a screenshot attached to any failure
- **Log:** `logs/automation.log`, with each line tagged by time, thread, test name (including the country code), level and class. The file rolls over at 5 MB into compressed archives.
- These folders are git-ignored and created on the first run.

## Troubleshooting

- **`Missing 'url'`.** The environment variable isn't set in the terminal (or IDE run configuration) you're running from. Set it, or pass `-Durl=...`.
- **Salesforce asks for a verification code (OTP) or MFA.** Use a [saved session](#logging-in-with-a-saved-session). Adding your IP under *Setup → Network Access → Trusted IP Ranges* can also help, but it may not remove an MFA prompt.
- **The browser lands on the login page even with a saved session.** The session expired or was rejected. Run `SessionCapture` again. The log line `Loaded N of N saved cookies` shows how many cookies the browser accepted.
- **Stuck at `Starting browser` on the Grid.** Check that `SE_NODE_GRID_URL` is set in the compose file, that `curl http://localhost:4444/status` reports `"ready": true`, and that `parallelism` is not higher than the number of containers. A failed start can hold a node's only session until it times out; `docker compose restart chromium` clears it.
- **`port is already allocated`.** Another container is using port 4444, or you mapped one fixed host port on the scaled `chromium` service. Stop the other container or remove the mapping.
- **Timeouts on save.** Increase `longTimeout`. Large orgs can reload the Setup frame slowly.
- **Element not found after a Salesforce release.** The locators are in the page classes under `pages/address`. Update them in one place.

## Known limitations and roadmap

- Only adds states. It doesn't edit or delete existing ones.
- Login is either username and password or a saved browser session. Token login (`frontdoor.jsp` with an OAuth token) would avoid the manual step.
- Planned: a dry-run mode that reports what would be added without saving anything, a per-state results file, running the tests inside a container, and a GitHub Actions pipeline with secrets.

## Notes

This tool changes data in your Salesforce org and has no undo. Try it on a sandbox, start with a small file, and check the result in Setup before running a full list. Get approval from the org owner before running it against a live org, and use the account they agree to.
