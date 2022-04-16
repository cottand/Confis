# Requirement Clauses

[Permission](PermissionClauses.md) allows to specify what parties are allowed to do, and as long as no one does something they are not allowed to do, we can say they are compliant with the contract

**Requirement** clauses allow to further add _conditions_ for compliance.
In order to be compliant, parties now need to
1. Only do things they are allowed to do
2. Do the things they are required to do

A requirement can be written as follows:

```kotlin
// specifies that bob must pay alice in the first week of June
bob must pay(alice) underCircumstances {
    within { (1 of June)..(7 of June) year 2022 }
}
```

## Syntax

Requirement clauses have the following grammar:

```kotlin
subject must action(thing)
//or
subject must action(thing) underCircumstances {
    circumstance1
        ...
    circumstanceN
}
```
