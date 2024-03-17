# EECS 4443 - Final Project

## Using [Issues](https://github.com/ksmarty/EECS-4443-Final-Project/issues)

- Use the labels. Makes prioritization easier
- Assign yourself to an issue if you're working on it
- Try and reference the issue in your PR (optional but good practice)
- Close the issue when you're done

## Contribution Steps

- Create a new branch with the name `feature/[feature-name]` or `patch/[patch-name]`
  - E.g. `feature/connect-backend` or `patch/typos`
- Open a PR against main & get at least 1 review
- Squash & Merge the change

### Accidentally based on main

If you accidentally wrote your code on main and now can't push, run the [following commands](https://medium.com/@petehouston/git-move-unpushed-changes-to-a-new-branch-983eea7af741):

- git checkout -b YOUR_BRANCH_NAME
- git push -u origin YOUR_BRANCH_NAME

Or push the corresponding buttons in Android Studio:

- New Branch
- (Enter your branch name)
- Push...
