# Confis

**Confis** (In Spanish, slang for _trust_ and short for _confianzas_) is a Domain Specific
Language (DSL) that allows defining legal agreements in a computer-readable format.

This project is part of the requirements to complete my Master's degree in Computer Science at
Imperial College London and is currently a WIP. Ideally, it should enable:

- Contracts you can query without needing legal advice (_May I use this data for commercial
  purposes?_).
- Searchable contracts (_What are all the licenses relating to the commercial use of this dataset?_)
- Verifiable contracts

## The DSL

See `scriptHost/src/test/resources/scripts` for examples of legal agreements written in Confis.

Here is a simple one:

```kotlin
val Alice by party

val Bob by party

val data by thing

val distribute by action

val contract by thing

// the `additionally` modifier allows appending "purpose policies"
Alice may { distribute(data) } unless {
    with purpose Commercial
}

Bob mayNot { distribute(data) }

Alice may { terminate(contract) } asLongAs {
    with purpose Research
}

-"""
    The Licence and the terms and conditions thereof shall be governed and construed in
    accordance with the law of England and Wales.
"""

```

### Circumstances

Circumstances supported are

```kotlin
// for purpose
with purpose Commercial
with purpose Research

// within time range (always inclusive)
within { (10 of april)..(10 of july) year 2022 }
within { (10 of april year 2019)..(10 of july year 2022) }

// after a date (exclusive)
after { 2 of may year 2022 }

// before a date (exclusive)
before { 3 of may year 2024 }
```

## Implementation

Confis is implemented as a type-safe Kotlin DSL which has the following benefits

- Writing an invalid contract is impossible (disclaimer: writing an ambiguous one is!)
- IDE support is present - meaning that, when using IntelliJ IDEA, contract authors get
  autocompletion, warnings, etc.

## Thesis

The `report/` folder contains the thesis LaTeX document, which includes background reading, the
state of the art when it comes to legal agreement representation and verification, and success
criteria for the project, among other things.