# Team Git Workflow Plan

## 1. Workflow and Rationale

**Chosen workflow : Forking workflow.**

Our workflow is the forking workflow. It’s the one that works best for our team. All the developers on our team work locally on their personal computers. These versions are then pushed and merged into the AymericGe/The-Diversity-Hires page, which is our team page. All changes are then sent via a pull request to the main project: oh-gnues/Invaders-SDP-24592

## 2. The Branch Strategy

Each developer creates a fork of the team repository (AymericGe/The-Diversity-Hires). Each developer works on their fork in their personal workspace. They make their code changes. Then, the team developers submit a pull request to the team repository. The team then merges the code from the pull request. Once this is done, the team leader submits a pull request to the main repository (oh-gnues/Invaders-SDP-24592).
It is a three-tier architecture: the global repository, the team repository, and the developers' personal forks.

## 3. Committing Rules

- A commit should represent **one logical change** — e.g. one bug fix, one small feature step, or one refactor. Avoid mixing unrelated changes (e.g. a UI fix and a backend refactor) in the same commit.
- Commit message format follows **Conventional Commits**:

  ```
  <type>(<optional scope>): <short summary>

  <optional longer description>
  ```

  Types used: `feat`, `fix`, `refactor`, `docs`, `test`, `chore`, `style`.

  Examples:
  - `feat(auth): add password reset flow`
  - `fix(api): handle empty response from user endpoint`
  - `docs: update setup instructions in README`

- Summary line is written in the imperative mood ("add", not "added"/"adds"), kept under ~72 characters, with more detail in the body if needed.
- Commit early and often on your feature branch — commits don't need to be squashed until merge (see Section 5).

## 4. Pull Request and Code Review Rules

**When a pull request is opened**
- A PR is opened when a developer did a change on their own personal fork.
- The PR description states what the change does and links the related task/issue.

**Review and approval conditions before merging**
- At least **one approval** from a teammate other than the author is required before a PR can be merged. No self-approval.
- Reviewers check: the code works as intended, is reasonably readable, and follows the conventions in this document.
- Reviewers leave comments within 24 hours of a PR being opened where possible; the author addresses comments before merge.
- Any CI checks (build/tests, once set up) must pass before merging.

**Direct pushes to `main`**
- **Not allowed.** `main` is a protected branch — every change, including small fixes like typos, must go through a PR.

## 5. The Merge Strategy

**Method: Squash and Merge**, used for all PRs into `main`. Each feature/fix collapses into a single, clean commit on `main`, using the PR title (written in Conventional Commit format) as the commit message.

- Why squash: our feature branches accumulate small "WIP"/fixup commits during development. Squashing keeps `main`'s history readable as one entry per feature/fix, while the full commit history is still visible on the closed PR for reference.

**Conflict resolution**
- If a conflict touches code owned by another teammate, the author contacts them directly to resolve it together rather than guessing at intent.
- PR should be made regularly (at least before requesting review) to minimize last-minute conflicts.

## 6. Overall Development Workflow

**Step by step:**
1. Pick up a task from the team board.
2. Do the edits on you own fork.
3. Commit small, logical changes following the format in Section 3.
4. Open a PR (draft if work-in-progress).
5. Get at least one teammate's approval; address review comments.
6. Resolve any conflicts with `main` before merging.
7. Squash and merge into `main`
8. Pull the latest `main` locally before starting the next task.

## Additional Team Rules

- No force-pushing to `main`, ever.
- Broken builds on `main` are treated as top priority — whoever caused it fixes it immediately or reverts the merge.
- Any change to this workflow document must itself go through a PR and be agreed on by the whole team.
