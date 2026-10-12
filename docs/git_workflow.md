# Git Workflow & Branching Strategy

**Project:** DevOps-Enabled Public Parking Availability Dashboard (ParkWatch)  
**Student:** Sajiya Shaikh | Roll No: 24102A2005  

---

## 1. Branching Strategy: Feature Branch Workflow

The project follows the standard Git Feature Branch Workflow to ensure that the `main` (or `master`) branch remains continuously deployable and stable.

```mermaid
gitGraph
    commit id: "Initial Commit"
    branch develop
    checkout develop
    commit id: "Setup Spring Boot & Maven"
    branch feature/dashboard-ui
    checkout feature/dashboard-ui
    commit id: "Add Thymeleaf templates & CSS"
    commit id: "Add responsive search & filter"
    checkout develop
    merge feature/dashboard-ui id: "Merge PR #1"
    branch feature/selenium-e2e
    checkout feature/selenium-e2e
    commit id: "Implement 7 Selenium browser tests"
    checkout develop
    merge feature/selenium-e2e id: "Merge PR #2"
    checkout master
    merge develop id: "Release v1.0.0"
    commit id: "Tag v1.0.0" tag: "v1.0.0"
```

### 1.1 Branch Naming Conventions
- `main` / `master`: Production-ready release branch. Always deployable.
- `develop`: Integration branch for aggregating validated feature branches.
- `feature/<feature-name>`: Dedicated branches for developing specific tasks (e.g., `feature/api-endpoints`, `feature/docker-pipeline`).
- `hotfix/<fix-name>`: Rapid patches branched directly from `main` to address critical production issues.

---

## 2. Standard Feature Lifecycle

### Step 1: Branch Creation
Always branch from the latest state of `develop`:
```bash
git checkout develop
git pull origin develop
git checkout -b feature/parking-alerts
```

### Step 2: Atomic, Meaningful Commits
Keep commits focused, adhering to the Conventional Commits specification:
```bash
git add src/main/java/com/parking/dashboard/service/ParkingLocationService.java
git commit -m "feat(service): add automated capacity saturation alerts"
```

### Step 3: Local Quality Gate Validation
Before pushing or opening a Pull Request, verify that all tests pass locally:
```bash
mvn clean test
mvn package
```

### Step 4: Pushing and Pull Request
```bash
git push -u origin feature/parking-alerts
```
Open a Pull Request targeting `develop`. The automated Jenkins CI pipeline will trigger, run test suites, and report build status.

---

## 3. Merge Conflict Resolution Guide

When multiple branches modify the same files (e.g., `pom.xml` or `application.properties`), merge conflicts may arise. Follow this deterministic procedure:

1. **Rebase or Merge develop into feature branch:**
   ```bash
   git checkout feature/parking-alerts
   git fetch origin
   git merge origin/develop
   ```
2. **Identify Conflicted Files:**
   ```bash
   git status
   ```
   *Git highlights conflicted files as "both modified".*

3. **Inspect and Resolve Markers:**
   Open the conflicted file. Inspect the conflict markers:
   ```text
   <<<<<<< HEAD
   spring.application.name=Public Parking Dashboard
   =======
   spring.application.name=ParkWatch Public Parking Service
   >>>>>>> develop
   ```
   Choose the correct integrated solution and delete all marker lines (`<<<<<<<`, `=======`, `>>>>>>>`).

4. **Verify Build and Finalize Merge:**
   ```bash
   mvn clean test
   git add <resolved-files>
   git commit -m "chore: resolve merge conflict between develop and feature/parking-alerts"
   git push origin feature/parking-alerts
   ```

---

## 4. Release Tagging Strategy

Releases follow **Semantic Versioning 2.0.0** (`MAJOR.MINOR.PATCH`):
- `v1.0.0`: Initial comprehensive academic release with all 15 tasks implemented and validated.

### Creating an Annotated Release Tag:
```bash
git checkout master
git tag -a v1.0.0 -m "Release v1.0.0: Full DevOps implementation with Spring Boot 3.3, Selenium E2E, Docker, Jenkinsfile, and Ansible automation"
git push origin v1.0.0
```

### Verifying Git Tags:
```bash
git tag -n
git show v1.0.0
```
