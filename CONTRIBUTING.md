# Contributing to Echno Android

Thanks for helping. This guide covers how work moves from a branch to `development`. Read the
[README](README.md) first to get the app building, and [docs/architecture.md](docs/architecture.md)
before adding a feature.

## Branches

* `development` is the integration branch. Nobody commits to it directly; every change arrives
  through a pull request.
* Start each piece of work on its own branch from an up-to-date `development`:

  ```bash
  git switch development
  git pull --rebase
  git switch -c feature/projects-search
  ```

* Name the branch by what it does: `feature/…`, `fix/…`, `docs/…`, `chore/…`.
* Keep a branch to one topic. Several small pull requests are reviewed faster than one large one.

## Commits

Make small commits, each one a step that **compiles and passes ktlint and the unit tests on its own**.
Reviewers read a pull request commit by commit, and `git bisect` relies on every commit working.

Write the subject as `type: what changes, for whom`, in the imperative and lower case:

```text
feat: open a project's details from the list
fix: don't sign straight into an organization that is inactive
test: cover the profile screen with Compose UI tests
```

| Type | Use for |
|---|---|
| `feat` | Something a user can now do or see |
| `fix` | A bug fix |
| `test` | Tests only |
| `refactor` | A code change that keeps behavior the same |
| `style` | Wording, formatting or visual polish without new behavior |
| `docs` | Documentation |
| `build` | Gradle, dependencies, build variants |
| `chore` | Configuration and housekeeping |

Use the body to explain *why* when the diff doesn't make it obvious. To fix an earlier commit on your
branch, prefer `git commit --fixup <commit>` and `git rebase --autosquash development` over a
"fix review comments" commit.

## Checks

Run these before opening or updating a pull request:

```bash
./gradlew ktlintCheck lintDevDebug testDevDebugUnitTest assembleDebug assembleProdRelease
./gradlew connectedDevDebugAndroidTest   # with an emulator or phone connected
```

To run ktlint, the Kotlin compile and the unit tests on every commit of the branch (the full checks
above still run on the branch tip):

```bash
git rebase --exec "./gradlew ktlintCheck compileDevDebugKotlin testDevDebugUnitTest" development
```

If the device tests fail with "No compose hierarchies found", the emulator's screen is probably off.
Wake it, and keep it awake while it is plugged in:

```bash
adb shell input keyevent KEYCODE_WAKEUP
adb shell svc power stayon true
```

## Code style

* ktlint enforces the [Android Studio style](.editorconfig) with a 140-character line limit.
  `./gradlew ktlintFormat` fixes most findings.
* Follow the patterns already in the code; [docs/architecture.md](docs/architecture.md) describes them.
* Put every user-visible text in `res/values/strings.xml`, never in code. Buttons and field labels use
  title case ("Create Account"); messages are sentences.
* Give icons and images a `contentDescription`, or `null` when they are decorative.
* Comment the *why* of code that isn't obvious. Don't restate what the code says.
* Never log tokens, passwords or personal data.

## Tests

Every change comes with tests at the level it touches:

| What changed | Test with | Lives in |
|---|---|---|
| Repository or DTO mapping | JUnit and MockWebServer | `app/src/test` |
| ViewModel | JUnit, `MainDispatcherRule` and a fake repository | `app/src/test` |
| Screen | A Compose UI test of the stateless `…Content` composable | `app/src/androidTest` |

## Pull requests

1. Push your branch and open a pull request into `development`. The template asks for a summary,
   the commits and how you tested.
2. CodeRabbit reviews automatically. Answer each comment: fix it with a fixup commit, or reply with
   why not.
3. A code owner must approve ([CODEOWNERS](.github/CODEOWNERS)).
4. The pull request is **rebase-merged**, which keeps `development`'s history linear and every commit
   in it. That's why each commit must build.
5. Merged branches are kept, not deleted.

## Secrets and private files

The repository is public.

* Never commit `local.properties`, keystores, `.env` files, tokens or passwords. `.gitignore`
  already excludes the usual ones.
* Commit only fonts, images and other assets whose license allows redistribution in a public repository.
* `local-docs/` is ignored for private notes and drafts.

## Issues

Open an issue with the form that fits:

| Form | For |
|---|---|
| Bug report | Something in the app doesn't work as it should |
| Feature request | Something the app should do |
| Accessibility problem | Hard or impossible to use with TalkBack, large text or another assistive setting |
| Documentation request | Missing, wrong or unclear documentation |
| Task | Planned work that isn't a bug or a feature: a refactor, a dependency update, tooling |
| Question | Building, running or working on the app |

Problems with the server's data or errors belong in the
[backend repository](https://github.com/tornotron/echno-backend/issues). Security issues go through
[SECURITY.md](SECURITY.md), never a public issue.
