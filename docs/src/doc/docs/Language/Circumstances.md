# Circumstances

Circumstances allow narrowing down when exactly a
[clause](PermissionClauses.md) applies.
For where cirucmstances can be used, take a look at the different types of clauses, such as [Permission](PermissionClauses.md) and [Requirement](RequirementClauses.md) clauses.

## Circumstance Types

There are several types of circumstances:

### Time Periods
Meant to restrict a clause to acting within a specific time period. For example:

```kotlin
alice may { see(data) } asLongAs {
    within { (1 of May year 2021)..(1 of June year 2023) }
}
```

...states that `alice` is _Allowed_ to`see` the `data`, under the circumstance of doing it within 01/05/2021 and 01/06/2023, inclusive.

#### Formats

There are several ways of writing time periods and dates in general. All the following are valid:

=== "Inline"
    ```kotlin
    alice may { see(data) } asLongAs {
        within { (1 of May year 2021)..(1 of June year 2023) }
    }
    ```

=== "Dates as variables"
    ```kotlin
    val startOfContract = 1 of May year 2021
    val endOfContract = 1 of June year 2023

    alice may { see(data) } asLongAs {
        within { startOfContract..endOfContract }
    }
    ```

=== "Dates within same year"
    ```kotlin
    alice may { see(data) } asLongAs {
        within { (1 of May)..(1 of June) year 2022 }
    }
    ```

### Past Actions

A past action is a circumstance where we declare something to have already happened in the past.
They are the best way of saying _"X if Y happened"_.

To write a past action circumstance, you require a [sentence](Declarations.md#sentences), which you obtain from declaring parties, actions, and things:

```kotlin
val alice by party
val see by action
val data by thing

val pay by action
val bob by party

alice may { see(data) } asLongAs {
    after { alice did pay(bob) }
}
```

### Purpose

Meant to restrict a clause to acting with a specific purpose. For example:

```kotlin
alice may { see(data) } asLongAs {
    with purpose Research
}
```

...states that `alice` is _Allowed_ to`see` the `data`, under the circumstance of having `Research` purposes.

If the circumstance is not `Research`, then the answer to 'May Alice see the data?' will be `Unspecified`. See [Clauses](PermissionClauses.md) for more details.


## Mixing Circumstances

To write specific cases, you can mix different circumstances in the same clause to create very specific scenarios.
For example:

```kotlin title="Alice may only see the data for the duration of the contract, after paying bob, and with Research purposes"
val alice by party
val see by action
val data by thing

val pay by action
val bob by party

val startOfContract = 1 of May year 2021
val endOfContract = 1 of June year 2023

alice may { see(data) } asLongAs {
    within { startOfContract..endOfContract }
    after { alice did pay(bob) }
    with purpose Research
}
```
