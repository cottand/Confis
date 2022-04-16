# Circumstances

Circumstances allow narrowing down when exactly a
[clause](PermissionClauses.md) applies. There are several types of cirtcumstances:

## Purpose

Meant to restrict a clause to acting with a specific purpose. For example:

```kotlin
alice may { see(data) } asLongAs {
    with purpose Research
}
```

...states that `alice` is _Allowed_ to`see` the `data`, under the circumstance of having `Research` purposes.

If the circumstance is not `Research`, then the answer to 'May Alice see the data?' will be `Unspecified`. See [Clauses](PermissionClauses.md) for more details.

## Time Periods
Meant to restrict a clause to acting within a specific time period. For example:

```kotlin
alice may { see(data) } asLongAs {
    within { (1 of May year 2021)..(1 of June year 2023) }
}
```

...states that `alice` is _Allowed_ to`see` the `data`, under the circumstance of doing it within 01/05/2021 and 01/06/2023, inclusive.

### Formats

There are several ways of writing time periods and dates in general. All the following are valid:

Specifying dates in-place:
```kotlin
alice may { see(data) } asLongAs {
    within { (1 of May year 2021)..(1 of June year 2023) }
}
```

Specifying dates as variables:
```kotlin
val startOfContract = 1 of May year 2021
val endOfContract = 1 of June year 2023

alice may { see(data) } asLongAs {
    within { startOfContract..endOfContract }
}
```

Specifying dates within the same year:
```kotlin
alice may { see(data) } asLongAs {
    within { (1 of May)..(1 of June) year 2022 }
}
```
