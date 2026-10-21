## Summary

<!-- What changes for the user or the code, and why. Link the issue it closes, e.g. "Closes #12". -->

## Type of change

- [ ] Feature (`feat`)
- [ ] Bug fix (`fix`)
- [ ] Refactor, no behavior change (`refactor`)
- [ ] Tests only (`test`)
- [ ] Documentation (`docs`)
- [ ] Build, dependencies or configuration (`build`, `chore`)

## Commits

<!-- One line per commit, in order. Reviewers read the pull request commit by commit. -->

1.

## Screenshots

<!-- For UI changes: before and after, light and dark. Delete this section otherwise. -->

## Test plan

- [ ] Each commit: `ktlintCheck`, `compileDevDebugKotlin`, `testDevDebugUnitTest`
- [ ] Branch tip: `ktlintCheck lintDevDebug testDevDebugUnitTest assembleDebug assembleProdRelease`
- [ ] Compose UI tests: `connectedDevDebugAndroidTest`
- [ ] Tried on a device or emulator against: <!-- local / staging -->

## Checklist

- [ ] New user-visible text is in `strings.xml`
- [ ] Works with TalkBack and large text (content descriptions, headings, touch targets)
- [ ] No secrets, tokens or personal data in code, logs or screenshots
- [ ] README or `docs/` updated if setup or architecture changed

## Notes for reviewers

<!-- Decisions worth a second look, backend or Keycloak changes this relies on, follow-ups. Delete if none. -->
