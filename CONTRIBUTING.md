# Contributing to Annotation Command Framework

## The Golden Rule: Keep it small, work incrementally!

Unlike most other projects that won't take your PR every possible color of kitten can be considered, ACF prefers that
you keep your PR's small, and work incrementally.

Try to complete work to a small enough unit that it is mergeable.

There are plenty of large systems in ACF that has been built in small units. contributors are welcome to help build the
larger systems that power ACF by contributing small units of the feature.

The larger a PR becomes, the more time it takes to review, and the bigger risk of change comes. Keep it simple!

Leave code in a package-private access level, and then it will be promoted to public once we are ready to open it up for
use.

## Keep major formatting and/or docs changes to existing code as completely separate PR's, but again, incremental

Please keep formatting improvements and documentation to existing code (meaning, code that is not part of your own
feature API)
as standalone PR's. In other words, don't submit a PR that changes behavior or adds features, but also changes 10 other
files unrelated to the PR just to improve things.

Keep them separate, so they can be reviewed and pulled separately.

Additionally, keep them small and incremental!

## Discuss with Aikar before starting any major behavior change / annotation addition

Before you go spending a lot of time on a PR that isn't resolving an issue that Aikar filed, please consult with Aikar
on Discord or IRC about the idea, to ensure it fits well with the projects goals.

ACF is moving to a very extensible goal for the 1.0.0 release, with powerful custom annotation support.

I want to avoid adding new stuff that will then be quickly deprecated when the more preferred approach comes out.

## Translations

New messaging should add respective english Message Properties. if you know some other languages already translated,
feel free to go ahead and add the translations for your new feature too. English is the only required language for a new
feature.

Translators, I am leaving it up to the community to review and vet translations. If you see some bad translations,
please submit a PR to correct them!

Additionally, if you help translate messages for ACF, please follow the repo and keep up with new commits and watch for
new messages, to keep the non english languages caught up.


