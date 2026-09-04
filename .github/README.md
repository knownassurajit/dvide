# CI/CD (`dvide/.github`)

```text
.github/
├── dependabot.yml
├── workflows/ci-cd.yml
└── README.md
```

## Branching

- `develop` — integration
- `master` — production
- `release/dvide/<version>` — rollback snapshot

## Jobs

| Job | Trigger | Purpose |
|---|---|---|
| `test` | push to develop/master, PRs into master | compile, unit tests, lint |
| `dependency-review` | PRs | high-severity advisory gate |
| `debug-release` | push to develop | debug APK pre-release |
| `pr-summary` | PRs into master | sticky comment with results |
| `stable-release` | push to master | signed APK/AAB, GitHub release, optional Play internal |

Actions are SHA-pinned. Keystore is shredded after signing.
